package com.sharif.project.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "database";
    private static final int DATABASE_VERSION = 1;


    public static final String TOPIC_TABLE_NAME = "topic";
    public static final String TOPIC_ID = "id";
    public static final String TOPIC_NAME = "name";
    public static final String TOPIC_PARENT_ID = "parent_id";
    private static final String TOPIC_CREATE_TABLE_QUERY =
            "CREATE TABLE " + TOPIC_TABLE_NAME + " (" +
                    TOPIC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TOPIC_NAME + " TEXT, " +
                    TOPIC_PARENT_ID + " INTEGER);";
    private static final String TOPIC_DROP_TABLE_QUERY =
            "DROP TABLE IF EXISTS " + TOPIC_TABLE_NAME + ";";

    public static final String PLAN_TABLE_NAME = "_plan";
    public static final String PLAN_ID = "id";
    public static final String PLAN_NAME = "name";
    public static final String PLAN_START_DATE = "start_date";
    public static final String PLAN_END_DATE = "end_date";
    private static final String PLAN_CREATE_TABLE_QUERY =
            "CREATE TABLE " + PLAN_TABLE_NAME + " (" +
                    PLAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PLAN_NAME + " TEXT, " +
                    PLAN_START_DATE + " INTEGER, " +
                    PLAN_END_DATE + " INTEGER);";
    private static final String PLAN_DROP_TABLE_QUERY =
            "DROP TABLE IF EXISTS " + PLAN_TABLE_NAME + ";";

    public static final String PLAN_TOPIC_TABLE_NAME = "plan_topic";
    public static final String PLAN_TOPIC_PLAN_ID = "plan_id";
    public static final String PLAN_TOPIC_TOPIC_ID = "topic_id";
    private static final String PLAN_TOPIC_CREATE_TABLE_QUERY =
            "CREATE TABLE " + PLAN_TOPIC_TABLE_NAME + " (" +
                    PLAN_TOPIC_PLAN_ID + " INTEGER, " +
                    PLAN_TOPIC_TOPIC_ID + " INTEGER, " +
                    "FOREIGN KEY (" + PLAN_TOPIC_PLAN_ID + ") " +
                    "REFERENCES " + PLAN_TABLE_NAME + "(" + PLAN_ID + ") " +
                    "ON DELETE CASCADE ON UPDATE CASCADE, " +
                    "FOREIGN KEY (" + PLAN_TOPIC_TOPIC_ID + ") " +
                    "REFERENCES " + TOPIC_TABLE_NAME + "(" + TOPIC_ID + ") " +
                    "ON DELETE CASCADE ON UPDATE CASCADE, " +
                    "PRIMARY KEY (" + PLAN_TOPIC_PLAN_ID + ", " + PLAN_TOPIC_TOPIC_ID + "));";
    private static final String PLAN_TOPIC_DROP_TABLE_QUERY =
            "DROP TABLE IF EXISTS " + PLAN_TOPIC_TABLE_NAME + ";";

    public static final String BOX_TABLE_NAME = "box";
    public static final String BOX_ID = "id";
    public static final String BOX_PLAN_ID = "plan_id";
    public static final String BOX_TOPIC_ID = "topic_id";
    public static final String BOX_DURATION = "duration";
    public static final String BOX_TIME_SPENT = "time_spent";
    public static final String BOX_START_TIME = "start_time";
    public static final String BOX_NOTE = "note";
    public static final String BOX_FINISHED = "finished";
    private static final String BOX_CREATE_TABLE_QUERY =
            "CREATE TABLE " + BOX_TABLE_NAME + " (" +
                    BOX_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    BOX_PLAN_ID + " INTEGER, " +
                    BOX_TOPIC_ID + " INTEGER, " +
                    BOX_DURATION + " INTEGER, " +
                    BOX_TIME_SPENT + " INTEGER, " +
                    BOX_START_TIME + " INTEGER, " +
                    BOX_NOTE + " TEXT, " +
                    BOX_FINISHED + " INTEGER, " +
                    "FOREIGN KEY (" + BOX_PLAN_ID + ") " +
                    "REFERENCES " + PLAN_TABLE_NAME + "(" + PLAN_ID + ") " +
                    "ON DELETE CASCADE ON UPDATE CASCADE, " +
                    "FOREIGN KEY (" + BOX_TOPIC_ID + ") " +
                    "REFERENCES " + TOPIC_TABLE_NAME + "(" + TOPIC_ID + ") " +
                    "ON DELETE CASCADE ON UPDATE CASCADE);";
    private static final String BOX_DROP_TABLE_QUERY =
            "DROP TABLE IF EXISTS " + PLAN_TOPIC_TABLE_NAME + ";";


    public static final String LIMITATION_TABLE_NAME = "limitation";
    public static final String LIMITATION_ID = "id";
    public static final String LIMITATION_NAME = "name";
    public static final String LIMITATION_OFFSET = "_offset";
    public static final String LIMITATION_PERIOD = "period";
    public static final String LIMITATION_LENGTH = "length";
    private static final String LIMITATION_CREATE_TABLE_QUERY =
            "CREATE TABLE " + LIMITATION_TABLE_NAME + " (" +
                    LIMITATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    LIMITATION_NAME + " TEXT, " +
                    LIMITATION_OFFSET + " INTEGER, " +
                    LIMITATION_PERIOD + " INTEGER, " +
                    LIMITATION_LENGTH + " INTEGER);";
    private static final String LIMITATION_DROP_TABLE_QUERY =
            "DROP TABLE IF EXISTS " + LIMITATION_TABLE_NAME + ";";


    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TOPIC_CREATE_TABLE_QUERY);
        db.execSQL(PLAN_CREATE_TABLE_QUERY);
        db.execSQL(PLAN_TOPIC_CREATE_TABLE_QUERY);
        db.execSQL(BOX_CREATE_TABLE_QUERY);
        db.execSQL(LIMITATION_CREATE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(LIMITATION_DROP_TABLE_QUERY);
        db.execSQL(BOX_DROP_TABLE_QUERY);
        db.execSQL(PLAN_TOPIC_DROP_TABLE_QUERY);
        db.execSQL(PLAN_DROP_TABLE_QUERY);
        db.execSQL(TOPIC_DROP_TABLE_QUERY);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON");
    }
}
