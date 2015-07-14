package com.env.bean;

public class EP_Unit {
	private int UnitID;
	private String TextWithInfo;
	private String Text;
	private int Pid;
	public int getUnitID() {
		return UnitID;
	}
	public String getTextWithInfo() {
		return TextWithInfo;
	}
	public String getText() {
		return Text;
	}
	public int getPid() {
		return Pid;
	}
	public void setUnitID(int unitID) {
		UnitID = unitID;
	}
	public void setTextWithInfo(String textWithInfo) {
		TextWithInfo = textWithInfo;
	}
	public void setText(String text) {
		Text = text;
	}
	public void setPid(int pid) {
		Pid = pid;
	}
}
