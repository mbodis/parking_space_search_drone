package com.parrot.sdksample.models.time_move.iface;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.FlyAboveQrCode;
import com.parrot.sdksample.models.time_move.DroneActionsQueue;

/**
 * Created by mbodis on 6/4/17.
 */

public abstract class ConditionActionIface extends DroneActionIface {

    protected long durationMilis = 0;

    public ConditionActionIface(String actionName, long durationMilis){
        setActionName(actionName);
        this.durationMilis = durationMilis;
    }

    public abstract void executeOnMoveStarts(BebopDrone mBebopDrone, FlyAboveQrCode mFlyAboveQrCode);
    public abstract void executeOnMoveProcess(BebopDrone mBebopDrone, FlyAboveQrCode mFlyAboveQrCode);
    public abstract void executeOnMoveEnds(BebopDrone mBebopDrone, FlyAboveQrCode mFlyAboveQrCode, DroneActionsQueue mDroneActionsQueue);

    public abstract boolean isConditionSatisfied(BebopDrone mBebopDrone, FlyAboveQrCode mFlyAboveQrCode);
}
