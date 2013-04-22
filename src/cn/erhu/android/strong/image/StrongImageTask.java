package cn.erhu.android.strong.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

public class StrongImageTask implements Runnable {

    private static final int BITMAP_READY = 0;

    private boolean cancelled = false;
    private OnCompleteHandler onCompleteHandler;
    private StrongImage image;
    private Context context;

    public static class OnCompleteHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bitmap bitmap = (Bitmap) msg.obj;
            onComplete(bitmap);
        }

        public void onComplete(Bitmap bitmap) {
        }

    }

    public abstract static class OnCompleteListener {
        public abstract void onComplete();
    }

    public StrongImageTask(Context _context, StrongImage _image) {
        this.image = _image;
        this.context = _context;
    }

    @Override
    public void run() {
        if (image != null) {
            complete(image.getBitmap(context));
            context = null;
        }
    }

    public void setOnCompleteHandler(OnCompleteHandler handler) {
        this.onCompleteHandler = handler;
    }

    public void cancel() {
        cancelled = true;
    }

    public void complete(Bitmap bitmap) {
        if (onCompleteHandler != null && !cancelled) {
            onCompleteHandler.sendMessage(onCompleteHandler.obtainMessage(BITMAP_READY, bitmap));
        }
    }
}