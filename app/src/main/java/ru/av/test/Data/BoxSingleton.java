package ru.av.test.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import ru.av.test.util.CSVReadingHelper;
import ru.av.test.util.DBUsing;
import ru.av.test.activities.ScannerActivity;


public class BoxSingleton {
    private static BoxSingleton instance;
    public static CSVReadingHelper helper2;
    private static DBUsing dbUsing;
    private static SQLiteDatabase db;
    private int opened = 0;
    private int count = 0;
    private boolean qr, pdf417;

    private BoxSingleton(Context context) {
        dbUsing = new DBUsing(context);
        db = dbUsing.getWritableDatabase();
    }

    public static BoxSingleton getInstance(Context context) {
        if (instance == null) {
            instance = new BoxSingleton(context);
            instance.qr = false;
            instance.pdf417 = false;
        }
        return instance;
    }

    public void setOpened(int opened) {
        this.opened = opened;
    }

    public int getOpened() {
        return opened;
    }

    public int isOpened() {
        return this.opened;
    }

    public int getCount() {
        return this.count;
    }

    private void setCount() {
        this.count++;
    }


    public void setCount(int count) {
        this.count = count;
    }


    public void setQr() {
        this.qr = true;
        if (this.pdf417) {
            this.qr = false;
            this.pdf417 = false;
            this.setCount();

            ScannerActivity.newBottle(this.getCount());
        }
    }

    public void setPdf417() {
        this.pdf417 = true;
        if (this.qr) {
            this.qr = false;
            this.pdf417 = false;
            this.setCount();
            ScannerActivity.newBottle(this.getCount());

        }
    }

    public void delQRPDF() {
        this.pdf417 = false;
        this.qr = false;
    }

    public boolean isPdf417() {
        return pdf417;
    }

    public boolean isQr() {
        return qr;
    }

}
