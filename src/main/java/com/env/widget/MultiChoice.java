package com.env.widget;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.env.easypatrol.R;
import com.env.utils.SystemMethodUtil;
import com.env.utils.ViewUtil;

public class MultiChoice extends LinearLayout {
	private String[] options;
	private boolean[] optionOld,optionNow;
	private Context context;
	private String resultStr ="",dateTime="";
	private ArrayList<String> result;
	private int configid;
	private AlertDialog.Builder dialog = null;
	private HashMap<String, String> task;
	private ImageView statu;
	private TextView name,time,value;
	private RelativeLayout rootLayout;

	public MultiChoice(Context context,HashMap<String, String> task) {
		super(context);
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.multi_choice, this);
		this.task = task;
		ini();
		
	}

	public MultiChoice(Context context, AttributeSet attr) {
		super(context, attr);
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.multi_choice, this);
		ini();
	}

	public void ini(){
		statu = (ImageView) findViewById(R.id.multi_choice_logo);
		name = (TextView) findViewById(R.id.multi_choice_name);
		value = (TextView)findViewById(R.id.multi_choice_value);
		time = (TextView)findViewById(R.id.multi_choice_sttime);
		rootLayout = (RelativeLayout) findViewById(R.id.multi_choice_root);
	}
	
	public void hideTitle(){
		name.setVisibility(View.GONE);
	}
	
	
	public void setConfigID(int id){
		configid = id;
	}
	public int getConfigID(){
		return configid;
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
	}
	
	public void setTime(String arg0){
		time.setText(arg0);		
	}
	
	public void setOptions(String[] listOptions, boolean[] listOptionsStatu) {
		this.options = listOptions;
		if(listOptionsStatu == null) {
			listOptionsStatu = new boolean[listOptions.length];
		}
		this.optionOld = listOptionsStatu;
		optionNow = listOptionsStatu.clone();
	}

	public void setResult(String arg0){
		value.setText(arg0);
	}
	public ArrayList<String> getResult() {
		return result;
	}
	
	public String getResultStr(){
		return value.getText().toString();
	}

	public void setOPDateTime(String dateTime){
		this.dateTime = dateTime;
	}
	public String getOPDateTime(){
		return dateTime;
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
	
	public void setOldSelection(){
		if(getResultStr()!=null){
			for(int i=0;i<options.length;i++){
				if(getResultStr().contains(options[i]))optionOld[i]=true;
				else optionOld[i]=false;
			}
			optionNow = optionOld.clone();
		}
	}
	
	
	public void setClickEvent(boolean arg0,int pos) {	
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
					if(dialog==null){
						dialog = new AlertDialog.Builder(context);		
					}
					setOldSelection();
					dialog.setTitle(task.get("PatrolName")).setMultiChoiceItems(options,optionNow,new DialogInterface.OnMultiChoiceClickListener() {
						@Override
						public void onClick(DialogInterface dialog,int which, boolean isChecked) {
							optionNow[which] = isChecked;
						}
					}).setNegativeButton("确定",new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,int which) {
							resultStr = "";
							result = new ArrayList<String>();
							dialog.dismiss();
							for (int i = 0; i < optionOld.length; i++) {
								if (optionOld[i]) result.add(options[i]);																															
							}
							optionOld = optionNow.clone();
							for(int i=0;i<result.size();i++){
								if(i==(result.size()-1)){
									resultStr += result.get(i);
								}else {
									resultStr += result.get(i)+"\n";
								}	
							}
							value.setText(resultStr);
							SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
							dateTime = sdf.format(Calendar.getInstance().getTime());
						}
					}).setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							optionNow = optionOld.clone();
						}
					}).create().show();	
				}
			});
		}
	}
	public void setLongClickEvent(){
		rootLayout.setClickable(true);
		/*
		if(pos%2==1){
			rootLayout.setBackgroundResource(R.drawable.single_click_middle_gray);
		}else {
			rootLayout.setBackgroundResource(R.drawable.single_click_middle);
		}
		*/
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
