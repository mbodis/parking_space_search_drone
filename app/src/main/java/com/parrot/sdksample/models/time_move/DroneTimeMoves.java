package com.parrot.sdksample.models.time_move;

import android.content.Context;
import android.os.SystemClock;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.LandOnQrCode;
import com.parrot.sdksample.models.time_move.iface.ConditionMoveIface;
import com.parrot.sdksample.models.time_move.iface.DroneMoveIface;
import com.parrot.sdksample.models.time_move.iface.TimeMoveIface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mbodis on 6/3/17.
 */

public class DroneTimeMoves {

    public static final String TAG = DroneTimeMoves.class.getName();

    boolean started = false;
    boolean isLogicThreadAlive = true;
    Thread logicThread;
    private List<DroneMoveIface> moves = new ArrayList<DroneMoveIface>();

    public DroneTimeMoves(final Context ctx, final BebopDrone mBebopDrone, final LandOnQrCode mLandOnQrCode,
                          List<DroneMoveIface> newMoves) {
        this.moves = newMoves;
        logicThread = new Thread(new Runnable() {

            @Override
            public void run() {
                SystemClock.sleep(1000);//initial sleep
                while (isLogicThreadAlive) {

                    for (DroneMoveIface move : moves) {

                        // time move
                        if (executeTimeMove(move, mBebopDrone)) {
                            break;
                        }

                        // condition move
                        if (executeConditionMove(move, mBebopDrone, mLandOnQrCode)) {
                            break;
                        }
                    }

                    if (hasAllMovesEnded()) {
                        isLogicThreadAlive = false;
                    }

                    SystemClock.sleep(50);
                }
            }
        });
    }

    public void start(){
        logicThread.start();
        started = true;
    }

    public boolean isInProgress(){
        return isLogicThreadAlive && started;
    }

    public void stop(BebopDrone mBebopDrone, LandOnQrCode mLandOnQrCode){
        isLogicThreadAlive = false;
        for (DroneMoveIface move : moves) {
            if (move instanceof TimeMoveIface) {
                TimeMoveIface mTimeMove = (TimeMoveIface) move;
                mTimeMove.executeOnMoveEnds(mBebopDrone);
            }

            if (move instanceof ConditionMoveIface) {
                ConditionMoveIface mConditionMove = (ConditionMoveIface) move;
                mConditionMove.executeOnMoveEnds(mBebopDrone, mLandOnQrCode);
            }
        }
        moves = new ArrayList<DroneMoveIface>();
    }

    private boolean executeTimeMove(DroneMoveIface move, BebopDrone mBebopDrone) {
        if (move instanceof TimeMoveIface) {
            TimeMoveIface mTimeMove = (TimeMoveIface) move;
            if (!mTimeMove.isMoveFinished()) {
                // start
                if (!mTimeMove.isMoveStarted()) {
                    mTimeMove.executeOnMoveStarts(mBebopDrone);
                    mTimeMove.setMoveAsStarted(true);
                }

                // continue in execution
                if (mTimeMove.isMoveStarted()) {
                    // nothing - time moves has only start/end execution
                }

                // has finished
                if (mTimeMove.hasTimeLimitFinished()) {
                    mTimeMove.executeOnMoveEnds(mBebopDrone);
                    mTimeMove.setMoveFinished(true);
                }

                // run only first non-finished
                return true;
            }
        }

        return false;
    }

    private boolean executeConditionMove(DroneMoveIface move, BebopDrone mBebopDrone, LandOnQrCode mLandOnQrCode) {
        if (move instanceof ConditionMoveIface) {
            ConditionMoveIface mConditionMove = (ConditionMoveIface) move;
            if (!mConditionMove.isMoveFinished()) {
                // start
                if (!mConditionMove.isMoveStarted()) {
                    mConditionMove.executeOnMoveStarts(mBebopDrone, mLandOnQrCode);
                    mConditionMove.setMoveAsStarted(true);
                }

                // continue in execution
                if (mConditionMove.isMoveStarted()) {
                    mConditionMove.executeOnMoveProcess(mBebopDrone, mLandOnQrCode);
                }

                // has finished
                if (mConditionMove.isConditionSatisfied(mLandOnQrCode)) {
                    mConditionMove.executeOnMoveEnds(mBebopDrone, mLandOnQrCode);
                    mConditionMove.setMoveFinished(true);
                }

                // run only first non-finished
                return true;
            }

        }
        return false;
    }

    private boolean hasAllMovesEnded() {

        for (DroneMoveIface move : moves) {
            if (!move.isMoveFinished()) {
                return false;
            }
        }

        return true;
    }


}
