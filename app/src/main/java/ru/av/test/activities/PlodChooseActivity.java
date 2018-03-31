package ru.av.test.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import ru.av.test.R;
import ru.av.test.util.CSVReadingHelper;
import ru.av.test.util.DBUsing;
import ru.av.test.util.WriteSingleton;

import static ru.av.test.Data.BoxSingleton.helper2;



public class PlodChooseActivity extends Activity {
    private DBUsing dbUsing;
    private SQLiteDatabase db;
    private Spinner spinner;
    private ArrayList<String> list;

    public static String dir = "";
    private final String[] EXTERNAL_PERMS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private final int EXTERNAL_REQUEST = 138;


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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plodchoose_layout);
        requestForPermission();
        Button button = findViewById(R.id.choosePlodButton);
        dbUsing = new DBUsing(this);
        db = dbUsing.getWritableDatabase();


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (spinner != null)
                    if (spinner.getSelectedItemPosition() != -1L) {
                        Intent intent = new Intent(getApplicationContext(), ScannerActivity.class);

                        helper2.pointer = spinner.getSelectedItemPosition();
                        helper2.setPlod();

                        startActivity(intent);
                    }
            }
        });


    }

    @Override
    protected void onStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestForPermission();
        }
        File file = new File("/storage/sdcard0/AvExchange/In");
        File[] files = file.listFiles();

        File temp = new File("/storage/emulated/0/Temp/Av/tmp.txt");
        if (!temp.exists() || WriteSingleton.getWrote() || dbUsing.getPlods(db) == null) {
            if (files != null && files.length > 0) {
                File tmp = files[files.length - 1];

                try {
                    copyFile(tmp, temp);
                    dir = temp.toString();
                    setSpinnerAdapter(getPlodNames());
                } catch (IOException e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Ошибка!")
                            .setMessage("Не удалось сохранить плод")
                            .setCancelable(false)
                            .setNegativeButton("Закрыть",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                    Log.d("Copy", "Не удалось скопировать файл");
                    e.printStackTrace();
                }
                dbUsing.setLastPlod(db, dir);
                tmp.delete();
            }

        } else if (files != null && files.length > 0) {
            File tmp = files[files.length - 1];
            if (tmp.toString().hashCode() > dbUsing.getLastPlod(db).hashCode()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Внимание")
                        .setMessage("Был загружен новый файл, чтобы загрузить его сделайте выгрузку файл")
                        .setCancelable(false)
                        .setNegativeButton("Закрыть",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

        }

        dir = temp.toString();
        setSpinnerAdapter(getPlodNames());

        super.onStart();
    }

    @Override
    protected void onResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestForPermission();
        }


        super.onResume();

    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            File file = new File("/storage/emulated/0/Temp");
            if (!file.exists())
                file.mkdir();
            file = new File("/storage/emulated/0/Temp/Av");
            if (!file.exists())
                file.mkdir();
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    private void setSpinnerAdapter(ArrayList<String> plodNames) {
        spinner = findViewById(R.id.spinner);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, plodNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }


    // Проходимся по содержимому папки. Если это .csv файл, то из названия берем
    // PLOD и его дату, тем самым составляем лист для выпадающего списка
    private ArrayList<String> getPlodNames() {
        list = new ArrayList<>();
        helper2 = new CSVReadingHelper(dir);
        list = helper2.getList();


        return list;
    }


}
