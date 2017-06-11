package com.parrot.sdksample.models.time_move.iface;

import com.parrot.sdksample.drone.BebopDrone;

/**
 * Created by mbodis on 6/3/17.
 */

public abstract class MoveActionIface extends DroneActionIface {

    public static final int DIRECTION_UNDEFINED = -1;
    public static final int DIRECTION_UP = 0;
    public static final int DIRECTION_DOWN = 1;
    public static final int DIRECTION_FORWARD = 2;
    public static final int DIRECTION_BACKWARD = 3;
    public static final int DIRECTION_LEFT = 4;
    public static final int DIRECTION_RIGHT = 5;
    public static final int DIRECTION_ROTATE_LEFT = 6;
    public static final int DIRECTION_ROTATE_RIGHT = 7;

    public static final int SPEED_UNDEFINED = 0;
    public static final int SPEED_SLOW = 5;
    public static final int SPEED_NORMAL = 10;
    public static final int SPEED_FAST = 15;
    public static final int SPEED_EXTRA_FAST = 30;

    protected long durationMilis = 0;

    protected int direction = DIRECTION_UNDEFINED;
    protected int speed = SPEED_UNDEFINED;

    public MoveActionIface(String actionName, int direction, int speed, int durationMilis){
        setActionName(actionName);
        this.direction = direction;
        this.speed = speed;
        this.durationMilis = durationMilis;
    }

    public MoveActionIface(int durationMilis){
        this.durationMilis = durationMilis;
    }

    public abstract void executeOnMoveStarts(BebopDrone mBebopDrone);
    public abstract void executeOnMoveEnds(BebopDrone mBebopDrone);

    public boolean hasTimeLimitFinished(){
        if (startTimeMilis == 0) return false;

        return (System.currentTimeMillis() > (startTimeMilis+durationMilis));
    }

}
