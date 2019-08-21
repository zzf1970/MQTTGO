package com.example.mqttgo.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import io.moquette.BrokerConstants;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.moquette.proto.messages.AbstractMessage;
import io.moquette.proto.messages.PublishMessage;
import io.moquette.server.Server;
import io.moquette.server.config.MemoryConfig;

public class MqttService extends Service {

    private static final String TAG = "MqttService";

    private Server broker;

    @Override
    public void onCreate() {
        super.onCreate();
        createMqttService();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //开启mqtt服务
    private void createMqttService() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                broker = new Server();
                String path = getFilesDir().getAbsolutePath() + File.separator + BrokerConstants.DEFAULT_MOQUETTE_STORE_MAP_DB_FILENAME;
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
                try {
                    MemoryConfig memoryConfig = new MemoryConfig(new Properties());
                    memoryConfig.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, path);
                    List<InterceptHandler> list = new ArrayList<>();
                    list.add(new MyMqttServiceHandler());
                    broker.startServer(memoryConfig, list);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //mqtt回调
    private class MyMqttServiceHandler implements InterceptHandler {

        @Override
        public void onConnect(InterceptConnectMessage msg) {
            Log.i(TAG, "onConnect -->  clientID: " + msg.getClientID());
        }

        @Override
        public void onDisconnect(InterceptDisconnectMessage msg) {
            Log.i(TAG, "onDisconnect -->  clientID: " + msg.getClientID());
        }

        @Override
        public void onPublish(InterceptPublishMessage msg) {
            Log.i(TAG, "onPublish -->  clientID: " + msg.getClientID()
                    + ", topic: " + msg.getTopicName()
                    + ", message: " +bufferToString( msg.getPayload()));
            sendMessage("hello client");
        }

        @Override
        public void onSubscribe(InterceptSubscribeMessage msg) {
            Log.i(TAG, "onSubscribe -->  clientID: " + msg.getClientID()
                    + ", topic: " + msg.getTopicFilter());
        }

        @Override
        public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
            Log.i(TAG, "onUnsubscribe -->  clientID: " + msg.getClientID()
                    + ", topic: " + msg.getTopicFilter());
        }
    }

    //服务器发布消息
    private void sendMessage(String s) {
        PublishMessage message = new PublishMessage();
        message.setTopicName("Test/client");
        message.setPayload(ByteBuffer.wrap(s.getBytes()));
        message.setQos(AbstractMessage.QOSType.EXACTLY_ONCE);
        Log.i(TAG, "sendMessage: " + bufferToString(message.getPayload()));
        broker.internalPublish(message);
    }

    //ByteBuffer转string
    private String bufferToString(ByteBuffer byteBuffer){
        byte[] b = new byte [byteBuffer.limit()];
        byteBuffer.get(b);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            stringBuilder.append((char) b [i]);
        }
        return stringBuilder.toString();
    }

    @Override
    public void onDestroy() {
        //关闭mqtt服务
        broker.stopServer();
        super.onDestroy();
    }

}
