package com.env.activity;
import com.env.component.PatrolApplication;
import com.env.component.DataService;
import com.env.nfc.NfcActivity;
import com.env.utils.SystemMethodUtil;
import com.env.utils.HttpUtil;
import com.env.utils.SystemParamsUtil;
import com.env.easypatrol.R;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityConfig extends NfcActivity implements OnClickListener{
	private SharedPreferences sp;
	private SharedPreferences.Editor editor;	
	private RelativeLayout titleVersion,titleIPAd,titleUpdateMode,titleTips,uploadLog;
	private TextView valueVersion;
	private Button btnLogout;
	private CheckBox cbMode,cbTips;
	private ImageView imgVersion;
	private String IPAddr="";
	private DataService.DataServiceBinder binder;
	private Intent dataService;
	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {	
		}		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (DataService.DataServiceBinder)service;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
		mBindService();
		SystemParamsUtil.getInstance().addActivity(this);
		sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
		iniView();

		ActionBar actionBar = getActionBar();
		actionBar.setTitle(getResources().getString(R.string.config_title));
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();


	}
	private void mBindService(){
		if(binder==null){
			dataService = new Intent();
			dataService.setAction("com.env.component.DataService");
			dataService.setPackage(getPackageName());
			bindService(dataService, connection, Service.BIND_AUTO_CREATE);
		}
	}
	
	@Override
	public void iniView(){
		titleVersion = (RelativeLayout)findViewById(R.id.config_title_appversion);
		titleIPAd = (RelativeLayout)findViewById(R.id.config_title_ipaddr);
		titleUpdateMode = (RelativeLayout)findViewById(R.id.config_title_autoupdate);
		titleTips = (RelativeLayout)findViewById(R.id.config_title_hastips);
		uploadLog = (RelativeLayout)findViewById(R.id.config_title_uploadlog);
		
		imgVersion = (ImageView)findViewById(R.id.config_img_appversion);
		
		btnLogout = (Button)findViewById(R.id.config_btn_logout);
		
		valueVersion = (TextView)findViewById(R.id.config_value_version);
		
		valueVersion.setText(SystemMethodUtil.getVersionName(ActivityConfig.this));
		
		titleVersion.setOnClickListener(this);
		titleIPAd.setOnClickListener(this);
		btnLogout.setOnClickListener(this);
		titleUpdateMode.setOnClickListener(this);
		titleTips.setOnClickListener(this);
		uploadLog.setOnClickListener(this);
		
		cbMode = (CheckBox)findViewById(R.id.config_cb_datamode);
		cbTips = (CheckBox)findViewById(R.id.config_cb_hastips);
		
		if(sp.getInt(PatrolApplication.UPDATE_MODE, SystemMethodUtil.WifiNetWork)==SystemMethodUtil.WifiNetWork){
			cbMode.setChecked(true);
		}else {
			cbMode.setChecked(false);
		}
		if(sp.getBoolean(PatrolApplication.HAS_DATAEXCHANGE_TIPS, true)){
			cbTips.setChecked(true);
		}else {
			cbTips.setChecked(false);
		}
		IPAddr = sp.getString(PatrolApplication.IP_Addr, HttpUtil.getInstance(ActivityConfig.this).getIPAddr());
	}
	
	private void saveStatu(){
		editor = sp.edit();
		if(cbMode.isChecked()){
			editor.putInt(PatrolApplication.UPDATE_MODE, SystemMethodUtil.WifiNetWork);
		}else {
			editor.putInt(PatrolApplication.UPDATE_MODE, SystemMethodUtil.MobileNetWork);
		}
		if(cbTips.isChecked()){
			editor.putBoolean(PatrolApplication.HAS_DATAEXCHANGE_TIPS, true);
		}else {
			editor.putBoolean(PatrolApplication.HAS_DATAEXCHANGE_TIPS, false);
		}
		if(!IPAddr.isEmpty()){
			editor.putString(PatrolApplication.IP_Addr, IPAddr);
		}
		
		boolean ok = editor.commit();
		if(ok){
			HttpUtil.getInstance(ActivityConfig.this).setIPAddr(IPAddr);
		}else {
			Toast.makeText(ActivityConfig.this, "保存失败", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(SystemParamsUtil.getInstance().getIsLogin()){
			int verCode = sp.getInt(PatrolApplication.LATEST_VERSION_CODE, 0);
			if(verCode != 0 && SystemMethodUtil.getVersionCode(ActivityConfig.this)<verCode){
				imgVersion.setBackgroundResource(R.drawable.config_img_newversion);
			}else {
				imgVersion.setBackgroundResource(R.drawable.config_img_version);
			}	
		}else {
			startActivity(new Intent(ActivityConfig.this,ActivityLogin.class));
			finish();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();		
		saveStatu();
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void logout(){
		saveStatu();
		Intent finishPatrol = new Intent();
		finishPatrol.setAction("com.env.view.PatrolTaskConstruction.PatrolConsReceiver");
		finishPatrol.putExtra("action", "PatrolConfig");
		sendBroadcast(finishPatrol);
		Intent loginIntent = new Intent(this,ActivityLogin.class);
		SystemParamsUtil.getInstance().logout();
		startActivity(loginIntent);
		ActivityConfig.this.finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case android.R.id.home:
				onBackPressed();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.config_title_ipaddr:
				AlertDialog.Builder adb  = new AlertDialog.Builder(ActivityConfig.this);
				adb.setTitle("设置服务器IP地址");
				final EditText et = new EditText(ActivityConfig.this);
				et.setText(IPAddr);
				adb.setView(et).setCancelable(true);
				adb.setView(et).setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(et.getText().toString().isEmpty()){
							Toast.makeText(ActivityConfig.this, "不是有效的IP地址", Toast.LENGTH_SHORT).show();
						}else {
							IPAddr = et.getText().toString();
							HttpUtil.getInstance(ActivityConfig.this).setIPAddr(IPAddr);
						}
					}
				});
				adb.create().show();
				break;
			case R.id.config_title_appversion:
				if(SystemMethodUtil.getAPNType(ActivityConfig.this)!=SystemMethodUtil.NoNetWork){
					if(binder==null){
						mBindService();
						Toast.makeText(ActivityConfig.this, "获取版本信息失败，请重试", Toast.LENGTH_SHORT).show();
					}else {
						if(binder.isUpdatingSystem){
							Toast.makeText(ActivityConfig.this, "正在为您下载更新", Toast.LENGTH_SHORT).show();
						}else {
							Toast.makeText(ActivityConfig.this, "开始检测新版本", Toast.LENGTH_SHORT).show();
							binder.getDataService().getVersion();
						}
					}
				}else{
					Toast.makeText(this, "请检查网络情况然后重试", Toast.LENGTH_SHORT).show();
				}

				break;
			case R.id.config_title_autoupdate:
				if(cbMode.isChecked()){
					cbMode.setChecked(false);
				}else {
					cbMode.setChecked(true);
				}
				break;
			case R.id.config_title_hastips:
				if(cbTips.isChecked()){
					cbTips.setChecked(false);
				}else {
					cbTips.setChecked(true);
				}
				break;
			case R.id.config_btn_logout:
				logout();
				break;
			case R.id.config_title_uploadlog:
				if(SystemMethodUtil.getAPNType(ActivityConfig.this)!=SystemMethodUtil.NoNetWork){
					mBindService();
					if(binder!=null){
						binder.getDataService().uploadLogFile();
					}else {
						Toast.makeText(this, "请重试", Toast.LENGTH_SHORT).show();
					}
				}else{
					Toast.makeText(this, "请检查网络情况然后重试", Toast.LENGTH_SHORT).show();
				}

				break;
		}
	}
}
