package com.example.secura.splashloginregister;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secura.R;
import com.example.secura.homescreen.HomeActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Example: Apply a fade-in animation to a logo
        ImageView logo = findViewById(R.id.splash_logo);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logo.startAnimation(fadeInAnimation);

        // Use Handler to check for login status after the splash screen timeout
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Get SharedPreferences to check for login state
                SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.contains(LoginActivity.KEY_LOGGED_IN_USERNAME);

                Intent i;
                if (isLoggedIn) {
                    // User is already logged in, go directly to HomeActivity
                    i = new Intent(SplashActivity.this, HomeActivity.class);
                } else {
                    // User is not logged in, go to LoginActivity
                    i = new Intent(SplashActivity.this, LoginActivity.class);
                }

                startActivity(i);
                // Apply a transition animation when moving to the next activity
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish(); // Close the splash activity
            }
        }, SPLASH_TIME_OUT);
    }
}