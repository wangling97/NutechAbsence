package com.example.servicetest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.servicetest.models.auth.AuthPost;
import com.example.servicetest.retrofit.ApiClient;
import com.example.servicetest.retrofit.interfaces.auth.AuthInterface;
import com.example.servicetest.retrofit.interfaces.auth.TokenCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private final String TAG = "LoginActivity";

    private AuthInterface authInterface;
    private TextInputLayout loginUsername, loginPassword;
    private MaterialButton btnMasuk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authInterface = ApiClient.getRetrofit().create(AuthInterface.class);

        loginUsername = findViewById(R.id.loginUsername);
        loginPassword = findViewById(R.id.loginPassword);

        btnMasuk = findViewById(R.id.btnMasuk);
        btnMasuk.setOnClickListener(v -> {
            String loginUsernameText = loginUsername.getEditText().getText().toString();
            String loginPasswordText = loginPassword.getEditText().getText().toString();

            if (loginUsernameText.isEmpty())
                loginUsername.setError("Username wajib diisi.");

            if (loginPasswordText.isEmpty())
                loginPassword.setError("Password wajib diisi.");

            if (!loginUsernameText.isEmpty() && !loginPasswordText.isEmpty()) {
                JSONObject payload = new JSONObject();
                try {
                    payload.put("username", loginUsernameText);
                    payload.put("password", loginPasswordText);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Call<AuthPost> authPostCall = authInterface.authPostCall(payload.toString());
                authPostCall.enqueue(new Callback<AuthPost>() {
                    @Override
                    public void onResponse(Call<AuthPost> call, Response<AuthPost> response) {
                        if (response.code() == 200) {
                            if(response.body().getStatus() == true) {
                                String token = response.body().getToken();

                                SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                                preferences.edit().putString("token", token).apply();

                                Toast toast = Toast.makeText(LoginActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT);
                                toast.show();

                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast toast = Toast.makeText(LoginActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        } else {
                            try {
                                JSONObject object = new JSONObject(response.errorBody().string());
                                Toast toast = Toast.makeText(LoginActivity.this, object.getString("message"), Toast.LENGTH_SHORT);
                                toast.show();
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthPost> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        runNetworkChangeListerner();

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
        unregisterNetworkChanges();
    }
}