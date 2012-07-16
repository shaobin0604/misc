
package cn.yo2.aquarium.testprotobufserver;

import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.Person;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main extends NanoHTTPD {

    public Main(int port) throws IOException {
        super(port, null);
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
        AddressBook addressBook = getAddressBook();
        return new Response(HTTP_OK, "application/x-protobuf", new ByteArrayInputStream(addressBook.toByteArray()));
    }

    private Person getPerson(String name, int id) {
        return Person.newBuilder().setName(name).setId(id).build();
    }

    private AddressBook getAddressBook() {
        AddressBook.Builder builder = AddressBook.newBuilder();
        builder.addPerson(getPerson("shaobin", 1));
        builder.addPerson(getPerson("haungyu", 2));
        return builder.build();
    }

    public static void main(String[] args) {

        int port = 8080;

        try {
            new Main(port);
        } catch (IOException ioe) {
            myErr.println("Couldn't start server:\n" + ioe);
            System.exit(-1);
        }

        myOut.println("Now serving files in port " + port);
        myOut.println("Hit Enter to stop.\n");

        try {
            System.in.read();
        } catch (Throwable t) {
        }
    }
}
