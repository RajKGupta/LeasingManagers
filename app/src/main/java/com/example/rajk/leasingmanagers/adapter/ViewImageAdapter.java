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

import java.util.ArrayList;

public class ViewImageAdapter extends  RecyclerView.Adapter<ViewImageAdapter.MyViewHolder>
{
    ArrayList<String> list = new ArrayList<>();
    private Context context;
    public int selectedPosition = 0;

    public ViewImageAdapter(ArrayList<String> list, Context context)
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_image_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewImageAdapter.MyViewHolder holder, final int position) {
        String topic = list.get(position);
        holder.img.setImageURI(Uri.parse(topic));

        if(selectedPosition==position)
            holder.itemView.setBackgroundColor(Color.parseColor("#000000"));
        else
            holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
