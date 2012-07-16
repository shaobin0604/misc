
package cn.yo2.aquarium.testprotobufclient;

import com.example.tutorial.AddressBookProtos.AddressBook;

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
        HttpClient client = new DefaultHttpClient();
        
        HttpGet get = new HttpGet("http://localhost:8080/");
        
        try {
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            AddressBook addressBook = AddressBook.parseFrom(entity.getContent());
            EntityUtils.consume(entity);
            
            System.out.println(addressBook.toString());
            
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
