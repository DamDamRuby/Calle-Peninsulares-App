package com.example.callepeninsulares;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.callepeninsulares.EditScheduleActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ScheduleFragment extends Fragment implements EditScheduleActivity.OnScheduleUpdatedListener {

    private static ScheduleFragment instance;

    public ScheduleFragment() {
        instance = this; // set the static reference
    }

    public static void refreshSchedulesStatic() {
        if (instance != null) {
            instance.loadSchedulesFromDatabase();
        }
    }
    private BroadcastReceiver refreshReceiver;
    private DatabaseHelper dbHelper;
    private ArrayList<Schedule> schedules;
    private ArrayList<String> listItems;
    private ArrayAdapter<String> adapter;
    private ListView listView;


    @Override
    public void onScheduleUpdated() {
        loadSchedulesFromDatabase();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        listView = view.findViewById(R.id.scheduleListView);
        schedules = new ArrayList<>();
        listItems = new ArrayList<>();





        SharedPreferences settingsPrefs = requireContext().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
        boolean highContrast = settingsPrefs.getBoolean("highContrast", false);
        float textSize = settingsPrefs.getFloat("textSize", 16f);


        View root = view;
        if (highContrast) {
            root.setBackgroundColor(Color.BLACK);
            applyTextColorToAllTextViews(root, Color.WHITE);
        } else {
            root.setBackgroundResource(R.drawable.modern_gradient_bg);
            applyTextColorToAllTextViews(root, Color.BLACK);
        }

        loadSchedulesFromDatabase();


        listView.setOnItemClickListener((parent, v, position, id) -> {
            Schedule selectedSchedule = schedules.get(position);


            EditScheduleActivity fragment = new EditScheduleActivity();


            Bundle args = new Bundle();
            args.putInt("scheduleId", selectedSchedule.getId());
            fragment.setArguments(args);


            fragment.setTargetFragment(this, 1); // ScheduleFragment will be target
            fragment.show(getParentFragmentManager(), "edit_schedule");
        });
        return view;
    }


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onResume() {
        super.onResume();
        Log.d("ScheduleFragment", "onResume: Registering receiver for schedule refresh");

        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("ScheduleFragment", "onReceive: Received refresh broadcast");

                loadSchedulesFromDatabase();
            }
        };

        IntentFilter filter = new IntentFilter("com.example.callepeninsulares.REFRESH_SCHEDULES");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(refreshReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(refreshReceiver, filter);
        }

        loadSchedulesFromDatabase();
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d("ScheduleFragment", "onPause: Unregistering receiver");

        if (refreshReceiver != null) {
            requireContext().unregisterReceiver(refreshReceiver);
            refreshReceiver = null;
        }
    }




    public void loadSchedulesFromDatabase() {
        Log.d("ScheduleFragment", "loadSchedulesFromDatabase: Loading schedules from database");
        schedules.clear();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String currentUserEmail = sharedPreferences.getString("email", "");


        Cursor cursor = dbHelper.getAllSchedules(currentUserEmail);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Log.d("ScheduleFragment", "Schedule ID: " + cursor.getInt(0));
                Schedule sched = new Schedule(
                        cursor.getInt(0),
                        cursor.getString(2), // Building
                        cursor.getString(3), // Subject
                        cursor.getString(4), // Date
                        cursor.getString(5), // StartTime
                        cursor.getString(6), // EndTime
                        cursor.getString(7), // Room
                        cursor.getInt(8),    // MinutesBefore
                        cursor.getInt(9) == 1, // IsOnlineClass
                        cursor.getString(10)  // Meetlink
                );
                schedules.add(sched);
            } while (cursor.moveToNext());
            cursor.close();
        }
        Log.d("ScheduleFragment", "Schedules loaded: " + schedules.size() + " items");
        // Sort schedules by date & time
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy hh:mm a", Locale.getDefault());
        schedules.sort((s1, s2) -> {
            try {
                Date d1 = sdf.parse(s1.getDate() + " " + s1.getStartTime());
                Date d2 = sdf.parse(s2.getDate() + " " + s2.getStartTime());
                return d1.compareTo(d2);
            } catch (Exception e) {
                return 0;
            }
        });


        ScheduleAdapter adapter = new ScheduleAdapter(requireContext(), schedules);
        listView.setAdapter(adapter);


        adapter.notifyDataSetChanged();
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
