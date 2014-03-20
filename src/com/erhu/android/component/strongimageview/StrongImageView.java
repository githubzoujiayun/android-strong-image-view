package com.erhu.android.component.strongimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.erhu.android.component.BitmapLoader;
import com.erhu.android.component.R;

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
    private boolean autoDelFile;

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
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StrongImageView, defStyle, 0);
        int count = array.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = array.getIndex(i);
            if (attr == R.styleable.StrongImageView_auto_del) {
                // 默认自动删除
                autoDelFile = array.getBoolean(attr, true);
            }
        }
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
        BitmapLoader.getInstance().loadBitmap(this);
    }

    public int getMinWidth() {
        return minWidth;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public boolean autoDel() {
        return autoDelFile;
    }
}
