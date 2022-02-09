package com.example.nutechapps.models;

import com.google.gson.annotations.SerializedName;

public class BasicPost {

    @SerializedName("status")
    private Boolean status;
    @SerializedName("error_code")
    private int error_code;
    @SerializedName("message")
    private String message;

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
}
