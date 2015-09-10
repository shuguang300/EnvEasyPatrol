package com.env.activity;

import java.nio.charset.Charset;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.env.bean.EnumList.AppRightState;
import com.env.component.PatrolApplication;
import com.env.easypatrol.R;
import com.env.nfc.NfcActivity;
import com.env.utils.RemoteDataHelper;
import com.env.utils.SystemMethodUtil;

public class ActivityApplication extends NfcActivity implements OnClickListener{
	private TextView state3,refresh;
	private EditText name,phone,company,aduitinfo,desciption;
	private Button submit,reset;
	private int mState;
	private Thread checkAuditThread;
	private ProgressBar stateBar;
	private String mName,mPhone,mCompany,mAduitinfo,mDescription;
	private SharedPreferences sp;
	private Thread submitThread;
	private Editor editor;
	private boolean isSubmiting = false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.patrolapplication);
		iniData();
		iniView();
	}
	
	/**
	 * 显示上次申请的信息
	 */
	@Override
	public void  iniData(){
		sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
		editor = sp.edit();
		mState = sp.getInt(PatrolApplication.IDENTIFY, 0);
		mName = sp.getString(PatrolApplication.IDENTIFY_NAME, "");
		mPhone = sp.getString(PatrolApplication.IDENTIFY_PHONE, "");
		mCompany = sp.getString(PatrolApplication.IDENTIFY_COMPANY, "");
		mDescription = sp.getString(PatrolApplication.IDENTIFY_DESCRIPTION, "");
		mAduitinfo = sp.getString(PatrolApplication.IDENTIFY_AUDITINFO, "");
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.ACTION_DOWN){
			onBackPressed();
			return true;
		}else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	/**
	 * 从本地数据中读取
	 */
	private void setData(){
		mState = sp.getInt(PatrolApplication.IDENTIFY, AppRightState.ClientApplication.getStae());
		mName = sp.getString(PatrolApplication.IDENTIFY_NAME, "");
		mPhone = sp.getString(PatrolApplication.IDENTIFY_PHONE, "");
		mCompany = sp.getString(PatrolApplication.IDENTIFY_COMPANY, "");
		mDescription = sp.getString(PatrolApplication.IDENTIFY_DESCRIPTION, "");
		mAduitinfo = sp.getString(PatrolApplication.IDENTIFY_AUDITINFO, "");
	}
	
	/**
	 * 初始化控件
	 */
	@Override
	public void iniView(){
		stateBar = (ProgressBar)findViewById(R.id.application_state);
		refresh = (TextView)findViewById(R.id.application_refresh);
		state3 = (TextView)findViewById(R.id.application_state3);
		
		name = (EditText)findViewById(R.id.application_name);
		if(mName!=null&&!mName.isEmpty())name.setText(mName);
		
		phone = (EditText)findViewById(R.id.application_phone);
		if(mPhone!=null&&!mPhone.isEmpty())phone.setText(mPhone);
		
		company = (EditText)findViewById(R.id.application_company);
		if(mCompany!=null&&!mPhone.isEmpty())company.setText(mCompany);
		
		aduitinfo = (EditText)findViewById(R.id.application_auditinformation);
		if(mAduitinfo!=null&&!mAduitinfo.isEmpty())aduitinfo.setText(mAduitinfo);
		
		desciption = (EditText)findViewById(R.id.application_description);
		if(mDescription!=null&&!mDescription.isEmpty()&&mDescription!="null")desciption.setText(mDescription);
		
		submit = (Button)findViewById(R.id.application_submit);
		reset = (Button)findViewById(R.id.application_reset);
		submit.setOnClickListener(this);
		reset.setOnClickListener(this);
		refresh.setOnClickListener(this);
		
		handler.sendEmptyMessage(mState);
	}
	
	/**
	 * 验证，姓名，电话，单位必填项的结果
	 * @return 验证结果
	 */
	private boolean veiryfyResult(){
		mName = name.getText().toString().trim();
		mPhone = phone.getText().toString().trim();
		mCompany = company.getText().toString().trim();
		if(mName.isEmpty()||mPhone.isEmpty()||mCompany.isEmpty()){
			return false;
		}else {
			if(SystemMethodUtil.isMobileNO(mPhone)){
				return true;
			}else {
				phone.requestFocus();
				return false;
			}
		}
	}
	
	/**
	 * 提交前，将变量赋值
	 */
	private void getData(){
		mName = name.getText().toString().trim();
		mPhone = phone.getText().toString().trim();
		mCompany = company.getText().toString().trim();
		mDescription = desciption.getText().toString().trim();
	}
	
	private void setViewData(){
		if(mName!=null&&!mName.isEmpty())name.setText(mName);
		if(mPhone!=null&&!mPhone.isEmpty())phone.setText(mPhone);
		if(mCompany!=null&&!mPhone.isEmpty())company.setText(mCompany);
		if(mAduitinfo!=null&&!mAduitinfo.isEmpty())aduitinfo.setText(mAduitinfo);
		if(mDescription!=null&&!mDescription.isEmpty()&&mDescription!="null")desciption.setText(mDescription);
	}
	/**
	 * 重置已填写的信息
	 */
	private void reset(){
		name.setText("");
		name.setHint(R.string.application_verify_need);
		phone.setText("");
		phone.setHint(R.string.application_verify_need);
		company.setText("");
		company.setHint(R.string.application_verify_need);
		desciption.setText("");
		desciption.setHint(R.string.application_verify_need);
	}
	
	/**
	 * 记录此次申请人的信息
	 */
	private void recordApplicationInfo(){
		if(editor==null){
			editor = sp.edit();
		}
		editor.putInt(PatrolApplication.IDENTIFY, mState);
		editor.putString(PatrolApplication.IDENTIFY_NAME,mName);
		editor.putString(PatrolApplication.IDENTIFY_PHONE,mPhone);
		editor.putString(PatrolApplication.IDENTIFY_COMPANY,mCompany);
		editor.putString(PatrolApplication.IDENTIFY_DESCRIPTION,mDescription);
		editor.putString(PatrolApplication.IDENTIFY_AUDITINFO,mAduitinfo);
		editor.commit();
	}
	
	/**
	 * 设置信息是否可以编辑
	 * @param arg0
	 */
	private void setEditable(boolean arg0){
		name.setEnabled(arg0);
		phone.setEnabled(arg0);
		company.setEnabled(arg0);
		desciption.setEnabled(arg0);
	}
	
	/**
	 * 提交使用软件的申请信息
	 */
	private void submitApplication(){
		submitThread = new Thread(new Runnable() {
			@Override
			public void run() {
				getData();
				isSubmiting = true;
				try {
					Message msg = handler.obtainMessage();
					MultipartEntity me = new MultipartEntity();
					me.addPart("EquipID", new StringBody(SystemMethodUtil.getMacAddress(ActivityApplication.this,sp,editor),Charset.forName("UTF-8")));
					me.addPart("Name", new StringBody(mName,Charset.forName("UTF-8")));
					me.addPart("Tel1", new StringBody(mPhone,Charset.forName("UTF-8")));
					me.addPart("Tel2", new StringBody(SystemMethodUtil.getLocalPhoneNumber(ActivityApplication.this)+"",Charset.forName("UTF-8")));
					me.addPart("WorkUnit", new StringBody(mCompany,Charset.forName("UTF-8")));
					me.addPart("DeviceDescription", new StringBody(mDescription+"",Charset.forName("UTF-8")));
					String result = null;
					switch (mState) {
					case 0:
						result = RemoteDataHelper.setMobile(me, "DeviceApplication");
						break;
					case 1:
						break;
					case 2:
						break;
					case 3:
						result = RemoteDataHelper.setMobile(me, "UpdateDeviceApplication");
						break;
					}
					if(mState==0||mState==3){
						if(!result.equals("false")){
							switch (Integer.parseInt(result)) {
							case 0://客户端申请
								break;
							case 1://服务端审核
								break;
							case 2://审核通过
								break;
							case 3://审核失败
								break;
							}
							msg.what = Integer.parseInt(result);
						}else {
							msg.what = 4;
						}
					}else {
						msg.what = mState;
					}
					editor.putInt(PatrolApplication.IDENTIFY, mState);
					editor.commit();
					msg.sendToTarget();
				}catch (Exception e) {
					e.printStackTrace();
					Message msg = handler.obtainMessage();
					msg.what = 4;
					msg.sendToTarget();
				}finally{
					isSubmiting = false;
				}
			}
		});
		submitThread.start();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.application_submit) {
			if(!isSubmiting){
				if(veiryfyResult()){
					if(SystemMethodUtil.getAPNType(ActivityApplication.this)==SystemMethodUtil.NoNetWork){
						Toast.makeText(ActivityApplication.this, "无网络连接", Toast.LENGTH_SHORT).show();
						isSubmiting = false;
					}else {
						isSubmiting = true;
						Toast.makeText(ActivityApplication.this, "已提交，请等待提交结果", Toast.LENGTH_SHORT).show();
						submitApplication();
						recordApplicationInfo();
					}
				}else {
					Toast.makeText(ActivityApplication.this, "请将信息填写完全以及正确", Toast.LENGTH_SHORT).show();
					isSubmiting = false;
				}
			}
		} else if (id == R.id.application_reset) {
			reset();
		} else if (id == R.id.application_refresh) {
			if(!isSubmiting){
				if(SystemMethodUtil.getAPNType(ActivityApplication.this)==SystemMethodUtil.NoNetWork){
					Toast.makeText(ActivityApplication.this, "无网络连接", Toast.LENGTH_SHORT).show();
					isSubmiting = false;
				}else {
					Toast.makeText(ActivityApplication.this, "正在检测审核结果", Toast.LENGTH_SHORT).show();
					checkAuditThread = new Thread(new Runnable() {
						@Override
						public void run() throws NumberFormatException {
							try {
								isSubmiting = true;
								sp = getSharedPreferences(PatrolApplication.PREFS_NAME, Context.MODE_PRIVATE);
								editor = sp.edit();
								String result = RemoteDataHelper.getMobile(SystemMethodUtil.getMacAddress(ActivityApplication.this,sp,editor));
								if(result.equals("false")){
									handler.sendEmptyMessage(4);
								}else if (result.equals("noright")) {
									editor.putInt(PatrolApplication.IDENTIFY, 0);
									editor.putInt(PatrolApplication.IDENTIFY_PLANT, 0);
									mState = 0;
									handler.sendEmptyMessage(0);
								}else {
									JSONArray jsonArray  = new JSONArray(result);
									JSONObject jsonObject = jsonArray.getJSONObject(0);
									int state = jsonObject.getInt("Audit");
									String desciption = jsonObject.getString("DeviceDescription")==null?"":jsonObject.getString("DeviceDescription"); 
									String auditinfo = jsonObject.getString("AuditInformation")==null?"":jsonObject.getString("AuditInformation"); 
									String name = jsonObject.getString("Name")==null?"":jsonObject.getString("Name"); 
									String company = jsonObject.getString("WorkUnit")==null?"":jsonObject.getString("WorkUnit"); 
									String phone = jsonObject.getString("Tel1")==null?"":jsonObject.getString("Tel1"); 
									switch (state) {
									case 1://正在审核
										editor.putInt(PatrolApplication.IDENTIFY_PLANT, 0);
										break;
									case 2://审核通过
										int plant = 0;
										try {
											plant = Integer.parseInt(jsonObject.getString("PlantID"));
										} catch (Exception e) {
											plant = 0;
										}
										editor.putInt(PatrolApplication.IDENTIFY_PLANT, plant);
										break;
									case 3://审核未通过
										editor.putInt(PatrolApplication.IDENTIFY_PLANT, 0);
										break;
									}
									editor.putString(PatrolApplication.IDENTIFY_DESCRIPTION, desciption);
									editor.putString(PatrolApplication.IDENTIFY_AUDITINFO, auditinfo);
									editor.putString(PatrolApplication.IDENTIFY_NAME, name);
									editor.putString(PatrolApplication.IDENTIFY_COMPANY, company);
									editor.putString(PatrolApplication.IDENTIFY_PHONE, phone);
									editor.putInt(PatrolApplication.IDENTIFY, state);
									mState = state;
									handler.sendEmptyMessage(state);
								}
								boolean ok = editor.commit();
								Log.v("is submit ok ?", ok +"plantid " +sp.getInt(PatrolApplication.IDENTIFY_PLANT, 0));
							} catch (JSONException e) {
								e.printStackTrace();
								handler.sendEmptyMessage(4);
							}finally{
								isSubmiting = false;
							}
						}
					});
					checkAuditThread.start();
				}
			}
		}
	}
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(ActivityApplication.this, "新的申请", Toast.LENGTH_SHORT).show();
				setEditable(true);
				submit.setVisibility(View.VISIBLE);
				reset.setVisibility(View.VISIBLE);
				stateBar.setProgress(10);
				break;
			case 1:
				setEditable(false);
				setData();
				setViewData();
				submit.setVisibility(View.GONE);
				reset.setVisibility(View.GONE);
				stateBar.setProgress(60);
				Toast.makeText(ActivityApplication.this, "已提交成功，请等待人工审核结果", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(ActivityApplication.this, "审核已通过", Toast.LENGTH_SHORT).show();
				setEditable(false);
				submit.setVisibility(View.GONE);
				reset.setVisibility(View.GONE);
				stateBar.setProgress(100);
				state3.setText(R.string.application_state3);
				AlertDialog.Builder builder = new AlertDialog.Builder(ActivityApplication.this);
				builder.setCancelable(false);
				builder.setTitle("恭喜你").setMessage("你已经可以使用本产品");
				builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
				builder.setPositiveButton("进入程序", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
						startActivity(new Intent(ActivityApplication.this,ActivitySplash.class));
					}
				});
				builder.create().show();
				break;
			case 3:
				Toast.makeText(ActivityApplication.this, "审核未通过，请修改信息后重新提交", Toast.LENGTH_SHORT).show();
				setEditable(true);
				setData();
				setViewData();
				submit.setVisibility(View.VISIBLE);
				reset.setVisibility(View.VISIBLE);
				stateBar.setProgress(100);
				state3.setText(R.string.application_state4);
				break;
			case 4:
				Toast.makeText(ActivityApplication.this, "提交失败", Toast.LENGTH_SHORT).show();
				break;
			case 5:
				Toast.makeText(ActivityApplication.this, "检测失败，请检查网络然后重试", Toast.LENGTH_SHORT).show();
				break;
			}
		};
		
		
	};

}
