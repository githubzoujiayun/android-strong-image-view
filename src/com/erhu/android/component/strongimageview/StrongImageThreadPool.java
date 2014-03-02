package com.erhu.android.component.strongimageview;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * StrongImageThreadPool
 * <p/>
 * User: erhu
 * Date: 14-3-1
 * Time: 下午4:24
 */
public class StrongImageThreadPool {

    private static ExecutorService threadPool;

    public static ExecutorService getExecutor() {
        if (threadPool == null) {
            synchronized (StrongImageThreadPool.class) {
                if (threadPool == null) {
                    // when short time async task, use cachedThreadPool.
                    threadPool = Executors.newCachedThreadPool();
                }
            }
        } else {
            if (threadPool.isShutdown()) {
                synchronized (StrongImageThreadPool.class) {
                    if (threadPool.isShutdown()) {
                        threadPool = Executors.newCachedThreadPool();
                    }
                }
            }
        }

        return threadPool;
    }
}
