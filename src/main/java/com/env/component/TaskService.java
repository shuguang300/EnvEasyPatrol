package com.env.component;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.env.bean.EP_Application;
import com.env.bean.EP_User;
import com.env.bean.RequestResult;
import com.env.utils.DataBaseUtil;
import com.env.utils.HttpUtil;
import com.env.utils.LocalDataHelper;
import com.env.utils.NotificationUtil;
import com.env.utils.RemoteDataHelper;
import com.env.utils.SystemMethodUtil;
import com.env.utils.SystemParamsUtil;
import com.google.gson.Gson;

public class TaskService extends Service{
	private SQLiteDatabase db;
	private PowerManager powerManager;
	private ActivityManager activityManager;
	private Intent keepDataService,dataService;
	private List<RunningServiceInfo> serviceInfos;
	private NotificationManager nfm;
	private Notification nf;
	private WakeLock wakeLock;	
	private Timer secondCount,checkVersion;
	private ArrayList<String> taskStartDateTime;
	private TaskServiceHandle taskServiceHandle;
	private Thread checkAuditThread;
	private SharedPreferences sp;
	private SharedPreferences.Editor editor;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		keepDataService = new Intent();
		keepDataService.setAction("com.env.component.TaskService");
		keepDataService.setPackage(getPackageName());
		
		
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
		editor = sp.edit();
		if(!isServiceRuning()){
			dataService = new Intent();
			dataService.setAction("com.env.component.DataService");
			startService(dataService);
		}
		taskServiceHandle = new TaskServiceHandle();
		setStartDateTimeArray();
		NotificationUtil.getInstance().updateStayNotification(TaskService.this);
		if(secondCount == null){
			secondCount = new Timer("secondCount",true);
			secondCount.schedule(new TimerTask() {				
				@Override
				public void run() {
					if(taskStartDateTime != null && taskStartDateTime.size() > 0){
						for(int i = 0;i<taskStartDateTime.size();i++){
							long count = getTimeRemind(taskStartDateTime.get(i));
							if(count<0){
								taskStartDateTime.remove(i);
							}else if(count==0) {
								Message msg = taskServiceHandle.obtainMessage();
								msg.what = 0;
								msg.obj = taskStartDateTime.get(i);
								msg.sendToTarget();
							}
						}
					}
				}
			},0,1000);			
		}
		if(checkVersion == null){
			checkVersion = new Timer("checkVersion", true);
			checkVersion.schedule(new TimerTask() {
				@Override
				public void run() {
					checkAudit();
					checkAppVersion();
				}
			}, 0, 6 * 3600 * 1000);
		}
		return START_STICKY;
	}
	
	/**
	 * 检测该设备目前在服务端申请的状态
	 * 客户端申请阶段，服务端人工审核阶段，审核通过阶段，审核未通过阶段
	 * 并将审核结果存入配置文件中
	 */
	private void checkAudit(){
		if(SystemMethodUtil.getAPNType(TaskService.this)!=SystemMethodUtil.NoNetWork){
			checkAuditThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						editor = sp.edit();
						String result = RemoteDataHelper.getMobile(SystemMethodUtil.getMacAddress(TaskService.this,sp,editor));
						Log.v("checkAudit", result);

						if(result.equals("false")){
							
						}else if (result.equals("noright")) {
							editor.putInt(PatrolApplication.IDENTIFY, 0);
							editor.putInt(PatrolApplication.IDENTIFY_PLANT, 0);
						}else {
							JSONArray jsonArray  = new JSONArray(result);
							JSONObject jsonObject = jsonArray.getJSONObject(0);
							int state = jsonObject.getInt("Audit");
							String description = jsonObject.getString("DeviceDescription")==null?"":jsonObject.getString("DeviceDescription");
							String auditInfo = jsonObject.getString("AuditInformation")==null?"":jsonObject.getString("AuditInformation");
							String name = jsonObject.getString("Name")==null?"":jsonObject.getString("Name"); 
							String company = jsonObject.getString("WorkUnit")==null?"":jsonObject.getString("WorkUnit"); 
							String phone = jsonObject.getString("Tel1")==null?"":jsonObject.getString("Tel1"); 
							switch (state) {
							case 1://正在审核
								editor.putInt(PatrolApplication.IDENTIFY_PLANT, 0);
								break;
							case 2://审核通过
								int plant = 0;
								if(jsonObject.get("PlantID")==null){
									plant = 0;
								}else {
									plant = jsonObject.getInt("PlantID");
								}
								editor.putInt(PatrolApplication.IDENTIFY_PLANT, plant);
								break;
							case 3://审核未通过
								editor.putInt(PatrolApplication.IDENTIFY_PLANT, 0);
								break;
							}
							editor.putString(PatrolApplication.IDENTIFY_DESCRIPTION, description);
							editor.putString(PatrolApplication.IDENTIFY_AUDITINFO, auditInfo);
							editor.putString(PatrolApplication.IDENTIFY_NAME, name);
							editor.putString(PatrolApplication.IDENTIFY_COMPANY, company);
							editor.putString(PatrolApplication.IDENTIFY_PHONE, phone);
							editor.putInt(PatrolApplication.IDENTIFY, state);
						}
						editor.commit();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
			checkAuditThread.start();
		}
	}
	
	
	/**
	 * 检查是否有新的版本可以下载，如果有，将版本信息记录在本地，并通知用户更新
	 */
	private void checkAppVersion(){
		if(SystemMethodUtil.getAPNType(TaskService.this)!=SystemMethodUtil.NoNetWork){
			RequestResult rs = RemoteDataHelper.getNewestApkInfo();
			if(rs.getErrorcode()==RequestResult.NO_ERROR){
				EP_Application app = new Gson().fromJson(rs.getData(),EP_Application.class);
				sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
				editor = sp.edit();
				editor.putInt(PatrolApplication.LATEST_VERSION_CODE, app.getVerCode());
				editor.putString(PatrolApplication.LATEST_VERSION_NAME, app.getVerName());
				editor.putString(PatrolApplication.LATEST_VERSION_INFO, app.getVerInfo());
				editor.commit();
				if(app.getVerCode()>SystemMethodUtil.getVersionCode(TaskService.this)){
					if(nfm==null){
						nfm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
					}
					Notification nf = NotificationUtil.getInstance().getNotification(NotificationUtil.NEW_SYSTEM_VERSION, TaskService.this);
					nfm.notify(NotificationUtil.NEW_SYSTEM_VERSION, nf);
				}
			}
		}
	}
	
	/**
	 * 检查dataservice服务是否在运行，若否则开启
	 * @return  dataservice是否在运行
	 */
	public boolean isServiceRuning(){
		activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		serviceInfos = activityManager.getRunningServices(99);
		String calssName = "com.env.component.DataService";
		for(int i=0;i<serviceInfos.size();i++){
			if(calssName.equals(serviceInfos.get(i).service.getClassName())){
				return true;
			}
		}		
		return false;
	}
	/**
	 * 当有新的任务达到时改变通知栏的消息。
	 */
	@SuppressWarnings("deprecation")
	public void newTaskNotification(){
		nfm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		nf= NotificationUtil.getInstance().arriveNewTask(TaskService.this);
		nf.defaults |= Notification.DEFAULT_VIBRATE|Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND;
//		nf.sound = Uri.parse("android.resource://"+TaskService.this.getPackageName()+"/"+R.raw.beep);
		nfm.notify(NotificationUtil.ForeGroundNotification, nf);
		powerManager = (PowerManager)getSystemService(POWER_SERVICE);
		if(!powerManager.isScreenOn()){
			wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
			wakeLock.acquire();
			wakeLock.release();
		}				
	}
	
	/**
	 * 设置今天需要提醒的任务的时间节点
	 */
	public void setStartDateTimeArray(){
		if(db==null){
			db = DataBaseUtil.getInstance(TaskService.this).getReadableDatabase();
		}			
		sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
		SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
		String dateTime = sdf.format(Calendar.getInstance().getTime());
		EP_User user = SystemParamsUtil.getInstance().getLoginUser(sp);
		if(user==null) {
			taskStartDateTime = new ArrayList<String>();
			return;
		}
		taskStartDateTime = LocalDataHelper.getTipsOfTimeByUser(db, user.getUserID(), dateTime);

	}
	
	/**
	 * 计算任务提醒时间的
	 * @param time
	 * @return 剩余时间 毫秒数
	 */
	public long getTimeRemind(String time){
		long count = 0;
		SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
		String now = sdf.format(Calendar.getInstance().getTime());
		try {
			count = sdf.parse(time).getTime() -  sdf.parse(now).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return count;
	} 
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(secondCount!=null){
			secondCount.cancel();
			secondCount = null;
		}
		if(checkVersion!=null){
			checkVersion.cancel();
			checkVersion = null;
		}
//		Intent keepDataService = new Intent();
//		keepDataService.setAction("com.env.component.TaskService");
//		startService(keepDataService);
	}
	/**
	 * 当新的任务到来时发送消息通知前台界面 或进行 ui刷新 或进行数据保存
	 */
	private void sendBroadCast() {
		Intent intent = new Intent();
		intent.setAction("com.env.view.PatrolTaskNFCCard.TaskNFCCardReceiver");
		sendBroadcast(intent);
		intent.setAction("com.env.view.PatrolTaskConstruction.PatrolConsReceiver");
		intent.putExtra("action", "TaskService");
		sendBroadcast(intent);
	}
	
	private class TaskServiceHandle extends Handler{
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				newTaskNotification();
				sendBroadCast();
				break;
			case 1:
				break;
			}
		}
	}
}
