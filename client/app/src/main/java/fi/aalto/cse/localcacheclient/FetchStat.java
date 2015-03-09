package fi.aalto.cse.localcacheclient;

/**
 * Statistics for a single file fetch.
 * @see fi.aalto.cse.localcacheclient.FetchStatFactory for instantiation*
 */
public class FetchStat {
    public static class DeviceStat {
        private String name;
        private String id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
    
    private DeviceStat device;
    private long size;
    private String file;
    private long duration;
    private String connection;
    private String host;
    private float signal;
    private String meta;

    public DeviceStat getDevice() {
        return device;
    }

    public void setDevice(DeviceStat device) {
        this.device = device;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public float getSignal() {
        return signal;
    }

    public void setSignal(float signal) {
        this.signal = signal;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        return "FetchStat{" +
                "device=" + device +
                ", size=" + size +
                ", file='" + file + '\'' +
                ", duration=" + duration +
                ", connection='" + connection + '\'' +
                ", host='" + host + '\'' +
                ", signal=" + signal +
                ", meta='" + meta + '\'' +
                '}';
    }
}
