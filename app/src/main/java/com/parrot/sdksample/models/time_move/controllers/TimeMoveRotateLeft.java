package com.parrot.sdksample.models.time_move.controllers;

import android.util.Log;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.time_move.iface.TimeMoveIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class TimeMoveRotateLeft extends TimeMoveIface{

    public static final String TAG = TimeMoveRotateLeft.class.getName();

    public TimeMoveRotateLeft(int speed, int durationMilis) {
        super(TimeMoveIface.DIRECTION_ROTATE_LEFT, speed, durationMilis);
    }

    public TimeMoveRotateLeft(int durationMilis) {
        super(TimeMoveIface.DIRECTION_ROTATE_LEFT, TimeMoveIface.SPEED_NORMAL, durationMilis);
    }

    @Override
    public void executeOnMoveStarts(BebopDrone mBebopDrone) {
        mBebopDrone.setYaw((byte) -speed);
    }

    @Override
    public void executeOnMoveEnds(BebopDrone mBebopDrone) {
        mBebopDrone.setYaw((byte) 0);
    }
}
