package com.env.utils;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.env.component.CustomContextWrapper;

public class DataBaseUtil extends SQLiteOpenHelper{
	private static DataBaseUtil INSTANCE = null;
	public static final String DATABASE_NAME = "easypatrol.db";  
    private static final int DATABASE_VERSION = 1;

	private DataBaseUtil(Context context){
		super(new CustomContextWrapper(context, getPath()), DATABASE_NAME, null, DATABASE_VERSION);
	}
	public synchronized static DataBaseUtil getInstance(Context context){		
		if(INSTANCE==null){
			INSTANCE = new DataBaseUtil(context);			
		}
		return INSTANCE;
	}
	
	public static String getPath(){
		StringBuilder sb = new StringBuilder();
		sb.append(Environment.getExternalStorageDirectory().getAbsolutePath()).append(File.separator)
		.append("EnvEasyPatrol").append(File.separator).append("databases");
		return sb.toString();
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {	
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
