package com.litaal.caller.service.bg;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.IBinder;
import android.view.ViewGroup;

import com.litaal.caller.helper.rtc.PeerConnObserverImpl;
import com.litaal.caller.helper.rtc.SdpObserverImpl;
import com.litaal.caller.helper.serv.ServiceBinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraEnumerationAndroid;
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

    private GLSurfaceView selfVideo;

    private PeerConnectionFactory peerConnFactory;
    private PeerConnection peerConnection;
    private MediaStream localStream;
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
    public void initiateCamera(ViewGroup layout) {
        try {
            if (PeerConnectionFactory.initializeAndroidGlobals(getBaseContext(), true, true, false)) {
                peerConnFactory = new PeerConnectionFactory();
                String camName = CameraEnumerationAndroid.getNameOfFrontFacingDevice();

                VideoCapturerAndroid vidCapturer = VideoCapturerAndroid.create(camName, new VideoCapturerAndroid.CameraEventsHandler() {
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
                });

                MediaConstraints vidConstraints = new MediaConstraints();
                VideoSource vidSource = peerConnFactory.createVideoSource(vidCapturer, vidConstraints);
                VideoTrack localVidTrack = peerConnFactory.createVideoTrack("ARDAMSv0", vidSource);

                MediaConstraints audConstraints = new MediaConstraints();
                AudioSource audSource = peerConnFactory.createAudioSource(audConstraints);
                AudioTrack localAudTrack = peerConnFactory.createAudioTrack("ARDAMSa0", audSource);

                selfVideo = new GLSurfaceView(getBaseContext());
                selfVideo.setEGLContextClientVersion(2);
                VideoRendererGui.setView(selfVideo, new Runnable() {
                    @Override
                    public void run() {

                    }
                });
                VideoRenderer vidRenderer = VideoRendererGui.createGui(0, 0, 100, 100, RendererCommon.ScalingType.SCALE_ASPECT_FILL, true);
                localVidTrack.addRenderer(vidRenderer);
                layout.addView(selfVideo);

                localStream = peerConnFactory.createLocalMediaStream("LOCAL_VID_STREAM");
                localStream.addTrack(localVidTrack);
                localStream.addTrack(localAudTrack);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initiateCall(Activity activity) {
        try {
            MediaConstraints constraints = new MediaConstraints();
            constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
            constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

            List<PeerConnection.IceServer> iceServers = new ArrayList<>();
            iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

            PeerConnection.Observer observer = new PeerConnObserverImpl();
            ((PeerConnObserverImpl) observer).setActivity(activity);

            peerConnection = peerConnFactory.createPeerConnection(iceServers, new MediaConstraints(), observer);
            peerConnection.addStream(localStream);

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

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
