package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.LandOnQrCode;
import com.parrot.sdksample.models.time_move.iface.SimpleActionIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class SimpleActionCameraView extends SimpleActionIface{

    public static final String TAG = SimpleActionCameraView.class.getName();

    public static final int VIEW_FORWARD = 0;
    public static final int VIEW_DOWN = 1;

    int viewMode = VIEW_FORWARD;

    public SimpleActionCameraView(int viewMode) {
        super(250);
        this.viewMode = viewMode;
    }

    @Override
    public void executeAction(BebopDrone mBebopDrone, LandOnQrCode mLandOnQrCode) {
        switch (viewMode){
            case VIEW_DOWN:
                mBebopDrone.setCameraOrientationV2((byte) -100, (byte) 0);
                break;

            case VIEW_FORWARD:
                mBebopDrone.setCameraOrientationV2((byte) 0, (byte) 0);
                break;
        }
    }
}
