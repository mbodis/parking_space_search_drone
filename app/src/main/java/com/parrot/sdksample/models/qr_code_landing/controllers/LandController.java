package com.parrot.sdksample.models.qr_code_landing.controllers;

import android.content.Context;

import com.parrot.sdksample.drone.BebopDrone;

import static com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING;
import static com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING;

/**
 * Created by mbodis on 5/15/17.
 */

public class LandController {

    private boolean lockToQrCodeEnabled = false;
    private boolean landingConditionsSatisfied = false;

    private Context ctx;
    private BebopDrone mBebopDrone;

    public LandController(Context ctx, BebopDrone mBebopDrone){
        this.ctx = ctx;
        this.mBebopDrone = mBebopDrone;
    }

    public boolean isLockToQrCodeEnabled() {
        return lockToQrCodeEnabled;
    }

    public void setLockToQrCodeEnabled(boolean lockToQrCodeEnabled) {
        this.lockToQrCodeEnabled = lockToQrCodeEnabled;
    }

    public boolean isLandingEnabled(){
        return lockToQrCodeEnabled && !landingConditionsSatisfied;
    }

    public void landToLandingPattern(boolean landWidth, boolean landHeight, boolean landRotation, boolean landVertical){

        // qr code ir right below drone
        if (landWidth && landHeight && landRotation && landVertical) {
            if (mBebopDrone.getFlyingState() == ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING
                    || mBebopDrone.getFlyingState() == ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING) {
                mBebopDrone.land();
                setLandingConditionsSatisfied(true);
            }
        }
    }

    public boolean isLandingConditionsSatisfied() {
        return landingConditionsSatisfied;
    }

    public void setLandingConditionsSatisfied(boolean landingConditionsSatisfied) {
        this.landingConditionsSatisfied = landingConditionsSatisfied;
    }
}
