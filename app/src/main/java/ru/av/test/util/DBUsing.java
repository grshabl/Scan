package ru.av.test.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import ru.av.test.Data.Bottle;
import ru.av.test.Data.Box;
import ru.av.test.Data.Boxes;


public class DBUsing extends SQLiteOpenHelper{

    public DBUsing(Context context) {
        super(context, "mainDB",null,3);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table PLODS ("
                + "id integer primary key autoincrement,"
                + "EAN text,"
                + "LPB text,"
                + "multiply  text,"
                + "first_column text,"
                + "second_column text,"
                + "third_column text,"
                + "fullness integer,"
                + "count integer,"
                + "plod text);");
        db.execSQL("create table Bottles ("
                + "id integer primary key autoincrement,"
                + "boxId integer,"
                + "QR text,"
                + "PDF417 text);");
        db.execSQL("create table FILES (" +
                "fileName text);");
        db.execSQL("create table template ("
                + "template text);");
        db.execSQL("create table plod_list (" +
                "id integer primary key autoincrement," +
                "plod text);");
        db.execSQL("create table last_plod (plod text);");
        db.execSQL("INSERT INTO template VALUES('LPB-??????????');");
        db.execSQL("INSERT INTO FILES VALUES('0');");

    }
    public ArrayList<String> getPlods(SQLiteDatabase db){
        Cursor c = db.rawQuery("SELECT * FROM plod_list",null);
        if(c.moveToFirst()){
            ArrayList<String> list = new ArrayList<>();
            do{
                list.add(c.getString(c.getColumnIndex("plod")));
            }while(c.moveToNext());
            return  list;
        }
        else
            return null;
    }
    public void setLastPlod(SQLiteDatabase db,String plod){
        ContentValues newValues = new ContentValues();
        newValues.put("plod",plod);
        Cursor c = db.rawQuery("SELECT * FROM last_plod",null);
        if(c.moveToFirst())
            db.update("last_plod",newValues,null,null);
        else
            db.insert("last_plod",null,newValues);
    }
    public String getLastPlod(SQLiteDatabase db){
        Cursor c = db.rawQuery("SELECT * FROM last_plod",null);
        if(c.moveToFirst())
            return c.getString(c.getColumnIndex("plod"));
        else return null;
    }
    public void setPlod(SQLiteDatabase db, String plod){
        ContentValues newValues = new ContentValues();
        newValues.put("plod",plod);
        db.insert("plod_list",null,newValues);
    }

    public boolean isValidExit(SQLiteDatabase db,String barcode){
        Cursor c = db.rawQuery("SELECT * FROM template",null);
        if(c.moveToFirst()){
            String s = c.getString(c.getColumnIndex("template"));
            if(s.length()!=barcode.length())
                return false;
            for(int i = 0;i<s.length();i++){
                if(s.charAt(i)!='?' && s.charAt(i)!=barcode.charAt(i))
                    return false;
            }

        }
        return true;
    }
    public void deletePlod(SQLiteDatabase db,String plod){
        Cursor c = db.rawQuery("SELECT * FROM plod_list WHERE plod = '"+ plod + "'",null);
        if(c.moveToFirst()){
            Cursor c1 = db.rawQuery("SELECT * FROM PLODS WHERE plod = '"+ plod + "'",null);
            if(c1.moveToFirst()){
                int id = c1.getInt(c.getColumnIndex("id"));
                db.delete("Bottles", "boxId = " + id, null);
                db.delete("PLODS","plod = '" + plod+"'",null);
                db.delete("plod_list","plod = '" + plod+"'",null);
            }
            else{
                db.delete("plod_list","plod = '" + plod+"'",null);
            }
        }

    }

    public String getPlodName(SQLiteDatabase db,String id){
        if(id==null)
            return null;
        Cursor c = db.rawQuery("SELECT * FROM PLODS WHERE id = '" +Integer.parseInt(id) + "'",null);
        if(c.moveToFirst()){
            return c.getString(c.getColumnIndex("plod"));
        }
        else  return null;
    }
    public String getLpb(SQLiteDatabase db, int id){
        Cursor c = db.rawQuery("SELECT * FROM PLODS WHERE id = '" +id + "'",null);
        if(c.moveToFirst()){
            return c.getString(c.getColumnIndex("LPB"));
        }
        else  return null;
    }
    public int getId(SQLiteDatabase db,String lpb){
        Cursor c = db.rawQuery("SELECT * FROM PLODS WHERE LPB = '" + lpb +"'",null);
        if(c.moveToFirst()){
            return c.getInt(c.getColumnIndex("id"));
        }
        else return -1;
    }

    public boolean isHavePlod(SQLiteDatabase db,String plod){
        Cursor c = db.rawQuery("SELECT * FROM plod_list WHERE plod = '"+ plod+"'",null);
        return c.moveToFirst();
    }
    public String getFileName(SQLiteDatabase db){

        Cursor c = db.rawQuery("SELECT * FROM PLODS WHERE LPB = 0",null);
        if(c.moveToFirst()){
            return  c.getString(c.getColumnIndex("plod"));
        }
        else return  null;
    }
    public String getTemplateName(SQLiteDatabase db){
        Cursor c = db.rawQuery("SELECT * FROM template",null);
        if(c.moveToFirst()){
            return  c.getString(c.getColumnIndex("template"));
        }
        else return  null;
    }
    public void setTemplateName(SQLiteDatabase db,String template){
        ContentValues newValues = new ContentValues();
        Cursor c = db.rawQuery("SELECT * FROM template",null);
        if(c.moveToFirst()) {
            newValues.put("template", template);
            db.update("template", newValues, null, null);
        }
    }
    public void setFileName(SQLiteDatabase db,String fileName){
        ContentValues newValues = new ContentValues();
        Cursor c = db.rawQuery("SELECT * FROM FILES",null);
        if(c.moveToFirst()) {
            newValues.put("fileName", fileName);
            String where = "fileName = 0";
            db.update("FILES", newValues, where, null);
        }
        else {
            newValues.put("fileName",fileName);
            db.insert("FILES",null,newValues);
        }
    }
    public void delFileName(SQLiteDatabase db){
        ContentValues newValues = new ContentValues();
        newValues.put("fileName","0");
        db.update("FILES",newValues,null,null);
    }

    public void addBox(String ean,String plod,String first_col,String second_col,String third_col,SQLiteDatabase db){
        ContentValues newValues = new ContentValues();
        newValues.put("EAN",ean);
        newValues.put("first_column",first_col);
        newValues.put("second_column",second_col);
        newValues.put("third_column",third_col);
        newValues.put("fullness",0);
        newValues.put("LPB",0);
        newValues.put("plod",plod);
        db.insert("PLODS",null,newValues);

    }

    public void deleteBox(SQLiteDatabase db,int boxId){
        db.delete("Bottles", "boxId = '" + boxId+"'", null);
        db.delete("PLODS", "id = '" + boxId +"'", null);
        setFileName(db,"0");
    }
    public int isOpenBox(SQLiteDatabase db){
        Cursor c = db.rawQuery("SELECT * FROM PLODS WHERE LPB = 0",null);

        if(c.moveToFirst()){
            int i = c.getInt(c.getColumnIndex("id"));
            if(getBottles(db,i)!=null) {
                c.close();
                return i;
            }
        }
        c.close();
        return -1;
    }

    public void exitBox(String lpb,SQLiteDatabase db,int count,int multiply){

        ContentValues newValues = new ContentValues();

        newValues.put("fullness",multiply);
        newValues.put("count",count);
        newValues.put("LPB",lpb);
        String where  = "LPB = 0";
        db.update("PLODS",newValues,where,null);

    }
    public boolean isUnicLPB(String lpb , SQLiteDatabase db){
        Cursor c = db.rawQuery("SELECT * FROM PLODS WHERE LPB = '"+lpb+"'",null);
        return !c.moveToFirst();
    }
    public void setQRBottle(SQLiteDatabase bd, String QR){
        Cursor cursor = bd.rawQuery("SELECT * FROM Bottles WHERE QR = 0",null);
        ContentValues newValues = new ContentValues();
        if(cursor.moveToFirst()) {
            newValues.put("QR",QR);
            String where  = "QR = 0";
            bd.update("Bottles",newValues,where,null);
            cursor.close();
            cursor = bd.rawQuery("SELECT * FROM PLODS WHERE LPB = 0",null);
        }
        else {
            newValues.put("QR",QR);
            newValues.put("PDF417","0");
            newValues.put("BoxId",getId(bd));
            bd.insert("Bottles",null,newValues);
        }
        cursor.close();
    }
    public boolean canQR(SQLiteDatabase db,String qr){
        Cursor cursor = db.rawQuery("SELECT * FROM Bottles WHERE QR = '"+qr+"'",null);
        return !cursor.moveToFirst();
    }
    public boolean canPDF(SQLiteDatabase db,String pdf417){
        Cursor cursor = db.rawQuery("SELECT * FROM Bottles WHERE PDF417 = '"+pdf417+"'",null);
        return !cursor.moveToFirst();
    }
    public void changeQR(SQLiteDatabase db,String QR){
        Cursor cursor = db.rawQuery("SELECT * FROM Bottles WHERE PDF417 = 0",null);
        ContentValues newValues = new ContentValues();
        if(cursor.moveToFirst()){
            newValues.put("QR",QR);
            String where  = "PDF417 = 0";
            db.update("Bottles",newValues,where,null);
            cursor.close();
        }
    }
    public void changePDF417(SQLiteDatabase db,String PDF417){
        Cursor cursor = db.rawQuery("SELECT * FROM Bottles WHERE QR = 0",null);
        ContentValues newValues = new ContentValues();
        if(cursor.moveToFirst()){
            newValues.put("PDF417",PDF417);
            String where  = "QR = 0";
            db.update("Bottles",newValues,where,null);
            cursor.close();
        }
    }
    public int getId(SQLiteDatabase db){
        Cursor cursor = db.rawQuery("SELECT * FROM PLODS WHERE LPB = 0",null);
        if(cursor.moveToFirst()){
            return cursor.getInt(cursor.getColumnIndex("id"));
        }
        return -1;
    }
    public void setPDF417Bottle(SQLiteDatabase bd, String PDF417){
        Cursor cursor = bd.rawQuery("SELECT * FROM Bottles WHERE PDF417 = 0",null);
        ContentValues newValues = new ContentValues();
        if(cursor.moveToFirst()) {
            newValues.put("PDF417",PDF417);
            String where  = "PDF417 = 0";
            bd.update("Bottles",newValues,where,null);
            cursor.close();
            cursor = bd.rawQuery("SELECT * FROM PLODS WHERE LPB = 0",null);
        }
        else {
            newValues.put("QR","0");
            newValues.put("PDF417",PDF417);
            newValues.put("BoxId",getId(bd));
            bd.insert("Bottles",null,newValues);
        }
        cursor.close();
    }
    public void changeEAN(SQLiteDatabase bd,String EAN){
        Cursor cursor = bd.rawQuery("SELECT * FROM PLODS WHERE LPB = 0",null);
        if(cursor.moveToFirst()) {

            ContentValues newValues = new ContentValues();
            newValues.put("EAN",EAN);
            String where  = "LPB = 0";
            bd.update("PLODS",newValues,where,null);
            cursor.close();
        }
    }

    public String getLastEAN(SQLiteDatabase bd,int id){
        if(getId(bd) == -1)
            return "";
        else {
            Cursor cursor = bd.rawQuery("SELECT * FROM PLODS WHERE id = '"+ id+"'",null);
            cursor.moveToFirst();
            String s = cursor.getString(cursor.getColumnIndex("EAN"));
            cursor.close();
            return s;
        }
    }

    public ArrayList<Boxes> getBoxes(SQLiteDatabase bd){
        ArrayList<Boxes> boxes = new ArrayList<>();
        int i = 0;
        Cursor cursor = bd.rawQuery("SELECT * FROM PLODS WHERE LPB != 0",null);
        if(cursor.moveToFirst()) {
            int count = cursor.getColumnIndex("count");
            int lpb = cursor.getColumnIndex("LPB");
            int id = cursor.getColumnIndex("id");
            do {
                boxes.add(new Boxes());
                boxes.get(i).id = Integer.parseInt(cursor.getString(id));
                boxes.get(i).LPB = cursor.getString(lpb);
                boxes.get(i).count = cursor.getInt(count);
                i++;
            }while (cursor.moveToNext());
            cursor.close();
            return boxes;
        }
        cursor.close();
        return null;
    }
    public ArrayList<Boxes> getBoxes(String plod,SQLiteDatabase bd){
        ArrayList<Boxes> boxes = new ArrayList<>();
        int i = 0;
        Cursor cursor = bd.rawQuery("SELECT * FROM PLODS WHERE LPB != 0 AND plod = '"+plod+"'",null);
        if(cursor.moveToFirst()) {
            int count = cursor.getColumnIndex("count");
            int lpb = cursor.getColumnIndex("LPB");
            int id = cursor.getColumnIndex("id");
            int first_col = cursor.getColumnIndex("first_column");
            int sec_col = cursor.getColumnIndex("second_column");
            int third_col = cursor.getColumnIndex("third_column");
            int full = cursor.getColumnIndex("fullness");
            do {
                boxes.add(new Boxes());
                boxes.get(i).id = Integer.parseInt(cursor.getString(id));
                boxes.get(i).LPB = cursor.getString(lpb);
                boxes.get(i).fullness = cursor.getString(full);
                boxes.get(i).count = cursor.getInt(count);
                boxes.get(i).first_col = cursor.getString(first_col);
                boxes.get(i).sec_col = cursor.getString(sec_col);
                boxes.get(i).third_col = cursor.getString(third_col);
                i++;
            }while (cursor.moveToNext());
            return boxes;
        }
        cursor.close();
        return null;
    }

    public Box getBottles(SQLiteDatabase bd, int id){

        Cursor cursor = bd.rawQuery("SELECT * FROM Bottles WHERE boxId = '"+id +"'",null);
        Box box = new Box();
        String qrs,pdfs;
        int i = 0;
        if(cursor.moveToFirst()) {
            int qr = cursor.getColumnIndex("QR");
            int pdf417 = cursor.getColumnIndex("PDF417");
            do {
                qrs = cursor.getString(qr);
                pdfs = cursor.getString(pdf417);
                if((!qrs.equals("0")) && (!pdfs.equals("0"))) {
                    box.bottles.add(new Bottle());
                    box.bottles.get(i).qr = cursor.getString(qr);
                    box.bottles.get(i).pdf417 = cursor.getString(pdf417);
                    i++;
                }
            }while (cursor.moveToNext());
            return box;
        }
        cursor.close();
        return null;

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {


    }
}
