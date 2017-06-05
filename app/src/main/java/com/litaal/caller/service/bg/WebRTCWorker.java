package com.litaal.caller.service.bg;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.IBinder;
import android.view.ViewGroup;

import com.litaal.caller.helper.rtc.CameraEventsHandlerImpl;
import com.litaal.caller.helper.rtc.PeerConnObserverImpl;
import com.litaal.caller.helper.rtc.SdpObserverImpl;
import com.litaal.caller.helper.serv.ServiceBinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

public class WebRTCWorker extends Service {

    private static Logger log = LoggerFactory.getLogger(WebRTCWorker.class);
    private final IBinder binder = new WebRTCWorkerBinder();

    private GLSurfaceView videoView;

    private PeerConnectionFactory peerConnFactory;
    private PeerConnection peerConnection;
    private MediaStream mediaStream;
    private SdpObserver sdpObserver;

    public class WebRTCWorkerBinder extends ServiceBinder {
        @Override
        public Service service() {
            return WebRTCWorker.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    /**
     * Creates a camera preview and stream to broadcast
     */
    public void initCamera(ViewGroup layout) {
        try {
            if (PeerConnectionFactory.initializeAndroidGlobals(getBaseContext(), true, true, false)) {
                peerConnFactory = new PeerConnectionFactory();
                String camName = CameraEnumerationAndroid.getNameOfFrontFacingDevice();

                VideoCapturerAndroid vidCapturer = VideoCapturerAndroid.create(camName, new CameraEventsHandlerImpl());

                MediaConstraints vidConstraints = new MediaConstraints();
                VideoSource vidSource = peerConnFactory.createVideoSource(vidCapturer, vidConstraints);
                VideoTrack localVidTrack = peerConnFactory.createVideoTrack("ARDAMSv0", vidSource);

                MediaConstraints audConstraints = new MediaConstraints();
                AudioSource audSource = peerConnFactory.createAudioSource(audConstraints);
                AudioTrack localAudTrack = peerConnFactory.createAudioTrack("ARDAMSa0", audSource);

                videoView = new GLSurfaceView(getBaseContext());
                videoView.setEGLContextClientVersion(2);
                VideoRendererGui.setView(videoView, new Runnable() {
                    @Override
                    public void run() {

                    }
                });
                VideoRenderer vidRenderer = VideoRendererGui.createGui(0, 0, 100, 100, RendererCommon.ScalingType.SCALE_ASPECT_FILL, true);
                localVidTrack.addRenderer(vidRenderer);
                layout.addView(videoView);

                mediaStream = peerConnFactory.createLocalMediaStream("LOCAL_VID_STREAM");
                mediaStream.addTrack(localVidTrack);
                mediaStream.addTrack(localAudTrack);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initCall(Activity activity) {
        try {
            MediaConstraints constraints = new MediaConstraints();
            constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
            constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

            List<PeerConnection.IceServer> iceServers = new ArrayList<>();
            iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

            PeerConnection.Observer connObserver = new PeerConnObserverImpl();
            ((PeerConnObserverImpl) connObserver).setActivity(activity);

            peerConnection = peerConnFactory.createPeerConnection(iceServers, new MediaConstraints(), connObserver);
            peerConnection.addStream(mediaStream);

            sdpObserver = new SdpObserverImpl();
            ((SdpObserverImpl) sdpObserver).setActivity(activity);
            ((SdpObserverImpl) sdpObserver).setPeerConnection(peerConnection);
            ((SdpObserverImpl) sdpObserver).setSdpObserver(sdpObserver);

            peerConnection.createOffer(sdpObserver, constraints);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onReceivedAnswer(SessionDescription sdp) {
        peerConnection.setRemoteDescription(sdpObserver, sdp);
    }

    public void onReceiveCandidate(IceCandidate candidate) {
        peerConnection.addIceCandidate(candidate);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
