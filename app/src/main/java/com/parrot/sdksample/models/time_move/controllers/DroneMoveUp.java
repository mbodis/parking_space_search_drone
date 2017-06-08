package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.time_move.iface.MoveActionIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class DroneMoveUp extends MoveActionIface {

    public static final String TAG = DroneMoveUp.class.getName();

    public DroneMoveUp(int speed, int durationMilis) {
        super(DIRECTION_UP, speed, durationMilis);
    }

    public DroneMoveUp(int durationMilis) {
        super(DIRECTION_UP, SPEED_NORMAL, durationMilis);
    }

    @Override
    public void executeOnMoveStarts(BebopDrone mBebopDrone) {
        mBebopDrone.setGaz((byte) speed);
    }

    @Override
    public void executeOnMoveEnds(BebopDrone mBebopDrone) {
        mBebopDrone.setGaz((byte) 0);
    }
}
