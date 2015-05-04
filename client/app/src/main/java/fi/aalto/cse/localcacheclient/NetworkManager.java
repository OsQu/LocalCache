package fi.aalto.cse.localcacheclient;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class NetworkManager {
    private static final String TAG = NetworkManager.class.getSimpleName();
    private static final String hsApiUrl = "http://www.hs.fi/rest/k/editions/uusin/";
    private static final String serverUrl = "http://188.166.59.119/event";
    private static final String cacheApiUrl = "http://188.166.89.185/";
    public static final String DATA = "data";
    public static final String ARTICLES = "articles";
    public static final String MAIN_PICTURE = "mainPicture";
    public static final String URL = "url";
    public static final String WIDTH_BRACKETS = "{width}";
    public static final String WIDTH = "width";
    public static final String TYPE_BRACKETS = "{type}";
    public static final String IMAGE_TYPE = "nelio";
    public static final String PICTURES = "pictures";
    public static final String EDITORS = "editors";
    public static final String PICTURE = "picture";

    public static enum FetchType {
        FETCH_FROM_HS(hsApiUrl, "HS"), FETCH_FROM_CACHE(cacheApiUrl, "CACHE");

        private String url;
        private String shortRepresentation;

        FetchType(String url, String rep) {
            this.url = url;
            this.shortRepresentation = rep;
        }

        public String getUrl() {
            return url;
        }

        public String getShortRepresentation() {
            return shortRepresentation;
        }
    }

    private static NetworkManager instance;

    private ExecutorService execService = Executors.newFixedThreadPool(5);
    private OkHttpClient client = new OkHttpClient();
    private FetchStatFactory statFactory;
    private Gson gson = new Gson();
    private FetchType currentFetchType;
    private List<Future> futures = new ArrayList<>();
    private OnProgressListener progressListener;
    private int totalFileCount = 0;
    private int completedFileCount = 0;
    private static int fileId = 0;

    private NetworkManager(Context context, final OnProgressListener progressListener) {
        statFactory = new FetchStatFactory(context);
        this.progressListener = progressListener;
        client.networkInterceptors().add(new Interceptor() {
            @Override public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                if (!chain.request().urlString().equals(serverUrl)) {
                    fileId++;
                    progressListener.onNewFileDownload(new Progress(fileId, 0.0f, chain.request().urlString(), originalResponse.body().contentLength()));
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), progressListener, fileId, chain.request().urlString()))
                            .build();
                }
                else {
                    return originalResponse;
                }
            }
        });
    }

    private class ProgressResponseBody extends ResponseBody {

        private final ResponseBody responseBody;
        private final OnProgressListener progressListener;
        private BufferedSource bufferedSource;
        private int fileId;
        private String fileName;

        public ProgressResponseBody(ResponseBody responseBody, OnProgressListener progressListener, int fileId, String fileName) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
            this.fileId = fileId;
            this.fileName = fileName;
        }

        @Override public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override public long contentLength() throws IOException {
            return responseBody.contentLength();
        }

        @Override public BufferedSource source() throws IOException {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;
                @Override public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    //progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                    progressListener.onProgressUpdate(new Progress(fileId, ((float)totalBytesRead) / responseBody.contentLength(), fileName, responseBody.contentLength()));
                    if (bytesRead == -1) {
                        completedFileCount++;
                        progressListener.onOverallProgress(completedFileCount, totalFileCount);
                    }
                    return bytesRead;
                }
            };
        }
    }

    public static NetworkManager getInstance(Context context, OnProgressListener progressListener) {

        if (instance == null) {
            instance = new NetworkManager(context, progressListener);
        }
        return instance;
    }

    public void startFetch(final FetchType fetchType) {
        if (!futures.isEmpty()) {
            for (Future future : futures) {
                if(!future.isDone()) {
                    //We have a pending task. This means a fetch is already in progress.
                    return;
                }
            }
        }

        Log.d(TAG, "Starting fetch!");

        //New fetch
        currentFetchType = fetchType;
        futures.clear();
        fileId = 0;
        totalFileCount = 0;
        completedFileCount = 0;
        futures.add(execService.submit(new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "Fetching json");
                Request request = new Request.Builder()
                        .url(fetchType.getUrl())
                        .build();
                try {
                    long before = System.currentTimeMillis();
                    Response response = client.newCall(request).execute();
                    long diff = System.currentTimeMillis() - before;

                    if (response.isSuccessful()) {
                        sendStatsToServer(fetchType.getUrl(), diff, response.body().contentLength());
                        String responseBody = response.body().string();
                        Log.d(TAG, "Fetched: "+responseBody);
                        parseJson(responseBody);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Exception fetching");
                }
            }
        }));
    }

    private void parseJson(String jsonString) {

        try {
            JSONObject json = new JSONObject(jsonString);
            JSONObject articles = json.getJSONObject(DATA).getJSONObject(ARTICLES);
            Log.d(TAG, ARTICLES+":"+articles.toString());
            Iterator<String> keys = articles.keys();
            while (keys.hasNext()) {
                JSONObject article = articles.getJSONObject(keys.next());
                JSONObject mainPicture = article.optJSONObject(MAIN_PICTURE);
                if (mainPicture != null) {
                    String url = mainPicture.getString(URL);
                    url = url.replace(WIDTH_BRACKETS, "" + mainPicture.getInt(WIDTH));
                    url = url.replace(TYPE_BRACKETS, IMAGE_TYPE);
                    totalFileCount++;
                    fetchUrl(url);
                }
                // Additional pictures
                JSONArray pictures = article.optJSONArray(PICTURES);
                if (pictures != null) {
                    for (int i = 0; i < pictures.length(); i++) {
                        String url = ((JSONObject) pictures.get(i)).getString(URL);
                        url = url.replace(WIDTH_BRACKETS, "" + ((JSONObject) pictures.get(i)).getInt(WIDTH));
                        url = url.replace(TYPE_BRACKETS, IMAGE_TYPE);
                        totalFileCount++;
                        fetchUrl(url);
                    }
                }
                // Editor pictures
                JSONArray editorPictures = article.optJSONArray(EDITORS);
                if (editorPictures != null) {
                    for (int i = 0; i < editorPictures.length(); i++) {
                        JSONObject picture = ((JSONObject) editorPictures.get(i)).optJSONObject(PICTURE);
                        if (picture != null) {
                            String url = picture.getString(URL);
                            url = url.replace(WIDTH_BRACKETS, "" + picture.getInt(WIDTH));
                            url = url.replace(TYPE_BRACKETS, IMAGE_TYPE);
                            totalFileCount++;
                            fetchUrl(url);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSON Exception"+e.getMessage());
        }

    }

    private void fetchUrl(final String url) {
        futures.add(execService.submit(new Runnable() {

            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                try {
                    Log.d(TAG, "Fetching "+url);
                    long before = System.currentTimeMillis();
                    Response response = client.newCall(request).execute();
                    long diff = System.currentTimeMillis() - before;
                    completedFileCount++;
                    if (response.isSuccessful()) {
                        sendStatsToServer(url, diff, response.body().contentLength());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    public static final MediaType JSON_TYPE
            = MediaType.parse("application/json; charset=utf-8");

    private void sendStatsToServer(String url, long duration, long size) {
        //TODO should we queue up the sending to the server to be done at the end of the fetch?
        //TODO Will sending in between fetching affect our measurement?

        final FetchStat fetchStat = statFactory.getNewFetchStat();
        fetchStat.setDuration(duration);
        fetchStat.setFile(url);
        fetchStat.setSize(size);
        fetchStat.setHost(currentFetchType.getShortRepresentation());
        Log.d(TAG, "sending stats for :"+fetchStat);
        futures.add(execService.submit(new Runnable() {

            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url(serverUrl)
                        .post(RequestBody.create(JSON_TYPE, gson.toJson(fetchStat, FetchStat.class).toString()))
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
    }
}
