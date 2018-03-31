package ru.av.test.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import device.scanner.DecodeResult;
import device.scanner.IScannerService;
import device.scanner.ScannerService;
import ru.av.test.Data.Box;
import ru.av.test.Data.Items;
import ru.av.test.R;
import ru.av.test.adapters.AdapterView;
import ru.av.test.util.DBUsing;
import ru.av.test.util.WriteSingleton;


public class BoxViewActivity extends Activity {
    private static DBUsing dbUsing = null;
    private final Context context = this;
    public static AdapterView adapter;
    private static IScannerService iScanner = null;
    private static DecodeResult mDecodeResult = new DecodeResult();
    private static SQLiteDatabase db;
    private static List<Items> list;
    private static RecyclerView rv;
    private static WriteSingleton singleton;
    private static Context context2;
    private static int flag = 0;
    private static TextView txt;

    private static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static class ScanResultReceiver extends BroadcastReceiver {

        String barCode;

        @Override
        public void onReceive(Context context, Intent intent) {

            if (iScanner != null && flag != 0) {
                try {
                    mDecodeResult.recycle();
                    iScanner.aDecodeGetResult(mDecodeResult);
                    barCode = mDecodeResult.toString(); //ра
                    if (dbUsing.getId(db, barCode) != -1 && dbUsing.getLpb(db, dbUsing.getId(db, barCode)).equals(barCode)) {


                        dbUsing.deleteBox(db, Integer.parseInt(BoxesViewActivity.boxId));
                        WriteSingleton.write(dbUsing, db);
                        BoxViewActivity.initializeData();
                        BoxViewActivity.initializeAdapter();
                        rv.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        BoxesViewActivity.initializeData();
                        BoxesViewActivity.initializeAdapter();
                        BoxesViewActivity.adapter.notifyDataSetChanged();
                        Intent intent2 = new Intent(context2, BoxesViewActivity.class);
                        context.startActivity(intent2);

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Ошибка!")
                                .setMessage("Код коробки не совпадает со считанным кодом")
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


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.box_view_layout);
        context2 = this;
        dbUsing = new DBUsing(this);
        db = dbUsing.getWritableDatabase();
        initializeData();
        final Activity activity = this;
        if (BoxesViewActivity.boxId != null && dbUsing.getPlodName(db, BoxesViewActivity.boxId) != null) {

            rv = findViewById(R.id.boxViewList);
            LinearLayoutManager llm = new LinearLayoutManager(this);
            rv.setLayoutManager(llm);
            rv.setHasFixedSize(true);


            initializeAdapter();

            Button button = findViewById(R.id.deleteBox);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    flag = 1;
                    PackageManager pm = BoxViewActivity.this.getPackageManager();
                    ComponentName componentName = new ComponentName(BoxViewActivity.this, BoxViewActivity.ScanResultReceiver.class);
                    pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);

                    try {
                        initScanner();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    LayoutInflater li = LayoutInflater.from(context);
                    View promptsView = li.inflate(R.layout.alert_dialog, null);

                    AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);
                    mDialogBuilder.setView(promptsView);
                    TextView textView = promptsView.findViewById(R.id.tv);
                    textView.setText("Подвердите удаление сканированием или введите код в ручную");
                    final EditText userInput = promptsView.findViewById(R.id.input_text);
                    userInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                                if (i == KeyEvent.KEYCODE_ENTER) {
                                    if (dbUsing.getId(db, userInput.getText().toString()) != -1) {
                                        if (dbUsing.getLpb(db, dbUsing.getId(db, userInput.getText().toString())).equals(userInput.getText().toString())) {
                                            dbUsing.deleteBox(db, Integer.parseInt(BoxesViewActivity.boxId));
                                            WriteSingleton.write(dbUsing, db);
                                            BoxViewActivity.initializeData();
                                            BoxViewActivity.initializeAdapter();
                                            rv.setAdapter(adapter);
                                            adapter.notifyDataSetChanged();
                                            BoxesViewActivity.initializeData();
                                            BoxesViewActivity.initializeAdapter();
                                            BoxesViewActivity.adapter.notifyDataSetChanged();
                                            Intent intent = new Intent(getApplicationContext(), BoxesViewActivity.class);
                                            startActivity(intent);
                                        }
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
                                    return true;
                                }
                            }
                            return false;
                        }
                    });
                    mDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            if (dbUsing.getId(db, userInput.getText().toString()) != -1) {
                                                if (dbUsing.getLpb(db, dbUsing.getId(db, userInput.getText().toString())).equals(userInput.getText().toString())) {


                                                    dbUsing.deleteBox(db, Integer.parseInt(BoxesViewActivity.boxId));
                                                    WriteSingleton.write(dbUsing, db);
                                                    BoxViewActivity.initializeData();
                                                    BoxViewActivity.initializeAdapter();
                                                    rv.setAdapter(adapter);
                                                    adapter.notifyDataSetChanged();
                                                    BoxesViewActivity.initializeData();
                                                    BoxesViewActivity.initializeAdapter();
                                                    BoxesViewActivity.adapter.notifyDataSetChanged();
                                                    Intent intent = new Intent(getApplicationContext(), BoxesViewActivity.class);
                                                    startActivity(intent);
                                                }
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
                                            flag = 0;
                                            dialog.cancel();
                                        }
                                    });

                    AlertDialog alertDialog = mDialogBuilder.create();
                    alertDialog.show();
                    hideKeyboard(activity);


                }
            });

        }

    }


    @Override
    protected void onPause() {
        flag = 0;
        if (iScanner != null) {
            try {
                iScanner.aDecodeAPIDeinit();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        iScanner = null;
        PackageManager pm = BoxViewActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(BoxViewActivity.this, BoxViewActivity.ScanResultReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        super.onPause();
    }

    @Override
    protected void onStop() {
        flag = 0;
        if (iScanner != null) {
            try {
                iScanner.aDecodeAPIDeinit();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        iScanner = null;
        PackageManager pm = BoxViewActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(BoxViewActivity.this, BoxViewActivity.ScanResultReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        flag = 0;
        if (iScanner != null) {
            try {
                iScanner.aDecodeAPIDeinit();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        iScanner = null;
        PackageManager pm = BoxViewActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(BoxViewActivity.this, BoxViewActivity.ScanResultReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(getApplicationContext(), BoxesViewActivity.class);
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

    private static void initializeData() {

        list = new ArrayList<>();
        String qr, pdf417,tmp;
        final Box boxes = dbUsing.getBottles(db, Integer.parseInt(BoxesViewActivity.boxId));
        if (boxes != null && boxes.bottles != null)
            for (int i = 0; i < boxes.bottles.size(); i++) {
                tmp = boxes.bottles.get(i).qr;
                qr = tmp.substring(4, 7)+" "+tmp.substring(7,15);;
                if (boxes.bottles.get(i).pdf417.equals("1")) {
                    pdf417 = "Не считан";
                } else {
                    pdf417 = "Считан";
                }
                list.add(new Items(qr, pdf417));
            }
    }


    private static void initializeAdapter() {
        adapter = new AdapterView(list);
        rv.setAdapter(adapter);
    }

}
