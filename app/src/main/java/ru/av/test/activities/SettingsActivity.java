package ru.av.test.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

import ru.av.test.R;
import ru.av.test.adapters.CustomAdapter;
import ru.av.test.util.DBUsing;
import ru.av.test.util.WriteSingleton;


public class SettingsActivity extends Activity {
    private EditText editText;
    private DBUsing dbUsing;
    private Context context;
    private SQLiteDatabase db;
    private WriteSingleton singleton;
    private Spinner spinner;
    private ArrayAdapter<String> adapter;

    public static void hideKeyboard(Activity activity) {
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
        setContentView(R.layout.settings_layout);
        editText = findViewById(R.id.edittextishe);
        dbUsing = new DBUsing(this);
        db = dbUsing.getWritableDatabase();
        context = this;
        final Activity activity = this;
        singleton = WriteSingleton.getInstance();
        editText.setHint(dbUsing.getTemplateName(db));
        Button button = findViewById(R.id.chooseTemplate);
        Button button1 = findViewById(R.id.deletePlod);
        spinner = findViewById(R.id.spinirishe);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().length() == 14) {
                    dbUsing.setTemplateName(db, String.valueOf(editText.getText()));
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setTitle("Ошибка!")
                            .setMessage("Неверная длина шаблона")
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
                hideKeyboard(activity);
            }
        });

        if (dbUsing.getPlods(db) != null) {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dbUsing.getPlods(db));

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            if (adapter != null) {
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> adapterView, View view, int i, long l) {
                        CustomAdapter.flag = true;
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> adapterView) {
                    }

                });
            }
        }
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (spinner.getSelectedItem() != null) {
                    final ArrayList<String> plods = dbUsing.getPlods(db);

                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setTitle("Внимание!")
                            .setMessage("Внимание данное действие удалит все коробки с выбранного PLOD. Продолжить? ")
                            .setCancelable(false)
                            .setNegativeButton("Закрыть",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }).setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {



                            dbUsing.deletePlod(db, plods.get(spinner.getSelectedItemPosition()));
                            db.delete("plod_list", "plod ='" + plods.get(spinner.getSelectedItemPosition()) + "'", null);
                            WriteSingleton.write(dbUsing, db);
                            
                            if (dbUsing.getPlods(db) != null) {
                                adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, dbUsing.getPlods(db));

                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            } else {
                                adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, new ArrayList<String>());

                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            }
                            adapter.notifyDataSetChanged();
                            spinner.setAdapter(adapter);

                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();


                }

            }
        });


    }


    protected void onResume() {
        if (dbUsing.getPlods(db) != null) {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dbUsing.getPlods(db));

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            if (adapter != null) {
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> adapterView, View view, int i, long l) {
                        CustomAdapter.flag = true;
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> adapterView) {
                    }

                });
            }
        } else {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            if (adapter != null) {
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> adapterView, View view, int i, long l) {
                        CustomAdapter.flag = true;
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> adapterView) {
                    }

                });
            }
        }
        super.onResume();
    }
}
