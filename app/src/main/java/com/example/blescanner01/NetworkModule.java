package com.example.blescanner01;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
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
    // 싱글톤(전체에 retrofit 객체 단 한개) 위한 retrofit 변수 선언
    private static Retrofit retrofit;

    private NetworkModule() {} // 외부에서의 생성자 사용 방지 
    public static Retrofit getRetrofit() { // retrofit 객체 반환 메서드
        if (retrofit == null) {
            Gson gson = new GsonBuilder().setLenient().create();

            retrofit = new Retrofit.Builder()
                    .baseUrl("http://203.255.81.72:10024/")
                    .client(provideOkHttpClient())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit;
    }
    public static OkHttpClient provideOkHttpClient() {
        // 인터셉터가 http 통신 로그를 출력하는 방식을 오버라이딩
        HttpLoggingInterceptor.Logger customLogger = new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) { // 로그로 출력하도록 설정
                Log.d("team5httpLog :", message);
            }
        };

        // 인터셉터 생성
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(customLogger);
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 로그에 출력할 정보를 최대한 많이(헤더 요청/응답의 본문 포함)

        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor) // 설정한 인터셉터 추가
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }
    public static void showStatusDialog(Context cont, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(cont);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    public static boolean isNetworkAvailable(Context cont) {
        ConnectivityManager connectivityManager = (ConnectivityManager) cont.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (capabilities != null) {
                return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            }
        }

        return false;
    }
}
