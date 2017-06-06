package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.LandOnQrCode;
import com.parrot.sdksample.models.time_move.iface.ConditionMoveIface;
import com.parrot.sdksample.models.time_move.iface.DroneMoveIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class ConditionMoveTakePicture extends ConditionMoveIface {


    public ConditionMoveTakePicture() {
        super(250);
    }

    @Override
    public void executeOnMoveStarts(BebopDrone mBebopDrone, LandOnQrCode mLandOnQrCode) {
        mBebopDrone.takePicture();
    }

    @Override
    public void executeOnMoveProcess(BebopDrone mBebopDrone, LandOnQrCode mLandOnQrCode) {
        // do nothing
    }

    @Override
    public void executeOnMoveEnds(BebopDrone mBebopDrone, LandOnQrCode mLandOnQrCode) {
        //TODO retrieve image

    }

    @Override
    public boolean isConditionSatisfied(LandOnQrCode mLandOnQrCode) {
        return false;
    }
}
