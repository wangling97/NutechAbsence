package com.example.nutechapps;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.nutechapps.fragments.RegistDeviceDialog;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.TextView;

import com.example.nutechapps.models.auth.AuthPost;
import com.example.nutechapps.retrofit.ApiClient;
import com.example.nutechapps.retrofit.interfaces.auth.AuthInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private AuthInterface authInterface;
    private TextInputLayout loginUsername, loginPassword;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        checkGooglePlayServices(LoginActivity.this);

        TextView txtAndroidId = findViewById(R.id.txtAndroidId);
        TextView txtAndroidModel = findViewById(R.id.txtAndroidModel);
        TextView txtAndroidVersion = findViewById(R.id.txtAndroidVersion);
        TextView txtVersionApp = findViewById(R.id.txtVersionApp);

        txtAndroidId.setText(String.format("Android ID %s", getAndroidId(LoginActivity.this)));
        txtAndroidModel.setText(String.format("Android Model %s", getDeviceModel()));
        txtAndroidVersion.setText(String.format("Android Version %s - (API : %s)", getAndroidRelease(), getAndroidSdk()));
        txtVersionApp.setText(String.format("App Version %s", getVersionApp()));

        // Set ApiClient for authInterface
        authInterface = ApiClient.getRetrofit().create(AuthInterface.class);

        // Set layout variable
        loginUsername = findViewById(R.id.loginUsername);
        loginPassword = findViewById(R.id.loginPassword);

        loginUsername.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                loginUsername.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        loginPassword.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                loginPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Button Login Process
        MaterialButton btnMasuk = findViewById(R.id.btnMasuk);
        btnMasuk.setOnClickListener(view -> {
            if (isRooted(LoginActivity.this)) {
                showToast(LoginActivity.this, "Perangkat Anda tidak mendukung untuk menggunakan aplikasi ini!", "long");
            } else {
                showProgressDialog(LoginActivity.this);

                // Get text from input and set into local variabel
                String loginUsernameText = loginUsername.getEditText().getText().toString();
                String loginPasswordText = loginPassword.getEditText().getText().toString();

                // If username empty
                if (loginUsernameText.isEmpty()) {
                    stopProgressDialog(LoginActivity.this);
                    loginUsername.setError("Username wajib diisi.");
                }

                // If password empty
                if (loginPasswordText.isEmpty()) {
                    stopProgressDialog(LoginActivity.this);
                    loginPassword.setError("Password wajib diisi.");
                }

                // If username and password not empty
                if (!loginUsernameText.isEmpty() && !loginPasswordText.isEmpty()) {
                    // Create a payload to send to the endpoint
                    JSONObject payload = new JSONObject();
                    try {
                        payload.put("username", loginUsernameText);
                        payload.put("password", loginPasswordText);
                        payload.put("android_id", getAndroidId(LoginActivity.this));
                        payload.put("android_model", getDeviceModel());
                        payload.put("android_version", getAndroidRelease());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showToast(LoginActivity.this, e.getMessage(), "short");
                    }

                    // Call login endpoint and send the payload
                    Call<AuthPost> authPostCall = authInterface.authPostCall(payload.toString());
                    authPostCall.enqueue(new Callback<AuthPost>() {
                        @Override
                        public void onResponse(Call<AuthPost> call, Response<AuthPost> response) {
                            stopProgressDialog(LoginActivity.this);

                            if (response.code() == 200) {
                                if(response.body().getStatus()) {
                                    // Get token from response
                                    String token = response.body().getToken();

                                    // Put token from response in SharedPreferences
                                    SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                                    preferences.edit().putString("token", token).apply();

                                    // Show and Redirect into MainActivity
                                    showToast(LoginActivity.this, response.body().getMessage(), "short");
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    showToast(LoginActivity.this, response.body().getMessage(), "short");
                                }
                            } else {
                                try {
                                    JSONObject object = new JSONObject(response.errorBody().string());
                                    showToast(LoginActivity.this, object.getString("message"), "short");
                                } catch (IOException | JSONException e) {
                                    e.printStackTrace();
                                    showToast(LoginActivity.this, e.getMessage(), "short");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<AuthPost> call, Throwable t) {
                            stopProgressDialog(LoginActivity.this);

                            t.printStackTrace();
                            showToast(LoginActivity.this, t.getMessage(), "short");
                        }
                    });
                }
            }
        });

        TextView registDevice = findViewById(R.id.registDevice);
        registDevice.setOnClickListener(view -> {
            OpenModal();
        });
    }

    private void OpenModal() {
        RegistDeviceDialog registDeviceDialog = new RegistDeviceDialog();
        registDeviceDialog.show(getSupportFragmentManager(), "Pendaftaran Perangkat");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start Network listener
        runNetworkChangeListerner();

        // Validate token (if token still valid, redirect into MainActivity)
        validateToken(status -> {
            if (status) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                preferences.edit().putString("token", null).apply();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop Network Listener
        unregisterNetworkChanges();
    }
}