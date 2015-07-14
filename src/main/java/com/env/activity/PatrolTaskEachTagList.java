package com.env.activity;

import com.env.nfc.NfcActivity;
import com.env.utils.SystemParamsUtil;

import android.os.Bundle;

public class PatrolTaskEachTagList extends NfcActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(SystemParamsUtil.getInstance().getIsLogin()){
			
		}else {
			
		}
		
	}

}
