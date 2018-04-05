package com.azbuka.gshabalov.tsd_alcho_app.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.azbuka.gshabalov.tsd_alcho_app.utils.Items;
import com.azbuka.gshabalov.tsd_alcho_app.utils.WriteCSVHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Settings extends Activity {
    Spinner spinner;
    Database readDatabase;
    SQLiteDatabase readBase;
    SharedPreferences sPref;
    EditText template;
    ArrayAdapter<String> adapter;
    ArrayList<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ArrayList<String> plodNames = new ArrayList<>();
        spinner = (Spinner)findViewById(R.id.spinner1);

        template = findViewById(R.id.edittextishe);
        readDatabase = new Database(this);
        readBase = readDatabase.getWritableDatabase();
        Cursor c = readBase.rawQuery("SELECT * FROM "+Database.DATABASE_WRITE,null);
        Map<String,Integer> map = new HashMap<>();
        String tmp;
        int count;
        list = new ArrayList<>();
        if(c.moveToFirst()){
            do{
                tmp = c.getString(1);
                if(!list.contains(tmp)) {
                    list.add(tmp);
                }

            }while(c.moveToNext());
        }
        c.close();


            spinner = findViewById(R.id.spinner1);
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            if(adapter!=null)
            spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> adapterView, View view, int i, long l) {
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> adapterView) {
            }

        });

    }


    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.deletePlod:
                clearbase();
                break;


            case R.id.chooseTemplate:
                saveText(template.getText().toString());
                break;


        }
    }

    private void clearbase() {
        if(adapter!=null && spinner.getSelectedItem()!=null) {
            readBase.delete(Database.DATABASE_WRITE, Database.PLOD + " = '" + spinner.getSelectedItem().toString() + "'", null);
            Cursor c = readBase.rawQuery("SELECT * FROM " + Database.DATABASE_WRITE, null);
            Map<String, Integer> map = new HashMap<>();
            String tmp;
            int count;
            list = new ArrayList<>();
            if (c.moveToFirst()) {
                do {
                    tmp = c.getString(1);
                    if (!list.contains(tmp)) {
                        list.add(tmp);
                    }

                } while (c.moveToNext());
            }
            c.close();
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            if (adapter != null)
                spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> adapterView, View view, int i, long l) {
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> adapterView) {
                }

            });
        }
    }

    void saveText(String lpb) {
        sPref = getSharedPreferences("DataShared", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(Database.GOODS_LPB, lpb);
        ed.apply();
    }


    public void Alert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
        builder.setTitle("Информация")
                .setMessage(msg)
                .setCancelable(false)
                .setNegativeButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), StartMenu.class);
        startActivity(intent);
        finish();

    }



}
