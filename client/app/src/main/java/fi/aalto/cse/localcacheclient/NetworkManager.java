package fi.aalto.cse.localcacheclient;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

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

public class NetworkManager {
    private static final String TAG = NetworkManager.class.getSimpleName();
    private static final String hsApiUrl = "http://www.hs.fi/rest/k/editions/uusin/";
    private static final String serverUrl = "http://188.166.59.119/event";
    //TODO change this to our cache server.
    private static final String cacheApiUrl = "http://www.hs.fi/rest/k/editions/uusin/";

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

    private NetworkManager(Context context) {
        statFactory = new FetchStatFactory(context);
    }

    public static NetworkManager getInstance(Context context) {

        if (instance == null) {
            instance = new NetworkManager(context);
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
        
        //New fetch
        currentFetchType = fetchType;
        futures.clear();
        futures.add(execService.submit(new Runnable() {

            @Override
            public void run() {
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
                }
            }
        }));
    }

    private void parseJson(String jsonString) {
        //TODO declare JSON keys as constants.
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONObject articles = json.getJSONObject("data").getJSONObject("articles");
            Log.d(TAG, "Articles:"+articles.toString());
            Iterator<String> keys = articles.keys();
            while (keys.hasNext()) {
                JSONObject article = articles.getJSONObject(keys.next());
                JSONObject mainPicture = article.optJSONObject("mainPicture");
                if (mainPicture != null) {
                    String url = mainPicture.getString("url");
                    url = url.replace("{width}", "" + mainPicture.getInt("width"));
                    url = url.replace("{type}", "nelio");
                    fetchUrl(url);
                }
                // Additional pictures
                JSONArray pictures = article.optJSONArray("pictures");
                if (pictures != null) {
                    for (int i = 0; i < pictures.length(); i++) {
                        String url = ((JSONObject) pictures.get(i)).getString("url");
                        url = url.replace("{width}", "" + ((JSONObject) pictures.get(i)).getInt("width"));
                        url = url.replace("{type}", "nelio");
                        fetchUrl(url);
                    }
                }
                // Editor pictures
                
                JSONArray editorPictures = article.optJSONArray("editors");
                if (editorPictures != null) {
                    for (int i = 0; i < editorPictures.length(); i++) {
                        JSONObject picture = ((JSONObject) editorPictures.get(i)).optJSONObject("picture");
                        if (picture != null) {
                            String url = picture.getString("url");
                            url = url.replace("{width}", "" + picture.getInt("width"));
                            url = url.replace("{type}", "nelio");
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
