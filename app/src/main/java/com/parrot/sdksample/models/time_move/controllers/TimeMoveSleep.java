package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.time_move.iface.TimeMoveIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class TimeMoveSleep extends TimeMoveIface {

    public static final String TAG = TimeMoveSleep.class.getName();

    public TimeMoveSleep(int duration) {
        super(duration);
    }

    @Override
    public void executeOnMoveStarts(BebopDrone mBebopDrone) {
        // do nothing, just sleep
    }

    @Override
    public void executeOnMoveEnds(BebopDrone mBebopDrone) {
        // do nothing, just sleep
    }
}
