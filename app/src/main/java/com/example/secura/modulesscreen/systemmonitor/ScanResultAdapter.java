package com.example.secura.modulesscreen.systemmonitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secura.R;
import com.example.secura.modulesscreen.systemmonitor.ScanResult;

import java.util.List;

public class ScanResultAdapter extends RecyclerView.Adapter<ScanResultAdapter.ViewHolder> {

    private final Context context;
    private final List<ScanResult> results;

    public ScanResultAdapter(Context context, List<ScanResult> results) {
        this.context = context;
        this.results = results;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_scan_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanResult result = results.get(position);
        holder.appName.setText(result.getTitle());
        holder.reasonText.setText(result.getReason());
        if (result.getIcon() != null) {
            holder.appIcon.setImageDrawable(result.getIcon());
        } else {
            holder.appIcon.setImageResource(R.drawable.ic_warning); // A placeholder warning icon
        }
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView reasonText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.appIcon);
            appName = itemView.findViewById(R.id.appName);
            reasonText = itemView.findViewById(R.id.reasonText);
        }
    }
}
