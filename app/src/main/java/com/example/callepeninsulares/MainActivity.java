package com.example.callepeninsulares;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.callepeninsulares.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

     ActivityMainBinding binding;
    DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        binding.signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.signupEmail.getText().toString();
                String password = binding.signupPassword.getText().toString();
                String confirmPassword = binding.signupConfirm.getText().toString();

                if (email.equals("") || password.equals("") || confirmPassword.equals(""))
                    Toast.makeText(MainActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                else {
                     if (password.equals(confirmPassword)){
                         Boolean checkUserEmail = databaseHelper.checkEmail(email);

                         if(checkUserEmail == false){
                             Boolean insert = databaseHelper.insertData(email, password);

                             if (insert == true){
                                 Toast.makeText(MainActivity.this, "Signup Successfully", Toast.LENGTH_SHORT).show();
                                 Intent intent = new Intent(getApplicationContext(), Login.class);
                                 startActivity(intent);
                             }else{
                                 Toast.makeText(MainActivity.this, "Signup Failed", Toast.LENGTH_SHORT).show();
                             }
                         }else{
                             Toast.makeText(MainActivity.this, "User already exist, Please Login!", Toast.LENGTH_SHORT).show();
                         }
                     }else{
                         Toast.makeText(MainActivity.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                     }
                }
            }
        });
        binding.loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
            }
        });
    }
}