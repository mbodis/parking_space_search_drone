package com.parrot.sdksample.models.iface;

import android.graphics.Point;

/**
 * Created by mbodis on 5/15/17.
 */

public abstract class LandingPatternIface{

    public static final int PATTERN_TYPE_QR_CODE = 0;

    private int type;
    private long timestampDetected = 0;
    private Point center;
    private Point[] landingBB;

    public LandingPatternIface(long tsDetection, Point center, Point[] pointsBB, int type){
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

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public Point[] getLandingBB() {
        return landingBB;
    }

    public void setLandingBB(Point[] landingBB) {
        this.landingBB = landingBB;
    }
}
