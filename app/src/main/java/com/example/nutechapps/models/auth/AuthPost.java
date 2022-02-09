package com.example.nutechapps.models.auth;

import com.google.gson.annotations.SerializedName;

public class AuthPost {

    @SerializedName("status")
    private Boolean status;
    @SerializedName("error_code")
    private int error_code;
    @SerializedName("message")
    private String message;
    @SerializedName("token")
    private String token;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
