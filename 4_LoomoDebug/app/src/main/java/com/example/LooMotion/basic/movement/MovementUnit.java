package com.example.LooMotion.basic.movement;

import android.content.Context;
import android.util.Log;

import com.example.LooMotion.basic.location.Odometry;
import com.example.LooMotion.tool.math.Calc;
import com.segway.robot.algo.Pose2D;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.sbv.Base;

import java.util.Timer;

/**
 * Class for data processing
 * All data should be in float type
 */


public class MovementUnit extends Thread {
    private static Base base;

    String TAG = "Movement";

    public MovementUnit() {
        if (base == null) {
            base = Base.getInstance();
        }
    }

    /**
     * Function to get Instance of Base
     *
     * @return {Base} base
     */
    public static Base getUnit() {
        return base;
    }

    /**
     * Called in MainActivity, Used to bind different Services
     *
     * @param context       is {android.content.Context}
     * @param stateListener stateListener
     */
    public static void bindService(Context context, ServiceBinder.BindStateListener stateListener) {
        base.bindService(context, stateListener);
    }

    /**
     * Entry of this thread
     */
    @Override
    public void run() {
        super.run();
        moveTest();
    }


    /* ******************************************************************
     *                                                                  *
     *                       Methods for Moving                         *
     *                                                                  *
     *     !!! Coordination and Controlling part should be added !!!    *
     *                                                                  *
     ********************************************************************/

    /**
     * Method to reset the base origin to (0,0) at current position
     */
    public void baseOriginReset() {
        base.setControlMode(Base.CONTROL_MODE_NAVIGATION);
        base.clearCheckPointsAndStop();
        base.cleanOriginalPoint();
        Pose2D newOriginPoint = base.getOdometryPose(-1);
        base.setOriginalPoint(newOriginPoint);
    }


    /**
     * Method for moving and rotating
     * Before moving detect whether there is enough space
     *
     * @param x     the x coordination to the origin point
     * @param y     the y coordination to the origin point
     * @param angle the pointed direction of robot
     */
    public void moveTo(float x, float y, float angle) {

    }




    /* ******************************************************************
     *                                                                  *
     *              Methods for Precise Angular movement                *
     *                                                                  *
     *                      PI control is applied                       *
     *                                                                  *
     ********************************************************************/

    // Variable for data processing
    private MotionDataSlot thetaSlot = new MotionDataSlot(8);

    // Param for portion loop
    private final float THETA_KP = MotionConfig.THETA_KP;
    private final float THETA_KP_DISABLE_THRE = MotionConfig.THETA_KP_DISABLE_THRE; // unit: radian

    // Param for integral loop
    private final float THETA_KI = MotionConfig.THETA_KI;
    private final int THETA_TI = MotionConfig.THETA_TI; // unit: *50 ms

    private final float MAX_ANGULAR_P_SPEED = MotionConfig.MAX_ANGULAR_P_SPEED;
    private final float MAX_ANGULAR_I_SPEED = MotionConfig.MAX_ANGULAR_I_SPEED;

    // Param for Terminate Condition
    private final float ANGULAR_ARR_DELTA = MotionConfig.ANGULAR_ARR_DELTA;
    private final float ANGULAR_ARR_VARIANCE = MotionConfig.ANGULAR_ARR_VARIANCE;

    /**
     * Rotate the robot to a designated angle
     *
     * @param target can be any numbery
     */
    private void moveToAngle(float target) {
        /* **************
         * Initializing *
         ****************/
        base.setControlMode(Base.CONTROL_MODE_RAW);

        // initialize Odometry
        Odometry odometry = new Odometry(base);
        Timer timer = new Timer();
        timer.schedule(odometry, 0, Odometry.DATA_INTERVAL);

        // Wait Sensor to initialize
        try {
            sleep(Odometry.DATA_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Initialize data slot
        float theta = odometry.getTheta();
        for (int i = 0; i < thetaSlot.getLength(); i++) {
            thetaSlot.push(theta);
        }

        // Initialize parameter
        float pi = (float) Math.PI;
        float leftEnd = target - pi;
        float rightEnd = target + pi;

        // Var for portion
        float pSpeed = 0;
        float delt= 0;

        // var for integral
        float iSpeed = 0;
        float thetaError = 0;
        float speed = 0;

        // Loop to apply PI algorithm
        while (true) {

            /* **********************
             * Get angular position *
             ************************/
            // Get current state if Odometry is refreshed
            if (odometry.hasTheta()) {
                theta = odometry.getTheta();
                thetaSlot.push(theta);

                /* ****************************
                 *  Print Log, used to debug  *
                 ******************************/
                thetaSlot.printLog();
                Log.d(TAG, "moveToAngle Variance: " + thetaSlot.getVariance());
                Log.d(TAG, "moveToAngle AVG Theta: " + thetaSlot.getAvg());
                Log.d(TAG, "moveToAngle Target: " + target);
                Log.d(TAG, "moveToAngle AVG Delta: " + (thetaSlot.getAvg() - target) );
                Log.d(TAG, "moveToAngle Theta: " + odometry.getTheta());
                Log.d(TAG, "moveToAngle Delta: " + delt );
                Log.d(TAG, "moveToAngle Speed: " + speed );
                Log.d(TAG, "moveToAngle:     P Speed: " + pSpeed);
                Log.d(TAG, "moveToAngle:     I Speed: " + iSpeed);
            }


            /* *****************
             * Calculate Speed *
             *******************/

            // Calculate the Magnitude of speed gain in portion element
            // Portion element only work when delta distance is bigger than DISABLE_THREAD
            delt = Math.abs(target - theta);
            pSpeed = delt > THETA_KP_DISABLE_THRE ? delt * THETA_KP : 0;
            pSpeed = Calc.clamp(MAX_ANGULAR_P_SPEED, pSpeed);

            // Calculate the Magnitude of speed gain in integral element
            thetaError = Math.abs(THETA_TI * target - thetaSlot.getUpdateSum(THETA_TI) );
            iSpeed = delt < THETA_KP_DISABLE_THRE ? thetaError * THETA_KI : 0;
            iSpeed = Calc.clamp(MAX_ANGULAR_I_SPEED, iSpeed);

            speed = pSpeed + iSpeed;

            // Determine the direction of speed
            float relativeTheta = theta;
            if (theta < leftEnd) {
                relativeTheta += 2 * pi;
            }

            if (theta > rightEnd) {
                relativeTheta -= 2 * pi;
            }

            if (relativeTheta > target && relativeTheta <= rightEnd) {
                speed = -speed;
            }

            // Set speed
            base.setAngularVelocity(speed);


            /* *********************
             * Terminate condition *
             ***********************/
            if (Math.abs(thetaSlot.getAvg() - target) < ANGULAR_ARR_DELTA && thetaSlot.getVariance() < ANGULAR_ARR_VARIANCE) {
                Log.d(TAG, "Angular Position Arrived, Readings in Average: " + (thetaSlot.getAvg() - target));
                base.setAngularVelocity(0);
                break;
            }

        }
    }

    /* ******************************************************************
     *                                                                  *
     *              Methods for Precise Linear movement                 *
     *                                                                  *
     *                     PD control is applied                        *
     *                                                                  *
     ********************************************************************/
    /**
     * Function to test Movement Thread
     * Will be called in run() function
     */
    public void moveTest() {

        moveToAngle((float) (- 0.53 * Math.PI));
        moveToDist(5.0f);
        moveToAngle( (float) (0.47 * Math.PI) );
        moveToDist(5.0f);
        moveToAngle( (float) (- 0.53 * Math.PI) );
    }

    // Variable for data processing
    private MotionDataSlot distSlot = new MotionDataSlot(5);
    private MotionDataSlot pSpeedSlot = new MotionDataSlot(2);

    // Param for portion loop
    private final float DIST_KP = MotionConfig.DIST_KP;
    private final float MAX_LINEAR_P_SPEED = MotionConfig.MAX_LINEAR_P_SPEED;
    private final float MIN_LINEAR_P_SPEED = MotionConfig.MIN_LINEAR_P_SPEED;

    // Param for integral loop
    private final float DIST_KD = MotionConfig.DIST_KD;
    private final float MAX_LINEAR_D_SPEED = MotionConfig.MAX_LINEAR_D_SPEED;

    private final float LINEAR_BRAKE_THRE = MotionConfig.LINEAR_BRAKE_THRE;
    private final float LINEAR_ARR_DELT = MotionConfig.LINEAR_ARR_DELT;
    private final float LINEAR_ARR_VARIANCE = MotionConfig.LINEAR_ARR_VARIANCE;

    /**
     * Make Loomo move ahead for a certain distance
     * @param target is the distance, unit: meter
     */
    public void moveToDist(float target){
        /* **************
         * Initializing *
         ****************/
        base.setControlMode(Base.CONTROL_MODE_RAW);

        // initialize Odometry
        Odometry odometry = new Odometry(base);
        Timer timer = new Timer();
        timer.schedule(odometry, 0, Odometry.DATA_INTERVAL);

        // Wait Sensor to initialize
        try {
            sleep(Odometry.DATA_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Initialize data slot
        for (int i = 0; i < thetaSlot.getLength(); i++) {
            distSlot.push( (float) Math.sqrt(
                odometry.getPosX() * odometry.getPosX()
                + odometry.getPosY() * odometry.getPosY()
            ));
        }

        // Var for portion
        float pSpeed = 0;
        float dist = 0; // The distance have covered
        float delta= 0; // The delta between covered and targeted distance

        // var for differential
        float dSpeed = 0;
        float posDif = 0; // differential of position
        float speed = 0;

        while(true){

            /* **************
             * Get position *
             ****************/

            // Get current state if Odometry is refreshed
            if (odometry.hasPosX() && odometry.hasPosY()) {
                dist = (float) Math.sqrt(
                        odometry.getPosX() * odometry.getPosX()
                                + odometry.getPosY() * odometry.getPosY()
                );
                distSlot.push(dist);
                delta = target - dist;

                /* ****************************
                 *  Print Log, used to debug  *
                 ******************************/
//                Log.d(TAG, "X_POS: " + odometry.getPosX());
//                Log.d(TAG, "Y_POS: " + odometry.getPosY());
                Log.d(TAG, "moveToDist -- Target: " + target);
                Log.d(TAG, "moveToDist -- Dist: " + dist);
                Log.d(TAG, "moveToDist -- Delt: " + delta);
                Log.d(TAG, "moveToDist -- Speed : " + speed);
                Log.d(TAG, "moveToDist -- P_Speed: " + pSpeed);
                Log.d(TAG, "moveToDist -- D_Speed: " + dSpeed);

            }

            /* *****************
             * Calculate Speed *
             *******************/

            // Calculate the Magnitude of speed gain in portion element
            pSpeed = delta * DIST_KP;
            pSpeed = Calc.clamp(MAX_LINEAR_P_SPEED, MIN_LINEAR_P_SPEED, pSpeed);
            pSpeedSlot.push(pSpeed);

            // Calculate the Magnitude of speed gain in differential element
            posDif = distSlot.get(0) - distSlot.get(1);
            dSpeed = posDif * DIST_KD;
            dSpeed = Calc.clamp(MAX_LINEAR_D_SPEED,  dSpeed);

            speed = pSpeed + dSpeed;

            if(Math.abs(delta) < LINEAR_BRAKE_THRE){
                speed = pSpeed;
            }


            // Set speed
            base.setLinearVelocity(speed);

            /* *********************
             * Terminate condition *
             ***********************/
            if (Math.abs(distSlot.getAvg() - target) < LINEAR_ARR_DELT && distSlot.getVariance() < LINEAR_ARR_VARIANCE) {
                Log.d(TAG, "moveToDist Linear Position Arrived, Readings in Average: " + (distSlot.getAvg() - target));
                Log.d(TAG, "moveToDist Linear Position Arrived, Readings in Variance: " + (distSlot.getVariance()));
                base.setLinearVelocity(0);
                break;
            }
        }
    }





}

