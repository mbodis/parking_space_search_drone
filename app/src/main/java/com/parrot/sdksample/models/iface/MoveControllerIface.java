package com.parrot.sdksample.models.iface;

import android.content.Context;

import com.parrot.sdksample.drone.BebopDrone;

/**
 * Created by mbodis on 5/15/17.
 */

public abstract class MoveControllerIface {

    public abstract void executeMove(Context ctx, BebopDrone mBebopDrone);
    public abstract void endOfMove();
}
