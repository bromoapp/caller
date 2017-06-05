package com.litaal.caller.service.bg;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.litaal.caller.dto.SignalDTO;
import com.litaal.caller.helper.Constant;
import com.litaal.caller.helper.serv.ServiceBinder;

import org.phoenixframework.channels.Channel;
import org.phoenixframework.channels.Envelope;
import org.phoenixframework.channels.IErrorCallback;
import org.phoenixframework.channels.IMessageCallback;
import org.phoenixframework.channels.ISocketCloseCallback;
import org.phoenixframework.channels.ISocketOpenCallback;
import org.phoenixframework.channels.ITimeoutCallback;
import org.phoenixframework.channels.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalingWorker extends Service implements ISocketEventsListener, IChannelEventsListener {

    private static Logger log = LoggerFactory.getLogger(SignalingWorker.class);
    private final IBinder binder = new SignalingWorkerBinder();

    private String wsUrl = "ws://192.168.150.1:4000/socket/websocket?user=caller";
    private String channel_topic = "room";
    private String message_topic = "message";

    private Socket socket;
    private Channel channel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public class SignalingWorkerBinder extends ServiceBinder {
        @Override
        public SignalingWorker service() {
            return SignalingWorker.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        log.info(">>> SIGNALING WORKER CREATED...");
        super.onCreate();
        final ISocketEventsListener listener = SignalingWorker.this;
        try {
            socket = new Socket(wsUrl);
            socket.onOpen(new ISocketOpenCallback() {
                @Override
                public void onOpen() {
                    listener.onOpened(socket);
                }
            }).onClose(new ISocketCloseCallback() {
                @Override
                public void onClose() {
                    listener.onClosed(socket);
                }
            }).onError(new IErrorCallback() {
                @Override
                public void onError(String error) {
                    listener.onError(socket, error);
                }
            }).connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpened(Socket socket) {
        log.info(">>> SOCKET CONNECTEED");
        final IChannelEventsListener listener = SignalingWorker.this;
        channel = socket.chan(channel_topic, null);
        channel.on(message_topic, new IMessageCallback() {
            @Override
            public void onMessage(final Envelope envelope) {
                listener.onMessage(message_topic, envelope);
            }
        });
        try {
            channel.join().receive("ok", new IMessageCallback() {
                @Override
                public void onMessage(Envelope envelope) {
                    listener.onMessage("ok", envelope);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClosed(Socket socket) {
        log.info(">>> SOCKET CLOSED");
    }

    @Override
    public void onError(Socket socket, String error) {
        log.info(">>> SOCKET ERROR: ", error);
    }

    @Override
    public void onMessage(String topic, Envelope envelope) {
        Intent i = new Intent();
        if (topic.equalsIgnoreCase("message")) {
            try {
                final SignalDTO msg = objectMapper.treeToValue(envelope.getPayload(), SignalDTO.class);
                i.setAction(Constant.IntentTopic.ON_SIGNAL_EVENT);
                i.putExtra(Constant.IntentExtraKey.MESSAGE, msg);
                getBaseContext().sendBroadcast(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (topic.equalsIgnoreCase("ok")) {
            log.info(">>> ON JOINED SUCCEED: ", envelope.getPayload().asText());
        }
    }

    @Override
    public void onTimeout(String topic, String message) {
        //log.info(">>> TIMEOUT, TOPIC: " + topic + "; MSG: " + message);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void sendMessage(final String message) {
        final IChannelEventsListener listener = SignalingWorker.this;
        if (channel != null) {
            final ObjectNode payload = objectMapper.createObjectNode();
            payload.put("body", message);
            try {
                channel.push(message_topic, payload).receive("ok", new IMessageCallback() {
                    @Override
                    public void onMessage(final Envelope envelope) {
                        listener.onMessage("ok", envelope);
                    }
                }).timeout(new ITimeoutCallback() {
                    @Override
                    public void onTimeout() {
                        listener.onTimeout(message_topic, message);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
