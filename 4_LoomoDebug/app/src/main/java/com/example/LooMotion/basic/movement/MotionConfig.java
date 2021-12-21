package com.example.LooMotion.basic.movement;

/**
 * Class Used to store constants related to motion
 */
public class MotionConfig {
    /**
     * Called in basic.movement.Odemetry, used to adjust odometry coordinate sys to location system coordinate sys
     */
    public static final float ANGULAR_OFFSET = 0;

    /* *****************************
     *  Params For Angular Motion  *
     *******************************/
    // Param for portion loop
    public static final float THETA_KP = 2.0f;
    public static final float THETA_KP_DISABLE_THRE = 0.5f; // unit: radian

    // Param for integral loop
    public static final float THETA_KI = 0.5f;
    public static final int THETA_TI = 3; // unit: *50 ms

    public static final float MAX_ANGULAR_P_SPEED = 5.0f;
    public static final float MAX_ANGULAR_I_SPEED = 0.8f;

    // Param for Terminate Condition
    public static final float ANGULAR_ARR_DELTA = 0.05f;
    public static final float ANGULAR_ARR_VARIANCE = 0.001f;


    /* *****************************
     *   Params For Linear Motion  *
     *******************************/
    // Param for portion loop
    public static final float DIST_KP = 0.6f;
    public static final float MAX_LINEAR_P_SPEED = 10.0f;
    public static final float MIN_LINEAR_P_SPEED = 0.35f;

    // Param for Differential loop
    public static final float DIST_KD = 1.0f;
    public static final float MAX_LINEAR_D_SPEED = 10.0f;

    // Param for terminate condition
    public static final float LINEAR_BRAKE_THRE = 1.0f;
    public static final float LINEAR_ARR_DELT = 0.01f;
    public static final float LINEAR_ARR_VARIANCE = 0.001f;
}
