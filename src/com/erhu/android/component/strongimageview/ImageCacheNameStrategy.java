package com.erhu.android.component.strongimageview;

/**
 * URL 与 存储图片名称的对应关系
 * <p/>
 * User: erhu
 * Date: 14-2-26
 * Time: 下午2:39
 */
public interface ImageCacheNameStrategy {
    String getName(String _url);
}
