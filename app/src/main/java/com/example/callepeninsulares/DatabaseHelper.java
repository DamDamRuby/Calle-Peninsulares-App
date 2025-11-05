package com.example.callepeninsulares;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String databaseName = "Signup.db";

    public DatabaseHelper(@Nullable Context context) {
        super(context, databaseName, null, 3); // ⬅️ bumped to version 3
    }

    @Override
    public void onCreate(SQLiteDatabase MyDatabase) {
        // USER TABLE
        MyDatabase.execSQL("CREATE TABLE allusers(email TEXT PRIMARY KEY, password TEXT)");

        // SCHEDULE TABLE with OnlineClass and MeetLink
        MyDatabase.execSQL("CREATE TABLE schedules(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
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

    // ✅ Sign-up
    public boolean insertData(String email, String password) {
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("password", password);
        long result = MyDatabase.insert("allusers", null, contentValues);
        return result != -1;
    }

    // ✅ Login check
    public Boolean checkEmail(String email) {
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT * FROM allusers WHERE email = ?", new String[]{email});
        return cursor.getCount() > 0;
    }

    public Boolean checkEmailPassword(String email, String password) {
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT * FROM allusers WHERE email = ? AND password = ?", new String[]{email, password});
        return cursor.getCount() > 0;
    }

    // ✅ Insert new schedule (now includes minutesBefore)
    public long insertSchedule(String subject, String building, String date, String startTime, String endTime, String room, int minutesBefore,  boolean isOnlineClass, String meetLink) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
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

    // Other schedule-related methods stay the same
    public Cursor getAllSchedules() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM schedules", null);
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

        int result = db.update("schedules", cv, "id=?", new String[]{String.valueOf(id)});

        android.util.Log.d("DB_UPDATE", "Update result: " + result);
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
}