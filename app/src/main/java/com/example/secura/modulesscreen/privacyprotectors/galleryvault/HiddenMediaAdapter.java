package com.example.secura.modulesscreen.privacyprotectors.galleryvault;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secura.R;

import java.io.File;
import java.util.List;

public class HiddenMediaAdapter extends RecyclerView.Adapter<HiddenMediaAdapter.ViewHolder> {

    private Context context;
    private List<File> hiddenMediaList;
    private OnMediaActionListener listener;

    public interface OnMediaActionListener {
        void onUnhideClick(File mediaFile);
    }

    public HiddenMediaAdapter(Context context, List<File> hiddenMediaList, OnMediaActionListener listener) {
        this.context = context;
        this.hiddenMediaList = hiddenMediaList;
        this.listener = listener;
    }

    public void updateData(List<File> newData) {
        this.hiddenMediaList = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.hidden_media_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File mediaFile = hiddenMediaList.get(position);
        holder.hiddenMediaName.setText(mediaFile.getName());

        String mimeType = getMimeType(mediaFile.getName());
        if (mimeType != null) {
            if (mimeType.startsWith("image")) {
                try {
                    Bitmap thumbnail = ThumbnailUtils.extractThumbnail(
                            android.graphics.BitmapFactory.decodeFile(mediaFile.getAbsolutePath()),
                            200, 200); // Adjust thumbnail size as needed
                    holder.hiddenMediaThumbnail.setImageBitmap(thumbnail);
                } catch (Exception e) {
                    holder.hiddenMediaThumbnail.setImageResource(R.drawable.ic_broken_image); // Placeholder for error
                    e.printStackTrace();
                }
            } else if (mimeType.startsWith("video")) {
                try {
                    // For videos, createVideoThumbnail is generally better.
                    // MediaStore.Images.Thumbnails.MINI_KIND for smaller thumbnail
                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(
                            mediaFile.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
                    if (thumbnail != null) {
                        holder.hiddenMediaThumbnail.setImageBitmap(thumbnail);
                    } else {
                        // Fallback if thumbnail can't be generated (e.g., corrupt video)
                        holder.hiddenMediaThumbnail.setImageResource(R.drawable.ic_video_placeholder); // You'll need to add this drawable
                    }
                } catch (Exception e) {
                    holder.hiddenMediaThumbnail.setImageResource(R.drawable.ic_video_placeholder); // Placeholder for error
                    e.printStackTrace();
                }
            } else {
                holder.hiddenMediaThumbnail.setImageResource(R.drawable.ic_file_placeholder); // Generic placeholder
            }
        } else {
            holder.hiddenMediaThumbnail.setImageResource(R.drawable.ic_file_placeholder); // Generic placeholder
        }

        holder.unhideMediaButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUnhideClick(mediaFile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return hiddenMediaList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView hiddenMediaThumbnail;
        TextView hiddenMediaName;
        Button unhideMediaButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            hiddenMediaThumbnail = itemView.findViewById(R.id.hiddenMediaThumbnail);
            hiddenMediaName = itemView.findViewById(R.id.hiddenMediaName);
            unhideMediaButton = itemView.findViewById(R.id.unhideMediaButton);
        }
    }

    private String getMimeType(String fileName) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}