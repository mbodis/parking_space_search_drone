package com.parrot.sdksample.models.time_move.iface;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.LandOnQrCode;

/**
 * Created by mbodis on 6/4/17.
 */

public abstract class ConditionMoveIface extends DroneMoveIface{

    protected long durationMilis = 0;

    public ConditionMoveIface(long durationMilis){
        this.durationMilis = durationMilis;
    }

    public abstract void executeOnMoveStarts(BebopDrone mBebopDrone, LandOnQrCode mLandOnQrCode);
    public abstract void executeOnMoveProcess(BebopDrone mBebopDrone, LandOnQrCode mLandOnQrCode);
    public abstract void executeOnMoveEnds(BebopDrone mBebopDrone, LandOnQrCode mLandOnQrCode);

    public abstract boolean isConditionSatisfied(LandOnQrCode mLandOnQrCode);
}
