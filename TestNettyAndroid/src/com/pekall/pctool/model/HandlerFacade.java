
package com.pekall.pctool.model;

import android.content.Context;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;

import com.google.protobuf.ByteString;
import com.pekall.pctool.Slog;
import com.pekall.pctool.model.account.AccountInfo;
import com.pekall.pctool.model.app.AppInfo;
import com.pekall.pctool.model.app.AppUtil;
import com.pekall.pctool.model.app.AppUtil.AppNotExistException;
import com.pekall.pctool.model.calendar.CalendarInfo;
import com.pekall.pctool.model.calendar.CalendarUtil;
import com.pekall.pctool.model.calendar.EventInfo;
import com.pekall.pctool.model.contact.Contact;
import com.pekall.pctool.model.contact.Contact.AddressInfo;
import com.pekall.pctool.model.contact.Contact.EmailInfo;
import com.pekall.pctool.model.contact.Contact.ImInfo;
import com.pekall.pctool.model.contact.Contact.OrgInfo;
import com.pekall.pctool.model.contact.Contact.PhoneInfo;
import com.pekall.pctool.model.contact.ContactUtil;
import com.pekall.pctool.model.contact.GroupInfo;
import com.pekall.pctool.model.mms.Mms;
import com.pekall.pctool.model.mms.Mms.Attachment;
import com.pekall.pctool.model.mms.Mms.Slide;
import com.pekall.pctool.model.mms.MmsUtil;
import com.pekall.pctool.model.sms.Sms;
import com.pekall.pctool.model.sms.SmsUtil;
import com.pekall.pctool.protos.MsgDefProtos.AccountRecord;
import com.pekall.pctool.protos.MsgDefProtos.AddressRecord;
import com.pekall.pctool.protos.MsgDefProtos.AddressRecord.AddressType;
import com.pekall.pctool.protos.MsgDefProtos.AgendaRecord;
import com.pekall.pctool.protos.MsgDefProtos.AgendaSync;
import com.pekall.pctool.protos.MsgDefProtos.AppRecord;
import com.pekall.pctool.protos.MsgDefProtos.AppRecord.AppLocationType;
import com.pekall.pctool.protos.MsgDefProtos.AppRecord.AppType;
import com.pekall.pctool.protos.MsgDefProtos.AttachmentRecord;
import com.pekall.pctool.protos.MsgDefProtos.AttachmentRecord.AttachmentType;
import com.pekall.pctool.protos.MsgDefProtos.CalendarRecord;
import com.pekall.pctool.protos.MsgDefProtos.CmdRequest;
import com.pekall.pctool.protos.MsgDefProtos.CmdResponse;
import com.pekall.pctool.protos.MsgDefProtos.CmdResponse.Builder;
import com.pekall.pctool.protos.MsgDefProtos.CmdType;
import com.pekall.pctool.protos.MsgDefProtos.ContactRecord;
import com.pekall.pctool.protos.MsgDefProtos.ContactsSync;
import com.pekall.pctool.protos.MsgDefProtos.EmailRecord;
import com.pekall.pctool.protos.MsgDefProtos.EmailRecord.EmailType;
import com.pekall.pctool.protos.MsgDefProtos.GroupRecord;
import com.pekall.pctool.protos.MsgDefProtos.IMRecord;
import com.pekall.pctool.protos.MsgDefProtos.IMRecord.IMType;
import com.pekall.pctool.protos.MsgDefProtos.MMSRecord;
import com.pekall.pctool.protos.MsgDefProtos.ModifyTag;
import com.pekall.pctool.protos.MsgDefProtos.MsgOriginType;
import com.pekall.pctool.protos.MsgDefProtos.OrgRecord;
import com.pekall.pctool.protos.MsgDefProtos.OrgRecord.OrgType;
import com.pekall.pctool.protos.MsgDefProtos.PhoneRecord;
import com.pekall.pctool.protos.MsgDefProtos.PhoneRecord.PhoneType;
import com.pekall.pctool.protos.MsgDefProtos.SMSRecord;
import com.pekall.pctool.protos.MsgDefProtos.SlideRecord;
import com.pekall.pctool.protos.MsgDefProtos.SyncConflictPloy;
import com.pekall.pctool.protos.MsgDefProtos.SyncResult;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HandlerFacade {
    private static final int RESULT_CODE_OK = 0;
    private static final int RESULT_CODE_ERR_INSUFFICIENT_PARAMS = 100;
    private static final int RESULT_CODE_ERR_ILLEGAL_PARAMS = 101;
    private static final int RESULT_CODE_ERR_INTERNAL = 200;

    private static final String RESULT_MSG_OK = "OK";
    
    private static final boolean DUMP_CMD_REQUEST = true;

    private Context mContext;

    public HandlerFacade(Context context) {
        this.mContext = context;
    }

    /**
     * Return str if not null, otherwise empty string
     * 
     * @param str
     * @return the str if not null, otherwise empty string
     */
    private static String normalizeStr(String str) {
        return (str == null ? "" : str);
    }

    public CmdResponse unknownCmdResponse(CmdRequest request) {
        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();

        responseBuilder.setCmdType(CmdType.CMD_HEART_BEAT);

        setResultErrorInsufficentParams(responseBuilder, "Unknown CMD TYPE");

        return responseBuilder.build();
    }

    // ------------------------------------------------------------------------
    // Heartbeat
    // ------------------------------------------------------------------------

    public CmdResponse heartbeat(CmdRequest request) {
        Slog.d("heartbeat E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_HEART_BEAT);

        setResultOK(responseBuilder);

        Slog.d("heartbeat X");

        return responseBuilder.build();
    }

    // -------------------------------------------------------------------------
    // Sms related method
    // -------------------------------------------------------------------------

    public CmdResponse querySms(CmdRequest request) {
        Slog.d("querySms E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_QUERY_SMS);

        SMSRecord.Builder smsBuilder = SMSRecord.newBuilder();

        List<Sms> smsList = SmsUtil.querySmsList(mContext);

        for (Sms sms : smsList) {
            smsBuilder.setMsgId(sms.rowId);
            smsBuilder.setContactId(sms.person);
            smsBuilder.setMsgOrigin(smsTypeToMsgOriginType(sms.type));
            smsBuilder.setPhoneNum(normalizeStr(sms.address));
            smsBuilder.setMsgText(normalizeStr(sms.body));
            smsBuilder.setMsgTime(sms.date);
            smsBuilder.setReadTag(sms.read == Sms.READ_TRUE);

            responseBuilder.addSmsRecord(smsBuilder.build());

            smsBuilder.clear();
        }

        setResultOK(responseBuilder);
        Slog.d("querySms X");
        return responseBuilder.build();
    }

    public CmdResponse deleteSms(CmdRequest request) {
        Slog.d("deleteSms E");
        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_DELETE_SMS);

        List<Long> recordIdList = request.getRecordIdList();

        if (recordIdList != null && recordIdList.size() > 0) {
            if (SmsUtil.deleteSms(mContext, recordIdList)) {
                setResultOK(responseBuilder);
            } else {
                setResultErrorInternal(responseBuilder, "SmsUtil.deleteSms");
            }
        } else {
            setResultErrorInsufficentParams(responseBuilder, "recordIdList");
        }
        Slog.d("deleteSms X");
        return responseBuilder.build();
    }

    public CmdResponse importSms(CmdRequest request) {
        Slog.d("importSms E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_IMPORT_SMS);

        if (request.hasSmsParams()) {

            SMSRecord smsRecord = request.getSmsParams();
            String phoneNum = smsRecord.getPhoneNum();
            String msgText = smsRecord.getMsgText();
            MsgOriginType msgOriginType = smsRecord.getMsgOrigin();
            long msgTime = smsRecord.getMsgTime();

            Sms sms = new Sms();

            sms.address = phoneNum;
            sms.body = msgText;
            sms.type = msgOriginTypeToSmsType(msgOriginType);
            sms.date = msgTime;

            if (SmsUtil.importSms(mContext, sms) > 0) {
                setResultOK(responseBuilder);
            } else {
                setResultErrorInternal(responseBuilder, "SmsUtil.importSms");
            }
        } else {
            setResultErrorInsufficentParams(responseBuilder, "sms");
        }
        Slog.d("importSms X");
        return responseBuilder.build();
    }

    private static int msgOriginTypeToSmsType(MsgOriginType msgOriginType) {
        switch (msgOriginType) {
            case ANY:
                return Sms.TYPE_ALL;
            case INBOX:
                return Sms.TYPE_RECEIVED;
            case SENTBOX:
                return Sms.TYPE_SENT;
            case DRAFTBOX:
                return Sms.TYPE_DRAFT;
            case OUTBOX:
                return Sms.TYPE_OUTBOX;
            case FAILED:
                return Sms.TYPE_FAILED;
            case QUEUED:
                return Sms.TYPE_QUEUED;

            default:
                throw new IllegalArgumentException("Unkown MsgOriginType: "
                        + msgOriginType);
        }
    }

    private static MsgOriginType smsTypeToMsgOriginType(int smsType) {
        switch (smsType) {
            case Sms.TYPE_ALL:
                return MsgOriginType.ANY;
            case Sms.TYPE_RECEIVED:
                return MsgOriginType.INBOX;
            case Sms.TYPE_SENT:
                return MsgOriginType.SENTBOX;
            case Sms.TYPE_DRAFT:
                return MsgOriginType.DRAFTBOX;
            case Sms.TYPE_OUTBOX:
                return MsgOriginType.OUTBOX;
            case Sms.TYPE_FAILED:
                return MsgOriginType.FAILED;
            case Sms.TYPE_QUEUED:
                return MsgOriginType.QUEUED;

            default:
                throw new IllegalArgumentException("Unknown smsType: " + smsType);
        }
    }

    // ------------------------------------------------------------------------
    // MMS related method
    // ------------------------------------------------------------------------
    public CmdResponse queryMms(CmdRequest request) {
        Slog.d("queryMms E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_QUERY_MMS);

        MMSRecord.Builder mmsRecordBuilder = MMSRecord.newBuilder();
        SlideRecord.Builder slideRecordBuilder = SlideRecord.newBuilder();
        AttachmentRecord.Builder attachmentRecordBuilder = AttachmentRecord.newBuilder();

        List<Mms> mmsList = MmsUtil.query(mContext);
        

        for (Mms mms : mmsList) {
            
//            Slog.d(">>>>> dump mms >>>>>");
//            Slog.d(mms.toString());
//            Slog.d("<<<<< dump mms <<<<<");
            
            mmsRecordBuilder.setMsgId(mms.rowId);
            mmsRecordBuilder.setContactId(mms.person);
            mmsRecordBuilder.setMsgOrigin(mmsTypeToMsgOriginType(mms.msgBoxIndex));
            mmsRecordBuilder.setPhoneNum(normalizeStr(mms.phoneNum));
            mmsRecordBuilder.setSubject(normalizeStr(mms.subject));
            mmsRecordBuilder.setMsgTime(mms.date);
            mmsRecordBuilder.setReadTag(mms.isReaded == Mms.READ_TRUE);

            ArrayList<Slide> slides = mms.slides;
            ArrayList<Attachment> attachments = mms.attachments;
            
            List<Integer> slideAttachmentIndex = new ArrayList<Integer>();

            for (Slide slide : slides) {
                slideRecordBuilder.setDuration(slide.duration);
                slideRecordBuilder.setText(normalizeStr(slide.text));

                if (slide.imageIndex != -1) {
                    slideAttachmentIndex.add(slide.imageIndex);
                    
                    Attachment attachment = attachments.get(slide.imageIndex);

                    attachmentRecordBuilder.setType(AttachmentType.IMAGE);
                    attachmentRecordBuilder.setName(normalizeStr(attachment.name));
                    attachmentRecordBuilder.setSize(attachment.fileBytes.length);
                    attachmentRecordBuilder.setContent(ByteString.copyFrom(attachment.fileBytes));

                    slideRecordBuilder.addAttachment(attachmentRecordBuilder.build());

                    attachmentRecordBuilder.clear();
                }

                if (slide.audioIndex != -1) {
                    slideAttachmentIndex.add(slide.audioIndex);
                    
                    Attachment attachment = attachments.get(slide.audioIndex);

                    attachmentRecordBuilder.setType(AttachmentType.AUDIO);
                    attachmentRecordBuilder.setName(normalizeStr(attachment.name));
                    attachmentRecordBuilder.setSize(attachment.fileBytes.length);
                    attachmentRecordBuilder.setContent(ByteString.copyFrom(attachment.fileBytes));

                    slideRecordBuilder.addAttachment(attachmentRecordBuilder.build());

                    attachmentRecordBuilder.clear();
                }

                if (slide.videoIndex != -1) {
                    slideAttachmentIndex.add(slide.videoIndex);
                    
                    Attachment attachment = attachments.get(slide.videoIndex);

                    attachmentRecordBuilder.setType(AttachmentType.VIDEO);
                    attachmentRecordBuilder.setName(normalizeStr(attachment.name));
                    attachmentRecordBuilder.setSize(attachment.fileBytes.length);
                    attachmentRecordBuilder.setContent(ByteString.copyFrom(attachment.fileBytes));

                    slideRecordBuilder.addAttachment(attachmentRecordBuilder.build());

                    attachmentRecordBuilder.clear();
                }

                mmsRecordBuilder.addSlide(slideRecordBuilder.build());

                slideRecordBuilder.clear();
            }
            
            // mark slide attachment as null
            for (int index : slideAttachmentIndex) {
                attachments.set(index, null);
            }

            // the type of the rest attachments is OTHER
            for (Attachment attachment : attachments) {
                if (attachment == null) {
                    continue;
                }
                
                attachmentRecordBuilder.setType(AttachmentType.OTHER);
                attachmentRecordBuilder.setName(attachment.name);
                attachmentRecordBuilder.setSize(attachment.fileBytes.length);
                attachmentRecordBuilder.setContent(ByteString.copyFrom(attachment.fileBytes));

                mmsRecordBuilder.addAttachment(attachmentRecordBuilder.build());

                attachmentRecordBuilder.clear();
            }

            responseBuilder.addMmsRecord(mmsRecordBuilder.build());

            mmsRecordBuilder.clear();
        }

        setResultOK(responseBuilder);

        Slog.d("queryMms X");

        return responseBuilder.build();
    }

    private static int msgOriginTypeToMmsType(MsgOriginType msgOriginType) {
        switch (msgOriginType) {
            case ANY:
                return Mms.MESSAGE_BOX_ALL;
            case INBOX:
                return Mms.MESSAGE_BOX_INBOX;
            case SENTBOX:
                return Mms.MESSAGE_BOX_SENT;
            case DRAFTBOX:
                return Mms.MESSAGE_BOX_DRAFTS;
            case OUTBOX:
                return Mms.MESSAGE_BOX_OUTBOX;

            default:
                throw new IllegalArgumentException("Unkown MsgOriginType: "
                        + msgOriginType);
        }
    }

    private static MsgOriginType mmsTypeToMsgOriginType(int mmsType) {
        switch (mmsType) {
            case Mms.MESSAGE_BOX_ALL:
                return MsgOriginType.ANY;
            case Mms.MESSAGE_BOX_INBOX:
                return MsgOriginType.INBOX;
            case Mms.MESSAGE_BOX_SENT:
                return MsgOriginType.SENTBOX;
            case Mms.MESSAGE_BOX_DRAFTS:
                return MsgOriginType.DRAFTBOX;
            case Mms.MESSAGE_BOX_OUTBOX:
                return MsgOriginType.OUTBOX;
            default:
                throw new IllegalArgumentException("Unknown mmsType: " + mmsType);
        }
    }

    public CmdResponse deleteMms(CmdRequest request) {
        Slog.d("delete Mms E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_DELETE_MMS);

        List<Long> mmsIdList = request.getRecordIdList();

        if (mmsIdList != null && mmsIdList.size() > 0) {
            if (MmsUtil.delete(mContext, mmsIdList)) {
                setResultOK(responseBuilder);
            } else {
                setResultErrorInternal(responseBuilder, "MmsUtil.delete");
            }
        } else {
            setResultErrorInsufficentParams(responseBuilder, "recordIdList");
        }

        Slog.d("delete Mms X");
        return responseBuilder.build();
    }

    // ------------------------------------------------------------------------
    // Calendar related method
    // ------------------------------------------------------------------------
    public CmdResponse queryCalendar(CmdRequest request) {
        Slog.d("queryCalendar E");

        List<CalendarInfo> calendarInfoList = CalendarUtil
                .queryCalendarAll(mContext);

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_QUERY_CALENDAR);

        CalendarRecord.Builder calendarRecordBuilder = CalendarRecord.newBuilder();
        AccountRecord.Builder accountRecordBuilder = AccountRecord.newBuilder();

        for (CalendarInfo info : calendarInfoList) {
            calendarRecordBuilder.setId(info.caId);
            calendarRecordBuilder.setName(normalizeStr(info.name));

            accountRecordBuilder.setName(normalizeStr(info.accountInfo.accountName));
            accountRecordBuilder.setType(normalizeStr(info.accountInfo.accountType));

            calendarRecordBuilder.setAccountInfo(accountRecordBuilder.build());

            responseBuilder.addCalendarRecord(calendarRecordBuilder.build());

            accountRecordBuilder.clear();
            calendarRecordBuilder.clear();
        }

        setResultOK(responseBuilder);

        Slog.d("queryCalendar X");
        return responseBuilder.build();
    }

    public CmdResponse queryAgenda(CmdRequest request) {
        Slog.d("queryAgenda E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_QUERY_AGENDAS);

        long calendarId = CalendarUtil.INVALID_CALENDAR_ID;
        if (request.hasAgendaParams()) {
            AgendaRecord agendaRecord = request.getAgendaParams();
            calendarId = agendaRecord.getCalendarId();
        } else {
            Slog.e("calendar id not provided");
        }

        Slog.d("calendarId = " + calendarId);

        List<EventInfo> eventInfoList = CalendarUtil.queryEventsByCalendarId(mContext,
                calendarId);

        AgendaRecord.Builder agendaRecordBuilder = AgendaRecord.newBuilder();

        for (EventInfo eventInfo : eventInfoList) {
            agendaRecordBuilder.setId(eventInfo.id);
            agendaRecordBuilder.setCalendarId(eventInfo.calendarId);

            agendaRecordBuilder.setSubject(normalizeStr(eventInfo.title));
            agendaRecordBuilder.setLocation(normalizeStr(eventInfo.place));
            agendaRecordBuilder.setStartTime(eventInfo.startTime);
            agendaRecordBuilder.setEndTime(eventInfo.endTime);
            agendaRecordBuilder.setRepeatRule(normalizeStr(eventInfo.rrule));
            agendaRecordBuilder.setAlertTime(eventInfo.alertTime);
            agendaRecordBuilder.setNote(normalizeStr(eventInfo.note));

            responseBuilder.addAgendaRecord(agendaRecordBuilder.build());

            agendaRecordBuilder.clear();
        }

        setResultOK(responseBuilder);

        Slog.d("queryAgenda X");

        return responseBuilder.build();
    }

    public CmdResponse addAgenda(CmdRequest request) {
        Slog.d("addAgenda E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_ADD_AGENDA);

        if (request.hasAgendaParams()) {
            AgendaRecord agendaRecord = request.getAgendaParams();
            
            Slog.d("===== dump request =====");
            Slog.d(agendaRecord.toString());

            EventInfo eventInfo = new EventInfo();
            
            eventInfo.calendarId = agendaRecord.getCalendarId();
            eventInfo.title = agendaRecord.getSubject();
            eventInfo.place = agendaRecord.getLocation();
            eventInfo.startTime = agendaRecord.getStartTime();
            eventInfo.endTime = agendaRecord.getEndTime();
            eventInfo.alertTime = agendaRecord.getAlertTime();
            eventInfo.rrule = agendaRecord.getRepeatRule();
            eventInfo.note = agendaRecord.getNote();

            if (CalendarUtil.addEvent(mContext, eventInfo)) {
                setResultOK(responseBuilder);
            } else {
                setResultErrorInternal(responseBuilder, "CalendarUtil.addEvent");
            }

        } else {
            setResultErrorInsufficentParams(responseBuilder, "agenda");
        }

        Slog.d("addAgenda X");
        return responseBuilder.build();
    }

    public CmdResponse updateAgenda(CmdRequest cmdRequest) {
        Slog.d("updateAgenda E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_EDIT_AGENDA);

        if (cmdRequest.hasAgendaParams()) {
            AgendaRecord agendaRecord = cmdRequest.getAgendaParams();
            EventInfo eventInfo = new EventInfo();

            eventInfo.id = agendaRecord.getId();
            eventInfo.calendarId = agendaRecord.getCalendarId();
            eventInfo.title = agendaRecord.getSubject();
            eventInfo.place = agendaRecord.getLocation();
            eventInfo.startTime = agendaRecord.getStartTime();
            eventInfo.endTime = agendaRecord.getEndTime();
            eventInfo.alertTime = agendaRecord.getAlertTime();
            eventInfo.rrule = agendaRecord.getRepeatRule();
            eventInfo.note = agendaRecord.getNote();

            if (CalendarUtil.updateEvent(mContext, eventInfo)) {
                setResultOK(responseBuilder);
            } else {
                setResultErrorInternal(responseBuilder,
                        "CalendarUtil.updateEvent");
            }
        } else {
            setResultErrorInsufficentParams(responseBuilder, "agenda");
        }

        Slog.d("updateAgenda X");
        return responseBuilder.build();
    }

    public CmdResponse deleteAgenda(CmdRequest cmdRequest) {
        Slog.d("deleteAgenda E");

        boolean success = false;
        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_DELETE_AGENDA);

        List<Long> recordIdList = cmdRequest.getRecordIdList();
        if (recordIdList != null && recordIdList.size() > 0) {
            for (long eventInfoId : recordIdList) {
                success = CalendarUtil.deleteEvent(mContext, eventInfoId);
                if (!success) {
                    break;
                }
            }

            if (success) {
                setResultOK(responseBuilder);
            } else {
                setResultErrorInternal(responseBuilder,
                        "CalendarUtil.deleteEvent");
            }

        } else {
            setResultErrorInsufficentParams(responseBuilder, "record id list");
        }
        Slog.d("deleteAgenda X");
        return responseBuilder.build();
    }

    
    public CmdResponse syncAgendaWithOutlook(CmdRequest cmdRequest) {
        Slog.d("syncAgendaWithOutlook E");

        if (DUMP_CMD_REQUEST) {
            Slog.d(">>>>> dump CmdRequest >>>>>");
            Slog.d(cmdRequest.toString());
            Slog.d("<<<<< dump CmdRequest <<<<<");
        }
        
        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_SYNC_AGENDAS);
        
        if (cmdRequest.hasAgendaSync()) {
            AgendaSync agendaSync = cmdRequest.getAgendaSync();
            
            switch (agendaSync.getType()) {
                case PC_PHONE: {
                    
                    break;
                }
                case OUTLOOK_PHONE: {
                    handleSyncAgendaWithOutlook(agendaSync, responseBuilder);
                    break;
                }

                default: {
                    break;
                }
            }
        } else {
            setResultErrorInsufficentParams(responseBuilder, "Agenda Sync");
        }
        
        Slog.d("syncAgendaWithOutlook X");
        return responseBuilder.build();
    }
    
    private void handleSyncAgendaWithOutlook(AgendaSync agendaSync, Builder responseBuilder) {
        Slog.d("handleSyncAgendaWithOutlook E");
        
        switch (agendaSync.getSubType()) {
            case TWO_WAY_SLOW_SYNC: {
                handleTwoWaySyncAgenda(agendaSync, responseBuilder, /* fastSync */ false);
                break;
            }
            case TWO_WAY_FAST_SYNC: {
                handleTwoWaySyncAgenda(agendaSync, responseBuilder, /* fastSync */ true);
                break;
            }
            case TWO_WAY_SLOW_SYNC_SECOND: {
                handleTwoWaySyncAgendaSecond(agendaSync, responseBuilder);
                break;
            }
            case TWO_WAY_FAST_SYNC_SECOND: {
                handleTwoWaySyncAgendaSecond(agendaSync, responseBuilder);
                break;
            }

            default:
                break;
        }
        
        Slog.d("handleSyncAgendaWithOutlook X");        
    }

    private void handleTwoWaySyncAgenda(AgendaSync agendaSync, Builder responseBuilder, boolean fastSync) {
        // TODO Auto-generated method stub
        
    }
    
    private void handleTwoWaySyncAgendaSecond(AgendaSync agendaSync, Builder responseBuilder) {
        // TODO Auto-generated method stub
        
    }

    // ------------------------------------------------------------------------
    // Account related method
    // ------------------------------------------------------------------------

    public CmdResponse queryAccount(CmdRequest request) {
        Slog.d("queryAccount E");
        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_GET_ALL_ACCOUNTS);

        List<AccountInfo> accountInfoList = ContactUtil
                .getAllAccounts(mContext);

        AccountRecord.Builder accountRecordBuilder = AccountRecord.newBuilder();
        for (AccountInfo accountInfo : accountInfoList) {
            accountRecordBuilder.setName(normalizeStr(accountInfo.accountName));
            accountRecordBuilder.setType(normalizeStr(accountInfo.accountType));

            responseBuilder.addAccountRecord(accountRecordBuilder.build());
            accountRecordBuilder.clear();
        }
        setResultOK(responseBuilder);
        Slog.d("queryAccount X");
        return responseBuilder.build();
    }

    // ------------------------------------------------------------------------
    // Group related method
    // ------------------------------------------------------------------------

    public CmdResponse queryGroup(CmdRequest request) {
        Slog.d("queryGroup E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_GET_ALL_GROUPS);

        AccountRecord.Builder accountRecordBuilder = AccountRecord.newBuilder();
        GroupRecord.Builder groupRecordBuilder = GroupRecord.newBuilder();

        // List<AccountInfo> accountInfoList = ContactUtilSuperFast
        // .getAllAccounts(mContext);
        // for (AccountInfo accountInfo : accountInfoList) {
        // List<GroupInfo> groupInfoList =
        // ContactUtilSuperFast.queryGroup(mContext,
        // accountInfo);
        //
        // accountRecordBuilder.setName(normalizeStr(accountInfo.accountName));
        // accountRecordBuilder.setType(normalizeStr(accountInfo.accountType));
        //
        // for (GroupInfo groupInfo : groupInfoList) {
        // groupRecordBuilder.setId(groupInfo.grId);
        // groupRecordBuilder.setDataId(groupInfo.dataId);
        // groupRecordBuilder.setAccountInfo(accountRecordBuilder.build());
        // groupRecordBuilder.setName(normalizeStr(groupInfo.name));
        // groupRecordBuilder.setNote(normalizeStr(groupInfo.note));
        // groupRecordBuilder.setModifyTag(ModifyTag.SAME);
        //
        // responseBuilder.addGroupRecord(groupRecordBuilder.build());
        //
        // groupRecordBuilder.clear();
        // }
        //
        // accountRecordBuilder.clear();
        // }

        List<GroupInfo> groupInfos = ContactUtil.getAllGroups(mContext);

        for (GroupInfo groupInfo : groupInfos) {
            groupRecordBuilder.setId(groupInfo.grId);
            groupRecordBuilder.setDataId(groupInfo.dataId);
            
            accountRecordBuilder.setName(normalizeStr(groupInfo.accountInfo.accountName));
            accountRecordBuilder.setType(normalizeStr(groupInfo.accountInfo.accountType));
            
            groupRecordBuilder.setAccountInfo(accountRecordBuilder.build());
            groupRecordBuilder.setName(normalizeStr(groupInfo.name));
            groupRecordBuilder.setNote(normalizeStr(groupInfo.note));
            groupRecordBuilder.setModifyTag(ModifyTag.SAME);

            responseBuilder.addGroupRecord(groupRecordBuilder.build());

            groupRecordBuilder.clear();
            accountRecordBuilder.clear();
        }

        setResultOK(responseBuilder);

        Slog.d("queryGroup X");
        return responseBuilder.build();
    }

    public CmdResponse addGroup(CmdRequest request) {
        Slog.d("addGroup E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_ADD_GROUP);

        if (request.hasGroupParams()) {
            GroupRecord groupRecord = request.getGroupParams();

            GroupInfo groupInfo = new GroupInfo();

            groupInfo.name = groupRecord.getName();
            groupInfo.note = groupRecord.getNote();
            groupInfo.accountInfo.accountName = groupRecord.getAccountInfo()
                    .getName();
            groupInfo.accountInfo.accountType = groupRecord.getAccountInfo()
                    .getType();

            if (ContactUtil.addGroup(mContext, groupInfo)) {
                setResultOK(responseBuilder);
            } else {
                setResultErrorInternal(responseBuilder, "ContactUtilSuperFast.addGroup");
            }
        } else {
            setResultErrorInsufficentParams(responseBuilder, "group");
        }

        Slog.d("addGroup X");
        return responseBuilder.build();
    }

    public CmdResponse updateGroup(CmdRequest request) {
        Slog.d("updateGroup E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_EDIT_GROUP);

        if (request.hasGroupParams()) {
            GroupRecord groupRecord = request.getGroupParams();

            GroupInfo groupInfo = new GroupInfo();

            groupInfo.grId = groupRecord.getId();
            groupInfo.name = groupRecord.getName();
            groupInfo.note = groupRecord.getNote();

            if (ContactUtil.updateGroup(mContext, groupInfo)) {
                setResultOK(responseBuilder);
            } else {
                setResultErrorInternal(responseBuilder,
                        "ContactUtilSuperFast.updateGroup");
            }
        } else {
            setResultErrorInsufficentParams(responseBuilder, "group");
        }

        Slog.d("updateGroup X");
        return responseBuilder.build();
    }

    public CmdResponse deleteGroup(CmdRequest request) {
        Slog.d("deleteGroup E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_DELETE_GROUP);

        List<Long> groupIdList = request.getRecordIdList();
        boolean result = false;
        if (groupIdList != null && groupIdList.size() > 0) {
            for (long groupId : groupIdList) {
                result = ContactUtil.deleteGroup(mContext, groupId);

                if (!result) {
                    break;
                }
            }
            if (result) {
                setResultOK(responseBuilder);
            } else {
                setResultErrorInternal(responseBuilder,
                        "ContactUtilSuperFast.deleteGroup");
            }
        } else {
            setResultErrorInsufficentParams(responseBuilder, "recordIdList");
        }

        Slog.d("deleteGroup X");
        return responseBuilder.build();
    }

    // ------------------------------------------------------------------------
    // Contact related method
    // ------------------------------------------------------------------------
    public CmdResponse queryContact(CmdRequest request) {
        Slog.d("queryContact E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_QUERY_CONTACTS);

        List<Contact> contactList = ContactUtil.getAllContacts(mContext);
        
        Slog.d("Contacts number: " + contactList.size());
        
        ContactRecord.Builder contactRecordBuilder = ContactRecord.newBuilder();
        AccountRecord.Builder accountRecordBuilder = AccountRecord.newBuilder();
        GroupRecord.Builder groupRecordBuilder = GroupRecord.newBuilder();
        PhoneRecord.Builder phoneRecordBuilder = PhoneRecord.newBuilder();
        EmailRecord.Builder emailRecordBuilder = EmailRecord.newBuilder();
        IMRecord.Builder imRecordBuilder = IMRecord.newBuilder();
        AddressRecord.Builder addressRecordBuilder = AddressRecord.newBuilder();
        OrgRecord.Builder orgRecordBuilder = OrgRecord.newBuilder();
        
        for (Contact contact : contactList) {
            contactToContactRecord(contactRecordBuilder, accountRecordBuilder, groupRecordBuilder, phoneRecordBuilder,
                    emailRecordBuilder, imRecordBuilder, addressRecordBuilder, orgRecordBuilder, contact);
        
            responseBuilder.addContactRecord(contactRecordBuilder.build());
        
            accountRecordBuilder.clear();
            contactRecordBuilder.clear();
        }
        
        setResultOK(responseBuilder);
        Slog.d("queryContact X");
        return responseBuilder.build();
    }

    private void contactToContactRecord(ContactRecord.Builder contactRecordBuilder,
            AccountRecord.Builder accountRecordBuilder, GroupRecord.Builder groupRecordBuilder,
            PhoneRecord.Builder phoneRecordBuilder, EmailRecord.Builder emailRecordBuilder,
            IMRecord.Builder imRecordBuilder, AddressRecord.Builder addressRecordBuilder,
            OrgRecord.Builder orgRecordBuilder, Contact contact) {
        contactRecordBuilder.setId(contact.id);
        contactRecordBuilder.setVersion(contact.version);
        contactRecordBuilder.setName(normalizeStr(contact.name));
        contactRecordBuilder.setNickname(normalizeStr(contact.nickname));
        if (contact.photo != null && contact.photo.length > 0) {
            contactRecordBuilder.setPhoto(ByteString.copyFrom(contact.photo));
        } else {
            contactRecordBuilder.clearPhoto();
        }
        contactRecordBuilder.setPhotoModifyTag(false);
        
        contactRecordBuilder.setSyncResult(modifyTagToSyncResult(contact.modifyTag));

        accountRecordBuilder.setName(normalizeStr(contact.accountInfo.accountName));
        accountRecordBuilder.setType(normalizeStr(contact.accountInfo.accountType));

        contactRecordBuilder.setAccountInfo(accountRecordBuilder.build());

        Slog.d("groupInfos = " + contact.groupInfos);
        
        // group
        for (GroupInfo groupInfo : contact.groupInfos) {
            groupRecordBuilder.setId(groupInfo.grId); // only id is required
            groupRecordBuilder.setDataId(groupInfo.dataId);

            contactRecordBuilder.addGroup(groupRecordBuilder.build());

            groupRecordBuilder.clear();
        }

        // phone
        for (PhoneInfo phoneInfo : contact.phoneInfos) {
            phoneRecordBuilder.setId(phoneInfo.id);
            phoneRecordBuilder.setType(toPhoneType(phoneInfo.type));
            phoneRecordBuilder.setNumber(normalizeStr(phoneInfo.number));
            phoneRecordBuilder.setName(normalizeStr(phoneInfo.customName));

            phoneRecordBuilder.setModifyTag(ModifyTag.SAME);

            contactRecordBuilder.addPhone(phoneRecordBuilder.build());

            phoneRecordBuilder.clear();
        }

        // email
        for (EmailInfo emailInfo : contact.emailInfos) {
            emailRecordBuilder.setId(emailInfo.id);
            emailRecordBuilder.setType(toEmailType(emailInfo.type));
            emailRecordBuilder.setEmail(normalizeStr(emailInfo.email));
            emailRecordBuilder.setName(normalizeStr(emailInfo.customName));

            emailRecordBuilder.setModifyTag(ModifyTag.SAME);

            contactRecordBuilder.addEmail(emailRecordBuilder.build());

            emailRecordBuilder.clear();
        }

        // im
        for (ImInfo imInfo : contact.imInfos) {
            imRecordBuilder.setId(imInfo.id);
            imRecordBuilder.setType(toImType(imInfo.type));
            imRecordBuilder.setAccount(normalizeStr(imInfo.account));
            imRecordBuilder.setName(normalizeStr(imInfo.customName));

            imRecordBuilder.setModifyTag(ModifyTag.SAME);

            contactRecordBuilder.addIm(imRecordBuilder.build());

            imRecordBuilder.clear();
        }

        // address
        for (AddressInfo addressInfo : contact.addressInfos) {
            addressRecordBuilder.setId(addressInfo.id);
            addressRecordBuilder.setAddressType(toAddressType(addressInfo.type));
            addressRecordBuilder.setAddress(normalizeStr(addressInfo.address));
            addressRecordBuilder.setName(normalizeStr(addressInfo.customName));
            addressRecordBuilder.setCountry(normalizeStr(addressInfo.country));
            addressRecordBuilder.setProvince(normalizeStr(addressInfo.province));
            addressRecordBuilder.setCity(normalizeStr(addressInfo.city));
            addressRecordBuilder.setRoad(normalizeStr(addressInfo.street));
            addressRecordBuilder.setPostCode(normalizeStr(addressInfo.postcode));
            addressRecordBuilder.setModifyTag(ModifyTag.SAME);

            contactRecordBuilder.addAddress(addressRecordBuilder.build());

            addressRecordBuilder.clear();
        }

        // organization
        for (OrgInfo orgInfo : contact.orgInfos) {
            orgRecordBuilder.setId(orgInfo.id);
            orgRecordBuilder.setType(toOrgType(orgInfo.type));
            orgRecordBuilder.setName(normalizeStr(orgInfo.org));
            orgRecordBuilder.setName(normalizeStr(orgInfo.customName));

            orgRecordBuilder.setModifyTag(ModifyTag.SAME);

            contactRecordBuilder.addOrg(orgRecordBuilder.build());

            orgRecordBuilder.clear();
        }
    }
    
    private static SyncResult modifyTagToSyncResult(int modifyTag) {
        switch (modifyTag) {
            case com.pekall.pctool.model.contact.Contact.ModifyTag.add:
                return SyncResult.PHONE_ADD;
            case com.pekall.pctool.model.contact.Contact.ModifyTag.edit:
                return SyncResult.PHONE_MODIFY;
            case com.pekall.pctool.model.contact.Contact.ModifyTag.del:
                return SyncResult.PHONE_DEL;
            case com.pekall.pctool.model.contact.Contact.ModifyTag.same:
                return SyncResult.NO_CHANGE;
            default:
                final String msg = "Error unknown modifyTag = " + modifyTag;
                Slog.e(msg);
                throw new IllegalArgumentException(msg);
        }
    }

    public CmdResponse addContact(CmdRequest request) {
        Slog.d("addContact E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_ADD_CONTACT);

        if (request.hasContactParams()) {
            ContactRecord contactRecord = request.getContactParams();

            if (contactRecord != null) {
                Contact contact = contactRecordToContactForAdd(contactRecord);
                if (ContactUtil.addContact(mContext, contact) > 0) {
                    setResultOK(responseBuilder);
                } else {
                    setResultErrorInternal(responseBuilder, "ContactUtilSuperFast.addContact");
                }
            } else {
                setResultErrorInsufficentParams(responseBuilder, "contact");
            }

        } else {
            setResultErrorInsufficentParams(responseBuilder, "contact");
        }

        Slog.d("addContact X");

        return responseBuilder.build();
    }

    private Contact contactRecordToContactForAdd(ContactRecord contactRecord) {
        Contact contact = new Contact();

        contact.name = contactRecord.getName();
        contact.nickname = contactRecord.getNickname();

        if (contactRecord.hasPhoto()) {
            contact.photo = contactRecord.getPhoto().toByteArray();
        } else {
            contact.photo = null;
        }

        AccountRecord accountRecord = contactRecord.getAccountInfo();

        contact.setAccountInfo(accountRecord.getName(), accountRecord.getType());

        // group
        for (GroupRecord groupRecord : contactRecord.getGroupList()) {
            GroupInfo groupInfo = new GroupInfo();

            // only group id is required
            groupInfo.grId = groupRecord.getId();

            contact.addGroupInfo(groupInfo);
        }

        // phone
        for (PhoneRecord phoneRecord : contactRecord.getPhoneList()) {
            PhoneInfo phoneInfo = new PhoneInfo();

            phoneInfo.type = toCommonDataKindsPhoneType(phoneRecord.getType());
            phoneInfo.number = phoneRecord.getNumber();
            phoneInfo.customName = phoneRecord.getName();

            contact.addPhoneInfo(phoneInfo);
        }

        // email
        for (EmailRecord emailRecord : contactRecord.getEmailList()) {
            EmailInfo emailInfo = new EmailInfo();

            emailInfo.type = toCommonDataKindsEmailType(emailRecord.getType());
            emailInfo.email = emailRecord.getEmail();
            emailInfo.customName = emailRecord.getName();

            contact.addEmailInfo(emailInfo);
        }

        // im
        for (IMRecord imRecord : contactRecord.getImList()) {
            ImInfo imInfo = new ImInfo();

            imInfo.type = toCommonDataKindsImType(imRecord.getType());
            imInfo.account = imRecord.getAccount();
            imInfo.customName = imRecord.getName();

            contact.addImInfo(imInfo);
        }

        // address
        for (AddressRecord addressRecord : contactRecord.getAddressList()) {
            AddressInfo addressInfo = new AddressInfo();

            addressInfo.type = toCommonDataKindsAddressType(addressRecord.getAddressType());
            addressInfo.address = addressRecord.getAddress();
            addressInfo.customName = addressRecord.getName();
            addressInfo.country = addressRecord.getCountry();
            addressInfo.province = addressRecord.getProvince();
            addressInfo.city = addressRecord.getCity();
            addressInfo.street = addressRecord.getRoad();
            addressInfo.postcode = addressRecord.getPostCode();

            contact.addAddressInfo(addressInfo);
        }

        // organization
        for (OrgRecord orgRecord : contactRecord.getOrgList()) {
            OrgInfo orgInfo = new OrgInfo();

            orgInfo.type = toCommonDataKindsOrganizationType(orgRecord.getType());
            orgInfo.org = orgRecord.getOrgName();
            orgInfo.customName = orgRecord.getName();

            contact.addOrgInfo(orgInfo);
        }
        return contact;
    }

    public CmdResponse updateContact(CmdRequest request) {
        Slog.d("updateContact E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_EDIT_CONTACT);

        if (request.hasContactParams()) {
            ContactRecord contactRecord = request.getContactParams();

            if (contactRecord != null) {
                Slog.d("has contact param: " + contactRecord.toString());

                Contact contact = contactRecordToContactForUpdate(contactRecord);
                
                Slog.d("contact: " + contact);
                
                if (ContactUtil.updateContact(mContext, contact)) {
                    setResultOK(responseBuilder);
                } else {
                    setResultErrorInternal(responseBuilder, "ContactUtilSuperFast.updateContact");
                }

            } else {
                setResultErrorInsufficentParams(responseBuilder, "contact");
            }
        } else {
            setResultErrorInsufficentParams(responseBuilder, "contact");
        }

        Slog.d("updateContact X");
        return responseBuilder.build();
    }

    private Contact contactRecordToContactForUpdate(ContactRecord contactRecord) {
        Contact contact = new Contact();

        contact.id = contactRecord.getId();
        contact.name = contactRecord.getName();
        contact.nickname = contactRecord.getNickname();

        if (contactRecord.hasPhoto()) {
            contact.photo = contactRecord.getPhoto().toByteArray();
        } else {
            contact.photo = null;
        }

        contact.shouldUpdatePhoto = contactRecord.getPhotoModifyTag();

        AccountRecord accountRecord = contactRecord.getAccountInfo();

        contact.setAccountInfo(accountRecord.getName(), accountRecord.getType());

        // group
        for (GroupRecord groupRecord : contactRecord.getGroupList()) {
            GroupInfo groupInfo = new GroupInfo();

            // only group id is required
            
            if (groupRecord.hasId()) {
                groupInfo.grId = groupRecord.getId();
            }
            
            groupInfo.modifyFlag = toModelModifyTag(groupRecord.getModifyTag());

            contact.addGroupInfo(groupInfo);
        }

        // phone
        for (PhoneRecord phoneRecord : contactRecord.getPhoneList()) {
            PhoneInfo phoneInfo = new PhoneInfo();

            if (phoneRecord.hasId()) {
                phoneInfo.id = phoneRecord.getId();
            }
            phoneInfo.type = toCommonDataKindsPhoneType(phoneRecord.getType());
            phoneInfo.number = phoneRecord.getNumber();
            phoneInfo.customName = phoneRecord.getName();

            phoneInfo.modifyFlag = toModelModifyTag(phoneRecord.getModifyTag());

            contact.addPhoneInfo(phoneInfo);
        }

        // email
        for (EmailRecord emailRecord : contactRecord.getEmailList()) {
            EmailInfo emailInfo = new EmailInfo();
            
            if (emailRecord.hasId()) {
                emailInfo.id = emailRecord.getId();
            }
            emailInfo.type = toCommonDataKindsEmailType(emailRecord.getType());
            emailInfo.email = emailRecord.getEmail();
            emailInfo.customName = emailRecord.getName();

            emailInfo.modifyFlag = toModelModifyTag(emailRecord.getModifyTag());

            contact.addEmailInfo(emailInfo);
        }

        // im
        for (IMRecord imRecord : contactRecord.getImList()) {
            ImInfo imInfo = new ImInfo();

            if (imRecord.hasId()) {
                imInfo.id = imRecord.getId();
            }
            imInfo.type = toCommonDataKindsImType(imRecord.getType());
            imInfo.account = imRecord.getAccount();
            imInfo.customName = imRecord.getName();

            imInfo.modifyFlag = toModelModifyTag(imRecord.getModifyTag());

            contact.addImInfo(imInfo);
        }

        // address
        for (AddressRecord addressRecord : contactRecord.getAddressList()) {
            AddressInfo addressInfo = new AddressInfo();

            if (addressRecord.hasId()) {
                addressInfo.id = addressRecord.getId();
            }
            addressInfo.type = toCommonDataKindsAddressType(addressRecord.getAddressType());
            addressInfo.address = addressRecord.getAddress();
            addressInfo.customName = addressRecord.getName();
            addressInfo.country = addressRecord.getCountry();
            addressInfo.province = addressRecord.getProvince();
            addressInfo.city = addressRecord.getCity();
            addressInfo.street = addressRecord.getRoad();
            addressInfo.postcode = addressRecord.getPostCode();

            addressInfo.modifyFlag = toModelModifyTag(addressRecord.getModifyTag());

            contact.addAddressInfo(addressInfo);
        }

        // organization
        for (OrgRecord orgRecord : contactRecord.getOrgList()) {
            OrgInfo orgInfo = new OrgInfo();

            if (orgRecord.hasId()) {
                orgInfo.id = orgRecord.getId();
            }
            orgInfo.type = toCommonDataKindsOrganizationType(orgRecord.getType());
            orgInfo.org = orgRecord.getOrgName();
            orgInfo.customName = orgRecord.getName();

            orgInfo.modifyFlag = toModelModifyTag(orgRecord.getModifyTag());

            contact.addOrgInfo(orgInfo);
        }
        return contact;
    }

    public CmdResponse deleteContact(CmdRequest request) {
        Slog.d("deleteContact E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_DELETE_CONTACT);

        List<Long> contactIdList = request.getRecordIdList();

        if (contactIdList != null && contactIdList.size() > 0) {
            if (ContactUtil.deleteContactByIds(mContext, contactIdList)) {
                setResultOK(responseBuilder);
            } else {
                setResultErrorInternal(responseBuilder, "ContactUtilSuperFast.deleteContact");
            }
        } else {
            setResultErrorInsufficentParams(responseBuilder, "recordIdList");
        }

        Slog.d("deleteContact X");
        return responseBuilder.build();
    }
    
    //
    // Sync contact with outlook
    //
    public CmdResponse syncContactWithOutlook(CmdRequest cmdRequest) {
        Slog.d("syncContactWithOutlook E");
        
        if (DUMP_CMD_REQUEST) {
            Slog.d(">>>>> dump CmdRequest >>>>>");
            Slog.d(cmdRequest.toString());
            Slog.d("<<<<< dump CmdRequest <<<<<");
        }
        
        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_SYNC_CONTACTS);
        
        if (cmdRequest.hasContactsSync()) {
            ContactsSync contactsSync = cmdRequest.getContactsSync();
            
            switch (contactsSync.getType()) {
                case PC_PHONE: {
                    
                    break;
                }
                case OUTLOOK_PHONE: {
                    handleSyncContactWithOutlook(contactsSync, responseBuilder);
                    break;
                }

                default: {
                    break;
                }
            }
        } else {
            setResultErrorInsufficentParams(responseBuilder, "Contacts Sync");
        }
        
        Slog.d("syncContactWithOutlook X");
        return responseBuilder.build();
    }

    private void handleSyncContactWithOutlook(ContactsSync contactsSync, Builder responseBuilder) {
        Slog.d("handleSyncContactWithOutlook E");
        
        switch (contactsSync.getSubType()) {
            case TWO_WAY_SLOW_SYNC: {
                handleTwoWaySyncContact(contactsSync, responseBuilder, /* fastSync */ false);
                break;
            }
            case TWO_WAY_FAST_SYNC: {
                handleTwoWaySyncContact(contactsSync, responseBuilder, /* fastSync */ true);
                break;
            }
            case TWO_WAY_SLOW_SYNC_SECOND: {
                handleTwoWaySyncContactSecond(contactsSync, responseBuilder);
                break;
            }
            case TWO_WAY_FAST_SYNC_SECOND: {
                handleTwoWaySyncContactSecond(contactsSync, responseBuilder);
                break;
            }

            default:
                Slog.e("Error");
                break;
        }
        
        Slog.d("handleSyncContactWithOutlook X");
    }

    private void handleTwoWaySyncContact(ContactsSync contactsSync, Builder responseBuilder, boolean fastSync) {
        Slog.d("handleTwoWaySync E, fastSync = " + fastSync);
        
        List<Contact> contactList = null;
        if (fastSync) {
            contactList = FastSyncUtils.findChangedContacts(mContext);
        } else {
            contactList = ContactUtil.getAllContacts(mContext);
        }
        
        Slog.d("Contacts number: " + contactList.size());

        ContactRecord.Builder contactRecordBuilder = ContactRecord.newBuilder();
        AccountRecord.Builder accountRecordBuilder = AccountRecord.newBuilder();
        GroupRecord.Builder groupRecordBuilder = GroupRecord.newBuilder();
        PhoneRecord.Builder phoneRecordBuilder = PhoneRecord.newBuilder();
        EmailRecord.Builder emailRecordBuilder = EmailRecord.newBuilder();
        IMRecord.Builder imRecordBuilder = IMRecord.newBuilder();
        AddressRecord.Builder addressRecordBuilder = AddressRecord.newBuilder();
        OrgRecord.Builder orgRecordBuilder = OrgRecord.newBuilder();
        
        ContactsSync.Builder contactSyncBuilder = ContactsSync.newBuilder();
        
        contactSyncBuilder.setType(contactsSync.getType());
        contactSyncBuilder.setSubType(contactsSync.getSubType());
        
        if (contactsSync.hasSyncConflictPloy()) {
            contactSyncBuilder.setSyncConflictPloy(contactsSync.getSyncConflictPloy());
        }
        

        for (Contact contact : contactList) {
            contactToContactRecord(contactRecordBuilder, accountRecordBuilder, groupRecordBuilder, phoneRecordBuilder,
                    emailRecordBuilder, imRecordBuilder, addressRecordBuilder, orgRecordBuilder, contact);

            contactSyncBuilder.addContactRecord(contactRecordBuilder.build());

            accountRecordBuilder.clear();
            contactRecordBuilder.clear();
        }
        
        responseBuilder.setContactsSync(contactSyncBuilder);
        
        setResultOK(responseBuilder);
        
        Slog.d("handleTwoWaySync X, fastSync = " + fastSync);
    }
    
    
    
    private void handleTwoWaySyncContactSecond(ContactsSync contactsSync, Builder responseBuilder) {
        Slog.d("handleTwoWaySyncSecond E");
        
        ContactsSync.Builder contactSyncBuilder = ContactsSync.newBuilder();
        
        contactSyncBuilder.setType(contactsSync.getType());
        contactSyncBuilder.setSubType(contactsSync.getSubType());
        
        if (contactsSync.hasSyncConflictPloy()) {
            SyncConflictPloy syncConflictPloy = contactsSync.getSyncConflictPloy();

            Slog.d("SyncConflictPloy = " + syncConflictPloy);
            
            contactSyncBuilder.setSyncConflictPloy(contactsSync.getSyncConflictPloy());
        }
        
        ContactRecord.Builder contactRecordBuilder = ContactRecord.newBuilder();
        
        boolean success = true;
        
        for (ContactRecord contactRecord : contactsSync.getContactRecordList()) {
            final SyncResult syncResult = contactRecord.getSyncResult();
            final String pcId = contactRecord.getPcId();
            Slog.d("SyncResult = " + syncResult + ", pcId = " + pcId);
            
            switch (syncResult) {
                case PC_ADD: {
                    Contact contact = contactRecordToContactForAdd(contactRecord);
                    
                    final long contactId = ContactUtil.addContact(mContext, contact);
                    if (contactId > 0) {
                        int contactVersion = ContactUtil.getContactVersion(mContext, contactId);
                        
                        Slog.d("ContactUtilFast.addContact OK, contactId = " + contactId + ", contactVersion = " + contactVersion);
                        
                        contactRecordBuilder.setId(contactId);
                        contactRecordBuilder.setVersion(contactVersion);
                        contactRecordBuilder.setPcId(pcId);
                        contactRecordBuilder.setSyncResult(syncResult);
                        
                        contactSyncBuilder.addContactRecord(contactRecordBuilder.build());
                        
                        contactRecordBuilder.clear();
                    } else {
                        Slog.e("Error ContactUtilFast.addContact, contactId = " + contactId);
                        success = false;
                    }
                    break;
                }
                
                case PC_MODIFY: {
                    
                    break;
                }
                case BOTH_MODIFY: {
                    Contact contact = contactRecordToContactForUpdate(contactRecord);
                    
                    final long contactId = contact.id;
                    if (ContactUtil.updateContact(mContext, contact)) {
                        int contactVersion = ContactUtil.getContactVersion(mContext, contactId);
                        
                        Slog.d("ContactUtilFast.updateContact OK, contactId = " + contactId + ", contactVersion = " + contactVersion);
                        
                        contactRecordBuilder.setId(contactId);
                        contactRecordBuilder.setVersion(contactVersion);
                        contactRecordBuilder.setSyncResult(syncResult);
                        
                        contactSyncBuilder.addContactRecord(contactRecordBuilder.build());
                        
                        contactRecordBuilder.clear();
                    } else {
                        Slog.e("Error ContactUtilFast.updateContact, contactId = " + contactId);
                        success = false;
                    }
                    
                    break;
                }
                
                case PC_DEL: {
                    final long contactId = contactRecord.getId();
                    
                    if (ContactUtil.deleteContactById(mContext, contactId)) {
                        Slog.d("ContactUtilFast.deleteContactById OK, contactId = " + contactId);
                        
                        contactRecordBuilder.setId(contactId);
                        contactRecordBuilder.setSyncResult(syncResult);
                        
                        contactSyncBuilder.addContactRecord(contactRecordBuilder.build());
                        
                        contactRecordBuilder.clear();
                        
                    } else {
                        Slog.e("Error ContactUtilFast.deleteContactById, contactId = " + contactId);
                        success = false;
                    }
                    break;
                }
                
                default: {
                    Slog.e("Error invalid sync result = " + syncResult);
                    success = false;
                    break;
                }
            }
        }
        
        if (success) {
            responseBuilder.setContactsSync(contactSyncBuilder);
            
            setResultOK(responseBuilder);
        } else {
            setResultErrorInternal(responseBuilder, "add, update, delete");
        }
        
        FastSyncUtils.notifyUpdateContactVersionDB(mContext);
        
        Slog.d("handleTwoWaySyncSecond X");
    }
    

    /**
     * Convert protobuf ModifyTag to model ModifyTag
     * 
     * @param modifyTag
     * @return
     */
    private int toModelModifyTag(ModifyTag modifyTag) {
        switch (modifyTag) {
            case SAME:
                return com.pekall.pctool.model.contact.Contact.ModifyTag.same;
            case ADD:
                return com.pekall.pctool.model.contact.Contact.ModifyTag.add;
            case EDIT:
                return com.pekall.pctool.model.contact.Contact.ModifyTag.edit;
            case DEL:
                return com.pekall.pctool.model.contact.Contact.ModifyTag.del;
            default:
                throw new IllegalArgumentException("Unknown modifyTag: " + modifyTag);
        }
    }

    private PhoneType toPhoneType(int commonDataKindsPhoneType) {
        switch (commonDataKindsPhoneType) {
            case Phone.TYPE_MOBILE:
                return PhoneType.MOBILE;
            case Phone.TYPE_HOME:
                return PhoneType.HOME;
            case Phone.TYPE_WORK:
                return PhoneType.WORK;
            case Phone.TYPE_FAX_HOME:
                return PhoneType.HOME_FAX;
            case Phone.TYPE_FAX_WORK:
                return PhoneType.WORK_FAX;
            case Phone.TYPE_PAGER:
                return PhoneType.PAGER;
            case Phone.TYPE_MAIN:
                return PhoneType.MAIN;
            case Phone.TYPE_CUSTOM:
                return PhoneType.USER_DEFINED;
            case Phone.TYPE_OTHER:
                return PhoneType.OTHER;
            default:
                return PhoneType.OTHER;
        }
    }

    private int toCommonDataKindsPhoneType(PhoneType phoneType) {
        switch (phoneType) {
            case MOBILE:
                return Phone.TYPE_MOBILE;
            case HOME:
                return Phone.TYPE_HOME;
            case WORK:
                return Phone.TYPE_WORK;
            case HOME_FAX:
                return Phone.TYPE_FAX_HOME;
            case WORK_FAX:
                return Phone.TYPE_FAX_WORK;
            case PAGER:
                return Phone.TYPE_PAGER;
            case MAIN:
                return Phone.TYPE_MAIN;
            case USER_DEFINED:
                return Phone.TYPE_CUSTOM;
            case OTHER:
                return Phone.TYPE_OTHER;
            default:
                return Phone.TYPE_OTHER;
        }
    }

    private EmailType toEmailType(int commonDataKindsEmailType) {
        switch (commonDataKindsEmailType) {
            case Email.TYPE_HOME:
                return EmailType.HOME;
            case Email.TYPE_WORK:
                return EmailType.WORK;
            case Email.TYPE_CUSTOM:
                return EmailType.USER_DEFINED;
            case Email.TYPE_OTHER:
                return EmailType.OTHER;
            default:
                return EmailType.OTHER;
        }
    }

    private int toCommonDataKindsEmailType(EmailType emailType) {
        switch (emailType) {
            case HOME:
                return Email.TYPE_HOME;
            case WORK:
                return Email.TYPE_WORK;
            case USER_DEFINED:
                return Email.TYPE_CUSTOM;
            case OTHER:
                return Email.TYPE_OTHER;
            default:
                return Email.TYPE_OTHER;
        }
    }

    private IMType toImType(int commonDataKindsImType) {
        switch (commonDataKindsImType) {
            case Im.PROTOCOL_AIM:
                return IMType.AIM;
            case Im.PROTOCOL_MSN:
                return IMType.MSN;
            case Im.PROTOCOL_YAHOO:
                return IMType.YAHOO;
            case Im.PROTOCOL_SKYPE:
                return IMType.SKYPE;
            case Im.PROTOCOL_QQ:
                return IMType.QQ;
            case Im.PROTOCOL_GOOGLE_TALK:
                return IMType.GTALK;
            case Im.PROTOCOL_ICQ:
                return IMType.ICQ;
            case Im.PROTOCOL_JABBER:
                return IMType.JABBER;
            case Im.PROTOCOL_CUSTOM:
                return IMType.USER_DEFINED;
            case Im.PROTOCOL_NETMEETING:
                return IMType.NETMEETING;
            default:
                // FIXME: should not goes here
                return IMType.NETMEETING;
        }
    }

    private int toCommonDataKindsImType(IMType imType) {
        switch (imType) {
            case AIM:
                return Im.PROTOCOL_AIM;
            case MSN:
                return Im.PROTOCOL_MSN;
            case YAHOO:
                return Im.PROTOCOL_YAHOO;
            case SKYPE:
                return Im.PROTOCOL_SKYPE;
            case QQ:
                return Im.PROTOCOL_QQ;
            case GTALK:
                return Im.PROTOCOL_GOOGLE_TALK;
            case ICQ:
                return Im.PROTOCOL_ICQ;
            case JABBER:
                return Im.PROTOCOL_JABBER;
            case USER_DEFINED:
                return Im.PROTOCOL_CUSTOM;
            case NETMEETING:
                return Im.PROTOCOL_NETMEETING;
            default:
                return Im.PROTOCOL_NETMEETING;
        }
    }

    private AddressType toAddressType(int commonDataKindsAddressType) {
        switch (commonDataKindsAddressType) {
            case StructuredPostal.TYPE_HOME:
                return AddressType.HOME;
            case StructuredPostal.TYPE_WORK:
                return AddressType.WORK;
            case StructuredPostal.TYPE_CUSTOM:
                return AddressType.USER_DEFINED;
            case StructuredPostal.TYPE_OTHER:
                return AddressType.OTHER;
            default:
                return AddressType.OTHER;
        }
    }

    private int toCommonDataKindsAddressType(AddressType addressType) {
        switch (addressType) {
            case HOME:
                return StructuredPostal.TYPE_HOME;
            case WORK:
                return StructuredPostal.TYPE_WORK;
            case USER_DEFINED:
                return StructuredPostal.TYPE_CUSTOM;
            case OTHER:
                return StructuredPostal.TYPE_OTHER;
            default:
                return StructuredPostal.TYPE_OTHER;
        }
    }

    private OrgType toOrgType(int commonDataKindsOrganizationType) {
        switch (commonDataKindsOrganizationType) {
            case Organization.TYPE_WORK:
                return OrgType.COMPANY;
            case Organization.TYPE_CUSTOM:
                return OrgType.USER_DEFINED;
            case Organization.TYPE_OTHER:
                return OrgType.OTHER;
            default:
                return OrgType.OTHER;
        }
    }

    private int toCommonDataKindsOrganizationType(OrgType orgType) {
        switch (orgType) {
            case COMPANY:
                return Organization.TYPE_WORK;
            case USER_DEFINED:
                return Organization.TYPE_CUSTOM;
            case OTHER:
                return Organization.TYPE_OTHER;
            default:
                return Organization.TYPE_OTHER;
        }
    }

    // ------------------------------------------------------------------------
    // App related method
    // ------------------------------------------------------------------------

    public CmdResponse queryApp(CmdRequest request) {
        Slog.d("queryApp E");
        List<AppInfo> appInfos = AppUtil.getAppInfos(mContext);

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_QUERY_APP);

        AppRecord.Builder appBuilder = AppRecord.newBuilder();

        for (AppInfo appInfo : appInfos) {
            appBuilder.setAppName(appInfo.label);
            appBuilder.setAppType(appInfo.appType == AppInfo.FLAG_APP_TYPE_SYSTEM ? AppType.SYSTEM : AppType.USER);
            appBuilder
                    .setLocationType(appInfo.installLocation == AppInfo.FLAG_INSTALL_LOCATION_INTERNAL ? AppLocationType.INTERNAL
                            : AppLocationType.EXTERNAL);
            appBuilder.setPackageName(normalizeStr(appInfo.packageName));
            appBuilder.setVersionCode(appInfo.versionCode);
            appBuilder.setVersionName(normalizeStr(appInfo.versionName));
            appBuilder.setSize(appInfo.apkFileSize);
            appBuilder.setAppIcon(ByteString.copyFrom(appInfo.icon));
            appBuilder.setApkPath(appInfo.apkFilePath);

            responseBuilder.addAppRecord(appBuilder.build());

            appBuilder.clear();
        }

        setResultOK(responseBuilder);

        Slog.d("queryApp X");
        return responseBuilder.build();
    }

    public InputStream exportApp(String packageName)
            throws AppNotExistException {
        return AppUtil.getAppApkStream(mContext, packageName);
    }

    // -------------------------------------------------------------------------
    // Utility code
    // -------------------------------------------------------------------------
    private static void setResultOK(CmdResponse.Builder responseBuilder) {
        responseBuilder.setResultCode(RESULT_CODE_OK);
        responseBuilder.setResultMsg(RESULT_MSG_OK);
    }

    private static void setResultErrorInternal(
            CmdResponse.Builder responseBuilder, String methodCalled) {
        StringBuilder msgBuilder = new StringBuilder(
                "Error internal method call: ");
        msgBuilder.append(methodCalled);
        final String msg = msgBuilder.toString();

        Slog.e(msg);

        responseBuilder.setResultCode(RESULT_CODE_ERR_INTERNAL);
        responseBuilder.setResultMsg(msg);
    }

    private static void setResultErrorInsufficentParams(
            CmdResponse.Builder responseBuilder, String... params) {
        StringBuilder msgBuilder = new StringBuilder(
                "Error insufficient params: [");
        for (String param : params) {
            msgBuilder.append(param);
            msgBuilder.append(", ");
        }
        msgBuilder.append(']');
        final String msg = msgBuilder.toString();

        Slog.e(msg);

        responseBuilder.setResultCode(RESULT_CODE_ERR_INSUFFICIENT_PARAMS);
        responseBuilder.setResultMsg(msg);
    }
    
    private static void setResultErrorIllegalParams(
            CmdResponse.Builder responseBuilder, String... params) {
        StringBuilder msgBuilder = new StringBuilder(
                "Error insufficient params: [");
        for (String param : params) {
            msgBuilder.append(param);
            msgBuilder.append(", ");
        }
        msgBuilder.append(']');
        final String msg = msgBuilder.toString();

        Slog.e(msg);

        responseBuilder.setResultCode(RESULT_CODE_ERR_ILLEGAL_PARAMS);
        responseBuilder.setResultMsg(msg);
    }

    // -------------------------------------------------------------------------
    // TEST code
    // -------------------------------------------------------------------------

    public CmdResponse queryAppRecordList() {
        CmdResponse.Builder response = CmdResponse.newBuilder();
        response.setCmdType(CmdType.CMD_QUERY_APP);
        response.setResultCode(0);
        response.setResultMsg("OK");

        AppRecord.Builder app = AppRecord.newBuilder();

        app.setAppName("新浪微博");
        app.setAppType(AppType.USER);
        app.setLocationType(AppLocationType.INTERNAL);
        app.setPackageName("com.weibo");
        app.setVersionName("v2.0");
        app.setVersionCode(123456);
        app.setSize(2048);
        app.setAppIcon(ByteString.copyFrom(new byte[] {
                0, 1, 2, 3, 4, 5
        }));

        response.addAppRecord(app.build());

        app.clear();

        app.setAppName("腾讯微博");
        app.setAppType(AppType.USER);
        app.setLocationType(AppLocationType.INTERNAL);
        app.setPackageName("com.tencent.weibo");
        app.setVersionName("v2.3");
        app.setVersionCode(654321);
        app.setSize(1024);
        app.setAppIcon(ByteString.copyFrom(new byte[] {
                5, 4, 3, 2, 1, 0
        }));

        response.addAppRecord(app.build());

        return response.build();
    }

    

}
