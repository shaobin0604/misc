package com.pekall.pctool.model.picture;

import java.util.ArrayList;
import java.util.List;

public class QueryPictureResult {
    private List<Picture> pictures;
    private int offset;
    private int limit;
    private int totalCount;
    
    public QueryPictureResult(int offset, int limit, int totalCount) {
        pictures = new ArrayList<Picture>();
        this.offset = offset;
        this.limit = limit;
        this.totalCount = totalCount;
    }
    
    public void addPicture(Picture picture) {
        pictures.add(picture);
    }

    public List<Picture> getPictures() {
        return pictures;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public int getTotalCount() {
        return totalCount;
    }
    
    
}
