package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.LandOnQrCode;
import com.parrot.sdksample.models.time_move.iface.ConditionMoveIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class ConditionMoveLandQrCode extends ConditionMoveIface {

    public ConditionMoveLandQrCode(long durationMilis) {
        super(durationMilis);
    }

    @Override
    public void executeOnMoveStarts(BebopDrone mBebopDrone, LandOnQrCode mLandOnQrCode) {
        // launch landing procedure
        mLandOnQrCode.setLandingToQrCodeEnabled(true);
    }

    @Override
    public void executeOnMoveProcess(BebopDrone mBebopDrone, LandOnQrCode mLandOnQrCode) {
        // do nothing - landing in progress
    }

    @Override
    public void executeOnMoveEnds(BebopDrone mBebopDrone, LandOnQrCode mLandOnQrCode) {
        // launch landing procedure
        mLandOnQrCode.setLandingToQrCodeEnabled(true);
    }

    @Override
    public boolean isConditionSatisfied(LandOnQrCode mLandOnQrCode) {
        return mLandOnQrCode.mLandController.isHasLanded();
    }
}
