package com.erhu.android.component.strongimageview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * AsyncBitmapLoader
 * <p/>
 * User: erhu
 * Date: 14-2-23
 * Time: 下午8:35
 */
public abstract class AbstractBitmapLoader {

    private static final int TASK_SIZE = 20;
    private LruCache<String, SoftReference<Bitmap>> lruImgCache = null;
    private final Map<String, Runnable> taskMap;
    private static final String dir = StorageUtil.getInstance().getImgDir();
    protected ImageCacheNameStrategy imgNameStrategy;

    protected abstract void setImageCacheNameStrategy();

    public AbstractBitmapLoader() {
        int max_memory = (int) Runtime.getRuntime().maxMemory();

        // ?
        int cache_size = max_memory / 8;
        lruImgCache = new LruCache<String, SoftReference<Bitmap>>(cache_size) {
            @Override
            protected int sizeOf(String key, SoftReference<Bitmap> value) {
                Bitmap bitmap = value.get();
                if (bitmap != null) {
                    return bitmap.getRowBytes() * bitmap.getHeight();
                }
                return super.sizeOf(key, value);
            }
        };

        taskMap = new HashMap<String, Runnable>(TASK_SIZE);
        setImageCacheNameStrategy();
    }

    public void downloadBitmap(final StrongImageView _strong_image_view,
                               String _url) {
        if (_strong_image_view == null || _url == null ||
                _url.trim().equals("")) {
            return;
        }

        int min_width = _strong_image_view.getMinWidth();
        int min_height = _strong_image_view.getMinHeight();

        if (!taskMap.containsKey(_url)) {
            DownloadImgTask task = new DownloadImgTask(new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg != null) {
                        Bitmap bitmap = (Bitmap) msg.obj;
                        if (bitmap != null) {
                            _strong_image_view.setImageBitmap(bitmap);
                        }
                    }
                }
            }, _url, min_width, min_height);

            synchronized (taskMap) {
                taskMap.put(_url, task);
            }

            ThreadPool.getExecutor().execute(task);
        }
    }

    class DownloadImgTask implements Runnable {

        private Handler handler;
        private String url;
        private int width;
        private int height;

        public DownloadImgTask(Handler _handler, String url, int minWidth, int minHeight) {
            handler = _handler;
            this.url = url;
            this.width = minWidth;
            this.height = minHeight;
        }

        @Override
        public void run() {
            Bitmap bitmap = buildAdaptiveBitmapFromImgUrl(url, width, height);
            if (bitmap != null) {
                writeDataToFile(bitmap, url);
            }
            synchronized (taskMap) {
                taskMap.remove(url);
            }

            lruImgCache.put(url, new SoftReference<Bitmap>(bitmap));

            // send msg
            Message msg = handler.obtainMessage();
            msg.obj = bitmap;
            handler.sendMessage(msg);
        }
    }


    public Bitmap loadBitmap(final String _image_url, final int _min_width, final int _min_height) {

        if (_image_url == null || _image_url.trim().isEmpty()) {
            return null;
        }

        // in cache
        SoftReference<Bitmap> reference = lruImgCache.get(_image_url);

        if (reference != null) {
            Bitmap bitmap = reference.get();
            if (bitmap != null) {
                if (StrongImageViewConstants.IS_DEBUG) {
                    Log.d(StrongImageViewConstants.TAG, "load image from cache");
                }
                return bitmap;
            }
        } else {

            // in file system.
            String bitmap_name = imgNameStrategy.getName(_image_url);
            File[] cached_files = new File(dir).listFiles();
            int index = 0;

            if (null != cached_files) {
                for (; index < cached_files.length; index++) {
                    // file existed
                    if (bitmap_name.equals(cached_files[index].getName())) {
                        break;
                    }
                }
                if (index < cached_files.length) {
                    if (StrongImageViewConstants.IS_DEBUG) {
                        Log.d(StrongImageViewConstants.TAG, "load image from local file");
                    }
                    Bitmap bitmap = buildAdaptiveBitmapFromFilePath(dir + bitmap_name, _min_width, _min_height);
                    lruImgCache.put(_image_url, new SoftReference<Bitmap>(bitmap));
                    return bitmap;
                }
            }
        }
        return null;
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
                    Log.d(StrongImageViewConstants.TAG, "Now, sample_size = " + sample_size);
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
                Log.d(StrongImageViewConstants.TAG, String.format("视图尺寸: {width:%d, height:%d}", _min_width, _min_height));
                Log.d(StrongImageViewConstants.TAG, String.format("图片尺寸: {width:%d, height:%d}", _opts.outWidth, _opts.outHeight));
                Log.d(StrongImageViewConstants.TAG, String.format("压缩比率: %d", sample_size));
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

    /**
     * save image data to file
     *
     * @param _bitmap
     * @param _image_url
     */
    private void writeDataToFile(Bitmap _bitmap, String _image_url) {
        File cache_dir = new File(dir);
        if (!cache_dir.exists()) {
            cache_dir.mkdirs();
        }

        FileOutputStream fos = null;
        try {
            // create file
            File bitmap_file = new File(dir + imgNameStrategy.getName(_image_url));
            if (!bitmap_file.exists()) {
                bitmap_file.createNewFile();
            }

            fos = new FileOutputStream(bitmap_file);
            _bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
