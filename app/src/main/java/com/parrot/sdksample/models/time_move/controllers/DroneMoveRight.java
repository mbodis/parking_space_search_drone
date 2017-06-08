package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.time_move.iface.MoveActionIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class DroneMoveRight extends MoveActionIface {

    public static final String TAG = DroneMoveRight.class.getName();

    public DroneMoveRight(int speed, int durationMilis) {
        super(DIRECTION_RIGHT, speed, durationMilis);
    }

    public DroneMoveRight(int durationMilis) {
        super(DIRECTION_RIGHT, SPEED_NORMAL, durationMilis);
    }

    @Override
    public void executeOnMoveStarts(BebopDrone mBebopDrone) {
        mBebopDrone.setRoll((byte) speed);
        mBebopDrone.setFlag((byte) 1);
    }

    @Override
    public void executeOnMoveEnds(BebopDrone mBebopDrone) {
        mBebopDrone.setRoll((byte) 0);
        mBebopDrone.setFlag((byte) 0);
    }
}
