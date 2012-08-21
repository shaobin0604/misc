
package com.pekall.pctool.client;

import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.Person;
import com.pekall.pctool.protos.AppInfoProtos.AppInfoP;
import com.pekall.pctool.protos.AppInfoProtos.AppInfoPList;
import com.pekall.pctool.protos.MsgDefProtos.AccountRecord;
import com.pekall.pctool.protos.MsgDefProtos.AgendaRecord;
import com.pekall.pctool.protos.MsgDefProtos.CmdRequest;
import com.pekall.pctool.protos.MsgDefProtos.CmdResponse;
import com.pekall.pctool.protos.MsgDefProtos.CmdType;
import com.pekall.pctool.protos.MsgDefProtos.ContactRecord;
import com.pekall.pctool.protos.MsgDefProtos.PhoneRecord;
import com.pekall.pctool.protos.MsgDefProtos.PhoneRecord.PhoneType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class Main {
	
	private static final String HOME_DIR = "/home/dev01";
//	private static final String HOME_DIR = "/home/shaobin";

    public static void main(String[] args) {
        try {
//            System.out.println("adb install -r TestNettyAndroid.apk");
//            executeCommand(HOME_DIR + "/opt/android-sdk/platform-tools/adb install -r TestProtobufAndroidServer.apk");
//            Thread.sleep(3000);
            System.out.println("adb shell am broadcast -a com.pekall.pctool.AmCommandReceiver.ACTION_MAIN_SERVER_STOP -n com.pekall.pctool/.AmCommandReceiver");
            executeCommand(HOME_DIR + "/opt/android-sdk/platform-tools/adb shell am broadcast -a com.pekall.pctool.AmCommandReceiver.ACTION_MAIN_SERVER_STOP -n com.pekall.pctool/.AmCommandReceiver");
            Thread.sleep(3000);

            System.out.println("adb shell am broadcast -a com.pekall.pctool.AmCommandReceiver.ACTION_MAIN_SERVER_START -n com.pekall.pctool/.AmCommandReceiver");
            executeCommand(HOME_DIR + "/opt/android-sdk/platform-tools/adb shell am broadcast -a com.pekall.pctool.AmCommandReceiver.ACTION_MAIN_SERVER_START -n com.pekall.pctool/.AmCommandReceiver");
            Thread.sleep(3000);

            System.out.println("adb forward tcp:12580 tcp:12580");
            executeCommand(HOME_DIR + "/opt/android-sdk/platform-tools/adb forward tcp:12580 tcp:12580");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//        testGetAddressBook();
        
//        testGetAppInfoPList();
        
//        testQueryApp();
        
//        testQuerySms();
        
//        testQueryCalendar();
        
//        testQueryAgenda();
        
//        testQueryContact();
        
        testQueryAccount();
        
        testAddContact();
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("adb shell am broadcast -a com.pekall.pctool.AmCommandReceiver.ACTION_MAIN_SERVER_STOP -n com.pekall.pctool/.AmCommandReceiver");
        executeCommand(HOME_DIR + "/opt/android-sdk/platform-tools/adb shell am broadcast -a com.pekall.pctool.AmCommandReceiver.ACTION_MAIN_SERVER_STOP -n com.pekall.pctool/.AmCommandReceiver");

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

        postCmdRequest(builder);
        
        System.out.println("testQueryApp X");
    }
    
    //
    // Sms
    //
    private static void testQuerySms() {
        System.out.println("testQuerySms E");
        
        CmdRequest.Builder builder = CmdRequest.newBuilder();
        builder.setCmdType(CmdType.CMD_QUERY_SMS);
        
        postCmdRequest(builder);
        
        System.out.println("testQuerySms X");
    }
    
    //
    // Calendar
    //
    private static void testQueryCalendar() {
        System.out.println("testQueryCalendar E");
        
        CmdRequest.Builder builder = CmdRequest.newBuilder();
        builder.setCmdType(CmdType.CMD_QUERY_CALENDAR);
        
        postCmdRequest(builder);
        
        System.out.println("testQueryCalendar X");
    }
    
    
    private static void testQueryAgenda() {
        System.out.println("testQueryAgenda E");
        
        CmdRequest.Builder builder = CmdRequest.newBuilder();
        builder.setCmdType(CmdType.CMD_QUERY_AGENDAS);
        
        AgendaRecord.Builder agendaRecordBuilder = AgendaRecord.newBuilder();
        
        agendaRecordBuilder.setCalendarId(1);
        
        builder.setAgendaParams(agendaRecordBuilder);
        
        postCmdRequest(builder);
        
        System.out.println("testQueryAgenda X");
    }

    //
    // Contact
    //
    private static void testQueryContact() {
        System.out.println("testQueryContact E");
        
        CmdRequest.Builder builder = CmdRequest.newBuilder();
        builder.setCmdType(CmdType.CMD_QUERY_CONTACTS);
        
        postCmdRequest(builder);
        
        System.out.println("testQueryContact X");
    }
    
    private static void testQueryAccount() {
        System.out.println("testQuerryAccount E");
        
        CmdRequest.Builder builder = CmdRequest.newBuilder();
        builder.setCmdType(CmdType.CMD_GET_ALL_ACCOUNTS);
        
        postCmdRequest(builder);
        
        System.out.println("testQuerryAccount X");
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
        
        postCmdRequest(builder);
        
        System.out.println("testAddContact X");
    }
    
    private static void postCmdRequest(CmdRequest.Builder cmdRequestBuilder) {

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
            
            System.out.println(cmdResponse.toString());

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
