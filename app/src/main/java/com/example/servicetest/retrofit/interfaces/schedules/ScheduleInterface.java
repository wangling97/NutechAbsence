package com.example.servicetest.retrofit.interfaces.schedules;

import com.example.servicetest.models.BasicPost;
import com.example.servicetest.models.auth.AuthPost;
import com.example.servicetest.models.schedules.CheckSchedulePost;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ScheduleInterface {

    @Headers("Content-Type: application/json")
    @POST("schedules/today_schedule")
    Call<CheckSchedulePost> checkSchedulePostCall(@Header("Authorization") String token);

    @Headers("Content-Type: application/json")
    @POST("schedules/absence_in_check")
    Call<BasicPost> absenceInCheckPostCall(@Header("Authorization") String token, @Body String body);
}
