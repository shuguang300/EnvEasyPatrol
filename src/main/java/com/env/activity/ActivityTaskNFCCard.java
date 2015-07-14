package com.env.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.env.component.DataService;
import com.env.component.DataService.DataServiceBinder;
import com.env.component.PatrolApplication;
import com.env.easypatrol.R;
import com.env.nfc.NfcActivity;
import com.env.utils.DataBaseUtil;
import com.env.utils.DataCenterUtil;
import com.env.utils.DialogUtil;
import com.env.utils.LocalDataHelper;
import com.env.utils.NotificationUtil;
import com.env.utils.SystemMethodUtil;
import com.env.utils.SystemParamsUtil;
import com.env.utils.ViewUtil;
import com.env.widget.AudioRecordTask;
import com.env.widget.DataInput;
import com.env.widget.ImgRecordTask;
import com.env.widget.MultiChoice;
import com.env.widget.SingleChoice;

public class ActivityTaskNFCCard extends NfcActivity implements OnClickListener, OnMenuItemClickListener {
	private Intent getIntent, service;
	private HashMap<String, String> card;
	private ArrayList<HashMap<String, String>> tasks;
	private SQLiteDatabase db;
	private PopupMenu optionsMenu;
	private TaskNFCCardReceiver taskNFCCardReceiver = null;
	private LinearLayout Cards2Tasks;
	private Timer timer = null, fastUpload = null;
	private SharedPreferences sp;
	private SharedPreferences.Editor editor;
	private Message msg;
	private View view;
	private int ResultType, statuCode, days, curCardID, cardID, mode;
	private String statu, shortStatu, timeDescribe, name;
	private boolean IsEnable, isChangingCard = false, mustUseCard;
	private long oneDayMiles = 86400000;
	private TextView titleOptions, titleBack, cardName;
	private ProgressDialog progressDialog;
	private DataServiceBinder binder;
	private boolean showTaskTime = false;
	private Cursor cards;
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
				for (int i = 0; i < Cards2Tasks.getChildCount(); i++) {
					view = Cards2Tasks.getChildAt(i);
					name = tasks.get(i).get("PatrolName");
					ResultType = Integer.valueOf(tasks.get(i).get("ResultType"));
					statu = tasks.get(i).get("Statu");
					if (statu.equals("该任务可一直执行")) {
						shortStatu = "该任务可一直执行";
					} else {
						try {
							shortStatu = statu.substring(0, statu.length() - 5) + statu.substring(statu.length() - 3, statu.length());
						} catch (Exception e) {
							shortStatu = "";
						}
					}
					IsEnable = tasks.get(i).get("IsEnable").equals("1") ? true : false;
					statuCode = Integer.valueOf(tasks.get(i).get("StatuCode"));
					if (!showTaskTime) {
						timeDescribe = "";
					} else {
						timeDescribe = "开始于" + tasks.get(i).get("TimeDescibe") + "\n";
					}
					switch (ResultType) {
					case 0:
						((DataInput) view).setName(name);
						((DataInput) view).setTime(timeDescribe + shortStatu);
						((DataInput) view).setTimeColor(statuCode);
						break;
					case 1:
						((DataInput) view).setName(name);
						((DataInput) view).setTime(timeDescribe + shortStatu);
						((DataInput) view).setTimeColor(statuCode);
						break;
					case 2:
						((MultiChoice) view).setName(name);
						((MultiChoice) view).setTime(timeDescribe + shortStatu);
						((MultiChoice) view).setTimeColor(statuCode);
						break;
					case 3:
						((SingleChoice) view).setName(name);
						((SingleChoice) view).setTime(timeDescribe + shortStatu);
						((SingleChoice) view).setTimeColor(statuCode);
						break;
					case 4:
						((ImgRecordTask) view).setName(name);
						((ImgRecordTask) view).setTime(timeDescribe + shortStatu);
						((ImgRecordTask) view).setTimeColor(statuCode);
						break;
					case 5:
						((AudioRecordTask) view).setName(name);
						((AudioRecordTask) view).setTime(timeDescribe + shortStatu);
						((AudioRecordTask) view).setTimeColor(statuCode);
						break;
					}
					view.setVisibility(IsEnable ? View.VISIBLE : View.GONE);
				}
				break;
			case 1:
				switch (msg.arg1) {
				case DataService.Success:
					// update();
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
				Toast.makeText(ActivityTaskNFCCard.this, "网络不可用", Toast.LENGTH_SHORT).show();
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.card_task);
		SystemParamsUtil.getInstance().addActivity(this);
		initialize();
	}

	protected void onResume() {
		super.onResume();
		if (SystemParamsUtil.getInstance().getIsLogin()) {
			if (binder == null) {
				mBindService();
			}
			taskNFCCardReceiver = new TaskNFCCardReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction("com.env.view.PatrolTaskNFCCard.TaskNFCCardReceiver");
			registerReceiver(taskNFCCardReceiver, intentFilter);
		} else {
			startActivity(new Intent(ActivityTaskNFCCard.this, ActivityLogin.class));
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
//		if (!isChangingCard) {
//			sp = getSharedPreferences(PatrolApplication.PREFS_NAME, MODE_PRIVATE);
//			isChangingCard = true;
//			if (card != null) {
////				int cardType = NFCUtils.getCardType(intent, db);
//				curCardID = Integer.valueOf(card.get("CardID"));
//				if (cardType == -1) {
//					isChangingCard = false;
//					Toast.makeText(this, "未知卡片", Toast.LENGTH_SHORT).show();
//				} else if (cardType == NFCUtils.tagCardType) {
////					cardID = NFCUtils.getCardID(intent, db);
//					if (curCardID == cardID) {
//						if (mode == ActivityTaskConstruction.CARD_NORMAL) {
//							isChangingCard = true;
//							curCardID = cardID;
//							mode = ActivityTaskConstruction.CARD_NFC;
//							saveDate();
//							if (progressDialog == null) {
//								progressDialog = new ProgressDialog(ActivityTaskNFCCard.this);
//								progressDialog.setTitle("正在为您准备数据,请稍候");
//								progressDialog.setCancelable(false);
//							}
//							progressDialog.show();
//							setData();
//							new Handler().postDelayed(new Runnable() {
//								@Override
//								public void run() {
//									if (progressDialog != null) {
//										progressDialog.dismiss();
//									}
//								}
//							}, 1000);
//							cardName.setText(card.get("CardName"));
//							isChangingCard = false;
//							prepareForView();
//						} else {
//							Toast.makeText(ActivityTaskNFCCard.this, "当前巡检卡", Toast.LENGTH_SHORT).show();
//							isChangingCard = false;
//						}
//					} else {
//						cards = db.rawQuery(
//								"select  * from EP_NFCCard as a inner join EP_Construction as b on a.ConstructionID = b.ConstructionID where a.CardType = 0 and a.State = 1 and a.CardID = " + cardID + " and b.PlantID = "
//										+ sp.getInt(PatrolApplication.IDENTIFY_PLANT, 0), null);
//						cards.moveToFirst();
//						if (cards.getCount() < 1) {
//							Toast.makeText(this, "未知卡片", Toast.LENGTH_SHORT).show();
//							isChangingCard = false;
//						} else {
//							isChangingCard = true;
//							String cardNameStr = cards.getString(cards.getColumnIndex("CardName"));
//							new AlertDialog.Builder(ActivityTaskNFCCard.this).setTitle(cardNameStr).setMessage("检测到" + cardNameStr + ",\n确定跳转？").setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {
//								@Override
//								public void onClick(DialogInterface dialog, int which) {
//									curCardID = cardID;
//									mode = ActivityTaskConstruction.CARD_NFC;
//									saveDate();
//									if (progressDialog == null) {
//										progressDialog = new ProgressDialog(ActivityTaskNFCCard.this);
//										progressDialog.setTitle("正在为您准备数据,请稍候");
//										progressDialog.setCancelable(false);
//									}
//									progressDialog.show();
//									card = new HashMap<String, String>();
//									if (cards.isClosed() || cards == null) {
//										cards = db.rawQuery("select  * from EP_NFCCard as a inner join EP_Construction as b on a.ConstructionID = b.ConstructionID where a.CardType = 0 and a.State = 1 and a.CardID = " + cardID + " and b.PlantID = "
//												+ sp.getInt(PatrolApplication.IDENTIFY_PLANT, 0), null);
//										cards.moveToFirst();
//									}
//									for (int i = 0; i < cards.getColumnCount(); i++) {
//										card.put(cards.getColumnName(i), cards.getString(i));
//									}
//									cards.close();
//									setData();
//									new Handler().postDelayed(new Runnable() {
//										@Override
//										public void run() {
//											if (progressDialog != null) {
//												progressDialog.dismiss();
//											}
//										}
//									}, 1000);
//									cardName.setText(card.get("CardName"));
//									prepareForView();
//									isChangingCard = false;
//								}
//							}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
//								@Override
//								public void onClick(DialogInterface dialog, int which) {
//									dialog.dismiss();
//									isChangingCard = false;
//								}
//							}).create().show();
//							isChangingCard = true;
//						}
//						cards.close();
//					}
//				} else if (cardType == NFCUtils.userCardType) {
//					Toast.makeText(this, "该卡为用户卡，请刷巡检卡执行任务", Toast.LENGTH_SHORT).show();
//					isChangingCard = false;
//				}
//			} else {
//				isChangingCard = false;
//				Toast.makeText(this, "加载数据失败，请重试", Toast.LENGTH_SHORT).show();
//			}
//		} else {

//		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (taskNFCCardReceiver != null && SystemParamsUtil.getInstance().getIsLogin()) {
		}
	}

	@SuppressWarnings("unchecked")
	private void initialize() {
		getIntent = getIntent();
		sp = getSharedPreferences(PatrolApplication.PREFS_NAME, MODE_PRIVATE);
		editor = sp.edit();
		if (!SystemParamsUtil.getInstance().getIsLogin()) {
			new AlertDialog.Builder(ActivityTaskNFCCard.this).setTitle("检测到您未登录,将导航至登录窗口").setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(ActivityTaskNFCCard.this, ActivitySplash.class));
					ActivityTaskNFCCard.this.finish();
				}
			}).setCancelable(false).create().show();
		}
		mBindService();
		db = DataBaseUtil.getInstance(ActivityTaskNFCCard.this).getReadableDatabase();
		cardName = (TextView) findViewById(R.id.card_task_cardname);
		Cards2Tasks = (LinearLayout) findViewById(R.id.card_task_task);
		titleOptions = (TextView) findViewById(R.id.card_task_options);
		titleBack = (TextView) findViewById(R.id.card_task_back);

		titleOptions.setOnClickListener(this);
		titleBack.setOnClickListener(this);
		card = (HashMap<String, String>) getIntent.getSerializableExtra("Child");
		mode = getIntent.getExtras().getInt("Mode");
		if (card != null) {
			setData();
			cardName.setText(card.get("CardName"));
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
				}, 0, 1000);
			}
		} else {
			sp = getSharedPreferences(PatrolApplication.PREFS_NAME, MODE_PRIVATE);
//			cardID = NFCUtils.getCardID(getIntent(), db);
			curCardID = cardID;
			mode = ActivityTaskConstruction.CARD_NFC;
			cards = db.rawQuery(
					"select  * from EP_NFCCard as a inner join EP_Construction as b on a.ConstructionID = b.ConstructionID where a.CardType = 0 and a.State = 1 and a.CardID = " + cardID + " and b.PlantID = "
							+ sp.getInt(PatrolApplication.IDENTIFY_PLANT, 0), null);
			cards.moveToFirst();
			if (cards.getCount() < 1) {
				Toast.makeText(this, "未知卡片", Toast.LENGTH_SHORT).show();
				cards.close();
			} else {
				saveDate();
				if (progressDialog == null) {
					progressDialog = new ProgressDialog(ActivityTaskNFCCard.this);
					progressDialog.setTitle("正在为您准备数据,请稍候");
					progressDialog.setCancelable(false);
				}
				progressDialog.show();
				card = new HashMap<String, String>();
				cards.moveToFirst();
				if (cards != null && cards.getCount() > 0) {
					for (int i = 0; i < cards.getColumnCount(); i++) {
						card.put(cards.getColumnName(i), cards.getString(i));
					}
					cards.close();
					setData();
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							if (progressDialog != null) {
								progressDialog.dismiss();
							}
						}
					}, 2000);
					cardName.setText(card.get("CardName"));
					prepareForView();
				} else {
					Toast.makeText(this, "加载数据失败，请重试", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	private void setData() {

		SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
		Calendar nowClendar = Calendar.getInstance();
		long todayInt = Long.valueOf(sdf.format(nowClendar.getTime()));
		String nowDTStr = sdf.format(nowClendar.getTime());
		String startDateStr = nowDTStr.substring(0, 8);
		nowClendar.add(Calendar.DAY_OF_YEAR, 1);
		String endDateStr = sdf.format(nowClendar.getTime()).substring(0, 8);
		tasks = new ArrayList<HashMap<String, String>>();
		db = DataBaseUtil.getInstance(ActivityTaskNFCCard.this).getReadableDatabase();
		tasks = LocalDataHelper.getNewestTaskByCard(db, Integer.valueOf(card.get("CardID")), nowDTStr, startDateStr + "000000", endDateStr + "000000");

		if (tasks != null) {
			for (int i = 0; i < tasks.size(); i++) {
				tasks.get(i).put("Value", "");
				tasks.get(i).put("IsEnable", "1");
				tasks.get(i).put("Visible", "1");
				tasks.get(i).put("StatuCode", ViewUtil.VIEW_STATU_CODE_DOING + "");
				if (tasks.get(i).get("IsDone").equals("1")) {
					tasks.get(i).put("SampleTime", tasks.get(i).get("OPDateTime"));
					tasks.get(i).put("IsEnable", "0");
					String [] valueAndStep = LocalDataHelper.getTaskValuesAndSteps(db,tasks.get(i).get("ResultType") == "0" ? "EP_PatrolResult_Number" : "EP_PatrolResult_String", Integer.valueOf(tasks.get(i).get("TaskID")));
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
		String nowDateTimeStr, result, pre;
		int statuCode;
		for (int i = 0; i < tasks.size(); i++) {
			task = tasks.get(i);
			statuCode = Integer.valueOf(task.get("StatuCode"));
			SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
			nowDateTimeStr = sdf.format(Calendar.getInstance().getTime());
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
			task.put("TimeDescibe", ViewUtil.getInstance().getTimeDescribe(task.get("StartDateTime")));
			task.put("Statu", result + pre);
		}
	}

	private void prepareForView() {
		Cards2Tasks.removeAllViews();
		View view = null;
		for (int i = 0; i < tasks.size(); i++) {
			view = ViewUtil.getInstance().getView(db, tasks.get(i), ActivityTaskNFCCard.this, mode, 0);
			Cards2Tasks.addView(view);
		}
		calculateData();
	}

	private void saveDate() {
		View view;
		int ResultType;
		String result, opdateTime, valueTableName;
		HashMap<String, String> task;
		ContentValues cvTask, cvResult, cvValues;
		db = DataBaseUtil.getInstance(ActivityTaskNFCCard.this).getReadableDatabase();
		if (tasks != null) {
			db.beginTransaction();
			for (int i = 0; i < tasks.size(); i++) {
				task = tasks.get(i);
				if (task.get("IsEnable").equals("0")) {
					continue;
				}
				ResultType = Integer.valueOf(task.get("ResultType"));
				result = task.get("Value");
				opdateTime = task.get("SampleTime");
				valueTableName = "EP_PatrolResult_String";
				view = Cards2Tasks.getChildAt(i);
				switch (ResultType) {
				case 0:
					valueTableName = "EP_PatrolResult_Number";
					break;
				case 1:
					valueTableName = "EP_PatrolResult_String";
					break;
				case 2:
					result = ((MultiChoice) view).getResultStr();
					opdateTime = ((MultiChoice) view).getOPDateTime();
					valueTableName = "EP_PatrolResult_String";
					break;
				case 3:
					result = ((SingleChoice) view).getResult();
					opdateTime = ((SingleChoice) view).getOPDateTime();
					valueTableName = "EP_PatrolResult_String";
					break;
				case 4:
					valueTableName = "EP_PatrolResult_String";
					break;
				case 5:
					valueTableName = "EP_PatrolResult_String";
					break;
				}
				if (task.get("DataID") != null)
					db.delete(valueTableName, "DataID = " + task.get("DataID"), null);

				cvTask = new ContentValues();
				if (result.isEmpty()) {
					cvTask.put("IsDone", "0");
					cvTask.put("SampleTime", "");
					cvTask.put("DoneUserID", "");
					cvTask.put("HasRemind", "0");
					task.put("IsDone", "0");
					task.put("SampleTime", "");
					task.put("DoneUserID", "");
					if (task.get("DataID") != null)
						db.delete(valueTableName, "DataID = " + task.get("DataID"), null);
				} else {
					task.put("Value", result);
					task.put("SampleTime", opdateTime);
					cvResult = new ContentValues();
					cvResult.put("PlantID", Integer.valueOf(task.get("PlantID")));
					cvResult.put("TaskID", Integer.valueOf(task.get("TaskID")));
					cvResult.put("PatrolTagID", Integer.valueOf(task.get("PatrolTagID")));
					cvResult.put("OPDateTime", task.get("SampleTime"));
					if (task.get("DataID") == null) {
						db.insert("EP_PatrolResult", null, cvResult);
						task.put("DataID", DataCenterUtil.getDataIdByTask(db, Long.valueOf(task.get("TaskID"))) + "");
					} else {
						db.update("EP_PatrolResult", cvResult, "TaskID=" + task.get("TaskID"), null);
					}
					for (int j = 0; j < result.split("\n").length; j++) {
						cvValues = new ContentValues();
						cvValues.put("DataID", task.get("DataID") == null ? "" : task.get("DataID"));
						cvValues.put("DValue", result.split("\n")[j].trim());
						cvValues.put("OPDateTime", opdateTime);
						if (valueTableName.equals("EP_PatrolResult_Number")) {
							cvValues.put("UnitID", task.get("UnitID"));
						}
						if (valueTableName.equals("EP_PatrolResult_String")) {
							if (ResultType == 4 || ResultType == 5) {
								cvValues.put("IsUpload", "0");
							} else {
								cvValues.put("IsUpload", "1");
							}
						}
						db.insert(valueTableName, null, cvValues);
					}
					cvTask.put("HasRemind", "1");
					cvTask.put("IsDone", "1");
					cvTask.put("DoneUserID", SystemParamsUtil.getInstance().getLoginUser(sp).getUserID());
					cvTask.put("SampleTime", opdateTime);
					task.put("IsDone", "1");
					task.put("DoneUserID", SystemParamsUtil.getInstance().getLoginUser(sp).getUserID());
					task.put("SampleTime", opdateTime);
				}
				db.update("EP_PatrolTask", cvTask, "TaskID= ?", new String[] { task.get("TaskID") });
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			NotificationUtil.getInstance().updateStayNotification(ActivityTaskNFCCard.this);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			super.onActivityResult(requestCode, resultCode, data);
			HashMap<String, String> map = (HashMap<String, String>) data.getSerializableExtra("task");
			String valueString = map.get("Value");
			String TaskID = map.get("TaskID");
			String OPDateTime = map.get("SampleTime");
			for (int i = 0; i < tasks.size(); i++) {
				if (tasks.get(i).get("TaskID").equals(TaskID)) {
					tasks.get(i).put("Value", valueString);
					tasks.get(i).put("SampleTime", OPDateTime);
					switch (Integer.valueOf(map.get("ResultType"))) {
					case 0:
					case 1:
						((DataInput) Cards2Tasks.getChildAt(i)).setResult(valueString);
						break;
					case 4:
						((ImgRecordTask) Cards2Tasks.getChildAt(i)).setResult(valueString);
						break;
					case 5:
						((AudioRecordTask) Cards2Tasks.getChildAt(i)).setResult(valueString);
						break;
					}
				}
			}
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
		saveDate();
		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		// Intent intent = new Intent();
		// intent.setClass(this, PatrolTaskConstruction.class);
		// intent.putExtra("NeedTips", false);
		// startActivity(intent);
		// this.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(conn);
		if (taskNFCCardReceiver != null) {
			unregisterReceiver(taskNFCCardReceiver);
		}

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
		int apnType = SystemMethodUtil.getAPNType(ActivityTaskNFCCard.this);
		if (binder != null) {
			if (binder.isUploading) {
				Toast.makeText(getApplicationContext(), "正在上传任务，请不要重复操作", Toast.LENGTH_LONG).show();
			} else {
				switch (apnType) {
				case SystemMethodUtil.NoNetWork:
					DialogUtil.setApnDialog(ActivityTaskNFCCard.this);
					break;
				case SystemMethodUtil.MobileNetWork:
					sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
					editor = sp.edit();
					if (sp.getBoolean(PatrolApplication.HAS_DATAEXCHANGE_TIPS, true)) {
						DialogUtil.confirmNetWork(ActivityTaskNFCCard.this, new DialogInterface.OnClickListener() {
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

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.card_task_options) {
			showOptionsMenu();
		} else if (id == R.id.card_task_back) {
			onBackPressed();
		}
	}

	public void showTag() {
		Intent intent = new Intent(ActivityTaskNFCCard.this, ActivityTaskEachCard.class);
		intent.putExtra("Child", card);
		intent.putExtra("Mode", ActivityTaskConstruction.CARD_NORMAL);
		startActivityForResult(intent, 0);
	}

	private void showOptionsMenu() {
		if (optionsMenu == null) {
			optionsMenu = new PopupMenu(ActivityTaskNFCCard.this, titleOptions);
			optionsMenu.inflate(R.menu.patroltasknfccard_popupmenu);
			optionsMenu.setOnMenuItemClickListener(this);
		}
		optionsMenu.show();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
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
		}
		return true;
	}

	/**
	 * 数据更新或者上传后，1，保存数据，2，获取最新数据，3，刷新UI
	 */
	private void update() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		saveDate();
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
			}, 0, 1000);
		}

	}

	/**
	 * 使用2g/3G网络上传s
	 */
	public void uploadWithMobileNet() {
		saveDate();
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
			}, 0, 500);
		}

	}

	/**
	 * 使用wifi上传
	 */
	public void uploadWithWifi() {
		saveDate();
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
			}, 0, 500);
		}

	}
}