package com.example.LooMotion.basic;

import android.content.Context;
import android.widget.TextView;

import com.example.LooMotion.tool.MQTTClient;

public class LocationUnit {
    MQTTClient client;
    TextView textViewX;
    TextView textViewY;

    public LocationUnit(Context context){
        client = new MQTTClient(context);
        client.initMqtt(new MQTTClient.MsgListener(){
            @Override
            public void onMsgReturned(String x, String y) {
                textViewX.setText(x);
                textViewY.setText(y);
            }
        });

    }
}
