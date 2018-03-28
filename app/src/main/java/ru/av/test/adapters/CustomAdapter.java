package ru.av.test.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter {
    private Context context;
    private int textViewResourceId;
    private ArrayList<String> objects;
    public static boolean flag = false;
    public CustomAdapter(Context context, int textViewResourceId,
                         ArrayList<String> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.textViewResourceId = textViewResourceId;
        this.objects = objects;
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = View.inflate(context, textViewResourceId, null);
        if (flag) {
            TextView tv = (TextView) convertView;
            tv.setText(objects.get(position));
        }
        return convertView;
    }
}