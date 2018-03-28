package ru.av.test.util;

import android.database.sqlite.SQLiteDatabase;

import ru.av.test.Data.Bottle;
import ru.av.test.Data.Boxes;


public class WriteSingleton {
    private static WriteSingleton instance;
    private static boolean wrote = false;

    private WriteSingleton() {

    }

    public static WriteSingleton getInstance() {
        if (instance == null) {
            instance = new WriteSingleton();
            wrote = false;
        }
        return instance;
    }

    public static void setWrote(boolean wrote) {
        if(instance==null)
            getInstance();
        WriteSingleton.wrote = wrote;
    }
    public static boolean getWrote(){
        if(instance==null)
            getInstance();
            return wrote;
    }
    public static void write(DBUsing dbUsing, SQLiteDatabase db) {
        if (wrote) {
            WriteCSVHelper writeCSVHelper;

            String folderName = "/storage/emulated/0/AvExchange/Out";
            String fileName = "DATA_TSD_TO_WIN";
            String[] string = new String[8];
            String[] strings = {"", "", ""};

            if (dbUsing.getPlods(db) != null && dbUsing.getBoxes(db)!=null && dbUsing.getBoxes(db).size()>0) {
                writeCSVHelper = new WriteCSVHelper(folderName, fileName, WriteCSVHelper.SEMICOLON_SEPARATOR);
                for (String plod : dbUsing.getPlods(db)) {
                    if (plod != null && dbUsing.getBoxes(plod, db) != null)
                        for (Boxes box : dbUsing.getBoxes(plod, db)) {
                            for (Bottle bottle : dbUsing.getBottles(db, box.id).bottles) {
                                string[0] = box.first_col;
                                string[1] = box.sec_col;
                                string[2] = box.third_col;
                                string[6] = box.LPB;
                                string[7] = box.fullness;
                                if (bottle.qr.length() == 11)
                                    string[3] = "XXX-" + bottle.qr + "ХХХХХХХХХХХХХХХХХ";
                                else
                                    string[3] = bottle.qr;

                                string[4] = bottle.pdf417;
                                if (string[4].equals("1")) {
                                    string[4] = "0";
                                    string[5] = "1";
                                } else
                                    string[5] = "0";
                                writeCSVHelper.writeLine(string);
                            }

                        }
                }
                writeCSVHelper.writeLine(strings);

                writeCSVHelper.close();
            }

        }
    }
}
