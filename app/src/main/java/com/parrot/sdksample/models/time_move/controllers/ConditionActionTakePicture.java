package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.LandOnQrCode;
import com.parrot.sdksample.models.time_move.iface.ConditionActionIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class ConditionActionTakePicture extends ConditionActionIface {


    public ConditionActionTakePicture() {
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
