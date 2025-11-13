package com.example.callepeninsulares;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditScheduleActivity extends DialogFragment {

    DatabaseHelper dbHelper;
    int scheduleId;
    int selectedMinutesBefore = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view = inflater.inflate(R.layout.activity_edit_schedule, container, false);

        scheduleId = getArguments().getInt("scheduleId", -1);
        Log.d("EditScheduleActivity", "Schedule ID: " + scheduleId);


        EditText editMeetLink = view.findViewById(R.id.editTextMeetLink);
        EditText subject = view.findViewById(R.id.editSubject);
        EditText date = view.findViewById(R.id.editDate);
        EditText startTime = view.findViewById(R.id.editStartTime);
        EditText endTime = view.findViewById(R.id.editEndTime);
        EditText building = view.findViewById(R.id.editBuilding);

        Button btnUpdate = view.findViewById(R.id.btnUpdate);
        Button btnDelete = view.findViewById(R.id.btnDelete);


        Spinner roomSpinner = view.findViewById(R.id.Room);
        String[] roomOptions = getResources().getStringArray(R.array.roomed_options);
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, roomOptions);
        roomSpinner.setAdapter(roomAdapter);


        Spinner minuteBeforeSpinner = view.findViewById(R.id.spinnerMinutesBefore);
        String[] minuteOptions = getResources().getStringArray(R.array.reminder_options);
        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, minuteOptions);
        minuteBeforeSpinner.setAdapter(minuteAdapter);


        SharedPreferences settingsPrefs = requireContext().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
        boolean highContrast = settingsPrefs.getBoolean("highContrast", false);
        float textSize = settingsPrefs.getFloat("textSize", 16f);

        if (highContrast) {
            view.setBackgroundColor(Color.BLACK);
            applyTextColorToAllTextViews(view, Color.WHITE);
        } else {
            view.setBackgroundResource(R.drawable.modern_gradient_bg);
            applyTextColorToAllTextViews(view, Color.BLACK);
        }

        applyTextSizeToAllViews(view, textSize);

        // Handle room selection (Online class vs in-person)
        roomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRoom = parent.getItemAtPosition(position).toString();
                if (selectedRoom.equals("Online Class")) {
                    editMeetLink.setVisibility(View.VISIBLE);
                } else {
                    editMeetLink.setVisibility(View.GONE);
                    editMeetLink.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                editMeetLink.setVisibility(View.GONE);
            }
        });


        minuteBeforeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();

                Log.d("medsched", "Selected option: " + selectedOption);

                switch (selectedOption) {
                    case "5 minutes before":
                        selectedMinutesBefore = 5;
                        break;
                    case "10 minutes before":
                        selectedMinutesBefore = 10;
                        break;
                    case "15 minutes before":
                        selectedMinutesBefore = 15;
                        break;
                    default:
                        selectedMinutesBefore = 0;
                        break;
                }
                Log.d("medsched", "Selected minutes before: " + selectedMinutesBefore);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedMinutesBefore = 0;
            }
        });

        // Retrieve scheduleId from fragment arguments
        dbHelper = new DatabaseHelper(getContext());
        scheduleId = getArguments().getInt("scheduleId", -1);

        if (scheduleId == -1) {
            Toast.makeText(getContext(), "Invalid schedule!", Toast.LENGTH_SHORT).show();
            dismiss();
            return view;
        }


        Cursor cursor = dbHelper.getScheduleById(scheduleId);
        if (cursor != null && cursor.moveToFirst()) {
            building.setText(cursor.getString(2));   // building

            subject.setText(cursor.getString(3));    // subject
            date.setText(cursor.getString(4));       // date
            startTime.setText(cursor.getString(5));  // startTime
            endTime.setText(cursor.getString(6));    // endTime

            String buildingValue = cursor.getString(2);   // Building

            // Dynamically update roomSpinner based on building

            if (building.getText().toString().equals("BA Comm Building")) {
                roomOptions = getResources().getStringArray(R.array.bacomm_options);
            } else if (building.getText().toString().equals("Medina Lacson Building")) {
                roomOptions = getResources().getStringArray(R.array.roomed_options);
            } else if (building.getText().toString().equals("Automotive Technology")) {
                roomOptions = getResources().getStringArray(R.array.auto_options);
            } else {
                roomOptions = new String[]{"No room options available"};
            }

            // Update the existing adapter instead of redeclaring
            roomAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, roomOptions);
            roomSpinner.setAdapter(roomAdapter);

            String roomValue = cursor.getString(7);
            for (int i = 0; i < roomSpinner.getCount(); i++) {
                if (roomSpinner.getItemAtPosition(i).toString().equals(roomValue)) {
                    roomSpinner.setSelection(i);
                    break;
                }
            }

            selectedMinutesBefore = cursor.getInt(8);
            boolean isOnlineClass = cursor.getInt(9) == 1;
            String meetLink = cursor.getString(10);

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
            minuteBeforeSpinner.setSelection(spinnerPos);
        }

        if (cursor != null)
            cursor.close();
        else {
            // Log if no schedule with the given ID was found
            Log.d("DB_UPDATE", "Schedule with ID: " + scheduleId + " not found.");
        }

        minuteBeforeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
            public void onNothingSelected(AdapterView<?> parent) {
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
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy");
                date.setText(sdf.format(calendar.getTime()));
            });

            datePicker.show(getChildFragmentManager(), "DATE_PICKER");
        });

        // Time picker dialogs
        startTime.setOnClickListener(v -> showTimePicker(startTime));
        endTime.setOnClickListener(v -> showTimePicker(endTime));

        // Update schedule
        btnUpdate.setOnClickListener(v -> {
            boolean updated = dbHelper.updateSchedule(
                    scheduleId,
                    building.getText().toString(),
                    subject.getText().toString(),
                    date.getText().toString(),
                    startTime.getText().toString(),
                    endTime.getText().toString(),
                    roomSpinner.getSelectedItem().toString(),
                    selectedMinutesBefore,
                    roomSpinner.getSelectedItem().toString().equals("Online Class"),
                    editMeetLink.getText().toString()
            );

            if (updated) {
                cancelExistingAlarm(scheduleId);

                scheduleAlarm(
                        scheduleId,
                        subject.getText().toString(),
                        roomSpinner.getSelectedItem().toString(),
                        date.getText().toString(),
                        startTime.getText().toString(),
                        selectedMinutesBefore,
                        editMeetLink.getText().toString()
                );

                Toast.makeText(getContext(), "Updated and alarm rescheduled!", Toast.LENGTH_SHORT).show();
                ScheduleFragment scheduleFragment = (ScheduleFragment) getParentFragmentManager()
                        .findFragmentByTag("schedule_fragment");
                if (scheduleFragment != null) {
                    scheduleFragment.loadSchedulesFromDatabase();
                }

                // Still send broadcast (as backup)
                Intent refreshIntent = new Intent("com.example.callepeninsulares.REFRESH_SCHEDULES");
                getContext().sendBroadcast(refreshIntent);

                dismiss();
            } else {
                Toast.makeText(getContext(), "Update failed!", Toast.LENGTH_SHORT).show();
                Log.d("EditScheduleActivity", "Schedule update failed.");
            }
        });


        // Delete schedule
        btnDelete.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Delete Schedule")
                    .setMessage("Are you sure you want to delete this schedule?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        boolean deleted = dbHelper.deleteSchedule(scheduleId);
                        if (deleted) {
                            Toast.makeText(getContext(), "Deleted successfully!", Toast.LENGTH_SHORT).show();
                            // Send a broadcast to notify the ScheduleFragment to refresh
                            Intent refreshIntent = new Intent("com.example.callepeninsulares.REFRESH_SCHEDULES");
                            getContext().sendBroadcast(refreshIntent);
                            dismiss(); // Close the dialog on success
                        } else {
                            Toast.makeText(getContext(), "Delete failed!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
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

        timePicker.show(getChildFragmentManager(), "TIME_PICKER");
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
    private void cancelExistingAlarm(int scheduleId) {
        Context context = getContext(); // or requireContext() if this is a Fragment
        if (context == null) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                scheduleId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d("AlarmUpdate", "Cancelled existing alarm for schedule ID " + scheduleId);
        }
    }

    private void scheduleAlarm(int scheduleId, String subject, String room, String date, String startTime, int minutesBefore, String meetLink) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy hh:mm a", Locale.getDefault());
            Date classDateTime = sdf.parse(date + " " + startTime);
            if (classDateTime == null) return;

            long triggerTime = classDateTime.getTime() - (minutesBefore * 60 * 1000);

            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(requireContext(), AlarmReceiver.class);
            intent.putExtra("scheduleId", scheduleId);
            intent.putExtra("subject", subject);
            intent.putExtra("room", room);
            intent.putExtra("startTime", startTime);
            intent.putExtra("meetLink", meetLink);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    requireContext(),
                    scheduleId, // unique per schedule
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
            }

            Log.d("AlarmUpdate", "Rescheduled alarm for schedule ID " + scheduleId + " at " + triggerTime);
        } catch (Exception e) {
            Log.e("AlarmUpdate", "Error scheduling alarm: " + e.getMessage());
        }
    }
}

