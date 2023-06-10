package com.sharif.project.controller;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sharif.project.model.Box;
import com.sharif.project.model.DBHelper;
import com.sharif.project.model.Limitation;
import com.sharif.project.model.Plan;
import com.sharif.project.util.PersianDateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BoxController {

    public static int addBox(Context context, Box box) {
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.BOX_PLAN_ID, box.planId);
        contentValues.put(DBHelper.BOX_TOPIC_ID, box.topicId);
        contentValues.put(DBHelper.BOX_DURATION, box.duration);
        contentValues.put(DBHelper.BOX_TIME_SPENT, box.timeSpent);
        contentValues.put(DBHelper.BOX_START_TIME, box.startTime);
        contentValues.put(DBHelper.BOX_NOTE, box.note);
        contentValues.put(DBHelper.BOX_FINISHED, box.finished);
        db.insert(DBHelper.BOX_TABLE_NAME, null, contentValues);
        db.close();
        return Collections.max(getAllBoxes(context).keySet());
    }

    public static void updateBox(Context context, Box box, String key) {
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.BOX_PLAN_ID, box.planId);
        contentValues.put(DBHelper.BOX_TOPIC_ID, box.topicId);
        contentValues.put(DBHelper.BOX_DURATION, box.duration);
        contentValues.put(DBHelper.BOX_TIME_SPENT, box.timeSpent);
        contentValues.put(DBHelper.BOX_START_TIME, box.startTime);
        contentValues.put(DBHelper.BOX_NOTE, box.note);
        contentValues.put(DBHelper.BOX_FINISHED, box.finished);
        db.update(DBHelper.BOX_TABLE_NAME, contentValues,
                DBHelper.BOX_ID + "=?", new String[]{key});
        db.close();
    }

    public static String getBoxTimeError(Context context, Box box) {
        Plan plan = PlanController.getPlanById(context, box.planId);
        if (box.startTime == -1L)
            return null;
        if (box.startTime < plan.startDateTimeStamp)
            return "زمان شروع بسته نباید قبل از زمان شروع برنامه باشد.";
        if (box.startTime > plan.endDateTimeStamp + 86400000L)
            return "زمان شروع بسته نباید بعد از زمان پایان برنامه باشد.";
        if (box.startTime + 60000L * box.duration > plan.endDateTimeStamp + 86400000L)
            return "زمان پایان بسته نباید بعد از زمان پایان برنامه باشد.";

        int startMin = PersianDateUtil.getMinutesPassedFromStartOfDay(new Date(box.startTime));
        int endMin = PersianDateUtil.getMinutesPassedFromStartOfDay(new Date(box.startTime + 60000L * box.duration));
        if (endMin > 0 && startMin > endMin)
            return "زمان پایان بسته نباید در روز بعد باشد.";

        for (Box otherBox: new ArrayList<>(BoxController.getAllBoxes(context).values())) {
            if (box.id == null || !box.id.equals(otherBox.id)) {
                long boxEndTime = box.startTime + 60000L * box.duration;
                long otherBoxEndTime = otherBox.startTime + 60000L * otherBox.duration;
                if (boxEndTime > otherBox.startTime && otherBoxEndTime > box.startTime)
                    return "این زمان با زمان انجام یک بسته دیگر تلاقی دارد.";
            }
        }

        for (Limitation limitation: new ArrayList<>(LimitationController.getAllLimitations(context).values())) {
            long startTime1 = limitation.getStartTimeFromDate(PersianDateUtil.truncateToStartOfDay(new Date(box.startTime))).getTime();
            long startTime2 = box.startTime;
            long endTime1 = startTime1 + limitation.length;
            long endTime2 = box.startTime + 60000L * box.duration;
            if (endTime1 > startTime2 && endTime2 > startTime1)
                return "این زمان با زمان یک محدودیت در " + PersianDateUtil.getDateString(new Date(startTime2)) + " تلاقی دارد.";
        }

        return null;
    }


    public static void deleteBox(Context context, String key) {
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        db.delete(DBHelper.BOX_TABLE_NAME,
                DBHelper.BOX_ID + "=?", new String[]{key});
        db.close();
    }

    public static Map<Integer, Box> getAllBoxes(Context context) {
        Map<Integer, Box> boxes = new HashMap<>();
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        String query = "SELECT * FROM " + DBHelper.BOX_TABLE_NAME;
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                int planId = cursor.getInt(1);
                int topicId = cursor.getInt(2);
                int duration = cursor.getInt(3);
                int timeSpent = cursor.getInt(4);
                long startTime = cursor.getLong(5);
                String note = cursor.getString(6);
                boolean finished = cursor.getInt(7) == 1;
                boxes.put(id, new Box(id, planId, topicId, duration, timeSpent, startTime, note, finished));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return boxes;
    }

    public static Box getBoxById(Context context, int id) {
        return getAllBoxes(context).get(id);
    }

    public static ArrayList<Box> getAllBoxesById(Context context, int planId, int topicId) {
        ArrayList<Box> allBoxes =  new ArrayList<>(getAllBoxes(context).values());
        ArrayList<Box> boxes = new ArrayList<>();
        for (Box box: allBoxes)
            if ((planId == -1 || box.planId.equals(planId)) && (topicId == -1 || box.topicId.equals(topicId)))
                boxes.add(box);
        return boxes;
    }

    public static ArrayList<Box> getAllUnspecifiedBoxesByDate(Context context, Date date) {
        ArrayList<Box> allBoxes =  new ArrayList<>(getAllBoxes(context).values());
        ArrayList<Box> boxes = new ArrayList<>();

        Map<Integer, Plan> plans = PlanController.getAllPlans(context);

        for (Box box: allBoxes) {
            Plan plan = plans.get(box.planId);

            if (box.startTime == -1L && plan.startDateTimeStamp <= date.getTime() && plan.endDateTimeStamp + 86400000L > date.getTime())
                boxes.add(box);
        }
        return boxes;
    }

    public static double getCompletionRatio(Context context, int planId) {
        double cnt = 0;
        double n = 0;
        for (Box box: getAllBoxesById(context, planId, -1)) {
            n += box.duration;
            if (box.finished)
                cnt += box.duration;
        }
        return n == 0? 0 : cnt / n;
    }

    public static int getTotalTimeInRange(Context context, int planId, int topicId, Date start, Date end) {
        Map<Integer, ArrayList<Integer>> allTopicsDict = TopicController.getChildrenDict(context);
        ArrayList<Integer> planTopicsId = PlanController.getPlanTopicsID(context, planId);
        if (planId == -1)
            planTopicsId = new ArrayList<>(TopicController.getAllTopics(context).keySet());
        return getTotalTimeHelper(context, planId, topicId, start, end, allTopicsDict, planTopicsId);
    }

    public static int getTotalTime(Context context, int planId, int topicId) {
        Map<Integer, ArrayList<Integer>> allTopicsDict = TopicController.getChildrenDict(context);
        ArrayList<Integer> planTopicsId = PlanController.getPlanTopicsID(context, planId);
        if (planId == -1)
            planTopicsId = new ArrayList<>(TopicController.getAllTopics(context).keySet());
        return getTotalTimeHelper(context, planId, topicId, null, null, allTopicsDict, planTopicsId);
    }

    public static int getTotalTimeHelper(Context context, int planId, int topicId, Date start, Date end, Map<Integer, ArrayList<Integer>> allTopicsDict, ArrayList<Integer> planTopicsId) {
        int totalTime = 0;
        if (topicId == -1 || planTopicsId.contains(topicId))
            for (Box box : getAllBoxesById(context, planId, topicId))
                if (box.finished)
                    if ((start == null && end == null) || (start.getTime() <= box.startTime && box.startTime < end.getTime()))
                        totalTime += box.duration;

        if (topicId != -1) {
            for (Integer childId : allTopicsDict.get(topicId))
                totalTime += getTotalTimeHelper(context, planId, childId, start, end, allTopicsDict, planTopicsId);
        }

        return totalTime;
    }

    public static int getRemainingTimeInRange(Context context, int planId, int topicId, Date start, Date end) {
        Map<Integer, ArrayList<Integer>> allTopicsDict = TopicController.getChildrenDict(context);
        ArrayList<Integer> planTopicsId = PlanController.getPlanTopicsID(context, planId);
        if (planId == -1)
            planTopicsId = new ArrayList<>(TopicController.getAllTopics(context).keySet());
        return getRemainingTimeHelper(context, planId, topicId, start, end, allTopicsDict, planTopicsId);
    }

    public static int getRemainingTime(Context context, int planId, int topicId) {
        Map<Integer, ArrayList<Integer>> allTopicsDict = TopicController.getChildrenDict(context);
        ArrayList<Integer> planTopicsId = PlanController.getPlanTopicsID(context, planId);
        if (planId == -1)
            planTopicsId = new ArrayList<>(TopicController.getAllTopics(context).keySet());
        return getRemainingTimeHelper(context, planId, topicId, null, null, allTopicsDict, planTopicsId);
    }

    public static int getRemainingTimeHelper(Context context, int planId, int topicId, Date start, Date end, Map<Integer, ArrayList<Integer>> allTopicsDict, ArrayList<Integer> planTopicsId) {
        int remainingTime = 0;
        if (topicId == -1 || planTopicsId.contains(topicId))
            for (Box box : getAllBoxesById(context, planId, topicId))
                if (!box.finished)
                    if ((start == null && end == null) || (start.getTime() <= box.startTime && box.startTime < end.getTime()))
                        remainingTime += box.duration;

        if (topicId != -1) {
            for (Integer childId : allTopicsDict.get(topicId))
                remainingTime += getRemainingTimeHelper(context, planId, childId, start, end, allTopicsDict, planTopicsId);
        }

        return remainingTime;
    }

    public static int getTimeSpentInRange(Context context, int planId, int topicId, Date start, Date end) {
        Map<Integer, ArrayList<Integer>> allTopicsDict = TopicController.getChildrenDict(context);
        ArrayList<Integer> planTopicsId = PlanController.getPlanTopicsID(context, planId);
        if (planId == -1)
            planTopicsId = new ArrayList<>(TopicController.getAllTopics(context).keySet());
        return getTimeSpentHelper(context, planId, topicId, start, end, allTopicsDict, planTopicsId);
    }

    public static int getTimeSpent(Context context, int planId, int topicId) {
        Map<Integer, ArrayList<Integer>> allTopicsDict = TopicController.getChildrenDict(context);
        ArrayList<Integer> planTopicsId = PlanController.getPlanTopicsID(context, planId);
        if (planId == -1)
            planTopicsId = new ArrayList<>(TopicController.getAllTopics(context).keySet());
        return getTimeSpentHelper(context, planId, topicId, null, null, allTopicsDict, planTopicsId);
    }

    public static int getTimeSpentHelper(Context context, int planId, int topicId, Date start, Date end, Map<Integer, ArrayList<Integer>> allTopicsDict, ArrayList<Integer> planTopicsId) {
        int timeSpent = 0;
        if (planTopicsId.contains(topicId))
            for (Box box : getAllBoxesById(context, planId, topicId))
                if (box.finished)
                    if ((start == null && end == null) || (start.getTime() <= box.startTime && box.startTime < end.getTime()))
                        timeSpent += box.timeSpent;

        for (Integer childId: allTopicsDict.get(topicId))
            timeSpent += getTimeSpentHelper(context, planId, childId, start, end, allTopicsDict, planTopicsId);

        return timeSpent;
    }

    public static double getEfficiency(Context context, int planId, int topicId, Date start, Date end) {
        double timeSpent = getTimeSpentInRange(context, planId, topicId, start, end);
        double totalTime = getTotalTimeInRange(context, planId, topicId, start, end);
        return totalTime == 0 ? -1 : timeSpent / totalTime;
    }

    public static void autoCreateBox(Context context, int planId, int capacity, int maxDuration, double prevStudyImportance, Map<Integer, Double> coeffDict) {
        Map<Integer, Integer> historyDict = new HashMap<>();
        Map<Integer, Integer> timeDict = new HashMap<>();

        double historyTotalTime = 0;
        for (Integer topicId: coeffDict.keySet()) {
            historyDict.put(topicId, getTimeSpent(context, planId, topicId));
            historyTotalTime += historyDict.get(topicId);
        }

        double coeffSum = 0;
        for (Integer topicId: coeffDict.keySet()) {
            double coeff = coeffDict.get(topicId);
            if (historyTotalTime > 0)
                coeff /= (1 + prevStudyImportance * (historyDict.get(topicId) /  historyTotalTime));
            coeffDict.put(topicId, coeff);
            coeffSum += coeff;
        }

        if (coeffSum > 0) {
            for (Integer topicId : coeffDict.keySet()) {
                double coeff = coeffDict.get(topicId);
                int time = (int) (capacity * coeff / coeffSum);
                timeDict.put(topicId, time);
                for (int i = 0; i < time / maxDuration; i++) {
                    Box box = new Box(null, planId, topicId, maxDuration, 0, -1L, "", false);
                    addBox(context, box);
                }
                if (time % maxDuration > 0) {
                    Box box = new Box(null, planId, topicId, time % maxDuration, 0, -1L, "", false);
                    addBox(context, box);
                }
            }
        }
    }

}
