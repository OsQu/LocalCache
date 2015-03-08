package fi.aalto.cse.localcacheclient;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetworkManager {
	private static NetworkManager instance; 
	
	public static final String apiUrl = "http://www.hs.fi/rest/k/editions/uusin/";
	public static final String serverUrl = "http://188.166.59.119/event";
	
	private ExecutorService execService  = Executors.newFixedThreadPool(5);
	
	private NetworkManager () {
		
	}
	
	public static NetworkManager getInstance() {
		
		if (instance == null) {
			instance = new NetworkManager();
		}
		return instance;
	}
	
	public void startFetch() {
		execService.execute(new Runnable() {
			
			@Override
			public void run() {
				Request request = new Request.Builder()
			      .url(apiUrl)
			      .build();
                OkHttpClient client = new OkHttpClient();
                long before = System.currentTimeMillis();
                try {
                    Response response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                long diff = System.currentTimeMillis() - before;
			  // TODO collect stats here and send to server using sendStatsToServer
			}
		});
		
	}
	
	private void parseJson(String jsonString) {
		try {
			JSONObject json = new JSONObject(jsonString);
			JSONObject articles = json.getJSONObject("articles");
			Iterator<String> keys = articles.keys();
			while (keys.hasNext()) {
				JSONObject article = articles.getJSONObject(keys.next());
				JSONObject mainPicture = article.getJSONObject("mainPicture");
				if (mainPicture != null) {
					String url = mainPicture.getString("url");
					url = url.replace("{width}", ""+mainPicture.getInt("width"));
					url = url.replace("{type}", "nelio");
					fetchUrl(url);
				}
				// Additional pictures
				JSONArray pictures = article.getJSONArray("pictures");
				if (pictures != null) {
					for (int i = 0; i < pictures.length(); i++) {
						String url = ((JSONObject) pictures.get(i)).getString("url");
						url = url.replace("{width}", ""+((JSONObject) pictures.get(i)).getInt("width"));
						url = url.replace("{type}", "nelio");
						fetchUrl(url);
					}
				}
				// Editor pictures
				JSONArray editorPictures = article.getJSONArray("editors");
				if (editorPictures != null) {
					for (int i = 0; i<editorPictures.length(); i++) {
						JSONObject picture = ((JSONObject) editorPictures.get(i)).getJSONObject("picture");
						if (picture != null) {
							String url = picture.getString("url");
							url = url.replace("{width}", ""+picture.getInt("width"));
							url = url.replace("{type}", "nelio");
							fetchUrl(url);
						}
					}
 					
				}
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void fetchUrl(final String url) {
	execService.execute(new Runnable() {
			
			@Override
			public void run() {
				Request request = new Request.Builder()
			      .url(url)
			      .build();
              OkHttpClient client = new OkHttpClient();
			  long before = System.currentTimeMillis();
                try {
                    Response response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                long diff = System.currentTimeMillis() - before;
			  // TODO collect stats here and send to server using sendStatsToServer
			  
			  
			}
		});
		
	}
	
	private void sendStatsToServer(Object obj) {
		// Convert object to JSON and send to server serverUrl
		
	}
	
	
}
