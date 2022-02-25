package com.example.nutechapps;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ResetPasswordActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start Network listener
        runNetworkChangeListerner();

        // Validate token (if token not valid, redirect into LoginActivity)
        validateToken(status -> {
            if (!status) {
                startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
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