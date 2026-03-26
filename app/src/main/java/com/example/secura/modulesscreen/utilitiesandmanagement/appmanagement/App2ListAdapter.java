package com.example.secura.modulesscreen.utilitiesandmanagement.appmanagement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secura.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class App2ListAdapter extends RecyclerView.Adapter<App2ListAdapter.ViewHolder> implements Filterable {

    private final Context context;
    private List<AppInfoHelper.AppInfo> appList; // Original list
    private List<AppInfoHelper.AppInfo> appListFiltered; // Filtered list
    private OnAppClickListener onAppClickListener;

    public App2ListAdapter(Context context, List<AppInfoHelper.AppInfo> appList, OnAppClickListener listener) {
        this.context = context;
        this.appList = appList;
        this.appListFiltered = new ArrayList<>(appList); // Initialize filtered list
        this.onAppClickListener = listener;
    }

    // Interface for click events on app items
    public interface OnAppClickListener {
        void onAppClick(AppInfoHelper.AppInfo appInfo);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfoHelper.AppInfo appInfo = appListFiltered.get(position);
        holder.appIcon.setImageDrawable(appInfo.icon);
        holder.appName.setText(appInfo.appName);
        holder.packageName.setText(appInfo.packageName);

        holder.itemView.setOnClickListener(v -> {
            if (onAppClickListener != null) {
                onAppClickListener.onAppClick(appInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appListFiltered.size();
    }

    // ViewHolder class for individual app items
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView packageName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.appIcon);
            appName = itemView.findViewById(R.id.appName);
            packageName = itemView.findViewById(R.id.appPackageName);
        }
    }

    /**
     * Updates the data set and notifies the adapter.
     * @param newAppList The new list of apps.
     */
    public void updateList(List<AppInfoHelper.AppInfo> newAppList) {
        this.appList = newAppList;
        this.appListFiltered = new ArrayList<>(newAppList);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString().toLowerCase(Locale.getDefault());
                List<AppInfoHelper.AppInfo> filteredList = new ArrayList<>();

                if (charString.isEmpty()) {
                    filteredList.addAll(appList);
                } else {
                    for (AppInfoHelper.AppInfo app : appList) {
                        // Filter by app name or package name
                        if (app.appName.toLowerCase(Locale.getDefault()).contains(charString) ||
                                app.packageName.toLowerCase(Locale.getDefault()).contains(charString)) {
                            filteredList.add(app);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                appListFiltered.clear();
                appListFiltered.addAll((List<AppInfoHelper.AppInfo>) filterResults.values);
                notifyDataSetChanged();
            }
        };
    }
}
