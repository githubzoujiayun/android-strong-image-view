package com.erhu.android.component;

import com.erhu.android.component.strongimageview.ImageCacheDirStrategy;

public class ImageCacheDirStrategyImpl implements ImageCacheDirStrategy {
    @Override
    public String dir() {
        return StorageUtil.getInstance().getImgDir();
    }
}
