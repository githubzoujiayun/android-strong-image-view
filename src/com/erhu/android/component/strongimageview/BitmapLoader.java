package com.erhu.android.component.strongimageview;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BitmapLoader
 * <p/>
 * User: erhu
 * Date: 14-2-23
 * Time: 下午8:35
 */
public enum BitmapLoader {
    INSTANCE;

    private static final int JOB_SIZE = 20;
    private final Map<String, LoadImageTask> jobs = new ConcurrentHashMap<String, LoadImageTask>(JOB_SIZE);

    public void loadBitmap(final StrongImageView _strong_image_view) {
        final String key = _strong_image_view.getImageUrl();
        if (jobs.containsKey(key)) {
            jobs.get(key).cancel();
        }

        LoadImageTask task = new LoadImageTask(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg != null) {
                    Bitmap bitmap = (Bitmap) msg.obj;
                    if (bitmap != null) {
                        _strong_image_view.setImageBitmap(bitmap);
                    }
                }
            }
        }, _strong_image_view);

        jobs.put(_strong_image_view.getImageUrl(), task);

        StrongImageThreadPool.getExecutor().execute(task);
    }
}
