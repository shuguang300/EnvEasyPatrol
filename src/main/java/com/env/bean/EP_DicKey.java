package com.env.bean;


public class EP_DicKey {
	private int DicID;
	private String DicName;
	private String DicDescription;
	private int DicType;
	public int getDicID() {
		return DicID;
	}
	public String getDicName() {
		return DicName;
	}
	public String getDicDescription() {
		return DicDescription;
	}
	public void setDicID(int dicID) {
		DicID = dicID;
	}
	public void setDicName(String dicName) {
		DicName = dicName;
	}
	public void setDicDescription(String dicDescription) {
		DicDescription = dicDescription;
	}
	/**
	 * @return the dicType
	 */
	public int getDicType() {
		return DicType;
	}
	/**
	 * @param dicType the dicType to set
	 */
	public void setDicType(int dicType) {
		DicType = dicType;
	}
	
}
