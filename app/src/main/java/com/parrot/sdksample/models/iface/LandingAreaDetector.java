package com.parrot.sdksample.models.iface;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.parrot.sdksample.view.LandingPatternLayerView;

/**
 * Created by mbodis on 5/15/17.
 */

public abstract class LandingAreaDetector extends AsyncTask<Bitmap, Void, Object> {


    protected Context c;
    protected LandingPatternLayerView drawLayer;

    public LandingAreaDetector(Context c, LandingPatternLayerView mLandingPatternLayerView){
        this.c = c;
        this.drawLayer = mLandingPatternLayerView;
    }

}
