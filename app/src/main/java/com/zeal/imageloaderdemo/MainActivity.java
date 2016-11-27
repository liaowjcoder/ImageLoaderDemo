package com.zeal.imageloaderdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.zeal.imageloaderdemo.bean.FolderBean;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private TextView mTvFolderName;
    private TextView mTvImgCount;

    /**
     * 通过扫描得到的所有的文件夹
     */
    private List<FolderBean> mFolders;




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

    }

    private void initDatas() {
        //因为遍历手机的相册是比较耗时的操作，因此需要在子线程中去实现
        new Thread(){
            @Override
            public void run() {

            }
        }.start();

    }

    private void initLinsteners() {
    }
}
