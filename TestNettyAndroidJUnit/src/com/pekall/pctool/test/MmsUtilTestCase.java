package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.model.mms.Mms;
import com.pekall.pctool.model.mms.Mms.Attachment;
import com.pekall.pctool.model.mms.MmsUtil;
import com.pekall.pctool.util.Slog;

import java.io.FileOutputStream;
import java.util.List;

public class MmsUtilTestCase extends AndroidTestCase {
    
    public void testQueryMms() throws Exception {
        List<Mms> mmses = MmsUtil.query(getContext());
        
        for (Mms mms : mmses) {
            Slog.d(mms.toString());
        }
    }
    
    public void testQueryMmsAttachments() throws Exception {
        List<Mms> mmses = MmsUtil.query(getContext());
        
        for (Mms mms : mmses) {
            if (mms.rowId == 51) {
                for (Attachment attachment : mms.attachments) {
                    Slog.d(attachment.toString());
                    
                    FileOutputStream fos = new FileOutputStream("/sdcard/" + attachment.name);
                    
                    fos.write(attachment.fileBytes);
                    Slog.d("write " + attachment.name);
                    
                    fos.close();
                }
                
                break;
            }
        }
    }
}
