package com.example.callepeninsulares;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.callepeninsulares.databinding.ActivityLoginBinding;

public class Login extends AppCompatActivity {

    ActivityLoginBinding binding;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        // Check if the user is already logged in through shared preferences
        checkForRememberMe();

        // Handle login button click
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.loginEmail.getText().toString();
                String password = binding.loginPassword.getText().toString();



                if (email.isEmpty() || password.isEmpty())
                    Toast.makeText(Login.this, "All fields are required", Toast.LENGTH_SHORT).show();
                else {
                    // Check user credentials
                    Boolean checkCredentials = databaseHelper.checkEmailPassword(email, password);

                }if (!databaseHelper.isValidEmail(email)) {
                        Toast.makeText(Login.this, "Invalid email format!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Proceed with login
                        Boolean checkCredentials = databaseHelper.checkEmailPassword(email, password);

                    if (checkCredentials) {
                        Toast.makeText(Login.this, "Login Successfully!", Toast.LENGTH_SHORT).show();

                        // Save user session
                        getSharedPreferences("UserSession", MODE_PRIVATE).edit().putString("email", email).apply();

                        // Save "Remember Me" state
                        if (binding.rememberMeCheckBox.isChecked()) {
                            // Save the "Remember Me" status
                            getSharedPreferences("UserSession", MODE_PRIVATE).edit()
                                    .putBoolean("rememberMe", true)
                                    .apply();
                        } else {
                            getSharedPreferences("UserSession", MODE_PRIVATE).edit()
                                    .putBoolean("rememberMe", false)
                                    .apply();
                        }

                        // Redirect to home screen
                        startHomeActivity();
                    } else {
                        Toast.makeText(Login.this, "Invalid Credentials!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Redirect to signup screen
        binding.signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkForRememberMe() {
        // Check if "Remember Me" is enabled
        boolean rememberMe = getSharedPreferences("UserSession", MODE_PRIVATE).getBoolean("rememberMe", false);
        if (rememberMe) {
            String email = getSharedPreferences("UserSession", MODE_PRIVATE).getString("email", null);
            if (email != null) {
                // Auto login user if "Remember Me" is enabled
                startHomeActivity();
            }
        }
    }

    private void startHomeActivity() {
        Intent intent = new Intent(getApplicationContext(), Home.class);
        startActivity(intent);
        finish();  // Close the Login activity so the user cannot go back to it
    }
}
