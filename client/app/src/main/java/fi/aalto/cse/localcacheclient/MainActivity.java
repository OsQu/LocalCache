package fi.aalto.cse.localcacheclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    public static final String FETCHTYPE = "fi.aalto.cse.localcache.fetchtype";
    private Button fetchHsButton, fetchCacheButton;
    private LinearLayout progressContainer;
    private ProgressBar overallProgress;
    private ListView progressListView;
    private List<String> progresses = new ArrayList<>();
    private boolean mBound;
    private FetchApiService mService;
    private ProgressAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        fetchCacheButton = (Button) findViewById(R.id.button_send_cache);
        fetchHsButton = (Button) findViewById(R.id.button_send);
        overallProgress = (ProgressBar) findViewById(R.id.overall_progress);
        progressListView = (ListView) findViewById(R.id.progress_list);

        adapter = new ProgressAdapter();
        progressListView.setAdapter(adapter);
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

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            FetchApiService.LocalBinder binder = (FetchApiService.LocalBinder) service;
            mService = binder.getService();
            mService.setProgressListener(new OnProgressListener() {
                @Override
                public void onProgressUpdate(int completed, int total) {
                    overallProgress.setMax(total);
                    overallProgress.setProgress(completed);
                }

                @Override
                public void onNewFileDownload(String fileName) {
                    progresses.add(fileName);
                    adapter.notifyDataSetChanged();
                }
            });
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, FetchApiService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    public void fetchContent(View view) {
        progresses.clear();
        adapter.notifyDataSetChanged();
        Intent intent = new Intent(this, FetchApiService.class);
        intent.putExtra(FETCHTYPE, NetworkManager.FetchType.FETCH_FROM_HS);
        this.startService(intent);
        fetchCacheButton.setEnabled(false);
        fetchHsButton.setEnabled(false);
	}

    public void fetchContentCache(View view) {
        progresses.clear();
        adapter.notifyDataSetChanged();
        Intent intent = new Intent(this, FetchApiService.class);
        intent.putExtra(FETCHTYPE, NetworkManager.FetchType.FETCH_FROM_CACHE);
        this.startService(intent);
        fetchCacheButton.setEnabled(false);
        fetchHsButton.setEnabled(false);
    }

    public class ProgressAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return progresses.size();
        }

        @Override
        public Object getItem(int position) {
            return progresses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) MainActivity.this
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.progress_layout, parent, false);
            }

            TextView fileNameTextView = (TextView) findViewById(R.id.file);
            fileNameTextView.setText(progresses.get(position));
            return view;
        }
    }
}
