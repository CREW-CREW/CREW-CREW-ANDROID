package com.example.crewcrew_demo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBManager extends SQLiteOpenHelper {
    public DBManager(Context context){
        super(context, "csc", null, 1);
    }
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table crewTB (name text, level text, maxnum integer, time text)");
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}