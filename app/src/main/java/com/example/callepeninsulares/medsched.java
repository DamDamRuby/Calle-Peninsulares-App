package com.example.callepeninsulares;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class medsched extends DialogFragment {

    int selectedMinutesBefore = 0;

    @SuppressLint("ScheduleExactAlarm")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.medinasched, null);

        EditText Subject = view.findViewById(R.id.Subject);
        EditText editDate = view.findViewById(R.id.editTextDate);
        EditText editStartTime = view.findViewById(R.id.editTextStartTime);
        EditText editEndTime = view.findViewById(R.id.editTextEndTime);
        Button btnAddSchedule = view.findViewById(R.id.btnAddSchedule);
        Spinner spinnerMinutes = view.findViewById(R.id.SpinnerMinuteBefore);
        Spinner Room = view.findViewById(R.id.Room);
        EditText editMeetLink = view.findViewById(R.id.editTextMeetLink);

        Room.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRoom = parent.getItemAtPosition(position).toString();
                if (selectedRoom.equals("Online Class")) {
                    editMeetLink.setVisibility(View.VISIBLE);
                } else {
                    editMeetLink.setVisibility(View.GONE);
                    editMeetLink.setText(""); // clear previous link
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                editMeetLink.setVisibility(View.GONE);
            }
        });

        spinnerMinutes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { //Spinner for how many minutes before the exact time notif pop up
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
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        editDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Class Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selection);
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy");
                editDate.setText(sdf.format(calendar.getTime()));
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        editStartTime.setOnClickListener(v -> showTimePicker(editStartTime));

        editEndTime.setOnClickListener(v -> showTimePicker(editEndTime));



        btnAddSchedule.setOnClickListener(v -> {
            String dateStr = editDate.getText().toString();
            String startTimeStr = editStartTime.getText().toString();
            String endTimeStr = editEndTime.getText().toString();
            String subject = Subject.getText().toString();
            String room = Room.getSelectedItem().toString();

            if (dateStr.isEmpty() || startTimeStr.isEmpty()) {
                Toast.makeText(getContext(), "Please select date and time!", Toast.LENGTH_SHORT).show();

                return;
            }

            Calendar calendar = Calendar.getInstance();
            try {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy hh:mm a", Locale.getDefault());
                Date date = sdf.parse(dateStr + " " + startTimeStr);
                if (date != null) calendar.setTime(date);
            } catch (ParseException e) {
                Toast.makeText(getContext(), "Invalid date/time format!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isOnlineClass = room.equals("Online Class");
            String meetLink = editMeetLink.getText().toString();

            DatabaseHelper dbHelper = new DatabaseHelper(getContext());
            long scheduleId = dbHelper.insertSchedule(
                    subject,
                    "Medina Lacson Building",
                    dateStr,
                    startTimeStr,
                    endTimeStr,
                    room,
                    selectedMinutesBefore,
                    isOnlineClass,
                    meetLink
            );

            if (scheduleId == -1) {
                Toast.makeText(getContext(), "Failed to save schedule!", Toast.LENGTH_SHORT).show();
                return;
            } else {
                Toast.makeText(getContext(), "Successfully saved!", Toast.LENGTH_SHORT).show();
            }

            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

            try {
                //  EARLY REMINDER - if user selected something
                if (selectedMinutesBefore > 0) {
                    Calendar earlyReminder = (Calendar) calendar.clone();
                    earlyReminder.add(Calendar.MINUTE, -selectedMinutesBefore);

                    Intent earlyIntent = new Intent(getContext(), AlarmReceiver.class);
                    earlyIntent.putExtra("subject", subject);
                    earlyIntent.putExtra("room", room);
                    earlyIntent.putExtra("startTime", startTimeStr);
                    earlyIntent.putExtra("meetLink", meetLink);
                    earlyIntent.putExtra("isOnlineClass", isOnlineClass);

                    PendingIntent earlyPendingIntent = PendingIntent.getBroadcast(getContext(), (int) System.currentTimeMillis(), earlyIntent, PendingIntent.FLAG_IMMUTABLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, earlyReminder.getTimeInMillis(), earlyPendingIntent);
                        } else {
                            Toast.makeText(getContext(), "Please allow 'exact alarms' in Settings!", Toast.LENGTH_LONG).show();
                            return;
                        }
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, earlyReminder.getTimeInMillis(), earlyPendingIntent);
                    }
                }

                //  CLASS TIME REMINDER
                Intent exactIntent = new Intent(getContext(), AlarmReceiver.class);
                exactIntent.putExtra("subject", subject);
                exactIntent.putExtra("room", room);
                exactIntent.putExtra("startTime", startTimeStr);
                exactIntent.putExtra("meetLink", meetLink);
                exactIntent.putExtra("isOnlineClass", isOnlineClass);
                exactIntent.putExtra("scheduleId", (int) scheduleId);

                PendingIntent exactPendingIntent = PendingIntent.getBroadcast(
                        getContext(),
                        (int) scheduleId,
                        exactIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(),
                                exactPendingIntent
                        );
                    } else {
                        // ⚠️ Fallback if exact alarms not allowed
                        alarmManager.set(
                                AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(),
                                exactPendingIntent
                        );

                        Toast.makeText(
                                getContext(),
                                "Exact alarms permission not granted — using inexact alarm instead.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            exactPendingIntent
                    );
                }
                dismiss();

            } catch (SecurityException e) {
                Toast.makeText(getContext(), "Permission denied to schedule exact alarms.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });

        return view;
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

        timePicker.show(getParentFragmentManager(), "TIME_PICKER");
    }



    @Override
    public void onResume() {
        super.onResume();

        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();


            window.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setGravity(android.view.Gravity.CENTER);
        }
    }
}