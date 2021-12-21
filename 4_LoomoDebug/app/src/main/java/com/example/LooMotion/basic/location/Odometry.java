package com.example.LooMotion.basic.location;

import com.example.LooMotion.basic.movement.MotionConfig;
import com.segway.robot.sdk.locomotion.sbv.Base;

import java.util.TimerTask;


/**
 * Class for Odometry sensor, used to read Angular and position data
 * Need to run in subThread to get
 *
 * The coordination system is as followed:
 * ******************************************************
 *                                                      *
 *                                   X_Axis + (W)       *
 *                                                      *
 *                              0                       *
 *       Y_Axis + (S)           |                       *
 *                            (W)                       *
 *              0.5 PI  --(S)    (N)--  -0.5 PI         *
 *                           (E)                        *
 *                            |                         *
 *                        PI    - PI                    *
 *                                                      *
 * ******************************************************
 * variable OFFSET can be used to adjust the coordination
 */
public class Odometry extends TimerTask {
    /**
     * The time of odometry sensor, called as a param in getOdometry();
     * When it is -1, the function will return the current pose
     */
    public static final long SAMPLE_TIME = -1;

    /**
     * The Data of angular get every 50 ms
     */
    public static final long DATA_INTERVAL = 50; // unit: ms

    /**
     * Offset add to theta reading, used to adjust coordinate system
     */
    private static float ANGULAR_OFFSET = MotionConfig.ANGULAR_OFFSET;

    /**
     * Flag to indicate that data is coming
     */
    private boolean thetaFlag = false;
    private boolean posXFlag = false;
    private boolean posYFlag = false;

    private float theta;
    private float posX;
    private float posY;

    /**
     * Flag to mark whether the pos is initialized
     */
    private boolean posInitFlag;
    private float originPosX;
    private float originPosY;

    Base base;

    public Odometry(Base base){
        this.base = base;
    }

    /**
     * To reset the Origin point of Loomo
     * @param x
     * @param y
     */
    public void setOriginPoint(float x, float y){
        this.originPosX = x;
        this.originPosY = y;

        this.posInitFlag = true;
    }

    /**
     * Function used to check whether theta is updated since the last read
     * @return true if data is updated, false if not
     */
    public boolean hasTheta(){
        return thetaFlag;
    }

    public float getTheta() {
        thetaFlag = false;
        return this.theta;
    }

    /**
     * Function used to check whether Pos X is updated since the last read
     * @return true if data is updated, false if not
     */
    public boolean hasPosX(){
        return this.posXFlag;
    }

    public float getPosX(){
        posXFlag = false;
        return this.posX;
    }

    /**
     * Function used to check whether Pos Y is updated since the last read
     * @return true if data is updated, false if not
     */
    public boolean hasPosY(){
        return this.posYFlag;
    }

    public float getPosY(){
        posYFlag = false;
        return this.posY;
    }

    /**
     * Entry of Class
     * Read odometry data every 50 ms
     */
    @Override
    public void run() {
        float pi = (float) Math.PI;

        /* ************************
         *    Get Angle Reading   *
         **************************/
        thetaFlag = true;
        theta = base.getOdometryPose(SAMPLE_TIME).getTheta() + ANGULAR_OFFSET;
        if(theta > pi){
            theta -= 2 * pi;
        }else if(theta < - pi){
            theta += 2 * pi;
        }

        /* *********************
         *   Get Pos Reading   *
         ***********************/
        // Reset the origin point
        if(!posInitFlag){
            posInitFlag = true;
            originPosX = base.getOdometryPose(SAMPLE_TIME).getX();
            originPosY = base.getOdometryPose(SAMPLE_TIME).getY();
        }

        posXFlag = true;
        posYFlag = true;
        posX = base.getOdometryPose(SAMPLE_TIME).getX() - originPosX;
        posY = base.getOdometryPose(SAMPLE_TIME).getY() - originPosY;


    }
}
