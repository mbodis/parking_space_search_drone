package com.parrot.sdksample.logic;

import android.content.Context;
import android.graphics.Point;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.parrot.sdksample.activity.BebopActivity;
import com.parrot.sdksample.drone.BebopDrone;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mbodis on 4/23/17.
 */

public class QrCodeFlyAbove {

    public static final String TAG = QrCodeFlyAbove.class.getName();

    public static final int VIDEO_WIDTH = 640;
    public static final int VIDEO_HEIGHT = 368;

    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = 2;
    public static final int DIRECTION_FORWARD = 3;
    public static final int DIRECTION_BACKWARD = 4;


    // miliseconds that qr code last detected
    private static final long TS_LIMIT_QR_ACTIVE = 100;

    private static final long TS_LEFT_RIGHT_MOVE = 500;
    private static final long TS_LEFT_RIGHT_PAUSE = 1000;
    private static final long TS_FORWARD_BACKWARD_MOVE = 500;
    private static final long TS_FORWARD_BACKWARD_PAUSE = 1000;

    private static final byte SPEED_LEFT_RIGHT = 10;
    private static final byte SPEED_FORWARD_BACKWARD = 10;

    private long lastTsQrCode = 0;
    private Point centerQr;

    boolean isLogicThreadAlive = true;
    Thread logicThread;

    boolean leftRight = false;
    long leftRightEndMoveTs = 0;
    long leftRightEndPauseTs = 0;
    int leftRightDirection = -1;

    boolean forwardBackward = false;
    long forwardBackwardEndOfMoveTs = 0;
    long forwardBackwardEndOfPauseTs = 0;
    int forwardBackwardDirection = -1;

    public QrCodeFlyAbove(final BebopDrone mBebopDrone, final Context ctx) {
        logicThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (isLogicThreadAlive) {

                    executeLeftRightMove(ctx, mBebopDrone);
                    executeForwardBackwardMove(ctx, mBebopDrone);

                    endOfLeftRightMove(ctx, mBebopDrone);
                    endOfForwardBackwardMove(ctx, mBebopDrone);

                    SystemClock.sleep(50);
                }
            }
        });
        logicThread.start();
    }

    private void executeLeftRightMove(Context ctx, BebopDrone mBebopDrone) {
        if (!leftRight && isQrActive()) {
            double centerWidth = (double) centerQr.x / VIDEO_WIDTH * 100;

            if (centerWidth < 40) {
                BebopActivity.addTextLogIntent(ctx, "GO LEFT -> START" + (int) centerWidth);
                mBebopDrone.setRoll((byte) -SPEED_LEFT_RIGHT);
                mBebopDrone.setFlag((byte) 1);
                leftRight = true;
                leftRightEndMoveTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE;
                leftRightEndPauseTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE + TS_LEFT_RIGHT_PAUSE;
                leftRightDirection = DIRECTION_LEFT;

            } else if (centerWidth > 60) {
                BebopActivity.addTextLogIntent(ctx, "GO RIGHT -> START" + (int) centerWidth);
                mBebopDrone.setRoll((byte) SPEED_LEFT_RIGHT);
                mBebopDrone.setFlag((byte) 1);
                leftRight = true;
                leftRightEndMoveTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE;
                leftRightEndPauseTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE + TS_LEFT_RIGHT_PAUSE;
                leftRightDirection = DIRECTION_RIGHT;
            }

        }
    }

    private void endOfLeftRightMove(Context ctx, BebopDrone mBebopDrone) {
        if (leftRight) {
            if (leftRightEndMoveTs > 0) {
                if (System.currentTimeMillis() > leftRightEndMoveTs) {
                    leftRightEndMoveTs = 0;
                    if (leftRightDirection == DIRECTION_LEFT)
                        BebopActivity.addTextLogIntent(ctx, "GO LEFT << STOP");
                    if (leftRightDirection == DIRECTION_RIGHT)
                        BebopActivity.addTextLogIntent(ctx, "GO RIGHT << STOP");
                    mBebopDrone.setRoll((byte) 0);
                    mBebopDrone.setFlag((byte) 0);
                }
            }
            if (System.currentTimeMillis() > leftRightEndPauseTs) {
                leftRightEndPauseTs = 0;
                if (leftRightDirection == DIRECTION_LEFT)
                    BebopActivity.addTextLogIntent(ctx, "GO LEFT << STOP <<");
                if (leftRightDirection == DIRECTION_RIGHT)
                    BebopActivity.addTextLogIntent(ctx, "GO RIGHT << STOP <<");
                leftRight = false;
            }
        }
    }

    private void executeForwardBackwardMove(Context ctx, BebopDrone mBebopDrone) {
        if (!forwardBackward && isQrActive()) {

            double centerHeight = (double) centerQr.y / VIDEO_HEIGHT * 100;
            if (centerHeight > 60) {
                BebopActivity.addTextLogIntent(ctx, "GO BACKWARD -> START" + (int) centerHeight);
                mBebopDrone.setPitch((byte) -SPEED_FORWARD_BACKWARD);
                mBebopDrone.setFlag((byte) 1);
                forwardBackward = true;
                forwardBackwardEndOfMoveTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE;
                forwardBackwardEndOfPauseTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE + TS_FORWARD_BACKWARD_PAUSE;
                forwardBackwardDirection = DIRECTION_BACKWARD;

            } else if (centerHeight < 40) {
                BebopActivity.addTextLogIntent(ctx, "GO FORWARD -> START" + (int) centerHeight);
                mBebopDrone.setPitch((byte) SPEED_FORWARD_BACKWARD);
                mBebopDrone.setFlag((byte) 1);
                forwardBackward = true;
                forwardBackwardEndOfMoveTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE;
                forwardBackwardEndOfPauseTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE + TS_FORWARD_BACKWARD_PAUSE;
                forwardBackwardDirection = DIRECTION_FORWARD;
            }
        }
    }

    private void endOfForwardBackwardMove(Context ctx, BebopDrone mBebopDrone) {
        if (forwardBackward) {
            if (forwardBackwardEndOfMoveTs > 0) {
                if (System.currentTimeMillis() > forwardBackwardEndOfMoveTs) {
                    forwardBackwardEndOfMoveTs = 0;
                    if (forwardBackwardDirection == DIRECTION_FORWARD)
                        BebopActivity.addTextLogIntent(ctx, "GO FORWARD << STOP");
                    if (forwardBackwardDirection == DIRECTION_BACKWARD)
                        BebopActivity.addTextLogIntent(ctx, "GO BACKWARD << STOP");
                    mBebopDrone.setPitch((byte) 0);
                    mBebopDrone.setFlag((byte) 0);
                }
            }
            if (System.currentTimeMillis() > forwardBackwardEndOfPauseTs) {
                forwardBackwardEndOfPauseTs = 0;
                if (forwardBackwardDirection == DIRECTION_FORWARD)
                    BebopActivity.addTextLogIntent(ctx, "GO FORWARD << STOP <<");
                if (forwardBackwardDirection == DIRECTION_BACKWARD)
                    BebopActivity.addTextLogIntent(ctx, "GO BACKWARD << STOP <<");
                forwardBackward = false;
            }
        }
    }

    private boolean isQrActive() {
        return (System.currentTimeMillis() - getLastTsQrCode() < TS_LIMIT_QR_ACTIVE);
    }

    public void destroy() {
        Log.d(TAG, "QrCodeFlyAbove destroying ");
        isLogicThreadAlive = false;
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
