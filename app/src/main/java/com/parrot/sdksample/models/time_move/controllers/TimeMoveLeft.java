package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.time_move.iface.TimeMoveIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class TimeMoveLeft extends TimeMoveIface{

    public static final String TAG = TimeMoveLeft.class.getName();

    public TimeMoveLeft(int speed, int durationMilis) {
        super(DIRECTION_LEFT, speed, durationMilis);
    }

    public TimeMoveLeft(int durationMilis) {
        super(DIRECTION_LEFT, SPEED_NORMAL, durationMilis);
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
