package com.parrot.sdksample.models.qr_code_landing.detector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.parrot.sdksample.activity.BebopActivity;
import com.parrot.sdksample.models.common.MyPoint;
import com.parrot.sdksample.models.landing.iface.LandingAreaDetector;
import com.parrot.sdksample.models.qr_code_landing.logic.LandingPatternQrCode;
import com.parrot.sdksample.utils.ImageUtils;
import com.parrot.sdksample.view.LandingPatternLayerView;

import java.util.UUID;

/**
 * Created by mbodis on 5/15/17.
 */

public class QrCodeDetector extends LandingAreaDetector {

    BarcodeDetector detector;

    public QrCodeDetector(Context c, LandingPatternLayerView mLandingPatternLayerView) {
        super(c, mLandingPatternLayerView);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        detector = new BarcodeDetector.Builder(this.c)
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();
    }

    @Override
    protected Barcode doInBackground(
            Bitmap... params) {

        Barcode barcode = null;
        if (detector.isOperational()) {
            Frame frame = new Frame.Builder().setBitmap(params[0]).build();
            SparseArray<Barcode> barcodeArr = detector.detect(frame);

            // use only first
            if (barcodeArr.size() > 0){
                barcode = barcodeArr.valueAt(0);
            }

            //for (int index = 0; index < barcodeArr.size(); index++) {
            //   Barcode code = barcodeArr.valueAt(index);
            //   p = code.cornerPoints;
            //}
        }else{
            Log.e("QrCodeDetector", "detector is NOT Operational");
        }

        return barcode;
    }

    @Override
    protected void onPostExecute(Object barcode) {

        if(barcode != null && barcode instanceof Barcode){

            // update draw layer
            drawLayer.setQrCodePoints(((Barcode)barcode).cornerPoints);
            drawLayer.setPointsTs(System.currentTimeMillis());

            // create Landing pattern Obj
            MyPoint center = new MyPoint(((Barcode)barcode).getBoundingBox().centerX(), ((Barcode)barcode).getBoundingBox().centerY());
            MyPoint[] cornerPoints = new MyPoint[((Barcode)barcode).cornerPoints.length];
            for(int i=0; i < ((Barcode)barcode).cornerPoints.length; i++){
                Point p = ((Barcode)barcode).cornerPoints[i];
                cornerPoints[i] = new MyPoint(p.x, p.y);
            }
            String rawValue = ((Barcode)barcode).rawValue;
            long ts = System.currentTimeMillis();
            LandingPatternQrCode mLandingPatternQrCode = new LandingPatternQrCode(ts, center, cornerPoints, rawValue);

            // send pattern via intent - works incorrectly (delay ?)
            // FlyAboveQrCode.updateQrCodeDetectionStatus(c, mLandingPatternQrCode);

            // send pattern directly - works good
            ((BebopActivity)c).mFlyAboveQrCode.setLandingPattern(mLandingPatternQrCode);
        }
        drawLayer.toggleDrawView();
        drawLayer.invalidate();
    }

    static void saveImageToFile(Context c, Bitmap bmp){
        String uuid = UUID.randomUUID().toString();
        boolean res = ImageUtils.saveBitmapToSdCard(c, bmp, uuid);
    }
}
