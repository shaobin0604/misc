
package com.pekall.pctool.model;

import android.content.Context;

import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.Person;
import com.google.protobuf.ByteString;
import com.pekall.pctool.model.app.AppInfo;
import com.pekall.pctool.model.app.AppUtil;
import com.pekall.pctool.model.app.AppUtil.AppNotExistException;
import com.pekall.pctool.model.sms.Sms;
import com.pekall.pctool.model.sms.SmsUtil;
import com.pekall.pctool.protos.AppInfoProtos.AppInfoPList;
import com.pekall.pctool.protos.MsgDefProtos.AppRecord;
import com.pekall.pctool.protos.MsgDefProtos.AppRecord.AppLocationType;
import com.pekall.pctool.protos.MsgDefProtos.AppRecord.AppType;
import com.pekall.pctool.protos.MsgDefProtos.CmdRequest;
import com.pekall.pctool.protos.MsgDefProtos.CmdResponse;
import com.pekall.pctool.protos.MsgDefProtos.CmdType;
import com.pekall.pctool.protos.MsgDefProtos.MsgOriginType;
import com.pekall.pctool.protos.MsgDefProtos.SMSRecord;

import java.io.InputStream;
import java.util.List;

public class FakeBusinessLogicFacade {
    private static final int RESULT_CODE_OK = 0;
    
    // client error code
    private static final int RESULT_CODE_ERR_ILLEAGAL_ARGUMENT = 100;
    private static final int RESULT_CODE_ERR_INSUFFICIENT_ARGUMENT = 101;
    
    // server error code
    private static final int RESULT_CODE_ERR_INTERNAL = 200;
    
    private static final String RESULT_MSG_OK = "OK";
    private static final String RESULT_MSG_ERR_ILLEGAL_ARGUMENT = "Illegal argument";
    
    private static final String RESULT_MSG_ERR_INTERNAL = "Internal error";

    private Context mContext;

    public FakeBusinessLogicFacade(Context context) {
        this.mContext = context;
    }
    
    public CmdResponse defaultCmdResponse() {
        CmdResponse.Builder response = CmdResponse.newBuilder();
        
        response.setCmdType(CmdType.CMD_HEART_BEAT);
        response.setResultCode(RESULT_CODE_ERR_ILLEAGAL_ARGUMENT);
        response.setResultMsg(RESULT_MSG_ERR_ILLEGAL_ARGUMENT);
        
        return response.build();
    }
    
    //-------------------------------------------------------------------------
    //  Sms related method
    //-------------------------------------------------------------------------
    
    public CmdResponse querySms() {
        List<Sms> smsList = SmsUtil.getSmsList(mContext);
        
        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_QUERY_SMS);
        responseBuilder.setResultCode(RESULT_CODE_OK);
        responseBuilder.setResultMsg(RESULT_MSG_OK);
        
        SMSRecord.Builder smsBuilder = SMSRecord.newBuilder();
        
        for (Sms sms : smsList) {
            smsBuilder.setMsgId(sms.rowId);
            smsBuilder.setMsgOrigin(smsTypeToMsgOriginType(sms.type));
            smsBuilder.setPhoneNum(sms.address);
            smsBuilder.setMsgText(sms.body);
            smsBuilder.setMsgTime(sms.date);
            smsBuilder.setReadTag(sms.read == Sms.READ_TRUE);
        }

        return responseBuilder.build();
    }
    
    public CmdResponse deleteSms(CmdRequest request) {
        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_DELETE_SMS);
        
        if (SmsUtil.deleteSms(mContext, request.getRecordIdList())) {
            responseBuilder.setResultCode(RESULT_CODE_OK);
            responseBuilder.setResultMsg(RESULT_MSG_OK);
        } else {
            responseBuilder.setResultCode(RESULT_CODE_ERR_INTERNAL);
            responseBuilder.setResultMsg(RESULT_MSG_ERR_INTERNAL);
        }
        
        return responseBuilder.build();
    }
    
    public CmdRequest importSms(CmdRequest request) {
        
    }
    
    private static MsgOriginType smsTypeToMsgOriginType(int smsType) {
        switch (smsType) {
            case Sms.TYPE_RECEIVED:
                return MsgOriginType.INBOX;
            case Sms.TYPE_SENT:
                return MsgOriginType.SENTBOX;
            case Sms.TYPE_DRAFT:
                return MsgOriginType.DRAFTBOX;
            case Sms.TYPE_OUTBOX:
            case Sms.TYPE_QUEUED:
            case Sms.TYPE_FAILED:
                return MsgOriginType.OUTBOX;
            default:
                throw new IllegalArgumentException("Unknown smsType: " + smsType);
        }
    }
    
    //-------------------------------------------------------------------------
    //  App related method
    //-------------------------------------------------------------------------
    
    public CmdResponse queryApp() {
        List<AppInfo> appInfos = AppUtil.getAppInfos(mContext);
        
        CmdResponse.Builder responseBuilder = CmdResponse.newBuilder();
        responseBuilder.setCmdType(CmdType.CMD_QUERY_APP);
        responseBuilder.setResultCode(RESULT_CODE_OK);
        responseBuilder.setResultMsg(RESULT_MSG_OK);
        
        AppRecord.Builder appBuilder = AppRecord.newBuilder();
        
        for (AppInfo appInfo : appInfos) {
            appBuilder.setAppName(appInfo.label);
            appBuilder.setAppType(appInfo.appType == AppInfo.FLAG_APP_TYPE_SYSTEM ? AppType.SYSTEM : AppType.USER);
            appBuilder.setLocationType(appInfo.installLocation == AppInfo.FLAG_INSTALL_LOCATION_INTERNAL ? AppLocationType.INTERNAL : AppLocationType.EXTERNAL);
            appBuilder.setPackageName(appInfo.packageName);
            appBuilder.setVersionCode(appInfo.versionCode);
            appBuilder.setVersionName(appInfo.versionName);
            appBuilder.setSize(appInfo.apkFileSize);
            appBuilder.setAppIcon(ByteString.copyFrom(appInfo.icon));
            
            responseBuilder.addAppRecord(appBuilder.build());
            
            appBuilder.clear();
        }
        
        return responseBuilder.build();
    }
    
    public InputStream exportApp(String packageName) throws AppNotExistException {
        return AppUtil.getAppApkStream(mContext, packageName);
    }
    
    
    //-------------------------------------------------------------------------
    //  TEST code 
    //-------------------------------------------------------------------------
    
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
        app.setAppIcon(ByteString.copyFrom(new byte[] {0, 1, 2, 3, 4, 5}));
        
        response.addAppRecord(app.build());
        
        app.clear();

        app.setAppName("腾讯微博");
        app.setAppType(AppType.USER);
        app.setLocationType(AppLocationType.INTERNAL);
        app.setPackageName("com.tencent.weibo");
        app.setVersionName("v2.3");
        app.setVersionCode(654321);
        app.setSize(1024);
        app.setAppIcon(ByteString.copyFrom(new byte[] {5, 4, 3, 2, 1, 0}));
        
        response.addAppRecord(app.build());
        
        return response.build();
    }
}
