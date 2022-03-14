package com.example.nutechapps.retrofit.interfaces.auth;

import com.example.nutechapps.models.BasicPost;
import com.example.nutechapps.models.auth.AuthPost;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthInterface {

    @Headers("Content-Type: application/json")
    @POST("registration_device")
    Call<BasicPost> registrationDevicePostCall(@Body String body);

    @Headers("Content-Type: application/json")
    @POST("auth/login")
    Call<AuthPost> authPostCall(@Body String body);

    @Headers("Content-Type: application/json")
    @POST("auth/token_validate")
    Call<AuthPost> tokenPostCall(@Header("Authorization") String token);

}
