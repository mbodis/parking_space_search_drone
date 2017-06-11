package com.parrot.sdksample.models.time_move;

import android.content.Context;
import android.os.SystemClock;

import com.parrot.sdksample.activity.BebopActivity;
import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.FlyAboveQrCode;
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
    private int lastQrCodeId = 0;

    public DroneActionsQueue(final Context ctx, final BebopDrone mBebopDrone, final FlyAboveQrCode mFlyAboveQrCode,
                             List<DroneActionIface> newMoves) {
        this.moves = newMoves;
        logicThread = new Thread(new Runnable() {

            @Override
            public void run() {
                SystemClock.sleep(1000);//initial sleep
                while (isLogicThreadAlive) {

                    for (DroneActionIface move : moves) {

                        // simple action
                        if (executeSingleAction(ctx, move, mBebopDrone)) {
                            break;
                        }

                        // time move
                        if (executeTimeMove(ctx, move, mBebopDrone)) {
                            break;
                        }

                        // condition move
                        if (executeConditionMove(ctx, move, mBebopDrone, mFlyAboveQrCode)) {
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
        lastQrCodeId = 0;
    }

    public void addNewMove(DroneActionIface newMove){
        moves.add(newMove);
    }

    public boolean isInProgress(){
        return isLogicThreadAlive && started;
    }

    public void stop(BebopDrone mBebopDrone, FlyAboveQrCode mFlyAboveQrCode){
        isLogicThreadAlive = false;
        for (DroneActionIface move : moves) {
            if (move instanceof MoveActionIface) {
                MoveActionIface mTimeMove = (MoveActionIface) move;
                mTimeMove.executeOnMoveEnds(mBebopDrone);
            }

            if (move instanceof ConditionActionIface) {
                ConditionActionIface mConditionMove = (ConditionActionIface) move;
                mConditionMove.executeOnMoveEnds(mBebopDrone, mFlyAboveQrCode, this);
            }
        }
        moves = new ArrayList<DroneActionIface>();
    }

    private boolean executeSingleAction(Context ctx, DroneActionIface move, BebopDrone mBebopDrone){
        if (move instanceof SimpleActionIface) {
            SimpleActionIface mSimpleActionIface = (SimpleActionIface) move;
            if (!mSimpleActionIface.isActionFinished()) {
                // start + execute
                if (!mSimpleActionIface.isActionStarted()) {
                    BebopActivity.addTextLogIntent(ctx, "start action - " + move.getActionName());
                    mSimpleActionIface.executeAction(mBebopDrone);
                    mSimpleActionIface.setMoveAsStarted(true);
                }

                // has finished
                if (mSimpleActionIface.hasTimeLimitFinished()) {
                    BebopActivity.addTextLogIntent(ctx, "finish action - " + move.getActionName());
                    mSimpleActionIface.setActionFinished(true);
                }

                // run only first non-finished
                return true;
            }
        }

        return false;
    }

    private boolean executeTimeMove(Context ctx, DroneActionIface move, BebopDrone mBebopDrone) {
        if (move instanceof MoveActionIface) {
            MoveActionIface mMoveActionIface = (MoveActionIface) move;
            if (!mMoveActionIface.isActionFinished()) {
                // start
                if (!mMoveActionIface.isActionStarted()) {
                    BebopActivity.addTextLogIntent(ctx, "start move - " + move.getActionName());
                    mMoveActionIface.executeOnMoveStarts(mBebopDrone);
                    mMoveActionIface.setMoveAsStarted(true);
                }

                // continue in execution
                if (mMoveActionIface.isActionStarted()) {
                    // nothing - time moves has only start/end execution
                }

                // has finished
                if (mMoveActionIface.hasTimeLimitFinished()) {
                    BebopActivity.addTextLogIntent(ctx, "finish move - " + move.getActionName());
                    mMoveActionIface.executeOnMoveEnds(mBebopDrone);
                    mMoveActionIface.setActionFinished(true);
                }

                // run only first non-finished
                return true;
            }
        }

        return false;
    }

    private boolean executeConditionMove(Context ctx, DroneActionIface move, BebopDrone mBebopDrone, FlyAboveQrCode mFlyAboveQrCode) {
        if (move instanceof ConditionActionIface) {
            ConditionActionIface mConditionActionIface = (ConditionActionIface) move;
            if (!mConditionActionIface.isActionFinished()) {
                // start
                if (!mConditionActionIface.isActionStarted()) {
                    BebopActivity.addTextLogIntent(ctx, "start condition - " + move.getActionName());
                    mConditionActionIface.executeOnMoveStarts(mBebopDrone, mFlyAboveQrCode);
                    mConditionActionIface.setMoveAsStarted(true);
                }

                // continue in execution
                if (mConditionActionIface.isActionStarted()) {
                    mConditionActionIface.executeOnMoveProcess(mBebopDrone, mFlyAboveQrCode);
                }

                // has finished
                if (mConditionActionIface.isConditionSatisfied(mBebopDrone, mFlyAboveQrCode)) {
                    BebopActivity.addTextLogIntent(ctx, "finish condition - " + move.getActionName());
                    mConditionActionIface.executeOnMoveEnds(mBebopDrone, mFlyAboveQrCode, this);
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


    public int getLastQrCodeId() {
        return lastQrCodeId;
    }

    public void setLastQrCodeId(int lastQrCodeId) {
        this.lastQrCodeId = lastQrCodeId;
    }
}
