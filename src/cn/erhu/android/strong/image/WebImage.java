package cn.erhu.android.strong.image;

import android.content.Context;
import android.graphics.Bitmap;

public class WebImage implements StrongImage {

    private static WebImageCache webImageCache;

    private String url;

    public WebImage(String url) {
        this.url = url;
    }

    public Bitmap getBitmap(Context context) {
        // Don't leak context
        if (webImageCache == null) {
            webImageCache = new WebImageCache(context);
        }

        if (url != null) {
            return webImageCache.get(context, url);
        }

        return null;
    }

    public static void removeFromCache(String url) {
        if (webImageCache != null) {
            webImageCache.remove(url);
        }
    }
}
