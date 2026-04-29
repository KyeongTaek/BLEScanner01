package com.example.blescanner01;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SensorParser {
    private static String getCurrentTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        return sdf.format(new Date());

    }
    private static String formatSensorTime(long unixTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(unixTime * 1000L));
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

    private static long littleEndianToUInt32(byte b1, byte b2, byte b3, byte b4) {
        return ((long) (b4 & 0xFF) << 24) |
                ((long) (b3 & 0xFF) << 16) |
                ((long) (b2 & 0xFF) << 8)  |
                ((long) (b1 & 0xFF));
    }

    //규격: Temp(2) + Humid(2) + AQI(1) + TVOC(2) + eCO2(2) + Timestamp(4) = 총 13 Bytes

    public static SensorData parse(byte[] rawData, String deviceAddress, String deviceName,
                                   int rssi, String uuid, double lat, double lon) {
        if (rawData == null || rawData.length < 13) { return null; }
        if (deviceName == null) { deviceName = "unknown"; }
        if (uuid == null) { uuid = "unknown"; }

        String apptime = getCurrentTime();
        long unixTime = littleEndianToUInt32(rawData[9], rawData[10], rawData[11], rawData[12]);
        String sensorTime = formatSensorTime(unixTime);

        String rawHex = bytesToHex(rawData);

        // 순서 1. 온도 (Temp): short (2B), 규칙: / 100.0
        int tempRaw = littleEndianToUInt16(rawData[0], rawData[1]);
        float temperature = tempRaw / 100.0f;

        // 순서 2. 습도 (Humidity): short (2B), 규칙: / 100.0
        int humRaw = littleEndianToUInt16(rawData[2], rawData[3]);
        float humidity = humRaw / 100.0f;

        // 순서 3. 공기질 (AQI): byte (1B), 규칙: 그대로 읽음
        int aqi = rawData[4] & 0xFF;

        // 순서 4. 가스 (TVOC): ushort (2B), 규칙: & 0xFFFF
        int tvoc = littleEndianToUInt16(rawData[5], rawData[6]);

        // 순서 5. 이산화탄소 (eCO2): ushort (2B), 규칙: & 0xFFFF
        int eco2 = littleEndianToUInt16(rawData[7], rawData[8]);

        return new SensorData(apptime, sensorTime,unixTime, deviceAddress, deviceName,
                temperature, humidity, aqi, tvoc, eco2, lat, lon, rawHex, rssi, uuid);
    }
}