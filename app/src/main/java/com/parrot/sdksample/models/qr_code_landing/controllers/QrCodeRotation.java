package com.parrot.sdksample.models.qr_code_landing.controllers;

import android.content.Context;

import com.parrot.sdksample.activity.BebopActivity;
import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.landing.iface.MoveControllerIface;
import com.parrot.sdksample.models.qr_code_landing.detector.QrCodeDetector;

import static com.parrot.sdksample.models.qr_code_landing.detector.QrCodeDetector.readingLock;

/**
 * Created by mbodis on 6/22/17.
 */

/**
 * in final phase of landing on qr code try to rotate drone to normalized position
 *
 *
 */
public class QrCodeRotation extends MoveControllerIface {

    private static final String TAG = "QrCodeRotation";

    private static final boolean LOCAL_DEBUG = true;
    private static final long TS_ROTATE_PAUSE = TS_COMMON_PAUSE;

    private static final byte SPEED_FORWARD_BACKWARD_FAST = 15;

    private static final byte SPEED_ROTATION_EXTRA_FAST = 42;

    // controller rotation left/right
    boolean rotate = false;
    long rotateEndMoveTs = 0;
    long rotateEndPauseTs = 0;
    int rotateDirection = -1;

    // controller rotation forward/backward
    boolean forwardBackward = false;
    long forwardBackwardEndOfMoveTs = 0;
    long forwardBackwardEndOfPauseTs = 0;
    int forwardBackwardDirection = -1;

    private boolean qrCodeRotationProcess = false;

    public QrCodeRotation(Context ctx, BebopDrone mBebopDrone) {
        super(ctx, mBebopDrone);
    }

    @Override
    public void executeMove() {
        // lock while reading
        readingLock = true;
        int rotation = (int)getError();
        readingLock = false;

        // execute rotation
        BebopActivity.addTextLogIntent(ctx, "rotate " + ((rotation==-999) ? " - - - " : rotation));
        rotateToQrCodeOrientation(rotation);
    }

    /*
     * possible results: -90 0 90 180
     * invalid value: -999
     */
    @Override
    public synchronized double getError() {
        return com.parrot.sdksample.utils.QrCodeRotation.getQrCodeRotation(QrCodeDetector.lastQrCodeBitmap, mLandingPatternQrCode.getLandingBBPoints(), false);
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
                moveBackward();
            }
        }

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
                setQrCodeRotationProcess(false);
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

        if (forwardBackward){
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
    public boolean satisfyLandCondition() {
        if (isQrCodeRotationProcess())
            return false;

        // lock while reading
        readingLock = true;
        int rotation = (int)getError();
        readingLock = false;

        return (rotation == 0);
    }

    private void rotateToQrCodeOrientation(int rotation){
        if (rotation == -999 || rotation == 0) return;

        int angle90Time = 2000;
        int timeRotation = Math.abs(rotation)/90 * angle90Time;

        setQrCodeRotationProcess(true);
        mBebopDrone.setYaw((byte) ((rotation > 1) ? SPEED_ROTATION_EXTRA_FAST : -SPEED_ROTATION_EXTRA_FAST) );
        rotate = true;
        rotateEndMoveTs = System.currentTimeMillis() + timeRotation;
        rotateEndPauseTs = System.currentTimeMillis() + timeRotation + TS_ROTATE_PAUSE;
        rotateDirection = (rotation > 1) ? DIRECTION_CLOCKWISE: DIRECTION_COUNTER_CLOCKWISE ;
    }

    private void moveBackward(){
        int moveBackTime = 900;

        if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "moving backward");
        mBebopDrone.setPitch((byte) -SPEED_FORWARD_BACKWARD_FAST);
        mBebopDrone.setFlag((byte) 1);
        forwardBackward = true;
        forwardBackwardEndOfMoveTs = System.currentTimeMillis() + moveBackTime;
        forwardBackwardEndOfPauseTs = System.currentTimeMillis() + moveBackTime + TS_COMMON_PAUSE;
        forwardBackwardDirection = DIRECTION_BACKWARD;
    }

    public boolean isQrCodeRotationProcess() {
        return qrCodeRotationProcess;
    }

    public void setQrCodeRotationProcess(boolean qrCodeRotationProcess) {
        this.qrCodeRotationProcess = qrCodeRotationProcess;
    }

}
