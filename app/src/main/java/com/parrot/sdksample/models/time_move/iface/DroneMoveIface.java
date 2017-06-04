package com.parrot.sdksample.models.time_move.iface;

/**
 * Created by mbodis on 6/4/17.
 */

public abstract class DroneMoveIface {

    protected long startTimeMilis = 0;

    private boolean moveStarted = false;
    private boolean moveFinished = false;

    public boolean isMoveStarted(){
        return moveStarted;
    }

    public boolean isMoveFinished(){
        return moveFinished;
    }

    public void setMoveAsStarted(boolean newState){
        moveStarted = newState;
        if (newState)
            startTimeMilis = System.currentTimeMillis();
    }

    public void setMoveFinished(boolean newState){
        moveFinished = newState;
    }
}
