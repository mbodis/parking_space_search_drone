package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.time_move.iface.TimeMoveIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class TimeMoveForward extends TimeMoveIface {

    public static final String TAG = TimeMoveForward.class.getName();

    public TimeMoveForward(int speed, int durationMilis) {
        super(DIRECTION_BACKWARD, speed, durationMilis);
    }

    public TimeMoveForward(int durationMilis) {
        super(DIRECTION_FORWARD, SPEED_NORMAL, durationMilis);
    }

    @Override
    public void executeOnMoveStarts(BebopDrone mBebopDrone) {
        mBebopDrone.setPitch((byte) speed);
        mBebopDrone.setFlag((byte) 1);
    }

    @Override
    public void executeOnMoveEnds(BebopDrone mBebopDrone) {
        mBebopDrone.setPitch((byte) 0);
        mBebopDrone.setFlag((byte) 0);
    }
}
