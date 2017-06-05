package com.litaal.caller.activity;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.litaal.caller.R;
import com.litaal.caller.dto.CandidateSignalDTO;
import com.litaal.caller.dto.SdpSignalDTO;
import com.litaal.caller.dto.SignalDTO;
import com.litaal.caller.helper.Constant;
import com.litaal.caller.helper.serv.IConnectionListener;
import com.litaal.caller.helper.serv.IIntentReceiver;
import com.litaal.caller.helper.serv.IntentReceiver;
import com.litaal.caller.helper.serv.ServiceConnector;
import com.litaal.caller.service.bg.SignalingWorker;
import com.litaal.caller.service.bg.WebRTCWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        IConnectionListener, IIntentReceiver {

    private static Logger log = LoggerFactory.getLogger(MainActivity.class);

    private Gson gson = new Gson();
    private Button btnConnect;
    private Button btnCall;
    private Button btnHangup;
    private LinearLayout videoViewContainer;

    private SignalingWorker signalingWorker = null;
    private ServiceConnection signalingWorkerConn;
    private IntentReceiver signalingWorkerIntentReceiver;
    private boolean isSignalingWorkerIntentReceiverRegistered = false;

    private WebRTCWorker webRTCWorker = null;
    private ServiceConnection webRTCWorkerConn;
    private IntentReceiver webRTCWorkerIntentReceiver;
    private boolean isWebRTCWorkerIntentReceiverRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initiates UI components
        setContentView(R.layout.activity_main);

        videoViewContainer = (LinearLayout) findViewById(R.id.video_view_container);

        btnConnect = (Button) findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(this);

        btnCall = (Button) findViewById(R.id.btn_call);
        btnCall.setOnClickListener(this);

        btnHangup = (Button) findViewById(R.id.btn_hangup);
        btnHangup.setOnClickListener(this);

        // Initiates background services
        signalingWorkerConn = new ServiceConnector(this);
        startService(new Intent(getBaseContext(), SignalingWorker.class));
        bindService(new Intent(getBaseContext(), SignalingWorker.class), signalingWorkerConn, Service.BIND_AUTO_CREATE);

        if (!isSignalingWorkerIntentReceiverRegistered) {
            signalingWorkerIntentReceiver = new IntentReceiver(this);
            registerReceiver(signalingWorkerIntentReceiver, new IntentFilter(Constant.IntentTopic.ON_SIGNAL_EVENT));
            isSignalingWorkerIntentReceiverRegistered = true;
        }

        webRTCWorkerConn = new ServiceConnector(this);
        startService(new Intent(getBaseContext(), WebRTCWorker.class));
        bindService(new Intent(getBaseContext(), WebRTCWorker.class), webRTCWorkerConn, Service.BIND_AUTO_CREATE);

        if (!isWebRTCWorkerIntentReceiverRegistered) {
            webRTCWorkerIntentReceiver = new IntentReceiver(this);
            registerReceiver(webRTCWorkerIntentReceiver, new IntentFilter(Constant.IntentTopic.ON_PEER_EVENT));
            isWebRTCWorkerIntentReceiverRegistered = true;
        }
    }

    @Override
    protected void onDestroy() {
        if (signalingWorker != null) {
            unbindService(signalingWorkerConn);
        }
        if (isSignalingWorkerIntentReceiverRegistered) {
            unregisterReceiver(signalingWorkerIntentReceiver);
        }
        if (webRTCWorker != null) {
            unbindService(webRTCWorkerConn);
        }
        if (isWebRTCWorkerIntentReceiverRegistered) {
            unregisterReceiver(webRTCWorkerIntentReceiver);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        Button btn = (Button) v;
        if (btn.getText().toString().equalsIgnoreCase("Connect")) {
            btnConnect.setEnabled(false);
            webRTCWorker.initCamera(videoViewContainer);
        }
        if (btn.getText().toString().equalsIgnoreCase("Call")) {
            btnCall.setEnabled(false);
            webRTCWorker.initCall(MainActivity.this);
        }
        if (btn.getText().toString().equalsIgnoreCase("Hangup")) {
            //TODO
        }
    }

    @Override
    public void onConnected(String name, Service service) {
        if (name.equalsIgnoreCase(SignalingWorker.class.getCanonicalName())) {
            signalingWorker = (SignalingWorker) service;
        }
        if (name.equalsIgnoreCase(WebRTCWorker.class.getCanonicalName())) {
            webRTCWorker = (WebRTCWorker) service;
        }
    }

    @Override
    public void onDisconnected(String name) {
        // TODO
    }

    @Override
    public void onIntentReceived(Intent i) {
        if (i.getAction().equalsIgnoreCase(Constant.IntentTopic.ON_SIGNAL_EVENT)) {
            SignalDTO msg = (SignalDTO) i.getSerializableExtra(Constant.IntentExtraKey.MESSAGE);
            if (msg.getOrigin().equalsIgnoreCase("callee")) {
                if (msg.getBody().contains("{\"sdp\":")) {
                    SdpSignalDTO dto = gson.fromJson(msg.getBody(), SdpSignalDTO.class);
                    //log.info(">>> REMOTE SDP TYPE: " + dto.getSdp().getType());
                    //log.info(">>> REOMTE SDP DESC: " + dto.getSdp().getSdp());
                    if (dto.getSdp().getType().equalsIgnoreCase(SessionDescription.Type.ANSWER.canonicalForm())) {
                        SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER, dto.getSdp().getSdp());
                        webRTCWorker.onReceivedAnswer(sdp);
                    } else if (msg.getBody().contains("{\"candidate\":")) {
                        CandidateSignalDTO can = gson.fromJson(msg.getBody(), CandidateSignalDTO.class);
                        IceCandidate candidate = new IceCandidate(can.getCandidate().getSdpMid(),
                                can.getCandidate().getSdpMLineIndex(), can.getCandidate().getCandidate());
                        webRTCWorker.onReceiveCandidate(candidate);
                    }
                }
            }
        }
        if (i.getAction().equalsIgnoreCase(Constant.IntentTopic.ON_PEER_EVENT)) {
            String json = i.getStringExtra(Constant.IntentExtraKey.MESSAGE);
            if (json.contains("sdp")) {
                //log.info(">>> LOCAL SDP: " + json);
            } else {
                //log.info(">>> LOCAL CANDIDATE: " + json);
            }
            signalingWorker.sendMessage(json);
        }
    }

}