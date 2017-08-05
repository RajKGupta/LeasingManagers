package com.example.rajk.leasingmanagers.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.model.measurement;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by RajK on 16-05-2017.
 */

public class measurement_adapter extends RecyclerView.Adapter<measurement_adapter.MyViewHolder> {
    List<measurement> list = new ArrayList<>();
    private Context context;

    public measurement_adapter(List<measurement> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tag, width, height, unit;
        CircleImageView fleximage;

        public MyViewHolder(View itemView) {
            super(itemView);
            tag = (TextView) itemView.findViewById(R.id.tag);
            width = (TextView) itemView.findViewById(R.id.width);
            height = (TextView) itemView.findViewById(R.id.height);
            fleximage = (CircleImageView) itemView.findViewById(R.id.fleximage);
            unit = (TextView) itemView.findViewById(R.id.unit);
        }

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.measurement_row, parent, false);
        return new MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final measurement_adapter.MyViewHolder holder, int position) {
        measurement msr = list.get(position);
        holder.tag.setText(msr.getTag());
        holder.width.setText(msr.getWidth());
        holder.height.setText(msr.getHeight());
        holder.unit.setText(msr.getUnit());
        Picasso.with(context).load(msr.getFleximage()).into(holder.fleximage);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}


