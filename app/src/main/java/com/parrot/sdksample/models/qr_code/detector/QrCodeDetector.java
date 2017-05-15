package com.parrot.sdksample.models.qr_code.detector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.parrot.sdksample.activity.BebopActivity;
import com.parrot.sdksample.models.iface.LandingAreaDetector;
import com.parrot.sdksample.models.qr_code.LandingPatternQrCode;
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
            drawLayer.setQrCodePoints(((Barcode)barcode).cornerPoints);
            drawLayer.setPointsTs(System.currentTimeMillis());
            Point center = new Point(((Barcode)barcode).getBoundingBox().centerX(), ((Barcode)barcode).getBoundingBox().centerY());
            Point[] cornerPoints = ((Barcode)barcode).cornerPoints;
            long ts = System.currentTimeMillis();
            LandingPatternQrCode mLandingPatternQrCode = new LandingPatternQrCode(ts, center, cornerPoints);

            ((BebopActivity)c).mQrCodeFlyAbove.setLandingPattern(mLandingPatternQrCode); // TODO use intent/handler ?
        }
        drawLayer.toggleDrawView();
        drawLayer.invalidate();
    }

    static void saveImageToFile(Context c, Bitmap bmp){
        String uuid = UUID.randomUUID().toString();
        boolean res = ImageUtils.saveBitmapToSdCard(c, bmp, uuid);
    }
}
