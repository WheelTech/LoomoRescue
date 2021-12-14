package com.example.LooMotion;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LooMotion.basic.LocationUnit;
import com.example.LooMotion.basic.movement.MovementUnit;
import com.example.LooMotion.basic.RecognizerUnit;
import com.example.LooMotion.basic.UltrasonicUnit;
import com.example.LooMotion.service.FindCheckPoint;
import com.example.LooMotion.service.MoveOnVoice;
import com.example.LooMotion.tool.UDPClient;

public class MainActivity extends AppCompatActivity {
    Context applicationContext;

    TextView textViewX;
    TextView textViewY;
    TextView distText;
    TextView debugText;
    TextView speedText;

    MovementUnit move;

    /**
     * Auto created onCreate method gets called every time the app gets started
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initMovement();
        new UDPClient();
        new LocationUnit(getApplicationContext());
        new RecognizerUnit();
        new UltrasonicUnit();

    }

    //Method to initialise the TextViews with the matching id
    private void initView(){
        textViewX = findViewById(R.id.x_coordinate);
        textViewY = findViewById(R.id.y_coordinate);
        distText = findViewById(R.id.distText);
        debugText = findViewById(R.id.debugMsg);
        speedText = findViewById(R.id.speedText);
    }

    private void initMovement(){
        move = new MovementUnit();
    }

    /**
     *  Auto created onResume method binding objects to the server
     */

    @Override
    protected void onResume(){
        super.onResume();
        RecognizerUnit.bindService(getApplicationContext(), new MoveOnVoice());
        MovementUnit.bindService(getApplicationContext(), new FindCheckPoint());
        UltrasonicUnit.bindService(getApplicationContext());
    }

    public void showDistance(View v){
        UltrasonicUnit.showDistOnView(v, distText);
    }

    public void sendBtn(View v){
        UDPClient.sendMsg("aaa");
    }

    public void start(View v){
        move.run();

    }

    public void terminate(View v){

    }
}
