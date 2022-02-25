package com.example.nutechapps;

import android.content.Intent;
import android.support.design.card.MaterialCardView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ProfileActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        MaterialCardView menuResetPassword = findViewById(R.id.menu_reset_password);
        menuResetPassword.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this, ResetPasswordActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start Network listener
        runNetworkChangeListerner();

        // Validate token (if token not valid, redirect into LoginActivity)
        validateToken(status -> {
            if (!status) {
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
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