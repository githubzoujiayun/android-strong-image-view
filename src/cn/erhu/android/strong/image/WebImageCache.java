package cn.erhu.android.strong.image;

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

    private boolean diskCacheEnabled = false;

    private static ImageCacheKeyStrategy cacheKeyStrategy = new ImageCacheKeyStrategyImpl();

    public WebImageCache() {
        memoryCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>();

        File outFile = new File(DISK_CACHE_PATH);
        outFile.mkdirs();

        diskCacheEnabled = outFile.exists();
    }

    public Bitmap get(final String _url, int _width, int _height, boolean _from_memory) {

        Bitmap bitmap = null;

        // 检查内存
        if (_from_memory) {
            bitmap = getBitmapFromMemory(_url);
        }

        // 检查磁盘中是否存在图片
        if (bitmap == null) {
            bitmap = getBitmapFromDisk(_url, _width, _height);

            // 磁盘中存在图片, 加入内存
            if (bitmap != null) {
                Log.d(TAG, String.format("从 '磁盘' 读取图片:%s", _url));
                memoryCache.put(cacheKeyStrategy.genKey(_url), new SoftReference<Bitmap>(bitmap));
                return bitmap;
            } else {
                // 从网络下载图片数据, 保存到文件中
                downloadImgFromNetwork(_url);
                bitmap = getBitmapFromDisk(_url, _width, _height);
                if (bitmap != null) {
                    Log.d(TAG, String.format("从 '网络' 下载图片:%s", _url));
                    memoryCache.put(cacheKeyStrategy.genKey(_url), new SoftReference<Bitmap>(bitmap));
                    return bitmap;
                }
            }
        } else {
            Log.d(TAG, String.format("从 '内存' 读取图片:%s", _url));
        }

        return bitmap;
    }

    /**
     * 删除指定图片(包括内存和磁盘)
     */
    public void remove(String url) {
        if (url == null) return;

        memoryCache.remove(cacheKeyStrategy.genKey(url));

        File f = new File(DISK_CACHE_PATH, cacheKeyStrategy.genKey(url));
        if (f.exists() && f.isFile()) {
            f.delete();
        }
    }

    /**
     * 删除所有图片(包括内存和磁盘)
     */
    public void clear() {
        memoryCache.clear();

        File cachedFileDir = new File(DISK_CACHE_PATH);
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
        SoftReference<Bitmap> softRef = memoryCache.get(cacheKeyStrategy.genKey(url));

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
    private Bitmap getBitmapFromDisk(String _url, int _width, int _height) {
        Bitmap bit_map = null;

        if (diskCacheEnabled) {
            String file_path = getFilePath(_url);
            File file = new File(file_path);

            if (file.exists()) {
                bit_map = decodeFile(file_path, _width, _height);
            }
        }
        return bit_map;
    }

    /**
     * 获取缓存文件路径
     *
     * @param url
     */
    private String getFilePath(String url) {
        return DISK_CACHE_PATH + cacheKeyStrategy.genKey(url);
    }

    /**
     * 从网络取图片
     */
    private void downloadImgFromNetwork(final String _url) {

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
    private static Bitmap decodeFile(String _file_path_name, int _width, int _height) {

        Log.d(TAG, String.format("StrongImageView 实际尺寸: {width:%d, height:%d}", _width, _height));

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(_file_path_name, opts);

        Log.d(TAG, String.format("图片实际尺寸: {width:%d, height:%d}", opts.outWidth, opts.outHeight));

        float scale = 1;
        // 图片实际尺寸是要显示尺寸的2倍以上
        while (opts.outWidth / (scale * 2) >= _width && opts.outHeight / (scale * 2) >= _height) {
            scale *= 2;
        }
        Log.d(TAG, String.format("压缩比率: %d", (int) scale));

        opts.inSampleSize = (int) scale;
        opts.inJustDecodeBounds = false;

        try {
            return BitmapFactory.decodeFile(_file_path_name, opts);
        } catch (OutOfMemoryError err) {
            Log.d(TAG, "Oh~~~ 图片太大了, 我们来压缩一下吧.");

            // 减半宽度和高度
            return decodeFile(_file_path_name, _width / 2, _height / 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
