package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.FlyAboveQrCode;
import com.parrot.sdksample.models.time_move.DroneActionsQueue;
import com.parrot.sdksample.models.time_move.iface.ConditionActionIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class ConditionActionLandQrCode extends ConditionActionIface {

    public ConditionActionLandQrCode(long durationMilis) {
        super("LandQrCode", durationMilis);
    }

    @Override
    public void executeOnMoveStarts(BebopDrone mBebopDrone, FlyAboveQrCode mFlyAboveQrCode) {
        // launch landing procedure
        mFlyAboveQrCode.setLockToQrCodeEnabled();
    }

    @Override
    public void executeOnMoveProcess(BebopDrone mBebopDrone, FlyAboveQrCode mFlyAboveQrCode) {
        // do nothing - lock to qr code in progress
    }

    @Override
    public void executeOnMoveEnds(BebopDrone mBebopDrone, FlyAboveQrCode mFlyAboveQrCode, DroneActionsQueue droneActionsQueue) {
        // land drone
        mBebopDrone.land();
        // set drone has landed
        mFlyAboveQrCode.setDroneHasLanded();
    }

    @Override
    public boolean isConditionSatisfied(BebopDrone mBebopDrone, FlyAboveQrCode mFlyAboveQrCode) {
        return mFlyAboveQrCode.mLandController.isLandingConditionsSatisfied();
    }
}
