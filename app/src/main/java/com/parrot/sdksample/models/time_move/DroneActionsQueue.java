package com.parrot.sdksample.models.time_move;

import android.content.Context;
import android.os.SystemClock;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.LandOnQrCode;
import com.parrot.sdksample.models.time_move.iface.ConditionActionIface;
import com.parrot.sdksample.models.time_move.iface.DroneActionIface;
import com.parrot.sdksample.models.time_move.iface.MoveActionIface;
import com.parrot.sdksample.models.time_move.iface.SimpleActionIface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mbodis on 6/3/17.
 */

public class DroneActionsQueue {

    public static final String TAG = DroneActionsQueue.class.getName();

    boolean started = false;
    boolean isLogicThreadAlive = true;
    Thread logicThread;
    private List<DroneActionIface> moves = new ArrayList<DroneActionIface>();

    public DroneActionsQueue(final Context ctx, final BebopDrone mBebopDrone, final LandOnQrCode mLandOnQrCode,
                             List<DroneActionIface> newMoves) {
        this.moves = newMoves;
        logicThread = new Thread(new Runnable() {

            @Override
            public void run() {
                SystemClock.sleep(1000);//initial sleep
                while (isLogicThreadAlive) {

                    for (DroneActionIface move : moves) {

                        // simple action
                        if (executeSingleAction(move, mBebopDrone)) {
                            break;
                        }

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
        for (DroneActionIface move : moves) {
            if (move instanceof MoveActionIface) {
                MoveActionIface mTimeMove = (MoveActionIface) move;
                mTimeMove.executeOnMoveEnds(mBebopDrone);
            }

            if (move instanceof ConditionActionIface) {
                ConditionActionIface mConditionMove = (ConditionActionIface) move;
                mConditionMove.executeOnMoveEnds(mBebopDrone, mLandOnQrCode);
            }
        }
        moves = new ArrayList<DroneActionIface>();
    }

    private boolean executeSingleAction(DroneActionIface move, BebopDrone mBebopDrone){
        if (move instanceof SimpleActionIface) {
            SimpleActionIface mSimpleActionIface = (SimpleActionIface) move;
            if (!mSimpleActionIface.isActionFinished()) {
                // start + execute
                if (!mSimpleActionIface.isActionStarted()) {
                    mSimpleActionIface.executeAction(mBebopDrone);
                    mSimpleActionIface.setMoveAsStarted(true);
                }

                // has finished
                if (mSimpleActionIface.hasTimeLimitFinished()) {
                    mSimpleActionIface.setActionFinished(true);
                }

                // run only first non-finished
                return true;
            }
        }

        return false;
    }

    private boolean executeTimeMove(DroneActionIface move, BebopDrone mBebopDrone) {
        if (move instanceof MoveActionIface) {
            MoveActionIface mMoveActionIface = (MoveActionIface) move;
            if (!mMoveActionIface.isActionFinished()) {
                // start
                if (!mMoveActionIface.isActionStarted()) {
                    mMoveActionIface.executeOnMoveStarts(mBebopDrone);
                    mMoveActionIface.setMoveAsStarted(true);
                }

                // continue in execution
                if (mMoveActionIface.isActionStarted()) {
                    // nothing - time moves has only start/end execution
                }

                // has finished
                if (mMoveActionIface.hasTimeLimitFinished()) {
                    mMoveActionIface.executeOnMoveEnds(mBebopDrone);
                    mMoveActionIface.setActionFinished(true);
                }

                // run only first non-finished
                return true;
            }
        }

        return false;
    }

    private boolean executeConditionMove(DroneActionIface move, BebopDrone mBebopDrone, LandOnQrCode mLandOnQrCode) {
        if (move instanceof ConditionActionIface) {
            ConditionActionIface mConditionActionIface = (ConditionActionIface) move;
            if (!mConditionActionIface.isActionFinished()) {
                // start
                if (!mConditionActionIface.isActionStarted()) {
                    mConditionActionIface.executeOnMoveStarts(mBebopDrone, mLandOnQrCode);
                    mConditionActionIface.setMoveAsStarted(true);
                }

                // continue in execution
                if (mConditionActionIface.isActionStarted()) {
                    mConditionActionIface.executeOnMoveProcess(mBebopDrone, mLandOnQrCode);
                }

                // has finished
                if (mConditionActionIface.isConditionSatisfied(mLandOnQrCode)) {
                    mConditionActionIface.executeOnMoveEnds(mBebopDrone, mLandOnQrCode);
                    mConditionActionIface.setActionFinished(true);
                }

                // run only first non-finished
                return true;
            }

        }
        return false;
    }

    private boolean hasAllMovesEnded() {

        for (DroneActionIface move : moves) {
            if (!move.isActionFinished()) {
                return false;
            }
        }

        return true;
    }


}
