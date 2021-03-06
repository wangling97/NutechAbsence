package com.example.nutechapps;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.nutechapps.models.BasicPost;
import com.example.nutechapps.retrofit.ApiClient;
import com.example.nutechapps.retrofit.interfaces.schedules.ScheduleInterface;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
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

        detectMemoriFull(AbsenceInActivity.this);

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
                                            showToast(AbsenceInActivity.this, "Keterangan Wajib Diisi.", "short");
                                        }
                                    } else {
                                        submitValid = true;
                                    }

                                    saveBitmapToFile(compressImage);

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
                                                            imageFile.delete();
                                                            compressImage.delete();

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
                                        showToast(AbsenceInActivity.this, "Maaf sistem tidak bisa mendapatkan lokasi poin Anda, silakan coba lagi dalam beberapa detik.", "long");
                                    }
                                }
                                else {
                                    stopProgressDialog(AbsenceInActivity.this);

                                    Toast.makeText(AbsenceInActivity.this, "Harap masukkan foto terlebih dahulu.", Toast.LENGTH_SHORT).show();
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

    public File saveBitmapToFile(File file){
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE=50;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
    }

    private void askCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
                return;
            } else {
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
        } else {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                try {
                    imageFile = new File(currentPhotoPath);

                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(imageFile);
                    mediaScanIntent.setData(contentUri);
                    this.sendBroadcast(mediaScanIntent);
                    setCompressImage(contentUri, 50);

                    selectedImage.setImageURI(Uri.fromFile(compressImage));
                } catch (Exception e) {
                    showToast(AbsenceInActivity.this, e.getMessage(), "short");
                }
            }
        }
    }

    public Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        // Return result
        return rotatedBitmap;
    }

    private void setCompressImage(Uri imageUri, int img_quality){
        try {
            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            File path;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                path = new File(new File(Environment.getExternalStorageDirectory(), "Pictures"), "NutechApps");
            } else {
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/NutechApps");
            }

            if (!path.exists()) {
                path.mkdirs();
            }
            String filename = String.format("%d.jpg",System.currentTimeMillis());
            File finalFile = new File(path,filename);
            FileOutputStream fileOutputStream = new FileOutputStream(finalFile);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG,img_quality,fileOutputStream);
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

        File storageDir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            storageDir = new File(new File(Environment.getExternalStorageDirectory(), "Pictures"), "NutechApps");
        } else {
            storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/NutechApps");
        }

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
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
                Log.d(TAG, "Gagal : " + ex);
                showToast(AbsenceInActivity.this, ex.getMessage(), "short");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            } else {
                showToast(AbsenceInActivity.this, "File gambar tidak berhasil terbuat silahkan coba lagi.", "short");
            }
        } else {
            showToast(AbsenceInActivity.this, "Tidak dapat menemukan kamera yang compatible pada device Anda.", "short");
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
            compressImage.delete();
            imageFile.delete();
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