package com.sharif.project.controller;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sharif.project.model.DBHelper;
import com.sharif.project.model.Plan;
import com.sharif.project.model.Topic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class PlanController {

    public static int addPlan(Context context, Plan plan) {
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.PLAN_NAME, plan.name);
        contentValues.put(DBHelper.PLAN_START_DATE, String.valueOf(plan.startDateTimeStamp));
        contentValues.put(DBHelper.PLAN_END_DATE, String.valueOf(plan.endDateTimeStamp));
        db.insert(DBHelper.PLAN_TABLE_NAME, null, contentValues);
        db.close();
        return Collections.max(getAllPlans(context).keySet());
    }

    public static void addPlanTopics(Context context, int planId, ArrayList<Integer> topicsId) {
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        for (int topicId: topicsId) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.PLAN_TOPIC_PLAN_ID, planId);
            contentValues.put(DBHelper.PLAN_TOPIC_TOPIC_ID, topicId);
            db.insert(DBHelper.PLAN_TOPIC_TABLE_NAME, null, contentValues);
        }
        db.close();
    }

    public static void updatePlan(Context context, Plan plan, String key) {
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.PLAN_NAME, plan.name);
        contentValues.put(DBHelper.PLAN_START_DATE, String.valueOf(plan.startDateTimeStamp));
        contentValues.put(DBHelper.PLAN_END_DATE, String.valueOf(plan.endDateTimeStamp));
        db.update(DBHelper.PLAN_TABLE_NAME, contentValues,
                DBHelper.PLAN_ID + "=?", new String[]{key});
        db.close();
    }

    public static void updatePlanTopics(Context context, int planId, ArrayList<Integer> topicsId) {
        deletePlanTopics(context, String.valueOf(planId));
        addPlanTopics(context, planId, topicsId);
    }

    public static void deletePlan(Context context, String key) {
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        db.delete(DBHelper.PLAN_TABLE_NAME,
                DBHelper.PLAN_ID + "=?", new String[]{key});
        db.close();
    }

    public static void deletePlanTopics(Context context, String key) {
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        db.delete(DBHelper.PLAN_TOPIC_TABLE_NAME,
                DBHelper.PLAN_TOPIC_PLAN_ID + "=?", new String[]{key});
        db.close();
    }

    public static Map<Integer, Plan> getAllPlans(Context context) {
        Map<Integer, Plan> plans = new HashMap<>();
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        String query = "SELECT * FROM " + DBHelper.PLAN_TABLE_NAME;
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                long startDateTimeStamp = cursor.getLong(2);
                long endDateTimeStamp = cursor.getLong(3);
                plans.put(id, new Plan(id, name, startDateTimeStamp, endDateTimeStamp));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return plans;
    }

    public static Plan getPlanById(Context context, int id) {
        return getAllPlans(context).get(id);
    }

    public static ArrayList<Integer> getPlanTopicsID(Context context, int id) {
        ArrayList<Integer> topicsId = new ArrayList<>();
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        String query = "SELECT * FROM " + DBHelper.PLAN_TOPIC_TABLE_NAME;
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                int planId = cursor.getInt(0);
                int topicId = cursor.getInt(1);
                if (planId == id)
                    topicsId.add(topicId);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return topicsId;
    }

}
