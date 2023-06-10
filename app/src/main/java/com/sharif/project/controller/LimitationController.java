package com.sharif.project.controller;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.sharif.project.model.Box;
import com.sharif.project.model.DBHelper;
import com.sharif.project.model.Limitation;
import com.sharif.project.model.Plan;
import com.sharif.project.model.Topic;
import com.sharif.project.util.PersianDateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class LimitationController {

    public static void addLimitation(Context context, Limitation limitation) {
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.LIMITATION_NAME, limitation.name);
        contentValues.put(DBHelper.LIMITATION_OFFSET, limitation.offset);
        contentValues.put(DBHelper.LIMITATION_PERIOD, limitation.period);
        contentValues.put(DBHelper.LIMITATION_LENGTH, limitation.length);
        db.insert(DBHelper.LIMITATION_TABLE_NAME, null, contentValues);
        db.close();
    }

    public static void updateLimitation(Context context, Limitation limitation, String key) {
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.LIMITATION_NAME, limitation.name);
        contentValues.put(DBHelper.LIMITATION_OFFSET, limitation.offset);
        contentValues.put(DBHelper.LIMITATION_PERIOD, limitation.period);
        contentValues.put(DBHelper.LIMITATION_LENGTH, limitation.length);
        db.update(DBHelper.LIMITATION_TABLE_NAME, contentValues,
                DBHelper.LIMITATION_ID + "=?", new String[]{key});
        db.close();
    }

    public static void deleteLimitation(Context context, String key) {
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        db.delete(DBHelper.LIMITATION_TABLE_NAME,
                DBHelper.LIMITATION_ID + "=?", new String[]{key});
        db.close();
    }

    public static String getLimitationTimeError(Context context, Limitation limitation, Date date) {
        int startMin = PersianDateUtil.getMinutesPassedFromStartOfDay(new Date(limitation.offset));
        int endMin = PersianDateUtil.getMinutesPassedFromStartOfDay(new Date(limitation.offset + limitation.length));
        if (endMin > 0 && startMin > endMin)
            return "زمان پایان بسته نباید در روز بعد باشد.";

        for (Limitation otherLimitation: new ArrayList<>(getAllLimitations(context).values())) {
            if (limitation.id == null || !limitation.id.equals(otherLimitation.id)) {
                long startTime1 = limitation.getStartTimeFromDate(date).getTime();
                long startTime2 = otherLimitation.getStartTimeFromDate(date).getTime();
                long endTime1 = startTime1 + limitation.length;
                long endTime2 = startTime2 + otherLimitation.length;
                if (endTime1 > startTime2 && endTime2 > startTime1)
                    return "این زمان با زمان یک محدودیت دیگر در " + PersianDateUtil.getDateString(new Date(startTime2)) + " تلاقی دارد.";
                startTime1 = limitation.getStartTimeFromDate(PersianDateUtil.truncateToStartOfDay(new Date(startTime2))).getTime();
                endTime1 = startTime1 + limitation.length;
                if (endTime1 > startTime2 && endTime2 > startTime1)
                    return "این زمان با زمان یک محدودیت دیگر در " + PersianDateUtil.getDateString(new Date(startTime2)) + " تلاقی دارد.";
            }
        }

        for (Box box: new ArrayList<>(BoxController.getAllBoxes(context).values())) {
            if (box.startTime != -1L) {
                Date boxDate = PersianDateUtil.truncateToStartOfDay(new Date(box.startTime));
                long startTime1 = limitation.getStartTimeFromDate(boxDate).getTime();
                long startTime2 = box.startTime;
                long endTime1 = startTime1 + limitation.length;
                long endTime2 = startTime2 + 60000L * box.duration;
                if (PersianDateUtil.truncateToStartOfDay(new Date(limitation.offset)).getTime() > boxDate.getTime())
                    continue;
                if (endTime1 > startTime2 && endTime2 > startTime1)
                    return "این زمان با زمان یک بسته در " + PersianDateUtil.getDateString(boxDate) + " تلاقی دارد.";
            }
        }

        return null;
    }

    public static Map<Integer, Limitation> getAllLimitations(Context context) {
        Map<Integer, Limitation> limitations = new HashMap<>();
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        String query = "SELECT * FROM " + DBHelper.LIMITATION_TABLE_NAME;
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                long offset = cursor.getLong(2);
                long period = cursor.getLong(3);
                long length = cursor.getLong(4);
                limitations.put(id, new Limitation(id, name, offset, period, length));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return limitations;
    }

    public static Limitation getLimitationById(Context context, int id) {
        return getAllLimitations(context).get(id);
    }

    public static ArrayList<Limitation> getLimitationsByDate(Context context, Date date) {
        Map<Integer, Limitation> limitations = getAllLimitations(context);
        ArrayList<Limitation> filteredLimitations = new ArrayList<>();
        date = PersianDateUtil.truncateToStartOfDay(date);
        long startTime = date.getTime();
        long endTime = PersianDateUtil.nextDay(date).getTime();
        for (Limitation limitation: new ArrayList<>(limitations.values())) {
            if (limitation.period == -1L) {
                if (startTime <= limitation.offset && limitation.offset < endTime)
                    filteredLimitations.add(limitation);
            } else {
                if (PersianDateUtil.truncateToStartOfDay(new Date(limitation.offset)).getTime() > date.getTime())
                    continue;
                double l = Math.ceil((double) (startTime - limitation.offset) / limitation.period);
                double r = (double) (endTime - limitation.offset) / limitation.period;
                if (l < r)
                    filteredLimitations.add(limitation);
            }
        }
        return filteredLimitations;
    }

}
