package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    public interface OnImageClickListener {
        void onClick(ImageItem item);
    }

    private List<ImageItem> images;
    private OnImageClickListener listener;

    public ImageAdapter(List<ImageItem> images, OnImageClickListener listener) {
        this.images = images;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        public ViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.imageItem);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageItem item = images.get(position);

        Glide.with(holder.itemView.getContext())
                .load(item.getUrl())
                .into(holder.image);

        holder.image.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}