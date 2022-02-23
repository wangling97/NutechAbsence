package com.example.nutechapps;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.nutechapps.models.BasicPost;
import com.example.nutechapps.retrofit.ApiClient;
import com.example.nutechapps.retrofit.interfaces.schedules.ScheduleInterface;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AbsenceInActivity extends BaseActivity {

    private String TAG = "AbsenceInActivity";

    private ScheduleInterface scheduleInterface;
    protected LocationManager locationManager;

    private ImageView selectedImage;
    private RadioGroup radioAbsenceGroup;
    private RadioButton radioAbsenceButton;
    private TextInputLayout absenceNote;
    private MaterialButton submitAbsence;
    private SharedPreferences preferences;

    private static final int CAMERA_PERM_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;
    private File imageFile,compressImage;
    private String currentPhotoPath;

    private static Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absence_in);

        View btnBackAbsenceIn = findViewById(R.id.btn_back_absence_in);
        btnBackAbsenceIn.setOnClickListener(view -> {
            startActivity(new Intent(AbsenceInActivity.this, AbsencesActivity.class));
            finish();
        });

        radioAbsenceGroup = findViewById(R.id.radioAbsence);
        radioAbsenceGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            if (imageFile == null) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                    ActivityCompat.requestPermissions(AbsenceInActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                } else {
                    askCameraPermissions();
                }
            }
        });

        absenceNote = findViewById(R.id.absenceNote);

        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        scheduleInterface = ApiClient.getRetrofit().create(ScheduleInterface.class);

        selectedImage = findViewById(R.id.displayImageView);
        selectedImage.setOnClickListener(view -> {
            if (imageFile != null) {
                imageFile.delete();
                compressImage.delete();
            }

            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(AbsenceInActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            } else {
                askCameraPermissions();
            }
        });

        submitAbsence = findViewById(R.id.submitAbsence);
        submitAbsence.setOnClickListener(v -> {
            showProgressDialog(AbsenceInActivity.this);
            try {
                locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

                // getting GPS status
                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                // getting network status
                boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    stopProgressDialog(AbsenceInActivity.this);
                    showSettingsAlert();
                } else {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                        stopProgressDialog(AbsenceInActivity.this);
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                    } else {
                        if (!areThereMockPermissionApps()) {
                            if (!isMockSettingsON()) {
                                if (imageFile != null) {
                                    // get selected radio button from radioGroup
                                    int selectedId = radioAbsenceGroup.getCheckedRadioButtonId();

                                    // find the radiobutton by returned id
                                    radioAbsenceButton = findViewById(selectedId);

                                    // Get value from input and preference
                                    String schedule_id = preferences.getString("schedule_id", null);
                                    String latitude = (preferences.getString("latitude", null) != null) ? preferences.getString("latitude", null) : "";
                                    String longitude = (preferences.getString("longitude", null) != null) ? preferences.getString("longitude", null) : "";
                                    String note = absenceNote.getEditText().getText().toString();
                                    String status = radioAbsenceButton.getText().toString();

                                    // input validation
                                    boolean submitValid = false;

                                    if (!radioAbsenceButton.getText().toString().equals("Masuk")) {
                                        if (!absenceNote.getEditText().getText().toString().equals("")) {
                                            submitValid = true;
                                        } else {
                                            stopProgressDialog(AbsenceInActivity.this);
                                            showToast(AbsenceInActivity.this, "Note Description Required.", "short");
                                        }
                                    } else {
                                        submitValid = true;
                                    }

                                    if (!latitude.equals("") && !longitude.equals("")) {
                                        if (submitValid) {
                                            String token = preferences.getString("token", null);
                                            RequestBody schedule_idPart = RequestBody.create(MultipartBody.FORM, schedule_id);
                                            RequestBody latitudePart = RequestBody.create(MultipartBody.FORM, latitude);
                                            RequestBody longitudePart = RequestBody.create(MultipartBody.FORM, longitude);
                                            RequestBody notePart = RequestBody.create(MultipartBody.FORM, note);
                                            RequestBody statusPart = RequestBody.create(MultipartBody.FORM, status);

                                            MultipartBody.Part filePart = MultipartBody.Part.createFormData("foto_absen", compressImage.getName(), RequestBody.create(MediaType.parse("image/*"), compressImage));

                                            Call<BasicPost> submitAbsenInCheckPostCall = scheduleInterface.submitAbsenceInPostCall(token, schedule_idPart, latitudePart, longitudePart, notePart, statusPart, filePart);
                                            submitAbsenInCheckPostCall.enqueue(new Callback<BasicPost>() {
                                                @Override
                                                public void onResponse(Call<BasicPost> call, Response<BasicPost> response) {
                                                    stopProgressDialog(AbsenceInActivity.this);

                                                    if (response.code() == 200) {
                                                        if (response.body().getStatus()) {
                                                            compressImage.delete();
                                                            imageFile.delete();

                                                            Toast toast = Toast.makeText(AbsenceInActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT);
                                                            toast.show();

                                                            startActivity(new Intent(AbsenceInActivity.this, MainActivity.class));
                                                            finish();
                                                        } else {
                                                            Toast toast = Toast.makeText(AbsenceInActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT);
                                                            toast.show();
                                                        }
                                                    } else {
                                                        try {
                                                            JSONObject object = new JSONObject(response.errorBody().string());
                                                            Toast toast = Toast.makeText(AbsenceInActivity.this, object.getString("message"), Toast.LENGTH_SHORT);
                                                            toast.show();

                                                            startActivity(new Intent(AbsenceInActivity.this, LoginActivity.class));
                                                            finish();
                                                        } catch (IOException | JSONException e) {
                                                            e.printStackTrace();
                                                            showToast(AbsenceInActivity.this, e.getMessage(), "short");
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<BasicPost> call, Throwable t) {
                                                    t.printStackTrace();
                                                    showToast(AbsenceInActivity.this, t.getMessage(), "short");
                                                }
                                            });
                                        }
                                    } else {
                                        stopProgressDialog(AbsenceInActivity.this);
                                        showToast(AbsenceInActivity.this, "Sorry the system couldn't get your point location, please try again in a few seconds.", "long");
                                    }
                                }
                                else {
                                    stopProgressDialog(AbsenceInActivity.this);

                                    Toast.makeText(AbsenceInActivity.this, "Please input picture first.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                stopProgressDialog(AbsenceInActivity.this);
                            }
                        } else {
                            stopProgressDialog(AbsenceInActivity.this);
                        }
                    }
                }
            } catch (Exception e) {
                stopProgressDialog(AbsenceInActivity.this);

                e.printStackTrace();
                showToast(AbsenceInActivity.this, e.getMessage(), "short");
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
                selectedImage.setImageURI(Uri.fromFile(imageFile));

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(imageFile);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
                setCompressImage(contentUri);
            }
        }
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
            showToast(AbsenceInActivity.this, e.getMessage(), "short");
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
                showToast(AbsenceInActivity.this, ex.getMessage(), "short");
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
                startActivity(new Intent(AbsenceInActivity.this, LoginActivity.class));
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
    protected void onDestroy() {
        super.onDestroy();

        if (imageFile != null) {
            imageFile.delete();
            compressImage.delete();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        doUnbindLocationService();
        if (imageFile != null) {
            imageFile.delete();
            compressImage.delete();
        }
    }
}