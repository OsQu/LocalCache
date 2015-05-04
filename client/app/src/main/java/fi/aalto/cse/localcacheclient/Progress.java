package fi.aalto.cse.localcacheclient;

public class Progress {
    private int id;

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private float percentage;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    private String fileName;
    private long size;

    public Progress(int id, float percentage, String fileName, long size) {
        this.id = id;
        this.percentage = percentage;
        this.fileName = fileName;
        this.size = size;
    }
}
