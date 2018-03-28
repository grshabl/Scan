package ru.av.test.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.av.test.Data.Items;
import ru.av.test.R;


public class AdapterView extends RecyclerView.Adapter<AdapterView.PersonViewHolder> {

    private List<Items> items;


    public class PersonViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView packName;
        TextView packCount;


        PersonViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            packName = itemView.findViewById(R.id.lpb);
            packCount = itemView.findViewById(R.id.count);
        }
    }

    public AdapterView(List<Items> items) {
        this.items = items;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.template, viewGroup, false);
        return new PersonViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
        personViewHolder.packName.setText(String.valueOf(items.get(i).getItem1()));
        personViewHolder.packCount.setText(String.valueOf(items.get(i).getItem2()));

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

