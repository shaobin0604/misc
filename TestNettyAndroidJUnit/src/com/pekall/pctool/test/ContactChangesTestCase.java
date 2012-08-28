
package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.DatabaseHelper;
import com.pekall.pctool.model.Utils;
import com.pekall.pctool.model.contact.Contact;
import com.pekall.pctool.model.contact.Contact.ContactVersion;
import com.pekall.pctool.model.contact.ContactUtilFast;

import java.util.List;

public class ContactChangesTestCase extends AndroidTestCase {

    private DatabaseHelper mDatabaseHelper;

    protected void setUp() throws Exception {
        super.setUp();

        mDatabaseHelper = new DatabaseHelper(getContext());
        mDatabaseHelper.open();

        List<ContactVersion> contactVersions = ContactUtilFast.getAllContactVersion(getContext());

        mDatabaseHelper.updateContactVersions(contactVersions);
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        mDatabaseHelper.close();
        mDatabaseHelper = null;
    }

    public void testFindChanges() throws Exception {
        // add one contact
        {
            Contact contact = new Contact();

            contact.name = "add";
            
            boolean success = ContactUtilFast.addContact(getContext(), contact);
            
            Slog.d("add success = " + success);
        }
        // update one contact
        {
            Contact contact = ContactUtilFast.getContactById(getContext(), 8939);
            
            contact.name = "update";
            
            boolean success = ContactUtilFast.updateContact(getContext(), contact);
         
            Slog.d("update success = " + success);
        }
        // delete one contact
        {
            boolean success = ContactUtilFast.deleteContactById(getContext(), 8947);
            Slog.d("delete success = " + success);
        }
        

        List<ContactVersion> contactVersions = Utils.findContactChanges(
                ContactUtilFast.getAllContactVersion(getContext()), mDatabaseHelper.getLastSyncContactVersion());

        Slog.d(contactVersions.toString());
    }

}
