package com.env.bean;

import com.env.utils.SystemMethodUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class EP_User{
	private String UserID;
	private String RealUserName;
	private String UserPwd;
	private String UserName;
	private String PositionName;
	private String PlantName;
	
	private int PlantID;
	private int AccountState;
	private int PositionID;
	
	private int CardID;
	public String getUserID() {
		return UserID;
	}
	public String getRealUserName() {
		return RealUserName;
	}
	public int getPlantID() {
		return PlantID;
	}
	public void setUserID(String userID) {
		UserID = userID;
	}
	public void setRealUserName(String realUserName) {
		RealUserName = realUserName;
	}
	public void setPlantID(int plantID) {
		PlantID = plantID;
	}
	public int getAccountState() {
		return AccountState;
	}
	public void setAccountState(int accountState) {
		AccountState = accountState;
	}
	public int getCardID() {
		return CardID;
	}
	public void setCardID(int cardID) {
		CardID = cardID;
	}
	public String getUserName() {
		return UserName;
	}
	public void setUserName(String userName) {
		UserName = userName;
	}
	public String getUserPwd() {
		return UserPwd;
	}
	public void setUserPwd(String userPwd) {
		UserPwd = userPwd;
	}
	/**
	 * @return the positionName
	 */
	public String getPositionName() {
		return PositionName;
	}
	/**
	 * @return the positionID
	 */
	public int getPositionID() {
		return PositionID;
	}
	/**
	 * @param positionName the positionName to set
	 */
	public void setPositionName(String positionName) {
		PositionName = positionName;
	}
	/**
	 * @param positionID the positionID to set
	 */
	public void setPositionID(int positionID) {
		PositionID = positionID;
	}
	/**
	 * @return the plantName
	 */
	public String getPlantName() {
		return PlantName;
	}
	/**
	 * @param plantName the plantName to set
	 */
	public void setPlantName(String plantName) {
		PlantName = plantName;
	}
	
	@Override
	public String toString() {
		Gson gson = new GsonBuilder().setDateFormat(SystemMethodUtil.StandardDateTimeSdf).create();
		return gson.toJson(this);
	}
	
	public static EP_User fromString(String userStr){
		Gson gson = new GsonBuilder().setDateFormat(SystemMethodUtil.StandardDateTimeSdf).create();
		try {
			return gson.fromJson(userStr, EP_User.class);
		} catch (Exception e) {
			return null;
		}
		
	}
}
