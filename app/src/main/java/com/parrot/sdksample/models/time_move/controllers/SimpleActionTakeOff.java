package com.parrot.sdksample.models.time_move.controllers;

import android.util.Log;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.LandOnQrCode;
import com.parrot.sdksample.models.time_move.iface.SimpleActionIface;

import static com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED;

/**
 * Created by mbodis on 6/4/17.
 */

public class SimpleActionTakeOff extends SimpleActionIface{

    public static final String TAG = SimpleActionTakeOff.class.getName();

    public SimpleActionTakeOff() {
        super(3000);
    }

    @Override
    public void executeAction(BebopDrone mBebopDrone) {
        if (mBebopDrone.getFlyingState() == ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED) {
            mBebopDrone.takeOff();
        }else{
            Log.d(TAG, "invalid flight state, cannot takeOff");
        }
    }
}
