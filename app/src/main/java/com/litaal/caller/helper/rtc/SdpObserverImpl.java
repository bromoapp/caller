package com.litaal.caller.helper.rtc;

import android.app.Activity;
import android.content.Intent;

import com.google.gson.Gson;
import com.litaal.caller.dto.SdpSignalDTO;
import com.litaal.caller.helper.Constant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webrtc.PeerConnection;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

/**
 * Created by sadmin on 6/2/2017.
 */

public class SdpObserverImpl implements SdpObserver {

    private static Logger log = LoggerFactory.getLogger(SdpObserverImpl.class);

    private boolean sent = false;
    private Gson gson = new Gson();
    private Activity activity;
    private PeerConnection peerConnection;
    private SdpObserver sdpObserver;
    private SessionDescription localSdp;

    public SdpObserverImpl() {
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setPeerConnection(PeerConnection peerConnection) {
        this.peerConnection = peerConnection;
    }

    public void setSdpObserver(SdpObserver sdpObserver) {
        this.sdpObserver = sdpObserver;
    }

    @Override
    public void onCreateSuccess(final SessionDescription sdp) {
        log.info(">>> ON CREATE SUCCESS");
        localSdp = sdp;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                peerConnection.setLocalDescription(sdpObserver, sdp);
            }
        });
    }

    @Override
    public void onSetSuccess() {
        log.info(">>> ON SET SUCCESS");
        if (!sent) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    log.info(">>> SEND LOCAL SDP...");
                    sendLocalDescription();
                }
            });
            sent = true;
        }
    }

    @Override
    public void onCreateFailure(String s) {
        log.info(">>> ON CREATE FAILURE: " + s);
    }

    @Override
    public void onSetFailure(String s) {
        log.info(">>> ON SET FAILURE: "+ s);
    }

    private void sendLocalDescription() {
        try {
            SdpSignalDTO dto = new SdpSignalDTO(localSdp.type.canonicalForm(), localSdp.description);
            String json = gson.toJson(dto);

            Intent i = new Intent();
            i.setAction(Constant.IntentTopic.ON_PEER_EVENT);
            i.putExtra(Constant.IntentExtraKey.MESSAGE, json);
            activity.sendBroadcast(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
