
package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.contact.Contact;
import com.pekall.pctool.model.contact.ContactUtil;

import java.util.List;

public class ContactUtilTestCase extends AndroidTestCase {
    public void testQueryContact() throws Exception {
        List<Contact> contacts = ContactUtil.getAllContacts(getContext());
        for (Contact contact : contacts) {
            Slog.d(contact.toString());
        }
    }
}
