
package com.example.blescanner01;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.location.LocationManager;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.provider.Settings;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.io.File;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.ParcelUuid;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    Button btnScan, btnStop, btnRefresh, btnSave;

    ListView logListView;
    ListView historyListView;
    ArrayList<String> historyData;
    ArrayAdapter<String> historyAdapter;

    static ArrayList<String> logData; // 다른 파일에서 로그 남길 수 있도록, static으로 설정
    static ArrayAdapter<String> logAdapter; // 다른 파일에서 로그 남길 수 있도록, static으로 설정

    ListView scanView; // scan 정보 담는 리스트 추가

    List<Map<String, String>> scanData; // scan 정보 추가
    SimpleAdapter scanAdapter; // scan 리스트에 넣어주는 어댑터 추가
    List<BluetoothDevice> deviceList = new ArrayList<>(); // 중복 제거하기 위한 디바이스 리스트
    List<String> macList = new ArrayList<>(); // 디바이스를 필터링하기 위한 mac 주소 리스트

    private LineChart lineChart; // 차트

    private LineDataSet tempDataSet;
    private LineDataSet tvocDataSet;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    ScanCallback scanCallback;

    // 스캔 상태 관리 변수 추가
    boolean isScanning = false;

    private static final int REQUEST_CODE = 100;

    private final ArrayList<SensorData> sensorDataList = new ArrayList<>();

    private TransferManager transferManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        transferManager = new TransferManager(this);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        btnScan = findViewById(R.id.button);
        btnStop = findViewById(R.id.button2);
        btnRefresh = findViewById(R.id.button3);
        btnSave = findViewById(R.id.button4);

        historyListView = findViewById(R.id.history_list);
        historyData = new ArrayList<>();
        historyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyData);
        historyListView.setAdapter(historyAdapter);

        scanView = findViewById(R.id.scan_list); // scan 리스트 추가
        scanData = new ArrayList<>();
        scanAdapter = new SimpleAdapter(
                this,
                scanData,
                android.R.layout.simple_list_item_2,
                new String[] {"title", "subtitle"},
                new int[] {android.R.id.text1, android.R.id.text2}
        );
        scanView.setAdapter(scanAdapter);

        macList.add("D8:3A:DD:79:8E:BF"); // 센서 1 mac 주소 추가
        macList.add("B8:27:EB:D3:40:06"); // 센서 2 mac 주소 추가
        macList.add("88:A2:9E:9B:5E:6A"); // 센서 3 mac 주소 추가
        macList.add("D8:3A:DD:79:8F:80"); // 센서 4 mac 주소 추가
        macList.add("D8:3A:DD:C1:88:BD"); // 센서 5 mac 주소 추가

        lineChart = findViewById(R.id.lineChart);
        tempDataSet = createDataSet("Temperature (°C)", Color.BLUE); // 온도 데이터셋 생성
        tvocDataSet = createDataSet("TVOC", Color.MAGENTA); // tvoc 데이터셋 생성
        tempDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT); // 오른쪽 y축을 온도 축으로 설정

        LineData lineData = new LineData(tempDataSet, tvocDataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // 차트 새로고침

        logListView = findViewById(R.id.log_list);
        logData = new ArrayList<>();
        logAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, logData);
        logListView.setAdapter(logAdapter);

        btnScan.setOnClickListener(v -> {
            btnScan.setEnabled(false); // 스캔 버튼 누르지 않게
            btnStop.setEnabled(true); // 스톱 버튼 누를 수 있게

            // 이미 스캔 중이면 중복 실행 방지
            if (isScanning) {
                logData.add("이미 스캔 중");
                logAdapter.notifyDataSetChanged();
                return;
            }

            //BLE 스캔 콜백 (전역으로 사용)
            scanCallback = new ScanCallback() {
                @SuppressLint("MissingPermission")
                @Override //스캔 결과 처리
                public void onScanResult(int callbackType, ScanResult result) {
                    BluetoothDevice device = result.getDevice();
                    String deviceName = device.getName();
                    if (deviceName == null) deviceName = "이름 없음";

                    String deviceAddress = device.getAddress(); //MAC주소 받아오는부분
                    int rssi = result.getRssi(); //신호세기
                    String uuid = "0000181A-0000-1000-8000-00805F9B34FB";

                    if(!macList.contains(deviceAddress)) return;

                    // Raw 데이터 추출
                    byte[] rawData = null;
                    if (result.getScanRecord() != null) {
                        rawData = result.getScanRecord().getServiceData(ParcelUuid.fromString(uuid));
                    }

                    boolean isDuplicate = false;
                    int idx = 0;
                    for(BluetoothDevice d : deviceList) { // 리스트에 해당 장치가 들어온 적 있는지 확인
                        if(d.getAddress().equals(deviceAddress)) {
                            isDuplicate = true;
                            break;
                        }
                        idx = idx + 1;
                    }

                    String dataString = null;
                    if(deviceName != null && rawData != null) {
                        if(!isDuplicate) { // 리스트에 해당 장치가 들어온 적 없으면
                            dataString = "MAC: " + deviceAddress + "\nRSSI: " + rssi + "\nRAW: " + Arrays.toString(rawData); // scanrecord 전체 대신 rawdata만 표시함
                            deviceList.add(device); // 리스트에 새로 추가
                            addItemToScanList(deviceName, dataString);
                        }
                        else { // 리스트에 해당 장치가 들어온 적 있으면
                            dataString = "MAC: " + deviceAddress + "\nRSSI: " + rssi + "\nRAW: " + Arrays.toString(rawData); // scanrecord 전체 대신 rawdata만 표시함
                            addItemToScanList(deviceName, dataString, idx); // 리스트의 기존 자리 수정
                        }
                    }

                    if (rawData != null) {
                        handleScannedData(rawData, deviceAddress, deviceName, rssi, uuid);
                    }
                }
                public void onScanFailed(int errorCode) { //스캔 실패 시
                    String errorMsg;

                    switch (errorCode) {
                        case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
                            errorMsg = "이미 스캔 중";
                            break;
                        case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                            errorMsg = "앱 등록 실패";
                            break;
                        case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
                            errorMsg = "BLE 기능 미지원";
                            break;
                        case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
                            errorMsg = "내부 오류";
                            break;
                        default:
                            errorMsg = "알 수 없는 오류";
                    }

                    Log.e("BLE_SCAN", "스캔 실패: " + errorMsg + " (code=" + errorCode + ")");
                    logData.add("[BLE_SCAN] 스캔 실패: " + errorMsg + " (code=" + errorCode + ")"); // 로그 뷰 나타내는 부분 추가
                    logAdapter.notifyDataSetChanged();
                    logListView.smoothScrollToPosition(logData.size() - 1);
                }
            };

            // Android 11 이하 GPS 체크
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                if (!isLocationEnabled()) {
                    logData.add("GPS 꺼져있음. 위치 서비스 켜야 스캔 가능");
                    logAdapter.notifyDataSetChanged();
                    logListView.smoothScrollToPosition(logData.size() - 1);
                    return;
                }
            }

            // 블루투스 상태 확인
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                logData.add("블루투스 꺼져있음");
                logAdapter.notifyDataSetChanged();
                return;
            }

            // 로그 초기화 (선택 사항)
            logData.clear();
            // 스캔 목록 초기화
            scanData.clear();

            logData.add("스캔 시작");
            logAdapter.notifyDataSetChanged();

            Toast.makeText(this, "scanning start", Toast.LENGTH_LONG);

            // UUID 필터 설정
            UUID serviceUUID = UUID.fromString("0000181A-0000-1000-8000-00805F9B34FB");

//            ScanFilter uuid_filter = new ScanFilter.Builder()
//                    .setServiceUuid(new ParcelUuid(serviceUUID)) // uuid로 필터링
//                    .build();

            List<ScanFilter> filters = new ArrayList<>();
//            filters.add(uuid_filter);

            for (String address : macList) { // mac 주소로 필터링
                ScanFilter filter = new ScanFilter.Builder()
                        .setDeviceAddress(address)
                        .setServiceUuid(new ParcelUuid(serviceUUID)) // uuid로 필터링
                        .build();
                filters.add(filter);
            }

            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // 패킷 수신율을 극대화하는, 저지연 스캔 모드 설정
                    .build();

            // 스캔 시작
            bluetoothLeScanner.startScan(filters, settings, scanCallback);

            // 상태 ON
            isScanning = true;
        });

        btnStop.setOnClickListener(v -> {
            btnScan.setEnabled(true); // 스캔 버튼 누를 수 있도록
            btnStop.setEnabled(false); // 스톱 버튼 누르지 않도록

            // 스캔 중이 아닐 때 방지
            if (!isScanning) {
                logData.add("스캔 중 아님");
                logAdapter.notifyDataSetChanged();
                return;
            }

            bluetoothLeScanner.stopScan(scanCallback);
            scanCallback = null;

            deviceList.clear();

            logData.add("스캔 중지");
            logAdapter.notifyDataSetChanged();

            Toast.makeText(this, "scanning stop", Toast.LENGTH_LONG);

            // 상태 OFF
            isScanning = false;
        });

        btnRefresh.setOnClickListener(v -> {
            scanData.clear();
            logData.clear();
            logData.add("리프레시");
            logAdapter.notifyDataSetChanged();
            historyData.clear();
            historyAdapter.notifyDataSetChanged();
        });

        btnSave.setOnClickListener(v -> {
            saveSensorDataToCsv();
        });

        checkPermission();

        // 뷰 관련 부분(mainactivity.java 만들 때 생성된 것)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    public void saveSensorDataToCsv() {  // 로그 뷰에 나타나게 하는 부분 추가
        if (sensorDataList.isEmpty()) {
            Log.d("CSV", "저장할 데이터 없음");
            logData.add("[CSV] 저장할 데이터 없음");
            logAdapter.notifyDataSetChanged();
            logListView.smoothScrollToPosition(logData.size() - 1);

            return;
        }

        try {
            File file = CsvWriter.writeCsv(this, sensorDataList);

            Log.d("CSV", "CSV 저장 성공: " + file.getAbsolutePath());
            logData.add("[CSV] CSV 저장 성공: " + file.getAbsolutePath());
            logAdapter.notifyDataSetChanged();
            logListView.smoothScrollToPosition(logData.size() - 1);
        } catch (Exception e) {
            Log.e("CSV", "CSV 저장 실패", e);
            logData.add("[CSV] CSV 저장 실패: " + e);
            logAdapter.notifyDataSetChanged();
            logListView.smoothScrollToPosition(logData.size() - 1);
        }
    }

    // 권한 체크
    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // 버전 31 이상
            int scanPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
            int connectPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);

            if (scanPermission != PackageManager.PERMISSION_GRANTED ||
                    connectPermission != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT
                        },
                        REQUEST_CODE
                );
            }

        }
        else {
            int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

            if (locationPermission != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE
                );
            }
        }

        // 네트워크 권한 요청 추가
        int internetPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        int networkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        if (internetPermission != PackageManager.PERMISSION_GRANTED ||
        networkPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE
                    },
                    REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) { // 성공/실패 팝업 메시지 추가
            case REQUEST_CODE:
                int i;
                for(i=0;i<grantResults.length;i++) {
                    int grantResult = grantResults[i];
                    String permission = permissions[i];
                    if(grantResult == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, permission + " 권한 실패", Toast.LENGTH_LONG);
                    }
                    else if(grantResult == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, permission + " 권한 성공", Toast.LENGTH_LONG);
                    }
                }
        }
    }

    public boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void handleScannedData(byte[] rawData, String deviceAddress, String deviceName, int rssi, String uuid) {
        double lat = 36.6287; // 기본값 (충북대)
        double lon = 127.4606;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // GPS 센서로부터 위치 측위 시도
            android.location.Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location == null) {
                // GPS가 안 잡히면 네트워크(와이파이/기지국) 기반 위치 측위 시도
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                Log.d("GPS", "실시간 위치 획득 성공: " + lat + ", " + lon);
            }
        }

        SensorData data = SensorParser.parse(rawData, deviceAddress, deviceName, rssi, uuid, lat, lon);

        if (data == null) { // 로그 뷰에 나타나게 하는 부분 추가
            Log.d("TEST", "파싱 실패");
            logData.add("[TEST] 파싱 실패");
            logAdapter.notifyDataSetChanged();
            logListView.smoothScrollToPosition(logData.size() - 1);
            return;
        }
        String display =
                "[" + deviceName + "]\n" +
                "온도 : " + data.getTemperature() + ", 습도 : " + data.getHumidity() + "\n" +
                        "AQI : " + data.getAqi() + ", TVOC : " + data.getTvoc() + ", eCo2 : " + data.getEco2() + "\n" +
                        "앱시간 : " + data.getTime() + "\n" + "센서시간 : " + data.getSensorTime();

        runOnUiThread(() -> {
                historyData.add(display);
                if (historyData.size() > 10){
                    historyData.remove(0);
                }
                historyAdapter.notifyDataSetChanged();
                historyListView.smoothScrollToPosition(historyData.size() - 1);

                addEntry(data.getTemperature(), data.getTvoc());
                // main thread(ui) 업데이트 위한 runonuithread

                });
        sensorDataList.add(data);

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID); // 기기 id 가져옴
        DataRequest request = NetworkModule.fromSensorData(data, deviceId, lat, lon);
        transferManager.executeDataTransfer(request);

        try {
            CsvWriter.appendRow(this, data);
        } catch (Exception e){
            Log.e("CSV", "실시간 저장 실패: " + e);
        }
    }
    private void addItemToScanList(String title, String desc) { // 리스트에 새로 추가하는 함수
        HashMap<String, String> newItem = new HashMap<>();
        newItem.put("title", title);
        newItem.put("subtitle", desc);
        scanData.add(newItem);

        scanAdapter.notifyDataSetChanged();
    }
    private void addItemToScanList(String title, String desc, int idx) { // 리스트의 기존 자리 수정하는 함수
        HashMap<String, String> newItem = new HashMap<>();
        newItem.put("title", title);
        newItem.put("subtitle", desc);
        scanData.set(idx, newItem);

        scanAdapter.notifyDataSetChanged();
    }

    private LineDataSet createDataSet(String label, int color) { // 데이터셋 설정
        LineDataSet set = new LineDataSet(new ArrayList<>(), label);
        set.setColor(color);
        set.setCircleColor(color);
        set.setDrawCircles(false); // 점 안 그림
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER); // 부드러운 곡선
        return set;
    }
    public void addEntry(float temp, int tvoc) { // 실시간 데이터 추가 함수
        LineData data = lineChart.getData();

        if(data != null) {
            data.addEntry(new Entry(data.getDataSetByIndex(0).getEntryCount(), temp),  0); // 0번 데이터셋(온도)의 맨끝에 temp 추가
            data.addEntry(new Entry(data.getDataSetByIndex(1).getEntryCount(), tvoc),  1); // 1번 데이터셋(tvoc)의 맨끝에 tvoc 추가

            // 차트에 데이터 변경 알림
            data.notifyDataChanged();
            lineChart.notifyDataSetChanged();

            // 20개까지만 화면에 보이게
            lineChart.setVisibleXRangeMaximum(20);
            lineChart.moveViewToX(data.getEntryCount());
        }
    }
}