package com.example.secura.settingsscreen; // Adjust package as needed

import android.database.Cursor;
import android.text.method.PasswordTransformationMethod; // Import this
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secura.R;
import com.example.secura.splashloginregister.DatabaseHelper; // Import DatabaseHelper

public class UserCredentialAdapter extends RecyclerView.Adapter<UserCredentialAdapter.UserViewHolder> {

    Cursor mCursor;

    public UserCredentialAdapter(Cursor cursor) {
        mCursor = cursor;
    }

    // Method to update the cursor with new data
    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_credential, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return; // Should not happen
        }

        // Get column indices
        int nameColumnIndex = mCursor.getColumnIndex(DatabaseHelper.COL_NAME);
        int usernameColumnIndex = mCursor.getColumnIndex(DatabaseHelper.COL_USERNAME);
        int passwordHashColumnIndex = mCursor.getColumnIndex(DatabaseHelper.COL_PASSWORD_HASH);

        // Check if column exists before getting value
        String name = (nameColumnIndex != -1) ? mCursor.getString(nameColumnIndex) : "N/A";
        String username = (usernameColumnIndex != -1) ? mCursor.getString(usernameColumnIndex) : "N/A";
        String password = (passwordHashColumnIndex != -1) ? mCursor.getString(passwordHashColumnIndex) : "N/A";

        holder.tvName.setText("Name: " + name);
        holder.tvUsername.setText("Username: " + username);

        // Store original password
        holder.originalPassword = password;
        holder.isPasswordVisible = false; // Set initial hidden state

        // Set initial password visibility (hidden)
        holder.tvPassword.setText("Password: " + holder.originalPassword); // Set the actual password
        holder.tvPassword.setTransformationMethod(PasswordTransformationMethod.getInstance()); // Apply masking
        holder.ivTogglePassword.setImageResource(R.drawable.ic_visibility); // Assumes ic_visibility is available

        // Set click listener for toggle icon
        holder.ivTogglePassword.setOnClickListener(v -> {
            holder.isPasswordVisible = !holder.isPasswordVisible;
            if (holder.isPasswordVisible) {
                holder.tvPassword.setTransformationMethod(null); // Remove masking to show password
                holder.ivTogglePassword.setImageResource(R.drawable.ic_visibility_off); // Assumes ic_visibility_off is available
            } else {
                holder.tvPassword.setTransformationMethod(PasswordTransformationMethod.getInstance()); // Apply masking to hide password
                holder.ivTogglePassword.setImageResource(R.drawable.ic_visibility);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvUsername;
        TextView tvPassword;
        ImageView ivTogglePassword;
        String originalPassword; // Store the actual password
        boolean isPasswordVisible; // State of password visibility

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_credential_name);
            tvUsername = itemView.findViewById(R.id.tv_credential_username);
            tvPassword = itemView.findViewById(R.id.tv_credential_password);
            ivTogglePassword = itemView.findViewById(R.id.iv_toggle_password);
        }
    }
}
