package com.env.bean;

import com.google.gson.Gson;


public class EP_DicStep {
	private int DicValueID;
	private int StepValue;
	private String StepText;
	/**
	 * @return the dicValueID
	 */
	public int getDicValueID() {
		return DicValueID;
	}
	/**
	 * @return the stepValue
	 */
	public int getStepValue() {
		return StepValue;
	}
	/**
	 * @return the stepText
	 */
	public String getStepText() {
		return StepText;
	}
	/**
	 * @param dicValueID the dicValueID to set
	 */
	public void setDicValueID(int dicValueID) {
		DicValueID = dicValueID;
	}
	/**
	 * @param stepValue the stepValue to set
	 */
	public void setStepValue(int stepValue) {
		StepValue = stepValue;
	}
	/**
	 * @param stepText the stepText to set
	 */
	public void setStepText(String stepText) {
		StepText = stepText;
	}
	
	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
