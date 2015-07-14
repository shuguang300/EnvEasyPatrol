package com.env.bean;

import com.env.utils.SystemMethodUtil;
import com.env.utils.SystemParamsUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;

/**
 * Created by sk on 7/2/15.
 */
public class EP_UploadObject {
    private String PostDataDateTime;
    private String UserID;
    private List<HashMap<String,Object>> TaskList;
    private long PostDataCount;

    public long getPostDataCount() {
        return PostDataCount;
    }

    public void setPostDataCount(long postDataCount) {
        PostDataCount = postDataCount;
    }

    public String getPostDataDateTime() {
        return PostDataDateTime;
    }

    public void setPostDataDateTime(String postDataDateTime) {
        PostDataDateTime = postDataDateTime;
    }

    public List<HashMap<String, Object>> getTaskList() {
        return TaskList;
    }

    public void setTaskList(List<HashMap<String, Object>> taskList) {
        TaskList = taskList;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setDateFormat(SystemMethodUtil.StandardDateTimeSdf).create();
        return gson.toJson(this);
    }
}
