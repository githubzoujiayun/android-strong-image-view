package com.erhu.android.component.strongimageview;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HttpUtils
 * <p/>
 * User: erhu
 * Date: 14-2-23
 * Time: 下午8:46
 */
public class HttpUtils {

    public static InputStream getStreamFromURL(String imageURL) {
        try {
            return getStreamUseURLConn(imageURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static InputStream getStreamUseURLConn(String imageURL) throws IOException {
        InputStream stream = null;
        try {
            URL url = new URL(imageURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            stream = connection.getInputStream();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return stream;
    }
}
