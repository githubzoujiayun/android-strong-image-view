package com.erhu.android.component.strongimageview;

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

}
