package fi.aalto.cse.localcacheclient;

public interface OnProgressListener {
    public abstract void onProgressUpdate(Progress progress);
    public abstract void onNewFileDownload(Progress progress);
    public abstract void onFileCompleted(Progress progress);
    public abstract void onOverallProgress(int completed, int total);
}
