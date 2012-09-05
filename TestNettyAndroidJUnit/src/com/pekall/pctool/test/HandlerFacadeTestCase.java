package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.HandlerFacade;
import com.pekall.pctool.protos.MsgDefProtos.AttachmentRecord;
import com.pekall.pctool.protos.MsgDefProtos.CmdRequest;
import com.pekall.pctool.protos.MsgDefProtos.CmdResponse;
import com.pekall.pctool.protos.MsgDefProtos.CmdType;
import com.pekall.pctool.protos.MsgDefProtos.ContactRecord;
import com.pekall.pctool.protos.MsgDefProtos.ContactsSync;
import com.pekall.pctool.protos.MsgDefProtos.MMSRecord;
import com.pekall.pctool.protos.MsgDefProtos.SlideRecord;
import com.pekall.pctool.protos.MsgDefProtos.SyncResult;
import com.pekall.pctool.protos.MsgDefProtos.SyncSubType;
import com.pekall.pctool.protos.MsgDefProtos.SyncType;

import java.io.FileOutputStream;
import java.util.List;

public class HandlerFacadeTestCase extends AndroidTestCase {
    
    private HandlerFacade mHandlerFacade;

    @Override
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
        
        mHandlerFacade = new HandlerFacade(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
        
        mHandlerFacade = null;
    }
    
    public void testQueryContacts() throws Exception {
        CmdRequest.Builder cmdRequestBuilder = CmdRequest.newBuilder();
        cmdRequestBuilder.setCmdType(CmdType.CMD_QUERY_CONTACTS);
        
        CmdResponse cmdResponse = mHandlerFacade.queryContact(cmdRequestBuilder.build());
        
        Slog.d(cmdResponse.toString());
    }
    
    public void testQueryMms() throws Exception {
        CmdRequest.Builder cmdRequestBuilder = CmdRequest.newBuilder();
        
        cmdRequestBuilder.setCmdType(CmdType.CMD_QUERY_MMS);
        
        CmdResponse cmdResponse = mHandlerFacade.queryMms(cmdRequestBuilder.build());
        
        Slog.d(cmdResponse.toString());
    }
    
    public void testQueryMmsAttachment() throws Exception {
        CmdRequest.Builder cmdRequestBuilder = CmdRequest.newBuilder();
        
        cmdRequestBuilder.setCmdType(CmdType.CMD_QUERY_MMS);
        
        CmdResponse cmdResponse = mHandlerFacade.queryMms(cmdRequestBuilder.build());
        
        if (cmdResponse.getCmdType() == CmdType.CMD_QUERY_MMS) {
            List<MMSRecord> mmsRecords = cmdResponse.getMmsRecordList();
            
            for (MMSRecord mmsRecord : mmsRecords) {
                if (mmsRecord.getMsgId() == 51) {
                    for (SlideRecord slideRecord : mmsRecord.getSlideList()) {
                        
                        for (AttachmentRecord attachmentRecord : slideRecord.getAttachmentList()) {
                            
                            Slog.d("attachment type = " + attachmentRecord.getType() + ", name = " + attachmentRecord.getName() + ", size = " + attachmentRecord.getSize());
                            
                            FileOutputStream fos = new FileOutputStream("/sdcard/" + attachmentRecord.getName());
                            
                            fos.write(attachmentRecord.getContent().toByteArray());
                            Slog.d("write " + attachmentRecord.getName());
                            
                            fos.close();
                        }
                        
                    }
                    
                    
                    break;
                }
            }
        }
    }
    
    public void testSyncContactWithOutlookTwoWaySlowSync() throws Exception {
        CmdRequest.Builder cmdRequestBuilder = CmdRequest.newBuilder();
        ContactsSync.Builder contactsSyncBuilder = ContactsSync.newBuilder();
        
        cmdRequestBuilder.setCmdType(CmdType.CMD_SYNC_CONTACTS);
        
        contactsSyncBuilder.setType(SyncType.OUTLOOK_PHONE);
        contactsSyncBuilder.setSubType(SyncSubType.TWO_WAY_SLOW_SYNC);
        
        cmdRequestBuilder.setContactsSync(contactsSyncBuilder.build());
        
        CmdResponse cmdResponse = mHandlerFacade.syncContactWithOutlook(cmdRequestBuilder.build());
        
        Slog.d(cmdResponse.toString());
    }
    
    public void testSyncContactWithOutlookTwoWaySlowSyncSecondAdd() throws Exception {
        CmdRequest.Builder cmdRequestBuilder = CmdRequest.newBuilder();
        ContactsSync.Builder contactsSyncBuilder = ContactsSync.newBuilder();
        ContactRecord.Builder contactRecordBuilder = ContactRecord.newBuilder();
        
        cmdRequestBuilder.setCmdType(CmdType.CMD_SYNC_CONTACTS);
        
        contactsSyncBuilder.setType(SyncType.OUTLOOK_PHONE);
        contactsSyncBuilder.setSubType(SyncSubType.TWO_WAY_SLOW_SYNC_SECOND);
        
        contactRecordBuilder.setName("sync add-1");
        contactRecordBuilder.setSyncResult(SyncResult.PC_ADD);
        contactRecordBuilder.setPcId("outlook-1");
        
        contactsSyncBuilder.addContactRecord(contactRecordBuilder.build());
        contactRecordBuilder.clear();
        
        contactRecordBuilder.setName("sync add-2");
        contactRecordBuilder.setSyncResult(SyncResult.PC_ADD);
        contactRecordBuilder.setPcId("outlook-2");
        
        contactsSyncBuilder.addContactRecord(contactRecordBuilder.build());
        contactRecordBuilder.clear();
        
        cmdRequestBuilder.setContactsSync(contactsSyncBuilder);
        
        CmdResponse cmdResponse = mHandlerFacade.syncContactWithOutlook(cmdRequestBuilder.build());
        
        Slog.d(cmdResponse.toString());
    }
    
    public void testSyncContactWithOutlookTwoWaySlowSyncSecondUpdate() throws Exception {
        CmdRequest.Builder cmdRequestBuilder = CmdRequest.newBuilder();
        ContactsSync.Builder contactsSyncBuilder = ContactsSync.newBuilder();
        ContactRecord.Builder contactRecordBuilder = ContactRecord.newBuilder();
        
        cmdRequestBuilder.setCmdType(CmdType.CMD_SYNC_CONTACTS);
        
        contactsSyncBuilder.setType(SyncType.OUTLOOK_PHONE);
        contactsSyncBuilder.setSubType(SyncSubType.TWO_WAY_SLOW_SYNC_SECOND);
        
        contactRecordBuilder.setId(10);
        contactRecordBuilder.setName("sync add-2");
        contactRecordBuilder.setNickname("pc modify");
        contactRecordBuilder.setSyncResult(SyncResult.PC_MODIFY);
        contactRecordBuilder.setPcId("outlook-2");
        
        contactsSyncBuilder.addContactRecord(contactRecordBuilder.build());
        contactRecordBuilder.clear();
        
        cmdRequestBuilder.setContactsSync(contactsSyncBuilder);
        
        CmdResponse cmdResponse = mHandlerFacade.syncContactWithOutlook(cmdRequestBuilder.build());
        
        Slog.d(cmdResponse.toString());
    }
    
    public void testSyncContactWithOutlookTwoSlowSyncSecondDelete() throws Exception {
        CmdRequest.Builder cmdRequestBuilder = CmdRequest.newBuilder();
        ContactsSync.Builder contactsSyncBuilder = ContactsSync.newBuilder();
        ContactRecord.Builder contactRecordBuilder = ContactRecord.newBuilder();
        
        cmdRequestBuilder.setCmdType(CmdType.CMD_SYNC_CONTACTS);
        
        contactsSyncBuilder.setType(SyncType.OUTLOOK_PHONE);
        contactsSyncBuilder.setSubType(SyncSubType.TWO_WAY_SLOW_SYNC_SECOND);
        
        contactRecordBuilder.setId(9);
        contactRecordBuilder.setSyncResult(SyncResult.PC_DEL);
        contactRecordBuilder.setPcId("outlook-1");
        
        contactsSyncBuilder.addContactRecord(contactRecordBuilder.build());
        contactRecordBuilder.clear();
        
        cmdRequestBuilder.setContactsSync(contactsSyncBuilder);
        
        CmdResponse cmdResponse = mHandlerFacade.syncContactWithOutlook(cmdRequestBuilder.build());
        
        Slog.d(cmdResponse.toString());
    }
    
    public void testSyncContactWithOutlookTwoSlowSyncSecondUpdatePhone1() throws Exception {
        CmdRequest.Builder cmdRequestBuilder = CmdRequest.newBuilder();
        ContactsSync.Builder contactsSyncBuilder = ContactsSync.newBuilder();
        ContactRecord.Builder contactRecordBuilder = ContactRecord.newBuilder();
        
        cmdRequestBuilder.setCmdType(CmdType.CMD_SYNC_CONTACTS);
        
        contactsSyncBuilder.setType(SyncType.OUTLOOK_PHONE);
        contactsSyncBuilder.setSubType(SyncSubType.TWO_WAY_SLOW_SYNC_SECOND);
        
        contactRecordBuilder.setId(10);
        contactRecordBuilder.setName("sync add-2");
        contactRecordBuilder.setNickname("pc modify");
        contactRecordBuilder.setSyncResult(SyncResult.PC_MODIFY);
        contactRecordBuilder.setPcId("outlook-2");
        
        contactsSyncBuilder.addContactRecord(contactRecordBuilder.build());
        contactRecordBuilder.clear();
        
        cmdRequestBuilder.setContactsSync(contactsSyncBuilder);
        
        CmdResponse cmdResponse = mHandlerFacade.syncContactWithOutlook(cmdRequestBuilder.build());
        
        Slog.d(cmdResponse.toString());
    }
}
