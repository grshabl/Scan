package ru.av.test.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import device.scanner.DecodeResult;
import device.scanner.IScannerService;
import device.scanner.ScannerService;
import ru.av.test.Data.Box;
import ru.av.test.Data.BoxSingleton;
import ru.av.test.MainActivity;
import ru.av.test.R;
import ru.av.test.util.DBUsing;

import static ru.av.test.Data.BoxSingleton.helper2;


public class ScannerActivity extends Activity {
    private static IScannerService iScanner = null;

    private static DecodeResult mDecodeResult = new DecodeResult();
    private static String barCode = null;
    public static String ean;
    private static int index = -1;
    private static ArrayList<Integer> indexes = null;
    private static Context scannerContext = null;
    private DBUsing dbUsing;
    public static int plodFileName = -1;
    private BoxSingleton instance;
    public static TextView description, qrCode, pdf417Code, boxCount;
    SQLiteDatabase db;
    private static BarCodeRes barCodeRes;
    boolean enable = true;

    public static class ScanResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (iScanner != null) try {
                mDecodeResult.recycle();
                iScanner.aDecodeGetResult(mDecodeResult);
                barCode = mDecodeResult.toString(); /*расшифровка*/
                if (barCode.equals("READ_FAIL"))
                    Toast.makeText(context, "Просканируй еще раз", Toast.LENGTH_LONG).show();
                else barCodeRes.setBarCode();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public class BarCodeRes {

        public void setBarCode() {
            if(barCode.length()!=14 && barCode.length()!= 13 && instance.getMaxCount()==instance.getCount())
                alert("Было введено максимально допустимое количество бутылок");
            else if (barCode.length() == 13)
                setEan(barCode);
            else if (barCode.length() == 11 || barCode.length() == 33)
                setQR(barCode);
            else if (barCode.length() == 68)
                setPDF417(barCode);
            else if (barCode.length() == 14)
                exitBox(barCode);
            else {
                alert("Просканируйте другой шк или попробуйте ещё раз");
            }

        }

        public void setEan(String barcode) {
            if (barcode == null)
                return;
            if (instance.isOpened() == 2) {
                alert("Сначала закройте коробку");
                return;
            }
            indexes = helper2.getIndex(barcode);
            if (indexes.size() == 0) {
                alert("Товар не содержиться в данном PLOD");
                return;
            }
            if (indexes.size() == 1) {
                instance.setEAN(barcode);
                index = indexes.get(0);
                String[] s = helper2.getMultiplicity(index).split("~");
                instance.setMaxCount(Integer.parseInt(s[s.length-1]));
            }
            else index = -1;
            if (instance.isOpened() == 0) {
                setText();
                instance.setEAN(barcode);

                instance.setOpened(1);
                if (!dbUsing.isHavePlod(db, helper2.getColumn(indexes.get(0), 0)))
                    dbUsing.setPlod(db, helper2.getColumn(indexes.get(0), 0));
                dbUsing.setFileName(db, helper2.getColumn(indexes.get(0), 0));
                dbUsing.addBox(barCode, helper2.getColumn(indexes.get(0), 0), helper2.getColumn(indexes.get(0), 0), helper2.getColumn(indexes.get(0), 2), helper2.getColumn(indexes.get(0), 3), db);
            } else if (instance.isOpened() == 1) {
                setText();
                instance.setEAN(barcode);
                dbUsing.changeEAN(db, barcode);
                if (!dbUsing.isHavePlod(db, helper2.getColumn(indexes.get(0), 0)))
                    dbUsing.setPlod(db, helper2.getColumn(indexes.get(0), 0));
                dbUsing.setFileName(db, helper2.getColumn(indexes.get(0), 0));
            }

        }

        public void setQR(String qr) {
            if (qr == null)
                return;
            if (instance.isOpened() == 0) {
                alert("Сначала просканируйте ШК бутылки");
                return;
            }
            if (qr.length() == 11) {
                qr = "XXX-" + qr + "ХХХХХХХХХХХХХХХХХ";
            }
            if (index == -1) {
                for (int i : indexes) {
                    if (qr.substring(7, 15).hashCode() <= helper2.getLtMark(i).hashCode() &&
                            qr.substring(7, 15).hashCode() >= helper2.getStMark(i).hashCode() &&
                            qr.substring(4, 7).hashCode() == helper2.getMart(i).hashCode()) {
                        index = i;
                        String[] s = helper2.getMultiplicity(index).split("~");
                        instance.setMaxCount(Integer.parseInt(s[s.length-1]));
                        if (dbUsing.canQR(db, qr)) {
                            if (instance.isQr()) {
                                instance.setOpened(2);
                                instance.setQr();
                                qrCode.setText(qr.substring(4, 7)+" "+qr.substring(7,15));
                                dbUsing.changeQR(db, qr);
                                return;
                            }
                            instance.setQr();
                            instance.setOpened(2);
                            qrCode.setText(qr.substring(4, 7)+" "+qr.substring(7,15));
                            dbUsing.setQRBottle(db, qr);

                        } else {
                            alert("QR уже был считан");
                            return;
                        }
                    }
                }
                if (index == -1) {
                    alert("QR не входит в диапозоны");
                    return;
                }
            }
            if (!dbUsing.canQR(db, qr)) {
                alert("QR уже был считан");
                return;
            }
            if (qr.substring(7, 15).hashCode() <= helper2.getLtMark(index).hashCode() && qr.substring(7, 15).hashCode() >= helper2.getStMark(index).hashCode() && qr.substring(4, 7).hashCode() == helper2.getMart(index).hashCode()) {

                if (dbUsing.canQR(db, qr)) {
                    if (instance.isQr()) {
                        instance.setOpened(2);
                        instance.setQr();
                        qrCode.setText(qr.substring(4, 7)+" "+qr.substring(7,15));
                        dbUsing.changeQR(db, qr);
                        return;
                    }
                    qrCode.setText(qr.substring(4, 7)+" "+qr.substring(7,15));
                    dbUsing.setQRBottle(db, qr);
                    instance.setOpened(2);
                    instance.setQr();

                    return;
                } else {
                    alert("QR уже был считан");
                    return;
                }
            } else {
                alert("QR не входит в диапозоны");
                return;
            }
        }

        public void setPDF417(String pdf417) {
            if (pdf417 == null)
                return;
            if (instance.isOpened() == 0) {
                alert("Сначала просканируйте ШК бутылки");
            }

            if (pdf417.length() == 68) {
                if (!dbUsing.canPDF(db, pdf417)) {
                    alert("PDF417 уже был считан");
                    return;
                } else
                    setPdf417CodeText(1);
            } else
                setPdf417CodeText(0);
            if (!instance.isPdf417())
                dbUsing.setPDF417Bottle(db, pdf417);
            else
                dbUsing.changePDF417(db, pdf417);
            instance.setPdf417();

        }

        public void exitBox(String lpb) {
            final String tmp = lpb;
            if (lpb == null)
                return;
            if (dbUsing.isValidExit(db, lpb)) {
                if ((instance.isQr() && !instance.isPdf417()) || (!instance.isQr() && instance.isPdf417())) {
                    alert("Сначала закончите работу с бутылкой");

                    return;
                }
                if (!dbUsing.isUnicLPB(lpb, db)) {
                    alert("LPB уже был считан");
                    return;
                }
                String[] s = helper2.getMultiplicity(index).split("~"); /* Toast.makeText(getApplicationContext(),barCode.length(),Toast.LENGTH_SHORT);*/
                boolean chooser = false;
                for (String s1 : s) if (Integer.parseInt(s1) == instance.getCount()) chooser = true;
                if (!chooser) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ScannerActivity.this);
                    builder.setTitle("Внимание!").setMessage("Количество просканированных бутылок (" + instance.getCount() + ") не соответсвует кратности (" + helper2.getMultiplicity(index).replace("~", ".") + ") .Продолжить? ").setCancelable(false).setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            dbUsing.exitBox(tmp, db, instance.getCount(), 1);
                            qrCode.setText("");
                            dbUsing.delFileName(db);
                            pdf417Code.setText("");
                            boxCount.setText("");
                            description.setText("");
                            newBottle(0);
                            indexes = null;
                            instance.setCount(0);
                            instance.setOpened(0);
                            dialog.cancel();
                            setEan(instance.getEAN());
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    dbUsing.exitBox(lpb, db, instance.getCount(), 0);
                    qrCode.setText("");
                    dbUsing.delFileName(db);
                    pdf417Code.setText("");
                    indexes = null;
                    boxCount.setText("");
                    description.setText("");
                    newBottle(0);
                    instance.setCount(0);
                    instance.setOpened(0);
                }
            } else if (instance.isOpened() == 0) {
                alert("Сначала просканируйте или введите ШК продукта");

            } else {
                alert("Данный ШК не валиден для закрытия коробки");

            }
        }
        private void alert(String message){
            if (enable) {
                enable = false;
                AlertDialog.Builder builder = new AlertDialog.Builder(ScannerActivity.this);
                builder.setTitle("Ошибка!")
                        .setMessage(message)
                        .setCancelable(false)
                        .setNegativeButton("Закрыть",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        enable = true;
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }


    public static void newBottle(int count) {
        setPdf417CodeText(2);
        qrCode.setText("");
        boxCount.setText(String.valueOf(count));
    }

    private static void setText() {
        if (indexes != null)
            description.setText(helper2.getText(indexes.get(0)));
        else Toast.makeText(scannerContext, "Что-то пошло не так", Toast.LENGTH_SHORT).show();
    }

    private static void setPdf417CodeText(int val) {
        if (val == 1) {
            pdf417Code.setText("Считан");
        } else if (val == 0) {
            pdf417Code.setText("Не считан");
        } else if (val == 2) {
            qrCode.setText("");
            pdf417Code.setText("");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanner_layout);

        PackageManager pm = ScannerActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(ScannerActivity.this, ScanResultReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        instance = BoxSingleton.getInstance(this);
        scannerContext = this;
        final Activity activity = this;
        barCodeRes = new BarCodeRes();
        dbUsing = new DBUsing(getApplicationContext());
        db = dbUsing.getWritableDatabase();
        qrCode = findViewById(R.id.qrcode);
        pdf417Code = findViewById(R.id.pdf417code);
        description = findViewById(R.id.description);
        Button button = findViewById(R.id.erase);

        boxCount = findViewById(R.id.boxCount);

        componentName = new ComponentName(this, ScanResultReceiver.class);

        Button cantPdf = findViewById(R.id.cantPdf);
        cantPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                barCodeRes.setPDF417("1");
            }
        });
        TextView boxCountText = findViewById(R.id.boxCountText);
        Button enter = findViewById(R.id.enterCode);
        final EditText bCode = findViewById(R.id.code);


        boxCountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BoxActivity.class);
                intent.putExtra("index", index);
                intent.putExtra("boxId", dbUsing.getId(db));
                startActivity(intent);
            }
        });

        bCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    // обработка нажатия Enter
                    String s = bCode.getText().toString();
                    barCode = s;
                    bCode.setText("");
                    //Toast.makeText(getApplicationContext(),"Я ввод "+ barCode,Toast.LENGTH_SHORT);
                    barCodeRes.setBarCode();
                    hideKeyboard(activity);
                    return true;
                }
                return false;
            }
        });

        boxCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BoxActivity.class);
                intent.putExtra("index", index);
                intent.putExtra("boxId", dbUsing.getId(db));
                startActivity(intent);
            }
        });
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                barCode = bCode.getText().toString();
                bCode.setText("");
                barCodeRes.setBarCode();
                hideKeyboard(activity);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (instance.isPdf417() || instance.isQr()) {
                    SQLiteDatabase bd = dbUsing.getWritableDatabase();
                    Cursor c = bd.rawQuery("SELECT * FROM Bottles", null);
                    if (c.moveToLast()) {
                        if (instance.isPdf417()) {
                            db.delete("Bottles", "QR = 0", null);
                        } else {
                            db.delete("Bottles", "PDF417 = 0", null);
                        }

                    }
                    pdf417Code.setText("");
                    qrCode.setText("");
                }
            }
        });

        try {
            initScanner();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {

        if (qrCode == null)
            qrCode = findViewById(R.id.qrcode);
        if (pdf417Code == null)
            pdf417Code = findViewById(R.id.pdf417code);
        if (description == null)
            description = findViewById(R.id.description);
        qrCode.setText("");
        pdf417Code.setText("");
        if (boxCount == null)
            boxCount = findViewById(R.id.boxCount);
        int isOB = dbUsing.isOpenBox(db);


        if (isOB > -1) {
            Box boxl = dbUsing.getBottles(db, isOB);
            if (boxl != null && boxl.bottles != null && boxl.bottles.size() > 0) {
                ean = dbUsing.getLastEAN(db, isOB);
                indexes = helper2.getIndex(ean);
                instance.setOpened(2);
                if (indexes.size() == 1)
                    index = indexes.get(0);
                setText();
                instance.setCount(boxl.bottles.size());
                boxCount.setText(String.valueOf(boxl.bottles.size()));
            }
        }

        super.onStart();
    }

    @Override
    protected void onResume() {
        ComponentName component = new ComponentName(getApplicationContext(), ScanResultReceiver.class);


        scannerContext.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        try {
            initScanner();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        PackageManager pm = ScannerActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(ScannerActivity.this, ScanResultReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        if (iScanner != null) {
            try {
                iScanner.aDecodeAPIDeinit();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        iScanner = null;
        super.onPause();
    }

    @Override
    protected void onStop() {

        PackageManager pm = ScannerActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(ScannerActivity.this, ScanResultReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        if (iScanner != null) {
            try {
                iScanner.aDecodeAPIDeinit();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        iScanner = null;
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        if ((instance.getOpened() == 1 || instance.getOpened() == 2) && instance.getCount() > 0)
            dbUsing.deleteBox(db, dbUsing.getId(db));
        if (iScanner != null) {
            try {
                iScanner.aDecodeAPIDeinit();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        iScanner = null;
        SQLiteDatabase bd = dbUsing.getWritableDatabase();
        Cursor c = bd.rawQuery("SELECT * FROM Bottles", null);
        if (c.moveToLast()) {
            int id = c.getInt(c.getColumnIndex("id"));
            String qr = c.getString(c.getColumnIndex("QR"));
            String pdf = c.getString(c.getColumnIndex("PDF417"));
            if (qr.equals("0") || pdf.equals("0")) {
                ContentValues newValues = new ContentValues();
                newValues.put("QR", "0");
                newValues.put("PDF417", "0");
                String where = "id = " + id;
                db.update("Bottles", newValues, where, null);
            }
        }
        PackageManager pm = ScannerActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(ScannerActivity.this, ScanResultReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        if (instance.isPdf417() || instance.isQr()) {
            SQLiteDatabase bd = dbUsing.getWritableDatabase();
            Cursor c = bd.rawQuery("SELECT * FROM Bottles", null);
            if (c.moveToLast()) {
                if (instance.isPdf417()) {
                    db.delete("Bottles", "QR = 0", null);
                } else {
                    db.delete("Bottles", "PDF417 = 0", null);
                }

            }
            instance.delQRPDF();
            pdf417Code.setText("");
            qrCode.setText("");
        }

        if (instance.getOpened() == 2 && instance.getCount() > 0) {
            Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent2);
        } else {
            if (instance.getOpened() == 1 || instance.getOpened() == 2) {
                dbUsing.deleteBox(db, dbUsing.getId(db));
                instance.setOpened(0);
                instance.delQRPDF();
            }
            super.onBackPressed();
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
