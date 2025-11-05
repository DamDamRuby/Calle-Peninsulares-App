package com.example.callepeninsulares;

import static java.security.AccessController.getContext;

import android.os.Build;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditScheduleActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    int scheduleId;
    int selectedMinutesBefore = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_schedule);


        EditText editMeetLink = findViewById(R.id.editTextMeetLink);
        EditText subject = findViewById(R.id.editSubject);
        EditText date = findViewById(R.id.editDate);
        EditText startTime = findViewById(R.id.editStartTime);
        EditText endTime = findViewById(R.id.editEndTime);
        EditText building = findViewById(R.id.editBuilding);
        Spinner room = findViewById(R.id.Room);
        Spinner spinnerMinutes = findViewById(R.id.spinnerMinutesBefore);
        Button btnUpdate = findViewById(R.id.btnUpdate);
        Button btnDelete = findViewById(R.id.btnDelete);

        room.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRoom = parent.getItemAtPosition(position).toString();
                if (selectedRoom.equals("Online Class")) {
                    editMeetLink.setVisibility(View.VISIBLE);
                    building.setVisibility(View.GONE);
                } else {
                    editMeetLink.setVisibility(View.GONE);
                    editMeetLink.setText(""); // clear previous link
                    building.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                editMeetLink.setVisibility(View.GONE);
            }
        });


        dbHelper = new DatabaseHelper(this);
        scheduleId = getIntent().getIntExtra("scheduleId", -1);

        if (scheduleId == -1) {
            Toast.makeText(this, "Invalid schedule!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Load existing schedule
        Cursor cursor = dbHelper.getScheduleById(scheduleId);
        if (cursor != null && cursor.moveToFirst()) {
            building.setText(cursor.getString(1));   // building
            subject.setText(cursor.getString(2));    // subject
            date.setText(cursor.getString(3));       // date
            startTime.setText(cursor.getString(4));  // startTime
            endTime.setText(cursor.getString(5));    // endTime

            // Set room spinner
            String roomValue = cursor.getString(6);
            for (int i = 0; i < room.getCount(); i++) {
                if (room.getItemAtPosition(i).toString().equals(roomValue)) {
                    room.setSelection(i);
                    break;
                }
            }

            selectedMinutesBefore = cursor.getInt(7);
            boolean isOnlineClass = cursor.getInt(8) == 1;
            String meetLink = cursor.getString(9);

            editMeetLink.setText(meetLink);
            editMeetLink.setVisibility(isOnlineClass ? View.VISIBLE : View.GONE);

            int spinnerPos;
            switch (selectedMinutesBefore) {
                case 5:
                    spinnerPos = 1;
                    break;
                case 10:
                    spinnerPos = 2;
                    break;
                case 15:
                    spinnerPos = 3;
                    break;
                default:
                    spinnerPos = 0;
                    break;
            }
            ;
            spinnerMinutes.setSelection(spinnerPos);
        }
        if (cursor != null) cursor.close();

        spinnerMinutes.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                switch (position) {
                    case 0:
                        selectedMinutesBefore = 0;
                        break;
                    case 1:
                        selectedMinutesBefore = 5;
                        break;
                    case 2:
                        selectedMinutesBefore = 10;
                        break;
                    case 3:
                        selectedMinutesBefore = 15;
                        break;
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        date.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Class Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selection);
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy");
                date.setText(sdf.format(calendar.getTime()));
            });

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        startTime.setOnClickListener(v -> showTimePicker(startTime));
        endTime.setOnClickListener(v -> showTimePicker(endTime));


        btnUpdate.setOnClickListener(v -> {
            Log.d("EditScheduleActivity", "Updating ID: " + scheduleId);
            boolean updated = dbHelper.updateSchedule(
                    scheduleId,
                    building.getText().toString(),
                    subject.getText().toString(),
                    date.getText().toString(),
                    startTime.getText().toString(),
                    endTime.getText().toString(),
                    room.getSelectedItem().toString(),
                    selectedMinutesBefore,
                    room.getSelectedItem().toString().equals("Online Class"),
                    editMeetLink.getText().toString()
            );


            if (updated) {
                Toast.makeText(this, "Updated successfully!", Toast.LENGTH_SHORT).show();
                sendBroadcast(new Intent("com.example.callepeninsulares.REFRESH_SCHEDULES"));

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(this, AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        scheduleId, // use scheduleId as unique request code
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                if (alarmManager != null) {
                    alarmManager.cancel(pendingIntent);
                }

                Calendar calendar = Calendar.getInstance();
                try {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy hh:mm a", Locale.getDefault());
                    Date dateObj = sdf.parse(date.getText().toString() + " " + startTime.getText().toString());
                    if (dateObj != null) {
                        calendar.setTime(dateObj);

                        Intent newIntent = new Intent(this, AlarmReceiver.class);
                        newIntent.putExtra("subject", subject.getText().toString());
                        newIntent.putExtra("room", room.getSelectedItem().toString());
                        newIntent.putExtra("startTime", startTime.getText().toString());
                        newIntent.putExtra("meetLink", editMeetLink.getText().toString());
                        newIntent.putExtra("isOnlineClass", room.getSelectedItem().toString().equals("Online Class"));
                        newIntent.putExtra("scheduleId", scheduleId);

                        PendingIntent newPendingIntent = PendingIntent.getBroadcast(
                                this,
                                scheduleId,
                                newIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        );

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (alarmManager.canScheduleExactAlarms()) {
                                alarmManager.setExactAndAllowWhileIdle(
                                        AlarmManager.RTC_WAKEUP,
                                        calendar.getTimeInMillis(),
                                        newPendingIntent
                                );
                            }
                        } else {
                            alarmManager.setExact(
                                    AlarmManager.RTC_WAKEUP,
                                    calendar.getTimeInMillis(),
                                    newPendingIntent
                            );
                        }

                        // 🕒 Optional: Reschedule early reminder if needed
                        if (selectedMinutesBefore > 0) {
                            Calendar earlyReminder = (Calendar) calendar.clone();
                            earlyReminder.add(Calendar.MINUTE, -selectedMinutesBefore);

                            Intent earlyIntent = new Intent(this, AlarmReceiver.class);
                            earlyIntent.putExtra("subject", subject.getText().toString());
                            earlyIntent.putExtra("room", room.getSelectedItem().toString());
                            earlyIntent.putExtra("startTime", startTime.getText().toString());
                            earlyIntent.putExtra("meetLink", editMeetLink.getText().toString());
                            earlyIntent.putExtra("isOnlineClass", room.getSelectedItem().toString().equals("Online Class"));

                            PendingIntent earlyPendingIntent = PendingIntent.getBroadcast(
                                    this,
                                    scheduleId + 10000, // unique request code for early alarm
                                    earlyIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                            );

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (alarmManager.canScheduleExactAlarms()) {
                                    alarmManager.setExactAndAllowWhileIdle(
                                            AlarmManager.RTC_WAKEUP,
                                            earlyReminder.getTimeInMillis(),
                                            earlyPendingIntent
                                    );
                                }
                            } else {
                                alarmManager.setExact(
                                        AlarmManager.RTC_WAKEUP,
                                        earlyReminder.getTimeInMillis(),
                                        earlyPendingIntent
                                );
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT).show();
            }

            finish();
        });
        btnDelete.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Schedule")
                    .setMessage("Are you sure you want to delete this schedule?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        boolean deleted = dbHelper.deleteSchedule(scheduleId);
                        if (deleted) {
                            Toast.makeText(this, "Deleted successfully!", Toast.LENGTH_SHORT).show();

                            // Cancel any existing alarms for this schedule
                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            Intent intent = new Intent(this, AlarmReceiver.class);

                            // Cancel main alarm
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                    this,
                                    scheduleId,
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                            );
                            if (alarmManager != null) alarmManager.cancel(pendingIntent);

                            // Cancel early reminder (if any)
                            PendingIntent earlyPendingIntent = PendingIntent.getBroadcast(
                                    this,
                                    scheduleId + 10000,
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                            );
                            if (alarmManager != null) alarmManager.cancel(earlyPendingIntent);

                            // Notify list and close
                            sendBroadcast(new Intent("com.example.callepeninsulares.REFRESH_SCHEDULES"));
                            finish();
                        } else {
                            Toast.makeText(this, "Delete failed!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }
    private void showTimePicker(EditText targetField) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(8)
                .setMinute(0)
                .setTitleText("Select Time")
                .build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String amPm = (hour >= 12) ? "PM" : "AM";
            int displayHour = (hour % 12 == 0) ? 12 : hour % 12;
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm);
            targetField.setText(formattedTime);
        });

        timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
    }

}

