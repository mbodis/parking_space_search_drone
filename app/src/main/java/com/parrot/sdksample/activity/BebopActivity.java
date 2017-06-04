package com.parrot.sdksample.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.LandOnQrCode;
import com.parrot.sdksample.models.time_move.DroneTimeMoves;
import com.parrot.sdksample.models.time_move.controllers.ConditionMoveLandQrCode;
import com.parrot.sdksample.models.time_move.controllers.TimeMoveBackward;
import com.parrot.sdksample.models.time_move.controllers.TimeMoveCameraView;
import com.parrot.sdksample.models.time_move.controllers.TimeMoveDown;
import com.parrot.sdksample.models.time_move.controllers.TimeMoveForward;
import com.parrot.sdksample.models.time_move.controllers.TimeMoveLand;
import com.parrot.sdksample.models.time_move.controllers.TimeMoveLeft;
import com.parrot.sdksample.models.time_move.controllers.TimeMoveRight;
import com.parrot.sdksample.models.time_move.controllers.TimeMoveRotateLeft;
import com.parrot.sdksample.models.time_move.controllers.TimeMoveRotateRight;
import com.parrot.sdksample.models.time_move.controllers.TimeMoveSleep;
import com.parrot.sdksample.models.time_move.controllers.TimeMoveTakeOff;
import com.parrot.sdksample.models.time_move.controllers.TimeMoveUp;
import com.parrot.sdksample.models.time_move.iface.DroneMoveIface;
import com.parrot.sdksample.models.time_move.iface.TimeMoveIface;
import com.parrot.sdksample.view.LandingPatternLayerView;
import com.parrot.sdksample.view.BebopVideoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sk.svb.drone.parking_space.R;

import static com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED;

public class BebopActivity extends AppCompatActivity {
    private static final String TAG = "BebopActivity";
    private static final String ACTION_LOG_MESSAGE = TAG + "_log_message";
    private static final String ACTION_LANDING_STATUS = TAG + "_landing_status";
    private BebopDrone mBebopDrone;

    private ProgressDialog mConnectionProgressDialog;
    private ProgressDialog mDownloadProgressDialog;

    private BebopVideoView mVideoView;
    private LandingPatternLayerView myLandingPatternLayerView;

    private TextView mBatteryLabel, textViewLog, textViewLabelRoll, textViewLabelYaw,
    landWidthTv, landHeightTv, landRotationTv, landVerticalTv;
    private Button mTakeOffLandBt, mDownloadBt, takePictureBt, gazUpBt, gazDownBt,
            yawLeftBt, yawRightBt, forwardBt, backBt, rollLeftBt, rollRightBt, cameraDown,
            cameraCenter, parkPhase1Btn, parkPhase2Btn, parkPhase3Btn, parkStopBtn;
    private ScrollView scrollLog;

    private int mNbMaxDownload;
    private int mCurrentDownloadIndex;

    public DroneTimeMoves mDroneTimeMoves;
    public LandOnQrCode mLandOnQrCode;

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null){
                if (intent.getAction().equals(ACTION_LOG_MESSAGE)){
                    addTextLog(intent.getStringExtra("msg"));
                }

                if (intent.getAction().equals(ACTION_LANDING_STATUS)){
                    boolean landWidth = intent.getBooleanExtra("landWidth", false);
                    boolean landHeight = intent.getBooleanExtra("landHeight", false);
                    boolean landRotation = intent.getBooleanExtra("landRotation", false);
                    boolean landVertical = intent.getBooleanExtra("landVertical", false);
                    setLandingStatus(landWidth, landHeight, landRotation, landVertical);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bebop);

        initIHM();
        toggleViewControl(View.VISIBLE);
        toggleViewControl2(View.GONE);
        toggleViewLog(View.GONE);

        Intent intent = getIntent();
        ARDiscoveryDeviceService service = intent.getParcelableExtra(DeviceListActivity.EXTRA_DEVICE_SERVICE);
        mBebopDrone = new BebopDrone(this, service);
        mBebopDrone.addListener(mBebopListener);

        mLandOnQrCode = new LandOnQrCode(mBebopDrone, getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver, new IntentFilter(ACTION_LOG_MESSAGE));
        registerReceiver(mBroadcastReceiver, new IntentFilter(ACTION_LANDING_STATUS));
    }

    @Override
    protected void onStart() {
        super.onStart();

        // show a loading view while the bebop drone is connecting
        if ((mBebopDrone != null) && !(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(mBebopDrone.getConnectionState()))) {
            mConnectionProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
            mConnectionProgressDialog.setIndeterminate(true);
            mConnectionProgressDialog.setMessage("Connecting ...");
            mConnectionProgressDialog.setCancelable(false);
            mConnectionProgressDialog.show();

            // if the connection to the Bebop fails, finish the activity
            if (!mBebopDrone.connect()) {
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mBebopDrone != null) {
            mConnectionProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
            mConnectionProgressDialog.setIndeterminate(true);
            mConnectionProgressDialog.setMessage("Disconnecting ...");
            mConnectionProgressDialog.setCancelable(false);
            mConnectionProgressDialog.show();

            if (!mBebopDrone.disconnect()) {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bebop_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_view_control:
                toggleViewControl(View.VISIBLE);
                toggleViewControl2(View.GONE);
                toggleViewLog(View.GONE);
                break;

            case R.id.menu_view_control2:
                toggleViewControl(View.GONE);
                toggleViewControl2(View.VISIBLE);
                toggleViewLog(View.GONE);
                break;

            case R.id.menu_view_log:
                toggleViewControl(View.GONE);
                toggleViewControl2(View.GONE);
                toggleViewLog(View.VISIBLE);
                break;

            case R.id.menu_view_drone_stream:
                toggleViewControl(View.GONE);
                toggleViewControl2(View.GONE);
                toggleViewLog(View.GONE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        mBebopDrone.dispose();
        mLandOnQrCode.destroy(getApplicationContext());
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    private void toggleViewControl(int visibility){
        gazUpBt.setVisibility(visibility);
        gazDownBt.setVisibility(visibility);
        yawLeftBt.setVisibility(visibility);
        yawRightBt.setVisibility(visibility);
        forwardBt.setVisibility(visibility);
        backBt.setVisibility(visibility);
        rollLeftBt.setVisibility(visibility);
        rollRightBt.setVisibility(visibility);
        mDownloadBt.setVisibility(visibility);
        takePictureBt.setVisibility(visibility);

        mTakeOffLandBt.setVisibility(visibility);
        cameraCenter.setVisibility(visibility);
        cameraDown.setVisibility(visibility);

        textViewLabelRoll.setVisibility(visibility);
        textViewLabelYaw.setVisibility(visibility);
    }
    private void toggleViewControl2(int visibility){
        parkPhase1Btn.setVisibility(visibility);
        parkPhase2Btn.setVisibility(visibility);
        parkPhase3Btn.setVisibility(visibility);
        parkStopBtn.setVisibility(visibility);
    }

    private void toggleViewLog(int visibility){
        textViewLog.setVisibility(visibility);
        landWidthTv.setVisibility(visibility);
        landHeightTv.setVisibility(visibility);
        landRotationTv.setVisibility(visibility);
        landVerticalTv.setVisibility(visibility);
    }

    private void initIHM() {
        myLandingPatternLayerView = (LandingPatternLayerView) findViewById(R.id.myDrawLayer);

        landWidthTv = (TextView) findViewById(R.id.landWidthTv);
        landHeightTv = (TextView) findViewById(R.id.landHeightTv);
        landRotationTv = (TextView) findViewById(R.id.landRotationTv);
        landVerticalTv = (TextView) findViewById(R.id.landVerticalTv);

        // setup text views
        textViewLabelRoll = (TextView) findViewById(R.id.textViewLabelRoll);
        textViewLabelYaw = (TextView) findViewById(R.id.textViewLabelYaw);
        textViewLog = (TextView) findViewById(R.id.textViewLog);
        textViewLog.setText("");
        scrollLog = (ScrollView) findViewById(R.id.scrollLog);

        mVideoView = (BebopVideoView) findViewById(R.id.videoView);
        mVideoView.setSurfaceTextureListener(mVideoView);
        mVideoView.setupViews(myLandingPatternLayerView, textViewLog, scrollLog);

        findViewById(R.id.emergencyBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopDrone.emergency();
            }
        });

        mTakeOffLandBt = (Button) findViewById(R.id.takeOffOrLandBt);
        mTakeOffLandBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch (mBebopDrone.getFlyingState()) {
                    case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                        mBebopDrone.takeOff();
                        break;
                    case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                    case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                        mBebopDrone.land();
                        break;
                    default:
                }
            }
        });

        takePictureBt = (Button) findViewById(R.id.takePictureBt);
        takePictureBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopDrone.takePicture();
            }
        });

        mDownloadBt = (Button) findViewById(R.id.downloadBt);
        mDownloadBt.setEnabled(false);
        mDownloadBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopDrone.getLastFlightMedias();

                mDownloadProgressDialog = new ProgressDialog(BebopActivity.this, R.style.AppCompatAlertDialogStyle);
                mDownloadProgressDialog.setIndeterminate(true);
                mDownloadProgressDialog.setMessage("Fetching medias");
                mDownloadProgressDialog.setCancelable(false);
                mDownloadProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBebopDrone.cancelGetLastFlightMedias();
                    }
                });
                mDownloadProgressDialog.show();
            }
        });


        gazUpBt = (Button) findViewById(R.id.gazUpBt);
        gazUpBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setGaz((byte) 50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setGaz((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        gazDownBt = (Button) findViewById(R.id.gazDownBt);
        gazDownBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setGaz((byte) -50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setGaz((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        yawLeftBt = (Button) findViewById(R.id.yawLeftBt);
        yawLeftBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setYaw((byte) -50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setYaw((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        yawRightBt = (Button) findViewById(R.id.yawRightBt);
        yawRightBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setYaw((byte) 50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setYaw((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        forwardBt = (Button) findViewById(R.id.forwardBt);
        forwardBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setPitch((byte) 50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setPitch((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        backBt = (Button) findViewById(R.id.backBt);
        backBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setPitch((byte) -50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setPitch((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        rollLeftBt = (Button) findViewById(R.id.rollLeftBt);
        rollLeftBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setRoll((byte) -50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setRoll((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        rollRightBt = (Button) findViewById(R.id.rollRightBt);
        rollRightBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setRoll((byte) 50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setRoll((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        cameraDown = (Button) findViewById(R.id.cameraDown);
        cameraDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setCameraOrientationV2((byte) -100, (byte) 0);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);

                        break;

                    default:
                        break;
                }

                return true;
            }
        });

        cameraCenter = (Button) findViewById(R.id.cameraCenter);
        cameraCenter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setCameraOrientationV2((byte) 0, (byte) 0);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        break;

                    default:
                        break;
                }

                return true;
            }
        });

        mBatteryLabel = (TextView) findViewById(R.id.batteryLabel);

        parkPhase1Btn = ((Button) findViewById(R.id.parkPhase1Btn));
        parkPhase1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPhase1();
            }
        });
        parkPhase2Btn = ((Button) findViewById(R.id.parkPhase2Btn));
        parkPhase2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPhase2();
            }
        });
        parkPhase3Btn = ((Button) findViewById(R.id.parkPhase3Btn));
        parkPhase3Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPhase3();
            }
        });
        parkStopBtn = ((Button) findViewById(R.id.parkStopBtn));
        parkStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPhase();
            }
        });

    }

    private final BebopDrone.Listener mBebopListener = new BebopDrone.Listener() {
        @Override
        public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state) {
            switch (state) {
                case ARCONTROLLER_DEVICE_STATE_RUNNING:
                    mConnectionProgressDialog.dismiss();
                    break;

                case ARCONTROLLER_DEVICE_STATE_STOPPED:
                    // if the deviceController is stopped, go back to the previous activity
                    mConnectionProgressDialog.dismiss();
                    finish();
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onBatteryChargeChanged(int batteryPercentage) {
            mBatteryLabel.setText(String.format("%d%%", batteryPercentage));
        }

        @Override
        public void onPilotingStateChanged(ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state) {
            switch (state) {
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                    mTakeOffLandBt.setText("Take off");
                    mTakeOffLandBt.setEnabled(true);
                    mDownloadBt.setEnabled(true);
                    break;
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                    mTakeOffLandBt.setText("Land");
                    mTakeOffLandBt.setEnabled(true);
                    mDownloadBt.setEnabled(false);
                    break;
                default:
                    mTakeOffLandBt.setEnabled(false);
                    mDownloadBt.setEnabled(false);
            }
        }

        @Override
        public void onPictureTaken(ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {
            //Log.i(TAG, "Picture has been taken");
        }

        @Override
        public void configureDecoder(ARControllerCodec codec) {
            //Log.i(TAG, "configureDecoder");
            mVideoView.configureDecoder(codec);
        }

        @Override
        public void onFrameReceived(ARFrame frame) {
            //Log.i(TAG, "onFrameReceived");
            mVideoView.displayFrame(frame);
        }

        @Override
        public void onMatchingMediasFound(int nbMedias) {
            mDownloadProgressDialog.dismiss();

            mNbMaxDownload = nbMedias;
            mCurrentDownloadIndex = 1;

            if (nbMedias > 0) {
                mDownloadProgressDialog = new ProgressDialog(BebopActivity.this, R.style.AppCompatAlertDialogStyle);
                mDownloadProgressDialog.setIndeterminate(false);
                mDownloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mDownloadProgressDialog.setMessage("Downloading medias");
                mDownloadProgressDialog.setMax(mNbMaxDownload * 100);
                mDownloadProgressDialog.setSecondaryProgress(mCurrentDownloadIndex * 100);
                mDownloadProgressDialog.setProgress(0);
                mDownloadProgressDialog.setCancelable(false);
                mDownloadProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBebopDrone.cancelGetLastFlightMedias();
                    }
                });
                mDownloadProgressDialog.show();
            }
        }

        @Override
        public void onDownloadProgressed(String mediaName, int progress) {
            mDownloadProgressDialog.setProgress(((mCurrentDownloadIndex - 1) * 100) + progress);
        }

        @Override
        public void onDownloadComplete(String mediaName) {
            mCurrentDownloadIndex++;
            mDownloadProgressDialog.setSecondaryProgress(mCurrentDownloadIndex * 100);

            if (mCurrentDownloadIndex > mNbMaxDownload) {
                mDownloadProgressDialog.dismiss();
                mDownloadProgressDialog = null;
            }
        }

        @Override
        public void onGpsChanged(double lat, double lng, double alt) {
            //TODO svb

        }
    };

    public static void addTextLogIntent(Context ctx, String msg){
        Intent mIntent = new Intent(ACTION_LOG_MESSAGE);
        mIntent.putExtra("msg", msg);
        ctx.sendBroadcast(mIntent);
    }

    public static void sendLandControllerStatus(Context ctx, boolean landWidth, boolean landHeight, boolean landRotation, boolean landVertical){
        Intent mIntent = new Intent(ACTION_LANDING_STATUS);
        mIntent.putExtra("landWidth", landWidth);
        mIntent.putExtra("landHeight", landHeight);
        mIntent.putExtra("landRotation", landRotation);
        mIntent.putExtra("landVertical", landVertical);
        ctx.sendBroadcast(mIntent);
    }

    private void addTextLog(String msg){
        String date = new SimpleDateFormat("HH:mm:ss").format(new Date());
        textViewLog.append(date + " " + msg + "\n");
        scrollLog.scrollTo(0, 999999);
        Log.d(TAG, msg);
    }

    private void setLandingStatus(boolean landWidth, boolean landHeight, boolean landRotation, boolean landVertical){
        int red = Color.parseColor("#ff0000");
        int green = Color.parseColor("#00ff00");
        landWidthTv.setTextColor(landWidth ? green : red);
        landHeightTv.setTextColor(landHeight ? green : red);
        landRotationTv.setTextColor(landRotation ? green : red);
        landVerticalTv.setTextColor(landVertical ? green : red);
    }

    /**
     * 1) camera down
     * 2) take off
     * 3) land algorithm
     */
    private void launchPhase1(){
        List<DroneMoveIface> moves = new ArrayList<DroneMoveIface>();
        moves.add(new TimeMoveCameraView(TimeMoveCameraView.VIEW_DOWN));
        moves.add(new TimeMoveTakeOff());
        moves.add(new ConditionMoveLandQrCode(2*60*1000));
        mDroneTimeMoves = new DroneTimeMoves(getApplicationContext(), mBebopDrone, mLandOnQrCode, moves);
    }

    /**
     *
     * 1) camera down
     * 2) take off
     * 3) move 5m up
     * 4) wait 5sec
     * 4) move 5m down
     * 5) land algorithm
     */
    private void launchPhase2(){
        List<DroneMoveIface> moves = new ArrayList<DroneMoveIface>();
        moves.add(new TimeMoveCameraView(TimeMoveCameraView.VIEW_FORWARD));
        moves.add(new TimeMoveTakeOff());
        moves.add(new TimeMoveUp(TimeMoveIface.SPEED_FAST, 1000));
        moves.add(new TimeMoveSleep(5000));
        moves.add(new TimeMoveDown(TimeMoveIface.SPEED_FAST, 5000));
        moves.add(new ConditionMoveLandQrCode(2*60*1000));
        mDroneTimeMoves = new DroneTimeMoves(getApplicationContext(), mBebopDrone, mLandOnQrCode, moves);
    }

    /**
     * TODO
     * 1) camera down
     * 2) take off
     * 3) move 5m up (save interesting points)
     * 4) wait 5sec
     * 4) move 5m down (use interesting point to recover)
     * 5) land algorithm
     */
    private void launchPhase3(){
        // TODO create stack feature
        Toast.makeText(getApplicationContext(), "phase 3 TODO", Toast.LENGTH_LONG).show();
    }

    /**
     * stop all drone time moves
     */
    private void stopPhase(){
        mDroneTimeMoves.stop(mBebopDrone, mLandOnQrCode);
    }

    private void testTimeMoves(){
        List<DroneMoveIface> moves = new ArrayList<DroneMoveIface>();
        moves.add(new TimeMoveTakeOff());
        moves.add(new TimeMoveCameraView(TimeMoveCameraView.VIEW_DOWN));
        moves.add(new TimeMoveRotateLeft(TimeMoveRotateLeft.SPEED_FAST, 2000));
        moves.add(new TimeMoveRotateRight(TimeMoveRotateLeft.SPEED_FAST, 2000));
        moves.add(new TimeMoveCameraView(TimeMoveCameraView.VIEW_FORWARD));
        moves.add(new TimeMoveUp(1000));
        moves.add(new TimeMoveDown(1000));
        moves.add(new TimeMoveForward(2000));
        moves.add(new TimeMoveBackward(2000));
        moves.add(new TimeMoveLeft(1500));
        moves.add(new TimeMoveSleep(2000));
        moves.add(new TimeMoveRight(1500));
        moves.add(new TimeMoveLand());
        mDroneTimeMoves = new DroneTimeMoves(getApplicationContext(), mBebopDrone, mLandOnQrCode, moves);
    }
}
