package com.talk.myapp.wetalk.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;

/**
 * Created by 407973884 on 2017/12/18.
 */

public final class FileUtils {
    public static Bitmap tempBitmap;

    public static void destroyBitmap() {
        if (tempBitmap != null) {
            tempBitmap.recycle();
            tempBitmap = null;
        }
    }
    /**
     * 文件根目录
     *
     * @return 文件目录
     */
    public static String getFileDir() {
        return Environment.getExternalStorageDirectory().getPath() + File.separator + "wetalk";
    }

    /**
     * 根据Uri获取图片路径
     *
     * @param context Context
     * @param uri     Uri
     * @return 图片路径
     */
    public static String getFilePathFromUri(Context context, Uri uri){
        //异常判断
        if(context == null || uri == null){
            return "";
        }
        final String scheme = uri.getScheme();
        String data = null;
        //scheme为空
        if(scheme == null) {
            data = uri.getPath();
        }
        //scheme以 file: 开头
        else if(ContentResolver.SCHEME_FILE.equals(scheme)){
            data = uri.getPath();
        }
        //scheme以 content: 开头
        else if(ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
}
