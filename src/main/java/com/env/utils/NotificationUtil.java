package com.env.utils;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.widget.RemoteViews;

import com.env.activity.ActivitySplash;
import com.env.component.PatrolApplication;
import com.env.easypatrol.R;

public class NotificationUtil{
	public static final int ForeGroundNotification = 1;
	public static final int DOWNLOAD_DATA = 2;
	public static final int UPLOAD_FILE_DATA = 3;
	public static final int NEW_TASK_REACH = 4;
	public static final int DOWNLOAD_APK = 9;
	public static final int UPLOAD_TEXT_DATA = 7;
	public static final int GET_REMOTE_VERCODE = 8;
	public static final int NEW_SYSTEM_VERSION = 10;
	
	private static NotificationUtil INSTANCE = new NotificationUtil();
	
	private NotificationUtil (){
	}
	public static NotificationUtil getInstance(){
		if(INSTANCE == null){
			INSTANCE = new NotificationUtil();
		}
		return INSTANCE;
	}
	
	public void updateStayNotification(Context context){
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(ForeGroundNotification, getNotification(ForeGroundNotification, context));
	}
	public Notification arriveNewTask(Context context){
		Notification flagNF = new Notification();
		flagNF.icon = R.drawable.notification_stay_small;
		flagNF.tickerText = "快速进入系统";
		flagNF.when = System.currentTimeMillis();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setClass(context, ActivitySplash.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);		
		PendingIntent pendingIntent  = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT); 
		String titleString,timeString,nowTime;
		nowTime = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(Calendar.getInstance().getTime());
		SharedPreferences sp = context.getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
		timeString = "数据更新时间:"+SystemMethodUtil.toJsonDateByStr(sp.getString(PatrolApplication.LAST_UPDATE_DATETIME, nowTime));
		if(SystemParamsUtil.getInstance().getIsLogin()){
			titleString = SystemParamsUtil.getInstance().getLoginUser(sp).getRealUserName();
			SQLiteDatabase db = DataBaseUtil.getInstance(context).getReadableDatabase();
			long nowTimeInt = Long.valueOf(nowTime);
			int num = DataCenterUtil.getUnDoTaskNowByUser(db, SystemParamsUtil.getInstance().getLoginUser(sp).getUserID(),nowTimeInt+"");
			if(num>0){
				titleString =titleString +"当前"+num+"条任务未完成";
			}
		}else {
			titleString = "未登录 有新任务到达,请登录";
		}
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notification_stay);
		views.setTextViewText(R.id.notification_stay_title, titleString);
		views.setTextViewText(R.id.notification_stay_messgae, timeString);
		flagNF.contentView = views;
		flagNF.contentIntent = pendingIntent;
		return flagNF;
	}
	
	public Notification getNotification(int kind,Context context){		
		switch (kind) {
		case ForeGroundNotification:
			Notification flagNF = new Notification();
			flagNF.icon = R.drawable.notification_stay_small;
			flagNF.tickerText = "快速进入系统";
			flagNF.when = System.currentTimeMillis();
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setClass(context, ActivitySplash.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);		
			PendingIntent pendingIntent  = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT); 
			String titleString,timeString,nowTime;
			nowTime = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(Calendar.getInstance().getTime());
			SharedPreferences sp = context.getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
			timeString = "数据更新时间:"+SystemMethodUtil.toJsonDateByStr(sp.getString(PatrolApplication.LAST_UPDATE_DATETIME, nowTime));
			if(SystemParamsUtil.getInstance().getIsLogin()){
				titleString = SystemParamsUtil.getInstance().getLoginUser(sp).getRealUserName();
				SQLiteDatabase db = DataBaseUtil.getInstance(context).getReadableDatabase();
				long nowTimeInt = Long.valueOf(nowTime);
				int num = DataCenterUtil.getUnDoTaskNowByUser(db, SystemParamsUtil.getInstance().getLoginUser(sp).getUserID(),nowTimeInt+"");
				if(num>0){
					titleString =titleString +"当前"+num+"条任务未完成";
				}
			}else {
				titleString = "未登录 ";
			}
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notification_stay);
			views.setTextViewText(R.id.notification_stay_title, titleString);
			views.setTextViewText(R.id.notification_stay_messgae, timeString);
			flagNF.contentView = views;
			flagNF.contentIntent = pendingIntent;
			return flagNF;
//		case DOWNLOAD_DATA:	
//			downloadNF = new Notification();
//			downloadNF.icon = R.drawable.data_exc_download;
//			downloadNF.tickerText = "正在为您下载数据......";
//			view = new RemoteViews(context.getPackageName(), R.item_imageview.data_exchange_notification);
//			view.setImageViewResource(R.id.data_exchange_notification_img,  R.drawable.data_exc_download);
//			view.setTextViewText(R.id.data_exchange_notification_title, "正在为您下载数据");
//			notification.flags = Notification.FLAG_NO_CLEAR;
//			downloadNF.flags = Notification.FLAG_ONGOING_EVENT;
//			return downloadNF;
		case UPLOAD_FILE_DATA:
			Notification uploadNF = new Notification();			
			uploadNF.icon = R.drawable.notification_data_small;
			uploadNF.tickerText = "正在为您上传图片......";
			RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.data_exchange_notification);
			view.setTextViewText(R.id.data_exchange_notification_title, "正在为您上传图片");
//			uploadNF.flags |= Notification.FLAG_AUTO_CANCEL;
			uploadNF.flags = Notification.FLAG_ONGOING_EVENT;
			uploadNF.contentView = view;
			return uploadNF;
//		case TASK_REACH:
//			taskNF = new Notification();
//			taskNF.icon = R.drawable.notification_stay;
//			taskNF.tickerText = "有新的任务到达";
//			taskNF.when = System.currentTimeMillis();
//			taskNF.defaults |= Notification.DEFAULT_VIBRATE|Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND;
//			taskNF.sound = Uri.parse("android.resource://"+context.getPackageName()+"/"+R.raw.beep);
//			taskNF.sound = Uri.parse("file:///android_asset/beep.ogg");
//			taskNF.flags |= Notification.FLAG_AUTO_CANCEL|Notification.FLAG_SHOW_LIGHTS;
//			intent = new Intent(Intent.ACTION_MAIN);
//			intent.addCategory(Intent.CATEGORY_LAUNCHER);
//			intent.setClass(context, SplashActivity.class);
//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);		
//			pendingIntent  = PendingIntent.getActivity(context, 0, intent, 0); 
//			taskNF.setLatestEventInfo(context, context.getResources().getString(R.string.system_appname), "已经有新的任务可以执行了", pendingIntent);
//			return taskNF;
		case DOWNLOAD_APK:
			Notification apkNF = new Notification();
			apkNF.icon = R.drawable.notification_data_small;
			apkNF.tickerText = "正在为您下载文件......";
			apkNF.flags = Notification.FLAG_ONGOING_EVENT;
//			view = new RemoteViews(context.getPackageName(), R.item_imageview.data_exchange_notification);
//			view.setTextViewText(R.id.data_exchange_notification_title, "下载文件中");
			return apkNF;
		case NEW_SYSTEM_VERSION:
			Notification version = new Notification();
			version.icon = R.drawable.notification_stay_small;
			version.tickerText = "有新的版本可以下载了";
			version.flags = Notification.FLAG_AUTO_CANCEL;
			version.defaults |= Notification.DEFAULT_VIBRATE|Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND;
			RemoteViews viewVERSION = new RemoteViews(context.getPackageName(), R.layout.notification_stay);
			viewVERSION.setTextViewText(R.id.notification_stay_title, "系统消息");
			viewVERSION.setTextViewText(R.id.notification_stay_messgae, "有新的版本可以下载了");
			version.contentView = viewVERSION;
			Intent intentVERSION = new Intent("com.env.component.CustomBroadCast");
			PendingIntent pendingIntentVERSION  = PendingIntent.getBroadcast(context, 0, intentVERSION, 0); 
			version.contentIntent = pendingIntentVERSION;
			return version;
		}
		return null;
	}
	
	public RemoteViews getViews(Context context){
		return new RemoteViews(context.getPackageName(), R.layout.data_exchange_notification);
	}
}
