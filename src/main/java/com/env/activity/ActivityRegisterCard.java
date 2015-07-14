package com.env.activity;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.env.bean.EP_NFCCard;
import com.env.component.PatrolApplication;
import com.env.easypatrol.R;
import com.env.nfc.NfcActivity;
import com.env.nfc.NfcUtils;
import com.env.utils.DataBaseUtil;
import com.env.utils.HttpUtil;
import com.env.utils.SystemMethodUtil;
import com.env.utils.SystemParamsUtil;

public class ActivityRegisterCard extends NfcActivity implements OnClickListener{
	private SQLiteDatabase db;
	private String userID,plantName,cardTagID,sqlStr,selectedUserID;
	private int plantID,plantType,selectedConsID,selectedCardType,selectedPlantID,consPos,userPos,plantPos;
	private final int Verify = 0;
	private final int Submit = 1;
	private final int Update = 2;
	private final int netError = 0;
	private final int Failed = 1;
	private final int OK = 2;
	private int newCardID;
	private int newIntentFlag,onclickFlag,modeFlag;
	private ArrayAdapter<String> consAdapter,cardTypeAdapter,plantAdapter,userAdapter;
	private ArrayList<HashMap<String, String>> plantInfo,consInfo,userInfo;
	private HashMap<String, String> selectedPlant,selectedCons,selectedUser;
	private String[] consNameArr,plantNameArr,userNameArr;
	private String [] cardTypeArr = {"巡检卡","用户卡"};
	private Thread threadVerify,threadSubmit;
	private CheckBox cbVerify;
	private Button btSubmit;
	private RelativeLayout rlCardType,rlPlantName,rlCardName,rlCardCons,rlCardInfo,rlCardTagID,rlUserList;
	private LinearLayout cardTagTypeGroup;
	private Spinner spCardType,spConstruction,spPlantName,spUserList;
	private EditText etCardName,etCardInfo;
	private TextView tvCardTagID,titleBack;
	private InputMethodManager imm;
	private SharedPreferences sp;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {	
			case Verify:
				switch (msg.arg1) {
				case netError:
					newIntentFlag = 0; 
					Toast.makeText(ActivityRegisterCard.this, "读卡失败，请重试或检查您的网络", Toast.LENGTH_SHORT).show();	
					btSubmit.setVisibility(View.GONE);
					break;
				case Failed:
					newIntentFlag = 0; 
					iniUpdateLayout(getCard(cardTagID), true);
					break;
				case OK:
					newIntentFlag = 0; 
					Toast.makeText(ActivityRegisterCard.this, "该卡片未被使用，请继续补全卡片信息", Toast.LENGTH_SHORT).show();
					iniSubmitLayout(0, true);
					
					break;
				}
				break;

			case Submit:
				onclickFlag = 0;
				switch (msg.arg1) {
				case netError:
					Toast.makeText(ActivityRegisterCard.this, "提交失败，请重试或检查您的网络", Toast.LENGTH_SHORT).show();
					break;
				case Failed:
					Toast.makeText(ActivityRegisterCard.this, "提交失败，请重试或检查您的网络", Toast.LENGTH_SHORT).show();
					break;
				case OK:
					iniUpdateLayout(getCard(cardTagID), true);
					Toast.makeText(ActivityRegisterCard.this, "提交成功", Toast.LENGTH_SHORT).show();					
					break;
				}
				break;
			case Update:
				onclickFlag = 0;
				switch (msg.arg1) {
				case netError:
					Toast.makeText(ActivityRegisterCard.this, "修改失败，请检查您的网络", Toast.LENGTH_SHORT).show();
					break;
				case Failed:
					Toast.makeText(ActivityRegisterCard.this, "修改失败，请重试", Toast.LENGTH_SHORT).show();
					break;
				case OK:
					iniUpdateLayout(getCard(cardTagID), true);
					Toast.makeText(ActivityRegisterCard.this, "修改成功", Toast.LENGTH_SHORT).show();
					break;
				}			
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.patrol_registercard);
		SystemParamsUtil.getInstance().addActivity(this);
		if(db==null){
			db = DataBaseUtil.getInstance(ActivityRegisterCard.this).getReadableDatabase();
		}	
		sp = getSharedPreferences(PatrolApplication.PREFS_NAME, MODE_PRIVATE);
	//	userID = SystemParamsUtil.getInstance().getUser(sp).getUserID();
		sqlStr = "select PlantName,PlantID,PlantType from EP_Plant where PlantID = (select PlantID from EP_User where UserID = '"+userID+"')";
		Cursor plant = db.rawQuery(sqlStr, null);
		plant.moveToFirst();
		if(plant.getCount()>0){
			plantName = plant.getString(0);
			plantID = plant.getInt(1);
			plantType = plant.getInt(2);
		}
		plant.close();
		iniData();
		/*
		if(plantType==3){
			
		}else if (plantType==1) {
			iniConstruction(plantID);			
		}				
		*/
		initialize();
	}
	
	@Override
	public void iniData(){
		iniPlantName();
		iniConstruction(selectedPlantID);
		iniUserList(selectedPlantID);
		selectedCardType = 0;
	}
	
	private void initialize(){
		cbVerify = (CheckBox)findViewById(R.id.regcard_cb_verify);
		cardTagTypeGroup = (LinearLayout)findViewById(R.id.regcard_layout_tagcardgroup);
		
		tvCardTagID = (TextView)findViewById(R.id.regcard_value_cardid);
		
		rlCardTagID = (RelativeLayout)findViewById(R.id.regcard_layout_cardid);
		rlCardTagID.setOnClickListener(this);
		rlPlantName = (RelativeLayout)findViewById(R.id.regcard_layout_plantname);
		rlPlantName.setOnClickListener(this);
		rlCardType = (RelativeLayout)findViewById(R.id.regcard_layout_cardtype);
		rlCardType.setOnClickListener(this);
		rlCardName = (RelativeLayout)findViewById(R.id.regcard_layout_cardname);
		rlCardName.setOnClickListener(this);
		rlCardCons = (RelativeLayout)findViewById(R.id.regcard_layout_cardconstruction);
		rlCardCons.setOnClickListener(this);
		rlCardInfo = (RelativeLayout)findViewById(R.id.regcard_layout_cardinfo);
		rlCardInfo.setOnClickListener(this);
		rlUserList = (RelativeLayout)findViewById(R.id.regcard_layout_userlist);
		rlUserList.setOnClickListener(this);

		spCardType = (Spinner)findViewById(R.id.regcard_spinner_cardtype);
		spConstruction = (Spinner)findViewById(R.id.regcard_spinner_cardconstruction);
		spPlantName = (Spinner)findViewById(R.id.regcard_spinner_plantname);
		spUserList = (Spinner)findViewById(R.id.regcard_spinner_userlist);
		
		etCardInfo = (EditText)findViewById(R.id.regcard_value_cardinfo);
		etCardInfo.setText("");
		etCardName = (EditText)findViewById(R.id.regcard_value_cardname);
		etCardName.setText("");
		btSubmit = (Button)findViewById(R.id.regcard_submit);
		btSubmit.setOnClickListener(this);
		titleBack = (TextView)findViewById(R.id.regcard_back);
		titleBack.setOnClickListener(this);
								
		consAdapter = new ArrayAdapter<String>(ActivityRegisterCard.this,android.R.layout.simple_dropdown_item_1line, consNameArr);
		spConstruction.setAdapter(consAdapter);
		cardTypeAdapter = new ArrayAdapter<String>(ActivityRegisterCard.this,android.R.layout.simple_dropdown_item_1line, cardTypeArr);
		spCardType.setAdapter(cardTypeAdapter);
		plantAdapter = new ArrayAdapter<String>(ActivityRegisterCard.this,android.R.layout.simple_dropdown_item_1line, plantNameArr);
		spPlantName.setAdapter(plantAdapter);
		userAdapter = new ArrayAdapter<String>(ActivityRegisterCard.this,android.R.layout.simple_dropdown_item_1line, userNameArr);
		spUserList.setAdapter(userAdapter);
		
		
		spConstruction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
				selectedCons  =  consInfo.get(arg2);
				selectedConsID = Integer.parseInt(selectedCons.get("ConstructionID"));
				consPos = arg2;
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});	
		
		spCardType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
				selectedCardType = arg2;
				if(arg2==0){
					cardTagTypeGroup.setVisibility(View.VISIBLE);
					rlUserList.setVisibility(View.GONE);
				}else {
					cardTagTypeGroup.setVisibility(View.GONE);
					rlUserList.setVisibility(View.VISIBLE);
				}				
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {								
			}
		});
		
		spPlantName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
				selectedPlant = plantInfo.get(position);
				selectedPlantID = Integer.parseInt(selectedPlant.get("PlantID"));
				plantPos = position;
				switch (selectedCardType) {
				case 0:
					getConsList(selectedPlantID);
					consAdapter = new ArrayAdapter<String>(ActivityRegisterCard.this,android.R.layout.simple_dropdown_item_1line, consNameArr);
					spConstruction.setAdapter(consAdapter);
					for(int i =0;i<consInfo.size();i++){
						if(Integer.valueOf(consInfo.get(i).get("ConstructionID"))==selectedConsID){
							spConstruction.setSelection(i);			
							consPos = i;							
							break;
						}
					}
					break;

				case 1:
					getUserList(selectedPlantID);					
					userAdapter = new ArrayAdapter<String>(ActivityRegisterCard.this,android.R.layout.simple_dropdown_item_1line, userNameArr);
					spUserList.setAdapter(userAdapter);	
					for(int i =0;i<userInfo.size();i++){
						if(userInfo.get(i).get("UserID").equals(selectedUserID)){
							spUserList.setSelection(i);	
							userPos = i;								
							break;
						}
					}
					break;
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {						
			}
		});
		
		spUserList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
				selectedUser  =  userInfo.get(position);
				selectedUserID =selectedUser.get("UserID");
				userPos = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {						
			}
		});
		iniUI();
	}
	
	private void iniUI(){
		cbVerify.setChecked(false);
		btSubmit.setVisibility(View.GONE);
		rlPlantName.setVisibility(View.GONE);
		rlCardType.setVisibility(View.GONE);
		rlUserList.setVisibility(View.GONE);
		cardTagTypeGroup.setVisibility(View.GONE);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
//		super.onNewIntent(intent);
//		int netWork = SystemMethodUtil.getAPNType(ActivityRegisterCard.this);
//		if(netWork==SystemMethodUtil.NoNetWork){
//			newIntentFlag = 0;
//			Toast.makeText(ActivityRegisterCard.this, "未检测到网络，读卡失败", Toast.LENGTH_SHORT).show();
//		}else {
//			if(newIntentFlag==1){
//				newIntentFlag = 1;
//				Toast.makeText(ActivityRegisterCard.this, "正在检测，请不要重复操作", Toast.LENGTH_SHORT).show();
//			}else {
//				newIntentFlag = 1;
//				cardTagID = NfcUtils.getTagID(intent);
//				if(cardTagID.isEmpty()){
//					cbVerify.setChecked(false);
//					newIntentFlag = 0;
//					Toast.makeText(ActivityRegisterCard.this, "读取卡片信息失败，请重试", Toast.LENGTH_SHORT).show();
//				}else {
//					cbVerify.setChecked(true);
//					tvCardTagID.setHint(cardTagID);
//					threadVerify = new Thread(new Runnable() {
//						@Override
//						public void run() {
//							JSONObject object = new JSONObject();
//							Message msg = handler.obtainMessage();
//							msg.what = Verify;
//							try {
//								object.put("tagID", cardTagID);
//								String result = HttpUtil.getInstance(ActivityRegisterCard.this).HTTPClientPost(HttpUtil.getInstance(ActivityRegisterCard.this).URL_WSPatrol, "VerifyNFCCardTagID", object);
//								if(result.equals("false")){
//									msg.arg1 = netError;
//								}else {
//									JSONObject jsonObject = new JSONObject(result);
//									int count = jsonObject.getInt("d");
//									switch (count) {
//									case -1:
//										msg.arg1 = netError;
//										modeFlag = Verify;
//										break;
//									case 0:
//										msg.arg1 = OK;
//										modeFlag = Submit;
//										break;
//									case 1:
//										msg.arg1 = Failed;
//										modeFlag = Update;
//										break;
//									}
//								}
//							} catch (JSONException e) {
//								e.printStackTrace();
//								msg.arg1 = netError;
//							}finally{
//								handler.sendMessage(msg);
//								newIntentFlag = 0;
//							}
//						}
//					});
//					threadVerify.start();
//					Toast.makeText(ActivityRegisterCard.this, "读取卡片信息成功，正在检查卡片", Toast.LENGTH_SHORT).show();
//				}
//			}
//		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(SystemParamsUtil.getInstance().getIsLogin()){
			
		}else{
			startActivity(new Intent(ActivityRegisterCard.this,ActivityLogin.class));
			finish();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		ClearFocus();
		if(v.getId()==R.id.regcard_submit){
			verifyAndCommit(cardTagID);
		}
		/*
		if(v.getId()==R.id.regcard_submit){			
			if(onclickFlag == 1){
				Toast.makeText(PatrolRegisterCard.this, "正在提交，请不要重复操作", Toast.LENGTH_SHORT).show();
			}else {		
				onclickFlag = 1;
				Toast.makeText(PatrolRegisterCard.this, "正在提交， 请稍等", Toast.LENGTH_SHORT).show();				
				if(verify()){
					threadSubmit = new Thread(new Runnable() {				
						@Override
						public void run() {							
							Message msg = handler.obtainMessage();
							JSONObject object = new JSONObject();
							try {
								iniParams();
								object.put("CardTagID", cardTagID);
								object.put("CardName", etCardName.getText().toString().trim());
								object.put("CardType", selectedCardType);
								object.put("ConstructionID", selectedConsID);
								object.put("Description", etCardInfo.getText().toString().trim());
								object.put("UserID", selectedUserID);
								ContentValues cv = new ContentValues();
								cv.put("CardTagID", cardTagID);
								cv.put("CardName", etCardName.getText().toString().trim());
								cv.put("CardType", selectedCardType);
								cv.put("Description", etCardInfo.getText().toString().trim());
								cv.put("State", 1);
								cv.put("ConstructionID", selectedConsID);
								String result = "";
								if(modeFlag == Submit){
									msg.what = Submit;
									result = HTTPHelper.getInstance(PatrolRegisterCard.this).HTTPClientPost(HTTPHelper.getInstance(PatrolRegisterCard.this).URL_WSPatrol, "InsertNewNFCCard", object);
									if(result.equals("false")){
										msg.arg1 = netError;
									}else {
										JSONObject jsonObject = new JSONObject(result);
										newCardID = jsonObject.getInt("d");
										if(newCardID == -1){
											msg.arg1 = Failed;
										}else {					
											msg.arg1 = OK;	
											cv.put("CardID", newCardID);
											String sqlString = "select * from EP_NFCCard where CardTagID = '"+cardTagID+"'";
											Cursor card = db.rawQuery(sqlString,null);
											card.moveToFirst();
											if(card.getCount()<1){
												db.insert("EP_NFCCard", null, cv);
											}else {
												db.update("EP_NFCCard", cv, "CardTagID = '"+cardTagID+"'", null);
											}
											if(selectedCardType == 1){
												ContentValues cvUser = new ContentValues();
												cvUser.put("CardID", newCardID);
												db.update("EP_User", cvUser, "UserID = '"+selectedUserID+"'", null);
											}
											modeFlag = Update;
											card.close();																																			
										}
										modeFlag = Update;
									}
								}else if (modeFlag == Update) {
									msg.what = Update;
									result = HTTPHelper.getInstance(PatrolRegisterCard.this).HTTPClientPost(HTTPHelper.getInstance(PatrolRegisterCard.this).URL_WSPatrol, "UpdateExistNFCCard", object);
									if(result.equals("false")){
										msg.arg1 = netError;
									}else {
										JSONObject jsonObject = new JSONObject(result);
										boolean ok  = jsonObject.getBoolean("d");
										String sql ;
										if(ok){
											sql = "select * from EP_NFCCard where CardTagID = '"+cardTagID+"'";
											Cursor card = db.rawQuery(sql, null);
											card.moveToFirst();										
											if(card.getCount()>0){
												newCardID = card.getInt(card.getColumnIndex("CardID"));				
												db.beginTransaction();
												switch (selectedCardType) {
												case 0:
													db.execSQL("update EP_User set CardID = 0 where CardID = "+newCardID);
													db.execSQL("update EP_PatrolTag set CardID = 0 where CardID = "+newCardID);
													db.update("EP_NFCCard", cv, "CardID = "+newCardID, null);
													break;

												case 1:
													db.execSQL("update EP_User set CardID = 0 where CardID = "+newCardID);
													db.execSQL("update EP_PatrolTag set CardID = 0 where CardID = "+newCardID);
													db.update("EP_NFCCard", cv, "CardID = "+newCardID, null);
													db.execSQL("update EP_User set CardID = "+newCardID+" where UserID = '"+selectedUserID+"'");
													break;
												}
												db.setTransactionSuccessful();
												db.endTransaction();
												msg.arg1 = OK;
											}else {
												msg.arg1 = netError;
											}
											card.close();						
										}else {
											msg.arg1 = Failed;
										}					
									}
									modeFlag = Update;
								}															
							} catch (JSONException e) {
								e.printStackTrace();
								msg.arg1 = netError;
							}finally{
								msg.sendToTarget();
								onclickFlag = 0;
							}					
						}
					});
					threadSubmit.start();
				}else {
					onclickFlag = 0;
				}					
			}				
		}
		*/
		if(v.getId()==R.id.regcard_back){
			onBackPressed();
		}
	}
	
	public void ClearFocus(){
		etCardInfo.clearFocus();
		etCardName.clearFocus();		
	}
	public void hideSoftInput(){
		imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		if(imm.isActive()){
			imm.hideSoftInputFromWindow(ActivityRegisterCard.this.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
		}
		
	}
	
	public void iniConstruction(int plantid){
		getConsList(plantid);
		selectedCons = consInfo.get(0);
		selectedConsID = Integer.parseInt(selectedCons.get("ConstructionID"));
		consPos = 0;
	}
	
	public void getConsList(int plantid){
		sqlStr = "select * from EP_Construction where PlantID = "+plantid;
		Cursor cons = db.rawQuery(sqlStr, null);
		cons.moveToFirst();
		if(cons.getCount()>0){
			HashMap<String, String> map;
			consInfo = new ArrayList<HashMap<String,String>>();
			consNameArr = new String[cons.getCount()];
			while (!cons.isAfterLast()) {
				map = new HashMap<String, String>();
				map.put("ConstructionID", cons.getString(cons.getColumnIndex("ConstructionID")));
				map.put("ConstructionName", cons.getString(cons.getColumnIndex("ConstructionName")));		
				consNameArr[cons.getPosition()] = cons.getString(cons.getColumnIndex("ConstructionName"));
				consInfo.add(map);
				cons.moveToNext();
			}	
		}
		cons.close();
	}
	
	public void iniPlantName(){
		getPlantList();
		selectedPlant = plantInfo.get(0);
		selectedPlantID = Integer.parseInt(selectedPlant.get("PlantID"));
		plantPos = 0;
		
	}
	
	public void getPlantList(){
		sqlStr = "select * from EP_Plant where PlantType = 1";
		Cursor plant = db.rawQuery(sqlStr.toString(), null);
		plant.moveToFirst();
		if(plant.getCount()>0){
			HashMap<String, String> map;
			plantInfo = new ArrayList<HashMap<String,String>>();
			plantNameArr = new String[plant.getCount()];
			while (!plant.isAfterLast()) {
				map = new HashMap<String, String>();
				map.put("PlantID", plant.getString(plant.getColumnIndex("PlantID")));
				map.put("PlantName", plant.getString(plant.getColumnIndex("PlantName")));				
				plantInfo.add(map);
				plantNameArr[plant.getPosition()] = plant.getString(plant.getColumnIndex("PlantName"));
				plant.moveToNext();
			}
		}		
		plant.close();
	}
	
	public void iniUserList(int plantid){
		getUserList(plantid);
		selectedUser = userInfo.get(0);
		selectedUserID = selectedUser.get("UserID");
		userPos = 0;
	}
	public void getUserList(int plantid){
		sqlStr = "select * from EP_User where AccountState = 1 and PlantID = "+plantid;
		Cursor user = db.rawQuery(sqlStr.toString(), null);
		user.moveToFirst();
		if(user.getCount()>0){
			HashMap<String, String> map;
			userInfo = new ArrayList<HashMap<String,String>>();
			userNameArr = new String[user.getCount()];
			while (!user.isAfterLast()) {
				map = new HashMap<String, String>();
				map.put("UserID", user.getString(user.getColumnIndex("UserID")));
				map.put("UserRealName", user.getString(user.getColumnIndex("UserRealName")));				
				userInfo.add(map);
				userNameArr[user.getPosition()] = user.getString(user.getColumnIndex("UserRealName"));
				user.moveToNext();
			}
		}
		user.close();
	}
	
	/**
	 * 检验卡片信息是否完整
	 * @return true or false
	 */
	public boolean verify(){
		boolean ok = false;
		if(selectedCardType == 0){
			if(!etCardName.getText().toString().isEmpty() && !tvCardTagID.getHint().toString().isEmpty()){
				ok = true;
			}else {
				ok = false;
			}
		}else {
			if(!tvCardTagID.getHint().toString().isEmpty()){
				ok = true;
			}else {
				ok = false;
			}
		}
		return ok;
	}
	
	/**
	 * 每次点击button提交前都进行判断,然后进行提交
	 */
	public void verifyAndCommit(final String arg0){
//		switch (onclickFlag) {
//		case 0:
//			onclickFlag = 1;
//			int netWork = SystemMethodUtil.getAPNType(ActivityRegisterCard.this);
//			if(netWork == SystemMethodUtil.NoNetWork || !verify()){
//				Toast.makeText(this,"请检查网络或者卡片信息是否完整", Toast.LENGTH_SHORT).show();
//				modeFlag = Verify;
//				onclickFlag = 0;
//			}else {
//				Toast.makeText(this,"正在提交,请等待提交结果", Toast.LENGTH_SHORT).show();
//				threadVerify = new Thread(new Runnable() {
//					@Override
//					public void run() {
//						JSONObject object1 = new JSONObject();
//						try {
//							object1.put("tagID", arg0);
//							String result1 = HttpUtil.getInstance(ActivityRegisterCard.this).HTTPClientPost(HttpUtil.getInstance(ActivityRegisterCard.this).URL_WSPatrol, "VerifyNFCCardTagID", object1);
//							if(result1.equals("false")){
//								modeFlag = Verify;
//							}else {
//								JSONObject jsonObject = new JSONObject(result1);
//								int count = jsonObject.getInt("d");
//								switch (count) {
//								case -1:
//									modeFlag = Verify;
//									break;
//								case 0:
//									modeFlag = Submit;
//									break;
//								case 1:
//									modeFlag = Update;
//									break;
//								}
//							}
//							Message msg = handler.obtainMessage();
//							JSONObject object = new JSONObject();
//							try {
//								iniParams();
//								object.put("CardTagID", cardTagID);
//								object.put("CardName", etCardName.getText().toString().trim());
//								object.put("CardType", selectedCardType);
//								object.put("ConstructionID", selectedConsID);
//								object.put("Description", etCardInfo.getText().toString().trim());
//								object.put("UserID", selectedUserID);
//								ContentValues cv = new ContentValues();
//								cv.put("CardTagID", cardTagID);
//								cv.put("CardName", etCardName.getText().toString().trim());
//								cv.put("CardType", selectedCardType);
//								cv.put("Description", etCardInfo.getText().toString().trim());
//								cv.put("State", 1);
//								cv.put("ConstructionID", selectedConsID);
//								String result = "";
//								if(modeFlag == Submit){
//									msg.what = Submit;
//									result = HttpUtil.getInstance(ActivityRegisterCard.this).HTTPClientPost(HttpUtil.getInstance(ActivityRegisterCard.this).URL_WSPatrol, "InsertNewNFCCard", object);
//									if(result.equals("false")){
//										msg.arg1 = netError;
//									}else {
//										JSONObject jsonObject = new JSONObject(result);
//										newCardID = jsonObject.getInt("d");
//										if(newCardID == -1){
//											msg.arg1 = Failed;
//										}else {
//											msg.arg1 = OK;
//											cv.put("CardID", newCardID);
//											String sqlString = "select * from EP_NFCCard where CardTagID = '"+cardTagID+"'";
//											Cursor card = db.rawQuery(sqlString,null);
//											card.moveToFirst();
//											if(card.getCount()<1){
//												db.insert("EP_NFCCard", null, cv);
//											}else {
//												db.update("EP_NFCCard", cv, "CardTagID = '"+cardTagID+"'", null);
//											}
//											if(selectedCardType == 1){
//												ContentValues cvUser = new ContentValues();
//												cvUser.put("CardID", newCardID);
//												db.update("EP_User", cvUser, "UserID = '"+selectedUserID+"'", null);
//											}
//											modeFlag = Update;
//											card.close();
//										}
//										modeFlag = Update;
//									}
//								}else if (modeFlag == Update) {
//									msg.what = Update;
//									result = HttpUtil.getInstance(ActivityRegisterCard.this).HTTPClientPost(HttpUtil.getInstance(ActivityRegisterCard.this).URL_WSPatrol, "UpdateExistNFCCard", object);
//									if(result.equals("false")){
//										msg.arg1 = netError;
//									}else {
//										JSONObject jsonObject = new JSONObject(result);
//										boolean ok  = jsonObject.getBoolean("d");
//										String sql ;
//										if(ok){
//											sql = "select * from EP_NFCCard where CardTagID = '"+cardTagID+"'";
//											Cursor card = db.rawQuery(sql, null);
//											card.moveToFirst();
//											if(card.getCount()>0){
//												newCardID = card.getInt(card.getColumnIndex("CardID"));
//												db.beginTransaction();
//												switch (selectedCardType) {
//												case 0:
//													db.execSQL("update EP_User set CardID = 0 where CardID = "+newCardID);
//													db.execSQL("update EP_PatrolTag set CardID = 0 where CardID = "+newCardID);
//													db.update("EP_NFCCard", cv, "CardID = "+newCardID, null);
//													break;
//
//												case 1:
//													db.execSQL("update EP_User set CardID = 0 where CardID = "+newCardID);
//													db.execSQL("update EP_PatrolTag set CardID = 0 where CardID = "+newCardID);
//													db.update("EP_NFCCard", cv, "CardID = "+newCardID, null);
//													db.execSQL("update EP_User set CardID = "+newCardID+" where UserID = '"+selectedUserID+"'");
//													break;
//												}
//												db.setTransactionSuccessful();
//												db.endTransaction();
//												msg.arg1 = OK;
//											}else {
//												msg.arg1 = netError;
//											}
//											card.close();
//										}else {
//											msg.arg1 = Failed;
//										}
//									}
//									modeFlag = Update;
//								}else if(modeFlag == Verify){
//									//继续验证
//									onclickFlag = 0;
//								}
//							} catch (JSONException e) {
//								e.printStackTrace();
//								msg.arg1 = netError;
//							}finally{
//								msg.sendToTarget();
//								onclickFlag = 0;
//							}
//						} catch (JSONException e) {
//							e.printStackTrace();
//						}finally{
//						}
//					}
//				});
//				threadVerify.start();
//			}
//			break;
//		case 1:
//			Toast.makeText(this, "正在提交，请稍候", Toast.LENGTH_SHORT).show();
//			break;
//		}
	}
	
	public EP_NFCCard getCard(String cardTagID){
		EP_NFCCard ep_NFCCard = new EP_NFCCard();
		Cursor ct = db.rawQuery("select * from EP_NFCCard where CardTagID = '"+cardTagID+"'", null);
		ct.moveToFirst();
		if(ct.getCount()>0){
			ep_NFCCard.setCardID(ct.getInt(ct.getColumnIndex("CardID")));
			ep_NFCCard.setCardName(ct.getString(ct.getColumnIndex("CardName")));
			ep_NFCCard.setCardType(ct.getInt(ct.getColumnIndex("CardType")));
			ep_NFCCard.setPlantID(ct.getInt(ct.getColumnIndex("PlantID")));
			ep_NFCCard.setDescription(ct.getString(ct.getColumnIndex("Description")));
			ct.close();
			switch (ep_NFCCard.getCardType()) {
			case 0:
//				Cursor plant = db.rawQuery("select PlantID from EP_Construction where ConstructionID = "+ep_NFCCard.getConstructionID(), null);
//				plant.moveToFirst();
//				ep_NFCCard.setPlantID(plant.getInt(0));
//				plant.close();
				break;
			case 1:
//				Cursor user = db.rawQuery("select UserID from EP_User where CardID = "+ep_NFCCard.getCardID(), null);
//				user.moveToFirst();
//				if(user.getCount()>0){
//					ep_NFCCard.setUserID(user.getString(0));
//					Cursor plant1 = db.rawQuery("select PlantID from EP_User where UserID = '"+ep_NFCCard.getUserID()+"'", null);
//					plant1.moveToFirst();
//					ep_NFCCard.setPlantID(plant1.getInt(0));
//					plant1.close();
//				}else {
//					ep_NFCCard.setUserID("");
//				}			
//				user.close();
				break;
			}		
		}else {
			ep_NFCCard = null;
		}
		return ep_NFCCard;
	}
	
	
	public void iniUpdateLayout(EP_NFCCard ep_NFCCard,boolean editable){
		rlPlantName.setVisibility(View.VISIBLE);
		rlCardType.setVisibility(View.VISIBLE);
		if(ep_NFCCard!=null){
			selectedCardType = ep_NFCCard.getCardType();
			boolean change = selectedPlantID == ep_NFCCard.getPlantID()?true:false;
			selectedPlantID = ep_NFCCard.getPlantID();
			spCardType.setSelection(selectedCardType);	
			switch (selectedCardType) {
			case 0:
				etCardInfo.setText(ep_NFCCard.getDescription());
				etCardName.setText(ep_NFCCard.getCardName());
				if(change){
					for(int i =0;i<consInfo.size();i++){
						if(Integer.valueOf(consInfo.get(i).get("ConstructionID"))==selectedConsID){
							spConstruction.setSelection(i);			
							consPos = i;							
							break;
						}
					}
				}
				break;
			case 1:
//				selectedUserID = ep_NFCCard.getUserID();
				if(change){
					for(int i =0;i<userInfo.size();i++){
						if(userInfo.get(i).get("UserID").equals(selectedUserID)){
							spUserList.setSelection(i);	
							userPos = i;								
							break;
						}
					}
				}
				break;
			}		
			for(int i =0;i<plantInfo.size();i++){
				if(Integer.valueOf(plantInfo.get(i).get("PlantID"))==selectedPlantID){
					spPlantName.setSelection(i);
					plantPos = i;
					break;
				}
			}
			spPlantName.setEnabled(editable);
			spConstruction.setEnabled(editable);
			spCardType.setEnabled(editable);
			spUserList.setEnabled(editable);
			etCardInfo.setEnabled(editable);
			etCardName.setEnabled(editable);
			btSubmit.setVisibility(editable?View.VISIBLE:View.GONE);
		}else {
			iniSubmitLayout(0,true);
			Toast.makeText(ActivityRegisterCard.this, "读取卡片信息失败，请获取服务器最新数据", Toast.LENGTH_SHORT).show();
		}		
		
	}
	
	public void iniSubmitLayout(int cardtype,boolean editable){
		rlPlantName.setVisibility(View.VISIBLE);
		rlCardType.setVisibility(View.VISIBLE);
		spPlantName.setSelection(0);
		spCardType.setSelection(cardtype);
		etCardInfo.setText("");
		etCardName.setText("");
		spConstruction.setSelection(0);			
		spUserList.setSelection(0);
		spPlantName.setEnabled(editable);
		spConstruction.setEnabled(editable);
		spCardType.setEnabled(editable);
		spUserList.setEnabled(editable);
		etCardInfo.setEnabled(editable);
		etCardName.setEnabled(editable);
		btSubmit.setVisibility(editable?View.VISIBLE:View.GONE);
	}
	public void iniParams(){
		selectedCardType = spCardType.getSelectedItemPosition();
		selectedConsID = Integer.valueOf(consInfo.get(spConstruction.getSelectedItemPosition()).get("ConstructionID"));
		selectedPlantID = Integer.valueOf(plantInfo.get(spPlantName.getSelectedItemPosition()).get("PlantID"));
		selectedUserID  = userInfo.get(spUserList.getSelectedItemPosition()).get("UserID");
	}
	
}
