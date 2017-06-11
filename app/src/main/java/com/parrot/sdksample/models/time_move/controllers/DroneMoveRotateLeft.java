package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.time_move.iface.MoveActionIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class DroneMoveRotateLeft extends MoveActionIface {

    public static final String TAG = DroneMoveRotateLeft.class.getName();

    public DroneMoveRotateLeft(int speed, int durationMilis) {
        super("MoveRotateLeft", MoveActionIface.DIRECTION_ROTATE_LEFT, speed, durationMilis);
    }

    public DroneMoveRotateLeft(int durationMilis) {
        super("MoveRotateLeft", MoveActionIface.DIRECTION_ROTATE_LEFT, MoveActionIface.SPEED_NORMAL, durationMilis);
    }

    @Override
    public void executeOnMoveStarts(BebopDrone mBebopDrone) {
        mBebopDrone.setYaw((byte) -speed);
    }

    @Override
    public void executeOnMoveEnds(BebopDrone mBebopDrone) {
        mBebopDrone.setYaw((byte) 0);
    }
}
