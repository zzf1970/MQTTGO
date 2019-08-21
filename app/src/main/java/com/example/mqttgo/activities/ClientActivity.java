package com.example.mqttgo.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.mqttgo.MqttClient;
import com.example.mqttgo.R;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import butterknife.ButterKnife;

public class ClientActivity extends AppCompatActivity implements MqttClient.MqttListener {

    private static final String TAG = "ClientActivity";
    private MqttClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        ButterKnife.bind(this);

        createMqttClient();
    }

    private void createMqttClient() {
        mqttClient = new MqttClient();
        mqttClient.init(getApplicationContext(), this, MqttClient.MQTT_CLIENT_TO_SERVER);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        mqttClient.close();
        mqttClient = null;
        super.onDestroy();
    }


    @Override
    public void onConnectSuccess() {

    }

    @Override
    public void onConnectFailure() {

    }

    @Override
    public void onSubscribeSuccess(String topic) {
        mqttClient.sendMessage("hello server");
    }

    @Override
    public void onMessageArrived(String topic, MqttMessage message) {

    }
}
