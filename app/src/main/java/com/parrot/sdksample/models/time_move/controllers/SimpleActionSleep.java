package com.parrot.sdksample.models.time_move.controllers;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.time_move.iface.SimpleActionIface;

/**
 * Created by mbodis on 6/4/17.
 */

public class SimpleActionSleep extends SimpleActionIface{

    public static final String TAG = SimpleActionSleep.class.getName();

    public SimpleActionSleep(int duration) {
        super("Sleep", duration);
    }

    @Override
    public void executeAction(BebopDrone mBebopDrone) {
        // do nothing, just sleep
    }
}
