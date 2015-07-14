package com.env.bean;

public class EP_DicValue {
	private int DicID;
	private int IntValue;
	private String TextValue;
	private int DicValueID;
	private String Steps;
	private String SelectedSteps;


	public String getSteps() {
		return Steps;
	}
	public void setSteps(String steps) {
		Steps = steps;
	}
	public int getDicID() {
		return DicID;
	}
	public int getIntValue() {
		return IntValue;
	}
	public String getTextValue() {
		return TextValue;
	}
	public int getDicValueID() {
		return DicValueID;
	}
	public void setDicID(int dicID) {
		DicID = dicID;
	}
	public void setIntValue(int intValue) {
		IntValue = intValue;
	}
	public void setTextValue(String textValue) {
		TextValue = textValue;
	}
	public void setDicValueID(int dicValueID) {
		DicValueID = dicValueID;
	}
	public String getSelectedSteps() {
		return SelectedSteps;
	}
	public void setSelectedSteps(String selectedSteps) {
		SelectedSteps = selectedSteps;
	}

}
