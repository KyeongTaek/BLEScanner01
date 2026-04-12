package com.example.blescanner01;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

import com.google.gson.annotations.SerializedName;

public class NetworkModule {

    private static final String BASE_URL = "http://203.255.81.72:10021/";
    private static Retrofit retrofit = null;


    // retrofit 인스턴스 반환 함수
    public static Retrofit getRetrofit(){
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory((ScalarsConverterFactory.create()))
                    .addConverterFactory((GsonConverterFactory.create()))
                    .build();

        }
        return retrofit;
    }

}

class DataRequest{
    @SerializedName("key")       private String key;
    @SerializedName("team")      private String team;
    @SerializedName("sensor")    private String sensor;
    @SerializedName("mac")       private String mac;
    @SerializedName("temp")      private double temp;
    @SerializedName("humidity")  private double humidity;
    @SerializedName("AQI")       private int aqi;
    @SerializedName("TVOC")      private int tvoc;
    @SerializedName("eCO2")      private int eco2;
    @SerializedName("timestamp") private long timestamp;
    @SerializedName("lat")       private double lat;
    @SerializedName("lon")       private double lon;
    @SerializedName("sender")    private String sender;

    public DataRequest(String key, String team, String sensor, String mac,
                       double temp, double humidity, int aqi, int tvoc, int eco2,
                       long timestamp, double lat, double lon, String sender) {
        this.key = key; this.team = team; this.sensor = sensor; this.mac = mac;
        this.temp = temp; this.humidity = humidity; this.aqi = aqi;
        this.tvoc = tvoc; this.eco2 = eco2; this.timestamp = timestamp;
        this.lat = lat; this.lon = lon; this.sender = sender;
    }
}

class DataResponse{
    @SerializedName("result")        private String result;
    @SerializedName("message")       private String message;
    @SerializedName("received_data") private ReceivedData receivedData;

    public String getResult()  { return result; }
    public String getMessage() { return message; }

    static class ReceivedData {
        @SerializedName("team")   private String team;
        @SerializedName("sensor") private String sensor;
    }
}
interface ApiService{
    @POST("sensor/opensrc/test/")
    Call<DataResponse> sendSensorData(@Body DataRequest data);

}
