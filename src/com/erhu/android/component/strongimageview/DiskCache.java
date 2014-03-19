package com.erhu.android.component.strongimageview;

import android.util.Log;
import com.erhu.android.component.BitmapLoader;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * DiskCache
 * <p/>
 * User: erhu
 * Date: 14-3-19
 * Time: 下午12:22
 */
enum DiskCache {

    INSTANCE(BitmapLoader.getInstance().getDir(), BitmapLoader.getInstance().imgNameStrategy);

    String dir;
    ImageCacheNameStrategy imgNameStrategy;

    private DiskCache(String _dir, ImageCacheNameStrategy _strategy) {
        this.dir = _dir;
        this.imgNameStrategy = _strategy;

        // 每5分钟执行一次冗余图片删除操作
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        ses.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // 删除引用
                Iterator iterator = diskCacheList.iterator();
                while (diskCacheList.size() > SIZE && iterator.hasNext()) {
                    int last_index = diskCacheList.size() - 1;
                    CacheEntry last_entry = diskCacheList.get(last_index);

                    diskCacheList.remove(last_index);
                    diskCacheUrlSet.remove(last_entry.url);
                    diskCacheFileNameSet.remove(imgNameStrategy.getName(last_entry.url));

                    if (StrongImageViewConstants.IS_DEBUG) {
                        Log.d(StrongImageViewConstants.TAG, "del image ref");
                    }
                }

                // 获取缓存图片文件夹中所有的图片
                File[] file_array = new File(BitmapLoader.getInstance().getDir()).listFiles();
                // 删除物理文件
                for (File f : file_array) {
                    if (!diskCacheFileNameSet.contains(f.getName())) {
                        // 删除图片文件
                        boolean b = f.delete();
                        if (StrongImageViewConstants.IS_DEBUG) {
                            Log.d(StrongImageViewConstants.TAG, "del image file:" + b);
                        }
                    }
                }
            }
        }, 10, 40, TimeUnit.SECONDS);
    }

    private static final int SIZE = 10;

    // concurrent Set :)
    // 删除冗余图片时，使用MAP快速确定，某图片是否需要保留
    private Set<String> diskCacheUrlSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private Set<String> diskCacheFileNameSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    //private Map<String, Boolean> diskCacheMap = new ConcurrentHashMap<String, Boolean>(SIZE);

    static {
    }

    // 用于存储
    private List<CacheEntry> diskCacheList = Collections.synchronizedList(new ArrayList<CacheEntry>(SIZE));

    // synchronized 保证 map 和 list 同步；
    public synchronized void add(String url) {
        CacheEntry entry = new CacheEntry();
        entry.url = url;
        diskCacheList.add(entry);
        diskCacheUrlSet.add(url);
        diskCacheFileNameSet.add(imgNameStrategy.getName(url));
    }

    public synchronized boolean contains(String url) {
        //return diskCacheMap.get(url) != null;
        return diskCacheUrlSet.contains(url);
    }

    private String getFileName(String url) {
        return dir + imgNameStrategy.getName(url);
    }

    class CacheEntry {
        String url;
    }
}
