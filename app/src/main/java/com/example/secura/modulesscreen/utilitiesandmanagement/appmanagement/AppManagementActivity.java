package com.example.secura.modulesscreen.utilitiesandmanagement.appmanagement;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar; // Import Toolbar

import com.example.secura.R;

import java.util.ArrayList;
import java.util.List;

public class AppManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppManagementAdapter adapter;
    private List<AppManagementItem> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_management);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("App Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back button
        }

        recyclerView = findViewById(R.id.appManagementRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();
        itemList.add(new AppManagementItem("List Installed Apps", "View all applications installed on your device."));
        itemList.add(new AppManagementItem("Permissions Management", "Manage permissions granted to applications."));
        itemList.add(new AppManagementItem("App Information & Interaction", "Get detailed info and interact with apps (uninstall, force stop)."));
        itemList.add(new AppManagementItem("Data Management", "Clear cache or data for installed applications."));
        itemList.add(new AppManagementItem("Background Processing & Lifecycle", "Monitor app usage and background activity."));
        // Add more features as per request

        adapter = new AppManagementAdapter(this, itemList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Handle back button click on toolbar
        return true;
    }
}
