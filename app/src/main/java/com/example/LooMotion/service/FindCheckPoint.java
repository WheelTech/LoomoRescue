package com.example.LooMotion.service;

import com.example.LooMotion.basic.movement.MovementUnit;
import com.segway.robot.algo.Pose2D;
import com.segway.robot.algo.minicontroller.CheckPoint;
import com.segway.robot.algo.minicontroller.CheckPointStateListener;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.sbv.Base;

public class FindCheckPoint implements ServiceBinder.BindStateListener {
    MovementUnit move;

    @Override
    public void onBind() {
        //Setting Base control mode to Navigation mode
        move.getUnit().setControlMode(Base.CONTROL_MODE_NAVIGATION);
        move.getUnit().setOnCheckPointArrivedListener(new CheckPointStateListener() {
            @Override
            public void onCheckPointArrived(CheckPoint checkPoint, Pose2D realPose, boolean isLast) {
                //Resetting base origin
                move.baseOriginReset();
            }

            @Override
            public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {

            }
        });
    }

    @Override
    public void onUnbind(String reason) {

    }
}
