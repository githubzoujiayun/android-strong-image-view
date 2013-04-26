package cn.erhu.android.strong.image;

/**
 * URL 与 缓存文件名称的对应关系
 * <p/>
 * User: erhu
 * Date: 13-4-24
 * Time: 下午5:13
 */
public class ImageCacheKeyStrategyImpl implements ImageCacheKeyStrategy {
    @Override
    public String genKey(String _url) {
        return String.valueOf(_url.hashCode());
    }
}
