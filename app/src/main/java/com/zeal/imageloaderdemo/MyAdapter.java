package com.zeal.imageloaderdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.zeal.imageloaderdemo.utils.ImageLoader;

import java.util.List;

/**
 * Created by zeal on 16/11/27.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private String mFilePath;
    private Context mContext;
    private List<String> mDatas;

    public MyAdapter(Context context, String filePath, List<String> datas) {
        this.mContext = context;
        this.mDatas = datas;
        this.mFilePath = filePath;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.img_item_view, null);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.mIvImage.setImageResource(R.drawable.pictures_no);
        ImageLoader.getInstance(3, ImageLoader.Type.FILO).loadImage(mFilePath + "/" + mDatas.get(position), holder.mIvImage);
    }

    @Override
    public int getItemCount() {
        if (mDatas != null && mDatas.size() > 0) {
            return mDatas.size();

        }
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView mIvImage;
        ImageButton mIbCheck;

        public MyViewHolder(View itemView) {
            super(itemView);

            mIvImage = (ImageView) itemView.findViewById(R.id.id_item_image);
            mIbCheck = (ImageButton) itemView.findViewById(R.id.id_item_image_check);
        }
    }
}