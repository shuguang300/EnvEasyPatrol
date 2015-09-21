package com.env.nfc;
import com.env.utils.SystemParamsUtil;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import cn.jpush.android.api.JPushInterface;

public class NfcActivity extends Activity{
	private NfcAdapter nfcAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
	}
	public void iniData(){}
	public void iniView(){}
	
	@Override
	protected void onResume() {
		JPushInterface.onResume(this);
		super.onResume();
		SystemParamsUtil.getInstance().setCurActivityName(this.getClass().getName());
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (nfcAdapter != null && nfcAdapter.isEnabled()) {
			nfcAdapter.enableForegroundDispatch(this, NfcUtils.NFCPendingIntent(this), NfcUtils.intentFilters, NfcUtils.techList);
		}
	}


	
	
	@Override
	protected void onPause() {
		JPushInterface.onPause(this);
		super.onPause();
		if(nfcAdapter!=null && nfcAdapter.isEnabled()){
			nfcAdapter.disableForegroundDispatch(this);
		}		
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
	}
	
	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		SystemParamsUtil.getInstance().removeActivity(this);
	}
	
}
