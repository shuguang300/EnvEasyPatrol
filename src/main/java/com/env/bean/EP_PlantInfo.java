package com.env.bean;

public class EP_PlantInfo {
	private int PlantID;
	private String PlantName;
	private int OrganizationLevel;
	private int PlantType;
	private int FartherPlantID;
	private int FartherOperationalPlantID;
	public int getPlantID() {
		return PlantID;
	}
	public String getPlantName() {
		return PlantName;
	}
	public int getOrganizationLevel() {
		return OrganizationLevel;
	}
	public int getPlantType() {
		return PlantType;
	}
	public int getFartherPlantID() {
		return FartherPlantID;
	}
	public void setPlantID(int plantID) {
		PlantID = plantID;
	}
	public void setPlantName(String plantName) {
		PlantName = plantName;
	}
	public void setOrganizationLevel(int organizationLevel) {
		OrganizationLevel = organizationLevel;
	}
	public void setPlantType(int plantType) {
		PlantType = plantType;
	}
	public void setFartherPlantID(int fartherPlantID) {
		FartherPlantID = fartherPlantID;
	}
	public int getFartherOperationalPlantID() {
		return FartherOperationalPlantID;
	}
	public void setFartherOperationalPlantID(int fartherOperationalPlantID) {
		FartherOperationalPlantID = fartherOperationalPlantID;
	}
}
