package com.azbuka.gshabalov.tsd_alcho_app.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gshabalov on 29.03.2018.
 */

public class Database extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "alchoDB";

    public static final String BOTTLE_ID = "id";
    public static final String PLOD = "plod";
    public static final String PLOD_LINE = "plodline";
    public static final String GOODS_LINE = "goodsLINE";
    public static final String QR = "qr";
    public static final String PDF417 = "pdf417";
    public static final String MARK_BAD = "markbad";
    public static final String BOX_EAN = "boxean";
    public static final String MULTIPLICITY = "multiplicity";


    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + DATABASE_NAME + "(" + BOTTLE_ID
                + " integer primary key," + PLOD + " text," + PLOD_LINE + " text," + GOODS_LINE + " text," + QR +" text,"
                + PDF417 + " text," + MARK_BAD + " text,"
                + BOX_EAN + " text," + MULTIPLICITY + " text" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + DATABASE_NAME);

        onCreate(db);
    }

}
