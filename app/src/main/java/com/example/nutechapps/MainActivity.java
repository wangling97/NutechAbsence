package com.example.nutechapps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.nutechapps.models.schedules.CheckSchedulePost;
import com.example.nutechapps.models.schedules.ScheduleChannel;
import com.example.nutechapps.retrofit.ApiClient;
import com.example.nutechapps.retrofit.interfaces.schedules.ScheduleInterface;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    private ScheduleInterface scheduleInterface;
    protected LocationManager locationManager;
    private SharedPreferences preferences;

    private static final int ACCESS_FINE_LOCATION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        scheduleInterface = ApiClient.getRetrofit().create(ScheduleInterface.class);

        // Button Logout
        View btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setCancelable(true);
            builder.setTitle("Logout");
            builder.setMessage("You will exit the application, are you sure?");
            builder.setPositiveButton("Accept",
                    (dialogInterface, i) -> {
                        preferences.edit().putString("token", null).apply();

                        Toast toast = Toast.makeText(MainActivity.this, "Logout Success.", Toast.LENGTH_SHORT);
                        toast.show();

                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    });
            builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> { });

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        // Menu Absence
        MaterialCardView menuAbsence = findViewById(R.id.menu_absence);
        menuAbsence.setOnClickListener(view -> {
            showProgressDialog(MainActivity.this);
            if (checkGpsPermission(MainActivity.this)) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                    stopProgressDialog(MainActivity.this);
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_CODE);
                } else {
                    call_menu_absences();
                }
            } else {
                stopProgressDialog(MainActivity.this);
            }
        });

        // Menu Profile
        MaterialCardView menuProfile = findViewById(R.id.menu_profile);
        menuProfile.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
    }

    private void call_menu_absences() {
        String token = preferences.getString("token", null);

        Call<CheckSchedulePost> checkSchedulePostCall = scheduleInterface.checkSchedulePostCall(token);
        checkSchedulePostCall.enqueue(new Callback<CheckSchedulePost>() {
            @Override
            public void onResponse(Call<CheckSchedulePost> call, Response<CheckSchedulePost> response) {
                stopProgressDialog(MainActivity.this);

                if (response.code() == 200) {
                    if (response.body().getStatus()) {
                        preferences.edit().putString("schedule_id", response.body().getSchedulesModels().get(0).getId()).apply();

                        List<ScheduleChannel> scheduleChannels = response.body().getScheduleChannels();
                        ArrayList<ScheduleChannel> scheduleChannelsLists = new ArrayList<>(scheduleChannels);

                        Intent intent = new Intent(MainActivity.this, AbsencesActivity.class);
                        intent.putExtra("name", response.body().getSchedulesModels().get(0).getName());
                        intent.putExtra("division_name", response.body().getSchedulesModels().get(0).getDivision_name());
                        intent.putExtra("shift_start", response.body().getSchedulesModels().get(0).getSchedule_shift_start());
                        intent.putExtra("shift_end", response.body().getSchedulesModels().get(0).getSchedule_shift_end());
                        intent.putExtra("profile_pic", response.body().getSchedulesModels().get(0).getProfile_pic());
                        intent.putExtra("schedule_channels", scheduleChannelsLists);
                        startActivity(intent);
                    } else {
                        Toast toast = Toast.makeText(MainActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else {
                    try {
                        JSONObject object = new JSONObject(response.errorBody().string());
                        Toast toast = Toast.makeText(MainActivity.this, object.getString("message"), Toast.LENGTH_SHORT);
                        toast.show();

                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Toast toast = Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }

            @Override
            public void onFailure(Call<CheckSchedulePost> call, Throwable t) {
                stopProgressDialog(MainActivity.this);

                t.printStackTrace();
                Toast toast = Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_FINE_LOCATION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                call_menu_absences();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start Network listener
        runNetworkChangeListerner();

        // Validate token (if token not valid, redirect into LoginActivity)
        validateToken(status -> {
            if (!status) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop Network Listener
        unregisterNetworkChanges();
    }

    // Double back pressed to close the app
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast toast = Toast.makeText(MainActivity.this, "Please click BACK again to exit", Toast.LENGTH_SHORT);
        toast.show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }
}