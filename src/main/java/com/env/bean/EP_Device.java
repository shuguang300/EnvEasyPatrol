package com.env.bean;

/**
 * Created by sk on 7/2/15.
 */
public class EP_Device {
    private int PlantID;
    private int DeviceID;
    private int FartherDeviceID;
    private String DeviceName;
    private int DeviceOrganizationLevel;
    private int DeviceClassTypeID;

    public int getPlantID() {
        return PlantID;
    }

    public void setPlantID(int plantID) {
        PlantID = plantID;
    }
    public int getDeviceClassTypeID() {
        return DeviceClassTypeID;
    }

    public void setDeviceClassTypeID(int deviceClassTypeID) {
        DeviceClassTypeID = deviceClassTypeID;
    }

    public String getDeviceName() {
        return DeviceName;
    }

    public void setDeviceName(String deviceName) {
        DeviceName = deviceName;
    }

    public int getDeviceID() {
        return DeviceID;
    }

    public void setDeviceID(int deviceID) {
        DeviceID = deviceID;
    }

    public int getDeviceOrganizationLevel() {
        return DeviceOrganizationLevel;
    }

    public void setDeviceOrganizationLevel(int deviceOrganizationLevel) {
        DeviceOrganizationLevel = deviceOrganizationLevel;
    }

    public int getFartherDeviceID() {
        return FartherDeviceID;
    }

    public void setFartherDeviceID(int fartherDeviceID) {
        FartherDeviceID = fartherDeviceID;
    }
}
