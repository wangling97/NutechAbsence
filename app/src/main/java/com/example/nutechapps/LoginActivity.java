package com.example.nutechapps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
            showProgressDialog(LoginActivity.this);

            // Get text from input and set into local variabel
            String loginUsernameText = loginUsername.getEditText().getText().toString();
            String loginPasswordText = loginPassword.getEditText().getText().toString();

            // If username empty
            if (loginUsernameText.isEmpty())
                stopProgressDialog(LoginActivity.this);
                loginUsername.setError("Username is required.");

            // If password empty
            if (loginPasswordText.isEmpty())
                stopProgressDialog(LoginActivity.this);
                loginPassword.setError("Password is required.");

            // If username and password not empty
            if (!loginUsernameText.isEmpty() && !loginPasswordText.isEmpty()) {
                // Create a payload to send to the endpoint
                JSONObject payload = new JSONObject();
                try {
                    payload.put("username", loginUsernameText);
                    payload.put("password", loginPasswordText);
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
        });
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