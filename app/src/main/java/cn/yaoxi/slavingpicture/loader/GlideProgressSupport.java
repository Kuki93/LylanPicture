package cn.yaoxi.slavingpicture.loader;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class GlideProgressSupport {
    private static Interceptor createInterceptor(final ResponseProgressListener listener) {
        return new Interceptor() {
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                Request request = chain.request();
                Response response = chain.proceed(request);
                return response.newBuilder()
                        .body(new OkHttpProgressResponseBody(request.url(), response.body(),
                                listener))
                        .build();
            }
        };
    }

    public static void init(Glide glide, OkHttpClient okHttpClient) {
        OkHttpClient.Builder builder;
        if (okHttpClient != null) {
            builder = okHttpClient.newBuilder();
        } else {
            builder = new OkHttpClient.Builder();
        }
        builder.addNetworkInterceptor(createInterceptor(new DispatchingProgressListener()));
        glide.getRegistry().replace(GlideUrl.class, InputStream.class,
                new OkHttpUrlLoader.Factory(builder.build()));
    }

    public static void forget(String url) {
        DispatchingProgressListener.forget(url);
    }

    public static void expect(String url, ProgressListener listener) {
        DispatchingProgressListener.expect(url, listener);
    }

    public interface ProgressListener {
        void onProgress(float progress);
    }

    private interface ResponseProgressListener {
        void update(HttpUrl url, long bytesRead, long contentLength);
    }

    private static class DispatchingProgressListener implements ResponseProgressListener {
        private static final Map<String, ProgressListener> LISTENERS = new ArrayMap<>();
        private static final Map<String, Float> PROGRESSES = new ArrayMap<>();
        private static final String URL_QUERY_PARAM_START = "\\?";

        static void forget(String url) {
            LISTENERS.remove(getRawKey(url));
            PROGRESSES.remove(getRawKey(url));
        }

        static void expect(String url, ProgressListener listener) {
            LISTENERS.put(getRawKey(url), listener);
        }

        @Override
        public void update(HttpUrl url, final long bytesRead, final long contentLength) {
            String key = getRawKey(url.toString());
            final ProgressListener listener = LISTENERS.get(key);
            if (listener == null) {
                return;
            }

            Float lastProgress = PROGRESSES.get(key);
            if (contentLength <= bytesRead) {
                forget(key);
                return;
            }

            float progress = (float) bytesRead / contentLength;
            if (lastProgress == null || progress != lastProgress) {
                PROGRESSES.put(key, progress);
                listener.onProgress(progress);
            }
        }

        private static String getRawKey(String formerKey) {
            return formerKey.split(URL_QUERY_PARAM_START)[0];
        }
    }

    private static class OkHttpProgressResponseBody extends ResponseBody {
        private final HttpUrl mUrl;
        private final ResponseBody mResponseBody;
        private final ResponseProgressListener mProgressListener;
        private BufferedSource mBufferedSource;

        OkHttpProgressResponseBody(HttpUrl url, ResponseBody responseBody,
                                   ResponseProgressListener progressListener) {
            this.mUrl = url;
            this.mResponseBody = responseBody;
            this.mProgressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return mResponseBody.contentType();
        }

        @Override
        public long contentLength() {
            return mResponseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (mBufferedSource == null) {
                mBufferedSource = Okio.buffer(source(mResponseBody.source()));
            }
            return mBufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                private long mTotalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    long fullLength = mResponseBody.contentLength();
                    if (bytesRead == -1) { // this source is exhausted
                        mTotalBytesRead = fullLength;
                    } else {
                        mTotalBytesRead += bytesRead;
                    }
                    mProgressListener.update(mUrl, mTotalBytesRead, fullLength);
                    return bytesRead;
                }
            };
        }
    }

}
