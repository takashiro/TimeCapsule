package org.mogara.sunny.timecapsule;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * This is a file created by sunny on 12/26/15 for TimeCapsule
 * Contact sunny via sunny@mogara.org for cooperation.
 */
public class HttpUtil {
    public static void get(final String site,
                           final String apiName,
                           final List<NameValuePair> params,
                           final HttpCallbackListener listener,
                           final boolean willEncode) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(site + apiName + toQueryString(params));
                    Log.w("GET", site + apiName + toQueryString(params, willEncode));
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = httpResponse.getEntity();
                        String response = EntityUtils.toString(entity, "utf-8");
                        if (listener != null) {
                            listener.onFinished(response);
                        }
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        listener.onError(e);
                    }
                }
            }
        }).start();
    }

    public static void post(final String site,
                            final String apiName,
                            final List<NameValuePair> params,
                            final HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(site + apiName);
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");
                    httpPost.setEntity(entity);
                    Log.w("POST", entity.toString());
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity resEntity = httpResponse.getEntity();
                        String response = EntityUtils.toString(resEntity, "utf-8");
                        if (listener != null) {
                            listener.onFinished(response);
                        }
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        listener.onError(e);
                    }
                }
            }
        }).start();
    }

    static private String toQueryString(final List<NameValuePair> data)
            throws UnsupportedEncodingException {
        return toQueryString(data, true);
    }

    static private String toQueryString(final List<NameValuePair> data, boolean willEncode)
            throws UnsupportedEncodingException {
        StringBuffer queryString = new StringBuffer();
        queryString.append("?");
        for (NameValuePair pair : data) {
            queryString.append(pair.getName() + "=");
            String value = pair.getValue();
            if (willEncode) {
                value = URLEncoder.encode(value, "UTF-8");
            }
            queryString.append(value + "&");
        }
        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }
        return queryString.toString();
    }
}
