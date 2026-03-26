package com.example.secura.modulesscreen.privacyprotectors.applock;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.secura.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppListAdapter extends BaseAdapter {

    private final Context context;
    private final List<ApplicationInfo> apps;
    private final PackageManager pm;
    private Set<String> selectedApps;  // package names

    public AppListAdapter(Context context, List<ApplicationInfo> apps, Set<String> selectedApps) {
        this.context = context;
        this.apps = apps;
        this.selectedApps = new HashSet<>(selectedApps);
        pm = context.getPackageManager();
    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public ApplicationInfo getItem(int position) {
        return apps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Set<String> getSelectedApps() {
        return selectedApps;
    }

    public void setSelectedApps(Set<String> selectedApps) {
        this.selectedApps = new HashSet<>(selectedApps);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_app_lock, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.appIcon);
            holder.name = convertView.findViewById(R.id.appName);
            holder.checkbox = convertView.findViewById(R.id.appCheckbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ApplicationInfo appInfo = getItem(position);
        Drawable icon = pm.getApplicationIcon(appInfo);
        String label = pm.getApplicationLabel(appInfo).toString();

        holder.icon.setImageDrawable(icon);
        holder.name.setText(label);

        String packageName = appInfo.packageName;
        holder.checkbox.setOnCheckedChangeListener(null);
        holder.checkbox.setChecked(selectedApps.contains(packageName));

        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedApps.add(packageName);
            } else {
                selectedApps.remove(packageName);
            }
        });

        return convertView;
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        CheckBox checkbox;
    }
}
