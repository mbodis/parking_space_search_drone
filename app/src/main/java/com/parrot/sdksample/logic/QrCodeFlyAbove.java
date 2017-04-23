package com.parrot.sdksample.logic;

import android.graphics.Point;
import android.os.SystemClock;
import android.util.Log;

import com.parrot.sdksample.drone.BebopDrone;

/**
 * Created by mbodis on 4/23/17.
 */

public class QrCodeFlyAbove {

    public static final String TAG = QrCodeFlyAbove.class.getName();

    public static final int VIDEO_WIDTH = 640;
    public static final int VIDEO_HEIGHT = 368;

    private static final long TS_LIMIT_QR_ACTIVE = 2 * 1000;

    private long lastTsQrCode = 0;
    private Point centerQr;

    boolean isAlive = true;
    Thread logicThread;

    public QrCodeFlyAbove(final BebopDrone mBebopDrone) {
        logicThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while(isAlive){

                    boolean moveExecuted = false;
                    if (centerQr != null && isQrActive()){
                        double centerWidth = (double)centerQr.x / VIDEO_WIDTH * 100;

                        if (centerWidth < 40){
                            Log.d(TAG, "IS TOO LEFT -> go right" + centerWidth);
                            moveExecuted = true;
//                            mBebopDrone.setRoll((byte) 50);
//                            mBebopDrone.setFlag((byte) 1);
//                            mBebopDrone.setRoll((byte) 0);
//                            mBebopDrone.setFlag((byte) 0);

                        }else if (centerWidth > 60){
                            Log.d(TAG, "IS TOO RIGHT -> go left" + centerWidth);
                            moveExecuted = true;
//                            mBebopDrone.setRoll((byte) -50);
//                            mBebopDrone.setFlag((byte) 1);
//                            mBebopDrone.setRoll((byte) 0);
//                            mBebopDrone.setFlag((byte) 0);

                        }else{
                            Log.d(TAG, "HORIZONTAL OK " + centerWidth);
                        }

                        double centerHeight = (double)centerQr.y / VIDEO_HEIGHT * 100;
                        if (centerHeight < 40){
                            Log.d(TAG, "IS TOO FORWARD -> go back" + centerHeight);
                            moveExecuted = true;
//                            mBebopDrone.setPitch((byte) -50);
//                            mBebopDrone.setFlag((byte) 1);
//                            mBebopDrone.setPitch((byte) 0);
//                            mBebopDrone.setFlag((byte) 0);

                        }else if (centerHeight > 60){
                            Log.d(TAG, "IS TOO BACKWARD -> go forward" + centerHeight);
                            moveExecuted = true;
//                            mBebopDrone.setPitch((byte) 50);
//                            mBebopDrone.setFlag((byte) 1);
//                            mBebopDrone.setPitch((byte) 0);
//                            mBebopDrone.setFlag((byte) 0);


                        }else{
                            Log.d(TAG, "VERTICAL OK " + centerHeight);
                        }
                    }

                    if (moveExecuted){
                        SystemClock.sleep(1000);
                    }else{
                        SystemClock.sleep(100);
                    }

                }
            }
        });
        logicThread.start();
    }

    private boolean isQrActive(){
        return (System.currentTimeMillis() - getLastTsQrCode() < TS_LIMIT_QR_ACTIVE);
    }

    public void destroy(){
        Log.d(TAG, "QrCodeFlyAbove destroying ");
        isAlive = false;
    }

    public long getLastTsQrCode() {
        return lastTsQrCode;
    }

    public void setLastTsQrCode(long lastTsQrCode) {
        //Log.d(TAG, "QrCodeFlyAbove setLastTsQrCode");
        this.lastTsQrCode = lastTsQrCode;
    }

    public Point getCenterQr() {
        return centerQr;
    }

    public void setCenterQr(Point centerQr) {
        //Log.d(TAG, "QrCodeFlyAbove setCenterQr");
        this.centerQr = new Point(centerQr.x, centerQr.y);
    }
}
