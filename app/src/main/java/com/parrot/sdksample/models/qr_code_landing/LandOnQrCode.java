package com.parrot.sdksample.models.qr_code_landing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

import com.parrot.sdksample.activity.BebopActivity;
import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.landing.Constants;
import com.parrot.sdksample.models.qr_code_landing.controllers.ForwardBackwardController;
import com.parrot.sdksample.models.qr_code_landing.controllers.LandController;
import com.parrot.sdksample.models.qr_code_landing.controllers.LandingPatternLostController;
import com.parrot.sdksample.models.qr_code_landing.controllers.LeftRightController;
import com.parrot.sdksample.models.qr_code_landing.controllers.RotationController;
import com.parrot.sdksample.models.qr_code_landing.controllers.SearchController;
import com.parrot.sdksample.models.qr_code_landing.controllers.UpDownController;
import com.parrot.sdksample.models.qr_code_landing.logic.LandingPatternQrCode;

/**
 * Created by mbodis on 4/23/17.
 */

public class LandOnQrCode {

    public static final String TAG = LandOnQrCode.class.getName();

    public static final String ACTION_NEW_QR_CODE_STATUS = "new_qr_code_status";

    public static final String NEW_QR_CODE_STATUS_KEY = "new_qr_code_status_key";

    boolean isLogicThreadAlive = true;
    Thread logicThread;
    private LandingPatternQrCode mLandingPatternQrCode;

    ForwardBackwardController mForwardBackwardController;
    UpDownController mUpDownController;
    LeftRightController mLeftRightController;
    RotationController mRotationController;
    public LandController mLandController;
    SearchController mSearchController;
    LandingPatternLostController mLandingPatternLostController;

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null){
                if (intent.getAction().equals(ACTION_NEW_QR_CODE_STATUS)){
                    Object obj = intent.getParcelableExtra(NEW_QR_CODE_STATUS_KEY);

                    if (obj != null && obj instanceof LandingPatternQrCode){
                        setLandingPattern((LandingPatternQrCode)obj);
                    }
                }

            }
        }
    };

    public LandOnQrCode(final BebopDrone mBebopDrone, final Context ctx) {

        ctx.registerReceiver(mBroadcastReceiver, new IntentFilter(ACTION_NEW_QR_CODE_STATUS));
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

                        if (Constants.isQrActive(Constants.getLastTimeQrCodeDetected(mLandingPatternQrCode))) {
                            if (wasQrCodeDetected()) {
                                boolean landWidth = mLeftRightController.satisfyLandCondition();
                                boolean landHeight = mForwardBackwardController.satisfyLandCondition();
                                boolean landRotation = mRotationController.satisfyLandCondition();
                                boolean landVertical = mUpDownController.satisfyLandCondition();
                                mLandController.landToLandingPattern(landWidth, landHeight, landRotation, landVertical);
                                BebopActivity.sendLandControllerStatus(ctx, landWidth, landHeight, landRotation, landVertical);
                            }
                        }

                    // finish active moves
                    }else{
                        mLandingPatternLostController.endOfMove();
                        mSearchController.endOfMove();
                        mUpDownController.endOfMove();
                        mRotationController.endOfMove();
                        mLeftRightController.endOfMove();
                        mForwardBackwardController.endOfMove();
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

    public void destroy(Context ctx) {
        isLogicThreadAlive = false;
        ctx.unregisterReceiver(mBroadcastReceiver);
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

    public static void updateQrCodeDetectionStatus(Context ctx, LandingPatternQrCode mLandingPatternQrCode){
        Intent mIntent = new Intent(LandOnQrCode.ACTION_NEW_QR_CODE_STATUS);
        mIntent.putExtra(LandOnQrCode.NEW_QR_CODE_STATUS_KEY, mLandingPatternQrCode);
        ctx.sendBroadcast(mIntent);
    }

}
