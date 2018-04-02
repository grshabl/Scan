package com.azbuka.gshabalov.tsd_alcho_app.Activity;

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
    SharedPreferences sPref;
    private String[] data; // EAN, QR, PDF
    private TextView description, qrCode, pdf417Code, boxCount;
    private EditText code;
    Database database;
    SQLiteDatabase sqanBase;
    SQLiteDatabase prodBase;
    SQLiteDatabase readBase;
    ContentValues contentValues;
    Cursor cursor;

    private String goodsname = "";
    private String plod = "";
    private String plodLine;
    private String goodsCode;
    private String markbad;
    private String boxean;
    private Integer start;
    private Integer end;
    private String multiplicity;

    String[] pole = {Database.STARTNUM, Database.ENDNUM};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        barScan = new BarScan();
        sPref = getSharedPreferences("DataShared", MODE_PRIVATE);
        plod = sPref.getString(Database.PLOD, "");

        PackageManager pm = ScanActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(ScanActivity.this, ScanResultReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        database = new Database(this);
        readBase = database.getWritableDatabase();

        contentValues = new ContentValues();
        qrCode = findViewById(R.id.qrcode);
        pdf417Code = findViewById(R.id.pdf417code);
        description = findViewById(R.id.description);
        code = findViewById(R.id.code);

        data = new String[8];
        data[7] = "0";

        try {
            initScanner();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private Boolean checkInPlod(String ean) {
        cursor = inPlod(ean);
        if (cursor.moveToFirst()) {
            return true;
        } else {
            Alert("Данного товара нет в выбранном PLOD");
            return false;
        }
    }

    private boolean checkQR(String qr) {
        Boolean include = false;
        if (cursor.moveToFirst()) {
            int start = cursor.getColumnIndex(Database.STARTNUM);
            int end = cursor.getColumnIndex(Database.ENDNUM);
            do {
                if (cursor.getString(start).hashCode() < qr.hashCode() && cursor.getString(end).hashCode() > qr.hashCode()) {
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putString(Database.STARTNUM, cursor.getString(start));
                    ed.putString(Database.ENDNUM, cursor.getString(end));
                    ed.apply();
                    include = true;
                }
            } while (cursor.moveToNext());
        }
        return include;
    }

    private Cursor inPlod(String ean) {
        return readBase.query(Database.DATABASE_READ, pole, Database.BOX_EAN + " = '" + ean + "' and " + Database.PLOD + " ='" + plod + "'", null, null, null, null);
    }

    private void addBottleInDB() {
        contentValues.put(Database.PLOD, plod);
        contentValues.put(Database.PLOD_LINE, plodLine);
        contentValues.put(Database.GOODS_CODE, goodsCode);
        contentValues.put(Database.QR, data[1]);
        contentValues.put(Database.PDF417, data[2]);
        contentValues.put(Database.MARK_BAD, markbad);
        contentValues.put(Database.BOX_EAN, boxean);
        contentValues.put(Database.MULTIPLICITY, multiplicity);

        sqanBase.insert(Database.DATABASE_SCAN, null, contentValues);
        contentValues.clear();

    }

    public class BarScan {
        private void chooseType(String scanStr) {
            switch (scanStr.length()) {
                case 13:
                    if (checkScan()) {
                        Alert("Сначала закройте коробку");
                    } else {
                        if (checkInPlod(scanStr)) {
                            data[0] = scanStr; //EAN13
                        }
                    }
                    break;

                case 11:
                    if (scanStr.equals(data[1])) {
                        Alert("QR уже был просканирован");
                    } else if (data[0] != null) {
                        if (checkQR(scanStr.substring(7, 15))) {
                            data[1] = errorQR(scanStr); //QR hand
                            qrDestroy(scanStr);
                        } else {
                            Alert("QR не входит в диапазоны марок");
                        }
                    } else {
                        Alert("Необходимо просканировать ШК бутылки");
                    }
                    break;

                case 33:
                    if (scanStr.equals(data[1])) {
                        Alert("QR уже был считан");
                    } else if (data[0] == null) {
                        Alert("Необходимо просканировать ШК бутылки");
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
                    if (data[0] != null) {
                        data[2] = scanStr; //PDF
                        pdf417Code.setText("считан");
                    } else {
                        Alert("Необходимо просканировать ШК бутылки");
                    }
                    break;

                default:
                    Alert("Неизвестный код");
                    break;
            }

            checkFull();
        }
    }

    private void setMultiplicityError() {
        sqanBase.execSQL(String.format("Update %s set %s = '1'", Database.DATABASE_SCAN, Database.MULTIPLICITY));
    }

    private String errorQR(String QR) {
        return "ХХХ-" + QR + "ХХХХХХХХХХХХХХХХХ";
    }


    public Boolean checkScan() {
        return readBase.query(Database.DATABASE_SCAN, null, null, null, null, null, null).getCount() > 0;
    }

    private void clearbase() {
        sqanBase.delete(Database.DATABASE_NAME, null, null);
    }


    private void checkFull() {
        if (data[0] != null && data[1] != null && data[2] != null && data[3] != null && data[4] != null && data[5] != null && data[6] != null) {
            addBottleInDB();
            data[0] = null;
            data[1] = null;
            data[2] = null;
            data[3] = null;
            data[4] = null;
            data[5] = null;
            data[6] = null;
        }
    }


    public void Alert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
        builder.setTitle("Ошибка")
                .setMessage(msg)
                .setCancelable(false)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private String getBarcode() {
        String result = null;
        if (barCode != null) {
            result = barCode;
        }
        barCode = null;
        return result;
    }

    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.erase:
                data[0] = null;
                data[1] = null;
                data[2] = null;
                qrCode.setText("");
                pdf417Code.setText("");
                break;

            case R.id.cantPdf:
                data[2] = "0";
                pdf417Code.setText("Не считан");
                break;


            case R.id.count:
                // открытие коробки
                break;

            case R.id.enterCode:
                barScan.chooseType(code.getText().toString());
                break;


        }
    }

    private void qrDestroy(String qr) {
        qrCode.setText(qr.substring(4, 7) + " " + qr.substring(7, 15));
    }

    @Override
    public void onBackPressed() {
        if (checkScan()) {
            Intent intent = new Intent(this, StartMenu.class);
            this.startActivity(intent);
        } else {
            Intent intent = new Intent(this, ChoosePlod.class);
            this.startActivity(intent);
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

}
