package com.example.servicetest.models.schedules;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CheckSchedulePost {

    @SerializedName("status")
    private Boolean status;
    @SerializedName("error_code")
    private int error_code;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    List<SchedulesModel> schedulesModels;

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

    public List<SchedulesModel> getSchedulesModels() {
        return schedulesModels;
    }

    public void setSchedulesModels(List<SchedulesModel> schedulesModels) {
        this.schedulesModels = schedulesModels;
    }
}
