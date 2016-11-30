package com.zeal.imageloaderdemo;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zeal.imageloaderdemo.bean.FolderBean;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private RecyclerView mRecyclerView;
    private TextView mTvFolderName;
    private TextView mTvImgCount;
    private ProgressDialog mLoadingProgressDilage;
    private BottomSheetDialog mBottomSheetDialog;
    private BottomSheetAdapter bottomSheetAdapter;
    private TextView mTvConfirm;
    /**
     * 通过扫描得到的所有的文件夹
     */
    private List<FolderBean> mFolders = new ArrayList<>();

    /**
     * 当前选中的文件夹
     */
    private File mCurSelectedFolder;
    /**
     * 记录当前文件夹得最大的数量
     */
    private int mMaxFileCount;

    /**
     * 当前文件夹下的所有文件
     */
    private List<String> mCurFiles;

    private List<String> mAllFils;

    private MyAdapter myAdapter;


    private static final int LOAD_FINISH = 0X001;

    private Handler mUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == LOAD_FINISH) {
                //扫描完成
                //隐藏加载圈
                mLoadingProgressDilage.dismiss();
                //将数据绑定到view中
                data2View();
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        initDatas();

        initLinsteners();
    }

    private void initViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.id_recyclerview);
        mTvFolderName = (TextView) findViewById(R.id.id_folder_name);
        mTvImgCount = (TextView) findViewById(R.id.id_image_count);

        mTvConfirm = (TextView) findViewById(R.id.id_comfirm);
    }

    private void initDatas() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "请检查SD卡是否被挂载", Toast.LENGTH_SHORT).show();
            return;
        }

        mLoadingProgressDilage = ProgressDialog.show(this, null, "正在加载中...");


        //因为遍历手机的相册是比较耗时的操作，因此需要在子线程中去实现
        new Thread() {
            @Override
            public void run() {

                //模式延时
                ContentResolver cr = MainActivity.this.getContentResolver();

                //获取我们需要扫描的路径：该路径处于外部存储器中
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Cursor cursor = cr.query(uri, null, MediaStore.Images.Media.MIME_TYPE + "= ? or " + MediaStore.Images.Media.MIME_TYPE + "= ?",
                        new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED
                );


                Set<String> mTempParentFileSet = new HashSet<String>();

                mAllFils = new ArrayList<String>();

                while (cursor.moveToNext()) {
                    //文件的路径名称
                    String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    if (TextUtils.isEmpty(filePath)) {
                        continue;
                    }

                    //文件的父级文件夹
                    File parentFile = new File(filePath).getParentFile();
                    if (parentFile == null || !parentFile.exists()) {
                        continue;
                    }

                    //判断当前文件的父级文件是否已经遍历过了
                    if (mTempParentFileSet.contains(parentFile.getAbsolutePath())) {
                        //若是当前文件的父级文件已经被其他同级目录遍历过，那么就不需要再次遍历了
                        continue;
                    }
                    //保存当前文件的父级文件路径
                    mTempParentFileSet.add(parentFile.getAbsolutePath());


                    //得到当前文件夹有多少符合条件的子文件夹
                    int fileCount = getFileCounts(parentFile);


                    FolderBean folderBean = new FolderBean();
                    folderBean.setCount(fileCount);
                    folderBean.setFirstFilePath(filePath);
                    folderBean.setFolderPath(parentFile.getAbsolutePath());

                    mFolders.add(folderBean);


                    if (fileCount > mMaxFileCount) {//得到最多少数量文件的文件夹
                        mMaxFileCount = fileCount;
                        mCurSelectedFolder = parentFile;
                    }

                }


                //游标使用完毕之后需要关闭
                cursor.close();


                //通知主线程Handler更新UI
                mUIHandler.sendEmptyMessage(LOAD_FINISH);

            }
        }.start();

    }

    private void initLinsteners() {
        mTvFolderName.setOnClickListener(this);
        mTvConfirm.setOnClickListener(this);
    }

    /**
     * 将数据绑定到view中
     */
    private void data2View() {


        //当前选中的默认是全部文件
        //        FolderBean folder = new FolderBean();
        //        folder.setCount(mAllFils.size());
        //        folder.setFolderName("全部文件");
        //        folder.setFirstFilePath(mAllFils.get(0));
        //        mFolders.add(0, folder);

        if (mCurSelectedFolder != null) {
            mCurFiles = Arrays.asList(mCurSelectedFolder.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    if (filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith("jpeg")
                            || filename.endsWith(".JPEG") || filename.endsWith(".PNG") || filename.endsWith(".JPG")) {
                        return checkFileAviable(dir + "/" + filename);
                    }
                    return false;
                }
            }));


            myAdapter = new MyAdapter(this, mCurSelectedFolder.getAbsolutePath(), mCurFiles, 6);
            mRecyclerView.setAdapter(myAdapter);
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

            myAdapter.setOnMyCheckItemClickListener(new MyAdapter.OnMyCheckItemClickListener() {
                @Override
                public void click(List<String> mSelectedDatas) {
                    if (mSelectedDatas.size() == 0) {
                        mTvConfirm.setText("完成");
                        mTvConfirm.setEnabled(false);
                    } else {
                        mTvConfirm.setEnabled(true);
                        mTvConfirm.setText("完成(" + mSelectedDatas.size() + "/6)");
                    }

                }
            });
            /**
             *
             */
            myAdapter.setOnMyImageItemClickListener(new MyAdapter.OnMyImageItemClickListener() {
                @Override
                public void click(String filePath) {
                    Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
                    ArrayList<String> previewFiles = new ArrayList<String>();
                    previewFiles.add(filePath);
                    intent.putStringArrayListExtra(PreviewActivity.PREVIEW_FILES, previewFiles);
                    startActivity(intent);
                }
            });

            mTvImgCount.setText(mCurFiles.size() + "张");
            mTvFolderName.setText(mCurSelectedFolder.getName());
        }
    }

    /**
     * 检测当前的filepath是否可以转化为一个bitmap
     * 不是以.png,.jpeg等结尾的就是可以转化为bitmap，有一些字节是为0的就是不可以的
     * 需要将这些进行过滤掉。
     *
     * @param filePath
     *
     * @return
     */
    private boolean checkFileAviable(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        if (options.outHeight > 0 && options.outWidth > 0) {
            return true;
        }
        return false;
    }

    /**
     * 得到该文件夹下有多少符合条件的文件
     *
     * @param parentFile 需要检测
     *
     * @return
     */
    private int getFileCounts(File parentFile) {
        int count = 0;
        int fileCount = parentFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith("jpeg")
                        || filename.endsWith(".JPEG") || filename.endsWith(".PNG") || filename.endsWith(".JPG")) {
                    if (checkFileAviable(dir.getAbsolutePath() + "/" + filename)) {
                        mAllFils.add(dir.getAbsolutePath() + "/" + filename);
                        return true;
                    }
                }
                return false;
            }
        }).length;

        if (fileCount > 0) {
            return fileCount;
        }
        return count;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_comfirm:
                Intent intent = new Intent(this, PreviewActivity.class);
                intent.putStringArrayListExtra(PreviewActivity.PREVIEW_FILES, (ArrayList<String>) MyAdapter.mSelectedDatas);
                startActivity(intent);
                break;
            case R.id.id_folder_name:
                if (mBottomSheetDialog == null) {
                    mBottomSheetDialog = new BottomSheetDialog(this);
                }
                View mBottomView = View.inflate(this, R.layout.bottom_sheet_layout, null);
                RecyclerView mBottomRv = (RecyclerView) mBottomView.findViewById(R.id.id_bottom_recycler);
                mBottomRv.setLayoutManager(new LinearLayoutManager(this));

                bottomSheetAdapter = new BottomSheetAdapter(mFolders);
                mBottomRv.setAdapter(bottomSheetAdapter);


                bottomSheetAdapter.setOnItemClickListener(new BottomSheetAdapter.MyOnItemClickListener() {
                    @Override
                    public void click(FolderBean folder) {
                        mCurSelectedFolder = new File(folder.getFolderPath());
                        mTvFolderName.setText(folder.getFolderName());
                        mTvImgCount.setText(folder.getCount() + "张");
                        mBottomSheetDialog.dismiss();
                        //查找mCurSelectedFolder下的文件
                        //重现创建adapter
                        data2View();
                    }
                });

                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(getResources().getDisplayMetrics().widthPixels, (int) (getResources().getDisplayMetrics().heightPixels * 2 * 1.0f / 3));
                mBottomSheetDialog.setContentView(mBottomView, lp);
                mBottomSheetDialog.show();

                break;

        }
    }
}
