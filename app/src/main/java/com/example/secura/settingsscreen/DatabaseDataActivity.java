package com.example.secura.settingsscreen; // Adjust package as needed

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secura.R;
import com.example.secura.splashloginregister.DatabaseHelper;

public class DatabaseDataActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserCredentialAdapter adapter;
    private DatabaseHelper databaseHelper;
    private Button btnEditDeleteCredentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_data);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title
        }

        // Set up back button click listener
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Go back to the previous activity (SettingsFragment)
            }
        });

        recyclerView = findViewById(R.id.recycler_view_credentials);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseHelper = new DatabaseHelper(this);

        // Initialize and set up the adapter
        Cursor cursor = databaseHelper.getAllUsers();
        if (cursor != null && cursor.getCount() > 0) {
            adapter = new UserCredentialAdapter(cursor);
            recyclerView.setAdapter(adapter);
        } else {
            Toast.makeText(this, "No registered accounts found.", Toast.LENGTH_SHORT).show();
            if (cursor != null) {
                cursor.close();
            }
            // Optionally, set an empty adapter or show a message
            adapter = new UserCredentialAdapter(null);
            recyclerView.setAdapter(adapter);
        }


        btnEditDeleteCredentials = findViewById(R.id.btn_edit_delete_credentials);
        btnEditDeleteCredentials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DatabaseDataActivity.this, EditDeleteCredentialsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning to this activity (e.g., after editing/deleting)
        loadCredentials();
    }

    private void loadCredentials() {
        Cursor newCursor = databaseHelper.getAllUsers();
        if (newCursor != null) {
            if (adapter != null) {
                adapter.swapCursor(newCursor);
            } else {
                // This case should ideally not happen if adapter is initialized in onCreate
                adapter = new UserCredentialAdapter(newCursor);
                recyclerView.setAdapter(adapter);
            }
            if (newCursor.getCount() == 0) {
                Toast.makeText(this, "No registered accounts found.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Error loading credentials.", Toast.LENGTH_SHORT).show();
            if (adapter != null) {
                adapter.swapCursor(null); // Clear the list if error or no data
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null && adapter.mCursor != null) {
            adapter.mCursor.close(); // Ensure cursor is closed when activity is destroyed
        }
    }
}
