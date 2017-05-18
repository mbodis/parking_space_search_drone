package com.parrot.sdksample.models.qr_code.controllers;

import android.content.Context;

import com.parrot.sdksample.activity.BebopActivity;
import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.Constants;
import com.parrot.sdksample.models.iface.MoveControllerIface;

/**
 * Created by mbodis on 5/16/17.
 */

public class SearchController extends MoveControllerIface {

    public static final int SEARCH_STEP_0_INIT = 0;
    public static final int SEARCH_STEP_1_MOVE_UP = 1;
    public static final int SEARCH_STEP_2_CIRCLE = 2;
    public static final int SEARCH_STEP_3_FAILED = 3;
    public static final int SEARCH_STEP_4_EXIT = 4;

    private static final long TS_LIMIT_QR_UNACTIVE = 5 * 1000;

    private static final byte SPEED_ROTATION = 10;
    private static final byte SPEED_UP_DOWN = 5;

    // controller for searching qr code
    boolean search = false;
    long searchNextStep = 0;
    int searchStep = SEARCH_STEP_0_INIT;

    // controller up/down
    boolean upDown = false;
    long upDownEndMoveTs = 0;
    long upDownEndPauseTs = 0;
    int upDownDirection = -1;

    // controller rotation left/right
    boolean rotate = false;
    long rotateEndMoveTs = 0;
    long rotateEndPauseTs = 0;
    int rotateDirection = -1;

    public SearchController(Context ctx, BebopDrone mBebopDrone) {
        super(ctx, mBebopDrone);
    }

    /*
     * QR code was not detected for some time, search for it
     * 1) move up
     * 2) circle in place
     */
    @Override
    public void executeMove() {

        long timestampDetected = mLandingPatternQrCode == null ? 0 : this.mLandingPatternQrCode.getTimestampDetected();

        // init searching
        if ( (((System.currentTimeMillis() - timestampDetected) > TS_LIMIT_QR_UNACTIVE) || (timestampDetected==0))
                && searchStep == SEARCH_STEP_0_INIT) {
            search = true;
            searchStep = SEARCH_STEP_1_MOVE_UP;
            searchNextStep = 0;
        }

        if (search) {
            if (System.currentTimeMillis() > searchNextStep && searchNextStep != 0) {
                if (searchStep == SEARCH_STEP_1_MOVE_UP) {
                    searchStep = SEARCH_STEP_2_CIRCLE;

                } else if (searchStep == SEARCH_STEP_2_CIRCLE) {
                    searchStep = SEARCH_STEP_3_FAILED;

                } else if (searchStep == SEARCH_STEP_3_FAILED) {
                    searchStep = SEARCH_STEP_4_EXIT;
                }
            }

            // search moves
            switch (searchStep) {
                case SEARCH_STEP_1_MOVE_UP:
                    if (!upDown) {
                        BebopActivity.addTextLogIntent(ctx, "move up -> start search ");
                        mBebopDrone.setGaz((byte) (2 * SPEED_UP_DOWN));
                        upDown = true;
                        upDownEndMoveTs = System.currentTimeMillis() + 2 * TS_COMMON_MOVE;
                        upDownEndPauseTs = System.currentTimeMillis() + 2 * TS_COMMON_MOVE + TS_COMMON_PAUSE;
                        upDownDirection = DIRECTION_UP;
                        searchNextStep = upDownEndPauseTs;
                    }
                    break;

                case SEARCH_STEP_2_CIRCLE:
                    if (!rotate) {
                        BebopActivity.addTextLogIntent(ctx, "rotate clockwise -> start search ");
                        mBebopDrone.setYaw((byte) SPEED_ROTATION);
                        rotate = true;
                        rotateEndMoveTs = System.currentTimeMillis() + 100 * TS_COMMON_MOVE;
                        rotateEndPauseTs = System.currentTimeMillis() + 100 * TS_COMMON_MOVE + TS_COMMON_PAUSE;
                        rotateDirection = DIRECTION_CLOCKWISE;
                        searchNextStep = rotateEndPauseTs;
                    }
                    break;

                case SEARCH_STEP_3_FAILED:
                    BebopActivity.addTextLogIntent(ctx, "FAILED TO SEARCH QRcode");
                    break;
            }
        }
    }

    /*
     * stop searching, QR code was found
     */
    @Override
    public void endOfMove() {

        long timestampDetected = mLandingPatternQrCode == null ? 0 : this.mLandingPatternQrCode.getTimestampDetected();

        // end of move up
        endOfMoveUpDown();
        // end of move rotate
        endOfMoveRotate();

        if (search){
            if (Constants.isQrActive(timestampDetected)){
                BebopActivity.addTextLogIntent(ctx, "end searching, QR code found");
                search = false;
                searchStep = SEARCH_STEP_0_INIT;
                searchNextStep = 0;

                // stop rotation
                if (rotateDirection == DIRECTION_CLOCKWISE)
                    BebopActivity.addTextLogIntent(ctx, "move clockwise << stop search");
                if (rotateDirection == DIRECTION_COUNTER_CLOCKWISE)
                    BebopActivity.addTextLogIntent(ctx, "move counter clockwise << stop search");
                mBebopDrone.setYaw((byte) 0);
                rotate = false;
                rotateEndMoveTs = 0;
                rotateEndPauseTs = 0;
            }
        }
    }

    private void endOfMoveUpDown(){
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

    private void endOfMoveRotate(){
        if (rotate) {
            if (rotateEndMoveTs > 0) {
                if (System.currentTimeMillis() > rotateEndMoveTs) {
                    rotateEndMoveTs = 0;
                    if (rotateDirection == DIRECTION_CLOCKWISE)
                        BebopActivity.addTextLogIntent(ctx, "move clockwise << stop");
                    if (rotateDirection == DIRECTION_COUNTER_CLOCKWISE)
                        BebopActivity.addTextLogIntent(ctx, "move counter clockwise << stop");
                    mBebopDrone.setYaw((byte) 0);
                }
            }
            if (System.currentTimeMillis() > rotateEndPauseTs) {
                rotateEndPauseTs = 0;
                if (rotateDirection == DIRECTION_CLOCKWISE)
                    BebopActivity.addTextLogIntent(ctx, "move clockwise << stop pause");
                if (rotateDirection == DIRECTION_COUNTER_CLOCKWISE)
                    BebopActivity.addTextLogIntent(ctx, "move counter clockwise << stop pause");
                rotate = false;
            }
        }
    }

    @Override
    public boolean satisfyLandCondition() {
        return false;
    }
}
