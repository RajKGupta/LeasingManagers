package com.example.rajk.leasingmanagers.MainViews;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rajk.leasingmanagers.CoordinatorLogin.CoordinatorSession;
import com.example.rajk.leasingmanagers.ForwardTask.forwardTask;
import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.adapter.ViewImageAdapter;
import com.example.rajk.leasingmanagers.adapter.assignedto_adapter;
import com.example.rajk.leasingmanagers.adapter.bigimage_adapter;
import com.example.rajk.leasingmanagers.adapter.completedBy_adapter;
import com.example.rajk.leasingmanagers.adapter.measurement_adapter;
import com.example.rajk.leasingmanagers.adapter.taskdetailDescImageAdapter;
import com.example.rajk.leasingmanagers.adapter.taskimagesadapter;
import com.example.rajk.leasingmanagers.helper.CompressMe;
import com.example.rajk.leasingmanagers.helper.DividerItemDecoration;
import com.example.rajk.leasingmanagers.helper.MarshmallowPermissions;
import com.example.rajk.leasingmanagers.listener.ClickListener;
import com.example.rajk.leasingmanagers.listener.RecyclerTouchListener;
import com.example.rajk.leasingmanagers.measurement.MeasureList;
import com.example.rajk.leasingmanagers.model.CompletedBy;
import com.example.rajk.leasingmanagers.model.CompletedJob;
import com.example.rajk.leasingmanagers.model.Quotation;
import com.example.rajk.leasingmanagers.model.Task;
import com.example.rajk.leasingmanagers.model.measurement;
import com.example.rajk.leasingmanagers.services.DeleteTask;
import com.example.rajk.leasingmanagers.services.DownloadFileService;
import com.example.rajk.leasingmanagers.services.UploadTaskPhotosServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.zfdang.multiple_images_selector.ImagesSelectorActivity;
import com.zfdang.multiple_images_selector.SelectorSettings;

import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.rajk.leasingmanagers.LeasingManagers.AppName;
import static com.example.rajk.leasingmanagers.LeasingManagers.DBREF;
import static com.example.rajk.leasingmanagers.LeasingManagers.sendNotif;
import static com.example.rajk.leasingmanagers.LeasingManagers.sendNotifToAllCoordinators;
import static com.example.rajk.leasingmanagers.LeasingManagers.simpleDateFormat;

public class TaskDetail extends AppCompatActivity implements taskdetailDescImageAdapter.ImageAdapterListener, assignedto_adapter.assignedto_adapterListener, bigimage_adapter.bigimage_adapterListener {

    private DatabaseReference dbRef, dbTask, dbCompleted, dbAssigned, dbMeasurement, dbDescImages;
    ValueEventListener dbTaskVle;
    ImageButton download;
    ProgressBar progressBar;
    public static String task_id;
    private String mykey;
    private Task task;
    private String customername;
    EditText startDate, endDate, quantity, description;
    RecyclerView rec_assignedto, rec_completedby, rec_measurement, rec_DescImages;
    assignedto_adapter adapter_assignedto;
    completedBy_adapter adapter_completedby;
    taskdetailDescImageAdapter adapter_taskimages;
    ArrayList<String> DescImages = new ArrayList<>();
    List<CompletedBy> assignedtoList = new ArrayList<>();
    FloatingActionButton forward;
    List<CompletedJob> completedbyList = new ArrayList<>();
    ArrayList<measurement> measurementList = new ArrayList<>();
    measurement_adapter adapter_measurement;
    TextView measure_and_hideme, assign_and_hideme, complete_and_hideme, appByCustomer, uploadStatus;
    DatabaseReference dbQuotation;
    ProgressDialog progressDialog;
    private MarshmallowPermissions marshmallowPermissions;
    private AlertDialog viewSelectedImages, open_options, edit_description;
    LinearLayoutManager linearLayoutManager;
    bigimage_adapter adapter;
    private CoordinatorSession coordinatorSession;
    ImageButton written_desc, photo_desc;
    private int REQUEST_CODE = 1;
    private ArrayList<String> mResults;
    CompressMe compressMe;
    private ArrayList<String> picUriList = new ArrayList<>();
    ViewImageAdapter madapter;
    Button measure;
    AlertDialog taskEditDetails;
    String temp_taskname,temp_qty,temp_enddate;
    DatabaseReference dbedit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        coordinatorSession = new CoordinatorSession(this);
        mykey = coordinatorSession.getUsername();
        marshmallowPermissions = new MarshmallowPermissions(this);
        dbRef = DBREF;
        progressDialog = new ProgressDialog(this);
        download = (ImageButton) findViewById(R.id.download);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        uploadStatus = (TextView) findViewById(R.id.uploadStatus);
        appByCustomer = (TextView) findViewById(R.id.appByCustomer);
        measure_and_hideme = (TextView) findViewById(R.id.measure_and_hideme);
        assign_and_hideme = (TextView) findViewById(R.id.assign_and_hideme);
        complete_and_hideme = (TextView) findViewById(R.id.complete_and_hideme);
        forward = (FloatingActionButton) findViewById(R.id.forward);
        startDate = (EditText) findViewById(R.id.startDate);
        endDate = (EditText) findViewById(R.id.endDate);
        quantity = (EditText) findViewById(R.id.quantity);
        description = (EditText) findViewById(R.id.description);
        rec_assignedto = (RecyclerView) findViewById(R.id.rec_assignedto);
        rec_completedby = (RecyclerView) findViewById(R.id.rec_completedby);
        rec_measurement = (RecyclerView) findViewById(R.id.rec_measurement);
        rec_DescImages = (RecyclerView) findViewById(R.id.rec_DescImages);
        photo_desc = (ImageButton) findViewById(R.id.photo_desc);
        written_desc = (ImageButton) findViewById(R.id.written_desc);
        measure = (Button) findViewById(R.id.measure);

        Intent intent = getIntent();
        task_id = intent.getStringExtra("task_id");
        compressMe = new CompressMe(this);

        rec_assignedto.setLayoutManager(new LinearLayoutManager(this));
        rec_assignedto.setItemAnimator(new DefaultItemAnimator());
        rec_assignedto.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter_assignedto = new assignedto_adapter(assignedtoList, getApplicationContext(), task_id, this);
        rec_assignedto.setAdapter(adapter_assignedto);

        rec_completedby.setLayoutManager(new LinearLayoutManager(this));
        rec_completedby.setItemAnimator(new DefaultItemAnimator());
        rec_completedby.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter_completedby = new completedBy_adapter(completedbyList, getApplicationContext(), task_id);
        rec_completedby.setAdapter(adapter_completedby);

        rec_measurement.setLayoutManager(new LinearLayoutManager(this));
        rec_measurement.setItemAnimator(new DefaultItemAnimator());
        rec_measurement.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter_measurement = new measurement_adapter(measurementList, this);
        rec_measurement.setAdapter(adapter_measurement);

        rec_DescImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rec_DescImages.setItemAnimator(new DefaultItemAnimator());
        rec_DescImages.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL));
        adapter_taskimages = new taskdetailDescImageAdapter(DescImages, getApplicationContext(), this);
        rec_DescImages.setAdapter(adapter_taskimages);

        dbTask = dbRef.child("Task").child(task_id);
        dbQuotation = dbTask.child("Quotation").getRef();
        dbCompleted = dbTask.child("CompletedBy").getRef();
        dbAssigned = dbTask.child("AssignedTo").getRef();
        dbMeasurement = dbTask.child("Measurement").getRef();
        dbDescImages = dbTask.child("DescImages").getRef();


        measurementList.clear();
        prepareListData();


        measure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TaskDetail.this, MeasureList.class));
            }
        });



        dbTaskVle = dbTask.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                task = dataSnapshot.getValue(Task.class);
                setValue(task);
                getSupportActionBar().setTitle(task.getName());
                DatabaseReference dbCustomerName = DBREF.child("Customer").child(task.getCustomerId()).getRef();
                dbCustomerName.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        customername = dataSnapshot.child("name").getValue(String.class);
                        getSupportActionBar().setSubtitle(customername);
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
                Intent intent1 = new Intent(TaskDetail.this, forwardTask.class);
                intent1.putExtra("task_id", task_id);
                startActivity(intent1);
                finish();
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!marshmallowPermissions.checkPermissionForCamera()) {
                    marshmallowPermissions.requestPermissionForCamera();
                    if (!marshmallowPermissions.checkPermissionForExternalStorage()) {
                        marshmallowPermissions.requestPermissionForExternalStorage();
                    } else
                        launchLibrary();
                } else {

                    launchLibrary();
                }
            }
        });

        written_desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_description = new AlertDialog.Builder(TaskDetail.this)
                        .setView(R.layout.edit_description).create();
                edit_description.show();

                final EditText description2 = (EditText) edit_description.findViewById(R.id.description);
                Button oksave = (Button) edit_description.findViewById(R.id.oksave);
                Button okcancel = (Button) edit_description.findViewById(R.id.okcancel);

                String desc;
                if (description.getVisibility() == View.VISIBLE) {
                    desc = description.getText().toString().trim();
                    description2.setText(desc);
                }
                oksave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newdesc = description2.getText().toString().trim();
                        DBREF.child("Task").child(task_id).child("desc").setValue(newdesc);
                        edit_description.dismiss();
                    }
                });

                okcancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        edit_description.dismiss();
                    }
                });
            }
        });

        photo_desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!marshmallowPermissions.checkPermissionForCamera() && !marshmallowPermissions.checkPermissionForExternalStorage()) {
                    ActivityCompat.requestPermissions(TaskDetail.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                            2);
                } else {
                    Intent intent = new Intent(TaskDetail.this, ImagesSelectorActivity.class);
                    intent.putExtra(SelectorSettings.SELECTOR_MAX_IMAGE_NUMBER, 5);
                    intent.putExtra(SelectorSettings.SELECTOR_SHOW_CAMERA, true);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (data != null) {
                mResults = data.getStringArrayListExtra(SelectorSettings.SELECTOR_RESULTS);
                assert mResults != null;

                System.out.println(String.format("Totally %d images selected:", mResults.size()));
                for (String result : mResults) {
                    String l = compressMe.compressImage(result, getApplicationContext());
                    picUriList.add(l);
                }
                if (picUriList.size() > 0) {
                    viewSelectedImages = new AlertDialog.Builder(TaskDetail.this)
                            .setTitle("Selected Images").setView(R.layout.activity_view_selected_image).create();
                    viewSelectedImages.show();

                    final ImageView ImageViewlarge = (ImageView) viewSelectedImages.findViewById(R.id.ImageViewlarge);
                    ImageButton cancel = (ImageButton) viewSelectedImages.findViewById(R.id.cancel);
                    Button canceldone = (Button) viewSelectedImages.findViewById(R.id.canceldone);
                    Button okdone = (Button) viewSelectedImages.findViewById(R.id.okdone);
                    RecyclerView rv = (RecyclerView) viewSelectedImages.findViewById(R.id.viewImages);

                    linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                    rv.setLayoutManager(linearLayoutManager);
                    rv.setItemAnimator(new DefaultItemAnimator());
                    rv.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.HORIZONTAL));

                    madapter = new ViewImageAdapter(picUriList, this);
                    rv.setAdapter(madapter);

                    final String[] item = {picUriList.get(0)};
                    ImageViewlarge.setImageURI(Uri.parse(item[0]));

                    rv.addOnItemTouchListener(new RecyclerTouchListener(this, rv, new ClickListener() {
                        @Override
                        public void onClick(View view, int position) {
                            madapter.selectedPosition = position;
                            madapter.notifyDataSetChanged();
                            item[0] = picUriList.get(position);
                            ImageViewlarge.setImageURI(Uri.parse(item[0]));
                        }

                        @Override
                        public void onLongClick(View view, int position) {

                        }
                    }));

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int i = picUriList.indexOf(item[0]);
                            if (i == picUriList.size() - 1)
                                i = 0;
                            picUriList.remove(item[0]);
                            madapter.selectedPosition = i;
                            madapter.notifyDataSetChanged();
                            item[0] = picUriList.get(i);
                            ImageViewlarge.setImageURI(Uri.parse(item[0]));
                        }
                    });

                    canceldone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            viewSelectedImages.dismiss();
                        }
                    });

                    okdone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (picUriList.size() > 0) {
                                Intent serviceIntent = new Intent(getApplicationContext(), UploadTaskPhotosServices.class);
                                serviceIntent.putStringArrayListExtra("picUriList", picUriList);
                                serviceIntent.putExtra("taskid", task_id);
                                startService(serviceIntent);
                                finish();
                            } else {
                                viewSelectedImages.dismiss();
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void launchLibrary() {
        final String[] url = new String[1];
        dbQuotation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Quotation quotation = dataSnapshot.getValue(Quotation.class);
                    url[0] = quotation.getUrl();
                    Intent serviceIntent = new Intent(getApplicationContext(), DownloadFileService.class);
                    serviceIntent.putExtra("TaskId", task_id);
                    serviceIntent.putExtra("url", url[0]);
                    startService(serviceIntent);
                } else {
                    Toast.makeText(TaskDetail.this, "No Quotation Uploaded Yet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void prepareListData() {
        dbCompleted.orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    complete_and_hideme.setVisibility(View.GONE);
                    CompletedJob item = dataSnapshot.getValue(CompletedJob.class);
                    completedbyList.add(item);
                    adapter_completedby.notifyDataSetChanged();
                } else {
                    complete_and_hideme.setVisibility(View.VISIBLE);
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
                    assign_and_hideme.setVisibility(View.GONE);
                    CompletedBy item = dataSnapshot.getValue(CompletedBy.class);
                    assignedtoList.add(item);
                    adapter_assignedto.notifyDataSetChanged();
                } else {
                    assign_and_hideme.setVisibility(View.VISIBLE);
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
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    measure_and_hideme.setVisibility(View.GONE);
                    measurement item = dataSnapshot.getValue(measurement.class);
                    measurementList.add(item);
                    adapter_measurement.notifyDataSetChanged();
                } else {
                    measure_and_hideme.setVisibility(View.VISIBLE);
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
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
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
                String item = dataSnapshot.getValue(String.class);
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

    void setValue(Task task) {
        if (task.getStartDate() != null)
            startDate.setText(task.getStartDate());

        if (task.getExpEndDate() != null)
            endDate.setText(task.getExpEndDate());

        if (task.getQty() != null)
            quantity.setText(task.getQty());
        if (!task.getDesc().equals("") && task.getDesc() != null) {
            description.setVisibility(View.VISIBLE);
            description.setText(task.getDesc());
        }
        dbQuotation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    appByCustomer.setVisibility(View.VISIBLE);
                    Quotation quotation = dataSnapshot.getValue(Quotation.class);
                    if (quotation.getApprovedByCust() != null)
                        appByCustomer.setText(" " + quotation.getApprovedByCust());
                    uploadStatus.setText(" Yes");
                } else {
                    appByCustomer.setText("No");
                    uploadStatus.setText(" No");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onImageClicked(int position) {

        viewSelectedImages = new AlertDialog.Builder(TaskDetail.this)
                .setView(R.layout.view_image_on_click).create();
        viewSelectedImages.show();

        RecyclerView bigimage = (RecyclerView) viewSelectedImages.findViewById(R.id.bigimage);

        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        bigimage.setLayoutManager(linearLayoutManager);
        bigimage.setItemAnimator(new DefaultItemAnimator());
        bigimage.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.HORIZONTAL));

        adapter = new bigimage_adapter(DescImages, this, this);
        bigimage.setAdapter(adapter);

        bigimage.scrollToPosition(position);
    }


    @Override
    public void ondownloadButtonClicked(final int position, final bigimage_adapter.MyViewHolder holder) {
        if (!marshmallowPermissions.checkPermissionForExternalStorage()) {
            marshmallowPermissions.requestPermissionForExternalStorage();
        } else {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.download_taskdetail_image.setVisibility(View.GONE);
            String url = DescImages.get(position);
            StorageReference str = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            File rootPath = new File(Environment.getExternalStorageDirectory(), AppName + "/TaskDetailImages");

            if (!rootPath.exists()) {
                rootPath.mkdirs();
            }
            String uriSting = System.currentTimeMillis() + ".jpg";

            final File localFile = new File(rootPath, uriSting);

            str.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.e("firebase ", ";local tem file created  created " + localFile.toString());
                    holder.download_taskdetail_image.setVisibility(View.VISIBLE);
                    holder.progressBar.setVisibility(View.GONE);
                    Toast.makeText(TaskDetail.this, "Image " + position + 1 + " Downloaded", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("firebase ", ";local tem file not created  created " + exception.toString());
                    holder.download_taskdetail_image.setVisibility(View.VISIBLE);
                    holder.progressBar.setVisibility(View.GONE);
                    Toast.makeText(TaskDetail.this, "Failed to download image " + position + 1, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void ondeleteButtonClicked(final int position, bigimage_adapter.MyViewHolder holder) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(TaskDetail.this);
        builder.setMessage("Are you sure you want to delete this image")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        final DatabaseReference db = DBREF.child("Task").child(task_id).child("DescImages").getRef();
                        db.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String url = ds.getValue(String.class);
                                    if (url.equals(DescImages.get(position))) {
                                        final String key = ds.getKey();
                                        db.child(key).removeValue();
                                        viewSelectedImages.dismiss();
                                        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                                        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Toast.makeText(DeleteTask.this, item + " deleted successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                // Toast.makeText(DeleteTask.this, item + " does not exist", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
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
    }

    @Override
    public void onOptionsButtonClicked(final int position, final assignedto_adapter.MyViewHolder holder) {
        open_options = new AlertDialog.Builder(TaskDetail.this)
                .setView(R.layout.options_forassignedtask).create();
        open_options.show();

        LinearLayout remove = (LinearLayout) open_options.findViewById(R.id.remove);
        LinearLayout remind = (LinearLayout) open_options.findViewById(R.id.remind);
        LinearLayout repeatedreminder = (LinearLayout) open_options.findViewById(R.id.repeatedreminder);
        LinearLayout swap = (LinearLayout) open_options.findViewById(R.id.swap);
        LinearLayout editNote = (LinearLayout) open_options.findViewById(R.id.editNote);

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(TaskDetail.this);
                builder.setMessage("Are you sure you want to un-assign this task")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, int id) {
                                DatabaseReference dbCancelJob = DBREF.child("Task").child(task_id).child("AssignedTo").child(adapter_assignedto.emp.getEmpId()).getRef();
                                dbCancelJob.removeValue();

                                DatabaseReference dbEmployee = DBREF.child("Employee").child(adapter_assignedto.emp.getEmpId()).child("AssignedTask").child(task_id);
                                dbEmployee.removeValue(); //for employee
                                String contentforme = "You relieved " + holder.employeename.getText().toString().trim() + " of " + task.getName();
                                sendNotif(mykey, mykey, "cancelJob", contentforme, task_id);
                                String contentforother = "Coordinator " + coordinatorSession.getName() + " relieved you of " + task.getName();
                                sendNotif(mykey, adapter_assignedto.emp.getEmpId(), "cancelJob", contentforother, task_id);
                                Toast.makeText(TaskDetail.this, contentforme, Toast.LENGTH_SHORT).show();
                                assignedtoList.remove(position);
                                adapter_assignedto.notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                open_options.dismiss();
            }
        });

        remind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contentforme = "You reminded " + holder.employeename.getText().toString().trim() + " for " + task.getName();
                sendNotif(mykey, mykey, "remindJob", contentforme, task_id);
                String contentforother = "Coordinator " + coordinatorSession.getName() + " reminded you of " + task.getName();
                sendNotif(mykey, adapter_assignedto.emp.getEmpId(), "remindJob", contentforother, task_id);
                Toast.makeText(TaskDetail.this, contentforme, Toast.LENGTH_SHORT).show();
                open_options.dismiss();
            }
        });

        swap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(TaskDetail.this);
                builder.setMessage("Are you sure you want to swap this task")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, int id) {
                                Intent intent1 = new Intent(TaskDetail.this, forwardTask.class);
                                intent1.putExtra("task_id", task_id);
                                intent1.putExtra("swaping_id", adapter_assignedto.emp.getEmpId());
                                startActivity(intent1);
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
                open_options.dismiss();
            }
        });

        repeatedreminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(TaskDetail.this);
                View mView = layoutInflaterAndroid.inflate(R.layout.repeatedreminderlayout, null);
                AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(TaskDetail.this);
                alertDialogBuilderUserInput.setView(mView);

                final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
                alertDialogBuilderUserInput
                        .setCancelable(false)
                        .setPositiveButton("SET", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                String minutes = userInputDialogEditText.getText().toString().trim();
                                if (minutes != null && !minutes.equals("")) {
                                    Integer Minutes = Integer.parseInt(minutes);
                                    if (Minutes > 0) {
                                        String contentforme = "You reminded " + holder.employeename.getText().toString().trim() + " for " + task.getName();
                                        sendNotif(mykey, mykey, "repeatedReminder", contentforme, task_id);
                                        String contentforother = "Coordinator " + coordinatorSession.getName() + " reminded you of " + task.getName();
                                        sendNotif(mykey, adapter_assignedto.emp.getEmpId(), "repeatedReminder" + " " + minutes, contentforother, task_id);
                                        Toast.makeText(TaskDetail.this, contentforme, Toast.LENGTH_SHORT).show();
                                        dialogBox.dismiss();
                                    }
                                }
                            }
                        })

                        .setNegativeButton("CANCEL",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                alertDialogAndroid.show();
                open_options.dismiss();

            }
        });

        editNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(TaskDetail.this);
                View mView = layoutInflaterAndroid.inflate(R.layout.editcoordinatornote, null);
                AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(TaskDetail.this);
                alertDialogBuilderUserInput.setView(mView);

                final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
                alertDialogBuilderUserInput
                        .setCancelable(false)
                        .setPositiveButton("SET", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                String note = userInputDialogEditText.getText().toString().trim();
                                if (note != null && !note.equals("")) {
                                    DBREF.child("Task").child(task_id).child("AssignedTo").child(adapter_assignedto.emp.getEmpId()).child("note").setValue(note);
                                    holder.noteString.setText(note);
                                    Toast.makeText(TaskDetail.this, "Coordinator note changed successfully", Toast.LENGTH_SHORT).show();
                                    String contentforme = "You changed the coordinator note for " + task.getName();
                                    sendNotif(mykey, mykey, "changedNote", contentforme, task_id);
                                    String contentforother = "Coordinator " + coordinatorSession.getName() + " changed the note of " + task.getName();
                                    sendNotif(mykey, adapter_assignedto.emp.getEmpId(), "changedNote", contentforother, task_id);
                                    dialogBox.dismiss();
                                }
                                else
                                    dialogBox.dismiss();
                            }
                        })

                        .setNegativeButton("CANCEL",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                alertDialogAndroid.show();
                open_options.dismiss();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                final DatabaseReference dbTaskCompleteStatus = DBREF.child("Customer").child(task.getCustomerId()).child("Task").child(task_id).getRef();
                dbTaskCompleteStatus.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String status = dataSnapshot.getValue(String.class);
                            if (status.equals("pending")) {
                                dbAssigned.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                            Toast.makeText(TaskDetail.this, "You have to un-assign all tasks before marking this task as complete", Toast.LENGTH_LONG).show();
                                        } else {
                                            final AlertDialog.Builder builderCompleteTask = new AlertDialog.Builder(TaskDetail.this);
                                            builderCompleteTask.setMessage("Are you sure you want to mark this task as complete??")
                                                    .setCancelable(false)
                                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                                        public void onClick(final DialogInterface dialog, final int id) {
                                                            long idLong = Calendar.getInstance().getTimeInMillis();
                                                            idLong = 9999999999999L - idLong;
                                                            sendNotifToAllCoordinators(mykey, "completeJob", "Task " + task.getName() + " has been successfully completed", task_id);
                                                            sendNotif(mykey, task.getCustomerId(), "completeJob", "Task " + task.getName() + " has been successfully completed", task_id);
                                                            dbCompleted.child(mykey).setValue(new CompletedJob(coordinatorSession.getName(),"Coordinator",mykey,task.getStartDate(), new SimpleDateFormat("DD/MM/YYYY").format(Calendar.getInstance().getTime()), mykey, coordinatorSession.getName(), "Customer has been notified", "Task is successfully completed", idLong + ""));
                                                            Toast.makeText(TaskDetail.this, "Job completed sucessfully", Toast.LENGTH_SHORT).show();
                                                            dbTaskCompleteStatus.setValue("complete");
                                                            dialog.dismiss();

                                                        }
                                                    })
                                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.dismiss();
                                                        }
                                                    });
                                            AlertDialog alert = builderCompleteTask.create();
                                            alert.show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            } else {
                                Toast.makeText(TaskDetail.this, "Task Already Marked As Complete", Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;

            case R.id.item2:
                final AlertDialog.Builder builder = new AlertDialog.Builder(TaskDetail.this);
                builder.setMessage("Are you sure you want to delete this task??")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                if (dbTaskVle != null)
                                    dbTask.removeEventListener(dbTaskVle);
                                final ProgressDialog progressDialog = new ProgressDialog(TaskDetail.this);
                                progressDialog.setMessage("Deleting Task");
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                                Intent serviceIntent = new Intent(TaskDetail.this, DeleteTask.class);
                                serviceIntent.putExtra("task_id", task_id);
                                serviceIntent.putExtra("taskName", task.getName());
                                serviceIntent.putExtra("custId", task.getCustomerId());

                                startService(serviceIntent);
                                final DatabaseReference dbDelete = DBREF.child("DeleteTask").child(task_id).getRef();
                                dbDelete.setValue(Boolean.FALSE);
                                dbDelete.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            Boolean status = dataSnapshot.getValue(Boolean.class);
                                            if (status == true) {
                                                dbDelete.removeValue();
                                                progressDialog.dismiss();
                                                sendNotifToAllCoordinators(mykey, "deleteTask", "Task " + task.getName() + " has been deleted", task_id);
                                                //todo refresh the layout so that the layout is updated
                                                sendNotif(mykey, task.getCustomerId(), "deleteTask", coordinatorSession.getName() + " deleted the " + task.getName() + " task", task_id);
                                                Toast.makeText(TaskDetail.this, "Job deleted sucessfully", Toast.LENGTH_SHORT).show();
                                                onBackPressed();
                                                finish();
                                                dbDelete.removeEventListener(this);
                                            }
                                        }
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
            case R.id.item3:
                final EditText taskname_new,qty_new,enddate_new;
                Button sub;

                taskEditDetails = new AlertDialog.Builder(this)
                        .setView(R.layout.edit_taskdetails).create();
                taskEditDetails.show();

                dbedit = dbTask;
                taskname_new = (EditText) taskEditDetails.findViewById(R.id.taskname);
                qty_new = (EditText) taskEditDetails.findViewById(R.id.qty);
                enddate_new = (EditText) taskEditDetails.findViewById(R.id.endDate);
                sub = (Button) taskEditDetails.findViewById(R.id.submit);

                taskname_new.setText(task.getName());
                qty_new.setText(task.getQty());
                enddate_new.setText(task.getExpEndDate());

                sub.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        temp_taskname = taskname_new.getText().toString().trim();
                        temp_taskname= WordUtils.capitalizeFully(temp_taskname);
                        temp_qty = qty_new.getText().toString().trim();
                        temp_enddate = enddate_new.getText().toString();

                        if(TextUtils.isEmpty(temp_taskname) || TextUtils.isEmpty(temp_qty) || TextUtils.isEmpty(temp_enddate))
                            Toast.makeText(TaskDetail.this,"Enter details...",Toast.LENGTH_SHORT).show();

                        else
                        {
                            dbedit.child("name").setValue(temp_taskname);
                            dbedit.child("qty").setValue(temp_qty);
                            dbedit.child("expEndDate").setValue(temp_enddate);

                            taskEditDetails.dismiss();

                            getSupportActionBar().setTitle(temp_taskname);
                            quantity.setText(temp_qty);
                            endDate.setText(temp_enddate);
                        }
                    }
                });

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
        if (dbTaskVle != null)
            dbTask.removeEventListener(dbTaskVle);
    }

}