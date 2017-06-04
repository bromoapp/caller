package com.litaal.caller.service.bg;

import org.phoenixframework.channels.Envelope;

/**
 * Created by sadmin on 5/30/2017.
 */

public interface IChannelEventsListener {

    public void onMessage(String topic, Envelope envelope);
    public void onTimeout(String topic, String message);

}
