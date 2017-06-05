package com.litaal.caller.helper.rtc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webrtc.VideoCapturerAndroid;

/**
 * Created by sadmin on 6/5/2017.
 */

public class CameraEventsHandlerImpl implements VideoCapturerAndroid.CameraEventsHandler {
    private static Logger log = LoggerFactory.getLogger(CameraEventsHandlerImpl.class);

    @Override
    public void onCameraError(String s) {
        log.info(">>> CAMERA ERROR ", s);
    }

    @Override
    public void onCameraFreezed(String s) {
        log.info(">>> CAMERA FREEZED ", s);
    }

    @Override
    public void onCameraOpening(int i) {
        log.info(">>> CAMERA OPENING");
    }

    @Override
    public void onFirstFrameAvailable() {
        log.info(">>> CAMERA ON FIRST FRAME AVAIL");
    }

    @Override
    public void onCameraClosed() {
        log.info(">>> CAMERA CLOSED");
    }
}
