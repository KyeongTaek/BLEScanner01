package com.example.blescanner01;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;
import retrofit2.Response;
public class ResponseHandler {
    private Context context;
    private final String TAG = "ResponseHandler";

    public ResponseHandler(Context context) {
        this.context = context;
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    public String getSenderUuid() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    public void handleResponse(Response<DataResponse> response, ResponseCallback callback) {
        if (response.isSuccessful() && response.body() != null) {
            DataResponse dataResponse = response.body();

            // 강의자료 24p 응답 양식 확인
            if ("Success".equals(dataResponse.getResult())) {
                callback.onSuccess("전송 완료: " + dataResponse.getMessage());
            } else {
                callback.onFailure("서버 실패: " + dataResponse.getMessage());
            }
        } else {

            String errorMsg;
            switch (response.code()) {
                case 400: errorMsg = "잘못된 요청 (데이터 형식을 확인하세요)"; break;
                case 404: errorMsg = "서버 경로를 찾을 수 없습니다 (404)"; break;
                case 500: errorMsg = "서버 내부 오류 발생 (500)"; break;
                default: errorMsg = "통신 에러 (Code: " + response.code() + ")"; break;
            }
            callback.onFailure(errorMsg);
        }
    }

    // [담당 D] 네트워크 실패 처리
    public void handleFailure(Throwable t, ResponseCallback callback) {
        Log.e(TAG, "Network Failure: " + t.getMessage());
        callback.onFailure("네트워크 연결 실패: 서버에 접속할 수 없습니다.");
    }

    // A의 다이얼로그와 연결하기 위한 인터페이스
    public interface ResponseCallback {
        void onSuccess(String message);
        void onFailure(String message);
    }
}
