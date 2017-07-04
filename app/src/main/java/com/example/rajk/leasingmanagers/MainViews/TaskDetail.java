package com.example.rajk.leasingmanagers.MainViews;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rajk.leasingmanagers.ForwardTask.forwardTask;
import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.adapter.ViewImageAdapter;
import com.example.rajk.leasingmanagers.adapter.assignedto_adapter;
import com.example.rajk.leasingmanagers.adapter.bigimage_adapter;
import com.example.rajk.leasingmanagers.adapter.measurement_adapter;
import com.example.rajk.leasingmanagers.adapter.taskdetailDescImageAdapter;
import com.example.rajk.leasingmanagers.adapter.taskimagesadapter;
import com.example.rajk.leasingmanagers.helper.FilePath;
import com.example.rajk.leasingmanagers.model.CompletedBy;
import com.example.rajk.leasingmanagers.model.Quotation;
import com.example.rajk.leasingmanagers.model.Task;
import com.example.rajk.leasingmanagers.model.measurement;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TaskDetail extends AppCompatActivity implements taskdetailDescImageAdapter.ImageAdapterListener, assignedto_adapter.assignedto_adapterListener{

    private DatabaseReference dbRef, dbTask,dbCompleted,dbAssigned,dbMeasurement,dbDescImages;
    ImageButton upload,download;
    private String task_id;
    private Task task;
    private String customername;
    EditText startDate,endDate,custId,taskName,quantity,description;
    private static final int PICK_FILE_REQUEST = 1;
    RecyclerView rec_assignedto,rec_completedby,rec_measurement, rec_DescImages ;
    assignedto_adapter adapter_assignedto,adapter_completedby;
    taskdetailDescImageAdapter adapter_taskimages;
    ArrayList<String> DescImages = new ArrayList<>();
    List<CompletedBy> assignedtoList = new ArrayList<>();
    FloatingActionButton forward;
    List<CompletedBy> completedbyList = new ArrayList<>();
    ArrayList<measurement> measurementList = new ArrayList<>();
    measurement_adapter adapter_measurement;
    TextView open_assignedto,open_completedby,open_measurement,appByCustomer,uploadStatus;
    DatabaseReference dbQuotation;
    ProgressDialog progressDialog ;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private AlertDialog viewSelectedImages ;
    LinearLayoutManager linearLayoutManager;
    bigimage_adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        dbRef = FirebaseDatabase.getInstance().getReference().child("MeChat");
        progressDialog = new ProgressDialog(this);
        upload = (ImageButton)findViewById(R.id.upload);
        download = (ImageButton)findViewById(R.id.download);
        uploadStatus = (TextView)findViewById(R.id.uploadStatus);
        appByCustomer = (TextView)findViewById(R.id.appByCustomer);

        forward = (FloatingActionButton)findViewById(R.id.forward);
        taskName = (EditText) findViewById(R.id.taskName);
        startDate = (EditText) findViewById(R.id.startDate);
        endDate = (EditText) findViewById(R.id.endDate);
        quantity=(EditText) findViewById(R.id.quantity);
        description = (EditText) findViewById(R.id.description);
        custId = (EditText) findViewById(R.id.custId);
        rec_assignedto = (RecyclerView)findViewById(R.id.rec_assignedto);
        rec_completedby = (RecyclerView)findViewById(R.id.rec_completedby);
        rec_measurement = (RecyclerView)findViewById(R.id.rec_measurement);
        rec_DescImages = (RecyclerView)findViewById(R.id.rec_DescImages);
        open_assignedto = (TextView)findViewById(R.id.open_assignedto);
        open_completedby = (TextView)findViewById(R.id.open_completedby);
        open_measurement = (TextView)findViewById(R.id.open_measurement);

        Intent intent = getIntent();
        task_id = intent.getStringExtra("task_id");

        rec_assignedto.setLayoutManager(new LinearLayoutManager(this));
        rec_assignedto.setItemAnimator(new DefaultItemAnimator());
        rec_assignedto.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter_assignedto = new assignedto_adapter(assignedtoList, getApplicationContext(),"AssignedTo",task_id,this);
        rec_assignedto.setAdapter(adapter_assignedto);

        rec_completedby.setLayoutManager(new LinearLayoutManager(this));
        rec_completedby.setItemAnimator(new DefaultItemAnimator());
        rec_completedby.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter_completedby = new assignedto_adapter(completedbyList, getApplicationContext(),"CompletedBy",task_id,this);
        rec_completedby.setAdapter(adapter_completedby);

        rec_measurement.setLayoutManager(new LinearLayoutManager(this));
        rec_measurement.setItemAnimator(new DefaultItemAnimator());
        rec_measurement.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter_measurement = new measurement_adapter(measurementList, this);
        rec_measurement.setAdapter(adapter_measurement);

        rec_DescImages.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        rec_DescImages.setItemAnimator(new DefaultItemAnimator());
        rec_DescImages.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL));
        adapter_taskimages = new taskdetailDescImageAdapter(DescImages, getApplicationContext(),this);
        rec_DescImages.setAdapter(adapter_taskimages);

        dbTask = dbRef.child("Task").child(task_id);
        dbQuotation = dbTask.child("Quotation").getRef();
        dbCompleted = dbTask.child("CompletedBy").getRef();
        dbAssigned = dbTask.child("AssignedTo").getRef();
        dbMeasurement = dbTask.child("Measurement").getRef();
        dbDescImages = dbTask.child("DescImages").getRef();

        prepareListData();

        open_measurement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (rec_measurement.getVisibility()== View.GONE)
                {
                    rec_measurement.setVisibility(View.VISIBLE);
                }
                else
                {
                    rec_measurement.setVisibility(View.GONE);
                }
            }
        });


        open_completedby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rec_completedby.getVisibility()== View.GONE)
                {
                    rec_completedby.setVisibility(View.VISIBLE);
                }
                else
                {
                    rec_completedby.setVisibility(View.GONE);
                }
            }
        });

        open_assignedto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rec_assignedto.getVisibility()== View.GONE)
                {
                    rec_assignedto.setVisibility(View.VISIBLE);
                }
                else
                {
                    rec_assignedto.setVisibility(View.GONE);
                }
            }
        });
        dbTask.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                task = dataSnapshot.getValue(Task.class);
                setValue(task);
                getSupportActionBar().setTitle(task.getName());
                DatabaseReference dbCustomerName = FirebaseDatabase.getInstance().getReference().child("MeChat").child("Customer").child(task.getCustomerId()).getRef();
                dbCustomerName.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        customername = dataSnapshot.child("name").getValue(String.class);
                        getSupportActionBar().setSubtitle(customername);
                        custId.setText(task.getCustomerId()+ ": "+customername);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1  = new Intent(TaskDetail.this,forwardTask.class);
                intent1.putExtra("task_id",task_id);
                startActivity(intent1);
                finish();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                //sets the select file to all types of files
                intent.setType("*/*");
                //allows to select data and return it
                intent.setAction(Intent.ACTION_GET_CONTENT);
                //starts new activity to select file and return data
                startActivityForResult(Intent.createChooser(intent,"Choose File to Upload.."),PICK_FILE_REQUEST);

            }
        });
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                    checkPermission();
                } else {
                    launchLibrary();
                }
            }
        });

    }
 private void launchLibrary()
 {
     dbQuotation.addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
             if(dataSnapshot.exists())
             {
                 showpd("Downloading");
                 Quotation quotation=dataSnapshot.getValue(Quotation.class);
                 File localFile = null;
                 localFile = new File(Environment.getExternalStorageDirectory(), "Management/Quotation");
                 // Create direcorty if not exists
                 if (!localFile.exists()) {
                     localFile.mkdirs();
                 }

                 File myDownloadedFile = new File(localFile, task_id+"Quotation.pdf");
                 StorageReference storageReference =mStorageRef.child("Quotation").child(task_id);
                         storageReference.getFile(myDownloadedFile)
                         .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                             @Override
                             public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                 // Successfully downloaded data to local file
                                 // ...
                                 hidepd();
                                 Toast.makeText(TaskDetail.this,"Successfully downloaded",Toast.LENGTH_SHORT).show();
                             }
                         }).addOnFailureListener(new OnFailureListener() {
                     @Override
                     public void onFailure(@NonNull Exception exception) {
                         // Handle failed download
                         // ...
                         String s = exception.toString();
                         hidepd();
                         Toast.makeText(TaskDetail.this,"Download Failed",Toast.LENGTH_SHORT).show();
                     }


                 });
             }
             else
             {
                 Toast.makeText(TaskDetail.this,"No quotation uploaded yet!!",Toast.LENGTH_SHORT).show();
             }
         }

         @Override
         public void onCancelled(DatabaseError databaseError) {

         }
     });
 }


 private void checkPermission()
 {
     if (ContextCompat.checkSelfPermission(TaskDetail.this,
             Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
             && ContextCompat.checkSelfPermission(TaskDetail.this,
             Manifest.permission.READ_EXTERNAL_STORAGE)
             != PackageManager.PERMISSION_GRANTED) {//Can add more as per requirement

         ActivityCompat.requestPermissions(TaskDetail.this,
                 new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                 1);

     }

     else
     {
         launchLibrary();
     }

 }
    private void prepareListData()
    {
        dbCompleted.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    CompletedBy item = dataSnapshot.getValue(CompletedBy.class);
                    completedbyList.add(item);
                    adapter_completedby.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                CompletedBy item = dataSnapshot.getValue(CompletedBy.class);
                completedbyList.remove(item);
                adapter_completedby.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        dbAssigned.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    CompletedBy item = dataSnapshot.getValue(CompletedBy.class);
                    assignedtoList.add(item);
                    adapter_assignedto.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                CompletedBy item = dataSnapshot.getValue(CompletedBy.class);
                assignedtoList.remove(item);
                adapter_assignedto.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        dbMeasurement.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                if (dataSnapshot.exists()) {
                    measurement item = dataSnapshot.getValue(measurement.class);
                    measurementList.add(item);
                    adapter_measurement.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                measurement item = dataSnapshot.getValue(measurement.class);
                measurementList.remove(item);
                adapter_measurement.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        dbDescImages.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                if (dataSnapshot.exists()) {
                    rec_DescImages.setVisibility(View.VISIBLE);
                    String item = dataSnapshot.getValue(String.class);
                    DescImages.add(item);
                    adapter_taskimages.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String item = dataSnapshot.getKey();
                DescImages.remove(item);
                adapter_taskimages.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void setValue(Task task)
    {
        startDate.setText(task.getStartDate());
        endDate.setText(task.getExpEndDate());
        taskName.setText(task.getName());
        quantity.setText(task.getQty());
        if (!task.getDesc().equals("")) {
            description.setVisibility(View.VISIBLE);
            description.setText(task.getDesc());
        }
        dbQuotation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    appByCustomer.setVisibility(View.VISIBLE);
                    Quotation quotation = dataSnapshot.getValue(Quotation.class);
                    appByCustomer.setText(" "+quotation.getApprovedByCust());
                    uploadStatus.setText(" Yes");
                }
                else
                {
                    appByCustomer.setVisibility(View.GONE);
                    uploadStatus.setText(" No");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == PICK_FILE_REQUEST){
                if(data == null){
                    //no data present
                    return;
                }

                String selectedFilePath="";
                Uri selectedFileUri = data.getData();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    selectedFilePath = FilePath.getPath(this,selectedFileUri);
                }

                if(selectedFilePath != null && !selectedFilePath.equals("")){
                    StorageReference riversRef = mStorageRef.child("Quotation").child(task_id);

                    showpd("Uploading");
                    riversRef.putFile(selectedFileUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Get a URL to the uploaded content
                                    Quotation quotation = new Quotation("No");
                                    dbQuotation.setValue(quotation);
                                    Toast.makeText(TaskDetail.this,"Successfully Uploaded",Toast.LENGTH_SHORT).show();
                                    hidepd();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(TaskDetail.this,"Failed to Upload",Toast.LENGTH_SHORT).show();
                                    hidepd();
                                }
                            });
                }else{
                    Toast.makeText(this,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    void showpd(String text)
    {
        progressDialog.setMessage(text);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    void hidepd()
    {
        progressDialog.dismiss();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchLibrary();
                } else {
                    checkPermission();
                }
                return;
            }

        }
    }

    @Override
    public void onImageClicked(int position) {

        viewSelectedImages = new AlertDialog.Builder(TaskDetail.this)
                .setTitle("Images").setView(R.layout.view_image_on_click).create();
        viewSelectedImages.show();

        RecyclerView bigimage = (RecyclerView)viewSelectedImages.findViewById(R.id.bigimage);

        linearLayoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false);
        bigimage.setLayoutManager(linearLayoutManager);
        bigimage.setItemAnimator(new DefaultItemAnimator());
        bigimage.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.HORIZONTAL));

        adapter = new bigimage_adapter(DescImages, this);
        bigimage.setAdapter(adapter);

        bigimage.scrollToPosition(position);

    }

    @Override
    public void onRemoveButtonClicked(final int position)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to un-assign this task")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DatabaseReference dbCancelJob = FirebaseDatabase.getInstance().getReference().child("MeChat").child("Task").child(task_id).child("AssignedTo").child(adapter_assignedto.emp.getEmpId()).getRef();
                        dbCancelJob.removeValue();
                        assignedtoList.remove(position);

                        DatabaseReference dbEmployee = FirebaseDatabase.getInstance().getReference().child("MeChat").child("Employee").child(adapter_assignedto.emp.getEmpId()).child("AssignedTask").child(task_id);
                        dbEmployee.removeValue(); //for employee

                        adapter_assignedto.notifyDataSetChanged();

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
    public void onRemindButtonClicked(int position) {

    }
}
