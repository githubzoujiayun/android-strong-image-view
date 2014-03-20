package com.erhu.android.component.strongimageview;


import com.erhu.android.component.StorageUtil;

/**
 * Config
 * <p/>
 * User: erhu
 * Date: 14-3-20
 * Time: 下午8:12
 */
public interface StrongImageViewConfig {

    /**
     * URL 与 缓存文件名称的对应关系
     */
    ImageCacheNameStrategy imgCacheFileNameStrategy = new ImageCacheNameStrategy() {
        @Override
        public String getName(String _url) {
            return String.valueOf(_url.hashCode());
        }
    };

    /**
     * 图片存储位置
     */
    String dir = StorageUtil.getInstance().makeImgDir("tmp");

    /**
     * 设置最多缓存的文件数
     */
    int CACHE_IMG_FILE_COUNT = 1000;
}
