package com.example.servicetest;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.renderscript.ScriptGroup;
import android.support.design.card.MaterialCardView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.servicetest.models.schedules.CheckSchedulePost;
import com.example.servicetest.retrofit.ApiClient;
import com.example.servicetest.retrofit.interfaces.auth.AuthInterface;
import com.example.servicetest.retrofit.interfaces.schedules.ScheduleInterface;
import com.example.servicetest.services.LocationService;
import com.example.servicetest.services.NetworkChangeReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    private View btnLogout;
    private MaterialCardView menuAbsence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setCancelable(true);
            builder.setTitle("Logout");
            builder.setMessage("Anda akan keluar dari aplikasi, apakah yakin?");
            builder.setPositiveButton("Terima",
                    (dialogInterface, i) -> {
                        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                        preferences.edit().putString("token", null).apply();

                        Toast toast = Toast.makeText(MainActivity.this, "Logout berhasil.", Toast.LENGTH_SHORT);
                        toast.show();

                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    });
            builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> { });

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        // Menu Absence
        menuAbsence = findViewById(R.id.menu_absence);
        menuAbsence.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, AbsencesActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        runNetworkChangeListerner();

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
        unregisterNetworkChanges();
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }
}