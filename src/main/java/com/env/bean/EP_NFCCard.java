package com.env.bean;

public class EP_NFCCard {
	private int CardID;
	private String CardTagID;
	private String CardName;
	private int CardType;
	private int PlantID;
	private int State;
	private String Description;
	
	public int getCardID() {
		return CardID;
	}
	public String getCardTagID() {
		return CardTagID;
	}
	public String getCardName() {
		return CardName;
	}
	public int getCardType() {
		return CardType;
	}
	public int getPlantID() {
		return PlantID;
	}
	public int getState() {
		return State;
	}
	public String getDescription() {
		return Description;
	}
	public void setCardID(int cardID) {
		CardID = cardID;
	}
	public void setCardTagID(String cardTagID) {
		CardTagID = cardTagID;
	}
	public void setCardName(String cardName) {
		CardName = cardName;
	}
	public void setCardType(int cardType) {
		CardType = cardType;
	}
	public void setPlantID(int plantID) {
		PlantID = plantID;
	}
	public void setState(int state) {
		State = state;
	}
	public void setDescription(String description) {
		Description = description;
	}
}
