package fi.aalto.cse.localcacheclient;

public interface OnProgressListener {
    public abstract void onProgressUpdate(int completed, int total);
    public abstract void onNewFileDownload(String fileName);
}
