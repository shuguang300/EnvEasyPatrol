package com.env.activity;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import com.env.bean.EnumList;
import com.env.component.PatrolApplication;
import com.env.component.DataService;
import com.env.nfc.NfcActivity;
import com.env.nfc.NfcUtils;
import com.env.utils.DataCenterUtil;
import com.env.utils.SystemMethodUtil;
import com.env.utils.DialogUtil;
import com.env.utils.NotificationUtil;
import com.env.utils.DataBaseUtil;
import com.env.utils.SystemParamsUtil;
import com.env.easypatrol.R;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

public class ActivitySplash extends NfcActivity{	
	public SharedPreferences sp;
	public SharedPreferences.Editor editor;
	private String cur_cardTagID;
	private int cur_cardID;
	private int cardType = -1;
	private boolean first;
	private DataService.DataServiceBinder binder;
	private Intent intent;
	private Timer timer = null;
	private Intent main;
	private SQLiteDatabase db;
	private NfcAdapter nfcAdapter;
	private boolean noticeNfc;
	
	private ServiceConnection sc = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBindService();
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (DataService.DataServiceBinder)service;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		SystemParamsUtil.getInstance().exit();
		SystemParamsUtil.getInstance().addActivity(this);
		
		sp = getSharedPreferences(PatrolApplication.PREFS_NAME, MODE_PRIVATE);
		first = sp.getBoolean(PatrolApplication.FIRST_RUN, true);
		
		editor = sp.edit();
		
		if(!SystemMethodUtil.isSDCardReady()){
			SystemMethodUtil.SDCardNotReady(ActivitySplash.this);
		}
		
		startService(new Intent(ActivitySplash.this,DataService.class));	
		mBindService();
		db = DataBaseUtil.getInstance(ActivitySplash.this).getReadableDatabase();			
	
	}		
	
    private Handler handler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		if(timer != null){
				timer.cancel();	
				timer = null;
			}
    		switch (msg.what) {
			case 0:	
				break;
			case 1:							
				break;
			}
    		if(nfcAdapter == null){
    			if(noticeNfc){
    				DialogUtil.nfcNotIn(ActivitySplash.this, new DialogInterface.OnClickListener() {					
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
    						dialog.dismiss();
    						editor.putBoolean(PatrolApplication.NFCSTATUNOTE, false);
    						editor.commit();
    						login();
    					}
    				});
    			}else{
    				login();
    			}
    		}else if(!nfcAdapter.isEnabled()){
    			if(noticeNfc){
    				DialogUtil.nfcConfirm(ActivitySplash.this,new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							editor.putBoolean(PatrolApplication.NFCSTATUNOTE, false);
    						editor.commit();
    						login();
						}
					});
    			}else{
    				login();
    			}
    		}else{
    			login();
    		}    		   		    		
    	};
    };   	
    
	@Override
	protected void onResume() {
		super.onResume();
		noticeNfc = sp.getBoolean(PatrolApplication.NFCSTATUNOTE, true);
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if(binder==null){
			mBindService();
		}		
		if(timer == null){
			timer = new Timer();
			timer.schedule(new TimerTask() {			
				@Override
				public void run() {
					Message msg = handler.obtainMessage();
					if(binder!=null&&!binder.isUpdating){
						if(SystemMethodUtil.checkWifi(ActivitySplash.this)){
							msg.what = 0;	
						}else {
							msg.what =1;
						}
						msg.sendToTarget();
					}	
				}
			}, 0, 2000);	
		}		
	}
	@Override
	protected void onDestroy() {		
		super.onDestroy();
		unbindService(sc);
		if(timer!=null){
			timer.cancel();
			timer = null;
		}
	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		SystemParamsUtil.getInstance().exit();
		this.finish();
	}
    /**
     * 使用nfccard进行登录
     * @param intent
     */
    private void loginByNFCCard(Intent intent) {
    	editor = sp.edit();   
        if(first){
        	main = new Intent();
        	main.putExtra("username", "");
			main.setClass(ActivitySplash.this, ActivityWelcome.class);
		}else {    
			cur_cardTagID = NfcUtils.getTagID(intent);
	        String sqlStr1 = "select * from EP_NFCCard where CardTagID = '" + cur_cardTagID +"'";        
	        Cursor csCard = db.rawQuery(sqlStr1, null);
	        csCard.moveToFirst();
	        if(csCard.getCount()<1){
	        	SystemParamsUtil.getInstance().exit();
	        	SystemParamsUtil.getInstance().logout();
	        	main = new Intent();
	        	main.setClass(ActivitySplash.this, ActivityLogin.class);
	        	Toast.makeText(ActivitySplash.this, "没有找到该卡信息", Toast.LENGTH_SHORT).show();
	        }else {
	        	cardType = csCard.getInt(csCard.getColumnIndex("CardType"));
	        	cur_cardID = csCard.getInt(csCard.getColumnIndex("CardID"));
	        	if(cardType==NfcUtils.userCardType){
					String sqlStr ="select * from EP_User where CardID =" + cur_cardID;           
		            Cursor cr = db.rawQuery(sqlStr, null);
		            cr.moveToFirst();
		            if(cr.getCount()<1){
		            	SystemParamsUtil.getInstance().exit();
		            	main = new Intent();
		            	main.setClass(ActivitySplash.this, ActivityLogin.class);
		            	SystemParamsUtil.getInstance().logout();
		            	Toast.makeText(ActivitySplash.this, "未检测到持有该卡的用户", Toast.LENGTH_SHORT).show();           	
		            }else {
		            	if(!first){
		            		if(SystemParamsUtil.getInstance().getIsLogin()){
		            			if(SystemParamsUtil.getInstance().getLoginUser(sp).getUserID().equals(cr.getString(0))){
		            				SystemParamsUtil.getInstance().exit();
		            				main = new Intent();
		            				main.putExtra("NeedTips", false);
		            				main.setClass(ActivitySplash.this, ActivityTaskConstruction.class);
		            			}else {
		            				SystemParamsUtil.getInstance().exit();
		            				main = new Intent();
		            				main.putExtra("NeedTips", true);
		            				main.setClass(ActivitySplash.this, ActivityTaskConstruction.class);
		            				SystemParamsUtil.getInstance().login(DataCenterUtil.parseUserToBean(cr),editor);
								}
		            		}else {
		            			main = new Intent();
		            			main.putExtra("NeedTips", true);
		            			main.setClass(ActivitySplash.this, ActivityTaskConstruction.class);
		            			SystemParamsUtil.getInstance().login(DataCenterUtil.parseUserToBean(cr),editor);
							}
		        		}else {
		        			main = new Intent();
		        			main.setClass(ActivitySplash.this, ActivityWelcome.class);
		        			SystemParamsUtil.getInstance().login(DataCenterUtil.parseUserToBean(cr),editor);
		        			main.putExtra("username", SystemParamsUtil.getInstance().getLoginUser(sp).getRealUserName());
						}                                                                                                   
		    		}                                                                                   
		            cr.close();
				}else if(cardType==NfcUtils.tagCardType){
					main = new Intent();
					if(SystemParamsUtil.getInstance().getIsLogin()){
						HashMap<String, String> card = new HashMap<String, String>();
						card.put("CardName", csCard.getString(csCard.getColumnIndex("CardName")));
						card.put("CardID", csCard.getString(csCard.getColumnIndex("CardID")));
						main.putExtra("Child", card);
						main.putExtra("Mode", ActivityTaskConstruction.CARD_NFC);
						main.setClass(ActivitySplash.this, ActivityTaskList.class);
					}else {
						SystemParamsUtil.getInstance().exit();
						SystemParamsUtil.getInstance().logout();
						main.setClass(ActivitySplash.this, ActivityLogin.class);
					}
				}
			}
	        csCard.close();
		}
        editor.putBoolean(PatrolApplication.FIRST_RUN, false);
		editor.commit(); 
		
		
		startActivity(main);
		NotificationUtil.getInstance().updateStayNotification(ActivitySplash.this);
		finish();
		
//        new Handler().postDelayed(new Runnable() {
//			public void run() {
//				
//			}
//		}, App.SPLASH_DISPLAY_LENGHT);
    }
    
    /**
     * 使用普通方式登录
     */
    private void loginByMain(){
    	editor = sp.edit();
		if(first){
			main = new Intent(ActivitySplash.this,ActivityWelcome.class);
			editor.putBoolean(PatrolApplication.FIRST_RUN, false);
			editor.commit();
			ActivitySplash.this.startActivity(main);
			ActivitySplash.this.finish();
		}else {
			SystemParamsUtil.getInstance().exit();
			if(SystemParamsUtil.getInstance().getIsLogin()){
				main = new Intent(Intent.ACTION_MAIN);
				main.addCategory(Intent.CATEGORY_LAUNCHER);
				main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				main.putExtra("NeedTips", true);
				main.setClass(ActivitySplash.this,ActivityTaskConstruction.class);
			}else {
				SystemParamsUtil.getInstance().logout();
				main = new Intent();
				main.setClass(ActivitySplash.this,ActivityLogin.class);
//				if(isAutoLogin){
//					Cursor cr = db.rawQuery(sqlStr, null);
//		            cr.moveToFirst();
//		            if(cr!=null&&cr.getCount()>0){
//						main = new Intent();
//						main.setClass(ActivitySplash.this, ActivityTaskConstruction.class);
//						main.putExtra("NeedTips", true);
//						SystemParamsUtil.getInstance().setLoginInfo(true, DataCenterUtil.parseUserToBean(cr),editor);
//		            }else {
//		            	SystemParamsUtil.getInstance().setLoginInfo(false, null,editor);
//						main = new Intent();
//						main.setClass(ActivitySplash.this,ActivityLogin.class);
//					}
//		            cr.close();
//				}else {
//					
//				}
			}
			editor.putBoolean(PatrolApplication.FIRST_RUN, false);
			editor.commit();
			
			startActivity(main);
			NotificationUtil.getInstance().updateStayNotification(ActivitySplash.this);
			finish();
		}							
    }
    /**
     * 绑定dataservice
     */
    private void mBindService(){
    	intent = new Intent(ActivitySplash.this, DataService.class);
		bindService(intent, sc, Service.BIND_AUTO_CREATE);		
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if(timer == null){
			timer = new Timer();
			timer.schedule(new TimerTask() {			
				@Override
				public void run() {
					Message msg = handler.obtainMessage();
					if(binder!=null&&!binder.isUpdating){
						if(SystemMethodUtil.checkWifi(ActivitySplash.this)){
							msg.what = 0;	
						}else {
							msg.what =1;
						}
						msg.sendToTarget();
					}	
				}
			}, 0, 2000);	
		}	
    }

	/**
	 * 登录操作
	 */
	private void login() {
		if(sp.getInt(PatrolApplication.IDENTIFY, EnumList.AppRightState.ClientApplication.getStae())!=EnumList.AppRightState.AuditPass.getStae()){
			new Handler().postDelayed(new Runnable() {
				public void run() {
					main = new Intent(ActivitySplash.this, ActivityApplication.class);
					startActivity(main);
					NotificationUtil.getInstance().updateStayNotification(ActivitySplash.this);
					ActivitySplash.this.finish();
				}
			}, PatrolApplication.SPLASH_DISPLAY_LENGHT);
		}else {
			if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {							
				loginByNFCCard(getIntent());
			} else {
				loginByMain();
			}
		}
	}
	
}
