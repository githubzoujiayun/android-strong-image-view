package cn.erhu.android.strong.image;

/**
 * URL 与 存储图片名称的对应关系
 * <p/>
 * User: erhu
 * Date: 13-4-24
 * Time: 下午5:13
 */
public interface ImageCacheKeyStrategy {
    String genKey(String _url);
}
