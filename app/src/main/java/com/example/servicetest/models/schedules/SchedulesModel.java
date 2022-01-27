package com.example.servicetest.models.schedules;

import com.google.gson.annotations.SerializedName;

public class SchedulesModel {

    @SerializedName("id")
    private String id;
    @SerializedName("maintenance_id")
    private String maintenance_id;
    @SerializedName("user_id")
    private String user_id;
    @SerializedName("channel_id")
    private String channel_id;
    @SerializedName("shift_id")
    private String shift_id;
    @SerializedName("is_siklus")
    private Boolean is_siklus;
    @SerializedName("schedule_date")
    private String schedule_date;

    public SchedulesModel(String id, String maintenance_id, String user_id, String channel_id, String shift_id, Boolean is_siklus, String schedule_date) {
        this.id = id;
        this.maintenance_id = maintenance_id;
        this.user_id = user_id;
        this.channel_id = channel_id;
        this.shift_id = shift_id;
        this.is_siklus = is_siklus;
        this.schedule_date = schedule_date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaintenance_id() {
        return maintenance_id;
    }

    public void setMaintenance_id(String maintenance_id) {
        this.maintenance_id = maintenance_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getShift_id() {
        return shift_id;
    }

    public void setShift_id(String shift_id) {
        this.shift_id = shift_id;
    }

    public Boolean getIs_siklus() {
        return is_siklus;
    }

    public void setIs_siklus(Boolean is_siklus) {
        this.is_siklus = is_siklus;
    }

    public String getSchedule_date() {
        return schedule_date;
    }

    public void setSchedule_date(String schedule_date) {
        this.schedule_date = schedule_date;
    }
}
