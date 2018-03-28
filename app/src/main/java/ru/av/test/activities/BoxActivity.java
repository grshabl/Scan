package ru.av.test.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import ru.av.test.Data.Box;
import ru.av.test.Data.BoxSingleton;
import ru.av.test.Data.Items;
import ru.av.test.R;
import ru.av.test.adapters.AdapterView;
import ru.av.test.util.DBUsing;

public class BoxActivity extends Activity {

    private String qr, pdf417;
    private DBUsing dbUsing;
    private SQLiteDatabase db;
    private List<Items> list;
    private RecyclerView rv;
    private BoxSingleton boxSingleton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.box_activity);
        rv = findViewById(R.id.bottlesList);
        LinearLayoutManager llm = new LinearLayoutManager(this);

        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);
        initializeData();
        initializeAdapter();


        dbUsing = new DBUsing(this);
        db = dbUsing.getWritableDatabase();
        boxSingleton = BoxSingleton.getInstance(this);
        Box boxl = dbUsing.getBottles(db, dbUsing.getId(db));
        Button deleteBox = findViewById(R.id.eraseBox);

        if (boxl != null && boxl.bottles != null)
            for (int i = 0; i < boxl.bottles.size(); i++) {
                if (!boxl.bottles.get(i).qr.equals("0") && !boxl.bottles.get(i).pdf417.equals("0")) {

                    qr = boxl.bottles.get(i).qr.substring(4, 15);

                    if (boxl.bottles.get(i).pdf417.equals("1")) {
                        pdf417 = "Не считан";
                    } else {
                        pdf417 = "Считан";
                    }
                    list.add(new Items(qr, pdf417));
                }
            }



        deleteBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list = new ArrayList<>();
                initializeAdapter();
                ScannerActivity.newBottle(0);
                boxSingleton.setCount(0);
                boxSingleton.setOpened(0);
                ScannerActivity.description.setText("");
                dbUsing.deleteBox(db, dbUsing.getId(db));
            }
        });

    }

    private void initializeData() {
        list = new ArrayList<>();

    }

    private void initializeAdapter() {
        AdapterView adapter = new AdapterView(list);
        rv.setAdapter(adapter);
    }

}
