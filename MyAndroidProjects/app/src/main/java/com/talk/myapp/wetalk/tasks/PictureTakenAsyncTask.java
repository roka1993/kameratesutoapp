package com.talk.myapp.wetalk.tasks;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.talk.myapp.wetalk.activitys.camera.PictureMaskingActivity;
import com.talk.myapp.wetalk.utils.Consts;
import com.talk.myapp.wetalk.utils.FileUtils;

import java.lang.ref.WeakReference;

/**
 * Created by 407973884 on 2017/12/17.
 */

public class PictureTakenAsyncTask extends AsyncTask<Integer,Integer,Boolean> {

    // 图片旋转角度
    private int ori;

    private byte[] data;

    private int textureWidth, textureHeight;

    // 弱引用防止内存泄漏
    private WeakReference<Activity> activityWeakReference;
    private WeakReference<ProgressBar> progressBarWeakReference;

    private PictureTakenAsyncTask(){

    }

    /**
     * 公有构造函数
     *
     * @param activity      宿主Activity
     * @param progressBar   ProgressBar
     * @param data          相机返回二进制数组
     * @param textureWidth  预览框宽度
     * @param textureHeight 预览框高度
     */
    public PictureTakenAsyncTask(Activity activity, ProgressBar progressBar,byte[] data,int textureWidth,int textureHeight){
        this.activityWeakReference = new WeakReference<>(activity);
        this.progressBarWeakReference = new WeakReference<>(progressBar);
        this.data = data;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    protected Boolean doInBackground(Integer... params) {
        ori = params[0];

        Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
        float widthRate = (float) bitmap.getWidth() / textureHeight;
        float heightRate = (float) bitmap.getHeight() / textureWidth;
        float bitmapRate = widthRate < heightRate ? widthRate : heightRate;

        final Bitmap tempBitmap = Bitmap.createBitmap
                (
                        bitmap,
                (int) ((bitmap.getWidth() - textureHeight * bitmapRate) / 2),
                (int) ((bitmap.getHeight() - textureWidth * bitmapRate) / 2),
                (int) (textureHeight * bitmapRate),
                (int) (textureWidth * bitmapRate)
                );
        //控制图片大小
        int destWidth,destHeight;
        if(tempBitmap != null){
            destWidth = tempBitmap.getWidth();
            destHeight = tempBitmap.getHeight();
            if(destWidth > Consts.ORIGIN_MAX || destHeight > Consts.ORIGIN_MAX){
                if(destWidth > destHeight){
                    destWidth = Consts.ORIGIN_MAX;
                    destHeight = (int)((float)tempBitmap.getHeight()/tempBitmap.getWidth()*Consts.ORIGIN_MAX);
                }else {
                    destWidth = (int) (((float) Consts.ORIGIN_MAX / tempBitmap.getHeight()) * tempBitmap.getWidth());
                    destHeight = Consts.ORIGIN_MAX;
                }
            }
            FileUtils.tempBitmap = Bitmap.createScaledBitmap(tempBitmap, destWidth, destHeight, false);
        }
        bitmap.recycle();
        bitmap = null;

        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        // 跳转涂鸦页面
        Intent intent = new Intent(activityWeakReference.get(), PictureMaskingActivity.class);
        intent.putExtra(Consts.ExtraRotateDegree, ori);
        intent.putExtra(Consts.ExtraIsFromCamera, true);
        activityWeakReference.get().startActivity(intent);

        progressBarWeakReference.get().setVisibility(View.GONE);
    }
}
