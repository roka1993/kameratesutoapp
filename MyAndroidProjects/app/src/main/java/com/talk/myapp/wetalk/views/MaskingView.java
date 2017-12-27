package com.talk.myapp.wetalk.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 407973884 on 2017/12/18.
 */

public class MaskingView extends AppCompatImageView {

    // 涂鸦线数量变化监听器
    private OnPathCountChangeListener listener;

    // 画笔对象
    private Paint paint;
    // 当前涂鸦线对象
    private Path path;
    // 涂鸦线数组
    private List<Path> paths;
    // 涂鸦线颜色数组
    private List<Integer> pathColors;
    //当前画笔颜色
    private int currentPaintColor;

    private PointF offsetPoint = new PointF();
    private float zoomScale = 1;
    private float minScale;

    // 原图长宽
    private int originWidth;
    private int originHeight;

    // 窗口长宽
    private int windowWidth;
    private int windowHeight;

    private Matrix matrix = new Matrix();

    private boolean isFirst = true;

    /**
     * 构造函数
     *
     * @param context 上下文对象
     */
    public MaskingView(Context context) {
        super(context);

        // 初始化控件
        initWidget();
    }

    /**
     * 构造函数
     *
     * @param context 上下文对象
     * @param attrs   属性对象
     */
    public MaskingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 初始化控件
        initWidget();
    }

    /**
     * 构造函数
     *
     * @param context      上下文对象
     * @param attrs        属性对象
     * @param defStyleAttr 样式
     */
    public MaskingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 初始化控件
        initWidget();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            windowWidth = widthSize;
        } else {
            windowWidth = 100;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            windowHeight = heightSize;
        } else {
            windowHeight = 100;
        }

        setMeasuredDimension(windowWidth, windowHeight);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);

        // 获取Bitmap长宽
        if (bm != null) {
            originWidth = bm.getWidth();
            originHeight = bm.getHeight();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 判断是否时第一次初始化
        if (isFirst) {
            initWidgetPosition();

            minScale = zoomScale;
            isFirst = false;
        }

        // 绘制所有涂鸦线
        canvas.concat(matrix);

        final int size = paths.size();
        for (int i = 0; i < size; i++) {
            // 设置画笔颜色
            paint.setColor(pathColors.get(i));
            // 绘制涂鸦线
            canvas.drawPath(paths.get(i), paint);
        }
        // 当前Path不为空，绘制该Path
        if (!path.isEmpty()) {
            paint.setColor(currentPaintColor);
            canvas.drawPath(path, paint);
        }
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        setWillNotDraw(false);

        paint = new Paint();
        path = new Path();
        paths = new ArrayList<>();
        pathColors = new ArrayList<>();
        currentPaintColor = Color.BLACK;

        // 设置画笔对象
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(50);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        setScaleType(ScaleType.MATRIX);
        // 开启软件绘制，硬件绘制在某些机子上可能会有问题
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    /**
     * 撤销（如果可以的话）
     */
    public void undo() {
        path.reset();
        if (canUndo()) {
            paths.remove(paths.size() - 1);
            pathColors.remove(pathColors.size() - 1);
            invalidate();

            if (listener != null) {
                // 涂鸦线数量发生变更
                listener.onPathCountChange();
            }
        }
    }

    /**
     * 判断是否可以撤销
     *
     * @return true - 可以回退; false - 不能回退
     */
    public boolean canUndo() {
        return paths.size() > 0;
    }

    /**
     * 初始化图片位置
     */
    private void initWidgetPosition() {
        // 计算出x和y方向上刚好缩放到控件大小的最适放大倍数
        float scaleX = (float) windowWidth / originWidth;
        float scaleY = (float) windowHeight / originHeight;

        // x和y方向上放大倍数均大于1（即图片实际长宽均小于控件大小）
        if (scaleX >= 1 && scaleY >= 1) {
            // 设置当前缩放倍数为1
            zoomScale = 1;
            matrix.reset();

            // 无需缩放，将图片至于控件中间
            float offsetX = (windowWidth - originWidth * zoomScale) / 2;
            float offsetY = (windowHeight - originHeight * zoomScale) / 2;
            offsetPoint.set(offsetX, offsetY);
            matrix.postScale(zoomScale, zoomScale, 0, 0);
            matrix.postTranslate(offsetX, offsetY);
            setImageMatrix(matrix);
        }
        // 如果在x方向上所需的放大倍数比y方向上的小（即x方向上的缩小倍数比y方向上的大）
        else if (scaleX < scaleY) {
            // 将x方向上的缩放倍数作为当前缩放倍数
            zoomScale = scaleX;
            matrix.reset();

            // 缩放之后，将图片至于控件中间
            float offsetY = (windowHeight - originHeight * zoomScale) / 2;
            offsetPoint.set(0, offsetY);
            matrix.postScale(scaleX, scaleX, 0, 0);
            matrix.postTranslate(0, offsetY);
            setImageMatrix(matrix);
        }
        // 如果在x方向上所需的放大倍数比y方向上的大（即x方向上的缩小倍数比y方向上的小）
        else {
            // 将y方向上的缩放倍数作为当前缩放倍数
            zoomScale = scaleY;
            matrix.reset();

            // 缩放之后，将图片至于控件中间
            float offsetX = (windowWidth - originWidth * zoomScale) / 2;
            offsetPoint.set(offsetX, 0);
            matrix.postScale(scaleY, scaleY, 0, 0);
            matrix.postTranslate(offsetX, 0);
            setImageMatrix(matrix);
        }
    }

    /**
     * 重置图片
     */
    public void resetMatrix() {
        matrix.reset();
        setImageMatrix(matrix);
    }

    /**
     * 获取当前涂鸦线数组，并且清空控件中的数组
     *
     * @return 当前涂鸦线数组
     */
    public List<Path> getAndRemoveAllPath() {
        List<Path> tempPath = new ArrayList<>(paths);
        paths.clear();
        return tempPath;
    }

    /**
     * 获取当前缩放倍数
     *
     * @return 当前缩放倍数
     */
    public float getImageViewScale() {
        return zoomScale;
    }

    /**
     * 获取图片Bitmap对象（不包含任何涂鸦线）
     *
     * @return Bitmap对象
     */
    public Bitmap getImageBitmap() {
        BitmapDrawable drawable = (BitmapDrawable) getDrawable();
        if (drawable != null) {
            Bitmap temp = drawable.getBitmap();
            if (temp != null) {
                return temp;
            }
        }
        return null;
    }

    /**
     * 获取图片Bitmap对象（包含涂鸦线）
     *
     * @return Bitmap对象
     */
    public Bitmap getViewBitmap(){
        Bitmap bitmap = Bitmap.createBitmap(originWidth,originHeight,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }

    /**
     * 设置画笔颜色
     *
     * @param red   R
     * @param green G
     * @param blue  B
     */
    public void setPaintColor(int red, int green, int blue) {
        this.currentPaintColor = Color.rgb(red, green, blue);
    }

    /**
     * 设置涂鸦线数量变化监听器
     *
     * @param listener 涂鸦线数量变化监听器
     */
    public void setOnPathCountChangeListener(OnPathCountChangeListener listener) {
        this.listener = listener;
    }

    /**
     * 涂鸦线数量变化监听器
     */
    public interface OnPathCountChangeListener {
        void onPathCountChange();
    }
}
