package com.azbuka.gshabalov.tsd_alcho_app.Activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.azbuka.gshabalov.tsd_alcho_app.R;
import com.azbuka.gshabalov.tsd_alcho_app.utils.CSVReadingHelper;
import com.azbuka.gshabalov.tsd_alcho_app.utils.Database;
import com.azbuka.gshabalov.tsd_alcho_app.utils.WriteCSVHelper;

import java.util.ArrayList;

public class Settings extends Activity {
    Spinner spinner;
    Database readDatabase;
    SQLiteDatabase readBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ArrayList<String> plodNames = new ArrayList<>();

        readDatabase = new Database(this);
        readBase = readDatabase.getWritableDatabase();
        Cursor cursor = readBase.query(true, Database.DATABASE_WRITE, new String[]{Database.PLOD}, null, null, Database.PLOD, null, null, null);
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(Database.PLOD);
            do {
                plodNames.add(cursor.getString(index));
            } while (cursor.moveToNext());
            cursor.close();
        }

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, plodNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }


    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.deletePlod:
                //saveText(spinner.getSelectedItem().toString(), current);

                break;


            case R.id.chooseTemplate:
                //saveText(spinner.getSelectedItem().toString(), current);

                break;


        }
    }

    private void clearbase() {
        readBase.delete(Database.DATABASE_SCAN, Database.PLOD, new String[]{spinner.getSelectedItem().toString()});
    }

    private void outData(SQLiteDatabase db){
        WriteCSVHelper writeCSVHelper;

        String foldeName = "/storage/sdcard0/AvExchange/Out";
        //Имя файла нужно указывать с расширением если оно нужно
        String fileName = "Out";

        String[] string = new String[8];
        String[] strings = {"", "", ""};

        writeCSVHelper = new WriteCSVHelper(foldeName, fileName, WriteCSVHelper.SEMICOLON_SEPARATOR);
        Cursor c = db.rawQuery("SELECT * FROM "+ Database.DATABASE_WRITE,null);
        if(c.moveToFirst()) {
            do {
                string[0] = c.getString(1);
                string[1] = c.getString(2);
                string[2] = c.getString(3);
                string[3] = c.getString(4);
                string[4] = c.getString(5);
                string[5] = c.getString(6);
                string[6] = c.getString(7);
                string[7] = c.getString(8);
                writeCSVHelper.writeLine(string);
            }while (c.moveToNext());

        }
        writeCSVHelper.writeLine(strings);
        writeCSVHelper.close();
        db.delete(Database.DATABASE_WRITE,"1",null);
    }

}
