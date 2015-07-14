package com.env.utils;
import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.env.bean.EP_User;
import com.env.bean.EnumList.AccountState;

public class DataCenterUtil {
	/**
	 * 通过用户名获取用户id
	 * @param userName
	 * @param db
	 * @return
	 */
	public static String getUserIDByName(String userName,SQLiteDatabase db){
		String userID = "";
		Cursor cs = db.rawQuery("select * from EP_User where RealUserName = '"+userName+"'", null);
		if(cs!=null&&cs.getCount()>0){
			cs.moveToFirst();
			userID = cs.getString(cs.getColumnIndex("UserID"));
		}
		if(cs!=null)cs.close();
		return userID;
	}
	
	public static String [] parseUserListToArray(ArrayList<EP_User> users){
		String [] arrays = new String [users.size()];
		for(int i =0;i<users.size();i++){
			arrays[i] = users.get(i).getRealUserName();
		}
		return arrays;
	}
	
	public static EP_User parseUserToBean (Cursor cs){
		EP_User user = new EP_User();
		user.setAccountState(cs.getInt(cs.getColumnIndex("AccountState")));
		user.setCardID(cs.getInt(cs.getColumnIndex("CardID")));
		user.setPlantID(cs.getInt(cs.getColumnIndex("PlantID")));
		user.setUserID(cs.getString(cs.getColumnIndex("UserID")));
		user.setUserName(cs.getString(cs.getColumnIndex("UserName")));
		user.setUserPwd(cs.getString(cs.getColumnIndex("UserPwd")));
		user.setUserPwd(cs.getString(cs.getColumnIndex("RealUserName")));
		return user;
	}
	
	public static EP_User getUserInfoByID(String userID,SQLiteDatabase db){
		String sql = "select * from EP_User where UserID = '" + userID +"'";
		EP_User user = null ;
		Cursor cs = null;
		try {
			cs = db.rawQuery(sql, null);
			if(cs.getCount()>0){
				cs.moveToFirst();
				user = parseUserToBean(cs);
			}
		} catch (Exception e) {
			user = null;
		} finally {
			if(cs!=null)cs.close();
		}
		return user;
	}
	
	public static ArrayList<EP_User> getUserList(Integer plantID,SQLiteDatabase db){
		StringBuilder sql = new StringBuilder();
		sql.append("select * from EP_User where AccountState = "+AccountState.StateTrue.getState()+" and UserRoleName = '厂级巡检员'");
		if(plantID != null){
			sql.append(" and PlantID = ").append(plantID);
		}
		ArrayList<EP_User> users = null;
		EP_User user ;
		Cursor cs = null;
		try {
			cs = db.rawQuery(sql.toString(), null);
			if(cs.getCount()>0){
				cs.moveToFirst();
				users = new ArrayList<EP_User>();
				while(!cs.isAfterLast()){
					user = parseUserToBean(cs);
					users.add(user);
					cs.moveToNext();
				}
			}
		} catch (Exception e) {
			users = null;
		}finally{
			if(cs!=null)cs.close();
		}
		return users;
	}
	
	
	/**
	 * 通过用户id获取用户名
	 * @param userID
	 * @param db
	 * @return
	 */
	public static String getUserNameByID(String userID,SQLiteDatabase db){
		String userName = "";
		Cursor cs = db.rawQuery("select * from EP_User where UserID = '"+userID+"'", null);
		if(cs!=null&&cs.getCount()>0){
			cs.moveToFirst();
			userName = cs.getString(cs.getColumnIndex("UserRealName"));
		}else {
		}
		if(cs!=null)cs.close();
		return userName;
	}
	
	/**
	 * 通过userid获取用户密码
	 * @param id
	 * @param db
	 * @return
	 */
	public static String getUserPswByUserID(String id,SQLiteDatabase db){
		String temp = "false";
		Cursor psw = db.rawQuery("select UserPwd from EP_User where UserID = '"+id+"'", null);
		psw.moveToFirst();
		if(psw.getCount()>0){
			String result = psw.getString(0);
			if(result.toLowerCase().equals("null")||result.isEmpty()||result==null){
				temp = "false";
			}else {
				temp = result;
			}
		}else {
			temp = "false";
		}		
		psw.close();
		return temp;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	 * 创建日期：2015-06-03
	 *  二次供水部分 
	 */
	
	/**
	 * 
	 * 用户登录
	 * @param name
	 * @param pwd
	 * @param context
	 * @return
	 */
	public static EP_User login(String name,String pwd,SQLiteDatabase db){
		if(db!=null && db.isOpen()){
			Cursor cs = null;
			try {
				cs = db.rawQuery("select a.*,b.PlantName from EP_User a inner join EP_PlantInfo b on a.PlantID = b.PlantID where PositionID=4 and UserName=? and UserPwd=?",new String[]{name,pwd});
				if(cs!=null && cs.getCount()>0){
					cs.moveToFirst();
					EP_User user = new EP_User();
					user.setUserName(name);
					user.setUserPwd(pwd);
					user.setUserID(cs.getString(cs.getColumnIndex("UserID")));
					user.setRealUserName(cs.getString(cs.getColumnIndex("RealUserName")));
					user.setPositionName(cs.getString(cs.getColumnIndex("PositionName")));
					user.setPositionID(cs.getInt(cs.getColumnIndex("PositionID")));
					user.setPlantID(cs.getInt(cs.getColumnIndex("PlantID")));
					user.setAccountState(cs.getInt(cs.getColumnIndex("AccountState")));
					user.setPlantName(cs.getString(cs.getColumnIndex("PlantName")));
					return user;
				}
				return null;
			} catch (Exception e) {
				return null;
			}finally{
				if(cs!=null){
					cs.close();
				}
			}
		}else {
			return null;
		}
	}
	
	
	/**
	 * 删除停用计划的任务和被删除的巡检项的任务
	 * @param db
	 * @return
	 */
	public static boolean clearInvalidTask(SQLiteDatabase db){ 
		if(db!=null && db.isOpen()){
			try {
				db.delete("EP_PatrolTask",
						"PlanID NOT IN (SELECT PlanID FROM EP_PatrolTaskPlan WHERE State = 1) OR "
						+ "PatrolTagID NOT IN (SELECT PatrolTagID from EP_PatrolTag)",null);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}
	
	public static boolean clearAllTask(SQLiteDatabase db){
		if(db!=null && db.isOpen()){
			try {
				db.delete("EP_PatrolTask",null,null);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}
	
	public static boolean clearTaskByTagId(SQLiteDatabase db,int tagId){
		if(db!=null && db.isOpen()){
			try {
				db.delete("EP_PatrolTask","PatrolTagID = "+tagId,null);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}
	
	public static Cursor getAllPatrolTag(SQLiteDatabase db){
		if(db!=null && db.isOpen()){
			return db.rawQuery("select * from EP_PatrolTag", null);
		}
		return null;
	}
	
	public static Cursor getPlanIdByTag(SQLiteDatabase db , int tagId){
		if(db!=null && db.isOpen()){
			return db.rawQuery("select PlanID from EP_PatrolTag2Plan where PatrolTagID = "+ tagId, null);
		}
		return null;
	}
	
	
	//根据登录的用户ID ，获取他所 负责的泵房所属的 水务公司
	public static Cursor getFatherPlantByUserCharge(SQLiteDatabase db,String userId){
		if(db!=null && db.isOpen()){
			String sql = "select a.* from EP_PlantInfo a INNER JOIN "
					+ "(select * from EP_ChargeOfPlant c INNER JOIN "
					+ "EP_PlantInfo d on c.PlantID = d.PlantID WHERE UserID = ? GROUP BY FartherPlantID ) b "
					+ "on a.PlantID = b.PlantID";
			return db.rawQuery(sql, new String[]{userId});
		}
		return null;
	}

	/**
	 * 获取用户管辖的 泵房
	 * @param db
	 * @param userId
	 * @param dateTime
	 * @return
	 */
	public static Cursor getPlantByUserCharge(SQLiteDatabase db,String userId,String dateTime){
		if(db!=null && db.isOpen()){
			String sql = "select a.*,"
					+ "(select count(CardID) from EP_NFCCard where PlantID = a.PlantID) as CardCount, "
					+"(select StartDateTime from EP_PatrolTask where PlantID = a.PlantID and StartDateTime > ? "
					+ "and IsDone = 0 GROUP BY StartDateTime ORDER BY StartDateTime asc LIMIT 1) as NewestTime "
					+ "from EP_PlantInfo a inner join EP_ChargeOfPlant b on "
					+ "a.PlantID = b.PlantID where b.UserID = ? ORDER BY case WHEN NewestTime is null " +
					"then 1 else 0 end , NewestTime asc";
			return db.rawQuery(sql, new String[]{dateTime,userId});
		}
		return null;
	}
	
	//获取该用户当前最新的任务
	public static Cursor getNewestTaskByUserId(SQLiteDatabase db, String userId ,String startTime,String stopTime){
		if(db!=null && db.isOpen()){
			String sql = "select a.*,c.CardID from EP_PatrolTask a "
					+ "INNER JOIN EP_PatrolTag b on a.PatrolTagID = b.PatrolTagID "
					+ "INNER JOIN EP_NFCCard c ON b.CardID = c.CardID "
					+ "INNER JOIN EP_ChargeOfPlant d  on a.PlantID = d.PlantID "
					+ "where a.StartDateTime <= ? "
					+ "and a.EndDateTime >= ? and a.IsUpload = 0 and d.UserID=?";
			return db.rawQuery(sql, new String[]{startTime,stopTime,userId});
		}
		return null;
	}
	
	//获取用户管辖泵房下的 卡片
	public static Cursor getChargeOfCardByUserId(SQLiteDatabase db,String userId){
		if(db!=null && db.isOpen()){
			String sql = "select a.* from EP_NFCCard a INNER JOIN EP_ChargeOfPlant b "
					+ "on a.PlantID = b.PlantID where b.UserID = ? and a.State = 1 and a.CardType = 0";
			return db.rawQuery(sql, new String[]{userId});
		}
		return null;
	}
	
	//获取 某泵房下的 任务数量
	public static int getTaskCountByPlant(SQLiteDatabase db , int plantId,String startTime,String stopTime){
		if(db!=null && db.isOpen()){
			int count =0;
			String sql = "select count(TaskID) from EP_PatrolTask where PlantID =? and "
					+ "StartDateTime <= ? and EndDateTime >= ? and IsUpload = 0";
			Cursor cs = db.rawQuery(sql,  new String[]{String.valueOf(plantId),startTime,stopTime});
			if(cs!=null){
				cs.moveToFirst();
				count = cs.getInt(0);
				cs.close();
			}
			return count;
		}
		return 0;
	}

	/**
	 * 获取当前卡片下的任务数量
	 *
	 * @param db
	 * @param cardId
	 * @param startTime
	 * @param stopTime
	 * @return
	 */
	//
	public static int [] getTaskCountByCard(SQLiteDatabase db,int cardId,String startTime,String stopTime){
		int [] counts = new int[2];
		if(db!=null && db.isOpen()){
 			String sql = "select count(a.TaskID) as mount,sum(IsDone) as done from EP_PatrolTask a "
					+ "inner join EP_PatrolTag b on a.PatrolTagID = b.PatrolTagID "
					+ "inner JOIN EP_NFCCard c on b.CardID = c.CardID "
					+"where c.CardID = ? and "
					+ "a.StartDateTime <= ? and a.EndDateTime >= ? and a.IsUpload = 0";
			Cursor cs = db.rawQuery(sql,  new String[]{String.valueOf(cardId),startTime,stopTime});
			if(cs!=null && cs.getCount()>0){
				cs.moveToFirst();
				counts[0] = cs.getInt(0);
				counts[1] = cs.getInt(1);
				cs.close();
			}
			return counts;
		}
		return counts;
	}

	/**
	 * 查找该卡片下最近的一次任务时间
	 * @param db
	 * @param dateTime
	 * @param cardId
	 * @return
	 */
	public static String getNewestTaskTimeByCardId(SQLiteDatabase db ,String dateTime,String cardId){
		String time ="";
		if(db !=null && db.isOpen()){
			String sql = "select a.StartDateTime from EP_PatrolTask a " +
					"inner join EP_PatrolTag b on a.PatrolTagID = b.PatrolTagID " +
					"inner join EP_NFCCard c on b.CardID = c.CardID " +
					"where c.CardID = ? and a.StartDateTime > ? and a.IsDone = 0 GROUP BY a.StartDateTime ORDER BY StartDateTime asc LIMIT 1";
			Cursor cs = db.rawQuery(sql,new String[]{cardId,dateTime});
			if(cs !=null && cs.getCount()>0){
				cs.moveToFirst();
				time = cs.getString(0);
			}
		}
		return time;
	}

	/**
	 * 查找该泵房下最近的一次任务时间
	 * @param db
	 * @param dateTime
	 * @param plantId
	 * @return
	 */
	public static String getNewestTaskTimeByPlantId(SQLiteDatabase db, String dateTime,String plantId){
		String time ="";
		if(db !=null && db.isOpen()){
			String sql = "select StartDateTime from EP_PatrolTask where PlantID = ? and StartDateTime > ? " +
					"and IsDone = 0 GROUP BY StartDateTime ORDER BY StartDateTime asc LIMIT 1";
			Cursor cs = db.rawQuery(sql,new String[]{plantId,dateTime});
			if(cs !=null && cs.getCount()>0){
				cs.moveToFirst();
				time = cs.getString(0);
			}
		}
		return time;
	}

	/**
	 * 获取该卡片下有多少个巡检项
	 * @param db
	 * @param cardId
	 * @return
	 */
	public static int getTagCountByCard(SQLiteDatabase db ,int cardId){
		if(db!=null && db.isOpen()){
			int count =0;
			String sql = "select count(PatrolTagID) from EP_PatrolTag where CardID = ?";
			Cursor cs = db.rawQuery(sql,  new String[]{String.valueOf(cardId)});
			if(cs!=null){
				cs.moveToFirst();
				count = cs.getInt(0);
				cs.close();
			}
			return count;
		}
		return 0;
	}
	
	
	public static int getUnUploadTaskByUser(SQLiteDatabase db ,String userId){
		if(db!=null && db.isOpen()){
			int count =0;
			String sql = "select count(TaskID) from EP_PatrolTask a INNER JOIN "
					+ "EP_ChargeOfPlant b on a.PlantID = b.PlantID "
					+ "where a.IsDone=1 and a.IsUpload =0 and b.UserID= ?";
			Cursor cs = db.rawQuery(sql,  new String[]{userId});
			if(cs!=null){
				cs.moveToFirst();
				count = cs.getInt(0);
				cs.close();
			}
			return count;
		}
		return 0;
	}
	
	public static int getUnDoTaskNowByUser(SQLiteDatabase db ,String userId,String time){
		if(db!=null && db.isOpen()){
			int count =0;
			String sql = "select count(TaskID) from EP_PatrolTask a INNER JOIN "
					+ "EP_ChargeOfPlant b on a.PlantID = b.PlantID "
					+ "where a.IsDone=0 and a.IsUpload =0 and b.UserID= ? and "
					+ "StartDateTime <= ? and EndDateTime > ?";
			Cursor cs = db.rawQuery(sql,  new String[]{userId,time,time});
			if(cs!=null){
				cs.moveToFirst();
				count = cs.getInt(0);
				cs.close();
			}
			return count;
		}
		return 0;
	}
	
	
	public static Cursor getNewestTaskByCard(SQLiteDatabase db,int cardId,String nowTime,String startTime,String stopTime){
		if(db!=null && db.isOpen()){
			String sql = "select a.*,b.ResultType,b.UnitID,b.IsMustUseNFCCard,b.PatrolName,b.DefaultDicIntValues,b.DicID,b.IsMustUseText,b.IsMustUsePic,"
					+ "c.CardID,d.Text Unit,e.OPDateTime,e.DataID,e.Text from EP_PatrolTask a "
					+ "INNER JOIN EP_PatrolTag b on a.PatrolTagID = b.PatrolTagID "
					+ "INNER JOIN EP_NFCCard c on b.CardID = c.CardID "
					+ "LEFT JOIN EP_Unit d on b.UnitID = d.UnitID "
					+ "LEFT JOIN EP_PatrolResult e on a.TaskID = e.TaskID "
					+ "where c.CardID = ? AND a.IsUpload = 0 and "
					+ "((a.StartDateTime <= ? and a.EndDateTime >= ?) "
					+ "OR "
					+ "(a.StartDateTime >=? and a.EndDateTime>? and a.EndDateTime <= ?))";
			return db.rawQuery(sql, new String[]{String.valueOf(cardId),nowTime,nowTime,startTime,nowTime,stopTime});
		}
		return null;
	}

	public static Cursor getNewestTaskByCardAndDeviceGroup(SQLiteDatabase db,int cardId,String nowTime,String startTime,String stopTime,int deviceId){
		if(db!=null && db.isOpen()){
			String sql = "select * from (select a.*,b.ResultType,b.UnitID,b.IsMustUseNFCCard,b.PatrolName,b.DefaultDicIntValues,b.DicID,b.IsMustUseText,b.IsMustUsePic,"
					+ "c.CardID,d.Text Unit,e.OPDateTime,e.DataID,e.Text,f.FartherDeviceID from EP_PatrolTask a "
					+ "INNER JOIN EP_PatrolTag b on a.PatrolTagID = b.PatrolTagID "
					+ "INNER JOIN EP_NFCCard c on b.CardID = c.CardID "
					+ "LEFT JOIN EP_Unit d on b.UnitID = d.UnitID "
					+ "LEFT JOIN EP_PatrolResult e on a.TaskID = e.TaskID "
					+ "LEFT JOIN EP_Device f on a.DeviceID = f.DeviceID "
					+ "where c.CardID = ? AND a.IsUpload = 0 and "
					+ "((a.StartDateTime <= ? and a.EndDateTime >= ?) "
					+ "OR "
					+ "(a.StartDateTime >=? and a.EndDateTime>? and a.EndDateTime <= ?))) where DeviceID = ? or FartherDeviceID = ?";
			return db.rawQuery(sql, new String[]{String.valueOf(cardId),nowTime,nowTime,startTime,nowTime,stopTime,String.valueOf(deviceId),String.valueOf(deviceId)});
		}
		return null;
	}

	public static Cursor getNewestTaskByDevice(SQLiteDatabase db,String nowTime,String startTime,String stopTime,int deviceId){
		if(db!=null && db.isOpen()){
			String sql = "select * from (select a.*,b.ResultType,b.UnitID,b.IsMustUseNFCCard,b.PatrolName,b.DefaultDicIntValues,b.DicID,b.IsMustUseText,b.IsMustUsePic,"
					+ "c.CardID,d.Text Unit,e.OPDateTime,e.DataID,e.Text,f.FartherDeviceID from EP_PatrolTask a "
					+ "INNER JOIN EP_PatrolTag b on a.PatrolTagID = b.PatrolTagID "
					+ "INNER JOIN EP_NFCCard c on b.CardID = c.CardID "
					+ "LEFT JOIN EP_Unit d on b.UnitID = d.UnitID "
					+ "LEFT JOIN EP_PatrolResult e on a.TaskID = e.TaskID "
					+ "LEFT JOIN EP_Device f on a.DeviceID = f.DeviceID "
					+ "where a.IsUpload = 0 and "
					+ "((a.StartDateTime <= ? and a.EndDateTime >= ?) "
					+ "OR "
					+ "(a.StartDateTime >=? and a.EndDateTime>? and a.EndDateTime <= ?))) where DeviceID = ?";
			return db.rawQuery(sql, new String[]{nowTime,nowTime,startTime,nowTime,stopTime,String.valueOf(deviceId),String.valueOf(deviceId)});
		}
		return null;
	}

	public static Cursor getHistoryTaskByTag(SQLiteDatabase db,int tagId,String startTime,String stopTime,StringBuilder conditioin){
		String sql = "select a.*,b.ResultType,b.IsMustUseNFCCard,b.PatrolName,b.DicID,"
				+ "c.CardID,d.Text Unit,e.OPDateTime,e.DataID from EP_PatrolTask a "
				+ "INNER JOIN EP_PatrolTag b on a.PatrolTagID = b.PatrolTagID "
				+ "INNER JOIN EP_NFCCard c on b.CardID = c.CardID "
				+ "LEFT JOIN EP_Unit d on b.UnitID = d.UnitID "
				+ "LEFT JOIN EP_PatrolResult e on a.TaskID = e.TaskID "
				+ "where b.PatrolTagID = ? and "
				+ "StartDateTime >= ? and StartDateTime <= ? ";
		if(conditioin!=null) sql+= conditioin.toString();
		if(db!=null && db.isOpen()){
			return db.rawQuery(sql, new String[]{String.valueOf(tagId),startTime,stopTime});
		}
		return null;
	}
	
	public static Cursor getTaskValuesAndSteps(SQLiteDatabase db,int taskId,String tableName){
		if(db!=null && db.isOpen()){
			String sql ;
			if(tableName.equals("EP_PatrolResult_String")){
				sql = "SELECT DValue,Steps from "+tableName+" a "
						+ "INNER JOIN EP_PatrolResult b on a.DataID = b.DataID "
						+ "WHERE b.TaskID = ?";
			}else {
				sql = "SELECT DValue from "+tableName+" a "
						+ "INNER JOIN EP_PatrolResult b on a.DataID = b.DataID "
						+ "WHERE b.TaskID = ?";
			}
			return db.rawQuery(sql, new String[]{String.valueOf(taskId)});
		}
		return null;
	}
	
	public static Cursor getTaskValueSteps(SQLiteDatabase db,int taskId) {
		if(db!=null && db.isOpen()){
			String sql = "select Steps from EP_PatrolResult_String a inner join EP_PatrolResult b "
					+ "on a.DataID = b.DataID where b.TaskID = ?";
			return db.rawQuery(sql, new String[]{String.valueOf(taskId)});
		}
		return null;
	}
	
	public static Cursor getDefaultValues(SQLiteDatabase db,String defaultDicIntValues,int dicId){
		if(db!=null && db.isOpen()){
			String sql = "select TextValue from EP_DicValue where DicID = ? and IntValue in ( "+defaultDicIntValues+")";
			return db.rawQuery(sql,new String[]{String.valueOf(dicId)});
		}
		return null;
	}
	public static Cursor getTaskTextValues(SQLiteDatabase db,int dicId){
		if(db!=null && db.isOpen()){
			String sql = "select * from EP_DicValue where DicID = ?";
			return db.rawQuery(sql,new String[]{String.valueOf(dicId)});
		}
		return null;
	}
	
	public static int getDataIdByTask(SQLiteDatabase db,Long taskId){
		if(db!=null && db.isOpen()){
			int dataId = 0;
			String sql = "select DataID from EP_PatrolResult where TaskID = ?";
			Cursor cs = db.rawQuery(sql,new String[]{String.valueOf(taskId)});
			if(cs!=null){
				cs.moveToFirst();
				dataId = cs.getInt(0);
				cs.close();
			}
			return dataId;
		}
		return 0;
	}
	
	public static Cursor getPatrolTagByCard(SQLiteDatabase db,int cardId){
		if(db!=null && db.isOpen()){
			String sql = "select a.*,b.Text as Unit from EP_PatrolTag a "
					+ "LEFT JOIN EP_Unit b on a.UnitID = b.UnitID where a.CardID = ?";
			return db.rawQuery(sql,new String[]{String.valueOf(cardId)});
		}
		return null;
	}
	
	public static Cursor getPlanByTag(SQLiteDatabase db,int tagId){
		if(db!=null && db.isOpen()){
			String sql = "select a.* from EP_PatrolTaskPlan a INNER JOIN EP_PatrolTag2Plan b "
					+ "on a.PlanID = b.PlanID where a.State = 1 and b.PatrolTagID = ?";
			return db.rawQuery(sql,new String[]{String.valueOf(tagId)});
		}
		return null;
	}
	
	public static Cursor getTipsOfTimeByUser(SQLiteDatabase db, String userId,String startTime){
		if(db!=null && db.isOpen()){
			String sql = "select StartDateTime from EP_PatrolTask a "
					+ "inner join EP_ChargeOfPlant b on a.PlantID = b.PlantID "
					+ "where a.IsDone= 0 and a.IsUpload=0 and a.StartDateTime>= ? and b.UserID=? "
					+ "GROUP BY a.StartDateTime";
			return db.rawQuery(sql,new String[]{startTime,userId});
		}
		return null;
	}
	
	public static Cursor getTaskToUpload(SQLiteDatabase db,String endTime){
		if(db!=null && db.isOpen()){
			String sql = "select a.*,b.ResultType,b.DicID,c.DataID,c.Text from EP_PatrolTask a " +
					"inner join EP_PatrolTag b on a.PatrolTagID = b.PatrolTagID " +
					"inner join EP_PatrolResult c on a.TaskID = c.TaskID " +
					"where a.IsUpload = 0 and ( a.IsDone = 1 or a.EndDateTime <  ? )";
			return db.rawQuery(sql, new String[]{endTime});
		}
		return null;
	}

	public static Cursor getTaskMediaToUpload(SQLiteDatabase db,String endTime){
		if(db!=null && db.isOpen()){
			String sql = "select a.*,b.PlanID,b.StartDateTime,c.PatrolTagID from EP_PatrolResult_Media a " +
					"inner join EP_PatrolTask b on a.TaskID = b.TaskID " +
					"inner join EP_PatrolTag c on b.PatrolTagID = c.PatrolTagID " +
					"where a.IsUpload = 0 and ( b.IsDone = 1 or b.EndDateTime <  ? )";
			return db.rawQuery(sql, new String[]{endTime});
	}
		return null;
	}
	
	public static Cursor getTaskValueStepByDicValueId(SQLiteDatabase db, String dicValueId){
		if(db!=null && db.isOpen()){
			String sql = "select * from EP_DicStep where DicValueID = ?";
			return db.rawQuery(sql, new String[]{dicValueId});
		}
		return null;
	}

	public static Cursor getTaskValueSelectedStepsByDicValue(SQLiteDatabase db,String taskId, String dicValue){
		if(db!=null && db.isOpen()){
			String sql = "select b.Steps from EP_PatrolResult a "
					+ "inner join EP_PatrolResult_String b on a.DataID = b.DataID "
					+ "where a.TaskID = ? and b.DValue = ?";
			return db.rawQuery(sql, new String[]{taskId,dicValue});
		}
		return null;
	}

	public static Cursor getTaskStepByDicId(SQLiteDatabase db , int dicId) {
		if(db!=null && db.isOpen()){
			String sql = "select a.* from EP_DicStep a inner join EP_DicValue b on a.DicValueID = b.DicValueID "
					+ "inner join EP_DicKey c on b.DicID = c.DicID where c.DicID = ?";
			return db.rawQuery(sql, new String[]{String.valueOf(dicId)});
		}
		return null;
	}
	
	
	public static Cursor getNfcCardByTagId(SQLiteDatabase db,String tagId){
		if(db!=null && db.isOpen()){
			String sql = "select * from EP_NFCCard where CardTagID = ?";
			return db.rawQuery(sql, new String[]{tagId});
		}
		return null;
	}
	
	public static Cursor getNfcCardByCardId(SQLiteDatabase db,int cardId){
		if(db!=null && db.isOpen()){
			String sql = "select * from EP_NFCCard where CardID = ?";
			return db.rawQuery(sql, new String[]{String.valueOf(cardId)});
		}
		return null;
	}

	public static Cursor getUploadDataValue(SQLiteDatabase db, String dataId,String tableName,String dicId){
		if(db!=null && db.isOpen()){
			String sql;
			if(tableName.equals("EP_PatrolResult_Number")){
				sql = "select * from EP_PatrolResult_Number where DataID = ?";
				return  db.rawQuery(sql,new String[]{dataId});
			}else{
				sql = "select a.*, (select IntValue from EP_DicValue where DicID = ? and TextValue = a.DValue) IntValue " +
						"from EP_PatrolResult_String a where a.DataID = ?";
				return  db.rawQuery(sql,new String[]{dicId,dataId});
			}
		}
		return null;
	}

	public static Cursor getDeviceGroupByPlantId(SQLiteDatabase db,String plantId){
		if(db!=null && db.isOpen()){
			String sql = "select * from EP_Device where PlantID = ? and DeviceOrganizationLevel = 5";
			return  db.rawQuery(sql,new String[]{plantId});
		}
		return null;
	}

	public static Cursor getTaskMeddiaByTaskId(SQLiteDatabase db,String taskId){
		if(db!=null && db.isOpen()){
			String sql = "select * from EP_PatrolResult_Media where TaskID = ?";
			return  db.rawQuery(sql,new String[]{taskId});
		}
		return null;
	}

	public static Cursor getDeviceByCardId(SQLiteDatabase db,String cardId){
		if(db!=null && db.isOpen()){
			String sql = "select * from EP_Device where DeviceID in (select DeviceID from EP_PatrolTag where CardID = ? GROUP BY DeviceID)";
			return db.rawQuery(sql,new String[]{cardId});
		}
		return null;
	}


}
