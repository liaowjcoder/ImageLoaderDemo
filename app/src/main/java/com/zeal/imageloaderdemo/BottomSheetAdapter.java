package com.zeal.imageloaderdemo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zeal.imageloaderdemo.bean.FolderBean;
import com.zeal.imageloaderdemo.utils.ImageLoader;

import java.util.List;

/**
 * @作者 廖伟健
 * @创建时间 2016/11/29 9:26
 * @描述 BottomSheetDialog展示的内容
 */
public class BottomSheetAdapter extends RecyclerView.Adapter<BottomSheetAdapter.MyViewHolder> {
    private List<FolderBean> folders;
    private MyOnItemClickListener myOnItemClickListener;

    public BottomSheetAdapter(List<FolderBean> folders) {
        this.folders = folders;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = View.inflate(parent.getContext(), R.layout.dir_item_layout, null);
        MyViewHolder myViewHolder = new MyViewHolder(itemView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.tvFileCount.setText(folders.get(position).getCount() + "张");
        holder.tvFolderName.setText(folders.get(position).getFolderName());
        ImageLoader.getInstance().loadImage(folders.get(position).getFirstFilePath(), holder.ivFisrtImg);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myOnItemClickListener != null) {
                    myOnItemClickListener.click(folders.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (folders != null) {
            return folders.size() ;
        }
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFolderName;
        private TextView tvFileCount;
        private ImageView ivFisrtImg;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvFolderName = (TextView) itemView.findViewById(R.id.tv_folder_name);
            ivFisrtImg = (ImageView) itemView.findViewById(R.id.iv_first_img);
            tvFileCount = (TextView) itemView.findViewById(R.id.tv_file_count);
        }
    }

    public void setOnItemClickListener(MyOnItemClickListener onItemClickListener) {
        this.myOnItemClickListener = onItemClickListener;
    }

    public interface MyOnItemClickListener {
        void click(FolderBean folder);
    }
}
