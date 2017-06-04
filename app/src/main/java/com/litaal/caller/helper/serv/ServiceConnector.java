package com.litaal.caller.helper.serv;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by sadmin on 5/29/2017.
 */

public class ServiceConnector implements ServiceConnection {

    private IConnectionListener listener;

    public ServiceConnector(IConnectionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        listener.onConnected(name.getClassName(), ((ServiceBinder) service).service());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        listener.onDisconnected(name.getClassName());
    }
}
