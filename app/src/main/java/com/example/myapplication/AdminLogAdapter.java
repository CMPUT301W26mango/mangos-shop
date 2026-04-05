package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminLogAdapter extends RecyclerView.Adapter<AdminLogAdapter.ViewHolder> {

    private List<AdminLogItem> list;

    public AdminLogAdapter(List<AdminLogItem> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView msg, sender, time;

        public ViewHolder(View view) {
            super(view);
            msg = view.findViewById(R.id.textMessage);
            sender = view.findViewById(R.id.textSender);
            time = view.findViewById(R.id.textTime);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AdminLogItem item = list.get(position);
        holder.msg.setText(item.getMessage());
        holder.sender.setText("Event: " + item.getSender());
        holder.time.setText(item.getTime());
    }

    @Override
    public int getItemCount() { return list.size(); }
}