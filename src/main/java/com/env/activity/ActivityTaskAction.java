package com.env.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.env.nfc.NfcActivity;
import com.env.utils.SystemMethodUtil;
import com.env.utils.SystemParamsUtil;
import com.env.widget.AudioRecorder;
import com.env.widget.ImgRecorder;
import com.env.easypatrol.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityTaskAction extends NfcActivity implements OnClickListener{
	@SuppressWarnings("unused")
	private TextView name,describle,cancel,ok;
	private EditText data;
	private Intent intent;
	private HashMap<String, String> task;
	private int ResultType;
	private View view;
	private LinearLayout actionRoot;
	private AudioRecorder audioRecorder;
	private ImgRecorder imgRecorder;
	private ArrayList<File> files = null;
	private String endDateTime;
	private String exValue;
	private int requstCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.datainput_popup);
		SystemParamsUtil.getInstance().addActivity(this);
		intent = getIntent();
		task = (HashMap<String, String>)intent.getSerializableExtra("task");
		endDateTime = task.get("EndDateTime");
		exValue = task.get("Value");
		actionRoot = (LinearLayout)findViewById(R.id.task_action_root);
		name = (TextView)findViewById(R.id.tv_name);		
		cancel = (TextView)findViewById(R.id.bt_cancel);
		ok = (TextView)findViewById(R.id.bt_ok);
		name.setText(task.get("PatrolName"));
		ResultType = Integer.valueOf(task.get("ResultType")); 
		if(ResultType==0||ResultType==1){
			view = LayoutInflater.from(ActivityTaskAction.this).inflate(R.layout.data_input, null);			
		}
		if(ResultType==4){
			//图片
			requstCode = intent.getExtras().getInt("requestcode");
			imgRecorder = new ImgRecorder(ActivityTaskAction.this,requstCode);			
			imgRecorder.setPath("EnvEasyPatrol/imgRecord");			
			view = (View)imgRecorder;
			
		}
		if(ResultType==5){
			//语音
			requstCode = intent.getExtras().getInt("requestcode");
			audioRecorder = new AudioRecorder(ActivityTaskAction.this,requstCode);			
			audioRecorder.setPath("EnvEasyPatrol/voiceRecord");						
			view = (View)audioRecorder;
		}
		initialize();
		actionRoot.addView(view);
		ok.setOnClickListener(this);
		cancel.setOnClickListener(this);
		
	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(SystemParamsUtil.getInstance().getIsLogin()){
			
		}else{
			startActivity(new Intent(ActivityTaskAction.this,ActivityLogin.class));
			finish();
		}
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	private void initialize(){		
		if(ResultType==0){

			describle = (TextView)view.findViewById(R.id.tv_describle);
			data = (EditText)view.findViewById(R.id.et_data);
			data.setInputType(EditorInfo.TYPE_CLASS_PHONE);
			data.setText(task.get("Value"));	
			data.setSelection(task.get("Value").length());
			
			if(!task.get("Unit").isEmpty()){
				name.setText(task.get("PatrolName")+"("+task.get("Unit")+")");
			}else {
				name.setText(task.get("PatrolName"));
			}
			
//			String str="";
//			double min = CommonMethod.StrToDouble(task.get("MinValue"));
//			double max = CommonMethod.StrToDouble(task.get("MaxValue"));
			
//			if(min!=-1){
//				str += "最小值:"+min+task.get("Unit")+" \n";
//			}
//			if(max!=-1){
//				str += "最大值:"+max+task.get("Unit")+" \n"; 
//			}
//			if(task.get("Description")!=null&&!task.get("Description").equals("null")){
//				str = str + task.get("Description");
//			}
//			describle.setText(str);
			
		}else if (ResultType ==1) {
			describle = (TextView)view.findViewById(R.id.tv_describle);
			data = (EditText)view.findViewById(R.id.et_data);
			data.setInputType(EditorInfo.TYPE_CLASS_TEXT);
			data.setText(task.get("Value"));	
			data.setSelection(task.get("Value").length());

		}else {
			if(!task.get("Value").isEmpty()){
				String [] fileStrings = task.get("Value").split("\n");
				ArrayList<File> files = new ArrayList<File>();
				for(int i=0;i<fileStrings.length;i++){
					files.add(new File(fileStrings[i]));
				}
				if(ResultType==4){
					imgRecorder.setImgList(files);
				}else {
					audioRecorder.setFileList(files);
				}				
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		Intent postIntent = new Intent();
		int id = v.getId();
		if (id == R.id.bt_cancel) {
			postIntent.putExtra("task", task);
			if(ResultType==5){
				audioRecorder.OnDestroy(R.id.bt_cancel);
			}
			ActivityTaskAction.this.setResult(RESULT_CANCELED, postIntent);
			finish();
		} else if (id == R.id.bt_ok) {
			SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
			String time = sdf.format(Calendar.getInstance().getTime());
			if(Long.valueOf(time)<=Long.valueOf(endDateTime)){
				if(ResultType==1){//文本
					task.put("Value", data.getText().toString());
				}else if (ResultType==0) {//数字
					String tempStr =  data.getText().toString();
					if(tempStr.isEmpty()){
						Toast temp = Toast.makeText(ActivityTaskAction.this, "输入无效数据，请重新输入", Toast.LENGTH_SHORT);
						temp.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
						temp.show();
						return;
					}
					double tempDou = Double.MAX_VALUE;
					try {
						if(tempStr.isEmpty()){
							task.put("Value", "");
						}else {
							tempDou = Double.parseDouble(tempStr);
						}					
					} catch (NumberFormatException e) {
						e.printStackTrace();
						Toast temp = Toast.makeText(ActivityTaskAction.this, "输入无效数据，请重新输入", Toast.LENGTH_SHORT);
						temp.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
						temp.show();
						return;
					}
					double min = SystemMethodUtil.StrToDouble(task.get("MinValue"));
					double max = SystemMethodUtil.StrToDouble(task.get("MaxValue"));
					if(min!=-1&&tempDou<min){
						Toast temp = Toast.makeText(ActivityTaskAction.this, "输入数据低于下限", Toast.LENGTH_SHORT);
						temp.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
						temp.show();
						return;
					}
					if(max!=-1&&tempDou>max){
						Toast temp = Toast.makeText(ActivityTaskAction.this, "输入数据超过上限", Toast.LENGTH_SHORT);
						temp.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
						temp.show();
						return;
					}
					if(tempDou==Double.MAX_VALUE){
						task.put("Value", "");
					}else {
						task.put("Value", data.getText().toString());
					}			
				}else{
					if(ResultType==4){
						files = imgRecorder.getImgList();
					}else {
						audioRecorder.OnDestroy(R.id.bt_ok);
						files = audioRecorder.getFileList();					
					}
					String value = "";
					if(files!=null&&files.size()>0){
						for(int i =0;i<files.size();i++){
							if(i==files.size()-1){
								value = value+files.get(i).getAbsolutePath();
							}else {
								value = value+files.get(i).getAbsolutePath()+"\n";
							}
						}
					}else {
						value = "";
					}
					task.put("Value", value);
				}		
				task.put("SampleTime", time);
				postIntent.putExtra("task", task);
				ActivityTaskAction.this.setResult(RESULT_OK, postIntent);
				finish();
			}else {
				Toast temp = Toast.makeText(ActivityTaskAction.this, "任务已过期，无法保存结果", Toast.LENGTH_SHORT);
				temp.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
				temp.show();
				postIntent.putExtra("task", task);
				if(ResultType==5){
					audioRecorder.OnDestroy(R.id.bt_cancel);
				}
				ActivityTaskAction.this.setResult(RESULT_CANCELED, postIntent);
				finish();
			}
		}
		
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(ResultType == 4&&((ImgRecorder)view).IsImgShow()){
				((ImgRecorder)view).HideImaShow();	
				return true;
			}else {
				SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
				String time = sdf.format(Calendar.getInstance().getTime());
				if(Long.valueOf(time)<=Long.valueOf(endDateTime)){
					String result = "";
					if(ResultType==1){//文本
						result = data.getText().toString();
					}else if (ResultType==0) {//数字
						result =  data.getText().toString();
					}else{
						if(ResultType==4){
							files = imgRecorder.getImgList();
						}else {
							audioRecorder.OnDestroy(R.id.bt_ok);
							files = audioRecorder.getFileList();					
						}
						if(files!=null&&files.size()>0){
							for(int i =0;i<files.size();i++){
								if(i==files.size()-1){
									result = result+files.get(i).getAbsolutePath();
								}else {
									result = result+files.get(i).getAbsolutePath()+"\n";
								}
							}
						}else {
							result = "";
						}
					}
					if(exValue.equals(result)){
						onClick(cancel);
					}else {
						new AlertDialog.Builder(ActivityTaskAction.this).setMessage("保存您的巡检数据吗？").
						setTitle("系统消息").setCancelable(false)
						.setPositiveButton("保存", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ActivityTaskAction.this.onClick(ok);
							}
						}).setNegativeButton("撤销", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ActivityTaskAction.this.onClick(cancel);
							}
						}).create().show();
					}
				}else {
					Intent postIntent = new Intent();
					postIntent.putExtra("task", task);
					ActivityTaskAction.this.setResult(RESULT_CANCELED, postIntent);
					finish();
				}
				return true;
			}			
		}else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(ResultType==4){
			imgRecorder.onActivityResult(requestCode, resultCode, data);
		}
	}
}
