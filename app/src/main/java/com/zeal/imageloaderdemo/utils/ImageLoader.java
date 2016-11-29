package com.zeal.imageloaderdemo.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @作者 廖伟健
 * @创建时间 2016/11/24 10:02
 * @描述 ${TODO}
 */

public class ImageLoader {


    private static ImageLoader mInstance;


    /**
     * 缓存的核心
     */
    private LruCache<String, Bitmap> mLruCache;

    /**
     * 处理后台轮训任务
     */
    private Handler mBgHandler;
    /**
     * 后台轮训线程
     */
    private Thread mBgThread;

    /**
     * 负责ui更新的handler
     */
    private Handler mUIHandler;

    /**
     * 任务队列
     */
    private LinkedList<Runnable> mQueue;

    /**
     * 执行后台任务的线程池
     */
    private ThreadPoolExecutor mThreadPoolExecutor;
    /**
     * 默认的线程池数量
     */
    private static int THREAD_POOL_COUNT = 1;
    private Semaphore mSemaphoreBgHandler = new Semaphore(0);
    private Semaphore mQueueSemaphore;

    /**
     * 任务的加载策略
     */
    private Type mType = Type.FILO;

    private ImageLoader(int threadCount, Type type) {

        //获取可用内存
        int maxSize = (int) Runtime.getRuntime().maxMemory();
        mLruCache = new LruCache<String, Bitmap>(maxSize / 8) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //计算Bitmap的大小
                return value.getRowBytes() * value.getHeight();
            }
        };
        mBgThread = new Thread() {
            @Override
            public void run() {
                //轮训线程
                Looper.prepare();
                //mBgHandler的作用就相当于在子线程中while(true)，但是handler-looper可以实现这种机制，因此选择使用handler去实现
                mBgHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        //请求一个信号量
                        //假设threadCount = 3；
                        //若是当前进入的是第四个线程的话，去请求一个信号量，发现当前信号量没有了，那么就阻塞，知道信号量被释放
                        try {
                            mQueueSemaphore.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //通知线程池去执行这个任务
                        mThreadPoolExecutor.execute(getTask());
                    }
                };

                //释放信号量
                mSemaphoreBgHandler.release();
                Looper.loop();
            }
        };
        mBgThread.start();

        //任务队列
        mQueue = new LinkedList<>();
        //线程池
        mThreadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);

        mType = type == null ? Type.FILO : type;

        mQueueSemaphore = new Semaphore(threadCount);
    }

    public static ImageLoader getInstance(int threadCount, Type type) {
        if (mInstance == null) {//初次调用时，mInstance为null，这时候可能多个进程并发进来。这里判空时，减少每次对锁的判断
            synchronized (ImageLoader.class) {//加锁，保证只有一个线程能进来，这时候可能还有多个其他的线程等待着
                if (mInstance == null) {//第一个进来的mInstance为null，然后实例化对象。之后排队进来的判断mInstance不为null，那么就不会再重新初始化了
                    mInstance = new ImageLoader(threadCount, type);
                }
            }
        }
        return mInstance;
    }

    public static ImageLoader getInstance() {
        if (mInstance == null) {//初次调用时，mInstance为null，这时候可能多个进程并发进来。这里判空时，减少每次对锁的判断
            synchronized (ImageLoader.class) {//加锁，保证只有一个线程能进来，这时候可能还有多个其他的线程等待着
                if (mInstance == null) {//第一个进来的mInstance为null，然后实例化对象。之后排队进来的判断mInstance不为null，那么就不会再重新初始化了
                    mInstance = new ImageLoader(THREAD_POOL_COUNT, Type.FILO);
                }
            }
        }
        return mInstance;
    }

    /**
     * 根据指定的路径转化为Bitmap显示在ImageView中
     *
     * @param path
     * @param imageView
     */
    public void loadImage(final String path, final ImageView imageView) {
        //给imageView绑定path，避免错位问题
        imageView.setTag(path);

        //ui线程上的Handler，专门负责去处理ui的更新操作
        if (mUIHandler == null) {
            mUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    ImageHolder imageHolder = (ImageHolder) msg.obj;

                    ImageView imageView = imageHolder.imageView;
                    Bitmap bitmap = imageHolder.bitmap;
                    String path = imageHolder.path;

                    if (path.equals(imageView.getTag())) {//只有于绑定的路径一致时才可以更新UI，不然会出现错位问题
                        //第一屏：1 2 3 4 5
                        //第二屏：6 7 8 9 10
                        //1和6是共用一个ImageView，但是对应的path不一致
                        //1设置的是path1->loadImage(imageview1).setTag(path1) -> 缓存，加载 -> uiHandler更新ui
                        //6设置的是path6->loadImage(imageview1).setTage(path6)-> 缓存，加载 -> uiHandler更新ui imageholder.path = path1,tag= path6
                        imageView.setImageBitmap(bitmap);
                    }
                }
            };
        }

        Bitmap bitmap = mLruCache.get(path);
        if (bitmap != null) {
            refreshImageView(bitmap, imageView, path);
        } else {
            //没有在缓存中找到
            //往任务队列中添加任务 --  将path去加载出一个bitmap对象
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    //计算imageview的大小
                    ImageSize imageSize = getImageViewSize(imageView);

                    //得到缩放后的bitmap
                    Bitmap bitmap = decodeFileFromResouce(path, imageSize.width, imageSize.height);

                    if (bitmap != null && !TextUtils.isEmpty(path)) {
                        //将加载出来的bitmap添加到内存缓存中
                        addBitmapToLruCache(path, bitmap);

                        //刷新imageview
                        refreshImageView(bitmap, imageView, path);
                    }
                    //任务执行完毕，释放信号量--当前任务执行完毕了
                    mQueueSemaphore.release();
                }
            };
            addTask(task);
        }
    }

    /**
     * 添加一个任务到任务队列中
     *
     * @param task
     */
    private synchronized void addTask(Runnable task) {

        //请求一个信号量
        if (mBgHandler == null) {
            try {
                // 请求信号量，防止mPoolThreadHander为null
                mSemaphoreBgHandler.acquire();//请求一个信号量，没有请求到，则阻塞
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mQueue.add(task);
        mBgHandler.sendEmptyMessage(0);
    }

    /**
     * 刷新imageview的显示
     *
     * @param bitmap
     * @param imageView
     * @param path
     */

    private void refreshImageView(Bitmap bitmap, ImageView imageView, String path) {
        Message msg = Message.obtain();
        //将其封装到ImageHodler中
        ImageHolder imageHolder = new ImageHolder();
        imageHolder.bitmap = bitmap;
        imageHolder.imageView = imageView;
        imageHolder.path = path;
        msg.obj = imageHolder;
        mUIHandler.sendMessage(msg);
    }

    /**
     * 根据图片的加载策略去获取任务
     *
     * @return 返回一个任务
     */
    private synchronized Runnable getTask() {
        if (mType == Type.FIFO) {
            return mQueue.removeFirst();
        } else if (mType == Type.FILO) {
            return mQueue.removeLast();
        }
        return null;
    }

    /**
     * 解码出一个bitmap
     *
     * @param path
     * @param reqWidth
     * @param reqHeight
     *
     * @return 返回缩放后的Bitmap
     */
    private Bitmap decodeFileFromResouce(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        if (options.outHeight <= 0 || options.outWidth <= 0) {
            return null;
        }
        //计算采样率
        int inSamepleSize = 1;
        inSamepleSize = calculateInsamepleSize(options, reqWidth, reqHeight);


        //将图片真正的加载到内存中去
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSamepleSize;
        Bitmap bm = BitmapFactory.decodeFile(path, options);
        if (bm == null) {
            Log.e("zeal", "decodeFileFromResouce:");
        }
        return bm;
    }

    /**
     * 计算采样率
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     *
     * @return
     */
    private int calculateInsamepleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int inSamepleSize = 1;
        //得到实际的图片的大小
        int width = options.outWidth;
        int height = options.outHeight;
        if (width > reqWidth || height > reqHeight) {
            int widthRadio = Math.round((float) width / (float) reqWidth);
            int heightRadio = Math.round((float) height / (float) reqHeight);

            inSamepleSize = Math.max(widthRadio, heightRadio);
        }
        return inSamepleSize;
    }

    /**
     * 计算imageview的大小
     *
     * @param imageView
     *
     * @return
     */
    private ImageSize getImageViewSize(ImageView imageView) {
        ImageSize imageSize = new ImageSize();

        //计算width
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();

        DisplayMetrics displayMetrics = imageView.getResources().getDisplayMetrics();

        int width = imageView.getWidth();
        if (width <= 0) {
            width = lp.width;
        }
        //        if (width <= 0) {
        //            width = getImageViewFieldValue(imageView, "mMaxWidth");
        //        }
        if (width <= 0) {
            width = displayMetrics.widthPixels;
        }
        //计算height
        int height = imageView.getHeight();
        if (height <= 0) {
            height = lp.height;
        }
        //        if (height <= 0) {
        //            height = getImageViewFieldValue(imageView, "mMaxHeight");
        //        }
        if (height <= 0) {
            height = displayMetrics.heightPixels;
        }
        imageSize.height = height;
        imageSize.width = width;
        return imageSize;
    }

    /**
     * 通过反射获取指定属性的值
     *
     * @param imageView
     * @param fieldName
     *
     * @return
     */
    private int getImageViewFieldValue(ImageView imageView, String fieldName) {
        int value = 0;
        try {
            Field field = imageView.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = field.getInt(imageView);
            if (fieldValue < Integer.MAX_VALUE && fieldValue > 0) {
                value = fieldValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }


    /**
     * 添加bm添加到LruCache中
     *
     * @param path
     * @param
     */
    private void addBitmapToLruCache(String path, Bitmap bitmap) {
        if (mLruCache.get(path) != null) {
            return;
        }
        if (bitmap == null) {
            Log.e("zeal", "addBitmapToLruCache: " + path);
        }
        mLruCache.put(path, bitmap);
    }

    private class ImageHolder {
        ImageView imageView;
        Bitmap bitmap;
        String path;
    }

    /**
     * 图片的加载策略
     */
    public enum Type {
        FIFO, FILO;
    }

    private class ImageSize {
        int height;
        int width;
    }


}
