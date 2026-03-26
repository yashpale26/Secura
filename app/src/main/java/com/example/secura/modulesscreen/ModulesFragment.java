package com.example.secura.modulesscreen; // Replace with your package name

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.secura.R; // Ensure this matches your package structure
import com.example.secura.modulesscreen.chatbot.ChatbotActivity;
import com.example.secura.modulesscreen.privacyprotectors.PrivacyProtectorsActivity;
import com.example.secura.modulesscreen.securebrowser.SecureBrowserActivity;
import com.example.secura.modulesscreen.systemmonitor.SystemMonitorsActivity;
import com.example.secura.modulesscreen.utilitiesandmanagement.UtilitiesAndManagementActivity;
import com.google.android.material.card.MaterialCardView;

// Import your new activity classes
import com.example.secura.modulesscreen.scanners.ScannersActivity;

public class ModulesFragment extends Fragment {

    public ModulesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_modules, container, false); // [cite: 13]

        // Initialize CardViews
        MaterialCardView cardScanners = view.findViewById(R.id.card_scanners);
        MaterialCardView cardPrivacyProtectors = view.findViewById(R.id.card_privacy_protectors);
        MaterialCardView cardSystemMonitors = view.findViewById(R.id.card_system_monitors);
        MaterialCardView cardUtilitiesManagement = view.findViewById(R.id.card_utilities_management);
        MaterialCardView cardBrowser = view.findViewById(R.id.card_browser);
        MaterialCardView cardAssistance = view.findViewById(R.id.card_assistance);

        // Set click listeners for each CardView
        cardScanners.setOnClickListener(v -> {
            applyClickAnimation(v);
            startActivity(new Intent(getActivity(), ScannersActivity.class));
        });

        cardPrivacyProtectors.setOnClickListener(v -> {
            applyClickAnimation(v);
            startActivity(new Intent(getActivity(), PrivacyProtectorsActivity.class));
        });

        cardSystemMonitors.setOnClickListener(v -> {
            applyClickAnimation(v);
            startActivity(new Intent(getActivity(), SystemMonitorsActivity.class));
        });

        cardUtilitiesManagement.setOnClickListener(v -> {
            applyClickAnimation(v);
            startActivity(new Intent(getActivity(), UtilitiesAndManagementActivity.class));
        });

        cardBrowser.setOnClickListener(v -> {
            applyClickAnimation(v);
            startActivity(new Intent(getActivity(), SecureBrowserActivity.class));
        });

        cardAssistance.setOnClickListener(v -> {
            applyClickAnimation(v);
            startActivity(new Intent(getActivity(), ChatbotActivity.class));
        });

        return view;
    }

    private void applyClickAnimation(View view) {
        // Scale down animation
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);

        // Scale up animation
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f);
        scaleUpX.setDuration(100);
        scaleUpY.setDuration(100);

        scaleDownX.start();
        scaleDownY.start();

        scaleDownY.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                scaleUpX.start();
                scaleUpY.start();
            }
        });
    }
}