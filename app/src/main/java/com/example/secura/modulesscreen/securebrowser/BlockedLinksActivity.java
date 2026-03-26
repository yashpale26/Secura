package com.example.secura.modulesscreen.securebrowser;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secura.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockedLinksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BlockedLinksAdapter adapter;
    private LinkBlocker linkBlocker;
    private TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_links);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Blocked Links");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.blocked_links_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);
        linkBlocker = new LinkBlocker(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadBlockedLinks();
    }

    private void loadBlockedLinks() {
        Map<String, String[]> blockedLinksMap = linkBlocker.getAllBlockedLinks();
        if (blockedLinksMap.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
            List<BlockedLink> blockedLinks = new ArrayList<>();
            for (Map.Entry<String, String[]> entry : blockedLinksMap.entrySet()) {
                blockedLinks.add(new BlockedLink(entry.getValue()[0], entry.getKey(), entry.getValue()[1]));
            }
            adapter = new BlockedLinksAdapter(this, blockedLinks, linkBlocker, () -> loadBlockedLinks());
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

// Data class to hold a blocked link's information
class BlockedLink {
    private String name;
    private String url;
    private String description;

    public BlockedLink(String name, String url, String description) {
        this.name = name;
        this.url = url;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }
}

// RecyclerView Adapter for the blocked links list
class BlockedLinksAdapter extends RecyclerView.Adapter<BlockedLinksAdapter.BlockedLinkViewHolder> {

    private final Context context;
    private final List<BlockedLink> blockedLinks;
    private final LinkBlocker linkBlocker;
    private final Runnable onListUpdated;

    public BlockedLinksAdapter(Context context, List<BlockedLink> blockedLinks, LinkBlocker linkBlocker, Runnable onListUpdated) {
        this.context = context;
        this.blockedLinks = blockedLinks;
        this.linkBlocker = linkBlocker;
        this.onListUpdated = onListUpdated;
    }

    @Override
    public BlockedLinkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blocked_link, parent, false);
        return new BlockedLinkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BlockedLinkViewHolder holder, int position) {
        BlockedLink link = blockedLinks.get(position);
        holder.nameTextView.setText(link.getName());
        holder.urlTextView.setText(link.getUrl());
        holder.descriptionTextView.setText(link.getDescription());

        holder.unblockButton.setOnClickListener(v -> {
            linkBlocker.unblockLink(link.getUrl());
            onListUpdated.run();
            Toast.makeText(context, "Link unblocked.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return blockedLinks.size();
    }

    static class BlockedLinkViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView urlTextView;
        TextView descriptionTextView;
        Button unblockButton;

        BlockedLinkViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.blocked_link_name);
            urlTextView = itemView.findViewById(R.id.blocked_link_url);
            descriptionTextView = itemView.findViewById(R.id.blocked_link_description);
            unblockButton = itemView.findViewById(R.id.unblock_button);
        }
    }
}