package com.example.nutechapps;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.nutechapps.models.auth.AuthPost;
import com.example.nutechapps.retrofit.ApiClient;
import com.example.nutechapps.retrofit.interfaces.auth.AuthInterface;
import com.example.nutechapps.retrofit.interfaces.auth.TokenCallback;
import com.example.nutechapps.services.LocationService;
import com.example.nutechapps.services.NetworkChangeReceiver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaseActivity extends AppCompatActivity {

    private final String TAG = "BaseActivity";

    public String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getAndroidSdk() {
        int sdkVersion = Build.VERSION.SDK_INT;
        return String.valueOf(sdkVersion);
    }

    public String getAndroidRelease() {
        return Build.VERSION.RELEASE;
    }

    public String getDeviceModel() {
        return android.os.Build.DEVICE;
    }

    public String getVersionApp() {
        return BuildConfig.VERSION_NAME;
    }

    public static boolean isRooted(Context context) {
        boolean isEmulator = isEmulator(context);
        String buildTags = Build.TAGS;
        if (!isEmulator && buildTags != null && buildTags.contains("test-keys")) {
            return true;
        } else {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                return true;
            } else {
                file = new File("/system/xbin/su");
                return !isEmulator && file.exists();
            }
        }
    }

    private static boolean isEmulator(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), "android_id");
        return "sdk".equals(Build.PRODUCT) || "google_sdk".equals(Build.PRODUCT) || androidId == null;
    }

    public void detectMemoriFull(Context context) {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        } else {
            bytesAvailable = (long)stat.getBlockSize() * (long)stat.getAvailableBlocks();
        }
        long megAvailable = bytesAvailable / (1024 * 1024);

        if (megAvailable < 100) {
            showToast(context, "Peringatan: Sepertinya memori internal Anda hampir penuh, mohon periksa kapasitas memori internal Anda terlebih dahulu", "long");
        }
    }

    // Show Toast
    public void showToast(Context context, String msg, String length) {
        if (length.equals("long"))
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    // Show alert to enable gps
    public void showSettingsAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Pengaturan GPS");
        builder.setMessage("GPS tidak diaktifkan. Apakah Anda ingin pergi ke menu pengaturan?");
        builder.setPositiveButton("Settings",
                (dialogInterface, i) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    this.startActivity(intent);
                });
        builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> { });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean checkGooglePlayServices(Activity activity) {
        final int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            Log.d("PlayService", GoogleApiAvailability.getInstance().getErrorString(status));

            // ask user to update google play services.
            // show your own AlertDialog for example:
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            // set the message
            builder.setMessage("Your Google Play Service is outdated, please update your Google Play Service for better experience")
                    .setTitle("Do you want to update?"); // set a title

            builder.setPositiveButton("Update", (dialog, id) -> {
                // User clicked OK button
                final String appPackageName = "com.google.android.gms";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                }catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            });
            builder.setNegativeButton("Cancel", (dialog, id) -> {
                // User cancelled the dialog
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        } else {
            Log.d("PlayService", GoogleApiAvailability.getInstance().getErrorString(status));
            // google play services is updated.
            return true;
        }
    }

    // Function to check mock setting
    public boolean isMockSettingsON() {
        if (Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0")) {
            return false;
        }
        else {
            Toast toast = Toast.makeText(this, "Anda mencoba memalsukan lokasi GPS.", Toast.LENGTH_SHORT);
            toast.show();
            return true;
        }
    }
    // End of function to check mock setting


    /*****************************
     * Function to validate token
     *****************************/
    public void validateToken(final TokenCallback callback) {
        // Set ApiClient for authInterface
        AuthInterface authInterface = ApiClient.getRetrofit().create(AuthInterface.class);

        // Call SharedPreferences to get token string
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        // Call token validation endpoint and send current token from SharedPreferences
        Call<AuthPost> tokenPostCall = authInterface.tokenPostCall(token);
        tokenPostCall.enqueue(new Callback<AuthPost>() {
            @Override
            public void onResponse(Call<AuthPost> call, Response<AuthPost> response) {
                if (response.code() == 200) {
                    // if token still valid return true value
                    callback.onResponse(true);
                } else {
                    // if token not valid return false value and show message
                    try {
                        JSONObject object = new JSONObject(response.errorBody().string());

                        // endpoint will return true status with code 401 if token just expired
                        if(object.getBoolean("status")) {
                            showToast(getApplicationContext(), object.getString("message"), "short");
                        }

                        callback.onResponse(false);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        showToast(getApplicationContext(), e.getMessage(), "short");
                        callback.onResponse(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthPost> call, Throwable t) {
                t.printStackTrace();
                showToast(getApplicationContext(), t.getMessage(), "short");
                callback.onResponse(false);
            }
        });
    }
    /************************************
     * End of function to validate token
     ************************************/


    /*********************************
     * Start Network Listener Process
     *********************************/
    private BroadcastReceiver mNetworkReceiver;

    // Function to start network listener
    public void runNetworkChangeListerner() {
        mNetworkReceiver = new NetworkChangeReceiver();
        registerNetworkBroadcast();
    }

    private void registerNetworkBroadcast() {
        registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    // Function to stop network listener
    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            showToast(this, "unregisterNetworkChanges: " + e.getMessage(), "short");
        }
    }
    /*******************************
     * End Network Listener Process
     *******************************/

    protected LocationManager locationManager;

    public boolean checkGpsPermission(Context context) {
        try {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                showSettingsAlert();
                return false;
            } else {
                if(!areThereMockPermissionApps()){
                    if (!isMockSettingsON()) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            showToast(context, e.getMessage(), "short");
            return false;
        }
    }

    /*********************************
     * Start Location Service Process
     *********************************/
    private boolean mLocationServiceUnbind;
    private LocationService mBoundLocationService;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBoundLocationService = ((LocationService.LocalBinder)iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBoundLocationService = null;
        }
    };

    // Function to start tracking Lat and Long User
    public void doBindLocationService() {
        if (bindService(new Intent(this, LocationService.class), serviceConnection, Context.BIND_AUTO_CREATE)) {
            mLocationServiceUnbind = true;
        } else {
            Log.e(TAG, "Error: The requested service doesn't " + "exist, or this client isn't allowed access to it.");
        }
    }

    // Function to stop tracking Lat and Long User
    public void doUnbindLocationService() {
        if (mLocationServiceUnbind) {
            unbindService(serviceConnection);
            mLocationServiceUnbind = false;
        }
    }
    /*******************************
     * End Location Service Process
     *******************************/


    /******************************************
     * Process to check app using mock setting
     ******************************************/
    protected ArrayList<String> whiteListPackage = new ArrayList<>();
    protected ArrayList<String> blockedApps = new ArrayList<>();

    public boolean areThereMockPermissionApps() {
        int count = 0;

        // Set Whitelist Package
        whiteListPackage.add("com.example.nutech");
        whiteListPackage.add("com.nutech.app.maintenance");
        whiteListPackage.add("com.dev.nutech");
        whiteListPackage.add("com.example.servicetest");

        PackageManager pm = this.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);

                // Get Permissions
                String[] requestedPermissions = packageInfo.requestedPermissions;

                if (requestedPermissions != null) {
                    for (String requestedPermission : requestedPermissions) {
                        if (requestedPermission.equals("android.permission.ACCESS_MOCK_LOCATION") && !applicationInfo.packageName.equals(this.getPackageName())) {
                            if (!whiteListPackage.contains(this.getPackageName())) {
                                blockedApps.add(this.getPackageName());
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
            showToast(this, "Ponsel Anda mungkin memiliki Aplikasi GPS Palsu. (" + blockedApps.toString() + ")", "short");
            return true;
        } else {
            return false;
        }
    }
    /*************************************************
     * End of process to check app using mock setting
     *************************************************/


    /**********************************
     * Progress Dialog or Progress Bar
     **********************************/
    public void showProgressDialog(Context context) {
        ProgressBar progressBar = ((AppCompatActivity)context).findViewById(R.id.progressBar);
        LinearLayout progressBarBG = ((AppCompatActivity)context).findViewById(R.id.progressBarBG);
        progressBar.setVisibility(View.VISIBLE);
        progressBarBG.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void stopProgressDialog(Context context) {
        ProgressBar progressBar = ((AppCompatActivity)context).findViewById(R.id.progressBar);
        LinearLayout progressBarBG = ((AppCompatActivity)context).findViewById(R.id.progressBarBG);
        progressBar.setVisibility(View.GONE);
        progressBarBG.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    /**********************************
     * End Progress Dialog or Progress Bar
     **********************************/

}
