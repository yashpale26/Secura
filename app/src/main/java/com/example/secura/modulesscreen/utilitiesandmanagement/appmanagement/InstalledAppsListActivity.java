package com.example.secura.modulesscreen.utilitiesandmanagement.appmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secura.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class InstalledAppsListActivity extends AppCompatActivity implements App2ListAdapter.OnAppClickListener {

    private RecyclerView recyclerView;
    private App2ListAdapter adapter;
    private AppInfoHelper appInfoHelper;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private boolean showSystemApps = false; // Default: hide system apps
    private ExecutorService executorService; // For background processing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installed_apps_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Installed Apps");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.appsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        appInfoHelper = new AppInfoHelper(this);
        executorService = Executors.newSingleThreadExecutor(); // Use a single thread for app loading

        loadInstalledApps();
    }

    /**
     * Loads installed applications in a background thread to avoid blocking the UI.
     */
    private void loadInstalledApps() {
        progressBar.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);

        executorService.execute(() -> {
            // Get the list of apps
            List<AppInfoHelper.AppInfo> apps = appInfoHelper.getInstalledApps(showSystemApps);

            // Sort applications alphabetically by name
            Collections.sort(apps, Comparator.comparing(a -> a.appName.toLowerCase()));

            runOnUiThread(() -> {
                // Update UI on the main thread
                if (apps.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    if (adapter == null) {
                        adapter = new App2ListAdapter(this, apps, this);
                        recyclerView.setAdapter(adapter);
                    } else {
                        adapter.updateList(apps); // Update existing adapter
                    }
                }
                progressBar.setVisibility(View.GONE);
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_installed_apps, menu);

        // Set up the search view
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView != null) {
            searchView.setQueryHint("Search apps by name or package...");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (adapter != null) {
                        adapter.getFilter().filter(newText);
                    }
                    return true;
                }
            });
        }

        // Update the "Show System Apps" checkbox state
        MenuItem showSystemAppsToggle = menu.findItem(R.id.action_show_system_apps);
        showSystemAppsToggle.setChecked(showSystemApps);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Handle back button click on toolbar
            return true;
        } else if (item.getItemId() == R.id.action_show_system_apps) {
            showSystemApps = !item.isChecked();
            item.setChecked(showSystemApps); // Update the checkbox state
            loadInstalledApps(); // Reload apps based on the new filter
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAppClick(AppInfoHelper.AppInfo appInfo) {
        // When an app is clicked, navigate to AppDetailsActivity
        Intent intent = new Intent(this, AppDetailsActivity.class);
        intent.putExtra("packageName", appInfo.packageName);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow(); // Shut down the executor service
    }
}
