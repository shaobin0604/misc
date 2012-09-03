
package com.pekall.pctool.client;

import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.Person;
import com.pekall.pctool.protos.AppInfoProtos.AppInfoP;
import com.pekall.pctool.protos.AppInfoProtos.AppInfoPList;
import com.pekall.pctool.protos.MsgDefProtos.AccountRecord;
import com.pekall.pctool.protos.MsgDefProtos.AgendaRecord;
import com.pekall.pctool.protos.MsgDefProtos.AttachmentRecord;
import com.pekall.pctool.protos.MsgDefProtos.CmdRequest;
import com.pekall.pctool.protos.MsgDefProtos.CmdResponse;
import com.pekall.pctool.protos.MsgDefProtos.CmdType;
import com.pekall.pctool.protos.MsgDefProtos.ContactRecord;
import com.pekall.pctool.protos.MsgDefProtos.IMRecord;
import com.pekall.pctool.protos.MsgDefProtos.IMRecord.IMType;
import com.pekall.pctool.protos.MsgDefProtos.MMSRecord;
import com.pekall.pctool.protos.MsgDefProtos.ModifyTag;
import com.pekall.pctool.protos.MsgDefProtos.PhoneRecord;
import com.pekall.pctool.protos.MsgDefProtos.PhoneRecord.PhoneType;
import com.pekall.pctool.protos.MsgDefProtos.SlideRecord;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Main {

    private static final String HOME_DIR = "/home/dev01";

    // private static final String HOME_DIR = "/home/shaobin";

    public static void main(String[] args) {
        try {
            // installAPK();
            Thread.sleep(3000);

            stopMainServer();
            Thread.sleep(3000);

            startMainServer();
            Thread.sleep(3000);

            forwardMainServerPort();
            Thread.sleep(3000);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // testGetAddressBook();

        // testGetAppInfoPList();

        // testQueryApp();

        // testQuerySms();
        
//        testQueryMmsAttachment();

        // testQueryCalendar();

        // testQueryAgenda();

        // testQueryAccount();

         testQueryContact();

        // testAddContact();

        // testUpdateContact();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        stopMainServer();

    }

    private static void forwardMainServerPort() {
        System.out.println("adb forward tcp:12580 tcp:12580");
        executeCommand(HOME_DIR + "/opt/android-sdk/platform-tools/adb forward tcp:12580 tcp:12580");
    }

    private static void startMainServer() {
        System.out
                .println("adb shell am broadcast -a com.pekall.pctool.AmCommandReceiver.ACTION_MAIN_SERVER_START -n com.pekall.pctool/.AmCommandReceiver");
        executeCommand(HOME_DIR
                + "/opt/android-sdk/platform-tools/adb shell am broadcast -a com.pekall.pctool.AmCommandReceiver.ACTION_MAIN_SERVER_START -n com.pekall.pctool/.AmCommandReceiver");
    }

    private static void stopMainServer() {
        System.out
                .println("adb shell am broadcast -a com.pekall.pctool.AmCommandReceiver.ACTION_MAIN_SERVER_STOP -n com.pekall.pctool/.AmCommandReceiver");
        executeCommand(HOME_DIR
                + "/opt/android-sdk/platform-tools/adb shell am broadcast -a com.pekall.pctool.AmCommandReceiver.ACTION_MAIN_SERVER_STOP -n com.pekall.pctool/.AmCommandReceiver");
    }

    private static void installAPK() {
        System.out.println("adb install -r TestNettyAndroid.apk");
        executeCommand(HOME_DIR + "/opt/android-sdk/platform-tools/adb install -r TestProtobufAndroidServer.apk");
    }

    private static void forwardFtpServerPort() {
        System.out.println("adb forward tcp:2121 tcp:2121");
        executeCommand(HOME_DIR + "/opt/android-sdk/platform-tools/adb forward tcp:2121 tcp:2121");
        System.out.println("adb forward tcp:2120 tcp:2120");
        executeCommand(HOME_DIR + "/opt/android-sdk/platform-tools/adb forward tcp:2120 tcp:2120");
    }

    private static void startFtpServer() {
        System.out
                .println("adb shell am broadcast -a com.pekall.pctool.AmCommandReceiver.ACTION_FTP_SERVER_START -n com.pekall.pctool/.AmCommandReceiver");
        executeCommand(HOME_DIR
                + "/opt/android-sdk/platform-tools/adb shell am broadcast -a com.pekall.pctool.AmCommandReceiver.ACTION_FTP_SERVER_START -n com.pekall.pctool/.AmCommandReceiver");
    }

    private static void stopFtpServer() {
        System.out
                .println("adb shell am broadcast -a com.pekall.pctool.AmCommandReceiver.ACTION_FTP_SERVER_STOP -n com.pekall.pctool/.AmCommandReceiver");
        executeCommand(HOME_DIR
                + "/opt/android-sdk/platform-tools/adb shell am broadcast -a com.pekall.pctool.AmCommandReceiver.ACTION_FTP_SERVER_STOP -n com.pekall.pctool/.AmCommandReceiver");
    }

    private static void executeCommand(String cmd) {
        try {
            Process proc = Runtime.getRuntime().exec(cmd);

            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
            // any output?
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

            // kick them off
            errorGobbler.start();
            outputGobbler.start();

            // any error???
            int exitVal = proc.waitFor();
            System.out.println("ExitValue: " + exitVal);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //
    // APP
    //
    private static void testQueryApp() {
        System.out.println("testQueryApp E");

        CmdRequest.Builder builder = CmdRequest.newBuilder();
        builder.setCmdType(CmdType.CMD_QUERY_APP);

        postCmdRequest(builder, true);

        System.out.println("testQueryApp X");
    }

    //
    // Sms
    //
    private static void testQuerySms() {
        System.out.println("testQuerySms E");

        CmdRequest.Builder builder = CmdRequest.newBuilder();
        builder.setCmdType(CmdType.CMD_QUERY_SMS);

        postCmdRequest(builder, true);

        System.out.println("testQuerySms X");
    }

    //
    // Mms
    //
    private static void testQueryMmsAttachment() {
        System.out.println("testQueryMmsAttachment E");

        CmdRequest.Builder builder = CmdRequest.newBuilder();
        builder.setCmdType(CmdType.CMD_QUERY_MMS);

        CmdResponse cmdResponse = postCmdRequest(builder, false);

        if (cmdResponse.getCmdType() == CmdType.CMD_QUERY_MMS) {
            List<MMSRecord> mmsRecords = cmdResponse.getMmsRecordList();

            for (MMSRecord mmsRecord : mmsRecords) {
                if (mmsRecord.getMsgId() == 51) {
                    for (SlideRecord slideRecord : mmsRecord.getSlideList()) {

                        for (AttachmentRecord attachmentRecord : slideRecord.getAttachmentList()) {

                            System.out.println("attachment type = " + attachmentRecord.getType() + ", name = "
                                    + attachmentRecord.getName() + ", size = " + attachmentRecord.getSize());

                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(attachmentRecord.getName());
                                fos.write(attachmentRecord.getContent().toByteArray());
                                System.out.println("write " + attachmentRecord.getName());

                                
                            } catch (FileNotFoundException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } finally {
                                if (fos != null) {
                                    try {
                                        fos.close();
                                    } catch (IOException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                            }

                            
                        }

                    }

                    break;
                }
            }
        }

        System.out.println("testQueryMmsAttachment X");
    }

    //
    // Calendar
    //
    private static void testQueryCalendar() {
        System.out.println("testQueryCalendar E");

        CmdRequest.Builder builder = CmdRequest.newBuilder();
        builder.setCmdType(CmdType.CMD_QUERY_CALENDAR);

        postCmdRequest(builder, true);

        System.out.println("testQueryCalendar X");
    }

    private static void testQueryAgenda() {
        System.out.println("testQueryAgenda E");

        CmdRequest.Builder builder = CmdRequest.newBuilder();
        builder.setCmdType(CmdType.CMD_QUERY_AGENDAS);

        AgendaRecord.Builder agendaRecordBuilder = AgendaRecord.newBuilder();

        agendaRecordBuilder.setCalendarId(1);

        builder.setAgendaParams(agendaRecordBuilder);

        postCmdRequest(builder, true);

        System.out.println("testQueryAgenda X");
    }

    //
    // Contact
    //

    private static void testQueryAccount() {
        System.out.println("testQuerryAccount E");

        CmdRequest.Builder builder = CmdRequest.newBuilder();
        builder.setCmdType(CmdType.CMD_GET_ALL_ACCOUNTS);

        postCmdRequest(builder, true);

        System.out.println("testQuerryAccount X");
    }

    private static void testQueryContact() {
        System.out.println("testQueryContact E");

        CmdRequest.Builder builder = CmdRequest.newBuilder();
        builder.setCmdType(CmdType.CMD_QUERY_CONTACTS);

        postCmdRequest(builder, /* dumpResponse */ true);

        System.out.println("testQueryContact X");
    }

    private static void testAddContact() {
        System.out.println("testAddContact E");

        CmdRequest.Builder builder = CmdRequest.newBuilder();
        builder.setCmdType(CmdType.CMD_ADD_CONTACT);

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

        builder.setContactParams(contactRecordBuilder);

        postCmdRequest(builder, true);

        System.out.println("testAddContact X");
    }

    private static void testUpdateContact() {
        System.out.println("testUpdateContact E");

        CmdRequest.Builder builder = CmdRequest.newBuilder();
        builder.setCmdType(CmdType.CMD_QUERY_CONTACTS);

        CmdResponse cmdResponse = postCmdRequest(builder, true);

        List<ContactRecord> contactRecordList = cmdResponse.getContactRecordList();

        ContactRecord contactRecord = contactRecordList.get(0);

        System.out.println("original");
        System.out.println(contactRecord.toString());

        ContactRecord.Builder contactRecordBuilder = contactRecord.toBuilder();
        contactRecordBuilder.setNickname("testUpdateContact");

        IMRecord.Builder imRecordBuilder = IMRecord.newBuilder();

        imRecordBuilder.setAccount("65491117");
        imRecordBuilder.setType(IMType.QQ);
        imRecordBuilder.setModifyTag(ModifyTag.ADD);
        imRecordBuilder.setName("");

        contactRecordBuilder.addIm(imRecordBuilder);

        ContactRecord contactRecordParam = contactRecordBuilder.build();

        System.out.println("modified");
        System.out.println(contactRecordParam.toString());

        builder.clear();
        builder.setCmdType(CmdType.CMD_EDIT_CONTACT);

        builder.setContactParams(contactRecordParam);

        cmdResponse = postCmdRequest(builder, true);

        System.out.println("testUpdateContact X");
    }

    private static CmdResponse postCmdRequest(CmdRequest.Builder cmdRequestBuilder, boolean dumpResponse) {

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://localhost:12580/rpc");
        post.setHeader("Content-Type", "application/x-protobuf");
        final CmdRequest cmdRequest = cmdRequestBuilder.build();
        post.setEntity(new ByteArrayEntity(cmdRequest.toByteArray()));
        try {
            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            CmdResponse cmdResponse = CmdResponse.parseFrom(entity.getContent());
            EntityUtils.consume(entity);

            if (dumpResponse) {
                System.out.println(cmdResponse.toString());
            }

            return cmdResponse;

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            client.getConnectionManager().shutdown();
        }
        return null;
    }

    // ------------------------------------------------------------------------
    //
    // ------------------------------------------------------------------------
    private static void testGetAddressBook() {
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet("http://localhost:12580/test");
        try {
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            AddressBook addressBook = AddressBook.parseFrom(entity.getContent());
            EntityUtils.consume(entity);

            for (Person p : addressBook.getPersonList()) {
                System.out.println("{id: " + p.getId() + ", name: " + p.getName() + "}");
            }

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            get.releaseConnection();
            client.getConnectionManager().shutdown();
        }
    }

    private static void testGetAppInfoPList() {
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet("http://localhost:12580/apps");
        try {
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            AppInfoPList appInfoPList = AppInfoPList.parseFrom(entity.getContent());
            EntityUtils.consume(entity);

            System.out.println("app count = " + appInfoPList.getAppInfosCount());

            AppInfoP appInfoP = appInfoPList.getAppInfos(0);

            System.out.println("appInfoP at index 0 label: " + appInfoP.getLabel());
            System.out.println("appInfoP at index 0 package: " + appInfoP.getPackageName());
            System.out.println("appInfoP at index 0 apk path: " + appInfoP.getApkFilePath());
            System.out.println("appInfoP at index 0 apk size: " + appInfoP.getApkFileSize());

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            get.releaseConnection();
            client.getConnectionManager().shutdown();
        }
    }

}
