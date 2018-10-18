package com.example.hubaoyu.threebody;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.hubaoyu.threebody.helper.DataHelper;
import com.example.hubaoyu.threebody.http.SquatHttp;
import com.example.hubaoyu.threebody.model.SquatModel;
import com.example.hubaoyu.threebody.model.ViewModel;
import com.example.hubaoyu.threebody.ui.KeepFontTextView;

import java.io.IOException;

import rx.functions.Action1;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        Camera.PreviewCallback, View.OnClickListener {

    // raw frame resolution: 1280x720, image format is: YV12
    // you need get all resolution that supported on your devices;
    // my phone is HUAWEI honor 6Plus, most devices can use 1280x720
    private static final int SRC_FRAME_WIDTH = 1280;
    private static final int SRC_FRAME_HEIGHT = 720;
    private static final int IMAGE_FORMAT = ImageFormat.YV12;

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
                if (squatModel.getStatus() != 0) {
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
//        ByteArrayOutputStream baos;
//        byte[] rawImage;
//        Bitmap bitmap;
//
////        camera.setOneShotPreviewCallback(null);
//        //处理data
//        Camera.Size previewSize = camera.getParameters().getPreviewSize();//获取尺寸,格式转换的时候要用到
//        BitmapFactory.Options newOpts = new BitmapFactory.Options();
//        newOpts.inJustDecodeBounds = true;
//        YuvImage yuvimage = new YuvImage(
//                data,
//                IMAGE_FORMAT,
//                previewSize.width,
//                previewSize.height,
//                null);
//        baos = new ByteArrayOutputStream();
//        yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, baos);// 80--JPG图片的质量[0-100],100最高
//        rawImage = baos.toByteArray();
//        //将rawImage转换成bitmap
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.RGB_565;
//        bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);

        ImageUtils.saveImageData(data);
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
}
