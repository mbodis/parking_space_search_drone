package com.parrot.sdksample.logic;

import android.content.Context;
import android.graphics.Point;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.parrot.sdksample.activity.BebopActivity;
import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.Constants;
import com.parrot.sdksample.models.qr_code.LandingPatternQrCode;
import com.parrot.sdksample.models.qr_code.controllers.ForwardBackwardController;
import com.parrot.sdksample.models.qr_code.controllers.LandController;
import com.parrot.sdksample.models.qr_code.controllers.LandingPatternLostController;
import com.parrot.sdksample.models.qr_code.controllers.LeftRightController;
import com.parrot.sdksample.models.qr_code.controllers.RotationController;
import com.parrot.sdksample.models.qr_code.controllers.SearchController;
import com.parrot.sdksample.models.qr_code.controllers.UpDownController;
import com.parrot.sdksample.utils.TwoDimensionalSpace;

import static com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING;
import static com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING;

/**
 * Created by mbodis on 4/23/17.
 */

public class QrCodeFlyAbove {

    public static final String TAG = QrCodeFlyAbove.class.getName();

    boolean isLogicThreadAlive = true;
    Thread logicThread;
    private LandingPatternQrCode mLandingPatternQrCode;

    ForwardBackwardController mForwardBackwardController;
    UpDownController mUpDownController;
    LeftRightController mLeftRightController;
    RotationController mRotationController;
    LandController mLandController;
    SearchController mSearchController;
    LandingPatternLostController mLandingPatternLostController;

    public QrCodeFlyAbove(final BebopDrone mBebopDrone, final Context ctx) {

        initControllers(ctx, mBebopDrone);

        logicThread = new Thread(new Runnable() {

            @Override
            public void run() {
                SystemClock.sleep(1000);//initial sleep
                while (isLogicThreadAlive) {

                    if (mLandController.isLandingEnabled()) {

                        mLandingPatternLostController.executeMove();
                        mLandingPatternLostController.endOfMove();
                        mSearchController.executeMove();
                        mSearchController.endOfMove();

                        mUpDownController.executeMove();
                        mRotationController.executeMove();
                        mLeftRightController.executeMove();
                        mForwardBackwardController.executeMove();

                        mUpDownController.endOfMove();
                        mRotationController.endOfMove();
                        mLeftRightController.endOfMove();
                        mForwardBackwardController.endOfMove();

                        if (wasQrCodeDetected()) {
                            boolean landWidth = mLeftRightController.satisfyLandCondition();
                            boolean landHeight = mForwardBackwardController.satisfyLandCondition();
                            boolean landRotation = mRotationController.satisfyLandCondition();
                            boolean landVertical = mUpDownController.satisfyLandCondition();
                            mLandController.landToLandingPattern(landWidth, landHeight, landRotation, landVertical);
                            BebopActivity.sendLandControllerStatus(ctx, landWidth, landHeight, landRotation, landVertical);
                        }

                    }

                    SystemClock.sleep(50);
                }
            }
        });
        logicThread.start();
    }

    private void initControllers(final Context ctx, final BebopDrone mBebopDrone){
        mForwardBackwardController = new ForwardBackwardController(ctx, mBebopDrone);
        mUpDownController = new UpDownController(ctx, mBebopDrone);
        mLeftRightController = new LeftRightController(ctx, mBebopDrone);
        mRotationController = new RotationController(ctx, mBebopDrone);
        mLandController = new LandController(ctx, mBebopDrone);
        mSearchController = new SearchController(ctx, mBebopDrone);
        mLandingPatternLostController = new LandingPatternLostController(ctx, mBebopDrone);
    }

    private boolean wasQrCodeDetected(){
        return mLandingPatternQrCode != null;
    }

    public void destroy() {
        isLogicThreadAlive = false;
    }

    public long getLastTsQrCode() {
        if (!wasQrCodeDetected())
            return 0;

        return mLandingPatternQrCode.getTimestampDetected();
    }

    public void setLandingPattern(LandingPatternQrCode mLandingPatternQrCode){
        this.mLandingPatternQrCode = mLandingPatternQrCode;

        mForwardBackwardController.updateLandingPattern(mLandingPatternQrCode);
        mUpDownController.updateLandingPattern(mLandingPatternQrCode);
        mLeftRightController.updateLandingPattern(mLandingPatternQrCode);
        mRotationController.updateLandingPattern(mLandingPatternQrCode);
        mSearchController.updateLandingPattern(mLandingPatternQrCode);
        mLandingPatternLostController.updateLandingPattern(mLandingPatternQrCode);
    }

    public void setLandingToQrCodeEnabled(boolean isLandingToQrCodeEnabled) {
        mLandController.setLandToQrCodeEnabled(isLandingToQrCodeEnabled);
        mLandController.setHasLanded(false);
    }

}
