package com.example.callepeninsulares;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.callepeninsulares.databinding.ActivityHomeBinding;
import com.example.callepeninsulares.databinding.ActivityLoginBinding;

public class Home extends AppCompatActivity {

    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // ✅ Popup menu attached to ImageButton
        binding.menuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.popup_options, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                int id = menuItem.getItemId();
                if (id == R.id.popup_home) {
                    Intent intent = new Intent(getApplicationContext(), Home.class);
                    startActivity(intent);
                } else if (id == R.id.popup_schedules) {
                    Intent intent = new Intent(getApplicationContext(), activity_schedule_list.class);
                    startActivity(intent);
                } else if (id == R.id.popup_settings) {
                    Toast.makeText(this, "Go to Settings", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
            popupMenu.show();
        });

        // Your existing button
        binding.medinaBtn.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, activity_medina.class);
            startActivity(intent);
        });
    }
}