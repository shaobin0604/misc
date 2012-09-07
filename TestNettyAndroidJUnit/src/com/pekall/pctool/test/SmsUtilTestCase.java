
package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.sms.Sms;
import com.pekall.pctool.model.sms.SmsUtil;

import java.util.Calendar;
import java.util.List;

public class SmsUtilTestCase extends AndroidTestCase {
    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
//        SmsUtil.deleteSmsAll(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
//        SmsUtil.deleteSmsAll(getContext());
    }
    
    public void testQuerySms() throws Exception {
        List<Sms> smsList = SmsUtil.querySmsList(getContext());
        
        for (Sms sms : smsList) {
            Slog.d(sms.toString());
        }
    }
    
    public void testSmsOperation() throws Exception {
        Sms importSms = new Sms();
        importSms.address = "18601219014";
        String body1 = "Test import Sms reply1";
        importSms.body = body1;

        Calendar calendar = Calendar.getInstance();
        calendar.roll(Calendar.DAY_OF_MONTH, -2);
        
        importSms.date = calendar.getTimeInMillis();
        importSms.type = Sms.TYPE_RECEIVED;
        
        long rowId = SmsUtil.importSms(getContext(), importSms);
        Slog.d("rowId - " + rowId);
        
        assertTrue(rowId > 0);
        
        importSms = new Sms();
        importSms.address = "18601219014";
        String body2 = "Test import Sms reply2";
        importSms.body = body2;

        calendar = Calendar.getInstance();
        calendar.roll(Calendar.DAY_OF_MONTH, -3);
        
        importSms.date = calendar.getTimeInMillis();
        importSms.type = Sms.TYPE_SENT;
        
        rowId = SmsUtil.importSms(getContext(), importSms);
        Slog.d("rowId - " + rowId);
        
        assertTrue(rowId > 0);
        
        List<Sms> smsList = SmsUtil.querySmsList(getContext());
        assertEquals(2, smsList.size());
        
        SmsUtil.deleteSms(getContext(), rowId);
        
        smsList = SmsUtil.querySmsList(getContext());
        assertEquals(1, smsList.size());
        
        Sms sms = smsList.get(0);
        assertEquals(body1, sms.body);
    }
    
    public void testImportReceivedSms() throws Exception {
        Sms importSms = new Sms();
        importSms.address = "18601219014";
        importSms.body = "Test import Sms reply";

        Calendar calendar = Calendar.getInstance();
        calendar.roll(Calendar.DAY_OF_MONTH, -2);
        
        importSms.date = calendar.getTimeInMillis();
        importSms.type = Sms.TYPE_RECEIVED;
        
        long rowId = SmsUtil.importSms(getContext(), importSms);
        Slog.d("rowId - " + rowId);
        
        assertTrue(rowId > 0);
        
        List<Sms> smsList = SmsUtil.querySmsList(getContext());
        assertEquals(1, smsList.size());
        
        for (Sms sms : smsList) {
            Slog.d(sms.toString());
        }
    }
    
    public void testImportSentSms() throws Exception {
        Sms importSms = new Sms();
        importSms.address = "18601219014";
        importSms.body = "Test import Sms reply";

        Calendar calendar = Calendar.getInstance();
        calendar.roll(Calendar.DAY_OF_MONTH, -2);
        
        importSms.date = calendar.getTimeInMillis();
        importSms.type = Sms.TYPE_SENT;
        
        long rowId = SmsUtil.importSms(getContext(), importSms);
        Slog.d("rowId - " + rowId);
        
        assertTrue(rowId > 0);
        
        List<Sms> smsList = SmsUtil.querySmsList(getContext());
        assertEquals(1, smsList.size());
        
        for (Sms sms : smsList) {
            Slog.d(sms.toString());
        }
    }
}
