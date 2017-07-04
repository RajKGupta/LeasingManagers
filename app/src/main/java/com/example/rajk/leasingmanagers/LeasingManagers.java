package com.example.rajk.leasingmanagers;


import com.example.rajk.leasingmanagers.CheckInternetConnectivity.NetWatcher;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by RajK on 11-05-2017.
 */
public class LeasingManagers extends android.support.multidex.MultiDexApplication {
    private static LeasingManagers mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        if(!FirebaseApp.getApps(this).isEmpty()){
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        Fresco.initialize(getApplicationContext());

    }
    public static synchronized LeasingManagers getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(NetWatcher.ConnectivityReceiverListener listener) {
        NetWatcher.connectivityReceiverListener = listener;
    }
}
