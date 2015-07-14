package com.env.widget;

import java.util.HashMap;

import com.env.activity.ActivityTaskAction;
import com.env.utils.SystemMethodUtil;
import com.env.utils.ViewUtil;
import com.env.easypatrol.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ImgRecordTask extends LinearLayout{

	private Context context;
	private ImageView statu;
	private TextView taskName,taskTime,dataCount;
	private RelativeLayout rootView;
	private HashMap<String, String> task;
	private int request;
	
	public ImgRecordTask(Context context,HashMap<String, String> task){
		super(context);
		LayoutInflater.from(context).inflate(R.layout.img_record_task, this);
		this.context = context;
		this.task = task;
		ini();
	}
	
	
	public ImgRecordTask(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.img_record_task, this);
		this.context = context;
		ini();
	}
	
	
	private void ini(){
		statu = (ImageView)findViewById(R.id.img_take_task_logo);
		taskName = (TextView)findViewById(R.id.img_take_task_name);
		taskTime = (TextView)findViewById(R.id.img_take_task_time);
		rootView = (RelativeLayout)findViewById(R.id.img_take_task_root);
		dataCount = (TextView)findViewById(R.id.img_take_task_count);
	}
	public void hideTitle(){
		taskName.setVisibility(View.GONE);
	}
	
	public void setTaskStatu(int statuCode){
		switch (statuCode) {
		case ViewUtil.VIEW_STATU_CODE_DOING:
			statu.setBackgroundResource(R.drawable.task_doing);
			break;
		case ViewUtil.VIEW_STATU_CODE_DONE_PAST:
			statu.setBackgroundResource(R.drawable.task_done_past);
			break;
		case ViewUtil.VIEW_STATU_CODE_UNDO_PAST:
			statu.setBackgroundResource(R.drawable.task_undo_past);
			break;
		case ViewUtil.VIEW_STATU_CODE_UPLOADED:
			statu.setBackgroundResource(R.drawable.task_upload);
			break;
		case ViewUtil.VIEW_STATU_CODE_WAIT:
			statu.setBackgroundResource(R.drawable.task_wait);
			break;
		case ViewUtil.VIEW_STATU_CODE_DELAY:
			statu.setBackgroundResource(R.drawable.task_delay);
			break;
		}
	}
	
	public void setTimeColor(int statuCode){
		switch (statuCode) {
		case ViewUtil.VIEW_STATU_CODE_DOING:
			taskTime.setTextColor(getResources().getColor(R.color.lightgray));
			break;
		case ViewUtil.VIEW_STATU_CODE_DELAY:
			taskTime.setTextColor(getResources().getColor(R.color.orange));
			break;
		}
	}
	
	public void setName(String name) {
		taskName.setText(name);
	}
	public void setResult(String dataStr){
		if(dataStr.isEmpty()){
			dataCount.setText("0");
		}else {
			dataCount.setText(dataStr.split("\n").length+"");	
		}
			
	}
	
	public void setTime(String arg0){
		taskTime.setText(arg0);
	}
	public void setSingleClick(OnClickListener onClickListener,int pos){
		rootView.setClickable(true);
		if(pos%2==1){
			rootView.setBackgroundResource(R.drawable.single_click_middle_gray_stroke);
		}else {
			rootView.setBackgroundResource(R.drawable.single_click_middle_stroke);
		}
		rootView.setOnClickListener(onClickListener);
	}
	
	
	
	public void setClickEvent(int requestCode,int pos){
		request = requestCode;
		rootView.setClickable(true);
		if(pos%2==1){
			rootView.setBackgroundResource(R.drawable.single_click_middle_gray_stroke);
		}else {
			rootView.setBackgroundResource(R.drawable.single_click_middle_stroke);
		}
		rootView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(!SystemMethodUtil.isSDCardReady()){
					SystemMethodUtil.SDCardNotReady(context);
				}else {
					Intent intent = new Intent(context, ActivityTaskAction.class);
					intent.putExtra("task",task);
					intent.putExtra("requestcode", request);
					((Activity)context).startActivityForResult(intent, request);
				}
				
			}
		});
	}
	public void setLongClickEvent(){
		rootView.setClickable(true);
		rootView.setBackgroundResource(R.drawable.single_click_middle_stroke);
		rootView.setOnLongClickListener(new OnLongClickListener() {			
			@Override
			public boolean onLongClick(View v) {
				ViewUtil.getInstance().LongClickDialog(task, context).show();
				return true;
			}
		});
	}
}
