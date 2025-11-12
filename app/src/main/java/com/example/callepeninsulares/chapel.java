package com.example.callepeninsulares;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class chapel extends AppCompatActivity {

    ImageView backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chapel);

        backBtn = findViewById(R.id.backBtn);

        SharedPreferences settingsPrefs = getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
        boolean highContrast = settingsPrefs.getBoolean("highContrast", false);
        float textSize = settingsPrefs.getFloat("textSize", 16f);

// Apply settings
        View rootView = findViewById(android.R.id.content); // Root view of the activity

        if (highContrast) {
            rootView.setBackgroundColor(Color.BLACK);
            applyTextColorToAllTextViews(rootView, Color.WHITE);
        } else {
            rootView.setBackgroundResource(R.drawable.modern_gradient_bg);  // Default background
            applyTextColorToAllTextViews(rootView, Color.BLACK);
        }

        applyTextSizeToAllViews(rootView, textSize);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(chapel.this, Home.class);
                startActivity(intent);
            }
        });
    }
    private void applyTextColorToAllTextViews(View view, int color) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyTextColorToAllTextViews(group.getChildAt(i), color);
            }
        } else if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
        }
    }

    private void applyTextSizeToAllViews(View view, float sizeSp) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyTextSizeToAllViews(group.getChildAt(i), sizeSp);
            }
        } else if (view instanceof TextView) {
            ((TextView) view).setTextSize(sizeSp);
        }
    }
}