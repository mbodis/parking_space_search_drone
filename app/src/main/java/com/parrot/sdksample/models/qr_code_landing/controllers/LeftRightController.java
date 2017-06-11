package com.parrot.sdksample.models.qr_code_landing.controllers;

import android.content.Context;

import com.parrot.sdksample.activity.BebopActivity;
import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.landing.Constants;
import com.parrot.sdksample.models.landing.iface.MoveControllerIface;

/**
 * Created by mbodis on 5/15/17.
 */

public class LeftRightController extends MoveControllerIface {

    private static final boolean LOCAL_DEBUG = false;

    private static final long TS_LEFT_RIGHT_MOVE = TS_COMMON_MOVE;
    private static final long TS_LEFT_RIGHT_PAUSE = TS_COMMON_PAUSE;

    private static final byte SPEED_LEFT_RIGHT = 10;
    private static final byte SPEED_LEFT_RIGHT_SLOW = 10;
    private static final byte SPEED_LEFT_RIGHT_FAST = 15;

    private static final int LEFT_RIGHT_LIMIT_PERCENTAGE_TOP_FAR = 30;
    private static final int LEFT_RIGHT_LIMIT_PERCENTAGE_TOP = 40;
    private static final int LEFT_RIGHT_LIMIT_PERCENTAGE_CENTER = 50;
    private static final int LEFT_RIGHT_LIMIT_PERCENTAGE_BOTTOM = 60;
    private static final int LEFT_RIGHT_LIMIT_PERCENTAGE_BOTTOM_FAR = 70;

    byte LIMIT_WIDTH_ERROR_CENTER = 3; // percentage

    // controller left/right
    boolean leftRight = false;
    long leftRightEndMoveTs = 0;
    long leftRightEndPauseTs = 0;
    int leftRightDirection = -1;


    public LeftRightController(Context ctx, BebopDrone mBebopDrone) {
        super(ctx, mBebopDrone);
    }

    @Override
    public void executeMove() {

        if (mLandingPatternQrCode == null)
            return;
        if (!Constants.isQrActive(Constants.getLastTimeQrCodeDetected(mLandingPatternQrCode)))
            return;

        if (!leftRight ) {
            double centerWidth = (double) this.mLandingPatternQrCode.getCenter().x / Constants.VIDEO_WIDTH * 100;


            if (Math.abs(centerWidth - LEFT_RIGHT_LIMIT_PERCENTAGE_CENTER) < LIMIT_WIDTH_ERROR_CENTER) {
                return;
            }

            if (centerWidth < LEFT_RIGHT_LIMIT_PERCENTAGE_TOP_FAR) {
                if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move left -> startFAR " + (int) centerWidth);
                mBebopDrone.setRoll((byte) -SPEED_LEFT_RIGHT_FAST);
                mBebopDrone.setFlag((byte) 1);
                leftRight = true;
                leftRightEndMoveTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE;
                leftRightEndPauseTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE + TS_LEFT_RIGHT_PAUSE;
                leftRightDirection = DIRECTION_LEFT;

            } else if (centerWidth < LEFT_RIGHT_LIMIT_PERCENTAGE_TOP) {
                if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move left -> start " + (int) centerWidth);
                mBebopDrone.setRoll((byte) -SPEED_LEFT_RIGHT);
                mBebopDrone.setFlag((byte) 1);
                leftRight = true;
                leftRightEndMoveTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE;
                leftRightEndPauseTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE + TS_LEFT_RIGHT_PAUSE;
                leftRightDirection = DIRECTION_LEFT;

            } else if (centerWidth > LEFT_RIGHT_LIMIT_PERCENTAGE_BOTTOM_FAR) {
                if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move right -> startFAR " + (int) centerWidth);
                mBebopDrone.setRoll((byte) SPEED_LEFT_RIGHT_FAST);
                mBebopDrone.setFlag((byte) 1);
                leftRight = true;
                leftRightEndMoveTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE;
                leftRightEndPauseTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE + TS_LEFT_RIGHT_PAUSE;
                leftRightDirection = DIRECTION_RIGHT;

            } else if (centerWidth > LEFT_RIGHT_LIMIT_PERCENTAGE_BOTTOM) {
                if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move right -> start " + (int) centerWidth);
                mBebopDrone.setRoll((byte) SPEED_LEFT_RIGHT);
                mBebopDrone.setFlag((byte) 1);
                leftRight = true;
                leftRightEndMoveTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE;
                leftRightEndPauseTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE + TS_LEFT_RIGHT_PAUSE;
                leftRightDirection = DIRECTION_RIGHT;

            } else if (centerWidth > LEFT_RIGHT_LIMIT_PERCENTAGE_CENTER) {
                if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move right -> start slow " + (int) centerWidth);
                mBebopDrone.setRoll((byte) SPEED_LEFT_RIGHT_SLOW);
                mBebopDrone.setFlag((byte) 1);
                leftRight = true;
                leftRightEndMoveTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE;
                leftRightEndPauseTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE + TS_LEFT_RIGHT_PAUSE;
                leftRightDirection = DIRECTION_RIGHT;


            } else if (centerWidth < LEFT_RIGHT_LIMIT_PERCENTAGE_CENTER) {
                if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move left -> start slow " + (int) centerWidth);
                mBebopDrone.setRoll((byte) -SPEED_LEFT_RIGHT_SLOW);
                mBebopDrone.setFlag((byte) 1);
                leftRight = true;
                leftRightEndMoveTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE;
                leftRightEndPauseTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE + TS_LEFT_RIGHT_PAUSE;
                leftRightDirection = DIRECTION_LEFT;

            }

        }
    }

    @Override
    public  double getError(){
        return (double) this.mLandingPatternQrCode.getCenter().x / Constants.VIDEO_WIDTH * 100;
    }

    @Override
    public void endOfMove() {
        if (leftRight) {
            if (leftRightEndMoveTs > 0) {
                if (System.currentTimeMillis() > leftRightEndMoveTs) {
                    leftRightEndMoveTs = 0;
                    if (leftRightDirection == DIRECTION_LEFT){
                        if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move left << stop");
                    }
                    if (leftRightDirection == DIRECTION_RIGHT){
                        if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move right << stop");
                    }
                    mBebopDrone.setRoll((byte) 0);
                    mBebopDrone.setFlag((byte) 0);
                }
            }
            if (System.currentTimeMillis() > leftRightEndPauseTs) {
                leftRightEndPauseTs = 0;
                if (leftRightDirection == DIRECTION_LEFT){
                    if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move left << stop pause");
                }
                if (leftRightDirection == DIRECTION_RIGHT){
                    if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move right << stop pause");
                }
                leftRight = false;
            }
        }
    }

    @Override
    public boolean satisfyLandCondition() {
        double centerWidth = getError();

        if (Math.abs(centerWidth - LEFT_RIGHT_LIMIT_PERCENTAGE_CENTER) < LIMIT_WIDTH_ERROR_CENTER) {
            return true;
        }

        return false;
    }
}