package com.talk.myapp.wetalk.activitys.camera;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.talk.myapp.wetalk.R;
import com.talk.myapp.wetalk.tasks.PictureSaveAsyncTask;
import com.talk.myapp.wetalk.utils.Consts;
import com.talk.myapp.wetalk.utils.FileUtils;
import com.talk.myapp.wetalk.views.MaskingView;

public class PictureMaskingActivity extends Activity implements View.OnClickListener ,MaskingView.OnPathCountChangeListener{

    private MaskingView maskingView;

    // Bitmap对象
    private Bitmap bitmap;

    // 等待框
    private ProgressBar progressBar;

    // 是否在处理中
    public boolean isProcessing = false;

    // 文件Uri(接收从本应用文件夹中选取到图片)
    private Uri uri;

    //按钮
    private LinearLayout sendButton,timerButton,maskingButton,timerSelectArea;
    private RadioGroup timeSelectRadioGroup;
    //TODO 画弹出框并加动画

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_masking);
        initWidget();
        // 是否是相机拍摄的照片
        boolean isFromCamera = getIntent().getBooleanExtra(Consts.ExtraIsFromCamera, false);
        // 旋转角度
        int rotateDegree = getIntent().getIntExtra(Consts.ExtraRotateDegree, 90);

        // 如果是相机拍摄而来，读取全局Bitmap
        if (isFromCamera)
            bitmap = FileUtils.tempBitmap;
        // 如果是相册而来，读取文件
        else {
            uri = Uri.parse(getIntent().getStringExtra(Consts.ExtraPictureUri));
            bitmap = BitmapFactory.decodeFile(uri.getPath());
        }

        // 加载图片到控件上
        loadPictureToWidget(rotateDegree);

        //TODO 取色框，取色
        // 计算出提示框到高度
//        View maskingHint = findViewById(R.id.masking_maskingHint);
//        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        maskingHint.measure(w, h);
//        int height = maskingHint.getMeasuredHeight();
//        jzMaskingView.setOffsetHeight(height);
//
//        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        if (inflater != null) {
//            final View menuView = inflater.inflate(R.layout.layout_colorpicker, null);
//            ((JZColorPicker) menuView.findViewById(R.id.masking_colorPicker))
//                    .setOnColorChangeListener(new JZColorPicker.OnColorChangeListener() {
//                        @Override
//                        public void onColorChange(int red, int green, int blue) {
//                            jzMaskingView.setPaintColor(red, green, blue);
//                        }
//                    });
//            popupWindow = new PopupWindow(menuView);
//            popupWindow.setWidth(ActionBar.LayoutParams.WRAP_CONTENT);
//            popupWindow.setHeight(ActionBar.LayoutParams.WRAP_CONTENT);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 回收Bitmap
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
         maskingView = findViewById(R.id.masking_maskingView);
         sendButton = findViewById(R.id.picture_send_linear_layout);
         timerButton = findViewById(R.id.picture_timer_linear_layout);
         maskingButton = findViewById(R.id.masking_linear_layout);
         timerSelectArea = findViewById(R.id.timer_select_area);
         timeSelectRadioGroup = findViewById(R.id.timer_select_radio_group);
         progressBar = findViewById(R.id.progressBar);

         sendButton.setOnClickListener(this);
         timerButton.setOnClickListener(this);
         maskingButton.setOnClickListener(this);
        maskingView.setOnClickListener(this);
         maskingView.setOnPathCountChangeListener(this);
    }
    /**
     * 加载图片到控件上
     *
     * @param rotateDegree 旋转角度
     */
    private void loadPictureToWidget(int rotateDegree) {
        if (bitmap != null) {
            Matrix matrix = new Matrix();
            if (rotateDegree != 0) {
                matrix.setRotate(rotateDegree);
            }

            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            Bitmap destBitmap;

            // 如果图片大于最大尺寸，进行剪裁
            if (bitmapWidth > Consts.ORIGIN_MAX || bitmapHeight > Consts.ORIGIN_MAX) {
                float scale;

                if (bitmapWidth > bitmapHeight) {
                    scale = (float) Consts.ORIGIN_MAX / bitmap.getWidth();
                    bitmapWidth = Consts.ORIGIN_MAX;
                    bitmapHeight = (int) (scale * bitmap.getHeight());
                } else {
                    scale = (float) Consts.ORIGIN_MAX / bitmap.getHeight();
                    bitmapWidth = (int) (scale * bitmap.getWidth());
                    bitmapHeight = Consts.ORIGIN_MAX;
                }

                matrix.setScale(scale, scale);
            }

            destBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
            maskingView.setImageBitmap(destBitmap);
        } else {
            Log.d("PictureMaskingActivity", "Masking OnCreate bitmap is null");
        }
    }
    @Override
    public void onClick(View v) {
        // 如果现在系统正在处理中，忽略点击事件
        if (isProcessing) {
            return;
        }

        switch (v.getId()){
            case R.id.masking_maskingView:
                if(timerSelectArea.getVisibility()==View.VISIBLE){
                    //显示时间选择区域时，如果选择了时间，则隐藏时间选择区域，并保存选择的浏览时长，显示其它按钮
                                       //如果没选择时间，则弹出提示框，设置选择时间
                    switch (timeSelectRadioGroup.getCheckedRadioButtonId()){
                        case R.id.time_7_radio_button:
                            //TODO 保存选择的浏览时长
                            Toast.makeText(this,"保存选择的浏览时长7秒",Toast.LENGTH_SHORT).show();
                            timerSelectArea.setVisibility(View.GONE);
                            sendButton.setVisibility(View.VISIBLE);
                            timerButton.setVisibility(View.VISIBLE);
                            maskingButton.setVisibility(View.VISIBLE);
                            break;
                        case R.id.time_10_radio_button:
                            Toast.makeText(this,"保存选择的浏览时长10秒",Toast.LENGTH_SHORT).show();
                            timerSelectArea.setVisibility(View.GONE);
                            sendButton.setVisibility(View.VISIBLE);
                            timerButton.setVisibility(View.VISIBLE);
                            maskingButton.setVisibility(View.VISIBLE);
                            break;
                        case R.id.time_15_radio_button:
                            Toast.makeText(this,"保存选择的浏览时长15秒",Toast.LENGTH_SHORT).show();
                            timerSelectArea.setVisibility(View.GONE);
                            sendButton.setVisibility(View.VISIBLE);
                            timerButton.setVisibility(View.VISIBLE);
                            maskingButton.setVisibility(View.VISIBLE);
                            break;
                        default:
                            Toast.makeText(this,"请选择浏览时长",Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                break;
            case R.id.picture_send_linear_layout:
                //TODO 发送图片，关闭activity。。。保存图片。图片要记每一笔的动作时间（在别的地方写）

                //保存图片  异步？？？？？
                new PictureSaveAsyncTask(this, maskingView, progressBar).execute(uri);
                //finish();
                break;
            case R.id.picture_timer_linear_layout:
                //TODO 隐藏画面的按钮，显示时间选择框
                sendButton.setVisibility(View.GONE);
                timerButton.setVisibility(View.GONE);
                maskingButton.setVisibility(View.GONE);
                timerSelectArea.setVisibility(View.VISIBLE);
                break;
            case R.id.masking_linear_layout:
                //TODO 显示画笔，确认按钮，删除按钮，撤销按钮？
                break;
        }

    }

    @Override
    public void onPathCountChange() {
//        if (maskingView.canUndo()) {
//            undoButton.setEnabled(true);
//        } else {
//            undoButton.setEnabled(false);
//        }
    }

    @Override
    public void onBackPressed() {
//        if (popupWindow.isShowing())
//            popupWindow.dismiss();
//        else {
//            // 点击回退键，删除全局Bitmap
            FileUtils.destroyBitmap();

            super.onBackPressed();
//        }
    }
}
