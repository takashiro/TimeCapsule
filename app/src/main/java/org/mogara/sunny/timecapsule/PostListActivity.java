package org.mogara.sunny.timecapsule;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class PostListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);

        ListAdapter adapter = new MediaListAdapter(this,
                getData(),
                R.layout.post_item,
                new String[]{"image", "dateline", "expiry"},
                new int[]{R.id.post_image, R.id.post_dateline, R.id.post_expiry}
        );

        ListView view = (ListView) findViewById(R.id.post_list_view);
        view.setAdapter(adapter);
    }

    protected List<Map<String, Object>> getData() {
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < 6; i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("image", FileServer.BASIC_URL + "data/test.jpg");
            item.put("dateline", parseTime(1234));
            item.put("expiry", parseExpiry(1234));
            data.add(item);
        }
        return data;
    }

    protected Bitmap parseBitmap(String httpUrl) {
        try {
            URL url = new URL(httpUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            InputStream is = conn.getInputStream();
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    protected String parseTime(int timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(new Date(timestamp * 1000));
    }

    protected String parseExpiry(int expiry) {
        if (expiry >= 86400)
            return (expiry / 86400) + "天";
        if (expiry >= 3600)
            return "约" + (expiry / 3600) + "小时";
        int i = expiry / 60;
        int s = expiry % 60;
        return i + ":" + s;
    }

    private static class MediaListAdapter extends SimpleAdapter{

        public MediaListAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public void setViewImage(ImageView view, String path){
            if(path.startsWith("http://") || path.startsWith("https://")){
                new DownloadImageTask(view).execute(path);
            }else{
                super.setViewImage(view, path);
            }
        }

    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView view;
        int width;

        public DownloadImageTask(ImageView image_view) {
            this.view = image_view;
            width = 1000;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];

            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                if (width < bitmap.getWidth()) {
                    int height = (int) ((float) bitmap.getHeight() / bitmap.getWidth() * width);
                    return bitmap.createScaledBitmap(bitmap, width, height, true);
                }
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Bitmap result) {
            view.setImageBitmap(result);
        }
    }
}
