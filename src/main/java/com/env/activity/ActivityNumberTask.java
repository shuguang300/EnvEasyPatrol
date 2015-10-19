package com.env.activity;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.env.adapter.TaskPicAdapter;
import com.env.component.PatrolApplication;
import com.env.easypatrol.R;
import com.env.nfc.NfcActivity;
import com.env.utils.DataBaseUtil;
import com.env.utils.LocalDataHelper;
import com.env.utils.SystemMethodUtil;
import com.env.utils.SystemParamsUtil;
import com.env.widget.AddPicConfirmWindow;
import com.env.widget.DeleteConfirmWindow;
import com.env.widget.MyGridView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ActivityNumberTask extends NfcActivity{
	private HashMap<String, String> task;
	private MyGridView gridView;
	private ArrayList<HashMap<String,String>> media;
	private SQLiteDatabase db;
	private TaskPicAdapter adapter;
	private int saveOk=0,tmpIndex;
	private boolean isMustUseText,isMustUsePic;
	private EditText editText,valueEditText;
	private String tmpFilePath;
	private AddPicConfirmWindow addPicConfirmWindow;
	private DeleteConfirmWindow deleteConfirmWindow;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_numbertask);
		iniData();
		iniView();
	}

	@Override
	public void iniData() {
		super.iniData();
		db = DataBaseUtil.getInstance(this).getReadableDatabase();
		task = receiveTask();
		isMustUseText = task.get("IsMustUseText").equals("1")?true:false;
		isMustUsePic = task.get("IsMustUsePic").equals("1")?true:false;

		media = LocalDataHelper.getTaskMeddiaByTaskID(db, task.get("TaskID"));
		HashMap<String,String> map = new HashMap<String, String>();
		map.put("TaskID", "");
		media.add(map);

		adapter = new TaskPicAdapter(ActivityNumberTask.this,media);


	}

	@Override
	public void iniView() {
		super.iniView();
		editText = (EditText)findViewById(R.id.text);
		valueEditText = (EditText)findViewById(R.id.value);
		gridView = (MyGridView)findViewById(R.id.grid);

		editText.setText(task.get("Text"));
		valueEditText.setText(task.get("Value"));
		TextView text = (TextView)findViewById(R.id.text_head);
		if(isMustUseText) text.setText(text.getText()+"(必须填写)");
		TextView  pic = (TextView)findViewById(R.id.pic_head);
		if(isMustUsePic) pic.setText(pic.getText()+"(必须拍照)");

		gridView.setAdapter(adapter);


		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				HashMap<String,String> map = (HashMap<String ,String>) view.getTag();
				if(map.get("TaskID")==null || map.get("TaskID").length()<1){
					if(media.size()>TaskPicAdapter.PICTURE_COUNT){
						Toast.makeText(ActivityNumberTask.this, "最多只能添加5张图片", Toast.LENGTH_SHORT).show();
					}else{
						String picPath = Environment.getExternalStorageDirectory().getAbsolutePath()+
								File.separator+"EnvEasyPatrol"+File.separator+"pics"+File.separator;
						File folder = new File(picPath);
						if(!folder.exists()){
							folder.mkdirs();
						}
						tmpFilePath = picPath + new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(new Date()) +".jpg";
						addPicConfirmWindow = new AddPicConfirmWindow(ActivityNumberTask.this,tmpFilePath);
						addPicConfirmWindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_multitask,null), Gravity.NO_GRAVITY,0,0);
					}
				} else{
					Intent intent = new Intent();
					intent.setClass(ActivityNumberTask.this, ActivityPicturePreview.class);
					intent.putExtra("files", media);
					intent.putExtra("index",position);
					startActivity(intent);
				}
			}
		});

		gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				HashMap<String, String> map = (HashMap<String, String>) view.getTag();
				tmpIndex = position;
				if (map.get("TaskID") != null && map.get("TaskID").length() > 0) {
					deleteConfirmWindow = new DeleteConfirmWindow(ActivityNumberTask.this, new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							media.remove(tmpIndex);
							adapter.notifyDataSetChanged();
							deleteConfirmWindow.dismiss();
						}
					});
					deleteConfirmWindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_multitask, null), Gravity.NO_GRAVITY, 0, 0);
				}
				return false;
			}
		});

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(task.get("PatrolName"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_taskpage,menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case android.R.id.home:
				onBackPressed();
				break;
			case R.id.save:
				saveOk = save();
				if(saveOk!=0) onBackPressed();
				break;

		}
		return super.onOptionsItemSelected(item);
	}

	private HashMap<String, String> receiveTask(){
		Intent intent = getIntent();
		return (HashMap<String, String>)intent.getSerializableExtra("task");
	}

	private int save() {
		SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
		String dateTimeNow = sdf.format(new Date());
		String dateTimeEnd = task.get("EndDateTime");
		if(Long.valueOf(dateTimeNow)>Long.valueOf(dateTimeEnd)){
			Toast.makeText(ActivityNumberTask.this, "保存失败，任务已超时", Toast.LENGTH_SHORT).show();
			return -1;
		}
		if(isMustUsePic && media.size()<2){
			Toast.makeText(ActivityNumberTask.this, "保存失败，必须上传至少1张图片", Toast.LENGTH_SHORT).show();
			return 0;
		}
		if(isMustUseText && editText.getText().toString().trim().length()<1){
			Toast.makeText(ActivityNumberTask.this, "保存失败，备注必须填写", Toast.LENGTH_SHORT).show();
			editText.requestFocus();
			return 0;
		}
		ContentValues resultValues = new ContentValues();
		resultValues.put("OPDateTime", dateTimeNow);
		resultValues.put("Text",editText.getText().toString());
		if(task.get("DataID")==null || task.get("DataID").length()==0){
			resultValues.put("PlantID", task.get("PlantID"));
			resultValues.put("PatrolTagID", task.get("PatrolTagID"));
			resultValues.put("TaskID", task.get("TaskID"));
			long dataId = LocalDataHelper.updateEpResult(db, resultValues, task);
			if(dataId<1){
				Toast.makeText(ActivityNumberTask.this, "保存失败,请重试", Toast.LENGTH_SHORT).show();
				return -1;
			}
			task.put("DataID", String.valueOf(dataId));
		}else {
			long dataId = LocalDataHelper.updateEpResult(db, resultValues, task);
			if(dataId <1){
				Toast.makeText(ActivityNumberTask.this, "保存失败,请重试", Toast.LENGTH_SHORT).show();
				return -1;
			}
		}
		db.delete("EP_PatrolResult_Number","DataID = ? ",new String[]{task.get("DataID")});
		ContentValues numberValues = new ContentValues();
		numberValues.put("DataID",task.get("DataID"));
		numberValues.put("DValue",valueEditText.getText().toString());
		numberValues.put("OPDateTime",dateTimeNow);
		numberValues.put("UnitID",task.get("UnitID"));
		db.insert("EP_PatrolResult_Number",null,numberValues);
		ContentValues taskValues = new ContentValues();
		taskValues.put("IsDone", "1");
		taskValues.put("DoneUserID", SystemParamsUtil.getInstance().getLoginUser(getSharedPreferences(PatrolApplication.PREFS_NAME, MODE_PRIVATE)).getUserID());
		taskValues.put("SampleTime", dateTimeNow);
		taskValues.put("HasRemind", "1");
		long index = LocalDataHelper.updateEpTask(db, taskValues, task.get("TaskID"));
		if(index>0){
			db.delete("EP_PatrolResult_Media", "TaskID=?", new String[]{task.get("TaskID")});
			for (HashMap<String,String> pic : media){
				if(pic.get("TaskID")!=null && pic.get("TaskID").length() >0){
					ContentValues tmp = new ContentValues();
					tmp.put("TaskID",pic.get("TaskID"));
					tmp.put("FilePath",pic.get("FilePath"));
					tmp.put("IsUpload",0);
					db.insert("EP_PatrolResult_Media",null,tmp);
				}
			}
			Toast.makeText(ActivityNumberTask.this, "保存成功", Toast.LENGTH_SHORT).show();
			return 1;
		}else{
			Toast.makeText(ActivityNumberTask.this, "保存失败", Toast.LENGTH_SHORT).show();
			return -1;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			switch (requestCode){
				case R.id.resource_pick:
					Uri uri = data.getData();
					if(uri.getScheme().equals("content")){
						tmpFilePath = SystemMethodUtil.changeURItoPath(uri, ActivityNumberTask.this);
						if(!tmpFilePath.toLowerCase().endsWith(".jpg")){
							Toast.makeText(ActivityNumberTask.this, "不支持的图片格式", Toast.LENGTH_SHORT).show();
							return;
						}
					}else if(uri.getScheme().equals("file")){
						tmpFilePath = uri.getEncodedPath();
						if(!tmpFilePath.toLowerCase().endsWith(".jpg")){
							Toast.makeText(ActivityNumberTask.this, "不支持的图片格式", Toast.LENGTH_SHORT).show();
							return;
						}
					}
					break;
				case R.id.resource_take:
					break;
			}
			HashMap<String ,String> map = new HashMap<String, String>();
			map.put("TaskID",task.get("TaskID"));
			map.put("FilePath",tmpFilePath);
			media.add(0,map);
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onBackPressed() {
		if(addPicConfirmWindow !=null && addPicConfirmWindow.isShowing()){
			addPicConfirmWindow.dismiss();
			return;
		}
		if(deleteConfirmWindow !=null && deleteConfirmWindow.isShowing()){
			deleteConfirmWindow.dismiss();
			return;
		}
		setResult(saveOk==1?RESULT_OK:RESULT_CANCELED);
		finish();
	}
}
