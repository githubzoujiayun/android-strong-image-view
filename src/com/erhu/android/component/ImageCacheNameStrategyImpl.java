package com.erhu.android.component;

import com.erhu.android.component.strongimageview.ImageCacheNameStrategy;

/**
 * URL 与 缓存文件名称的对应关系
 * <p/>
 * User: erhu
 * Date: 14-2-26
 * Time: 下午2:40
 */
public class ImageCacheNameStrategyImpl implements ImageCacheNameStrategy {
    @Override
    public String getName(String _url) {
        return String.valueOf(_url.hashCode());
    }
}
