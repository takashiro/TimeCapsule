package org.mogara.sunny.timecapsule;

import android.util.Log;

import com.baidu.location.BDLocation;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a file created by sunny on 12/27/15 for TimeCapsule
 * Contact sunny via sunny@mogara.org for cooperation.
 */
public class MapDB {

    private static final String DATA_SITE = "http://api.map.baidu.com/geodata/v3/";

    private static final String SEARCH_SITE = "http://api.map.baidu.com/geosearch/v3/";

    private static final String BAIDU_AK = "qMw4Cb8iqoK7DD92nZfLfy2V";

    private static final String GEOTABLE_ID = "129945";

    private static final String TYPE_MESSAGE = "1";

    private static final String TYPE_AUDIO = "2";

    public static void postTextAndImage(BDLocation location, final String text, final String path) {
        post(location, TYPE_MESSAGE, text, path);
    }

    public static void postAudio(final BDLocation location, final String path) {
        post(location, TYPE_AUDIO, null, path);
    }

    private static void post(final BDLocation location, final String type,
                             final String text, final String path) {
        if (location == null) return;

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("title", "test"));
        params.add(new BasicNameValuePair("latitude", Double.toString(location.getLatitude())));
        params.add(new BasicNameValuePair("longitude", Double.toString(location.getLongitude())));
        params.add(new BasicNameValuePair("coord_type", "3"));
        params.add(new BasicNameValuePair("geotable_id", GEOTABLE_ID));
        params.add(new BasicNameValuePair("ak", BAIDU_AK));

        if (type == TYPE_MESSAGE) {
            params.add(new BasicNameValuePair("message", text));
            params.add(new BasicNameValuePair("post_type", TYPE_MESSAGE));
        } else {
            params.add(new BasicNameValuePair("post_type", TYPE_AUDIO));
        }

        HttpUtil.post(DATA_SITE, "poi/create", params, new HttpCallbackListener() {
            @Override
            public void onFinished(String response) {
                Log.w("BaiduLBS", response);
                try {
                    JSONObject object = new JSONObject(response);
                    int id = object.getInt("id");

                    File file = new File(path);
                    Log.w("ImagePath", path);
                    if (file.exists()) {
                        Log.w("Image", "666");
                        InputStream in = new BufferedInputStream(new FileInputStream(file));
                        FileServer.upload(id + getExtensionName(path), in);
                    }

                    getRowById(id, new RowQueryListener() {
                        @Override
                        public void onGet(String response) {
                            Log.w("Query", response);
                        }
                    });
                } catch (Exception e) {

                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    public static void getRowById(final int id, final RowQueryListener listener) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("id", Integer.toString(id)));
        params.add(new BasicNameValuePair("geotable_id", GEOTABLE_ID));
        params.add(new BasicNameValuePair("ak", BAIDU_AK));

        HttpUtil.get(DATA_SITE, "poi/detail", params, new HttpCallbackListener() {
            @Override
            public void onFinished(String response) {
                Log.w("PreQuery", response);
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.getInt("status") == 0 && listener != null) {
                        listener.onGet(response);
                    }
                } catch (Exception e) {

                }
            }

            @Override
            public void onError(Exception e) {

            }
        }, true);
    }

    public static void getNearbyPosts(final BDLocation location, final RowQueryListener listener) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("geotable_id", GEOTABLE_ID));
        params.add(new BasicNameValuePair("ak", BAIDU_AK));
        String latitude = Double.toString(location.getLatitude());
        String longitude = Double.toString(location.getLongitude());

        Log.w("location", latitude + "," + longitude);
        params.add(new BasicNameValuePair("location", longitude + "," + latitude));
        params.add(new BasicNameValuePair("coord_type", "3"));
        params.add(new BasicNameValuePair("radius", "500"));
        params.add(new BasicNameValuePair("sortby", "distance:1"));

        HttpUtil.get(SEARCH_SITE, "nearby", params, new HttpCallbackListener() {
            @Override
            public void onFinished(String response) {
                Log.w("getNearPreQuery", response);
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.getInt("status") == 0 && listener != null) {
                        listener.onGet(response);
                    }
                } catch (Exception e) {

                }
            }

            @Override
            public void onError(Exception e) {

            }
        }, false);

    }

    private static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot);
            }
        }
        return filename;
    }
}
