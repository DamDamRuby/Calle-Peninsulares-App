package com.example.callepeninsulares;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.content.IntentFilter;
import android.os.Build;
import android.annotation.SuppressLint;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class activity_schedule_list extends AppCompatActivity {

    // ✅ Declare broadcast receiver inside the class
    private BroadcastReceiver refreshReceiver;

    DatabaseHelper dbHelper;
    ArrayList<Schedule> schedules;
    ArrayList<String> listItems;
    ArrayAdapter<String> adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_list);

        dbHelper = new DatabaseHelper(this);
        listView = findViewById(R.id.scheduleListView);
        schedules = new ArrayList<>();
        listItems = new ArrayList<>();

        // 🔹 Load schedules initially
        loadSchedulesFromDatabase();

        // 🔹 Open edit screen on item click
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Schedule selectedSchedule = schedules.get(position);
            Intent intent = new Intent(activity_schedule_list.this, EditScheduleActivity.class);
            intent.putExtra("scheduleId", selectedSchedule.getId());
            startActivity(intent);
        });
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();

        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadSchedulesFromDatabase();
            }
        };

        IntentFilter filter = new IntentFilter("com.example.callepeninsulares.REFRESH_SCHEDULES");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+ (API 33)
            registerReceiver(refreshReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            // For Android 12 and below
            registerReceiver(refreshReceiver, filter);
        }


        loadSchedulesFromDatabase();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // ✅ Unregister to avoid leaks
        if (refreshReceiver != null) {
            unregisterReceiver(refreshReceiver);
            refreshReceiver = null;
        }
    }

    // ✅ Load all schedules and display
    private void loadSchedulesFromDatabase() {
        schedules.clear();

        Cursor cursor = dbHelper.getAllSchedules();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Schedule sched = new Schedule(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getInt(7),
                        cursor.getInt(8) == 1, // isOnlineClass
                        cursor.getString(9)    // meetLink
                );
                schedules.add(sched);
            } while (cursor.moveToNext());
            cursor.close();
        }

        // ✅ Sort by date & time
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("d/M/yyyy hh:mm a", java.util.Locale.getDefault());
        schedules.sort((s1, s2) -> {
            try {
                java.util.Date d1 = sdf.parse(s1.getDate() + " " + s1.getStartTime());
                java.util.Date d2 = sdf.parse(s2.getDate() + " " + s2.getStartTime());
                return d1.compareTo(d2);
            } catch (Exception e) {
                return 0;
            }
        });

        // ✅ Use custom adapter instead of ArrayAdapter<String>
        ScheduleAdapter adapter = new ScheduleAdapter(this, schedules);
        listView.setAdapter(adapter);
    }
}