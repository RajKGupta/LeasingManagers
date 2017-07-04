package com.example.rajk.leasingmanagers.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.rajk.leasingmanagers.R;

import java.util.ArrayList;

/**
 * Created by SoumyaAgarwal on 6/27/2017.
 */

public class taskimagesadapter extends  RecyclerView.Adapter<taskimagesadapter.MyViewHolder>
{
    ArrayList<String> list = new ArrayList<>();
    private Context context;

    public taskimagesadapter(ArrayList<String> list, Context context)
    {
        this.list = list;
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;

        public MyViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.image_here);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_image,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final taskimagesadapter.MyViewHolder holder, final int position) {
        String topic = list.get(position);
        holder.img.setImageURI(Uri.parse(topic));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
