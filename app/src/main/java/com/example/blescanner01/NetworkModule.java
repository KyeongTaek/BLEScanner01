package com.example.blescanner01;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.*;
import retrofit2.converter.scalars.*;



public class NetworkModule {
    // 싱글톤(전체에 retrofit 객체 단 한 개) 위한 retrofit 변수 선언
    private static Retrofit retrofit;

    private NetworkModule() {} // 외부에서의 생성자 사용 방지 위해 private으로 선언
    public static Retrofit getRetrofit() { // retrofit 객체 반환 메서드(싱글톤 유지)
        if (retrofit == null) { // 객체가 없는 경우에만 생성 작업
            Gson gson = new GsonBuilder().setLenient().create(); // json을 java object로 바꿔주고, 반대도 변환해주는 gson. json 파싱할 때 느슨한 규칙(lenient) 적용

            retrofit = new Retrofit.Builder()
                    .baseUrl("http://203.255.81.72:10024/") // 기본 url을 설정
                    .client(provideOkHttpClient()) // 실질적인 네트워크 작업을 담당하는 부분을 약간 수정해서, retrofit과 연결
                    .addConverterFactory(ScalarsConverterFactory.create()) // response를 string 형태로 받을 때 처리하는 애 지정(에러 처리 위해 먼저)
                    .addConverterFactory(GsonConverterFactory.create(gson)) // response를 json 형태로 받을 때 처리하는 애 지정
                    .build();
        }

        return retrofit;
    }

    // OkHttpClient: 데이터를 실제로 전송하는 부분을 담당(연결 시간 제한, 로그 기록 등)
    public static OkHttpClient provideOkHttpClient() { // OkHttpClient를 약간 수정해서 반환하는 함수
        // 인터셉터가 http 통신 로그를 출력하는 부분
        HttpLoggingInterceptor.Logger customLogger = new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) { // Logcat에 태그를 team5httpLog로 설정해 출력하도록 설정
                Log.d("team5httpLog :", message);
            }
        };

        // 수정한 Logger를 기반으로 하는 인터셉터 생성
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(customLogger);
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 로그에 출력할 정보를 최대한 많이(헤더 요청/응답의 본문 포함)

        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor) // 설정한 인터셉터 추가
                .connectTimeout(10, TimeUnit.SECONDS) // 10초 안에 연결 안되면 에러 로그 출력(연결 시간 제한)
                .readTimeout(15, TimeUnit.SECONDS) // 연결 후, 15초 안에 응답 패킷 못 받으면 에러 로그 출력(읽기 시간 제한)
                .writeTimeout(15, TimeUnit.SECONDS) // 15초 안에 패킷 못 보내면 에러 로그 출력(쓰기 시간 제한)
                .build();
    }
    public static void showStatusDialog(Context cont, String title, String message) { // 통신 결과를 보여주는, 확인버튼만 있는 dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(cont); // activity 컨텍스트를 받아, UI를 띄워주는 틀.
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // 확인 버튼 누르면 창 닫음
                    }
                })
                .create()
                .show();
    }

    public static boolean isNetworkAvailable(Context cont) { // 통신 전 연결 상태 확인하는 함수
        ConnectivityManager connectivityManager = (ConnectivityManager) cont.getSystemService(Context.CONNECTIVITY_SERVICE); // 연결과 관련된 서비스를 모아 관리하는 매니저 생성
        if (connectivityManager != null) { // 매니저가 있는 경우에
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork()); // 현재 사용 중인 네트워크 객체 가져와서, 해당 네트워크가 가진 속도, 대역폭 등 구체적인 속성 담은 NetworkCapabilities 객체 반환.
            if (capabilities != null) { // 현재 사용 중인 네트워크가 있는 경우에
                return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || // wifi를 통해 데이터를 보낼 수 있거나
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR); // 모바일 데이터를 통해 데이터를 보낼 수 있는지 확인해 true/false 반환.
            }
        }

        return false;
    }
}
