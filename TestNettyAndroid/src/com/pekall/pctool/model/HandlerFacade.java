package com.pekall.pctool.model;

import android.content.Context;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.text.TextUtils;

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
import com.pekall.pctool.protos.AppInfoProtos.AppInfoPList;
import com.pekall.pctool.protos.MsgDefProtos.AccountRecord;
import com.pekall.pctool.protos.MsgDefProtos.AddressRecord;
import com.pekall.pctool.protos.MsgDefProtos.AddressRecord.AddressType;
import com.pekall.pctool.protos.MsgDefProtos.AgendaRecord;
import com.pekall.pctool.protos.MsgDefProtos.AppRecord;
import com.pekall.pctool.protos.MsgDefProtos.AppRecord.AppLocationType;
import com.pekall.pctool.protos.MsgDefProtos.AppRecord.AppType;

import com.pekall.pctool.protos.MsgDefProtos.CalendarRecord;
import com.pekall.pctool.protos.MsgDefProtos.CmdRequest;
import com.pekall.pctool.protos.MsgDefProtos.CmdResponse;
import com.pekall.pctool.protos.MsgDefProtos.CmdType;
import com.pekall.pctool.protos.MsgDefProtos.ContactRecord;
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

import java.io.InputStream;
import java.util.ArrayList;
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
		com.pekall.pctool.protos.MsgDefProtos.Slide.Builder slideBuilder = com.pekall.pctool.protos.MsgDefProtos.Slide.newBuilder();
		com.pekall.pctool.protos.MsgDefProtos.Attachment.Builder attachmentBuilder = com.pekall.pctool.protos.MsgDefProtos.Attachment.newBuilder();
		
		List<Mms> mmsList = MmsUtil.query(mContext);
		
		for (Mms mms : mmsList) {
		    mmsRecordBuilder.setMsgId(mms.rowId);
		    mmsRecordBuilder.setMsgOrigin(mmsTypeToMsgOriginType(mms.msgBoxIndex));
		    mmsRecordBuilder.setPhoneNum(mms.phoneNum);
		    mmsRecordBuilder.setSubject(mms.subject);
		    mmsRecordBuilder.setMsgTime(mms.date);
		    mmsRecordBuilder.setReadTag(mms.isReaded == Mms.READ_TRUE);
		    
		    for (Slide slide : mms.slides) {
		    	slideBuilder.setDuration(slide.duration);
		    	slideBuilder.setText(slide.text);
		    	slideBuilder.setImageIndex(slide.imageIndex);
		    	slideBuilder.setAudioIndex(slide.audioIndex);
		    	slideBuilder.setVideoIndex(slide.videoIndex);
		    	
		    	mmsRecordBuilder.addSlide(slideBuilder.build());
		    	
		    	slideBuilder.clear();
		    }

		    for (Attachment attachment : mms.attachments) {
		    	attachmentBuilder.setName(attachment.name);
		    	attachmentBuilder.setSize(attachment.fileBytes.length);
		    	attachmentBuilder.setFileBytes(ByteString.copyFrom(attachment.fileBytes));
		    	
		    	mmsRecordBuilder.addAttachment(attachmentBuilder.build());
		    	
		    	attachmentBuilder.clear();
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
				.getAllCalendarInfos(mContext);

		CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
		responseBuilder.setCmdType(CmdType.CMD_QUERY_CALENDAR);

		CalendarRecord.Builder calendarRecordBuilder = CalendarRecord
				.newBuilder();
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

		List<EventInfo> eventInfoList = CalendarUtil.getEvents(mContext,
				calendarId);

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

		List<AccountInfo> accountInfoList = ContactUtil
				.getAllAccounts(mContext);

		AccountRecord.Builder accountRecordBuilder = AccountRecord.newBuilder();
		GroupRecord.Builder groupRecordBuilder = GroupRecord.newBuilder();

		for (AccountInfo accountInfo : accountInfoList) {
			List<GroupInfo> groupInfoList = ContactUtil.queryGroup(mContext,
					accountInfo);

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
			groupInfo.accountInfo.accountName = groupRecord.getAccountInfo()
					.getName();
			groupInfo.accountInfo.accountType = groupRecord.getAccountInfo()
					.getType();

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
				setResultErrorInternal(responseBuilder,
						"ContactUtil.updateGroup");
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
						"ContactUtil.deleteGroup");
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
		EmailRecord.Builder emailRecordBuilder = EmailRecord.newBuilder();
		IMRecord.Builder imRecordBuilder = IMRecord.newBuilder();
		AddressRecord.Builder addressRecordBuilder = AddressRecord.newBuilder();
		OrgRecord.Builder orgRecordBuilder = OrgRecord.newBuilder();

		for (Contact contact : contactList) {
			contactRecordBuilder.setId(contact.id);
			contactRecordBuilder.setName(contact.name);
			contactRecordBuilder.setNickname(contact.nickname);
			contactRecordBuilder.setPhoto(ByteString.copyFrom(contact.photo));
			contactRecordBuilder.setPhotoModifyTag(false);

			accountRecordBuilder.setName(contact.accountInfo.accountName);
			accountRecordBuilder.setType(contact.accountInfo.accountType);

			contactRecordBuilder.setAccountInfo(accountRecordBuilder.build());

			// group
			for (GroupInfo groupInfo : contact.groupInfos) {
				groupRecordBuilder.setId(groupInfo.grId); // only id is required

				contactRecordBuilder.addGroup(groupRecordBuilder.build());

				groupRecordBuilder.clear();
			}

			// phone
			for (PhoneInfo phoneInfo : contact.phoneInfos) {
				phoneRecordBuilder.setId(phoneInfo.id);
				phoneRecordBuilder.setType(toPhoneType(phoneInfo.type));
				phoneRecordBuilder.setNumber(phoneInfo.number);
				
				// the name is optional
				if (phoneInfo.type == Phone.TYPE_CUSTOM) {
				    phoneRecordBuilder.setName(phoneInfo.customName);
				}
				
				phoneRecordBuilder.setModifyTag(ModifyTag.SAME);
				
				contactRecordBuilder.addPhone(phoneRecordBuilder.build());
				
				phoneRecordBuilder.clear();
			}
			
			// email
			for (EmailInfo emailInfo : contact.emailInfos) {
			    emailRecordBuilder.setId(emailInfo.id);
			    emailRecordBuilder.setType(toEmailType(emailInfo.type));
			    emailRecordBuilder.setEmail(emailInfo.email);
			    
			    // the name is optional
			    if (emailInfo.type == Email.TYPE_CUSTOM) {
			        emailRecordBuilder.setName(emailInfo.customName);
			    }
			    
			    emailRecordBuilder.setModifyTag(ModifyTag.SAME);
			    
			    contactRecordBuilder.addEmail(emailRecordBuilder.build());
			    
			    emailRecordBuilder.clear();
			}
			
			// im
			for (ImInfo imInfo : contact.imInfos) {
			    imRecordBuilder.setId(imInfo.id);
			    imRecordBuilder.setType(toImType(imInfo.type));
			    imRecordBuilder.setAccount(imInfo.account);
			    
			    if (imInfo.type == Im.PROTOCOL_CUSTOM) {
			        imRecordBuilder.setName(imInfo.customName);
			    }
			    
			    imRecordBuilder.setModifyTag(ModifyTag.SAME);
			    
			    contactRecordBuilder.addIm(imRecordBuilder.build());
			    
			    imRecordBuilder.clear();
			}
			
			// address
			for (AddressInfo addressInfo : contact.addressInfos) {
			    addressRecordBuilder.setId(addressInfo.id);
			    addressRecordBuilder.setAddressType(toAddressType(addressInfo.type));
			    if (addressInfo.type == StructuredPostal.TYPE_CUSTOM) {
			        addressRecordBuilder.setName(addressInfo.customName);
			    }
			    addressRecordBuilder.setAddress(addressInfo.address);
			    addressRecordBuilder.setCountry(addressInfo.country);
			    addressRecordBuilder.setProvince(addressInfo.province);
			    addressRecordBuilder.setCity(addressInfo.city);
			    addressRecordBuilder.setRoad(addressInfo.street);
			    addressRecordBuilder.setPostCode(addressInfo.postcode);
			    addressRecordBuilder.setModifyTag(ModifyTag.SAME);
			    
			    contactRecordBuilder.addAddress(addressRecordBuilder.build());
			    
			    addressRecordBuilder.clear();
			}
			
			// organization
			for (OrgInfo orgInfo : contact.orgInfos) {
			    orgRecordBuilder.setId(orgInfo.id);
			    orgRecordBuilder.setType(toOrgType(orgInfo.type));
			    orgRecordBuilder.setName(orgInfo.org);
			    if (orgInfo.type == Organization.TYPE_CUSTOM) {
			        orgRecordBuilder.setName(orgInfo.customName);
			    }
			    
			    orgRecordBuilder.setModifyTag(ModifyTag.SAME);
			    
			    contactRecordBuilder.addOrg(orgRecordBuilder.build());
			    
			    orgRecordBuilder.clear();
			}

			responseBuilder.addContactRecord(contactRecordBuilder.build());

			accountRecordBuilder.clear();
			contactRecordBuilder.clear();
		}
		setResultOK(responseBuilder);
		Slog.d("queryContact X");
		return responseBuilder.build();
	}
    
	
	public CmdResponse addContact(CmdRequest request) {
	    Slog.d("addContact E");
	    
	    CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_ADD_CONTACT);

        if (request.hasContactParams()) {
            ContactRecord contactRecord = request.getContactParams();
            
            if (contactRecord != null) {
                Contact contact = new Contact();
                
                contact.name = contactRecord.getName();
                contact.nickname = contactRecord.getNickname();
                contact.photo = contactRecord.getPhoto().toByteArray();
                
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
                
                if (ContactUtil.addContact(mContext, contact)) {
                    setResultOK(responseBuilder);
                } else {
                    setResultErrorInternal(responseBuilder, "ContactUtil.addContact");
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
	
	
	
	public CmdResponse updateContact(CmdRequest request) {
	    Slog.d("updateContact E");
	    
	    CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_EDIT_CONTACT);
        
        if (request.hasContactParams()) {
            ContactRecord contactRecord = request.getContactParams();
            
            if (contactRecord != null) {
                Contact contact = new Contact();
                
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

                // TODO
                
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
            }
        } else {
            setResultErrorInsufficentParams(responseBuilder, "contact");
        }
	    
	    Slog.d("updateContact X");
	    return responseBuilder.build();
	}
	
	public CmdResponse deleteContact(CmdRequest request) {
	    Slog.d("deleteContact E");
	    
	    CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_DELETE_CONTACT);
        
        List<Long> contactIdList = request.getRecordIdList();
        
        if (contactIdList != null && contactIdList.size() > 0) {
            if (ContactUtil.deleteContact(mContext, contactIdList)) {
                setResultOK(responseBuilder);
            } else {
                setResultErrorInternal(responseBuilder, "ContactUtil.deleteContact");
            }
        } else {
            setResultErrorInsufficentParams(responseBuilder, "recordIdList");
        }
        
        Slog.d("deleteContact X");
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
			appBuilder
					.setAppType(appInfo.appType == AppInfo.FLAG_APP_TYPE_SYSTEM ? AppType.SYSTEM
							: AppType.USER);
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
		app.setAppIcon(ByteString.copyFrom(new byte[] { 0, 1, 2, 3, 4, 5 }));

		response.addAppRecord(app.build());

		app.clear();

		app.setAppName("腾讯微博");
		app.setAppType(AppType.USER);
		app.setLocationType(AppLocationType.INTERNAL);
		app.setPackageName("com.tencent.weibo");
		app.setVersionName("v2.3");
		app.setVersionCode(654321);
		app.setSize(1024);
		app.setAppIcon(ByteString.copyFrom(new byte[] { 5, 4, 3, 2, 1, 0 }));

		response.addAppRecord(app.build());

		return response.build();
	}

    

}
