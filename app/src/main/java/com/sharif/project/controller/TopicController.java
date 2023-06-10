package com.sharif.project.controller;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sharif.project.model.DBHelper;
import com.sharif.project.model.Topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class TopicController {

    public static void addTopic(Context context, Topic topic) {
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.TOPIC_NAME, topic.name);
        contentValues.put(DBHelper.TOPIC_PARENT_ID, topic.parentId);
        db.insert(DBHelper.TOPIC_TABLE_NAME, null, contentValues);
        db.close();
    }

    public static void updateTopic(Context context, Topic topic, String key) {
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.TOPIC_NAME, topic.name);
        contentValues.put(DBHelper.TOPIC_PARENT_ID, topic.parentId);
        db.update(DBHelper.TOPIC_TABLE_NAME, contentValues,
                DBHelper.TOPIC_ID + "=?", new String[]{key});
        db.close();
    }

    public static void deleteTopic(Context context, int key) {
        Map<Integer, ArrayList<Integer>> childrenDict = getChildrenDict(context);
        deleteRecursive(context, key, childrenDict);
    }

    public static void deleteSingleTopic(Context context, String key) {
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        db.delete(DBHelper.TOPIC_TABLE_NAME,
                DBHelper.TOPIC_ID + "=?", new String[]{key});
        db.close();
    }

    public static void deleteRecursive(Context context, int key, Map<Integer, ArrayList<Integer>> childrenDict) {
        ArrayList<Integer> childrenId = childrenDict.get(key);
        deleteSingleTopic(context, String.valueOf(key));
        for (Integer childId: childrenId)
            deleteRecursive(context, childId, childrenDict);
    }

    public static Map<Integer, Topic> getAllTopics(Context context) {
        Map<Integer, Topic> topics = new HashMap<>();
        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        String query = "SELECT * FROM " + DBHelper.TOPIC_TABLE_NAME;
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int parentId = cursor.getInt(2);
                topics.put(id, new Topic(id, name, parentId));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return topics;
    }

    public static Topic getTopicById(Context context, int id) {
        return getAllTopics(context).get(id);
    }

    public static Map<Integer, ArrayList<Integer>> getChildrenDict(Context context) {
        Map<Integer, ArrayList<Integer>> childrenDict = new HashMap<>();
        Map<Integer, Topic> topics = getAllTopics(context);
        for (Topic topic: topics.values())
            childrenDict.put(topic.id, new ArrayList<>());
        for (Topic topic: topics.values())
            if (topic.parentId != 0)
                childrenDict.get(topic.parentId).add(topic.id);
        return childrenDict;
    }

    public static Map<Integer, String> getHierarchicalInfoDict(Context context, int exception) {
        Map<Integer, String> hierarchicalInfoDict = new HashMap<>();

        Map<Integer, ArrayList<Integer>> childrenDict = getChildrenDict(context);
        Map<Integer, Topic> topics = getAllTopics(context);
        LinkedList<Integer> queue = new LinkedList<>();

        for (Integer id: topics.keySet()) {
            if (topics.get(id).parentId == 0) {
                queue.add(id);
                while (queue.size() > 0) {
                    int topicId = queue.poll();
                    if (topicId == exception)
                        continue;
                    int topicParentId = topics.get(topicId).parentId;
                    String parentPath = hierarchicalInfoDict.getOrDefault(topicParentId, "");
                    String path = (parentPath.isEmpty() ? "" : parentPath + " › ") + topics.get(topicId).name;
                    hierarchicalInfoDict.put(topicId, path);
                    queue.addAll(childrenDict.get(topicId));
                }
            }
        }
        return hierarchicalInfoDict;
    }

    public static String getFullNameById(Context context, int id) {
        Map<Integer, Topic> topics = getAllTopics(context);
        Topic topic = topics.get(id);
        String fullName = topic.name;
        while (topic.parentId != 0) {
            Topic parentTopic = topics.get(topic.parentId);
            fullName = parentTopic.name + " › " + fullName;
            topic = parentTopic;
        }
        return fullName;
    }

    public static Map<Integer, Boolean> getExpansionDict(Context context, ArrayList<Integer> topicsId) {
        Map<Integer, Boolean> expansionDict = new HashMap<>();
        Map<Integer, Topic> topics = getAllTopics(context);
        for (Integer topicId: new ArrayList<>(topics.keySet()))
            expansionDict.put(topicId, false);
        if (topicsId != null) {
            for (Integer topicId : topicsId) {
                topicId = topics.get(topicId).parentId;
                while (topicId != 0) {
                    expansionDict.put(topicId, true);
                    topicId = topics.get(topicId).parentId;
                }
            }
        }
        return expansionDict;
    }
}
