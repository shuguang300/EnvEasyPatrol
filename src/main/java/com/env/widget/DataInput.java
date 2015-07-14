package com.env.widget;
import java.util.HashMap;

import com.env.activity.ActivityTaskAction;
import com.env.easypatrol.R;
import com.env.utils.ViewUtil;

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

public class DataInput extends LinearLayout {
	public static final int NUMBER = 0;
	public static final int STRING = 1;
	private int configid;
	private Context context;
	private String result="";
	private String dateTime;
	private RelativeLayout rootLayout;
	private TextView data,taskName,dataUnit,time;
	private HashMap<String, String> task;
	private ImageView statu;
	
	public DataInput(Context context,HashMap<String, String> task) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.date_input, this);
		this.task = task;
		this.context =context;	
		ini();
	}
	
	public DataInput(Context context, AttributeSet attributeSet) {
		super(context,attributeSet);
		LayoutInflater.from(context).inflate(R.layout.date_input, this);
		this.context = context;
		ini();
	}
	
	public void ini(){
		data = (TextView)findViewById(R.id.data_input_data);
		taskName = (TextView) findViewById(R.id.data_input_name);
		dataUnit = (TextView) findViewById(R.id.data_input_unit);
		time = (TextView)findViewById(R.id.data_input_time);
		rootLayout = (RelativeLayout) findViewById(R.id.data_input_root);
		statu = (ImageView)findViewById(R.id.data_input_logo);
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
			time.setTextColor(getResources().getColor(R.color.lightgray));
			break;
		case ViewUtil.VIEW_STATU_CODE_DELAY:
			time.setTextColor(getResources().getColor(R.color.orange));
			break;
		}
	}
	
	public void setConfigID(int id){
		configid = id;
	}
	public int getConfigID(){
		return configid;
	}
	public void setName(String name) {
		taskName.setText(name);
	}
	public void setResult(String dataStr){
		data.setText(dataStr);
		result = dataStr;
	}
	public String getResult(){
		return result;
	}
	public void setUnit(String unit) {
		dataUnit.setText(unit);
	}
	
	public void setTime(String arg0){
		time.setText(arg0);
	}
	
	public String getOPDateTime(){
		return dateTime;
	}
	public void setOPDateTime(String optime){
		dateTime = optime;
	}
	public void setSingleClick(OnClickListener onClickListener,int pos){
		rootLayout.setClickable(true);
		if(pos%2==1){
			rootLayout.setBackgroundResource(R.drawable.single_click_middle_gray_stroke);
		}else {
			rootLayout.setBackgroundResource(R.drawable.single_click_middle_stroke);
		}
		rootLayout.setOnClickListener(onClickListener);
	}
	
	public void setSingleClick(OnClickListener onClickListener){
		rootLayout.setClickable(true);
		rootLayout.setOnClickListener(onClickListener);
	}

	public void setClickEvent(boolean arg0,int pos){
		rootLayout.setClickable(true);
		if(pos%2==1){
			rootLayout.setBackgroundResource(R.drawable.single_click_middle_gray_stroke);
		}else {
			rootLayout.setBackgroundResource(R.drawable.single_click_middle_stroke);
		}
		if(arg0){
			rootLayout.setOnClickListener(new OnClickListener() {			
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, ActivityTaskAction.class);
					intent.putExtra("task",task);
					((Activity)context).startActivityForResult(intent, 0);
				}
			});
		}
		
	}
	public void setLongClickEvent(){
		rootLayout.setClickable(true);
		rootLayout.setBackgroundResource(R.drawable.single_click_middle_stroke);
		rootLayout.setOnLongClickListener(new OnLongClickListener() {			
			@Override
			public boolean onLongClick(View v) {
				ViewUtil.getInstance().LongClickDialog(task, context).show();
				return true;
			}
		});
	}
}
