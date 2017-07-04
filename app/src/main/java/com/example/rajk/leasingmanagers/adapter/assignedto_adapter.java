package com.example.rajk.leasingmanagers.adapter;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.rajk.leasingmanagers.MainViews.CreateTask;
import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.model.CompletedBy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RajK on 16-05-2017.
 */

public class assignedto_adapter extends  RecyclerView.Adapter<assignedto_adapter.MyViewHolder>
{
    List<CompletedBy> list = new ArrayList<>();
    private Context context;
    SharedPreferences sharedPreferences ;
    String type,taskId;
    assignedto_adapterListener listener;
    public CompletedBy emp = new CompletedBy();

    public assignedto_adapter(List<CompletedBy> list, Context context, String type,String taskId,assignedto_adapterListener listener) {
        this.list = list;
        this.context = context;
        sharedPreferences = context.getSharedPreferences("SESSION",Context.MODE_PRIVATE);
        this.type = type;
        this.taskId=taskId;
        this.listener = listener;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.assignedto_list_row,parent,false);
        return new MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final assignedto_adapter.MyViewHolder holder, final int position)
    {
        emp = list.get(position);
        if (type.equals("CompletedBy")) {
            holder.button_rl.setVisibility(View.GONE);
            holder.noteAuthor.setText("Employee's Note:");
            holder.tv_dateCompleted.setText("Date Completed :");
        } else if (type.equals("AssignedTo")){
            holder.button_rl.setVisibility(View.VISIBLE);
            holder.noteAuthor.setText("Coordinator's Note:");
            holder.tv_dateCompleted.setText("Expected Deadline :");
        }

        DatabaseReference dbEmp = FirebaseDatabase.getInstance().getReference().child("MeChat").child("Employee").child(emp.getEmpId()).getRef();
        dbEmp.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.dateassigned.setText(emp.getDateassigned());
                holder.dateCompleted.setText(emp.getDatecompleted());
                holder.noteString.setText(emp.getNote());
                String empname = dataSnapshot.child("name").getValue(String.class);
                holder.employeename.setText(empname);
                String empdesig = dataSnapshot.child("designation").getValue(String.class);
                holder.employeeDesig.setText(empdesig);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        applyClickEvents(holder,position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView dateCompleted,employeename,employeeDesig,dateassigned,tv_dateCompleted,noteAuthor,noteString;
        RelativeLayout button_rl;
        ImageButton removeButton,remindButton;

        public MyViewHolder(View itemView) {
            super(itemView);

            dateCompleted = (TextView) itemView.findViewById(R.id.dateCompleted);

            employeename = (TextView)
                    itemView.findViewById(R.id.employeeName);


            employeeDesig = (TextView)
                    itemView.findViewById(R.id.employeeDesig);

            dateassigned = (TextView)
                    itemView.findViewById(R.id.dateAssign);


            tv_dateCompleted = (TextView)
                    itemView.findViewById(R.id.tv_datecompleted);

            noteAuthor = (TextView)itemView.findViewById(R.id.noteAuthor);
            noteString = (TextView) itemView.findViewById(R.id.noteString);

            button_rl = (RelativeLayout)itemView.findViewById(R.id.button_rl);

            removeButton = (ImageButton)itemView.findViewById(R.id.remove);
            remindButton = (ImageButton) itemView.findViewById(R.id.remind);


        }
    }
    public interface assignedto_adapterListener {
        void onRemoveButtonClicked(int position);
        void onRemindButtonClicked(int position);
    }
    private void applyClickEvents(MyViewHolder holder, final int position) {

        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onRemoveButtonClicked(position);
            }
        });

        holder.remindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onRemindButtonClicked(position);
            }
        });
    }
}
