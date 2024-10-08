package com.parrot.sdksample.models.qr_code_landing.controllers;

import android.content.Context;

import com.parrot.sdksample.activity.BebopActivity;
import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.landing.Constants;
import com.parrot.sdksample.models.landing.iface.MoveControllerIface;

/**
 * Created by mbodis on 5/15/17.
 */

public class ForwardBackwardController extends MoveControllerIface {

    private static final boolean LOCAL_DEBUG = false;

    private static final long TS_FORWARD_BACKWARD_MOVE = TS_COMMON_MOVE;
    private static final long TS_FORWARD_BACKWARD_PAUSE = TS_COMMON_PAUSE;

    private static final byte SPEED_FORWARD_BACKWARD_SLOW = 5;
    private static final byte SPEED_FORWARD_BACKWARD_NORMAL = 10;
    private static final byte SPEED_FORWARD_BACKWARD_FAST = 15;

    private static final int FORWARD_BACKWARD_LIMIT_PERCENTAGE_TOP_FAR = 50;
    private static final int FORWARD_BACKWARD_LIMIT_PERCENTAGE_TOP = 70;
    private static final int FORWARD_BACKWARD_LIMIT_PERCENTAGE_CENTER = 80;
    private static final int FORWARD_BACKWARD_LIMIT_PERCENTAGE_BOTTOM = 90;
//    private static final int FORWARD_BACKWARD_LIMIT_PERCENTAGE_BOTTOM_FAR = 95;

    byte LIMIT_HEIGHT_ERROR_CENTER = 3; // percentage

    // controller forward/backward
    boolean forwardBackward = false;
    long forwardBackwardEndOfMoveTs = 0;
    long forwardBackwardEndOfPauseTs = 0;
    int forwardBackwardDirection = -1;

    public ForwardBackwardController(Context ctx, BebopDrone mBebopDrone) {
        super(ctx, mBebopDrone);
    }

    @Override
    public void executeMove() {

        if (mLandingPatternQrCode == null)
            return;
        if (!Constants.isQrActive(Constants.getLastTimeQrCodeDetected(mLandingPatternQrCode)))
            return;

        if (!forwardBackward) {

            double centerHeight = getError();

            if (Math.abs(centerHeight - FORWARD_BACKWARD_LIMIT_PERCENTAGE_CENTER) < LIMIT_HEIGHT_ERROR_CENTER) {
                return;
            }

            if (centerHeight < FORWARD_BACKWARD_LIMIT_PERCENTAGE_TOP_FAR) {
                if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move forward -> startFAR " + (int) centerHeight);
                mBebopDrone.setPitch((byte) SPEED_FORWARD_BACKWARD_FAST);
                mBebopDrone.setFlag((byte) 1);
                forwardBackward = true;
                forwardBackwardEndOfMoveTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE;
                forwardBackwardEndOfPauseTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE + TS_FORWARD_BACKWARD_PAUSE;
                forwardBackwardDirection = DIRECTION_FORWARD;

            } else if (centerHeight < FORWARD_BACKWARD_LIMIT_PERCENTAGE_TOP) {
                if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move forward -> start " + (int) centerHeight);
                mBebopDrone.setPitch((byte) SPEED_FORWARD_BACKWARD_NORMAL);
                mBebopDrone.setFlag((byte) 1);
                forwardBackward = true;
                forwardBackwardEndOfMoveTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE;
                forwardBackwardEndOfPauseTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE + TS_FORWARD_BACKWARD_PAUSE;
                forwardBackwardDirection = DIRECTION_FORWARD;

//            } else if (centerHeight < FORWARD_BACKWARD_LIMIT_PERCENTAGE_BOTTOM_FAR) {
//                BebopActivity.addTextLogIntent(ctx, "move backward -> startFAR " + (int) centerHeight);
//                mBebopDrone.setPitch((byte) -SPEED_FORWARD_BACKWARD_FAST);
//                mBebopDrone.setFlag((byte) 1);
//                forwardBackward = true;
//                forwardBackwardEndOfMoveTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE;
//                forwardBackwardEndOfPauseTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE + TS_FORWARD_BACKWARD_PAUSE;
//                forwardBackwardDirection = DIRECTION_FORWARD;

            } else if (centerHeight > FORWARD_BACKWARD_LIMIT_PERCENTAGE_BOTTOM) {
                if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move backward -> start " + (int) centerHeight);
                mBebopDrone.setPitch((byte) -SPEED_FORWARD_BACKWARD_NORMAL);
                mBebopDrone.setFlag((byte) 1);
                forwardBackward = true;
                forwardBackwardEndOfMoveTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE;
                forwardBackwardEndOfPauseTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE + TS_FORWARD_BACKWARD_PAUSE;
                forwardBackwardDirection = DIRECTION_BACKWARD;

            } else if (centerHeight < FORWARD_BACKWARD_LIMIT_PERCENTAGE_CENTER) {
                if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move forward -> start slow " + (int) centerHeight);
                mBebopDrone.setPitch((byte) SPEED_FORWARD_BACKWARD_SLOW);
                mBebopDrone.setFlag((byte) 1);
                forwardBackward = true;
                forwardBackwardEndOfMoveTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE;
                forwardBackwardEndOfPauseTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE + TS_FORWARD_BACKWARD_PAUSE;
                forwardBackwardDirection = DIRECTION_FORWARD;

            } else if (centerHeight > FORWARD_BACKWARD_LIMIT_PERCENTAGE_CENTER) {
                if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move backward -> start slow " + (int) centerHeight);
                mBebopDrone.setPitch((byte) -SPEED_FORWARD_BACKWARD_SLOW);
                mBebopDrone.setFlag((byte) 1);
                forwardBackward = true;
                forwardBackwardEndOfMoveTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE;
                forwardBackwardEndOfPauseTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE + TS_FORWARD_BACKWARD_PAUSE;
                forwardBackwardDirection = DIRECTION_BACKWARD;
            }
        }
    }

    @Override
    public void endOfMove() {
        if (forwardBackward) {
            if (forwardBackwardEndOfMoveTs > 0) {
                if (System.currentTimeMillis() > forwardBackwardEndOfMoveTs) {
                    forwardBackwardEndOfMoveTs = 0;
                    if (forwardBackwardDirection == DIRECTION_FORWARD) {
                        if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move forward << stop");
                    }
                    if (forwardBackwardDirection == DIRECTION_BACKWARD) {
                        if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move backward << stop");
                    }
                    mBebopDrone.setPitch((byte) 0);
                    mBebopDrone.setFlag((byte) 0);
                }
            }
            if (System.currentTimeMillis() > forwardBackwardEndOfPauseTs) {
                forwardBackwardEndOfPauseTs = 0;
                if (forwardBackwardDirection == DIRECTION_FORWARD) {
                    if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move forward << stop pause");
                }
                if (forwardBackwardDirection == DIRECTION_BACKWARD) {
                    if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move backward << stop pause");
                }
                forwardBackward = false;
            }
        }
    }

    @Override
    public void stopMoveImmediately() {
        if (forwardBackward) {
            mBebopDrone.setPitch((byte) 0);
            mBebopDrone.setFlag((byte) 0);
            forwardBackwardEndOfMoveTs = 0;
            forwardBackwardEndOfPauseTs = 0;
            forwardBackwardDirection = -1;
            forwardBackward = false;
            if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move forward/backward << stop immediately");
        }
    }

    @Override
    public double getError(){
        return (double) mLandingPatternQrCode.getCenter().y / Constants.VIDEO_HEIGHT * 100;
    }


    @Override
    public boolean satisfyLandCondition() {
        double centerHeight = getError();

        if (Math.abs(centerHeight - FORWARD_BACKWARD_LIMIT_PERCENTAGE_CENTER) < LIMIT_HEIGHT_ERROR_CENTER) {
            return true;
        }

        return false;
    }

}
