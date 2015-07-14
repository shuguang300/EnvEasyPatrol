package com.env.activity;
import android.content.Intent;
import android.os.Bundle;

import com.env.nfc.NfcActivity;
import com.env.easypatrol.R;
public class PatrolActivityMain extends NfcActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.patrol_activity_main);
	}
	
	@Override
	public void iniData() {
		super.iniData();
	}
	
	@Override
	public void iniView() {
		super.iniView();
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
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
	};
	
}
