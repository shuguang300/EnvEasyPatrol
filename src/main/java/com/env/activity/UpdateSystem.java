package com.env.activity;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.env.component.PatrolApplication;
import com.env.component.DataService;
import com.env.nfc.NfcActivity;
import com.env.utils.SystemMethodUtil;
import com.env.utils.SystemParamsUtil;
import com.env.easypatrol.R;

public class UpdateSystem extends NfcActivity implements OnClickListener{
	private Button ok,cancel;
	private TextView vername,updateinfo;
	private DataService.DataServiceBinder binder;
	private Intent dataService;
	private SharedPreferences sp;
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
		SystemParamsUtil.getInstance().addActivity(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.updatesystem);
		sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
		startBindDataService();
		ok = (Button)findViewById(R.id.updatesystem_ok);
		cancel = (Button)findViewById(R.id.updatesystem_cancel);
		ok.setOnClickListener(this);
		cancel.setOnClickListener(this);
		vername = (TextView)findViewById(R.id.updatesystem_vername);
		updateinfo = (TextView)findViewById(R.id.updatesystem_updateinfo);
		vername.setText(sp.getString(PatrolApplication.LATEST_VERSION_NAME, ""));
		updateinfo.setText(sp.getString(PatrolApplication.LATEST_VERSION_INFO, ""));
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
	public void onBackPressed() {
		super.onBackPressed();
		finish();
//		startBindDataService();
//		binder.isUpdatingSystem = false;
		
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		binder.isUpdatingSystem = false;
		unbindService(connection);
		
	}
	
	private void startBindDataService(){
		if(binder==null){
			dataService = new Intent(UpdateSystem.this,DataService.class);
			bindService(dataService, connection, Service.BIND_AUTO_CREATE);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.updatesystem_ok) {
			switch (SystemMethodUtil.getAPNType(UpdateSystem.this)) {
			case SystemMethodUtil.NoNetWork:
				Toast.makeText(UpdateSystem.this, "下载失败，没有检测到网络", Toast.LENGTH_SHORT).show();
				break;
			case SystemMethodUtil.WifiNetWork:
				startBindDataService();
				binder.getDataService().getApkFile();
				break;
			case SystemMethodUtil.MobileNetWork:
				AlertDialog.Builder builder = new AlertDialog.Builder(UpdateSystem.this);
				builder.setTitle("系统消息").setMessage(getResources().getString(R.string.system_dataexchange_tips_message));
				builder.setPositiveButton("继续下载", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startBindDataService();
						binder.getDataService().getApkFile();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				builder.create().show();
				break;
			}
			finish();
		} else if (id == R.id.updatesystem_cancel) {
			startBindDataService();
			binder.isUpdatingSystem = false;
			finish();
		}	
	}
}
