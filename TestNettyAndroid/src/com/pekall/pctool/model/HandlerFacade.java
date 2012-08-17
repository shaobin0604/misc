
package com.pekall.pctool.model;

import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;

import android.content.Context;

import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.Person;
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
import com.pekall.pctool.model.contact.Contact.PhoneInfo;
import com.pekall.pctool.model.contact.ContactUtil;
import com.pekall.pctool.model.contact.GroupInfo;
import com.pekall.pctool.model.sms.Sms;
import com.pekall.pctool.model.sms.SmsUtil;
import com.pekall.pctool.protos.AppInfoProtos.AppInfoPList;
import com.pekall.pctool.protos.MsgDefProtos.AccountRecord;
import com.pekall.pctool.protos.MsgDefProtos.AgendaRecord;
import com.pekall.pctool.protos.MsgDefProtos.AppRecord;
import com.pekall.pctool.protos.MsgDefProtos.AppRecord.AppLocationType;
import com.pekall.pctool.protos.MsgDefProtos.AppRecord.AppType;
import com.pekall.pctool.protos.MsgDefProtos.CalendarRecord;
import com.pekall.pctool.protos.MsgDefProtos.CmdRequest;
import com.pekall.pctool.protos.MsgDefProtos.CmdResponse;
import com.pekall.pctool.protos.MsgDefProtos.CmdType;
import com.pekall.pctool.protos.MsgDefProtos.ContactRecord;
import com.pekall.pctool.protos.MsgDefProtos.GroupRecord;
import com.pekall.pctool.protos.MsgDefProtos.ModifyTag;
import com.pekall.pctool.protos.MsgDefProtos.MsgOriginType;
import com.pekall.pctool.protos.MsgDefProtos.PhoneRecord;
import com.pekall.pctool.protos.MsgDefProtos.PhoneRecord.PhoneType;
import com.pekall.pctool.protos.MsgDefProtos.SMSRecord;

import java.io.InputStream;
import java.util.List;

public class HandlerFacade {
    private static final int RESULT_CODE_OK = 0;
    private static final int RESULT_CODE_ERR_ILLEGAL_ARGUMENT = 100;
    private static final int RESULT_CODE_ERR_INSUFFICIENT_PARAMS = 101;
    private static final int RESULT_CODE_ERR_INTERNAL = 200;

    private static final String RESULT_MSG_OK = "OK";
    

    private Context mContext;

    public HandlerFacade(Context context) {
        this.mContext = context;
    }

    public CmdResponse defaultCmdResponse() {
        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();

        responseBuilder.setCmdType(CmdType.CMD_HEART_BEAT);
        
        setResultErrorInsufficentParams(responseBuilder, "cmd type");

        return responseBuilder.build();
    }

    // -------------------------------------------------------------------------
    // Sms related method
    // -------------------------------------------------------------------------

    public CmdResponse querySms(CmdRequest request) {
        Slog.d("querySms E");

        List<Sms> smsList = SmsUtil.querySmsList(mContext);

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_QUERY_SMS);

        SMSRecord.Builder smsBuilder = SMSRecord.newBuilder();

        for (Sms sms : smsList) {
            smsBuilder.setMsgId(sms.rowId);
            smsBuilder.setMsgOrigin(smsTypeToMsgOriginType(sms.type));
            smsBuilder.setPhoneNum(sms.address);
            smsBuilder.setMsgText(sms.body);
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
                throw new IllegalArgumentException("Unkown MsgOriginType: " + msgOriginType);
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
    // Calendar related method
    // ------------------------------------------------------------------------
    public CmdResponse queryCalendar(CmdRequest request) {
        Slog.d("queryCalendar E");

        List<CalendarInfo> calendarInfoList = CalendarUtil.getAllCalendarInfos(mContext);

        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_QUERY_CALENDAR);

        CalendarRecord.Builder calendarRecordBuilder = CalendarRecord.newBuilder();
        AccountRecord.Builder accountRecordBuilder = AccountRecord.newBuilder();

        for (CalendarInfo info : calendarInfoList) {
            calendarRecordBuilder.setId(info.caId);
            calendarRecordBuilder.setName(info.name);

            accountRecordBuilder.setName(info.accountInfo.accountName);
            accountRecordBuilder.setType(info.accountInfo.accountType);

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
        }
        
        Slog.d("calendarId = " + calendarId);
        
        List<EventInfo> eventInfoList = CalendarUtil.getEvents(mContext, calendarId);
        
        AgendaRecord.Builder agendaRecordBuilder = AgendaRecord.newBuilder();
        
        for (EventInfo eventInfo : eventInfoList) {
            agendaRecordBuilder.setId(eventInfo.evId);
            agendaRecordBuilder.setCalendarId(eventInfo.calendarId);
            
            if (!TextUtils.isEmpty(eventInfo.title)) {
                agendaRecordBuilder.setSubject(eventInfo.title);
            }
            if (!TextUtils.isEmpty(eventInfo.place)) {
                agendaRecordBuilder.setLocation(eventInfo.place);
            }
            agendaRecordBuilder.setStartTime(eventInfo.startTime);
            agendaRecordBuilder.setEndTime(eventInfo.endTime);
            if (!TextUtils.isEmpty(eventInfo.rrule)) {
                agendaRecordBuilder.setRepeatRule(eventInfo.rrule);
            }
            agendaRecordBuilder.setAlertTime(eventInfo.alertTime);
            if (!TextUtils.isEmpty(eventInfo.note)) {
                agendaRecordBuilder.setNote(eventInfo.note);
            }
            
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
            
            eventInfo.evId = agendaRecord.getId();
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
                setResultErrorInternal(responseBuilder, "CalendarUtil.updateEvent");
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
                setResultErrorInternal(responseBuilder, "CalendarUtil.deleteEvent");
            }
            
        } else {
            setResultErrorInsufficentParams(responseBuilder, "record id list");
        }
        Slog.d("deleteAgenda X");
        return responseBuilder.build();
    }
    
    // ------------------------------------------------------------------------
    // Account related method
    // ------------------------------------------------------------------------

    public CmdResponse queryAccount(CmdRequest request) {
        Slog.d("queryAccount E");
        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_GET_ALL_ACCOUNTS);
        
        List<AccountInfo> accountInfoList = ContactUtil.getAllAccounts(mContext);
        
        AccountRecord.Builder accountRecordBuilder = AccountRecord.newBuilder();
        for (AccountInfo accountInfo : accountInfoList) {
            accountRecordBuilder.setName(accountInfo.accountName);
            accountRecordBuilder.setType(accountInfo.accountType);
            
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
        
        List<AccountInfo> accountInfoList = ContactUtil.getAllAccounts(mContext);
        
        AccountRecord.Builder accountRecordBuilder = AccountRecord.newBuilder();
        GroupRecord.Builder groupRecordBuilder = GroupRecord.newBuilder();
        
        
        for (AccountInfo accountInfo : accountInfoList) {
            List<GroupInfo> groupInfoList = ContactUtil.queryGroup(mContext, accountInfo);
            
            accountRecordBuilder.setName(accountInfo.accountName);
            accountRecordBuilder.setType(accountInfo.accountType);
            
            for (GroupInfo groupInfo : groupInfoList) {
                groupRecordBuilder.setId(groupInfo.grId);
                groupRecordBuilder.setDataId(groupInfo.dataId);
                groupRecordBuilder.setAccountInfo(accountRecordBuilder.build());
                groupRecordBuilder.setName(groupInfo.name);
                groupRecordBuilder.setNote(groupInfo.note);
                groupRecordBuilder.setModifyTag(ModifyTag.SAME);
                
                responseBuilder.addGroupRecord(groupRecordBuilder.build());
                
                groupRecordBuilder.clear();
            }
            
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
            groupInfo.accountInfo.accountName = groupRecord.getAccountInfo().getName();
            groupInfo.accountInfo.accountType = groupRecord.getAccountInfo().getType();
            
            if (ContactUtil.addGroup(mContext, groupInfo)) {
                setResultOK(responseBuilder);
            } else {
                setResultErrorInternal(responseBuilder, "ContactUtil.addGroup");
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
                setResultErrorInternal(responseBuilder, "ContactUtil.updateGroup");
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
                setResultErrorInternal(responseBuilder, "ContactUtil.deleteGroup");
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
        
        // default query all Contact
        List<Contact> contactList = ContactUtil.getAllContacts(mContext);
        
        ContactRecord.Builder contactRecordBuilder = ContactRecord.newBuilder();
        AccountRecord.Builder accountRecordBuilder = AccountRecord.newBuilder();
        GroupRecord.Builder groupRecordBuilder = GroupRecord.newBuilder();
        PhoneRecord.Builder phoneRecordBuilder = PhoneRecord.newBuilder();
        
        for (Contact contact : contactList) {
            contactRecordBuilder.setId(contact.id);
            contactRecordBuilder.setName(contact.name);
            contactRecordBuilder.setNickname(contact.nickname);
            contactRecordBuilder.setPhoto(ByteString.copyFrom(contact.photo));
            contactRecordBuilder.setPhotoModifyTag(false);
            
            accountRecordBuilder.setName(contact.accountInfo.accountName);
            accountRecordBuilder.setType(contact.accountInfo.accountType);
            
            contactRecordBuilder.setAccountInfo(accountRecordBuilder.build());
            
            // groups
            for (GroupInfo groupInfo : contact.groupInfos) {
                groupRecordBuilder.setId(groupInfo.grId); // only id is required
                
                contactRecordBuilder.addGroup(groupRecordBuilder.build());
                
                groupRecordBuilder.clear();
            }
            
            // phones
            for (PhoneInfo phoneInfo : contact.phoneInfos) {
                phoneRecordBuilder.setId(phoneInfo.id);
                phoneRecordBuilder.setType(toPhoneType(phoneInfo.type));
                phoneRecordBuilder.setNumber(phoneInfo.number);
                phoneRecordBuilder.setName(phoneInfo.customName);
            }
            
            responseBuilder.addContactRecord(contactRecordBuilder.build());
            
            accountRecordBuilder.clear();
            contactRecordBuilder.clear();
        }
        
        Slog.d("queryContact X");
        return responseBuilder.build();
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
                return PhoneType.WORK_FAX;
            case Phone.TYPE_FAX_WORK:
                return PhoneType.HOME_FAX;
            case Phone.TYPE_PAGER:
                return PhoneType.PAGER;
            case Phone.TYPE_MAIN:
                return PhoneType.MAIN;
            case Phone.TYPE_CUSTOM:
                return PhoneType.USER_DEFINED;
            default:
                return PhoneType.OTHER;
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
            appBuilder.setPackageName(appInfo.packageName);
            appBuilder.setVersionCode(appInfo.versionCode);
            appBuilder.setVersionName(appInfo.versionName);
            appBuilder.setSize(appInfo.apkFileSize);
            appBuilder.setAppIcon(ByteString.copyFrom(appInfo.icon));

            responseBuilder.addAppRecord(appBuilder.build());

            appBuilder.clear();
        }
        
        setResultOK(responseBuilder);

        Slog.d("queryApp X");
        return responseBuilder.build();
    }

    public InputStream exportApp(String packageName) throws AppNotExistException {
        return AppUtil.getAppApkStream(mContext, packageName);
    }
    
    // -------------------------------------------------------------------------
    // Utility code
    // -------------------------------------------------------------------------
    private static void setResultOK(CmdResponse.Builder responseBuilder) {
        responseBuilder.setResultCode(RESULT_CODE_OK);
        responseBuilder.setResultMsg(RESULT_MSG_OK);
    }
    
    private static void setResultErrorInternal(CmdResponse.Builder responseBuilder, String methodCalled) {
        StringBuilder msgBuilder = new StringBuilder("Error internal method call: ");
        msgBuilder.append(methodCalled);
        final String msg = msgBuilder.toString();

        Slog.e(msg);
        
        responseBuilder.setResultCode(RESULT_CODE_ERR_INTERNAL);
        responseBuilder.setResultMsg(msg);
    }
    
    private static void setResultErrorInsufficentParams(CmdResponse.Builder responseBuilder, String... params) {
        StringBuilder msgBuilder = new StringBuilder("Error insufficient params: [");
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

    // -------------------------------------------------------------------------
    // TEST code
    // -------------------------------------------------------------------------

    public AddressBook getAddressBook() {
        AddressBook.Builder builder = AddressBook.newBuilder();
        builder.addPerson(getPerson("李雷", 1));
        builder.addPerson(getPerson("韩梅梅", 2));
        return builder.build();
    }

    private Person getPerson(String name, int id) {
        return Person.newBuilder().setName(name).setId(id).build();
    }

    public AppInfoPList getAppInfoPList() {
        return AppUtil.getUserAppInfoPList(mContext);
    }

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
