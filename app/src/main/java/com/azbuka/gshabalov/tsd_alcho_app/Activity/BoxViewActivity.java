package com.azbuka.gshabalov.tsd_alcho_app.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.azbuka.gshabalov.tsd_alcho_app.R;
import com.azbuka.gshabalov.tsd_alcho_app.utils.AdapterView;
import com.azbuka.gshabalov.tsd_alcho_app.utils.Database;
import com.azbuka.gshabalov.tsd_alcho_app.utils.Items;

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

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import device.scanner.DecodeResult;
import device.scanner.IScannerService;
import device.scanner.ScannerService;


public class BoxViewActivity extends Activity {
    final Context context = this;
    public  static AdapterView adapter;
    private static IScannerService iScanner = null;
    private static DecodeResult mDecodeResult = new DecodeResult();
    static SQLiteDatabase db ;
    private static List<Items> list;
    private Activity activity;
    private static RecyclerView rv;
    Database database;
    private static SQLiteDatabase readBase;
    private static Intent intent;
    private static int kostil = 0;

    public static class ScanResultReceiver extends BroadcastReceiver {

        static BoxViewActivity activity;
        String barCode;
        @Override
        public void onReceive(Context context, Intent intent) {

            if (iScanner != null && kostil!=0) {
                try {
                    // Toast.makeText(context,"Я работаю",Toast.LENGTH_SHORT).show();
                    mDecodeResult.recycle();
                    iScanner.aDecodeGetResult(mDecodeResult);
                    barCode = mDecodeResult.toString(); //ра
                    if (ViewActivity.boxId.equals(barCode)) {


                        readBase.delete(Database.DATABASE_WRITE, Database.GOODS_LPB+" = " + barCode, null);
                        rv.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        ViewActivity.initializeData();
                        ViewActivity.initializeAdapter();
                        ViewActivity.adapter.notifyDataSetChanged();
                        Intent intent1 = new Intent(context, ViewActivity.class);
                        context.startActivity(intent1);
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



                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_view);
        activity = this;
        intent = getIntent();
        TextView lpbText = findViewById(R.id.lpbText);
        lpbText.setText(ViewActivity.boxId);
        rv = (RecyclerView) findViewById(R.id.boxViewList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);
        database = new Database(this);
        readBase = database.getWritableDatabase();
        initializeData();
        initializeAdapter();

        Button button = (Button) findViewById(R.id.deleteBox);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kostil = 1;
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

                //Создаем AlertDialog
                AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);

                //Настраиваем prompt.xml для нашего AlertDialog:
                mDialogBuilder.setView(promptsView);
                TextView textView = (TextView) promptsView.findViewById(R.id.tv);
                textView.setText("Подвердите удаление сканированием или введите код в ручную");
                //Настраиваем отображение поля для ввода текста в открытом диалоге:
                final EditText userInput = (EditText) promptsView.findViewById(R.id.input_text);
                userInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                        if(keyEvent.getAction()==keyEvent.ACTION_DOWN){
                            if(i == KeyEvent.KEYCODE_ENTER) {
                                if (ViewActivity.boxId.equals(userInput.getText())) {


                                    readBase.delete(Database.DATABASE_WRITE, Database.GOODS_LPB+" = " + userInput.getText(), null);
                                    rv.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                    ViewActivity.initializeData();
                                    ViewActivity.initializeAdapter();
                                    ViewActivity.adapter.notifyDataSetChanged();
                                    Intent intent = new Intent(getApplicationContext(), ViewActivity.class);
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
                            return true;

                        }
                        return false;
                    }
                });
                //Настраиваем сообщение в диалоговом окне:
                mDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //Вводим текст и отображаем в строке ввода на основном экране:

                                        if (ViewActivity.boxId.equals(userInput.getText())) {


                                            readBase.delete(Database.DATABASE_WRITE, Database.GOODS_LPB+" = " + userInput.getText(), null);
                                            rv.setAdapter(adapter);
                                            adapter.notifyDataSetChanged();
                                            ViewActivity.initializeData();
                                            ViewActivity.initializeAdapter();
                                            ViewActivity.adapter.notifyDataSetChanged();
                                            Intent intent = new Intent(getApplicationContext(), ViewActivity.class);
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
                                        kostil = 0;
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

    }






    @Override
    public void onBackPressed() {

        Intent intent = new Intent(getApplicationContext(),ViewActivity.class);
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
    public static void initializeData(){

        list = new ArrayList<>();
        Cursor c = readBase.rawQuery("SELECT * FROM "+Database.DATABASE_WRITE,null);
        if(c.moveToFirst()){
            do{
                if(c.getString(8).equals(ViewActivity.boxId)) {
                    list.add(new Items(c.getString(4).length()>12?c.getString(4).substring(4,15):c.getString(4),
                            c.getString(5).length() > 1 ? "Считан" : "Не считан"));
                }
            }while(c.moveToNext());
        }
    }


    public static void initializeAdapter(){
        adapter = new AdapterView(list);
        rv.setAdapter(adapter);
    }

}
