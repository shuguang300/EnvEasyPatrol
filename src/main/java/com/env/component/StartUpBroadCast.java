package com.env.component;
import com.env.activity.UpdateSystem;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartUpBroadCast extends BroadcastReceiver{
	private Intent dateService,keepDataService,updateSystem;
	private AlarmManager alarmManager;
	private PendingIntent pendingIntent;
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals("com.env.component.CustomBroadCast")){
			updateSystem = new Intent();
			updateSystem.setClass(context, UpdateSystem.class);
			updateSystem.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(updateSystem);
		}else {
			dateService = new Intent();
			dateService.setAction("com.env.component.DataService");
			dateService.setPackage(context.getPackageName());
			context.startService(dateService);
			keepDataService = new Intent();
			keepDataService.setAction("com.env.component.TaskService");
			keepDataService.setPackage(context.getPackageName());
//			context.startService(keepDataService);
			alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			pendingIntent = PendingIntent.getService(context, 0, keepDataService, 0);
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, 5*60*1000, pendingIntent);		
		}
		
	}
}
