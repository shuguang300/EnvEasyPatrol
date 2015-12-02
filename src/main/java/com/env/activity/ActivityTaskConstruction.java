package com.env.activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.env.bean.EP_NFCCard;
import com.env.bean.EP_User;
import com.env.component.DataService;
import com.env.component.PatrolApplication;
import com.env.easypatrol.R;
import com.env.nfc.NfcActivity;
import com.env.nfc.NfcUtils;
import com.env.utils.DataBaseUtil;
import com.env.utils.DataCenterUtil;
import com.env.utils.DialogUtil;
import com.env.utils.LocalDataHelper;
import com.env.utils.NotificationUtil;
import com.env.utils.SystemMethodUtil;
import com.env.utils.SystemParamsUtil;
import com.env.utils.ViewUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zxing.activity.CaptureActivity;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 巡检系统登录后的界面，展示泵房以及泵房下面的巡检卡
 * @author sk
 */
public class ActivityTaskConstruction extends NfcActivity{
	/** 
	 * 以单击模式进入系统 
	 */
	public static final int CARD_NORMAL = 0;
	/**
	 * 以刷卡模式进去系统
	 */
	
	public static final int CARD_NFC = 1;
	
	
	private final int RefreshUITimer = 0;
	private final int UploadTimerFlag = -1;
	private final int UpdateTimerFlag = 1;
	private final int Logout = 2;
	private final int Exit = 3;
	private SharedPreferences sp;
	private SharedPreferences.Editor editor;
	private long oneDayMiles = 86400000;
	private ExpandableListView constuction2Tag;
	private GetLocalDataAsyncTask getLocalDataAsyncTask;
	private ConstructionAdapter consAdapter;
	private String lastTime;
	private SQLiteDatabase db =null;
	private ArrayList<HashMap<String, String>>plantInfos,cards;
	private Intent Cards2Task,Card2Tag,dataService;
    private ProgressDialog logoutProgressDialog;
    private AlertDialog.Builder cardDialog,cardBuilder;
    private HashMap<String, String> child ;
    private Timer refreshUI = null,checkData = null,upload = null;
    private Message msg ;
    private int cardID,startMode = 0;
    private EditText ipText;
    private String [] longClickItme = {"查看本卡片巡检项"};
    private DataService.DataServiceBinder binder;
    private PatrolConsReceiver receiver;
    private LogoutThread thread;
    private ProgressDialog localDataDialog;
    private EP_User loginUser;
	private ActionBar actionBar;
    private boolean isTimerRun=true,dialogShow=false,getLocalDateIng=false,isNewIntent=false;
    private ServiceConnection connection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {	
			mBindService();
		}		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (DataService.DataServiceBinder)service;
		}
	};
    private Handler refreshUIHandler = new Handler(){
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
			case RefreshUITimer:
				consAdapter.notifyDataSetChanged();
				isTimerRun = false;
				break;
			case UploadTimerFlag:
				switch (msg.arg1) {
				case DataService.Success:
					consAdapter.notifyDataSetChanged();
					break;
				}
				if(upload !=null){
					upload.cancel();
					upload = null;
				}
				break;
			case UpdateTimerFlag:
				switch (msg.arg1) {
				case DataService.Success:
					consAdapter.notifyDataSetChanged();
					break;
				}
				if(checkData!=null){
					checkData.cancel();
					checkData = null;
				}
				break;
			case DataService.WifiDisabled:
				Toast.makeText(ActivityTaskConstruction.this, "网络不可用", Toast.LENGTH_SHORT).show();
    			break;
			case Logout:
				if(logoutProgressDialog!=null&&logoutProgressDialog.isShowing()){
	    			logoutProgressDialog.cancel();
	    		}
				userLogout();
				break;
			case Exit:
				if(logoutProgressDialog!=null&&logoutProgressDialog.isShowing()){
	    			logoutProgressDialog.cancel();
	    		}
				exitApp();
				break;
			}  		    		    		
    	};
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		iniParams();
		boolean needTips = getIntent().getBooleanExtra("NeedTips",false);
		if(needTips){
			countUpdateTime();
		}
		receiver = new PatrolConsReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.env.view.PatrolTaskConstruction.PatrolConsReceiver");
		registerReceiver(receiver, intentFilter);
		mBindService();
		iniLayout();
		initialActionBar();
	}

	private void initialActionBar(){
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(loginUser.getPositionName() + ":" + loginUser.getRealUserName());
		actionBar.show();
	}

	

	private void iniLayout(){		
		setContentView(R.layout.construction_card);
		iniUser();
	}
	
	
	
	private void iniParams(){
		db = DataBaseUtil.getInstance(ActivityTaskConstruction.this).getReadableDatabase();
		sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
		editor = sp.edit();
		loginUser = SystemParamsUtil.getInstance().getLoginUser(sp);
	}
	
	
	
//  初始化管理员界面
//	private void iniAdmin(){
//		isTimerRun = false;
//		userNameTv = (TextView)findViewById(R.id.user_admin_name);
//		userNameTv.setText("管理员:");
//		btnData = (Button)findViewById(R.id.admin_btn_data);
//		btnConfig = (Button)findViewById(R.id.admin_btn_config);
//		btnData.setOnClickListener(this);
//		btnConfig.setOnClickListener(this);
//	}
	
	
	//初始化操作工界面
	private void iniUser(){
		constuction2Tag = (ExpandableListView)findViewById(R.id.construction_card);		
		Cards2Task = new Intent(ActivityTaskConstruction.this, ActivityTaskGroup.class);
		Card2Tag = new Intent(ActivityTaskConstruction.this, ActivityTaskEachCard.class);
		setData();
		showLocalDataDialog();
		FirstLoadDataAsynTask firstLoadDataAsynTask = new FirstLoadDataAsynTask();
		firstLoadDataAsynTask.execute("");
		
	}
	
	/**
	 * 登出
	 */
	private void userLogout(){
		Intent loginIntent = new Intent(ActivityTaskConstruction.this,ActivityLogin.class);
		SystemParamsUtil.getInstance().logout();
		NotificationUtil.getInstance().updateStayNotification(ActivityTaskConstruction.this);
		startActivity(loginIntent);
		ActivityTaskConstruction.this.finish();
		
	}
	
	private void countUpdateTime(){
		sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
		editor = sp.edit();
		db = DataBaseUtil.getInstance(ActivityTaskConstruction.this).getReadableDatabase();
//		showAllCard = sp.getBoolean(PatrolApplication.CONS_CARD_INFLATEER, false);
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		Date now = Calendar.getInstance().getTime();
		String nowTime = df.format(now);
		lastTime = sp.getString(PatrolApplication.LAST_UPDATE_DATETIME, nowTime);
		int count = DataCenterUtil.getUnUploadTaskByUser(db, loginUser.getUserID());
		try {
			Date last = df.parse(lastTime);
			long span = now.getTime()-last.getTime();
			if(span>=1000*3600*24||count>0){
				String info = "";
				if(span>=1000*3600*24){
					final int days = (int) (span/oneDayMiles);
					info = "您已经"+days+"天没有更新数据了，请及时获取最新的数据";
				}
				if(count>0){
					info = info + "\r\n您还有"+count+"条已完成的任务没有上传，请及时进行上传";
				}
				DialogUtil.confirmDataExchange(ActivityTaskConstruction.this,info ,null);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void updateDate() {
		isTimerRun =true;
		if(refreshUI == null){
			refreshUI = new Timer();
			refreshUI.schedule(new TimerTask() {
				@Override
				public void run() {
					isTimerRun = true;
					setData();
					msg = refreshUIHandler.obtainMessage();
					msg.what = RefreshUITimer;
					msg.sendToTarget();
					isTimerRun = false;
				}
			}, 5 * 60 * 1000, 5 * 60 * 1000);
		}
		isTimerRun =false;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(SystemParamsUtil.getInstance().getIsLogin()){
			if(binder == null){
				mBindService();
			}
			if(startMode == 1 && !isNewIntent){
				getLocalData();
			}
			updateDate();	
			NotificationUtil.getInstance().updateStayNotification(ActivityTaskConstruction.this);
			startMode = 1;
			isNewIntent = false;
		}else{
			startActivity(new Intent(ActivityTaskConstruction.this,ActivityLogin.class));
			finish();
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);	
		isNewIntent = true;
		String tagId =NfcUtils.getTagID(intent);
		if(tagId.length()<1) {
			Toast.makeText(this, "无法读取卡片，请重试", Toast.LENGTH_SHORT).show();
			return;
		}
		EP_NFCCard card = LocalDataHelper.getNfcCardByTagId(db, tagId);
		if(card==null){
			Toast.makeText(this, "卡片未注册", Toast.LENGTH_SHORT).show();
			return;
		}
		if(dialogShow || getLocalDateIng) return;
		if(card.getState()==0){
			Toast.makeText(this, "卡片被禁用", Toast.LENGTH_SHORT).show();
			return;
		}
		if(card.getCardType() == NfcUtils.tagCardType){
			HashMap<String, String> tmp = new HashMap<String, String>();
			tmp.put("CardID", String.valueOf(card.getCardID()));
			tmp.put("CardName", card.getCardName());
			Cards2Task.putExtra("Child", tmp);
			Cards2Task.putExtra("Mode", CARD_NFC);
			startActivityForResult(Cards2Task, 1);
		} else if (card.getCardType() ==NfcUtils.userCardType) {
//				curUserID = NFCUtils.getUserID(intent,db);
//				if(curUserID.equals("")){
//					Toast.makeText(this, "未找到卡片信息", Toast.LENGTH_SHORT).show();
//				}else {
//					if(!curUserID.equals(SystemParamsUtil.getInstance().getUser(sp).getUserID())){					
//						dialog = new AlertDialog.Builder(this).setTitle("检测到用户卡，是否切换用户？").setPositiveButton("确定",new DialogInterface.OnClickListener() {					
//							@Override
//							public void onClick(DialogInterface dialog, int which) {							
//								Cursor userCS = db.rawQuery("select * from EP_User where UserID = ?", new String[]{curUserID});
//								userCS.moveToFirst();
//								if(userCS!=null && userCS.getCount()>0){
//									userID = curUserID;
//									userName =  userCS.getString(2);
//									SystemParamsUtil.getInstance().setLoginInfo(true, DataCenterUtil.parseUserToBean(userCS),editor);
//									countUpdateTime();
//									iniParams();
//									iniLayout(PlantType);
//									iniBothLayout();
//									onResume();		
//								}else {
//									Toast.makeText(ActivityTaskConstruction.this, "切换用户失败，请重试", Toast.LENGTH_SHORT).show();
//								}
//								if(userCS!=null)userCS.close();
//								dialogShow = false;
//							}
//						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//								dialogShow = false;
//							}
//						});
//						dialog.create().show();	
//						dialogShow = true;
//					}else {
//						Toast.makeText(this, "当前用户卡", Toast.LENGTH_SHORT).show();
//					}
//				}			
			} 		
	}
	
	@Override
	public void onBackPressed() {
		ExitDialog(ActivityTaskConstruction.this).show();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode==1) {
			Bundle bundle = data.getExtras();
			String scanResult = bundle.getString("result");
			try {
				Gson gson = new Gson();
				HashMap<String,String> hash = gson.fromJson(scanResult,new TypeToken<HashMap<String,String>>(){}.getType());
				if(hash.get("Type")!=null || hash.get("Type").toString().length()>0){
					Intent intent = new Intent(ActivityTaskConstruction.this,ActivityDeviceInfo.class);
					intent.putExtra("data", hash);
					startActivity(intent);
				}else{
					Toast.makeText(ActivityTaskConstruction.this,"未能识别的二维码",Toast.LENGTH_SHORT).show();
				}
			} catch (Exception ex){
				Toast.makeText(ActivityTaskConstruction.this,"未能识别的二维码",Toast.LENGTH_SHORT).show();
			}

		}
	}

	/**
	 * 退出程序
	 * @param context
	 * @return
	 */
	private Dialog ExitDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("系统信息");
		builder.setMessage("确定要退出程序吗?");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				thread = new LogoutThread(refreshUIHandler, ActivityTaskConstruction.this, Exit);
				thread.doStart("正在退出程序......");
			}
		});
		builder.setNegativeButton("取消", null);
		return builder.create();
	}
	
	/**
	 * 退出整个程序
	 */
	private void exitApp(){
		SystemParamsUtil.getInstance().exit();
		SystemParamsUtil.getInstance().logout();
		NotificationUtil.getInstance().updateStayNotification(ActivityTaskConstruction.this);
		ActivityTaskConstruction.this.finish();
	}
	
	/**
	 * 获取数据
	 */
	private void setData(){
		if(db==null){
			db = DataBaseUtil.getInstance(ActivityTaskConstruction.this).getReadableDatabase();
		}
		sp = getSharedPreferences(PatrolApplication.PREFS_NAME, MODE_PRIVATE);
		SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);

		String now = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(new Date());

		String nowDTStr = sdf.format(Calendar.getInstance().getTime());
		plantInfos = LocalDataHelper.getPlantByUserCharge(db, loginUser.getUserID(),now);
		cards = LocalDataHelper.getChargeOfCardByUserId(db, loginUser.getUserID());

		for(HashMap<String, String> card : cards){
			int [] counts = DataCenterUtil.getTaskCountByCard(db, Integer.parseInt(card.get("CardID")), nowDTStr, nowDTStr);
			card.put("MountCount", counts[0]+"");
			card.put("DoneCount", counts[1]+"");
			card.put("NeedRemind", (counts[0]-counts[1])+"");

		}
	}

	private void goToQrCodeActivity(){
		Intent intent = new Intent(ActivityTaskConstruction.this, CaptureActivity.class);
		startActivityForResult(intent,1);
	}
	
	
	/**
	 * 检查更新，使用手机网络和wifi网络
	 */
	private void checkUpdate(){
		mBindService();
		int apnType = SystemMethodUtil.getAPNType(ActivityTaskConstruction.this);
		if(binder!=null){
			if (binder.isUpdating) {
				Toast.makeText(getApplicationContext(), "正在更新任务数据",Toast.LENGTH_LONG).show();
			}else {
				switch (apnType) {
				case SystemMethodUtil.NoNetWork:
					DialogUtil.setApnDialog(ActivityTaskConstruction.this);
					break;
				case SystemMethodUtil.MobileNetWork:
					if(sp==null){
						sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
					}
					if(editor ==null){
						editor = sp.edit();
					}
					boolean hasTips = sp.getBoolean(PatrolApplication.HAS_DATAEXCHANGE_TIPS, true);
					if(hasTips){
						DialogUtil.confirmNetWork(ActivityTaskConstruction.this,new DialogInterface.OnClickListener() {						
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								Toast.makeText(getApplicationContext(), "获取最新数据中....",Toast.LENGTH_LONG).show();
								update();						
							}
						},editor);
					}else {
						Toast.makeText(getApplicationContext(), "获取最新数据中....",Toast.LENGTH_LONG).show();
						update();
					}
					
					break;
				case SystemMethodUtil.WifiNetWork:
					Toast.makeText(getApplicationContext(), "获取最新数据中....",Toast.LENGTH_LONG).show();
					update();
					break;
				}
			}
		}else {
			Toast.makeText(getApplicationContext(), "服务正在启动中，请稍后重试获取数据",Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * 上传任务，分为使用wifi和手机网络上传2种情况
	 */
	private void uploadData(){
		mBindService();
		int apnType = SystemMethodUtil.getAPNType(ActivityTaskConstruction.this);
		if(binder!=null){
			if (binder.isUploading) {
				Toast.makeText(getApplicationContext(), "正在上传任务，请不要重复操作",Toast.LENGTH_LONG).show();
			}else {
				switch (apnType) {
				case SystemMethodUtil.NoNetWork:
					DialogUtil.setApnDialog(ActivityTaskConstruction.this);
					break;
				case SystemMethodUtil.MobileNetWork:
					if(sp==null){
						sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
					}
					if(editor ==null){
						editor = sp.edit();
					}
					boolean hasTips = sp.getBoolean(PatrolApplication.HAS_DATAEXCHANGE_TIPS, true);
					if(hasTips){
						DialogUtil.confirmNetWork(ActivityTaskConstruction.this,new DialogInterface.OnClickListener() {						
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								Toast.makeText(getApplicationContext(), "上传数据中....",Toast.LENGTH_LONG).show();
								uploadWithMobileNet();							
							}
						},editor);
					}else {
						Toast.makeText(getApplicationContext(), "上传数据中....",Toast.LENGTH_LONG).show();
						uploadWithMobileNet();
					}
					break;
				case SystemMethodUtil.WifiNetWork:
					Toast.makeText(getApplicationContext(), "上传数据中....",Toast.LENGTH_LONG).show();
					uploadWithWifi();
					break;
				}
			}
		}else {
			Toast.makeText(getApplicationContext(), "服务正在启动中，请稍后重试上传数据",Toast.LENGTH_LONG).show();
		}
	}
	

	/**
	 * 使用wifi上传任务
	 */
	public void uploadWithWifi() {
		binder.getDataService().uploadDataJsonObject(db);
		upload = new Timer();
		upload.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!binder.isUploading) {
					msg = refreshUIHandler.obtainMessage();
					msg.what = UploadTimerFlag;
					if (binder.isExchangeDataSucss) {
						setData();
						msg.arg1 = DataService.Success;
					} else {
						msg.arg1 = DataService.Error;
					}
					msg.sendToTarget();
				}
			}
		}, 0, 2000);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.patroltaskconstruction_popupmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==android.R.id.home){
			AlertDialog.Builder builder = new AlertDialog.Builder(ActivityTaskConstruction.this);
			builder.setTitle("系统信息");
			builder.setMessage("是否注销当前用户？");
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					logout();
				}
			});
			builder.setNegativeButton("取消", null);
			builder.create().show();
		}else{
			switch (item.getOrder()) {
				case 0:
					checkUpdate();
					break;
				case 1:
					uploadData();
					break;
				case 2:
					goToQrCodeActivity();
					break;
				case 3:
					break;
				case 4:
					Intent config = new Intent(ActivityTaskConstruction.this,ActivityConfig.class);
					startActivity(config);
					break;
				case 5:
					logout();
					break;
			}
		}

		return super.onOptionsItemSelected(item);
	}


	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	/**
	 * 登出
	 */
	private void logout(){
		editor.putString(PatrolApplication.USER_INFO,"");
		editor.commit();
		thread = new LogoutThread(refreshUIHandler, ActivityTaskConstruction.this, Logout);
		thread.doStart("正在注销......");
	}
	
	@Override
	protected void onDestroy() {
		if(receiver!=null){
			unregisterReceiver(receiver);
		}
		unbindService(connection);
		if(refreshUI !=null){
			refreshUI.cancel();
			refreshUI = null;
		}	
		super.onDestroy();
	}
	
	/**
	 * 绑定后台服务
	 */
	private void mBindService(){
		if(binder==null){
			dataService = new Intent();
			dataService.setAction("com.env.component.DataService");
			dataService.setPackage(getPackageName());
			bindService(dataService, connection, Service.BIND_AUTO_CREATE);
		}
	}
		
	/**
	 * 通过查找卡号进入任务界面
	 */
	private void showCard(){
		if(cardBuilder==null){
			cardBuilder = new AlertDialog.Builder(ActivityTaskConstruction.this);
		}
		View view = LayoutInflater.from(ActivityTaskConstruction.this).inflate(R.layout.find_card, null);
		cardBuilder.setTitle("输入卡片号进行查找");
		cardBuilder.setView(view);
		ipText = (EditText)view.findViewById(R.id.find_card);
		cardBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				String cardIDStr = ipText.getText().toString();
				try {
					cardID = Integer.parseInt(cardIDStr);
					sp = getSharedPreferences(PatrolApplication.PREFS_NAME, MODE_PRIVATE);
					Cursor cards = db.rawQuery("select  * from EP_NFCCard as a inner join EP_Construction as b on a.ConstructionID = b.ConstructionID where a.CardType = 0 and a.State = 1 and a.CardID = "+cardID + " and b.PlantID = " +sp.getInt(PatrolApplication.IDENTIFY_PLANT, 0), null);
					cards.moveToFirst();
					if(cards.getCount()<1){
						Toast.makeText(ActivityTaskConstruction.this, "未找到卡片", Toast.LENGTH_LONG).show();
					}else {
						HashMap<String, String> cardsHash = new HashMap<String, String>();
						for(int i =0;i<cards.getColumnCount();i++){
							cardsHash.put(cards.getColumnName(i), cards.getString(i));
						}
						Cards2Task.putExtra("Child", cardsHash);
						Cards2Task.putExtra("Mode", CARD_NFC);
						startActivityForResult(Cards2Task, 0);
					}
					cards.close();
				} catch (Exception e) {
					Toast.makeText(ActivityTaskConstruction.this, "请输入有效的卡片号码", Toast.LENGTH_SHORT).show();
				}											
			}
		});
		cardBuilder.create().show();	
	}
	
	
	/**
	 * list的数据适配器
	 * @author sk
	 *
	 */
	class ConstructionAdapter extends BaseExpandableListAdapter{
		private Context mcontext;
		

		public ConstructionAdapter (Context context){
			this.mcontext = context;
		}
		
		@Override
		public int getGroupCount() {
			return plantInfos.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			String count = plantInfos.get(groupPosition).get("CardCount");
			return Integer.valueOf(count);
		}

		@Override
		public HashMap<String, String> getGroup(int groupPosition) {
			return plantInfos.get(groupPosition);
		}

		@Override
		public HashMap<String, String> getChild(int groupPosition, int childPosition) {
			HashMap<String, String> map  = null;
			int count = 0;
			String plantId = plantInfos.get(groupPosition).get("PlantID");
			for(HashMap<String, String> card : cards){
				if(card.get("PlantID").equals(plantId)){
					if(count == childPosition){
						map = card;
						break;
					}
					count++;
				}
//				if(showAllCard){
//					if(card.get("PlantID").equals(plantId)){
//						if(count == childPosition){
//							map = card;
//							break;
//						}
//						count++;
//					}
//				}else {
//					if(card.get("PlantID").equals(plantId)&&!card.get("MountCount").equals("0")){
//						if(count == childPosition){
//							map = card;
//							break;
//						}
//						count++;
//					}
//				}
			}
			return map;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return Long.valueOf(plantInfos.get(groupPosition).get("PlantID"));
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {	
			return Long.valueOf(getChild(groupPosition, childPosition).get("CardID"));
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,View convertView, ViewGroup parent) {
			HashMap<String,String> map = getGroup(groupPosition);
			if(convertView==null){
				convertView = LayoutInflater.from(mcontext).inflate(R.layout.cons2cards_cons, null);
			}
			TextView name = (TextView)convertView.findViewById(R.id.cons2cards_cons_name);
			if(map.get("NewestTime")==null) name.setText(map.get("PlantName"));
			else {
				String time = SystemMethodUtil.toJsonDateByStr(map.get("NewestTime"));
				name.setText(map.get("PlantName")+"\r\n"+"下次任务："+time);
			}
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,boolean isLastChild, View convertView, ViewGroup parent) {
			HashMap<String, String> map = getChild(groupPosition, childPosition);
			if(convertView==null){
				convertView = LayoutInflater.from(mcontext).inflate(R.layout.cons2cards_cards, null);
			}
			convertView.setTag(map);
			TextView name = (TextView)convertView.findViewById(R.id.cons2cards_cards_name);
			TextView count = (TextView)convertView.findViewById(R.id.cons2cards_cards_count);
			TextView remindCount = (TextView)convertView.findViewById(R.id.cons2cards_cards_remind_count);
			ProgressBar progressBar = (ProgressBar)convertView.findViewById(R.id.cons2cards_cards_progress);
			int needRemindCount = 0;
			try {
				needRemindCount = Integer.parseInt(map.get("NeedRemind"));
			} catch (Exception e) {
				needRemindCount = 0;
			}
			if(needRemindCount>0){
				remindCount.setBackgroundResource(R.drawable.cons2cards_cards_remind_count);
				remindCount.setText(map.get("NeedRemind"));
			}else {
				remindCount.setBackgroundResource(0);
				remindCount.setText("");
			}
			count.setText(map.get("DoneCount")+"/"+map.get("MountCount"));
			name.setText(map.get("CardName"));
			int mountCount = 0;
			try {
				mountCount = Integer.parseInt(map.get("MountCount"));
			} catch (Exception e) {
				mountCount = 0;
			}
			if(mountCount==0){
				progressBar.setMax(100);
				progressBar.setProgress(100);
//				convertView.setVisibility(View.GONE);
			}else {
				progressBar.setMax(Integer.valueOf(map.get("MountCount")));
				progressBar.setProgress(Integer.valueOf(map.get("DoneCount")));	
//				convertView.setVisibility(View.VISIBLE);
			}
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
		
	}
	
	/**
	 * 有任务更新后，接受广播，并更新ui
	 * @author sk
	 */
	class PatrolConsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getExtras().getString("action").equals("PatrolConfig")){
				SystemParamsUtil.getInstance().logout();
				NotificationUtil.getInstance().updateStayNotification(ActivityTaskConstruction.this);
				editor.putString(PatrolApplication.USER_INFO, "");
				editor.commit();
				ActivityTaskConstruction.this.finish();
			}else if (intent.getExtras().getString("action").equals("DataService")||intent.getExtras().getString("action").equals("TaskService")) {
				Log.v("PatrolConsReceiver", "message has received");
				getLocalData();
			}				
		}
	}

	

	
	/**
	 * 从服务器上拉去数据
	 */
	public void update() {
		binder.getDataService().upDateDB(db);
		checkData = new Timer();
		checkData.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!binder.isUpdating) {
					msg = refreshUIHandler.obtainMessage();
					msg.what = UpdateTimerFlag;
					if (binder.isExchangeDataSucss) {
						setData();
						msg.arg1 = DataService.Success;
					} else {
						msg.arg1 = DataService.Error;
					}
					msg.sendToTarget();
				}
			}
		}, 0, 2000);
	}

	/**
	 * 使用手机网络上传任务
	 */
	public void uploadWithMobileNet() {
		binder.getDataService().uploadOnlyTextData(db);
		upload = new Timer();
		upload.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!binder.isUploading) {
					msg = refreshUIHandler.obtainMessage();
					msg.what = UploadTimerFlag;
					if (binder.isExchangeDataSucss) {
						setData();
						msg.arg1 = DataService.Success;
					} else {
						msg.arg1 = DataService.Error;
					}
					msg.sendToTarget();
				}
			}
		}, 0, 2000);
	}


	/**
	 * 弹出加载数据的等待框
	 */
	private void showLocalDataDialog(){
		if(localDataDialog == null){
			localDataDialog = new ProgressDialog(ActivityTaskConstruction.this);
			localDataDialog.setCancelable(false);
			localDataDialog.setTitle("请稍等");
			localDataDialog.setMessage("正在努力的加载数据");
		}
		localDataDialog.show();
	}
	

	/**
	 * 其他情况时（非oncreate事件中）使用的加载本地数据的事件的异步类
	 * @author sk
	 */
	private class GetLocalDataAsyncTask extends AsyncTask<String, String, ArrayList<HashMap<String, String>>>{
		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(String... params) {
			getLocalDateIng = true;
			setData();
			return null;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		@Override
		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			consAdapter.notifyDataSetChanged();
			getLocalDateIng = false;
			if(localDataDialog!=null){
				localDataDialog.dismiss();
			}
			
		}
	}
	
	/**
	 * 其他情况时（非oncreate事件中）使用的加载本地数据的事件的异步方法
	 */
	private void getLocalData(){
		if(!getLocalDateIng){
			showLocalDataDialog();
			getLocalDataAsyncTask = new GetLocalDataAsyncTask();
			getLocalDataAsyncTask.execute("");
		}
	}
	
	
	/**
	 * oncreate事件中执行的事件异步类
	 * @author sk
	 *
	 */
	private class FirstLoadDataAsynTask extends AsyncTask<String, String, ArrayList<HashMap<String, String>>>{
		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(String... params) {
			getLocalDateIng = true;
			setData();
			return null;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		@Override
		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
			consAdapter = new ConstructionAdapter(ActivityTaskConstruction.this);
			constuction2Tag.setAdapter(consAdapter);
			for(int i =0;i<consAdapter.getGroupCount();i++){
				constuction2Tag.expandGroup(i);
			}
			constuction2Tag.setGroupIndicator(null);		
			constuction2Tag.setOnItemLongClickListener(new OnItemLongClickListener() {
				@SuppressWarnings("unchecked")
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
					if(view.getTag()!=null){
						child = (HashMap<String, String>)view.getTag();
						if(cardDialog == null){
							cardDialog = new AlertDialog.Builder(ActivityTaskConstruction.this);
							cardDialog.setCancelable(true);
							cardDialog.setSingleChoiceItems(longClickItme, -1, new DialogInterface.OnClickListener() {						
								@Override
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
									case 0:
										Card2Tag.putExtra("Child", child);
										Card2Tag.putExtra("Mode", CARD_NORMAL);
										startActivityForResult(Card2Tag, 0);
										break;
									}
									dialog.dismiss();
								}
							});
							cardDialog.create();
						}
						cardDialog.show();
					}	
					return true;
				}
			});		
			constuction2Tag.setOnChildClickListener(new OnChildClickListener() {			
				@Override
				public boolean onChildClick(ExpandableListView parent, View v,int groupPosition, int childPosition, long id) {
					HashMap<String,String> hashMap = consAdapter.getChild(groupPosition,childPosition);
					int doneCount = Integer.parseInt(hashMap.get("DoneCount"));
					int mountCount = Integer.parseInt(hashMap.get("MountCount"));
					int taskCount = mountCount-doneCount;
					Cards2Task.putExtra("TaskCount",taskCount);
					Cards2Task.putExtra("Child", hashMap);
					Cards2Task.putExtra("Mode", CARD_NORMAL);
					startActivityForResult(Cards2Task, ViewUtil.VIEW_DOING_TASK);
					return true;
				}
			});		
			if(localDataDialog!=null && localDataDialog.isShowing()){
				localDataDialog.dismiss();
			}
			getLocalDateIng = false;
		}
	}
	
	/**
	 * 登出线程
	 * @author sk
	 */
	private class LogoutThread extends Thread{
		private Handler mHandler;
		private Context mContext;
		private int mMode;
		public LogoutThread(Handler handler,Context context,int mode){
			mHandler = handler;
			mContext = context;
			mMode = mode;
		}		
		public void doStart(String info){
			if(logoutProgressDialog == null){
				logoutProgressDialog = new ProgressDialog(mContext);
				logoutProgressDialog.setTitle("系统消息");
				logoutProgressDialog.setMessage(info);
				logoutProgressDialog.setCancelable(false);
				logoutProgressDialog.setIndeterminate(true);
			}
			logoutProgressDialog.show();
			start();
		}
		@Override
		public void run() {
			super.run();
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(isTimerRun == true){
					
				}else {
					mHandler.sendEmptyMessage(mMode);
					break;
				}
				
			}
		}
	}
}


