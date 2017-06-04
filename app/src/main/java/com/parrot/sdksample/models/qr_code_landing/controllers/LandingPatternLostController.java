package com.parrot.sdksample.models.qr_code_landing.controllers;

import android.content.Context;

import com.parrot.sdksample.activity.BebopActivity;
import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.landing.Constants;
import com.parrot.sdksample.models.landing.iface.MoveControllerIface;

/**
 * Created by mbodis on 5/17/17.
 */
/*
 * search for last position of QR code too much forward / backward
 *
 */
public class LandingPatternLostController extends MoveControllerIface {

    private static final long TS_LIMIT_QR_SHORT_TIME_MISSING = 500;

    private static final byte SPEED_FORWARD_BACKWARD = 10;

    // controller forward/backward
    boolean forwardBackward = false;
    long forwardBackwardEndOfMoveTs = 0;
    long forwardBackwardEndOfPauseTs = 0;
    int forwardBackwardDirection = -1;

    public LandingPatternLostController(Context ctx, BebopDrone mBebopDrone) {
        super(ctx, mBebopDrone);
    }


    @Override
    public void executeMove() {
        if (mLandingPatternQrCode == null)
            return;
        if (!Constants.isQrActive(Constants.getLastTimeQrCodeDetected(mLandingPatternQrCode)))
            return;


        if ((System.currentTimeMillis() - this.mLandingPatternQrCode.getTimestampDetected()) > TS_LIMIT_QR_SHORT_TIME_MISSING) {
            if ((System.currentTimeMillis() - this.mLandingPatternQrCode.getTimestampDetected()) < 2 * TS_LIMIT_QR_SHORT_TIME_MISSING) {

                double centerWidth = (double) this.mLandingPatternQrCode.getCenter().x / Constants.VIDEO_WIDTH * 100;

                // the qr code should be somewhere in middle
                if (centerWidth > 30 && centerWidth < 70) {

                    double centerHeight = (double) this.mLandingPatternQrCode.getCenter().y / Constants.VIDEO_HEIGHT * 100;

                    if (!forwardBackward) {
                        // qr code was last time in front, drone moved too backward, move forward to see QR code
                        if (centerHeight < 30) {
                            BebopActivity.addTextLogIntent(ctx, "go forward QR missing -> start " + (int) centerHeight);
                            mBebopDrone.setPitch((byte) SPEED_FORWARD_BACKWARD);
                            mBebopDrone.setFlag((byte) 1);
                            forwardBackward = true;
                            forwardBackwardEndOfMoveTs = System.currentTimeMillis() + 2 * TS_COMMON_MOVE;
                            forwardBackwardEndOfPauseTs = System.currentTimeMillis() + 2 * TS_COMMON_MOVE + TS_COMMON_PAUSE;
                            forwardBackwardDirection = DIRECTION_FORWARD;

                            // qr code was last time in back, drone moved too forward, move backward to see QR code
                        } else if (centerHeight > 70) {
                            BebopActivity.addTextLogIntent(ctx, "go backward QR missing -> start " + (int)centerHeight);
                            mBebopDrone.setPitch((byte) -SPEED_FORWARD_BACKWARD);
                            mBebopDrone.setFlag((byte) 1);
                            forwardBackward = true;
                            forwardBackwardEndOfMoveTs = System.currentTimeMillis() + 2 * TS_COMMON_MOVE;
                            forwardBackwardEndOfPauseTs = System.currentTimeMillis() + 2 * TS_COMMON_MOVE + TS_COMMON_PAUSE;
                            forwardBackwardDirection = DIRECTION_BACKWARD;
                        }
                    }
                }
            }
        }

    }

    @Override
    public void endOfMove() {
        if (forwardBackward) {
            if (forwardBackwardEndOfMoveTs > 0) {
                if (System.currentTimeMillis() > forwardBackwardEndOfMoveTs) {
                    forwardBackwardEndOfMoveTs = 0;
                    if (forwardBackwardDirection == DIRECTION_FORWARD)
                        BebopActivity.addTextLogIntent(ctx, "move forward << stop");
                    if (forwardBackwardDirection == DIRECTION_BACKWARD)
                        BebopActivity.addTextLogIntent(ctx, "move backward << stop");
                    mBebopDrone.setPitch((byte) 0);
                    mBebopDrone.setFlag((byte) 0);
                }
            }
            if (System.currentTimeMillis() > forwardBackwardEndOfPauseTs) {
                forwardBackwardEndOfPauseTs = 0;
                if (forwardBackwardDirection == DIRECTION_FORWARD)
                    BebopActivity.addTextLogIntent(ctx, "move forward << stop pause");
                if (forwardBackwardDirection == DIRECTION_BACKWARD)
                    BebopActivity.addTextLogIntent(ctx, "move backward << stop pause");
                forwardBackward = false;
            }
        }
    }

    @Override
    public boolean satisfyLandCondition() {
        return false;
    }
}
