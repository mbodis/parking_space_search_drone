package com.parrot.sdksample.models.landing;

import com.parrot.sdksample.models.qr_code_landing.logic.LandingPatternQrCode;

/**
 * Created by mbodis on 5/16/17.
 */

public class Constants {

    public static final int VIDEO_WIDTH = 640;
    public static final int VIDEO_HEIGHT = 368;

    // miliseconds that qr code last detected
    public static final long TS_LIMIT_QR_ACTIVE = 100;

    public static boolean isQrActive(long ts) {
        return (System.currentTimeMillis() - ts < TS_LIMIT_QR_ACTIVE);
    }

    public static long getLastTimeQrCodeDetected(LandingPatternQrCode mLandingPatternQrCode){
        return mLandingPatternQrCode == null ? 0 : mLandingPatternQrCode.getTimestampDetected();
    }
}
