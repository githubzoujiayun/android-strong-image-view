package com.erhu.android.component.strongimageview;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AsyncBitmapLoader
 * <p/>
 * User: erhu
 * Date: 14-2-23
 * Time: 下午8:35
 */
public abstract class AbstractBitmapLoader {

    private static final int JOB_SIZE = 20;

    private final Map<String, LoadImageTask> jobs;
    private static String dir;
    protected ImageCacheNameStrategy imgNameStrategy;
    protected ImageCacheDirStrategy imgCacheDirStrategy;

    protected abstract void setImageCacheNameStrategy();
    protected abstract void setImageCacheDirStrategy();

    public AbstractBitmapLoader() {
        setImageCacheNameStrategy();
        setImageCacheDirStrategy();

        jobs = new ConcurrentHashMap<String, LoadImageTask>(JOB_SIZE);
        dir = imgCacheDirStrategy.dir();
    }

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
        }, _strong_image_view, getFileName(_strong_image_view));

        jobs.put(_strong_image_view.getImageUrl(), task);

        StrongImageThreadPool.getExecutor().execute(task);
    }

    private String getFileName(StrongImageView _strong_image_view) {
        String file_name = imgNameStrategy.getName(_strong_image_view.getImageUrl());
        // 对于不自动删除的图片，名称前加下划线
        if (!_strong_image_view.autoDel()) {
            file_name = "_" + file_name;
        }
        return dir + file_name;
    }

    public String getDir() {
        return dir;
    }
}
