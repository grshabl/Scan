package com.azbuka.gshabalov.tsd_alcho_app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.azbuka.gshabalov.tsd_alcho_app.BaseActivity;
import com.azbuka.gshabalov.tsd_alcho_app.R;


public class StartMenu extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menu);
    }



    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.scan: /** Start a new Activity MyCards.java */
                Intent intent = new Intent(this, ChoosePlod.class);
                this.startActivity(intent);
                break;


        }
    }
}
