package com.env.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.env.adapter.TaskPicAdapter;
import com.env.bean.EP_DicValue;
import com.env.component.PatrolApplication;
import com.env.easypatrol.R;
import com.env.nfc.NfcActivity;
import com.env.utils.DataBaseUtil;
import com.env.utils.LocalDataHelper;
import com.env.utils.SystemMethodUtil;
import com.env.utils.SystemParamsUtil;
import com.env.utils.ViewUtil;
import com.env.widget.AddPicConfirmWindow;
import com.env.widget.DeleteConfirmWindow;
import com.env.widget.MyGridView;

import org.apache.commons.lang3.StringUtils;

public class ActivitySingleTask extends NfcActivity implements OnClickListener{

	private HashMap<String, String> task;
	private TextView title,back,save;
	private MyGridView gridView;
	private LinearLayout groupContent;
	private List<EP_DicValue> content;
	private ArrayList<HashMap<String,String>> media;
	private SQLiteDatabase db;
	private List<Integer> contentSelected;
	private TaskPicAdapter adapter;
	private int saveOk=0,tmpIndex;
	private boolean isMustUseText,isMustUsePic;
	private EditText editText;
	private String tmpFilePath;
	private AddPicConfirmWindow addPicConfirmWindow;
	private DeleteConfirmWindow deleteConfirmWindow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_singletask);
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

		content = LocalDataHelper.getTaskTextValues(db, Integer.parseInt(task.get("DicID")));
		media = LocalDataHelper.getTaskMeddiaByTaskID(db, task.get("TaskID"));
		HashMap<String,String> map = new HashMap<String, String>();
		map.put("TaskID", "");
		media.add(map);

		adapter = new TaskPicAdapter(ActivitySingleTask.this,media);

		if(content==null)content=new ArrayList<EP_DicValue>();

		if(task.get("IsDone").equals("1")){
			for (EP_DicValue arg: content) {
				arg.setSteps(LocalDataHelper.getTaskValueStepByDicValueIdReturnString(db, String.valueOf(arg.getDicValueID())));
				arg.setSelectedSteps(LocalDataHelper.getTaskValueSelectedStepsByDicValue(db,task.get("TaskID"),arg.getTextValue()));
			}
		}else{
			for (EP_DicValue arg: content) {
				arg.setSteps(LocalDataHelper.getTaskValueStepByDicValueIdReturnString(db, String.valueOf(arg.getDicValueID())));
				arg.setSelectedSteps("");
			}
		}
	}

	private boolean isStringSelected(String value ,String values){
		String [] arr = values.split(",");
		if(arr.length<1) return false;
		else{
			for (String arg :arr){
				if(arg.equals(value))return true;
			}
		}
		return false;
	}

	@Override
	public void iniView() {
		super.iniView();
		title = (TextView)findViewById(R.id.title);
		back = (TextView)findViewById(R.id.back);
		save = (TextView)findViewById(R.id.save);
		editText = (EditText)findViewById(R.id.text);
		gridView = (MyGridView)findViewById(R.id.grid);

		editText.setText(task.get("Text"));
		TextView text = (TextView)findViewById(R.id.text_head);
		if(isMustUseText) text.setText(text.getText()+"(必须填写)");
		TextView  pic = (TextView)findViewById(R.id.pic_head);
		if(isMustUsePic) pic.setText(pic.getText()+"(必须拍照)");
		gridView.setAdapter(adapter);

		groupContent = (LinearLayout)findViewById(R.id.group_content);

		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				HashMap<String,String> map = (HashMap<String ,String>) view.getTag();
				if(map.get("TaskID")==null || map.get("TaskID").length()<1){
					if(media.size()>TaskPicAdapter.PICTURE_COUNT){
						Toast.makeText(ActivitySingleTask.this, "最多只能添加5张图片", Toast.LENGTH_SHORT).show();
					}else{
						String picPath = Environment.getExternalStorageDirectory().getAbsolutePath()+
								File.separator+"EnvEasyPatrol"+File.separator+"pics"+File.separator;
						File folder = new File(picPath);
						if(!folder.exists()){
							folder.mkdirs();
						}
						tmpFilePath = picPath + new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(new Date()) +".jpg";
						addPicConfirmWindow = new AddPicConfirmWindow(ActivitySingleTask.this,tmpFilePath);
						addPicConfirmWindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_multitask,null), Gravity.NO_GRAVITY,0,0);
					}
				} else{
					Intent intent = new Intent();
					intent.setClass(ActivitySingleTask.this, ActivityPicturePreview.class);
					intent.putExtra("files", media);
					intent.putExtra("index",position);
					startActivity(intent);
				}
			}
		});

		gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				HashMap<String,String> map = (HashMap<String ,String>) view.getTag();
				tmpIndex = position;
				if(map.get("TaskID")!=null && map.get("TaskID").length()>0){
					deleteConfirmWindow = new DeleteConfirmWindow(ActivitySingleTask.this, new OnClickListener() {
						@Override
						public void onClick(View v) {
							media.remove(tmpIndex);
							adapter.notifyDataSetChanged();
							deleteConfirmWindow.dismiss();
						}
					});
					deleteConfirmWindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_multitask,null), Gravity.NO_GRAVITY,0,0);
				}
				return false;
			}
		});

		for(int j=0;j<content.size();j++){
			LinearLayout contentView = createDivValueView(content.get(j),j);
			groupContent.addView(contentView);
		}

		title.setOnClickListener(this);
		back.setOnClickListener(this);
		save.setOnClickListener(this);
		title.setText(task.get("PatrolName"));
	}

	private void changeCheckBoxState(String tag,boolean arg){
		String [] indexs = tag.split("=");
		int [] indexArr = getPindexAndIndexOfCheckBox(tag);
		CheckBox checkBox = getCheckBoxByPindexAndIndex(indexArr);
		checkBox.setChecked(arg);
	}


	private CheckBox getCheckBoxByPindexAndIndex(int [] tags){
		LinearLayout linearLayout = (LinearLayout)groupContent.getChildAt(tags[0]);
		int rightId = tags[1]+1;
		RelativeLayout relativeLayout = (RelativeLayout)linearLayout.getChildAt(rightId);
		CheckBox checkBox = (CheckBox)relativeLayout.findViewById(R.id.value);
		return checkBox;
	}


	private int [] getPindexAndIndexOfCheckBox(String tag){
		String [] indexs = tag.split("=");
		int [] indexIntArr = new int[2];
		indexIntArr[0] = Integer.parseInt(indexs[0]);
		indexIntArr[1] = Integer.parseInt(indexs[1]);
		return indexIntArr;
	}

	private LinearLayout createDivValueView(EP_DicValue dicValue,int pIndex){
		//定义 pindex=-1 为 valuecheckbox 的tag
		//定义 pindex=index 为stepcheckbox的tag


		//添加一个 group 包含 value 和step 选项
		LinearLayout valueGroup = new LinearLayout(this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		valueGroup.setLayoutParams(lp);
		valueGroup.setOrientation(LinearLayout.VERTICAL);
		valueGroup.setTag(pIndex + "=-1");

		//添加 value
		valueGroup.setBackgroundResource(R.drawable.taskpage_valuegroup);
		RelativeLayout valueView = ViewUtil.getContentItemViewSinlgeChoice(getLayoutInflater(), dicValue.getTextValue(), isStringSelected(dicValue.getTextValue(), task.get("Value")));
		valueGroup.addView(valueView);

		RadioButton rdValue = (RadioButton)valueView.findViewById(R.id.value);
		rdValue.setTag(pIndex + "=-1");
		rdValue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int[] indexArr = getPindexAndIndexOfCheckBox(buttonView.getTag().toString());
				if (isChecked) {
					for (int i = 0; i < groupContent.getChildCount(); i++) {
						if(i!=indexArr[0]){
							LinearLayout linearLayout = (LinearLayout) groupContent.getChildAt(i);
							RelativeLayout relativeLayout = (RelativeLayout)linearLayout.getChildAt(0);
							RadioButton valueRd = (RadioButton)relativeLayout.findViewById(R.id.value);
							valueRd.setChecked(false);
							for (int j = 1; j < linearLayout.getChildCount(); j++) {
								changeCheckBoxState(i + "=" + (j - 1), false);
							}
						}
					}
				}
			}
		});



		//添加step
		String [] steps = StringUtils.split(dicValue.getSteps(),",");
		for(int i =0;i<steps.length;i++){
			RelativeLayout stepView = ViewUtil.getStepItemView(getLayoutInflater(),steps[i],dicValue.getSelectedSteps().contains(steps[i]));
			CheckBox cbStep = (CheckBox)stepView.findViewById(R.id.value);
			cbStep.setTag(pIndex+"="+i);
			cbStep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					int [] indexArr = getPindexAndIndexOfCheckBox(buttonView.getTag().toString());
					if(isChecked){
						LinearLayout linearLayout = (LinearLayout) groupContent.getChildAt(indexArr[0]);
						RelativeLayout relativeLayout = (RelativeLayout)linearLayout.getChildAt(0);
						RadioButton valueRd = (RadioButton)relativeLayout.findViewById(R.id.value);
						valueRd.setChecked(true);
					}
				}
			});
			valueGroup.addView(stepView);
		}
		return valueGroup;
	}

	private String getSelectedStepsByPindex(int pIndex){
		LinearLayout linearLayout = (LinearLayout)groupContent.getChildAt(pIndex);
		String result = "";
		for(int i=1;i<linearLayout.getChildCount();i++){
			int [] indexArr = {pIndex,i-1};
			CheckBox cb = getCheckBoxByPindexAndIndex(indexArr);
			if(cb.isChecked()){
				result += content.get(pIndex).getSteps().split(",")[i-1]+",";
			}
		}
		if(result.length()>0){
			result = result.substring(0,result.length()-1);
		}
		return result;
	}


	private int save() {
		SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
		String dateTimeNow = sdf.format(new Date());
		String dateTimeEnd = task.get("EndDateTime");
		if(Long.valueOf(dateTimeNow)>Long.valueOf(dateTimeEnd)){
			Toast.makeText(ActivitySingleTask.this, "保存失败，任务已超时", Toast.LENGTH_SHORT).show();
			return -1;
		}
		if(isMustUsePic && media.size()<2){
			Toast.makeText(ActivitySingleTask.this, "保存失败，必须上传至少1张图片", Toast.LENGTH_SHORT).show();
			return 0;
		}
		if(isMustUseText && editText.getText().toString().trim().length()<1){
			Toast.makeText(ActivitySingleTask.this, "保存失败，备注必须填写", Toast.LENGTH_SHORT).show();
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
				Toast.makeText(ActivitySingleTask.this, "保存失败,请重试", Toast.LENGTH_SHORT).show();
				return 0;
			}
			task.put("DataID", String.valueOf(dataId));
		}else {
			LocalDataHelper.updateEpResult(db, resultValues, task);
		}

		contentSelected = new ArrayList<Integer>();
		for (int i=0;i<content.size();i++) {
			LinearLayout linearLayout = (LinearLayout)groupContent.getChildAt(i);
			RelativeLayout relativeLayout = (RelativeLayout)linearLayout.getChildAt(0);
			RadioButton v = (RadioButton)relativeLayout.findViewById(R.id.value);
			if(v.isChecked()){
				contentSelected.add(i);
			}
		}

		db.delete("EP_PatrolResult_String", "DataID = ?", new String[]{task.get("DataID")});
		for (int i : contentSelected) {
			ContentValues tmp = new ContentValues();
			tmp.put("DataID", task.get("DataID"));
			tmp.put("DValue", content.get(i).getTextValue());
			tmp.put("OPDateTime", dateTimeNow);
			tmp.put("IsUpload", 0);
			tmp.put("Steps",getSelectedStepsByPindex(i));
			LocalDataHelper.updateEpResultString(db, tmp, task.get("DataID"));
		}

		ContentValues taskValues = new ContentValues();
		taskValues.put("IsDone", "1");
		taskValues.put("DoneUserID", SystemParamsUtil.getInstance().getLoginUser(getSharedPreferences(PatrolApplication.PREFS_NAME, MODE_PRIVATE)).getUserID());
		taskValues.put("SampleTime", dateTimeNow);
		taskValues.put("HasRemind", "1");
		LocalDataHelper.updateEpTask(db, taskValues, task.get("TaskID"));

		db.delete("EP_PatrolResult_Media","TaskID=?",new String[]{task.get("TaskID")});
		for (HashMap<String,String> pic : media){
			if(pic.get("TaskID")!=null && pic.get("TaskID").length() >0){
				ContentValues tmp = new ContentValues();
				tmp.put("TaskID",pic.get("TaskID"));
				tmp.put("FilePath",pic.get("FilePath"));
				tmp.put("IsUpload",0);
				db.insert("EP_PatrolResult_Media",null,tmp);
			}
		}
		Toast.makeText(ActivitySingleTask.this, "保存成功", Toast.LENGTH_SHORT).show();
		return 1;
	}

	private HashMap<String, String> receiveTask(){
		Intent intent = getIntent();
		return (HashMap<String, String>)intent.getSerializableExtra("task");
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.back:
				onBackPressed();
				break;
			case R.id.title:

				break;
			case R.id.save:
				saveOk = save();
				if(saveOk!=0) onBackPressed();
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			switch (requestCode){
				case R.id.resource_pick:
					Uri uri = data.getData();
					if(uri.getScheme().equals("content")){
						tmpFilePath = SystemMethodUtil.changeURItoPath(uri, ActivitySingleTask.this);
						if(!tmpFilePath.toLowerCase().endsWith(".jpg")){
							Toast.makeText(ActivitySingleTask.this, "不支持的图片格式", Toast.LENGTH_SHORT).show();
							return;
						}
					}else if(uri.getScheme().equals("file")){
						tmpFilePath = uri.getEncodedPath();
						if(!tmpFilePath.toLowerCase().endsWith(".jpg")){
							Toast.makeText(ActivitySingleTask.this, "不支持的图片格式", Toast.LENGTH_SHORT).show();
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


	@Override
	protected void onStart() {
		super.onStart();
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
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	
}
