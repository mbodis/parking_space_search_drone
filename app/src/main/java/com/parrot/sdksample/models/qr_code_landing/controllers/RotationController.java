package com.parrot.sdksample.models.qr_code_landing.controllers;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.parrot.sdksample.activity.BebopActivity;
import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.landing.Constants;
import com.parrot.sdksample.models.landing.iface.MoveControllerIface;
import com.parrot.sdksample.models.qr_code_landing.detector.QrCodeDetector;
import com.parrot.sdksample.utils.QrCodeRotation;

import static com.parrot.sdksample.models.qr_code_landing.detector.QrCodeDetector.readingLock;

/**
 * Created by mbodis on 5/15/17.
 */

public class RotationController extends MoveControllerIface {

    private static final String TAG = "RotationController";

    private static final boolean LOCAL_DEBUG = false;

    private static final long TS_ROTATE_MOVE = TS_COMMON_MOVE;
    private static final long TS_ROTATE_PAUSE = TS_COMMON_PAUSE;

    private static final byte SPEED_ROTATION_SLOW = 5;
    private static final byte SPEED_ROTATION_NORMAL = 10;
    private static final byte SPEED_ROTATION_FAST = 15;

    private static final int ROTATE_LIMIT_PERCENTAGE_HIGH_FAR = 23;
    private static final int ROTATE_LIMIT_PERCENTAGE_HIGH = 18;
    private static final int ROTATE_LIMIT_PERCENTAGE_CENTER = 0;

    byte LIMIT_ROTATION_ERROR = 5; // pixel distance TopLeft and BottomLeft

    // controller rotation left/right
    boolean rotate = false;
    long rotateEndMoveTs = 0;
    long rotateEndPauseTs = 0;
    int rotateDirection = -1;

    public RotationController(Context ctx, BebopDrone mBebopDrone) {
        super(ctx, mBebopDrone);
    }

    @Override
    public void executeMove() {

        if (mLandingPatternQrCode == null)
            return;
        if (!Constants.isQrActive(Constants.getLastTimeQrCodeDetected(mLandingPatternQrCode)))
            return;

        if (!rotate && this.mLandingPatternQrCode.getLandingBB() != null ) {
            double horizontalShiftPx = getError();

            if ((horizontalShiftPx >= 0 && horizontalShiftPx < LIMIT_ROTATION_ERROR) || (
                    (horizontalShiftPx < 0 && horizontalShiftPx > -LIMIT_ROTATION_ERROR))) {
                return;
            }

            if (horizontalShiftPx > ROTATE_LIMIT_PERCENTAGE_HIGH_FAR) {
                if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "rotate clockwise -> startFAST " + horizontalShiftPx);
                mBebopDrone.setYaw((byte) SPEED_ROTATION_FAST);
                rotate = true;
                rotateEndMoveTs = System.currentTimeMillis() + TS_ROTATE_MOVE;
                rotateEndPauseTs = System.currentTimeMillis() + TS_ROTATE_MOVE + TS_ROTATE_PAUSE;
                rotateDirection = DIRECTION_CLOCKWISE;

            }else if (horizontalShiftPx > ROTATE_LIMIT_PERCENTAGE_HIGH) {
                if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "rotate clockwise -> start " + horizontalShiftPx);
                mBebopDrone.setYaw((byte) SPEED_ROTATION_NORMAL);
                rotate = true;
                rotateEndMoveTs = System.currentTimeMillis() + TS_ROTATE_MOVE;
                rotateEndPauseTs = System.currentTimeMillis() + TS_ROTATE_MOVE + TS_ROTATE_PAUSE;
                rotateDirection = DIRECTION_CLOCKWISE;

            } else if (horizontalShiftPx < -ROTATE_LIMIT_PERCENTAGE_HIGH_FAR) {
                if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "rotate counter clockwise -> startFAST " + horizontalShiftPx);
                mBebopDrone.setYaw((byte) -SPEED_ROTATION_FAST);
                rotate = true;
                rotateEndMoveTs = System.currentTimeMillis() + TS_ROTATE_MOVE;
                rotateEndPauseTs = System.currentTimeMillis() + TS_ROTATE_MOVE + TS_ROTATE_PAUSE;
                rotateDirection = DIRECTION_COUNTER_CLOCKWISE;

            } else if (horizontalShiftPx < -ROTATE_LIMIT_PERCENTAGE_HIGH) {
                if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "rotate counter clockwise -> start " + horizontalShiftPx);
                mBebopDrone.setYaw((byte) -SPEED_ROTATION_NORMAL);
                rotate = true;
                rotateEndMoveTs = System.currentTimeMillis() + TS_ROTATE_MOVE;
                rotateEndPauseTs = System.currentTimeMillis() + TS_ROTATE_MOVE + TS_ROTATE_PAUSE;
                rotateDirection = DIRECTION_COUNTER_CLOCKWISE;

            } else if (horizontalShiftPx > ROTATE_LIMIT_PERCENTAGE_CENTER) {
                if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "rotate clockwise -> start slow " + horizontalShiftPx);
                mBebopDrone.setYaw((byte) SPEED_ROTATION_SLOW);
                rotate = true;
                rotateEndMoveTs = System.currentTimeMillis() + TS_ROTATE_MOVE;
                rotateEndPauseTs = System.currentTimeMillis() + TS_ROTATE_MOVE + TS_ROTATE_PAUSE;
                rotateDirection = DIRECTION_CLOCKWISE;


            } else if (horizontalShiftPx < ROTATE_LIMIT_PERCENTAGE_CENTER) {
                if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "rotate counter clockwise -> start slow " + horizontalShiftPx);
                mBebopDrone.setYaw((byte) -SPEED_ROTATION_SLOW);
                rotate = true;
                rotateEndMoveTs = System.currentTimeMillis() + TS_ROTATE_MOVE;
                rotateEndPauseTs = System.currentTimeMillis() + TS_ROTATE_MOVE + TS_ROTATE_PAUSE;
                rotateDirection = DIRECTION_COUNTER_CLOCKWISE;

            }

        }
    }

    @Override
    public double getError(){
        return this.mLandingPatternQrCode.getLandingBB()[0].x - this.mLandingPatternQrCode.getLandingBB()[3].x;
    }

    @Override
    public void endOfMove() {
        if (rotate) {
            if (rotateEndMoveTs > 0) {
                if (System.currentTimeMillis() > rotateEndMoveTs) {
                    rotateEndMoveTs = 0;
                    if (rotateDirection == DIRECTION_CLOCKWISE){
                        if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move clockwise << stop");
                    }
                    if (rotateDirection == DIRECTION_COUNTER_CLOCKWISE){
                        if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move counter clockwise << stop");
                    }
                    mBebopDrone.setYaw((byte) 0);
                }
            }
            if (System.currentTimeMillis() > rotateEndPauseTs) {
                rotateEndPauseTs = 0;
                if (rotateDirection == DIRECTION_CLOCKWISE){
                    if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move clockwise << stop pause");
                }
                if (rotateDirection == DIRECTION_COUNTER_CLOCKWISE){
                    if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move counter clockwise << stop pause");
                }
                rotate = false;
            }
        }
    }

    @Override
    public void stopMoveImmediately() {
        if (rotate) {
            mBebopDrone.setYaw((byte) 0);
            rotateEndMoveTs = 0;
            rotateEndPauseTs = 0;
            rotateDirection = -1;
            rotate = false;
            if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "move rotation << stop immediately");
        }
    }

    @Override
    public boolean satisfyLandCondition() {

        double horizontalShiftPx = getError();
        if ((horizontalShiftPx >= 0 && horizontalShiftPx < LIMIT_ROTATION_ERROR) || (
                (horizontalShiftPx < 0 && horizontalShiftPx > -LIMIT_ROTATION_ERROR)) ) {

            return true;
        }

        return false;
    }
}
