package com.env.bean;

import com.env.component.PatrolApplication;
import com.env.utils.SystemMethodUtil;
import com.env.utils.SystemParamsUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.annotation.Annotation;
import java.util.Date;

/**
 * Created by sk on 7/20/15.
 */
public class DeviceInfo {
    private int DeviceID;
    private String DeviceSN;
    private String DeviceName;
    private String PlantName;
    private Date StartUseTime;
    private String FactorySN;
    private Date FactoryDate;
    private String ManufactureName;
    private String SpecificationName;
    private Date SampleTime;

    public int getDeviceID() {

        return DeviceID;
    }

    public void setDeviceID(int deviceID) {
        DeviceID = deviceID;
    }

    public String getDeviceName() {
        return DeviceName;
    }

    public void setDeviceName(String deviceName) {
        DeviceName = deviceName;
    }

    public String getDeviceSN() {
        return DeviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        DeviceSN = deviceSN;
    }

    public Date getFactoryDate() {
        return FactoryDate;
    }

    public void setFactoryDate(Date factoryDate) {
        FactoryDate = factoryDate;
    }

    public String getFactorySN() {
        return FactorySN;
    }

    public void setFactorySN(String factorySN) {
        FactorySN = factorySN;
    }

    public String getManufactureName() {
        return ManufactureName;
    }

    public void setManufactureName(String manufactureName) {
        ManufactureName = manufactureName;
    }

    public String getPlantName() {
        return PlantName;
    }

    public void setPlantName(String plantName) {
        PlantName = plantName;
    }

    public Date getSampleTime() {
        return SampleTime;
    }

    public void setSampleTime(Date sampleTime) {
        SampleTime = sampleTime;
    }

    public String getSpecificationName() {
        return SpecificationName;
    }

    public void setSpecificationName(String specificationName) {
        SpecificationName = specificationName;
    }

    public Date getStartUseTime() {
        return StartUseTime;
    }

    public void setStartUseTime(Date startUseTime) {
        StartUseTime = startUseTime;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setDateFormat(SystemMethodUtil.StandardDateTimeSdf).create();
        return gson.toJson(this);
    }
}
