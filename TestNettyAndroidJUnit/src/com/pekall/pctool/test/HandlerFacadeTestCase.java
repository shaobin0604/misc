package com.pekall.pctool.test;

import android.provider.ContactsContract.CommonDataKinds;
import android.test.AndroidTestCase;
import android.text.format.Time;

import com.pekall.pctool.model.HandlerFacade;
import com.pekall.pctool.model.contact.Contact;
import com.pekall.pctool.model.contact.ContactUtil;
import com.pekall.pctool.model.contact.Contact.ImInfo;
import com.pekall.pctool.model.contact.Contact.ModifyTag;
import com.pekall.pctool.model.contact.Contact.PhoneInfo;
import com.pekall.pctool.protos.MsgDefProtos.AccountRecord;
import com.pekall.pctool.protos.MsgDefProtos.AgendaRecord;
import com.pekall.pctool.protos.MsgDefProtos.AgendaSync;
import com.pekall.pctool.protos.MsgDefProtos.AttachmentRecord;
import com.pekall.pctool.protos.MsgDefProtos.CmdRequest;
import com.pekall.pctool.protos.MsgDefProtos.CmdResponse;
import com.pekall.pctool.protos.MsgDefProtos.CmdType;
import com.pekall.pctool.protos.MsgDefProtos.ContactRecord;
import com.pekall.pctool.protos.MsgDefProtos.ContactsSync;
import com.pekall.pctool.protos.MsgDefProtos.EmailRecord;
import com.pekall.pctool.protos.MsgDefProtos.EmailRecord.EmailType;
import com.pekall.pctool.protos.MsgDefProtos.MMSRecord;
import com.pekall.pctool.protos.MsgDefProtos.PhoneRecord;
import com.pekall.pctool.protos.MsgDefProtos.SlideRecord;
import com.pekall.pctool.protos.MsgDefProtos.SyncConflictPloy;
import com.pekall.pctool.protos.MsgDefProtos.SyncResult;
import com.pekall.pctool.protos.MsgDefProtos.SyncSubType;
import com.pekall.pctool.protos.MsgDefProtos.SyncType;
import com.pekall.pctool.protos.MsgDefProtos.PhoneRecord.PhoneType;
import com.pekall.pctool.util.Slog;

import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

public class HandlerFacadeTestCase extends AndroidTestCase {
    
    private HandlerFacade mHandlerFacade;
    
    private static final String CONTACT_NAME = "unit test";
    private static final String CONTACT_NICKNAME = "unit test nick";
    
    private static final String PHONE_NUMBER_1 = "028-65478965";
    private static final int PHONE_TYPE_1 = CommonDataKinds.Phone.TYPE_HOME;
    
    private static final String IM_ACCOUNT_1 = "shaobin0604@gmail.com";
    private static final int IM_TYPE_1 = CommonDataKinds.Im.TYPE_HOME;
    
    private static final int COUNT_OF_INITIAL_CONTACT = 1;
    
    private long mContactId;
    
    
    private void populateContacts() {
        Contact contact = new Contact();
        contact.name = CONTACT_NAME;
        
        PhoneInfo phoneInfo = new PhoneInfo();
        phoneInfo.modifyFlag = ModifyTag.add;
        phoneInfo.number = PHONE_NUMBER_1;
        phoneInfo.type = PHONE_TYPE_1;
        
        contact.addPhoneInfo(phoneInfo);
        
        ImInfo imInfo = new ImInfo();
        imInfo.modifyFlag = ModifyTag.add;
        imInfo.account = IM_ACCOUNT_1;
        imInfo.protocol = IM_TYPE_1;
        
        contact.addImInfo(imInfo);
        
        mContactId = ContactUtil.addContact(getContext(), contact, true);
        
        assertTrue(mContactId > 0);
    }
    

    @Override
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
        
        mHandlerFacade = new HandlerFacade(getContext());
        
        ContactUtil.deleteAllContacts(getContext());
        
        populateContacts();
    }

    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
        
        mHandlerFacade = null;
    }
    
    //
    // Contacts
    //
    public void testQueryContacts() throws Exception {
        CmdRequest.Builder cmdRequestBuilder = CmdRequest.newBuilder();
        cmdRequestBuilder.setCmdType(CmdType.CMD_QUERY_CONTACTS);
        
        CmdResponse cmdResponse = mHandlerFacade.queryContact(cmdRequestBuilder.build());
        
        Slog.d(cmdResponse.toString());
        
        assertEquals(1, cmdResponse.getContactRecordList().size());
    }
    
    public void testAddContact() throws Exception {
        CmdRequest.Builder cmdRequestBuilder = CmdRequest.newBuilder();
        cmdRequestBuilder.setCmdType(CmdType.CMD_ADD_CONTACT);

        ContactRecord.Builder contactRecordBuilder = ContactRecord.newBuilder();
        AccountRecord.Builder accountRecordBuilder = AccountRecord.newBuilder();
        PhoneRecord.Builder phoneRecordBuilder = PhoneRecord.newBuilder();

        accountRecordBuilder.setName("contacts.account.name.local");
        accountRecordBuilder.setType("contacts.account.type.local");

        contactRecordBuilder.setAccountInfo(accountRecordBuilder.build());
        contactRecordBuilder.setName("testAddContact");
        contactRecordBuilder.setNickname("NICK testAddContact");

        phoneRecordBuilder.setType(PhoneType.MOBILE);
        phoneRecordBuilder.setNumber("18601219014");

        contactRecordBuilder.addPhone(phoneRecordBuilder.build());

        cmdRequestBuilder.setContactParams(contactRecordBuilder);
        
        CmdResponse cmdResponse = mHandlerFacade.handleCmdRequest(cmdRequestBuilder.build());
        
        assertEquals(1, cmdResponse.getContactRecordList().size());
    }
    
    public void testUpdateContact() throws Exception {
        // query exist contact
        CmdRequest.Builder cmdRequestBuilder = CmdRequest.newBuilder();
        cmdRequestBuilder.setCmdType(CmdType.CMD_QUERY_CONTACTS);
        
        CmdResponse cmdResponse = mHandlerFacade.queryContact(cmdRequestBuilder.build());
        ContactRecord contactRecord = cmdResponse.getContactRecord(0);
        
        // update contact
        ContactRecord.Builder contactRecordBuilder = ContactRecord.newBuilder(contactRecord);
        
        EmailRecord.Builder emailRecordBuilder = EmailRecord.newBuilder();
        emailRecordBuilder.setEmail("shaobin0604@qq.com");
        emailRecordBuilder.setType(EmailType.HOME);
        emailRecordBuilder.setModifyTag(com.pekall.pctool.protos.MsgDefProtos.ModifyTag.ADD);
        
        contactRecordBuilder.addEmail(emailRecordBuilder);
        
        cmdRequestBuilder = CmdRequest.newBuilder();
        cmdRequestBuilder.setCmdType(CmdType.CMD_EDIT_CONTACT);
        cmdRequestBuilder.setContactParams(contactRecordBuilder);
        
        cmdResponse = mHandlerFacade.handleCmdRequest(cmdRequestBuilder.build());
    }
    
    public void testAddAgenda() throws Exception {
        CmdRequest.Builder cmdRequestBuilder = CmdRequest.newBuilder();
        cmdRequestBuilder.setCmdType(CmdType.CMD_ADD_AGENDA);
        
        AgendaRecord.Builder agendaRecordBuilder = AgendaRecord.newBuilder();
        
        agendaRecordBuilder.setCalendarId(1);
        agendaRecordBuilder.setAlertTime(60);
        agendaRecordBuilder.setLocation("哈哈哈");
        agendaRecordBuilder.setNote("呵呵呵");
        agendaRecordBuilder.setSubject("测试");
        
        long now = System.currentTimeMillis();
        
        agendaRecordBuilder.setStartTime(now + 5 * 3600 * 1000);
        agendaRecordBuilder.setEndTime(now + 10 * 3600 * 1000);
        
        cmdRequestBuilder.setAgendaParams(agendaRecordBuilder);
        
        CmdResponse cmdResponse = mHandlerFacade.addAgenda(cmdRequestBuilder.build());
        
        Slog.d(cmdResponse.toString());
    }
    
    public void testQuerySms() throws Exception {
        CmdRequest.Builder cmdRequestBuilder = CmdRequest.newBuilder();
        
        cmdRequestBuilder.setCmdType(CmdType.CMD_QUERY_SMS);
        
        CmdResponse cmdResponse = mHandlerFacade.querySms(cmdRequestBuilder.build());
        
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
    
    //
    // Sync contacts
    //
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
    
    public void testSyncContactWithOutlookTwoWaySlowSyncSecondDelete() throws Exception {
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
    
    public void testSyncContactWithOutlookTwoWaySlowSyncSecondUpdatePhone1() throws Exception {
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
    
    // 
    // Sync Agenda
    //
    
    public void testSyncAgendaWithOutlookTwoWaySlowSync() throws Exception {
        CmdRequest.Builder cmdRequestBuilder = CmdRequest.newBuilder();
        AgendaSync.Builder agendaSyncBuilder = AgendaSync.newBuilder();
        
        cmdRequestBuilder.setCmdType(CmdType.CMD_SYNC_AGENDAS);
        
        agendaSyncBuilder.setType(SyncType.OUTLOOK_PHONE);
        agendaSyncBuilder.setSubType(SyncSubType.TWO_WAY_SLOW_SYNC);
        agendaSyncBuilder.setSyncConflictPloy(SyncConflictPloy.PHONE_SIDE);
        
        cmdRequestBuilder.setAgendaSync(agendaSyncBuilder);
        
        mHandlerFacade.handleCmdRequest(cmdRequestBuilder.build());
    }
    
    public void testSyncAgendaWithOutlookTwoWaySlowSyncSecondAdd() throws Exception {
        CmdRequest.Builder cmdRequestBuilder = CmdRequest.newBuilder();
        AgendaSync.Builder agendaSyncBuilder = AgendaSync.newBuilder();
        AgendaRecord.Builder agendaRecordBuilder = AgendaRecord.newBuilder();
        
        cmdRequestBuilder.setCmdType(CmdType.CMD_SYNC_AGENDAS);
        
        agendaSyncBuilder.setType(SyncType.OUTLOOK_PHONE);
        agendaSyncBuilder.setSubType(SyncSubType.TWO_WAY_SLOW_SYNC_SECOND);
        agendaSyncBuilder.setSyncConflictPloy(SyncConflictPloy.PC_SIDE);
        
        agendaRecordBuilder.setCalendarId(1);
        agendaRecordBuilder.setSubject("测试2");
        agendaRecordBuilder.setLocation("华洋2");
        
        long now = System.currentTimeMillis();
        
        agendaRecordBuilder.setStartTime(now + 5* 60 * 1000);
        agendaRecordBuilder.setEndTime(now + 10 * 60 * 1000);
        agendaRecordBuilder.setRepeatRule("");
        agendaRecordBuilder.setAlertTime(30);
        agendaRecordBuilder.setNote("测试2 说明");
        agendaRecordBuilder.setSyncResult(SyncResult.PC_ADD);
        agendaRecordBuilder.setPcId("3243fdhskafhdskfdflasj");
        
        agendaSyncBuilder.addAgendaRecord(agendaRecordBuilder.build());
        
        agendaRecordBuilder.clear();
        
        cmdRequestBuilder.setAgendaSync(agendaSyncBuilder);
        
        mHandlerFacade.handleCmdRequest(cmdRequestBuilder.build());
    }
}
