package com.erhu.android.component.strongimageview;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ThreadPool
 * <p/>
 * User: erhu
 * Date: 14-3-1
 * Time: 下午4:24
 */
public class ThreadPool {

    private static ExecutorService threadPool;

    public static ExecutorService getExecutor() {
        int pool_size = Runtime.getRuntime().availableProcessors() * 2 + 1;

        if (threadPool == null) {
            synchronized (ThreadPool.class) {
                if (threadPool == null) {
                    threadPool = Executors.newFixedThreadPool(pool_size);
                }
            }
        } else {
            if (threadPool.isShutdown()) {
                synchronized (ThreadPool.class) {
                    if (threadPool.isShutdown()) {
                        threadPool = Executors.newFixedThreadPool(pool_size);
                    }
                }
            }
        }

        return threadPool;
    }

    /**
     * 销毁这个线程池，有潜在的危险，但是。。。。 可以忽略
     */
    public static void destroyPool() {
        if (threadPool != null) {
            threadPool.shutdown();
            threadPool = null;
        }
    }

}
