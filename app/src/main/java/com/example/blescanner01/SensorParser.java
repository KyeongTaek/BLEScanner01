package com.example.blescanner01;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SensorParser {
    private static String getCurrentTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        return sdf.format(new Date());

    }

    public static String bytesToHex(byte[] bytes){
        if (bytes == null){return "";}
        StringBuilder sb = new StringBuilder();
        for (byte b:bytes){
            sb.append(String.format("%02x", b & 0xFF));
        }
        return sb.toString().trim();

    }
    private static int littleEndianToUInt16(byte low, byte high) {return ((high & 0xFF) << 8) | (low & 0xFF);}

    public static SensorData parse(byte[] rawData, String deviceAddress, String deviceName,
                                   int rssi, String uuid) {
        if (rawData == null || rawData.length < 12) { return null; }
        if (deviceName == null) { deviceName = "unknown"; }
        if (uuid == null) { uuid = "unknown"; }

        String time = getCurrentTime();
        String rawHex = bytesToHex(rawData);

        // 슬라이드 자료 기준: Temp(2) Humidity(2) AQI(1) TVOC(2) eCO2(2) Timestamp(4)
        int tempRaw   = littleEndianToUInt16(rawData[0], rawData[1]);
        float temperature = tempRaw / 100.0f;

        int humRaw    = littleEndianToUInt16(rawData[2], rawData[3]);
        float humidity = humRaw / 100.0f;
        //1
        int aqi  = rawData[4] & 0xFF;
        int tvoc = littleEndianToUInt16(rawData[5], rawData[6]);
        int eco2 = littleEndianToUInt16(rawData[7], rawData[8]);

        return new SensorData(time, deviceAddress, deviceName,
                temperature, humidity, aqi, tvoc, eco2, rawHex, rssi, uuid);
    }
}