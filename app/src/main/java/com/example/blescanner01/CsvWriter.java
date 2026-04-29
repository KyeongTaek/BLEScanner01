package com.example.blescanner01;

import android.content.Context;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class CsvWriter {

    public static File writeCsv(Context context, List<SensorData> dataList) throws Exception {
        if (dataList == null) {throw new Exception("저장할 데이터 목록이 없습니다.");}
        File dir = context.getExternalFilesDir(null);

        if (dir == null) { throw new Exception("외부 저장소 경로를 가져오지 못했습니다.");}

        File file = new File(dir, "sensor_data.csv");

        boolean isNewFile = !file.exists();

        FileWriter writer = new FileWriter(file, true); // 텍스트 뒤로 이어쓰기 허용

        if(isNewFile) {
            writer.append("app_timestamp,sensor_timestamp,device_name,device_address,rssi,uuid,temperature,humidity,aqi,tvoc,eco2,lat,lon\n");
        }
        // ppt 형식에 맞게 수정
        for (SensorData data : dataList) {
            writer.append(csvSafe(data.getTime())).append(",")
                    .append(csvSafe(data.getSensorTime())).append(",")
                            .append(csvSafe(data.getDeviceName())).append(",")
                            .append(csvSafe(data.getDeviceAddress())).append(",")
                            .append(String.valueOf((data.getRssi()))).append(",")
                            .append(csvSafe(data.getUuid())).append(",")
                    .append(String.valueOf(data.getTemperature())).append(",")
                    .append(String.valueOf(data.getHumidity())).append(",")
                    .append(String.valueOf(data.getAqi())).append(",")
                    .append(String.valueOf(data.getTvoc())).append(",")
                    .append(String.valueOf(data.getEco2())).append(",")
                    .append(String.valueOf(data.getLat())).append(",")
                    .append(String.valueOf(data.getLon())).append("\n");

        }

        writer.flush();
        writer.close();
        return file;

    }


    public static void appendRow(Context context, SensorData data) throws Exception {
        File dir = context.getExternalFilesDir(null);
        if (dir == null) throw new Exception("외부 저장소 경로를 가져오지 못했습니다.");

        File file = new File(dir, "sensor_data.csv");
        boolean isNewFile = !file.exists();

        FileWriter writer = new FileWriter(file, true);
        if (isNewFile) {
            writer.append("app_timestamp,sensor_timestamp, device_name,device_address,rssi,uuid,temperature,humidity,aqi,tvoc,eco2,lat,lon\n");
        }

        writer.append(csvSafe(data.getTime())).append(",")
                .append(csvSafe(data.getSensorTime())).append(",")
                .append(csvSafe(data.getDeviceName())).append(",")
                .append(csvSafe(data.getDeviceAddress())).append(",")
                .append(String.valueOf(data.getRssi())).append(",")
                .append(csvSafe(data.getUuid())).append(",")
                .append(String.valueOf(data.getTemperature())).append(",")
                .append(String.valueOf(data.getHumidity())).append(",")
                .append(String.valueOf(data.getAqi())).append(",")
                .append(String.valueOf(data.getTvoc())).append(",")
                .append(String.valueOf(data.getEco2())).append(",")
                .append(String.valueOf(data.getLat())).append(",")
                .append(String.valueOf(data.getLon())).append("\n");

        writer.flush();
        writer.close();
    }
    private static String csvSafe(String value) {

        if (value == null) {return "";}

        String escaped = value.replace("\"", "\"\"");

        return "\"" + escaped + "\"";
    }


}
