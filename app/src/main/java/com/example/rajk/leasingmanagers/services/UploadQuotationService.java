package com.example.rajk.leasingmanagers.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.example.rajk.leasingmanagers.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by SoumyaAgarwal on 7/3/2017.
 */

public class UploadQuotationService extends IntentService
{
    public static ArrayList<String> picUriList = new ArrayList<String>();
    public static String taskid;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;
    private static int totalnoofimages;
    private static int s = 0;
    private static int f = 0;

    //private boolean isSuccess;

    public UploadQuotationService() {
        super("Upload");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        int icon = R.mipmap.ic_upload;
        //isSuccess = false;
        mBuilder = new NotificationCompat.Builder(
                getApplicationContext());
        mBuilder.setSmallIcon(icon)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_upload))
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setColor(getApplicationContext().getResources().getColor(R.color.white))
                .setContentText("Uploading quotations...");
        synchronized (this) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, mBuilder.build());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            picUriList = intent.getStringArrayListExtra("picUriList");
            taskid = intent.getStringExtra("taskid");
            totalnoofimages = picUriList.size();
            saveImagesToFirebase(picUriList);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void saveImagesToFirebase(ArrayList<String> picUriList)
    {
        Toast.makeText(getBaseContext(),"Uploading Images in Background", Toast.LENGTH_SHORT).show();
        for (String p: picUriList)
        {
            FirebaseStorage storageRef = FirebaseStorage.getInstance();
            final StorageReference mediaRef;

            final long timestamp = System.currentTimeMillis();
            final String fileNameOnFirebase = String.valueOf(timestamp);

            mediaRef = storageRef.getReference().child("TaskImages").child(taskid).child(fileNameOnFirebase);

            try
            {
                mediaRef.putFile(Uri.fromFile(new File(p))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        s++;
                        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("MeChat").child("Task").child(taskid).child("DescImages");
                        ref.child(fileNameOnFirebase).setValue(taskSnapshot.getDownloadUrl());

                        if (f+s==totalnoofimages)
                        {
                            updateNotification(s+" Uploaded ,"+f+" Failed ");
                            stopSelf();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        f++;
                        if (f+s==totalnoofimages)
                        {
                            updateNotification(s+" Uploaded ,"+f+" Failed ");
                            stopSelf();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                updateNotification("Upload Unsuccessful. Please try again ");
                stopSelf();
            }

        }
    }

    private void updateNotification(String information) {
        notificationManager.cancel(0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                getApplicationContext());
        int icon = R.mipmap.ic_launcher;
        mBuilder.setSmallIcon(icon)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher))
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(false)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setColor(getApplicationContext().getResources().getColor(R.color.white))
                .setContentText(information)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(information));

        //Log.d("check","service started"+id);
        synchronized (this) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, mBuilder.build());
        }
    }
}


