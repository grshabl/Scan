package com.azbuka.gshabalov.tsd_alcho_app.utils;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.azbuka.gshabalov.tsd_alcho_app.R;

import java.util.List;



public class AdapterView extends RecyclerView.Adapter<AdapterView.PersonViewHolder> {

    public static class PersonViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView personName;
        TextView personAge;


        PersonViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            personName = (TextView)itemView.findViewById(R.id.textasd);
            personAge = (TextView)itemView.findViewById(R.id.textasder);

        }
    }

    List<Items> items;

    public AdapterView(List<Items> items){
        this.items = items;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.template, viewGroup, false);
        PersonViewHolder pvh = new PersonViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
        personViewHolder.personName.setText(String.valueOf(items.get(i).item1));
        personViewHolder.personAge.setText(String.valueOf(items.get(i).item2));

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

