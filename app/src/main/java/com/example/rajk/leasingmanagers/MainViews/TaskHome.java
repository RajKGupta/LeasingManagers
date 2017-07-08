package com.example.rajk.leasingmanagers.MainViews;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.adapter.taskAdapter;
import com.example.rajk.leasingmanagers.helper.FilePath;
import com.example.rajk.leasingmanagers.helper.MarshmallowPermissions;
import com.example.rajk.leasingmanagers.model.Quotation;
import com.example.rajk.leasingmanagers.model.Task;
import com.example.rajk.leasingmanagers.services.UploadQuotationService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zfdang.multiple_images_selector.ImagesSelectorActivity;
import com.zfdang.multiple_images_selector.SelectorSettings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TaskHome extends Fragment implements taskAdapter.TaskAdapterListener{
    RecyclerView task_list;
    DatabaseReference dbTask;
    LinearLayoutManager linearLayoutManager;
    private ArrayList<Task> TaskList= new ArrayList<>();
    private taskAdapter mAdapter;
    private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    MarshmallowPermissions marshMallowPermission;
    private static final int PICK_FILE_REQUEST = 1;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    ProgressDialog progressDialog ;
    DatabaseReference dbQuotation;

    public TaskHome() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_task_home, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        marshMallowPermission = new MarshmallowPermissions(getActivity());
        progressDialog = new ProgressDialog(getActivity());
        dbTask = FirebaseDatabase.getInstance().getReference().child("MeChat").child("Task").getRef();
        task_list = (RecyclerView) getView().findViewById(R.id.task_list);
        LoadData();
        mAdapter = new taskAdapter(TaskList,getContext(),this);
        linearLayoutManager=new LinearLayoutManager(getContext());
        task_list.setLayoutManager(linearLayoutManager);
        task_list.setItemAnimator(new DefaultItemAnimator());
        task_list.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        task_list.setAdapter(mAdapter);

        actionModeCallback = new ActionModeCallback();

    }

    @Override
    public void onRowLongClicked(int position) {
        // long press is performed, enable action mode
        enableActionMode(position);
    }

    private void enableActionMode(int position) {
        if (actionMode == null) {
            actionMode = ((AppCompatActivity)getActivity()).startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            //actionMode = null;
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    @Override
    public void onIconClicked(int position) {
        if (actionMode == null) {
            actionMode = ((AppCompatActivity)getActivity()).startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    @Override
    public void onMessageRowClicked(int position) {

        if (mAdapter.getSelectedItemCount() > 0) {
            enableActionMode(position);
        }
        else {
            Intent intent = new Intent(getContext(),TaskDetail.class);
            Task task = TaskList.get(position);
            intent.putExtra("task_id",task.getTaskId());
            startActivity(intent);
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.uploadquotation:
                    // delete all the selected messages
                    UploadQuotation();
                    //mode.finish();
                    mode = null;
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelections();
            actionMode = null;
            task_list.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.resetAnimationIndex();
                    // mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void UploadQuotation()
    {
        if (!marshMallowPermission.checkPermissionForCamera()&&!marshMallowPermission.checkPermissionForExternalStorage()) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    2);
        }
        else {

            if (Build.VERSION.SDK_INT < 19) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(Intent.createChooser(intent, "Choose File to Upload.."), PICK_FILE_REQUEST);
            } else {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(intent, "Choose File to Upload.."), PICK_FILE_REQUEST);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                    selectedFilePath = FilePath.getPath(getActivity(),selectedFileUri);
                }


                if(selectedFilePath != null && !selectedFilePath.equals(""))
                {
                    mAdapter.resetAnimationIndex();
                    List<Integer> selectedItemPositions = mAdapter.getSelectedItems();

                    Intent serviceIntent = new Intent(getActivity(), UploadQuotationService.class);
                    serviceIntent.putExtra("TaskList", TaskList);
                    serviceIntent.putExtra("selectedFileUri", selectedFileUri.toString());
                    serviceIntent.putExtra("selectedItemPositions", (Serializable) selectedItemPositions);
                    getActivity().startService(serviceIntent);

                }
                    else{
                    Toast.makeText(getActivity(),"Cannot upload file to server",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    void LoadData()
    {

        dbTask.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                Task task = dataSnapshot.getValue(Task.class);
                TaskList.add(task);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Task task = dataSnapshot.getValue(Task.class);
                TaskList.remove(task);
                mAdapter.notifyDataSetChanged();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 2: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent();
                    intent.setType("*/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Choose File to Upload.."), PICK_FILE_REQUEST);
                }
                else
                {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("These permissions are necessary else you cant upload images")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ActivityCompat.requestPermissions(getActivity(),
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                                            2);
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                return;
            }

        }
    }
}