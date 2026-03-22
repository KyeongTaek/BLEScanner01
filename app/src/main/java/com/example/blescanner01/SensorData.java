package com.example.blescanner01;

public class SensorData {
    private String time;
    private String deviceName;
    private String deviceAddress;
    private int co2;
    private float temperature;
    private String rawHex;
    private int rssi;
    private String uuid;
    public SensorData(String time,String deviceAddress, String deviceName,int co2,
                      float temperature, String rawHex, int rssi, String uuid) {
        this.time = time;
        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;

        this.co2 = co2;
        this.temperature = temperature;

        this.rawHex = rawHex;
        this.rssi = rssi;
        this.uuid = uuid;
    }

    public String getTime() {return time;}
    public String getDeviceName() {return deviceName;}
    public String getDeviceAddress() {return deviceAddress;}
    public int getCo2() {return co2;}
    public float getTemperature() {return temperature;}
    public String getRawHex() {return rawHex;}
    public int getRssi(){return rssi;}
    public String getUuid(){return uuid;}

}