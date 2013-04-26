package cn.erhu.android.strong.image;

import android.graphics.Bitmap;

public class WebImage implements StrongImage {

    private static WebImageCache webImageCache;
    private int minHeight;
    private int minWidth;
    private boolean fromMemory;

    private String url;

    public WebImage(String url) {
        this.url = url;
    }

    public WebImage(String _url, int _min_width, int _min_height, boolean _from_memory) {
        this.url = _url;
        this.minWidth = _min_width;
        this.minHeight = _min_height;
        this.fromMemory = _from_memory;
    }

    public Bitmap getBitmap() {
        // Don't leak context
        if (webImageCache == null) {
            webImageCache = new WebImageCache();
        }

        if (url != null) {
            return webImageCache.get(url, minWidth, minHeight, fromMemory);
        }

        return null;
    }

    public static void removeFromCache(String url) {
        if (webImageCache != null) {
            webImageCache.remove(url);
        }
    }
}
