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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.servicetest.models.auth.AuthPost;
import com.example.servicetest.retrofit.ApiClient;
import com.example.servicetest.retrofit.interfaces.auth.AuthInterface;
import com.example.servicetest.retrofit.interfaces.auth.TokenCallback;
import com.example.servicetest.services.LocationService;
import com.example.servicetest.services.NetworkChangeReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaseActivity extends AppCompatActivity {

    private String TAG = "BaseActivity";

    // Validate Token
    public void validateToken(final TokenCallback callback) {
        AuthInterface authInterface;
        authInterface = ApiClient.getRetrofit().create(AuthInterface.class);

        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        Boolean validate = false;

        Call<AuthPost> tokenPostCall = authInterface.tokenPostCall(token);
        tokenPostCall.enqueue(new Callback<AuthPost>() {
            @Override
            public void onResponse(Call<AuthPost> call, Response<AuthPost> response) {
                if (response.code() == 200) {
                    callback.onResponse(true);
                } else {
                    try {
                        JSONObject object = new JSONObject(response.errorBody().string());

                        if(object.getBoolean("status")) {
                            Toast toast = Toast.makeText(getApplicationContext(), object.getString("message"), Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        callback.onResponse(false);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        callback.onResponse(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthPost> call, Throwable t) {
                t.printStackTrace();
                callback.onResponse(false);
            }
        });
    }
    // end validate token

    // Network Listener
    private BroadcastReceiver mNetworkReceiver;

    public void runNetworkChangeListerner() {
        mNetworkReceiver = new NetworkChangeReceiver();
        registerNetworkBroadcastForNougat();
    }

    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
    // End Network Listener

    // Location Service
    private boolean mLocationServiceUnbind;
    private LocationService mBoundLocationService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBoundLocationService = ((LocationService.LocalBinder)iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBoundLocationService = null;
        }
    };

    public void doBindLocationService() {
        if (bindService(new Intent(this, LocationService.class), serviceConnection, Context.BIND_AUTO_CREATE)) {
            mLocationServiceUnbind = true;
        } else {
            Log.e(TAG, "Error: The requested service doesn't " + "exist, or this client isn't allowed access to it.");
        }
    }

    public void doUnbindLocationService() {
        if (mLocationServiceUnbind) {
            unbindService(serviceConnection);
            mLocationServiceUnbind = false;
        }
    }
    // End Location Service

    // Check Mock Setting
    public boolean isMockSettingsON() {
        if (Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0")) {
            return false;
        }
        else {
            Toast toast = Toast.makeText(this, R.string.mock_setting, Toast.LENGTH_SHORT);
            toast.show();
            return true;
        }
    }
    // End Check Mock Setting

    // Check Mock App
    protected ArrayList<String> whiteListPackage = new ArrayList<>();

    public boolean areThereMockPermissionApps() {
        int count = 0;

        // Set Whitelist Package
        whiteListPackage.add("com.example.nutech");

        PackageManager pm = this.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);

                // Get Permissions
                String[] requestedPermissions = packageInfo.requestedPermissions;

                if (requestedPermissions != null) {
                    for (int i = 0; i < requestedPermissions.length; i++) {
                        if (requestedPermissions[i].equals("android.permission.ACCESS_MOCK_LOCATION") && !applicationInfo.packageName.equals(this.getPackageName())) {
                            if (!whiteListPackage.contains(this.getPackageName())) {
                                count++;
                            }
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("Got exception " , e.getMessage());
            }
        }

        if (count > 0) {
            Toast toast = Toast.makeText(this, R.string.mock_apps, Toast.LENGTH_SHORT);
            toast.show();
            return true;
        } else {
            return false;
        }
    }
    // End Check Mock App
}
