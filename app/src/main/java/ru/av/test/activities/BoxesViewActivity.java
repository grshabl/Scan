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
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import device.scanner.DecodeResult;
import device.scanner.IScannerService;
import device.scanner.ScannerService;
import ru.av.test.Data.Boxes;
import ru.av.test.MainActivity;
import ru.av.test.R;
import ru.av.test.adapters.BoxesAdapterView;
import ru.av.test.util.DBUsing;
import ru.av.test.util.WriteCSVHelper;
import ru.av.test.util.WriteSingleton;


public class BoxesViewActivity extends AppCompatActivity {
    private static IScannerService iScanner = null;
    private static Context context;
    private static DecodeResult mDecodeResult = new DecodeResult();
    private static List<Boxes> list;
    private static RecyclerView rv;
    private static DBUsing dbUsing;
    public static BoxesAdapterView adapter;
    public static String boxId;
    private static SQLiteDatabase db;

    public static class ScanResultReceiver extends BroadcastReceiver {
        String barcode;

        @Override
        public void onReceive(Context context, Intent intent) {

            if (iScanner != null) {
                try {
                    mDecodeResult.recycle();
                    iScanner.aDecodeGetResult(mDecodeResult);
                    barcode = mDecodeResult.toString();
                    if (dbUsing.getId(db, barcode) != -1) {
                        boxId = String.valueOf(dbUsing.getId(db, mDecodeResult.toString()));
                        Intent intent1 = new Intent(context, BoxViewActivity.class);
                        intent.putExtra("boxId", dbUsing.getId(db, mDecodeResult.toString()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        context.startActivity(intent1);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(BoxesViewActivity.context);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boxes_view_layout);
        context = this;
        dbUsing = new DBUsing(this);
        db = dbUsing.getWritableDatabase();
        initializeData();
        final Activity activity = this;
        rv = (RecyclerView) findViewById(R.id.boxingViewList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        Button but = (Button) findViewById(R.id.importBut);
        Button button = (Button) findViewById(R.id.find);

        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WriteSingleton.setWrote(true);
                WriteSingleton.write(dbUsing, db);
                if(WriteCSVHelper.folderName!=null){
                    MediaScannerConnection.scanFile(BoxesViewActivity.this, new String[] {
                            WriteCSVHelper.folderName+"/"+WriteCSVHelper.fileName}, null, null);

                }
                MediaScannerConnection.scanFile(BoxesViewActivity.this, new String[] {}, null, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(BoxesViewActivity.this);
                builder
                        .setMessage("Операция выполнена успешно")
                        .setCancelable(false)
                        .setNegativeButton("Закрыть",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();

                File file = new File(PlodChooseActivity.dir);
                if(file.exists())
                file.delete();
                ArrayList<String> arrayList = dbUsing.getPlods(db);
                if(arrayList!=null) {
                    for (String s : arrayList) {
                        dbUsing.deletePlod(db, s);
                        db.delete("plod_list", "plod ='" + s + "'", null);
                    }
                    initializeData();
                    initializeAdapter();
                    adapter.notifyDataSetChanged();
                    WriteSingleton.write(dbUsing, db);
                }
            }
        });

        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);
        context = this;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.alert_dialog, null);
                AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);
                mDialogBuilder.setView(promptsView);
                final EditText userInput = promptsView.findViewById(R.id.input_text);
                TextView textView = promptsView.findViewById(R.id.tv);
                textView.setText("Введите ШК коробки");

                mDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (dbUsing.getId(db, userInput.getText().toString()) != -1) {
                                            Intent intent = new Intent(context, BoxViewActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                            boxId = String.valueOf(dbUsing.getId(db, userInput.getText().toString()));
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

                AlertDialog alertDialog = mDialogBuilder.create();
                alertDialog.show();
                hideKeyboard(activity);

            }
        });
        initializeAdapter();

        PackageManager pm = BoxesViewActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(BoxesViewActivity.this, ScanResultReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        try {
            initScanner();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        initializeData();
        initializeAdapter();
        adapter.notifyDataSetChanged();
        try {
            initScanner();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        PackageManager pm = BoxesViewActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(BoxesViewActivity.this, BoxesViewActivity.ScanResultReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        super.onResume();
    }

    @Override
    protected void onRestart() {

        try {
            initScanner();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.onRestart();

    }

    @Override
    protected void onPause() {
        if (iScanner != null) {
            try {
                iScanner.aDecodeAPIDeinit();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        iScanner = null;
        PackageManager pm = BoxesViewActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(BoxesViewActivity.this, BoxesViewActivity.ScanResultReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (iScanner != null) {
            try {
                iScanner.aDecodeAPIDeinit();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        iScanner = null;
        PackageManager pm = BoxesViewActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(BoxesViewActivity.this, BoxesViewActivity.ScanResultReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        super.onStop();
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
        PackageManager pm = BoxesViewActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(BoxesViewActivity.this, BoxesViewActivity.ScanResultReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
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
        list = dbUsing.getBoxes(db);
    }

    public static void initializeAdapter() {
        if (list == null) {
            list = new ArrayList<>();
        }
        adapter = new BoxesAdapterView(list, context);
        rv.setAdapter(adapter);
    }

    private static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}








