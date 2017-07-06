package com.example.rajk.leasingmanagers.chat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.example.rajk.leasingmanagers.CoordinatorLogin.CoordinatorSession;
import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.adapter.chatAdapter;
import com.example.rajk.leasingmanagers.helper.MarshmallowPermissions;
import com.example.rajk.leasingmanagers.model.ChatMessage;
import com.example.rajk.leasingmanagers.services.UploadFileService;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.zfdang.multiple_images_selector.ImagesSelectorActivity;
import com.zfdang.multiple_images_selector.SelectorSettings;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static com.example.rajk.leasingmanagers.LeasingManagers.DBREF;

public class ChatActivity extends AppCompatActivity implements chatAdapter.ChatAdapterListener,View.OnClickListener{
    private static final int REQUEST_CODE = 101;
    private EditText typeComment;
    private ImageButton sendButton;
    Intent intent;
    private RecyclerView recyclerView;
    DatabaseReference dbChat;
    private String otheruserkey, mykey;
    LinearLayoutManager linearLayoutManager;
    private MarshmallowPermissions marshmallowPermissions;
    LinearLayout emptyView;
    private ArrayList<String> mResults = new ArrayList<>();
    private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    UploadFileService uploadFileService;
    boolean mServiceBound = false;
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa");
    private String lastDate = "20-01-3000 00:00";
    private chatAdapter mAdapter;
    private ArrayList<ChatMessage> chatList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    String receiverToken="nil";
    LinearLayout commentView;
    private ChildEventListener dbChatlistener;
    ImageButton photoattach, docattach;
    public String dbTableKey;
    private CoordinatorSession session;
    private ArrayList<String> docPaths,photoPaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        marshmallowPermissions = new MarshmallowPermissions(this);

        actionModeCallback = new ActionModeCallback();

        intent = getIntent();
        dbTableKey = intent.getStringExtra("dbTableKey");
        otheruserkey = intent.getStringExtra("otheruserkey");

        System.out.println("recevier token chat act oncreate"+getRecivertoken(otheruserkey));

        session = new CoordinatorSession(this);

        mykey = session.getUsername();
        dbChat = DBREF.child("Chats").child(dbTableKey).child("ChatMessages").getRef();

        commentView = (LinearLayout) findViewById(R.id.commentView);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        emptyView = (LinearLayout) findViewById(R.id.empty_view);

        typeComment = (EditText) findViewById(R.id.typeComment);
        sendButton = (ImageButton) findViewById(R.id.sendButton);

        photoattach = (ImageButton) findViewById(R.id.photoattach);
        docattach = (ImageButton) findViewById(R.id.docattach);

        photoattach.setOnClickListener(this);
        docattach.setOnClickListener(this);

        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new chatAdapter(chatList, this, dbTableKey,this);
        recyclerView.setAdapter(mAdapter);
        sendButton.setOnClickListener(this);
        loadData();

    }

    private String getRecivertoken(String otheruserkey) {
        System.out.println(otheruserkey+"recd token in chat act ");
        DBREF.child("Fcmtokens").child(otheruserkey).child("token").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                receiverToken = dataSnapshot.getValue().toString();
                    System.out.println(dataSnapshot.getValue()+"recd token in chat act "+receiverToken);
                }
                else{
                    receiverToken="nil";
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return  receiverToken;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       /* if(requestCode == REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                mResults = data.getStringArrayListExtra(SelectorSettings.SELECTOR_RESULTS);
                assert mResults != null;

                // show results in textview
                for(String result : mResults) {
                    uploadFile(result,"photo");
                }
            }
        }*/
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
          case FilePickerConst.REQUEST_CODE_PHOTO:
                if(resultCode== Activity.RESULT_OK && data!=null)
                {
                    photoPaths = new ArrayList<>();
                    photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                    for(String result : docPaths) {
                        uploadFile(result, "photo");
                    }
                    }
                break;
            case FilePickerConst.REQUEST_CODE_DOC:
                if(resultCode== Activity.RESULT_OK && data!=null)
                {
                    docPaths = new ArrayList<>();
                    docPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                    for(String result : docPaths) {
                        uploadFile(result,"doc");
                }
                }
                break;
        }
    }

    private void uploadFile(String filePath, String type)
    {

        uploadFileService.uploadFile(filePath,type,mykey, otheruserkey, receiverToken, dbTableKey,dbChat);

    }


    private void loadData() {
        dbChatlistener = dbChat.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(ChatActivity.this, "No more comments", Toast.LENGTH_SHORT).show();
                }
        else {
                    ChatMessage comment = dataSnapshot.getValue(ChatMessage.class);
                    if (!comment.getSenderUId().equals(mykey)) {

                        dbChat.child(comment.getId()).child("status").setValue("3");
                        comment.setStatus("3");  // all message status set to read
                    }
                    else {
                        if (comment.getStatus().equals("0"))
                            dbChat.child(comment.getId()).child("status").setValue("1");
                            comment.setStatus("1");  // all message status set to read
                    }

                    chatList.add(comment);
                    mAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(chatList.size() - 1);
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
    protected void onResume(){
        super.onResume();

    }


    @Override
    public void onBackPressed() {
            super.onBackPressed();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(dbChatlistener!=null)
            dbChat.removeEventListener(dbChatlistener);
        if (mServiceBound) {
            if(mServiceConnection!=null)
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
        Intent intent = new Intent(ChatActivity.this,
                UploadFileService.class);
                stopService(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(dbChatlistener!=null)
        dbChat.removeEventListener(dbChatlistener);
    }


////maintain all the clicks on buttons on this page
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.sendButton:
                if(receiverToken.matches("nil")){
                    getRecivertoken(otheruserkey);
                    System.out.println("calling receiver token from send message"+receiverToken);
                }
                String commentString = typeComment.getText().toString().trim();
                if (TextUtils.isEmpty(commentString)) {
                    Toast.makeText(ChatActivity.this, "What?? No Comment!!", Toast.LENGTH_SHORT).show();
                } else {


                    long curTime = Calendar.getInstance().getTimeInMillis();
                    long id = curTime;
                    String timestamp = formatter.format(Calendar.getInstance().getTime());
                    System.out.println(commentString+"time stamp"+timestamp);
                    ChatMessage cm = new ChatMessage(mykey,otheruserkey,timestamp,"text",id+"","0",commentString,receiverToken,dbTableKey);
                    dbChat.child(String.valueOf(id)).setValue(cm);
                    typeComment.setText("");

                }
                break;

            case R.id.photoattach:
                mResults = new ArrayList<>();
                if(!marshmallowPermissions.checkPermissionForCamera())
                    marshmallowPermissions.requestPermissionForCamera();
                if(!marshmallowPermissions.checkPermissionForExternalStorage())
                    marshmallowPermissions.requestPermissionForExternalStorage();

                if(marshmallowPermissions.checkPermissionForCamera()&&marshmallowPermissions.checkPermissionForExternalStorage()) {
                    FilePickerBuilder.getInstance().setMaxCount(10)
                            .setActivityTheme(R.style.AppTheme)
                            .pickPhoto(this);
                    }
                break;

            case R.id.docattach:

                if(!marshmallowPermissions.checkPermissionForExternalStorage())
                    marshmallowPermissions.requestPermissionForExternalStorage();

                if(marshmallowPermissions.checkPermissionForExternalStorage()) {

                    FilePickerBuilder.getInstance().setMaxCount(10)
                            .setActivityTheme(R.style.AppTheme)
                            .pickFile(this);
                }
                break;

           }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, UploadFileService.class);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

////////////////////binding the service
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UploadFileService.MyBinder myBinder = (UploadFileService.MyBinder) service;
            uploadFileService = myBinder.getService();
            mServiceBound = true;
        }
    };


///////////Everything below is for action mode
    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_action, menu);

            // disable swipe refresh if action mode is enabled
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    // delete all the selected messages
                    deleteMessages();
                    mode.finish();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelections();
            actionMode = null;
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.resetAnimationIndex();
                    // mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void deleteMessages() {
        mAdapter.resetAnimationIndex();
        List<Integer> selectedItemPositions =
                mAdapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            mAdapter.removeData(selectedItemPositions.get(i));
        }
        mAdapter.notifyDataSetChanged();
    }
    private void enableActionMode(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }

    }

    @Override
    public void onMessageRowClicked(int position) {
        if (mAdapter.getSelectedItemCount() > 0) {
            enableActionMode(position);
        }
    }

    @Override
    public void onRowLongClicked(int position) {
        enableActionMode(position);
    }

}
