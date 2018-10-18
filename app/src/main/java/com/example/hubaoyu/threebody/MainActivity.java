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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hubaoyu.threebody.helper.DataHelper;
import com.example.hubaoyu.threebody.http.SquatHttp;
import com.example.hubaoyu.threebody.model.SquatModel;
import com.example.hubaoyu.threebody.model.ViewModel;
import com.example.hubaoyu.threebody.ui.KeepFontTextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import rx.functions.Action1;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        Camera.PreviewCallback, View.OnClickListener {

    private static final int SRC_FRAME_WIDTH = 320;
    private static final int SRC_FRAME_HEIGHT = 240;
    //    private static final int IMAGE_FORMAT = ImageFormat.YV12;
    private static final int IMAGE_FORMAT = ImageFormat.NV21;

    private Camera mCamera;
    private Camera.Parameters mParams;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private FrameLayout actionButton;
    private TextView startTips;
    private KeepFontTextView textNumber;
    private TextView textStatus;


    private SquatHttp squatHttp;
    private DataHelper dataHelper;
    private TriggerVoiceController triggerVoiceController;
    private TextView recContent, statusLabel;
    private ImageView imageView;

    public static String flag = "24aa5366b7223b1b30c0620326222275";
    public static byte[] flagBytes = flag.getBytes();

    public static final String IP = "10.2.1.216";
    private static final int PORT = 7000;
    private long timestamp;


    NettyClient nettyClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setListener();
        checkPermission();
        squatHttp = new SquatHttp();
        dataHelper = new DataHelper();
        triggerVoiceController = new TriggerVoiceController(this);
        squatHttp.setDataCall(new Action1<SquatModel>() {
            @Override
            public void call(SquatModel squatModel) {
                ViewModel viewModel = ViewModel.squatToViewModel(squatModel);
                textNumber.setText(String.valueOf(viewModel.getCount()));
                textStatus.setText(viewModel.getStatusString());
                if (squatModel.getToast() != null && !squatModel.getToast().equals("")) {
                    startTips.setVisibility(View.VISIBLE);
                    startTips.setText(squatModel.getToast());
                } else {
                    startTips.setVisibility(View.GONE);
                }
                if (squatModel.getStatus() != 0 && squatModel.getCount() != 0) {
                    triggerVoiceController.play(dataHelper.countToAudio(squatModel.getCount()));
                }
            }
        });
    }

    private void initView() {
        actionButton = findViewById(R.id.layout_start_squat);
        startTips = findViewById(R.id.text_start_tips);
        textNumber = findViewById(R.id.text_number);
        textStatus = findViewById(R.id.text_status);
        mSurfaceView = findViewById(R.id.sv_recording);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setFixedSize(SRC_FRAME_WIDTH, SRC_FRAME_HEIGHT);
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        statusLabel = findViewById(R.id.textview_label);
        recContent = findViewById(R.id.textview_tips);
        imageView = findViewById(R.id.imageview);

        openSocket();
    }

    private void setListener() {
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开始深蹲，换界面，显示SufaceView
                startTips.setVisibility(View.VISIBLE);
                actionButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        squatHttp.start();
                    }
                }, 2000);
            }
        });
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (data == null) return;

        if (System.currentTimeMillis() - timestamp < 50) return;

        ByteArrayOutputStream outputSteam = new ByteArrayOutputStream();
        YuvImage yuvImage = new YuvImage(data, IMAGE_FORMAT, SRC_FRAME_WIDTH, SRC_FRAME_HEIGHT, null);
        yuvImage.compressToJpeg(new Rect(0, 0, SRC_FRAME_WIDTH, SRC_FRAME_HEIGHT), 80, outputSteam);

        byte[] jpegByte = outputSteam.toByteArray();
        final Bitmap bmp = BitmapFactory.decodeByteArray(jpegByte, 0, outputSteam.size());
        final Bitmap finalBitmap = ImageUtils.rotate(bmp, 90);

        outputSteam.reset();
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputSteam);
        jpegByte = outputSteam.toByteArray();

        try {
            outputSteam.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        timestamp = System.currentTimeMillis();
        nettyClient.sendData(jpegByte);
        nettyClient.sendData(flagBytes);

        camera.addCallbackBuffer(data);
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
        mParams.setRotation(90);
        mCamera.setParameters(mParams); // setting camera parameters
        mCamera.setDisplayOrientation(90);
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
        nettyClient = new NettyClient();
        nettyClient.setReceiveCallback(new TimeClientHandler.ReceiveCallback() {
            @Override
            public void onReceiveData(byte[] data) {
                if (data != null) {
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    nettyClient.connect(PORT, IP);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        statusLabel.setText("Connectioning");
    }

    private void closeSocket() {
        statusLabel.setText("disconnect");
    }

    private void reConnect() {
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
    protected void onStop() {
        squatHttp.stop();
        triggerVoiceController.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nettyClient.disConnect();
    }

}
