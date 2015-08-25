package com.env.component;

import java.io.File;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.env.utils.DataBaseUtil;
import com.env.utils.HttpUtil;
import com.env.utils.SystemMethodUtil;

public class PatrolApplication extends Application{
	private Intent startService,keepDataService;
	private Context context;
	private AlarmManager alarmManager;
	private PendingIntent pendingIntent;
	private SharedPreferences spf;
//	public static final int PLANTID_INT = 4;
	
	public static final String IP_Addr = "IP_Addr";
	
	public static final String USER_INFO = "user";
	
	public static final String NFCSTATUNOTE = "Nfc_Statu_Note";
	public static final String APN_PREFER = "Apn_Prefer";
	
	public static final String IP = "183.81.180.26:9999/secondwatersupply";
	
	public static final String PREFS_NAME = "EnvPrefsFile";
	public static final String MAC_ADDR = "macaddr";
	public static final String FIRST_RUN = "firstrun";
	public static final String DATABASE_READY = "databaseready";
	public static final String UPDATE_MODE = "updatemode";
	public static final String HAS_DATAEXCHANGE_TIPS = "exchangedatanotips";
	public static final String LAST_UPDATE_DATETIME = "lastupdate";
	public static final String LAST_UPLOAD_DATETIME = "lastupload";
	public static final String LAST_CREATETASK_DATETIME = "lastcreatetask";
	
	public static final String LAST_USERNAME = "lastUsername";
	public static final String LAST_USERID = "lastUserID"; 
	public static final String LAST_USERPLANT = "lastUserPlant";
	public static final String AUTO_LOGIN = "autoLogin";
	public static final String REM_NAME = "remembername";
	
	
	public static final String IDENTIFY = "identify";
	public static final String IDENTIFY_NAME = "identifyname";
	public static final String IDENTIFY_PHONE = "identifyphone";
	public static final String IDENTIFY_COMPANY = "identifycompany";
	public static final String IDENTIFY_PLANT = "identifyplant";
	public static final String IDENTIFY_AUDITINFO = "identifyauditinfo";
	public static final String IDENTIFY_DESCRIPTION = "identifydescription";
	
	
	public static final String LATEST_VERSION_CODE = "latestvercode";
	public static final String LATEST_VERSION_NAME = "latestvername";
	public static final String LATEST_VERSION_INFO = "latestverinfo";
	public static final String LATEST_VERSION_FILENAME = "latestfilename";
	
	
	public static final String CONS_CARD_INFLATEER = "conscardinflater";
	
	
	public static final int SPLASH_DISPLAY_LENGHT = 1500;
	
	@Override
	public void onCreate() {		
		super.onCreate();
		context = getApplicationContext();
		
		CrashHandler crashHandler = CrashHandler.getInstance();  
		crashHandler.init(context); 
        
//		if(!SystemMethodUtil.hasShortcut(context)){
//			SystemMethodUtil.addShortcut(context);
//		}
		
		spf = getSharedPreferences(PatrolApplication.PREFS_NAME, MODE_PRIVATE);
		HttpUtil.getInstance(context).setIPAddr(spf.getString(IP_Addr, IP));
		
		
		String path = DataBaseUtil.getPath()+File.separator+DataBaseUtil.DATABASE_NAME;
		SystemMethodUtil.copyDB(context,path);
		
		try {
			Thread.sleep(1000);
			startService = new Intent();
			startService.setAction("com.env.component.DataService");
			context.startService(startService);

			keepDataService = new Intent();
			keepDataService.setAction("com.env.component.TaskService");

			alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			pendingIntent = PendingIntent.getService(context, 0, keepDataService, 0);
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, 5*60*1000, pendingIntent);
		} catch (Exception e) {
			
		}
		
		
			
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		context = getApplicationContext();
		startService = new Intent();
		startService.setAction("com.env.component.DataService");
		context.startService(startService);
		keepDataService = new Intent();
		keepDataService.setAction("com.env.component.TaskService");
		context.startService(keepDataService);
		
	}
	
}
