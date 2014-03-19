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
class StrongImageThreadPool {

    private static ExecutorService threadPool;
    private static ExecutorService singleThreadPool;

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

    public static ExecutorService getSingleThreadExecutor() {
        if (singleThreadPool == null) {
            synchronized (StrongImageThreadPool.class) {
                if (singleThreadPool == null) {
                    singleThreadPool = Executors.newSingleThreadExecutor();
                }
            }
        } else {
            if (singleThreadPool.isShutdown()) {
                synchronized (StrongImageThreadPool.class) {
                    if (singleThreadPool.isShutdown()) {
                        singleThreadPool = Executors.newSingleThreadExecutor();
                    }
                }
            }
        }

        return singleThreadPool;
    }


    public static void shutdown() {
        threadPool.shutdown();
        singleThreadPool.shutdown();
    }

    ;
}
