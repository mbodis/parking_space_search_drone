package com.parrot.sdksample.models.time_move.controllers;

import android.util.Log;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.time_move.iface.TimeMoveIface;

import static com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED;

/**
 * Created by mbodis on 6/4/17.
 */

public class TimeMoveTakeOff extends TimeMoveIface {

    public static final String TAG = TimeMoveTakeOff.class.getName();

    public TimeMoveTakeOff() {
        super(3000);
    }

    @Override
    public void executeOnMoveStarts(BebopDrone mBebopDrone) {
        if (mBebopDrone.getFlyingState() == ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED) {
            mBebopDrone.takeOff();
        }else{
            Log.d(TAG, "invalid flight state, cannot takeOff");
        }
    }

    @Override
    public void executeOnMoveEnds(BebopDrone mBebopDrone) {
        // do nothing
    }
}
