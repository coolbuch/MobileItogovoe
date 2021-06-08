package com.example.googlemapphonefinding;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIService {
    @POST("setpoint/")
    @Headers({"Content-Type: application/json"})
    Call<Response<String >> setCoords(@Body String st);


    @POST("reg/")
    @Headers({"Content-Type: application/json"})
    Call<Response<String>> reg(@Body String st);
}
