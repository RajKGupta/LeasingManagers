package com.example.rajk.leasingmanagers.employee;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.customer.Cust_add;
import com.example.rajk.leasingmanagers.model.GlobalEmployee;
import com.example.rajk.leasingmanagers.tablayout.Tabs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.text.WordUtils;

import static com.example.rajk.leasingmanagers.LeasingManagers.AppName;
import static com.example.rajk.leasingmanagers.LeasingManagers.DBREF;

public class Emp_add extends AppCompatActivity {

    String name, num, add, desig, username, password;
    EditText Name, Num, Add, Username, Password;
    AutoCompleteTextView Desig;
    Button submit;
    DatabaseReference db = DBREF.child("Employee").getRef();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_add);

        getSupportActionBar().setTitle("Add New Employee");
        getSupportActionBar().setIcon(R.mipmap.ic_new_person_white);
        Name = (EditText) findViewById(R.id.name);
        Num = (EditText) findViewById(R.id.num);
        Add = (EditText) findViewById(R.id.add);
        Desig = (AutoCompleteTextView) findViewById(R.id.desig);
        Username = (EditText) findViewById(R.id.username);
        Password = (EditText) findViewById(R.id.password);
        submit = (Button) findViewById(R.id.submit);
        String[] designations = getResources().getStringArray(R.array.designations);
        ArrayAdapter<String> adapterstate = new ArrayAdapter<String>
                (this,android.R.layout.simple_dropdown_item_1line,designations);
        Desig.setAdapter(adapterstate);
        Desig.setThreshold(0);//will start working from first character
   //     Desig.setTextColor(Color.BLACK);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = Name.getText().toString().trim();
                name = WordUtils.capitalizeFully(name);
                num = Num.getText().toString().trim();
                add = Add.getText().toString().trim();
                add = WordUtils.capitalizeFully(add);
                desig = Desig.getText().toString().trim();
                desig = WordUtils.capitalize(desig);
                username = Username.getText().toString().trim();
                password = Password.getText().toString().trim();

                if (!((TextUtils.isEmpty(name) || TextUtils.isEmpty(num) || TextUtils.isEmpty(add) || TextUtils.isEmpty(desig) || (TextUtils.isEmpty(username) || TextUtils.isEmpty(password))))) {
                    // upload info to database

                    final DatabaseReference dbnewEmp = db.child(username).getRef();
                    dbnewEmp.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Toast.makeText(Emp_add.this, "Username Already Exists!!", Toast.LENGTH_SHORT).show();
                            } else {
                                Employee employee = new Employee(getRandomMaterialColor("400"), name, num, add, desig, username, password);
                                dbnewEmp.setValue(employee);
                                DBREF.child("Users").child("Usersessions").child(username).child("name").setValue(name);
                                DBREF.child("Users").child("Usersessions").child(username).child("online").setValue(Boolean.FALSE);
                                DBREF.child("Fcmtokens").child(username).child("token").setValue("nil");
                                DBREF.child("Users").child("Usersessions").child(username).child("num").setValue(num);
                                String id = AppName+"_"+username;
                                GlobalEmployee employee1 = new GlobalEmployee(name,num,add,desig, AppName+"_"+username,"na");
                                FirebaseDatabase.getInstance().getReference().child("GlobalEmployee").child("EmployeeDetail").child(id).setValue(employee1);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    Intent intent = new Intent(Emp_add.this, Tabs.class);
                    intent.putExtra("page", 2);
                    startActivity(intent);
                    finish();
                } else
                    Toast.makeText(Emp_add.this, "Please enter all details", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", getPackageName());

        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        Intent intent = new Intent(Emp_add.this,Tabs.class);
                        intent.putExtra("page",1);
                        startActivity(intent);
                        finish();
                    }


                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }
}