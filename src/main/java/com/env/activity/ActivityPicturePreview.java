package com.env.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.env.easypatrol.R;
import com.env.nfc.NfcActivity;
import com.env.utils.SystemMethodUtil;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by SK on 2015/7/7.
 */
public class ActivityPicturePreview extends NfcActivity {

    private List<HashMap<String,String>> files;
    private int index;
    private LinearLayout iconGroup;
    private ViewPager viewPager;
    private List<View> views;
    private PagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_picturepreview);
        iniData();
        iniView();

    }

    @Override
    public void iniData() {
        super.iniData();
        index = getIntent().getIntExtra("index",0);
        files = (ArrayList<HashMap<String,String>>)getIntent().getSerializableExtra("files");
        files.remove(files.size()-1);
        views = new ArrayList<View>();
    }

    @Override
    public void iniView() {
        super.iniView();
        iconGroup = (LinearLayout)findViewById(R.id.icongroup);
        viewPager = (ViewPager)findViewById(R.id.viewpager);



        for(int i =0;i<files.size();i++){
            iconGroup.addView(getPositionIcon(i));
            View view = getLayoutInflater().from(this).inflate(R.layout.item_picpre, null);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            ImageView img = (ImageView)view.findViewById(R.id.value);
            ViewGroup.LayoutParams lp = img.getLayoutParams();
            lp.height = getWindowManager().getDefaultDisplay().getHeight();
            lp.width = getWindowManager().getDefaultDisplay().getWidth();
            img.setLayoutParams(lp);
            img.setImageBitmap(SystemMethodUtil.compressBitmap(files.get(i).get("FilePath")));
         //   img.setImageURI(Uri.fromFile(new File(files.get(i).get("FilePath"))));
            view.setTag(i);
            views.add(view);
        }

        adapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return files.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public int getItemPosition(Object object) {
                return super.getItemPosition(object);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(views.get(position));
                return views.get(position);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(views.get(position));
            }
        };

        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < views.size(); i++) {
                    ImageView imageView = (ImageView) iconGroup.getChildAt(i);
                    if (i == position) imageView.setImageResource(R.drawable.page_now);
                    else imageView.setImageResource(R.drawable.page);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setCurrentItem(index);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private ImageView getPositionIcon (int position){
        ImageView imageView = new ImageView(this);
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(lp);
        imageView.setScaleType(ImageView.ScaleType.MATRIX);
        if(position == index){
            imageView.setImageResource(R.drawable.page_now);
        }else{
            imageView.setImageResource(R.drawable.page);
        }
        imageView.setTag(position);
        return imageView;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
