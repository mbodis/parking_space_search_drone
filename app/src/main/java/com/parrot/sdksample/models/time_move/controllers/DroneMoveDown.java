package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.time_move.iface.MoveActionIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class DroneMoveDown extends MoveActionIface {

    public static final String TAG = DroneMoveDown.class.getName();

    public DroneMoveDown(int speed, int durationMilis) {
        super("MoveDown", DIRECTION_DOWN, speed, durationMilis);
    }

    public DroneMoveDown(int durationMilis) {
        super("MoveDown", DIRECTION_DOWN, SPEED_NORMAL, durationMilis);
    }

    @Override
    public void executeOnMoveStarts(BebopDrone mBebopDrone) {
        mBebopDrone.setGaz((byte) -speed);
    }

    @Override
    public void executeOnMoveEnds(BebopDrone mBebopDrone) {
        mBebopDrone.setGaz((byte) 0);
    }
}
