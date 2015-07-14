package com.env.widget;

import com.env.activity.ActivitySplash;
import com.env.easypatrol.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class AppWidget extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		final int N = appWidgetIds.length;
		Log.v("onUpdate", N+"**************************************");
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setClass(context, ActivitySplash.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,intent, 0);
			RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.appwidget);
			views.setOnClickPendingIntent(R.id.update, pendingIntent);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		Log.v("onDeleted", "**************************************");
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.v("onEnabled", "**************************************");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.v("onReceive", "**************************************");
	}
}
