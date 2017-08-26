package com.example.rajk.leasingmanagers.customer;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.rajk.leasingmanagers.CoordinatorLogin.CoordinatorSession;
import com.example.rajk.leasingmanagers.MainViews.CreateTask;
import com.example.rajk.leasingmanagers.MainViews.TaskDetail;
import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.adapter.CustomerTasks_Adapter;
import com.example.rajk.leasingmanagers.chat.ChatActivity;
import com.example.rajk.leasingmanagers.helper.DividerItemDecoration;
import com.example.rajk.leasingmanagers.model.CustomerAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.rajk.leasingmanagers.LeasingManagers.DBREF;
import static com.example.rajk.leasingmanagers.LeasingManagers.sendNotif;
import static com.example.rajk.leasingmanagers.LeasingManagers.sendNotifToAllCoordinators;

public class Cust_details extends AppCompatActivity implements CustomerTasks_Adapter.CustomerTaskAdapterListener, View.OnClickListener {

    AlertDialog customerEditDetails;
    String id, name, num, add, temp_name, temp_add, temp_num;
    EditText Name, Num, Add;
    DatabaseReference db, dbTask, dbaccountinfo;
    RecyclerView rec_customertasks;
    LinearLayoutManager linearLayoutManager;
    private String dbTablekey, mykey;
    ValueEventListener dblistener, dbtasklistener, dbaccountlistener;
    private CustomerTasks_Adapter mAdapter;
    public AlertDialog customerAccountDialog;
    CoordinatorSession coordinatorSession;
    private Button quotationButton;
    private List<String> listoftasks = new ArrayList<>();
    ImageButton callme, msgme;
    private TextView pendingJobCount, completeJobCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cust_details);

        coordinatorSession = new CoordinatorSession(this);
        quotationButton = (Button) findViewById(R.id.quotation);
        quotationButton.setOnClickListener(this);
        id = getIntent().getStringExtra("id");
        mykey = coordinatorSession.getUsername();
        Name = (EditText) findViewById(R.id.name);
        Num = (EditText) findViewById(R.id.num);
        Add = (EditText) findViewById(R.id.add);
        callme = (ImageButton) findViewById(R.id.callme);
        msgme = (ImageButton) findViewById(R.id.msgme);
        pendingJobCount = (TextView) findViewById(R.id.pendingTasks);
        completeJobCount = (TextView) findViewById(R.id.completedTasks);

        rec_customertasks = (RecyclerView) findViewById(R.id.rec_customertasks);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rec_customertasks.setLayoutManager(linearLayoutManager);
        rec_customertasks.setItemAnimator(new DefaultItemAnimator());
        rec_customertasks.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));

        db = DBREF.child("Customer").child(id);
        dblistener = db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
        dbTask.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer pendingJobs = 0, completeJobs = 0;
                if (dataSnapshot.exists()) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String status = ds.getValue(String.class);
                        if (status.equals("pending")) {
                            pendingJobs++;
                            pendingJobCount.setText(String.valueOf(pendingJobs));
                        } else {
                            completeJobs++;
                            completeJobCount.setText(String.valueOf(completeJobs));
                        }
                    }
                }
                if (pendingJobs == 0)
                    quotationButton.setVisibility(View.GONE);
                else
                    quotationButton.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        dbtasklistener = dbTask.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    listoftasks.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        listoftasks.add(childSnapshot.getKey());
                    }
                    mAdapter = new CustomerTasks_Adapter(listoftasks, getApplication(), Cust_details.this, id);
                    rec_customertasks.setAdapter(mAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        msgme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkChatref(mykey, id);
            }
        });

        callme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + num));
                startActivity(callIntent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cust_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                final EditText name_new, num_new, add_new;
                Button sub;
                customerEditDetails = new AlertDialog.Builder(this)
                        .setView(R.layout.edit_cust)
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
                        temp_add = WordUtils.capitalizeFully(temp_add);
                        temp_name = name_new.getText().toString();
                        temp_name = WordUtils.capitalizeFully(temp_name);
                        temp_num = num_new.getText().toString();

                        if (TextUtils.isEmpty(temp_add) || TextUtils.isEmpty(temp_name) || TextUtils.isEmpty(temp_num))
                            Toast.makeText(Cust_details.this, "Enter details...", Toast.LENGTH_SHORT).show();

                        else {
                            db.child("name").setValue(temp_name);
                            db.child("address").setValue(temp_add);
                            db.child("phone_num").setValue(temp_num);
                            DBREF.child("Users").child("Usersessions").child(id).child("num").setValue(temp_num);

                            customerEditDetails.dismiss();
                        }
                    }
                });

                break;

            case R.id.item2:
                Intent intent = new Intent(Cust_details.this, CreateTask.class);
                intent.putExtra("customerId", id);
                intent.putExtra("customerName", Name.getText().toString().trim());
                startActivity(intent);
                finish();
                break;

            case R.id.item3:
                customerAccountDialog = new AlertDialog.Builder(this)
                        .setView(R.layout.account_info_layout)
                        .create();
                customerAccountDialog.show();
                final Button edit, submit;
                final EditText total, advance, balance;
                final LinearLayout balanceLayout;
                total = (EditText) customerAccountDialog.findViewById(R.id.total);
                advance = (EditText) customerAccountDialog.findViewById(R.id.advance);
                balance = (EditText) customerAccountDialog.findViewById(R.id.balance);
                edit = (Button) customerAccountDialog.findViewById(R.id.edit);
                submit = (Button) customerAccountDialog.findViewById(R.id.submit);
                balanceLayout = (LinearLayout) customerAccountDialog.findViewById(R.id.balanceLayout);
                dbaccountinfo = db.child("Account").getRef();
                dbaccountlistener = dbaccountinfo.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            CustomerAccount customerAccount = dataSnapshot.getValue(CustomerAccount.class);
                            total.setText("Total "+customerAccount.getTotal() + "");
                            advance.setText("Advance "+customerAccount.getAdvance() + "");
                            balance.setText((customerAccount.getTotal() - customerAccount.getAdvance()) + "");
                        }
                    }

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
                        CustomerAccount customerAccount = new CustomerAccount();
                        Integer total_amount = Integer.parseInt(total.getText().toString().trim());
                        customerAccount.setTotal(total_amount);
                        Integer advance_amount =   Integer.parseInt(advance.getText().toString().trim());
                        customerAccount.setAdvance(advance_amount);
                        dbaccountinfo.setValue(customerAccount);
                        total.setEnabled(false);
                        advance.setEnabled(false);
                        balanceLayout.setVisibility(View.VISIBLE);
                        submit.setVisibility(View.GONE);
                        edit.setVisibility(View.VISIBLE);
                        sendNotif(mykey, id, "accountReset", "Your advance deposited is Rs." + advance_amount + " and balance left is Rs." + (total_amount - advance_amount), id);
                        sendNotifToAllCoordinators(mykey, "accountReset", name + " advance deposited is Rs." + advance_amount + " and balance left is Rs." + (total_amount - advance_amount), id);

                    }
                });
                break;
            case R.id.item6:
                // TODO : Null pointer exception (Null Object Refrence)
                //if nothing is added to account this error would occur
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Are you sure you want to reset the account?")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, int id) {
                                final DatabaseReference dbAccount = db.child("Account").getRef();
                                dbAccount.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChildren())
                                            dbAccount.removeValue();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }


                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

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

        if (db != null && dblistener != null)
            db.removeEventListener(dblistener);

        if (dbTask != null && dbtasklistener != null)
            dbTask.removeEventListener(dbtasklistener);

        if (dbaccountinfo != null && dbaccountlistener != null)
            dbaccountinfo.removeEventListener(dbaccountlistener);
    }

    @Override
    public void onCustomerTaskRowClicked(int position) {
        Intent intent = new Intent(this, TaskDetail.class);
        intent.putExtra("task_id", listoftasks.get(position));
        startActivity(intent);
    }


    private void checkChatref(final String mykey, final String otheruserkey) {
        DatabaseReference dbChat = DBREF.child("Chats").child(mykey + otheruserkey).getRef();
        dbChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dbTablekey = mykey + otheruserkey;
                    goToChatActivity();

                } else {
                    checkChatref2(mykey, otheruserkey);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void checkChatref2(final String mykey, final String otheruserkey) {
        final DatabaseReference dbChat = DBREF.child("Chats").child(otheruserkey + mykey).getRef();
        dbTablekey = otheruserkey + mykey;
        dbChat.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    goToChatActivity();
                } else {

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

    private void goToChatActivity() {
        Intent in = new Intent(this, ChatActivity.class);
        in.putExtra("dbTableKey", dbTablekey);
        in.putExtra("otheruserkey", id);
        startActivity(in);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.quotation:
                Intent intent = new Intent(this, UploadQuotationActivity.class);
                intent.putExtra("custId", id);
                intent.putExtra("customerName",name);
                intent.putExtra("custName", name);
                startActivity(intent);
                break;
        }
    }

}