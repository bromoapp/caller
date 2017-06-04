package com.litaal.caller.helper.rtc;

import android.app.Activity;
import android.content.Intent;

import com.google.gson.Gson;
import com.litaal.caller.dto.CandidateSignalDTO;
import com.litaal.caller.helper.Constant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;

/**
 * Created by sadmin on 6/2/2017.
 */

public class PeerConnObserverImpl implements PeerConnection.Observer {

    private static Logger log = LoggerFactory.getLogger(PeerConnObserverImpl.class);

    private Activity activity;
    private Gson gson = new Gson();

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState state) {
        log.info(">>> SIGNALING STATE: " + state);
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState state) {
        log.info(">>> ICE CONNECTION CHANGE: " + state);
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        log.info(">>> ICE CONNECTION RECEIVING...");
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState state) {
        log.info(">>> ICE GATHERING CHANGE: " + state);
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {
        //log.info(">>> ICE CANDIDATE");
        sendLocalCandidate(candidate);
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        log.info(">>> ADD STREAM...");
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        log.info(">>> REMOVE STREAM...");
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        log.info(">>> ADD DATA CHANNEL...");
    }

    @Override
    public void onRenegotiationNeeded() {
        log.info(">>> RE-NEGOTIATION NEEDED...");
    }

    private void sendLocalCandidate(IceCandidate candidate) {
        CandidateSignalDTO dto = new CandidateSignalDTO(candidate.sdp, candidate.sdpMid, candidate.sdpMLineIndex);
        String json = gson.toJson(dto);

        Intent i = new Intent();
        i.setAction(Constant.IntentTopic.ON_PEER_EVENT);
        i.putExtra(Constant.IntentExtraKey.MESSAGE, json);
        activity.sendBroadcast(i);
    }

}
