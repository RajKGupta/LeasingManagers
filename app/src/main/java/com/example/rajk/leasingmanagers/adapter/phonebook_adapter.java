package com.example.rajk.leasingmanagers.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.model.Phonebook;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SoumyaAgarwal on 8/6/2017.
 */

public class phonebook_adapter extends RecyclerView.Adapter<phonebook_adapter.MyViewHolder> {
    List<Phonebook> list = new ArrayList<>();
    private Context context;
    phonebook_adapterListener listener;

    public phonebook_adapter(List<Phonebook> list, Context context, phonebook_adapterListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.phonebook_row, parent, false);
        return new MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        Phonebook phonenumber = list.get(position);

        holder.Name.setText(phonenumber.getName());
        holder.Desig.setText(phonenumber.getDesignation());
        holder.contact.setText(phonenumber.getContact());
        if (!phonenumber.getEmail().equals("")) {
            holder.email.setText(phonenumber.getEmail());
            holder.email.setVisibility(View.VISIBLE);
        } else
            holder.email.setVisibility(View.GONE);
        String caps = phonenumber.getName().toUpperCase();
        holder.icon_text.setText(caps.charAt(0) + "");

        applyClickEvents(holder, position);
        applyProfilePicture(holder);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void applyProfilePicture(MyViewHolder holder) {

        holder.imgProfile.setImageResource(R.drawable.bg_circle);
        holder.imgProfile.setColorFilter(getRandomMaterialColor("400"));

    }

    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = context.getResources().getIdentifier("mdcolor_" + typeColor, "array", context.getPackageName());

        if (arrayId != 0) {
            TypedArray colors = context.getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView Name, icon_text, Desig, contact, email;
        LinearLayout employee_row;
        ImageButton callme, deleteme;
        ImageView imgProfile;

        public MyViewHolder(View itemView) {
            super(itemView);

            Name = (TextView) itemView.findViewById(R.id.Name);
            Desig = (TextView) itemView.findViewById(R.id.Desig);
            contact = (TextView) itemView.findViewById(R.id.contact);
            email = (TextView) itemView.findViewById(R.id.email);
            icon_text = (TextView) itemView.findViewById(R.id.icon_text);
            employee_row = (LinearLayout) itemView.findViewById(R.id.employee_row);
            imgProfile = (ImageView) itemView.findViewById(R.id.icon_profile);
            callme = (ImageButton) itemView.findViewById(R.id.callme);
            deleteme = (ImageButton) itemView.findViewById(R.id.deleteme);
        }
    }

    public interface phonebook_adapterListener {
        void onCALLMEclicked(int position);

        void onDELETEMEclicked(int position);
    }

    private void applyClickEvents(MyViewHolder holder, final int position) {

        holder.callme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCALLMEclicked(position);
            }
        });

        holder.deleteme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDELETEMEclicked(position);
            }
        });
    }
}