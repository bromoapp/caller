package com.litaal.caller.helper.serv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by sadmin on 5/30/2017.
 */

public class IntentReceiver extends BroadcastReceiver {

    private IIntentReceiver receiver;

    public IntentReceiver(IIntentReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        receiver.onIntentReceived(intent);
    }
}
