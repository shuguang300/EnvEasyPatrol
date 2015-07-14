package com.env.activity;

import android.os.Bundle;

import com.env.nfc.NfcActivity;
import com.env.easypatrol.R;

public class ActivityAdminData extends NfcActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.patroladmin_data);
	}
}
