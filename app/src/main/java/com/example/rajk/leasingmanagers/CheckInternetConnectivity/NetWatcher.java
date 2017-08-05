package com.example.rajk.leasingmanagers.CheckInternetConnectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.rajk.leasingmanagers.LeasingManagers;
import com.example.rajk.leasingmanagers.services.MyFirebaseMessagingService;

public class NetWatcher
        extends BroadcastReceiver {

    public NetWatcher() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent arg1) {
        if (arg1.getAction() == "android.intent.action.BOOT_COMPLETED") {
            Intent serviceIntent = new Intent(context, MyFirebaseMessagingService.class);
            context.startService(serviceIntent);
        }

    }}