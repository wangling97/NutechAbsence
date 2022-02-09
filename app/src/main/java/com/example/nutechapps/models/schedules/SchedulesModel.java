package com.example.nutechapps.models.schedules;

import com.google.gson.annotations.SerializedName;

public class SchedulesModel {

    @SerializedName("id")
    private String id;
    @SerializedName("maintenance_id")
    private String maintenance_id;
    @SerializedName("user_id")
    private String user_id;
    @SerializedName("shift_id")
    private String shift_id;
    @SerializedName("is_siklus")
    private Boolean is_siklus;
    @SerializedName("schedule_date")
    private String schedule_date;
    @SerializedName("schedule_shift_start")
    private String schedule_shift_start;
    @SerializedName("schedule_shift_end")
    private String schedule_shift_end;
    @SerializedName("name")
    private String name;
    @SerializedName("division_name")
    private String division_name;
    @SerializedName("profile_pic")
    private String profile_pic;

    public SchedulesModel(String id, String maintenance_id, String user_id, String shift_id, Boolean is_siklus, String schedule_date, String schedule_shift_start, String schedule_shift_end, String name, String division_name, String profile_pic) {
        this.id = id;
        this.maintenance_id = maintenance_id;
        this.user_id = user_id;
        this.shift_id = shift_id;
        this.is_siklus = is_siklus;
        this.schedule_date = schedule_date;
        this.schedule_shift_start = schedule_shift_start;
        this.schedule_shift_end = schedule_shift_end;
        this.name = name;
        this.division_name = division_name;
        this.profile_pic = profile_pic;
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

    public String getSchedule_shift_start() {
        return schedule_shift_start;
    }

    public void setSchedule_shift_start(String schedule_shift_start) {
        this.schedule_shift_start = schedule_shift_start;
    }

    public String getSchedule_shift_end() {
        return schedule_shift_end;
    }

    public void setSchedule_shift_end(String schedule_shift_end) {
        this.schedule_shift_end = schedule_shift_end;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDivision_name() {
        return division_name;
    }

    public void setDivision_name(String division_name) {
        this.division_name = division_name;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }
}
