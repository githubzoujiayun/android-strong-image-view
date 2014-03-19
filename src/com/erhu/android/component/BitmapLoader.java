package com.erhu.android.component;

import com.erhu.android.component.strongimageview.AbstractBitmapLoader;
import com.erhu.android.component.strongimageview.ImageCacheDirStrategy;
import com.erhu.android.component.strongimageview.ImageCacheNameStrategy;

/**
 * BitmapLoader
 * <p/>
 * User: erhu
 * Date: 14-2-26
 * Time: 下午2:45
 */
public class BitmapLoader extends AbstractBitmapLoader {

    private static BitmapLoader instance;

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
        // URL 与 缓存文件名称的对应关系
        super.imgNameStrategy = new ImageCacheNameStrategy() {

            @Override
            public String getName(String _url) {
                return String.valueOf(_url.hashCode());
            }
        };
    }

    @Override
    protected void setImageCacheDirStrategy() {
        /**
         * 图片存储位置
         */
        super.imgCacheDirStrategy = new ImageCacheDirStrategy() {

            @Override
            public String dir() {
                return StorageUtil.getInstance().getImgDir();
            }
        };
    }

}
