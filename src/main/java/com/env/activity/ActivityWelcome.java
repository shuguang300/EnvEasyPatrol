package com.env.activity;

import com.env.adapter.EnvPagerAdapter;
import com.env.nfc.NfcActivity;
import com.env.utils.SystemParamsUtil;
import com.env.easypatrol.R;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.content.Intent;
import android.view.View.OnClickListener;

public class ActivityWelcome extends NfcActivity {

	private ViewPager mViewPager;
	//private PagerTitleStrip mPagerTitleStrip;
	
	//根据图片数量增减
//	private ImageView mPage0;
//	private ImageView mPage1;
//	private ImageView mPage2;
//	private ImageView mPage3;
//	private ImageView mPage4;
//	private ImageView mPage5;

	
	private Button startButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.welcome);
        mViewPager = (ViewPager)findViewById(R.id.whatsnew_viewpager);
        
        
        
//        mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());        
//        mPage0 = (ImageView)findViewById(R.id.page0);
//        mPage1 = (ImageView)findViewById(R.id.page1);
//        mPage2 = (ImageView)findViewById(R.id.page2);
//        mPage3 = (ImageView)findViewById(R.id.page3);
//        mPage4 = (ImageView)findViewById(R.id.page4);
//        mPage5 = (ImageView)findViewById(R.id.page5);

               
        //这里是每一页要显示的布局，根据应用需要和特点自由设计显示的内容以及需要显示多少页等  
        LayoutInflater mLi = LayoutInflater.from(this);
//        View view1 = mLi.inflate(R.item_imageview.whats_news_gallery_one, null);
//        View view2 = mLi.inflate(R.item_imageview.whats_news_gallery_two, null);
//        View view3 = mLi.inflate(R.item_imageview.whats_news_gallery_three, null);
//        View view4 = mLi.inflate(R.item_imageview.whats_news_gallery_four, null);
//        View view5 = mLi.inflate(R.item_imageview.whats_news_gallery_five, null);
        View view6 = mLi.inflate(R.layout.whats_news_gallery_six, null);
        
      	
        //这里将每一页显示的view存放到ArrayList集合中可以在ViewPager适配器中顺序调用展示
        final ArrayList<View> views = new ArrayList<View>();
//        views.add(view1);
//        views.add(view2);
//        views.add(view3);
//        views.add(view4);
//        views.add(view5);
        views.add(view6);   

        
        
        //展示标题
        final ArrayList<String> titles = new ArrayList<String>();
//        titles.add("tab1");
//        titles.add("tab2");
//        titles.add("tab3");
//        titles.add("tab4");
//        titles.add("tab5");
        titles.add("tab6");

        
        //填充ViewPager的数据适配器
        EnvPagerAdapter mPagerAdapter = new EnvPagerAdapter(views,titles){
        	public Object instantiateItem(View container, int position) {
        		if (position == views.size()-1){
        			startButton = (Button) views.get(position).findViewById(R.id.whats_new_start_btn);
        	        startButton.setOnClickListener(new OnClickListener() {      				
        				@Override
        				public void onClick(View v) {
        					Intent loginIntent = new Intent();
        					if(!SystemParamsUtil.getInstance().getIsLogin()){
        						loginIntent.setClass(ActivityWelcome.this, ActivityLogin.class);
        					}else {
        						loginIntent.setClass(ActivityWelcome.this, ActivityTaskConstruction.class);
        						loginIntent.putExtra("NeedTips", true);
							}					
        					startActivity(loginIntent);
        					ActivityWelcome.this.finish();
        				}
        			});
        		}
        		((ViewPager)container).addView(views.get(position));
        		return views.get(position);
        	}
        };
		mViewPager.setAdapter(mPagerAdapter);
    }
	/*
    public class MyOnPageChangeListener implements OnPageChangeListener {
    	
    	
		public void onPageSelected(int page) {
			
			//翻页时当前page,改变下面当前对应的圆点状态
			switch (page) {
			case 0:				
				mPage0.setImageDrawable(getResources().getDrawable(R.drawable.page_now));
				mPage1.setImageDrawable(getResources().getDrawable(R.drawable.page));
				break;
			case 1:
				mPage1.setImageDrawable(getResources().getDrawable(R.drawable.page_now));
				mPage0.setImageDrawable(getResources().getDrawable(R.drawable.page));
				mPage2.setImageDrawable(getResources().getDrawable(R.drawable.page));
				break;
			case 2:
				mPage2.setImageDrawable(getResources().getDrawable(R.drawable.page_now));
				mPage1.setImageDrawable(getResources().getDrawable(R.drawable.page));
				mPage3.setImageDrawable(getResources().getDrawable(R.drawable.page));
				break;
			case 3:
				mPage3.setImageDrawable(getResources().getDrawable(R.drawable.page_now));
				mPage4.setImageDrawable(getResources().getDrawable(R.drawable.page));
				mPage2.setImageDrawable(getResources().getDrawable(R.drawable.page));
				break;
			case 4:
				mPage4.setImageDrawable(getResources().getDrawable(R.drawable.page_now));
				mPage3.setImageDrawable(getResources().getDrawable(R.drawable.page));
				mPage5.setImageDrawable(getResources().getDrawable(R.drawable.page));
				break;
			case 5:
				mPage5.setImageDrawable(getResources().getDrawable(R.drawable.page_now));
				mPage4.setImageDrawable(getResources().getDrawable(R.drawable.page));
				break;
				
			}
		}
		
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		
		public void onPageScrollStateChanged(int arg0) {
			
	        
		}
	}
*/
    //响应回退
    @Override 
    public void onBackPressed() {   
    	ActivityWelcome.this.finish();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
    }
}
