package com.example.nutechapps.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.nutechapps.R;
import com.example.nutechapps.models.BasicPost;
import com.example.nutechapps.retrofit.ApiClient;
import com.example.nutechapps.retrofit.interfaces.auth.AuthInterface;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistDeviceDialog extends AppCompatDialogFragment {

    private TextInputLayout txtUsername;
    private AuthInterface authInterface;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.modal_regist_device, null);

        txtUsername = view.findViewById(R.id.registUsername);

        builder.setView(view)
                .setTitle("Pendaftaran Perangkat")
                .setNegativeButton("Batal", (dialogInterface, i) -> { })
                .setPositiveButton("Kirim", (dialogInterface, i) -> {
                    String registUsername = txtUsername.getEditText().getText().toString();
                    String androidId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                    String androidModel = android.os.Build.DEVICE;

                    if (registUsername.isEmpty()) {
                        txtUsername.setError("Username wajib diisi.");
                    }

                    if (!registUsername.isEmpty()) {
                        // Create a payload to send to the endpoint
                        JSONObject payload = new JSONObject();
                        try {
                            payload.put("username", registUsername);
                            payload.put("android_id", androidId);
                            payload.put("android_model", androidModel);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(view.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        authInterface = ApiClient.getRetrofit().create(AuthInterface.class);

                        Call<BasicPost> registrationDevicePostCall = authInterface.registrationDevicePostCall(payload.toString());
                        registrationDevicePostCall.enqueue(new Callback<BasicPost>() {
                            @Override
                            public void onResponse(Call<BasicPost> call, Response<BasicPost> response) {
                                if (response.code() == 200) {
                                    if(response.body().getStatus()) {
                                        Toast.makeText(view.getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(view.getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    try {
                                        JSONObject object = new JSONObject(response.errorBody().string());
                                        Toast.makeText(view.getContext(), object.getString("message"), Toast.LENGTH_SHORT).show();
                                    } catch (IOException | JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(view.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<BasicPost> call, Throwable t) {
                                t.printStackTrace();
                                Toast.makeText(view.getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });

        return builder.create();
    }
}
