package com.example.mqttgo.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.mqttgo.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.start_server_activity_btn)
    public void startServerActivity() {
        startActivity(new Intent(MainActivity.this, ServerActivity.class));
        finish();
    }

    @OnClick(R.id.start_client_activity_btn)
    public void startClientActivity() {
        startActivity(new Intent(MainActivity.this, ClientActivity.class));
        finish();
    }
}
