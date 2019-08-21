package com.example.mqttgo.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mqttgo.MqttClient;
import com.example.mqttgo.MqttClient.MqttListener;
import com.example.mqttgo.R;
import com.example.mqttgo.services.MqttService;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import butterknife.ButterKnife;

public class ServerActivity extends ClientActivity{

    private Intent startMqttServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        startMqttServiceIntent = new Intent(ServerActivity.this, MqttService.class);
        startService(startMqttServiceIntent);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        stopService(startMqttServiceIntent);
        super.onDestroy();
    }

}
