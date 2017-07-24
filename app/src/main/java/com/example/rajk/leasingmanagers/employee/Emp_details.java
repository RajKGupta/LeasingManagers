package com.example.rajk.leasingmanagers.employee;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rajk.leasingmanagers.CoordinatorLogin.CoordinatorSession;
import com.example.rajk.leasingmanagers.MainViews.TaskDetail;
import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.adapter.CustomerTasks_Adapter;
import com.example.rajk.leasingmanagers.adapter.EmployeeTask_Adapter;
import com.example.rajk.leasingmanagers.chat.ChatActivity;
import com.example.rajk.leasingmanagers.model.CompletedBy;
import com.example.rajk.leasingmanagers.model.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.rajk.leasingmanagers.LeasingManagers.DBREF;
import static com.example.rajk.leasingmanagers.LeasingManagers.sendNotif;

public class Emp_details extends AppCompatActivity implements EmployeeTask_Adapter.EmployeeTask_AdapterListener{

    Dialog dialog;
    String id,name,num,add,desig,temp_name,temp_add,temp_num,temp_designation;
    EditText Name,Num,Add,Desig;
    DatabaseReference db;
    RecyclerView rec_employeetask;
    LinearLayoutManager linearLayoutManager;
    private EmployeeTask_Adapter mAdapter;
    List<String> listoftasks;
    CoordinatorSession coordinatorSession;
    String mykey, dbTablekey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_details);

        id = getIntent().getStringExtra("id");

        Name = (EditText) findViewById(R.id.name);
        Num = (EditText) findViewById(R.id.num);
        Add = (EditText) findViewById(R.id.add);
        Desig = (EditText) findViewById(R.id.desig);

        rec_employeetask = (RecyclerView)findViewById(R.id.rec_employeetask);
        linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        rec_employeetask.setLayoutManager(linearLayoutManager);
        rec_employeetask.setItemAnimator(new DefaultItemAnimator());
        rec_employeetask.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));

        coordinatorSession = new CoordinatorSession(this);
        mykey = coordinatorSession.getUsername();

        db = DBREF.child("Employee").child(id);
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Map<String, String> map_new = (Map<String, String>) dataSnapshot.getValue();

                name = (map_new.get("name"));
                add = (map_new.get("address"));
                num = (map_new.get("phone_num"));
                desig = (map_new.get("designation"));

                Name.setText(name);
                Num.setText(num);
                Add.setText(add);
                Desig.setText(desig);
                getSupportActionBar().setTitle(name);
                getSupportActionBar().setSubtitle(desig);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        listoftasks = new ArrayList<>();
        mAdapter = new EmployeeTask_Adapter(listoftasks,getApplicationContext(),id,this);
        rec_employeetask.setAdapter(mAdapter);

        db = db.child("AssignedTask").getRef();
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    listoftasks.add(childSnapshot.getKey());
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.emp_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item1:

                final EditText name_new,num_new,add_new,desig_new;
                Button sub;

                dialog = new Dialog(Emp_details.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.edit_emp);

                name_new = (EditText) dialog.findViewById(R.id.name);
                num_new = (EditText) dialog.findViewById(R.id.num);
                add_new = (EditText) dialog.findViewById(R.id.add);
                desig_new = (EditText) dialog.findViewById(R.id.desig);
                sub = (Button) dialog.findViewById(R.id.submit);

                name_new.setText(name);
                num_new.setText(num);
                add_new.setText(add);
                desig_new.setText(desig);

                sub.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // update database accordingly

                        temp_add = add_new.getText().toString().trim();
                        temp_name = name_new.getText().toString().trim();
                        temp_num = num_new.getText().toString().trim();
                        temp_designation = desig_new.getText().toString().trim();

                        if(TextUtils.isEmpty(temp_add) || TextUtils.isEmpty(temp_name) || TextUtils.isEmpty(temp_num) || TextUtils.isEmpty(temp_designation))
                            Toast.makeText(Emp_details.this,"Enter details...",Toast.LENGTH_SHORT).show();

                        else{

                            db = DBREF.child("Employee").child(id);
                            db.child("name").setValue(temp_name);
                            db.child("address").setValue(temp_add);
                            db.child("phone_num").setValue(temp_num);
                            db.child("designation").setValue(temp_designation);
                            dialog.dismiss();
                        }

                    }
                });

                dialog.show();
                break;
            case R.id.item4:

                checkChatref(mykey,id);
                break;

        }
        return true;
    }

    @Override
    public void onEmployeeRemoveButtonClicked(final int position , final EmployeeTask_Adapter.MyViewHolder holder)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to un-assign this task")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int i) {
                        DatabaseReference dbCancelJob = DBREF.child("Task").child(listoftasks.get(position)).child("AssignedTo").child(id).getRef();
                        dbCancelJob.removeValue();

                        DatabaseReference dbEmployee = DBREF.child("Employee").child(id).child("AssignedTask").child(listoftasks.get(position));
                        dbEmployee.removeValue(); //for employee
                        final String task_id = listoftasks.get(position);
                                String taskName = holder.employeename.getText().toString().trim();
                                String contentforme = "You relieved "+name+" of "+taskName;//todo get taskname here
                                sendNotif(mykey,mykey,"cancelJob",contentforme,task_id);
                                String contentforother= "Coordinator "+coordinatorSession.getName()+" relieved you of "+taskName;
                                sendNotif(mykey,id,"cancelJob",contentforother,task_id);
                                listoftasks.remove(position);
                                mAdapter.notifyDataSetChanged();
                                dialog.dismiss();


                            }
                })
                .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onEmployeeRemindButtonClicked(int position, EmployeeTask_Adapter.MyViewHolder holder) {
        final String task_id = listoftasks.get(position);
        String taskName = holder.employeename.getText().toString().trim();
        String contentforme = "You reminder "+name+" for "+taskName;//todo get taskname here
        sendNotif(mykey,mykey,"remindJob",contentforme,task_id);
        String contentforother= "Coordinator "+coordinatorSession.getName()+" reminded you of "+taskName;
        sendNotif(mykey,id,"remindJob",contentforother,task_id);

    }

    @Override
    public void onEmployeeInfoButtonClicked(int position) {
        Intent intent = new Intent(this,TaskDetail.class);
        intent.putExtra("task_id",listoftasks.get(position));
        startActivity(intent);
    }



    @Override
    public void onEmployeedotmenuButtonClicked(int position, final EmployeeTask_Adapter.MyViewHolder holder) {
        if (holder.buttonshow.getVisibility()==View.INVISIBLE)
        {
            holder.buttonshow.setVisibility(View.VISIBLE);
            holder.buttonshow.setAlpha(0.2f);
            holder.buttonshow
                    .animate()
                    .setDuration(500)
                    .alpha(1.0f)
                    .translationXBy(-holder.dotmenu.getWidth())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            //updateShowElementsButton();
                            holder.buttonshow.animate().setListener(null);
                        }
                    });

        }
        else {

            holder.buttonshow.setAlpha(0.6f);
            holder.buttonshow
                    .animate()
                    .setDuration(500)
                    .alpha(0.0f)
                    .translationXBy(holder.dotmenu.getWidth())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            //updateShowElementsButton();
                            holder.buttonshow.setVisibility(View.INVISIBLE);
                            holder.buttonshow.animate().setListener(null);

                        }
                    });
        }
    }

    private void checkChatref(final String mykey, final String otheruserkey) {
        DatabaseReference dbChat = DBREF.child("Chats").child(mykey+otheruserkey).getRef();
        dbChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("query1" + mykey+otheruserkey);
                System.out.println("datasnap 1" + dataSnapshot.toString());
                if (dataSnapshot.exists()) {
                    System.out.println("datasnap exists1" + dataSnapshot.toString());
                    dbTablekey = mykey+otheruserkey;
                    goToChatActivity();
                }
                else
                {
                    checkChatref2(mykey,otheruserkey);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void checkChatref2(final String mykey, final String otheruserkey) {
        final DatabaseReference dbChat = DBREF.child("Chats").child(otheruserkey+mykey).getRef();
        dbTablekey = otheruserkey+mykey;
        dbChat.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    System.out.println("query1" + otheruserkey+mykey);
                    goToChatActivity();
                }
                else
                {
                    DBREF.child("Users").child("Userchats").child(mykey).child(otheruserkey).setValue(dbTablekey);
                    DBREF.child("Users").child("Userchats").child(otheruserkey).child(mykey).setValue(dbTablekey);
                    goToChatActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void goToChatActivity()
    {
        Intent in = new Intent(this, ChatActivity.class);
        in.putExtra("dbTableKey",dbTablekey);
        in.putExtra("otheruserkey",id);
        startActivity(in);
    }
}