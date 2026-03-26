package com.example.secura.modulesscreen.utilitiesandmanagement.appmanagement;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secura.R;

import java.util.List;

public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.ViewHolder> {

    private final Context context;
    private final List<AppInfoHelper.PermissionDetail> permissionList;

    public PermissionAdapter(Context context, List<AppInfoHelper.PermissionDetail> permissionList) {
        this.context = context;
        this.permissionList = permissionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_permission_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfoHelper.PermissionDetail permission = permissionList.get(position);

        // Display permission name and protection level
        holder.permissionName.setText(permission.name);
        holder.protectionLevel.setText(permission.getProtectionLevelString());

        // Display permission description (if available)
        if (permission.description != null && !permission.description.isEmpty()) {
            holder.permissionDescription.setText(permission.description);
            holder.permissionDescription.setVisibility(View.VISIBLE);
        } else {
            holder.permissionDescription.setVisibility(View.GONE);
        }

        // Indicate if granted or not
        if (permission.granted) {
            holder.permissionStatus.setText("Granted");
            holder.permissionStatus.setTextColor(context.getResources().getColor(R.color.green_success));
        } else {
            holder.permissionStatus.setText("Denied");
            holder.permissionStatus.setTextColor(context.getResources().getColor(R.color.red_error));
        }
    }

    @Override
    public int getItemCount() {
        return permissionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView permissionName;
        TextView protectionLevel;
        TextView permissionDescription;
        TextView permissionStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            permissionName = itemView.findViewById(R.id.permissionName);
            protectionLevel = itemView.findViewById(R.id.protectionLevel);
            permissionDescription = itemView.findViewById(R.id.permissionDescription);
            permissionStatus = itemView.findViewById(R.id.permissionStatus);
        }
    }
}
