package com.env.widget;

import com.env.activity.ActivityTaskEachTag;
import com.env.utils.SystemMethodUtil;
import com.env.utils.ViewUtil;
import com.env.easypatrol.R;

import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
@SuppressWarnings("unused")
public class SingleChoice extends LinearLayout {
	private Context context;
	private RelativeLayout rootLayout;
	private TextView name,value,time;
	private String result="",title,dateTime="";
	private String[] options;
	private int configid,selectID = -1;
	private AlertDialog.Builder dialog = null;
	private HashMap<String, String> task;
	private ImageView statu;

	public SingleChoice(Context context,HashMap<String, String> task) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.single_choice, this);		
		this.context = context;
		this.task = task;
		ini();
	}

	public SingleChoice(Context context, AttributeSet attr) {
		super(context, attr);
		LayoutInflater.from(context).inflate(R.layout.single_choice, this);
		this.context = context;
		ini();
		/*
		 * TypedArray a =
		 * context.obtainStyledAttributes(attr,R.styleable.singlechoiceview);
		 * setLabelText(a.getString(R.styleable.singlechoiceview_labelText));
		 * setValue(a.getString(R.styleable.singlechoiceview_valueText));
		 * a.recycle();
		 */
	}
	public void setConfigID(int id){
		configid = id;
	}
	public int getConfigID(){
		return configid;
	}

	public void ini(){
		name = (TextView) findViewById(R.id.single_choice_name);
		value =(TextView) findViewById(R.id.single_choice_value);
		time = (TextView)findViewById(R.id.single_choice_sttime);
		statu = (ImageView)findViewById(R.id.single_choice_logo);
		rootLayout = (RelativeLayout) findViewById(R.id.single_choice_root);
	}
	public void hideTitle(){
		name.setVisibility(View.GONE);
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
	
	public void setName(String arg0) {		
		name.setText(arg0);
		title = arg0;
	}


	public void setOptions(String[] options) {
		this.options = options;
	}

	public void setResult(String arg0){
		value.setText(arg0);
		result = arg0;
	}
	
	public void setTime(String arg0){
		time.setText(arg0);
	}
	
	public void setOPDateTime(String dateTime){
		this.dateTime = dateTime;
	}
	
	public String getOPDateTime(){
		return dateTime;
	}
	
	public String getResult() {
		return value.getText().toString();
	}
		
	public String getName(){
		return name.getText().toString();
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
	
	public void setClickEvent(boolean arg0,int pos) {		
		rootLayout.setClickable(true);
		if(pos%2==1){
			rootLayout.setBackgroundResource(R.drawable.single_click_middle_gray_stroke);
		}else {
			rootLayout.setBackgroundResource(R.drawable.single_click_middle_stroke);
		}
		if(arg0){
			for(int i = 0;i<options.length;i++){
				if(result.equals(options[i])){
					selectID = i;
				}
			}
			rootLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(dialog == null){
						dialog = new AlertDialog.Builder(context);						
					}
					dialog.setTitle(task.get("PatrolName")).setSingleChoiceItems(options, selectID,new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,int which) {
							selectID = which;
							dialog.dismiss();
							result = options[which];
							value.setText(result);
							SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
							dateTime = sdf.format(Calendar.getInstance().getTime());
						}
					}).create().show();					
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
