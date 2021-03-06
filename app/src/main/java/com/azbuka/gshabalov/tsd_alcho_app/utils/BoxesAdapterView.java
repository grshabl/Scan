package com.azbuka.gshabalov.tsd_alcho_app.utils;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.azbuka.gshabalov.tsd_alcho_app.Activity.BoxViewActivity;
import com.azbuka.gshabalov.tsd_alcho_app.Activity.ViewActivity;
import com.azbuka.gshabalov.tsd_alcho_app.R;

import java.util.List;


public class BoxesAdapterView extends RecyclerView.Adapter<BoxesAdapterView.ViewHolder> {

    Context context;
    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView Name;
        TextView Age;


        ViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            Name = itemView.findViewById(R.id.textasd);
            Age = itemView.findViewById(R.id.textasder);


        }
    }

    List<Items> items;

    public BoxesAdapterView(List<Items> items, Context context){
        this.items = items;
        this.context = context;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.template, viewGroup, false);
        ViewHolder pvh = new ViewHolder(v);

        return pvh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        if(getItemCount()!=0) {
            viewHolder.Name.setText(String.valueOf(items.get(i).item1));
            viewHolder.Age.setText(String.valueOf(items.get(i).item2));
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inten = new Intent(context, BoxViewActivity.class);
                    inten.putExtra("boxId", items.get(i).item1);
                    ViewActivity.boxId = items.get(i).item1;
                    inten.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    inten.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(inten);
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

