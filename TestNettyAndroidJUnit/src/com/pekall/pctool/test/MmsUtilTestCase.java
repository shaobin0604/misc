package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.mms.Mms;
import com.pekall.pctool.model.mms.MmsUtil;

import java.util.List;

public class MmsUtilTestCase extends AndroidTestCase {
    
    public void testQueryMms() throws Exception {
        List<Mms> mmses = MmsUtil.query(getContext());
        
        for (Mms mms : mmses) {
            Slog.d(mms.toString());
        }
    }
}
