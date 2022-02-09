package com.example.nutechapps.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nutechapps.R;
import com.example.nutechapps.models.schedules.ScheduleChannel;

import java.util.ArrayList;
import java.util.List;

public class SchedulesChannelAdapter extends RecyclerView.Adapter<SchedulesChannelAdapter.SchedulesChannelViewHolder> {

    List<ScheduleChannel> scheduleChannels;

    public SchedulesChannelAdapter(List<ScheduleChannel> scheduleChannels) {
        this.scheduleChannels = new ArrayList<>();

        this.scheduleChannels.clear();
        this.scheduleChannels.addAll(scheduleChannels);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SchedulesChannelViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_schedules_channel, viewGroup, false);
        return new SchedulesChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SchedulesChannelViewHolder holder, int i) {
        int number = (i+1);

        holder.scheduleChannelName.setText(String.format("%d. %s", number, scheduleChannels.get(i).getChannel_name()));
    }

    @Override
    public int getItemCount() {
        return scheduleChannels.size();
    }

    public static class SchedulesChannelViewHolder extends RecyclerView.ViewHolder {

        public TextView scheduleChannelName;

        public SchedulesChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            scheduleChannelName = itemView.findViewById(R.id.schedulesChannelName);
        }
    }
}
