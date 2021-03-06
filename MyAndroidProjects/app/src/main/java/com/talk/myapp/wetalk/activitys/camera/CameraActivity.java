package com.talk.myapp.wetalk.activitys.camera;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.talk.myapp.wetalk.R;
import com.talk.myapp.wetalk.tasks.PictureTakenAsyncTask;
import com.talk.myapp.wetalk.utils.Consts;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraActivity extends Activity implements Camera.PictureCallback,TextureView.SurfaceTextureListener,SensorEventListener,
View.OnClickListener{

    //拍照按钮
    private ImageView takePictureImageView;
    //切换摄像头
    private LinearLayout cameraChangeButton;

    //相机对象
    private Camera camera;

    //相机角度
    private int cameraOri;

    //0代表前置摄像头，1代表后置摄像头
    private int cameraPosition = 1;

    // 手机X轴和Y轴的重力加速度
    private float gravityX, gravityY;

    //预览控件
    private TextureView textureView;

    //传感器类
    private SensorManager sensorManager;

    // 相机预览尺寸和控件尺寸的长宽
    private int previewWidth, previewHeight, textureWidth, textureHeight;

    // 是否是从其他Activity回退回来
    private boolean isBackToThis = true;

    // 等待框
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        //获取控件
        takePictureImageView = findViewById(R.id.take_photo_image_view);
        cameraChangeButton = findViewById(R.id.camera_change_linear_layout);
        textureView = findViewById(R.id.camera_texture);

        //添加监听
        takePictureImageView.setOnClickListener(this);
        cameraChangeButton.setOnClickListener(this);
        textureView.setSurfaceTextureListener(this);

        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //拍照按钮可用
        takePictureImageView.setEnabled(true);
        // StartAutoFocus();
        //添加重力加速度传感器
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        if(sensorManager != null) {
            //如果传感器不为空，注册重力加速度传感器
            sensorManager.registerListener
                    (this,
                            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                            SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //注销重力加速度传感器
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.d("onPictureTaken", "onPictureTaken start ");
        // 显示等待框
        progressBar.setVisibility(View.VISIBLE);
        if (camera != null) {
            // 停止预览
            camera.stopPreview();

            // 计算手机重力加速度，用于判断照片方向
            int ori = cameraOri;
            if (Math.abs(gravityX) > Math.abs(gravityY)) {
                if (gravityX > 0) {
                    ori -= 90;
                }
                if (gravityX < 0) {
                    ori += 90;
                }
            } else {
                if (gravityY < 0) {
                    ori -= 180;
                }
            }
            // 处理拍摄的图片
            new PictureTakenAsyncTask(this, progressBar, data, textureWidth, textureHeight).execute(ori);

        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("onSensorChanged", "onSensorChanged start ");
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // 通过回调不断修正传感器的值
            gravityX = event.values[0];
            gravityY = event.values[1];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("onAccuracyChanged", "onAccuracyChanged start");
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d("onSTA", "onSurfaceTextureAvailable:start ");
        initCamera(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d("onSTSC", "onSurfaceTextureSizeChanged: start");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d("onSTD", "onSurfaceTextureDestroyed: start");
        if(camera != null){
            //销毁相机
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        Log.d("onSTU", "onSurfaceTextureUpdated: start");
    }

    private void initCamera(SurfaceTexture surface) {
        //如果可用相机为空，跳过下面的操作
        if (Camera.getNumberOfCameras() == 0) {
            //TODO
            return;
        }
        if (camera == null) {
            camera = Camera.open();
            if (camera == null)
                camera = Camera.open(0);
        }

        setCameraDisplayOrientation();

        if (isBackToThis) {
            textureWidth = textureView.getWidth();
            textureHeight = textureView.getHeight();
            isBackToThis = false;
        }
        Camera.Parameters param = camera.getParameters();
        Camera.Size previewSize = param.getPreviewSize();
        if (previewSize.width >= textureView.getHeight() && previewSize.height >= textureView.getWidth()) {
            previewWidth = previewSize.width;
            previewHeight = previewSize.height;

            textureView.setLayoutParams(new FrameLayout.LayoutParams(previewHeight, previewWidth, Gravity.CENTER));
        } else {
            List<Camera.Size> previewSizes = param.getSupportedPreviewSizes();
            Collections.sort(previewSizes, new Comparator<Camera.Size>() {
                @Override
                public int compare(Camera.Size o1, Camera.Size o2) {
                    return Integer.compare(o2.height * o2.width, o1.height * o1.width);
                }
            });
            Camera.Size maxSize = previewSizes.get(0);
            if (maxSize.width >= textureView.getHeight() && maxSize.height >= textureView.getWidth()) {
                for (Camera.Size size : previewSizes) {
                    if (size.width >= textureView.getHeight() && size.height >= textureView.getWidth()) {
                        previewWidth = size.width;
                        previewHeight = size.height;
                    } else {
                        break;
                    }
                }
                param.setPreviewSize(previewWidth, previewHeight);
                camera.setParameters(param);
                textureView.setLayoutParams(new FrameLayout.LayoutParams(previewHeight, previewWidth, Gravity.CENTER));
            } else {
                previewWidth = maxSize.width;
                previewHeight = maxSize.height;
                float exceptWidth, exceptHeight, rate;
                if ((float) maxSize.height / textureView.getWidth() > (float) maxSize.width / textureView.getHeight()) {
                    exceptHeight = textureView.getHeight();
                    rate = (float) textureView.getHeight() / maxSize.width;
                    exceptWidth = rate * maxSize.height;
                } else {
                    exceptWidth = textureView.getWidth();
                    rate = (float) textureView.getWidth() / maxSize.height;
                    exceptHeight = rate * maxSize.width;
                }
                param.setPreviewSize(previewWidth, previewHeight);
                camera.setParameters(param);
                textureView.setLayoutParams(new FrameLayout.LayoutParams((int) exceptWidth, (int) exceptHeight, Gravity.CENTER));
            }
        }
        List<Camera.Size> pictureSizes = param.getSupportedPictureSizes();
        Collections.sort(pictureSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                return Integer.compare(o1.height * o1.width, o2.height * o2.width);
            }
        });
        param.setPictureSize(pictureSizes.get(pictureSizes.size() - 1).width, pictureSizes.get(pictureSizes.size() -
                1).height);
        for (Camera.Size size : pictureSizes) {
            if (size.width > Consts.ORIGIN_MAX && size.height > Consts.ORIGIN_MAX) {
                param.setPictureSize(size.width, size.height);
                break;
            }
        }
        camera.setParameters(param);

        try {
            setCamFocusMode();
            camera.setPreviewTexture(surface);
            camera.startPreview();
        } catch (IOException ex) {
            //            Console.WriteLine(ex.Message);
        }
    }

    /**
     * 设置相机展示方向
     */
    private void setCameraDisplayOrientation() {
        //获取相机信息对象
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(0,cameraInfo);

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Log.d("rotation", "rotation: "+rotation);
        int degrees = 0;
        switch (rotation){
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
        if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
            cameraOri = (cameraInfo.orientation + degrees) % 360;
            cameraOri = (360 - cameraOri) % 360;
            Log.d("cameraOri", "cameraOri: "+cameraOri);
        }else {
            cameraOri = (cameraInfo.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(cameraOri);
    }

    /**
     *  设置相机自动对焦
     */
    private void setCamFocusMode(){
        if (null == camera)
        {
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        //TODO
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        camera.setParameters(parameters);
    }

    /**
     *  摄像头转换时重置相机
     */
    private void reloadCamera(int i){
        camera = Camera.open(i);
        Camera.Parameters param = camera.getParameters();
        param.setPreviewSize(previewWidth, previewHeight);
        camera.setParameters(param);
        camera.setDisplayOrientation(cameraOri);
        setCamFocusMode();
        try {
            camera.setPreviewTexture(textureView.getSurfaceTexture());
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //拍照按钮点击
            case R.id.take_photo_image_view:
                Log.d("Take picture", "click success");
                //注销重力加速度传感器
                sensorManager.unregisterListener(CameraActivity.this);
                takePictureImageView.setEnabled(false);
                //TODO 设备是否有相机，及相机权限等的处理（前后摄像头？）
                camera.takePicture(null, null, CameraActivity.this);
                break;
            //切换摄像头按钮
            case R.id.camera_change_linear_layout:
                int cameraCount = 0;
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
                //TODO 摄像头数量大于1，显示切换摄像头按钮，否则不显示。
                for (int i = 0; i < cameraCount; i++) {
                    //得到每一个摄像头的信息
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraPosition == 1) {
                        //现在是后置，变更为前置
                        //CAMERA_FACING_FRONT=1
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            //CAMERA_FACING_FRONT前置，CAMERA_FACING_BACK后置
                            camera.stopPreview();
                            camera.release();
                            camera = null;
                            //打开当前选中的摄像头
                            reloadCamera(i);
                            cameraPosition = 0;
                            break;
                        }
                    }
                    //现在是前置，变更为后置
                    else {
                        camera.stopPreview();
                        camera.release();
                        camera = null;
                        //打开当前选中的摄像头
                        reloadCamera(i);
                        cameraPosition = 1;
                        break;
                    }
                }
                break;
            //美颜按钮
            case R.id.photo_editor_linear_layout:
                //TODO
                break;
            //返回或显示消息数量按钮
            case R.id.back_image_view:
                //TODO
                break;
            //显示相册按钮
            case R.id.photo_select_image_view:
                //TODO

                break;
        }
    }

}