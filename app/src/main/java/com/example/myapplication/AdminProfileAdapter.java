package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.AdminProfileViewHolder> {

    public interface OnProfileClickListener {
        void onProfileClick(AdminProfileItem profileItem);
    }

    private List<AdminProfileItem> profileList;
    private OnProfileClickListener listener;

    public AdminProfileAdapter(List<AdminProfileItem> profileList, OnProfileClickListener listener) {
        this.profileList = profileList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_profile, parent, false);
        return new AdminProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProfileViewHolder holder, int position) {
        AdminProfileItem profileItem = profileList.get(position);

        String name = profileItem.getName();
        if (name == null || name.isEmpty()) {
            name = "Unnamed User";
        }

        String email = profileItem.getEmail();
        if (email == null || email.isEmpty()) {
            email = "No email";
        }

        String role = profileItem.getRole();
        if (role == null || role.isEmpty()) {
            role = "Unknown role";
        }

        holder.textViewProfileName.setText(name);
        holder.textViewProfileEmail.setText("Email: " + email);
        holder.textViewProfileRole.setText("Role: " + role);

        holder.itemView.setOnClickListener(v -> listener.onProfileClick(profileItem));
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    static class AdminProfileViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProfileName, textViewProfileEmail, textViewProfileRole;

        public AdminProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProfileName = itemView.findViewById(R.id.textViewProfileName);
            textViewProfileEmail = itemView.findViewById(R.id.textViewProfileEmail);
            textViewProfileRole = itemView.findViewById(R.id.textViewProfileRole);
        }
    }
}