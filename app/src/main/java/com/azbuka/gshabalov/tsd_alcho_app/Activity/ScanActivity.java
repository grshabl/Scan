package com.azbuka.gshabalov.tsd_alcho_app.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.azbuka.gshabalov.tsd_alcho_app.BaseActivity;
import com.azbuka.gshabalov.tsd_alcho_app.R;
import com.azbuka.gshabalov.tsd_alcho_app.utils.Database;

import device.scanner.DecodeResult;
import device.scanner.IScannerService;
import device.scanner.ScannerService;


public class ScanActivity extends BaseActivity {
    private static IScannerService iScanner = null;
    private static DecodeResult mDecodeResult = new DecodeResult();
    public static String barCode = null;
    public static BarScan barScan;
    private String[] data; // EAN, QR, PDF
    private TextView description, qrCode, pdf417Code, boxCount;
    private EditText code;
    private Button entercode;

    Database database;
    SQLiteDatabase readBase;
    ContentValues contentValues;
    Cursor cursor;
    SharedPreferences sPref;

    private Activity activity;

    private String goodsname = "";
    private String plod = "";
    private String plodLine;
    private String goodsCode;
    private String markbad;
    private String boxean;
    private String lpb;
    private String multiplicity;
    private Boolean scanenable = true;
    private String mult ="0";

    String[] pole = {Database.STARTNUM, Database.ENDNUM, Database.GOODS_NAME, Database.GOODS_CODE, Database.PLOD_LINE, Database.MULTIPLICITY};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        entercode = findViewById(R.id.enterCode);
        activity = this;
        barScan = new BarScan();
        sPref = getSharedPreferences("DataShared", MODE_PRIVATE);
        plod = sPref.getString(Database.PLOD, "");
        boxean = sPref.getString(Database.BOX_EAN, "");
        database = new Database(this);
        readBase = database.getWritableDatabase();
        description = findViewById(R.id.description);
        data = new String[3];
        if(mult.equals("0") && boxcount()!=0){
            Cursor c = readBase.rawQuery("SELECT * FROM "+Database.DATABASE_SCAN,null);
            if(c.moveToFirst()){
                mult = c.getString(c.getColumnIndex(Database.GOODS_MULTI));
            }
        }

        if (boxean != null && !boxean.equals("")) {
            data[0] = boxean;
            checkInPlod(boxean);
//            description.setText(sPref.getString(Database.GOODS_NAME, ""));
        }

        PackageManager pm = ScanActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(ScanActivity.this, ScanResultReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        contentValues = new ContentValues();
        qrCode = findViewById(R.id.qrcode);
        pdf417Code = findViewById(R.id.pdf417code);
        boxCount = findViewById(R.id.boxCount);
        boxCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScanActivity.this, ScanBoxViewActivity.class);
                startActivity(intent);
            }
        });

        entercode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barScan.chooseType(code.getText().toString());

            }
        });

        code = findViewById(R.id.code);
        multiplicity = "0";
        markbad = "0";
        boxCount.setText(boxcount().toString());
        try {
            initScanner();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private Boolean checkInPlod(String ean) {
        cursor = inPlod(ean);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(Database.GOODS_NAME));
            description.setText(name);
            saveText(name, ean);
            boxean = ean;
            return true;
        } else {
            description.setText("");
            if(!cursor.isClosed()){
                cursor.close();
            }
            boxean = "";
            Alert("Данного товара нет в выбранном PLOD");
            return false;
        }
    }

    private Integer boxcount() {
        return readBase.query(Database.DATABASE_SCAN, null, null, null, null, null, null, null).getCount();
    }

    private boolean checkQR(String qr) {
        boolean include = false;
        if (cursor.moveToFirst()) {
            int start = cursor.getColumnIndex(Database.STARTNUM);
            int end = cursor.getColumnIndex(Database.ENDNUM);
            int plodline = cursor.getColumnIndex(Database.PLOD_LINE);
            int goodscode = cursor.getColumnIndex(Database.GOODS_CODE);
            int multiplicity = cursor.getColumnIndex(Database.MULTIPLICITY);
            do {
                if (cursor.getString(start).hashCode() <= qr.hashCode() && cursor.getString(end).hashCode() >= qr.hashCode()) {
                    this.plodLine = cursor.getString(plodline);
                    this.goodsCode = cursor.getString(goodscode);
                    this.multiplicity = cursor.getString(multiplicity);
                    this.mult = this.multiplicity;
                    return true;
                }
            } while (cursor.moveToNext());
        }
        return include;
    }

    private Cursor inPlod(String ean) {
        boxean = ean;
        String[] args = {ean, plod};
        return readBase.query(Database.DATABASE_READ,
                              pole,
                              Database.BOX_EAN + " = ? and " + Database.PLOD + " = ?",
                              args,
                              null,
                              null,
                              null);
    }

    private void addBottleInDB() {
        cursor.getColumnIndex(Database.PLOD_LINE);
        contentValues.put(Database.PLOD, plod);
        contentValues.put(Database.PLOD_LINE, plodLine);
        contentValues.put(Database.GOODS_CODE, goodsCode);
        contentValues.put(Database.QR, data[1]);
        contentValues.put(Database.PDF417, data[2]);
        contentValues.put(Database.MARK_BAD, markbad);
        contentValues.put(Database.BOX_EAN, lpb);
        contentValues.put(Database.MULTIPLICITY, multiplicity);
        contentValues.put(Database.GOODS_MULTI,mult);
        readBase.insert(Database.DATABASE_SCAN, null, contentValues);
        boxCount.setText(boxcount().toString());
        contentValues.clear();

    }

    private boolean lpb(String str){
        String template = sPref.getString(Database.GOODS_LPB, "LPB-??????????");
        String startwth = template.split("-")[0];
        return (str.startsWith(startwth) && str.length() == template.length());
    }

    public class BarScan {
        private void chooseType(String scanStr) {
            code.setText("");
            hideKeyboard(activity);
            if (lpb(scanStr)) {
                String[] multi = mult.split("~");
                boolean flag = false;
                if(mult.equals("0"))
                    flag = true;
                for(String mult: multi){
                    if(boxcount() == Integer.parseInt(mult)){
                        flag = true;
                    }
                }
                if (!flag) {
                    chooseAlert(scanStr);
                } else if (data[1] == null && data[2] == null) {
                    if (hasAlreadyScanBoxWithCode(scanStr)) {
                        Alert("ШК коробки уже был просканирован");
                        return;
                    }
                    lpb = scanStr;
                    boxCount.setText("0");
//                    description.setText("");
                    readBase.execSQL(String.format("Update %s set %s = '0'", Database.DATABASE_SCAN, Database.MULTIPLICITY));
                    twoInOne(lpb);
//                    data[0] = null;
                } else {
                    Alert("Сначала закончите сканирование бутылки");
                }
            } else {
                if (scanenable) {
                    if(!mult.equals("0")) {
                        String[] multi = mult.split("~");
                        if(Integer.parseInt(multi[multi.length-1])<=boxcount()){
                            Alert("Было введено максималное количество бутылок");
                            return;
                        }
                    }
                    switch (scanStr.length()) {
                        case 13:
                            if (checkScan() && data[0] != null && boxcount()!=0) {
                                Alert("Сначала закройте коробку");
                            } else {
                                if (checkInPlod(scanStr)) {
                                    data[0] = scanStr; //EAN13
                                }
                            }
                            break;

                        case 11:
                            if (data[0] == null) {
                                Alert("Необходимо просканировать ШК бутылки");
                            } else if (errorQR(scanStr).equals(data[1]) || qrnew(errorQR(scanStr))) {
                                Alert("QR уже был считан");
                            } else {
                                if (checkQR(scanStr.substring(3, 11))) {
                                    data[1] = errorQR(scanStr); //QR scan
                                    qrDestroy(errorQR(scanStr));
                                } else {
                                    Alert("QR не входит в диапазоны марок");
                                }
                            }
                            break;

                        case 33:
                            if (data[0] == null) {
                                Alert("Необходимо просканировать ШК бутылки");
                            } else if (scanStr.equals(data[1]) || qrnew(scanStr)) {
                                Alert("QR уже был считан");
                            } else {

                                if (checkQR(scanStr.substring(7, 15))) {
                                    data[1] = scanStr; //QR scan
                                    qrDestroy(scanStr);
                                } else {
                                    Alert("QR не входит в диапазоны марок");
                                }
                            }
                            break;

                        case 68:
                            if (data[0] == null) {
                                Alert("Необходимо просканировать ШК бутылки");
                            } else if (scanStr.equals(data[2]) || isInputPDFWasScanned(scanStr)) {
                                Alert("PDF417 уже был считан");
                            } else {
                                data[2] = scanStr; //PDF
                                pdf417Code.setText("считан");
                            }
                            break;

                        default:
                            Alert("Неизвестный код");
                            break;
                    }

                }
                checkFull();
            }
        }
    }

    private boolean hasAlreadyScanBoxWithCode(String lpb) {
        String[] rows = { Database.GOODS_LPB };
        String[] args = { lpb };
        return readBase.query(Database.DATABASE_WRITE,
                              rows,
                              Database.GOODS_LPB + " = ?",
                              args,
                              null,
                              null,
                              null,
                              null).getCount() > 0;
    }

    private void setMultiplicityError() {
        readBase.execSQL(String.format("Update %s set %s = '1'", Database.DATABASE_SCAN, Database.MULTIPLICITY));
    }

    private String errorQR(String QR) {
        return "ХХХ-" + QR + "ХХХХХХХХХХХХХХХХХ";
    }


    public Boolean checkScan() {
        return readBase.query(Database.DATABASE_SCAN, null, null, null, null, null, null).getCount() > 0;
    }

    private void clearbase() {
        readBase.delete(Database.DATABASE_SCAN, null, null);
    }


    private void checkFull() {
        if (data[1] != null && data[2] != null && data[0] != null) {
            addBottleInDB();
            data[1] = null;
            data[2] = null;
            qrCode.setText("");
            pdf417Code.setText("");
        }
    }

    private boolean qrnew(String qr) {
        Cursor c = readBase.rawQuery("SELECT * FROM "+Database.DATABASE_SCAN,null);
        if(c.moveToFirst()){
            do {
                if(c.getString(4).equals(qr)) {
                    c.close();
                    return true;
                }
            }while (c.moveToNext());
        }
        c.close();
         c = readBase.rawQuery("SELECT * FROM "+Database.DATABASE_WRITE,null);
        if(c.moveToFirst()){
            do {
                if(c.getString(4).equals(qr)) {
                    c.close();
                    return true;
                }
            }while (c.moveToNext());
        }
        c.close();
        return false;
    }

    private boolean isInputPDFWasScanned(String pfd) {
        Cursor c = readBase.rawQuery("SELECT * FROM " + Database.DATABASE_SCAN,null);
        if (c.moveToFirst()){
            do {
                if(c.getString(5).equals(pfd)) {
                    c.close();
                    return true;
                }
            } while (c.moveToNext());
        }
        c.close();
        c = readBase.rawQuery("SELECT * FROM " + Database.DATABASE_WRITE,null);
        if(c.moveToFirst()){
            do {
                if(c.getString(5).equals(pfd)) {
                    c.close();
                    return true;
                }
            }while (c.moveToNext());
        }
        c.close();
        return false;
    }

    public void Alert(String msg) {
        scanenable = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
        builder.setTitle("Ошибка")
                .setMessage(msg)
                .setCancelable(false)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                scanenable = true;
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void chooseAlert(final String scanStr) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
        builder
                .setTitle("Внимание!")
                .setMessage("Количество просканированных бутылок (" + boxCount.getText().toString() + ") не соответсвует кратности (" + mult.replace("~", ".") + ") .Продолжить? ")
                .setCancelable(false)
                .setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        setMultiplicityError();
                        boxCount.setText("0");
//                        description.setText("");
                        lpb = scanStr;
//                        data[0] = null;
                        twoInOne(lpb);
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }


    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.erase:
                AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
                builder.setTitle("Внимание!").setMessage("Вы действительно хотите удалить введенные данные?").setCancelable(false).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        data[1] = null;
                        data[2] = null;
                        qrCode.setText("");
                        pdf417Code.setText("");
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

                break;

            case R.id.cantPdf:
                if(!mult.equals("0")) {
                    String[] multi = mult.split("~");
                    if(Integer.parseInt(multi[multi.length-1])<=boxcount()){
                        Alert("Было введено максималное количество бутылок");
                        break;
                    }
                }
                data[2] = "0";
                markbad = "1";
                pdf417Code.setText("Не считан");
                checkFull();
                break;


            case R.id.enterCode:
                barScan.chooseType(code.getText().toString());
                code.setText("");
                hideKeyboard(activity);
                break;


        }
    }
    private static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private void qrDestroy(String qr) {
        qrCode.setText(qr.substring(4, 7) + " " + qr.substring(7, 15));
    }

    private void twoInOne(String lpb) {
        data[1] = null;
        data[2] = null;
        qrCode.setText("");
        pdf417Code.setText("");

        Cursor c = readBase.rawQuery("SELECT * FROM " + Database.DATABASE_SCAN, null);
        ContentValues values;
        if (c.moveToFirst()) {
            do {
                values = new ContentValues();
                values.put(Database.PLOD, c.getString(1));
                values.put(Database.PLOD_LINE, c.getString(2));
                values.put(Database.GOODS_CODE, c.getString(3));
                values.put(Database.QR, c.getString(4));
                values.put(Database.PDF417, c.getString(5));
                values.put(Database.MARK_BAD, c.getString(6));
                values.put(Database.BOX_EAN, c.getString(7));
                values.put(Database.GOODS_LPB, lpb);
                values.put(Database.MULTIPLICITY, c.getString(9));
                readBase.insert(Database.DATABASE_WRITE, null, values);
            } while (c.moveToNext());
        }
        readBase.delete(Database.DATABASE_SCAN, null, null);
        c.close();
    }

    @Override
    public void onBackPressed() {
        if (checkScan()) {
            Intent intent = new Intent(this, StartMenu.class);
            this.startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, ChoosePlod.class);
            this.startActivity(intent);
            finish();
        }

    }


    public static class ScanResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (iScanner != null) try {
                mDecodeResult.recycle();
                iScanner.aDecodeGetResult(mDecodeResult);
                barCode = mDecodeResult.toString();
                if (barCode.equals("READ_FAIL"))
                    Toast.makeText(context, barCode, Toast.LENGTH_LONG).show();
                else
                    barScan.chooseType(barCode);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void initScanner() throws RemoteException {
        iScanner = IScannerService.Stub.asInterface(ServiceManager
                .getService("ScannerService"));
        if (iScanner != null) {
            try {
                iScanner.aDecodeSetBeepEnable(1);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
            iScanner.aDecodeAPIInit();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            iScanner.aDecodeSetDecodeEnable(1);
            iScanner.aDecodeSetResultType(ScannerService.ResultType.DCD_RESULT_USERMSG);
        }
    }

    void saveText(String name, String boxean) {
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(Database.GOODS_NAME, name);
        ed.putString(Database.BOX_EAN, boxean);
        ed.apply();
    }

    @Override
    protected void onResume() {
        boxCount.setText(boxcount().toString());
        super.onResume();
    }

    @Override
    protected void onRestart() {
        boxCount.setText(boxcount().toString());
        super.onRestart();
    }
    @Override
    protected void onStart(){
        if(mult.equals("0") && boxcount()!=0){
            Cursor c = readBase.rawQuery("SELECT * FROM "+Database.DATABASE_SCAN,null);
            if(c.moveToFirst()){
                mult = c.getString(c.getColumnIndex(Database.GOODS_MULTI));
            }
        }
        boxean = sPref.getString(Database.BOX_EAN, "");
        if (boxean != null && !boxean.equals("")) {
            data[0] = boxean;
            checkInPlod(boxean);
//            description.setText(sPref.getString(Database.GOODS_NAME, ""));
        }
        super.onStart();

    }
    @Override
    protected void onStop() {
        if (cursor != null) cursor.close();
        super.onStop();
    }

}
