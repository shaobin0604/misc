package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.HandlerFacade;
import com.pekall.pctool.protos.MsgDefProtos.CmdRequest;
import com.pekall.pctool.protos.MsgDefProtos.CmdResponse;
import com.pekall.pctool.protos.MsgDefProtos.CmdType;

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
    
    public void testQueryMms() throws Exception {
        CmdRequest.Builder cmdRequestBuilder = CmdRequest.newBuilder();
        
        cmdRequestBuilder.setCmdType(CmdType.CMD_QUERY_MMS);
        
        CmdResponse cmdResponse = mHandlerFacade.queryMms(cmdRequestBuilder.build());
        
        Slog.d(cmdResponse.toString());
    }
}
