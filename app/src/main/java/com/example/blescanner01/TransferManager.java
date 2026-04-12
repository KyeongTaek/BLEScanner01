package com.example.blescanner01;


import android.content.Context;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransferManager {

    private static final String TAG = "TransferManager";

    private final Context context;
    private final ApiService apiService;

    public TransferManager(Context context) {
        this.context = context;
        this.apiService = NetworkModule
                .getRetrofit()
                .create(ApiService.class);
                //Retrofit이 ApiService 인터페이스를 실제 동작하는 객체로 만들어줌
    }

    public void executeDataTransfer(DataRequest data) {
        if (!NetworkModule.isNetworkAvailable(context)) { //현재 인터넷 연결 여부를 확인
            NetworkModule.showStatusDialog(
                    context,
                    "네트워크 오류",
                    "인터넷 연결 상태를 확인해주세요."
            );
            return;
        }


        Call<DataResponse> call = apiService.sendSensorData(data);

        // enqueue()를 이용한 비동기 통신 시작
        // 앱 화면을 멈추지 않고 서버 요청을 백그라운드에서 처리
        call.enqueue(new Callback<DataResponse>() {

            //서버가 응답 돌려줬을 때 실행
            @Override
            public void onResponse(Call<DataResponse> call, Response<DataResponse> response) {
                //역할 D 담당
                //응답 코드별 처리(성공, 실패 분기 / 다이얼로그 표시)
            }

            //서버와 통신 자체가 실패했을 때 실행함
            //타임아웃, 인터넷 끊김, 서버 다운 등의 이유
            @Override
            public void onFailure(Call<DataResponse> call, Throwable t) {
                //개발자 확인용
                Log.e(TAG, "요청 실패", t);

                //사용자 내용 다이얼로그
                NetworkModule.showStatusDialog(
                        context,
                        "통신 오류",
                        "요청 실패: " + t.getMessage()
                );
            }
        });
    }

}




// ApiService, DataRequest, DataRespoonse, NetworkModule.getRetrofit(), NetworkModule.isNetworkAvailable() 등은 별도의 파일에서 정의되어야 함.