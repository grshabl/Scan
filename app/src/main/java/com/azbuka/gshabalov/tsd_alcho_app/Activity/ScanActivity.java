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
import android.widget.TextView;
import android.widget.Toast;


import com.azbuka.gshabalov.tsd_alcho_app.BaseActivity;
import com.azbuka.gshabalov.tsd_alcho_app.R;
import com.azbuka.gshabalov.tsd_alcho_app.utils.Database;
import com.azbuka.gshabalov.tsd_alcho_app.utils.ReadDatabase;
import com.azbuka.gshabalov.tsd_alcho_app.utils.ScanDatabase;

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
    public TextView description, qrCode, pdf417Code, boxCount;
    ReadDatabase readDatabase;
    ScanDatabase scandatabase;
    Database proddatabase;
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

    String[] pole = {ReadDatabase.STARTNUM, ReadDatabase.ENDNUM};

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

        data = new String[7];
        scandatabase = new ScanDatabase(this);
        proddatabase = new Database(this);
        sqanBase = scandatabase.getWritableDatabase();
        prodBase = proddatabase.getWritableDatabase();
        contentValues = new ContentValues();
        readDatabase = new ReadDatabase(this);
        readBase = readDatabase.getWritableDatabase();

        qrCode = findViewById(R.id.qrcode);
        pdf417Code = findViewById(R.id.pdf417code);
        description = findViewById(R.id.description);


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
            int start = cursor.getColumnIndex(ReadDatabase.STARTNUM);
            int end = cursor.getColumnIndex(ReadDatabase.ENDNUM);
            do {
                if (cursor.getString(start).hashCode() < qr.hashCode() && cursor.getString(end).hashCode() > qr.hashCode()){
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putString(ReadDatabase.STARTNUM, cursor.getString(start));
                    ed.putString(ReadDatabase.ENDNUM, cursor.getString(end));
                    ed.apply();
                    include = true;
                }
            } while (cursor.moveToNext());
        }
        return include;
    }

    private Cursor inPlod(String ean) {
        return readBase.query(ReadDatabase.DATABASE_NAME, pole, ReadDatabase.BOX_EAN + " = '" + ean + "' and " + ReadDatabase.PLOD + " ='" + plod + "'", null, null, null, null);
    }

    private void addBottleInDB() {
        contentValues.put(ScanDatabase.PLOD, plod);
        contentValues.put(ScanDatabase.PLOD_LINE, plodLine);
        contentValues.put(ScanDatabase.GOODS_CODE, goodsCode);
        contentValues.put(ScanDatabase.QR, data[1]);
        contentValues.put(ScanDatabase.PDF417, data[2]);
        contentValues.put(ScanDatabase.MARK_BAD, markbad);
        contentValues.put(ScanDatabase.BOX_EAN, boxean);
        contentValues.put(ScanDatabase.MULTIPLICITY, multiplicity);

        sqanBase.insert(ScanDatabase.DATABASE_NAME, null, contentValues);
        contentValues.clear();

    }

    public class BarScan {
        private void chooseType(String scanStr) {
            if (scanStr.length() == 13 && data[0] == null && checkInPlod(scanStr)) {
                data[0] = scanStr;
            } else {
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
                        } else {
                            if (Integer.parseInt(scanStr) >= start && Integer.parseInt(scanStr) <= end) {
                                data[1] = errorQR(scanStr); //QR hand
                                qrCode.setText(qrDestroy(scanStr));
                            }
                        }
                        break;

                    case 33:
                        if (scanStr.equals(data[1])) {
                            Alert("QR уже был просканирован");
                        } else if (data[0] == null) {
                            Alert("Необходимо просканировать ШК бутылки");
                        } else {
                            if (checkQR(scanStr.substring(7,15))) {
                                data[1] = scanStr; //QR scan
                                qrCode.setText(qrDestroy(scanStr.substring(4, 7) + " " + scanStr.substring(7, 15)));
                            }
                        }
                        break;

                    case 68:
                        data[2] = scanStr; //PDF
                        pdf417Code.setText(scanStr);
                        break;

                    default:
                        Alert("Неизвестный код");
                        break;
                }
            }
            checkFull();
        }
    }

    private void setMultiplicityError() {
        sqanBase.execSQL(String.format("Update %s set %s = '1'", ScanDatabase.DATABASE_NAME, ScanDatabase.MULTIPLICITY));
    }

    private String errorQR(String QR) {
        return "ХХХ-" + QR + "ХХХХХХХХХХХХХХХХХ";
    }


    public Boolean checkScan() {
        return sqanBase.query(ScanDatabase.DATABASE_NAME, null, null, null, null, null, null).getCount() > 0;
    }

    private void clearbase() {
        sqanBase.delete(ScanDatabase.DATABASE_NAME, null, null);
    }


    private void checkFull() {
        if(data[0] != null) {
            int goodsname = cursor.getColumnIndex(ReadDatabase.GOODS_NAME);
            String description = cursor.getString(goodsname);
            this.description.setText(description);
        }
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

    private void seletStart() {

    }

    private void seletEnd() {

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


        }
    }

    private String qrDestroy(String qr) {
        return qr.substring(0, 2) + " " + qr.substring(4, 14);
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
