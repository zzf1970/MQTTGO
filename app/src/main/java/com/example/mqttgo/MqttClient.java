package com.example.mqttgo;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Timer;
import java.util.TimerTask;

public class MqttClient {
    private static final String TAG = "MqttClient";
    public static final int MQTT_SERVER_TO_CLIENT = 1;
    public static final int MQTT_CLIENT_TO_SERVER = 2;

    private String mqttServerURI = "tcp://192.168.43.1:1883";
    private String mqttClientId;
    private String mqttSubTopic;
    private String mqttPubTopic;

//    private static MqttClient instance = new MqttClient();
//
//    public MqttClient() {
//    }
//
//    public static MqttClient getInstance() {
//        return instance;
//    }

    private MqttAndroidClient mClient;
    private MqttConnectOptions mqttConnectOptions;
    private int attemptConnectTimes = 0;

    public void init(Context context, MqttListener mqttListener, int clientType) {
        initWithClientType(clientType);
        mClient = new MqttAndroidClient(context, mqttServerURI, mqttClientId);
        this.mqttListener = mqttListener;
        mqttConnectOptions = getMqttConnectOptions();

        mClient.setCallback(new MqttCallbackExtended() {

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.i(TAG, "onConnectComplete --> clientId: " + mqttClientId + ", serverURI: " + serverURI);
                try {
                    subscribe();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "onConnectionLost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                Log.i(TAG, "onMessageArrived ----> topic: " + topic + ", message: " + message.toString());
                mqttListener.onMessageArrived(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                try {
                    Log.i(TAG, "onDeliveryComplete --> message: " + token.getMessage().toString());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void initWithClientType(int type) {
        if (type == MQTT_CLIENT_TO_SERVER) {
            mqttClientId = "AC" + System.currentTimeMillis();
            mqttSubTopic = "Test/client";
            mqttPubTopic = "Test/server";
        } else {
            mqttClientId = "AndroidServer";
            mqttSubTopic = "Test/server";
            mqttPubTopic = "Test/client";
        }
    }

    public void sendMessage(String message) {
        if (mClient == null) {
            return;
        }

        try {
            publish(message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (mClient != null) {
            if (mClient.isConnected()) {
                try {
                    disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG, "close: ");
//            mClient.close();
            mClient = null;
        }
    }

    private MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
//        options.setUserName("username");
//        options.setPassword("password".toCharArray());
        options.setAutomaticReconnect(true);
        return options;
    }

    private void connect() throws MqttException {
        mClient.connect(mqttConnectOptions, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i(TAG, "onConnectSuccess: ");
                mqttListener.onConnectSuccess();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i(TAG, "onConnectFailure: ");
//                重试5次
                if (attemptConnectTimes < 5) {
                    //1秒后重新连接
                    new Timer().schedule(new TimerTask() {
                        public void run() {
                            attemptConnectTimes++;
                            Log.i(TAG, "onReconnect: " + attemptConnectTimes);
                            try {
                                connect();
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 1000);
                } else {
                    //连接失败
                    mqttListener.onConnectFailure();
                }
            }
        });
    }

    private void subscribe() throws MqttException {
        mClient.subscribe(mqttSubTopic, 1, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i(TAG, "onSubscribeSuccess --> topic: " + mqttSubTopic);
                mqttListener.onSubscribeSuccess(mqttSubTopic);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i(TAG, "onSubscribeFailure --> topic: " + mqttSubTopic);
            }
        });
    }

    private void publish(String message) throws MqttException {
        mClient.publish(mqttPubTopic, message.getBytes(), 1, false, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i(TAG, "onPublishSuccess: ");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i(TAG, "onPublishFailure: ");
            }
        });
    }

    private void unsubscribe() throws MqttException {
        mClient.unsubscribe(mqttSubTopic, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i(TAG, "onUnsubscribeSuccess: ");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i(TAG, "onUnsubscribeFailure: ");
            }
        });
    }

    private void disconnect() throws MqttException {
        mClient.disconnect(null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i(TAG, "onDisconnectSuccess: ");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i(TAG, "onDisconnectFailure: ");
            }
        });
    }

    private MqttListener mqttListener;

    public interface MqttListener {
        void onConnectSuccess();

        void onConnectFailure();

        void onSubscribeSuccess(String topic);

        void onMessageArrived(String topic, MqttMessage message);
    }
}
