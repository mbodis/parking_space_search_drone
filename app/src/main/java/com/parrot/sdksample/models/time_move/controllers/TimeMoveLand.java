package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.time_move.iface.TimeMoveIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class TimeMoveLand extends TimeMoveIface {

    public static final String TAG = TimeMoveLand.class.getName();

    public TimeMoveLand(){
        super(3000);
    }

    @Override
    public void executeOnMoveStarts(BebopDrone mBebopDrone) {
        mBebopDrone.land();
    }

    @Override
    public void executeOnMoveEnds(BebopDrone mBebopDrone) {
        // do nothing
    }
}
