package com.env.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.env.bean.EP_User;
import com.env.component.DataService;
import com.env.component.PatrolApplication;
import com.env.easypatrol.R;
import com.env.nfc.NfcActivity;
import com.env.utils.DataBaseUtil;
import com.env.utils.DataCenterUtil;
import com.env.utils.DialogUtil;
import com.env.utils.HttpUtil;
import com.env.utils.NotificationUtil;
import com.env.utils.SystemMethodUtil;
import com.env.utils.SystemParamsUtil;


public class ActivityLogin extends NfcActivity {

	private final int loginSuccess= 1;
	private final int loginFailed = 0;
	private final int accountState =2;
	
	private EditText etName,etPwd;
	private Button loginButton;
	private LoginThread loginThread = null;
	private ProgressDialog progressDialog = null;
	private Intent dataService;
	private SQLiteDatabase db ;
	private DataService.DataServiceBinder binder;
	private String userName,userPwd,IPAddr;
	private EP_User user;
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private ServiceConnection connection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {	
			mBindService();
		}		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (DataService.DataServiceBinder)service;
		}
	};
	private void mBindService(){
		dataService = new Intent(ActivityLogin.this,DataService.class);
		bindService(dataService, connection, Service.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SystemParamsUtil.getInstance().exit();
		SystemParamsUtil.getInstance().addActivity(this);
		mBindService();
		setContentView(R.layout.login);
		iniData();
		iniView();
	} 
	
	@Override
	public void iniData(){
		db = DataBaseUtil.getInstance(ActivityLogin.this).getReadableDatabase();
		settings = getSharedPreferences(PatrolApplication.PREFS_NAME, 0);
		editor = settings.edit();		
		IPAddr = settings.getString(PatrolApplication.IP_Addr, HttpUtil.getInstance(ActivityLogin.this).getIPAddr());
	}
	
	public void iniView(){
		loginButton = (Button) this.findViewById(R.id.btn_login);
		etName = (EditText) this.findViewById(R.id.tv_username);
		etPwd = (EditText)findViewById(R.id.tv_userpsw);
		setViewState();
	}
	
	public void setViewState() {
		// 初始化记住用户名控件
		
		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				userName = etName.getText().toString();
				userPwd = etPwd.getText().toString();
				if(userName!="" && userPwd!=null){
//					isLogin = true;
					loginThread = new LoginThread(handlerLogin); // 建立线程实例
					loginThread.doStart();		
				}else{
					Toast.makeText(ActivityLogin.this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
				}
							
			}
		});		
	}
	

	// 响应回退
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.finish();
		SystemParamsUtil.getInstance().exit();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(dataService!=null){
			unbindService(connection);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, getResources().getString(R.string.login_menu_update)).setIcon(R.drawable.config_img_version);
		menu.add(0, 1, 1, getResources().getString(R.string.login_menu_ipaddr)).setIcon(R.drawable.config_img_ip);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			checkUpdate();
			break;
		case 1:
			AlertDialog.Builder adb  = new AlertDialog.Builder(ActivityLogin.this);
			adb.setTitle("设置服务器IP地址");
			final EditText et = new EditText(ActivityLogin.this);
			et.setText(IPAddr);
			adb.setView(et).setCancelable(true);
			adb.setView(et).setPositiveButton("确定", new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {					
					if(et.getText().toString().isEmpty()){
						Toast.makeText(ActivityLogin.this, "不是有效的IP地址", Toast.LENGTH_SHORT).show();
					}else {
						IPAddr = et.getText().toString();
						HttpUtil.getInstance(ActivityLogin.this).setIPAddr(IPAddr);
						editor.putString(PatrolApplication.IP_Addr, IPAddr);
						editor.commit();
					}
				}
			});
			adb.create().show();	
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	
	
	/**
	 * 检查更新
	 */
	private void checkUpdate(){
		mBindService();
		int apnType = SystemMethodUtil.getAPNType(ActivityLogin.this);
		if(binder!=null){
			if (binder.isUpdating) {
				Toast.makeText(getApplicationContext(), "正在更新任务数据",Toast.LENGTH_LONG).show();
			}else {
				switch (apnType) {
				case SystemMethodUtil.NoNetWork:
					DialogUtil.setApnDialog(ActivityLogin.this);
					break;
				case SystemMethodUtil.MobileNetWork:
					settings = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
					editor = settings.edit();
					if(settings.getBoolean(PatrolApplication.HAS_DATAEXCHANGE_TIPS, true)){
						DialogUtil.confirmNetWork(ActivityLogin.this,new DialogInterface.OnClickListener() {						
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								Toast.makeText(getApplicationContext(), "获取最新数据中....",Toast.LENGTH_LONG).show();
								binder.getDataService().upDateDB(db);					
							}
						},editor);
					}else {
						Toast.makeText(getApplicationContext(), "获取最新数据中....",Toast.LENGTH_LONG).show();
						binder.getDataService().upDateDB(db);	
					}
					break;
				case SystemMethodUtil.WifiNetWork:
					Toast.makeText(getApplicationContext(), "获取最新数据中....",Toast.LENGTH_LONG).show();
					binder.getDataService().upDateDB(db);
					break;
				}
			}
		}else {
			Toast.makeText(getApplicationContext(), "服务正在启动中，请稍后重试获取数据",Toast.LENGTH_LONG).show();
		}
	}
	
	
	
	private Handler handlerLogin = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case loginSuccess:
				final Intent logininIntent = new Intent(ActivityLogin.this,ActivityTaskConstruction.class);			
				logininIntent.putExtra("NeedTips", true);
				new Handler().postDelayed(new Runnable() {					
					@Override
					public void run() {
						if(progressDialog!=null){
							progressDialog.cancel();
						}
//						isLogin = false; 
						SystemParamsUtil.getInstance().login(user,editor);
						NotificationUtil.getInstance().updateStayNotification(ActivityLogin.this);
						startActivity(logininIntent);				
						ActivityLogin.this.finish();
					}
				}, 1200);				
				break;
			case loginFailed:
				Toast.makeText(ActivityLogin.this, "用户名或密码错误，请重试", Toast.LENGTH_SHORT).show();
				etPwd.setText("");	
//				isLogin = false;
				progressDialog.cancel();
				SystemParamsUtil.getInstance().logout();
				break;
			case accountState:
				Toast.makeText(ActivityLogin.this, "账号已被停用，请联系管理员", Toast.LENGTH_SHORT).show();
				etPwd.setText("");	
//				isLogin = false;
				progressDialog.cancel();
				SystemParamsUtil.getInstance().logout();
				break;
			}
			super.handleMessage(msg);
		}
	};

	// 登录线程
	
	protected class LoginThread extends Thread {
		private Handler handle = null;
		HashMap<String, Object> params = null;	
		public LoginThread(Handler hander) {
			handle = hander;
		}
		public void doStart() {
			progressDialog = new ProgressDialog(ActivityLogin.this);
			progressDialog.setTitle("系统消息");
			progressDialog.setMessage("正在登录，请稍等...");
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(true);
			progressDialog.show();
//			isLogin = true;
			this.start(); // 线程开始了
		}
		
		@Override
		public void run() {
			super.run();
			Message msg = handle.obtainMessage();
			try {
				user = DataCenterUtil.login(userName, userPwd, db);
				if(user==null)msg.what = loginFailed;
				else if(user.getAccountState()==0) msg.what = accountState;
				else msg.what = loginSuccess;
				LoginThread.sleep(1000);
			} catch (Exception e) {
				user = null;
				msg.what=loginFailed;
				Log.e("login", "**********"+e.getMessage()+"**********");
			}finally{
				msg.sendToTarget();
			}
		}
	};


	@Override
	protected void onStop() {
		super.onStop();				
	}
	
	/**
	 * 设置用户列表
	 * @return
	 */
	private void setUserList(){
		ArrayList<EP_User> users ;
		if(settings.getInt(PatrolApplication.IDENTIFY_PLANT, 0)==0){
			users = DataCenterUtil.getUserList(null, db);
		}else {
			users = DataCenterUtil.getUserList(settings.getInt(PatrolApplication.IDENTIFY_PLANT, 0), db);
		}
//		SystemParamsUtil.getInstance().setUsers(users);
		if(users==null||users.size()==0){
			if(binder.isUpdating){
				Toast.makeText(ActivityLogin.this, "正在为您准备数据，请稍后", Toast.LENGTH_SHORT).show();
			}else {
				AlertDialog.Builder  ab = new AlertDialog.Builder(ActivityLogin.this);
				ab.setCancelable(false);
				ab.setTitle("系统消息").setMessage("您当前没有获取数据，点击确定获取数据");
				ab.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						checkUpdate();
					}
				});
				ab.setNegativeButton("退出", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
				ab.create().show();
			}
		}
	}
}
