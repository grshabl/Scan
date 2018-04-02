package com.azbuka.gshabalov.tsd_alcho_app.Activity;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.azbuka.gshabalov.tsd_alcho_app.R;
import com.azbuka.gshabalov.tsd_alcho_app.utils.AdapterView;
import com.azbuka.gshabalov.tsd_alcho_app.utils.Database;
import com.azbuka.gshabalov.tsd_alcho_app.utils.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raushaniya on 02.04.18.
 */

public class ScanBoxViewActivity extends Activity {
    private List<Items> list;
    private RecyclerView rv;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_box_view);
        rv=(RecyclerView)findViewById(R.id.bottlesList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);
        initializeData();
        Database database = new Database(this);
        db = database.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+Database.DATABASE_SCAN,null);
        Items item;
        if(c.moveToFirst()){
            do{
                item = new Items(c.getString(4).substring(4,17),
                        c.getString(5).equals("0")?"Не считан":"Считан");
                list.add(item);
            }while (c.moveToNext());
        }

        initializeAdapter();
        Button button = (Button)findViewById(R.id.eraseBox);
    }

    public void onClick(View view) {
        db.delete(Database.DATABASE_SCAN,"1",null);
        list = new ArrayList<>();
        initializeAdapter();

    }
    private void initializeData(){
        list = new ArrayList<>();

    }

    private void initializeAdapter(){
        AdapterView adapter = new AdapterView(list);
        rv.setAdapter(adapter);
    }
}
