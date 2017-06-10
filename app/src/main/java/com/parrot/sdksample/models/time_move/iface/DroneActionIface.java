package com.parrot.sdksample.models.time_move.iface;

import android.util.Log;

/**
 * Created by mbodis on 6/4/17.
 */

public abstract class DroneActionIface {

    private static final String TAG = DroneActionIface.class.getName();

    protected long startTimeMilis = 0;

    private boolean actionStarted = false;
    private boolean actionFinished = false;

    public boolean isActionStarted(){
        return actionStarted;
    }

    public boolean isActionFinished(){
        return actionFinished;
    }

    public void setMoveAsStarted(boolean newState){
        Log.d(TAG, "setMoveAsStarted: " + newState);

        actionStarted = newState;
        if (newState)
            startTimeMilis = System.currentTimeMillis();
    }

    public void setActionFinished(boolean newState){
        actionFinished = newState;
    }
}
