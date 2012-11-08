package com.pekall.pctool.model.picture;

public class Picture {
    public long id;
    public String title;
    public String displayName;
    public String mimeType;
    public long dateTaken;
    public long size;
    public String data;
    public String bucketDisplayName;
    
    public Picture(long id, String title, String displayName, String mimeType, long dateTaken, long size, String data,
            String bucketDisplayName) {
        super();
        this.id = id;
        this.title = title;
        this.displayName = displayName;
        this.mimeType = mimeType;
        this.dateTaken = dateTaken;
        this.size = size;
        this.data = data;
        this.bucketDisplayName = bucketDisplayName;
    }

    @Override
    public String toString() {
        return "Picture [id=" + id + ", title=" + title + ", displayName=" + displayName + ", mimeType=" + mimeType
                + ", dateTaken=" + dateTaken + ", size=" + size + ", data=" + data + ", bucketDisplayName="
                + bucketDisplayName + "]";
    }
    
}
