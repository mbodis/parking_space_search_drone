package com.parrot.sdksample.models.time_move.iface;

import com.parrot.sdksample.drone.BebopDrone;

/**
 * Created by mbodis on 6/8/17.
 */

public abstract class SimpleActionIface extends DroneActionIface {

    protected long durationMilis = 0;

    public SimpleActionIface(String actionName, long durationMilis){
        setActionName(actionName);
        this.durationMilis = durationMilis;
    }

    public abstract void executeAction(BebopDrone mBebopDrone);

    public boolean hasTimeLimitFinished(){
        if (startTimeMilis == 0) return false;

        return (System.currentTimeMillis() > (startTimeMilis+durationMilis));
    }
}