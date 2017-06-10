package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.LandOnQrCode;
import com.parrot.sdksample.models.time_move.iface.ConditionActionIface;
import com.parrot.sdksample.models.time_move.iface.SimpleActionIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class SimpleActionTakePicture extends SimpleActionIface{


    public SimpleActionTakePicture() {
        super(250);
    }

    @Override
    public void executeAction(BebopDrone mBebopDrone) {
        mBebopDrone.takePicture();
    }
}
