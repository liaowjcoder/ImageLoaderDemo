package com.zeal.imageloaderdemo;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.zeal.imageloaderdemo.utils.ImageLoader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zeal on 16/11/27.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private String mFilePath;
    private Context mContext;
    private List<String> mDatas;
    /**
     * 在这里设置静态的原因是在选择不同的文件，需要重新创建adapter，而选中的数据是需要共享的，因此使用static来描述
     */
    private static Set<String> mSelectedDatas = new HashSet<>();

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
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.mIvImage.setImageResource(R.drawable.pictures_no);
        holder.mIvImage.setColorFilter(null);
        final String filePath = mFilePath + "/" + mDatas.get(position);
        ImageLoader.getInstance(3, ImageLoader.Type.FILO).loadImage(filePath, holder.mIvImage);


        holder.mIvImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedDatas.contains(filePath)) {
                    holder.mIbCheck.setImageResource(R.drawable.picture_unselected);
                    mSelectedDatas.remove(filePath);
                    holder.mIvImage.setColorFilter(null);
                } else {
                    holder.mIbCheck.setImageResource(R.drawable.pictures_selected);
                    holder.mIvImage.setColorFilter(Color.parseColor("#44000000"));
                    mSelectedDatas.add(filePath);
                }
            }
        });
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