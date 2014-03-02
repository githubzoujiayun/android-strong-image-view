package com.erhu.android.component.strongimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import com.erhu.android.component.BitmapLoader;

/**
 * StrongImageView
 * <p/>
 * User: erhu
 * Date: 14-2-25
 * Time: 上午8:04
 */
public class StrongImageView extends ImageView {

    private String imageUrl;
    private int minWidth;
    private int minHeight;
    private AbstractBitmapLoader loader = BitmapLoader.getInstance();

    public StrongImageView(Context context) {
        super(context);
    }

    public StrongImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        minHeight = this.getSuggestedMinimumHeight();
        minWidth = this.getSuggestedMinimumWidth();
    }

    public StrongImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setImageUrl(String _url) {
        imageUrl = _url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void loadImage(String _url) {
        setImageUrl(_url);
        loadImage();
    }

    private void loadImage() {
        Bitmap bitmap = loader.loadBitmap(this);
        if (bitmap != null) {
            setImageBitmap(bitmap);
        } else {
            if (StrongImageViewConstants.IS_DEBUG) {
                Log.d(StrongImageViewConstants.TAG, "downloadBitmap, url = " + imageUrl);
            }
            loader.downloadBitmap(this);
        }
    }

    public int getMinWidth() {
        return minWidth;
    }

    public int getMinHeight() {
        return minHeight;
    }
}
