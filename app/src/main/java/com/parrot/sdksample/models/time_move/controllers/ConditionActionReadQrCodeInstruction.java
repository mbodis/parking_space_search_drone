package com.parrot.sdksample.models.time_move.controllers;

import android.content.Intent;
import android.util.Log;

import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.FlyAboveQrCode;
import com.parrot.sdksample.models.time_move.DroneActionsQueue;
import com.parrot.sdksample.models.time_move.iface.ConditionActionIface;
import com.parrot.sdksample.models.time_move.iface.DroneActionIface;

/**
 * Created by mbodis on 6/10/17.
 */

public class ConditionActionReadQrCodeInstruction extends ConditionActionIface {

    private static final String TAG = ConditionActionReadQrCodeInstruction.class.getName();

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


    public ConditionActionReadQrCodeInstruction() {
        super("ReadQrCodeInstruction", DEFAULT_TIME_TO_READ_NEW_QR_CODE_INSTRUCTION_MILIS);
    }

    // custom time limit
    public ConditionActionReadQrCodeInstruction(long durationMilis) {
        super("ReadQrCodeInstruction", durationMilis);
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
    public void executeOnMoveEnds(BebopDrone mBebopDrone, FlyAboveQrCode mFlyAboveQrCode, DroneActionsQueue mDroneActionsQueue) {
        // disable lock to qr code
        mFlyAboveQrCode.setLockToQrCodeDisabled();

        // parse instruction
        String qrCodeMessage = mFlyAboveQrCode.getLandingPatternQrCode().getQrCodeMessage();
        DroneActionIface nextMove = parseQrCodeMessage(qrCodeMessage, mDroneActionsQueue);

        if (nextMove != null) {
            // add new instruction from QrCode to Queue
            mDroneActionsQueue.addNewMove(nextMove);
            // add new instruction to readQrCodeCondition Queue
            mDroneActionsQueue.addNewMove(new ConditionActionReadQrCodeInstruction());
        }
    }

    @Override
    public boolean isConditionSatisfied(BebopDrone mBebopDrone, FlyAboveQrCode mFlyAboveQrCode) {
        mFlyAboveQrCode.mLandController.isLandingConditionsSatisfied();
        return false;
    }

    /**
     * message schema: id:[unique-identificator int],[move-type string]:[time-milis int]
     * move-type: fw(foreward), bw(backward), rr(right rotation), lr(left rotation), l(left), r(right), u (up), d (down)
     *
     * message example: id:1,move:forward
     * @param message
     * @return
     */
    private DroneActionIface parseQrCodeMessage(String message, DroneActionsQueue mDroneActionsQueue){

        if (message != null){
            if (message.contains(":") && message.contains(":")){
                String arr[] = message.split(",");
                if (arr.length ==2){
                    String idArr[] = arr[0].split(":");
                    String moveArr[] = arr[0].split(":");

                    if (idArr.length == 2 && moveArr.length == 2){
                        int id = Integer.parseInt(idArr[1]);
                        if (mDroneActionsQueue.getLastQrCodeId() < id){
                            mDroneActionsQueue.setLastQrCodeId(id);
                        }else{
                            Log.d(TAG, "invalid id: " + message);
                            return null;
                        }
                        // TODO compare ID
                        String moveType = moveArr[0];
                        int timeMilis = Integer.parseInt(moveArr[1]);

                        if (moveType.equals(QR_CODE_MOVE_TYPE_FORWARD)){
                            return new DroneMoveForward(timeMilis);
                        }else if (moveType.equals(QR_CODE_MOVE_TYPE_BACKWARD)){
                            return new DroneMoveBackward(timeMilis);
                        }else if (moveType.equals(QR_CODE_MOVE_TYPE_RIGHT_ROTATE)){
                            return new DroneMoveRotateRight(timeMilis);
                        }else if (moveType.equals(QR_CODE_MOVE_TYPE_LEFT_ROTATE)){
                            return new DroneMoveRotateLeft(timeMilis);
                        }else if (moveType.equals(QR_CODE_MOVE_TYPE_LEFT)){
                            return new DroneMoveLeft(timeMilis);
                        }else if (moveType.equals(QR_CODE_MOVE_TYPE_RIGHT)){
                            return new DroneMoveRight(timeMilis);
                        }else if (moveType.equals(QR_CODE_MOVE_TYPE_UP)){
                            return new DroneMoveUp(timeMilis);
                        }else if (moveType.equals(QR_CODE_MOVE_TYPE_DOWN)){
                            return new DroneMoveDown(timeMilis);

                        }else if (moveType.equals(QR_CODE_MOVE_TYPE_LAND)){
                            return new SimpleActionLand();
                        }

                    }
                }
            }
        }

        Log.d(TAG, "invalid message: " + message);

        return null;
    }
}
