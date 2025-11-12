package com.example.callepeninsulares;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.slider.Slider;

public class SettingsFragment extends Fragment {

    private Switch switchTalkBack, switchHighContrast;
    private Slider sliderTextSize;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        prefs = requireContext().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);

        switchTalkBack = view.findViewById(R.id.switchTalkBack);
        switchHighContrast = view.findViewById(R.id.switchHighContrast);
        sliderTextSize = view.findViewById(R.id.sliderTextSize);

        // Load saved settings
        switchTalkBack.setChecked(prefs.getBoolean("talkBack", false));
        switchHighContrast.setChecked(prefs.getBoolean("highContrast", false));
        sliderTextSize.setValue(prefs.getFloat("textSize", 16f));

        // TalkBack toggle
        switchTalkBack.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("talkBack", isChecked).apply();
            if (isChecked) promptEnableTalkBack();
        });

        // High contrast
        switchHighContrast.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("highContrast", isChecked).apply();
            applyHighContrastMode(isChecked);
        });

        // Text size
        sliderTextSize.addOnChangeListener((slider, value, fromUser) -> {
            prefs.edit().putFloat("textSize", value).apply();
            applyTextSizeToAllViews(value);
        });

        // Apply current settings to this fragment immediately
        applyHighContrastMode(switchHighContrast.isChecked());
        applyTextSizeToAllViews(sliderTextSize.getValue());

        return view;
    }

    private void applyHighContrastMode(boolean enabled) {
        View root = getView();
        if (root == null) return;

        if (enabled) {
            // Apply dark background with white text
            root.setBackgroundColor(Color.BLACK);
            applyTextColorToAllTextViews(root, Color.WHITE);
            applyBackgroundColorToAllViews(root, Color.BLACK);
        } else {
            // Revert to the default background and text color
            root.setBackgroundResource(R.drawable.modern_gradient_bg);  // Assuming this is the default background
            applyTextColorToAllTextViews(root, Color.BLACK);
            applyBackgroundColorToAllViews(root, Color.WHITE);
        }
    }

    private void applyBackgroundColorToAllViews(View view, int color) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyBackgroundColorToAllViews(group.getChildAt(i), color);
            }
        } else {
            view.setBackgroundColor(color);
        }
    }


    private void applyTextSizeToAllViews(float sizeSp) {
        View root = getView();
        if (root != null) applyTextSize(root, sizeSp);
    }

    private void applyTextSize(View view, float sizeSp) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyTextSize(group.getChildAt(i), sizeSp);
            }
        } else if (view instanceof TextView) {
            ((TextView) view).setTextSize(sizeSp);
        }
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

    public void promptEnableTalkBack() {
        try {
            Intent intent = new Intent();
            intent.setAction("android.settings.ACCESSIBILITY_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Optional: direct TalkBack settings
            intent.setClassName("com.android.settings",
                    "com.android.settings.accessibility.TalkBackSettingsActivity");

            startActivity(intent);
            Toast.makeText(getContext(),
                    "Please enable TalkBack to use this feature.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            // Fallback to general Accessibility settings
            Intent fallbackIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(fallbackIntent);
            Toast.makeText(getContext(),
                    "Could not open TalkBack directly. Please enable it in Accessibility settings.",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    @Override
    public void onResume() {
        super.onResume();

        // Reload preferences when coming back to Settings
        boolean talkBack = prefs.getBoolean("talkBack", false);
        boolean highContrast = prefs.getBoolean("highContrast", false);
        float textSize = prefs.getFloat("textSize", 16f);

        switchTalkBack.setChecked(talkBack);
        switchHighContrast.setChecked(highContrast);
        sliderTextSize.setValue(textSize);

        // Apply them again visually
        applyHighContrastMode(highContrast);
        applyTextSizeToAllViews(textSize);
    }

}
