package com.example.hubaoyu.threebody;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.open.net.client.impl.tcp.nio.NioClient;
import com.open.net.client.structures.BaseClient;
import com.open.net.client.structures.BaseMessageProcessor;
import com.open.net.client.structures.IConnectListener;
import com.open.net.client.structures.TcpAddress;
import com.open.net.client.structures.message.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        Camera.PreviewCallback, View.OnClickListener {

    // raw frame resolution: 1280x720, image format is: YV12
    // you need get all resolution that supported on your devices;
    // my phone is HUAWEI honor 6Plus, most devices can use 1280x720
    private static final int SRC_FRAME_WIDTH = 320;
    private static final int SRC_FRAME_HEIGHT = 240;
    //    private static final int IMAGE_FORMAT = ImageFormat.YV12;
    private static final int IMAGE_FORMAT = ImageFormat.NV21;

    private Camera mCamera;
    private Camera.Parameters mParams;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private TextView recContent, statusLabel;
    private ImageView imageView;
    private NioClient mClient = null;

    private String flag = "24aa5366b7223b1b30c0620326222275";
    private byte[] flagBytes = flag.getBytes();

    private static final String IP = "10.2.25.238";
    private static final int PORT = 7000;
    private long timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setListener();
        checkPermission();
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.sv_recording);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setFixedSize(SRC_FRAME_WIDTH, SRC_FRAME_HEIGHT);
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        statusLabel = findViewById(R.id.textview_label);
        recContent = findViewById(R.id.textview_tips);
        imageView = findViewById(R.id.imageview);

        mClient = new NioClient(mMessageProcessor, mConnectResultListener);
        mClient.setConnectAddress(new TcpAddress[]{new TcpAddress(IP, PORT)});
        openSocket();
    }

    private void setListener() {
        // set Listener if you want, eg: onClickListener
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (data == null) return;

        if (System.currentTimeMillis() - timestamp < 100) return;

        if (!mClient.isConnected()) {
            reConnect();
            return;
        }

        ByteArrayOutputStream outputSteam = new ByteArrayOutputStream();
        YuvImage yuvImage = new YuvImage(data, IMAGE_FORMAT, SRC_FRAME_WIDTH, SRC_FRAME_HEIGHT, null);
        yuvImage.compressToJpeg(new Rect(0, 0, SRC_FRAME_WIDTH, SRC_FRAME_HEIGHT), 80, outputSteam);

        byte[] jpegByte = outputSteam.toByteArray();
        final Bitmap bmp = BitmapFactory.decodeByteArray(jpegByte, 0, outputSteam.size());
        try {
            outputSteam.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bmp);
            }
        });

        timestamp = System.currentTimeMillis();
        mMessageProcessor.send(mClient, jpegByte);
        mMessageProcessor.send(mClient, flag.getBytes());
        camera.addCallbackBuffer(data);

//        mMessageProcessor.send(mClient, "hello world!".getBytes());
//        mMessageProcessor.send(mClient, flag.getBytes());
    }

    private void checkPermission() {
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);

        } else {
            Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCamera(SurfaceHolder holder) {
        releaseCamera(); // release Camera, if not release camera before call camera, it will be locked
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        mParams = mCamera.getParameters();
        setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
        mParams.setPreviewSize(SRC_FRAME_WIDTH, SRC_FRAME_HEIGHT);
        mParams.setPreviewFormat(IMAGE_FORMAT); // setting preview format：YV12
        mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        mCamera.setParameters(mParams); // setting camera parameters
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        mCamera.setPreviewCallback(this);
        mCamera.startPreview();
    }

    private synchronized void releaseCamera() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mCamera = null;
        }
    }

    private void openSocket() {
        mClient.connect();
        if (!mClient.isConnected()) {
            statusLabel.setText("Connectioning");
        }
    }

    private void closeSocket() {
        mClient.disconnect();
        statusLabel.setText("disconnect");
    }

    private void reConnect() {
        mClient.reconnect();
        statusLabel.setText("Re-Connectioning");
    }

    /**
     * Android API: Display Orientation Setting
     * Just change screen display orientation,
     * the rawFrame data never be changed.
     */
    private void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int displayDegree;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayDegree = (info.orientation + degrees) % 360;
            displayDegree = (360 - displayDegree) % 360;  // compensate the mirror
        } else {
            displayDegree = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(displayDegree);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                mSurfaceHolder = holder;
                openCamera(mSurfaceHolder); // open camera
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        openCamera(mSurfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mClient.disconnect();
    }

    private IConnectListener mConnectResultListener = new IConnectListener() {
        @Override
        public void onConnectionSuccess() {
            runOnUiThread(new Runnable() {
                public void run() {
                    statusLabel.setText("Connection-Success");
                }
            });
        }

        @Override
        public void onConnectionFailed() {
            runOnUiThread(new Runnable() {
                public void run() {
                    statusLabel.setText("Connection-Failed");
                }
            });
        }
    };

    private BaseMessageProcessor mMessageProcessor = new BaseMessageProcessor() {

        int packCount = 0;

        @Override
        public void onReceiveMessages(BaseClient mClient, LinkedList<Message> mQueen) {
            for (int i = 0; i < mQueen.size(); i++) {
                Message msg = mQueen.get(i);
                final String s = new String(msg.data, msg.offset, msg.length);
                packCount++;
                runOnUiThread(new Runnable() {
                    public void run() {
                        recContent.setText("pack：" + packCount);
                    }
                });
            }
        }
    };

}
