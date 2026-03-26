package com.example.secura.modulesscreen.privacyprotectors.intruderselfie;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.secura.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context context;
    private List<SelfieItem> selfieItems;
    private List<SelfieItem> selectedSelfies;

    public ImageAdapter(Context context, List<SelfieItem> selfieItems) {
        this.context = context;
        this.selfieItems = selfieItems;
        this.selectedSelfies = new ArrayList<>();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_selfie_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        SelfieItem currentSelfie = selfieItems.get(position);

        Glide.with(context)
                .load(Uri.parse(currentSelfie.getImagePath()))
                .into(holder.selfieImageView);

        // Changed SimpleDateFormat pattern to include 'a' for AM/PM
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        holder.selfieTimestampTextView.setText(sdf.format(currentSelfie.getTimestamp()));

        holder.selfieCheckBox.setChecked(currentSelfie.isSelected());
        holder.selfieCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentSelfie.setSelected(isChecked);
            if (isChecked) {
                selectedSelfies.add(currentSelfie);
            } else {
                selectedSelfies.remove(currentSelfie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return selfieItems.size();
    }

    public List<SelfieItem> getSelectedSelfies() {
        return selectedSelfies;
    }

    public void clearSelectedSelfies() {
        selectedSelfies.clear();
        for (SelfieItem item : selfieItems) {
            item.setSelected(false);
        }
        notifyDataSetChanged();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView selfieImageView;
        CheckBox selfieCheckBox;
        TextView selfieTimestampTextView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            selfieImageView = itemView.findViewById(R.id.selfieImageView);
            selfieCheckBox = itemView.findViewById(R.id.selfieCheckBox);
            selfieTimestampTextView = itemView.findViewById(R.id.selfieTimestampTextView);
        }
    }
}