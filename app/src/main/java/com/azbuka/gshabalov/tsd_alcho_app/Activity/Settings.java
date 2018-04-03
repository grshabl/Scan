package com.azbuka.gshabalov.tsd_alcho_app.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
    SharedPreferences sPref;
    EditText template;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ArrayList<String> plodNames = new ArrayList<>();

        template = findViewById(R.id.edittextishe);
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
                saveText(template.getText().toString());
                break;


        }
    }

    private void clearbase() {
        readBase.delete(Database.DATABASE_SCAN, Database.PLOD, new String[]{spinner.getSelectedItem().toString()});
    }

    void saveText(String lpb) {
        sPref = getSharedPreferences("DataShared", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(Database.GOODS_LPB, lpb);
        ed.apply();
    }



}
