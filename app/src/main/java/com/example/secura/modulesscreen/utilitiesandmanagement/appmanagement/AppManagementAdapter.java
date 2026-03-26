package com.example.secura.modulesscreen.utilitiesandmanagement.appmanagement;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppManagementAdapter extends RecyclerView.Adapter<AppManagementAdapter.ViewHolder> {

    private final Context context;
    private final List<AppManagementItem> itemList;

    public AppManagementAdapter(Context context, List<AppManagementItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppManagementItem item = itemList.get(position);
        holder.titleTextView.setText(item.getTitle());
        holder.descriptionTextView.setText(item.getDescription());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = null;
            if (item.getTitle().equals("List Installed Apps")) {
                intent = new Intent(context, InstalledAppsListActivity.class);
            } else if (item.getTitle().equals("Permissions Management")) {
                // For now, this will lead to the list of apps, then you select an app for permissions
                // Or you could make a dedicated activity to list permissions across all apps.
                // For simplicity, we'll direct to the app list first.
                intent = new Intent(context, InstalledAppsListActivity.class);
                intent.putExtra("feature_mode", "permissions"); // Indicate mode if needed
            } else if (item.getTitle().equals("App Information & Interaction")) {
                intent = new Intent(context, InstalledAppsListActivity.class);
                intent.putExtra("feature_mode", "details");
            } else if (item.getTitle().equals("Data Management")) {
                intent = new Intent(context, InstalledAppsListActivity.class);
                intent.putExtra("feature_mode", "data");
            } else if (item.getTitle().equals("Background Processing & Lifecycle")) {
                intent = new Intent(context, InstalledAppsListActivity.class);
                intent.putExtra("feature_mode", "usage_stats");
            }

            if (intent != null) {
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(android.R.id.text1);
            descriptionTextView = itemView.findViewById(android.R.id.text2);
        }
    }
}

