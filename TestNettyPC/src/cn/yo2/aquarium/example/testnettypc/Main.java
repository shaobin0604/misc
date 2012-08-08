
package cn.yo2.aquarium.example.testnettypc;

import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.Person;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        
        try {
            System.out.println("adb install -r TestNettyAndroid.apk");
            Runtime.getRuntime().exec("/home/dev01/opt/android-sdk/platform-tools/adb install -r TestProtobufAndroidServer.apk");
            Thread.sleep(3000);
            
            System.out.println("adb shell am broadcast -a NotifyServiceStop -n cn.yo2.aquarium.example.testnettyandroid/.MainServiceBroadcastReceiver");
            Runtime.getRuntime().exec("/home/dev01/opt/android-sdk/platform-tools/adb shell am broadcast -a NotifyServiceStop -n cn.yo2.aquarium.example.testnettyandroid/.MainServiceBroadcastReceiver");
            Thread.sleep(3000);

            System.out.println("adb shell am broadcast -a NotifyServiceStart -n cn.yo2.aquarium.example.testnettyandroid/.MainServiceBroadcastReceiver");
            Runtime.getRuntime().exec("/home/dev01/opt/android-sdk/platform-tools/adb shell am broadcast -a NotifyServiceStart -n cn.yo2.aquarium.example.testnettyandroid/.MainServiceBroadcastReceiver");
            Thread.sleep(3000);

            System.out.println("adb forward tcp:12580 tcp:12580");
            Runtime.getRuntime().exec("/home/dev01/opt/android-sdk/platform-tools/adb forward tcp:12580 tcp:12580");
            Thread.sleep(3000);

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        HttpClient client = new DefaultHttpClient();

        HttpGet get = new HttpGet("http://localhost:12580/");

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
        }

    }
}
