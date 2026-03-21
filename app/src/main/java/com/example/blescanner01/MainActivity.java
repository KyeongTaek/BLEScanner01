package com.example.blescanner01;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.location.LocationManager;

import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import java.util.ArrayList;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.ParcelUuid;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button btnScan, btnStop, btnRefresh, btnSave;
    ListView logListView;

    ArrayList<String> logData;
    ArrayAdapter<String> logAdapter;

    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    ScanCallback scanCallback;

    // 스캔 상태 관리 변수 추가
    boolean isScanning = false;

    private static final int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        btnScan = findViewById(R.id.button);
        btnStop = findViewById(R.id.button2);
        btnRefresh = findViewById(R.id.button3);
        btnSave = findViewById(R.id.button4);

        logListView = findViewById(R.id.log_list);

        logData = new ArrayList<>();
        logAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, logData);
        logListView.setAdapter(logAdapter);

        // BLE 스캔 콜백 (전역으로 사용)
        scanCallback = new ScanCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onScanResult(int callbackType, ScanResult result) {

                BluetoothDevice device = result.getDevice();
                String deviceName = device.getName();
                if (deviceName == null) deviceName = "이름 없음";

                // Raw 데이터 추출
                byte[] scanData = null;
                if (result.getScanRecord() != null) {
                    scanData = result.getScanRecord().getBytes();
                }

                String rawDataString;

                if (scanData != null) {
                    StringBuilder sb = new StringBuilder();
                    for (byte b : scanData) {
                        sb.append(String.format("%02X ", b));
                    }
                    rawDataString = sb.toString();
                } else {
                    rawDataString = "데이터 없음";
                }

                logData.add("[" + deviceName + "] " + rawDataString);
                logAdapter.notifyDataSetChanged();
                logListView.smoothScrollToPosition(logData.size() - 1);
            }
        };

        btnScan.setOnClickListener(v -> {

            // 이미 스캔 중이면 중복 실행 방지
            if (isScanning) {
                logData.add("이미 스캔 중");
                logAdapter.notifyDataSetChanged();
                return;
            }

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

            logData.add("스캔 시작");
            logAdapter.notifyDataSetChanged();

            // UUID 필터 설정
            UUID serviceUUID = UUID.fromString("0000181A-0000-1000-8000-00805F9B34FB");

            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(serviceUUID))
                    .build();

            List<ScanFilter> filters = new ArrayList<>();
            filters.add(filter);

            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();

            // 스캔 시작
            bluetoothLeScanner.startScan(filters, settings, scanCallback);

            // 상태 ON
            isScanning = true;
        });

        btnStop.setOnClickListener(v -> {

            // 스캔 중이 아닐 때 방지
            if (!isScanning) {
                logData.add("스캔 중 아님");
                logAdapter.notifyDataSetChanged();
                return;
            }

            bluetoothLeScanner.stopScan(scanCallback);

            logData.add("스캔 중지");
            logAdapter.notifyDataSetChanged();

            // 상태 OFF
            isScanning = false;
        });

        btnRefresh.setOnClickListener(v -> {
            logData.clear();
            logData.add("리프레시");
            logAdapter.notifyDataSetChanged();
        });

        btnSave.setOnClickListener(v -> {
            logData.add("저장");
            logAdapter.notifyDataSetChanged();
        });

        checkPermission();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 권한 체크
    public void checkPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

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

        } else {

            int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

            if (locationPermission != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}