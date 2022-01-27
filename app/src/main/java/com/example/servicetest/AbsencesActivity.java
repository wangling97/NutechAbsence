package com.example.servicetest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.design.card.MaterialCardView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.servicetest.models.BasicPost;
import com.example.servicetest.models.schedules.CheckSchedulePost;
import com.example.servicetest.retrofit.ApiClient;
import com.example.servicetest.retrofit.interfaces.schedules.ScheduleInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AbsencesActivity extends BaseActivity {

    private final String TAG = "AbsencesActivity";

    MaterialCardView absenceMasuk, absenceKeluar;

    private ScheduleInterface scheduleInterface;

    protected LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absences);

        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        scheduleInterface = ApiClient.getRetrofit().create(ScheduleInterface.class);

        absenceMasuk = findViewById(R.id.menu_absence_masuk);
        absenceMasuk.setOnClickListener(view -> {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isGPSEnabled && isNetworkEnabled) {
                doBindLocationService();

                AlertDialog.Builder builder = new AlertDialog.Builder(AbsencesActivity.this);
                builder.setCancelable(true);
                builder.setTitle("GPS Settings");
                builder.setMessage("GPS is not enabled. Do you want to go to settings menu?");
                builder.setPositiveButton("Settings",
                        (dialogInterface, i) -> {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            this.startActivity(intent);
                        });
                builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> {

                });
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setCancelable(true);
                builder.setTitle("GPS Settings");
                builder.setMessage("GPS is not enabled. Do you want to go to settings menu?");
                builder.setPositiveButton("Settings",
                        (dialogInterface, i) -> {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            this.startActivity(intent);
                        });
                builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> {

                });
            }
        });

        absenceKeluar = findViewById(R.id.menu_absence_keluar);
        absenceKeluar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doUnbindLocationService();
            }
        });

//        absenceMasuk.setOnClickListener(view -> {
//            doBindLocationService();
//            validateToken(status -> {
//                if (!status) {
//                    startActivity(new Intent(AbsencesActivity.this, LoginActivity.class));
//                    finish();
//                }
//            });
//
//            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
//
//            // getting GPS status
//            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//
//            // getting network status
//            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//            if (!isGPSEnabled && !isNetworkEnabled) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setCancelable(true);
//                builder.setTitle("GPS Settings");
//                builder.setMessage("GPS is not enabled. Do you want to go to settings menu?");
//                builder.setPositiveButton("Settings",
//                        (dialogInterface, i) -> {
//                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                            this.startActivity(intent);
//                        });
//                builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> {
//
//                });
//            } else {
//                if (ActivityCompat.checkSelfPermission(AbsencesActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AbsencesActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(AbsencesActivity.this, new String[]{
//                            android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
//                    }, 101);
//                } else {
//                    Call<CheckSchedulePost> checkSchedulePostCall = scheduleInterface.checkSchedulePostCall(token);
//                    checkSchedulePostCall.enqueue(new Callback<CheckSchedulePost>() {
//                        @Override
//                        public void onResponse(Call<CheckSchedulePost> call, Response<CheckSchedulePost> response) {
//                            if (response.code() == 200) {
//                                String schedule_id = preferences.getString("schedule_id", null);
//
//                                if(schedule_id == null) {
//                                    if (response.body().getStatus()) {
//                                        preferences.edit().putString("schedule_id", response.body().getSchedulesModels().get(0).getId()).apply();
//
//                                        startActivity(new Intent(AbsencesActivity.this, AbsencesActivity.class));
//                                    } else {
//                                        Toast toast = Toast.makeText(AbsencesActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT);
//                                        toast.show();
//                                    }
//                                } else {
//                                    startActivity(new Intent(AbsencesActivity.this, AbsencesActivity.class));
//                                }
//                            } else {
//                                try {
//                                    JSONObject object = new JSONObject(response.errorBody().string());
//                                    if (object.getInt("error_code") == 401) {
//                                        Toast toast = Toast.makeText(AbsencesActivity.this, object.getString("message"), Toast.LENGTH_SHORT);
//                                        toast.show();
//
//                                        startActivity(new Intent(AbsencesActivity.this, LoginActivity.class));
//                                        finish();
//                                    }
//                                } catch (IOException | JSONException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<CheckSchedulePost> call, Throwable t) {
//                            t.printStackTrace();
//                        }
//                    });
//                }
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        runNetworkChangeListerner();

        validateToken(status -> {
            if (!status) {
                startActivity(new Intent(AbsencesActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterNetworkChanges();
    }
}