package com.parrot.sdksample.models.landing.iface;

import android.graphics.Point;
import android.os.Parcelable;

import com.parrot.sdksample.models.common.MyPoint;

/**
 * Created by mbodis on 5/15/17.
 */

public abstract class LandingPatternIface implements Parcelable {

    public static final int PATTERN_TYPE_QR_CODE = 0;

    private int type;
    private long timestampDetected = 0;
    private MyPoint center;
    private MyPoint[] landingBB;

    protected LandingPatternIface() {
    }

    public LandingPatternIface(long tsDetection, MyPoint center, MyPoint[] pointsBB, int type){
        this.timestampDetected = tsDetection;
        this.center = center;
        this.landingBB = pointsBB;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimestampDetected() {
        return timestampDetected;
    }

    public void setTimestampDetected(long timestampDetected) {
        this.timestampDetected = timestampDetected;
    }

    public MyPoint getCenter() {
        return center;
    }

    public void setCenter(MyPoint center) {
        this.center = center;
    }

    public MyPoint[] getLandingBB() {
        return landingBB;
    }

    public void setLandingBB(MyPoint[] landingBB) {
        this.landingBB = landingBB;
    }
}
