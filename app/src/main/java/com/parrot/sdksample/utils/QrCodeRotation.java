package com.parrot.sdksample.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;
import java.util.UUID;

/**
 * Created by mbodis on 6/18/17.
 */

public class QrCodeRotation {

    private static final String TAG = QrCodeRotation.class.getName();

    /*
     * just for test purposes
     */
    public static void test(Context ctx) {
        //String externalDirectory = Environment.getExternalStorageDirectory().toString().concat("/ARSDKMedias/test_qr0.jpg");
        String externalDirectory = Environment.getExternalStorageDirectory().toString().concat("/ARSDKMedias/test_qr1.png");
        //String externalDirectory = Environment.getExternalStorageDirectory().toString().concat("/ARSDKMedias/test_qr2.png");
        //String externalDirectory = Environment.getExternalStorageDirectory().toString().concat("/ARSDKMedias/test_qr4.jpg");
        File f = new File(externalDirectory);
        if (f.exists()) {
            Log.d(TAG, "file exists: " + externalDirectory);
            BarcodeDetector detector = new BarcodeDetector.Builder(ctx)
                    .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                    .build();

            Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath());
            b = b.copy(Bitmap.Config.ARGB_8888, true);

            if (detector.isOperational()) {
                Frame frame = new Frame.Builder().setBitmap(b).build();
                SparseArray<Barcode> barcodeArr = detector.detect(frame);

                // use only first
                if (barcodeArr.size() > 0) {
                    Log.d(TAG, "barcodeArr.size(): " + barcodeArr.size());
                    Barcode barcode = barcodeArr.valueAt(0);

                    int rotation = QrCodeRotation.getQrCodeRotation(b, barcode.cornerPoints, true);
                    Log.d(TAG, "QR CODE ROTATION: " + rotation);

                    // save img
                    String uuid = UUID.randomUUID().toString();
                    Log.d(TAG, "uuid: " + uuid);
                    ImageUtils.saveBitmapToSdCard(ctx, b, uuid);

                }
            }
        } else {
            Log.e(TAG, "file NOT exists");
        }

    }


    /*
     *
     * NOTE !
     *  works only for -90 0 90 180 rotations
     *  @cornerPoints are in order
     *      1 . . . 2
     *      .       .
     *      .       .
     *      .       .
     *      4 . . . 3
     */
    public static int getQrCodeRotation(Bitmap b, Point[] cornerPoints, boolean drawOnBitmap) {

        try {
            Canvas c = new Canvas(b);

            int tr = 0, tl = 0, br = 0, bl = 0;

            for (int i = 0; i < 4; i++) {
                //Log.d(TAG, " coords: " + cornerPoints[i].x + " " + cornerPoints[i].y);
                //Log.d(TAG, " dist: " + TwoDimensionalSpace.distTwoPoints(cornerPoints[i], cornerPoints[(i + 1) % 4]));
                Point point = cornerPoints[i];
                Point nextPoint = cornerPoints[(i + 1) % 4];
                Point prevPoint = cornerPoints[(i - 1 + 4) % 4];
                double distNextPoint = TwoDimensionalSpace.distTwoPoints(point, nextPoint);
                double distPrevPoint = TwoDimensionalSpace.distTwoPoints(point, prevPoint);

                int posX, posY;
                Point sq1 = new Point(0, 0);
                Point sq2 = new Point(0, 0);
                Point sq3 = new Point(0, 0);
                Point sq4 = new Point(0, 0);

                // top-left
                if (i == 0) {
                    Log.d(TAG, "top-left " + i);
                    posX = 1;
                    posY = 1;
                    sq1 = point;
                    sq2 = new Point(point.x + (int) (distNextPoint / 3) * posX, point.y);
                    sq3 = new Point(point.x + (int) (distNextPoint / 3) * posX, point.y + (int) (distPrevPoint / 3) * posY);
                    sq4 = new Point(point.x, point.y + (int) (distPrevPoint / 3) * posY);
                    tl = isRectCorner(c, b, sq1, sq2, sq3, sq4);

                    //top-right
                } else if (i == 1) {
                    Log.d(TAG, "top-right " + i);
                    posX = -1;
                    posY = 1;
                    sq1 = new Point(point.x + (int) (distPrevPoint / 3) * posX, point.y);
                    sq2 = point;
                    sq3 = new Point(point.x, point.y + (int) (distNextPoint / 3) * posY);
                    sq4 = new Point(point.x + (int) (distPrevPoint / 3) * posX, point.y + (int) (distNextPoint / 3) * posY);
                    tr = isRectCorner(c, b, sq1, sq2, sq3, sq4);

                    // bottom-right
                } else if (i == 2) {
                    Log.d(TAG, "bottom-right " + i);
                    posX = -1;
                    posY = -1;
                    sq1 = new Point(point.x + (int) (distNextPoint / 3) * posX, point.y + (int) (distPrevPoint / 3) * posY);
                    sq2 = new Point(point.x, point.y + (int) (distPrevPoint / 3) * posY);
                    sq3 = point;
                    sq4 = new Point(point.x + (int) (distNextPoint / 3) * posX, point.y);
                    br = isRectCorner(c, b, sq1, sq2, sq3, sq4);

                    // bottom-left
                } else if (i == 3) {
                    Log.d(TAG, "bottom-left " + i);
                    posX = 1;
                    posY = -1;
                    sq1 = new Point(point.x, point.y + (int) (distNextPoint / 3) * posY);
                    sq2 = new Point(point.x + (int) (distPrevPoint / 3) * posX, point.y + (int) (distNextPoint / 3) * posY);
                    sq3 = new Point(point.x + (int) (distPrevPoint / 3) * posX, point.y);
                    sq4 = point;
                    bl = isRectCorner(c, b, sq1, sq2, sq3, sq4);
                }


                if (drawOnBitmap) {
                    drawSquares(c, sq1, sq2, sq3, sq4);
                }

            }

            // top right is detected as not square
            if (tr < tl && tr < br && tr < bl) {
                return -90;
            }

            // top left is detected as not square
            if (tl < tr && tl < br && tl < bl) {
                return 180;
            }

            // bottom right is detected as not square
            if (br < tl && br < tl && br < bl) {
                return 0;
            }

            // bottom left is detected as not square
            if (bl < tl && bl < tl && bl < br) {
                return 90;
            }

            // fallback do not rotate
            return 0;
        } catch (Exception e) {
            return -999;
        }
    }

    /*
     * 1 . . . 2
     * .       .
     * .       .
     * .       .
     * 4 . . . 3
     */
    /*private static int isRectCornerTest(Canvas c, Bitmap b, Point sq1, Point sq2, Point sq3, Point sq4) {

        Paint red = new Paint();
        red.setColor(Color.RED);
        red.setStyle(Paint.Style.FILL);
        red.setStrokeWidth(1);

        String horizontalStr = "";
        int distHorizontal = (int) TwoDimensionalSpace.distTwoPoints(sq1, sq2);
        for (int i = 0; i < distHorizontal; i++) {
            horizontalStr += getPxl(b.getPixel(((sq1.x + sq2.x) / 2), sq1.y + i));
            c.drawPoint(((sq1.x + sq2.x) / 2), sq1.y + i, red);
        }

        String verticalStr = "";
        int distVertical = (int) TwoDimensionalSpace.distTwoPoints(sq1, sq4);
        for (int i = 0; i < distVertical; i++) {
            verticalStr += getPxl(b.getPixel(sq1.x + i, (sq2.y + sq3.y) / 2));
            c.drawPoint(sq1.x + i, (sq2.y + sq3.y) / 2, red);
        }

        String leftTopRightBottomStr = "";
        for (int i = 0; i < distHorizontal; i++) {
            leftTopRightBottomStr += getPxl(b.getPixel(sq1.x + i, sq1.y + i));
            c.drawPoint(sq1.x + i, sq1.y + i, red);
        }

        String rightTopLeftBottomStr = "";
        for (int i = 0; i < distHorizontal; i++) {
            rightTopLeftBottomStr += getPxl(b.getPixel(sq2.x - i, sq2.y - i));
            c.drawPoint(sq2.x - i, sq2.y + i, red);
        }

        int levenshteinDistance = StringUtils.getLevenshteinDistance(horizontalStr, verticalStr);
        int levenshteinDistance2 = StringUtils.getLevenshteinDistance(leftTopRightBottomStr, rightTopLeftBottomStr);
        Log.d(TAG, "vertical: " + verticalStr);
        Log.d(TAG, "horizontal: " + horizontalStr);
        Log.d(TAG, "leftTopRightBottomStr: " + leftTopRightBottomStr);
        Log.d(TAG, "rightTopLeftBottomStr: " + rightTopLeftBottomStr);
        Log.d(TAG, "levenstein: " + levenshteinDistance);
        Log.d(TAG, "levensteinCross: " + levenshteinDistance2);

        return levenshteinDistance + levenshteinDistance2;
    }*/

    /*
     * 1 . . . 2
     * .       .
     * .       .
     * .       .
     * 4 . . . 3
     */
    /*private static boolean isRectCornerTest2(Canvas c, Bitmap b, Point sq1, Point sq2, Point sq3, Point sq4) {

        Paint red = new Paint();
        red.setColor(Color.RED);
        red.setStyle(Paint.Style.FILL);
        red.setStrokeWidth(1);

        String horizontalStr = "";
        String verticalStr = "";
        int leven = 0;
        int distVertical = (int) TwoDimensionalSpace.distTwoPoints(sq1, sq4);
        int distHorizontal = (int) TwoDimensionalSpace.distTwoPoints(sq1, sq2);
        for (int y = 0; y < distVertical; y++) {
            horizontalStr = "";
            for (int x = 0; x < distHorizontal; x++) {
                horizontalStr += getPxl(b.getPixel(sq1.x + x, sq1.y + y));
            }
            String s1 = horizontalStr.substring(0, horizontalStr.length() / 2);
            String s2 = horizontalStr.substring(horizontalStr.length() / 2, horizontalStr.length() - 1);
            Log.d(TAG, "horizontalStr: " + horizontalStr);
            Log.d(TAG, "s1: " + s1);
            Log.d(TAG, "s2: " + s2);
            leven += StringUtils.getLevenshteinDistance(s1, s2);
        }

        for (int x = 0; x < distHorizontal; x++) {
            verticalStr = "";
            for (int y = 0; y < distVertical; y++) {
                verticalStr += getPxl(b.getPixel(sq1.x + x, sq1.y + y));
            }
            String s1 = verticalStr.substring(0, verticalStr.length() / 2);
            String s2 = verticalStr.substring(verticalStr.length() / 2, verticalStr.length() - 1);
            Log.d(TAG, "verticalStr: " + verticalStr);
            Log.d(TAG, "s1: " + s1);
            Log.d(TAG, "s2: " + s2);
            leven += StringUtils.getLevenshteinDistance(s1, s2);
        }

        Log.d(TAG, "---- leven: " + leven + "--------");
        return false;
    }*/

    private static int isRectCorner(Canvas c, Bitmap b, Point sq1, Point sq2, Point sq3, Point sq4) {

        Point center = new Point((sq1.x + sq3.x) / 2, (sq1.y + sq3.y) / 2);

        int sum = 0;
        for (int i = 0; i < 200; i++) {
            String right = getPxl(b.getPixel(center.x + i, center.y));
            String left = getPxl(b.getPixel(center.x, center.y + i));
            String up = getPxl(b.getPixel(center.x - i, center.y));
            String down = getPxl(b.getPixel(center.x, center.y - i));

            if (right.equals("b") && left.equals("b") && up.equals("b") && down.equals("b")) {
                sum = i;
            } else {
                break;
            }
        }

        //Log.d(TAG, "---- MAX: " + sum + "--------");
        return sum;
    }

    private static String getPxl(int pixel) {
        int rColor = Color.red(pixel);
        int gColor = Color.green(pixel);
        int bColor = Color.blue(pixel);

        // Log.d(TAG, "pixel: " + rColor + " " + gColor + " " + bColor + " " + pixel);
        return ((rColor + gColor + bColor) < 300) ? "b" : "w";
    }

    private static void drawSquares(Canvas c, Point sq1, Point sq2, Point sq3, Point sq4) {
        Paint red = new Paint();
        red.setColor(Color.RED);
        red.setStyle(Paint.Style.FILL);
        red.setStrokeWidth(4);
        c.drawLine(sq1.x, sq1.y, sq2.x, sq2.y, red);
        c.drawLine(sq2.x, sq2.y, sq3.x, sq3.y, red);
        c.drawLine(sq3.x, sq3.y, sq4.x, sq4.y, red);
        c.drawLine(sq4.x, sq4.y, sq1.x, sq1.y, red);
    }
}
