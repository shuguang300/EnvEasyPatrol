package com.env.utils;

import com.env.easypatrol.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

public class ToastUtil {
	public static final int SDCardNotReady = 1;
	private Toast toast;
	private View view;
	private static ToastUtil INSTANCE = new ToastUtil();
	private ToastUtil(){
		
	}
	public static ToastUtil getInstance(){
		if(INSTANCE == null){
			INSTANCE = new ToastUtil();
		}
		return INSTANCE;
	}
	
	public Toast getToast(Context context,int toastKind) {
		toast = new Toast(context);
		
		switch (toastKind) {
		case SDCardNotReady:
			view = LayoutInflater.from(context).inflate(R.layout.sdcard_toast, null);
			toast.setView(view);
			break;
		}
		return toast;
	}
	
}
