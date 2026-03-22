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
        FileWriter writer = new FileWriter(file, false);



        for (SensorData data : dataList) {

            writer.append("Time: ");
            writer.append(csvSafe(data.getTime()));
            writer.append("\n");

            writer.append("deviceAddress: ");
            writer.append(csvSafe(data.getDeviceAddress()));
            writer.append("\n");


            writer.append("deviceName: ");
            writer.append(csvSafe(data.getDeviceName()));
            writer.append("\n");


            writer.append("Co2: ");
            writer.append(String.valueOf(data.getCo2()));
            writer.append(" ppm");
            writer.append("\n");


            writer.append("Temp: ");
            writer.append(String.valueOf(data.getTemperature()));
            writer.append(" °C");
            writer.append("\n");


            writer.append("rawHex: ");
            writer.append(csvSafe(data.getRawHex()));
            writer.append("\n");


            writer.append("Rssi: ");
            writer.append(String.valueOf((data.getRssi())));
            writer.append("\n");


            writer.append("Uuid: ");
            writer.append(csvSafe(data.getUuid()));
            writer.append("\n");
        }

        writer.flush();
        writer.close();
        return file;





    }
    private static String csvSafe(String value) {

        if (value == null) {return "";}

        String escaped = value.replace("\"", "\"\"");

        return "\"" + escaped + "\"";
    }


}
