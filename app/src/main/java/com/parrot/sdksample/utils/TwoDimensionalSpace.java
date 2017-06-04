package com.parrot.sdksample.utils;

import android.graphics.Point;

import com.parrot.sdksample.models.common.MyPoint;

/**
 * Created by mbodis on 5/13/17.
 */

public class TwoDimensionalSpace {

    public static double distTwoPoints(Point a, Point b){
        return Math.sqrt( ((b.x - a.x) * (b.x - a.x)) + ((b.y - a.y) * (b.y - a.y)));
    }

    public static double distTwoPoints(MyPoint a, MyPoint b){
        return Math.sqrt( ((b.x - a.x) * (b.x - a.x)) + ((b.y - a.y) * (b.y - a.y)));
    }
}
