package ru.av.test;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import java.io.File;

import ru.av.test.activities.BoxesViewActivity;
import ru.av.test.activities.PlodChooseActivity;
import ru.av.test.activities.ScannerActivity;
import ru.av.test.activities.SettingsActivity;
import ru.av.test.util.CSVReadingHelper;
import ru.av.test.util.DBUsing;

import static ru.av.test.Data.BoxSingleton.helper2;

public class MainActivity extends Activity {
    private DBUsing dbUsing;
    private SQLiteDatabase db;
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
        setContentView(R.layout.activity_main);
        Button scan, view, settings;
        scan = findViewById(R.id.scan);
        view = findViewById(R.id.view);
        Button exit = findViewById(R.id.exit);

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAffinity();
            }
        });
        dbUsing = new DBUsing(this);
        db = dbUsing.getWritableDatabase();

        settings = findViewById(R.id.settings);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = dbUsing.getId(db);
                if (id == -1) {
                    Intent intent = new Intent(getApplicationContext(), PlodChooseActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), ScannerActivity.class);
                    File temp = new File("/storage/emulated/0/Temp/Av/tmp.txt");
                    intent.putExtra("ref", "main");
                    helper2 = new CSVReadingHelper(temp.toString());
                    helper2.getList();
                    helper2.setString(dbUsing.getFileName(db));
                    startActivity(intent);
                }
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BoxesViewActivity.class);
                startActivity(intent);
            }
        });


        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestForPermission();
        }
        requestForPermission();
        requestForPermission();

    }

}
