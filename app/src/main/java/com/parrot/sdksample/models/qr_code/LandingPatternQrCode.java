package com.parrot.sdksample.models.qr_code;

import android.graphics.Point;

import com.parrot.sdksample.models.iface.LandingPatternIface;

/**
 * Created by mbodis on 5/15/17.
 */

public class LandingPatternQrCode extends LandingPatternIface{
    public LandingPatternQrCode(long tsDetection, Point center, Point[] pointsBB) {

        super(tsDetection, center, pointsBB, LandingPatternIface.PATTERN_TYPE_QR_CODE);
    }
}
