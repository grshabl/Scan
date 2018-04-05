package com.azbuka.gshabalov.tsd_alcho_app.Activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;

import com.azbuka.gshabalov.tsd_alcho_app.BaseActivity;
import com.azbuka.gshabalov.tsd_alcho_app.R;
import com.azbuka.gshabalov.tsd_alcho_app.utils.Database;


public class StartMenu extends BaseActivity {
    Database database;
    SQLiteDatabase readBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menu);
        database = new Database(this);
        readBase = database.getWritableDatabase();
    }



    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {

            case R.id.scan:
                if(boxcount()){
                    intent = new Intent(this, ScanActivity.class);
                } else {
                    intent = new Intent(this, ChoosePlod.class);
                }
                startActivity(intent);
                break;

            case R.id.settings:
                intent = new Intent(this, Settings.class);
                startActivity(intent);
                break;



            case R.id.view:
                intent = new Intent(this, ViewActivity.class);
                startActivity(intent);
                break;


            case R.id.ext:
                finishAffinity();
                break;


        }
    }
    @Override
    public void onBackPressed() {
        finishAffinity();
    }
    private boolean boxcount() {
        return readBase.query(Database.DATABASE_SCAN, null, null, null, null, null, null, null).getCount() > 0;
    }
}
