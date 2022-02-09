package com.example.nutechapps.retrofit.interfaces.schedules;

import com.example.nutechapps.models.BasicPost;
import com.example.nutechapps.models.schedules.CheckSchedulePost;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ScheduleInterface {

    @Headers("Content-Type: application/json")
    @POST("schedules/today_schedule")
    Call<CheckSchedulePost> checkSchedulePostCall(@Header("Authorization") String token);

    @Headers("Content-Type: application/json")
    @POST("schedules/absence_in_check")
    Call<BasicPost> absenceInCheckPostCall(@Header("Authorization") String token, @Body String body);

    @Multipart
    @POST("schedules/submit_absence_in")
    Call<BasicPost> submitAbsenceInPostCall(@Header("Authorization") String token,
                                            @Part("schedule_id") RequestBody schedule_id,
                                            @Part("latitude") RequestBody latitude,
                                            @Part("longitude") RequestBody longitude,
                                            @Part("note") RequestBody note,
                                            @Part("status") RequestBody status,
                                            @Part MultipartBody.Part part);

    // Absence Out
    @Headers("Content-Type: application/json")
    @POST("schedules/absence_out_check")
    Call<BasicPost> absenceOutCheckPostCall(@Header("Authorization") String token, @Body String body);

    @Multipart
    @POST("schedules/absence_out")
    Call<BasicPost> submitAbsenceOutPostCall(
                                            @Header("Authorization") String token,
                                            @Part("schedule_id") RequestBody schedule_id,
                                            @Part("latitude") RequestBody latitude,
                                            @Part("longitude") RequestBody longitude,
                                            @Part MultipartBody.Part part
    );
}
