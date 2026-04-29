package com.example.blescanner01;

public class SensorData {
    private String time;
    private String sensorTime;
    private String deviceName;
    private String deviceAddress;

    private float temperature;
    private float humidity;
    private int aqi;
    private int tvoc;
    private int eco2;

    private double lat;
    private double lon;


    private String rawHex;
    private int rssi;
    private String uuid;
    //숫자 형식 시간을 받는 변수 추가
    private long unixTimestamp;
    //sensordata에 unixtimestamp 추가
    public SensorData(String time,String sensorTime,long unixTimestamp, String deviceAddress, String deviceName,
                      float temperature, float humidity, int aqi, int tvoc, int eco2, double lat, double lon, String rawHex, int rssi, String uuid) {
        this.time = time;
        this.sensorTime = sensorTime;
        this.unixTimestamp = unixTimestamp;
        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;

        this.temperature = temperature;
        this.humidity = humidity;
        this.aqi = aqi;
        this.tvoc = tvoc;
        this.eco2 = eco2;

        this.lat = lat;
        this.lon = lon;

        this.rawHex = rawHex;
        this.rssi = rssi;
        this.uuid = uuid;
    }

    public String getTime() {return time;}
    public String getSensorTime() {return sensorTime;}

    public long getUnixTimestamp() { return unixTimestamp;}
    public String getDeviceName() {return deviceName;}
    public String getDeviceAddress() {return deviceAddress;}

    public float getTemperature() {return temperature;}
    public float getHumidity(){return humidity;}
    public int getAqi() {return aqi;}
    public int getTvoc() {return tvoc;}
    public int getEco2(){return eco2;}

    public double getLat(){return lat;}
    public double getLon(){return lon;}

    public String getRawHex() {return rawHex;}
    public int getRssi(){return rssi;}
    public String getUuid(){return uuid;}

}
