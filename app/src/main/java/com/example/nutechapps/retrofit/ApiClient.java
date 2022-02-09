package com.example.nutechapps.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {

    public static final String BASE_URL = "http://103.135.215.100/dev/api/v1/mobile/";
    public static Retrofit retrofit;

    public static Retrofit getRetrofit() {
//        Gson gson = new GsonBuilder()
//                .setLenient()
//                .create();

        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
