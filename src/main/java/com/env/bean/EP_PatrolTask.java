package com.env.bean;

public class EP_PatrolTask {
	
	public static final String CN_TaskID = "TaskID";
	public static final String CN_PlantID = "PlantID";
	public static final String CN_PatrolTagID = "PatrolTagID";
	public static final String CN_PlanID = "PlanID";
	public static final String CN_IsDone = "IsDone";
	public static final String CN_IsUpload = "IsUpload";
	public static final String CN_DoneUserID = "DoneUserID";
	public static final String CN_StartDateTime = "StartDateTime";
	public static final String CN_EndDateTime = "EndDateTime";
	public static final String CN_SampleTime = "SampleTime";
	public static final String CN_StopDateTime = "StopDateTime";
	public static final String CN_IsRemind = "IsRemind";
	public static final String CN_UploadDateTime = "UploadDateTime";
	public static final String CN_HasRemind = "HasRemind";
	public static final String CN_EnableDelay = "EnableDelay";
	public static final String CN_DeviceID = "DeviceID";
	
	private int TaskID;
	private int PlantID;
	private int PatrolTagID;
	private String DoneUserID;
	private boolean IsDone;
	private boolean IsUpload;
	private String StartDateTime;
	private String EndDateTime;
	private String RemindDateTime;
	private String SampleTime;
	private String StopDateTime;
	private int PlanID;
	private boolean IsRemind;
	private String UploadDateTime;
	private boolean HasRemind;
	private boolean EnableDelay;
	private int DeviceID;
	public int getTaskID() {
		return TaskID;
	}
	public int getPlantID() {
		return PlantID;
	}
	public int getPatrolTagID() {
		return PatrolTagID;
	}
	public String getDoneUserID() {
		return DoneUserID;
	}
	public boolean getIsDone() {
		return IsDone;
	}
	public boolean getIsUpload() {
		return IsUpload;
	}
	public String getStartDateTime() {
		return StartDateTime;
	}
	public String getEndDateTime() {
		return EndDateTime;
	}
	public String getRemindDateTime() {
		return RemindDateTime;
	}
	public String getSampleTime() {
		return SampleTime;
	}
	public String getStopDateTime() {
		return StopDateTime;
	}
	public int getPlanID() {
		return PlanID;
	}
	public boolean getIsRemind() {
		return IsRemind;
	}
	public String getUploadDateTime() {
		return UploadDateTime;
	}
	public boolean getHasRemind() {
		return HasRemind;
	}
	public boolean getEnableDelay() {
		return EnableDelay;
	}
	public int getDeviceID() {
		return DeviceID;
	}
	public void setTaskID(int taskID) {
		TaskID = taskID;
	}
	public void setPlantID(int plantID) {
		PlantID = plantID;
	}
	public void setPatrolTagID(int patrolTagID) {
		PatrolTagID = patrolTagID;
	}
	public void setDoneUserID(String doneUserID) {
		DoneUserID = doneUserID;
	}
	public void setIsDone(boolean isDone) {
		IsDone = isDone;
	}
	public void setIsUpload(boolean isUpload) {
		IsUpload = isUpload;
	}
	public void setStartDateTime(String startDateTime) {
		StartDateTime = startDateTime;
	}
	public void setEndDateTime(String endDateTime) {
		EndDateTime = endDateTime;
	}
	public void setRemindDateTime(String remindDateTime) {
		RemindDateTime = remindDateTime;
	}
	public void setSampleTime(String sampleTime) {
		SampleTime = sampleTime;
	}
	public void setStopDateTime(String stopDateTime) {
		StopDateTime = stopDateTime;
	}
	public void setPlanID(int planID) {
		PlanID = planID;
	}
	public void setIsRemind(boolean isRemind) {
		IsRemind = isRemind;
	}
	public void setUploadDateTime(String uploadDateTime) {
		UploadDateTime = uploadDateTime;
	}
	public void setHasRemind(boolean hasRemind) {
		HasRemind = hasRemind;
	}
	public void setEnableDelay(boolean enableDelay) {
		EnableDelay = enableDelay;
	}
	public void setDeviceID(int deviceID) {
		DeviceID = deviceID;
	}
}
