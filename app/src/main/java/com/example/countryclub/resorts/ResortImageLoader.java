
package com.example.countryclub.resorts;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ResortImageLoader {

    private static final ExecutorService executor =
            Executors.newFixedThreadPool(3);

    private static final android.os.Handler mainHandler =
            new android.os.Handler(android.os.Looper.getMainLooper());

    private static final int maxMemory =
            (int) (Runtime.getRuntime().maxMemory() / 1024);

    private static final LruCache<String, Bitmap> cache =
            new LruCache<String, Bitmap>(maxMemory / 8) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };

    private ResortImageLoader() {
    }

    public static void load(ImageView imageView, String imageUrl) {
        imageView.setTag(imageUrl);

        GradientDrawable placeholder = new GradientDrawable();
        placeholder.setColor(Color.rgb(240, 236, 246));
        placeholder.setCornerRadius(16);
        imageView.setImageDrawable(placeholder);

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return;
        }

        String url = imageUrl.trim();

        Bitmap cached = cache.get(url);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        if (!url.startsWith("https://")
                && !url.startsWith("http://")) {
            return;
        }

        executor.execute(() -> {
            Bitmap bitmap = download(url);

            if (bitmap != null) {
                cache.put(url, bitmap);
            }

            mainHandler.post(() -> {
                if (url.equals(imageView.getTag())
                        && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            });
        });
    }

    private static Bitmap download(String urlString) {
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection)
                    new URL(urlString).openConnection();

            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);

            if (connection.getResponseCode() != 200) {
                return null;
            }

            try (InputStream input = connection.getInputStream()) {
                return BitmapFactory.decodeStream(input);
            }

        } catch (Exception e) {
            android.util.Log.e(
                    "ResortImageLoader",
                    "Unable to load image", e);
            return null;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
