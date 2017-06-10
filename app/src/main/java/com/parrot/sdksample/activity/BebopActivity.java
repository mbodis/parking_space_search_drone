package com.parrot.sdksample.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
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

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.sdksample.drone.BebopDrone;
import com.parrot.sdksample.models.qr_code_landing.LandOnQrCode;
import com.parrot.sdksample.models.time_move.DroneActionsQueue;
import com.parrot.sdksample.models.time_move.controllers.ConditionActionLandQrCode;
import com.parrot.sdksample.models.time_move.controllers.SimpleActionTakePicture;
import com.parrot.sdksample.models.time_move.controllers.DroneMoveBackward;
import com.parrot.sdksample.models.time_move.controllers.SimpleActionCameraView;
import com.parrot.sdksample.models.time_move.controllers.DroneMoveDown;
import com.parrot.sdksample.models.time_move.controllers.DroneMoveForward;
import com.parrot.sdksample.models.time_move.controllers.SimpleActionLand;
import com.parrot.sdksample.models.time_move.controllers.DroneMoveLeft;
import com.parrot.sdksample.models.time_move.controllers.DroneMoveRight;
import com.parrot.sdksample.models.time_move.controllers.DroneMoveRotateLeft;
import com.parrot.sdksample.models.time_move.controllers.DroneMoveRotateRight;
import com.parrot.sdksample.models.time_move.controllers.SimpleActionMoveSleep;
import com.parrot.sdksample.models.time_move.controllers.SimpleActionTakeOff;
import com.parrot.sdksample.models.time_move.controllers.DroneMoveUp;
import com.parrot.sdksample.models.time_move.iface.DroneActionIface;
import com.parrot.sdksample.models.time_move.iface.MoveActionIface;
import com.parrot.sdksample.view.BebopVideoView;
import com.parrot.sdksample.view.LandingPatternLayerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sk.svb.drone.parking_space.R;

public class BebopActivity extends AppCompatActivity {
    private static final String TAG = "BebopActivity";
    private static final String ACTION_LOG_MESSAGE = TAG + "_log_message";
    private static final String ACTION_LANDING_STATUS = TAG + "_landing_status";
    private BebopDrone mBebopDrone;

    private ProgressDialog mConnectionProgressDialog;
    private ProgressDialog mDownloadProgressDialog;

    private BebopVideoView mVideoView;
    private LandingPatternLayerView myLandingPatternLayerView;

    private TextView textViewLog, textViewLabelRoll, textViewLabelYaw,
    landWidthTv, landHeightTv, landRotationTv, landVerticalTv;
    private Button mTakeOffLandBt, mDownloadBt, takePictureBt, gazUpBt, gazDownBt,
            yawLeftBt, yawRightBt, forwardBt, backBt, rollLeftBt, rollRightBt, cameraDown,
            cameraCenter, parkPhase1Btn, parkPhase2Btn, parkPhase3Btn, parkPhase4Btn, parkStopBtn, showImgBtn;
    private ScrollView scrollLog;
    private SimpleDraweeView sequenceLayerTakenFrame;
    private View layerControl, layerLog, layerSequences;


    private int mNbMaxDownload;
    private int mCurrentDownloadIndex;

    public DroneActionsQueue mDroneActionsQueue;
    public LandOnQrCode mLandOnQrCode;

    public String lastImgPath;
    public boolean imageShown = false;

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
        Fresco.initialize(getApplicationContext());

        setContentView(R.layout.activity_bebop);

        initIHM();
        toggleLayerControl(View.VISIBLE);
        toggleLayerSequence(View.GONE);
        toggleLayerLog(View.GONE);

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
        // hide foto
        if (imageShown){
            imageShown = false;
            sequenceLayerTakenFrame.setVisibility(View.INVISIBLE);
            return;
        }

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
                toggleLayerControl(View.VISIBLE);
                toggleLayerSequence(View.GONE);
                toggleLayerLog(View.GONE);
                break;

            case R.id.menu_view_control2:
                toggleLayerControl(View.GONE);
                toggleLayerSequence(View.VISIBLE);
                toggleLayerLog(View.GONE);
                break;

            case R.id.menu_view_log:
                toggleLayerControl(View.GONE);
                toggleLayerSequence(View.GONE);
                toggleLayerLog(View.VISIBLE);
                break;

            case R.id.menu_view_drone_stream:
                toggleLayerControl(View.GONE);
                toggleLayerSequence(View.GONE);
                toggleLayerLog(View.GONE);
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

    private void toggleLayerControl(int visibility){
        layerControl.setVisibility(visibility);
    }

    private void toggleLayerSequence(int visibility){
        layerSequences.setVisibility(visibility);
    }

    private void toggleLayerLog(int visibility){
        layerLog.setVisibility(visibility);
    }

    private void initIHM() {
        layerControl = findViewById(R.id.layer_controls);
        layerLog = findViewById(R.id.layer_log);
        layerSequences = findViewById(R.id.layer_sequences);

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

        sequenceLayerTakenFrame = (SimpleDraweeView) findViewById(R.id.sequenceTakenFrame);
        sequenceLayerTakenFrame.setVisibility(View.GONE);

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
        parkPhase4Btn = ((Button) findViewById(R.id.parkPhase4Btn));
        parkPhase4Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPhase4();
            }
        });
        parkStopBtn = ((Button) findViewById(R.id.parkStopBtn));
        parkStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPhase();
            }
        });
        showImgBtn = ((Button) findViewById(R.id.showImgBtn));
        showImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLastImage();
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
            ((ActionBar)getSupportActionBar()).setTitle("battery: " + String.format("%d%%", batteryPercentage));
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
            //Log.d(TAG, "Picture has been taken err:" + error.getValue());

            // if DroneActionsQueue download image automatically
            if (error == ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_OK){
                if (mDroneActionsQueue.isInProgress()){
                    mBebopDrone.getLastFlightMedias();
                }
            }
        }

        @Override
        public void configureDecoder(ARControllerCodec codec) {
            // Log.d(TAG, "configureDecoder");
            mVideoView.configureDecoder(codec);
        }

        @Override
        public void onFrameReceived(ARFrame frame) {
            // Log.d(TAG, "onFrameReceived");
            mVideoView.displayFrame(frame);
        }

        @Override
        public void onMatchingMediasFound(int nbMedias) {
            //Log.d(TAG, "onMatchingMediasFound");
            if (mDownloadProgressDialog != null) {
                mDownloadProgressDialog.dismiss();
            }

            mNbMaxDownload = nbMedias;
            mCurrentDownloadIndex = 1;

            Log.d(TAG, "nbMedias: " + nbMedias);
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
            //Log.d(TAG, "onDownloadProgressed " + progress);
            mDownloadProgressDialog.setProgress(((mCurrentDownloadIndex - 1) * 100) + progress);
        }

        @Override
        public void onDownloadComplete(String mediaName) {
            //Log.d(TAG, "onDownloadComplete " + mediaName);

            String externalDirectory = Environment.getExternalStorageDirectory().toString().concat("/ARSDKMedias/");
            File newPhoto = new File(externalDirectory + mediaName);
            if (newPhoto.exists()) {
                lastImgPath = newPhoto.getAbsolutePath();
                showLastImage();
                Toast.makeText(getApplicationContext(), "photo downloaded", Toast.LENGTH_SHORT).show();

                // stop downloading
                mBebopDrone.cancelGetLastFlightMedias();
                // hide download dialog
                mDownloadProgressDialog.cancel();
            }

            mCurrentDownloadIndex++;
            mDownloadProgressDialog.setSecondaryProgress(mCurrentDownloadIndex * 100);

            if (mCurrentDownloadIndex > mNbMaxDownload) {
                mDownloadProgressDialog.dismiss();
                mDownloadProgressDialog = null;
            }
        }

        @Override
        public void onGpsChanged(double lat, double lng, double alt) {
            //use GPS ?
        }
    };

    private void showLastImage(){
        if (lastImgPath != null) {
            Uri imageUri = Uri.fromFile(new File(lastImgPath));
            sequenceLayerTakenFrame.setImageURI(imageUri);
            sequenceLayerTakenFrame.setVisibility(View.VISIBLE);
            imageShown = true;
        }else{
            Toast.makeText(getApplicationContext(), "foto not available", Toast.LENGTH_SHORT).show();
        }
    }

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
        if (stopPhase()) return;

        List<DroneActionIface> moves = new ArrayList<DroneActionIface>();
        moves.add(new SimpleActionCameraView(SimpleActionCameraView.VIEW_DOWN));
        moves.add(new SimpleActionTakeOff());
        moves.add(new ConditionActionLandQrCode(2*60*1000));
        mDroneActionsQueue = new DroneActionsQueue(getApplicationContext(), mBebopDrone, mLandOnQrCode, moves);
        mDroneActionsQueue.start();
    }

    /**
     *
     * 1) camera down
     * 2) take off
     * 3) move 5m up
     * 4) take picture
     * 4) wait 5sec
     * 4) move 5m down
     * 5) land algorithm
     */
    private void launchPhase2(){
        if (stopPhase()) return;

        int timeUp = 6000; // 6 ssc
        int timeDown = 3000; // 3 ssc
        List<DroneActionIface> moves = new ArrayList<DroneActionIface>();
        moves.add(new SimpleActionCameraView(SimpleActionCameraView.VIEW_FORWARD));
        moves.add(new SimpleActionTakeOff());
        moves.add(new DroneMoveUp(MoveActionIface.SPEED_EXTRA_FAST, timeUp));
        moves.add(new SimpleActionMoveSleep(1000));
        moves.add(new SimpleActionTakePicture());
        moves.add(new SimpleActionMoveSleep(2000));
        moves.add(new SimpleActionCameraView(SimpleActionCameraView.VIEW_DOWN));
        moves.add(new DroneMoveDown(MoveActionIface.SPEED_EXTRA_FAST, timeDown));
        moves.add(new ConditionActionLandQrCode(2 * 60 * 1000));
        mDroneActionsQueue = new DroneActionsQueue(getApplicationContext(), mBebopDrone, mLandOnQrCode, moves);
        mDroneActionsQueue.start();
    }

    /**
     * 1) camera down
     * 2) take off
     * 3) qr code read instructions
     */
    private void launchPhase3(){
        if (stopPhase()) return;

        Toast.makeText(getApplicationContext(), "phase 3 TODO", Toast.LENGTH_LONG).show();

//        List<DroneActionIface> moves = new ArrayList<DroneActionIface>();
//        moves.add(new SimpleActionCameraView(SimpleActionCameraView.VIEW_DOWN));
//        moves.add(new SimpleActionTakeOff());
//        // TODO read QR code action
//        mDroneActionsQueue = new DroneActionsQueue(getApplicationContext(), mBebopDrone, mLandOnQrCode, moves);
//        mDroneActionsQueue.start();
    }

    /**
     * TODO
     * 1) camera down
     * 2) take off
     * 3) move 5m up (save interesting points)
     * 4) take picture
     * 4) move 5m down (use interesting point to recover)
     * 5) land algorithm
     */
    private void launchPhase4(){
        if (stopPhase()) return;

        // TODO create stack feature
        Toast.makeText(getApplicationContext(), "phase 4 TODO", Toast.LENGTH_LONG).show();
    }

    /**
     * stop drone action queue
     */
    private boolean stopPhase(){
        if (mDroneActionsQueue != null && mDroneActionsQueue.isInProgress()) {
            mDroneActionsQueue.stop(mBebopDrone, mLandOnQrCode);
            return true;
        }

        return false;
    }

    private void testTimeMoves(){
        List<DroneActionIface> moves = new ArrayList<DroneActionIface>();
        moves.add(new SimpleActionTakeOff());
        moves.add(new SimpleActionCameraView(SimpleActionCameraView.VIEW_DOWN));
        moves.add(new DroneMoveRotateLeft(DroneMoveRotateLeft.SPEED_FAST, 2000));
        moves.add(new DroneMoveRotateRight(DroneMoveRotateLeft.SPEED_FAST, 2000));
        moves.add(new SimpleActionCameraView(SimpleActionCameraView.VIEW_FORWARD));
        moves.add(new DroneMoveUp(1000));
        moves.add(new DroneMoveDown(1000));
        moves.add(new DroneMoveForward(2000));
        moves.add(new DroneMoveBackward(2000));
        moves.add(new DroneMoveLeft(1500));
        moves.add(new SimpleActionMoveSleep(2000));
        moves.add(new DroneMoveRight(1500));
        moves.add(new SimpleActionLand());
        mDroneActionsQueue = new DroneActionsQueue(getApplicationContext(), mBebopDrone, mLandOnQrCode, moves);
        mDroneActionsQueue.start();
    }
}
