package com.example.callepeninsulares;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment {

    private TextView userEmail, userName;
    private ImageView profileImage;
    private MaterialButton btnEditProfile, btnResetPassword, btnLogout;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profileImage);
        userEmail = view.findViewById(R.id.userEmail);
        userName = view.findViewById(R.id.userName);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnResetPassword = view.findViewById(R.id.btnResetPassword);
        btnLogout = view.findViewById(R.id.btnLogout);

        dbHelper = new DatabaseHelper(requireContext());

        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String email = prefs.getString("email", "Unknown User");
        userEmail.setText(email);

        SharedPreferences settingsPrefs = requireContext().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
        boolean highContrast = settingsPrefs.getBoolean("highContrast", false);
        float textSize = settingsPrefs.getFloat("textSize", 16f);

// Apply high contrast mode
        View root = view;
        if (highContrast) {
            root.setBackgroundColor(Color.BLACK);
            applyTextColorToAllTextViews(root, Color.WHITE);
        } else {
            root.setBackgroundResource(R.drawable.modern_gradient_bg);
            applyTextColorToAllTextViews(root, Color.BLACK);
        }

// Apply text size
        applyTextSizeToAllViews(root, textSize);

        // Safe username extraction
        if (email.contains("@")) {
            userName.setText(email.substring(0, email.indexOf("@")));
        } else {
            userName.setText(email);
        }

        btnEditProfile.setOnClickListener(v -> editProfileDialog(email));
        btnResetPassword.setOnClickListener(v -> showResetDialog(email));
        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    private void editProfileDialog(String email) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);
        EditText newEmailInput = dialogView.findViewById(R.id.editEmailInput);

        newEmailInput.setText(email);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Profile").setView(dialogView).setPositiveButton("Save", (d, w) -> {
                    String newEmail = newEmailInput.getText().toString().trim();
                    if (!newEmail.isEmpty()) {
                        // Update in Database
                        boolean updated = dbHelper.updateUserEmail(email, newEmail);
                        if (updated) {
                            // Update SharedPreferences
                            SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                            prefs.edit().putString("email", newEmail).apply();

                            userEmail.setText(newEmail);
                            if (newEmail.contains("@")) {
                                userName.setText(newEmail.substring(0, newEmail.indexOf("@")));
                            } else {
                                userName.setText(newEmail);
                            }
                            Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update email!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Email cannot be empty!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void showResetDialog(String email) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reset_password, null);
        EditText input = dialogView.findViewById(R.id.newPasswordInput);

        new AlertDialog.Builder(requireContext())
                .setTitle("Reset Password")
                .setView(dialogView)
                .setPositiveButton("Save", (d, w) -> {
                    String newPass = input.getText().toString().trim();
                    if (newPass.isEmpty()) {
                        Toast.makeText(requireContext(), "Password cannot be empty!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                    String currentEmail = prefs.getString("email", email);

                    if (dbHelper != null) {
                        boolean success = dbHelper.updateUserPassword(currentEmail, newPass);
                        if (success) {
                            Toast.makeText(requireContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void logout() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        new AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    prefs.edit().clear().apply();
                    Intent i = new Intent(requireContext(), Login.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                })
                .setNegativeButton("Cancel", null)
                .show();
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
