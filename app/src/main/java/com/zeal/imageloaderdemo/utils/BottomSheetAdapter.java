package com.zeal.imageloaderdemo.utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zeal.imageloaderdemo.R;
import com.zeal.imageloaderdemo.bean.FolderBean;

import java.util.List;

/**
 * Created by zeal on 16/11/28.
 */

public class BottomSheetAdapter extends RecyclerView.Adapter<BottomSheetAdapter.MyViewHolder> {

    private List<FolderBean> datas;
    private OnBottomSheetItemClickListener onBottomSheetItemListener;

    public BottomSheetAdapter(List<FolderBean> datas) {

        this.datas = datas;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = View.inflate(parent.getContext(), R.layout.item_dir, null);
        MyViewHolder holder = new MyViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.mTvDirName.setText(datas.get(position).getFolderName());
        holder.mTvDirPath.setText(datas.get(position).getFirstFilePath());
        holder.mTvFileCount.setText(datas.get(position).getCount());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onBottomSheetItemListener != null) {
                    onBottomSheetItemListener.click(datas.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (datas != null) {
            return datas.size();
        }
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mTvDirName;
        TextView mTvDirPath;
        TextView mTvFileCount;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTvDirName = (TextView) itemView.findViewById(R.id.dir_name);
            mTvDirPath = (TextView) itemView.findViewById(R.id.dir_path);
            mTvFileCount = (TextView) itemView.findViewById(R.id.file_count);
        }
    }

    public void setOnBottomSheetItemClickListener(OnBottomSheetItemClickListener onBottomSheetItemListener) {
        this.onBottomSheetItemListener = onBottomSheetItemListener;

    }

    public interface OnBottomSheetItemClickListener {
        void click(FolderBean folder);
    }
}
