package com.cheng.lkc12306.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by cheng on 2016/8/26.
 */
public class HistotyHelper extends SQLiteOpenHelper {
    private static final String DATABASENAME="history.db";
    private static final int VERSION=1;
    public HistotyHelper(Context context) {
        super(context, DATABASENAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table history(id integer primary key autoincrement,rec text)";
        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
