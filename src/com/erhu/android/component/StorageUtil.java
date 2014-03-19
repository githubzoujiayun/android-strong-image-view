package com.erhu.android.component;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * 存储工具类，例如SD卡
 * <p/>
 * User: erhu Date: 13-3-30 Time: 下午12:17
 */
public class StorageUtil {
    private static final String TAG = "StorageUtil";

    private static StorageUtil instance = new StorageUtil();
    private HashMap<STORAGE_TYPE, File> storageLocations;

    private static final String STORAGE_ROOT = "strong_image_cache";

    /**
     * 存储类型 *
     */
    public enum STORAGE_TYPE {
        /**
         * 第一张存储卡，或者是手机自带的大容量存储卡*
         */
        SDCARD,
        /**
         * 外置存储卡，一般是4.0以后的机器会有自带的大容量存储卡，然后还可以插入一个SD卡，那么就是这个 *
         */
        EXTERNAL_SDCARD
    }

    private StorageUtil() {
        initStorageLocations();

        if (isAvailable(STORAGE_TYPE.EXTERNAL_SDCARD)) {
            Log.d(TAG, String.format("external storage exist, path:%s", getPath(STORAGE_TYPE.EXTERNAL_SDCARD)));
        }
    }

    public static StorageUtil getInstance() {
        return instance;
    }

    /**
     * 当前使用的存储类型
     */
    public STORAGE_TYPE getCurrentUseStorage() {
        if (isAvailable(STORAGE_TYPE.EXTERNAL_SDCARD)) {
            return STORAGE_TYPE.EXTERNAL_SDCARD;
        } else {
            return STORAGE_TYPE.SDCARD;
        }
    }

    /**
     * 存储是否存在
     *
     * @param st 存储类型 {@see STORAGE_TYPE}
     */
    public boolean isAvailable(STORAGE_TYPE st) {
        return storageLocations != null
                && storageLocations.containsKey(st)
                && storageLocations.get(st).exists();
    }

    /**
     * 当前存储是否存在
     */
    public boolean isAvailable() {
        return isAvailable(getCurrentUseStorage());
    }

    /**
     * 获取存储的可用容量 (M)
     *
     * @param st 存储类型 {@see STORAGE_TYPE}
     */
    public long getAvailableSize(STORAGE_TYPE st) {
        if (storageLocations == null) {
            return 0;
        }
        File where = storageLocations.get(st);
        if (where == null) {
            return 0;
        }
        if (!where.exists()) {
            return 0;
        }

        StatFs sf = new StatFs(where.getPath());
        return sf.getAvailableBlocks() * (long) sf.getBlockSize() >> 20;
    }

    /**
     * 获取当前存储的总容量 (M)
     */
    public long getAvailableSize() {
        return getAvailableSize(getCurrentUseStorage());
    }


    /**
     * 获取存储的总容量 (M)
     *
     * @param st 存储类型 {@see STORAGE_TYPE}
     */
    public long getSize(STORAGE_TYPE st) {
        if (storageLocations == null) {
            return 0;
        }
        File where = storageLocations.get(st);
        if (where == null) {
            return 0;
        }
        if (!where.exists()) {
            return 0;
        }
        StatFs sf = new StatFs(where.getPath());
        return sf.getBlockCount() * (long) sf.getBlockSize() >> 20;
    }

    /**
     * 获取当前存储的总容量 (M)
     */
    public long getSize() {
        return getSize(getCurrentUseStorage());
    }

    /*
     * 初始化，获取机器存储空间
     */
    private void initStorageLocations() {
        storageLocations = new HashMap<STORAGE_TYPE, File>(2);

        List<String> mMounts = new ArrayList<String>(10);
        List<String> mVold = new ArrayList<String>(10);
        mMounts.add("/mnt/sdcard");
        mVold.add("/mnt/sdcard");

        try {
            File mountFile = new File("/proc/mounts");
            if (mountFile.exists()) {
                Scanner scanner = new Scanner(mountFile);
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("/dev/block/vold/")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[1];

                        // don't add the default mount path
                        // it's already in the list.
                        if (!element.equals("/mnt/sdcard"))
                            mMounts.add(element);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            File voldFile = new File("/system/etc/vold.fstab");
            if (voldFile.exists()) {
                Scanner scanner = new Scanner(voldFile);
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("dev_mount")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[2];

                        if (element.contains(":"))
                            element = element.substring(0, element.indexOf(":"));
                        if (!element.equals("/mnt/sdcard"))
                            mVold.add(element);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        for (int i = 0; i < mMounts.size(); i++) {
            String mount = mMounts.get(i);
            if (!mVold.contains(mount))
                mMounts.remove(i--);
        }
        mVold.clear();

        List<String> mountHash = new ArrayList<String>(10);

        for (String mount : mMounts) {
            File root = new File(mount);
            if (root.exists() && root.isDirectory() && root.canWrite()) {
                File[] list = root.listFiles();
                String hash = "[";
                if (list != null) {
                    for (File f : list) {
                        hash += f.getName().hashCode() + ":" + f.length() + ", ";
                    }
                }
                hash += "]";
                if (!mountHash.contains(hash)) {
//                    String key = SDCARD + "_" + map.size();
                    STORAGE_TYPE key = null;
                    if (storageLocations.size() == 0) {
                        key = STORAGE_TYPE.SDCARD;
                    } else if (storageLocations.size() == 1) {
                        key = STORAGE_TYPE.EXTERNAL_SDCARD;
                    }
                    mountHash.add(hash);
                    storageLocations.put(key, root);
                }
            }
        }

        mMounts.clear();

        if (storageLocations.isEmpty()) {
            storageLocations.put(STORAGE_TYPE.SDCARD, Environment.getExternalStorageDirectory());
        }
    }

    private void makeDirIfNotExist(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    /**
     * 获取存储路径
     *
     * @param st 存储类型 {@see STORAGE_TYPE}
     */
    public String getPath(STORAGE_TYPE st) {
        if (storageLocations == null || !storageLocations.containsKey(st)) {
            return "";
        }
        return storageLocations.get(st).getPath();
    }

    /**
     * 获取当前存储路径
     */
    public String getPath() {
        return getPath(getCurrentUseStorage());
    }

    public String makeImgDir(String _dir_name) {
        String pre = getPath().concat(File.separator).concat(STORAGE_ROOT).concat(File.separator);
        makeDirIfNotExist(pre);
        String img_dir = pre.concat(_dir_name).concat(File.separator);
        makeDirIfNotExist(img_dir);
        return img_dir;
    }
}
