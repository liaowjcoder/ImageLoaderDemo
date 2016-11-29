package com.zeal.imageloaderdemo;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.zeal.imageloaderdemo.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeal on 16/11/27.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private String mFilePath;
    private Context mContext;
    private List<String> mDatas;
    private int mMaxSelectImgs = Integer.MAX_VALUE;
    /**
     * 在这里设置静态的原因是在选择不同的文件，需要重新创建adapter，而选中的数据是需要共享的，因此使用static来描述
     */
    public static List<String> mSelectedDatas = new ArrayList<>();
    private OnMyCheckItemClickListener onMyCheckItemClickListener;
    private OnMyImageItemClickListener onMyImageItemClickListener;

    public MyAdapter(Context context, String filePath, List<String> datas) {
        this.mContext = context;
        this.mDatas = datas;
        this.mFilePath = filePath;
    }

    public MyAdapter(Context context, String filePath, List<String> datas, int maxSelectImgs) {
        this(context, filePath, datas);
        this.mMaxSelectImgs = maxSelectImgs;
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
        ImageLoader.getInstance().loadImage(filePath, holder.mIvImage);


        if (mSelectedDatas.contains(filePath)) {
            holder.mIbCheck.setImageResource(R.drawable.icon_checked);
            holder.mIvImage.setColorFilter(Color.parseColor("#44000000"));
        } else {
            holder.mIbCheck.setImageResource(R.drawable.icon_pic_check);
            holder.mIvImage.setColorFilter(null);
        }

        holder.mIvImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (onMyImageItemClickListener != null) {
                    onMyImageItemClickListener.click(filePath);
                }
            }
        });

        holder.mIbCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedDatas.contains(filePath)) {
                    holder.mIbCheck.setImageResource(R.drawable.icon_pic_check);
                    mSelectedDatas.remove(filePath);
                    holder.mIvImage.setColorFilter(null);
                } else {
                    //判断当前选中的数量是否超过了mMaxSelectImgs值
                    if (mSelectedDatas.size() >= mMaxSelectImgs) {
                        Toast.makeText(mContext, "所选中图片不能超过" + mMaxSelectImgs + "张", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    holder.mIbCheck.setImageResource(R.drawable.icon_checked);
                    holder.mIvImage.setColorFilter(Color.parseColor("#44000000"));
                    mSelectedDatas.add(filePath);
                }
                if (onMyCheckItemClickListener != null) {
                    onMyCheckItemClickListener.click(mSelectedDatas);
                }
            }
        });
    }

    public interface OnMyCheckItemClickListener {
        void click(List<String> mSelectedDatas);
    }

    public void setOnMyCheckItemClickListener(OnMyCheckItemClickListener listener) {
        this.onMyCheckItemClickListener = listener;
    }

    public interface OnMyImageItemClickListener {
        void click(String filePath);
    }

    public void setOnMyImageItemClickListener(OnMyImageItemClickListener listener) {
        this.onMyImageItemClickListener = listener;
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