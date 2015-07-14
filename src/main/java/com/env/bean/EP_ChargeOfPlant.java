package com.env.bean;

public class EP_ChargeOfPlant {
	private String UserID;
	private int PlantID;
	private int PositionID;
	/**
	 * @return the userID
	 */
	public String getUserID() {
		return UserID;
	}
	/**
	 * @return the plantID
	 */
	public int getPlantID() {
		return PlantID;
	}
	/**
	 * @return the positionID
	 */
	public int getPositionID() {
		return PositionID;
	}
	/**
	 * @param userID the userID to set
	 */
	public void setUserID(String userID) {
		UserID = userID;
	}
	/**
	 * @param plantID the plantID to set
	 */
	public void setPlantID(int plantID) {
		PlantID = plantID;
	}
	/**
	 * @param positionID the positionID to set
	 */
	public void setPositionID(int positionID) {
		PositionID = positionID;
	}
}
