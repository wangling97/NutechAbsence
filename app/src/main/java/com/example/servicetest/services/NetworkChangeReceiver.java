package com.example.servicetest.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.servicetest.R;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private String TAG = "NetworkChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = NetworkConnection.getConnectivityStatusString(context);
        Log.d(TAG, "Network Change Reciver");

        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkConnection.NETWORK_STATUS_NOT_CONNECTED) {
                Toast.makeText(context, R.string.connection_not_available, Toast.LENGTH_SHORT).show();
            } else {
                // Toast.makeText(context, R.string.connection_available, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
