package ru.av.test.adapters;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.av.test.activities.BoxViewActivity;
import ru.av.test.activities.BoxesViewActivity;
import ru.av.test.Data.Boxes;
import ru.av.test.R;


public class BoxesAdapterView extends RecyclerView.Adapter<BoxesAdapterView.ViewHolder> {

    private Context context;
    private List<Boxes> items;


    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView lpb;
        TextView count;


        ViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cardView);
            lpb = itemView.findViewById(R.id.lpb);
            count = itemView.findViewById(R.id.count);


        }
    }


    public BoxesAdapterView(List<Boxes> items, Context context) {
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
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        if (getItemCount() != 0) {
            viewHolder.lpb.setText(String.valueOf(items.get(i).LPB));
            viewHolder.count.setText(String.valueOf(items.get(i).count));
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, BoxViewActivity.class);
                    intent.putExtra("boxId", items.get(i).id);
                    BoxesViewActivity.boxId = String.valueOf(items.get(i).id);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    BoxesViewActivity.initializeData();
                    BoxesViewActivity.adapter.notifyDataSetChanged();
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

