package fi.aalto.cse.localcacheclient;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Liisa on 15.3.2015.
 */
public class FetchApiService extends Service {


    private static final String TAG = FetchApiService.class.getSimpleName();

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private OnProgressListener progressListener;

    public OnProgressListener getProgressListener() {
        return progressListener;
    }

    public void setProgressListener(OnProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        FetchApiService getService() {
            // Return this instance of LocalService so clients can call public methods
            return FetchApiService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    private void handleIntent(Intent intent) {
        Log.d(TAG, "Service Started!");
        NetworkManager.FetchType type = (NetworkManager.FetchType)intent.getSerializableExtra(MainActivity.FETCHTYPE);
        if (type != null) {
            Log.d(TAG, "Starting fetch!");
            NetworkManager.getInstance(this, new OnProgressListener() {
                @Override
                public void onProgressUpdate(int completed, int total) {
                    if (progressListener != null) {
                        progressListener.onProgressUpdate(completed, total);
                    }
                }

                @Override
                public void onNewFileDownload(String fileName) {
                    Log.d(TAG, "onNewFileDownload "+fileName);
                    if (progressListener != null) {
                        progressListener.onNewFileDownload(fileName);
                    }
                }
            }).startFetch(type);
        }
        Log.d(TAG, "Service Stopping!");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        this.progressListener = null;
        return super.onUnbind(intent);
    }
}
