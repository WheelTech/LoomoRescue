package com.example.LooMotion.basic.location;

import android.content.Context;

import com.example.LooMotion.tool.MQTTClient;
import com.example.LooMotion.tool.math.Vec2D;

public class LocationUnit {
    MQTTClient client;

    public static Vec2D pos = new Vec2D();

    public LocationUnit(Context context){
        client = new MQTTClient(context);
        client.initMqtt(new MQTTClient.MsgListener(){
            @Override
            public void onMsgReturned(String x, String y) {
                pos.x = Float.parseFloat(x);
                pos.y = Float.parseFloat(y);
            }
        });

    }
}
