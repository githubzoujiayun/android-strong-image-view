package com.erhu.android.component.strongimageview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加载歌曲的任务
 * <p/>
 * User: erhu
 * Date: 14-3-18
 * Time: 下午4:37
 */
public class LoadImageTask implements Runnable {

    private boolean canceled = false;
    private Handler handler;
    private StrongImageView strongImageView;
    private String fileName;

    // concurrent Set :)
    private static Set<String> diskCache = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    // 可用内存的最大值，使用内存超出这个值会引起OutOfMemory异常。
    // LruCache通过构造函数传入缓存值（KB）
    // 使用最大可用内存值的1/8作为缓存的大小
    private static LruCache<String, SoftReference<Bitmap>> lruImgCache =
            new LruCache<String, SoftReference<Bitmap>>((int) Runtime.getRuntime().maxMemory() / 1024 / 8) {
                @Override
                protected int sizeOf(String key, SoftReference<Bitmap> value) {
                    Bitmap bitmap = value.get();
                    if (bitmap != null) {
                        return bitmap.getByteCount() / 1024;
                    }
                    return 0;
                }
            };

    public LoadImageTask(Handler _handler, StrongImageView _strong_image_view, String file_name) {
        handler = _handler;
        strongImageView = _strong_image_view;
        fileName = file_name;
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
            bitmap = loadImageFromFileSystem(strongImageView, fileName);
        }

        // 无缓存数据，下载图片
        if (bitmap == null) {
            HttpUtils.downloadImg(strongImageView.getImageUrl(), fileName);
            bitmap = loadImageFromFileSystem(strongImageView, fileName);
        }

        // send msg
        if (bitmap != null) {
            Message msg = handler.obtainMessage();
            msg.obj = bitmap;
            handler.sendMessage(msg);
        }
    }


    private Bitmap loadImageFromFileSystem(StrongImageView _strong_image_view, String _file_name) {
        if (_strong_image_view == null) {
            return null;
        }

        Bitmap bitmap = null;

        String image_url = _strong_image_view.getImageUrl();
        int min_width = _strong_image_view.getMinWidth();
        int min_height = _strong_image_view.getMinHeight();
        try {
            if (diskCache.contains(image_url)) {
                // 不用检查文件是否存在
                bitmap = buildAdaptiveBitmapFromFilePath(_file_name, min_width, min_height);
                lruImgCache.put(image_url, new SoftReference<Bitmap>(bitmap));
            } else {
                // in file system.
                if (new File(_file_name).exists()) {
                    if (StrongImageViewConstants.IS_DEBUG) {
                        Log.d(StrongImageViewConstants.TAG, "load image from local file");
                    }
                    bitmap = buildAdaptiveBitmapFromFilePath(_file_name, min_width, min_height);

                    lruImgCache.put(image_url, new SoftReference<Bitmap>(bitmap));
                    diskCache.add(image_url);
                }
            }
        } catch (IllegalStateException e) {
            // ignore illegalStateException of LRU
        }
        return bitmap;
    }

    private Bitmap buildAdaptiveBitmapFromFilePath(final String _file_path, int _min_width, int _min_height) {
        if (_file_path == null) {
            if (StrongImageViewConstants.IS_DEBUG) {
                Log.e(StrongImageViewConstants.TAG, "filePath is null when load image from file system.");
            }
            return null;
        }

        Bitmap bitmap = null;

        // 设置 inJustDecodeBounds = true, decodeFile时不分配内存空间，但可计算出原始图片的长度和宽度，
        // 然后根据需要显示的长宽，计算出inSampleSize;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(_file_path, opts);
        int sample_size = genSampleSizeByOptions(opts, _min_width, _min_height);

        int max_try_loop = 10;
        while (max_try_loop-- >= 0) {
            try {
                if (StrongImageViewConstants.IS_DEBUG) {
                    //Log.d(StrongImageViewConstants.TAG, "Now, sample_size = " + sample_size);
                }
                BitmapFactory.Options options = buildBFOptions(sample_size);
                bitmap = BitmapFactory.decodeFile(_file_path, options);

                if (bitmap == null) {
                    if (StrongImageViewConstants.IS_DEBUG) {
                        Log.d(StrongImageViewConstants.TAG, "bitmap = null when sample_size = " + sample_size);
                    }
                    sample_size *= 2;
                } else {
                    break;
                }
            } catch (OutOfMemoryError error) {
                if (StrongImageViewConstants.IS_DEBUG) {
                    Log.d(StrongImageViewConstants.TAG, String.format("OutOfMemory error, when sample_size = %d, sample_size *= 2 and tray again.", sample_size));
                }
                sample_size *= 2;
            }
        }

        return bitmap;
    }

    private int genSampleSizeByOptions(BitmapFactory.Options _opts, int _min_width, int _min_height) {
        int sample_size = 1;

        // 如果用户设置了最小宽和最小高
        if (_min_width != 0 && _min_height != 0) {
            while (_opts.outWidth / _min_width >= (sample_size * 2)
                    && _opts.outHeight / _min_height >= (sample_size * 2)) {
                if (StrongImageViewConstants.IS_DEBUG) {
                    Log.d(StrongImageViewConstants.TAG, "bitmap is too large, when sample_size = " + sample_size + ", enlarge it.");
                }
                sample_size *= 2;
            }
            if (StrongImageViewConstants.IS_DEBUG) {
                /*
                Log.d(StrongImageViewConstants.TAG, String.format("视图尺寸: {width:%d, height:%d}", _min_width, _min_height));
                Log.d(StrongImageViewConstants.TAG, String.format("图片尺寸: {width:%d, height:%d}", _opts.outWidth, _opts.outHeight));
                Log.d(StrongImageViewConstants.TAG, String.format("压缩比率: %d", sample_size));
                */
            }
        }
        return sample_size;
    }

    private BitmapFactory.Options buildBFOptions(int _sample_size) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = _sample_size;
        return options;
    }

    public void cancel() {
        this.canceled = true;
    }

    private Bitmap buildAdaptiveBitmapFromImgUrl(final String _image_url, int minWidth, int minHeight) {
        InputStream input_stream = HttpUtils.getStreamFromURL(_image_url);
        if (input_stream == null) {
            if (StrongImageViewConstants.IS_DEBUG) {
                Log.e(StrongImageViewConstants.TAG, "inputStream is null when download image from web.");
            }
            return null;
        }

        Bitmap bitmap = null;
        int max_try_loop = 10;

        try {
            // 设置 inJustDecodeBounds = true, decodeFile时不分配内存空间，但可计算出原始图片的长度和宽度，
            // 然后根据需要显示的长宽，计算出inSampleSize;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input_stream, null, opts);
            int sample_size = genSampleSizeByOptions(opts, minWidth, minHeight);

            while (max_try_loop-- >= 0) {
                try {
                    if (StrongImageViewConstants.IS_DEBUG) {
                        Log.d(StrongImageViewConstants.TAG, "Now, sample_size = " + sample_size);
                    }
                    BitmapFactory.Options options = buildBFOptions(sample_size);
                    bitmap = BitmapFactory.decodeStream(input_stream, null, options);

                    if (bitmap == null) {
                        if (StrongImageViewConstants.IS_DEBUG) {
                            Log.d(StrongImageViewConstants.TAG, "bitmap = null when sample_size = " + sample_size);
                        }
                        input_stream.close();

                        // todo
                        // not good strategy, but how to copy the stream? I failed.
                        input_stream = HttpUtils.getStreamFromURL(_image_url);
                        sample_size *= 2;
                    } else {
                        break;
                    }
                } catch (OutOfMemoryError error) {
                    if (StrongImageViewConstants.IS_DEBUG) {
                        Log.d(StrongImageViewConstants.TAG, String.format("OutOfMemory error, when sample_size = %d, sample_size *= 2 and tray again.", sample_size));
                    }
                    sample_size *= 2;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input_stream != null) {
                try {
                    input_stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

}
