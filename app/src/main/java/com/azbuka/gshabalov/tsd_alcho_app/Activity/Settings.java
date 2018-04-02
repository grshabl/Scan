package com.azbuka.gshabalov.tsd_alcho_app.Activity;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.azbuka.gshabalov.tsd_alcho_app.R;
import com.azbuka.gshabalov.tsd_alcho_app.utils.Database;
import com.azbuka.gshabalov.tsd_alcho_app.utils.WriteCSVHelper;

public class Settings extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }



    private void outData(SQLiteDatabase db){
        WriteCSVHelper writeCSVHelper;

        String foldeName = "/storage/sdcard0/AvExchange/Out";
        //Имя файла нужно указывать с расширением если оно нужно
        String fileName = "DATA_TSD_TO_WIN";

        String[] string = new String[8];
        String[] strings = {"", "", ""};

        writeCSVHelper = new WriteCSVHelper(foldeName, fileName, WriteCSVHelper.SEMICOLON_SEPARATOR);
        Cursor c = db.rawQuery("SELECT * FROM "+ Database.DATABASE_WRITE,null);
        if(c.moveToFirst()) {
            do {
                string[0] = c.getString(1);
                string[1] = c.getString(2);
                string[2] = c.getString(3);
                string[3] = c.getString(4);
                string[4] = c.getString(5);
                string[5] = c.getString(6);
                string[6] = c.getString(7);
                string[7] = c.getString(8);
                writeCSVHelper.writeLine(string);
            }while (c.moveToNext());

        }
        writeCSVHelper.writeLine(strings);
        writeCSVHelper.close();
        db.delete(Database.DATABASE_WRITE,"1",null);
    }

}
