package com.azbuka.gshabalov.tsd_alcho_app.Activity;

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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.azbuka.gshabalov.tsd_alcho_app.BaseActivity;
import com.azbuka.gshabalov.tsd_alcho_app.R;
import com.azbuka.gshabalov.tsd_alcho_app.utils.BoxesAdapterView;
import com.azbuka.gshabalov.tsd_alcho_app.utils.Database;
import com.azbuka.gshabalov.tsd_alcho_app.utils.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import device.scanner.DecodeResult;
import device.scanner.IScannerService;
import device.scanner.ScannerService;


/**
 * Created by mezkresh on 02.11.2017.
 */

public class ViewActivity extends Activity {
    private static IScannerService iScanner = null;
    public static Context context;
    private static DecodeResult mDecodeResult = new DecodeResult();
    public static ComponentName component1;
    public static PackageManager packageManager;

    private Activity activity;
    static BoxesAdapterView adapter;
    Database database;
    private static SQLiteDatabase readBase;
    public static String boxId;

    public static class ScanResultReceiver extends BroadcastReceiver {
        String barcode;

        @Override
        public void onReceive(Context context, Intent intent) {

            if (iScanner != null) {
                try {
                    mDecodeResult.recycle();
                    iScanner.aDecodeGetResult(mDecodeResult);
                    barcode = mDecodeResult.toString();
                    Cursor c = readBase.rawQuery("SELECT * FROM "+Database.DATABASE_WRITE+" WHERE "+Database.GOODS_LPB+" = "+barcode,null);
                    if(c.moveToFirst()) {
                        Intent intent1 = new Intent(context, BoxViewActivity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        boxId = barcode;
                        intent1.putExtra("boxId", barcode);
                        context.startActivity(intent1);


                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ViewActivity.context);
                        builder.setTitle("Ошибка!")
                                .setMessage("Данной коробки нет в базе данных")
                                .setCancelable(false)
                                .setNegativeButton("Закрыть",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }


                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    ArrayList<Integer> idList;

    private static List<Items> list;
    private static RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        context = this;
        database = new Database(this);
        readBase = database.getWritableDatabase();
        for(int i=0;i<20;i++) {
            ContentValues values = new ContentValues();
            values.put(Database.PLOD, "asd");
            values.put(Database.PLOD_LINE, "asd");
            values.put(Database.GOODS_CODE, "asd");
            values.put(Database.QR, "asdds");
            values.put(Database.PDF417, "asdasd");
            values.put(Database.MARK_BAD, "asd");
            values.put(Database.BOX_EAN, "asdasd");
            values.put(Database.GOODS_LPB, i<10?"LPB-1234567890":"LPB-9876543210");
            values.put(Database.MULTIPLICITY, "0");
            readBase.insert(Database.DATABASE_WRITE, null, values);
        }
        //component1 = new ComponentName(this,ScanResultReceiver.class);
        initializeData();


        rv = (RecyclerView) findViewById(R.id.boxingViewList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);
        context = this;
        activity = this;
        Button button = (Button) findViewById(R.id.find);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.alert_dialog, null);

                //Создаем AlertDialog
                AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);

                //Настраиваем prompt.xml для нашего AlertDialog:
                mDialogBuilder.setView(promptsView);

                //Настраиваем отображение поля для ввода текста в открытом диалоге:
                final EditText userInput = (EditText) promptsView.findViewById(R.id.input_text);
                TextView textView = (TextView) promptsView.findViewById(R.id.tv);
                textView.setText("Введите ШК коробки");
                //Настраиваем сообщение в диалоговом окне:
                mDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //Вводим текст и отображаем в строке ввода на основном экране:
                                        Cursor c = readBase.rawQuery("SELECT * FROM "+Database.DATABASE_WRITE+" WHERE "+
                                                Database.GOODS_LPB+" = "+userInput.getText(),null);
                                        if(c.moveToFirst()){
                                            Intent intent = new Intent(context, BoxViewActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                            intent.putExtra("boxId",userInput.getText());
                                            startActivity(intent);

                                        } else {

                                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                            builder.setTitle("Ошибка!")
                                                    .setMessage("Данной коробке нет в базе данных!")
                                                    .setCancelable(false)
                                                    .setNegativeButton("Закрыть",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    dialog.cancel();
                                                                }
                                                            });
                                            AlertDialog alert = builder.create();
                                            alert.show();
                                        }


                                    }
                                })
                        .setNegativeButton("Отмена",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                //Создаем AlertDialog:
                AlertDialog alertDialog = mDialogBuilder.create();

                //и отображаем его:
                alertDialog.show();
                hideKeyboard(activity);

            }
        });
        initializeAdapter();


        try {
            initScanner();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }




    @Override
    protected void onDestroy() {
        if (iScanner != null) {
            try {
                iScanner.aDecodeAPIDeinit();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        iScanner = null;
        PackageManager pm = ViewActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(ViewActivity.this, ViewActivity.ScanResultReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();

        Intent intent = new Intent(getApplicationContext(), StartMenu.class);
        startActivity(intent);

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

    public static void initializeData() {
        ArrayList<Items> lbp = new ArrayList<>();
        Cursor c = readBase.rawQuery("SELECT * FROM "+Database.DATABASE_WRITE,null);
        Map<String,Integer> map = new HashMap<>();
        String tmp;
        int count;
        ArrayList<String> lpblist = new ArrayList<>();
        if(c.moveToFirst()){
            do{
                tmp = c.getString(8);
                if(map.containsKey(tmp)) {
                    count = map.get(tmp);
                    map.put(tmp,count+1);
                }
                else {
                    lpblist.add(tmp);
                    map.put(tmp,1);
                }
            }while(c.moveToNext());
        }

        for (String lpb1:lpblist) {
            Items item;
            item = new Items(lpb1,map.get(lpb1).toString());
            item.setLpb(lpb1);
            lbp.add(item);
        }
        list = lbp;


    }

    public static void initializeAdapter() {
        if (list == null) {
            list = new ArrayList<>();
        }
        adapter = new BoxesAdapterView(list, context);
        rv.setAdapter(adapter);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}












