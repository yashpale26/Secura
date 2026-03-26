package com.example.secura.settingsscreen; // Replace with your package name

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.secura.R; // Ensure this is correct for your R file
import com.example.secura.settingsscreen.ProfileActivity; // Import the new ProfileActivity
// Import the new activities
import com.example.secura.settingsscreen.DatabaseDataActivity;
import com.example.secura.settingsscreen.AboutActivity;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    @SuppressLint("MissingInflatedId")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Find the CardViews
        CardView profileCard = view.findViewById(R.id.card_profile);
        CardView databaseDataCard = view.findViewById(R.id.card_database_data);
        CardView aboutCard = view.findViewById(R.id.card_about);

        // Set OnClickListener for the Profile CardView
        profileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the ProfileActivity when the Profile CardView is clicked
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                startActivity(intent);
                // Optional: Add a transition animation
                if (getActivity() != null) {
                    getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }
            }
        });

        // Set OnClickListener for Database Data CardView
        databaseDataCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the DatabaseDataActivity when the Database Data CardView is clicked
                Intent intent = new Intent(getActivity(), DatabaseDataActivity.class);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }
            }
        });

        // Set OnClickListener for About CardView
        aboutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the AboutActivity when the About CardView is clicked
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }
            }
        });

        return view;
    }
}
