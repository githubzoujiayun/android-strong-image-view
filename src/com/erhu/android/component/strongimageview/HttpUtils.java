package com.erhu.android.component.strongimageview;

import android.net.http.AndroidHttpClient;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.FileOutputStream;
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
class HttpUtils {

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

    /**
     * 从网络取图片
     */
    public static void downloadImg(final String _url, String _dir) {
        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        final HttpGet getRequest = new HttpGet(_url);

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                Log.d("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + _url);
            } else {
                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream inputStream = null;
                    try {
                        inputStream = entity.getContent();
                        // 保存图片到本地
                        saveImgStreamToFile(_dir, inputStream);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        entity.consumeContent();
                    }
                }
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            getRequest.abort();
            e.printStackTrace();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    /**
     * 保存图片文件到SD卡
     */
    private static void saveImgStreamToFile(String _file_path, InputStream inputStream) {
        FileOutputStream fos = null;

        try {
            // 创建文件流
            fos = new FileOutputStream(new File(_file_path));

            // 把图片数据压缩到流中
            byte[] bytes = new byte[1024];
            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                fos.write(bytes, 0, len);
            }

            fos.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
