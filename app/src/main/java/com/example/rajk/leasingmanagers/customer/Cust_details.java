package com.example.rajk.leasingmanagers.customer;

import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rajk.leasingmanagers.MainViews.CreateTask;
import com.example.rajk.leasingmanagers.MainViews.TaskDetail;
import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.adapter.CustomerTasks_Adapter;
import com.example.rajk.leasingmanagers.model.CustomerAccount;
import com.example.rajk.leasingmanagers.model.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cust_details extends AppCompatActivity implements CustomerTasks_Adapter.CustomerTaskAdapterListener{

    AlertDialog customerEditDetails;
    String id,name,num,add,temp_name,temp_add,temp_num;
    EditText Name,Num,Add;
    DatabaseReference db,dbTask,dbaccountinfo;
    RecyclerView rec_customertasks;
    LinearLayoutManager linearLayoutManager;
    ValueEventListener dblistener,dbtasklistener,dbaccountlistener;
    private ArrayList<Task> TaskList= new ArrayList<>();
    private CustomerTasks_Adapter mAdapter;
    private ProgressDialog progressDialog;
    public AlertDialog customerAccountDialog;
    private List<String> listoftasks = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cust_details);
        progressDialog = new ProgressDialog(this);
        id = getIntent().getStringExtra("id");

        Name = (EditText) findViewById(R.id.name);
        Num = (EditText) findViewById(R.id.num);
        Add = (EditText) findViewById(R.id.add);

        rec_customertasks = (RecyclerView)findViewById(R.id.rec_customertasks);
        linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        rec_customertasks.setLayoutManager(linearLayoutManager);
        rec_customertasks.setItemAnimator(new DefaultItemAnimator());
        rec_customertasks.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));

        // get name, num and address from database using id and show them in activity

        db = FirebaseDatabase.getInstance().getReference().child("MeChat").child("Customer").child(id);
        dblistener =db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Map<String, String> map_new = (Map<String, String>) dataSnapshot.getValue();

                name = (map_new.get("name"));
                add = (map_new.get("address"));
                num = (map_new.get("phone_num"));
                getSupportActionBar().setTitle(name);
                Name.setText(name);
                Num.setText(num);
                Add.setText(add);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        dbTask = db.child("Task").getRef();
        dbtasklistener = dbTask.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    listoftasks.add(childSnapshot.getKey());
                }
                mAdapter = new CustomerTasks_Adapter(listoftasks,getApplication(),Cust_details.this);
                rec_customertasks.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cust_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item1:

                final EditText name_new,num_new,add_new;
                Button sub;
                customerEditDetails = new AlertDialog.Builder(this)
                        .setTitle("Edit Customer Details")
                        .setView(R.layout.edit_cust).setIcon(R.mipmap.ic_edit_pink)
                        .create();
                customerEditDetails.show();

                name_new = (EditText) customerEditDetails.findViewById(R.id.name);
                num_new = (EditText) customerEditDetails.findViewById(R.id.num);
                add_new = (EditText) customerEditDetails.findViewById(R.id.add);
                sub = (Button) customerEditDetails.findViewById(R.id.submit);

                name_new.setText(name);
                num_new.setText(num);
                add_new.setText(add);

                sub.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // update database accordingly
                        temp_add = add_new.getText().toString();
                        temp_name = name_new.getText().toString();
                        temp_num = num_new.getText().toString();

                        if(TextUtils.isEmpty(temp_add) || TextUtils.isEmpty(temp_name) || TextUtils.isEmpty(temp_num))
                            Toast.makeText(Cust_details.this,"Enter details...",Toast.LENGTH_SHORT).show();

                        else
                        {
                            db.child("name").setValue(temp_name);
                            db.child("address").setValue(temp_add);
                            db.child("phone_num").setValue(temp_num);

                            customerEditDetails.dismiss();
                        }
                    }
                });

                break;

            case R.id.item2:
                Intent intent = new Intent(Cust_details.this, CreateTask.class);
                intent.putExtra("customerId",id);
                intent.putExtra("customerName",Name.getText().toString().trim());
                startActivity(intent);
                finish();
                break;

            case R.id.item3:
                customerAccountDialog = new AlertDialog.Builder(this)
                        .setTitle("Account Information")
                        .setView(R.layout.account_info_layout).setIcon(R.mipmap.ic_account_info_pink)
                        .create();
                customerAccountDialog.show();
                final Button edit,submit;
                final EditText total,advance,balance;
                final LinearLayout balanceLayout;
                total = (EditText)customerAccountDialog.findViewById(R.id.total);
                advance = (EditText)customerAccountDialog.findViewById(R.id.advance);
                balance = (EditText)customerAccountDialog.findViewById(R.id.balance);
                edit = (Button)customerAccountDialog.findViewById(R.id.edit);
                submit = (Button)customerAccountDialog.findViewById(R.id.submit);
                balanceLayout = (LinearLayout)customerAccountDialog.findViewById(R.id.balanceLayout);
                 dbaccountinfo = db.child("Account").getRef();
                dbaccountlistener =dbaccountinfo.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {

                            CustomerAccount customerAccount = dataSnapshot.getValue(CustomerAccount.class);
                            total.setText(customerAccount.getTotal() + "");
                            advance.setText(customerAccount.getAdvance() + "");
                            balance.setText((customerAccount.getTotal() - customerAccount.getAdvance()) + "");
                        }}

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        total.setEnabled(true);
                        advance.setEnabled(true);
                        balanceLayout.setVisibility(View.GONE);
                        submit.setVisibility(View.VISIBLE);
                        edit.setVisibility(View.GONE);
                    }
                });

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CustomerAccount  customerAccount = new CustomerAccount();
                        customerAccount.setTotal(Integer.parseInt(total.getText().toString()));
                        customerAccount.setAdvance(Integer.parseInt(advance.getText().toString()));
                        dbaccountinfo.setValue(customerAccount);
                        total.setEnabled(false);
                        advance.setEnabled(false);
                        balanceLayout.setVisibility(View.VISIBLE);
                        submit.setVisibility(View.GONE);
                        edit.setVisibility(View.VISIBLE);

                    }
                });
                break;
            case R.id.item4:
                break;

            case  R.id.item6:
                dbaccountinfo.removeValue();
                break;


        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(db!=null && dblistener!=null)
        db.removeEventListener(dblistener);

        if(dbTask!=null && dbtasklistener!=null)
            dbTask.removeEventListener(dbtasklistener);

        if(dbaccountinfo!=null && dbaccountlistener!=null)
            dbaccountinfo.removeEventListener(dbaccountlistener);
    }

    @Override
    public void onCustomerTaskRowClicked(int position) {
        Intent intent = new Intent(this,TaskDetail.class);
        intent.putExtra("task_id",listoftasks.get(position));
        startActivity(intent);
    }
}