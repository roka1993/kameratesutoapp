package com.talk.myapp.wetalk.tasks;

import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.talk.myapp.wetalk.activitys.CameraActivity;
import com.talk.myapp.wetalk.activitys.LoginFinishActivity;
import com.talk.myapp.wetalk.activitys.PictureMaskingActivity;
import com.talk.myapp.wetalk.utils.Consts;
import com.talk.myapp.wetalk.utils.FileUtils;
import com.talk.myapp.wetalk.views.MaskingView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * Created by 407973884 on 2017/12/20.
 */

public final class PictureSaveAsyncTask extends AsyncTask<Uri,Integer,Boolean> {
    // 弱引用防止内存泄漏
    private WeakReference<PictureMaskingActivity> activityWeakReference;
    private WeakReference<MaskingView> maskingViewWeakReference;
    private WeakReference<ProgressBar> progressBarWeakReference;

    private PictureSaveAsyncTask() {
    }

    /**
     * 公有构造函数
     *
     * @param activity      宿主Activity
     * @param maskingView 自定义涂鸦控件
     * @param progressBar   ProgressBar
     */
    public PictureSaveAsyncTask(PictureMaskingActivity activity, MaskingView maskingView, ProgressBar progressBar) {
        activityWeakReference = new WeakReference<>(activity);
        maskingViewWeakReference = new WeakReference<>(maskingView);
        progressBarWeakReference = new WeakReference<>(progressBar);
    }
    @Override
    protected void onPreExecute() {
        maskingViewWeakReference.get().resetMatrix();

        progressBarWeakReference.get().setVisibility(View.VISIBLE);
        activityWeakReference.get().isProcessing = true;
    }

    @Override
    protected Boolean doInBackground(Uri... uris) {
        try {
            if (uris == null || uris.length < 1)
                saveToFile(null);
            else
                saveToFile(uris[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        progressBarWeakReference.get().setVisibility(View.GONE);
        activityWeakReference.get().isProcessing = false;

        Intent intent = new Intent(activityWeakReference.get(), LoginFinishActivity.class);
        activityWeakReference.get().startActivity(intent);

        FileUtils.destroyBitmap();
    }
    /**
     * 保存为图片
     *
     * @param uri 文件Uri
     */
    private void saveToFile(Uri uri) {
        Bitmap bitmap = maskingViewWeakReference.get().getViewBitmap();

        if (bitmap == null) {
            Log.d("PictureSaveAsyncTask", "Masking GetFile bitmap is null");
            return;
        }

        final File file1 = new File(FileUtils.getFilePathFromUri(activityWeakReference.get(), uri));

        // 参数uri不为空
        if (uri != null) {
            // 删除原本文件
            final File file = new File(FileUtils.getFilePathFromUri(activityWeakReference.get(), uri));
            file.delete();
        }

        // 生成输出文件

        String fileName = new Date().getTime() + ".png";
        String pathName = FileUtils.getFileDir() + File.separator + fileName;
        File file = new File(pathName);

        // 文件如果不存在，创建文件
        if (!file.exists())
            file.getParentFile().mkdirs();

        try {
            FileOutputStream out = new FileOutputStream(file);
            // 将Bitmap绘制到文件中,压缩质量
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            // 释放Bitmap资源
            bitmap.recycle();
            // 关闭流
            out.close();
        } catch (IOException e) {
            // do nothing
        }
        //对相册进行刷新
        // 把刚保存的图片文件插入到系统相册
        try {
            MediaStore.Images.Media.insertImage(activityWeakReference.get().getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //相册更新
        activityWeakReference.get().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + pathName)));
    }
}
