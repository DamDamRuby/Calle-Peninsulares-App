package com.example.callepeninsulares;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String databaseName = "Signup.db";

    public DatabaseHelper(@Nullable Context context) {
        super(context, databaseName, null, 5);
    }

    @Override
    public void onCreate(SQLiteDatabase MyDatabase) {

        MyDatabase.execSQL("CREATE TABLE allusers(email TEXT PRIMARY KEY, password TEXT)");


        MyDatabase.execSQL("CREATE TABLE schedules(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userEmail TEXT, " +
                "building TEXT, " +
                "subject TEXT, " +
                "date TEXT, " +
                "startTime TEXT, " +
                "endTime TEXT, " +
                "room TEXT, " +
                "minutesBefore INTEGER, " +
                "isOnlineClass INTEGER DEFAULT 0, " +
                "meetLink TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase MyDatabase, int oldVersion, int newVersion) {
        MyDatabase.execSQL("DROP TABLE IF EXISTS allusers");
        MyDatabase.execSQL("DROP TABLE IF EXISTS schedules");
        onCreate(MyDatabase);
    }

    public boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        return email.matches(emailPattern);
    }

    public boolean insertData(String email, String password) {

        if (!isValidEmail(email)) {
            return false;
        }
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("password", password);
        long result = MyDatabase.insert("allusers", null, contentValues);
        return result != -1;
    }


    public Boolean checkEmail(String email) {
        // Validate email format
        if (!isValidEmail(email)) {
            return false;
        }
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT * FROM allusers WHERE email = ?", new String[]{email});
        return cursor.getCount() > 0;
    }

    public Boolean checkEmailPassword(String email, String password) {

        if (!isValidEmail(email)) {
            return false;
        }
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT * FROM allusers WHERE email = ? AND password = ?", new String[]{email, password});
        return cursor.getCount() > 0;
    }


    public long insertSchedule(String userEmail, String subject, String building, String date, String startTime, String endTime, String room, int minutesBefore,  boolean isOnlineClass, String meetLink) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("userEmail", userEmail);
        cv.put("subject", subject);
        cv.put("building", building);
        cv.put("date", date);
        cv.put("startTime", startTime);
        cv.put("endTime", endTime);
        cv.put("room", room);
        cv.put("minutesBefore", minutesBefore);
        cv.put("isOnlineClass", isOnlineClass ? 1 : 0);
        cv.put("meetLink", meetLink);

        android.util.Log.d("DB_INSERT", "Inserting new schedule with values: " + cv.toString());

        long result = db.insert("schedules", null, cv);
        android.util.Log.d("DB_INSERT", "Insert result ID: " + result);
        return result;
    }


    public Cursor getAllSchedules(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM schedules WHERE userEmail = ?", new String[]{userEmail});
    }

    public boolean updateSchedule(int id, String building, String subject, String date, String startTime, String endTime, String room, int minutesBefore, boolean isOnlineClass, String meetLink) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("building", building);
        cv.put("subject", subject);
        cv.put("date", date);
        cv.put("startTime", startTime);
        cv.put("endTime", endTime);
        cv.put("room", room);
        cv.put("minutesBefore", minutesBefore);
        cv.put("isOnlineClass", isOnlineClass ? 1 : 0);
        cv.put("meetLink", meetLink);

        android.util.Log.d("DB_UPDATE", "Attempting to update ID: " + id);
        android.util.Log.d("DB_UPDATE", "Values: " + cv.toString());
        Log.d("DatabaseHelper", "Updating schedule with ID: " + id);
        Log.d("DatabaseHelper", "Updating with data: " + cv.toString());

        int result = db.update("schedules", cv, "id=?", new String[]{String.valueOf(id)});

        Log.d("DatabaseHelper", "Update result: " + result);
        return result > 0;
    }
    public Cursor getScheduleById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM schedules WHERE id = ?", new String[]{String.valueOf(id)});
    }
    public boolean deleteSchedule(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("schedules", "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public boolean updateUserEmail(String oldEmail, String newEmail) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if new email already exists
        Cursor cursor = db.rawQuery("SELECT email FROM allusers WHERE email = ?", new String[]{newEmail});
        boolean exists = cursor.getCount() > 0;
        cursor.close();

        if (exists) {
            // Email already taken
            return false;
        }

        ContentValues cv = new ContentValues();
        cv.put("email", newEmail);
        int result = db.update("allusers", cv, "email=?", new String[]{oldEmail});

        // Update schedules too (so new email is consistent)
        if (result > 0) {
            ContentValues scheduleValues = new ContentValues();
            scheduleValues.put("userEmail", newEmail);
            db.update("schedules", scheduleValues, "userEmail=?", new String[]{oldEmail});
        }

        return result > 0;
    }


    public boolean updateUserPassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password", newPassword);
        int result = db.update("allusers", cv, "email=?", new String[]{email});
        return result > 0;
    }
}