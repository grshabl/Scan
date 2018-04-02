package com.azbuka.gshabalov.tsd_alcho_app.Activity;

import android.Manifest;
import android.app.AlertDialog;
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

import java.io.File;
import java.util.ArrayList;

public class ChoosePlod extends BaseActivity {
    String current;
    SharedPreferences sPref;
    Database readDatabase;
    SQLiteDatabase readBase;
    Spinner spinner;
    CSVReadingHelper csvReadingHelper;

    private final String[] EXTERNAL_PERMS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private final int EXTERNAL_REQUEST = 138;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_plod);
        ArrayList<String> plodNames = new ArrayList<>();
        sPref = getSharedPreferences("DataShared", MODE_PRIVATE);
        current = sPref.getString(Database.DATABASE_FILENAME, "");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestForPermission();
        }


        File file = new File("/storage/sdcard0/AvExchange/In");
        File[] files = file.listFiles();

        if (files.length != 0) {
            for (File f : files) {
                if (f.toString().hashCode() > current.hashCode()) {
                    current = f.toString();
                    clearbase();
                    Alert("Список PLOD был обновлен");
                }

            }

        } else {
            Alert("Папка пуста или файлы не распознаны");
        }

        readDatabase = new Database(this);
        csvReadingHelper = new CSVReadingHelper(current, readDatabase);
        readBase = readDatabase.getWritableDatabase();
        Cursor cursor = readBase.query(true, Database.DATABASE_READ, new String[]{Database.PLOD}, null, null, Database.PLOD, null, null, null);
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(Database.PLOD);
            do {
                plodNames.add(cursor.getString(index));
            } while (cursor.moveToNext());
            cursor.close();
            spinner = findViewById(R.id.spinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, plodNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }

    }


    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.choosePlodButton:
                saveText(spinner.getSelectedItem().toString(), current);
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

    private void clearbase() {
        readBase.delete(Database.DATABASE_READ, null, null);
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
        ed.putString(Database.DATABASE_FILENAME, filename);
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
