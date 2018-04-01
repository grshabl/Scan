package com.azbuka.gshabalov.tsd_alcho_app.utils;

import android.app.Activity;
import android.app.Application;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by gshabalov on 29.03.2018.
 */

public class CSVReadingHelper {
    BufferedReader reader;
    SQLiteDatabase readBase;
    ContentValues contentValues;
    private String[] fileString;



    public CSVReadingHelper(String fileLocation, ReadDatabase readDatabase) {
        readBase = readDatabase.getWritableDatabase();
        contentValues = new ContentValues();
        fileString = new String[9];

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileLocation), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                fileString = line.split("\\t");
                if(fileString.length != 9)
                addReadData(fileString[0], fileString[1], fileString[2], fileString[3], fileString[4], fileString[5], fileString[6], fileString[7], fileString[8], fileString[9]);
            }

            reader.close();
        } catch (UnsupportedEncodingException e) {
            Log.d("reader", "Кодировка");
        } catch (FileNotFoundException e) {
            Log.d("reader", "Не найден файл");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("reader", "Неизвестная ошибка");
            e.printStackTrace();
        }

    }


    public void addReadData(String plodnum, String day, String plodline, String goodsLine, String ean13, String goodname, String markserial, String start, String end, String multiplicity) {
        contentValues.put(ReadDatabase.PLOD, plodnum);
        contentValues.put(ReadDatabase.PLOD_DAY, day);
        contentValues.put(ReadDatabase.PLOD_LINE, plodline);
        contentValues.put(ReadDatabase.GOODS_LINE, goodsLine);
        contentValues.put(ReadDatabase.BOX_EAN, ean13);
        contentValues.put(ReadDatabase.GOODS_NAME, goodname);
        contentValues.put(ReadDatabase.MARK_SERIAL, markserial);
        contentValues.put(ReadDatabase.STARTNUM, start);
        contentValues.put(ReadDatabase.ENDNUM, end);
        contentValues.put(ReadDatabase.MULTIPLICITY, multiplicity);


        readBase.insert(ReadDatabase.DATABASE_NAME, null, contentValues);
        contentValues.clear();

    }

}

