package com.example.rajk.leasingmanagers.MyProfile;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import com.example.rajk.leasingmanagers.helper.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.adapter.phonebook_adapter;
import com.example.rajk.leasingmanagers.model.Phonebook;
import com.example.rajk.leasingmanagers.tablayout.Tabs;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import static com.example.rajk.leasingmanagers.LeasingManagers.DBREF;

public class phonebook extends AppCompatActivity implements phonebook_adapter.phonebook_adapterListener {

    RecyclerView rec_contact_list;
    ArrayList<Phonebook> contact_list = new ArrayList<>();
    phonebook_adapter phonebook_adapter;
    LinearLayoutManager linearLayoutManager;
    DatabaseReference dbCoordinator;
    private AlertDialog add_contacts;
    String Name, Desig, Contact, Email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonebook);

        rec_contact_list = (RecyclerView) findViewById(R.id.contact_list);

        phonebook_adapter = new phonebook_adapter(contact_list, getApplicationContext(), this);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rec_contact_list.setLayoutManager(linearLayoutManager);
        rec_contact_list.setItemAnimator(new DefaultItemAnimator());
        rec_contact_list.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
        rec_contact_list.setAdapter(phonebook_adapter);

        dbCoordinator = DBREF.child("Contacts").getRef();

        LoadData();
    }

    void LoadData() {

        dbCoordinator.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    Phonebook phonebook = dataSnapshot.getValue(Phonebook.class);
                    contact_list.add(phonebook);
                    phonebook_adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.phonebook_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                add_contacts = new AlertDialog.Builder(phonebook.this)
                        .setView(R.layout.add_contact_dialogue).create();
                add_contacts.show();

                final EditText name = (EditText) add_contacts.findViewById(R.id.name);
                final EditText designation = (EditText) add_contacts.findViewById(R.id.designation);
                final EditText contact = (EditText) add_contacts.findViewById(R.id.contact);
                final EditText email = (EditText) add_contacts.findViewById(R.id.email);
                Button save = (Button) add_contacts.findViewById(R.id.oksave);
                Button cancel = (Button) add_contacts.findViewById(R.id.okcancel);

                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Name = name.getText().toString().trim();
                        Desig = designation.getText().toString().trim();
                        Contact = contact.getText().toString().trim();
                        Email = email.getText().toString().trim();

                        if (TextUtils.isEmpty(Name) || TextUtils.isEmpty(Desig) || (TextUtils.isEmpty(Contact))) {
                            Toast.makeText(phonebook.this, "Fill all the details", Toast.LENGTH_SHORT).show();
                        } else {
                            Phonebook phonebook = new Phonebook(Contact, Name, Desig, Email);
                            DBREF.child("Contacts").child(Contact).setValue(phonebook);

                            Toast.makeText(phonebook.this, "Contact Added", Toast.LENGTH_SHORT).show();

                            add_contacts.dismiss();
                        }

                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        add_contacts.dismiss();
                    }
                });
                break;
        }
        return true;
    }

    @Override
    public void onCALLMEclicked(int position) {
        Phonebook phonebook = contact_list.get(position);
        String num = phonebook.getContact();
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + num));
        startActivity(callIntent);
    }

    @Override
    public void onDELETEMEclicked(final int position)
    {
        Contact = contact_list.get(position).getContact();
        final AlertDialog.Builder builder = new AlertDialog.Builder(phonebook.this);
        builder.setMessage("Are you sure you want to delete this contact")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int i) {
                        DBREF.child("Contacts").child(Contact).removeValue();
                        contact_list.remove(position);
                        phonebook_adapter.notifyDataSetChanged();
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
    public void onBackPressed() {
        Intent intent = new Intent(this, Tabs.class);
        startActivity(intent);
        finish();
    }

}