package com.example.LooMotion;

/**
 * Class Used to store some constant
 */
public class Constant {
    /**
     * Called in basic.movement.Odemetry, used to adjust odometry coordinate sys to location system coordinate sys
     */
    public static final float ANGULAR_OFFSET = 0;

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
}
