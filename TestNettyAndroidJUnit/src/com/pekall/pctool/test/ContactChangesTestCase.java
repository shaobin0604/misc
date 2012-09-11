
package com.pekall.pctool.test;

import android.provider.ContactsContract.CommonDataKinds;
import android.test.AndroidTestCase;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.DatabaseHelper;
import com.pekall.pctool.model.FastSyncUtils;
import com.pekall.pctool.model.contact.Contact;
import com.pekall.pctool.model.contact.Contact.ContactVersion;
import com.pekall.pctool.model.contact.Contact.ImInfo;
import com.pekall.pctool.model.contact.Contact.ModifyTag;
import com.pekall.pctool.model.contact.Contact.PhoneInfo;
import com.pekall.pctool.model.contact.ContactUtil;

import java.util.List;

public class ContactChangesTestCase extends AndroidTestCase {
    
    private static final String CONTACT_NAME_1 = "ContactChangesTestCase1";
    private static final String CONTACT_NICKNAME_1 = "ContactChangesTestCase1 nick";
    
    private static final String PHONE_NUMBER_1 = "028-65478965";
    private static final int PHONE_TYPE_1 = CommonDataKinds.Phone.TYPE_HOME;
    
    private static final String IM_ACCOUNT_1 = "shaobin0604@gmail.com";
    private static final int IM_TYPE_1 = CommonDataKinds.Im.TYPE_HOME;
    
    
    private static final String CONTACT_NAME_2 = "ContactChangesTestCase2";
    private static final String CONTACT_NICKNAME_2 = "ContactChangesTestCase2 nick";
    
    private static final String PHONE_NUMBER_2 = "028-12345678";
    private static final int PHONE_TYPE_2 = CommonDataKinds.Phone.TYPE_HOME;
    
    private static final String IM_ACCOUNT_2 = "shaobin0604@hotmail.com";
    private static final int IM_TYPE_2 = CommonDataKinds.Im.TYPE_HOME;
    
    private static final int COUNT_OF_INITIAL_CONTACT = 2;
    
    private long mFirstContactId;
    private long mSecondContactId;
    private DatabaseHelper mDatabaseHelper;
    
    
    private void populateContacts() {
        Contact contact = new Contact();
        contact.name = CONTACT_NAME_1;
        contact.nickname = CONTACT_NICKNAME_1;
        
        PhoneInfo phoneInfo = new PhoneInfo();
        phoneInfo.modifyFlag = ModifyTag.add;
        phoneInfo.number = PHONE_NUMBER_1;
        phoneInfo.type = PHONE_TYPE_1;
        
        contact.addPhoneInfo(phoneInfo);
        
        ImInfo imInfo = new ImInfo();
        imInfo.modifyFlag = ModifyTag.add;
        imInfo.account = IM_ACCOUNT_1;
        imInfo.type = IM_TYPE_1;
        
        contact.addImInfo(imInfo);
        
        mFirstContactId = ContactUtil.addContact(getContext(), contact);
        assertTrue(mFirstContactId > 0);
        
        
        
        contact = new Contact();
        contact.name = CONTACT_NAME_2;
        contact.nickname = CONTACT_NICKNAME_2;
        
        phoneInfo = new PhoneInfo();
        phoneInfo.modifyFlag = ModifyTag.add;
        phoneInfo.number = PHONE_NUMBER_2;
        phoneInfo.type = PHONE_TYPE_2;
        
        contact.addPhoneInfo(phoneInfo);
        
        imInfo = new ImInfo();
        imInfo.modifyFlag = ModifyTag.add;
        imInfo.account = IM_ACCOUNT_2;
        imInfo.type = IM_TYPE_2;
        
        contact.addImInfo(imInfo);
        
        mSecondContactId = ContactUtil.addContact(getContext(), contact);
        assertTrue(mSecondContactId > 0);
    }

    protected void setUp() throws Exception {
        super.setUp();
        
        ContactUtil.deleteContactAll(getContext());
        
        populateContacts();

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
            Contact contact = ContactUtil.getContactById(getContext(), mFirstContactId);
            
            contact.name = "update";
            
            boolean success = ContactUtil.updateContact(getContext(), contact);
         
            Slog.d("update success = " + success);
        }
        // delete one contact
        {
            boolean success = ContactUtil.deleteContactById(getContext(), mSecondContactId);
            Slog.d("delete success = " + success);
        }
        

        List<ContactVersion> contactVersions = FastSyncUtils.calculateContactChanges(
                ContactUtil.getAllContactVersions(getContext()), mDatabaseHelper.getLastSyncContactVersions());

        Slog.d(contactVersions.toString());
    }

}
