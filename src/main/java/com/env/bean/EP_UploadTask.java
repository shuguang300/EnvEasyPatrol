package com.env.bean;

import java.util.ArrayList;

/**
 * Created by sk on 7/2/15.
 */
public class EP_UploadTask {
    public int TaskID;
    public int PlanID;
    public int PlantID;
    public int PatrolTagID;
    public int DeviceID;
    public String StartDateTime;
    public String StopDateTime;
    public String ValidDateTime;
    public String DoneUserID;
    public boolean Isdone;
    public String SampleTime;
    public ArrayList<DValue_Binary> DValueList_Binary;
    public ArrayList<DValue_String> DValueList_String;
    public ArrayList<DValue_Number> DValueList_Number;
}
