package com.env.utils;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.env.bean.EP_User;


public class SystemParamsUtil {
	
	private EP_User loginUser;
	private boolean isLogin = false;
	
	private	static SystemParamsUtil INSTANCE = new SystemParamsUtil();
		
	public static SystemParamsUtil getInstance() {
		if(INSTANCE == null){
			INSTANCE = new SystemParamsUtil();
		}
		return INSTANCE;
	}
	private SystemParamsUtil(){
		
	}
	
	public void login(EP_User user,Editor editor){
		setLoginUser(loginUser);
		setIsLogin(true);
		LocalDataHelper.saveUserInfoToLocal(user, editor);
	}
	
	public void logout(){
		setLoginUser(null);
		setIsLogin(false);
	}
	
	
	private List<Activity> activityList = new LinkedList<Activity>();
	// 添加Activity到容器中
	public void addActivity(Activity activity) {
		activityList.add(activity);
	}
	public void removeActivity(Activity activity) {
		activityList.remove(activity);
	}

	// 遍历所有Activity并finish
	public void exit() {
		for (Activity activity : activityList) {
			activity.finish();
		}
	}
	
	public void setIsLogin(boolean arg0){
		isLogin = arg0;
	} 
	
	public boolean getIsLogin(){
		return isLogin;
	}
	
	public EP_User getLoginUser(SharedPreferences sp) {
		if(loginUser==null){
			return LocalDataHelper.getUserInfoFromLocal(sp);
		}
		return loginUser;
	}
	
	public void setLoginUser(EP_User loginUser) {
		this.loginUser = loginUser;
	}
	
	
	private String mCurActivityName="";
	
	public void setCurActivityName(String activityName){
		mCurActivityName = activityName;
	}
	public String getCurActivityName(){
		return mCurActivityName;
	}
	
}