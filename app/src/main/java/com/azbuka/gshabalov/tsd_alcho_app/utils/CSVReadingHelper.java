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



public class CSVReadingHelper {
    BufferedReader reader;
    Database readdataBase;
    SQLiteDatabase readBase;
    ContentValues contentValues;
    private String[] fileString;



    public CSVReadingHelper(String fileLocation, Database readDatabase) {
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
        contentValues.put(Database.PLOD, plodnum);
        contentValues.put(Database.PLOD_DAY, day);
        contentValues.put(Database.PLOD_LINE, plodline);
        contentValues.put(Database.GOODS_CODE, goodsLine);
        contentValues.put(Database.BOX_EAN, ean13);
        contentValues.put(Database.GOODS_NAME, goodname);
        contentValues.put(Database.SERIAL, markserial);
        contentValues.put(Database.STARTNUM, start);
        contentValues.put(Database.ENDNUM, end);
        contentValues.put(Database.MULTIPLICITY, multiplicity);


        readBase.insert(Database.DATABASE_READ, null, contentValues);
        contentValues.clear();

    }

}

