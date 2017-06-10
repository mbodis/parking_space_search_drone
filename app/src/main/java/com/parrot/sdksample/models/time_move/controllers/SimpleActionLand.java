package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.LandOnQrCode;
import com.parrot.sdksample.models.time_move.iface.MoveActionIface;
import com.parrot.sdksample.models.time_move.iface.SimpleActionIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class SimpleActionLand extends SimpleActionIface{

    public static final String TAG = SimpleActionLand.class.getName();

    public SimpleActionLand(){
        super(3000);
    }

    @Override
    public void executeAction(BebopDrone mBebopDrone) {
        mBebopDrone.land();
    }
}
