package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.LandOnQrCode;
import com.parrot.sdksample.models.time_move.iface.SimpleActionIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class SimpleActionMoveSleep extends SimpleActionIface{

    public static final String TAG = SimpleActionMoveSleep.class.getName();

    public SimpleActionMoveSleep(int duration) {
        super(duration);
    }

    @Override
    public void executeAction(BebopDrone mBebopDrone, LandOnQrCode mLandOnQrCode) {
        // do nothing, just sleep
    }
}
