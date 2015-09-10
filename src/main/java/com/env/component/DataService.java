package com.env.component;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.env.activity.UpdateSystem;
import com.env.bean.EP_Application;
import com.env.bean.EP_PatrolTask;
import com.env.bean.EP_PatrolTaskPlan;
import com.env.bean.EP_RightData;
import com.env.bean.EP_UploadObject;
import com.env.bean.EnumList.AppRightState;
import com.env.bean.RequestResult;
import com.env.easypatrol.R;
import com.env.utils.DataBaseUtil;
import com.env.utils.DataCenterUtil;
import com.env.utils.HttpUtil;
import com.env.utils.LocalDataHelper;
import com.env.utils.NotificationUtil;
import com.env.utils.RemoteDataHelper;
import com.env.utils.SystemMethodUtil;
import com.env.utils.SystemParamsUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DataService extends Service {
	public static final int Operating = -1;
	public static final int Error = 1;
	public static final int Success = 0;
	public static final int NoNeedUpdate = 2;
	public static final int NoRight = 3;
	public static final int WifiDisabled = 4;
	public static final int SDCardIsNotReady = 6;
	private final int HasNewVersion = 3;
	private final int NotNewVersion = 2;
	private final int Start = -1;
	private final int Stop = 99;
	private final int NoTaskToUpload = 2;
	private final int UpdateTimerStart = 5;
	private final int UploadTimerStart = 6;
	private final int UploadLogStart = 12;
	private final int UploadLogEnd = 13;
	private final int UploadLogFaid = 14;
	private final int SuccessForData = 9;
	private final int SuccessForFile = 10;
	private Calendar nowCL;
	private Timer updateTimer = null, createTaskTimer = null, uploadTimer = null;
	private NotificationManager nfm;
	private Notification uploadNotification, getApkNotification;
	private RemoteViews viewUpload, viewGetApk;
	private int fileCount, fileProgress, textDataCount, textDataTotleCount, logFileCount;
	private long apkFileLength, downloadLength;
	private SQLiteDatabase mDatabase;
	private DataServiceHandler dataServiceHandler;
	private SharedPreferences sp = null;
	private SharedPreferences.Editor editor = null;
	private Thread threadUpdate, uploadLogFile, getApkFileThread, checkTaskCreate;
	private DataServiceBinder dataServiceBinder = new DataServiceBinder();
	private DecimalFormat tempDecimalFormat = new DecimalFormat("##0.00");

	public class DataServiceBinder extends Binder {
		public boolean isWifiOK = false;
		public boolean isUpdating = false;
		public boolean isUploading = false;
		public boolean isUploadLog = false;
		public boolean isExchangeDataSucss = true;
		public boolean isUpdatingSystem = false;

		public DataService getDataService() {
			return DataService.this;
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		if (dataServiceBinder == null) {
			dataServiceBinder = new DataServiceBinder();
		}
		return dataServiceBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		initialize();
		// startForeground(NotificationUtil.ForeGroundNotification,
		// null);
		if (updateTimer == null) {
			updateTimer = new Timer("updateTimer", true);
			updateTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (mDatabase == null) {
						mDatabase = DataBaseUtil.getInstance(DataService.this).getReadableDatabase();
					}
					sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
					int updateMode = sp.getInt(PatrolApplication.UPDATE_MODE, SystemMethodUtil.WifiNetWork);
					dataServiceBinder.isWifiOK = SystemMethodUtil.checkWifi(DataService.this);
					switch (updateMode) {
					case SystemMethodUtil.WifiNetWork:
						if (SystemMethodUtil.getAPNType(DataService.this) == SystemMethodUtil.WifiNetWork) {
							if (!dataServiceBinder.isUpdating) {
								Message updateMessage = dataServiceHandler.obtainMessage();
								updateMessage.what = UpdateTimerStart;
								updateMessage.sendToTarget();
							}
						} else {
							Message updateMessage = dataServiceHandler.obtainMessage();
							updateMessage.what = WifiDisabled;
							updateMessage.sendToTarget();
							String lctDateTime = sp.getString(PatrolApplication.LAST_CREATETASK_DATETIME, "");
							if (!lctDateTime.isEmpty()) {
								SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.ShortDateSdf);
								if (Long.valueOf(sdf.format(Calendar.getInstance().getTime())) > Long.valueOf(lctDateTime.substring(0, 8))) {
									if (!dataServiceBinder.isUpdating) {
										createPatrolTask(mDatabase);
										startTaskService();
									}
								}
							}
						}
						break;
					case SystemMethodUtil.MobileNetWork:
						if (SystemMethodUtil.getAPNType(DataService.this) != SystemMethodUtil.NoNetWork) {
							if (!dataServiceBinder.isUpdating) {
								Message updateMessage = dataServiceHandler.obtainMessage();
								updateMessage.what = UpdateTimerStart;
								updateMessage.sendToTarget();
							}
						}
						break;
					case SystemMethodUtil.NoNetWork:
						Message updateMessage = dataServiceHandler.obtainMessage();
						updateMessage.what = WifiDisabled;
						updateMessage.sendToTarget();
						String lctDateTime = sp.getString(PatrolApplication.LAST_CREATETASK_DATETIME, "");
						if (!lctDateTime.isEmpty()) {
							SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.ShortDateSdf);
							if (Long.valueOf(sdf.format(Calendar.getInstance().getTime())) > Long.valueOf(lctDateTime.substring(0, 8))) {
								if (!dataServiceBinder.isUpdating) {
									createPatrolTask(mDatabase);
									startTaskService();
								}
							}
							;
						}
						break;
					}
				}
			}, 0, 1000 * 7200);
		}
		if (createTaskTimer == null) {
			nowCL = Calendar.getInstance();
			nowCL.set(Calendar.HOUR_OF_DAY, 23);
			nowCL.set(Calendar.MINUTE, 59);
			nowCL.set(Calendar.SECOND, 59);
			nowCL.add(Calendar.SECOND, 2);
			createTaskTimer = new Timer("createTaskTimer", true);
			createTaskTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					new Thread(new Runnable() {
						@Override
						public void run() {
							if (mDatabase == null) {
								mDatabase = DataBaseUtil.getInstance(DataService.this).getReadableDatabase();
							}
							boolean ok = false;
							while (!ok) {
								if (!dataServiceBinder.isUpdating) {
									createPatrolTask(mDatabase);
									ok = true;
								}
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}).start();
				}
			}, nowCL.getTime(), 24 * 3600 * 1000);
		}
		if (uploadTimer == null) {
			uploadTimer = new Timer("uploadTimer", true);
			updateTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
					int updateMode = sp.getInt(PatrolApplication.UPDATE_MODE, SystemMethodUtil.WifiNetWork);
					dataServiceBinder.isWifiOK = SystemMethodUtil.checkWifi(DataService.this);
					switch (updateMode) {
					case SystemMethodUtil.WifiNetWork:
						if (SystemMethodUtil.getAPNType(DataService.this) == SystemMethodUtil.WifiNetWork) {
							if (mDatabase == null) {
								mDatabase = DataBaseUtil.getInstance(DataService.this).getReadableDatabase();
							}
							Message updateMessage = dataServiceHandler.obtainMessage();
							updateMessage.what = UploadTimerStart;
							updateMessage.sendToTarget();
						}
						break;
					case SystemMethodUtil.MobileNetWork:
						if (SystemMethodUtil.getAPNType(DataService.this) != SystemMethodUtil.NoNetWork) {
							if (mDatabase == null) {
								mDatabase = DataBaseUtil.getInstance(DataService.this).getReadableDatabase();
							}
							Message updateMessage = dataServiceHandler.obtainMessage();
							updateMessage.what = UploadTimerStart;
							updateMessage.sendToTarget();
						}
						break;
					case SystemMethodUtil.NoNetWork:

						break;
					}
				}
			}, 5 * 60 * 1000, 1 * 3600 * 1000);
		}
		String lctDateTime = sp.getString(PatrolApplication.LAST_CREATETASK_DATETIME, "");
		if (!lctDateTime.isEmpty()) {
			SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.ShortDateSdf);
			if (Long.valueOf(sdf.format(Calendar.getInstance().getTime())) > Long.valueOf(lctDateTime.substring(0, 8))) {
				if (!dataServiceBinder.isUpdating) {
					if (mDatabase == null) {
						mDatabase = DataBaseUtil.getInstance(DataService.this).getReadableDatabase();
					}
					checkTaskCreate = new Thread(new Runnable() {
						@Override
						public void run() {
							createPatrolTask(mDatabase);
							startTaskService();
						}
					});
					checkTaskCreate.start();

				}
			}
			;
		} else {
			if (!dataServiceBinder.isUpdating) {
				if (mDatabase == null) {
					mDatabase = DataBaseUtil.getInstance(DataService.this).getReadableDatabase();
				}
				checkTaskCreate = new Thread(new Runnable() {
					@Override
					public void run() {
						createPatrolTask(mDatabase);
						startTaskService();
					}
				});
				checkTaskCreate.start();
			}
		}
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (updateTimer != null) {
			updateTimer.cancel();
			updateTimer = null;
		}
		if (createTaskTimer != null) {
			createTaskTimer.cancel();
			createTaskTimer = null;
		}
		if (uploadTimer != null) {
			uploadTimer.cancel();
			uploadTimer = null;
		}
		stopForeground(true);
		// Intent service = new Intent();
		// service.setAction("com.env.component.DataService");
		// startService(service);
	}

	private void initialize() {
		sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
		editor = sp.edit();
		nfm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// serviceNotification = NotificationUtil.getInstance().getNotification(
		// NotificationUtil.ForeGroundNotification, this);
		dataServiceHandler = new DataServiceHandler();

	}

	/**
	 * 更新本地数据库
	 * 
	 * @param db
	 */
	public void upDateDB(SQLiteDatabase db) {
		mDatabase = db;
		dataServiceBinder.isUpdating = true;
		if (dataServiceHandler == null) {
			dataServiceHandler = new DataServiceHandler();
		}

		Message msgStart = dataServiceHandler.obtainMessage();
		msgStart.what = NotificationUtil.DOWNLOAD_DATA;
		msgStart.arg1 = Start;
		msgStart.sendToTarget();

		threadUpdate = new Thread(new Runnable() {
			@Override
			public void run() {
				Message msg = dataServiceHandler.obtainMessage();
				msg.what = NotificationUtil.DOWNLOAD_DATA;
				msg.arg1 = Stop;
				boolean planReady = false;
				try {
					sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
					editor = sp.edit();
					String needUpdate = RemoteDataHelper.getMobile(SystemMethodUtil.getMacAddress(DataService.this,sp,editor));
					if (needUpdate.equals("false")) {
						msg.arg2 = Error;
					} else if (needUpdate.equals("noright")) {
						msg.arg2 = NoRight;
					} else {
						Gson gson = new Gson();

						List<HashMap<String, String>> maps = gson.fromJson(needUpdate, new TypeToken<List<HashMap<String, String>>>() {
						}.getType());
						HashMap<String, String> map = maps.get(0);
						int state = Integer.parseInt(map.get("Audit"));
						int plantID;
						if (map.get("PlantID") == null || map.get("PlantID") == "") {
							plantID = 0;
						} else {
							plantID = Integer.parseInt(map.get("PlantID"));
						}
						editor.putInt(PatrolApplication.IDENTIFY, state);
						editor.putInt(PatrolApplication.IDENTIFY_PLANT, plantID);
						editor.commit();
					}
					if (sp.getInt(PatrolApplication.IDENTIFY, AppRightState.ClientApplication.getStae()) == AppRightState.AuditPass.getStae()) {
						int identifyPlant = sp.getInt(PatrolApplication.IDENTIFY_PLANT, 0);
						if (identifyPlant != 0) {
							RequestResult rs = RemoteDataHelper.getRightData(identifyPlant);
							if (rs.getErrorcode() == RequestResult.NO_ERROR) {
								Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
								EP_RightData dataJson = gson.fromJson(rs.getData(), EP_RightData.class);
								ArrayList<ContentValues> unitCVs = null, tagCVs = null, keyCVs = null, 
										valueCVs = null, cardCVs = null, plantCVs = null, tag2planCVs = null, 
										userCVs = null, chargesCVs = null,stepCVs = null, deviceCVS = null;

								userCVs = LocalDataHelper.updateUser(dataJson.EP_User);
								unitCVs = LocalDataHelper.updateUnit(dataJson.EP_Unit);
								tagCVs = LocalDataHelper.updateTag(dataJson.EP_PatrolTag);
								keyCVs = LocalDataHelper.updateKey(dataJson.EP_DicKey);
								valueCVs = LocalDataHelper.updateValue(dataJson.EP_DicValue);
								cardCVs = LocalDataHelper.updateNfcCard(dataJson.EP_NFCCard);
								tag2planCVs = LocalDataHelper.updateTag2Plan(dataJson.EP_PatrolTag2Plan);
								plantCVs = LocalDataHelper.updatePlant(dataJson.EP_PlantInfo);
								chargesCVs = LocalDataHelper.updateChargeOfPlant(dataJson.EP_ChargeOfPlant);
								stepCVs = LocalDataHelper.updateStep(dataJson.EP_DicStep);
								deviceCVS = LocalDataHelper.updateDevice(dataJson.EP_Device);

								// 开始更新数据库各张表的内容
								mDatabase.beginTransaction();
								// 删除旧的 User表
								mDatabase.delete("EP_User", null, null);
								for (int i = 0; i < userCVs.size(); i++) {
									mDatabase.insert("EP_User", null, userCVs.get(i));
								}
								// 删除旧的 Unit表
								mDatabase.delete("EP_Unit", null, null);
								for (int i = 0; i < unitCVs.size(); i++) {
									mDatabase.insert("EP_Unit", null, unitCVs.get(i));
								}
								// 删除旧的tag表
								mDatabase.delete("EP_PatrolTag", null, null);
								for (int i = 0; i < tagCVs.size(); i++) {
									mDatabase.insert("EP_PatrolTag", null, tagCVs.get(i));
								}
								// 删除旧的 key表
								mDatabase.delete("EP_DicKey", null, null);
								for (int i = 0; i < keyCVs.size(); i++) {
									mDatabase.insert("EP_DicKey", null, keyCVs.get(i));
								}

								// 删除旧的 value表
								mDatabase.delete("EP_DicValue", null, null);
								for (int i = 0; i < valueCVs.size(); i++) {
									mDatabase.insert("EP_DicValue", null, valueCVs.get(i));
								}

								// 删除旧的card表
								mDatabase.delete("EP_NFCCard", null, null);
								for (int i = 0; i < cardCVs.size(); i++) {
									mDatabase.insert("EP_NFCCard", null, cardCVs.get(i));
								}

								// 删除旧的 tag2plan表
								mDatabase.delete("EP_PatrolTag2Plan", null, null);
								for (int i = 0; i < tag2planCVs.size(); i++) {
									mDatabase.insert("EP_PatrolTag2Plan", null, tag2planCVs.get(i));
								}

								// 删除旧的 plant表
								mDatabase.delete("EP_PlantInfo", null, null);
								for (int i = 0; i < plantCVs.size(); i++) {
									mDatabase.insert("EP_PlantInfo", null, plantCVs.get(i));
								}

								// 更新ChargeOfPlant表
								mDatabase.delete("EP_ChargeOfPlant", null, null);
								for (int i = 0; i < chargesCVs.size(); i++) {
									mDatabase.insert("EP_ChargeOfPlant", null, chargesCVs.get(i));
								}

								//更新DicStep表
								mDatabase.delete("EP_DicStep", null, null);
								for (int i = 0; i < stepCVs.size(); i++) {
									mDatabase.insert("EP_DicStep", null, stepCVs.get(i));
								}

								//更新Device表
								mDatabase.delete("EP_Device", null, null);
								for (int i = 0; i < deviceCVS.size(); i++) {
									mDatabase.insert("EP_Device", null, deviceCVS.get(i));
								}

								mDatabase.setTransactionSuccessful();
								mDatabase.endTransaction();

								if (dataJson.EP_Plan != null) {
									planReady = LocalDataHelper.updateTaskPlan(dataJson.EP_Plan, mDatabase);
									if (planReady)
										msg.arg2 = createPatrolTask(mDatabase);
									else
										msg.arg2 = Success;
								} else
									msg.arg2 = Error;
							}
						} else
							msg.arg2 = NoRight;
					} else
						msg.arg2 = NoRight;
				} catch (Exception e) {
					msg.arg2 = Error;
					e.printStackTrace();
				} finally {
					msg.sendToTarget();
				}
			}
		});
		threadUpdate.start();
	}

	

	

	/**
	 * 根据该厂的计划和巡检想，来生成一天所有的任务
	 * 
	 * @param db
	 * @return 任务生成是否成功
	 */
	public int createPatrolTask(SQLiteDatabase db) {
		int code = Success;
		if (sp == null) {
			sp = getSharedPreferences(PatrolApplication.APN_PREFER, Context.MODE_PRIVATE);
		}
		mDatabase = db;
		dataServiceBinder.isExchangeDataSucss = false;
		dataServiceBinder.isUpdating = true;
		Calendar todayCalendar = Calendar.getInstance();
		Calendar startCalendar = Calendar.getInstance();
		Calendar stopCalendar = Calendar.getInstance();
		Calendar tempCalendar = Calendar.getInstance();
		Calendar realDTCalendar = Calendar.getInstance();
		Calendar lastCreateDate = Calendar.getInstance();
		String todayDateTime = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(todayCalendar.getTime());
		StringBuilder sql = null;
		String tempStr = "";
		Date date1 = null, date2 = null;
		ArrayList<ContentValues> plans = new ArrayList<ContentValues>();
		ContentValues plan = null;
		try {
			DataCenterUtil.clearInvalidTask(db);
			Cursor patrolTagCS = DataCenterUtil.getAllPatrolTag(db);
			patrolTagCS.moveToFirst();
			if (patrolTagCS.getCount() < 1) {
				DataCenterUtil.clearAllTask(db);
				return code;
			}
			int PatrolTagID, PlanID;
			long mileSecond = 0, tempMileSecond = 0;
			EP_PatrolTaskPlan ep_Plan = new EP_PatrolTaskPlan();
			ArrayList<ContentValues> tasks = new ArrayList<ContentValues>();
			ContentValues task = null;
			Cursor planIDCS, planCS;
			// 循环每个 巡检项
			while (!patrolTagCS.isAfterLast()) {
				PatrolTagID = patrolTagCS.getInt(patrolTagCS.getColumnIndex("PatrolTagID"));
				planIDCS = DataCenterUtil.getPlanIdByTag(db, PatrolTagID);
				planIDCS.moveToFirst();

				// 如果 该巡检项下没有 巡检计划 则删除之前该巡检项下的任务
				if (planIDCS != null && planIDCS.getCount() < 1) {
					DataCenterUtil.clearTaskByTagId(db, PatrolTagID);
					patrolTagCS.moveToNext();
					continue;
				}

				while (!planIDCS.isAfterLast()) {

					PlanID = planIDCS.getInt(0);
					planCS = mDatabase.rawQuery("select * from EP_PatrolTaskPlan where PlanID = " + PlanID, null);
					planCS.moveToFirst();
					ep_Plan.setState(planCS.getInt(planCS.getColumnIndex("State")));
					if (ep_Plan.getState() == 0) {
						// 删除 停用计划的 巡检任务
						sql = new StringBuilder();
						sql.append("IsUpload = 'false' and IsDone = 'false'");
						sql.append(" and PlanID = " + PlanID);
						mDatabase.delete("EP_PatrolTask", sql.toString(), null);
						planIDCS.moveToNext();
						continue;
					}

					plan = new ContentValues();
					ep_Plan.setPlanID(patrolTagCS.getInt(patrolTagCS.getColumnIndex("PlantID")));
					ep_Plan.setPlanType(planCS.getInt(planCS.getColumnIndex("PlanType")));
					ep_Plan.setPlanID(planCS.getInt(planCS.getColumnIndex("PlanID")));
					ep_Plan.setIsRemind(planCS.getInt(planCS.getColumnIndex("IsRemind")) == 1 ? true : false);
					ep_Plan.setIsInUse(planCS.getInt(planCS.getColumnIndex("IsInUse")) == 1 ? true : false);
					ep_Plan.setRemindSpanUnit(planCS.getInt(planCS.getColumnIndex("RemindSpanUnit")));
					ep_Plan.setRemindSpan(planCS.getInt(planCS.getColumnIndex("RemindSpan")));
					ep_Plan.setTimeout(planCS.getInt(planCS.getColumnIndex("Timeout")));
					ep_Plan.setTimeoutUnit(planCS.getInt(planCS.getColumnIndex("TimeoutUnit")));
					ep_Plan.setDurationStopDateIsEndless(planCS.getInt(planCS.getColumnIndex("DurationStopDateIsEndless")) == 1 ? true : false);
					ep_Plan.setVersionID(planCS.getInt(planCS.getColumnIndex("CurVersionID")));
					ep_Plan.setLastVersionID(planCS.getInt(planCS.getColumnIndex("LastVersionID")));

					ep_Plan.setTaskCloseIsInUse(planCS.getInt(planCS.getColumnIndex("TaskCloseIsInUse")) == 1 ? true : false);
					ep_Plan.setTaskCloseSpanUnit(planCS.getInt(planCS.getColumnIndex("TaskCloseSpanUnit")));
					ep_Plan.setTaskCloseSpan(planCS.getInt(planCS.getColumnIndex("TaskCloseSpan")));
					plan.put("PlanID", ep_Plan.getPlanID());

					switch (ep_Plan.getPlanType()) {
					case 0:// 执行一次
						if (ep_Plan.getLastVersionID() < ep_Plan.getVersionID() ) {
							task = new ContentValues();

							ep_Plan.setExecuteOneTimeTheDateTime(planCS.getString(planCS.getColumnIndex("ExecuteOneTimeTheDateTime")));
							task.put("PlantID", patrolTagCS.getInt(patrolTagCS.getColumnIndex("PlantID")));
							task.put("DeviceID", patrolTagCS.getInt(patrolTagCS.getColumnIndex("DeviceID")));
							task.put(EP_PatrolTask.CN_PatrolTagID, PatrolTagID);
							task.put(EP_PatrolTask.CN_DoneUserID, "");
							task.put(EP_PatrolTask.CN_UploadDateTime, "");
							task.put(EP_PatrolTask.CN_IsDone, false);
							task.put(EP_PatrolTask.CN_IsUpload, false);
							task.put(EP_PatrolTask.CN_StartDateTime, ep_Plan.getExecuteOneTimeTheDateTime());
							task.put(EP_PatrolTask.CN_SampleTime, "");
							task.put(EP_PatrolTask.CN_PlanID, PlanID);
							task.put(EP_PatrolTask.CN_IsRemind, ep_Plan.getIsRemind());
							task.put(EP_PatrolTask.CN_HasRemind, !ep_Plan.getIsRemind());
							task.put(EP_PatrolTask.CN_EnableDelay, ep_Plan.getIsRemind());

							date1 = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).parse(ep_Plan.getExecuteOneTimeTheDateTime());
							mileSecond = date1.getTime();
							tempMileSecond = mileSecond;
							if (ep_Plan.getTaskCloseIsInUse()) {
								switch (ep_Plan.getTaskCloseSpanUnit()) {
								case 0:// 分钟
									tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 60 * 1000;
									break;
								case 1:// 小时
									tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 3600 * 1000;
									break;
								case 2:// 天
									tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 3600 * 1000 * 24;
									break;
								}
								date1 = new Date(tempMileSecond);
								task.put(EP_PatrolTask.CN_EndDateTime, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(date1));
							} else {
								task.put(EP_PatrolTask.CN_EndDateTime, SystemMethodUtil.EndlessDate);
							}
							if (ep_Plan.getIsInUse()) {
								switch (ep_Plan.getTimeoutUnit()) {
								case 0:// 分钟
									mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 60 * 1000;
									break;
								case 1:// 小时
									mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 3600 * 1000;
									break;
								case 2:// 天
									mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 3600 * 1000 * 24;
									break;
								}
								date1 = new Date(mileSecond);
								task.put(EP_PatrolTask.CN_StopDateTime, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(date1));
							} else {
								task.put(EP_PatrolTask.CN_StopDateTime, task.getAsString(EP_PatrolTask.CN_EndDateTime));
							}
							tasks.add(task);
							plan.put("LastVersionID", ep_Plan.getVersionID());
						}
						break;
					case 1:// 循环执行
						if (ep_Plan.getLastVersionID() < ep_Plan.getVersionID()) {
							ep_Plan.setDurationStartDate(planCS.getString(planCS.getColumnIndex("DurationStartDate")));
						} else {
							ep_Plan.setDurationStartDate(SystemMethodUtil.StrToCreateTaskStartDate(planCS.getString(planCS.getColumnIndex("CreateTaskStartDate")), todayDateTime));
							if (Long.valueOf(todayDateTime.substring(0, 8)) < Long.valueOf(ep_Plan.getDurationStartDate())) {
								break;
							}
						}
						ep_Plan.setDurationStopDate(planCS.getString(planCS.getColumnIndex("DurationStopDate")));
						ep_Plan.setEveryDayFrequencyStartTime(planCS.getString(planCS.getColumnIndex("EveryDayFrequencyStartTime")));
						ep_Plan.setEveryDayFrequencyStopTime(planCS.getString(planCS.getColumnIndex("EveryDayFrequencyStopTime")));
						ep_Plan.setRepeatFrequencyType(planCS.getInt(planCS.getColumnIndex("RepeatFrequencyType")));
						ep_Plan.setEveryDayFrequencyType(planCS.getInt(planCS.getColumnIndex("EveryDayFrequencyType")));
						ep_Plan.setExeOnceAtTime(planCS.getString(planCS.getColumnIndex("ExeOnceAtTime")));
						ep_Plan.setExeSpan(planCS.getInt(planCS.getColumnIndex("ExeSpan")));
						ep_Plan.setExeSpanType(planCS.getInt(planCS.getColumnIndex("ExeSpanType")));
						if (!ep_Plan.getDurationStopDateIsEndless()) {
							if (Long.valueOf(todayDateTime.substring(0, 8)) > Long.valueOf(ep_Plan.getDurationStopDate())) {
								break;
							}
						}
						if (Long.valueOf(todayDateTime.substring(0, 8)) >= Long.valueOf(ep_Plan.getDurationStartDate())) {
							ep_Plan.setDurationStartDate(todayDateTime.substring(0, 8));
						}
						switch (ep_Plan.getRepeatFrequencyType()) {
						case 0:// 按天循环
							ep_Plan.setRepeatFrequencyDay(planCS.getInt(planCS.getColumnIndex("RepeatFrequencyDay")));
							if (ep_Plan.getDurationStopDateIsEndless()) {
								tempCalendar.setTime(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).parse(ep_Plan.getDurationStartDate()));
								tempCalendar.add(Calendar.DAY_OF_YEAR, ep_Plan.getRepeatFrequencyDay());
								ep_Plan.setDurationStopDate(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(tempCalendar.getTime()));
							} else {
								tempCalendar.setTime(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).parse(ep_Plan.getDurationStartDate()));
								tempCalendar.add(Calendar.DAY_OF_YEAR, ep_Plan.getRepeatFrequencyDay());
								if (Long.valueOf(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(tempCalendar.getTime())) <= Long.valueOf(ep_Plan.getDurationStopDate())) {
									ep_Plan.setDurationStopDate(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(tempCalendar.getTime()));
								}
							}
							startCalendar.setTime(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).parse(ep_Plan.getDurationStartDate()));
							stopCalendar.setTime(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).parse(ep_Plan.getDurationStopDate()));
							lastCreateDate.setTime(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).parse(ep_Plan.getDurationStopDate()));

							while (Long.valueOf(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(startCalendar.getTime())) < Long.valueOf(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(stopCalendar.getTime()))) {
								switch (ep_Plan.getEveryDayFrequencyType()) {
								case 0:// 执行一次
									task = new ContentValues();
									task.put("PlantID", patrolTagCS.getInt(patrolTagCS.getColumnIndex("PlantID")));
									task.put("DeviceID", patrolTagCS.getInt(patrolTagCS.getColumnIndex("DeviceID")));
									task.put(EP_PatrolTask.CN_PatrolTagID, PatrolTagID);
									task.put(EP_PatrolTask.CN_DoneUserID, "");
									task.put(EP_PatrolTask.CN_UploadDateTime, "");
									task.put(EP_PatrolTask.CN_IsDone, false);
									task.put(EP_PatrolTask.CN_IsUpload, false);
									tempStr = new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(startCalendar.getTime()) + ep_Plan.getExeOnceAtTime();
									task.put(EP_PatrolTask.CN_StartDateTime, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(new SimpleDateFormat(SystemMethodUtil.ShortDateTimeSdf).parse(tempStr)));
									task.put(EP_PatrolTask.CN_SampleTime, "");
									task.put(EP_PatrolTask.CN_PlanID, PlanID);
									task.put(EP_PatrolTask.CN_IsRemind, ep_Plan.getIsRemind());
									task.put(EP_PatrolTask.CN_HasRemind, !ep_Plan.getIsRemind());
									task.put(EP_PatrolTask.CN_EnableDelay, ep_Plan.getIsInUse());
									date1 = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).parse(task.getAsString(EP_PatrolTask.CN_StartDateTime));
									mileSecond = date1.getTime();
									tempMileSecond = mileSecond;

									if (ep_Plan.getTaskCloseIsInUse()) {
										switch (ep_Plan.getTaskCloseSpanUnit()) {
										case 0:// 分钟
											tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 60 * 1000;
											break;
										case 1:// 小时
											tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 3600 * 1000;
											break;
										case 2:// 天
											tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 3600 * 1000 * 24;
											break;
										}
									} else {
										tempMileSecond = tempMileSecond + ep_Plan.getRepeatFrequencyDay() * 24 * 3600 * 1000;
									}
									date2 = new Date(tempMileSecond);
									task.put(EP_PatrolTask.CN_EndDateTime, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(date2));

									if (ep_Plan.getIsInUse()) {
										switch (ep_Plan.getTimeoutUnit()) {
										case 0:// 分钟
											mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 60 * 1000;
											break;
										case 1:// 小时
											mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 3600 * 1000;
											break;
										case 2:// 天
											mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 3600 * 1000 * 24;
											break;
										}
										date1 = new Date(mileSecond);
										task.put(EP_PatrolTask.CN_StopDateTime, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(date1));
									} else {
										task.put(EP_PatrolTask.CN_StopDateTime, task.getAsString(EP_PatrolTask.CN_EndDateTime));
									}
									tasks.add(task);
									break;
								case 1:// 执行多次
									tempCalendar.setTime(new SimpleDateFormat(SystemMethodUtil.ShortDateTimeSdf).parse(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(startCalendar.getTime()) + ep_Plan.getEveryDayFrequencyStopTime()));
									startCalendar.setTime(new SimpleDateFormat(SystemMethodUtil.ShortDateTimeSdf).parse(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(startCalendar.getTime()) + ep_Plan.getEveryDayFrequencyStartTime()));
									realDTCalendar = SystemMethodUtil.getRealStartTime(todayCalendar, startCalendar, tempCalendar, ep_Plan.getExeSpanType(), ep_Plan.getExeSpan());
									while (realDTCalendar.getTime().getTime() < tempCalendar.getTime().getTime()) {
										task = new ContentValues();
										task.put("PlantID", patrolTagCS.getInt(patrolTagCS.getColumnIndex("PlantID")));
										task.put("DeviceID", patrolTagCS.getInt(patrolTagCS.getColumnIndex("DeviceID")));
										task.put(EP_PatrolTask.CN_PatrolTagID, PatrolTagID);
										task.put(EP_PatrolTask.CN_DoneUserID, "");
										task.put(EP_PatrolTask.CN_UploadDateTime, "");
										task.put(EP_PatrolTask.CN_IsDone, false);
										task.put(EP_PatrolTask.CN_IsUpload, false);
										task.put(EP_PatrolTask.CN_StartDateTime, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(realDTCalendar.getTime()));
										task.put(EP_PatrolTask.CN_SampleTime, "");
										task.put(EP_PatrolTask.CN_PlanID, PlanID);
										task.put(EP_PatrolTask.CN_IsRemind, ep_Plan.getIsRemind());
										task.put(EP_PatrolTask.CN_HasRemind, !ep_Plan.getIsRemind());
										task.put(EP_PatrolTask.CN_EnableDelay, ep_Plan.getIsInUse());

										date1 = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).parse(task.getAsString(EP_PatrolTask.CN_StartDateTime));
										tempMileSecond = date1.getTime();
										mileSecond = tempMileSecond;

										if (ep_Plan.getTaskCloseIsInUse()) {
											switch (ep_Plan.getTaskCloseSpanUnit()) {
											case 0:// 分钟
												tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 60 * 1000;
												break;
											case 1:// 小时
												tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 3600 * 1000;
												break;
											case 2:// 天
												tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 3600 * 1000 * 24;
												break;
											}
										} else {
											switch (ep_Plan.getExeSpanType()) {
											case 0:
												tempMileSecond = tempMileSecond + (long) ep_Plan.getExeSpan() * 60 * 1000;
												break;
											case 1:
												tempMileSecond = tempMileSecond + (long) ep_Plan.getExeSpan() * 3600 * 1000;
												break;
											}
										}
										date1 = new Date(tempMileSecond);
										task.put(EP_PatrolTask.CN_EndDateTime, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(date1));

										if (ep_Plan.getIsInUse()) {
											switch (ep_Plan.getTimeoutUnit()) {
											case 0:// 分钟
												mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 60 * 1000;
												break;
											case 1:// 小时
												mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 3600 * 1000;
												break;
											case 2:// 天
												mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 3600 * 1000 * 24;
												break;
											}
											date2 = new Date(mileSecond);
											task.put(EP_PatrolTask.CN_StopDateTime, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(date2));
										} else {
											task.put(EP_PatrolTask.CN_StopDateTime, task.getAsString(EP_PatrolTask.CN_EndDateTime));
										}
										tasks.add(task);
										switch (ep_Plan.getExeSpanType()) {
										case 0:
											realDTCalendar.add(Calendar.MINUTE, ep_Plan.getExeSpan());
											break;
										case 1:
											realDTCalendar.add(Calendar.HOUR_OF_DAY, ep_Plan.getExeSpan());
											break;
										}
									}
									break;
								}
								startCalendar.add(Calendar.DAY_OF_YEAR, ep_Plan.getRepeatFrequencyDay());
								plan.put("CreateTaskStartDate", new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(lastCreateDate.getTime()));
								plan.put("LastVersionID", ep_Plan.getVersionID());
							}
							break;
						case 1:// 按周循环
							ep_Plan.setSpanWeek(planCS.getInt(planCS.getColumnIndex("SpanWeek")));
							if (ep_Plan.getDurationStopDateIsEndless()) {
								tempCalendar.setTime(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).parse(ep_Plan.getDurationStartDate()));
								tempCalendar.add(Calendar.DAY_OF_YEAR, ep_Plan.getSpanWeek() * 7);
								ep_Plan.setDurationStopDate(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(tempCalendar.getTime()));
							} else {
								tempCalendar.setTime(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).parse(ep_Plan.getDurationStartDate()));
								tempCalendar.add(Calendar.DAY_OF_YEAR, ep_Plan.getSpanWeek() * 7);
								if (Long.valueOf(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(tempCalendar.getTime())) <= Long.valueOf(ep_Plan.getDurationStopDate())) {
									ep_Plan.setDurationStopDate(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(tempCalendar.getTime()));
								}
							}
							ArrayList<Integer> WeekAt = new ArrayList<Integer>();
							for (int i = 0; i < 7; i++) {
								WeekAt.add(planCS.getInt(planCS.getColumnIndex("WeekAt" + (i + 1))));
							}

							startCalendar.setTime(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).parse(ep_Plan.getDurationStartDate()));
							stopCalendar.setTime(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).parse(ep_Plan.getDurationStopDate()));
							lastCreateDate.setTime(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).parse(ep_Plan.getDurationStopDate()));

							while (Long.valueOf(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(startCalendar.getTime())) < Long.valueOf(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(stopCalendar.getTime()))) {
								for (int i = 0; i < 7; i++) {
									int startAtWeek = startCalendar.get(Calendar.DAY_OF_WEEK) - 1;// 得到今天是星期几
									if (startAtWeek == 0) {
										startAtWeek = 7;
									}
									if (WeekAt.get(startAtWeek - 1) == 1) {
										// tempCalendar.setTime(startCalendar.getTime());
										// tempCalendar.add(Calendar.DAY_OF_YEAR,
										// ep_Plan.SpanWeek*7);
										switch (ep_Plan.getEveryDayFrequencyType()) {
										case 0:// 执行一次
											task = new ContentValues();
											task.put("PlantID", patrolTagCS.getInt(patrolTagCS.getColumnIndex("PlantID")));
											task.put("DeviceID", patrolTagCS.getInt(patrolTagCS.getColumnIndex("DeviceID")));
											task.put(EP_PatrolTask.CN_PatrolTagID, PatrolTagID);
											task.put(EP_PatrolTask.CN_DoneUserID, "");
											task.put(EP_PatrolTask.CN_UploadDateTime, "");
											task.put(EP_PatrolTask.CN_IsDone, false);
											task.put(EP_PatrolTask.CN_IsUpload, false);
											tempStr = new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(startCalendar.getTime()) + ep_Plan.getExeOnceAtTime();
											task.put(EP_PatrolTask.CN_StartDateTime, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(new SimpleDateFormat(SystemMethodUtil.ShortDateTimeSdf).parse(tempStr)));
											task.put(EP_PatrolTask.CN_SampleTime, "");
											task.put(EP_PatrolTask.CN_PlanID, PlanID);
											task.put(EP_PatrolTask.CN_IsRemind, ep_Plan.getIsRemind());
											task.put(EP_PatrolTask.CN_HasRemind, !ep_Plan.getIsRemind());
											task.put(EP_PatrolTask.CN_EnableDelay, ep_Plan.getIsInUse());

											date1 = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).parse(task.getAsString(EP_PatrolTask.CN_StartDateTime));
											mileSecond = date1.getTime();
											tempMileSecond = mileSecond;

											if (ep_Plan.getTaskCloseIsInUse()) {
												switch (ep_Plan.getTaskCloseSpanUnit()) {
												case 0:// 分钟
													tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 60 * 1000;
													break;
												case 1:// 小时
													tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 3600 * 1000;
													break;
												case 2:// 天
													tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 3600 * 1000 * 24;
													break;
												}
											} else {
												tempMileSecond = tempMileSecond + (long) ep_Plan.getSpanWeek() * 7 * 24 * 3600 * 1000;
											}
											date2 = new Date(tempMileSecond);
											task.put(EP_PatrolTask.CN_EndDateTime, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(date2));

											if (ep_Plan.getIsInUse()) {
												switch (ep_Plan.getTimeoutUnit()) {
												case 0:// 分钟
													mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 60 * 1000;
													break;
												case 1:// 小时
													mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 3600 * 1000;
													break;
												case 2:// 天
													mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 3600 * 1000 * 24;
													break;
												}
												date1 = new Date(mileSecond);
												task.put(EP_PatrolTask.CN_StopDateTime, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(date1));
											} else {
												task.put(EP_PatrolTask.CN_StopDateTime, task.getAsString(EP_PatrolTask.CN_EndDateTime));
											}
											tasks.add(task);
											break;
										case 1:
											tempCalendar.setTime(new SimpleDateFormat(SystemMethodUtil.ShortDateTimeSdf).parse(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(startCalendar.getTime())
													+ ep_Plan.getEveryDayFrequencyStopTime()));
											startCalendar.setTime(new SimpleDateFormat(SystemMethodUtil.ShortDateTimeSdf).parse(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(startCalendar.getTime())
													+ ep_Plan.getEveryDayFrequencyStartTime()));
											realDTCalendar = SystemMethodUtil.getRealStartTime(todayCalendar, startCalendar, tempCalendar, ep_Plan.getExeSpanType(), ep_Plan.getExeSpan());
											while (realDTCalendar.getTime().getTime() < tempCalendar.getTime().getTime()) {
												task = new ContentValues();
												task.put("PlantID", patrolTagCS.getInt(patrolTagCS.getColumnIndex("PlantID")));
												task.put("DeviceID", patrolTagCS.getInt(patrolTagCS.getColumnIndex("DeviceID")));
												task.put(EP_PatrolTask.CN_PatrolTagID, PatrolTagID);
												task.put(EP_PatrolTask.CN_DoneUserID, "");
												task.put(EP_PatrolTask.CN_UploadDateTime, "");
												task.put(EP_PatrolTask.CN_IsDone, false);
												task.put(EP_PatrolTask.CN_IsUpload, false);
												task.put(EP_PatrolTask.CN_StartDateTime, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(realDTCalendar.getTime()));
												task.put(EP_PatrolTask.CN_SampleTime, "");
												task.put(EP_PatrolTask.CN_PlanID, PlanID);
												task.put(EP_PatrolTask.CN_IsRemind, ep_Plan.getIsRemind());
												task.put(EP_PatrolTask.CN_HasRemind, !ep_Plan.getIsRemind());
												task.put(EP_PatrolTask.CN_EnableDelay, ep_Plan.getIsInUse());

												date1 = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).parse(task.getAsString(EP_PatrolTask.CN_StartDateTime));
												tempMileSecond = date1.getTime();
												mileSecond = tempMileSecond;

												if (ep_Plan.getTaskCloseIsInUse()) {
													switch (ep_Plan.getTaskCloseSpanUnit()) {
													case 0:// 分钟
														tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 60 * 1000;
														break;
													case 1:// 小时
														tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 3600 * 1000;
														break;
													case 2:// 天
														tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 3600 * 1000 * 24;
														break;
													}
												} else {
													switch (ep_Plan.getExeSpanType()) {
													case 0:
														tempMileSecond = tempMileSecond + (long) ep_Plan.getExeSpan() * 60 * 1000;
														break;
													case 1:
														tempMileSecond = tempMileSecond + (long) ep_Plan.getExeSpan() * 3600 * 1000;
														break;
													}
												}
												date1 = new Date(tempMileSecond);
												task.put(EP_PatrolTask.CN_EndDateTime, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(date1));
												if (ep_Plan.getIsInUse()) {
													switch (ep_Plan.getTimeoutUnit()) {
													case 0:// 分钟
														mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 60 * 1000;
														break;
													case 1:// 小时
														mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 3600 * 1000;
														break;
													case 2:// 天
														mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 3600 * 1000 * 24;
														break;
													}
													date2 = new Date(mileSecond);
													task.put(EP_PatrolTask.CN_StopDateTime, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(date2));
												} else {
													task.put(EP_PatrolTask.CN_StopDateTime, task.getAsString(EP_PatrolTask.CN_EndDateTime));
												}
												tasks.add(task);
												switch (ep_Plan.getExeSpanType()) {
												case 0:
													realDTCalendar.add(Calendar.MINUTE, ep_Plan.getExeSpan());
													break;
												case 1:
													realDTCalendar.add(Calendar.HOUR_OF_DAY, ep_Plan.getExeSpan());
													break;
												}
											}
											break;
										}
									}
									startCalendar.add(Calendar.DAY_OF_YEAR, 1);
								}
								startCalendar.add(Calendar.DAY_OF_YEAR, (ep_Plan.getSpanWeek() - 1) * 7);
								plan.put("CreateTaskStartDate", new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(lastCreateDate.getTime()));
								plan.put("LastVersionID", ep_Plan.getVersionID());
							}
							break;
						case 2:// 按月循环
							ep_Plan.setMonthAtDay(planCS.getInt(planCS.getColumnIndex("MonthAtDay")));
							ep_Plan.setSpanMonth(planCS.getInt(planCS.getColumnIndex("SpanMonth")));
							startCalendar.setTime(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).parse(ep_Plan.getDurationStartDate()));
							startCalendar.set(Calendar.DAY_OF_MONTH, ep_Plan.getMonthAtDay());

							if (ep_Plan.getDurationStopDateIsEndless()) {
								tempCalendar.setTime(startCalendar.getTime());
								tempCalendar.add(Calendar.MONTH, ep_Plan.getSpanMonth());
								ep_Plan.setDurationStopDate(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(tempCalendar.getTime()));
							} else {
								tempCalendar.setTime(startCalendar.getTime());
								tempCalendar.add(Calendar.MONTH, ep_Plan.getSpanMonth());
								if (Long.valueOf(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(tempCalendar.getTime())) <= Long.valueOf(ep_Plan.getDurationStopDate())) {
									ep_Plan.setDurationStopDate(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(tempCalendar.getTime()));
								}
							}
							stopCalendar.setTime(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).parse(ep_Plan.getDurationStopDate()));
							lastCreateDate.setTime(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).parse(ep_Plan.getDurationStopDate()));
							while (Long.valueOf(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(startCalendar.getTime())) < Long.valueOf(new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(stopCalendar.getTime()))) {
								switch (ep_Plan.getEveryDayFrequencyType()) {
								case 0:// 执行一次
									task = new ContentValues();
									task.put("PlantID", patrolTagCS.getInt(patrolTagCS.getColumnIndex("PlantID")));
									task.put("DeviceID", patrolTagCS.getInt(patrolTagCS.getColumnIndex("DeviceID")));
									task.put(EP_PatrolTask.CN_PatrolTagID, PatrolTagID);
									task.put(EP_PatrolTask.CN_DoneUserID, "");
									task.put(EP_PatrolTask.CN_UploadDateTime, "");
									task.put(EP_PatrolTask.CN_IsDone, false);
									task.put(EP_PatrolTask.CN_IsUpload, false);
									tempStr = new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(startCalendar.getTime()) + ep_Plan.getExeOnceAtTime();
									task.put(EP_PatrolTask.CN_StartDateTime, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(new SimpleDateFormat(SystemMethodUtil.ShortDateTimeSdf).parse(tempStr)));
									task.put(EP_PatrolTask.CN_SampleTime, "");
									task.put(EP_PatrolTask.CN_PlanID, PlanID);
									task.put(EP_PatrolTask.CN_IsRemind, ep_Plan.getIsRemind());
									task.put(EP_PatrolTask.CN_HasRemind, !ep_Plan.getIsRemind());
									task.put(EP_PatrolTask.CN_EnableDelay, ep_Plan.getIsInUse());

									date1 = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).parse(task.getAsString(EP_PatrolTask.CN_StartDateTime));
									mileSecond = date1.getTime();
									tempMileSecond = mileSecond;

									if (ep_Plan.getTaskCloseIsInUse()) {
										switch (ep_Plan.getTaskCloseSpanUnit()) {
										case 0:// 分钟
											tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 60 * 1000;
											break;
										case 1:// 小时
											tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 3600 * 1000;
											break;
										case 2:// 天
											tempMileSecond = tempMileSecond + (long) ep_Plan.getTaskCloseSpan() * 3600 * 1000 * 24;
											break;
										}
									} else {
										tempMileSecond = tempMileSecond + (long) ep_Plan.getSpanMonth() * 30 * 24 * 3600 * 1000;
									}
									date2 = new Date(tempMileSecond);
									task.put(EP_PatrolTask.CN_EndDateTime, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(date2));

									if (ep_Plan.getIsInUse()) {
										switch (ep_Plan.getTimeoutUnit()) {
										case 0:// 分钟
											mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 60 * 1000;
											break;
										case 1:// 小时
											mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 3600 * 1000;
											break;
										case 2:// 天
											mileSecond = mileSecond + (long) ep_Plan.getTimeout() * 24 * 3600 * 1000;
											break;
										}
										date1 = new Date(mileSecond);
										task.put(EP_PatrolTask.CN_StopDateTime, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(date1));
									} else {
										task.put(EP_PatrolTask.CN_StopDateTime, task.getAsString(EP_PatrolTask.CN_EndDateTime));
									}
									tasks.add(task);
									break;
								/*
								 * case 1://执行多次 SimpleDateFormat sdf10 = new
								 * SimpleDateFormat
								 * (CommonMethod.ShortDateTimeSdf);
								 * SimpleDateFormat sdf11 = new
								 * SimpleDateFormat(CommonMethod.ShortDateSdf);
								 * tempCalendar
								 * .setTime(sdf10.parse(sdf11.format(
								 * startCalendar
								 * .getTime())+ep_Plan.EveryDayFrequencyStopTime
								 * ));
								 * startCalendar.setTime(sdf10.parse(sdf11.format
								 * (startCalendar.getTime())+ep_Plan.
								 * EveryDayFrequencyStartTime)); realDTCalendar
								 * =
								 * CommonMethod.getRealStartTime(todayCalendar,
								 * startCalendar, tempCalendar,
								 * ep_Plan.ExeSpanType, ep_Plan.ExeSpan); while
								 * (
								 * realDTCalendar.getTime().getTime()<tempCalendar
								 * .getTime().getTime()) { task = new
								 * ContentValues();
								 * task.put(EP_PatrolTask.CN_PlantID,
								 * ep_Plan.PlantID);
								 * task.put(EP_PatrolTask.CN_PatrolTagID,
								 * PatrolTagID);
								 * task.put(EP_PatrolTask.CN_DoneUserID, "");
								 * task.put(EP_PatrolTask.CN_UploadDateTime,
								 * ""); task.put(EP_PatrolTask.CN_IsDone,
								 * "false"); task.put(EP_PatrolTask.CN_IsUpload,
								 * "false"); SimpleDateFormat sdf12 = new
								 * SimpleDateFormat
								 * (CommonMethod.LongDateTimeSdf);
								 * task.put(EP_PatrolTask
								 * .CN_StartDateTime,sdf12.
								 * format(realDTCalendar.getTime()));
								 * task.put(EP_PatrolTask.CN_SampleTime, "");
								 * task.put(EP_PatrolTask.CN_PlanID, PlanID);
								 * task.put(EP_PatrolTask.CN_GroupID,
								 * ep_Plan.GroupID);
								 * task.put(EP_PatrolTask.CN_IsRemind,
								 * ep_Plan.IsRemind?"true":"false");
								 * task.put(EP_PatrolTask.CN_HasRemind,
								 * ep_Plan.IsRemind?"false":"true");
								 * task.put(EP_PatrolTask.CN_EnableDelay,
								 * ep_Plan.IsInUse?"true":"false"); try {
								 * SimpleDateFormat sdf13 = new
								 * SimpleDateFormat(
								 * CommonMethod.LongDateTimeSdf); date1 =
								 * sdf13.parse
								 * (task.getAsString(EP_PatrolTask.CN_StartDateTime
								 * )); tempMileSecond = date1.getTime(); date2 =
								 * sdf13.parse(task.getAsString(EP_PatrolTask.
								 * CN_StartDateTime)); mileSecond =
								 * date2.getTime(); } catch (ParseException e) {
								 * e.printStackTrace(); } switch
								 * (ep_Plan.ExeSpanType) { case 0:
								 * tempMileSecond = tempMileSecond +
								 * ep_Plan.ExeSpan*60*1000; break; case 1:
								 * tempMileSecond = tempMileSecond +
								 * ep_Plan.ExeSpan*3600*1000; break; } date1 =
								 * new Date(tempMileSecond);
								 * if(ep_Plan.IsInUse){ switch
								 * (ep_Plan.TimeoutUnit) { case 0:// 分钟
								 * mileSecond = mileSecond +
								 * 60*ep_Plan.Timeout*1000; break; case 1://小时
								 * mileSecond = mileSecond +
								 * 3600*ep_Plan.Timeout*1000; break; case 2:// 天
								 * mileSecond = mileSecond +
								 * 3600*ep_Plan.Timeout*1000*24; break; } date2
								 * = new Date(mileSecond); SimpleDateFormat
								 * sdf13 = new
								 * SimpleDateFormat(CommonMethod.LongDateTimeSdf
								 * );
								 * task.put(EP_PatrolTask.CN_StopDateTime,sdf13
								 * .format(date2)); }else { SimpleDateFormat
								 * sdf13 = new
								 * SimpleDateFormat(CommonMethod.LongDateTimeSdf
								 * );
								 * task.put(EP_PatrolTask.CN_StopDateTime,sdf13
								 * .format(date1)); } SimpleDateFormat sdf13 =
								 * new
								 * SimpleDateFormat(CommonMethod.LongDateTimeSdf
								 * );
								 * task.put(EP_PatrolTask.CN_EndDateTime,sdf13
								 * .format(date1)); tasks.add(task);
								 * realDTCalendar.setTime(new
								 * Date(tempMileSecond)); } break;
								 */
								}
								startCalendar.add(Calendar.MONTH, ep_Plan.getSpanMonth());
								plan.put("CreateTaskStartDate", new SimpleDateFormat(SystemMethodUtil.ShortDateSdf).format(lastCreateDate.getTime()));
								plan.put("LastVersionID", ep_Plan.getVersionID());
								break;
							}
						}
						break;
					case 2:// 不定期执行

						break;

					}
					plans.add(plan);
					planIDCS.moveToNext();
					planCS.close();
				}
				planIDCS.close();
				patrolTagCS.moveToNext();
			}
			patrolTagCS.close();

			mDatabase.beginTransaction();

			Cursor deleteDiffPlan = mDatabase.rawQuery("select PlanID from EP_PatrolTaskPlan where CurVersionID > LastVersionID", null);
			deleteDiffPlan.moveToFirst();
			if (deleteDiffPlan.getCount() > 0) {
				while (!deleteDiffPlan.isAfterLast()) {
					sql = new StringBuilder();
					sql.append("(StartDateTime>= " + todayDateTime);
					sql.append(" or (StartDateTime <=" + todayDateTime);
					sql.append(" and EndDateTime >= " + todayDateTime);
					sql.append(" and IsUpload = 0 and IsDone = 0))");
					sql.append(" and PlanID = " + deleteDiffPlan.getInt(0));
					mDatabase.delete("EP_PatrolTask", sql.toString(), null);
					deleteDiffPlan.moveToNext();
				}
			}
			deleteDiffPlan.close();
			for (int i = 0; i < tasks.size(); i++) {
				mDatabase.insert("EP_PatrolTask", null, tasks.get(i));
			}
			for (int i = 0; i < plans.size(); i++) {
				mDatabase.update("EP_PatrolTaskPlan", plans.get(i), "PlanID = " + plans.get(i).getAsInteger("PlanID"), null);
			}
			mDatabase.setTransactionSuccessful();
			mDatabase.endTransaction();
			editor.putString(PatrolApplication.LAST_CREATETASK_DATETIME, todayDateTime);
			editor.commit();
			dataServiceBinder.isExchangeDataSucss = true;
		} catch (ParseException e) {

			dataServiceBinder.isExchangeDataSucss = false;
			code = Error;
		} finally {
			dataServiceBinder.isUpdating = false;
		}

		return code;
	}

	/**
	 * 在无wifi情况，无论使用3G还是2G网络，都只能传输文本类型的数据
	 * 
	 * @param db
	 */
	public void uploadOnlyTextData(final SQLiteDatabase db) {
		dataServiceBinder.isUploading = true;
		dataServiceBinder.isExchangeDataSucss = false;
		if (dataServiceHandler == null) {
			dataServiceHandler = new DataServiceHandler();
		}
		textDataCount = 0;
		textDataTotleCount = 0;


		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {

				Calendar calendar = Calendar.getInstance();
				String nowDT = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(calendar.getTime());
				String standardDateTime = new SimpleDateFormat(SystemMethodUtil.StandardDateTimeSdf).format(calendar.getTime());

				textDataCount = 0;
				List<HashMap<String,String>> tasks = LocalDataHelper.getTaskToUpload(db,nowDT);
				if(tasks==null) tasks = new ArrayList<HashMap<String, String>>();
				textDataTotleCount = tasks.size();
				EP_UploadObject uploadObject = new EP_UploadObject();
				List<HashMap<String,Object>> uploadTasks = new ArrayList<HashMap<String, Object>>();

				//ResultType,DataID
				uploadObject.setPostDataDateTime(standardDateTime);
				uploadObject.setUserID(SystemParamsUtil.getInstance().getLoginUser(sp).getUserID());

				for (HashMap<String,String> task : tasks){
					HashMap<String,Object> tmp = new HashMap<String,Object>();

					tmp.put("DValueList_Binary", new ArrayList<HashMap<String,String>>());
					tmp.put("DValueList_Number", new ArrayList<HashMap<String,String>>());
					tmp.put("DValueList_String", new ArrayList<HashMap<String,String>>());

					int resultType = Integer.valueOf(task.get("ResultType"));

					tmp.put("TaskID", task.get("TaskID"));
					tmp.put("PlanID", task.get("PlanID"));
					tmp.put("DeviceID", task.get("DeviceID"));
					tmp.put("PlantID", task.get("PlantID"));
					tmp.put("PatrolTagID", task.get("PatrolTagID"));
					tmp.put("DoneUserID", task.get("DoneUserID"));
					tmp.put("Mark",task.get("Text"));
					tmp.put("IsDone", task.get("IsDone").equals("1") ? true : false);
					tmp.put("SampleTime", SystemMethodUtil.toJsonDateByStr(task.get("SampleTime")));
					tmp.put("StartDateTime", SystemMethodUtil.toJsonDateByStr(task.get("StartDateTime")));
					tmp.put("StopDateTime", SystemMethodUtil.toJsonDateByStr(task.get("EndDateTime")));

					if (task.get("EndDateTime").equals(task.get("StopDateTime"))) tmp.put("ValidDateTime", SystemMethodUtil.EndlessJsonDate);
					else tmp.put("ValidDateTime", SystemMethodUtil.toJsonDateByStr(task.get("StopDateTime")));

					if (task.get("IsDone").equals("1")){
						textDataCount++;
						if(task.get("DataID")!=null && task.get("DataID").length()>0){
							if (resultType == 0) tmp.put("DValueList_Number", LocalDataHelper.getUploadDataValue(db,task.get("DataID"),resultType,task.get("DicID")));
							else tmp.put("DValueList_String", LocalDataHelper.getUploadDataValue(db,task.get("DataID"),resultType,task.get("DicID")));
						}
					}
					uploadTasks.add(tmp);
				}
				uploadObject.setTaskList(uploadTasks);
				uploadObject.setPostDataCount(textDataTotleCount);





				if (textDataTotleCount < 1) {
					Message msg = dataServiceHandler.obtainMessage();
					msg.what = NotificationUtil.UPLOAD_TEXT_DATA;
					msg.arg1 = NoTaskToUpload;
					msg.sendToTarget();
				} else {
					Message msg = dataServiceHandler.obtainMessage();
					msg.what = NotificationUtil.UPLOAD_TEXT_DATA;
					msg.arg1 = Start;
					msg.arg2 = textDataCount;
					msg.sendToTarget();


					MultipartEntity entity = new MultipartEntity();
					String result;
					try {
						entity.addPart("postDataJsonStr", new StringBody(uploadObject.toString(),Charset.forName("UTF-8")));
						result  = RemoteDataHelper.uploadTaskOnlyText(entity);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						result = "false";
					}


					if (result.toLowerCase().contains("true")) {
						db.beginTransaction();
						ContentValues cv = new ContentValues();
						cv.put("IsUpload", "1");
						cv.put("UploadDateTime", nowDT);
						for (int i = 0; i < tasks.size(); i++) {
							db.update("EP_PatrolTask", cv, "TaskID = ?", new String[]{tasks.get(i).get("TaskID")});
						}
						db.setTransactionSuccessful();
						db.endTransaction();
						msg = dataServiceHandler.obtainMessage();
						msg.what = NotificationUtil.UPLOAD_TEXT_DATA;
						msg.arg1 = SuccessForData;
						msg.arg2 = textDataCount;
						msg.sendToTarget();
					} else {
						msg = dataServiceHandler.obtainMessage();
						msg.what = NotificationUtil.UPLOAD_TEXT_DATA;
						msg.arg1 = Error;
						msg.sendToTarget();
					}
					msg = dataServiceHandler.obtainMessage();
					msg.what = NotificationUtil.UPLOAD_TEXT_DATA;
					msg.arg1 = Stop;
					msg.arg2 = 0;
					msg.sendToTarget();

				}
			}
		});
		t1.start();
	}

	/**
	 * 有wifi情况下，将文本的数据以及多媒体文件传到服务器
	 * 
	 * @param db
	 */
	public void uploadDataJsonObject(final SQLiteDatabase db) {
		uploadOnlyTextData(db);

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				List<HashMap<String,String>> files = LocalDataHelper.getTaskMediaToUpload(db, new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(new Date()));
				fileCount = files.size();
				if(fileCount > 0 ){
					uploadNotification = NotificationUtil.getInstance().getNotification(NotificationUtil.UPLOAD_FILE_DATA,DataService.this);
					viewUpload = uploadNotification.contentView;
					Message msg = dataServiceHandler.obtainMessage();
					msg.what = NotificationUtil.UPLOAD_FILE_DATA;
					msg.arg1 = Start;
					msg.sendToTarget();

					fileProgress = 0;

					for(int i =0;i<files.size();i++){
						MultipartEntity mpe = new MultipartEntity();
						try {
							mpe.addPart("patrolTagId",new StringBody(files.get(i).get("PatrolTagID")));
							mpe.addPart("planId",new StringBody(files.get(i).get("PlanID")));
							mpe.addPart("startTime",new StringBody(SystemMethodUtil.toJsonDateByStr(files.get(i).get("StartDateTime"))));
							File file = new File(files.get(i).get("FilePath"));
							if(!file.exists()) continue;
							mpe.addPart("file", new FileBody(file));
							String result = RemoteDataHelper.uploadTaskMedia(mpe);
							Log.v("file upload","***************"+result);
							if(result.toLowerCase().contains("true")){
								fileProgress ++ ;
								ContentValues contentValues = new ContentValues();
								contentValues.put("IsUpload", 1);
								db.update("EP_PatrolResult_Media", contentValues,"TaskID=? and FilePath = ?", new String[]{files.get(i).get("TaskID"),files.get(i).get("FilePath")});
								msg = dataServiceHandler.obtainMessage();
								msg.what = NotificationUtil.UPLOAD_FILE_DATA;
								msg.arg1 = SuccessForFile;
								msg.sendToTarget();
							}
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						} catch (Exception e){
							e.printStackTrace();
						}
					}
					msg = dataServiceHandler.obtainMessage();
					msg.what = NotificationUtil.UPLOAD_FILE_DATA;
					msg.arg1 = Stop;
					msg.sendToTarget();
				}

			}
		});
		thread.start();
	}

	/**
	 * 每次获取新任务后，需要start taskservice，校正任务提醒的时间
	 */
	public void startTaskService() {
		Intent intent = new Intent();
		intent.setAction("com.env.component.TaskService");
		startService(intent);
	}

	/**
	 * 获取最新的版本号
	 */
	public void getVersion() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				dataServiceBinder.isUpdatingSystem = true;
				if (dataServiceHandler == null) {
					dataServiceHandler = new DataServiceHandler();
				}
				sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
				editor = sp.edit();
				RequestResult rs = RemoteDataHelper.getNewestApkInfo();
				Message msg = dataServiceHandler.obtainMessage();
				msg.what = NotificationUtil.GET_REMOTE_VERCODE;
				if(rs.getErrorcode()==RequestResult.NO_ERROR){
					Gson gson = new Gson();
					EP_Application app = gson.fromJson(rs.getData(), EP_Application.class);
					if (SystemMethodUtil.getVersionCode(DataService.this) >= app.getVerCode()) {
						msg.arg1 = NotNewVersion;
					} else {
						editor.putInt(PatrolApplication.LATEST_VERSION_CODE, app.getVerCode());
						editor.putString(PatrolApplication.LATEST_VERSION_NAME, app.getVerName());
						editor.putString(PatrolApplication.LATEST_VERSION_INFO, app.getVerInfo());
						editor.commit();
						msg.arg1 = HasNewVersion;
					}
				}else{
					msg.arg1 = Error;
				}
				dataServiceBinder.isUpdatingSystem = false;
				msg.sendToTarget();
			}
		});
		thread.start();
	}



	/**
	 * 获取更新文件apk
	 */
	public void getApkFile() {
		if (dataServiceHandler == null) {
			dataServiceHandler = new DataServiceHandler();
		}
		dataServiceBinder.isUpdatingSystem = true;

		getApkNotification = NotificationUtil.getInstance().getNotification(NotificationUtil.DOWNLOAD_APK, DataService.this);
		getApkNotification.flags = Notification.FLAG_ONGOING_EVENT;
		viewGetApk = NotificationUtil.getInstance().getViews(DataService.this);
		viewGetApk.setTextViewText(R.id.data_exchange_notification_title, "下载文件中");
		getApkNotification.contentView = viewGetApk;

		getApkFileThread = new Thread(new Runnable() {
			@Override
			public void run() {
				dataServiceBinder.isUpdatingSystem = true;
				if (SystemMethodUtil.isSDCardReady()) {
					Message msg = dataServiceHandler.obtainMessage();
					msg.what = NotificationUtil.DOWNLOAD_APK;
					msg.arg1 = Start;
					msg.sendToTarget();
					sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
					editor = sp.edit();
					String target = HttpUtil.URL_Datacenter + "GetNewestApp";
					String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EnvEasyPatrol/Download";
					File folder = new File(path);
					if (!folder.exists()) {
						folder.mkdirs();
					}
					String filePath;
					filePath = path + File.separator + "EnvEasyPatrol" + sp.getInt(PatrolApplication.LATEST_VERSION_CODE, 0) + ".apk";
					File file = new File(filePath);
					if (file.exists()) {
						editor.putString(PatrolApplication.LATEST_VERSION_FILENAME, filePath);
						editor.commit();
						Message msgStop = dataServiceHandler.obtainMessage();
						msgStop.what = NotificationUtil.DOWNLOAD_APK;
						apkFileLength = 1;
						downloadLength = 1;
						msgStop.arg1 = Stop;
						msgStop.sendToTarget();
					} else {
						URL url = null;
						HttpURLConnection conn = null;
						InputStream is = null;
						apkFileLength = 0;
						downloadLength = 0;
						try {
							byte[] temp = new byte[102400];
							url = new URL(target);
							conn = (HttpURLConnection) url.openConnection();
							conn.setConnectTimeout(60 * 1000);
							conn.setReadTimeout(300 * 1000);
							conn.setRequestMethod("GET");
							conn.setRequestProperty(HTTP.CONTENT_TYPE, "application/vnd.android");
							conn.setDoInput(true);
							conn.setUseCaches(false);
							conn.setInstanceFollowRedirects(false);
							is = conn.getInputStream();
							apkFileLength = conn.getContentLength();
							int pos = 0;
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							while ((pos = is.read(temp)) != -1) {
								Message msgProgress = dataServiceHandler.obtainMessage();
								msgProgress.what = NotificationUtil.DOWNLOAD_APK;
								msgProgress.arg1 = SuccessForFile;
								downloadLength += pos;
								baos.write(temp, 0, pos);
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								msgProgress.sendToTarget();
							}
							OutputStream os = new FileOutputStream(file);
							baos.writeTo(os);
							os.flush();
							os.close();
							editor.putString(PatrolApplication.LATEST_VERSION_FILENAME, filePath);
							editor.commit();
							baos.flush();
							baos.close();
							Message msgStop = dataServiceHandler.obtainMessage();
							msgStop.what = NotificationUtil.DOWNLOAD_APK;
							msgStop.arg1 = Stop;
							msgStop.sendToTarget();
						} catch (MalformedURLException e) {
							Message msgError = dataServiceHandler.obtainMessage();
							msgError.what = NotificationUtil.DOWNLOAD_APK;
							msgError.arg1 = Error;
							msgError.sendToTarget();
						} catch (IOException e) {
							Message msgError = dataServiceHandler.obtainMessage();
							msgError.what = NotificationUtil.DOWNLOAD_APK;
							msgError.arg1 = Error;
							msgError.sendToTarget();
						} finally {
							if (is != null) {
								try {
									is.close();
									conn.disconnect();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				} else {
					Message msg = dataServiceHandler.obtainMessage();
					msg.what = NotificationUtil.DOWNLOAD_APK;
					msg.arg1 = WifiDisabled;
					msg.sendToTarget();
				}
			}
		});
		getApkFileThread.start();
	}



	/**
	 * 上传错误日志文件去服务器
	 */
	public void uploadLogFile() {
		if (dataServiceBinder.isUploadLog) {
			Toast.makeText(DataService.this, "正在上传", Toast.LENGTH_SHORT).show();
		} else {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && SystemMethodUtil.getAPNType(DataService.this) != SystemMethodUtil.NoNetWork) {
				uploadLogFile = new Thread(new Runnable() {
					@Override
					public void run() {
						logFileCount = 0;
						dataServiceBinder.isUploadLog = true;
						dataServiceHandler.sendEmptyMessage(UploadLogStart);
						String deviceID = SystemMethodUtil.getMacAddress(DataService.this,sp,editor);
						String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EnvEasyPatrol/Log";
						File dirFile = new File(dirPath);
						File[] files = dirFile.listFiles();

						if (files != null) {
							for (int i = 0; i < files.length; i++) {
								MultipartEntity me = new MultipartEntity();
								try {
									me.addPart("ClientID", new StringBody(deviceID));
									me.addPart("file", new FileBody(files[i]));
									String result = RemoteDataHelper.uploadLogFiles(me);
									if (result.equals("true")) {
										if (files[i].exists()) {
											files[i].delete();
										}
										logFileCount++;
									}
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
							}
						}
						dataServiceHandler.sendEmptyMessage(UploadLogEnd);
						dataServiceBinder.isUploadLog = false;
					}
				});
				uploadLogFile.start();
			} else {
				Toast.makeText(DataService.this, "请检查网络和SD卡", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 记录最近一次更新数据的时间
	 * */
	public void recordUpdateTime() {
		if (sp == null) {
			sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
		}
		if (editor == null) {
			editor = sp.edit();
		}
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		editor.putString(PatrolApplication.LAST_UPDATE_DATETIME, df.format(Calendar.getInstance().getTime()));
		editor.apply();
	}

	/**
	 * 记录用户申请
	 */
	public void recordClientInfo() {
		try {
			String result = RemoteDataHelper.getMobile(SystemMethodUtil.getMacAddress(DataService.this,sp,editor));
			if (result.equals("false")) {
			} else if (result.equals("noright")) {
				editor.putInt(PatrolApplication.IDENTIFY, 0);
				editor.putInt(PatrolApplication.IDENTIFY_PLANT, 0);
			} else {
				JSONArray jsonArray = new JSONArray(result);
				JSONObject jsonObject = jsonArray.getJSONObject(0);
				int state = jsonObject.getInt("Audit");
				String desciption = jsonObject.getString("DeviceDescription") == null ? "" : jsonObject.getString("DeviceDescription");
				String auditinfo = jsonObject.getString("AuditInformation") == null ? "" : jsonObject.getString("AuditInformation");
				String name = jsonObject.getString("Name") == null ? "" : jsonObject.getString("Name");
				String company = jsonObject.getString("WorkUnit") == null ? "" : jsonObject.getString("WorkUnit");
				String phone = jsonObject.getString("Tel1") == null ? "" : jsonObject.getString("Tel1");
				switch (state) {
				case 1:// 正在审核
					editor.putInt(PatrolApplication.IDENTIFY_PLANT, 0);
					break;
				case 2:// 审核通过
					int plant = 0;
					if (jsonObject.get("PlantID") == null) {
						plant = 0;
					} else {
						plant = jsonObject.getInt("PlantID");
					}
					editor.putInt(PatrolApplication.IDENTIFY_PLANT, plant);
					break;
				case 3:// 审核未通过
					editor.putInt(PatrolApplication.IDENTIFY_PLANT, 0);
					break;
				}
				editor.putString(PatrolApplication.IDENTIFY_DESCRIPTION, desciption);
				editor.putString(PatrolApplication.IDENTIFY_AUDITINFO, auditinfo);
				editor.putString(PatrolApplication.IDENTIFY_NAME, name);
				editor.putString(PatrolApplication.IDENTIFY_COMPANY, company);
				editor.putString(PatrolApplication.IDENTIFY_PHONE, phone);
				editor.putInt(PatrolApplication.IDENTIFY, state);
			}
			editor.commit();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 记录最近一次上传数据的时间
	 */
	public void recordUploadTime() {
		if (sp == null) {
			sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
		}
		if (editor == null) {
			editor = sp.edit();
		}
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		editor.putString(PatrolApplication.LAST_UPLOAD_DATETIME, df.format(Calendar.getInstance().getTime()));
		editor.apply();
	}

	private class DataServiceHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UploadLogStart:
				Toast.makeText(DataService.this, "开始上传", Toast.LENGTH_SHORT).show();
				break;
			case UploadLogEnd:
				if (logFileCount > 0) {
					Toast.makeText(DataService.this, "本次上传完毕，共上传" + logFileCount + "个日志", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(DataService.this, "日志文件已全部上传", Toast.LENGTH_SHORT).show();
				}
				break;
			case UploadLogFaid:
				Toast.makeText(DataService.this, "日志文件上传上传失败", Toast.LENGTH_SHORT).show();
				break;
			case NotificationUtil.DOWNLOAD_DATA:
				switch (msg.arg1) {
				case Start:
					Toast.makeText(getApplicationContext(), "正在准备数据，请稍候", Toast.LENGTH_LONG).show();
					break;
				case Stop:
					switch (msg.arg2) {
					case Success:
						// 开始通知服务端，客户端数据已经更新完毕
						// threadNoticeServer.start();
						Toast.makeText(getApplicationContext(), "最新数据已经准备好", Toast.LENGTH_SHORT).show();
						dataServiceBinder.isExchangeDataSucss = true;
						startTaskService();
						SystemMethodUtil.sendBroadCast(DataService.this);
						recordUpdateTime();
						NotificationUtil.getInstance().updateStayNotification(DataService.this);
						break;
					case Error:
						Toast.makeText(getApplicationContext(), "数据更新失败", Toast.LENGTH_SHORT).show();
						dataServiceBinder.isExchangeDataSucss = false;
						break;
					case NoNeedUpdate:
						Toast.makeText(getApplicationContext(), "已是最新数据", Toast.LENGTH_SHORT).show();
						dataServiceBinder.isExchangeDataSucss = false;
						break;
					case NoRight:
						Toast.makeText(getApplicationContext(), "您的设备未经授权，请与我们联系", Toast.LENGTH_SHORT).show();
						dataServiceBinder.isExchangeDataSucss = false;
						break;
					}
					dataServiceBinder.isUpdating = false;
					break;
				}
				break;
			case NotificationUtil.UPLOAD_FILE_DATA:
				if (msg.arg1 == Start) {
					uploadNotification.tickerText = "共有" + fileCount + "个文件需要上传";
					viewUpload.setTextViewText(R.id.data_exchange_notification_title, "正在上传");
				} else if (msg.arg1 == Stop) {
					uploadNotification.tickerText = "成功上传" + fileProgress + "个文件";
					viewUpload.setTextViewText(R.id.data_exchange_notification_title, "共有" + fileCount + "个文件,上传成功" + fileProgress + "个");
					uploadNotification.flags = Notification.FLAG_AUTO_CANCEL;
					Toast.makeText(DataService.this, "上传完毕", Toast.LENGTH_SHORT).show();
				} else if (msg.arg1 == Error) {
					Toast.makeText(DataService.this, "上传失败", Toast.LENGTH_SHORT).show();
				}
				viewUpload.setProgressBar(R.id.data_exchange_notification_progressbar, 100, 100 * fileProgress / fileCount, false);
				viewUpload.setTextViewText(R.id.data_exchange_notification_progress, tempDecimalFormat.format(100 * fileProgress / fileCount) + "%");
				uploadNotification.contentView = viewUpload;
				nfm.notify(NotificationUtil.UPLOAD_FILE_DATA, uploadNotification);
				break;
			case UpdateTimerStart:
				upDateDB(mDatabase);
				break;
			case UploadTimerStart:
				Toast.makeText(DataService.this, "自动 上传开始", Toast.LENGTH_SHORT).show();
				uploadDataJsonObject(mDatabase);
				break;
			case WifiDisabled:
				Toast.makeText(DataService.this, "网络不可用", Toast.LENGTH_SHORT).show();
				break;
			case NotificationUtil.UPLOAD_TEXT_DATA:
				switch (msg.arg1) {
				case NoTaskToUpload:
					Toast.makeText(DataService.this, "没有可上传的数据", Toast.LENGTH_SHORT).show();
					dataServiceBinder.isExchangeDataSucss = true;
					dataServiceBinder.isUploading = false;
					break;
				case Error:
					Toast.makeText(DataService.this, "上传失败", Toast.LENGTH_SHORT).show();
					dataServiceBinder.isExchangeDataSucss = false;
					dataServiceBinder.isUploading = false;
					break;
				case Start:
					if (msg.arg2 == 0) {
						Toast.makeText(DataService.this, "没有可上传的数据", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(DataService.this, "本次将上传" + msg.arg2 + "条数据", Toast.LENGTH_SHORT).show();
					}
					break;
				case SuccessForData:
					if (msg.arg2 > 0) {
						Toast.makeText(DataService.this, "成功上传" + msg.arg2 + "条数据", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(DataService.this, "上传完毕", Toast.LENGTH_SHORT).show();
					}
					dataServiceBinder.isExchangeDataSucss = true;
					dataServiceBinder.isUploading = false;
					SystemMethodUtil.sendBroadCast(DataService.this);
					break;
				}
				break;
			case NotificationUtil.GET_REMOTE_VERCODE:
				switch (msg.arg1) {
				case Error:
					Toast.makeText(getApplicationContext(), "检测失败，请重试", Toast.LENGTH_SHORT).show();
					break;
				case HasNewVersion:
					Intent intent = new Intent(DataService.this, UpdateSystem.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					break;
				case NotNewVersion:
					Toast.makeText(getApplicationContext(), "已经是最新版本", Toast.LENGTH_SHORT).show();
					break;
				}
				break;
			case NotificationUtil.DOWNLOAD_APK:
				switch (msg.arg1) {
				case Start:
					nfm.notify(NotificationUtil.DOWNLOAD_APK, getApkNotification);
					dataServiceBinder.isUpdatingSystem = true;
					break;
				case SuccessForFile:
					dataServiceBinder.isUpdatingSystem = true;
					viewGetApk.setProgressBar(R.id.data_exchange_notification_progressbar, 100, (int) (100 * downloadLength / apkFileLength), false);
					viewGetApk.setTextViewText(R.id.data_exchange_notification_progress, tempDecimalFormat.format(100 * downloadLength / apkFileLength) + "%");
					nfm.notify(NotificationUtil.DOWNLOAD_APK, getApkNotification);
					break;
				case Stop:
					File file = new File(getSharedPreferences(PatrolApplication.PREFS_NAME, MODE_PRIVATE).getString(PatrolApplication.LATEST_VERSION_FILENAME, ""));
					getApkNotification.flags = Notification.FLAG_AUTO_CANCEL;
					if (file.exists()) {
						SystemMethodUtil.installAPK(DataService.this, file);
						viewGetApk.setTextViewText(R.id.data_exchange_notification_title, "下载完毕，点击进行安装");
						Intent intent = new Intent();
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setAction(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
						PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
						getApkNotification.contentIntent = pendingIntent;
					} else {
						Toast.makeText(getApplicationContext(), "未找到文件", Toast.LENGTH_SHORT).show();
					}
					viewGetApk.setProgressBar(R.id.data_exchange_notification_progressbar, 100, (int) (100 * downloadLength / apkFileLength), false);
					viewGetApk.setTextViewText(R.id.data_exchange_notification_progress, tempDecimalFormat.format(100 * downloadLength / apkFileLength) + "%");
					nfm.notify(NotificationUtil.DOWNLOAD_APK, getApkNotification);
					dataServiceBinder.isUpdatingSystem = false;
					apkFileLength = 0;
					downloadLength = 0;
					break;
				case WifiDisabled:
					Toast.makeText(getApplicationContext(), "请检查sd卡", Toast.LENGTH_SHORT).show();
					dataServiceBinder.isUpdatingSystem = false;
					break;
				case Error:
					getApkNotification.flags = Notification.FLAG_AUTO_CANCEL;
					viewGetApk.setTextViewText(R.id.data_exchange_notification_title, "下载文件失败");
					nfm.notify(NotificationUtil.DOWNLOAD_APK, getApkNotification);
					Toast.makeText(getApplicationContext(), "下载文件失败", Toast.LENGTH_SHORT).show();
					dataServiceBinder.isUpdatingSystem = false;
					break;
				}
				break;
			}
		}

	}

}
