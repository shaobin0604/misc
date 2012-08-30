package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.HandlerFacade;
import com.pekall.pctool.protos.MsgDefProtos.AttachmentRecord;
import com.pekall.pctool.protos.MsgDefProtos.CmdRequest;
import com.pekall.pctool.protos.MsgDefProtos.CmdResponse;
import com.pekall.pctool.protos.MsgDefProtos.CmdType;
import com.pekall.pctool.protos.MsgDefProtos.MMSRecord;
import com.pekall.pctool.protos.MsgDefProtos.SlideRecord;

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
}
