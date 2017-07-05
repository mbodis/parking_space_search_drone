package com.parrot.sdksample.models.time_move.controllers;

import android.content.Context;

import com.parrot.sdksample.activity.BebopActivity;
import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.FlyAboveQrCode;
import com.parrot.sdksample.models.time_move.DroneActionsQueue;
import com.parrot.sdksample.models.time_move.iface.ConditionActionIface;
import com.parrot.sdksample.models.time_move.iface.DroneActionIface;

import static com.parrot.sdksample.models.time_move.iface.MoveActionIface.SPEED_FAST;

/**
 * Created by mbodis on 6/10/17.
 */

public class ConditionActionReadQrCodeInstruction extends ConditionActionIface {

    private static final String TAG = ConditionActionReadQrCodeInstruction.class.getName();

    private static final boolean LOCAL_DEBUG = true;

    public static final int DEFAULT_TIME_TO_READ_NEW_QR_CODE_INSTRUCTION_MILIS = 2*60*1000; // 2 minutes
    public static final String QR_CODE_MOVE_TYPE_FORWARD = "fw";
    public static final String QR_CODE_MOVE_TYPE_BACKWARD = "bw";
    public static final String QR_CODE_MOVE_TYPE_RIGHT_ROTATE = "rr";
    public static final String QR_CODE_MOVE_TYPE_LEFT_ROTATE = "lr";
    public static final String QR_CODE_MOVE_TYPE_LEFT = "l";
    public static final String QR_CODE_MOVE_TYPE_RIGHT = "r";
    public static final String QR_CODE_MOVE_TYPE_UP = "u";
    public static final String QR_CODE_MOVE_TYPE_DOWN = "d";
    public static final String QR_CODE_MOVE_TYPE_LAND = "land";

    private int customTimeForMove = 0;

    public ConditionActionReadQrCodeInstruction() {
        super("ReadQrCodeInstruction", DEFAULT_TIME_TO_READ_NEW_QR_CODE_INSTRUCTION_MILIS);
    }

    /**
     * custom time limit
     */
    public ConditionActionReadQrCodeInstruction(long durationMilis) {
        super("ReadQrCodeInstruction", durationMilis);
    }

    /**
     * custom time limit
     */
    public ConditionActionReadQrCodeInstruction(long durationMilis, int customTimeMove) {
        super("ReadQrCodeInstruction", durationMilis);
        this.customTimeForMove = customTimeMove;
    }

    @Override
    public void executeOnMoveStarts(BebopDrone mBebopDrone, FlyAboveQrCode mFlyAboveQrCode) {
        // lock to qr code
        mFlyAboveQrCode.setLockToQrCodeEnabled();
    }

    @Override
    public void executeOnMoveProcess(BebopDrone mBebopDrone, FlyAboveQrCode mFlyAboveQrCode) {
        // do nothing - lock to qr code in progress
    }

    @Override
    public void executeOnMoveEnds(Context ctx, BebopDrone mBebopDrone, FlyAboveQrCode mFlyAboveQrCode, DroneActionsQueue mDroneActionsQueue) {
        // disable lock to qr code
        mFlyAboveQrCode.setLockToQrCodeDisabled();

        // parse instruction
        String qrCodeMessage = mFlyAboveQrCode.getLandingPatternQrCode().getQrCodeMessage();
        if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "qrCodeMessage: " + qrCodeMessage);
        DroneActionIface nextMove = parseQrCodeMessage(ctx, qrCodeMessage, mDroneActionsQueue);


        if (nextMove != null) {
            if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "nextMove: " + nextMove.getActionName());
            // add new instruction from QrCode to Queue
            mDroneActionsQueue.addNewMove(nextMove);
            // add new instruction to readQrCodeCondition Queue
            mDroneActionsQueue.addNewMove(new ConditionActionReadQrCodeInstruction());
        }
    }

    @Override
    public boolean isConditionSatisfied(BebopDrone mBebopDrone, FlyAboveQrCode mFlyAboveQrCode) {
        return mFlyAboveQrCode.getLandController().isLandingConditionsSatisfied();
    }

    /**
     * message schema: id:[unique-identificator int],[move-type string]:[time-milis int]
     * move-type: fw(foreward), bw(backward), rr(right rotation), lr(left rotation), l(left), r(right), u (up), d (down)
     *
     * message example: id:1,fw:2000
     * @param message
     * @return
     */
    private DroneActionIface parseQrCodeMessage(Context ctx, String message, DroneActionsQueue mDroneActionsQueue){

        if (message != null){
            if (message.contains(",") && message.contains(":")){
                String arr[] = message.split(",");
                if (arr.length ==2){
                    String idArr[] = arr[0].split(":");
                    String moveArr[] = arr[1].split(":");

                    if (idArr.length == 2 && moveArr.length == 2){
                        int id = Integer.parseInt(idArr[1]);
                        if (mDroneActionsQueue.getLastQrCodeId() < id){
                            mDroneActionsQueue.setLastQrCodeId(id);
                        }else{
                            return null;
                        }

                        String moveType = moveArr[0];
                        int timeMilis = Integer.parseInt(moveArr[1]);
                        if (this.customTimeForMove != 0){
                            timeMilis = this.customTimeForMove;
                        }

                        if (moveType.equals(QR_CODE_MOVE_TYPE_FORWARD)){
                            return new DroneMoveForward(SPEED_FAST, timeMilis);
                        }else if (moveType.equals(QR_CODE_MOVE_TYPE_BACKWARD)){
                            return new DroneMoveBackward(SPEED_FAST, timeMilis);
                        }else if (moveType.equals(QR_CODE_MOVE_TYPE_RIGHT_ROTATE)){
                            return new DroneMoveRotateRight(SPEED_FAST, timeMilis);
                        }else if (moveType.equals(QR_CODE_MOVE_TYPE_LEFT_ROTATE)){
                            return new DroneMoveRotateLeft(SPEED_FAST, timeMilis);
                        }else if (moveType.equals(QR_CODE_MOVE_TYPE_LEFT)){
                            return new DroneMoveLeft(SPEED_FAST, timeMilis);
                        }else if (moveType.equals(QR_CODE_MOVE_TYPE_RIGHT)){
                            return new DroneMoveRight(SPEED_FAST, timeMilis);
                        }else if (moveType.equals(QR_CODE_MOVE_TYPE_UP)){
                            return new DroneMoveUp(SPEED_FAST, timeMilis);
                        }else if (moveType.equals(QR_CODE_MOVE_TYPE_DOWN)){
                            return new DroneMoveDown(SPEED_FAST, timeMilis);

                        }else if (moveType.equals(QR_CODE_MOVE_TYPE_LAND)){
                            return new SimpleActionLand();
                        }

                    }
                }
            }
        }

        if (LOCAL_DEBUG) BebopActivity.addTextLogIntent(ctx, "invalid message: " + message);

        return null;
    }
}
