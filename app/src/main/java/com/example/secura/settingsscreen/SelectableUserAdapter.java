package com.example.secura.settingsscreen; // Adjust package as needed

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secura.R;
import com.example.secura.splashloginregister.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class SelectableUserAdapter extends RecyclerView.Adapter<SelectableUserAdapter.SelectableUserViewHolder> {

    Cursor mCursor;
    private List<String> selectedUsernames; // To keep track of selected usernames
    private OnSelectionChangeListener selectionChangeListener;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int selectedCount);
    }

    public SelectableUserAdapter(Cursor cursor, OnSelectionChangeListener listener) {
        mCursor = cursor;
        selectedUsernames = new ArrayList<>();
        this.selectionChangeListener = listener;
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = newCursor;
        selectedUsernames.clear(); // Clear selections when data changes
        if (newCursor != null) {
            notifyDataSetChanged();
        }
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(selectedUsernames.size());
        }
    }

    @NonNull
    @Override
    public SelectableUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selectable_user, parent, false);
        return new SelectableUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectableUserViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        int nameColumnIndex = mCursor.getColumnIndex(DatabaseHelper.COL_NAME);
        int usernameColumnIndex = mCursor.getColumnIndex(DatabaseHelper.COL_USERNAME);

        String name = (nameColumnIndex != -1) ? mCursor.getString(nameColumnIndex) : "N/A";
        String username = (usernameColumnIndex != -1) ? mCursor.getString(usernameColumnIndex) : "N/A";

        holder.tvName.setText("Name: " + name);
        holder.tvUsername.setText("Username: " + username);
        holder.username = username; // Store username in ViewHolder for easy access

        // Set checkbox state based on selection list
        holder.cbSelectUser.setChecked(selectedUsernames.contains(username));

        // Avoid issues with recycling by removing previous listeners
        holder.cbSelectUser.setOnCheckedChangeListener(null);
        holder.cbSelectUser.setOnClickListener(v -> {
            boolean isChecked = holder.cbSelectUser.isChecked();
            if (isChecked) {
                selectedUsernames.add(holder.username);
            } else {
                selectedUsernames.remove(holder.username);
            }
            if (selectionChangeListener != null) {
                selectionChangeListener.onSelectionChanged(selectedUsernames.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    public List<String> getSelectedUsernames() {
        return new ArrayList<>(selectedUsernames); // Return a copy to prevent external modification
    }

    public static class SelectableUserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvUsername;
        CheckBox cbSelectUser;
        String username; // To store the username associated with this row

        public SelectableUserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_selectable_name);
            tvUsername = itemView.findViewById(R.id.tv_selectable_username);
            cbSelectUser = itemView.findViewById(R.id.cb_select_user);
        }
    }
}
