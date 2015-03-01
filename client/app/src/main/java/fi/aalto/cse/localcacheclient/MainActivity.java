package fi.aalto.cse.localcacheclient;

import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Response;

public class MainActivity extends ActionBarActivity {
	
	public static final String apiUrl = "http://www.hs.fi/rest/k/editions/uusin/";

	private RequestQueue queue;
	
	private TextView textDisplay;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		// noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	private RequestQueue getRequestQueue() {
		
		if (queue == null) {
			queue = Volley.newRequestQueue(this);
		}
		return queue;
		
	}
	private TextView getTextDisplay() {
		if (textDisplay == null) {
			textDisplay = (TextView) findViewById(R.id.text_display);
		}
		return textDisplay;
	}
	
	public void fetchContent(View view) {
		JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
			    new Response.Listener<JSONObject>()
			    {
			        @Override
			        public void onResponse(JSONObject response) {  
			        	TextView tv = getTextDisplay();
			        	tv.setText("Fetched content!");
			        	
			        }
			    },
			    new Response.ErrorListener()
			    {
			         @Override
			         public void onErrorResponse(VolleyError error) {           
				        TextView tv = getTextDisplay();
				        tv.setText("Failed to fetch content.");
			       }
			    }
			);
		RequestQueue queue = getRequestQueue();
		queue.add(request);
		
	}
	
}
