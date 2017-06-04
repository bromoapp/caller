package com.litaal.caller.helper.serv;

import android.app.Service;

/**
 * Created by sadmin on 5/29/2017.
 */

public interface IConnectionListener {

    public void onConnected(String name, Service service);
    public void onDisconnected(String name);

}
