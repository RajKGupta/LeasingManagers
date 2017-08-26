package com.example.rajk.leasingmanagers.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.model.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.rajk.leasingmanagers.LeasingManagers.DBREF;

/**
 * Created by RajK on 16-05-2017.
 */

public class CustomerTasks_Adapter extends RecyclerView.Adapter<CustomerTasks_Adapter.MyViewHolder> {
    List<String> list = new ArrayList<>();
    private Context context;
    private CustomerTaskAdapterListener listener;
    private String customerId;

    public CustomerTasks_Adapter(List<String> list, Context c, CustomerTaskAdapterListener listener,String customerId) {
        this.list = list;
        this.context = c;
        this.listener = listener;
        this.customerId = customerId;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView taskname, timestamp, icon_text,tv_taskStatus;
        ImageView imgProfile;
        RelativeLayout viewdetail;

        public MyViewHolder(View itemView) {
            super(itemView);
            taskname = (TextView) itemView.findViewById(R.id.tv_taskname);
            tv_taskStatus = (TextView)itemView.findViewById(R.id.tv_taskStatus);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            icon_text = (TextView) itemView.findViewById(R.id.icon_text);
            imgProfile = (ImageView) itemView.findViewById(R.id.icon_profile);
            viewdetail = (RelativeLayout) itemView.findViewById(R.id.viewdetails);
        }

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customertasks_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomerTasks_Adapter.MyViewHolder holder, final int position)
    {
        final DatabaseReference taskStatus = DBREF.child("Customer").child(customerId).child("Task").child(list.get(position)).getRef();
        taskStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String status = dataSnapshot.getValue(String.class);
                    if (status.equals("pending")) {
                        holder.tv_taskStatus.setText("(Pending)");
                    } else {
                        holder.tv_taskStatus.setText("(Completed)");
                    }
            }}

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });/*ChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String status = dataSnapshot.getValue(String.class);
                if(status.equals("pending")&&dataSnapshot.getKey().equals(list.get(position)))
                {
                    holder.tv_taskStatus.setText("(Pending)");
                }
                else
                {
                    holder.tv_taskStatus.setText("(Completed)");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String status = dataSnapshot.getValue(String.class);
                if(status.equals("pending"))
                {
                    holder.tv_taskStatus.setText("(Pending)");
                }
                else
                {
                    holder.tv_taskStatus.setText("(Completed)");
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                taskStatus.removeEventListener(this);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
        DatabaseReference refh = DBREF.child("Task").child(list.get(position)).getRef();
        refh.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Task task = dataSnapshot.getValue(Task.class);
                    holder.taskname.setText(task.getName());
                    String iconText = task.getName().toUpperCase();
                    holder.icon_text.setText(iconText.charAt(0) + "");
                    holder.imgProfile.setImageResource(R.drawable.bg_circle);
                    holder.imgProfile.setColorFilter(task.getColor());
                    holder.timestamp.setText("End Date:" + task.getExpEndDate());
                    applyClickEvents(holder, position);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface CustomerTaskAdapterListener {
        void onCustomerTaskRowClicked(int position);
    }

    private void applyClickEvents(CustomerTasks_Adapter.MyViewHolder holder, final int position) {

        holder.viewdetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onCustomerTaskRowClicked(position);
            }
        });
    }
}