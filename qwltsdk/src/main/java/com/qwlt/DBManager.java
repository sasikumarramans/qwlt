package com.qwlt;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

public class DBManager {
    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    public DBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String name, String desc) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.STEPS, name);
        contentValue.put(DatabaseHelper.DATE, desc);
        database.insert(DatabaseHelper.TABLE_NAME, null, contentValue);
    }

    public String last5Records() {
        Cursor cursor = database.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME +" ORDER BY _id DESC LIMIT 5",
                null);
        JSONArray returnObj = new JSONArray();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                try {
                    do {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("steps", cursor.getString(1));
                        jsonObject.put("date", cursor.getString(2));
                        returnObj.put(jsonObject);

                    } while (cursor.moveToNext());
                } catch (Exception e) {
                    e.getMessage();
                }

            }
        }
        return returnObj.toString();

    }

    public int first() {
        Cursor cursor=database.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME +" ORDER BY _id DESC LIMIT 1",
                null);
        int steps = 0;
        if (cursor != null) {

            if (cursor.moveToFirst()) {

                steps = Integer.parseInt(cursor.getString(1)); // DeviceID

            }
        }
        return steps;

    }

    public StepCount latestRecord() {
        Cursor cursor = database.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME +" ORDER BY _id DESC LIMIT 1",
                null);
        StepCount steps = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                steps = new StepCount();
                steps.setDate(cursor.getString(2));
                steps.setStepCount(cursor.getString(1));

            }
        }
        return steps;

    }

    public Cursor fetch() {
        String[] columns = new String[]{DatabaseHelper._ID, DatabaseHelper.STEPS, DatabaseHelper.DATE};
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(long _id, String name, String desc) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.STEPS, name);
        contentValues.put(DatabaseHelper.DATE, desc);
        int i = database.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper._ID + " = " + _id, null);
        return i;
    }
    public void updateWhere(String STEPS, String DATE) {
        Object[] bindArgs={STEPS,DATE};
        database.execSQL("UPDATE "+DatabaseHelper.TABLE_NAME+" SET steps = ? WHERE date = ?",bindArgs);
    }

    public void delete(long _id) {
        database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper._ID + "=" + _id, null);
    }
}
