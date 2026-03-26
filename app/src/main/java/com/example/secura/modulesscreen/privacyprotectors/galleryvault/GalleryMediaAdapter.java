package com.example.secura.modulesscreen.privacyprotectors.galleryvault;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.secura.R;

import java.util.List;

public class GalleryMediaAdapter extends RecyclerView.Adapter<GalleryMediaAdapter.ViewHolder> {

    private Context context;
    private List<MediaItem> mediaList;
    private OnMediaItemClickListener listener;

    public interface OnMediaItemClickListener {
        void onMediaItemClick(MediaItem item);
    }

    public GalleryMediaAdapter(Context context, List<MediaItem> mediaList, OnMediaItemClickListener listener) {
        this.context = context;
        this.mediaList = mediaList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gallery_media_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItem item = mediaList.get(position);
        holder.mediaName.setText(item.getDisplayName());

        // Use Glide for efficient image/video thumbnail loading
        Glide.with(context)
                .load(item.getUri()) // Load directly from Uri for MediaStore items
                .placeholder(R.drawable.ic_file_placeholder) // Generic placeholder
                .error(R.drawable.ic_broken_image) // Error placeholder
                .centerCrop()
                .into(holder.mediaThumbnail);

        // Show/hide selection overlay
        if (item.isSelected()) {
            holder.selectionOverlay.setVisibility(View.VISIBLE);
        } else {
            holder.selectionOverlay.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMediaItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mediaThumbnail;
        ImageView selectionOverlay;
        TextView mediaName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mediaThumbnail = itemView.findViewById(R.id.mediaThumbnail);
            selectionOverlay = itemView.findViewById(R.id.selectionOverlay);
            mediaName = itemView.findViewById(R.id.mediaName);
        }
    }
}