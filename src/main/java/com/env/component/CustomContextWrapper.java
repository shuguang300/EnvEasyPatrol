package com.env.component;

import java.io.File;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class CustomContextWrapper extends ContextWrapper{
	
	private String mDirPath;

	public CustomContextWrapper(Context base,String dirPath) {
		super(base);
		this.mDirPath = dirPath;
	}
	
	@Override
	public File getDatabasePath(String name) {
		File result = new File(mDirPath+File.separator+name);
		if(!result.exists()){
			result.getParentFile().mkdirs();
		}
		return result;
	}
	
	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory) {
		return super.openOrCreateDatabase(getDatabasePath(name).getAbsolutePath(), mode, factory);
	}
	
	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory, DatabaseErrorHandler errorHandler) {
		return super.openOrCreateDatabase(getDatabasePath(name).getAbsolutePath(), mode, factory, errorHandler);
	}

}
