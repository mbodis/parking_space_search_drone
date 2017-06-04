package com.parrot.sdksample.view;

    import android.content.Context;
import android.graphics.Bitmap;
    import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
    import android.view.Surface;
import android.view.TextureView;
import android.widget.ScrollView;
import android.widget.TextView;

    import com.parrot.arsdk.arcontroller.ARCONTROLLER_STREAM_CODEC_TYPE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;
    import com.parrot.sdksample.models.landing.iface.LandingAreaDetector;
    import com.parrot.sdksample.models.qr_code_landing.detector.QrCodeDetector;

    import java.io.IOException;
import java.nio.ByteBuffer;
    import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//import org.opencv.core.Mat;
/**
 * Created by mbodis on 4/21/17.
 */

/**
    helpful source:
    http://forum.developer.parrot.com/t/bebopvideoview-to-mat/3064/26
 */
public class BebopVideoView extends TextureView implements TextureView.SurfaceTextureListener{

    private static final String TAG = "BebopVideoView";
    private static final String VIDEO_MIME_TYPE = "video/avc";
    private static final int VIDEO_DEQUEUE_TIMEOUT = 33000;

    private MediaCodec mMediaCodec;

    private Lock mReadyLock;

    private boolean mIsCodecConfigured = false;

    private ByteBuffer mSpsBuffer;
    private ByteBuffer mPpsBuffer;

    private ByteBuffer[] mBuffers;
    Surface surface;

    public static final int VIDEO_WIDTH = 640;
    public static final int VIDEO_HEIGHT = 368;

    // svb
    Bitmap bmp;
    LandingPatternLayerView drawLayer;
    TextView textViewLog;
    ScrollView scrollLog;
    LandingAreaDetector mLandingAreaDetector;


    public void setupViews(LandingPatternLayerView drawLayer, TextView textViewLog, ScrollView scrollLog){
        this.drawLayer = drawLayer;
        this.textViewLog = textViewLog;
        this.scrollLog = scrollLog;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height)
    {
        this.surface= new Surface(surface);

        mReadyLock.lock();
        initMediaCodec(VIDEO_MIME_TYPE);

        mReadyLock.unlock();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mReadyLock.lock();
        releaseMediaCodec();
        mReadyLock.unlock();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public BebopVideoView(Context context) {
        super(context);
        customInit();
    }

    public BebopVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        customInit();
    }

    public BebopVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        customInit();
    }

    private void customInit() {
        setSurfaceTextureListener(this);
        mReadyLock = new ReentrantLock();
        bmp = Bitmap.createBitmap(640, 368, Bitmap.Config.ARGB_8888);
    }

    public void displayFrame(ARFrame frame) {
        mReadyLock.lock();
        if ((mMediaCodec != null)) {
            if (mIsCodecConfigured) {
                // Here we have either a good PFrame, or an IFrame
                int index = -1;

                try {
                    index = mMediaCodec.dequeueInputBuffer(VIDEO_DEQUEUE_TIMEOUT);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Error while dequeue input buffer");
                }
                if (index >= 0) {
                    ByteBuffer b;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        b = mMediaCodec.getInputBuffer(index);
                    } else {
                        b = mBuffers[index];
                        b.clear();
                    }

                    if (b != null) {

                        b.put(frame.getByteData(), 0, frame.getDataSize());
                    }

                    try {
                        mMediaCodec.queueInputBuffer(index, 0, frame.getDataSize(), 0, 0);
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "Error while queue input buffer");
                    }
                }
            }

            // Try to display previous frame
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            int outIndex;
            try {

                outIndex = mMediaCodec.dequeueOutputBuffer(info, 0);

                while (outIndex >= 0) {
                    mMediaCodec.releaseOutputBuffer(outIndex, true);
                    outIndex = mMediaCodec.dequeueOutputBuffer(info, 0);
                }


            } catch (IllegalStateException e) {
                Log.e(TAG, "Error while dequeue input buffer (outIndex)");
            }
        }

        analyseImage();

        mReadyLock.unlock();
    }


    public void configureDecoder(ARControllerCodec codec) {
        mReadyLock.lock();


        if (codec.getType() == ARCONTROLLER_STREAM_CODEC_TYPE_ENUM.ARCONTROLLER_STREAM_CODEC_TYPE_H264) {
            ARControllerCodec.H264 codecH264 = codec.getAsH264();

            mSpsBuffer = ByteBuffer.wrap(codecH264.getSps().getByteData());
            mPpsBuffer = ByteBuffer.wrap(codecH264.getPps().getByteData());

        }


        if ((mMediaCodec != null) && (mSpsBuffer != null)) {
            configureMediaCodec();
        }

        mReadyLock.unlock();
    }

    /*
    12-26 16:15:26.013 29726-30224/com.i3rivale.droneapp D/BebopVideoView:
    outputimage={mime=video/raw, crop-top=0, crop-right=855, slice-height=480, color-format=2141391876,
    height=480, width=864, what=1869968451, crop-bottom=479, crop-left=0, stride=896}

    12-26 17:20:58.729 22819-25389/com.i3rivale.droneapp D/BebopVideoView:
    inputimage={height=368, width=640, mime=video/avc, adaptive-playback=1}
    * */
    private void configureMediaCodec() {
        mMediaCodec.stop();//?
        MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, VIDEO_WIDTH, VIDEO_HEIGHT);
        format.setByteBuffer("csd-0", mSpsBuffer);
        format.setByteBuffer("csd-1", mPpsBuffer);

        mMediaCodec.configure(format, surface, null, 0);
        mMediaCodec.start();

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            mBuffers = mMediaCodec.getInputBuffers();
        }

        mIsCodecConfigured = true;
    }

    private void initMediaCodec(String type) {
        try {
            mMediaCodec = MediaCodec.createDecoderByType(type);
        } catch (IOException e) {
            Log.e(TAG, "Exception", e);
        }

        if ((mMediaCodec != null) && (mSpsBuffer != null)) {
            configureMediaCodec();
        }
    }

    private void releaseMediaCodec() {
        if (mMediaCodec != null) {
            if (mIsCodecConfigured) {
                mMediaCodec.stop();
                mMediaCodec.release();
            }
            mIsCodecConfigured = false;
            mMediaCodec = null;
        }
    }

    /**
     * async image preprocessing
     */
    private void analyseImage(){
        if (mLandingAreaDetector == null || mLandingAreaDetector.getStatus() == AsyncTask.Status.FINISHED){
            this.getBitmap(bmp);
            mLandingAreaDetector = new QrCodeDetector(getContext(), drawLayer);
            mLandingAreaDetector.execute(bmp);
        }
    }

}