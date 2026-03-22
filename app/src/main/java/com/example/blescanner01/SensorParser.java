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
                                   int rssi, String uuid){
        if (rawData == null){return null;}
        if(rawData.length < 4){return null;}
        if (deviceName == null){deviceName = "unknown";}
        if (uuid == null) {uuid = "unknown";}

        String time = getCurrentTime();
        String rawHex = bytesToHex(rawData);
        int co2 = littleEndianToUInt16(rawData[0], rawData[1]);
        int tempRaw = littleEndianToUInt16(rawData[2], rawData[3]);
        float temperature = tempRaw / 100.0f;
        return new SensorData(time, deviceAddress, deviceName,co2, temperature, rawHex, rssi, uuid);
    }
}