package com.example.rajk.leasingmanagers.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.model.CompletedBy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.example.rajk.leasingmanagers.LeasingManagers.DBREF;
import static com.example.rajk.leasingmanagers.LeasingManagers.simpleDateFormatDDMMYYYY;

public class assignedto_adapter extends  RecyclerView.Adapter<assignedto_adapter.MyViewHolder>
{
    List<CompletedBy> list = new ArrayList<>();
    private Context context;
    SharedPreferences sharedPreferences ;
    String taskId;
    assignedto_adapterListener listener;
    public CompletedBy emp = new CompletedBy();

    public assignedto_adapter(List<CompletedBy> list, Context context,String taskId,assignedto_adapterListener listener) {
        this.list = list;
        this.context = context;
        sharedPreferences = context.getSharedPreferences("SESSION",Context.MODE_PRIVATE);
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
        holder.open_options.setVisibility(View.VISIBLE);
            holder.noteAuthor.setText("Coordinator's Note:");
            holder.tv_dateCompleted.setText("Expected Deadline :");

        holder.dateassigned.setText(emp.getDateassigned());
        holder.dateCompleted.setText(emp.getDatecompleted());
        holder.noteString.setText(emp.getNote());
        holder.assignedby.setText(emp.getAssignedByName());

        DatabaseReference dbEmp = DBREF.child("Employee").child(emp.getEmpId()).getRef();
        dbEmp.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String empname = dataSnapshot.child("name").getValue(String.class);
                holder.employeename.setText(empname);
                String empdesig = dataSnapshot.child("designation").getValue(String.class);
                holder.employeeDesig.setText(empdesig);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        applyBackgroundColor(holder,emp);
        applyClickEvents(holder,position);
    }

    private void applyBackgroundColor(MyViewHolder holder,CompletedBy emp) {

        try {
            Date curDate = new Date();

            curDate.setTime(Calendar.MONTH);
            curDate.setTime(Calendar.YEAR);
            curDate.setTime(Calendar.DATE);

            Date aDate = simpleDateFormatDDMMYYYY.parse(emp.getDateassigned());

            if(curDate.compareTo(aDate)>-1)
            {
                holder.ll_overall.setBackgroundColor(R.color.colorAccent);
            }
            else
            {
                holder.ll_overall.setBackgroundColor(R.color.white);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView dateCompleted,employeename,employeeDesig,dateassigned,tv_dateCompleted,noteAuthor,noteString,assignedby;
        ImageButton open_options;
        LinearLayout ll_overall;

        public MyViewHolder(View itemView) {
            super(itemView);

            dateCompleted = (TextView) itemView.findViewById(R.id.dateCompleted);
            ll_overall = (LinearLayout)itemView.findViewById(R.id.ll_overall);
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
            assignedby = (TextView)itemView.findViewById(R.id.assignedBy);
            open_options = (ImageButton)itemView.findViewById(R.id.open_options);
        }
    }
    public interface assignedto_adapterListener {
        void onOptionsButtonClicked(int position,MyViewHolder holder);
    }
    private void applyClickEvents(final MyViewHolder holder, final int position) {

        holder.open_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onOptionsButtonClicked(position,holder);
            }
        });
    }
}