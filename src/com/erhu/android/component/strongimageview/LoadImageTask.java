package com.erhu.android.component.strongimageview;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * very simple, u know.
 * <p/>
 * User: erhu
 * Date: 14-3-18
 * Time: 下午4:37
 */
class LoadImageTask implements Runnable {

    private boolean canceled = false;
    private Handler handler;
    private StrongImageView strongImageView;

    // 可用内存的最大值，使用内存超出这个值会引起OutOfMemory异常。
    // LruCache通过构造函数传入缓存值（KB）
    // 使用最大可用内存值的1/8作为缓存的大小
    private static LruCache<String, SoftReference<Bitmap>> lruImgCache =
            new LruCache<String, SoftReference<Bitmap>>((int) Runtime.getRuntime().maxMemory() / 1024 / 8) {
                @Override
                protected int sizeOf(String key, SoftReference<Bitmap> value) {
                    Bitmap bitmap = value.get();
                    if (bitmap != null) {
                        return bitmap.getHeight() * bitmap.getRowBytes() / 1024;
                    }
                    return 0;
                }
            };

    public LoadImageTask(Handler _handler, StrongImageView _strong_image_view) {
        handler = _handler;
        WeakReference<StrongImageView> ref = new WeakReference<StrongImageView>(_strong_image_view);
        strongImageView = ref.get();
    }

    @Override
    public void run() {
        if (strongImageView == null || canceled) {
            return;
        }

        Bitmap bitmap;
        // in cache
        SoftReference<Bitmap> reference = lruImgCache.get(strongImageView.getImageUrl());

        if (reference != null) {
            bitmap = reference.get();
            if (StrongImageViewConstants.IS_DEBUG) {
                if (bitmap != null) {
                    Log.d(StrongImageViewConstants.TAG, "load image from cache");
                }
            }
        } else {
            bitmap = StrongImageViewHelper.getBitmapFromFileByView(strongImageView, lruImgCache);
        }

        // 无缓存数据，下载图片
        if (bitmap == null) {
            if (StrongImageViewConstants.IS_DEBUG) {
                Log.d(StrongImageViewConstants.TAG, "download image from web....");
            }
            HttpUtils.downloadImg(strongImageView.getImageUrl(), StrongImageViewHelper.getFileNameByView(strongImageView));
            bitmap = StrongImageViewHelper.getBitmapFromFileByView(strongImageView, lruImgCache);
        }

        // send msg
        if (bitmap != null) {
            Message msg = handler.obtainMessage();
            msg.obj = bitmap;
            handler.sendMessage(msg);
        }
    }

    public void cancel() {
        this.canceled = true;
    }

}