package com.zeal.imageloaderdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.zeal.imageloaderdemo.utils.ImageLoader;

import java.util.List;

import uk.co.senab.photoview.PhotoView;

/**
 * @作者 廖伟健
 * @创建时间 2016/11/29 16:39
 * @描述 ${TODO} 
 */

public class PreviewActivity extends AppCompatActivity {
    public static final String PREVIEW_FILES = "preview_files";
    private ViewPager mVp;

    private List<String> previewFiles;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_img_preview);

        mVp = (ViewPager) findViewById(R.id.preview_pager);

        previewFiles = getIntent().getStringArrayListExtra(PREVIEW_FILES);


        mVp.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                if (previewFiles != null) {
                    return previewFiles.size();
                }
                return 0;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {

                PhotoView photoView = new PhotoView(container.getContext());
                ImageLoader.getInstance().loadImage(previewFiles.get(position), photoView);

                // Now just add PhotoView to ViewPager and return it
                container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

//                return photoView;

//                ImageView previewImage = (ImageView) View.inflate(container.getContext(), R.layout.preview_item, null).findViewById(R.id.preview_img);
//
//                container.addView(previewImage);
//                PhotoViewAttacher mAttacher = new PhotoViewAttacher(previewImage);
//                mAttacher.update();
//                ImageLoader.getInstance().loadImage(previewFiles.get(position), previewImage);
                return photoView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });
    }
}
