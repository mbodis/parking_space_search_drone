package com.parrot.sdksample.models.qr_code_landing.controllers;

import android.content.Context;

import com.parrot.sdksample.drone.BebopDrone;

import static com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING;
import static com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING;

/**
 * Created by mbodis on 5/15/17.
 */

public class LandController {

    private boolean landToQrCodeEnabled = false;
    private boolean hasLanded = false;

    private Context ctx;
    private BebopDrone mBebopDrone;

    public LandController(Context ctx, BebopDrone mBebopDrone){
        this.ctx = ctx;
        this.mBebopDrone = mBebopDrone;
    }

    public boolean isLandToQrCodeEnabled() {
        return landToQrCodeEnabled;
    }

    public void setLandToQrCodeEnabled(boolean landToQrCodeEnabled) {
        this.landToQrCodeEnabled = landToQrCodeEnabled;
    }

    public boolean isHasLanded() {
        return hasLanded;
    }

    public void setHasLanded(boolean hasLanded) {
        this.hasLanded = hasLanded;
    }

    public boolean isLandingEnabled(){
        return landToQrCodeEnabled && !hasLanded;
    }

    public void landToLandingPattern(boolean landWidth, boolean landHeight, boolean landRotation, boolean landVertical){

        // qr code ir right below drone
        if (landWidth && landHeight && landRotation && landVertical) {
            if (mBebopDrone.getFlyingState() == ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING
                    || mBebopDrone.getFlyingState() == ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING) {
                mBebopDrone.land();
                hasLanded = true;
            }
        }
    }
}
