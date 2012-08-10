
package com.pekall.pctool;

import com.google.protobuf.ByteString;
import com.pekall.pctool.protos.MsgDefProtos.AppLocationType;
import com.pekall.pctool.protos.MsgDefProtos.AppRecord;
import com.pekall.pctool.protos.MsgDefProtos.AppType;
import com.pekall.pctool.protos.MsgDefProtos.CmdResponse;
import com.pekall.pctool.protos.MsgDefProtos.CmdType;

public class FakeBusinessLogicFacade {

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
