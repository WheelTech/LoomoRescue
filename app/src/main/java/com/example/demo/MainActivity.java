package com.example.demo;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.segway.robot.algo.Pose2D;
import com.segway.robot.algo.minicontroller.CheckPoint;
import com.segway.robot.algo.minicontroller.CheckPointStateListener;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.perception.sensor.Sensor;
import com.segway.robot.sdk.perception.sensor.SensorData;
import com.segway.robot.sdk.voice.Recognizer;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.grammar.GrammarConstraint;
import com.segway.robot.sdk.voice.grammar.Slot;
import com.segway.robot.sdk.voice.recognition.RecognitionListener;
import com.segway.robot.sdk.voice.recognition.RecognitionResult;
import com.segway.robot.sdk.voice.recognition.WakeupListener;
import com.segway.robot.sdk.voice.recognition.WakeupResult;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //Initialization of all objects and variables
    Recognizer mRecognizer;
    Base mBase;
    Sensor mSensor;
    ServiceBinder.BindStateListener mRecognizerBindStateListener;
    ServiceBinder.BindStateListener mBaseBindStateListener;
    GrammarConstraint movementGrammar;
    WakeupListener mWakeupListener;
    RecognitionListener mRecognitionListener;


    public int x=0;     //X coordinate counter (Unit: m)
    public int y=0;     //Y coordinate counter (Unit: m)
    float us = 1000;    //Constant for distance checking (Unit: mm)

    TextView textViewX;
    TextView textViewY;
    TextView textViewDistance;

    //Auto created onCreate method gets called every time the app gets started
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //calling get.instance method for objects
        mBase = Base.getInstance();
        mRecognizer = Recognizer.getInstance();
        mSensor = Sensor.getInstance();

        init();

        initListeners();

        //Setting all TextViews to zero
        textViewX.setText(String.format("%d", x));
        textViewY.setText(String.format("%d", y));
        textViewDistance.setText(String.format("0"));


    }

    //Method to initialise the TextViews with the matching id
    public void init(){
        textViewX = findViewById(R.id.x_coordinate);
        textViewY = findViewById(R.id.y_coordinate);
        textViewDistance = findViewById(R.id.distance);

    }

    //Auto created onResume method binding objects to the server
    @Override
    protected void onResume(){
        super.onResume();
        mRecognizer.bindService(getApplicationContext(),mRecognizerBindStateListener);
        mBase.bindService(getApplicationContext(),mBaseBindStateListener);
    }

    //Method in which the Listeners are being initialized and the movement of Loomo is defined
    public void initListeners(){


        mRecognizerBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                try {
                    initGrammar();
                    mRecognizer.startWakeupAndRecognition(mWakeupListener, mRecognitionListener);
                } catch (VoiceException e) {}


            }

            @Override
            public void onUnbind(String reason) {

            }
        };

        //creating new WakeupListener
        mWakeupListener = new WakeupListener() {
            @Override
            public void onStandby() {

            }

            @Override
            public void onWakeupResult(WakeupResult wakeupResult) {

            }

            @Override
            public void onWakeupError(String error) {

            }
        };

        mSensor.bindService(getApplicationContext(), new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {

            }

            @Override
            public void onUnbind(String reason) {

            }
        });

        //creating new RecognitionListener
        mRecognitionListener = new RecognitionListener() {
            @Override
            public void onRecognitionStart() {

            }

            //Checkpoint settings for every command combination
            @Override
            public boolean onRecognitionResult(RecognitionResult recognitionResult) {
                //Loading commands into "result"
                String result = recognitionResult.getRecognitionResult();

                //Resetting the base origin
                baseOriginReset();

                //Checking first slot for "rotate"
                if(result.contains("rotate")){

                    //Checking second slot for left and setting new checkpoint for left rotation
                    if (result.contains("left")){

                        mBase.addCheckPoint(0f,0f, (float) (Math.PI/2));

                    //Checking second slot for right and setting new checkpoint for right rotation
                    } else if (result.contains("right")){

                        mBase.addCheckPoint(0f, 0f, (float) -(Math.PI/2));

                    }

                //Checking first slot for "move", "go", or "turn"
                }else if (result.contains("move") || result.contains("go") ||result.contains("turn")){

                    //Checking second slot for forward
                    if (result.contains("forward")) {

                        //asking ultrasonic sensor for current distance
                        SensorData mUltrasonicData = mSensor.querySensorData(Arrays.asList(Sensor.ULTRASONIC_BODY)).get(0);
                        float mUltrasonicDistance = mUltrasonicData.getIntData()[0];

                        //Checking if way ahead is clear
                        if (mUltrasonicDistance >= us) {
                            mBase.addCheckPoint(1f, 0f);

                            //SystemClock.sleep(3000);

                            //increaseX();

                            //updateCoordinateX();

                            //Increase x counter variable
                            x++;

                            //updateCoordinates();
                        }

                    }else if(result.contains("backward")){
 //                    CheckPoint flag;

                        //Rotate to the commanded direction
                        mBase.addCheckPoint(0f, 0f, (float) Math.PI);
//                        if(flag instanceof CheckPoint) {

                        //Wait for rotation to end
                        SystemClock.sleep(5000);

                        //asking ultrasonic sensor for current distance
                        SensorData mUltrasonicData = mSensor.querySensorData(Arrays.asList(Sensor.ULTRASONIC_BODY)).get(0);
                        float mUltrasonicDistance = mUltrasonicData.getIntData()[0];

                        //Checking if way ahead is clear
                        if (mUltrasonicDistance >= us) {
                            mBase.addCheckPoint(1f, 0f);

                            x--;

                        }

                    }else if(result.contains("left")){


                        mBase.addCheckPoint(0f, 0f, (float) (Math.PI/2));

                        SystemClock.sleep(3000);

                        SensorData mUltrasonicData = mSensor.querySensorData(Arrays.asList(Sensor.ULTRASONIC_BODY)).get(0);
                        float mUltrasonicDistance = mUltrasonicData.getIntData()[0];

                        //Checking if way ahead is clear
                        if (mUltrasonicDistance >= us) {
                            mBase.addCheckPoint(1f, 0f);
                            y++;

                        }

                    }else if(result.contains("right")){

                        //Rotate to the commanded direction
                        mBase.addCheckPoint(0f, 0f, (float) -(Math.PI/2));

                        //Wait for rotation to end
                        SystemClock.sleep(3000);

                        //asking ultrasonic sensor for current distance
                        SensorData mUltrasonicData = mSensor.querySensorData(Arrays.asList(Sensor.ULTRASONIC_BODY)).get(0);
                        float mUltrasonicDistance = mUltrasonicData.getIntData()[0];

                        //Checking if way ahead is clear
                        if (mUltrasonicDistance >= us) {
                            mBase.addCheckPoint(1f, 0f);

                            y--;

                        }

                    }

                    //Calling the update method to update coordinates
                    updateCoordinates();

                //Checking first slot for "get"
                }else if (result.contains("get")){

                    //Checking second slot for "distance"
                    if (result.contains("distance")){

                        //asking ultrasonic sensor for current distance
                        SensorData mUltrasonicData = mSensor.querySensorData(Arrays.asList(Sensor.ULTRASONIC_BODY)).get(0);
                        float mUltrasonicDistance = mUltrasonicData.getIntData()[0];

                        //Updating the new distance into the TextView on screen
                        textViewDistance.setText(String.format("%f", mUltrasonicDistance));

                        //Update screen
                        textViewDistance.invalidate();
                    }
                }

                //updateCoordinateX();
                //updateCoordinateY();

                return false;
            }


            @Override
            public boolean onRecognitionError(String error) {
                return false;
            }
        };

        mBaseBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {

                //Setting Base control mode to Navigation mode
                mBase.setControlMode(Base.CONTROL_MODE_NAVIGATION);
                mBase.setOnCheckPointArrivedListener(new CheckPointStateListener() {
                    @Override
                    public void onCheckPointArrived(CheckPoint checkPoint, Pose2D realPose, boolean isLast) {

                        //Resetting base origin
                        baseOriginReset();

                    }

                    @Override
                    public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {

                    }
                });

            }

            @Override
            public void onUnbind(String reason) {

            }
        };



    }


    //Method to create a grammar Loomo can understand
    private void initGrammar() throws VoiceException {

        //Defining every possible word for the first slot
        Slot firstSlot = new Slot ("Movement");
        firstSlot.addWord("Move");
        firstSlot.addWord("go");
        firstSlot.addWord("turn");
        firstSlot.addWord("rotate");
        firstSlot.addWord("get");

        //Defining every possible word for the second slot
        Slot secondSlot = new Slot ("direction");
        secondSlot.addWord("forward");
        secondSlot.addWord("backward");
        secondSlot.addWord("left");
        secondSlot.addWord("right");
        secondSlot.addWord("distance");

        //Adding both slots to a list
        List<Slot> movementSlotList = new LinkedList<>();
        movementSlotList.add(firstSlot);
        movementSlotList.add(secondSlot);

        //Creating the grammar constraint
        movementGrammar = new GrammarConstraint("movements", movementSlotList);

        //Adding the grammar constraint to the Recognizer
        mRecognizer.addGrammarConstraint(movementGrammar);

    }

    //Method to reset the base origin to (0,0) at current position
    private void baseOriginReset(){
        mBase.setControlMode(Base.CONTROL_MODE_NAVIGATION);
        mBase.clearCheckPointsAndStop();
        mBase.cleanOriginalPoint();
        Pose2D newOriginPoint = mBase.getOdometryPose(-1);
        mBase.setOriginalPoint(newOriginPoint);
    }



    //Method to increase x counter (NOT USED)
    public void increaseX(){

        x++;
    }

    //Method to decrease x counter (NOT USED)
    public void decreaseX(){

        x--;
    }

    //Method to increase y counter (NOT USED)
    public void increaseY(){

        y++;
    }

    //Method to decrease y counter (NOT USED)
    public void decreaseY(){

        y--;
    }

    //Previous Method to increase update the x coordinate (NOT USED)
    public void updateCoordinateX(){
        //((TextView)findViewById(R.id.x_coordinate)).setText((char)x);
   //     x++;
        textViewX.setText(String.format("%d", x));
    }

    //Previous Method to increase update the y coordinate (NOT USED)
    public void updateCoordinateY(){
 //       ((TextView)findViewById(R.id.y_coordinate)).setText((char)y);
 //       y++;
        textViewY.setText(String.format("%d", y));

    }

    //Method to update the coordinate into the TextViews on screen
    public void updateCoordinates(){
        //Loading x and y counter into the TextViews
        textViewX.setText(String.format("%d", x));
        textViewY.setText(String.format("%d", y));

        // Attempt to refresh of the both textView fields (DOES NOT WORK)
        //textViewX.invalidate();
        //textViewY.invalidate();

        //finish();
        startActivity(getIntent());

    }

    //Alternative way to get distance by clicking on the button "DISTANCE" (Works, shows up on screen)
    public void getDistance(View v){
        //asking ultrasonic sensor for current distance
        SensorData mUltrasonicData = mSensor.querySensorData(Arrays.asList(Sensor.ULTRASONIC_BODY)).get(0);
        float mUltrasonicDistance = mUltrasonicData.getIntData()[0];

        //Updating the new distance into the TextView on screen
        textViewDistance.setText(String.format("%f", mUltrasonicDistance));

    }

    //Attempt to create a Toast (Pop up message at the bottom of the screen) that shows up when ultrasonic sensor detects an obstacle
    public void toaster(){
        Toast.makeText(this, "Obstacle in the Way!", Toast.LENGTH_LONG).show();
    }

    //Method to set both TextViews to 0
    public void resetCoordinates(View v){

        //Experiment if the screen updates the TextView if increased by the button
//        x++;
//        textViewX.setText(String.format("%d", x));

        x = 0;
        y = 0;

        updateCoordinates();

        //updateCoordinateX();
        //updateCoordinateY();
    }

}
