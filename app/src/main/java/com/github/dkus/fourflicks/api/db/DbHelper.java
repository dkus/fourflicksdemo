package com.github.dkus.fourflicks.api.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.dkus.fourflicks.util.Logger;

import java.util.ArrayList;
import java.util.List;


public class DbHelper extends SQLiteOpenHelper {

    //Database version
    private static final int DATABASE_VERSION = 1;

    //Database name
    private static final String DATABASE_NAME = "ffdemo";

    private List<OnTableDdlListener> mOnTableDdlListeners;

    public DbHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mOnTableDdlListeners = new ArrayList<OnTableDdlListener>();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        for (OnTableDdlListener onTableDdlListener : mOnTableDdlListeners) {
            Logger.log("Creating table "+onTableDdlListener.getTableName(), DbHelper.class);
            db.execSQL(onTableDdlListener.createTableDdl());
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //for this demo nothing here
    }

    public interface OnTableDdlListener {

        public String getTableName();

        public String createTableDdl();

    }

    public void addOnTableDdlListener(OnTableDdlListener onTableDdlListener) {
        mOnTableDdlListeners.add(onTableDdlListener);
    }

}
