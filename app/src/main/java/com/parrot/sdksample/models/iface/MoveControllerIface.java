package com.parrot.sdksample.models.iface;

import android.content.Context;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code.LandingPatternQrCode;

/**
 * Created by mbodis on 5/15/17.
 */

public abstract class MoveControllerIface {

    public static final long TS_COMMON_MOVE = 500;
    public static final long TS_COMMON_PAUSE = 1000;

    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = 2;
    public static final int DIRECTION_FORWARD = 3;
    public static final int DIRECTION_BACKWARD = 4;
    public static final int DIRECTION_CLOCKWISE = 5;
    public static final int DIRECTION_COUNTER_CLOCKWISE = 6;
    public static final int DIRECTION_UP = 7;
    public static final int DIRECTION_DOWN = 8;

    protected Context ctx;
    protected BebopDrone mBebopDrone;
    protected LandingPatternQrCode mLandingPatternQrCode;

    public MoveControllerIface(Context ctx, BebopDrone mBebopDrone){
        this.ctx = ctx;
        this.mBebopDrone = mBebopDrone;
    }

    public void updateLandingPattern(LandingPatternQrCode mLandingPatternQrCode){
        this.mLandingPatternQrCode = mLandingPatternQrCode;
    }
    public double getError(){
        return -1;
    }
    public abstract void executeMove();
    public abstract void endOfMove();
    public abstract boolean satisfyLandCondition();

}
