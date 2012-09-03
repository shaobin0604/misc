
package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.DatabaseHelper;
import com.pekall.pctool.model.FastSyncUtils;
import com.pekall.pctool.model.contact.Contact;
import com.pekall.pctool.model.contact.Contact.ContactVersion;
import com.pekall.pctool.model.contact.ContactUtil;

import java.util.List;

public class ContactChangesTestCase extends AndroidTestCase {

    private DatabaseHelper mDatabaseHelper;

    protected void setUp() throws Exception {
        super.setUp();

        mDatabaseHelper = new DatabaseHelper(getContext());
        mDatabaseHelper.open();

        List<ContactVersion> contactVersions = ContactUtil.getAllContactVersions(getContext());

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
            
            boolean success = ContactUtil.addContact(getContext(), contact) > 0;
            
            Slog.d("add success = " + success);
        }
        // update one contact
        {
            Contact contact = ContactUtil.getContactById(getContext(), 8939);
            
            contact.name = "update";
            
            boolean success = ContactUtil.updateContact(getContext(), contact);
         
            Slog.d("update success = " + success);
        }
        // delete one contact
        {
            boolean success = ContactUtil.deleteContactById(getContext(), 8947);
            Slog.d("delete success = " + success);
        }
        

        List<ContactVersion> contactVersions = FastSyncUtils.calculateContactChanges(
                ContactUtil.getAllContactVersions(getContext()), mDatabaseHelper.getLastSyncContactVersions());

        Slog.d(contactVersions.toString());
    }

}
