package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.time_move.iface.TimeMoveIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class TimeMoveCameraView extends TimeMoveIface {

    public static final String TAG = TimeMoveCameraView.class.getName();

    public static final int VIEW_FORWARD = 0;
    public static final int VIEW_DOWN = 1;

    int viewMode = VIEW_FORWARD;

    public TimeMoveCameraView(int viewType) {
        super(250);
    }

    @Override
    public void executeOnMoveStarts(BebopDrone mBebopDrone) {
        switch (viewMode){
            case VIEW_DOWN:
                mBebopDrone.setCameraOrientationV2((byte) -100, (byte) 0);
                break;

            case VIEW_FORWARD:
                mBebopDrone.setCameraOrientationV2((byte) 0, (byte) 0);
                break;
        }
    }

    @Override
    public void executeOnMoveEnds(BebopDrone mBebopDrone) {
        // do nothing
    }
}
