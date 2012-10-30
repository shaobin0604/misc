
package com.pekall.pctool.model;

import android.content.Context;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;

import com.google.protobuf.ByteString;
import com.pekall.pctool.ServerController;
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
import com.pekall.pctool.protos.MsgDefProtos.ConnectParam;
import com.pekall.pctool.protos.MsgDefProtos.ConnectParam.ConnectType;
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
import com.pekall.pctool.protos.MsgDefProtos.SyncSubType;
import com.pekall.pctool.util.DeviceInfoUtil;
import com.pekall.pctool.util.Slog;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class HandlerFacade {
    private static final boolean DUMP_CMD_REQUEST = true;
    private static final boolean DUMP_CMD_RESPONSE = true;
    
    private static void dumpCmdRequest(CmdRequest cmdRequest) {
        if (DUMP_CMD_REQUEST) {
            StringBuilder log = new StringBuilder();
            log.append("\n\n++++++++++ CMD_REQUEST ++++++++++\n");
            log.append(cmdRequest.toString());
            log.append( "++++++++++++++++++++++++++++++++++\n\n");
            Slog.d(log.toString());
        }
    }
    
    private static void dumpCmdResponse(CmdResponse cmdResponse) {
        if (DUMP_CMD_RESPONSE) {
            StringBuilder log = new StringBuilder();
            log.append("\n\n---------- CMD_RESPONSE ----------\n");
            log.append(cmdResponse.toString());
            log.append( "-----------------------------------\n\n");
            Slog.d(log.toString());
        }
    }
    
    private static final int RESULT_CODE_OK = 0;
    private static final int RESULT_CODE_ERR_INSUFFICIENT_PARAMS = 100;
    private static final int RESULT_CODE_ERR_ILLEGAL_PARAMS = 101;
    private static final int RESULT_CODE_ERR_AUTH_FAIL = 102;
    private static final int RESULT_CODE_ERR_PERMISSION_DENY = 103;
    private static final int RESULT_CODE_ERR_INTERNAL = 200;

    private static final String RESULT_MSG_OK = "OK";

    private Context mContext;

    public HandlerFacade(Context context) {
        this.mContext = context;
    }
    
    public Context getContext() {
        return mContext;
    }
    
    public CmdResponse handleCmdRequest(CmdRequest cmdRequest) {
        dumpCmdRequest(cmdRequest);

        CmdResponse cmdResponse;
        if (cmdRequest.hasCmdType()) {
            CmdType cmdType = cmdRequest.getCmdType();
            Slog.d("cmdType = " + cmdType);
            switch (cmdType) {
                //
                // HEARTBEAT related methods
                //
                case CMD_HEART_BEAT: {
                    cmdResponse = heartbeat(cmdRequest);
                    break;
                }
                
                //
                // Connection management related methods
                //
                case CMD_CONNECT: {
                    cmdResponse = connect(cmdRequest);
                    break;
                }
                
                case CMD_DISCONNECT: {
                    cmdResponse = disconnect(cmdRequest);
                    break;
                }
                
                //
                // APP related methods
                //
                case CMD_QUERY_APP: {
                    cmdResponse = queryApp(cmdRequest);
                    break;
                }
                
                case CMD_UNINSTALL_APP: {
                    cmdResponse = uninstallApp(cmdRequest);
                    break;
                }

                //
                // SMS related methods
                //
                case CMD_QUERY_SMS: {
                    cmdResponse = querySms(cmdRequest);
                    break;
                }

                case CMD_DELETE_SMS: {
                    cmdResponse = deleteSms(cmdRequest);
                    break;
                }

                case CMD_IMPORT_SMS: {
                    cmdResponse = importSms(cmdRequest);
                    break;
                }
                
                case CMD_SEND_SMS: {
                    cmdResponse = sendSms(cmdRequest);
                    break;
                }
                
                //
                // MMS related methods
                //
                case CMD_QUERY_MMS: {
                    cmdResponse = queryMms(cmdRequest);
                    break;
                }
                
                case CMD_DELETE_MMS: {
                    cmdResponse = deleteMms(cmdRequest);
                    break;
                }

                //
                // Calendar related methods
                //
                case CMD_QUERY_CALENDAR: {
                    cmdResponse = queryCalendar(cmdRequest);
                    break;
                }
                
                case CMD_QUERY_AGENDAS: {
                    cmdResponse = queryAgenda(cmdRequest);
                    break;
                }

                case CMD_ADD_AGENDA: {
                    cmdResponse = addAgenda(cmdRequest);
                    break;
                }
                
                case CMD_EDIT_AGENDA: {
                    cmdResponse = updateAgenda(cmdRequest);
                    break;
                }
                
                case CMD_DELETE_AGENDA: {
                    cmdResponse = deleteAgenda(cmdRequest);
                    break;
                }
                
                //
                // Contact related methods
                //
                case CMD_GET_ALL_ACCOUNTS: {
                    cmdResponse = queryAccount(cmdRequest);
                    break;
                }
                
                case CMD_GET_ALL_GROUPS: {
                    cmdResponse = queryGroup(cmdRequest);
                    break;
                }
                
                case CMD_ADD_GROUP: {
                    cmdResponse = addGroup(cmdRequest);
                    break;
                }
                
                case CMD_EDIT_GROUP: {
                    cmdResponse = updateGroup(cmdRequest);
                    break;
                }
                
                case CMD_DELETE_GROUP: {
                    cmdResponse = deleteGroup(cmdRequest);
                    break;
                }
                
                case CMD_QUERY_CONTACTS: {
                    cmdResponse = queryContact(cmdRequest);
                    break;
                }
                
                case CMD_ADD_CONTACT: {
                    cmdResponse = addContact(cmdRequest);
                    break;
                }
                
                case CMD_EDIT_CONTACT: {
                    cmdResponse = updateContact(cmdRequest);
                    break;
                }
                
                case CMD_DELETE_CONTACT: {
                    cmdResponse = deleteContact(cmdRequest);
                    break;
                }
                
                case CMD_SYNC_CONTACTS: {
                    cmdResponse = syncContactWithOutlook(cmdRequest);
                    break;
                }
                
                case CMD_SYNC_AGENDAS: {
                    cmdResponse = syncAgendaWithOutlook(cmdRequest);
                    break;
                }
                
                default: {
                    // should not goes here
                    cmdResponse = unknownCmdResponse(cmdRequest);
                    break;
                }
            }
        } else {
            Slog.e("Error CmdType must be provided");
            cmdResponse = unknownCmdResponse(cmdRequest);
        }
        
        dumpCmdResponse(cmdResponse);
        
        return cmdResponse;
    }


    private CmdResponse connect(CmdRequest cmdRequest) {
        Slog.d("connect E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(cmdRequest.getCmdType());
        
        if (cmdRequest.hasConnectParam()) {
            ConnectParam connectParam = cmdRequest.getConnectParam();
            final ConnectType connectType = connectParam.getConnectType();
            String hostname = connectParam.getHostName();
            
            ConnectParam.Builder connectResultBuilder = ConnectParam.newBuilder();
            connectResultBuilder.setConnectType(connectType);
            connectResultBuilder.setDeviceModel(DeviceInfoUtil.getDeviceModel());
            connectResultBuilder.setDeviceImei(DeviceInfoUtil.getDeviceUuid(mContext));
            
            switch (connectType) {
                case USB: {
                    ServerController.setServiceState(ServerController.STATE_CONNECTED);
                    ServerController.setHostname(hostname);
                    ServerController.sendServerStateBroadcast(mContext, ServerController.STATE_CONNECTED);
                    
                    responseBuilder.setConnectResult(connectResultBuilder);
                    
                    setResultOK(responseBuilder);
                    break;
                }
                case WIFI: {
                    if (connectParam.hasSecret()) {
                        String wifiSecret = connectParam.getSecret();
                        if (ServerController.isWifiSecretMatch(wifiSecret)) {
                            ServerController.setServiceState(ServerController.STATE_CONNECTED);
                            ServerController.setHostname(hostname);
                            ServerController.sendServerStateBroadcast(mContext, ServerController.STATE_CONNECTED);
                            
                            responseBuilder.setConnectResult(connectResultBuilder);
                            
                            setResultOK(responseBuilder);
                        } else {
                            setResultErrorAuthFail(responseBuilder, "secret");
                        }
                    } else {
                        setResultErrorInsufficentParams(responseBuilder, "secret");
                    }
                    
                    break;
                }
                default: {
                    setResultErrorIllegalParams(responseBuilder, "connectType: " + connectType);
                    break;
                }
            }
        } else {
            setResultErrorInsufficentParams(responseBuilder, "connect");
        }

        Slog.d("connect X");

        return responseBuilder.build();
    }
    
    private CmdResponse disconnect(CmdRequest cmdRequest) {
        Slog.d("disconnect E");
        
        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(cmdRequest.getCmdType());
        setResultOK(responseBuilder);
        
        ServerController.setServiceState(ServerController.STATE_DISCONNECTED);
        ServerController.sendServerStateBroadcast(mContext, ServerController.STATE_CONNECTED);
        
        Slog.d("disconnect X");
        return responseBuilder.build();
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
            if (SmsUtil.deletePhoneSms(mContext, recordIdList)) {
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

            if (SmsUtil.importPhoneSms(mContext, sms) > 0) {
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
    
    public CmdResponse sendSms(CmdRequest request) {
        Slog.d("sendSms E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_SEND_SMS);

        if (request.hasSmsParams()) {

            SMSRecord smsRecord = request.getSmsParams();
            
            String phoneNum = smsRecord.getPhoneNum();
            String msgText = smsRecord.getMsgText();

            Sms sms = new Sms();

            sms.address = phoneNum;
            sms.body = msgText;

            long newSmsId = SmsUtil.sendSms(mContext, sms);
            if (newSmsId > 0) {
                SMSRecord.Builder smsRecordBuilder = SMSRecord.newBuilder(smsRecord);
                smsRecordBuilder.setMsgId(newSmsId);
                
                responseBuilder.addSmsRecord(smsRecordBuilder);
                setResultOK(responseBuilder);
            } else {
                setResultErrorInternal(responseBuilder, "SmsUtil.sendSms");
            }
        } else {
            setResultErrorInsufficentParams(responseBuilder, "sms");
        }
        Slog.d("sendSms X");
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
            mmsRecordBuilder.setSize(mms.size);

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
                .queryAllCalendars(mContext);

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

        List<EventInfo> eventInfoList;
        if (request.hasAgendaParams()) {
            Slog.i("calendar id provided");
            AgendaRecord agendaRecord = request.getAgendaParams();
            long calendarId = agendaRecord.getCalendarId();
            
            eventInfoList = CalendarUtil.queryEventsByCalendarId(mContext, calendarId);
        } else {
            Slog.i("calendar id not provided");
            eventInfoList = CalendarUtil.queryAllEvents(mContext);
        }

        AgendaRecord.Builder agendaRecordBuilder = AgendaRecord.newBuilder();

        for (EventInfo eventInfo : eventInfoList) {
            eventInfoToAgendaRecord(agendaRecordBuilder, eventInfo);

            responseBuilder.addAgendaRecord(agendaRecordBuilder.build());

            agendaRecordBuilder.clear();
        }

        setResultOK(responseBuilder);

        Slog.d("queryAgenda X");

        return responseBuilder.build();
    }

    private void eventInfoToAgendaRecord(AgendaRecord.Builder agendaRecordBuilder, EventInfo eventInfo) {
        agendaRecordBuilder.setId(eventInfo.id);
        agendaRecordBuilder.setCalendarId(eventInfo.calendarId);
        agendaRecordBuilder.setSubject(normalizeStr(eventInfo.title));
        agendaRecordBuilder.setLocation(normalizeStr(eventInfo.place));
        agendaRecordBuilder.setStartTime(eventInfo.startTime);
        agendaRecordBuilder.setEndTime(eventInfo.endTime);
        agendaRecordBuilder.setRepeatRule(normalizeStr(eventInfo.rrule));
        agendaRecordBuilder.setAlertTime(eventInfo.alertTime);
        agendaRecordBuilder.setNote(normalizeStr(eventInfo.note));
        agendaRecordBuilder.setVersion(eventInfo.getChecksum());
        
        agendaRecordBuilder.setSyncResult(modifyTagToSyncResult(eventInfo.modifyTag));
    }

    public CmdResponse addAgenda(CmdRequest request) {
        Slog.d("addAgenda E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_ADD_AGENDA);

        if (request.hasAgendaParams()) {
            AgendaRecord agendaRecord = request.getAgendaParams();

            EventInfo eventInfo = agendaRecordToEventInfoForAdd(agendaRecord);

            final long newEventId = CalendarUtil.addEvent(mContext, eventInfo);
            if (newEventId > 0) {
                AgendaRecord.Builder agendaRecordBuilder = AgendaRecord.newBuilder(agendaRecord);
                agendaRecordBuilder.setId(newEventId);
                
                responseBuilder.addAgendaRecord(agendaRecordBuilder);
                
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

    private EventInfo agendaRecordToEventInfoForAdd(AgendaRecord agendaRecord) {
        EventInfo eventInfo = new EventInfo();
        
        eventInfo.calendarId = agendaRecord.getCalendarId();
        eventInfo.title = agendaRecord.getSubject();
        eventInfo.place = agendaRecord.getLocation();
        eventInfo.startTime = agendaRecord.getStartTime();
        eventInfo.endTime = agendaRecord.getEndTime();
        eventInfo.alertTime = agendaRecord.getAlertTime();
        eventInfo.rrule = agendaRecord.getRepeatRule();
        eventInfo.note = agendaRecord.getNote();
        return eventInfo;
    }
    
    private EventInfo agendaRecordToEventInfoForSyncAdd(AgendaRecord agendaRecord, long calendarId) {
        EventInfo eventInfo = new EventInfo();
        
        eventInfo.calendarId = calendarId;
        eventInfo.title = agendaRecord.getSubject();
        eventInfo.place = agendaRecord.getLocation();
        eventInfo.startTime = agendaRecord.getStartTime();
        eventInfo.endTime = agendaRecord.getEndTime();
        eventInfo.alertTime = agendaRecord.getAlertTime();
        eventInfo.rrule = agendaRecord.getRepeatRule();
        eventInfo.note = agendaRecord.getNote();
        return eventInfo;
    }

    public CmdResponse updateAgenda(CmdRequest cmdRequest) {
        Slog.d("updateAgenda E");

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_EDIT_AGENDA);

        if (cmdRequest.hasAgendaParams()) {
            AgendaRecord agendaRecord = cmdRequest.getAgendaParams();
            
            EventInfo eventInfo = agendaRecordToEventInfoForUpdate(agendaRecord);

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

    private EventInfo agendaRecordToEventInfoForUpdate(AgendaRecord agendaRecord) {
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
        return eventInfo;
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
    
    /*
     * Only sync with local account
     */
    private void handleSyncAgendaWithOutlook(AgendaSync agendaSync, Builder responseBuilder) {
        Slog.d("handleSyncAgendaWithOutlook E");
        
        final SyncSubType subSyncType = agendaSync.getSubType();
        switch (subSyncType) {
            case TWO_WAY_SLOW_SYNC: {
                handleSyncAgendaTwoWay(agendaSync, responseBuilder, /* fastSync */ false);
                break;
            }
            case TWO_WAY_FAST_SYNC: {
                handleSyncAgendaTwoWay(agendaSync, responseBuilder, /* fastSync */ true);
                break;
            }
            case TWO_WAY_SLOW_SYNC_SECOND: {
                handleSyncAgendaTwoWaySecond(agendaSync, responseBuilder);
                break;
            }
            case TWO_WAY_FAST_SYNC_SECOND: {
                handleSyncAgendaTwoWaySecond(agendaSync, responseBuilder);
                break;
            }
            case PHONE_REFRESH_SYNC: {
                handleSyncAgendaPhoneRefresh(agendaSync, responseBuilder);
                break;
            }
            case PC_REFRESH_SYNC: {
                handleSyncAgendaPcRefresh(agendaSync, responseBuilder);
                break;
            }

            default:
                Slog.e("unsupported subSyncType: " + subSyncType);
                break;
        }
        
        Slog.d("handleSyncAgendaWithOutlook X");        
    }

    /**
     * Overwrite phone's events with outlook's events
     * 
     * @param agendaSync
     * @param responseBuilder
     */
    private void handleSyncAgendaPcRefresh(AgendaSync agendaSync, Builder responseBuilder) {
        Slog.d("handleSyncAgendaPcRefresh E");
        
        AgendaSync.Builder agendaSyncBuilder = AgendaSync.newBuilder();
        agendaSyncBuilder.setType(agendaSync.getType());
        agendaSyncBuilder.setSubType(agendaSync.getSubType());
        if (agendaSync.hasSyncConflictPloy()) {
            agendaSyncBuilder.setSyncConflictPloy(agendaSync.getSyncConflictPloy());
        }
        
        //
        // 1. delete all events
        //
        final int count = CalendarUtil.deleteEventAll(mContext);
        Slog.d("delete count = " + count);
        
        //
        // 2. add event from outlook
        //
        AgendaRecord.Builder agendaRecordBuilder = AgendaRecord.newBuilder();
        
        boolean success = true;
        
        final List<AgendaRecord> agendaRecordList = agendaSync.getAgendaRecordList();
        
        Slog.d("agendaRecordList size = " + agendaRecordList.size());
        
        long defaultCalendarId = CalendarUtil.getDefaultCalendarId(mContext);
        
        for (AgendaRecord agendaRecord : agendaRecordList) {
            final SyncResult syncResult = agendaRecord.getSyncResult();
            final String pcId = agendaRecord.getPcId();
            Slog.d("syncResult = " + syncResult + ", pcId = " + pcId);
            
            switch (syncResult) {
                case PC_ADD: {
                    EventInfo eventInfo = agendaRecordToEventInfoForSyncAdd(agendaRecord, defaultCalendarId);
                    
                    final long eventInfoId = CalendarUtil.addEvent(mContext, eventInfo);
                    if (eventInfoId > 0) {
                        long eventVersion = CalendarUtil.queryEventVersion(mContext, eventInfoId);
                        
                        Slog.d("CalendarUtil.addEvent OK, eventInfoId = " + eventInfoId + ", eventVersion = " + eventVersion);
                        
                        agendaRecordBuilder.setId(eventInfoId);
                        agendaRecordBuilder.setVersion(eventVersion);
                        agendaRecordBuilder.setPcId(pcId);
                        agendaRecordBuilder.setSyncResult(syncResult);
                        
                        agendaSyncBuilder.addAgendaRecord(agendaRecordBuilder.build());
                        
                        agendaRecordBuilder.clear();
                    } else {
                        Slog.e("Error CalendarUtil.addEvent, eventInfoId = " + eventInfoId);
                        success = false;
                    }
                    break;
                }
                
                default: {
                    Slog.e("Error invalid syncResult = " + syncResult);
                    break;
                }
            }
        }
        
        if (success) {
            responseBuilder.setAgendaSync(agendaSyncBuilder);
            
            setResultOK(responseBuilder);
        } else {
            setResultErrorInternal(responseBuilder, "CalendarUtil.addEvent");
        }
        
        FastSyncUtils.notifyUpdateEventVersionDB(mContext);
        
        Slog.d("handleSyncAgendaPcRefresh X");
    }

    /**
     * Overwrite outlook's events with phone's events
     * 
     * @param agendaSync
     * @param responseBuilder
     */
    private void handleSyncAgendaPhoneRefresh(AgendaSync agendaSync, Builder responseBuilder) {
        Slog.d("handleSyncAgendaPhoneRefresh E");
        
        AgendaSync.Builder agendaSyncBuilder = AgendaSync.newBuilder();
        agendaSyncBuilder.setType(agendaSync.getType());
        agendaSyncBuilder.setSubType(agendaSync.getSubType());
        if (agendaSync.hasSyncConflictPloy()) {
            agendaSyncBuilder.setSyncConflictPloy(agendaSync.getSyncConflictPloy());
        }
        
        AgendaRecord.Builder agendaRecordBuilder = AgendaRecord.newBuilder();
        
        List<EventInfo> eventInfos = CalendarUtil.queryAllEvents(mContext);
        
        Slog.d("eventInfos count = " + eventInfos.size());

        for (EventInfo eventInfo : eventInfos) {
            eventInfoToAgendaRecord(agendaRecordBuilder, eventInfo);

            //
            // We use phone's event to overwrite outlook's event, so mark as PHONE_ADD
            //
            agendaRecordBuilder.setSyncResult(SyncResult.PHONE_ADD);
            
            agendaSyncBuilder.addAgendaRecord(agendaRecordBuilder.build());

            agendaRecordBuilder.clear();
        }

        responseBuilder.setAgendaSync(agendaSyncBuilder);

        setResultOK(responseBuilder);
        
        FastSyncUtils.notifyUpdateEventVersionDB(mContext);
        
        Slog.d("handleSyncAgendaPhoneRefresh X");
        
    }

    private void handleSyncAgendaTwoWay(AgendaSync agendaSync, Builder responseBuilder, boolean fastSync) {
        Slog.d("handleSyncAgendaTwoWay E, fastSync = " + fastSync);

        List<EventInfo> eventInfos;
        if (fastSync) {
            eventInfos = FastSyncUtils.findChangedEvents(mContext);
        } else {
            eventInfos = CalendarUtil.queryAllEvents(mContext);
        }
        
        AgendaSync.Builder agendaSyncBuilder = AgendaSync.newBuilder();
        agendaSyncBuilder.setType(agendaSync.getType());
        agendaSyncBuilder.setSubType(agendaSync.getSubType());
        if (agendaSync.hasSyncConflictPloy()) {
            agendaSyncBuilder.setSyncConflictPloy(agendaSync.getSyncConflictPloy());
        }
        
        AgendaRecord.Builder agendaRecordBuilder = AgendaRecord.newBuilder();

        Slog.d("eventInfos size = " + eventInfos.size());
        
        for (EventInfo eventInfo : eventInfos) {
            eventInfoToAgendaRecord(agendaRecordBuilder, eventInfo);

            agendaSyncBuilder.addAgendaRecord(agendaRecordBuilder.build());

            agendaRecordBuilder.clear();
        }

        
        responseBuilder.setAgendaSync(agendaSyncBuilder);
        
        setResultOK(responseBuilder);
        
        Slog.d("handleSyncAgendaTwoWay X, fastSync = " + fastSync);
    }
    
    private void handleSyncAgendaTwoWaySecond(AgendaSync agendaSync, Builder responseBuilder) {
        Slog.d("handleSyncAgendaTwoWaySecond E");
        
        AgendaSync.Builder agendaSyncBuilder = AgendaSync.newBuilder();
        
        agendaSyncBuilder.setType(agendaSync.getType());
        agendaSyncBuilder.setSubType(agendaSync.getSubType());
        
        if (agendaSync.hasSyncConflictPloy()) {
            SyncConflictPloy syncConflictPloy = agendaSync.getSyncConflictPloy();

            Slog.d("SyncConflictPloy = " + syncConflictPloy);
            
            agendaSyncBuilder.setSyncConflictPloy(agendaSync.getSyncConflictPloy());
        }
        
        AgendaRecord.Builder agendaRecordBuilder = AgendaRecord.newBuilder();
        
        boolean success = true;
        
        final List<AgendaRecord> agendaRecordList = agendaSync.getAgendaRecordList();
        
        Slog.d("agendaRecordList size = " + agendaRecordList.size());
        
        long defaultCalendarId = CalendarUtil.getDefaultCalendarId(mContext);
        
        for (AgendaRecord agendaRecord : agendaRecordList) {
            final SyncResult syncResult = agendaRecord.getSyncResult();
            final String pcId = agendaRecord.getPcId();
            Slog.d("SyncResult = " + syncResult + ", pcId = " + pcId);
            
            switch (syncResult) {
                case PC_ADD: {
                    EventInfo eventInfo = agendaRecordToEventInfoForSyncAdd(agendaRecord, defaultCalendarId);
                    
                    final long eventInfoId = CalendarUtil.addEvent(mContext, eventInfo);
                    if (eventInfoId > 0) {
                        long eventVersion = CalendarUtil.queryEventVersion(mContext, eventInfoId);
                        
                        Slog.d("CalendarUtil.addEvent OK, eventInfoId = " + eventInfoId + ", eventVersion = " + eventVersion);
                        
                        agendaRecordBuilder.setId(eventInfoId);
                        agendaRecordBuilder.setVersion(eventVersion);
                        agendaRecordBuilder.setPcId(pcId);
                        agendaRecordBuilder.setSyncResult(syncResult);
                        
                        agendaSyncBuilder.addAgendaRecord(agendaRecordBuilder.build());
                        
                        agendaRecordBuilder.clear();
                    } else {
                        Slog.e("Error CalendarUtil.addEvent, eventInfoId = " + eventInfoId);
                        success = false;
                    }
                    break;
                }
                
                case PC_MODIFY:
                case BOTH_MODIFY: {
                    EventInfo eventInfo = agendaRecordToEventInfoForUpdate(agendaRecord);
                    
                    final long eventId = eventInfo.id;
                    if (CalendarUtil.updateEvent(mContext, eventInfo)) {
                        long eventVersion = CalendarUtil.queryEventVersion(mContext, eventId);
                        
                        Slog.d("CalendarUtil.updateEvent OK, eventId = " + eventId + ", eventVersion = " + eventVersion);
                        
                        agendaRecordBuilder.setId(eventId);
                        agendaRecordBuilder.setVersion(eventVersion);
                        agendaRecordBuilder.setPcId(pcId);
                        agendaRecordBuilder.setSyncResult(syncResult);
                        
                        agendaSyncBuilder.addAgendaRecord(agendaRecordBuilder.build());
                        
                        agendaRecordBuilder.clear();
                    } else {
                        Slog.e("Error CalendarUtil.updateEvent, eventId = " + eventId);
                        success = false;
                    }
                    
                    break;
                }
                
                case PC_DEL: {
                    final long eventId = agendaRecord.getId();
                    
                    if (CalendarUtil.deleteEvent(mContext, eventId)) {
                        Slog.d("CalendarUtil.deleteEvent OK, eventInfoId = " + eventId);
                        
                        agendaRecordBuilder.setId(eventId);
                        agendaRecordBuilder.setPcId(pcId);
                        agendaRecordBuilder.setSyncResult(syncResult);
                        
                        agendaSyncBuilder.addAgendaRecord(agendaRecordBuilder.build());
                        
                        agendaRecordBuilder.clear();
                        
                    } else {
                        Slog.e("Error CalendarUtil.deleteEvent, eventInfoId = " + eventId);
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
            responseBuilder.setAgendaSync(agendaSyncBuilder);
            
            setResultOK(responseBuilder);
        } else {
            setResultErrorInternal(responseBuilder, "add, update, delete Event");
        }
        
        FastSyncUtils.notifyUpdateEventVersionDB(mContext);
        
        Slog.d("handleSyncAgendaTwoWaySecond X");
    }

    // ------------------------------------------------------------------------
    // Account related method
    // ------------------------------------------------------------------------

    public CmdResponse queryAccount(CmdRequest request) {
        Slog.d("queryAccount E");
        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_GET_ALL_ACCOUNTS);

        Set<AccountInfo> accountInfoList = ContactUtil
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

        Collection<Contact> contactList = ContactUtil.getAllContacts(mContext);
        
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

//      Slog.d("groupInfos = " + contact.groupInfos);
        
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
            emailRecordBuilder.setEmail(normalizeStr(emailInfo.address));
            emailRecordBuilder.setName(normalizeStr(emailInfo.customName));

            emailRecordBuilder.setModifyTag(ModifyTag.SAME);

            contactRecordBuilder.addEmail(emailRecordBuilder.build());

            emailRecordBuilder.clear();
        }

        // im
        for (ImInfo imInfo : contact.imInfos) {
            imRecordBuilder.setId(imInfo.id);
            imRecordBuilder.setType(toImType(imInfo.protocol));
            imRecordBuilder.setAccount(normalizeStr(imInfo.account));
            imRecordBuilder.setName(normalizeStr(imInfo.customProtocol));

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
            addressRecordBuilder.setProvince(normalizeStr(addressInfo.region));
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
            orgRecordBuilder.setOrgName(normalizeStr(orgInfo.company));
            orgRecordBuilder.setName(normalizeStr(orgInfo.customName));

            orgRecordBuilder.setModifyTag(ModifyTag.SAME);

            contactRecordBuilder.addOrg(orgRecordBuilder.build());

            orgRecordBuilder.clear();
        }
    }
    
    private static SyncResult modifyTagToSyncResult(int modifyTag) {
//      Slog.d("modifyTag = " + com.pekall.pctool.model.contact.Contact.ModifyTag.toString(modifyTag));
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
                final long newContactId = ContactUtil.addContact(mContext, contact);
                if (newContactId > 0) {
                    
                    // return the new created contact

                    contact = ContactUtil.getContactById(mContext, newContactId);
                    
                    ContactRecord.Builder contactRecordBuilder = ContactRecord.newBuilder();
                    AccountRecord.Builder accountRecordBuilder = AccountRecord.newBuilder();
                    GroupRecord.Builder groupRecordBuilder = GroupRecord.newBuilder();
                    PhoneRecord.Builder phoneRecordBuilder = PhoneRecord.newBuilder();
                    EmailRecord.Builder emailRecordBuilder = EmailRecord.newBuilder();
                    IMRecord.Builder imRecordBuilder = IMRecord.newBuilder();
                    AddressRecord.Builder addressRecordBuilder = AddressRecord.newBuilder();
                    OrgRecord.Builder orgRecordBuilder = OrgRecord.newBuilder();
                    
                    contactToContactRecord(contactRecordBuilder, accountRecordBuilder, groupRecordBuilder, phoneRecordBuilder,
                            emailRecordBuilder, imRecordBuilder, addressRecordBuilder, orgRecordBuilder, contact);
                
                    responseBuilder.addContactRecord(contactRecordBuilder.build());
                    
                    accountRecordBuilder.clear();
                    contactRecordBuilder.clear();
                    
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
    
    private Contact contactRecordToContactForSyncAdd(ContactRecord contactRecord, String accountName, String accountType) {
        Contact contact = new Contact();

        contact.name = contactRecord.getName();
        contact.nickname = contactRecord.getNickname();

        if (contactRecord.hasPhoto()) {
            contact.photo = contactRecord.getPhoto().toByteArray();
        } else {
            contact.photo = null;
        }

        contact.setAccountInfo(accountName, accountType);

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
            emailInfo.address = emailRecord.getEmail();
            emailInfo.customName = emailRecord.getName();

            contact.addEmailInfo(emailInfo);
        }

        // im
        for (IMRecord imRecord : contactRecord.getImList()) {
            ImInfo imInfo = new ImInfo();

            imInfo.protocol = toCommonDataKindsImType(imRecord.getType());
            imInfo.account = imRecord.getAccount();
            imInfo.customProtocol = imRecord.getName();

            contact.addImInfo(imInfo);
        }

        // address
        for (AddressRecord addressRecord : contactRecord.getAddressList()) {
            AddressInfo addressInfo = new AddressInfo();

            addressInfo.type = toCommonDataKindsAddressType(addressRecord.getAddressType());
            addressInfo.address = addressRecord.getAddress();
            addressInfo.customName = addressRecord.getName();
            addressInfo.country = addressRecord.getCountry();
            addressInfo.region = addressRecord.getProvince();
            addressInfo.city = addressRecord.getCity();
            addressInfo.street = addressRecord.getRoad();
            addressInfo.postcode = addressRecord.getPostCode();

            contact.addAddressInfo(addressInfo);
        }

        // organization
        for (OrgRecord orgRecord : contactRecord.getOrgList()) {
            OrgInfo orgInfo = new OrgInfo();

            orgInfo.type = toCommonDataKindsOrganizationType(orgRecord.getType());
            orgInfo.company = orgRecord.getOrgName();
            orgInfo.customName = orgRecord.getName();

            contact.addOrgInfo(orgInfo);
        }
        return contact;
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
            emailInfo.address = emailRecord.getEmail();
            emailInfo.customName = emailRecord.getName();

            contact.addEmailInfo(emailInfo);
        }

        // im
        for (IMRecord imRecord : contactRecord.getImList()) {
            ImInfo imInfo = new ImInfo();

            imInfo.protocol = toCommonDataKindsImType(imRecord.getType());
            imInfo.account = imRecord.getAccount();
            imInfo.customProtocol = imRecord.getName();

            contact.addImInfo(imInfo);
        }

        // address
        for (AddressRecord addressRecord : contactRecord.getAddressList()) {
            AddressInfo addressInfo = new AddressInfo();

            addressInfo.type = toCommonDataKindsAddressType(addressRecord.getAddressType());
            addressInfo.address = addressRecord.getAddress();
            addressInfo.customName = addressRecord.getName();
            addressInfo.country = addressRecord.getCountry();
            addressInfo.region = addressRecord.getProvince();
            addressInfo.city = addressRecord.getCity();
            addressInfo.street = addressRecord.getRoad();
            addressInfo.postcode = addressRecord.getPostCode();

            contact.addAddressInfo(addressInfo);
        }

        // organization
        for (OrgRecord orgRecord : contactRecord.getOrgList()) {
            OrgInfo orgInfo = new OrgInfo();

            orgInfo.type = toCommonDataKindsOrganizationType(orgRecord.getType());
            orgInfo.company = orgRecord.getOrgName();
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
                
                Queue<Long> newDataIds;
                
                if ((newDataIds = ContactUtil.updateContact(mContext, contact)) != null) {
                    
                    ContactRecord.Builder contactRecordBuilder = ContactRecord.newBuilder(contactRecord);
                    
                    // groups
                    for (int i = 0; i < contactRecord.getGroupCount(); i++) {
                        GroupRecord groupRecord = contactRecord.getGroup(i);
                        if (groupRecord.getModifyTag() == ModifyTag.ADD) {
                            GroupRecord.Builder groupRecordBuilder = contactRecordBuilder.getGroupBuilder(i);
                            long newDataId = newDataIds.remove();
                            groupRecordBuilder.setDataId(newDataId);
                        }
                    }
                    
                    // phones
                    for (int i = 0; i < contactRecord.getPhoneCount(); i++) {
                        PhoneRecord phoneRecord = contactRecord.getPhone(i);
                        if (phoneRecord.getModifyTag() == ModifyTag.ADD) {
                            PhoneRecord.Builder phoneRecordBuilder = contactRecordBuilder.getPhoneBuilder(i);
                            long newDataId = newDataIds.remove();
                            phoneRecordBuilder.setId(newDataId);
                        }
                    }
                    
                    // emails
                    for (int i = 0; i < contactRecord.getEmailCount(); i++) {
                        EmailRecord emailRecord = contactRecord.getEmail(i);
                        if (emailRecord.getModifyTag() == ModifyTag.ADD) {
                            EmailRecord.Builder emailRecordBuilder = contactRecordBuilder.getEmailBuilder(i);
                            long newDataId = newDataIds.remove();
                            emailRecordBuilder.setId(newDataId);
                        }
                    }
                    
                    // Organizations
                    for (int i = 0; i < contactRecord.getOrgCount(); i++) {
                        OrgRecord orgRecord = contactRecord.getOrg(i);
                        if (orgRecord.getModifyTag() == ModifyTag.ADD) {
                            OrgRecord.Builder orgRecordBuilder = contactRecordBuilder.getOrgBuilder(i);
                            long newDataId = newDataIds.remove();
                            orgRecordBuilder.setId(newDataId);
                        }
                    }
                    
                    // Address
                    for (int i = 0; i < contactRecord.getAddressCount(); i++) {
                        AddressRecord addressRecord = contactRecord.getAddress(i);
                        if (addressRecord.getModifyTag() == ModifyTag.ADD) {
                            AddressRecord.Builder addressRecordBuilder = contactRecordBuilder.getAddressBuilder(i);
                            long newDataId = newDataIds.remove();
                            addressRecordBuilder.setId(newDataId);
                        }
                    }
                    
                    // Im
                    for (int i = 0; i < contactRecord.getImCount(); i++) {
                        IMRecord imRecord = contactRecord.getIm(i);
                        if (imRecord.getModifyTag() == ModifyTag.ADD) {
                            IMRecord.Builder imRecordBuilder = contactRecordBuilder.getImBuilder(i);
                            long newDataId = newDataIds.remove();
                            imRecordBuilder.setId(newDataId);
                        }
                    }
                    
                    
                    responseBuilder.addContactRecord(contactRecordBuilder.build());
                    
                    
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

        if (contactRecord.hasAccountInfo()) {
            AccountRecord accountRecord = contactRecord.getAccountInfo();
            contact.setAccountInfo(accountRecord.getName(), accountRecord.getType());
        }

        // group
        for (GroupRecord groupRecord : contactRecord.getGroupList()) {
            GroupInfo groupInfo = new GroupInfo();

            // only id in 'data' table is needed
            if (groupRecord.hasId()) {
                groupInfo.grId = groupRecord.getId();
                groupInfo.dataId = groupRecord.getDataId();
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
            emailInfo.address = emailRecord.getEmail();
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
            imInfo.protocol = toCommonDataKindsImType(imRecord.getType());
            imInfo.account = imRecord.getAccount();
            imInfo.customProtocol = imRecord.getName();

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
            addressInfo.region = addressRecord.getProvince();
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
            orgInfo.company = orgRecord.getOrgName();
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

    /*
     * only sync with local account
     */
    private void handleSyncContactWithOutlook(ContactsSync contactsSync, Builder responseBuilder) {
        Slog.d("handleSyncContactWithOutlook E");
        
        switch (contactsSync.getSubType()) {
            case TWO_WAY_SLOW_SYNC: {
                handleSyncContactTwoWay(contactsSync, responseBuilder, /* fastSync */ false);
                break;
            }
            case TWO_WAY_FAST_SYNC: {
                handleSyncContactTwoWay(contactsSync, responseBuilder, /* fastSync */ true);
                break;
            }
            case TWO_WAY_SLOW_SYNC_SECOND: {
                handleSyncContactTwoWaySecond(contactsSync, responseBuilder);
                break;
            }
            case TWO_WAY_FAST_SYNC_SECOND: {
                handleSyncContactTwoWaySecond(contactsSync, responseBuilder);
                break;
            }
            case PHONE_REFRESH_SYNC: {
                handleSyncContactPhoneRefresh(contactsSync, responseBuilder);
                break;
            }
            case PC_REFRESH_SYNC: {
                handleSyncContactPcRefresh(contactsSync, responseBuilder);
                break;
            }

            default:
                Slog.e("Error");
                break;
        }
        
        Slog.d("handleSyncContactWithOutlook X");
    }

    /**
     * Overwrite phone's all contacts with outlook's contacts
     * 
     * @param contactsSync
     * @param responseBuilder
     */
    private void handleSyncContactPcRefresh(ContactsSync contactsSync, Builder responseBuilder) {
        Slog.d("handleSyncContactPcRefresh E");
        
        ContactsSync.Builder contactSyncBuilder = ContactsSync.newBuilder();
        
        contactSyncBuilder.setType(contactsSync.getType());
        contactSyncBuilder.setSubType(contactsSync.getSubType());
        
        if (contactsSync.hasSyncConflictPloy()) {
            contactSyncBuilder.setSyncConflictPloy(contactsSync.getSyncConflictPloy());
        }
        
        //
        // 1. delete phone contacts
        //
        final int count = ContactUtil.deletePhoneContacts(mContext);
        Slog.d("delete count = " + count);
        
        //
        // 2. add contact from outlook
        //
        ContactRecord.Builder contactRecordBuilder = ContactRecord.newBuilder();
        boolean success = true;
        
        
        final List<ContactRecord> contactRecordList = contactsSync.getContactRecordList();
        
        Slog.d("contactRecordList size = " + contactRecordList.size());
        
        String defaultAccountName = ContactUtil.getDefaultAccountName();
        String defaultAccountType = ContactUtil.getDefaultAccountType();
        
        for (ContactRecord contactRecord : contactRecordList) {
            String pcId = contactRecord.getPcId();
            SyncResult syncResult = contactRecord.getSyncResult();
            
            switch (syncResult) {
                case PC_ADD: {
                    Contact contact = contactRecordToContactForSyncAdd(contactRecord, defaultAccountName, defaultAccountType);
                    
                    final long contactId = ContactUtil.addContact(mContext, contact);
                    if (contactId > 0) {
                        int contactVersion = ContactUtil.getContactVersion(mContext, contactId);
                        
                        Slog.d("ContactUtil.addContact OK, contactId = " + contactId + ", contactVersion = " + contactVersion);
                        
                        contactRecordBuilder.setId(contactId);
                        contactRecordBuilder.setVersion(contactVersion);
                        contactRecordBuilder.setPcId(pcId);
                        contactRecordBuilder.setSyncResult(syncResult);
                        
                        contactSyncBuilder.addContactRecord(contactRecordBuilder.build());
                        
                        contactRecordBuilder.clear();
                    } else {
                        Slog.e("Error ContactUtil.addContact, contactId = " + contactId);
                        success = false;
                    }
                    break;
                }

                default: {
                    Slog.e("Error invalid syncResult: " + syncResult);
                    break;
                }
            }
        }
        
        if (success) {
            responseBuilder.setContactsSync(contactSyncBuilder);
            
            setResultOK(responseBuilder);
        } else {
            setResultErrorInternal(responseBuilder, "ContactUtil.addContact");
        }
        
        FastSyncUtils.notifyUpdateContactVersionDB(mContext);
        
        Slog.d("handleSyncContactPcRefresh X");
    }

    /**
     * Overwrite outlook's contacts with phone's contacts
     * 
     * @param contactsSync
     * @param responseBuilder
     */
    private void handleSyncContactPhoneRefresh(ContactsSync contactsSync, Builder responseBuilder) {
        Slog.d("handleSyncContactPhoneRefresh E");
        
        // only sync phone contacts
        Collection<Contact> contactList = ContactUtil.getPhoneContacts(mContext);

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
        
        Slog.d("Contacts number: " + contactList.size());
        
        for (Contact contact : contactList) {
            contactToContactRecord(contactRecordBuilder, accountRecordBuilder, groupRecordBuilder, phoneRecordBuilder,
                    emailRecordBuilder, imRecordBuilder, addressRecordBuilder, orgRecordBuilder, contact);

            // 
            // We use phone's contacts to overwrite outlook's contacts, so mark as PHONE_ADD
            //
            contactRecordBuilder.setSyncResult(SyncResult.PHONE_ADD);
            
            contactSyncBuilder.addContactRecord(contactRecordBuilder.build());

            accountRecordBuilder.clear();
            contactRecordBuilder.clear();
        }
        
        responseBuilder.setContactsSync(contactSyncBuilder);
        
        setResultOK(responseBuilder);
        
        FastSyncUtils.notifyUpdateContactVersionDB(mContext);
        
        Slog.d("handleSyncContactPhoneRefresh X");
    }

    private void handleSyncContactTwoWay(ContactsSync contactsSync, Builder responseBuilder, boolean fastSync) {
        Slog.d("handleSyncContactTwoWay E, fastSync = " + fastSync);
        
        Collection<Contact> contactList = null;
        if (fastSync) {
            contactList = FastSyncUtils.findChangedPhoneContacts(mContext);
        } else {
            contactList = ContactUtil.getPhoneContacts(mContext);
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
        
        Slog.d("handleSyncContactTwoWay X, fastSync = " + fastSync);
    }
    
    
    
    private void handleSyncContactTwoWaySecond(ContactsSync contactsSync, Builder responseBuilder) {
        Slog.d("handleSyncContactTwoWaySecond E");
        
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
        
        final List<ContactRecord> contactRecordList = contactsSync.getContactRecordList();
        
        Slog.d("contactRecordList size = " + contactRecordList.size());
        
        String defaultAccountName = ContactUtil.getDefaultAccountName();
        String defaultAccountType = ContactUtil.getDefaultAccountType();
        
        for (ContactRecord contactRecord : contactRecordList) {
            final SyncResult syncResult = contactRecord.getSyncResult();
            final String pcId = contactRecord.getPcId();
            Slog.d("SyncResult = " + syncResult + ", pcId = " + pcId);
            
            switch (syncResult) {
                case PC_ADD: {
                    Contact contact = contactRecordToContactForSyncAdd(contactRecord, defaultAccountName, defaultAccountType);
                    
                    final long contactId = ContactUtil.addContact(mContext, contact);
                    if (contactId > 0) {
                        int contactVersion = ContactUtil.getContactVersion(mContext, contactId);
                        
                        Slog.d("ContactUtil.addContact OK, contactId = " + contactId + ", contactVersion = " + contactVersion);
                        
                        contactRecordBuilder.setId(contactId);
                        contactRecordBuilder.setVersion(contactVersion);
                        contactRecordBuilder.setPcId(pcId);
                        contactRecordBuilder.setSyncResult(syncResult);
                        
                        contactSyncBuilder.addContactRecord(contactRecordBuilder.build());
                        
                        contactRecordBuilder.clear();
                    } else {
                        Slog.e("Error ContactUtil.addContact, contactId = " + contactId);
                        success = false;
                    }
                    break;
                }
                
                case PC_MODIFY: {
                    Contact contact = contactRecordToContactForUpdate(contactRecord);
                    
                    final long contactId = contact.id;
                    if (ContactUtil.updateContactForce(mContext, contact)) {
                        int contactVersion = ContactUtil.getContactVersion(mContext, contactId);
                        
                        Slog.d("ContactUtil.updateContactForce OK, contactId = " + contactId + ", contactVersion = " + contactVersion);
                        
                        contactRecordBuilder.setId(contactId);
                        contactRecordBuilder.setVersion(contactVersion);
                        contactRecordBuilder.setPcId(pcId);
                        contactRecordBuilder.setSyncResult(syncResult);
                        
                        contactSyncBuilder.addContactRecord(contactRecordBuilder.build());
                        
                        contactRecordBuilder.clear();
                    } else {
                        Slog.e("Error ContactUtil.updateContact, contactId = " + contactId);
                        success = false;
                    }
                    
                    break;
                }
                case BOTH_MODIFY: {
                    Contact contact = contactRecordToContactForUpdate(contactRecord);
                    
                    final long contactId = contact.id;
                    if (ContactUtil.updateContact(mContext, contact) != null) {
                        int contactVersion = ContactUtil.getContactVersion(mContext, contactId);
                        
                        Slog.d("ContactUtil.updateContactForce OK, contactId = " + contactId + ", contactVersion = " + contactVersion);
                        
                        contactRecordBuilder.setId(contactId);
                        contactRecordBuilder.setVersion(contactVersion);
                        contactRecordBuilder.setPcId(pcId);
                        contactRecordBuilder.setSyncResult(syncResult);
                        
                        contactSyncBuilder.addContactRecord(contactRecordBuilder.build());
                        
                        contactRecordBuilder.clear();
                    } else {
                        Slog.e("Error ContactUtil.updateContact, contactId = " + contactId);
                        success = false;
                    }
                    
                    break;
                }
                
                case PC_DEL: {
                    final long contactId = contactRecord.getId();
                    
                    if (ContactUtil.deleteContactById(mContext, contactId)) {
                        Slog.d("ContactUtil.deleteContactById OK, contactId = " + contactId);
                        
                        contactRecordBuilder.setId(contactId);
                        contactRecordBuilder.setSyncResult(syncResult);
                        contactRecordBuilder.setPcId(pcId);
                        
                        contactSyncBuilder.addContactRecord(contactRecordBuilder.build());
                        
                        contactRecordBuilder.clear();
                        
                    } else {
                        Slog.e("Error ContactUtil.deleteContactById, contactId = " + contactId);
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
        
        Slog.d("handleSyncContactTwoWaySecond X");
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
            
            // FIXME: do not send icon temporarily
            //
            //appBuilder.setAppIcon(ByteString.copyFrom(appInfo.icon));
            
            appBuilder.setApkPath(appInfo.apkFilePath);

            responseBuilder.addAppRecord(appBuilder.build());

            appBuilder.clear();
        }

        setResultOK(responseBuilder);

        Slog.d("queryApp X");
        return responseBuilder.build();
    }
    
    private CmdResponse uninstallApp(CmdRequest cmdRequest) {
        Slog.d("uninstallApp E");
        
        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_UNINSTALL_APP);
        
        if (cmdRequest.hasAppParams()) {
            AppRecord appRecord = cmdRequest.getAppParams();
            String packageName = appRecord.getPackageName();
            AppUtil.uninstallAPK(mContext, packageName);
            setResultOK(responseBuilder);
        } else {
            setResultErrorInsufficentParams(responseBuilder, "AppParams");
        }
        
        Slog.d("uninstallApp X");
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
    
    private static void setResultErrorAuthFail(
            CmdResponse.Builder responseBuilder, String... params) {
        StringBuilder msgBuilder = new StringBuilder(
                "Error authentication params: [");
        for (String param : params) {
            msgBuilder.append(param);
            msgBuilder.append(", ");
        }
        msgBuilder.append(']');
        final String msg = msgBuilder.toString();

        Slog.e(msg);

        responseBuilder.setResultCode(RESULT_CODE_ERR_AUTH_FAIL);
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
