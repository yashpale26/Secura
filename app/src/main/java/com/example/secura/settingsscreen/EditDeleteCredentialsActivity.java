package com.example.secura.settingsscreen; // Adjust package as needed

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat; // Import for ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secura.R;
import com.example.secura.splashloginregister.DatabaseHelper;

import java.util.List;

public class EditDeleteCredentialsActivity extends AppCompatActivity implements SelectableUserAdapter.OnSelectionChangeListener {

    private RecyclerView recyclerView;
    private SelectableUserAdapter adapter;
    private DatabaseHelper databaseHelper;
    private Button btnEditSelected, btnDeleteSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_delete_credentials);

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
                onBackPressed(); // Go back to DatabaseDataActivity
            }
        });

        recyclerView = findViewById(R.id.recycler_view_selectable_credentials);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseHelper = new DatabaseHelper(this);

        btnEditSelected = findViewById(R.id.btn_edit_selected);
        btnDeleteSelected = findViewById(R.id.btn_delete_selected);

        // Initialize and set up the adapter
        loadUsersForSelection();

        btnEditSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> selectedUsernames = adapter.getSelectedUsernames();
                if (selectedUsernames.size() == 1) {
                    String usernameToEdit = selectedUsernames.get(0);
                    Intent intent = new Intent(EditDeleteCredentialsActivity.this, EditSingleCredentialActivity.class);
                    intent.putExtra("username_to_edit", usernameToEdit);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                } else {
                    Toast.makeText(EditDeleteCredentialsActivity.this, "Please select exactly one account to edit.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDeleteSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> selectedUsernames = adapter.getSelectedUsernames();
                if (selectedUsernames.isEmpty()) {
                    Toast.makeText(EditDeleteCredentialsActivity.this, "Please select accounts to delete.", Toast.LENGTH_SHORT).show();
                    return;
                }

                showDeleteConfirmationDialog(selectedUsernames);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning to this activity (e.g., after editing/deleting in EditSingleCredentialActivity)
        loadUsersForSelection();
    }

    private void loadUsersForSelection() {
        Cursor cursor = databaseHelper.getAllUsers();
        if (cursor != null) {
            if (adapter == null) {
                adapter = new SelectableUserAdapter(cursor, this);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.swapCursor(cursor);
            }
            if (cursor.getCount() == 0) {
                Toast.makeText(this, "No registered accounts found.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Error loading users.", Toast.LENGTH_SHORT).show();
            if (adapter != null) {
                adapter.swapCursor(null);
            }
        }
        // Update button states immediately after loading
        onSelectionChanged(adapter != null ? adapter.getSelectedUsernames().size() : 0);
    }

    @Override
    public void onSelectionChanged(int selectedCount) {
        btnEditSelected.setEnabled(selectedCount == 1);
        btnDeleteSelected.setEnabled(selectedCount > 0);
    }

    private void showDeleteConfirmationDialog(List<String> usernamesToDelete) {
        // Corrected line: Using Theme.MaterialComponents.Light.Dialog.Alert
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_AlertDialog);
        builder.setTitle("Confirm Deletion");
        String message = "Are you sure you want to delete the following account(s)?\n\n";
        for (String username : usernamesToDelete) {
            message += "- " + username + "\n";
        }
        builder.setMessage(message);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                performDeletion(usernamesToDelete);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        // Apply custom button colors if needed (after show())
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.error_red));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.blue_standard));
    }


    private void performDeletion(List<String> usernamesToDelete) {
        int deletedCount = 0;
        for (String username : usernamesToDelete) {
            if (databaseHelper.deleteUser(username)) {
                deletedCount++;
            }
        }
        if (deletedCount > 0) {
            Toast.makeText(this, deletedCount + " account(s) deleted successfully.", Toast.LENGTH_SHORT).show();
            loadUsersForSelection(); // Refresh the list after deletion
        } else {
            Toast.makeText(this, "Failed to delete accounts.", Toast.LENGTH_SHORT).show();
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
