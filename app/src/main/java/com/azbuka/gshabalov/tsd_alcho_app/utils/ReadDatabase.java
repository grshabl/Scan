package com.azbuka.gshabalov.tsd_alcho_app.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gshabalov on 29.03.2018.
 */

public class ReadDatabase extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ReadDatabase";

    public static final String LINE = "_id";
    public static final String PLOD = "plod";
    public static final String PLOD_DAY = "plodday";
    public static final String PLOD_LINE = "plodline";
    public static final String GOODS_LINE = "goodsLINE";
    public static final String BOX_EAN = "boxean";
    public static final String GOODS_NAME = "goodsName";
    public static final String MARK_SERIAL = "markserial";
    public static final String STARTNUM = "startnum";
    public static final String ENDNUM = "endnum";
    public static final String MULTIPLICITY = "multiplicity";


    public ReadDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + DATABASE_NAME + "(" + LINE
                + " integer primary key," + PLOD + " text," + PLOD_DAY + " text," + PLOD_LINE + " text," + GOODS_LINE + " text," + BOX_EAN + " text," + GOODS_NAME +" text,"  +
                MARK_SERIAL + " text," + STARTNUM + " text," + ENDNUM + " text," + MULTIPLICITY + " text" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + DATABASE_NAME);

        onCreate(db);
    }



}