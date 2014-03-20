package com.erhu.android.component.strongimageview;

import android.util.Log;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * DiskCache
 * <p/>
 * User: erhu
 * Date: 14-3-19
 * Time: 下午12:22
 */
enum DiskCache {

    INSTANCE;

    // 缓存时，图片名称命名规则
    private ImageCacheNameStrategy cacheFileNameStrategy = StrongImageViewConfig.imgCacheFileNameStrategy;

    // concurrent Set :)
    // 删除冗余图片时，快速定位图片是否在缓存中。
    private Set<String> diskCacheUrlSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    // 删除冗余图片时，按物理文件名称，快速定位图片。
    private Set<String> diskCacheFileNameSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    // 用于存储，当缓存图片数超标时，从链表尾部开始删除。
    private List<CacheEntry> diskCacheList = Collections.synchronizedList(
            new ArrayList<CacheEntry>(StrongImageViewConfig.CACHE_IMG_FILE_COUNT));

    private DiskCache() {
        if (dumpFile().exists()) {
            // 从DUMP文件读取数据（1分钟后，只一次）
            Executors.newScheduledThreadPool(1).schedule(
                    new Runnable() {
                        @Override
                        public void run() {
                            // 保存引用数据到文件中
                            ObjectMapper mapper = new ObjectMapper();
                            try {
                                List list = mapper.readValue(dumpFile(), ArrayList.class);
                                for (Object o : list) {
                                    HashMap<String, String> map = (HashMap<String, String>) o;
                                    CacheEntry entry = new CacheEntry();
                                    entry.url = map.get("url");
                                    addEntry(entry);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 1, TimeUnit.SECONDS
            );
        }

        // 2分钟后开始检查，然后每10分钟执行一次冗余图片删除操作。
        Executors.newScheduledThreadPool(1).
                scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        // 删除引用
                        Iterator iterator = diskCacheList.iterator();
                        while (diskCacheList.size() > StrongImageViewConfig.CACHE_IMG_FILE_COUNT
                                && iterator.hasNext()) {
                            int last_index = diskCacheList.size() - 1;
                            removeEntry(diskCacheList.get(last_index));

                            diskCacheList.remove(last_index);

                            if (StrongImageViewConstants.IS_DEBUG) {
                                Log.d(StrongImageViewConstants.TAG, "del image ref");
                            }
                        }

                        // 保存引用数据到文件中
                        // todo 使用jackson，大材小用。
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            mapper.writeValue(dumpFile(), diskCacheList);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // 获取缓存图片文件夹中所有的图片
                        // 下划线开头的文件是日志文件或者不自动管理的图片
                        final File[] file_array = new File(StrongImageViewConfig.dir).listFiles(
                                new FilenameFilter() {
                                    @Override
                                    public boolean accept(File dir, String filename) {
                                        return !filename.startsWith("_");
                                    }
                                }
                        );

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
                }, 2, 4, TimeUnit.MINUTES);
    }

    // synchronized 保证 map 和 list 同步；
    public synchronized void add(String url) {
        CacheEntry entry = new CacheEntry();
        entry.url = url;
        addEntry(entry);
    }

    public synchronized boolean contains(String url) {
        return diskCacheUrlSet.contains(url);
    }

    public synchronized void removeEntry(CacheEntry entry) {
        diskCacheUrlSet.remove(entry.url);
        diskCacheFileNameSet.remove(cacheFileNameStrategy.getName(entry.url));
    }

    public synchronized void addEntry(CacheEntry entry) {
        if (!diskCacheUrlSet.contains(entry.url)) {
            diskCacheList.add(entry);
            diskCacheUrlSet.add(entry.url);
            diskCacheFileNameSet.add(cacheFileNameStrategy.getName(entry.url));
        }
    }

    private File dumpFile() {
        String dir = StrongImageViewConfig.dir;
        return new File(dir + "_" + dir.hashCode() + ".data");
    }

    class CacheEntry {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public int hashCode() {
            return url.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof CacheEntry && url.hashCode() == ((CacheEntry) o).url.hashCode();
        }
    }
}
