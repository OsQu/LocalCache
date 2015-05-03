package fi.aalto.cse.localcacheclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    public static final String FETCHTYPE = "fi.aalto.cse.localcache.fetchtype";
	private TextView textDisplay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textDisplay = (TextView) findViewById(R.id.text_display);

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


	public void fetchContent(View view) {
        Intent intent = new Intent(this, FetchApiService.class);
        intent.putExtra(FETCHTYPE, NetworkManager.FetchType.FETCH_FROM_HS);
        this.startService(intent);
        textDisplay.setText("Started fetching");

	}

}
