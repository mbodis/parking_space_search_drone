package com.parrot.sdksample.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by mbodis on 4/22/17.
 */

public class DrawView extends View{

    private static final String TAG = DrawView.class.getName();
    public static final int TIME_DISPLAY_QR_CODE = 2 * 1000; // 2 sec

    Paint paint = new Paint();
    private Point[] qrCodePoints = new Point[]{};
    private long pointsTs = 0;

    Bitmap b;

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context) {
        super(context);
        init();
    }

    private void init(){
        paint.setColor(Color.RED);
        paint.setStrokeWidth(15);
        b = Bitmap.createBitmap(640, 368, Bitmap.Config.ARGB_8888);
    }

    public void setBitmap(Bitmap b){
        Log.d(TAG, "setBitmap");
        this.b = b.copy(b.getConfig(), true);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw");

        if (qrCodePoints.length > 0){

            /** bitmap version **/
//            Canvas c = new Canvas(b);
//            c.drawLine(qrCodePoints[0].x, qrCodePoints[0].y, qrCodePoints[1].x, qrCodePoints[1].y, paint);
//            c.drawLine(qrCodePoints[1].x, qrCodePoints[1].y, qrCodePoints[2].x, qrCodePoints[2].y, paint);
//            c.drawLine(qrCodePoints[2].x, qrCodePoints[2].y, qrCodePoints[3].x, qrCodePoints[3].y, paint);
//            c.drawLine(qrCodePoints[3].x, qrCodePoints[3].y, qrCodePoints[0].x, qrCodePoints[0].y, paint);
//            Rect src = new Rect(0,0,b.getWidth()-1, b.getHeight()-1);
//            Rect dest = new Rect(0,0,canvas.getWidth()-1, canvas.getHeight()-1);
//            canvas.drawBitmap(b, src, dest, null);

            /** draw lines **/
            double xScale = (double)canvas.getWidth() / MyBebopVideoView.VIDEO_WIDTH;
            double yScale = (double)canvas.getHeight() / MyBebopVideoView.VIDEO_HEIGHT;
            canvas.drawLine((int)(qrCodePoints[0].x*xScale), (int)(qrCodePoints[0].y*yScale), (int)(qrCodePoints[1].x*xScale), (int)(qrCodePoints[1].y*yScale), paint);
            canvas.drawLine((int)(qrCodePoints[1].x*xScale), (int)(qrCodePoints[1].y*yScale), (int)(qrCodePoints[2].x*xScale), (int)(qrCodePoints[2].y*yScale), paint);
            canvas.drawLine((int)(qrCodePoints[2].x*xScale), (int)(qrCodePoints[2].y*yScale), (int)(qrCodePoints[3].x*xScale), (int)(qrCodePoints[3].y*yScale), paint);
            canvas.drawLine((int)(qrCodePoints[3].x*xScale), (int)(qrCodePoints[3].y*yScale), (int)(qrCodePoints[0].x*xScale), (int)(qrCodePoints[0].y*yScale), paint);
        }

        //Log.d(TAG, "-------------------------");
    }

    public void toggleDrawView(){
        if (System.currentTimeMillis() - pointsTs < TIME_DISPLAY_QR_CODE) {
            if (getVisibility() == View.GONE){
                setVisibility(View.VISIBLE);
                Log.d(TAG, "View.VISIBLE -- > ");
            }
        }else{
            if (getVisibility() == View.VISIBLE){
                setVisibility(View.GONE);
                Log.d(TAG, "View.GONE -- > ");
            }
        }
    }

    public Point[] getQrCodePoints() {
        return qrCodePoints;
    }

    public void setQrCodePoints(Point[] qrCodePoints) {
        this.qrCodePoints = qrCodePoints;
    }

    public long getPointsTs() {
        return pointsTs;
    }

    public void setPointsTs(long pointsTs) {
        this.pointsTs = pointsTs;
    }
}
