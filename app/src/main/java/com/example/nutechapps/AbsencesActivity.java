package com.example.nutechapps;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.card.MaterialCardView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nutechapps.adapters.SchedulesChannelAdapter;
import com.example.nutechapps.models.BasicPost;
import com.example.nutechapps.models.schedules.ScheduleChannel;
import com.example.nutechapps.retrofit.ApiClient;
import com.example.nutechapps.retrofit.interfaces.schedules.ScheduleInterface;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AbsencesActivity extends BaseActivity {

    private final String TAG = "AbsencesActivity";

    private ScheduleInterface scheduleInterface;
    protected LocationManager locationManager;
    private SharedPreferences preferences;
    private RecyclerView recyclerView;

    private static final int CAMERA_PERM_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;
    private File imageFile,compressImage;
    private String currentPhotoPath, schedule_id, latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absences);

        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        scheduleInterface = ApiClient.getRetrofit().create(ScheduleInterface.class);

        String name = getIntent().getStringExtra("name");
        String division_name = getIntent().getStringExtra("division_name");
        String shift_start = getIntent().getStringExtra("shift_start");
        String shift_end = getIntent().getStringExtra("shift_end");
        String profile_pic = getIntent().getStringExtra("profile_pic");

        List<ScheduleChannel> schedule_channels = (ArrayList<ScheduleChannel>) getIntent().getSerializableExtra("schedule_channels");
        SchedulesChannelAdapter adapter = new SchedulesChannelAdapter(schedule_channels);
        recyclerView = findViewById(R.id.recyclerAbsenceLocation);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (profile_pic != "") {
            ImageView absencePic = findViewById(R.id.absencePic);
            Picasso.get().load(profile_pic).into(absencePic);
        }

        TextView absenceUser = findViewById(R.id.absenceUser);
        absenceUser.setText(name);

        TextView absenceUnit = findViewById(R.id.absenceUnit);
        absenceUnit.setText(division_name);

        TextView absenceTime = findViewById(R.id.absenceTime);
        absenceTime.setText(String.format("Jam Kerja : %s - %s", shift_start, shift_end));

        View btnBackAbsence = findViewById(R.id.btn_back_absence);
        btnBackAbsence.setOnClickListener(view -> {
            startActivity(new Intent(AbsencesActivity.this, MainActivity.class));
            finish();
        });

        MaterialCardView absenceMasuk = findViewById(R.id.menu_absence_masuk);
        absenceMasuk.setOnClickListener(view -> {
            showProgressDialog(AbsencesActivity.this);
            try {
                locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

                // getting GPS status
                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                // getting network status
                boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    stopProgressDialog(AbsencesActivity.this);
                    showSettingsAlert();
                } else {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                        stopProgressDialog(AbsencesActivity.this);
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                    } else {
                        if(!areThereMockPermissionApps()) {
                            if (!isMockSettingsON()) {
                                String token = preferences.getString("token", null);
                                schedule_id = preferences.getString("schedule_id", null);

                                JSONObject payload = new JSONObject();
                                try {
                                    payload.put("schedule_id", schedule_id);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    showToast(AbsencesActivity.this, e.getMessage(), "short");
                                }

                                Call<BasicPost> absenInCheckPostCall = scheduleInterface.absenceInCheckPostCall(token, payload.toString());
                                absenInCheckPostCall.enqueue(new Callback<BasicPost>() {
                                    @Override
                                    public void onResponse(Call<BasicPost> call, Response<BasicPost> response) {
                                        stopProgressDialog(AbsencesActivity.this);

                                        if (response.code() == 200) {
                                            if (response.body().getStatus()) {
                                                startActivity(new Intent(AbsencesActivity.this, AbsenceInActivity.class));
                                            } else {
                                                Toast toast = Toast.makeText(AbsencesActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT);
                                                toast.show();
                                            }
                                        } else {
                                            try {
                                                JSONObject object = new JSONObject(response.errorBody().string());
                                                Toast toast = Toast.makeText(AbsencesActivity.this, object.getString("message"), Toast.LENGTH_SHORT);
                                                toast.show();

                                                startActivity(new Intent(AbsencesActivity.this, LoginActivity.class));
                                                finish();
                                            } catch (IOException | JSONException e) {
                                                e.printStackTrace();
                                                showToast(AbsencesActivity.this, e.getMessage(), "short");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<BasicPost> call, Throwable t) {
                                        stopProgressDialog(AbsencesActivity.this);

                                        t.printStackTrace();
                                        showToast(AbsencesActivity.this, t.getMessage(), "short");
                                    }
                                });
                            } else {
                                stopProgressDialog(AbsencesActivity.this);
                            }
                        } else {
                            stopProgressDialog(AbsencesActivity.this);
                        }
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
                showToast(AbsencesActivity.this, e.getMessage(), "short");
            }
        });

        // Absence Out
        MaterialCardView absenceKeluar = findViewById(R.id.menu_absence_keluar);
        absenceKeluar.setOnClickListener(v -> {
            showProgressDialog(AbsencesActivity.this);
            try {
                locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

                // getting GPS status
                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                // getting network status
                boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    stopProgressDialog(AbsencesActivity.this);
                    showSettingsAlert();
                } else {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                        stopProgressDialog(AbsencesActivity.this);
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                    } else {
                        if(!areThereMockPermissionApps()) {
                            if (!isMockSettingsON()) {
                                String token = preferences.getString("token", null);
                                schedule_id = preferences.getString("schedule_id", null);
                                latitude = preferences.getString("latitude", null);
                                longitude = preferences.getString("longitude", null);

                                JSONObject payload = new JSONObject();
                                try {
                                    payload.put("schedule_id", schedule_id);
                                    payload.put("latitude", latitude);
                                    payload.put("longitude", longitude);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    showToast(AbsencesActivity.this, e.getMessage(), "short");
                                }

                                Call<BasicPost> absenOutCheckPostCall = scheduleInterface.absenceOutCheckPostCall(token, payload.toString());
                                absenOutCheckPostCall.enqueue(new Callback<BasicPost>() {
                                    @Override
                                    public void onResponse(Call<BasicPost> call, Response<BasicPost> response) {
                                        if (response.code() == 200) {
                                            if (response.body().getStatus()) {
                                                askCameraPermissions();
                                            } else {
                                                stopProgressDialog(AbsencesActivity.this);
                                                Toast toast = Toast.makeText(AbsencesActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT);
                                                toast.show();
                                            }
                                        } else {
                                            stopProgressDialog(AbsencesActivity.this);
                                            try {
                                                JSONObject object = new JSONObject(response.errorBody().string());
                                                Toast toast = Toast.makeText(AbsencesActivity.this, object.getString("message"), Toast.LENGTH_SHORT);
                                                toast.show();

                                                startActivity(new Intent(AbsencesActivity.this, LoginActivity.class));
                                                finish();
                                            } catch (IOException | JSONException e) {
                                                e.printStackTrace();
                                                showToast(AbsencesActivity.this, e.getMessage(), "short");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<BasicPost> call, Throwable t) {
                                        stopProgressDialog(AbsencesActivity.this);

                                        t.printStackTrace();
                                        showToast(AbsencesActivity.this, t.getMessage(), "short");
                                    }
                                });
                            } else {
                                stopProgressDialog(AbsencesActivity.this);
                            }
                        } else {
                            stopProgressDialog(AbsencesActivity.this);
                        }
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
                showToast(AbsencesActivity.this, e.getMessage(), "short");
            }
        });
    }

    private void askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 112);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 113);
            } else {
                dispatchTakePictureIntent();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                imageFile = new File(currentPhotoPath);

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(imageFile);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
                setCompressImage(contentUri);

                try {
                    if (imageFile != null) {
                        sendData();
                    } else {
                        Toast.makeText(AbsencesActivity.this, "Please input picture first.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast(AbsencesActivity.this, e.getMessage(), "short");
                }
            }
        }
    }

    private void sendData() {
        String token = preferences.getString("token", null);

        RequestBody schedule_idPart = RequestBody.create(MultipartBody.FORM, schedule_id);
        RequestBody latitudePart = RequestBody.create(MultipartBody.FORM, latitude);
        RequestBody longitudePart = RequestBody.create(MultipartBody.FORM, longitude);

        MultipartBody.Part filePart = MultipartBody.Part.createFormData("foto_absen", compressImage.getName(), RequestBody.create(MediaType.parse("image/*"), compressImage));

        Call<BasicPost> submitAbsenOutPostCall = scheduleInterface.submitAbsenceOutPostCall(token, schedule_idPart, latitudePart, longitudePart, filePart);
        submitAbsenOutPostCall.enqueue(new Callback<BasicPost>() {
            @Override
            public void onResponse(Call<BasicPost> call, Response<BasicPost> response) {
                stopProgressDialog(AbsencesActivity.this);

                if (response.code() == 200) {
                    if (response.body().getStatus()) {
                        compressImage.delete();
                        imageFile.delete();

                        Toast toast = Toast.makeText(AbsencesActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        Toast toast = Toast.makeText(AbsencesActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else {
                    try {
                        JSONObject object = new JSONObject(response.errorBody().string());
                        Toast toast = Toast.makeText(AbsencesActivity.this, object.getString("message"), Toast.LENGTH_SHORT);
                        toast.show();

                        startActivity(new Intent(AbsencesActivity.this, LoginActivity.class));
                        finish();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        showToast(AbsencesActivity.this, e.getMessage(), "short");
                    }
                }
            }

            @Override
            public void onFailure(Call<BasicPost> call, Throwable t) {
                stopProgressDialog(AbsencesActivity.this);

                t.printStackTrace();
                showToast(AbsencesActivity.this, t.getMessage(), "short");
            }
        });
    }

    private void setCompressImage(Uri imageUri){
        try {
            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String filename = String.format("%d.jpg",System.currentTimeMillis());
            File finalFile = new File(path,filename);
            FileOutputStream fileOutputStream = new FileOutputStream(finalFile);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG,50,fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(finalFile);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
            compressImage = new File(String.valueOf(finalFile));
        }catch (IOException e){
            e.printStackTrace();
            showToast(AbsencesActivity.this, e.getMessage(), "short");
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d(TAG, "Gagal : "+ex);
                showToast(AbsencesActivity.this, ex.getMessage(), "short");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        runNetworkChangeListerner();

        doBindLocationService();

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

        doUnbindLocationService();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        doUnbindLocationService();
    }
}