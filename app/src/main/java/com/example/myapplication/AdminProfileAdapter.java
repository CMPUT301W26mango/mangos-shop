/**
 * RecyclerView adapter for displaying profile items on the admin browse profiles screen.
 *
 * Role in application:
 * - Presentation-layer adapter for admin profile browsing.
 */

package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

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

        holder.textName.setText(name);
        holder.textRole.setText(role);

        String profileUrl = profileItem.getProfileImageUrl();

        if (profileUrl != null && !profileUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(profileUrl)
                    .circleCrop()
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(android.R.drawable.sym_def_app_icon);
        }

        holder.itemView.setOnClickListener(v -> listener.onProfileClick(profileItem));
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    static class AdminProfileViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textRole;
        ImageView profileImage;

        public AdminProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textRole = itemView.findViewById(R.id.textRole);
            profileImage = itemView.findViewById(R.id.profileImageView);
        }
    }
}