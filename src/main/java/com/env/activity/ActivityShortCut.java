package com.env.activity;

import com.env.easypatrol.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

public class ActivityShortCut extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent addShortCut = new Intent();
		addShortCut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.system_appname));
		Parcelable icon = Intent.ShortcutIconResource.fromContext(this,R.drawable.logo);
//		addShortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
		addShortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		Intent startIntent = new Intent(Intent.ACTION_MAIN);
		startIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		startIntent.setClass(this, ActivitySplash.class);
		startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);	
		addShortCut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, startIntent);
		setResult(RESULT_OK, addShortCut);
		finish();
	}
}
