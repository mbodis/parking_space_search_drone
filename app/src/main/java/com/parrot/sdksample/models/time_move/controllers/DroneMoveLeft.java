package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.time_move.iface.MoveActionIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class DroneMoveLeft extends MoveActionIface {

    public static final String TAG = DroneMoveLeft.class.getName();

    public DroneMoveLeft(int speed, int durationMilis) {
        super("MoveLeft", DIRECTION_LEFT, speed, durationMilis);
    }

    public DroneMoveLeft(int durationMilis) {
        super("MoveLeft", DIRECTION_LEFT, SPEED_NORMAL, durationMilis);
    }

    @Override
    public void executeOnMoveStarts(BebopDrone mBebopDrone) {
        mBebopDrone.setRoll((byte) -speed);
        mBebopDrone.setFlag((byte) 1);
    }

    @Override
    public void executeOnMoveEnds(BebopDrone mBebopDrone) {
        mBebopDrone.setRoll((byte) 0);
        mBebopDrone.setFlag((byte) 0);
    }
}
