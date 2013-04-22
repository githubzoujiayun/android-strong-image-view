package cn.erhu.android.strong.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Image cache
 */
public class WebImageCache {

    private static final String TAG = "WebImageCache";

    private static final String DISK_CACHE_PATH = "/strong_web_image_cache/";

    private ConcurrentHashMap<String, SoftReference<Bitmap>> memoryCache;
    private String diskCachePath;
    private boolean diskCacheEnabled = false;

    // 重试下载图片次数
    private int retryTimes;
    private static final int maxRetryTimes = 3;

    public WebImageCache(Context context) {
        memoryCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>();

        Context app_context = context.getApplicationContext();
        diskCachePath = app_context.getCacheDir().getAbsolutePath() + DISK_CACHE_PATH;

        File outFile = new File(diskCachePath);
        outFile.mkdirs();

        diskCacheEnabled = outFile.exists();
    }

    public Bitmap get(Context _context, final String url) {
        // 重试(递归结束条件)
        if (retryTimes++ >= maxRetryTimes) {
            retryTimes = 0;
            return null;
        }

        // 检查内存中是否有图片
        Bitmap bitmap = getBitmapFromMemory(url);

        // 检查磁盘中是否存在图片
        if (bitmap == null) {
            bitmap = getBitmapFromDisk(_context, url);

            // 磁盘中存在图片, 加入内存
            if (bitmap != null) {
                memoryCache.put(getCacheKey(url), new SoftReference<Bitmap>(bitmap));
                return bitmap;
            } else {
                // 从网络下载图片数据, 保存到文件中
                downloadImgFromNetwork(url);
                // 递归调用, 重新获取图片
                return get(_context, url);
            }
        }

        return bitmap;
    }

    /**
     * 删除指定图片(包括内存和磁盘)
     */
    public void remove(String url) {
        if (url == null) return;

        memoryCache.remove(getCacheKey(url));

        File f = new File(diskCachePath, getCacheKey(url));
        if (f.exists() && f.isFile()) {
            f.delete();
        }
    }

    /**
     * 删除所有图片(包括内存和磁盘)
     */
    public void clear() {
        memoryCache.clear();

        File cachedFileDir = new File(diskCachePath);
        if (cachedFileDir.exists() && cachedFileDir.isDirectory()) {
            File[] cachedFiles = cachedFileDir.listFiles();
            for (File f : cachedFiles) {
                if (f.exists() && f.isFile()) {
                    f.delete();
                }
            }
        }
    }

    /**
     * 从内存取图片
     *
     * @param url
     */
    private Bitmap getBitmapFromMemory(String url) {
        Bitmap bitmap = null;
        SoftReference<Bitmap> softRef = memoryCache.get(getCacheKey(url));

        if (softRef != null) {
            bitmap = softRef.get();
        }

        return bitmap;
    }

    /**
     * 从磁盘取图片
     *
     * @param _url
     */
    private Bitmap getBitmapFromDisk(Context _context, String _url) {
        Bitmap bit_map = null;

        if (diskCacheEnabled) {
            String file_path = getFilePath(_url);
            File file = new File(file_path);

            if (file.exists()) {
                long file_size = file.length();
                bit_map = decodeFile(_context, file_path, file_size);
            }
        }
        return bit_map;
    }

    private String getFilePath(String url) {
        return diskCachePath + getCacheKey(url);
    }

    private String getCacheKey(String url) {
        if (url == null) {
            throw new RuntimeException("图片路径为空");
        } else {
            return url.replaceAll("[.:/,%?&=]", "+").replaceAll("[+]+", "+");
        }
    }

    /**
     * 从网络取图片
     */
    private void downloadImgFromNetwork(final String _url) {
        Log.d(TAG, "从网络下载图片： ".concat(_url));

        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        final HttpGet getRequest = new HttpGet(_url);

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + _url);
            } else {
                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream inputStream = null;
                    try {
                        inputStream = entity.getContent();
                        // 保存图片到本地
                        saveImgStreamToFile(getFilePath(_url), inputStream);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        entity.consumeContent();
                    }
                }
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            getRequest.abort();
            e.printStackTrace();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    /**
     * 保存图片文件到SD卡
     */
    private static void saveImgStreamToFile(String _file_path, InputStream inputStream) {
        FileOutputStream fos = null;

        try {
            // 创建文件流
            fos = new FileOutputStream(new File(_file_path));

            // 把图片数据压缩到流中
            byte[] bytes = new byte[1024];
            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                fos.write(bytes, 0, len);
            }

            fos.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据 图片文件路径 取BITMAP对象
     */
    private static Bitmap decodeFile(Context _context, String _file_path, long _file_size) {
        // 根据文件大小, 计算压缩比例.(默认值为1)
        int sample_size = Config.getImageSampleSize(_context, _file_size);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(_file_path, opts);
        opts.inSampleSize = sample_size;
        opts.inJustDecodeBounds = false;
        Bitmap bmp = null;

        try {
            bmp = BitmapFactory.decodeFile(_file_path, opts);
            return bmp;
        } catch (OutOfMemoryError err) {
            Log.d(TAG, "哦~~~ 图片太大了, 我们来压缩一下吧.");

            // 设置 inSampleSize = inSampleSize * 2
            // 只内存溢出一次, 保存当前文件尺寸的图片手机阀值, 下次提高压缩比;
            Config.setImageSampleSize(_context, _file_size, sample_size << 1);
            return decodeFile(_context, _file_path, _file_size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }
}
