package com.example.rajk.leasingmanagers.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.rajk.leasingmanagers.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by SoumyaAgarwal on 7/3/2017.
 */

public class bigimage_adapter extends  RecyclerView.Adapter<bigimage_adapter.MyViewHolder>
{
    ArrayList<String> list = new ArrayList<>();
    private Context context;

    public bigimage_adapter(ArrayList<String> list, Context context)
    {
        this.list = list;
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageButton img;

        public MyViewHolder(View itemView) {
            super(itemView);
            img = (ImageButton) itemView.findViewById(R.id.image);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bigimage_row,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        String topic = list.get(position);
        Picasso.with(context).load(Uri.parse(topic)).into(holder.img);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
