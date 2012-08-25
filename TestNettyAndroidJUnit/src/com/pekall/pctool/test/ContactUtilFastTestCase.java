
package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.contact.Contact;
import com.pekall.pctool.model.contact.ContactUtilFast;

import java.util.List;

public class ContactUtilFastTestCase extends AndroidTestCase {
    public void testQueryContact() throws Exception {
        List<Contact> contacts = ContactUtilFast.getAllContacts(getContext());
//        for (Contact contact : contacts) {
//            Slog.d(contact.toString());
//        }
    }
}
