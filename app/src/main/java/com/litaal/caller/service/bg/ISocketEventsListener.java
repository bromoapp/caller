package com.litaal.caller.service.bg;

import org.phoenixframework.channels.Socket;

/**
 * Created by sadmin on 5/30/2017.
 */

public interface ISocketEventsListener {

    public void onOpened(Socket socket);
    public void onClosed(Socket socket);
    public void onError(Socket socket, String error);

}
