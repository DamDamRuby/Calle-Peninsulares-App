package com.example.callepeninsulares;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class activity_medina extends AppCompatActivity {

    ImageView btnOpen;
    ImageView backBtn;

    private TextView aboutText;
    private TextView facilitiesText;
    private TextView dynamicContent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medina);

        btnOpen = findViewById(R.id.btnOpen);
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


        btnOpen = findViewById(R.id.btnOpen);
        if (btnOpen != null) {
            btnOpen.setOnClickListener(v -> {
                Log.d("activity_medina", "Add Schedule button clicked");
                medsched fragmentDialog = new medsched();
                fragmentDialog.show(getSupportFragmentManager(), "CustomDialog");
            });
        } else {
            Log.e("activity_medina", "btnOpen button not found");
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_medina.this, Home.class);
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