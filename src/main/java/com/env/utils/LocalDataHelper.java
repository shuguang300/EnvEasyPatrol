package com.env.utils;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.env.bean.EP_ChargeOfPlant;
import com.env.bean.EP_Device;
import com.env.bean.EP_DicKey;
import com.env.bean.EP_DicStep;
import com.env.bean.EP_DicValue;
import com.env.bean.EP_NFCCard;
import com.env.bean.EP_PatrolTag;
import com.env.bean.EP_PatrolTag2Plan;
import com.env.bean.EP_PatrolTaskPlan;
import com.env.bean.EP_PlantInfo;
import com.env.bean.EP_Unit;
import com.env.bean.EP_User;
import com.env.component.PatrolApplication;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocalDataHelper {
	
	public static List<EP_NFCCard> getChargeOfCardByUserId(Cursor cursor){
		if(cursor!=null && cursor.getCount()>0){
			List<EP_NFCCard> cards = new ArrayList<EP_NFCCard>();
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				EP_NFCCard card = new EP_NFCCard();
				card.setCardID(cursor.getInt(cursor.getColumnIndex("CardID")));
				cards.add(card);
				cursor.moveToNext();
			}
			cursor.close();
			return cards;
		}
		if(cursor!=null){
			cursor.close();
		}
		return null;
	}
	
	//获取最新任务by userid
	public static ArrayList<HashMap<String, String>> getNewestTaskByUserId(SQLiteDatabase db, String userId ,String startTime,String stopTime){
		Cursor cs = DataCenterUtil.getNewestTaskByUserId(db, userId, startTime, stopTime);
		if(cs!=null && cs.getCount()>0) {
			cs.moveToFirst();
			HashMap<String, String> task;
			ArrayList<HashMap<String, String>> tasks = new ArrayList<HashMap<String, String>>();
			while (!cs.isAfterLast()) {
				task = new HashMap<String, String>();
				for(int i =0;i<cs.getColumnCount();i++){
	 				task.put(cs.getColumnName(i), cs.getString(i));
				}
				tasks.add(task);
				cs.moveToNext();
			}
			cs.close();
			return tasks;
		}
		if(cs!=null){
			cs.close();
		}
		return new ArrayList<HashMap<String, String>>();
	}
	
	public static ArrayList<HashMap<String, String>> getPlantByUserCharge(SQLiteDatabase db,String userId,String dateTime){
		Cursor cs = DataCenterUtil.getPlantByUserCharge(db, userId, dateTime);
		if(cs!=null && cs.getCount()>0) {
			cs.moveToFirst();
			HashMap<String, String> plant;
			ArrayList<HashMap<String, String>> plants = new ArrayList<HashMap<String, String>>();
			while (!cs.isAfterLast()) {
				plant = new HashMap<String, String>();
				for(int i =0;i<cs.getColumnCount();i++){
					plant.put(cs.getColumnName(i), cs.getString(i));
				}
				plants.add(plant);
				cs.moveToNext();
			}
			cs.close();
			return plants;
		}
		if(cs!=null){
			cs.close();
		}
		return new ArrayList<HashMap<String, String>>();
	}
	
	public static ArrayList<HashMap<String, String>> getChargeOfCardByUserId(SQLiteDatabase db,String userId){
		Cursor cs = DataCenterUtil.getChargeOfCardByUserId(db, userId);
		if(cs!=null && cs.getCount()>0) {
			cs.moveToFirst();
			HashMap<String, String> card;
			ArrayList<HashMap<String, String>> cards = new ArrayList<HashMap<String, String>>();
			while (!cs.isAfterLast()) {
				card = new HashMap<String, String>();
				for(int i =0;i<cs.getColumnCount();i++){
					card.put(cs.getColumnName(i), cs.getString(i));
				}
				cards.add(card);
				cs.moveToNext();
			}
			cs.close();
			return cards;
		}
		if(cs!=null){
			cs.close();
		}
		return new ArrayList<HashMap<String, String>>();
	}
	
	public static ArrayList<HashMap<String, String>> getNewestTaskByCard(SQLiteDatabase db,int cardId, String nowTime,String startTime,String stopTime){
		Cursor cs = DataCenterUtil.getNewestTaskByCard(db, cardId, nowTime, startTime, stopTime);
		if(cs!=null && cs.getCount()>0) {
			cs.moveToFirst();
			HashMap<String, String> task;
			ArrayList<HashMap<String, String>> tasks = new ArrayList<HashMap<String, String>>();
			while (!cs.isAfterLast()) {
				task = new HashMap<String, String>();
				for(int i =0;i<cs.getColumnCount();i++){
					task.put(cs.getColumnName(i), cs.getString(i));
				}
				tasks.add(task);
				cs.moveToNext();
			}
			cs.close();
			return tasks;
		}
		if(cs!=null){
			cs.close();
		}
		return new ArrayList<HashMap<String, String>>();
	}

	public static ArrayList<HashMap<String,String>> getNewestTaskByCardAndDeviceGroup(SQLiteDatabase db,int cardId,String nowTime,String startTime,String stopTime,int deviceId){
		Cursor cs = DataCenterUtil.getNewestTaskByCardAndDeviceGroup(db, cardId, nowTime, startTime, stopTime, deviceId);
		if(cs!=null && cs.getCount()>0) {
			cs.moveToFirst();
			HashMap<String, String> task;
			ArrayList<HashMap<String, String>> tasks = new ArrayList<HashMap<String, String>>();
			while (!cs.isAfterLast()) {
				task = new HashMap<String, String>();
				for(int i =0;i<cs.getColumnCount();i++){
					task.put(cs.getColumnName(i), cs.getString(i));
				}
				tasks.add(task);
				cs.moveToNext();
			}
			cs.close();
			return tasks;
		}
		if(cs!=null){
			cs.close();
		}
		return new ArrayList<HashMap<String, String>>();
	}

	public static ArrayList<HashMap<String,String>> getNewestTaskByDevice(SQLiteDatabase db,String nowTime,String startTime,String stopTime,int deviceId){
		Cursor cs = DataCenterUtil.getNewestTaskByDevice(db, nowTime, startTime, stopTime, deviceId);
		if(cs!=null && cs.getCount()>0) {
			cs.moveToFirst();
			HashMap<String, String> task;
			ArrayList<HashMap<String, String>> tasks = new ArrayList<HashMap<String, String>>();
			while (!cs.isAfterLast()) {
				task = new HashMap<String, String>();
				for(int i =0;i<cs.getColumnCount();i++){
					task.put(cs.getColumnName(i), cs.getString(i));
				}
				tasks.add(task);
				cs.moveToNext();
			}
			cs.close();
			return tasks;
		}
		if(cs!=null){
			cs.close();
		}
		return new ArrayList<HashMap<String, String>>();
	}


	
	public static ArrayList<HashMap<String, String>> getHistoryTaskByTag(SQLiteDatabase db,int tagId,String startTime,String stopTime,StringBuilder condition){
		Cursor cs = DataCenterUtil.getHistoryTaskByTag(db, tagId, startTime, stopTime, condition);
		if(cs!=null && cs.getCount()>0) {
			cs.moveToFirst();
			HashMap<String, String> task;
			ArrayList<HashMap<String, String>> tasks = new ArrayList<HashMap<String, String>>();
			while (!cs.isAfterLast()) {
				task = new HashMap<String, String>();
				for(int i =0;i<cs.getColumnCount();i++){
					task.put(cs.getColumnName(i), cs.getString(i));
				}
				tasks.add(task);
				cs.moveToNext();
			}
			cs.close();
			return tasks;
		}
		if(cs!=null){
			cs.close();
		}
		return new ArrayList<HashMap<String, String>>();
	}
	
	public static String [] getTaskValuesAndSteps(SQLiteDatabase db,String dtName,int taskId){
		Cursor cs = DataCenterUtil.getTaskValuesAndSteps(db, taskId, dtName);
		if(cs!=null && cs.getCount()>0){
			cs.moveToFirst();
			String values = "";
			String steps = "";
			while (!cs.isAfterLast()) {
				if(cs.getString(0)==null){
					continue;
				}
				if(cs.isLast()){
					values += cs.getString(0);
					if(dtName.equals("EP_PatrolResult_String")) steps += cs.getString(1);
				}else {
					values += cs.getString(0)+","; 
					if(dtName.equals("EP_PatrolResult_String")) steps += cs.getString(1)+",";
				}				
				cs.moveToNext();
			}
			cs.close();
			String[] result = new String[2];
			result[0] = values;
			result[1] = steps;
			return result;
		}
		if(cs!=null){
			cs.close();
		}
		return new String[]{"",""};
	}
	

	
	
	public static String getTaskDefaultValues(SQLiteDatabase db,String defaultDicIntValues,int dicId){
		Cursor cs = DataCenterUtil.getDefaultValues(db, defaultDicIntValues, dicId);
		if(cs!=null && cs.getCount()>0){
			cs.moveToFirst();
			String values = "";
			while (!cs.isAfterLast()) {
				if(cs.getString(0)==null){
					continue;
				}
				if(cs.isLast()){
					values += cs.getString(0);
				}else {
					values += cs.getString(0)+"\n"; 
				}				
				cs.moveToNext();
			}
			cs.close();
			return values;
		}
		if(cs!=null){
			cs.close();
		}
		return "";
	}
	
	public static List<EP_DicValue> getTaskTextValues(SQLiteDatabase db,int dicId){
		Cursor cs = DataCenterUtil.getTaskTextValues(db, dicId);
		if(cs!=null && cs.getCount()>0){
			List<EP_DicValue> datas = new ArrayList<EP_DicValue>();
			cs.moveToFirst();
			while (!cs.isAfterLast()) {
				EP_DicValue data = new EP_DicValue();
				data.setIntValue(cs.getInt(cs.getColumnIndex("IntValue")));
				data.setDicValueID(cs.getInt(cs.getColumnIndex("DicValueID")));
				data.setTextValue(cs.getString(cs.getColumnIndex("TextValue")));
				cs.moveToNext();
				datas.add(data);
			}
			cs.close();
			return datas;
		}
		if(cs!=null){
			cs.close();
		}
		return new ArrayList<EP_DicValue>();
	}

	public static ArrayList<HashMap<String,String>> getTaskMeddiaByTaskID(SQLiteDatabase db, String taskId){
		Cursor cs = DataCenterUtil.getTaskMeddiaByTaskId(db, taskId);
		if(cs!=null && cs.getCount()>0){
			ArrayList<HashMap<String,String>> datas  = new ArrayList<HashMap<String,String>>();
			cs.moveToFirst();
			while (!cs.isAfterLast()) {
				HashMap <String,String > data = new HashMap<String, String>();
				for(int i =0;i<cs.getColumnCount();i++){
					data.put(cs.getColumnName(i), cs.getString(i));
				}
				datas.add(data);
				cs.moveToNext();
			}
			cs.close();
			return datas;
		}
		if(cs!=null){
			cs.close();
		}
		return new ArrayList<HashMap<String,String>>();
	}
	
	public static List<EP_DicStep> getTaskValueStepByDicValueId(SQLiteDatabase db, String dicValueId){
		Cursor cs = DataCenterUtil.getTaskValueStepByDicValueId(db, dicValueId);
		if(cs!=null && cs.getCount()>0){
			List<EP_DicStep> datas = new ArrayList<EP_DicStep>();
			cs.moveToFirst();
			while (!cs.isAfterLast()) {
				EP_DicStep data = new EP_DicStep();
				data.setDicValueID(cs.getInt(cs.getColumnIndex("DicValueID")));
				data.setStepValue(cs.getInt(cs.getColumnIndex("StepValue")));
				data.setStepText(cs.getString(cs.getColumnIndex("StepText")));
				datas.add(data);
				cs.moveToNext();
			}
			cs.close();
			return datas;
		}
		if(cs!=null){
			cs.close();
		}
		return new ArrayList<EP_DicStep>();
	}
	
	public static String getTaskValueStepByDicValueIdReturnString(SQLiteDatabase db, String dicValueId){
		Cursor cs = DataCenterUtil.getTaskValueStepByDicValueId(db, dicValueId);
		if(cs!=null && cs.getCount()>0){
			String result = "";
			cs.moveToFirst();
			while (!cs.isAfterLast()) {
				if(cs.isLast()) result += cs.getString(cs.getColumnIndex("StepText"));
				else result += cs.getString(cs.getColumnIndex("StepText"))+",";
				cs.moveToNext();
			}
			cs.close();
			return result;
		}
		if(cs!=null){
			cs.close();
		}
		return "";
	}

	public static String getTaskValueSelectedStepsByDicValue(SQLiteDatabase db,String taskId ,String dicValue){
		Cursor cs = DataCenterUtil.getTaskValueSelectedStepsByDicValue(db,taskId, dicValue);
		if(cs!=null && cs.getCount()>0){
			cs.moveToFirst();
			String result = cs.getString(0);
			cs.close();
			return result;
		}
		if(cs!=null){
			cs.close();
		}
		return "";
	}

	
	public static List<EP_DicStep> getTaskStepByDicId(SQLiteDatabase db, int dicId){
		Cursor cs = DataCenterUtil.getTaskStepByDicId(db, dicId);
		if(cs!=null && cs.getCount()>0){
			List<EP_DicStep> datas = new ArrayList<EP_DicStep>();
			cs.moveToFirst();
			while (!cs.isAfterLast()) {
				EP_DicStep data = new EP_DicStep();
				data.setDicValueID(cs.getInt(cs.getColumnIndex("DicValueID")));
				data.setStepValue(cs.getInt(cs.getColumnIndex("StepValue")));
				data.setStepText(cs.getString(cs.getColumnIndex("StepText")));
				datas.add(data);
				cs.moveToNext();
			}
			cs.close();
			return datas;
		}
		if(cs!=null){
			cs.close();
		}
		return new ArrayList<EP_DicStep>();
	}

	
	public static boolean saveUserInfoToLocal(EP_User user, Editor editor){
		editor.putString(PatrolApplication.USER_INFO, user.toString());
		return editor.commit();
	}
	
	public static EP_User getUserInfoFromLocal(SharedPreferences sp){
		return EP_User.fromString(sp.getString(PatrolApplication.USER_INFO, ""));
	}
	
	public static ArrayList<HashMap<String, String>> getPatrolTagByCard(SQLiteDatabase db,int cardId){
		Cursor cs = DataCenterUtil.getPatrolTagByCard(db, cardId);
		if(cs!=null && cs.getCount()>0){
			cs.moveToFirst();
			HashMap<String, String> map;
			ArrayList<HashMap<String, String>> maps = new ArrayList<HashMap<String, String>>();
			while (!cs.isAfterLast()) {
				map = new HashMap<String, String>();
				for(int i =0;i<cs.getColumnCount();i++){
					map.put(cs.getColumnName(i), cs.getString(i));
				}
				maps.add(map);
				cs.moveToNext();
			}
			cs.close();
			return maps;
		}
		if(cs!=null){
			cs.close();
		}
		return new ArrayList<HashMap<String, String>>();
	}
	
	public static String getPlanDescByTag(SQLiteDatabase db,int tagId){
		Cursor cs = DataCenterUtil.getPlanByTag(db, tagId);
		if(cs!=null && cs.getCount()>0){
			cs.moveToFirst();
			StringBuilder values = new StringBuilder();
			while (!cs.isAfterLast()) {
				String value = cs.getString(cs.getColumnIndex("Description"));
				if(value==null){
					continue;
				}
				if(cs.isLast()){
					values.append(cs.getPosition()+1).append(".").append(value);
				}else {
					values.append(cs.getPosition()+1).append(".").append(value).append("\n");
				}
				cs.moveToNext();
			}
			cs.close();
			return values.toString();
		}
		if(cs!=null){
			cs.close();
		}
		return "无巡检计划";
	}
	
	public static String getPlanNameByTag(SQLiteDatabase db,int tagId){
		Cursor cs = DataCenterUtil.getPlanByTag(db, tagId);
		if(cs!=null && cs.getCount()>0){
			cs.moveToFirst();
			StringBuilder values = new StringBuilder();
			while (!cs.isAfterLast()) {
				String value = cs.getString(cs.getColumnIndex("PlanName"));
				if(value==null){
					continue;
				}
				if(cs.isLast()){
					values.append(cs.getPosition()+1).append(".").append(value);
				}else {
					values.append(cs.getPosition()+1).append(".").append(value).append("\n");
				}
				cs.moveToNext();
			}
			cs.close();
			return values.toString();
		}
		if(cs!=null){
			cs.close();
		}
		return "";
	}
	
	public static ArrayList<String> getTipsOfTimeByUser(SQLiteDatabase db, String userId,String startTime){
		Cursor cs = DataCenterUtil.getTipsOfTimeByUser(db, userId, startTime);
		if(cs!=null && cs.getCount()>0){
			cs.moveToFirst();
			ArrayList<String> strs = new ArrayList<String>();
			while (!cs.isAfterLast()) {
				strs.add(cs.getString(0));
				cs.moveToNext();
			}
			cs.close();
			return strs;
		}
		if(cs!=null){
			cs.close();
		}
		return new ArrayList<String>();
	}
	
	
	
	public static ArrayList<ContentValues> updatePlant(List<EP_PlantInfo> plants) {
		ArrayList<ContentValues> cvs = new ArrayList<ContentValues>();
		ContentValues cv;
		EP_PlantInfo plant;
		if(plants==null)return  cvs;
		for (int i = 0; i < plants.size(); i++) {
			cv = new ContentValues();
			plant = plants.get(i);
			cv.put("PlantID", plant.getPlantID());
			cv.put("PlantName", plant.getPlantName());
			cv.put("PlantType", plant.getPlantType());
			cv.put("OrganizationLevel", plant.getOrganizationLevel());
			cv.put("FartherPlantID", plant.getFartherPlantID());
			cv.put("FartherOperationalPlantID", plant.getFartherOperationalPlantID());
			cvs.add(cv);
		}
		return cvs;
	}
	
	public static ArrayList<ContentValues> updateStep(List<EP_DicStep> steps) {
		ArrayList<ContentValues> cvs = new ArrayList<ContentValues>();
		ContentValues cv;
		EP_DicStep step;
		if(steps==null)return  cvs;
		for (int i = 0; i < steps.size(); i++) {
			cv = new ContentValues();
			step = steps.get(i);
			cv.put("DicValueID", step.getDicValueID());
			cv.put("StepValue", step.getStepValue());
			cv.put("StepText", step.getStepText());
			cvs.add(cv);
		}
		return cvs;
	}
	
	

	public static ArrayList<ContentValues> updateChargeOfPlant(List<EP_ChargeOfPlant> chargeOfPlant) {
		ArrayList<ContentValues> cvs = new ArrayList<ContentValues>();
		ContentValues cv;
		EP_ChargeOfPlant obj;
		if(chargeOfPlant==null)return  cvs;
		for (int i = 0; i < chargeOfPlant.size(); i++) {
			cv = new ContentValues();
			obj = chargeOfPlant.get(i);
			cv.put("UserID", obj.getUserID());
			cv.put("PlantID", obj.getPlantID());
			cv.put("PositionID", obj.getPositionID());
			cvs.add(cv);
		}
		return cvs;
	}

	public static ArrayList<ContentValues> updateUnit(List<EP_Unit> units) {
		ArrayList<ContentValues> cvs = new ArrayList<ContentValues>();
		ContentValues cv;
		EP_Unit unit;
		if(units==null)return  cvs;
		for (int i = 0; i < units.size(); i++) {
			cv = new ContentValues();
			unit = units.get(i);
			cv.put("UnitID", unit.getUnitID());
			cv.put("TextWithInfo", unit.getTextWithInfo());
			cv.put("Text", unit.getText());
			cv.put("Pid", unit.getPid());
			cvs.add(cv);
		}
		return cvs;
	}

	public static ArrayList<ContentValues> updateDevice(List<EP_Device> devices) {
		ArrayList<ContentValues> cvs = new ArrayList<ContentValues>();
		ContentValues cv;
		EP_Device device;
		if(devices==null)return  cvs;
		for (int i = 0; i < devices.size(); i++) {
			cv = new ContentValues();
			device = devices.get(i);
			cv.put("PlantID", device.getPlantID());
			cv.put("DeviceID", device.getDeviceID());
			cv.put("FartherDeviceID", device.getFartherDeviceID());
			cv.put("DeviceName", device.getDeviceName());
			cv.put("DeviceClassTypeID", device.getDeviceClassTypeID());
			cv.put("DeviceOrganizationLevel", device.getDeviceOrganizationLevel());
			cvs.add(cv);
		}
		return cvs;
	}

	public static ArrayList<ContentValues> updateTag2Plan(List<EP_PatrolTag2Plan> tag2Plans) {
		ArrayList<ContentValues> cvs = new ArrayList<ContentValues>();
		ContentValues cv;
		EP_PatrolTag2Plan tag2Plan;
		if(tag2Plans==null)return  cvs;
		for (int i = 0; i < tag2Plans.size(); i++) {
			tag2Plan = tag2Plans.get(i);
			cv = new ContentValues();
			cv.put("PlanID", tag2Plan.getPlanID());
			cv.put("PatrolTagID", tag2Plan.getPatrolTagID());
			cvs.add(cv);
		}
		return cvs;
	}

	public static ArrayList<ContentValues> updateNfcCard(List<EP_NFCCard> cards) {
		ArrayList<ContentValues> cvs = new ArrayList<ContentValues>();
		ContentValues cv;
		EP_NFCCard card;
		if(cards==null)return  cvs;
		for (int i = 0; i < cards.size(); i++) {
			card = cards.get(i);
			cv = new ContentValues();
			cv.put("CardID", card.getCardID());
			cv.put("CardTagID", card.getCardTagID());
			cv.put("CardName", card.getCardName());
			cv.put("CardType", card.getCardType());
			cv.put("PlantID", card.getPlantID());
			cv.put("State", card.getState());
			cv.put("Description", card.getDescription());
			cvs.add(cv);
		}
		return cvs;
	}

	public static ArrayList<ContentValues> updateUser(List<EP_User> users) {
		ArrayList<ContentValues> cvs = new ArrayList<ContentValues>();
		ContentValues cv;
		EP_User user;
		if(users==null)return  cvs;
		for (int i = 0; i < users.size(); i++) {
			cv = new ContentValues();
			user = users.get(i);
			cv.put("UserID", user.getUserID());
			cv.put("UserName", user.getUserName());
			cv.put("UserPwd", user.getUserPwd());
			cv.put("RealUserName", user.getRealUserName());
			cv.put("PlantID", user.getPlantID());
			cv.put("AccountState", user.getAccountState());
			cv.put("PositionID", user.getPositionID());
			cv.put("PositionName", user.getPositionName());
			cvs.add(cv);
		}
		return cvs;
	}

	public static ArrayList<ContentValues> updateTag(List<EP_PatrolTag> tags) throws JSONException {
		ArrayList<ContentValues> cvs = new ArrayList<ContentValues>();
		ContentValues cv;
		EP_PatrolTag tag;
		if(tags==null)return  cvs;
		for (int i = 0; i < tags.size(); i++) {
			tag = tags.get(i);
			cv = new ContentValues();
			cv.put("PlantID", tag.getPlantID());
			cv.put("PatrolTagID", tag.getPatrolTagID());
			cv.put("MustInputInfo", tag.getMustInputInfo());
			cv.put("ResultType", tag.getResultType());
			cv.put("DicID", tag.getDicID());
			cv.put("PatrolName", tag.getPatrolName());
			cv.put("UnitID", tag.getUnitID());
			cv.put("IsMustUseNFCCard", tag.getIsMustUseNFCCard());
			cv.put("MaxValue", tag.getMaxValue());
			cv.put("IsMustInput", tag.getIsMustInput());
			cv.put("Description", tag.getDescription());
			cv.put("MinValue", tag.getMinValue());
			cv.put("CardID", tag.getCardID());
			cv.put("DecimalPlaces", tag.getDecimalPlaces());
			cv.put("DefaultDicIntValues", tag.getDefaultDicIntValues());
			cv.put("IsMustUseText", tag.getIsMustUseText());
			cv.put("IsMustUsePic", tag.getIsMustUsePic());
			cv.put("DeviceID", tag.getDeviceID());
			cvs.add(cv);
		}
		return cvs;
	}

	public static ArrayList<ContentValues> updateKey(List<EP_DicKey> keys) throws JSONException {
		ArrayList<ContentValues> cvs = new ArrayList<ContentValues>();
		ContentValues cv;
		EP_DicKey key;
		if(keys==null)return  cvs;
		for (int i = 0; i < keys.size(); i++) {
			key = keys.get(i);
			cv = new ContentValues();
			cv.put("DicID", key.getDicID());
			cv.put("DicType", key.getDicType());
			cv.put("DicName", key.getDicName());
			cv.put("DicDescription", key.getDicDescription());
			cvs.add(cv);
		}
		return cvs;
	}

	public static ArrayList<ContentValues> updateValue(List<EP_DicValue> values) {
		ArrayList<ContentValues> cvs = new ArrayList<ContentValues>();
		ContentValues cv;
		EP_DicValue value;
		if(values==null)return  cvs;
		for (int i = 0; i < values.size(); i++) {
			value = values.get(i);
			cv = new ContentValues();
			cv.put("DicValueID", value.getDicValueID());
			cv.put("DicID", value.getDicID());
			cv.put("IntValue", value.getIntValue());
			cv.put("TextValue", value.getTextValue());
			cvs.add(cv);
		}
		return cvs;
	}
	
	public static boolean updateTaskPlan(List<EP_PatrolTaskPlan> plans, SQLiteDatabase db) {
		if (plans==null || plans.size() == 0) {
			db.delete("EP_PatrolTaskPlan", null, null);
			return false;
		} else {
			ArrayList<ContentValues> cvs = new ArrayList<ContentValues>();
			EP_PatrolTaskPlan plan;
			ContentValues cv;
			for (int i = 0; i < plans.size(); i++) {
				plan = plans.get(i);
				cv = new ContentValues();
				cv.put("ExeOnceAtTime", plan.getExeOnceAtTime());
				cv.put("ExeSpanType", plan.getExeSpanType());
				cv.put("PlanID", plan.getPlanID());
				cv.put("State", plan.getState());
				cv.put("PlanType", plan.getPlanType());
				cv.put("RemindSpan", plan.getRemindSpan());
				cv.put("Description", plan.getDescription());
				if (plan.getEveryDayFrequencyStopTime().equals("00:00:00")) {
					cv.put("EveryDayFrequencyStopTime", "23:59:59");
				} else {
					cv.put("EveryDayFrequencyStopTime", plan.getEveryDayFrequencyStopTime());
				}
				cv.put("RepeatFrequencyType", plan.getRepeatFrequencyType());
				cv.put("ExeSpan", plan.getExeSpan());
				cv.put("RemindSpanUnit", plan.getRemindSpanUnit());
				cv.put("DurationStopDate", plan.getDurationStopDate().replace("-", "").substring(0, 8));
				cv.put("SpanWeek", plan.getSpanWeek());
				cv.put("SpanMonth", plan.getSpanMonth());
				cv.put("IsInUse", plan.getIsInUse());
				cv.put("RepeatFrequencyDay", plan.getRepeatFrequencyDay());
				cv.put("TaskCloseIsInUse", plan.getTaskCloseIsInUse());
				if (plan.getTaskCloseIsInUse()) {
					cv.put("TaskCloseSpanUnit", plan.getTaskCloseSpanUnit());
					cv.put("TaskCloseSpan", plan.getTaskCloseSpan());
				} else {
					cv.put("TaskCloseSpanUnit", 0);
					cv.put("TaskCloseSpan", 0);
				}
				cv.put("EveryDayFrequencyType", plan.getEveryDayFrequencyType());
				cv.put("EveryDayFrequencyStartTime", plan.getEveryDayFrequencyStartTime());
				cv.put("ExecuteOneTimeTheDateTime", plan.getExecuteOneTimeTheDateTime().replace("-", "").replace("T", "").replace(":", ""));
				cv.put("MonthAtDay", plan.getMonthAtDay());
				cv.put("IsRemind", plan.getIsRemind());
				cv.put("DurationStartDate", plan.getDurationStartDate().replace("-", "").substring(0, 8));
				cv.put("PlanName", plan.getPlanName());
				cv.put("Timeout", plan.getTimeout());
				cv.put("TimeoutUnit", plan.getTimeoutUnit());
				cv.put("WeekAt5", plan.getWeekAt5());
				cv.put("WeekAt1", plan.getWeekAt1());
				cv.put("WeekAt4", plan.getWeekAt4());
				cv.put("WeekAt3", plan.getWeekAt3());
				cv.put("WeekAt2", plan.getWeekAt2());
				cv.put("WeekAt7", plan.getWeekAt7());
				cv.put("WeekAt6", plan.getWeekAt6());
				cv.put("DurationStopDateIsEndless", plan.getDurationStopDateIsEndless());
				cv.put("PlantID", plan.getPlantID());
				cv.put("CurVersionID", plan.getVersionID());
				cvs.add(cv);
			}
			int PlanID, CurVersionID, LastVersionID;
			Cursor planCS = null;
			db.beginTransaction();
			for (int i = 0; i < cvs.size(); i++) {
				cv = cvs.get(i);
				PlanID = cv.getAsInteger("PlanID");
				planCS = db.rawQuery("select * from EP_PatrolTaskPlan where PlanID = " + PlanID, null);
				planCS.moveToFirst();
				if (planCS.getCount() < 1) {
					cv.put("LastVersionID", -1);
					db.insert("EP_PatrolTaskPlan", null, cv);
				} else {
					LastVersionID = planCS.getInt(planCS.getColumnIndex("CurVersionID"));
					CurVersionID = cv.getAsInteger("CurVersionID");
					if (LastVersionID < CurVersionID) {
						cv.put("CurVersionID", CurVersionID);
						cv.put("LastVersionID", LastVersionID);
					}
					db.update("EP_PatrolTaskPlan", cv, "PlanID = " + PlanID, null);
				}
				planCS.close();
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			return true;
		}
	}
	
	public static long updateEpResult(SQLiteDatabase db,ContentValues contentValues,HashMap<String, String> task) {
		if(task.get("DataID")!=null && task.get("DataID").length()>0){
			db.update("EP_PatrolResult", contentValues, "DataID = ?", new String[]{task.get("DataID")});
		}else {
			db.insert("EP_PatrolResult", null, contentValues);
		}
		return DataCenterUtil.getDataIdByTask(db, Long.valueOf(task.get("TaskID")));
	}
	
	public static long updateEpResultString(SQLiteDatabase db, ContentValues contentValues,String dataId){
		long row = db.insert("EP_PatrolResult_String", null, contentValues);
		return row;
	}
	
	public static long updateEpTask(SQLiteDatabase db,ContentValues values,String taskId){
		long row = db.update("EP_PatrolTask", values, "TaskID = ?", new String[]{taskId});
		return row;
	}
	
	public static EP_NFCCard getNfcCardByTagId(SQLiteDatabase db,String tagId) {
		Cursor cursor = DataCenterUtil.getNfcCardByTagId(db, tagId);
		if(cursor!=null && cursor.getCount()>0){
			cursor.moveToFirst();
			EP_NFCCard card = new EP_NFCCard();
			card.setCardID(cursor.getInt(cursor.getColumnIndex("CardID")));
			card.setCardTagID(tagId);
			card.setPlantID(cursor.getInt(cursor.getColumnIndex("PlantID")));
			card.setCardName(cursor.getString(cursor.getColumnIndex("CardName")));
			card.setCardType(cursor.getInt(cursor.getColumnIndex("CardType")));
			card.setState(cursor.getInt(cursor.getColumnIndex("State")));
			card.setDescription(cursor.getString(cursor.getColumnIndex("Description")));
			cursor.close();
			return card;
		}
		if(cursor!=null){
			cursor.close();
		}
		return null;
	}
	
	public static EP_NFCCard getNfcCardByCardId(SQLiteDatabase db,int cardId) {
		Cursor cursor = DataCenterUtil.getNfcCardByCardId(db, cardId);
		if(cursor!=null && cursor.getCount()>0){
			cursor.moveToFirst();
			EP_NFCCard card = new EP_NFCCard();
			card.setCardID(cardId);
			card.setCardTagID(cursor.getString(cursor.getColumnIndex("CardTagID")));
			card.setPlantID(cursor.getInt(cursor.getColumnIndex("PlantID")));
			card.setCardName(cursor.getString(cursor.getColumnIndex("CardName")));
			card.setCardType(cursor.getInt(cursor.getColumnIndex("CardType")));
			card.setState(cursor.getInt(cursor.getColumnIndex("State")));
			card.setDescription(cursor.getString(cursor.getColumnIndex("Description")));
			cursor.close();
			return card;
		}
		if(cursor!=null){
			cursor.close();
		}
		return null;
	}

	public static List<HashMap<String,String>> getTaskToUpload(SQLiteDatabase db,String endTime){
		Cursor cursor = DataCenterUtil.getTaskToUpload(db, endTime);
		if(cursor!=null && cursor.getCount()>0){
			cursor.moveToFirst();
			List<HashMap<String,String>> maps = new ArrayList<HashMap<String, String>>();
			while (!cursor.isAfterLast()){
				HashMap<String,String> map = new HashMap<String,String>();
				for (int i =0;i<cursor.getColumnCount();i++){
					map.put(cursor.getColumnName(i),cursor.getString(i));
				}
				maps.add(map);
				cursor.moveToNext();
			}
			cursor.close();
			return maps;
		}
		if(cursor!=null){
			cursor.close();
		}
		return new ArrayList<HashMap<String, String>>();
	}
	public static List<HashMap<String,String>> getTaskMediaToUpload(SQLiteDatabase db,String endTime){
		Cursor cursor = DataCenterUtil.getTaskMediaToUpload(db, endTime);
		if(cursor!=null && cursor.getCount()>0){
			cursor.moveToFirst();
			List<HashMap<String,String>> maps = new ArrayList<HashMap<String, String>>();
			while (!cursor.isAfterLast()){
				HashMap<String,String> map = new HashMap<String,String>();
				for (int i =0;i<cursor.getColumnCount();i++){
					map.put(cursor.getColumnName(i),cursor.getString(i));
				}
				maps.add(map);
				cursor.moveToNext();
			}
			cursor.close();
			return maps;
		}
		if(cursor!=null){
			cursor.close();
		}
		return new ArrayList<HashMap<String, String>>();
	}

	public static List<HashMap<String,String>> getUploadDataValue(SQLiteDatabase db,String dataId,int resultType,String dicId){
		Cursor cursor;
		List<HashMap<String,String>> datas = new ArrayList<HashMap<String,String>>();
		if(resultType == 0){
			cursor = DataCenterUtil.getUploadDataValue(db,dataId,"EP_PatrolResult_Number",dicId);
			if(cursor!=null && cursor.getCount()>0){
				cursor.moveToFirst();
				while (!cursor.isAfterLast()){
					HashMap<String,String> data = new HashMap<String, String>();
					data.put("DValue",cursor.getString(cursor.getColumnIndex("DValue")));
					data.put("UnitID",cursor.getString(cursor.getColumnIndex("UnitID")));
					cursor.moveToNext();
					datas.add(data);
				}
				cursor.close();
				return datas;
			}
		}else{
			cursor = DataCenterUtil.getUploadDataValue(db,dataId,"EP_PatrolResult_String",dicId);
			if(cursor!=null && cursor.getCount()>0){
				cursor.moveToFirst();
				while (!cursor.isAfterLast()){
					HashMap<String,String> data = new HashMap<String, String>();
					data.put("DValue",cursor.getString(cursor.getColumnIndex("IntValue")));
					data.put("DStep",cursor.getString(cursor.getColumnIndex("Steps")));
					cursor.moveToNext();
					datas.add(data);
				}
				cursor.close();
				return datas;
			}
		}
		if(cursor!=null){
			cursor.close();
		}
		return  datas;
	}

	public static List<EP_Device> getDeviceGroupByPlantId(SQLiteDatabase db,String plantId){
		Cursor cursor = DataCenterUtil.getDeviceGroupByPlantId(db,plantId);
		List<EP_Device> datas = new ArrayList<EP_Device>();
		if(cursor!=null && cursor.getCount()>0) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				EP_Device data = new EP_Device();
				data.setDeviceClassTypeID(cursor.getInt(cursor.getColumnIndex("DeviceClassTypeID")));
				data.setDeviceOrganizationLevel(cursor.getInt(cursor.getColumnIndex("DeviceOrganizationLevel")));
				data.setPlantID(cursor.getInt(cursor.getColumnIndex("PlantID")));
				data.setDeviceID(cursor.getInt(cursor.getColumnIndex("DeviceID")));
				data.setFartherDeviceID(cursor.getInt(cursor.getColumnIndex("FartherDeviceID")));
				data.setDeviceName(cursor.getString(cursor.getColumnIndex("DeviceName")));
				datas.add(data);
				cursor.moveToNext();
			}
			cursor.close();
		}
		return datas;
	}

	public static List<EP_Device> getDeviceByCardId(SQLiteDatabase db,String cardId){
		Cursor cursor = DataCenterUtil.getDeviceByCardId(db, cardId);
		if(cursor!=null && cursor.getCount()>0){
			List<EP_Device> datas = new ArrayList<EP_Device>();
			cursor.moveToFirst();
			while (!cursor.isAfterLast()){
				EP_Device data = new EP_Device();
				data.setDeviceClassTypeID(cursor.getInt(cursor.getColumnIndex("DeviceClassTypeID")));
				data.setDeviceOrganizationLevel(cursor.getInt(cursor.getColumnIndex("DeviceOrganizationLevel")));
				data.setPlantID(cursor.getInt(cursor.getColumnIndex("PlantID")));
				data.setDeviceID(cursor.getInt(cursor.getColumnIndex("DeviceID")));
				data.setFartherDeviceID(cursor.getInt(cursor.getColumnIndex("FartherDeviceID")));
				data.setDeviceName(cursor.getString(cursor.getColumnIndex("DeviceName")));
				datas.add(data);
				cursor.moveToNext();
			}
			return  datas;
		}
		if(cursor!=null){
			cursor.close();
		}
		return new ArrayList<EP_Device>();
	}
}
