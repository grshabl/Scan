package com.azbuka.gshabalov.tsd_alcho_app.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.azbuka.gshabalov.tsd_alcho_app.BaseActivity;
import com.azbuka.gshabalov.tsd_alcho_app.R;
import com.azbuka.gshabalov.tsd_alcho_app.utils.CSVReadingHelper;
import com.azbuka.gshabalov.tsd_alcho_app.utils.Database;
import com.azbuka.gshabalov.tsd_alcho_app.utils.ReadDatabase;

import java.io.File;
import java.util.ArrayList;

public class ChoosePlod extends BaseActivity {
    File current;
    SharedPreferences sPref;
    ReadDatabase readDatabase;
    SQLiteDatabase readBase;
    Spinner spinner;
    CSVReadingHelper csvReadingHelper;

    private final String[] EXTERNAL_PERMS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private final int EXTERNAL_REQUEST = 138;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_plod);
        ReadDatabase readDatabase = new ReadDatabase(this);
        ArrayList<String> plodNames = new ArrayList<>();
        sPref = getSharedPreferences("DataShared", MODE_PRIVATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestForPermission();
        }


        File file = new File("/storage/sdcard0/AvExchange/In");
        File[] files = file.listFiles();

        if (files.length != 0) {
            current = files[0];
            for (File f : files) {
                if (f.toString().hashCode() > current.toString().hashCode()) { // не уверен что работает
                    current = f;
                }
            }

        } else {
            Alert("Папка пуста или файлы не распознаны");
        }

        csvReadingHelper = new CSVReadingHelper(current.toString(), readDatabase);

        readDatabase = new ReadDatabase(this);
        readBase = readDatabase.getWritableDatabase();
        Cursor cursor = readBase.query(true, ReadDatabase.DATABASE_NAME, new String[]{ReadDatabase.PLOD}, null, null, ReadDatabase.PLOD, null, null, null);
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(ReadDatabase.PLOD);
            do {
                plodNames.add(cursor.getString(index));
            } while (cursor.moveToNext());
            spinner = findViewById(R.id.spinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, plodNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }

    }


    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.choosePlodButton:
                saveText(spinner.getSelectedItem().toString(), current.toString());
                Intent intent = new Intent(this, ScanActivity.class);
                this.startActivity(intent);
                break;


        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean requestForPermission() {

        boolean isPermissionOn = true;
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            if (!canAccessExternalSd()) {
                isPermissionOn = false;
                requestPermissions(EXTERNAL_PERMS, EXTERNAL_REQUEST);
            }
        }

        return isPermissionOn;
    }

    private boolean canAccessExternalSd() {
        return (hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));

    }

    void saveText(String plod, String filename) {
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(Database.PLOD, plod);
        ed.putString("Filename", filename);
        ed.apply();
    }

    public void Alert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChoosePlod.this);
        builder.setTitle("Ошибка")
                .setMessage(msg)
                .setCancelable(false)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}