/**
 * 
 */
package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.model.contact.Contact;
import com.pekall.pctool.model.contact.ContactUtil;

import java.util.List;

/**
 * @author dev01
 *
 */
public class ContactUtilTestCase extends AndroidTestCase {

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testQueryContact() throws Exception {
        List<Contact> contacts = ContactUtil.getAllContacts(getContext());
        for (Contact contact : contacts) {
            System.out.println(contact);
        }
    }
}
