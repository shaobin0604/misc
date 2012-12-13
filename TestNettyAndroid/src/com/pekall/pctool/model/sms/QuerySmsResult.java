package com.pekall.pctool.model.sms;

import com.pekall.pctool.model.picture.Picture;

import java.util.ArrayList;
import java.util.List;

public class QuerySmsResult {
    private List<Sms> smses;
    private int offset;
    private int limit;
    private int totalCount;
    
    public QuerySmsResult(int offset, int limit, int totalCount) {
        smses = new ArrayList<Sms>();
        this.offset = offset;
        this.limit = limit;
        this.totalCount = totalCount;
    }
    
    public void addSms(Sms sms) {
        smses.add(sms);
    }

    public List<Sms> getSmses() {
        return smses;
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
