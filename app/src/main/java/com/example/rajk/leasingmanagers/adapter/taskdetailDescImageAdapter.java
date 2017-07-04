package com.example.rajk.leasingmanagers.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.rajk.leasingmanagers.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by SoumyaAgarwal on 7/3/2017.
 */

public class taskdetailDescImageAdapter extends  RecyclerView.Adapter<taskdetailDescImageAdapter.MyViewHolder>
{
    ArrayList<String> list = new ArrayList<>();
    private Context context;
    private ImageAdapterListener listener;

    public taskdetailDescImageAdapter(ArrayList<String> list, Context context, ImageAdapterListener listener)
    {
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;
        public LinearLayout imageContainer;

        public MyViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.image_here);
            imageContainer = (LinearLayout)itemView.findViewById(R.id.container);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_image,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final taskdetailDescImageAdapter.MyViewHolder holder, final int position) {
        String topic = list.get(position);
        Picasso.with(context).load(Uri.parse(topic)).into(holder.img);
        applyClickEvents(holder, position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface ImageAdapterListener {
        void onImageClicked(int position);
    }
    private void applyClickEvents(MyViewHolder holder, final int position) {

        holder.imageContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onImageClicked(position);
            }
        });

    }

}