package com.erhu.android.component.strongimageview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

/**
 * 针对非StrongImageView控件的一些操作
 * <p/>
 * User: erhu
 * Date: 14-3-21
 * Time: 下午12:15
 */
public class StrongImageViewHelper {

    // 最多执行缩小操作的次数
    private static final int MAX_TRY_LOOP = 10;

    /**
     * 下载图片
     *
     * @param url
     */
    public static void downloadImgFromWeb(String url) {
        // 图片文件，会被选择性地自动删除
        downloadImgFromWeb(url, true);
    }

    public static void downloadImgFromWeb(String url, boolean auto_del_file/*是否自动删除文件*/) {
        String file_name = StrongImageViewHelper.getFileNameByUrl(url, auto_del_file);
        HttpUtils.downloadImg(url, file_name);
    }

    public static Bitmap getBitmapFromFileByUrl(String url, int width, int height) {
        return getBitmapFromFileByUrl(url, width, height, null);
    }

    public static Bitmap getBitmapFromFileByUrl(String _image_url,
                                                int _min_width,
                                                int _min_height,
                                                LruCache<String, SoftReference<Bitmap>> _lru_img_cache) {
        // 先找会自动删除的图片文件
        String file_name = getFileNameByUrl(_image_url, true);
        if (!new File(file_name).exists()) {
            // 再找不自动删除的图片文件
            file_name = getFileNameByUrl(_image_url, false);
            if (!new File(file_name).exists()) {
                return null;
            }
        }

        Bitmap bitmap = null;

        try {
            if (DiskCache.INSTANCE.contains(_image_url)) {
                // 不用检查文件是否存在
                bitmap = getBitmapByPath(file_name, _min_width, _min_height);
                if (_lru_img_cache != null) {
                    _lru_img_cache.put(_image_url, new SoftReference<Bitmap>(bitmap));
                }
            } else {
                // in file system.
                if (new File(file_name).exists()) {
                    if (StrongImageViewConstants.IS_DEBUG) {
                        Log.d(StrongImageViewConstants.TAG, "load image from local file");
                    }
                    bitmap = getBitmapByPath(file_name, _min_width, _min_height);
                    if (bitmap != null) {
                        if (_lru_img_cache != null) {
                            _lru_img_cache.put(_image_url, new SoftReference<Bitmap>(bitmap));
                        }
                        DiskCache.INSTANCE.add(_image_url);
                    }
                }
            }
        } catch (IllegalStateException e) {
            // ignore illegalStateException of LRU
        }
        return bitmap;

    }

    public static Bitmap getBitmapFromFileByView(StrongImageView _strong_image_view,
                                                 LruCache<String, SoftReference<Bitmap>> lruImgCache) {
        if (_strong_image_view == null) {
            return null;
        }
        return getBitmapFromFileByUrl(_strong_image_view.getImageUrl(),
                _strong_image_view.getMinWidth(),
                _strong_image_view.getMinHeight(), lruImgCache);
    }

    public static Bitmap getBitmapByPath(final String _file_path) {
        return getBitmapByPath(_file_path, 0, 0);
    }

    public static Bitmap getBitmapByPath(final String _file_path, int _min_width, int _min_height) {
        if (_file_path == null) {
            if (StrongImageViewConstants.IS_DEBUG) {
                Log.e(StrongImageViewConstants.TAG, "filePath is null when load image from file system.");
            }
            return null;
        }

        if (!new File(_file_path).exists()) {
            if (StrongImageViewConstants.IS_DEBUG) {
                Log.e(StrongImageViewConstants.TAG, "file not exists.");
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

        int max_try_loop = MAX_TRY_LOOP;
        while (max_try_loop-- >= 0) {
            try {
                BitmapFactory.Options options = buildBFOptions(sample_size);
                bitmap = BitmapFactory.decodeFile(_file_path, options);

                if (bitmap == null) {
                    if (StrongImageViewConstants.IS_DEBUG) {
                        Log.d(StrongImageViewConstants.TAG, "bitmap = null when sample_size = " + sample_size);
                    }
                    sample_size = incSampleSize(sample_size);
                } else {
                    if (StrongImageViewConstants.IS_DEBUG) {
                        Log.d(StrongImageViewConstants.TAG, "bitmap is ok, when sample_size = " + sample_size);
                    }
                    break;
                }
            } catch (OutOfMemoryError error) {
                if (StrongImageViewConstants.IS_DEBUG) {
                    Log.d(StrongImageViewConstants.TAG, String.format("OutOfMemory error, when sample_size = %d, sample_size *= 2 and tray again.", sample_size));
                }
                sample_size = incSampleSize(sample_size);
            }
        }

        return bitmap;
    }

    private static int genSampleSizeByOptions(BitmapFactory.Options _opts, int _min_width, int _min_height) {
        int sample_size = 1;

        // 如果用户设置了最小宽和最小高
        if (_min_width != 0 && _min_height != 0) {
            while (_opts.outWidth / _min_width >= incSampleSize(sample_size)
                    && _opts.outHeight / _min_height >= incSampleSize(sample_size)) {
                if (StrongImageViewConstants.IS_DEBUG) {
                    Log.d(StrongImageViewConstants.TAG, "bitmap is too large, when sample_size = " + sample_size + ", enlarge it.");
                }
                sample_size = incSampleSize(sample_size);
            }
            if (StrongImageViewConstants.IS_DEBUG) {
                if (sample_size > 2) {
                    Log.d(StrongImageViewConstants.TAG, String.format("视图尺寸: {width:%d, height:%d}", _min_width, _min_height));
                    Log.d(StrongImageViewConstants.TAG, String.format("图片尺寸: {width:%d, height:%d}", _opts.outWidth, _opts.outHeight));
                    Log.d(StrongImageViewConstants.TAG, String.format("压缩比率: %d", sample_size));
                }
            }
        }
        return sample_size;
    }

    private static BitmapFactory.Options buildBFOptions(int _sample_size) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = _sample_size;
        return options;
    }

    /**
     * 这个方法被折叠，效率太低
     */
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
                        sample_size = incSampleSize(sample_size);
                    } else {
                        break;
                    }
                } catch (OutOfMemoryError error) {
                    if (StrongImageViewConstants.IS_DEBUG) {
                        Log.d(StrongImageViewConstants.TAG, String.format("OutOfMemory error, when sample_size = %d, sample_size *= 2 and tray again.", sample_size));
                    }
                    sample_size = incSampleSize(sample_size);
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

    private static int incSampleSize(int _old_size) {
        // 加载速度优先
        //return _old_size * 2;
        // 图片精度优先
        return _old_size + 1;
    }

    public static String getFileNameByView(StrongImageView _strong_image_view) {
        return getFileNameByUrl(_strong_image_view.getImageUrl(), _strong_image_view.autoDel());
    }

    public static String getFileNameByUrl(String url, boolean auto_del) {
        String file_name = StrongImageViewConfig.imgCacheFileNameStrategy.getName(url);

        // 对于不自动删除的图片，名称前加下划线
        // todo to improve.
        if (!auto_del) {
            file_name = "_" + file_name;
        }
        return StrongImageViewConfig.dir + file_name;
    }
}