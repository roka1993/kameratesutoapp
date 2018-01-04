package com.talk.myapp.wetalk.activitys.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.talk.myapp.wetalk.R;
import com.talk.myapp.wetalk.adapters.EmojiGridViewAdapter;
import com.talk.myapp.wetalk.tasks.PictureSaveAsyncTask;
import com.talk.myapp.wetalk.utils.Consts;
import com.talk.myapp.wetalk.utils.FileUtils;
import com.talk.myapp.wetalk.views.MaskingView;

import java.util.ArrayList;
import java.util.List;

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

    private GridView gridView;
    private EmojiGridViewAdapter gridViewAdapter;
    //横屏滑动的gridview
    HorizontalScrollView horizontalScrollView;
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
        gridView = findViewById(R.id.gridview);
        horizontalScrollView = findViewById(R.id.scrollView);
        horizontalScrollView.setHorizontalScrollBarEnabled(false);// 隐藏滚动条
        setValue();

        sendButton.setOnClickListener(this);
        timerButton.setOnClickListener(this);
        maskingButton.setOnClickListener(this);
        maskingView.setOnClickListener(this);
        maskingView.setOnPathCountChangeListener(this);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gridViewAdapter.setSelectedItemIndex(position);
                gridViewAdapter.notifyDataSetChanged();
            }
        });
    }

    //GridView值的设定
    private void setValue() {
        //加载表情图片数据
        List dataList = new ArrayList();
        dataList.add(R.drawable.emoji_barrage_1);
        dataList.add(R.drawable.emoji_barrage_2);
        dataList.add(R.drawable.emoji_barrage_3);
        dataList.add(R.drawable.emoji_barrage_4);
        dataList.add(R.drawable.emoji_barrage_5);
        dataList.add(R.drawable.emoji_barrage_6);
        dataList.add(R.drawable.emoji_barrage_7);
        dataList.add(R.drawable.emoji_barrage_8);
        dataList.add(R.drawable.emoji_barrage_9);

        gridViewAdapter= new EmojiGridViewAdapter(this,dataList);
        gridView.setAdapter(gridViewAdapter);

        //结合emoji_list_item.xml 设置gridview的宽度
        int itemWidth = (int)((60+15*2) * this.getResources().getDisplayMetrics().density);
        int marginWidth = (int)(30 * this.getResources().getDisplayMetrics().density);
        int itemSize = dataList.size();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(itemSize * itemWidth+marginWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

        gridView.setLayoutParams(params);
        gridView.setNumColumns(itemSize);
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
