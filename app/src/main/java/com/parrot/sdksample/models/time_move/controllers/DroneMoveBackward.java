package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.time_move.iface.MoveActionIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class DroneMoveBackward extends MoveActionIface {

    public static final String TAG = DroneMoveBackward.class.getName();

    public DroneMoveBackward(int speed, int durationMilis) {
        super(DIRECTION_BACKWARD, speed, durationMilis);
    }

    public DroneMoveBackward(int durationMilis) {
        super(DIRECTION_BACKWARD, SPEED_NORMAL, durationMilis);
    }

    @Override
    public void executeOnMoveStarts(BebopDrone mBebopDrone) {
        mBebopDrone.setPitch((byte) -speed);
        mBebopDrone.setFlag((byte) 1);
    }

    @Override
    public void executeOnMoveEnds(BebopDrone mBebopDrone) {
        mBebopDrone.setPitch((byte) 0);
        mBebopDrone.setFlag((byte) 0);
    }
}
