package com.env.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.env.easypatrol.R;
import com.env.nfc.NfcActivity;
import com.env.utils.DataBaseUtil;
import com.env.utils.LocalDataHelper;
import com.env.utils.SystemParamsUtil;
import com.env.utils.ViewUtil;
import com.env.widget.AudioRecordTask;
import com.env.widget.DataInput;
import com.env.widget.ImgRecordTask;
import com.env.widget.MultiChoice;
import com.env.widget.SingleChoice;

public class ActivityTaskEachCard extends NfcActivity implements OnClickListener{
	private DataInput dataInputNum;
	private DataInput dataInputStr;
	private MultiChoice multiChoice ;
	private SingleChoice singleChoice;
	private ImgRecordTask imgRecordTask;
	private AudioRecordTask audioRecordTask;
	private Intent getedIntent;
	private HashMap<String, String> card;
	private ArrayList<HashMap<String, String>> tags;
	private TextView cardName,titleBack;
	private SQLiteDatabase db;
	private ListView tagList;
	private TaglistAdapter adapter;
	private int ResultType;
	private View view;
	private Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.patroltask_each_card);
		SystemParamsUtil.getInstance().addActivity(this);
		ini();
	}
	
	@SuppressWarnings("unchecked")
	private void ini (){
		getedIntent = getIntent();
		context = ActivityTaskEachCard.this;
		db = DataBaseUtil.getInstance(ActivityTaskEachCard.this).getReadableDatabase();
		card = (HashMap<String, String>)getedIntent.getSerializableExtra("Child");
		cardName = (TextView)findViewById(R.id.eachcard_cardname);	
		tagList = (ListView)findViewById(R.id.eachcard_tag);
		titleBack = (TextView)findViewById(R.id.eachcard_back);
		cardName.setText(card.get("CardName"));
		titleBack.setOnClickListener(this);
	}
	
	public void setData(){
		tags = LocalDataHelper.getPatrolTagByCard(db, Integer.valueOf(card.get("CardID")));
		if (tags!=null) {
			for (HashMap<String, String> tag : tags) {
				tag.put("Description", LocalDataHelper.getPlanDescByTag(db, Integer.valueOf(tag.get("PatrolTagID"))));
			}
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
			setData();
			adapter = new TaglistAdapter();
			tagList.setAdapter(adapter);	
		}else{
			startActivity(new Intent(ActivityTaskEachCard.this,ActivityLogin.class));
			finish();
		}
		
	}
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	class TaglistAdapter extends BaseAdapter{

		@Override
		public int getCount() {			
			if(tags==null)return 0;
			else return tags.size();
		}

		@Override
		public Object getItem(int position) {
			return tags.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			HashMap<String, String> tag = tags.get(position);
			ResultType = Integer.valueOf(tag.get("ResultType"));
			EachCardClickListener onClickListener = new EachCardClickListener(tag);
			switch (ResultType) {
			case 0://数字
				dataInputNum = new DataInput(context,tag);
				dataInputNum.setName(tag.get("PatrolName"));
				dataInputNum.setTime(tag.get("Unit")==null?"":"单位:"+tag.get("Unit"));
				dataInputNum.setSingleClick(onClickListener, 0);
				dataInputNum.setTime(tag.get("Description"));
//				dataInputNum.setLongClickEvent();
				view = dataInputNum;
				break;
			case 1://文本
				dataInputStr = new DataInput(context,tag);
				dataInputStr.setName(tag.get("PatrolName"));
				dataInputStr.setSingleClick(onClickListener, 0);
				dataInputStr.setTime(tag.get("Description"));
//				dataInputStr.setLongClickEvent();
				view = dataInputStr;
				break;
			case 2://多选			
				multiChoice = new MultiChoice(context,tag);
				multiChoice.setName(tag.get("PatrolName"));
				multiChoice.setSingleClick(onClickListener, 0);
				multiChoice.setTime(tag.get("Description"));
//				multiChoice.setLongClickEvent();
				view = multiChoice;						
				break;
			case 3://单选
				singleChoice = new SingleChoice(context,tag);				
				singleChoice.setName(tag.get("PatrolName"));
				singleChoice.setSingleClick(onClickListener, 0);
				singleChoice.setTime(tag.get("Description"));
//				singleChoice.setLongClickEvent();
				view = singleChoice;			
				break;
			case 4://图片
				imgRecordTask = new ImgRecordTask(context,tag);
				imgRecordTask.setName(tag.get("PatrolName"));
				imgRecordTask.setSingleClick(onClickListener, 0);
				imgRecordTask.setTime(tag.get("Description"));
//				imgRecordTask.setLongClickEvent();
				view = imgRecordTask;
				break;
			case 5://录音
				audioRecordTask = new AudioRecordTask(context,tag);
				audioRecordTask.setName(tag.get("PatrolName"));
				audioRecordTask.setSingleClick(onClickListener, 0);
				audioRecordTask.setTime(tag.get("Description"));
//				audioRecordTask.setLongClickEvent();
				view = audioRecordTask;
				break;				
			}
			return view;
		}
		
	}
	
	class EachCardClickListener implements OnClickListener{
		private HashMap<String, String> taskToSend;
		public EachCardClickListener (HashMap<String, String> arg0){
			taskToSend = arg0;
		}
		@Override
		public void onClick(View v) {
			ViewUtil.getInstance().LongClickDialog(taskToSend, ActivityTaskEachCard.this).show();
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.eachcard_back) {
			onBackPressed();
		} else {
		}
		
	}
}
