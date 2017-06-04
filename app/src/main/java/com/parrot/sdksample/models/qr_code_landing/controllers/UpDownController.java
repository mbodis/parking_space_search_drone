package com.parrot.sdksample.models.qr_code_landing.controllers;

import android.content.Context;

import com.parrot.sdksample.activity.BebopActivity;
import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.landing.Constants;
import com.parrot.sdksample.models.landing.iface.MoveControllerIface;
import com.parrot.sdksample.utils.TwoDimensionalSpace;

/**
 * Created by mbodis on 5/15/17.
 */

public class UpDownController extends MoveControllerIface {

    private static final long TS_UP_DOWN_MOVE = 2*TS_COMMON_MOVE;
    private static final long TS_UP_DOWN_PAUSE = TS_COMMON_PAUSE;

    private static final byte SPEED_UP_DOWN_SLOW = 3;
    private static final byte SPEED_UP_DOWN_NORMAL = 5;
    private static final byte SPEED_UP_DOWN_FAST = 15;

    private static final int UP_DOWN_WIDTH_OPTIMAL = 130;
    private static final int UP_DOWN_WIDTH_TOO_HIGH = 100;
    private static final int UP_DOWN_WIDTH_FAR_TOO_HIGH = 80;

    byte LIMIT_VERTICAL_ERROR = 25; // 5 pixel detected QRcode width

    // controller
    boolean upDown = false;
    long upDownEndMoveTs = 0;
    long upDownEndPauseTs = 0;
    int upDownDirection = -1;

    public UpDownController(Context ctx, BebopDrone mBebopDrone) {
        super(ctx, mBebopDrone);
    }

    /*
     * NOTE: it uses FIXED FRAME SIZE
     */
    @Override
    public void executeMove() {

        if (mLandingPatternQrCode == null)
            return;
        if (!Constants.isQrActive(Constants.getLastTimeQrCodeDetected(mLandingPatternQrCode)))
            return;

        if (!upDown) {

            double verticalShiftPx = getError();

            if (Math.abs(verticalShiftPx - UP_DOWN_WIDTH_OPTIMAL) < LIMIT_VERTICAL_ERROR) {
                return;
            }

            if (verticalShiftPx < UP_DOWN_WIDTH_FAR_TOO_HIGH) {
                BebopActivity.addTextLogIntent(ctx, "move down -> startFAST " + (int) verticalShiftPx);
                mBebopDrone.setGaz((byte) -SPEED_UP_DOWN_FAST);
                upDown = true;
                upDownEndMoveTs = System.currentTimeMillis() + TS_UP_DOWN_MOVE;
                upDownEndPauseTs = System.currentTimeMillis() + TS_UP_DOWN_MOVE + TS_UP_DOWN_PAUSE;
                upDownDirection = DIRECTION_DOWN;

            }else if (verticalShiftPx < UP_DOWN_WIDTH_TOO_HIGH) {
                BebopActivity.addTextLogIntent(ctx, "move down -> start " + (int) verticalShiftPx);
                mBebopDrone.setGaz((byte) -SPEED_UP_DOWN_NORMAL);
                upDown = true;
                upDownEndMoveTs = System.currentTimeMillis() + TS_UP_DOWN_MOVE;
                upDownEndPauseTs = System.currentTimeMillis() + TS_UP_DOWN_MOVE + TS_UP_DOWN_PAUSE;
                upDownDirection = DIRECTION_DOWN;

            } else if (verticalShiftPx > UP_DOWN_WIDTH_OPTIMAL) {
                BebopActivity.addTextLogIntent(ctx, "move up -> start slow " + (int) verticalShiftPx);
                mBebopDrone.setGaz((byte) SPEED_UP_DOWN_SLOW);
                upDown = true;
                upDownEndMoveTs = System.currentTimeMillis() + TS_UP_DOWN_MOVE;
                upDownEndPauseTs = System.currentTimeMillis() + TS_UP_DOWN_MOVE + TS_UP_DOWN_PAUSE;
                upDownDirection = DIRECTION_UP;

            } else if (verticalShiftPx < UP_DOWN_WIDTH_OPTIMAL) {
                BebopActivity.addTextLogIntent(ctx, "move down -> start slow " + (int) verticalShiftPx);
                mBebopDrone.setGaz((byte) -SPEED_UP_DOWN_SLOW);
                upDown = true;
                upDownEndMoveTs = System.currentTimeMillis() + TS_UP_DOWN_MOVE;
                upDownEndPauseTs = System.currentTimeMillis() + TS_UP_DOWN_MOVE + TS_UP_DOWN_PAUSE;
                upDownDirection = DIRECTION_DOWN;

            }
        }
    }

    @Override
    public void endOfMove() {
        if (upDown) {
            if (upDownEndMoveTs > 0) {
                if (System.currentTimeMillis() > upDownEndMoveTs) {
                    upDownEndMoveTs = 0;
                    if (upDownDirection == DIRECTION_UP)
                        BebopActivity.addTextLogIntent(ctx, "move up << stop");
                    if (upDownDirection == DIRECTION_DOWN)
                        BebopActivity.addTextLogIntent(ctx, "move down << stop");
                    mBebopDrone.setGaz((byte) 0);
                }
            }
            if (System.currentTimeMillis() > upDownEndPauseTs) {
                upDownEndPauseTs = 0;
                if (upDownDirection == DIRECTION_UP)
                    BebopActivity.addTextLogIntent(ctx, "move up << stop pause");
                if (upDownDirection == DIRECTION_DOWN)
                    BebopActivity.addTextLogIntent(ctx, "move down << stop pause");
                upDown = false;
            }
        }
    }

    @Override
    public double getError() {
        return TwoDimensionalSpace.distTwoPoints(this.mLandingPatternQrCode.getLandingBB()[0], this.mLandingPatternQrCode.getLandingBB()[1]);
    }

    @Override
    public boolean satisfyLandCondition() {

        double verticalShiftPx = getError();

        if (Math.abs(verticalShiftPx - UP_DOWN_WIDTH_OPTIMAL) < LIMIT_VERTICAL_ERROR) {
            return true;
        }
        return false;
    }
}
