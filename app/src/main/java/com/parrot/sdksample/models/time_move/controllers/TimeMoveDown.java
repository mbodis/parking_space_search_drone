package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.time_move.iface.TimeMoveIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class TimeMoveDown extends TimeMoveIface {

    public static final String TAG = TimeMoveDown.class.getName();

    public TimeMoveDown(int speed, int durationMilis) {
        super(DIRECTION_DOWN, speed, durationMilis);
    }

    public TimeMoveDown(int durationMilis) {
        super(DIRECTION_DOWN, SPEED_NORMAL, durationMilis);
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
