package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.time_move.iface.MoveActionIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class DroneMoveRotateRight extends MoveActionIface {

    public static final String TAG = DroneMoveRotateRight.class.getName();

    public DroneMoveRotateRight(int speed, int durationMilis) {
        super("MoveRotateRight", MoveActionIface.DIRECTION_ROTATE_RIGHT, speed, durationMilis);
    }

    public DroneMoveRotateRight(int durationMilis) {
        super("MoveRotateRight", MoveActionIface.DIRECTION_ROTATE_RIGHT, MoveActionIface.SPEED_NORMAL, durationMilis);
    }

    @Override
    public void executeOnMoveStarts(BebopDrone mBebopDrone) {
        mBebopDrone.setYaw((byte) speed);
    }

    @Override
    public void executeOnMoveEnds(BebopDrone mBebopDrone) {
        mBebopDrone.setYaw((byte) 0);
    }
}
