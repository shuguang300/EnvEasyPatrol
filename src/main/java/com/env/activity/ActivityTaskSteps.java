package com.env.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.env.bean.EP_DicStep;
import com.env.easypatrol.R;
import com.env.nfc.NfcActivity;
import com.env.utils.DataBaseUtil;
import com.env.utils.LocalDataHelper;
import com.env.utils.ViewUtil;

public class ActivityTaskSteps extends NfcActivity{
	
	private TextView back,title;
	private LinearLayout groupStep;
	private String dicValueId,dicValueText,stepText;
	private String steps[];
	private List<EP_DicStep> stepsList;
	private int index,mode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tasksteps);
		iniData();
		iniView();
	}
	
	@Override
	public void iniData() {
		super.iniData();
		Intent intent = getIntent();
		SQLiteDatabase db = DataBaseUtil.getInstance(this).getReadableDatabase();
		mode = intent.getIntExtra("Mode", 0);
		stepText = intent.getStringExtra("StepText");
		dicValueText = intent.getStringExtra("TextValue");
		if(mode==1){
			dicValueId = intent.getStringExtra("DicValueID");
			index = intent.getIntExtra("Index", -1);
		}
		stepsList = LocalDataHelper.getTaskValueStepByDicValueId(db, dicValueId);
		if(stepsList ==null) stepsList = new ArrayList<EP_DicStep>();
		steps = new String[stepsList.size()];
		for (int i=0;i<stepsList.size();i++) {
			steps[i] = stepsList.get(i).getStepText();
		}
	
	}
	
	@Override
	public void iniView() {
		super.iniView();
		back = (TextView)findViewById(R.id.back);
		title = (TextView)findViewById(R.id.title);
		groupStep = (LinearLayout)findViewById(R.id.group_step);
		
		title.setText(dicValueText);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		for (EP_DicStep data : stepsList) {
			RelativeLayout relativeLayout = ViewUtil.getContentItemView(getLayoutInflater(),data.getStepText(),stepText.contains(data.getStepText()));
			groupStep.addView(relativeLayout);
		}
	}
	
	private String save() {
		String result = "";
		for (int i =0 ;i<groupStep.getChildCount();i++) {
			RelativeLayout relativeLayout = (RelativeLayout)groupStep.getChildAt(i);
			CheckBox cb = (CheckBox)relativeLayout.findViewById(R.id.value);
			if(cb.isChecked()){
				if(i==groupStep.getChildCount()-1)result+= stepsList.get(i).getStepText();
				else result+= stepsList.get(i).getStepText()+",";
			}
		}
		return result;
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		if(mode==0){

		}else{
			String result = save();
			intent.putExtra("Result", result);
			intent.putExtra("Index", index);
		}
		setResult(RESULT_OK, intent);
		finish();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
}
