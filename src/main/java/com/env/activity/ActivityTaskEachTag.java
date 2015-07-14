package com.env.activity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.env.easypatrol.R;
import com.env.nfc.NfcActivity;
import com.env.utils.DataBaseUtil;
import com.env.utils.LocalDataHelper;
import com.env.utils.SystemMethodUtil;
import com.env.utils.SystemParamsUtil;
import com.env.utils.ViewUtil;
import com.env.widget.AudioRecordTask;
import com.env.widget.DataInput;
import com.env.widget.ImgRecordTask;
import com.env.widget.MultiChoice;
import com.env.widget.SingleChoice;

public class ActivityTaskEachTag extends NfcActivity implements OnClickListener,OnMenuItemClickListener{
	private DataInput dataInputNum;
	private DataInput dataInputStr;
	private MultiChoice multiChoice ;
	private SingleChoice singleChoice;
	private ImgRecordTask imgRecordTask;
	private AudioRecordTask audioRecordTask;
	private Intent receivedIntent;
	private HashMap<String, String> task;
	private ArrayList<HashMap<String, String>> tasks;
	private ListView taskList;
	private SQLiteDatabase db = null;
	private int ResultType,statuCode,dialogID,opsPos=0;
	private TaskListAdapter adapter;
	private View view = null;
	private Context context;
	private PlanInfoDialog adb;
	private TextView txtStartDate,txtStopDate,tagNameTV,tagPlanTV;
	private CustomDatePick datePickerDialog;
	private Calendar startCL,stopCL,temCL;
	private TextView titleOptions,titleBack;
	private Button searchBtn;
	private PopupMenu popupMenu;
	private String startDT,stopDT;
	private ProgressDialog progressDialog = null;
	private boolean isDialogShow =false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.patroltask_each_tag);
		SystemParamsUtil.getInstance().addActivity(this);
		init();
	}
	
	@SuppressWarnings("unchecked")
	public void init(){
		receivedIntent  = getIntent();
		context = ActivityTaskEachTag.this;
		db = DataBaseUtil.getInstance(ActivityTaskEachTag.this).getReadableDatabase();	
		
		task = (HashMap<String, String>)receivedIntent.getSerializableExtra("task");
		
		startCL = Calendar.getInstance();
		stopCL = Calendar.getInstance();
		temCL = Calendar.getInstance();
		
		txtStartDate =  (TextView)findViewById(R.id.tv_start_date);
		txtStopDate = (TextView)findViewById(R.id.tv_stop_date);
		txtStartDate.setOnClickListener(this);
		txtStopDate.setOnClickListener(this);
		SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.StandardDateSdf);
		txtStartDate.setText(sdf.format(startCL.getTime()));
		txtStopDate.setText(sdf.format(stopCL.getTime()));
		
		tagNameTV = (TextView)findViewById(R.id.eachtag_tagname);
		taskList = (ListView)findViewById(R.id.eachtag_tasklistview);
		titleOptions = (TextView)findViewById(R.id.eachtag_options);
		tagPlanTV = (TextView)findViewById(R.id.eachcard_planinfo);
		titleBack = (TextView)findViewById(R.id.eachtag_back);
		searchBtn = (Button)findViewById(R.id.eachtag_search);
		if(task.get("Unit")==null){
			tagNameTV.setText(task.get("PatrolName"));
		}else {
			tagNameTV.setText(task.get("PatrolName")+"("+task.get("Unit")+")");
		}		
		ResultType = Integer.valueOf(task.get("ResultType"));
		
		titleOptions.setOnClickListener(this);
		titleBack.setOnClickListener(this);
		searchBtn.setOnClickListener(this);	
		tagPlanTV.setOnClickListener(this);
		
		task.put("PlanName", LocalDataHelper.getPlanNameByTag(db, Integer.valueOf(task.get("PatrolTagID"))));
		tagPlanTV.setText(task.get("PlanName"));
		SimpleDateFormat sdf1 = new SimpleDateFormat(SystemMethodUtil.ShortDateSdf);
		startDT = sdf1.format(startCL.getTime())+"000000";
		stopDT = sdf1.format(stopCL.getTime())+"235959";
		setData(startDT,stopDT,null);
		adapter = new TaskListAdapter();
		taskList.setAdapter(adapter);
		
		
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
			startActivity(new Intent(ActivityTaskEachTag.this,ActivityLogin.class));
			finish();
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
	}
	private void changeDataType(){
		if(popupMenu ==null){
			popupMenu = new PopupMenu(context, titleOptions);
			popupMenu.inflate(R.menu.patroleachtag_popupmenu);
			popupMenu.setOnMenuItemClickListener(this);
		}
		for(int i =0;i<4;i++){
			if(i==opsPos){
				popupMenu.getMenu().getItem(i).setChecked(true);
			}else {
				popupMenu.getMenu().getItem(i).setChecked(false);
			}
		}
		popupMenu.show();
	}
	
	private void searchData(){
		if(startCL.getTime().getTime()>stopCL.getTime().getTime()){
			Toast.makeText(context, "开始日期不能大于结束日期", Toast.LENGTH_LONG).show();
		}else {
			if(progressDialog == null){
				progressDialog = new ProgressDialog(ActivityTaskEachTag.this);
				progressDialog.setTitle("请稍候");
				progressDialog.setMessage("正在为您准备数据......");
				progressDialog.setCancelable(false);
			}
			progressDialog.show();
			SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.ShortDateSdf);
			startDT = sdf.format(startCL.getTime())+"000000";
			stopDT = sdf.format(stopCL.getTime())+"235959";
			StringBuilder condition = new StringBuilder();
			switch (opsPos) {
			case 0:					
				break;
			case 1:
				condition.append("and IsDone = 1");
				break;
			case 2:
				condition.append("and IsDone = 0");
				break;
			case 3:
				condition.append("and IsUpload = 0");
				break;
			}
			setData(startDT,stopDT,condition);
			new Handler().postDelayed(new Runnable() {				
				@Override
				public void run() {
					adapter.notifyDataSetChanged();
					if(progressDialog!=null){
						progressDialog.dismiss();
					}						
				}
			}, 1000);
		}
	}
	private void showCalendar(){
		datePickerDialog = new CustomDatePick(context, new DatePickerDialog.OnDateSetListener() {					
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
				temCL.set(Calendar.YEAR, year);
				temCL.set(Calendar.MONTH, monthOfYear);
				temCL.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.StandardDateSdf);
				if (dialogID == R.id.tv_start_date) {
					startCL.setTime(temCL.getTime());
					txtStartDate.setText(sdf.format(startCL.getTime()));
				} else if (dialogID == R.id.tv_stop_date) {
					stopCL.setTime(temCL.getTime());
					txtStopDate.setText(sdf.format(stopCL.getTime()));
				}
			}
		}, temCL.get(Calendar.YEAR), temCL.get(Calendar.MONTH), temCL.get(Calendar.DAY_OF_MONTH));
		datePickerDialog.show();	
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.tv_start_date) {
			dialogID = R.id.tv_start_date;
			temCL.setTime(startCL.getTime());
			showCalendar();
		} else if (id == R.id.tv_stop_date) {
			dialogID  = R.id.tv_stop_date;
			temCL.setTime(stopCL.getTime());
			showCalendar();
		} else if (id == R.id.eachtag_back) {
			onBackPressed();
		} else if (id == R.id.eachtag_search) {
			searchData();
		} else if (id == R.id.eachtag_options) {
			changeDataType();
		} else if (id == R.id.eachcard_planinfo) {
			if(adb==null){
				adb = new PlanInfoDialog(ActivityTaskEachTag.this,R.style.PlanInfoDialog,task.get("Description"));
			}
			if(isDialogShow){
				adb.dismiss();
				isDialogShow = false;
			}else {
				adb.show();
				isDialogShow = true;
			}
		}
			
	}
	
	public void setData(String startTime,String stopTime,StringBuilder condition){
		SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
		long todayInt = Long.valueOf(sdf.format(Calendar.getInstance().getTime()));
		tasks = LocalDataHelper.getHistoryTaskByTag(db, Integer.valueOf(task.get("PatrolTagID")), startTime, stopTime,condition);		
		if(tasks!=null){	
			for (HashMap<String, String> tmp : tasks) {
				tmp.put("Value", "");
				tmp.put("IsEnable", "1");
				tmp.put("Visible", "1");
				tmp.put("StatuCode", ViewUtil.VIEW_STATU_CODE_DOING + "");
				if(tmp.get("IsDone").equals("1")){
					tmp.put("SampleTime",tmp.get("OPDateTime"));
					tmp.put("IsEnable", "0");
					tmp.put("Value", LocalDataHelper.getTaskValuesAndSteps(db,tmp.get("ResultType") == "0" ? "EP_PatrolResult_Number" : "EP_PatrolResult_String", Integer.valueOf(tmp.get("TaskID")))[0]);
				}
				tmp.put("StatuCode", getStatuCode(todayInt, tmp) + "");
			}
		}				
	}
	
	private int getStatuCode(long todayNum,HashMap<String, String> task){
		boolean candelay = task.get("EnableDelay").equals("1")?true:false;
		int statuCode = ViewUtil.VIEW_STATU_CODE_DOING;
		if(task.get("IsUpload").equals("1")){
			statuCode = ViewUtil.VIEW_STATU_CODE_UPLOADED;
			task.put("IsEnable", "0");
		}else {
			if(task.get("IsDone").equals("1")){
				if(todayNum<Long.valueOf(task.get("StopDateTime"))&&todayNum>=Long.valueOf(task.get("StartDateTime"))){
					statuCode = ViewUtil.VIEW_STATU_CODE_DOING;
					task.put("IsEnable", "1");
				}else if(todayNum>=Long.valueOf(task.get("StopDateTime"))){
					if(candelay){
						if(todayNum>=Long.valueOf(task.get("StopDateTime"))&&todayNum<Long.valueOf(task.get("EndDateTime"))){
							statuCode = ViewUtil.VIEW_STATU_CODE_DELAY;
							task.put("IsEnable", "1");
						}else {
							statuCode = ViewUtil.VIEW_STATU_CODE_DONE_PAST;
							task.put("IsEnable", "0");
						};						
					}else {
						statuCode = ViewUtil.VIEW_STATU_CODE_DONE_PAST;
						task.put("IsEnable", "0");
					}
					
				}
			}else {
				if(todayNum<Long.valueOf(task.get("StopDateTime"))&&todayNum>=Long.valueOf(task.get("StartDateTime"))){
					statuCode = ViewUtil.VIEW_STATU_CODE_DOING;
					task.put("IsEnable", "1");
				}else if (todayNum>=Long.valueOf(task.get("StopDateTime"))) {
					if(candelay){
						if(todayNum>=Long.valueOf(task.get("StopDateTime"))&&todayNum<Long.valueOf(task.get("EndDateTime"))){
							statuCode = ViewUtil.VIEW_STATU_CODE_DELAY;
							task.put("IsEnable", "1");
						}else {
							statuCode = ViewUtil.VIEW_STATU_CODE_UNDO_PAST;
							task.put("IsEnable", "0");
						};						
					}else {
						statuCode = ViewUtil.VIEW_STATU_CODE_UNDO_PAST;
						task.put("IsEnable", "0");
					}
				}else if (todayNum<Long.valueOf(task.get("StartDateTime"))) {
					statuCode = ViewUtil.VIEW_STATU_CODE_WAIT;
					task.put("IsEnable", "0");
				}
			}
		}		
		return statuCode;
	}
	
	class CustomDatePick extends DatePickerDialog{
		public CustomDatePick(Context context,OnDateSetListener callBack, int year, int monthOfYear,int dayOfMonth) {
			super(context, callBack, year, monthOfYear, dayOfMonth);
			setButton(DatePickerDialog.BUTTON_POSITIVE, "确定", this);
		}		
	}
			
	class TaskListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return tasks.size();
		}

		@Override
		public Object getItem(int position) {			
			return tasks.get(position);
		}

		@Override
		public long getItemId(int position) {
			return Long.valueOf(tasks.get(position).get("TaskID"));
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			HashMap<String, String> map = tasks.get(position);
			statuCode = Integer.valueOf(map.get("StatuCode"));
			context = ActivityTaskEachTag.this;
			StringBuilder temp = new StringBuilder();
			temp.append(SystemMethodUtil.toJsonDateByStr(map.get("StartDateTime"))+"\n");
			temp.append(SystemMethodUtil.toJsonDateByStr(map.get("EndDateTime")));
//			switch (statuCode) {
//			case ViewHelper.VIEW_STATU_CODE_DOING:
//				taskStatu = "正在执行";
//				break;
//			case ViewHelper.VIEW_STATU_CODE_DONE_PAST:
//				taskStatu = "已完成";
//				break;
//			case ViewHelper.VIEW_STATU_CODE_UNDO_PAST:
//				taskStatu = "已关闭";
//				break; 
//			case ViewHelper.VIEW_STATU_CODE_UPLOADED:
//				taskStatu = "已上传";
//				break;
//			case ViewHelper.VIEW_STATU_CODE_WAIT:
//				taskStatu = "待执行";
//				break;
//			case ViewHelper.VIEW_STATU_CODE_DELAY:
//				taskStatu = "已延迟";
//				break;
//			}
			switch (ResultType) {
			case 0://数字
				dataInputNum = new DataInput(context,map);
			//	dataInputNum.setName(taskStatu);
				dataInputNum.hideTitle();
				dataInputNum.setTaskStatu(statuCode);
				dataInputNum.setResult(map.get("Value"));
				dataInputNum.setTime(temp.toString());
				dataInputNum.setClickEvent(false, 0);
				view = dataInputNum;
				break;
			case 1://文本
				dataInputStr = new DataInput(context,map);
			//	dataInputStr.setName(taskStatu);
				dataInputStr.hideTitle();
				dataInputStr.setResult(map.get("Value"));
				dataInputStr.setTaskStatu(statuCode);
				dataInputStr.setTime(temp.toString());
				dataInputStr.setClickEvent(false, 0);
				view = dataInputStr;
				break;
			case 2://多选			
				multiChoice = new MultiChoice(context,map);
			//	multiChoice.setName(taskStatu);
				multiChoice.hideTitle();
				multiChoice.setResult(map.get("Value"));
				multiChoice.setTaskStatu(statuCode);
				multiChoice.setTime(temp.toString());
				multiChoice.setClickEvent(false, 0);
				view = multiChoice;						
				break;
			case 3://单选
				singleChoice = new SingleChoice(context,map);	
			//	singleChoice.setName(taskStatu);
				singleChoice.hideTitle();
				singleChoice.setResult(map.get("Value"));
				singleChoice.setTaskStatu(statuCode);
				singleChoice.setTime(temp.toString());
				singleChoice.setClickEvent(false, 0);
				view = singleChoice;			
				break;
			case 4://图片
				imgRecordTask = new ImgRecordTask(context,map);
			//	imgRecordTask.setName(taskStatu);
				imgRecordTask.hideTitle();
				imgRecordTask.setResult(map.get("Value"));
				imgRecordTask.setTaskStatu(statuCode);
				imgRecordTask.setTime(temp.toString());
				imgRecordTask.setClickEvent(ViewUtil.VIEW_HISTORY_TASK,0);
				view = imgRecordTask;
				break;
			case 5://录音
				audioRecordTask = new AudioRecordTask(context,map);
			//	audioRecordTask.setName(taskStatu);
				audioRecordTask.hideTitle();
				audioRecordTask.setResult(map.get("Value"));
				audioRecordTask.setTaskStatu(statuCode);
				audioRecordTask.setTime(temp.toString());
				audioRecordTask.setClickEvent(ViewUtil.VIEW_HISTORY_TASK,0);
				view = audioRecordTask;
				break;				
			}
			return view;
		}		
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.taskeachtag_menu_all) {
			opsPos = 0;
		} else if (itemId == R.id.taskeachtag_menu_done) {
			opsPos = 1;
		} else if (itemId == R.id.taskeachtag_menu_undo) {
			opsPos = 2;
		} else if (itemId == R.id.taskeachtag_menu_unupload) {
			opsPos = 3;
		}
		return true;
	}
	
	class PlanInfoDialog extends Dialog{
		private String mContent;
		private LinearLayout layout;
		private String [] mContentArray;
		private Context mContext;
		private ImageView close;
		public PlanInfoDialog (Context context,String content){
			super(context);
			mContent = content;
			mContext = context;
		}
		public PlanInfoDialog (Context context,int theme,String content){
			super(context, theme);
			mContent = content;
			mContext = context;
		}
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			this.setContentView(R.layout.planinfodialog);
			close = (ImageView)findViewById(R.id.planinfodialog_close);
			layout = (LinearLayout)findViewById(R.id.planinfodialog_content);
			layout.removeAllViews();
			mContentArray = mContent.split("\n");
			for(int i =0;i<mContentArray.length;i++){
				TextView textView = new TextView(mContext);
				textView.setSingleLine(false);
				textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				textView.setPadding(5, 5, 5, 5);
				textView.setText(mContentArray[i]);
				TextView textView2 = new TextView(mContext);
				textView2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 2));
				textView2.setBackgroundResource(R.drawable.content_divider);
				layout.addView(textView);
				layout.addView(textView2);
			}
			close.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					isDialogShow = false;
					cancel();
				}
			});
			this.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					isDialogShow = false;
				}
			});
		}
	}
}

