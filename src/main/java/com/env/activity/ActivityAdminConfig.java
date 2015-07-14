package com.env.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridLayout;
import android.widget.TextView;

import com.env.easypatrol.R;
import com.env.nfc.NfcActivity;

/**
 * 配置管理界面
 * @author sk
 */
public class ActivityAdminConfig extends NfcActivity implements OnClickListener{
	private TextView back,title;
	private GridLayout opstions;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.patroladmin_config);
		iniView();
	}
	
	
	/**
	 * 初始化控件
	 */
	@Override
	public void iniView(){
		back = (TextView)findViewById(R.id.patroladminconfig_back);
		title = (TextView)findViewById(R.id.patroladminconfig_title);
		opstions = (GridLayout)findViewById(R.id.patroladminconfig_options);
		opstions.setColumnCount(4);
		title.setText("配置项管理");
		back.setOnClickListener(this);
		
	}


	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.patroladminconfig_back) {
			finish();
		}
	}
}
