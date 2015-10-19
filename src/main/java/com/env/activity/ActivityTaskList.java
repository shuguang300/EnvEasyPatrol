package com.env.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.env.bean.EP_Device;
import com.env.component.DataService;
import com.env.component.DataService.DataServiceBinder;
import com.env.component.PatrolApplication;
import com.env.easypatrol.R;
import com.env.nfc.NfcActivity;
import com.env.utils.DataBaseUtil;
import com.env.utils.DialogUtil;
import com.env.utils.LocalDataHelper;
import com.env.utils.SystemMethodUtil;
import com.env.utils.SystemParamsUtil;
import com.env.utils.ViewUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityTaskList extends NfcActivity{
	private int VIEW_HISTORY_TASK = 1;
	private String [] LONG_CLICK_DIALOG_ITEM = {"查看本地数据","查看远程数据"};
	private Intent getIntent, service;
	private String toastInfo;
	private HashMap<String, String> card;
	private ArrayList<HashMap<String, String>> tasks;
	private List<EP_Device> devices;
	private SQLiteDatabase db;
	private PopupMenu optionsMenu;
	private TaskNFCCardReceiver taskNFCCardReceiver;
	private Timer timer = null, fastUpload = null;
	private DeviceListAdapter deviceListAdapter;
	private SharedPreferences sp;
	private SharedPreferences.Editor editor;
	private Message msg;
	private TaskListAdapter adapter;
	private ListView listView,deviceListView;
	private DrawerLayout drawerLayout;
	private int deviceIndex = 0;
	private int days, mode;
	private boolean  mustUseCard,showTaskTime = false;
	private long oneDayMiles = 86400000;
	private DataServiceBinder binder;
	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (DataServiceBinder) service;
		}
	};
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				adapter.notifyDataSetChanged();
				break;
			case 1:
				switch (msg.arg1) {
				case DataService.Success:
					update();
					break;
				case DataService.Error:
					Toast.makeText(getApplicationContext(), "上传失败", Toast.LENGTH_SHORT).show();
					break;
				}
				if (fastUpload != null) {
					fastUpload.cancel();
					fastUpload = null;
				}
				break;
			case DataService.WifiDisabled:
				Toast.makeText(ActivityTaskList.this, "网络不可用", Toast.LENGTH_SHORT).show();
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tasklist);
		initialize();
	}
	
	protected void onResume() {
		super.onResume();
		if (SystemParamsUtil.getInstance().getIsLogin()) {
			if (binder == null) {
				mBindService();
			}
		} else {
			startActivity(new Intent(ActivityTaskList.this, ActivityLogin.class));
			finish();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private void initialize() {
		getIntent = getIntent();
		sp = getSharedPreferences(PatrolApplication.PREFS_NAME, MODE_PRIVATE);
		editor = sp.edit();
		mBindService();
		db = DataBaseUtil.getInstance(ActivityTaskList.this).getReadableDatabase();
		listView = (ListView)findViewById(R.id.tasklist);
		deviceListView = (ListView)findViewById(R.id.devicelist);
		drawerLayout = (DrawerLayout)findViewById(R.id.drawlayout);
		drawerLayout.openDrawer(Gravity.LEFT);
		toastInfo = getResources().getString(R.string.toast_must);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				HashMap<String, String> map = tasks.get(position);
				boolean IsMustUseNFCCard = map.get("IsMustUseNFCCard").equals("1") ? true : false;
				int resultType = Integer.parseInt(map.get("ResultType"));
				boolean clickMode = canClick(IsMustUseNFCCard, mode);
				if (clickMode) {
					Intent intent = new Intent();
					intent.putExtra("task", map);
					switch (resultType) {
						case 0:
							intent.setClass(ActivityTaskList.this, ActivityNumberTask.class);
							break;
						case 2:
							intent.setClass(ActivityTaskList.this, ActivityMultiTask.class);
							break;
						case 3:
							intent.setClass(ActivityTaskList.this, ActivitySingleTask.class);
							break;
					}
					startActivityForResult(intent, 0);
				} else {
					Toast.makeText(ActivityTaskList.this, toastInfo, Toast.LENGTH_SHORT).show();
				}
			}
		});
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				HashMap<String, String> map = tasks.get(position);
				final Intent intent = new Intent();
				intent.putExtra("task", map);
				AlertDialog.Builder longClickDialg = new AlertDialog.Builder(ActivityTaskList.this);
				longClickDialg.setCancelable(true);
				longClickDialg.setSingleChoiceItems(LONG_CLICK_DIALOG_ITEM, -1, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						switch (which) {
							case 0:
								intent.setClass(ActivityTaskList.this, ActivityTaskEachTag.class);
								startActivityForResult(intent, VIEW_HISTORY_TASK);
								break;
							case 1:
								intent.setClass(ActivityTaskList.this, ActivityEachTagWebView.class);
								startActivityForResult(intent, VIEW_HISTORY_TASK);
								break;
						}
					}
				});
				longClickDialg.create().show();
				return false;
			}
		});
		deviceListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				deviceIndex = position;
				deviceListAdapter.notifyDataSetChanged();
				update();
				drawerLayout.closeDrawer(Gravity.LEFT);
			}
		});

		card = (HashMap<String, String>) getIntent.getSerializableExtra("Child");
		mode = getIntent.getExtras().getInt("Mode");
		setPlantData(card.get("CardID"));
		setData();
		prepareForView();
		if (timer == null) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					calculateData();
					Message msg = handler.obtainMessage();
					msg.what = 0;
					msg.sendToTarget();
				}
			}, 0, 5000);
		}

		taskNFCCardReceiver = new TaskNFCCardReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.env.view.PatrolTaskNFCCard.TaskNFCCardReceiver");
		registerReceiver(taskNFCCardReceiver, intentFilter);
		initialActionBar();
	}

	private void initialActionBar(){
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(card.get("CardName"));
		actionBar.show();
	}

	private void setPlantData(String cardId){
		devices = LocalDataHelper.getDeviceByCardId(db, cardId);
		if (devices == null) devices = new ArrayList<EP_Device>();
	}

	private void setData() {
		if(devices.size()==0){
			tasks = new ArrayList<HashMap<String, String>>();
		}else{
			SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
			Calendar nowClendar = Calendar.getInstance();
			long todayInt = Long.valueOf(sdf.format(nowClendar.getTime()));
			String nowDTStr = sdf.format(nowClendar.getTime());
			String startDateStr = nowDTStr.substring(0, 8);
			nowClendar.add(Calendar.DAY_OF_YEAR, 1);
			String endDateStr = sdf.format(nowClendar.getTime()).substring(0, 8);
			tasks = new ArrayList<HashMap<String, String>>();
			db = DataBaseUtil.getInstance(ActivityTaskList.this).getReadableDatabase();
			tasks = LocalDataHelper.getNewestTaskByDevice(db, nowDTStr, startDateStr + "000000", endDateStr + "000000", devices.get(deviceIndex).getDeviceID());
			for (int i = 0; i < tasks.size(); i++) {
				tasks.get(i).put("Value", "");
				tasks.get(i).put("IsEnable", "1");
				tasks.get(i).put("Visible", "1");
				tasks.get(i).put("StatuCode", ViewUtil.VIEW_STATU_CODE_DOING + "");
				if (tasks.get(i).get("IsDone").equals("1")) {
					tasks.get(i).put("SampleTime", tasks.get(i).get("OPDateTime"));
					tasks.get(i).put("IsEnable", "0");
					String [] valueAndStep = LocalDataHelper.getTaskValuesAndSteps(db, tasks.get(i).get("ResultType").equals("0") ? "EP_PatrolResult_Number" : "EP_PatrolResult_String", Integer.valueOf(tasks.get(i).get("TaskID")));
					tasks.get(i).put("Value", valueAndStep[0]);
					tasks.get(i).put("Step", valueAndStep[1]);
				} else {
					mustUseCard = tasks.get(i).get("IsMustUseNFCCard").equals("1") ? true : false;
					if ((mode == ActivityTaskConstruction.CARD_NFC && mustUseCard) || !mustUseCard) {
						if (tasks.get(i).get("DefaultDicIntValues") != null) {
							tasks.get(i).put("Value", LocalDataHelper.getTaskDefaultValues(db, tasks.get(i).get("DefaultDicIntValues"), Integer.parseInt(tasks.get(i).get("DicID"))));
							tasks.get(i).put("Step", "");
							tasks.get(i).put("SampleTime", new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(Calendar.getInstance().getTime()));
							tasks.get(i).put("IsEnable", "0");
						}
					}
				}
				tasks.get(i).put("StatuCode", getStatuCode(todayInt, tasks.get(i)) + "");
			}
			calculateData();
		}
	}

	private int getStatuCode(long todayNum, HashMap<String, String> task) {
		boolean candelay = task.get("EnableDelay").equals("1") ? true : false;
		int statuCode = ViewUtil.VIEW_STATU_CODE_DOING;
		if (task.get("IsUpload").equals("1")) {
			statuCode = ViewUtil.VIEW_STATU_CODE_UPLOADED;
			task.put("IsEnable", "0");
		} else {
			if (task.get("IsDone").equals("1")) {
				if (todayNum < Long.valueOf(task.get("StopDateTime")) && todayNum >= Long.valueOf(task.get("StartDateTime"))) {
					statuCode = ViewUtil.VIEW_STATU_CODE_DOING;
					task.put("IsEnable", "1");
				} else if (todayNum >= Long.valueOf(task.get("StopDateTime"))) {
					if (candelay) {
						if (todayNum >= Long.valueOf(task.get("StopDateTime")) && todayNum < Long.valueOf(task.get("EndDateTime"))) {
							statuCode = ViewUtil.VIEW_STATU_CODE_DELAY;
							task.put("IsEnable", "1");
						} else {
							statuCode = ViewUtil.VIEW_STATU_CODE_DONE_PAST;
							task.put("IsEnable", "0");
						}
						;
					} else {
						statuCode = ViewUtil.VIEW_STATU_CODE_DONE_PAST;
						task.put("IsEnable", "0");
					}
				}
			} else {
				if (todayNum < Long.valueOf(task.get("StopDateTime")) && todayNum >= Long.valueOf(task.get("StartDateTime"))) {
					statuCode = ViewUtil.VIEW_STATU_CODE_DOING;
					task.put("IsEnable", "1");
				} else if (todayNum >= Long.valueOf(task.get("StopDateTime"))) {
					if (candelay) {
						if (todayNum >= Long.valueOf(task.get("StopDateTime")) && todayNum < Long.valueOf(task.get("EndDateTime"))) {
							statuCode = ViewUtil.VIEW_STATU_CODE_DELAY;
							task.put("IsEnable", "1");
						} else {
							statuCode = ViewUtil.VIEW_STATU_CODE_UNDO_PAST;
							task.put("IsEnable", "0");
						}
						;
					} else {
						statuCode = ViewUtil.VIEW_STATU_CODE_UNDO_PAST;
						task.put("IsEnable", "0");
					}
				} else if (todayNum < Long.valueOf(task.get("StartDateTime"))) {
					statuCode = ViewUtil.VIEW_STATU_CODE_WAIT;
					task.put("IsEnable", "0");
				}
			}
		}
		return statuCode;
	}

	private void calculateData() {
		HashMap<String, String> task;
		SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
		String nowDateTimeStr = sdf.format(Calendar.getInstance().getTime());
		String result, pre;
		int statuCode;
		for (int i = 0; i < tasks.size(); i++) {
			task = tasks.get(i);
			statuCode = Integer.valueOf(task.get("StatuCode"));
			result = getTimeCount(nowDateTimeStr, task);
			pre = "";
			switch (statuCode) {
			case ViewUtil.VIEW_STATU_CODE_UPLOADED:
				pre = "已上传";
				break;
			case ViewUtil.VIEW_STATU_CODE_WAIT:
				pre = "后开始";
				if (result.equals("00时00分00")) {
					task.put("StatuCode", ViewUtil.VIEW_STATU_CODE_DOING + "");
					task.put("IsEnable", "1");
				}
				break;
			case ViewUtil.VIEW_STATU_CODE_UNDO_PAST:
				pre = "已过期";
				break;
			case ViewUtil.VIEW_STATU_CODE_DONE_PAST:
				try {
					SimpleDateFormat sdf2 = new SimpleDateFormat(SystemMethodUtil.ShortTimeCHSdf);
					pre = "任务完成时间 :" + sdf2.format(sdf.parse(task.get("SampleTime")));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				break;
			case ViewUtil.VIEW_STATU_CODE_DOING:
				if (task.get("EndDateTime").equals(task.get("StopDateTime"))) {
					pre = "后关闭";
				} else {
					pre = "后过期";
				}
				if (result.equals("00时00分00")) {
					task.put("StatuCode", ViewUtil.VIEW_STATU_CODE_DELAY + "");
					task.put("IsEnable", "1");
				} else if (result.equals("该任务可一直执行")) {
					pre = "";
				}
				break;
			case ViewUtil.VIEW_STATU_CODE_DELAY:
				pre = "后关闭";
				if (result.equals("00时00分00")) {
					if (task.get("IsDone").equals("1")) {
						task.put("StatuCode", ViewUtil.VIEW_STATU_CODE_DONE_PAST + "");
						task.put("IsEnable", "0");
					} else {
						task.put("StatuCode", ViewUtil.VIEW_STATU_CODE_UNDO_PAST + "");
						task.put("IsEnable", "0");
					}
				} else if (result.equals("该任务可一直执行")) {
					pre = "";
				}
				break;
			}
			if(Long.valueOf(nowDateTimeStr)>Long.valueOf(task.get("EndDateTime"))) task.put("IsEnable", "0");
			task.put("TimeDescibe", ViewUtil.getInstance().getTimeDescribe(task.get("StartDateTime")));
			task.put("Statu", result + pre);
		}
	}

	private void prepareForView() {
		deviceListAdapter = new DeviceListAdapter();
		deviceListView.setAdapter(deviceListAdapter);
		adapter = new TaskListAdapter();
		listView.setAdapter(adapter);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			super.onActivityResult(requestCode, resultCode, data);
			update();
		}
	}

	private String getTimeCount(String nowDateTime, HashMap<String, String> task) {
		String timerCount = "";
		Calendar calendar = Calendar.getInstance();
		int statuCode = Integer.valueOf(task.get("StatuCode"));
		long mileSpan;
		Date date;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
			switch (statuCode) {
			case ViewUtil.VIEW_STATU_CODE_UPLOADED:// 已上传
				timerCount = "";
				break;
			case ViewUtil.VIEW_STATU_CODE_WAIT:// 等待执行
				mileSpan = sdf.parse(task.get("StartDateTime")).getTime() - sdf.parse(nowDateTime).getTime();
				days = (int) (mileSpan / oneDayMiles);
				if (days <= 0) {
					date = new Date(mileSpan);
					calendar.setTime(date);
					calendar.add(Calendar.HOUR, -8);
					SimpleDateFormat sdf2 = new SimpleDateFormat(SystemMethodUtil.ShortTimeCHSdf);
					timerCount = sdf2.format(calendar.getTime());
				} else {
					date = new Date(mileSpan % oneDayMiles);
					calendar.setTime(date);
					calendar.add(Calendar.HOUR, -8);
					SimpleDateFormat sdf2 = new SimpleDateFormat(SystemMethodUtil.ShortTimeCHSdf);
					timerCount = days + "天" + sdf2.format(calendar.getTime());
				}
				break;
			case ViewUtil.VIEW_STATU_CODE_UNDO_PAST:// 未执行，且过期
				timerCount = "";
				break;
			case ViewUtil.VIEW_STATU_CODE_DONE_PAST:// 已执行，且过期
				timerCount = "";
				break;
			case ViewUtil.VIEW_STATU_CODE_DOING:// 正在执行
				if (task.get("StopDateTime").equals(SystemMethodUtil.EndlessDate)) {
					timerCount = "该任务可一直执行";
				} else {
					mileSpan = sdf.parse(task.get("StopDateTime")).getTime() - sdf.parse(nowDateTime).getTime();
					days = (int) (mileSpan / oneDayMiles);
					if (days <= 0) {
						date = new Date(mileSpan);
						calendar.setTime(date);
						calendar.add(Calendar.HOUR, -8);
						SimpleDateFormat sdf2 = new SimpleDateFormat(SystemMethodUtil.ShortTimeCHSdf);
						timerCount = sdf2.format(calendar.getTime());
					} else {
						date = new Date(mileSpan % oneDayMiles);
						calendar.setTime(date);
						calendar.add(Calendar.HOUR, -8);
						SimpleDateFormat sdf2 = new SimpleDateFormat(SystemMethodUtil.ShortTimeCHSdf);
						timerCount = days + "天" + sdf2.format(calendar.getTime());
					}
				}

				break;
			case ViewUtil.VIEW_STATU_CODE_DELAY:// 延迟
				if (task.get("EndDateTime").equals(SystemMethodUtil.EndlessDate)) {
					timerCount = "该任务可一直执行";
				} else {
					mileSpan = sdf.parse(task.get("EndDateTime")).getTime() - sdf.parse(nowDateTime).getTime();
					days = (int) (mileSpan / oneDayMiles);
					if (days <= 0) {
						date = new Date(mileSpan);
						calendar.setTime(date);
						calendar.add(Calendar.HOUR, -8);
						SimpleDateFormat sdf2 = new SimpleDateFormat(SystemMethodUtil.ShortTimeCHSdf);
						timerCount = sdf2.format(calendar.getTime());
					} else {
						date = new Date(mileSpan % oneDayMiles);
						calendar.setTime(date);
						calendar.add(Calendar.HOUR, -8);
						SimpleDateFormat sdf2 = new SimpleDateFormat(SystemMethodUtil.ShortTimeCHSdf);
						timerCount = days + "天" + sdf2.format(calendar.getTime());
					}
				}
				break;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return timerCount;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if(drawerLayout.isDrawerOpen(Gravity.LEFT)){
			drawerLayout.closeDrawer(Gravity.LEFT);
			return;
		}
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(conn);
		unregisterReceiver(taskNFCCardReceiver);
	}

	private void mBindService() {
		if (binder == null) {
			service = new Intent();
			service.setAction("com.env.component.DataService");
			bindService(service, conn, Context.BIND_AUTO_CREATE);
		}

	}

	private void uploadData() {
		mBindService();
		int apnType = SystemMethodUtil.getAPNType(ActivityTaskList.this);
		if (binder != null) {
			if (binder.isUploading) {
				Toast.makeText(getApplicationContext(), "正在上传任务，请不要重复操作", Toast.LENGTH_LONG).show();
			} else {
				switch (apnType) {
				case SystemMethodUtil.NoNetWork:
					DialogUtil.setApnDialog(ActivityTaskList.this);
					break;
				case SystemMethodUtil.MobileNetWork:
					sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
					editor = sp.edit();
					if (sp.getBoolean(PatrolApplication.HAS_DATAEXCHANGE_TIPS, true)) {
						DialogUtil.confirmNetWork(ActivityTaskList.this, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								Toast.makeText(getApplicationContext(), "上传数据中....", Toast.LENGTH_LONG).show();
								uploadWithMobileNet();
							}
						}, editor);
					} else {
						Toast.makeText(getApplicationContext(), "上传数据中....", Toast.LENGTH_LONG).show();
						uploadWithMobileNet();
					}
					break;
				case SystemMethodUtil.WifiNetWork:
					Toast.makeText(getApplicationContext(), "上传数据中....", Toast.LENGTH_LONG).show();
					uploadWithWifi();
					break;
				}
			}
		} else {
			Toast.makeText(getApplicationContext(), "服务正在启动中，请稍后重试上传数据", Toast.LENGTH_LONG).show();
		}
	}

	class TaskNFCCardReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			update();
		}
	}


	public void showTag() {
		Intent intent = new Intent(ActivityTaskList.this, ActivityTaskEachCard.class);
		intent.putExtra("Child", card);
		intent.putExtra("Mode", ActivityTaskConstruction.CARD_NORMAL);
		startActivityForResult(intent, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.patroltasknfccard_popupmenu,menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.patroltasknfccard_menu_upload) {
			uploadData();
		} else if (itemId == R.id.patroltasknfccard_menu_showtag) {
			showTag();
		} else if (itemId == R.id.patroltasknfccard_menu_showtime) {
			if (showTaskTime) {
				showTaskTime = false;
				item.setTitle(getResources().getString(R.string.tasknfccard_menu_showtime));
			} else {
				showTaskTime = true;
				item.setTitle(getResources().getString(R.string.tasknfccard_menu_notime));
			}
		} else if (itemId == R.id.patroltasknfccard_menu_changeview){
			Intent intent = new Intent(ActivityTaskList.this, ActivityTaskGroup.class);
			intent.putExtra("Child", card);
			intent.putExtra("Mode", mode);
			startActivity(intent);
			finish();
		} else if(itemId == android.R.id.home){
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 数据更新或者上传后，1，保存数据，2，获取最新数据，3，刷新UI
	 */
	private void update() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		setData();
		adapter.notifyDataSetChanged();
		if (timer == null) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					calculateData();
					Message msg = handler.obtainMessage();
					msg.what = 0;
					msg.sendToTarget();
				}
			}, 0, 5000);
		}

	}

	/**
	 * 使用2g/3G网络上传s
	 */
	public void uploadWithMobileNet() {
		binder.getDataService().uploadOnlyTextData(db);
		if (fastUpload == null) {
			fastUpload = new Timer();
			fastUpload.schedule(new TimerTask() {
				@Override
				public void run() {
					if (!binder.isUploading) {
						msg = handler.obtainMessage();
						msg.what = 1;
						if (binder.isExchangeDataSucss) {
							msg.arg1 = DataService.Success;
						} else {
							msg.arg1 = DataService.Error;
						}
						msg.sendToTarget();
					}
				}
			}, 0, 1000);
		}

	}

	/**
	 * 使用wifi上传
	 */
	public void uploadWithWifi() {
		binder.getDataService().uploadDataJsonObject(db);
		if (fastUpload == null) {
			fastUpload = new Timer();
			fastUpload.schedule(new TimerTask() {
				@Override
				public void run() {
					if (!binder.isUploading) {
						msg = handler.obtainMessage();
						msg.what = 1;
						if (binder.isExchangeDataSucss) {
							msg.arg1 = DataService.Success;
						} else {
							msg.arg1 = DataService.Error;
						}
						msg.sendToTarget();
					}
				}
			}, 0, 1000);
		}

	}
	private boolean canClick(boolean isMustUseNfc,int mode){
		boolean arg0;
		if(isMustUseNfc){
			if(mode==ActivityTaskConstruction.CARD_NFC){
				arg0 = true;
			}else {
				arg0 = false;
			}				
		}else {
			arg0 = true;
		}
		return arg0;
	}
	
	private class ViewHolder{
		public ImageView status;
		public TextView name;
		public TextView time;
	}

	private class DeviceListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return devices.size();
		}

		@Override
		public EP_Device getItem(int position) {
			return devices.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).getDeviceID();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = LayoutInflater.from(ActivityTaskList.this).inflate(android.R.layout.simple_list_item_activated_1,null);
			}
			TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
			tv.setText(getItem(position).getDeviceName());
			if(position == deviceIndex) tv.setBackgroundResource(R.color.lightblue);
			else tv.setBackgroundResource(R.color.white);
			return convertView;
		}
	}
	
	private class TaskListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if(tasks==null){
				return 0; 
			}
			return tasks.size();
		}

		@Override
		public HashMap<String, String> getItem(int position) {
			if(tasks==null){
				return null;
			}
			return tasks.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder viewHolder = null ;
			HashMap<String, String> task = getItem(position);
			if(convertView == null){
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(ActivityTaskList.this).inflate(R.layout.item_tasklist, null);
				viewHolder.name = (TextView)convertView.findViewById(R.id.item_tasklist_name);
				viewHolder.time = (TextView)convertView.findViewById(R.id.item_tasklist_sttime);
				viewHolder.status = (ImageView)convertView.findViewById(R.id.item_tasklist_logo);
				convertView.setTag(viewHolder);
			}else {
				viewHolder = (ViewHolder)convertView.getTag();
			}
			String statu = task.get("Statu");
			String shortStatu="",timeDescribe = "";
			if (statu.equals("该任务可一直执行")) {
				shortStatu = "该任务可一直执行";
			} else {
				try {
					shortStatu = statu.substring(0, statu.length() - 5) + statu.substring(statu.length() - 3, statu.length());
				} catch (Exception e) {
				}
			}
			boolean IsEnable = task.get("IsEnable").equals("1") ? true : false;
			int statuCode = Integer.valueOf(task.get("StatuCode"));
			if (!showTaskTime) {
				timeDescribe = "";
			} else {
				timeDescribe = "开始于" + task.get("TimeDescibe") + "\n";
			}
			switch (statuCode) {
			case ViewUtil.VIEW_STATU_CODE_DONE_PAST:
				viewHolder.status.setBackgroundResource(R.drawable.task_done_past);
				break;
			case ViewUtil.VIEW_STATU_CODE_DELAY:
				viewHolder.status.setBackgroundResource(R.drawable.task_delay);
				break;
			default :
				viewHolder.status.setBackgroundResource(R.drawable.task_doing);
				break;
			}
			if(task.get("IsDone").equals("1"))viewHolder.status.setBackgroundResource(R.drawable.task_done_past);
			viewHolder.name.setText(task.get("PatrolName"));
			viewHolder.time.setText(timeDescribe + shortStatu);
			convertView.setVisibility(IsEnable?View.VISIBLE:View.GONE);
			return convertView;
		}		
	}
}
