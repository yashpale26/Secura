package com.example.secura.homescreen; // Replace with your package name

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

// Import all 12 Card Activities
import com.example.secura.homescreen.cards.Card1Activity;
import com.example.secura.homescreen.cards.Card2Activity;
import com.example.secura.homescreen.cards.Card3Activity;
import com.example.secura.homescreen.cards.Card4Activity;
import com.example.secura.homescreen.cards.Card5Activity;
import com.example.secura.homescreen.cards.Card6Activity;
import com.example.secura.homescreen.cards.Card7Activity;
import com.example.secura.homescreen.cards.Card8Activity;
import com.example.secura.homescreen.cards.Card9Activity;
import com.example.secura.homescreen.cards.Card10Activity;
import com.example.secura.homescreen.cards.Card11Activity;
import com.example.secura.homescreen.cards.Card12Activity;
import com.example.secura.R; // Make sure R is imported

import java.util.ArrayList;
import java.util.List;
import me.relex.circleindicator.CircleIndicator3;

public class HomeFragment extends Fragment {

    private ViewPager2 cardCarouselViewPager;
    private CardAdapter cardAdapter;
    private List<CardItem> cardList;
    private Handler autoSlideHandler = new Handler();
    private Runnable autoSlideRunnable;
    private static final long AUTO_SLIDE_INTERVAL = 3000; // 3 seconds

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        cardCarouselViewPager = view.findViewById(R.id.card_carousel_view_pager);
        CircleIndicator3 indicator = view.findViewById(R.id.card_carousel_indicator);

        cardList = new ArrayList<>();
        // Populate with 12 cards, ensuring correct activity mapping
        // Use R.drawable.your_image_name for imageResId if you have images.
        // For now, using 0 or a generic placeholder.
        cardList.add(new CardItem(R.drawable.ic_cmscan, "Malware Scanner", "Scan your device for malware and viruses.", Card1Activity.class));
        cardList.add(new CardItem(R.drawable.ic_cnscan, "Network Scanner", "Scan your WIFI network for security vulnerabilities.", Card2Activity.class));
        cardList.add(new CardItem(R.drawable.ic_ccsscan, "Call & SMS Scanner", "Scan for suspicious spams calls and messages.", Card3Activity.class));
        cardList.add(new CardItem(R.drawable.ic_capplock, "App Lock", "Secure your apps with a pin.", Card4Activity.class));
        cardList.add(new CardItem(R.drawable.ic_cgvault, "Gallery Vault", "Hide your private photos and videos.", Card5Activity.class));
        cardList.add(new CardItem(R.drawable.ic_cintruder, "Intruder Selfie", "Capture photos of unauthorized access attempts.", Card6Activity.class));
        cardList.add(new CardItem(R.drawable.ic_csnote, "Secure Notepad", "Keep your notes private and encrypted.", Card7Activity.class));
        cardList.add(new CardItem(R.drawable.ic_csmonitor, "Security Monitor", "Monitor your device for security threats and breaches.", Card8Activity.class));
        cardList.add(new CardItem(R.drawable.ic_cappmanage, "App Management", "Manage your installed applications and permissions.", Card9Activity.class));
        cardList.add(new CardItem(R.drawable.ic_cbattery, "Battery Monitor", "Monitor battery usage and optimize performance.", Card10Activity.class));
        cardList.add(new CardItem(R.drawable.ic_cbrowser, "Secure Browser", "Secure and private Browse.", Card11Activity.class));
        cardList.add(new CardItem(R.drawable.ic_cchatbot, "Chatbot", "A smart assistant to help you understand cybersecurity terms.", Card12Activity.class));

        cardAdapter = new CardAdapter(cardList, getContext());
        cardCarouselViewPager.setAdapter(cardAdapter);

        // No custom PageTransformer for "peeking" effect here.
        // ViewPager2 will naturally slide one full page at a time with the match_parent item_card.xml.

        indicator.setViewPager(cardCarouselViewPager);

        setupAutoSlide();

        return view;
    }

    private void setupAutoSlide() {
        autoSlideRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = cardCarouselViewPager.getCurrentItem();
                int totalItems = cardList.size();
                int nextItem = (currentItem + 1) % totalItems;
                cardCarouselViewPager.setCurrentItem(nextItem, true); // Smooth scroll
                autoSlideHandler.postDelayed(this, AUTO_SLIDE_INTERVAL);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start auto-sliding when the fragment is resumed
        autoSlideHandler.postDelayed(autoSlideRunnable, AUTO_SLIDE_INTERVAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop auto-sliding when the fragment is paused
        autoSlideHandler.removeCallbacks(autoSlideRunnable);
    }
}