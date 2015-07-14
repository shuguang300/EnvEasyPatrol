package com.env.utils;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.env.activity.ActivityEachTagWebView;
import com.env.activity.ActivityTaskConstruction;
import com.env.activity.ActivityTaskEachTag;
import com.env.easypatrol.R;
import com.env.widget.AudioRecordTask;
import com.env.widget.DataInput;
import com.env.widget.ImgRecordTask;
import com.env.widget.MultiChoice;
import com.env.widget.SingleChoice;

public class ViewUtil {
	
	public static final int VIEW_STATU_CODE_UPLOADED = 0;
	public static final int VIEW_STATU_CODE_WAIT = 1;
	public static final int VIEW_STATU_CODE_UNDO_PAST = 2;
	public static final int VIEW_STATU_CODE_DONE_PAST = 3;	
	public static final int VIEW_STATU_CODE_DOING = 4;
	public static final int VIEW_STATU_CODE_DELAY = 5;
	public static final int VIEW_DOING_TASK = 0;
	public static final int VIEW_HISTORY_TASK = 1;
	public static final int VIEW_NFC_TASK = 2;
	public static final String [] LONG_CLICK_DIALOG_ITEM = {"查看本地数据","查看远程数据"};
	private static ViewUtil INSTANCE = null;	
	private DataInput dataInputNum;
	private DataInput dataInputStr;
	private MultiChoice multiChoice ;
	private SingleChoice singleChoice;
	private ImgRecordTask imgRecordTask;
	private AudioRecordTask audioRecordTask;
	private AlertDialog.Builder longClickDialg = null;
	private Context context;
	private Intent intent;
	private ViewUtil(){
		
	}	
	public static ViewUtil getInstance(){
		if(INSTANCE==null){
			INSTANCE = new ViewUtil();
		}
		return INSTANCE;
	}
	
	public View getView(SQLiteDatabase db, HashMap<String, String> map,Context arg0,int mode,int pos){
		this.context = arg0;
		final String toastInfo = context.getResources().getString(R.string.toast_must);
		int ResultType = Integer.valueOf(map.get("ResultType"));
		int DicID;
		int StatuCode = Integer.valueOf(map.get("StatuCode"));
		boolean IsMustUseNFCCard = map.get("IsMustUseNFCCard").equals("1")?true:false;
		boolean clickMode = CanClick(IsMustUseNFCCard,mode);
		View view = null;
		switch (ResultType) {
		case 0://数字
			dataInputNum = new DataInput(context,map);
			dataInputNum.setName(map.get("PatrolName"));
			dataInputNum.setUnit(map.get("Unit")==null?"":map.get("Unit"));
			dataInputNum.setTime(map.get("Statu"));	
		//	dataInputNum.setTaskStatu(StatuCode);
			dataInputNum.setTimeColor(StatuCode);
			dataInputNum.setResult(map.get("Value"));
			if(clickMode){
				dataInputNum.setClickEvent(clickMode,pos);
			}else {
				dataInputNum.setSingleClick(new View.OnClickListener() {					
					@Override
					public void onClick(View v) {
						Toast.makeText(context, toastInfo, Toast.LENGTH_SHORT).show();							
					}
				});
			}			
			dataInputNum.setLongClickEvent();
			view = dataInputNum;
			break;
		case 1://文本
			dataInputStr = new DataInput(context,map);
			dataInputStr.setName(map.get("PatrolName"));
			dataInputStr.setUnit(map.get("Unit")==null?"":map.get("Unit"));
			dataInputStr.setTime(map.get("Statu"));	
		//	dataInputStr.setTaskStatu(StatuCode);
			dataInputStr.setTimeColor(StatuCode);
			dataInputStr.setResult(map.get("Value"));
			if(clickMode){
				dataInputStr.setClickEvent(clickMode,pos);
			}else {
				dataInputStr.setSingleClick(new View.OnClickListener() {					
					@Override
					public void onClick(View v) {
						Toast.makeText(context, toastInfo, Toast.LENGTH_SHORT).show();							
					}
				});
			}
			dataInputStr.setLongClickEvent();
			view = dataInputStr;
			break;
		case 2://多选			
			multiChoice = new MultiChoice(context,map);
			DicID = Integer.valueOf(map.get("DicID"));
			multiChoice.setName(map.get("PatrolName"));
			multiChoice.setTime(map.get("Statu"));	
	//		multiChoice.setOptions(LocalDataHelper.getTaskTextValues(db, DicID),null);
			multiChoice.setResult(map.get("Value"));
			multiChoice.setOPDateTime(map.get("SampleTime"));
	//		multiChoice.setTaskStatu(StatuCode);	
			multiChoice.setTimeColor(StatuCode);
			if(clickMode){
				multiChoice.setClickEvent(clickMode,pos);
			}else {
				multiChoice.setSingleClick(new View.OnClickListener() {					
					@Override
					public void onClick(View v) {
						Toast.makeText(context, toastInfo, Toast.LENGTH_SHORT).show();							
					}
				});
			}
			multiChoice.setLongClickEvent();
			view = multiChoice;						
			break;
		case 3://单选
			singleChoice = new SingleChoice(context,map);
			DicID = Integer.valueOf(map.get("DicID"));
			singleChoice.setName(map.get("PatrolName"));
	//		singleChoice.setOptions(LocalDataHelper.getTaskTextValues(db, DicID));
			singleChoice.setResult(map.get("Value"));
			singleChoice.setOPDateTime(map.get("SampleTime"));
			singleChoice.setTime(map.get("Statu"));
	//		singleChoice.setTaskStatu(StatuCode);
			singleChoice.setTimeColor(StatuCode);
			if(clickMode){
				singleChoice.setClickEvent(clickMode,pos);
			}else {
				singleChoice.setSingleClick(new View.OnClickListener() {					
					@Override
					public void onClick(View v) {
						Toast.makeText(context, toastInfo, Toast.LENGTH_SHORT).show();							
					}
				});
			}
			singleChoice.setLongClickEvent();
			view = singleChoice;			
			break;
		case 4://图片
			imgRecordTask = new ImgRecordTask(context,map);
			imgRecordTask.setName(map.get("PatrolName"));
			imgRecordTask.setTime(map.get("Statu"));	
			imgRecordTask.setResult(map.get("Value"));
	//		imgRecordTask.setTaskStatu(StatuCode);
			imgRecordTask.setTimeColor(StatuCode);
			if(IsMustUseNFCCard){
				if(mode==ActivityTaskConstruction.CARD_NFC){
					imgRecordTask.setClickEvent(VIEW_DOING_TASK,pos);
				}else {
					imgRecordTask.setClickEvent(VIEW_NFC_TASK,pos);
				}				
			}else {
				imgRecordTask.setClickEvent(VIEW_DOING_TASK,pos);
			}				
			imgRecordTask.setLongClickEvent();
			view = imgRecordTask;
			break;
		case 5://录音
			audioRecordTask = new AudioRecordTask(context,map);
			audioRecordTask.setName(map.get("PatrolName"));
			audioRecordTask.setTime(map.get("Statu"));	
			audioRecordTask.setResult(map.get("Value"));
	//		audioRecordTask.setTaskStatu(StatuCode);
			audioRecordTask.setTimeColor(StatuCode);
			if(IsMustUseNFCCard){
				if(mode==ActivityTaskConstruction.CARD_NFC){
					audioRecordTask.setClickEvent(VIEW_DOING_TASK,pos);	
				}else {
					audioRecordTask.setClickEvent(VIEW_NFC_TASK,pos);	
				}				
			}else {
				audioRecordTask.setClickEvent(VIEW_DOING_TASK,pos);	
			}				
			audioRecordTask.setLongClickEvent();
			view = audioRecordTask;
			break;
			
		}
		view.setTag(map);
		return view;
	}
	
	public AlertDialog LongClickDialog(HashMap<String, String> map,Context context){
		this.context = context;
		intent = new Intent();
		intent.putExtra("task",map);
		longClickDialg = new AlertDialog.Builder(context);
		longClickDialg.setCancelable(true);		
		longClickDialg.setSingleChoiceItems(LONG_CLICK_DIALOG_ITEM, -1, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				switch (which) {
					case 0:
						intent.setClass(ViewUtil.this.context, ActivityTaskEachTag.class);
						((Activity) ViewUtil.this.context).startActivityForResult(intent, VIEW_HISTORY_TASK);
						break;
					case 1:
						intent.setClass(ViewUtil.this.context, ActivityEachTagWebView.class);
						((Activity) ViewUtil.this.context).startActivityForResult(intent, VIEW_HISTORY_TASK);
						break;
				}
			}
		});		
		return longClickDialg.create();
	}
	
	private boolean CanClick(boolean isMustUseNfc,int mode){
		boolean arg0;
		if(isMustUseNfc){
			if(mode==ActivityTaskConstruction.CARD_NFC){
				arg0 = true;
			}else {
				arg0 = false;
			}				
		}else {
			arg0 = true;
		}
		return arg0;
	}
	
	public String getTimeDescribe(String startDateTime){
		Calendar nowCl = Calendar.getInstance();
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
		String today = sdf.format(nowCl.getTime());
		if(today.substring(0, 8).equals(startDateTime.substring(0, 8))){
			sb.append("今天");
		}else if (Integer.valueOf(today.substring(0, 8))-1==Integer.valueOf(startDateTime.substring(0, 8))) {
			sb.append("昨天");
		}else {
			sb.append(today.substring(4, 6)).append("月").append(today.substring(6, 8)).append("日");
		}
		sb.append(startDateTime.substring(8, 10)).append(":").append(startDateTime.substring(10, 12));
		return sb.toString();
	}
	
	public static RelativeLayout getContentItemView(LayoutInflater layoutInflater,String name,boolean val){
		RelativeLayout relativeLayout = (RelativeLayout)layoutInflater.inflate(R.layout.item_task_content, null);
		TextView textView = (TextView)relativeLayout.findViewById(R.id.name);
		CheckBox checkBox = (CheckBox)relativeLayout.findViewById(R.id.value);
		textView.setText(name);
		checkBox.setChecked(val);
		return relativeLayout;
	}

	public static RelativeLayout getContentItemViewSinlgeChoice(LayoutInflater layoutInflater,String name,boolean val){
		RelativeLayout relativeLayout = (RelativeLayout)layoutInflater.inflate(R.layout.item_task_contentsinglechoice, null);
		TextView textView = (TextView)relativeLayout.findViewById(R.id.name);
		RadioButton radioButton = (RadioButton)relativeLayout.findViewById(R.id.value);
		textView.setText(name);
		radioButton.setChecked(val);
		return relativeLayout;
	}


	public static RelativeLayout getStepItemView(LayoutInflater layoutInflater,String name,boolean val){
		RelativeLayout relativeLayout = (RelativeLayout)layoutInflater.inflate(R.layout.item_task_step, null);
		TextView textView = (TextView)relativeLayout.findViewById(R.id.name);
		CheckBox checkBox = (CheckBox)relativeLayout.findViewById(R.id.value);
		textView.setText(name);
		checkBox.setChecked(val);
		return relativeLayout;
	}
}
