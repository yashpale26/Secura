package com.example.secura.modulesscreen.scanners.callandsmsscan;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.example.secura.R;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SpamItemAdapter extends BaseAdapter {
    private Context context;
    private List<SpamItem> spamItems;
    private LayoutInflater inflater;

    public SpamItemAdapter(Context context, List<SpamItem> spamItems) {
        this.context = context;
        this.spamItems = spamItems;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return spamItems.size();
    }

    @Override
    public SpamItem getItem(int position) {
        return spamItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_spam, parent, false);
            holder = new ViewHolder();
            holder.typeTextView = convertView.findViewById(R.id.spamItemType);
            holder.senderTextView = convertView.findViewById(R.id.spamItemSender);
            holder.contentTextView = convertView.findViewById(R.id.spamItemContent);
            holder.dateTextView = convertView.findViewById(R.id.spamItemDate);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SpamItem item = getItem(position);
        holder.typeTextView.setText(item.getType());
        holder.senderTextView.setText(item.getSender());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        holder.dateTextView.setText(dateFormat.format(new Date(item.getDate())));

        if (item.getType().equals("Call")) {
            holder.contentTextView.setText(String.format("Duration: %d seconds", item.getDuration()));
        } else {
            holder.contentTextView.setText(item.getMessage());
        }

        if (parent instanceof ListView) {
            ListView listView = (ListView) parent;
            if (listView.isItemChecked(position)) {
                convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_ultralight));
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        return convertView;
    }

    static class ViewHolder {
        TextView typeTextView;
        TextView senderTextView;
        TextView contentTextView;
        TextView dateTextView;
    }

    public int getCheckedItemCount() {
        return 0;
    }

    public void clearSelection() {
    }
}