package com.example.nutechapps.models.schedules;

import com.google.gson.annotations.SerializedName;

public class ScheduleChannel implements java.io.Serializable {

    @SerializedName("id")
    private String id;
    @SerializedName("channel_name")
    private String channel_name;
    @SerializedName("channel_addr")
    private String channel_addr;
    @SerializedName("channel_long")
    private String channel_long;
    @SerializedName("channel_lat")
    private String channel_lat;

    public ScheduleChannel(String id, String channel_name, String channel_addr, String channel_long, String channel_lat) {
        this.id = id;
        this.channel_name = channel_name;
        this.channel_addr = channel_addr;
        this.channel_long = channel_long;
        this.channel_lat = channel_lat;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannel_name() {
        return channel_name;
    }

    public void setChannel_name(String channel_name) {
        this.channel_name = channel_name;
    }

    public String getChannel_addr() {
        return channel_addr;
    }

    public void setChannel_addr(String channel_addr) {
        this.channel_addr = channel_addr;
    }

    public String getChannel_long() {
        return channel_long;
    }

    public void setChannel_long(String channel_long) {
        this.channel_long = channel_long;
    }

    public String getChannel_lat() {
        return channel_lat;
    }

    public void setChannel_lat(String channel_lat) {
        this.channel_lat = channel_lat;
    }
}
