package fi.aalto.cse.localcacheclient;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class FetchStatFactory {
    private Context context;
    
    public FetchStatFactory(Context context) {
        this.context = context;
    }
    
    public FetchStat getNewFetchStat() {
        FetchStat fetchStat = new FetchStat();
        FetchStat.DeviceStat deviceStat = new FetchStat.DeviceStat();
        deviceStat.setId(getDeviceId());
        deviceStat.setName(Build.MANUFACTURER + " " + Build.MODEL);
        fetchStat.setDevice(deviceStat);
        fetchStat.setConnection(ConnectivityUtil.getNetworkClass(context));
        
        //TODO set signal strength based on network type.
        fetchStat.setSignal(0.0f);
        return fetchStat;
    }
    
    private String getDeviceId() {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }
}
