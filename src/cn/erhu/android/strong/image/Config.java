package cn.erhu.android.strong.image;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * #请替换掉这里的东东#
 * <p/>
 * User: erhu
 * Date: 13-4-22
 * Time: 下午12:10
 */
public class Config {

    private static final String TAG = "Config";

    private static final String PREF = "STRONG_IMAGE_CONFIG";

    /**
     * 保存当前 文件 大小 的sampleSize, 保压缩图片使用;
     *
     * @param _image_size  单位 KB
     * @param _sample_size
     */
    public static void setImageSampleSize(Context _context, long _image_size, int _sample_size) {
        long size_k = convertByte2HK(_image_size);
        SharedPreferences.Editor editor = getEditor(_context);
        editor.putInt(String.valueOf(size_k), _sample_size);

        Log.d(TAG, String.format("当图片大小为 %s:时, 设置 sampleSize为 %d.", size_k, _sample_size));

        editor.commit();
    }

    /**
     * 根据图片文件大小, 获取压缩比例
     *
     * @param _image_size
     * @return
     */
    public static int getImageSampleSize(Context _context, long _image_size) {
        long size_k = convertByte2HK(_image_size);
        int sample_size = getPreference(_context).getInt(String.valueOf(size_k), 1);

        Log.d(TAG, String.format("当图片大小为 %s 时, sampleSize为 %d", size_k, sample_size));

        return sample_size;
    }

    private static SharedPreferences.Editor getEditor(Context _context) {
        return getPreference(_context).edit();
    }

    private static SharedPreferences getPreference(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_APPEND);
    }

    /**
     * byte to HKB(100K的整数倍)
     *
     * @param _byte
     * @return
     */
    private static long convertByte2HK(long _byte) {
        return (_byte >> 10) / 100;
    }
}
