
package com.example.blescanner01;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final ArrayList<SensorData> sensorDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        byte[] testRawData = new byte[] {
                (byte) -68,  // 0xBC
                (byte) 1,    // 0x01
                (byte) -67,  // 0xBD
                (byte) 9     // 0x09
        };


        String testAdd = "B8:27:EB:47:8D:50";
        String testName = "opensrc_week3";
        int testrssi = -77;
        String testuuid = "0x181A";



        handleScannedData(testRawData, testAdd, testName, testrssi, testuuid);
        saveSensorDataToCsv();




    }

    public void handleScannedData(byte[] rawData, String deviceAddress, String deviceName, int rssi, String uuid) {
        SensorData data = SensorParser.parse(rawData, deviceAddress, deviceName, rssi, uuid);

        if (data == null) {
            Log.d("TEST", "파싱 실패");
            return;
        }

        sensorDataList.add(data);

        Log.d("TEST", "time = " + data.getTime());
        Log.d("TEST", "deviceAddress = " + data.getDeviceAddress());
        Log.d("TEST", "deviceName = " + data.getDeviceName());
        Log.d("TEST", "co2 = " + data.getCo2());
        Log.d("TEST", "temperature = " + data.getTemperature());
        Log.d("TEST", "rawHex = " + data.getRawHex());
        Log.d("TEST", "rssi = " + data.getRssi());
        Log.d("TEST", "uuid = " + data.getUuid());
    }

    public void saveSensorDataToCsv() {
        if (sensorDataList.isEmpty()) {
            Log.d("TEST", "저장할 데이터 없음");
            return;
        }

        try {
            File file = CsvWriter.writeCsv(this, sensorDataList);
            Log.d("TEST", "CSV 저장 성공: " + file.getAbsolutePath());
        } catch (Exception e) {
            Log.e("TEST", "CSV 저장 실패", e);
        }
    }
}
