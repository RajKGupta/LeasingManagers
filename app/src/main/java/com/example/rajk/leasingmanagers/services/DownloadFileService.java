package com.example.rajk.leasingmanagers.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.rajk.leasingmanagers.MainViews.TaskDetail;
import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.model.Quotation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.example.rajk.leasingmanagers.LeasingManagers.AppName;

public class DownloadFileService extends IntentService {

    String TaskId;
    String url;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;

    public DownloadFileService() {
        super("Upload");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int icon = R.mipmap.ic_download;
        mBuilder = new NotificationCompat.Builder(
                getApplicationContext());
        mBuilder.setSmallIcon(icon)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_download))
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setColor(getApplicationContext().getResources().getColor(R.color.white))
                .setContentText("Downloading Quotation...");
        synchronized (this) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, mBuilder.build());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            TaskId = intent.getStringExtra("TaskId");
            url = intent.getStringExtra("url");
            downloadFile(url, TaskId);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void downloadFile(final String url, final String task_id) {

        StorageReference mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        File rootPath = new File(Environment.getExternalStorageDirectory(), AppName+"/Quotations");
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
        String uriSting = System.currentTimeMillis() + ".jpg";

        final File localFile = new File(rootPath, uriSting);

        mStorageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.e("firebase ", ";local tem file created  created " + localFile.toString());
                Toast.makeText(DownloadFileService.this, "Downloaded Quotation", Toast.LENGTH_SHORT).show();
                updateNotification("Succesfully Downloaded");
                stopSelf();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(DownloadFileService.this, "Download Failed", Toast.LENGTH_SHORT).show();
                updateNotification("Download failed");
                stopSelf();
                Log.e("firebase ", ";local tem file not created  created " + exception.toString());
            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                /*double fprogress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                long bytes = taskSnapshot.getBytesTransferred();

                String progress = String.format("%.2f", fprogress);
                int constant = 1000;
                if (bytes % constant == 0) {
                    android.support.v4.app.NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getApplicationContext())
                                    .setSmallIcon(android.R.drawable.stat_sys_download)
                                    .setContentTitle("Downloading " + task_id + "Quotation.pdf")
                                    .setContentText(" " + progress + "% completed");

                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    mNotificationManager.notify(100, mBuilder.build());
                }*/
            }
        });
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

        synchronized (this) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, mBuilder.build());
        }
    }
}
