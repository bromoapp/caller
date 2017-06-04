package com.litaal.caller.helper.serv;

import android.app.Service;
import android.os.Binder;

/**
 * Created by sadmin on 5/29/2017.
 */

public abstract class ServiceBinder extends Binder {

    public abstract Service service();

}
