package com.env.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.env.activity.ActivitySplash;
import com.env.easypatrol.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Administrator
 *
 */
public class SystemMethodUtil {	
	
	public static final String LongDateTimeSdf = "yyyyMMddHHmmss";
	public static final String StandardDateTimeSdf = "yyyy-MM-dd HH:mm:ss";
	public static final String StandardDateSdf = "yyyy-MM-dd";
	public static final String ShortTimeSdf = "HH:mm:ss";
	public static final String ShortTimeCHSdf = "HH'时'mm'分'ss";
	public static final String ShortDateSdf = "yyyyMMdd";
	public static final String ShortDateTimeSdf = "yyyyMMddHH:mm:ss";
	public static final String HttpGetDateTimeSdf = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String IniDateTimeStr = "0001/1/1 0:00:00";
	public static final String EndlessDate = "99991231000000";
	public static final String EndlessJsonDate = "9999-12-31 00:00:00";
	public static final int NoNetWork = -1;
	public static final int WifiNetWork = 0;
	public static final int MobileNetWork = 1;
	
	
	
	/**
	 * 获取当前手机号码
	 * @param context
	 * @return
	 */
	public static String getLocalPhoneNumber(Context context){
		TelephonyManager honeMgr=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		return honeMgr.getLine1Number();
	}
	
	/**
	 * 判断SD卡是否已经准备好
	 * @return
	 */
	public static boolean isSDCardReady(){
		boolean isSDCardReady = true;
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			isSDCardReady = true;
		}else {
			isSDCardReady = false;
		}
		return isSDCardReady;
	}
	
	/**
	 * 判断是否为有效的电话号码
	 * @param mobiles
	 * @return
	 */
	public static boolean isMobileNO(String mobiles){  
		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");  
		Matcher m = p.matcher(mobiles);  
		return m.matches();  
	}
	
	/**
	 * 当sd卡为准备好时弹出提示
	 * @param context
	 */
	public static void SDCardNotReady(Context context){
		Toast toast = ToastUtil.getInstance().getToast(context, ToastUtil.SDCardNotReady);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.show();
	}
	
	/**
	 * 获取当前应用的版本名
	 * @param context
	 * @return
	 */
	public static String getVersionName(Context context){
		String versionName = null ;
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}
	/**
	 * 获取当前应用的版本号
	 * @param context
	 * @return
	 */
	public static int getVersionCode(Context context){
		int versionCode = 0;
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionCode = pi.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}
	

	/**
	 * 获取屏幕状况
	 * @param context
	 * @return
	 */
	public static DisplayMetrics getDisplayMetrics(Context context){
		Resources res = context.getResources();  
		Configuration config=new Configuration();  
		config.setToDefaults();  
		res.updateConfiguration(config,res.getDisplayMetrics());
		return res.getDisplayMetrics();
	}
	
	
	public static String toJsonDateByStr(String datetime){
		StringBuilder sb = new StringBuilder();
		if(datetime.isEmpty()||datetime.equals("null")||datetime==null){
			sb.append(SystemMethodUtil.IniDateTimeStr);
		}else {
			sb.append(datetime.substring(0, 4)).append("-").
			append(datetime.substring(4, 6)).append("-").
			append(datetime.substring(6, 8)).append(" ").
			append(datetime.substring(8, 10)).append(":").
			append(datetime.substring(10, 12)).append(":").
			append(datetime.substring(12, 14));			
		}
		return sb.toString();
	}
	
	public static String ToSqliteShortDate(String dateTime){
		String result = "";
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(ShortDateSdf);
			SimpleDateFormat sdf2 = new SimpleDateFormat(HttpGetDateTimeSdf);
			Date date = sdf2.parse(dateTime);
			result =  sdf.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
	public static String ToSqliteLongDateTime(String dateTime){
		String result = "";
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(LongDateTimeSdf);
			SimpleDateFormat sdf2 = new SimpleDateFormat(HttpGetDateTimeSdf);
			Date date = sdf2.parse(dateTime);
			result =  sdf.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static int StrToInt (String arg0){
		try {
			return Integer.parseInt(arg0);
		} catch (Exception e) {
			return -1;
		}
	}
	
	public static long StrToLong (String arg0){
		try {
			return Long.parseLong(arg0);
		} catch (Exception e) {
			return -1;
		}
	}
	
	public static double StrToDouble (String arg0){
		try {
			return Double.parseDouble(arg0);
		} catch (Exception e) {
			return -1;
		}	
	}
	
	public static String StrToCreateTaskStartDate(String arg0,String today){
		String date;
		if(arg0 == null){
			date = today.substring(0, 8);
		}else {
			if(arg0.toLowerCase().equals("null")||arg0.isEmpty()){
				date = today.substring(0, 8);
			}else {
				date = arg0;
			}
		}
		return date;
	}
	
	public static String jsonDateToShortDate(String jsonDate){
		String tem = jsonDate.replace("/Date(", "").replace(")/", "");		
		Date date = new Date(Long.valueOf(tem));
		SimpleDateFormat sdf = new SimpleDateFormat(ShortDateSdf);
		return sdf.format(date);
	}
	public static String jsonDateToLongDateTime(String jsonDate){
		String tem = jsonDate.replace("/Date(", "").replace(")/", "");		
		Date date = new Date(Long.valueOf(tem));
		SimpleDateFormat sdf = new SimpleDateFormat(LongDateTimeSdf);
		return sdf.format(date);
	}
	
	public static Bitmap changeURItoBitmap(Uri uri,Context context){		
		ContentResolver cr = context.getContentResolver();
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return bitmap;		
	}
	public static Uri changeBitmaptoURI(Bitmap bitmap,Context context){
		ContentResolver cr = context.getContentResolver();
		Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(cr, bitmap, null, null));
		return uri;
	}
	public static String changeURItoPath(Uri uri,Context context){		
		String bitmapPath=null;
		String [] object = {MediaStore.Images.Media.DATA};
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(uri, object, null, null, null);
		cursor.moveToFirst();	
		bitmapPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
		cursor.close();
		return bitmapPath;
	}
	public static Bitmap getBitmapByPath(String path){
		Bitmap bitmap = BitmapFactory.decodeFile(path);
		return bitmap;
	}
	public static Bitmap compressBitmap(String imgPath){
		Bitmap bitmap = null;		
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		bitmap = BitmapFactory.decodeFile(imgPath,opts);
		int w = opts.outWidth;  
        int h = opts.outHeight; 
		opts.inJustDecodeBounds= false;		 
		float hh = 800f;//这里设置高度为800f   2048 * 1536
        float ww = 480f;//这里设置宽度为480f  
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可  
        int be = 1;//be=1表示不缩放  
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放  
            be = (int) (opts.outWidth / ww);  
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放  
            be = (int) (opts.outHeight / hh);  
        }  
        if (be <= 0)  
            be = 1;  
        opts.inSampleSize = be;//设置缩放比例  
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了  
        bitmap = BitmapFactory.decodeFile(imgPath, opts); 
		return bitmap;
	}


	public static Bitmap compressBitmap(String imgPath,float width,float height){
		Bitmap bitmap = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		bitmap = BitmapFactory.decodeFile(imgPath,opts);
		int w = opts.outWidth;
		int h = opts.outHeight;
		opts.inJustDecodeBounds= false;
		float hh = width;//这里设置高度为800f   2048 * 1536
		float ww = height;//这里设置宽度为480f
		//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;//be=1表示不缩放
		if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
			be = (int) (opts.outWidth / ww);
		} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
			be = (int) (opts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		opts.inSampleSize = be;//设置缩放比例
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(imgPath, opts);
		return bitmap;
	}
	
	public static String BitmapToStr(Bitmap bitmap){
		String temp = "";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();;
		if(bitmap==null){
			return temp;
		}else {
			int options = 100;
			bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
			while (baos.size()>50*1024) {
				baos.reset();
				options = options -10;
				bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
			}
			try {
				baos.flush();
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		temp = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
		return temp;
	}
	
	public static String FileToBase64Str(File file){
		StringBuffer base64Str = new StringBuffer();
		if (file == null||!file.exists()){  
            return base64Str.toString();  
        }  
        try{  
            FileInputStream stream = new FileInputStream(file);
            ByteArrayOutputStream temp = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            while (stream.read(buffer)!=-1) {
				temp.write(buffer);
			}
            temp.flush();
            temp.close();
            stream.close();
            base64Str.append(Base64.encodeToString(temp.toByteArray(), Base64.DEFAULT));
            return  base64Str.toString();
        } catch (IOException e){  
            e.printStackTrace();  
        }  
        return base64Str.toString();  
	}
	
	
	public static boolean checkWifi(Context context){
		boolean iswifiConnect = false;
		ConnectivityManager cManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo [] networkInfos = cManager.getAllNetworkInfo();
		for(int i=0;i<networkInfos.length;i++){
			if(networkInfos[i].getState() == NetworkInfo.State.CONNECTED){
				if(networkInfos[i].getType() == ConnectivityManager.TYPE_MOBILE){
					iswifiConnect = false;
				}else if (networkInfos[i].getType() == ConnectivityManager.TYPE_WIFI) {
					iswifiConnect = true;
				}
			}
		}
		return iswifiConnect;
	}
	
	public static String getIMEIString (Context context){
		String mime ="";
		try {
			TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			mime = telephonyManager.getDeviceId();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mime;
	}
	
	public static String getMacAddress(Context context){
		String macAddrString="";
		WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		macAddrString = wifiInfo.getMacAddress();
		return macAddrString.replace(":", "");
	}
	
	public static int getAPNType(Context context){
		ConnectivityManager cManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cManager.getActiveNetworkInfo();
		if(networkInfo == null){
			return NoNetWork;
		}else {
		//	NetworkInfo [] networkInfos = cManager.getAllNetworkInfo();
			if(networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
				return MobileNetWork;
			}else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
				return WifiNetWork;
			}else {
				return NoNetWork;
			}
		}		
	}
	
	public static Calendar getRealStartTime(Calendar nowDateTime,Calendar startDateTime,Calendar stopDateTime,int ExeSpanType,int ExeSpan){
		Calendar realStartTime = Calendar.getInstance();
		realStartTime.setTime(startDateTime.getTime());
		if(nowDateTime.getTime().getTime()<=startDateTime.getTime().getTime()){
			realStartTime.setTime(startDateTime.getTime());
		}else if(nowDateTime.getTime().getTime()<=stopDateTime.getTime().getTime()&&nowDateTime.getTime().getTime()>=startDateTime.getTime().getTime()){
			Calendar st = Calendar.getInstance();
			st.setTime(startDateTime.getTime());
			Calendar end = Calendar.getInstance();
			while (st.getTime().getTime()<=nowDateTime.getTime().getTime()) {
				if(ExeSpanType==0){					
					long miles = st.getTime().getTime() + ExeSpan*1000*60;
					end.setTime(new Date(miles));
					if(nowDateTime.after(st)&&nowDateTime.before(end)){
						realStartTime.setTime(st.getTime());
					}else if (nowDateTime.after(stopDateTime)) {
						realStartTime.setTime(stopDateTime.getTime());
					}
					st.setTime(end.getTime());
				}else {
					long miles = st.getTime().getTime() + ExeSpan*1000*3600;
					end.setTime(new Date(miles));
					if(nowDateTime.after(st)&&nowDateTime.before(end)){
						realStartTime.setTime(st.getTime());
					}else if (nowDateTime.after(stopDateTime)) {
						realStartTime.setTime(stopDateTime.getTime());
					}
					st.setTime(end.getTime());
				}			
			}
		}else if(nowDateTime.getTime().getTime()>stopDateTime.getTime().getTime()){
			realStartTime.setTime(stopDateTime.getTime());
		}		
		return realStartTime;
	}
	
	public static void installAPK(File file,Context context){
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
		context.startActivity(intent);
	}
	
	public static boolean isServiceRuning(Context context,String className){
		ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> serviceInfos = activityManager.getRunningServices(99);
		for(int i=0;i<serviceInfos.size();i++){
			if(className.equals(serviceInfos.get(i).service.getClassName())){
				return true;
			}
		}		
		return false;
	}
	
	public static void foreceStopProcess(Context context){
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		am.killBackgroundProcesses(context.getPackageName());
	}
	
	public static boolean verifyIP(String ipaddr){
		boolean ok = false;
		Pattern pattern = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
		Matcher matcher = pattern.matcher(ipaddr);
		if(matcher.matches()){
			ok=true;
		}else {
			ok=false;
		}
		return ok;
	}
	
//	public static void copyDB(Context context) {
//		String dataBaseFolder = "/data/data/" + context.getPackageName() + "/databases";
//		String dataBasePath = dataBaseFolder + "/" + "easypatrol.db";
//		File dir = new File(dataBaseFolder);
//		if (!dir.exists()) {
//			dir.mkdirs();
//		}
//		
//		try {
//			File file = new File(dataBasePath);
//			if(file.exists()){
//				
//			}else {
//				InputStream is = context.getResources().openRawResource(R.raw.easypatrol);
//				FileOutputStream fos = new FileOutputStream(file);
//				byte[] buffer = new byte[8192];
//				int count = 0;
//				while ((count = is.read(buffer)) > 0) {
//					fos.write(buffer, 0, count);
//				}
//				is.close();
//				fos.flush();
//				fos.close();
//			}
//			
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}			
//	}
	
	/**
	 * 将数据库文件复制到sd卡上。
	 * @param context
	 * @param path
	 * @return
	 */
	public static boolean copyDB(Context context,String path){
		boolean temp = false;
		File file = new File(path);
		if(!file.exists()){
			temp = file.getParentFile().mkdirs();
			InputStream is = context.getResources().openRawResource(R.raw.easypatrol);
			try {
				byte [] tempByte = new byte[is.available()];
				FileOutputStream fos = new FileOutputStream(file);
				int len =0 ;
				while ((len=is.read(tempByte))>0) {
					fos.write(tempByte, 0, len);
				}
				fos.flush();
				fos.close();
				is.close();
				temp = true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				temp = false;
			} catch (IOException e) {
				e.printStackTrace();
				temp = false;
			}				
		}else {
			temp = true;
		}
		return temp;
	}
	
	
	/**
	 * 判断桌面是否已经存在快捷方式
	 * @param context
	 * @return
	 */
	public static boolean hasShortcut(Context context){
		boolean result = false;
		String title = null;
		try {
	        final PackageManager pm = context.getPackageManager();
	        title = pm.getApplicationLabel(pm.getApplicationInfo(context.getPackageName(),PackageManager.GET_META_DATA)).toString();
	    } catch (Exception e) {
	    	
	    }
		String uriStr="content://"+getAuthorityFromPermission(context,"com.android.launcher.permission.READ_SETTINGS")+"/favorites?notify=true";
//		String uriStr = null;
//		if (android.os.Build.VERSION.SDK_INT < 8) {
//	        uriStr = "content://com.android.launcher.settings/favorites?notify=true";
//	    } else {
//	        uriStr = "content://com.android.launcher2.settings/favorites?notify=true";
//	    }
	    Uri CONTENT_URI = Uri.parse(uriStr);
	    Cursor c = context.getContentResolver().query(CONTENT_URI, null,"title=?", new String[] { title }, null);
	    if (c != null) {
	    	if(c.getCount() > 0){
	    		result = true;
	    	}else {
	    		result = false;
			}	        
	        c.close();
	    }	    
		return result;
	}
	
	static String getAuthorityFromPermission(Context context, String permission){
		String authority = "";
	    if (permission == null || permission.isEmpty()) return authority;
	    List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
	    if (packs != null) {
	        for (PackageInfo pack : packs) { 
	            ProviderInfo[] providers = pack.providers; 
	            if (providers != null) { 
	                for (ProviderInfo provider : providers) { 
	                    if (permission.equals(provider.readPermission)){
	                    	authority = provider.authority;
	                    	return authority;
	                    }
	                    if (permission.equals(provider.writePermission)){
	                    	authority = provider.authority;
	                    	return authority;
	                    }
	                } 
	            }
	        }
	    }
	    return authority;
	}
	
	/**
	 * 添加快捷方式到桌面
	 * @param context
	 */
	public static void addShortcut(Context context){
		/*
		注：Intent intent2 = new Intent(Intent.ACTION_MAIN); 
		这个也可以换成的构造参数也可以是Intent.ACTION_CREATE_SHORTCUT，
		也可以生成快捷方式图标，但是这样不标准，在删除的时候如果不和这个对于相同则无法删除。
		所以还是用Intent.ACTION_MAIN。
		*/
		Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");		
	//	Intent shortcIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
		Intent shortcIntent = new Intent(Intent.ACTION_MAIN);
		shortcIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		shortcIntent.setClass(context, ActivitySplash.class);
		shortcIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);	
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcIntent);
		String title = null;
		final PackageManager pm = context.getPackageManager();
		try {
			title = pm.getApplicationLabel(pm.getApplicationInfo(context.getPackageName(),PackageManager.GET_META_DATA)).toString();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		// 快捷方式名称
	    shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
	    // 不允许重复创建（不一定有效）
	    shortcut.putExtra("duplicate", false);
	 // 快捷方式的图标
	    Parcelable iconResource = Intent.ShortcutIconResource.fromContext(context,R.drawable.logo);
	    shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
	//    ((Activity)context).setResult(Activity.RESULT_CANCELED, shortcIntent);
	    context.sendBroadcast(shortcut);
				
	}
	
	/**
	 * 删除快捷方式
	 * @param context
	 */
	public static void deleteShortcut(Context context){
		Intent shortcut = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
	    // 获取当前应用名称
	    String title = null;
	    try {
	        final PackageManager pm = context.getPackageManager();
	        title = pm.getApplicationLabel(pm.getApplicationInfo(context.getPackageName(),PackageManager.GET_META_DATA)).toString();
	    } catch (Exception e) {
	    }
	    // 快捷方式名称
	    shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
	    Intent shortcutIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
	    shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
	    context.sendBroadcast(shortcut);
	}

	/**
	 * 备份数据库
	 * @param context
	 * @param fileName
	 */
//	public static void backupDB(Context context,String fileName){
//		File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/EnvEasyPatrol/Backup");
//		if (!folder.exists()) {
//			folder.mkdirs();
//		} 
//		try {
//			FileInputStream fis = new FileInputStream(new File("/data/data/" + context.getPackageName()+ "/databases/easypatrol.db"));
//			FileOutputStream fos = new FileOutputStream(new File(folder.getAbsolutePath()+"/"+fileName));
//			byte[] buffer = new byte[8192];
//			int count = 0;
//			while ((count = fis.read(buffer)) > 0) {
//				fos.write(buffer, 0, count);
//			}
//			fis.close();
//			fos.flush();
//			fos.close();
//			Toast.makeText(context, "成功备份"+folder.getAbsolutePath()+"/"+fileName, Toast.LENGTH_SHORT).show();
//		} catch (FileNotFoundException e) {
//			Toast.makeText(context, "备份失败", Toast.LENGTH_SHORT).show();
//			e.printStackTrace();
//		} catch (IOException e) {
//			Toast.makeText(context, "备份失败", Toast.LENGTH_SHORT).show();
//			e.printStackTrace();
//		}	
//		
//	}
	
	/**
	 * 数据更新成功后，发送广播告知前台界面进行更新
	 */
	public static void sendBroadCast(Context context) {
		Intent intent = new Intent();
		intent.setAction("com.env.view.PatrolTaskNFCCard.TaskNFCCardReceiver");
		context.sendBroadcast(intent);
		intent = new Intent();
		intent.setAction("com.env.view.PatrolTaskConstruction.PatrolConsReceiver");
		intent.putExtra("action", "DataService");
		context.sendBroadcast(intent);
	}

	/**
	 * 安装下载好的更新文件
	 * @param context
	 * @param file
	 */
	public static void installAPK(Context context,File file){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

}
