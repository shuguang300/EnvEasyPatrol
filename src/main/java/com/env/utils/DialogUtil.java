package com.env.utils;

import com.env.component.PatrolApplication;
import com.env.easypatrol.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class DialogUtil {	
	
	/**
	 * @param context 
	 * APN设置的对话框
	 */
	public static void setApnDialog(final Context context){		
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setTitle("网络设置").setMessage("数据更新及上传需要网络连接，检测到您的网络不可用，点击确定进行设置");
		ab.setPositiveButton("设置",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int which) {
				Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
				((Activity) context).startActivityForResult(intent, 0); // //此为设置完成后返回到获取界面
			}
		}).setNegativeButton("取消", null);
		ab.create().show();
	}
	
	/**
	 * @param context
	 * @param exchangeData 确定使用2g/3g网进行数据交互的事件
	 */
	public static void confirmNetWork(final Context context,OnClickListener exchangeData,final Editor editor){
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		View view = LayoutInflater.from(context).inflate(R.layout.dataexchange_tips,null);
		((CheckBox)view.findViewById(R.id.dataexchange_tips_notips)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					editor.putBoolean(PatrolApplication.HAS_DATAEXCHANGE_TIPS, false);
				}else {
					editor.putBoolean(PatrolApplication.HAS_DATAEXCHANGE_TIPS, true);
				}
				editor.commit();
			}
		});
		ab.setTitle("请注意").setView(view);
		ab.setPositiveButton("设置wifi", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
				((Activity) context).startActivityForResult(intent, 0);				
			}
		});
		ab.setNegativeButton("确定",exchangeData);
		ab.create().show();
	}
	
	public static void nfcConfirm(final Context context,OnClickListener exchangeData){
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setCancelable(false);
		ab.setTitle("警告").setMessage("检测到您的NFC设备没有开启，部分功能将无法使用");
		ab.setPositiveButton("设置NFC", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
				((Activity) context).startActivityForResult(intent, 0);	
			}
		});
		ab.setNeutralButton("不再提醒", exchangeData);
		ab.create().show();
	}
	public static void nfcNotIn(final Context context,OnClickListener exchangeData){
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setCancelable(false);
		ab.setTitle("警告").setMessage("您的设备并没有NFC功能，应用的部分功能将无法使用");
		ab.setPositiveButton("不再提醒", exchangeData);
		ab.create().show();
	}	
	public static void confirmDataExchange(final Context context,String message,OnClickListener exchangeData){
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setTitle("请注意！").setMessage(message);
		ab.setPositiveButton("知道了", exchangeData);
		ab.create().show();
	}
}
