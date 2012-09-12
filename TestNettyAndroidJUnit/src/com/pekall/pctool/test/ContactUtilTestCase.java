
package com.pekall.pctool.test;

import android.provider.ContactsContract.CommonDataKinds;
import android.test.AndroidTestCase;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.contact.Contact;
import com.pekall.pctool.model.contact.Contact.ImInfo;
import com.pekall.pctool.model.contact.Contact.ModifyTag;
import com.pekall.pctool.model.contact.Contact.PhoneInfo;
import com.pekall.pctool.model.contact.ContactUtil;

import java.util.List;

public class ContactUtilTestCase extends AndroidTestCase {
    
    private static final String CONTACT_NAME = "unit test";
    private static final String CONTACT_NICKNAME = "unit test nick";
    
    private static final String PHONE_NUMBER_1 = "028-65478965";
    private static final int PHONE_TYPE_1 = CommonDataKinds.Phone.TYPE_HOME;
    
    private static final String IM_ACCOUNT_1 = "shaobin0604@gmail.com";
    private static final int IM_TYPE_1 = CommonDataKinds.Im.TYPE_HOME;
    
    private static final int COUNT_OF_INITIAL_CONTACT = 1;
    
    private long mContactId;
    
    
    private void populateContacts() {
        Contact contact = new Contact();
        contact.name = CONTACT_NAME;
        contact.nickname = CONTACT_NICKNAME;
        
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
        
        mContactId = ContactUtil.addContact(getContext(), contact);
        
        assertTrue(mContactId > 0);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        ContactUtil.deleteContactAll(getContext());
        
        populateContacts();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testQueryContact() throws Exception {
        List<Contact> contacts = ContactUtil.getAllContacts(getContext());
        for (Contact contact : contacts) {
            Slog.d(contact.toString());
        }
        assertEquals(COUNT_OF_INITIAL_CONTACT, contacts.size());
    }
    
    public void testUpdateContactNameEmpty() throws Exception {
        final String expectedName = "";
        
        Contact contact = ContactUtil.getContactById(getContext(), mContactId);
        contact.name = expectedName;
        
        boolean success = ContactUtil.updateContact(getContext(), contact);
        
        assertTrue(success);
        
        contact = ContactUtil.getContactById(getContext(), mContactId);
        assertEquals(expectedName, contact.name);
        
    }
    
    /**
     * Test add a phone record and delete a phone record
     * 
     * @throws Exception
     */
    public void testUpdateContactPhone1() throws Exception {
        Contact contact = ContactUtil.getContactById(getContext(), mContactId);
        List<PhoneInfo> phoneInfos = contact.phoneInfos;
        PhoneInfo phoneInfo = phoneInfos.get(0);
        phoneInfo.modifyFlag = ModifyTag.del;
        
        phoneInfo = new PhoneInfo();
        phoneInfo.type = CommonDataKinds.Phone.TYPE_HOME;
        final String newPhoneNumber = "0832-3993098";
        phoneInfo.number = newPhoneNumber;
        phoneInfo.modifyFlag = ModifyTag.add;
        
        phoneInfos.add(phoneInfo);
        
        boolean success = ContactUtil.updateContact(getContext(), contact);
        
        assertTrue(success);
        
        contact = ContactUtil.getContactById(getContext(), mContactId);
        phoneInfos = contact.phoneInfos;
        
        assertEquals(1, phoneInfos.size());
        
        phoneInfo = phoneInfos.get(0);
        
        assertEquals(newPhoneNumber, phoneInfo.number);
    }
    
    /**
     * Test add a phone record and delete a phone record
     * 
     * @throws Exception
     */
    public void testUpdateContactPhone2() throws Exception {
        Contact contact = ContactUtil.getContactById(getContext(), mContactId);
        List<PhoneInfo> phoneInfos = contact.phoneInfos;
        
        PhoneInfo phoneInfo = new PhoneInfo();
        phoneInfo.type = CommonDataKinds.Phone.TYPE_HOME;
        final String newPhoneNumber = "0832-3993098";
        phoneInfo.number = newPhoneNumber;
        phoneInfo.modifyFlag = ModifyTag.add;
        
        phoneInfos.add(phoneInfo);
        
        boolean success = ContactUtil.updateContact(getContext(), contact);
        
        assertTrue(success);
        
        contact = ContactUtil.getContactById(getContext(), mContactId);
        phoneInfos = contact.phoneInfos;
        
        assertEquals(2, phoneInfos.size());
    }
    
    public void testUpdateContactForcePhone1() throws Exception {
        Contact contact = ContactUtil.getContactById(getContext(), mContactId);
        List<PhoneInfo> phoneInfos = contact.phoneInfos;
        
        // remove all phones
        phoneInfos.clear();
        
        // add one phone
        PhoneInfo phoneInfo = new PhoneInfo();
        
        phoneInfo.type = CommonDataKinds.Phone.TYPE_HOME;
        final String newPhoneNumber = "0832-3993098";
        phoneInfo.number = newPhoneNumber;
        
        phoneInfos.add(phoneInfo);
        
        List<ImInfo> imInfos = contact.imInfos;
        
        ImInfo imInfo = new ImInfo();
        imInfo.type = CommonDataKinds.Im.TYPE_HOME;
        final String newImAccount = "shaobin0604@hotmail.com";
        imInfo.account = newImAccount;
        
        imInfos.add(imInfo);
        
        boolean success = ContactUtil.updateContactForce(getContext(), contact);
        
        assertTrue(success);
        
        contact = ContactUtil.getContactById(getContext(), mContactId);
        phoneInfos = contact.phoneInfos;
        
        assertEquals(1, phoneInfos.size());
        
        phoneInfo = phoneInfos.get(0);
        
        assertEquals(newPhoneNumber, phoneInfo.number);
        
        imInfos = contact.imInfos;
        
        assertEquals(2, imInfos.size());
    }
}
