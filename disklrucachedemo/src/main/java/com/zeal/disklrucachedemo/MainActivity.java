package com.zeal.disklrucachedemo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * DiskLruCache的使用
 */
public class MainActivity extends AppCompatActivity {
    private Button disk_cache;
    private ImageView disk_image;

    private final static String TAG = "zeal";
    private DiskLruCache mDiskLruCache;
    private Button disk_read;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Log.e(TAG, "getExternalCacheDir():" + getExternalCacheDir().getAbsolutePath());//得到外置sd卡该应用下cache目录
        //Log.e(TAG, "getExternalCacheDir():" + getFilesDir().getAbsolutePath());//得到的是内部存储该应用下的files目录
        //Log.e(TAG, "getCacheDir():" + getCacheDir().getAbsolutePath());//得到的是内部存储该应用下的cache目录
        //.../storage/emulated/0/Android/data/com.zeal.disklrucachedemo/cache
        //.../data/data/com.zeal.disklrucachedemo/files
        //.../data/data/com.zeal.disklrucachedemo/cache

        disk_cache = (Button) findViewById(R.id.disk_cache);
        disk_image = (ImageView) findViewById(R.id.id_image);
        disk_read = (Button) findViewById(R.id.disk_read);


        //获取文件缓存的路径
        File cacehFile = getCacheFilePath(MainActivity.this, "bitmap");
        int appVersion = getAppVersion(MainActivity.this);
        try {
            mDiskLruCache = DiskLruCache.open(cacehFile, appVersion, 1, 10 * 1024 * 1024);
        } catch (Exception e) {
            e.printStackTrace();
        }

        disk_cache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            String url = "http://img.my.csdn.net/uploads/201309/01/1378037235_7476.jpg";
                            //为了避免特殊符号的原因不能保存文件名，因此将key进行md5加密
                            String key = hashKeyForDisk(url);
                            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                            if (editor != null) {
                                OutputStream outputStream = editor.newOutputStream(0);//得到可以写的输出流
                                if (startDownloadImage(outputStream, url)) {
                                    editor.commit();
                                } else {
                                    editor.abort();
                                }
                            }
                            mDiskLruCache.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
        });

        disk_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String url = "http://img.my.csdn.net/uploads/201309/01/1378037235_7476.jpg";
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(hashKeyForDisk(url));
                    if (snapshot != null) {
                        InputStream is = snapshot.getInputStream(0);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        disk_image.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void remove(View view) {
        try {
            String url = "http://img.my.csdn.net/uploads/201309/01/1378037235_7476.jpg";
            //这个方法我们并不应该经常去调用它。因为你完全不需要担心缓存的数据过多从而占用SD卡太多空间的问题，
            // DiskLruCache会根据我们在调用open()方法时设定的缓存最大值来自动删除多余的缓存。
            // 只有你确定某个key对应的缓存内容已经过期，需要从网络获取最新数据的时候才应该调用remove()方法来移除缓存。
            boolean result = mDiskLruCache.remove(hashKeyForDisk(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 得到app版本号
     *
     * @param context
     *
     * @return
     */
    private int getAppVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 根据sd卡状态获取缓存的绝对路径
     *
     * @param context
     *
     * @return
     */
    private File getCacheFilePath(Context context, String uniqueName) {
        String cachePath = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ||
                !Environment.isExternalStorageRemovable()//sd卡是否能被移除，有些sd卡是可以拔的，有些是内置的
                ) {
            cachePath = context.getExternalCacheDir().getPath();//返回应用的缓存目录，return null表示还没有被sd卡挂载
        } else {
            cachePath = getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    private boolean startDownloadImage(final OutputStream os, final String path) {

        HttpURLConnection connection = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            URL url = new URL(path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5 * 1000);
            connection.setReadTimeout(5 * 1000);
            connection.setDoOutput(true);
            connection.connect();
            bis = new BufferedInputStream(connection.getInputStream());
            bos = new BufferedOutputStream(os);
            //开始读写操作
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = bis.read(bytes, 0, bytes.length)) != -1) {
                bos.write(bytes, 0, len);
            }
            //Log.e(TAG, "startDownloadImage: ");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //Log.e(TAG, "finally: ");
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
