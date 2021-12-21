package com.example.LooMotion.service;

import com.example.LooMotion.basic.movement.MovementUnit;
import com.example.LooMotion.basic.RecognizerUnit;
import com.example.LooMotion.basic.UltrasonicUnit;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.grammar.GrammarConstraint;
import com.segway.robot.sdk.voice.grammar.Slot;
import com.segway.robot.sdk.voice.recognition.RecognitionListener;
import com.segway.robot.sdk.voice.recognition.RecognitionResult;
import com.segway.robot.sdk.voice.recognition.WakeupListener;
import com.segway.robot.sdk.voice.recognition.WakeupResult;

import java.util.LinkedList;
import java.util.List;

/**
 * Class of providing Move on Voice service
 * The istance of this class should be bind on Recognizer
 */
public class MoveOnVoice implements ServiceBinder.BindStateListener {

    MovementUnit move;
    UltrasonicUnit sensor;
    RecognizerUnit recog;

    public MoveOnVoice() {
        this.move = new MovementUnit();
        this.sensor = new UltrasonicUnit();
        this.recog = new RecognizerUnit();
    }

    /********************************************************************
     *                                                                  *
     *            Implementation of methods in StateBinder              *
     *                                                                  *
     ********************************************************************/

    @Override
    public void onBind() {
        try {
            initGrammar();
            recog.getUnit().startWakeupAndRecognition(wakeupListener, recognitionListener);
        }catch (VoiceException e){
            System.out.println("Initializing Recognizer Grammar failed");
        }
    }

    @Override
    public void onUnbind(String reason) {

    }

    /********************************************************************
     *                                                                  *
     *            Initialize Grammar of Voice Recognition               *
     *                                                                  *
     ********************************************************************/

    /**
     * Method to create a grammar for voice recognition
     * The Grammar is bind to Recognizer instead of listener
     * The Grammar is consisted of at most 2 slots(?)
     * @throws VoiceException
     */
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
        GrammarConstraint movementGrammar = new GrammarConstraint("movements", movementSlotList);

        //Adding the grammar constraint to the Recognizer
        recog.getUnit().addGrammarConstraint(movementGrammar);
    }

    /********************************************************************
     *                                                                  *
     *            Initialize Listener For Voice Recognition:            *
     *              1. Recognition Listener                             *
     *              2. Wakeup Listener                                  *
     *                                                                  *
     ********************************************************************/

    /**
     * Creating new RecognitionListener
     * This is the coral logic part of this service
     */
    RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onRecognitionStart() {

        }

        @Override
        public boolean onRecognitionResult(RecognitionResult recognitionResult) {
            //Loading commands into "result"
            String result = recognitionResult.getRecognitionResult();

            //Resetting the base origin
            move.baseOriginReset();

            if (result.contains("move") || result.contains("go") || result.contains("turn")) {
                //Checking second slot for forward
                if (result.contains("forward")) {
                    move.moveTo(0.5f, 0.0f, sensor.getDist());
                } else if (result.contains("backward")) {
                    move.moveTo(-0.5f, 0.0f, sensor.getDist());
                } else if (result.contains("left")) {
                    move.moveTo(0.0f, 0.5f, sensor.getDist());
                } else if (result.contains("right")) {
                    move.moveTo(0.5f, -0.5f, sensor.getDist());
                }

                //Checking first slot for "get"
            } else if (result.contains("get")) {

            }

            return false;
        }

        @Override
        public boolean onRecognitionError(String error) {
            return false;
        }
    };

    /**
     * Creating new WakeupListener
     * The wakeup Listener is used to detect the wake up phase,
     * e.g. "OK Loomo!"
     */
    WakeupListener wakeupListener = new WakeupListener() {
        @Override
        public void onStandby() {}

        @Override
        public void onWakeupResult(WakeupResult wakeupResult) { }

        @Override
        public void onWakeupError(String error) { }
    };

}
