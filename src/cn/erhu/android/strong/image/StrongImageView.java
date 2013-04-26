package cn.erhu.android.strong.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StrongImageView extends ImageView {

    private static final String TAG = "StrongImageView";

    private static final int LOADING_THREADS = 4;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(LOADING_THREADS);

    private StrongImageTask currentTask;
    private int minWidth;
    private int minHeight;

    public StrongImageView(Context context) {
        super(context);
    }

    public StrongImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        minHeight = this.getSuggestedMinimumHeight();
        minWidth = this.getSuggestedMinimumWidth();
    }

    public StrongImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public StrongImageView buildSmartImageView(View viewById) {
        return (StrongImageView) viewById;
    }

    // Helpers to set image by URL
    public void setImageUrl(String url) {
        setImage(new WebImage(url, minWidth, minHeight, true));
    }

    /**
     * 设置图片URL
     *
     * @param url
     * @param _from_memory 是否读取内存数据
     *                     从内存读取时, 可能读取到的是压缩后的数据, 因此在显示大图时, 设置_from_memory = false;
     */
    public void setImageUrl(String url, boolean _from_memory) {
        setImage(new WebImage(url, minWidth, minHeight, _from_memory));
    }

    public void setImageUrl(String url, StrongImageTask.OnCompleteListener completeListener) {
        setImage(new WebImage(url), completeListener);
    }

    public void setImageUrl(String url, final Integer fallbackResource) {
        setImage(new WebImage(url), fallbackResource);
    }

    public void setImageUrl(String url, final Integer fallbackResource, StrongImageTask.OnCompleteListener completeListener) {
        setImage(new WebImage(url), fallbackResource, completeListener);
    }

    public void setImageUrl(String url, final Integer fallbackResource, final Integer loadingResource) {
        setImage(new WebImage(url), fallbackResource, loadingResource);
    }

    public void setImageUrl(String url, final Integer fallbackResource, final Integer loadingResource, StrongImageTask.OnCompleteListener completeListener) {
        setImage(new WebImage(url), fallbackResource, loadingResource, completeListener);
    }

    // Set image using StrongImage object
    public void setImage(final StrongImage image) {
        setImage(image, null, null, null);
    }

    public void setImage(final StrongImage image, final StrongImageTask.OnCompleteListener completeListener) {
        setImage(image, null, null, completeListener);
    }

    public void setImage(final StrongImage image, final Integer fallbackResource) {
        setImage(image, fallbackResource, fallbackResource, null);
    }

    public void setImage(final StrongImage image, final Integer fallbackResource, StrongImageTask.OnCompleteListener completeListener) {
        setImage(image, fallbackResource, fallbackResource, completeListener);
    }

    public void setImage(final StrongImage image, final Integer fallbackResource, final Integer loadingResource) {
        setImage(image, fallbackResource, loadingResource, null);
    }

    public void setImage(final StrongImage image, final Integer fallbackResource, final Integer loadingResource, final StrongImageTask.OnCompleteListener completeListener) {
        // Set a loading resource
        if (loadingResource != null) {
            setImageResource(loadingResource);
        }

        // Cancel any existing tasks for this image view
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }

        // 启动任务去下载图片
        currentTask = new StrongImageTask(image);
        currentTask.setOnCompleteHandler(new StrongImageTask.OnCompleteHandler() {
            @Override
            public void onComplete(Bitmap bitmap) {
                if (bitmap != null) {
                    setImageBitmap(bitmap);
                } else {
                    // Set fallback resource
                    if (fallbackResource != null) {
                        setImageResource(fallbackResource);
                    }
                }

                if (completeListener != null) {
                    completeListener.onComplete();
                }
            }
        });

        // Run the task in a thread pool
        threadPool.execute(currentTask);
    }

    public static void cancelAllTasks() {
        threadPool.shutdownNow();
        threadPool = Executors.newFixedThreadPool(LOADING_THREADS);
    }

    private int parseStringDpiToInt(String _dpi) {
        return Integer.parseInt(_dpi.substring(0, _dpi.length() - 5));
    }
}