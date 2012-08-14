
package com.pekall.pctool.model.contact;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;

import com.pekall.pctool.model.contact.Contact.AddressRecord;
import com.pekall.pctool.model.contact.Contact.EmailRecord;
import com.pekall.pctool.model.contact.Contact.IMRecord;
import com.pekall.pctool.model.contact.Contact.OrgRecord;
import com.pekall.pctool.model.contact.Contact.PhoneRecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * the helper class helps to do with the contact
 * 
 * @author fql
 */
public class ContactUtil {

    public static final String DEFAULT_ACCOUNT_NAME = "contacts.account.name.local";
    public static final String DEFAULT_ACCOUNT_TYPE = "contacts.account.type.local";
    public static final String SIM1_ACCOUNT_NAME = "contacts.account.name.sim1";
    public static final String SIM1_ACCOUNT_TYPE = "contacts.account.type.sim";
    public static final String SIM2_ACCOUNT_NAME = "contacts.account.name.sim2";
    public static final String SIM2_ACCOUNT_TYPE = "contacts.account.type.sim";
    public static final int CONTACTS_COLLECT_FLAG = 1;
    public static final int Custom_DEFINE_TYPE = 0;

    /**
     * This utility class cannot be instantiated
     */
    private ContactUtil() {
    }

    /**
     * get Single Contact by rawContactId
     * 
     * @param context rawId return Contact
     */
    public static Contact getContactByRawId(Context context, long rawId) {
        Contact c = new Contact();
        String rawContactId = String.valueOf(rawId);
        c._ID = Integer.parseInt(String.valueOf(rawId));
        Uri rawContactUri = RawContacts.CONTENT_URI;
        ContentResolver resolver = context.getContentResolver();
        
        
        Cursor cursorOfAccount = resolver.query(rawContactUri, new String[] {
                RawContacts.ACCOUNT_NAME, RawContacts.ACCOUNT_TYPE
        }, RawContacts._ID + "=?", new String[] {
                rawContactId
        }, null);
        while (cursorOfAccount.moveToNext()) {
            c.accountInfo.accountName = cursorOfAccount.getString(cursorOfAccount
                    .getColumnIndex(RawContacts.ACCOUNT_NAME));
            c.accountInfo.accountType = cursorOfAccount.getString(cursorOfAccount
                    .getColumnIndex(RawContacts.ACCOUNT_TYPE));
        }
        cursorOfAccount.close();
        
        // get group
        Cursor cursorOfGroup = context.getContentResolver().query(Data.CONTENT_URI, new String[] {
                Data.DATA1
        }, Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + "=?", new String[] {
                rawContactId, GroupMembership.CONTENT_ITEM_TYPE
        }, null);
        while (cursorOfGroup.moveToNext()) {
            c.groupInfo.grId = cursorOfGroup.getLong(cursorOfGroup.getColumnIndex(Data.DATA1));
            break;
        }
        cursorOfGroup.close();
        
        
        Cursor cursorOfGroupInfo = context.getContentResolver().query(Groups.CONTENT_URI, new String[] {
                Groups.TITLE, Groups.NOTES
        }, Groups._ID + " = ?  ", new String[] {
                String.valueOf(c.groupInfo.grId)
        }, null);
        while (cursorOfGroupInfo.moveToNext()) {
            c.groupInfo.name = cursorOfGroupInfo.getString(cursorOfGroupInfo.getColumnIndex(Groups.TITLE));
            c.groupInfo.note = convertToString(cursorOfGroupInfo.getString(cursorOfGroupInfo
                    .getColumnIndex(Groups.NOTES)));
            break;
        }
        cursorOfGroupInfo.close();
        
        
        // get the contact name
        Cursor cursorOfName = context.getContentResolver().query(Data.CONTENT_URI, new String[] {
                Data.DISPLAY_NAME
                // --------->data1
        }, Data.RAW_CONTACT_ID + " = ? ", new String[] {
                rawContactId
        }, null);
        while (cursorOfName.moveToNext()) {
            c.name = cursorOfName.getString(cursorOfName.getColumnIndex(Data.DISPLAY_NAME));
            // we only need one name for a contact
            break;
        }
        cursorOfName.close();
        
        
        // nick name
        Cursor cursorOfNickName = context.getContentResolver().query(Data.CONTENT_URI, new String[] {
                Data.DATA1
                // --------->data1
        }, Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ? ", new String[] {
                rawContactId, Nickname.TYPE
        }, null);
        while (cursorOfNickName.moveToNext()) {
            c.nickname = cursorOfName.getString(cursorOfName.getColumnIndex(Data.DATA1));
            // we only need one name for a contact
            break;
        }
        cursorOfNickName.close();
        
        
        // phone Number
        Cursor cursorOfPhone = context.getContentResolver().query(Data.CONTENT_URI, new String[] {
                Phone.NUMBER,// ----data1
                Phone.TYPE, Phone.DATA3
                // -----custom data name
        }, Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ? ", new String[] {
                rawContactId, Phone.CONTENT_ITEM_TYPE,
        }, null);
        while (cursorOfPhone.moveToNext()) {
            PhoneRecord pr = new PhoneRecord();
            pr.type = cursorOfPhone.getLong(cursorOfPhone.getColumnIndex(Phone.TYPE));
            pr.number = cursorOfPhone.getString(cursorOfPhone.getColumnIndex(Phone.NUMBER));
            if (pr.type == Custom_DEFINE_TYPE) {
                pr.customName = cursorOfPhone.getString(cursorOfPhone.getColumnIndex(Phone.DATA3));
            }
            c.phoneRecord.add(pr);
        }
        cursorOfPhone.close();

        
        // email
        Cursor cursorOfEmail = context.getContentResolver().query(Data.CONTENT_URI, new String[] {
                Email.ADDRESS,// data1
                Email.TYPE, Email.DATA3
        }, Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ? ", new String[] {
                rawContactId, Email.CONTENT_ITEM_TYPE,
        }, null);
        while (cursorOfEmail.moveToNext()) {
            EmailRecord er = new EmailRecord();
            er.type = cursorOfEmail.getLong(cursorOfEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
            er.email = cursorOfEmail.getString(cursorOfEmail.getColumnIndex(Email.ADDRESS));
            if (er.type == Custom_DEFINE_TYPE) {
                er.customName = cursorOfEmail.getString(cursorOfEmail.getColumnIndex(Email.DATA3));
            }
            c.emailRecord.add(er);
        }
        cursorOfEmail.close();
        
        
        // im
        Cursor cursorOfIm = context.getContentResolver().query(Data.CONTENT_URI, // 查询data表
                new String[] {
                        Im.PROTOCOL, // --->data5(type)
                        Data.DATA1, Data.DATA3
                // IM IM.type means home work and so on
                }, Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ? ", new String[] {
                        rawContactId, Im.CONTENT_ITEM_TYPE,
                }, null);
        while (cursorOfIm.moveToNext()) {
            IMRecord ir = new IMRecord();
            ir.type = cursorOfIm.getLong(cursorOfIm.getColumnIndex(Im.PROTOCOL));
            ir.im = cursorOfIm.getString(cursorOfIm.getColumnIndex(Data.DATA1));
            if (ir.type == Custom_DEFINE_TYPE) {
                ir.customName = cursorOfIm.getString(cursorOfIm.getColumnIndex(Data.DATA3));
            }
            c.imRecord.add(ir);
        }
        cursorOfIm.close();
        
        
        // address
        Cursor cursorOfAddress = context.getContentResolver().query(Data.CONTENT_URI, new String[] {
                StructuredPostal.FORMATTED_ADDRESS, StructuredPostal.TYPE, Data.DATA3
        }, Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ? ", new String[] {
                rawContactId, StructuredPostal.CONTENT_ITEM_TYPE,
        }, null);
        while (cursorOfAddress.moveToNext()) {
            AddressRecord ar = new AddressRecord();
            ar.type = cursorOfAddress.getLong(cursorOfAddress
                    .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
            ar.address = cursorOfAddress.getString(cursorOfAddress.getColumnIndex(StructuredPostal.FORMATTED_ADDRESS));
            ar.address = convertToString(ar.address);
            if (ar.type == Custom_DEFINE_TYPE) {
                ar.customName = cursorOfAddress.getString(cursorOfAddress.getColumnIndex(Data.DATA3));
            }
            c.addressRecord.add(ar);
        }
        cursorOfAddress.close();
        
        
        // organization
        Cursor cursorOfOrganization = context.getContentResolver().query(Data.CONTENT_URI, new String[] {
                Organization.COMPANY, Organization.TYPE, Data.DATA3
        }, Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ? ", new String[] {
                rawContactId, Organization.CONTENT_ITEM_TYPE,
        }, null);
        while (cursorOfOrganization.moveToNext()) {
            OrgRecord or = new OrgRecord();
            or.type = cursorOfOrganization.getLong(cursorOfOrganization.getColumnIndex(Organization.TYPE));
            or.org = convertToString(cursorOfOrganization.getString(cursorOfOrganization
                    .getColumnIndex(Organization.COMPANY)));
            if (or.type == Custom_DEFINE_TYPE) {
                or.customName = cursorOfOrganization.getString(cursorOfOrganization.getColumnIndex(Data.DATA3));
            }
            c.orgRecord.add(or);
        }
        cursorOfOrganization.close();
        
        
        return c;
    }

    /**
     * get all contacts no matter the phone or the SIM card it includes all
     * accounts's contacts
     * 
     * @param context
     * @return
     */
    public static List<Contact> getAllContacts(Context context) {
        List<Contact> l = new ArrayList<Contact>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, new String[] {
                RawContacts._ID
        }, null, null, null);
        while (cursor.moveToNext()) {
            long rawContactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
            l.add(getContactByRawId(context, rawContactId));
        }
        return l;
    }

    /**
     * get some contacts of one account
     * 
     * @param context
     * @param accountName
     * @param accountType
     * @return
     */
    public static List<Contact> getAccountContacts(Context context, AccountInfo account) {
        List<Contact> l = new ArrayList<Contact>();
        if (account == null)
            return l;
        String where = ContactsContract.RawContacts.ACCOUNT_NAME + "=?" + " and "
                + ContactsContract.RawContacts.ACCOUNT_TYPE + "=?";
        Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, where,
                new String[] {
                        account.accountName, account.accountType
                }, null);
        while (cursor.moveToNext()) {
            long rawContactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
            l.add(getContactByRawId(context, rawContactId));
        }
        return l;
    }

    /**
     * get all accounts of phone we need to show the account.name
     * 
     * @param context
     * @return
     */
    public static List<AccountInfo> getAllAccounts(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccounts();
        List<AccountInfo> ls = new ArrayList<AccountInfo>();
        for (int i = 0; i < accounts.length; i++) {
            AccountInfo ai = new AccountInfo();
            ai.accountName = accounts[i].name;
            ai.accountType = accounts[i].type;
            ls.add(ai);
        }
        AccountInfo ai = new AccountInfo();
        ai.accountName = DEFAULT_ACCOUNT_NAME;
        ai.accountType = DEFAULT_ACCOUNT_TYPE;
        ls.add(ai);
        if (getSimCardState(context) == 1) {
            ai = new AccountInfo();
            ai.accountName = SIM1_ACCOUNT_NAME;
            ai.accountType = SIM1_ACCOUNT_TYPE;
            ls.add(ai);
        } else if (getSimCardState(context) == 2) {
            ai = new AccountInfo();
            ai.accountName = SIM2_ACCOUNT_NAME;
            ai.accountType = SIM2_ACCOUNT_TYPE;
            ls.add(ai);
        } else if (getSimCardState(context) == 3) {
            ai = new AccountInfo();
            ai.accountName = SIM1_ACCOUNT_NAME;
            ai.accountType = SIM1_ACCOUNT_TYPE;
            ls.add(ai);
            ai = new AccountInfo();
            ai.accountName = SIM2_ACCOUNT_NAME;
            ai.accountType = SIM2_ACCOUNT_TYPE;
            ls.add(ai);
        }
        return ls;
    }
    
    public static final byte SIM1_AVAILABLE = 0x00000001;
    public static final byte SIM2_AVAILABLE = 0x00000002;

    /**
     * 
     * 
     * @param context
     * @return
     */
    public static byte getSimCardState(Context context) {
        byte simState = 0;
        
        // sim1
        String where = ContactsContract.RawContacts.ACCOUNT_NAME + "=?" + " and "
                + ContactsContract.RawContacts.ACCOUNT_TYPE + "=?";
        Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, where,
                new String[] {
                        SIM1_ACCOUNT_NAME, SIM1_ACCOUNT_TYPE
                }, null);
        if (cursor.getCount() > 0) {
            simState |= SIM1_AVAILABLE;
        }
        cursor.close();
        
        // sim2
        cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, where,
                new String[] {
                        SIM2_ACCOUNT_NAME, SIM2_ACCOUNT_TYPE
                }, null);
        if (cursor.getCount() > 0) {
            simState |= SIM2_AVAILABLE;
        }
        cursor.close();
        
        return simState;
    }

    /**
     * delete a contact if the contact has the same name
     * 
     * @param name context
     */

    public static boolean delContact(Context context, String name) {
        Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, new String[] {
                Data.RAW_CONTACT_ID
        }, ContactsContract.Contacts.DISPLAY_NAME + "=?", new String[] {
                name
        }, null);
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        if (cursor.moveToFirst()) {
            do {
                long Id = cursor.getLong(cursor.getColumnIndex(Data.RAW_CONTACT_ID));
                ops.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(RawContacts.CONTENT_URI, Id))
                        .build());
                try {
                    context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                } catch (Exception e) {
                    return false;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return true;
    }

    /**
     * delete a contact by rawId
     * 
     * @param context ,id
     */
    public static boolean delContact(Context context, long rawId) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawId)).build());
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            return false;
        }
        return true;

    }

    /**
     * delete some contact by rawId[]
     * 
     * @param context rawId[]
     */
    public static boolean delContact(Context context, long rawId[]) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        for (int i = 0; i < rawId.length; i++) {
            ops.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawId[i]))
                    .build());
        }
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * add a group to Contact if the contact has no group
     */
    public static boolean addGroupToContact(Context context, long grId, long rawId) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(Data.DATA1, grId);
            cv.put(Data.RAW_CONTACT_ID, rawId);
            cv.put(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
            context.getContentResolver().insert(Data.CONTENT_URI, cv);
        } catch (Exception e) {
            return false;
        }
        return true;

    }

    /**
     * update contact
     * 
     * @param context
     * @param c
     * @return
     */
    public static boolean updateContact(Context context, Contact c) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        PhoneRecord pr = new PhoneRecord();
        EmailRecord er = new EmailRecord();
        OrgRecord or = new OrgRecord();
        IMRecord ir = new IMRecord();
        AddressRecord ar = new AddressRecord();

        if (c.accountInfo == null) {
            c.accountInfo = new AccountInfo();
            c.accountInfo.accountName = DEFAULT_ACCOUNT_NAME;
            c.accountInfo.accountType = DEFAULT_ACCOUNT_TYPE;
        }

        // TODO: 判断顺序
        if (!c.groupInfo.name.equals("") && c.groupInfo.name != null) {
            // has group
            if (getGroupState(context, c._ID)) {
                // update group
                ops.add(ContentProviderOperation
                        .newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(Data.RAW_CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?",
                                new String[] {
                                        String.valueOf(c._ID), GroupMembership.CONTENT_ITEM_TYPE
                                }).withValue(Data.DATA1, c.groupInfo.grId).build());
            } else {
                if (!addGroupToContact(context, c.groupInfo.grId, c._ID)) {
                    return false;
                }
            }
        }
        // update account
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(RawContacts._ID + "=?", new String[] {
                        String.valueOf(c._ID)
                }).withValue(RawContacts.ACCOUNT_NAME, c.accountInfo.accountName)
                .withValue(RawContacts.ACCOUNT_TYPE, c.accountInfo.accountType).build());
        // update nickname
        ops.add(ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(Data.RAW_CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?",
                        new String[] {
                                String.valueOf(c._ID), Nickname.CONTENT_ITEM_TYPE
                        }).withValue(Nickname.NAME, c.nickname).build());
        // update Phone
        for (Iterator<PhoneRecord> iter = c.phoneRecord.iterator(); iter.hasNext();) {
            pr = iter.next();
            ops.add(ContentProviderOperation
                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                            Data.RAW_CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?" + " AND "
                                    + Phone.TYPE + "=?", new String[] {
                                    String.valueOf(c._ID), Phone.CONTENT_ITEM_TYPE, String.valueOf(pr.type)
                            }).withValue(Phone.NUMBER, pr.number).build());
        }
        // update Email
        for (Iterator<EmailRecord> iter = c.emailRecord.iterator(); iter.hasNext();) {
            er = iter.next();
            ops.add(ContentProviderOperation
                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                            Data.RAW_CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?" + " AND "
                                    + Email.TYPE + "=?", new String[] {
                                    String.valueOf(c._ID), Email.CONTENT_ITEM_TYPE, String.valueOf(er.type)
                            }).withValue(Email.ADDRESS, er.email).withValue(Email.DATA3, er.customName).build());
        }
        // update Name
        ops.add(ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(Data.RAW_CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?",
                        new String[] {
                                String.valueOf(c._ID), StructuredName.CONTENT_ITEM_TYPE
                        }).withValue(StructuredName.DISPLAY_NAME, c.name).build());
        // update Organization
        for (Iterator<OrgRecord> iter = c.orgRecord.iterator(); iter.hasNext();) {
            or = iter.next();
            ops.add(ContentProviderOperation
                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                            Data.RAW_CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?" + " AND "
                                    + Organization.TYPE + "=?", new String[] {
                                    String.valueOf(c._ID), Organization.CONTENT_ITEM_TYPE, String.valueOf(or.type)
                            }).withValue(Organization.COMPANY, or.org).build());
        }
        // update Address
        for (Iterator<AddressRecord> iter = c.addressRecord.iterator(); iter.hasNext();) {
            ar = iter.next();
            ops.add(ContentProviderOperation
                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                            Data.RAW_CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?" + " AND "
                                    + StructuredPostal.TYPE + "=?", new String[] {
                                    String.valueOf(c._ID), StructuredPostal.CONTENT_ITEM_TYPE, String.valueOf(ar.type)
                            }).withValue(StructuredPostal.FORMATTED_ADDRESS, ar.address).build());
        }
        // update InfoM
        for (Iterator<IMRecord> iter = c.imRecord.iterator(); iter.hasNext();) {
            ir = iter.next();
            ops.add(ContentProviderOperation
                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                            Data.RAW_CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?" + " AND "
                                    + Im.TYPE + "=?", new String[] {
                                    String.valueOf(c._ID), Im.CONTENT_ITEM_TYPE, String.valueOf(ir.type)
                            }).withValue(Im.DATA1, ir.im).build());
        }
        
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return true;
    }

    /**
     * add a contact b
     * 
     * @param context
     * @param c
     * @param AccountInfo
     * @return
     */
    public static boolean addContact(Context context, Contact c, AccountInfo ac, GroupInfo gr) {

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        if (ac == null) {
            ac = new AccountInfo();
            ac.accountName = DEFAULT_ACCOUNT_NAME;
            ac.accountType = DEFAULT_ACCOUNT_TYPE;
        }

        // rawcontacts'account
        // don't give c._Id value because it is automaticly increased
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, ac.accountType)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, ac.accountName)
                .withValue(ContactsContract.RawContacts.AGGREGATION_MODE,
                        ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED).build());
        // add group
        if (gr != null) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, c._ID)
                    .withValue(ContactsContract.Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE)
                    .withValue(Data.DATA1, gr.grId).build());
        }
        // name
        if (!c.name.equals("")) {
            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, c._ID)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, c.name).build());
        }
        // Organization
        if (c.orgRecord.size() > 0) {
            for (Iterator<OrgRecord> iter = c.orgRecord.iterator(); iter.hasNext();) {
                OrgRecord or = iter.next();
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, c._ID)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, or.org)
                        .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, or.type)
                        .withValue(ContactsContract.CommonDataKinds.Organization.DATA3, or.customName).build());
            }
        }
        // phone number
        if (c.phoneRecord.size() > 0) {
            for (Iterator<PhoneRecord> iter = c.phoneRecord.iterator(); iter.hasNext();) {
                PhoneRecord pr = iter.next();
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, c._ID)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, pr.number)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, pr.type)
                        .withValue(ContactsContract.CommonDataKinds.Phone.DATA3, pr.customName).build());
            }
        }
        // email
        if (c.emailRecord.size() > 0) {
            for (Iterator<EmailRecord> iter = c.emailRecord.iterator(); iter.hasNext();) {
                EmailRecord er = iter.next();
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, c._ID)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, er.email)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, er.type)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA3, er.customName).build());
            }
        }
        // address
        if (c.addressRecord.size() > 0) {
            for (Iterator<AddressRecord> iter = c.addressRecord.iterator(); iter.hasNext();) {
                AddressRecord ar = iter.next();
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, c._ID)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ar.type)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, ar.address)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.DATA3, ar.customName).build());
            }
        }
        // IM
        if (c.imRecord.size() > 0) {
            for (Iterator<IMRecord> iter = c.imRecord.iterator(); iter.hasNext();) {
                IMRecord ir = iter.next();
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, c._ID)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Im.DATA1, ir.im)
                        .withValue(ContactsContract.CommonDataKinds.Im.PROTOCOL, ir.type)
                        .withValue(ContactsContract.CommonDataKinds.Im.DATA3, ir.customName).build());
            }
        }

        // nick name
        if (c.nickname != null && !"".equals(c.nickname)) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, c._ID)
                    .withValue(ContactsContract.Data.MIMETYPE, Nickname.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Nickname.NAME, c.nickname).build());
        }
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * get all group's detail infomation to one account
     * 
     * @param context
     */

    public static List<GroupInfo> queryGroup(Context context, AccountInfo ai) {
        Uri uri = ContactsContract.Groups.CONTENT_URI;
        if (ai == null) {
            ai = new AccountInfo();
            ai.accountName = DEFAULT_ACCOUNT_NAME;
            ai.accountType = DEFAULT_ACCOUNT_TYPE;
        }
        String where = ContactsContract.Groups.ACCOUNT_NAME + "=?" + " and " + ContactsContract.Groups.ACCOUNT_TYPE
                + "=?";
        Cursor mr = context.getContentResolver().query(uri, null, where, new String[] {
                ai.accountName, ai.accountType
        }, null);
        List<GroupInfo> ls = new ArrayList<GroupInfo>();
        while (mr.moveToNext()) {
            GroupInfo gi = new GroupInfo();
            gi.grId = mr.getLong(mr.getColumnIndex(ContactsContract.Groups._ID));
            gi.name = mr.getString(mr.getColumnIndex(ContactsContract.Groups.TITLE));
            gi.accountInfo.accountType = mr.getString(mr.getColumnIndex(ContactsContract.Groups.ACCOUNT_TYPE));
            gi.accountInfo.accountName = mr.getString(mr.getColumnIndex(ContactsContract.Groups.ACCOUNT_NAME));
            ls.add(gi);
        }
        return ls;
    }

    /**
     * get all contacts from one account's one group
     * 
     * @param context
     * @param gi
     * @return
     */
    public static List<Contact> getContactsByGroup(Context context, GroupInfo gi) {
        List<Contact> ls = new ArrayList<Contact>();
        if (gi == null)
            gi = new GroupInfo();
        String[] RAW_PROJECTION = new String[] {
                Data.RAW_CONTACT_ID,
        };
        String RAW_CONTACTS_WHERE = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=?" + " and "
                + ContactsContract.Data.MIMETYPE + "=" + "'"
                + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
        Cursor mr = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, RAW_PROJECTION,
                RAW_CONTACTS_WHERE, new String[] {
                    String.valueOf(gi.grId)
                }, null);
        while (mr.moveToNext()) {
            long rawId = mr.getLong(mr.getColumnIndex(Data.RAW_CONTACT_ID));
            ls.add(getContactByRawId(context, rawId));
        }
        return ls;
    }

    /**
     * get all Favorite contacts
     * 
     * @param context
     * @return
     */
    public static List<Contact> getFavoriteContacts(Context context) {
        List<Contact> l = new ArrayList<Contact>();
        String where = RawContacts.STARRED + "=?";
        Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, new String[] {
                RawContacts._ID
        }, where, new String[] {
                String.valueOf(CONTACTS_COLLECT_FLAG)
        }, null);
        while (cursor.moveToNext()) {
            long rawContactId = cursor.getLong(cursor.getColumnIndex(RawContacts._ID));
            l.add(getContactByRawId(context, rawContactId));
        }
        return l;

    }

    /**
     * @param context
     * @return
     */
    public static boolean getGroupState(Context context, long rawId) {
        Cursor cr = context.getContentResolver().query(Data.CONTENT_URI, new String[] {
                Data.DATA1
        }, Data.RAW_CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[] {
                String.valueOf(rawId), GroupMembership.CONTENT_ITEM_TYPE
        }, null);

        int k = cr.getCount();
        cr.close();
        if (k > 0) {
            return true;
        }
        return false;

    }

    /**
     * @param obj
     * @return
     */
    public static String convertToString(Object obj) {
        if (obj == null)
            return "";
        return obj.toString();
    }
}
