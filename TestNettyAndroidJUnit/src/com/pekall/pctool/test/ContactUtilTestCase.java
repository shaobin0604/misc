
package com.pekall.pctool.test;

import java.util.Collection;
import java.util.List;
import java.util.Queue;

import android.provider.ContactsContract.CommonDataKinds;
import android.test.AndroidTestCase;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.contact.Contact;
import com.pekall.pctool.model.contact.Contact.EmailInfo;
import com.pekall.pctool.model.contact.Contact.ImInfo;
import com.pekall.pctool.model.contact.Contact.ModifyTag;
import com.pekall.pctool.model.contact.Contact.PhoneInfo;
import com.pekall.pctool.model.contact.ContactUtil;

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
        
        PhoneInfo phoneInfo = new PhoneInfo();
        phoneInfo.modifyFlag = ModifyTag.add;
        phoneInfo.number = PHONE_NUMBER_1;
        phoneInfo.type = PHONE_TYPE_1;
        
        contact.addPhoneInfo(phoneInfo);
        
        ImInfo imInfo = new ImInfo();
        imInfo.modifyFlag = ModifyTag.add;
        imInfo.account = IM_ACCOUNT_1;
        imInfo.protocol = IM_TYPE_1;
        
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
        Collection<Contact> contacts = ContactUtil.getContactsAll(getContext());
        for (Contact contact : contacts) {
            Slog.d(contact.toString());
        }
        assertEquals(COUNT_OF_INITIAL_CONTACT, contacts.size());
        
        Slog.d("count = " + contacts.size());
    }
    
    
    public void testQueryContactByIdFast() throws Exception {
        Contact contact = ContactUtil.getContactById(getContext(), mContactId);
        Slog.d(contact.toString());
        assertEquals(CONTACT_NAME, contact.name);
    }
    
    public void testUpdateContactNameEmpty() throws Exception {
        final String expectedName = "";
        
        Contact contact = ContactUtil.getContactById(getContext(), mContactId);
        contact.name = expectedName;
        
        boolean success = ContactUtil.updateContact(getContext(), contact) != null;
        
        assertTrue(success);
        
        contact = ContactUtil.getContactById(getContext(), mContactId);
        assertEquals(expectedName, contact.name);
        
    }
    
    public void testUpdateContactRetrieveDataIds() throws Exception {
        Contact contact = ContactUtil.getContactById(getContext(), mContactId);
        
        contact.nickname = CONTACT_NICKNAME;
        
        List<PhoneInfo> phoneInfos = contact.phoneInfos;
        PhoneInfo phoneInfo = new PhoneInfo();
        phoneInfo.type = CommonDataKinds.Phone.TYPE_HOME;
        final String newPhoneNumber = "0832-3993098";
        phoneInfo.number = newPhoneNumber;
        phoneInfo.modifyFlag = ModifyTag.add;
        phoneInfos.add(phoneInfo);
        
        List<EmailInfo> emailInfos = contact.emailInfos;
        EmailInfo emailInfo = new EmailInfo();
        emailInfo.type = CommonDataKinds.Email.TYPE_HOME;
        emailInfo.address = "shaobin0604@qq.com";
        emailInfo.modifyFlag = ModifyTag.add;
        emailInfos.add(emailInfo);
        
        List<ImInfo> imInfos = contact.imInfos;
        ImInfo imInfo = new ImInfo();
        imInfo.protocol = CommonDataKinds.Im.PROTOCOL_QQ;
        imInfo.account = "407403384";
        imInfo.modifyFlag = ModifyTag.add;
        imInfos.add(imInfo);
        
        Queue<Long> newDataIds = ContactUtil.updateContact(getContext(), contact);
        
        Slog.d(newDataIds.toString());
        
        contact = ContactUtil.getContactById(getContext(), mContactId);
        
        Slog.d(contact.toString());
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
        
        boolean success = ContactUtil.updateContact(getContext(), contact) != null;
        
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
        
        boolean success = ContactUtil.updateContact(getContext(), contact) != null;
        
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
        imInfo.protocol = CommonDataKinds.Im.TYPE_HOME;
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
