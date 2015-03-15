package fi.aalto.cse.localcacheclient;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Liisa on 15.3.2015.
 */
public class FetchApiService extends IntentService {


    private static final String TAG = FetchApiService.class.getSimpleName();

    /**
     * Creates an IntentService.
     *
     */
    public FetchApiService() {
        super(FetchApiService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Service Started!");
        NetworkManager.FetchType type = (NetworkManager.FetchType)intent.getSerializableExtra(MainActivity.FETCHTYPE);
        if (type != null) {
            NetworkManager.getInstance(this).startFetch(type);
        }
        Log.d(TAG, "Service Stopping!");


    }
}
