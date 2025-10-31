package com.example.callepeninsulares;

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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

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
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view1, year1, monthOfYear, dayOfMonth) -> {
                        // monthOfYear starts at 0, so add 1
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                        editDate.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        editStartTime.setOnClickListener(v -> showTimePicker(editStartTime));

        editEndTime.setOnClickListener(v -> showTimePicker(editEndTime));

        btnAddSchedule.setOnClickListener(v -> {
            String dateStr = editDate.getText().toString();
            String timeStr = editStartTime.getText().toString();
            String subject = Subject.getText().toString();

            if (dateStr.isEmpty() || timeStr.isEmpty()) {
                Toast.makeText(getContext(), "Please select date and time!", Toast.LENGTH_SHORT).show();

                return;
            }

            Calendar calendar = Calendar.getInstance();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy hh:mm a", Locale.getDefault());
                Date date = sdf.parse(dateStr + " " + timeStr);
                calendar.setTime(date);
            } catch (ParseException e) {
                Toast.makeText(getContext(), "Invalid date/time format!", Toast.LENGTH_SHORT).show();
                return;
            }

            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

            try {
                //  EARLY REMINDER - if user selected something
                if (selectedMinutesBefore > 0) {
                    Calendar earlyReminder = (Calendar) calendar.clone();
                    earlyReminder.add(Calendar.MINUTE, -selectedMinutesBefore);

                    Intent earlyIntent = new Intent(getContext(), AlarmReceiver.class);
                    earlyIntent.putExtra("subject", subject + " (Starting in " + selectedMinutesBefore + " min");
                    earlyIntent.putExtra("building", "Medina Lacson Building");

                    PendingIntent earlyPendingIntent = PendingIntent.getBroadcast(getContext(), (int) System.currentTimeMillis(), earlyIntent, PendingIntent.FLAG_IMMUTABLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, earlyReminder.getTimeInMillis(), earlyPendingIntent);
                        } else {
                            Toast.makeText(getContext(), "Please allow exact alarms in Settings!", Toast.LENGTH_LONG).show();
                            return;
                        }
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, earlyReminder.getTimeInMillis(), earlyPendingIntent);
                    }
                }

                //  CLASS TIME REMINDER
                Intent exactIntent = new Intent(getContext(), AlarmReceiver.class);
                exactIntent.putExtra("subject", subject + " Class time now!");
                exactIntent.putExtra("building", "Medina Lacson Building");

                PendingIntent exactPendingIntent = PendingIntent.getBroadcast(
                        getContext(),
                        (int) (System.currentTimeMillis() + 1), exactIntent, PendingIntent.FLAG_IMMUTABLE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), exactPendingIntent);
                    }
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), exactPendingIntent);
                }

                if (selectedMinutesBefore > 0) {
                    Toast.makeText(getContext(), "Reminders set: " + selectedMinutesBefore + " min early", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Reminder set for class time only!", Toast.LENGTH_SHORT).show();
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
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute1) -> {
                    String amPm = (hourOfDay >= 12) ? "PM" : "AM";
                    int displayHour = (hourOfDay % 12 == 0) ? 12 : hourOfDay % 12;
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute1, amPm);
                    targetField.setText(formattedTime);
                },
                hour, minute, false
        );
        timePickerDialog.show();
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