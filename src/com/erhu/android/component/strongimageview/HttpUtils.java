package com.erhu.android.component.strongimageview;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * #请替换掉这里的东东#
 * <p/>
 * User: erhu
 * Date: 14-2-23
 * Time: 下午8:46
 */
public class HttpUtils {

    public static InputStream getStreamFromURL(String imageURL) {
        InputStream in = null;
        try {
            URL url = new URL(imageURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            in = connection.getInputStream();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return in;

    }
}
