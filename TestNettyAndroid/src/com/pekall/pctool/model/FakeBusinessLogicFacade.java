
package com.pekall.pctool.model;

import android.content.Context;

import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.Person;
import com.google.protobuf.ByteString;
import com.pekall.pctool.model.app.AppInfo;
import com.pekall.pctool.model.app.AppUtil;
import com.pekall.pctool.model.app.AppUtil.AppNotExistException;
import com.pekall.pctool.protos.AppInfoProtos.AppInfoPList;
import com.pekall.pctool.protos.MsgDefProtos.AppLocationType;
import com.pekall.pctool.protos.MsgDefProtos.AppRecord;
import com.pekall.pctool.protos.MsgDefProtos.AppType;
import com.pekall.pctool.protos.MsgDefProtos.CmdResponse;
import com.pekall.pctool.protos.MsgDefProtos.CmdType;

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

    private Context mContext;

    public FakeBusinessLogicFacade(Context context) {
        this.mContext = context;
    }
    
    //-------------------------------------------------------------------------
    //  APP related method
    //-------------------------------------------------------------------------
    
    public CmdResponse queryApp() {
        List<AppInfo> appInfos = AppUtil.getAppInfos(mContext);
        
        CmdResponse.Builder response = CmdResponse.newBuilder();
        response.setType(CmdType.CMD_QUERY_APP);
        response.setResultCode(RESULT_CODE_OK);
        response.setResultMsg(RESULT_MSG_OK);
        
        AppRecord.Builder app = AppRecord.newBuilder();
        
        for (AppInfo appInfo : appInfos) {
            app.setAppName(appInfo.label);
            app.setType(appInfo.appType == AppInfo.FLAG_APP_TYPE_SYSTEM ? AppType.SYSTEM : AppType.USER);
            app.setLocation(appInfo.installLocation == AppInfo.FLAG_INSTALL_LOCATION_INTERNAL ? AppLocationType.INNER : AppLocationType.OUTER);
            app.setPackageName(appInfo.packageName);
            app.setVersionCode(appInfo.versionCode);
            app.setVersionName(appInfo.versionName);
            app.setSize(appInfo.apkFileSize);
            app.setAppIcon(ByteString.copyFrom(appInfo.icon));
            
            response.addAppRecord(app.build());
            
            app.clear();
        }
        
        return response.build();
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
        response.setType(CmdType.CMD_QUERY_APP);
        response.setResultCode(0);
        response.setResultMsg("OK");
        
        AppRecord.Builder app = AppRecord.newBuilder();
        
        app.setAppName("新浪微博");
        app.setType(AppType.USER);
        app.setLocation(AppLocationType.INNER);
        app.setPackageName("com.weibo");
        app.setVersionName("v2.0");
        app.setVersionCode(123456);
        app.setSize(2048);
        app.setAppIcon(ByteString.copyFrom(new byte[] {0, 1, 2, 3, 4, 5}));
        
        response.addAppRecord(app.build());
        
        app.clear();

        app.setAppName("腾讯微博");
        app.setType(AppType.USER);
        app.setLocation(AppLocationType.INNER);
        app.setPackageName("com.tencent.weibo");
        app.setVersionName("v2.3");
        app.setVersionCode(654321);
        app.setSize(1024);
        app.setAppIcon(ByteString.copyFrom(new byte[] {5, 4, 3, 2, 1, 0}));
        
        response.addAppRecord(app.build());
        
        return response.build();
    }
}
