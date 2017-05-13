package com.parrot.sdksample.logic;

import android.content.Context;
import android.graphics.Point;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.parrot.sdksample.activity.BebopActivity;
import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.utils.TwoDimensionalSpace;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;

import static com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING;
import static com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING;

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
    public static final int DIRECTION_CLOCKWISE = 5;
    public static final int DIRECTION_UNCLOCKWISE = 6;
    public static final int DIRECTION_UP = 7;
    public static final int DIRECTION_DOWN = 8;


    // miliseconds that qr code last detected
    private static final long TS_LIMIT_QR_ACTIVE = 100;
    private static final long TS_LIMIT_QR_MISSING_SEARCH = 500;
    private static final long TS_COMMON_MOVE = 500;
    private static final long TS_COMMON_PAUSE = 1000;

    private static final long TS_LEFT_RIGHT_MOVE = TS_COMMON_MOVE;
    private static final long TS_LEFT_RIGHT_PAUSE = TS_COMMON_PAUSE;
    private static final long TS_FORWARD_BACKWARD_MOVE = TS_COMMON_MOVE;
    private static final long TS_FORWARD_BACKWARD_PAUSE = TS_COMMON_PAUSE;
    private static final long TS_ROTATE_MOVE = TS_COMMON_MOVE;
    private static final long TS_ROTATE_PAUSE = TS_COMMON_PAUSE;
    private static final long TS_UP_DOWN_MOVE = TS_COMMON_MOVE;
    private static final long TS_UP_DOWN_PAUSE = TS_COMMON_PAUSE;

    private static final byte SPEED_LEFT_RIGHT = 10;
    private static final byte SPEED_LEFT_RIGHT_SLOW = 10;
    private static final byte SPEED_FORWARD_BACKWARD = 10;
    private static final byte SPEED_FORWARD_BACKWARD_SLOW = 5;
    private static final byte SPEED_ROTATION = 10;
    private static final byte SPEED_ROTATION_SLOW = 5;
    private static final byte SPEED_UP_DOWN = 5;
    private static final byte SPEED_UP_DOWN_SLOW = 3;


    private static final int LEFT_RIGHT_LIMIT_PERCENTAGE_TOP = 40;
    private static final int LEFT_RIGHT_LIMIT_PERCENTAGE_CENTER = 50;
    private static final int LEFT_RIGHT_LIMIT_PERCENTAGE_BOTTOM = 60;

    private static final int FORWARD_BACKWARD_LIMIT_PERCENTAGE_TOP = 70;
    private static final int FORWARD_BACKWARD_LIMIT_PERCENTAGE_CENTER = 80;
    private static final int FORWARD_BACKWARD_LIMIT_PERCENTAGE_BOTTOM = 90;

    private static final int ROTATE_LIMIT_PERCENTAGE_HIGH = 20;
    private static final int ROTATE_LIMIT_PERCENTAGE_CENTER = 0;

    private static final int UP_DOWN_WIDTH_OPTIMAL = 130;
    private static final int UP_DOWN_WIDTH_TOO_HIGH = 90;


    private long lastTsQrCode = 0;
    private Point centerQr = null;
    private Point[] qrCodePoints;

    boolean isLogicThreadAlive = true;
    private boolean landToQrCodeEnabled = true; // TODO
    boolean hasLanded = false;
    Thread logicThread;

    // controller
    boolean upDown = false;
    long upDownEndMoveTs = 0;
    long upDownEndPauseTs = 0;
    int upDownDirection = -1;

    // controller left/right
    boolean leftRight = false;
    long leftRightEndMoveTs = 0;
    long leftRightEndPauseTs = 0;
    int leftRightDirection = -1;

    // controller forward/backward
    boolean forwardBackward = false;
    long forwardBackwardEndOfMoveTs = 0;
    long forwardBackwardEndOfPauseTs = 0;
    int forwardBackwardDirection = -1;

    // controller rotation left/right
    boolean rotate = false;
    long rotateEndMoveTs = 0;
    long rotateEndPauseTs = 0;
    int rotateDirection = -1;


    public QrCodeFlyAbove(final BebopDrone mBebopDrone, final Context ctx) {
        logicThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (isLogicThreadAlive) {

                    if (landToQrCodeEnabled && !hasLanded) {

                        // TODO search for code

                        qrCodeMissed(ctx, mBebopDrone);

                        executeUpDown(ctx, mBebopDrone);
                        executeRotateMove(ctx, mBebopDrone);
                        executeLeftRightMove(ctx, mBebopDrone);
                        executeForwardBackwardMove(ctx, mBebopDrone);

                        endOfUpDownMove(ctx, mBebopDrone);
                        endOfRotateMove(ctx, mBebopDrone);
                        endOfLeftRightMove(ctx, mBebopDrone);
                        endOfForwardBackwardMove(ctx, mBebopDrone);

                        land(ctx, mBebopDrone);
                    }

                    SystemClock.sleep(50);
                }
            }
        });
        logicThread.start();
    }

    /*
     * search for last position of QR code too much forward / backward
     *
     */
    private void qrCodeMissed(Context ctx, BebopDrone mBebopDrone) {
        if ((lastTsQrCode != 0) && (!isQrActive()) && (centerQr != null)) {
            if ((System.currentTimeMillis() - lastTsQrCode) > TS_LIMIT_QR_MISSING_SEARCH) {
                if ((System.currentTimeMillis() - lastTsQrCode) < 2 * TS_LIMIT_QR_MISSING_SEARCH) {

                    double centerWidth = (double) centerQr.x / VIDEO_WIDTH * 100;

                    // the qr code should be somewhere in middle
                    if (centerWidth > 30 && centerWidth < 70) {

                        double centerHeight = (double) centerQr.y / VIDEO_HEIGHT * 100;

                        if (!forwardBackward) {
                            // qr code was last time in front, drone moved too backward, move forward to see QR code
                            if (centerHeight < 30) {
                                BebopActivity.addTextLogIntent(ctx, "go forward QR missing -> start ");
                                mBebopDrone.setPitch((byte) SPEED_FORWARD_BACKWARD);
                                mBebopDrone.setFlag((byte) 1);
                                forwardBackward = true;
                                forwardBackwardEndOfMoveTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE;
                                forwardBackwardEndOfPauseTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE + TS_FORWARD_BACKWARD_PAUSE;
                                forwardBackwardDirection = DIRECTION_FORWARD;

                                // qr code was last time in back, drone moved too forward, move backward to see QR code
                            } else if (centerHeight > 70) {
                                BebopActivity.addTextLogIntent(ctx, "go backward QR missing -> start ");
                                mBebopDrone.setPitch((byte) -SPEED_FORWARD_BACKWARD);
                                mBebopDrone.setFlag((byte) 1);
                                forwardBackward = true;
                                forwardBackwardEndOfMoveTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE;
                                forwardBackwardEndOfPauseTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE + TS_FORWARD_BACKWARD_PAUSE;
                                forwardBackwardDirection = DIRECTION_BACKWARD;
                            }
                        }
                    }
                }
            }
        }
    }

    private void executeUpDown(Context ctx, BebopDrone mBebopDrone) {
        if (!upDown && isQrActive() && qrCodePoints != null && qrCodePoints.length == 4) {
            //TODO use percentage qr/screen width
            double verticalShiftPx = TwoDimensionalSpace.distTwoPoints(qrCodePoints[0], qrCodePoints[1]);

            if (verticalShiftPx < UP_DOWN_WIDTH_TOO_HIGH) {
                BebopActivity.addTextLogIntent(ctx, "move down -> start " + (int) verticalShiftPx);
                mBebopDrone.setGaz((byte) -SPEED_UP_DOWN);
                upDown = true;
                upDownEndMoveTs = System.currentTimeMillis() + TS_UP_DOWN_MOVE;
                upDownEndPauseTs = System.currentTimeMillis() + TS_UP_DOWN_MOVE + TS_UP_DOWN_PAUSE;
                upDownDirection = DIRECTION_DOWN;

            } else if (verticalShiftPx > UP_DOWN_WIDTH_OPTIMAL) {
                BebopActivity.addTextLogIntent(ctx, "move up -> start slow " + (int) verticalShiftPx);
                mBebopDrone.setGaz((byte) SPEED_UP_DOWN_SLOW);
                upDown = true;
                upDownEndMoveTs = System.currentTimeMillis() + TS_UP_DOWN_MOVE;
                upDownEndPauseTs = System.currentTimeMillis() + TS_UP_DOWN_MOVE + TS_UP_DOWN_PAUSE;
                upDownDirection = DIRECTION_UP;

            } else if (verticalShiftPx < UP_DOWN_WIDTH_OPTIMAL) {
                BebopActivity.addTextLogIntent(ctx, "move down -> start slow " + (int) verticalShiftPx);
                mBebopDrone.setGaz((byte) -SPEED_UP_DOWN_SLOW);
                upDown = true;
                upDownEndMoveTs = System.currentTimeMillis() + TS_UP_DOWN_MOVE;
                upDownEndPauseTs = System.currentTimeMillis() + TS_UP_DOWN_MOVE + TS_UP_DOWN_PAUSE;
                upDownDirection = DIRECTION_DOWN;

            }
        }
    }

    private void endOfUpDownMove(Context ctx, BebopDrone mBebopDrone) {
        if (upDown) {
            if (upDownEndMoveTs > 0) {
                if (System.currentTimeMillis() > upDownEndMoveTs) {
                    upDownEndMoveTs = 0;
                    if (upDownDirection == DIRECTION_UP)
                        BebopActivity.addTextLogIntent(ctx, "move up << stop");
                    if (upDownDirection == DIRECTION_DOWN)
                        BebopActivity.addTextLogIntent(ctx, "move down << stop");
                    mBebopDrone.setGaz((byte) 0);
                }
            }
            if (System.currentTimeMillis() > upDownEndPauseTs) {
                upDownEndPauseTs = 0;
                if (upDownDirection == DIRECTION_UP)
                    BebopActivity.addTextLogIntent(ctx, "move up << stop pause");
                if (upDownDirection == DIRECTION_DOWN)
                    BebopActivity.addTextLogIntent(ctx, "move down << stop pause");
                upDown = false;
            }
        }
    }

    private void executeRotateMove(Context ctx, BebopDrone mBebopDrone) {
        if (!rotate && isQrActive() && qrCodePoints != null && qrCodePoints.length == 4) {
            int horizontalShiftPx = qrCodePoints[0].x - qrCodePoints[3].x;

            if (horizontalShiftPx > ROTATE_LIMIT_PERCENTAGE_HIGH) {
                BebopActivity.addTextLogIntent(ctx, "rotate clockwise -> start " + horizontalShiftPx);
                mBebopDrone.setYaw((byte) SPEED_ROTATION);
                rotate = true;
                rotateEndMoveTs = System.currentTimeMillis() + TS_ROTATE_MOVE;
                rotateEndPauseTs = System.currentTimeMillis() + TS_ROTATE_MOVE + TS_ROTATE_PAUSE;
                rotateDirection = DIRECTION_CLOCKWISE;

            } else if (horizontalShiftPx < -ROTATE_LIMIT_PERCENTAGE_HIGH) {
                BebopActivity.addTextLogIntent(ctx, "rotate unclockwise -> start " + horizontalShiftPx);
                mBebopDrone.setYaw((byte) -SPEED_ROTATION);
                rotate = true;
                rotateEndMoveTs = System.currentTimeMillis() + TS_ROTATE_MOVE;
                rotateEndPauseTs = System.currentTimeMillis() + TS_ROTATE_MOVE + TS_ROTATE_PAUSE;
                rotateDirection = DIRECTION_UNCLOCKWISE;

            } else if (horizontalShiftPx > ROTATE_LIMIT_PERCENTAGE_CENTER) {
                BebopActivity.addTextLogIntent(ctx, "rotate clockwise -> start slow " + horizontalShiftPx);
                mBebopDrone.setYaw((byte) SPEED_ROTATION_SLOW);
                rotate = true;
                rotateEndMoveTs = System.currentTimeMillis() + TS_ROTATE_MOVE;
                rotateEndPauseTs = System.currentTimeMillis() + TS_ROTATE_MOVE + TS_ROTATE_PAUSE;
                rotateDirection = DIRECTION_CLOCKWISE;


            } else if (horizontalShiftPx < ROTATE_LIMIT_PERCENTAGE_CENTER) {
                BebopActivity.addTextLogIntent(ctx, "rotate unclockwise -> start slow " + horizontalShiftPx);
                mBebopDrone.setYaw((byte) -SPEED_ROTATION_SLOW);
                rotate = true;
                rotateEndMoveTs = System.currentTimeMillis() + TS_ROTATE_MOVE;
                rotateEndPauseTs = System.currentTimeMillis() + TS_ROTATE_MOVE + TS_ROTATE_PAUSE;
                rotateDirection = DIRECTION_UNCLOCKWISE;

            }

        }
    }

    private void endOfRotateMove(Context ctx, BebopDrone mBebopDrone) {
        if (rotate) {
            if (rotateEndMoveTs > 0) {
                if (System.currentTimeMillis() > rotateEndMoveTs) {
                    rotateEndMoveTs = 0;
                    if (rotateDirection == DIRECTION_CLOCKWISE)
                        BebopActivity.addTextLogIntent(ctx, "move clockwise << stop");
                    if (rotateDirection == DIRECTION_UNCLOCKWISE)
                        BebopActivity.addTextLogIntent(ctx, "move unclockwise << stop");
                    mBebopDrone.setYaw((byte) 0);
                }
            }
            if (System.currentTimeMillis() > rotateEndPauseTs) {
                rotateEndPauseTs = 0;
                if (rotateDirection == DIRECTION_CLOCKWISE)
                    BebopActivity.addTextLogIntent(ctx, "move clockwise << stop pause");
                if (rotateDirection == DIRECTION_UNCLOCKWISE)
                    BebopActivity.addTextLogIntent(ctx, "move unclockwise << stop pause");
                rotate = false;
            }
        }
    }

    private void executeLeftRightMove(Context ctx, BebopDrone mBebopDrone) {
        if (!leftRight && isQrActive()) {
            double centerWidth = (double) centerQr.x / VIDEO_WIDTH * 100;

            if (centerWidth < LEFT_RIGHT_LIMIT_PERCENTAGE_TOP) {
                BebopActivity.addTextLogIntent(ctx, "move left -> start " + (int) centerWidth);
                mBebopDrone.setRoll((byte) -SPEED_LEFT_RIGHT);
                mBebopDrone.setFlag((byte) 1);
                leftRight = true;
                leftRightEndMoveTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE;
                leftRightEndPauseTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE + TS_LEFT_RIGHT_PAUSE;
                leftRightDirection = DIRECTION_LEFT;

            } else if (centerWidth > LEFT_RIGHT_LIMIT_PERCENTAGE_BOTTOM) {
                BebopActivity.addTextLogIntent(ctx, "move right -> start " + (int) centerWidth);
                mBebopDrone.setRoll((byte) SPEED_LEFT_RIGHT);
                mBebopDrone.setFlag((byte) 1);
                leftRight = true;
                leftRightEndMoveTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE;
                leftRightEndPauseTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE + TS_LEFT_RIGHT_PAUSE;
                leftRightDirection = DIRECTION_RIGHT;

            } else if (centerWidth > LEFT_RIGHT_LIMIT_PERCENTAGE_CENTER) {
                BebopActivity.addTextLogIntent(ctx, "move right -> start slow " + (int) centerWidth);
                mBebopDrone.setRoll((byte) SPEED_LEFT_RIGHT_SLOW);
                mBebopDrone.setFlag((byte) 1);
                leftRight = true;
                leftRightEndMoveTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE;
                leftRightEndPauseTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE + TS_LEFT_RIGHT_PAUSE;
                leftRightDirection = DIRECTION_RIGHT;


            } else if (centerWidth < LEFT_RIGHT_LIMIT_PERCENTAGE_CENTER) {
                BebopActivity.addTextLogIntent(ctx, "move left -> start slow " + (int) centerWidth);
                mBebopDrone.setRoll((byte) -SPEED_LEFT_RIGHT_SLOW);
                mBebopDrone.setFlag((byte) 1);
                leftRight = true;
                leftRightEndMoveTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE;
                leftRightEndPauseTs = System.currentTimeMillis() + TS_LEFT_RIGHT_MOVE + TS_LEFT_RIGHT_PAUSE;
                leftRightDirection = DIRECTION_LEFT;

            }

        }
    }

    private void endOfLeftRightMove(Context ctx, BebopDrone mBebopDrone) {
        if (leftRight) {
            if (leftRightEndMoveTs > 0) {
                if (System.currentTimeMillis() > leftRightEndMoveTs) {
                    leftRightEndMoveTs = 0;
                    if (leftRightDirection == DIRECTION_LEFT)
                        BebopActivity.addTextLogIntent(ctx, "move left << stop");
                    if (leftRightDirection == DIRECTION_RIGHT)
                        BebopActivity.addTextLogIntent(ctx, "move right << stop");
                    mBebopDrone.setRoll((byte) 0);
                    mBebopDrone.setFlag((byte) 0);
                }
            }
            if (System.currentTimeMillis() > leftRightEndPauseTs) {
                leftRightEndPauseTs = 0;
                if (leftRightDirection == DIRECTION_LEFT)
                    BebopActivity.addTextLogIntent(ctx, "move left << stop pause");
                if (leftRightDirection == DIRECTION_RIGHT)
                    BebopActivity.addTextLogIntent(ctx, "move right << stop pause");
                leftRight = false;
            }
        }
    }

    private void executeForwardBackwardMove(Context ctx, BebopDrone mBebopDrone) {
        if (!forwardBackward && isQrActive()) {

            double centerHeight = (double) centerQr.y / VIDEO_HEIGHT * 100;
            if (centerHeight < FORWARD_BACKWARD_LIMIT_PERCENTAGE_TOP) {
                BebopActivity.addTextLogIntent(ctx, "move forward -> start " + (int) centerHeight);
                mBebopDrone.setPitch((byte) SPEED_FORWARD_BACKWARD);
                mBebopDrone.setFlag((byte) 1);
                forwardBackward = true;
                forwardBackwardEndOfMoveTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE;
                forwardBackwardEndOfPauseTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE + TS_FORWARD_BACKWARD_PAUSE;
                forwardBackwardDirection = DIRECTION_FORWARD;

            } else if (centerHeight > FORWARD_BACKWARD_LIMIT_PERCENTAGE_BOTTOM) {
                BebopActivity.addTextLogIntent(ctx, "move backward -> start " + (int) centerHeight);
                mBebopDrone.setPitch((byte) -SPEED_FORWARD_BACKWARD);
                mBebopDrone.setFlag((byte) 1);
                forwardBackward = true;
                forwardBackwardEndOfMoveTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE;
                forwardBackwardEndOfPauseTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE + TS_FORWARD_BACKWARD_PAUSE;
                forwardBackwardDirection = DIRECTION_BACKWARD;

            } else if (centerHeight < FORWARD_BACKWARD_LIMIT_PERCENTAGE_CENTER) {
                BebopActivity.addTextLogIntent(ctx, "move forward -> start slow " + (int) centerHeight);
                mBebopDrone.setPitch((byte) SPEED_FORWARD_BACKWARD_SLOW);
                mBebopDrone.setFlag((byte) 1);
                forwardBackward = true;
                forwardBackwardEndOfMoveTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE;
                forwardBackwardEndOfPauseTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE + TS_FORWARD_BACKWARD_PAUSE;
                forwardBackwardDirection = DIRECTION_FORWARD;

            } else if (centerHeight > FORWARD_BACKWARD_LIMIT_PERCENTAGE_CENTER) {
                BebopActivity.addTextLogIntent(ctx, "move backward -> start slow " + (int) centerHeight);
                mBebopDrone.setPitch((byte) -SPEED_FORWARD_BACKWARD_SLOW);
                mBebopDrone.setFlag((byte) 1);
                forwardBackward = true;
                forwardBackwardEndOfMoveTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE;
                forwardBackwardEndOfPauseTs = System.currentTimeMillis() + TS_FORWARD_BACKWARD_MOVE + TS_FORWARD_BACKWARD_PAUSE;
                forwardBackwardDirection = DIRECTION_BACKWARD;
            }
        }
    }

    private void endOfForwardBackwardMove(Context ctx, BebopDrone mBebopDrone) {
        if (forwardBackward) {
            if (forwardBackwardEndOfMoveTs > 0) {
                if (System.currentTimeMillis() > forwardBackwardEndOfMoveTs) {
                    forwardBackwardEndOfMoveTs = 0;
                    if (forwardBackwardDirection == DIRECTION_FORWARD)
                        BebopActivity.addTextLogIntent(ctx, "move forward << stop");
                    if (forwardBackwardDirection == DIRECTION_BACKWARD)
                        BebopActivity.addTextLogIntent(ctx, "move backward << stop");
                    mBebopDrone.setPitch((byte) 0);
                    mBebopDrone.setFlag((byte) 0);
                }
            }
            if (System.currentTimeMillis() > forwardBackwardEndOfPauseTs) {
                forwardBackwardEndOfPauseTs = 0;
                if (forwardBackwardDirection == DIRECTION_FORWARD)
                    BebopActivity.addTextLogIntent(ctx, "move forward << stop pause");
                if (forwardBackwardDirection == DIRECTION_BACKWARD)
                    BebopActivity.addTextLogIntent(ctx, "move backward << stop pause");
                forwardBackward = false;
            }
        }
    }

    private void land(Context ctx, BebopDrone mBebopDrone) {
        if (centerQr == null) return;
        if (qrCodePoints == null) return;

        byte LIMIT_ERROR_CENTER = 2; // 2 percentage
        byte LIMIT_VERTICAL_ERROR = 25; // 5 pixel detected QRcode width

        double centerHeight = (double) centerQr.y / VIDEO_HEIGHT * 100;
        double centerWidth = (double) centerQr.x / VIDEO_WIDTH * 100;
        int horizontalShiftPx = qrCodePoints[0].x - qrCodePoints[3].x;
        double verticalShiftPx = TwoDimensionalSpace.distTwoPoints(qrCodePoints[0], qrCodePoints[1]);

        boolean landWidth = false;
        boolean landHeight = false;
        boolean landRotation = false;
        boolean landVertical = false;

        if (Math.abs(centerWidth - LEFT_RIGHT_LIMIT_PERCENTAGE_CENTER) < LIMIT_ERROR_CENTER) {
            landWidth = true;
        }
        if (Math.abs(centerHeight - FORWARD_BACKWARD_LIMIT_PERCENTAGE_CENTER) < LIMIT_ERROR_CENTER) {
            landHeight = true;
        }
        if (horizontalShiftPx < LIMIT_ERROR_CENTER) {
            landRotation = true;
        }
        if (Math.abs(verticalShiftPx - UP_DOWN_WIDTH_OPTIMAL) < LIMIT_VERTICAL_ERROR) {
            landVertical = true;
        }

        // qr code ir right below drone
        if (landWidth && landHeight && landRotation && landVertical) {
            if (mBebopDrone.getFlyingState() == ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING
                    || mBebopDrone.getFlyingState() == ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING) {
                mBebopDrone.land();
                hasLanded = true;
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

    public void setLandingToQrCodeEnabled(boolean isLandingToQrCodeEnabled) {
        this.landToQrCodeEnabled = isLandingToQrCodeEnabled;
        this.hasLanded = false;
    }

    public boolean getLandingToQrCodeEnabled() {
        return this.landToQrCodeEnabled;
    }

    public void setPointsCenterQr(Point[] qrCodePoints) {
        this.qrCodePoints = qrCodePoints;
    }

}
