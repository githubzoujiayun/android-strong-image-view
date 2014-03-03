package com.erhu.android.component;

import com.erhu.android.component.strongimageview.AbstractBitmapLoader;

/**
 * BitmapLoader
 * <p/>
 * User: erhu
 * Date: 14-2-26
 * Time: 下午2:45
 */
public class BitmapLoader extends AbstractBitmapLoader {

    private static AbstractBitmapLoader instance;

    private BitmapLoader() {
    }

    public static AbstractBitmapLoader getInstance() {
        if (instance == null) {
            instance = new BitmapLoader();
        }
        return instance;
    }

    @Override
    protected void setImageCacheNameStrategy() {
        super.imgNameStrategy = new ImageCacheNameStrategyImpl();
    }

    @Override
    protected void setImageCacheDirStrategy() {
        super.imgCacheDirStrategy = new ImageCacheDirStrategyImpl();
    }

}
