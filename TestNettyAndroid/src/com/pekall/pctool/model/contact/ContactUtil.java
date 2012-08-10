
package com.pekall.pctool.model.contact;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        // get the contact name
        Cursor cursorOfName = context.getContentResolver().query(Data.CONTENT_URI, new String[] {
            Data.DISPLAY_NAME
        // --------->data1
                }, Data.RAW_CONTACT_ID + " = ? ", new String[] {
                    rawContactId
                }, null);
        while (cursorOfName.moveToNext()) {
            c.Name = cursorOfName.getString(cursorOfName.getColumnIndex(Data.DISPLAY_NAME));
            // we only need one name for a contact
            System.out.println("解析出名字：" + c.Name);
            break;
        }
        cursorOfName.close();
        // phone Number
        Cursor cursorOfPhone = context.getContentResolver().query(Data.CONTENT_URI, new String[] {
                Phone.NUMBER,// ----data1
                Phone.TYPE,
        }, Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ? ", new String[] {
                rawContactId, Phone.CONTENT_ITEM_TYPE,
        }, null);
        while (cursorOfPhone.moveToNext()) {
            Map<String, String> m = new HashMap<String, String>();
            String phoneType = cursorOfPhone.getString(cursorOfPhone.getColumnIndex(Phone.TYPE));
            String phone = cursorOfPhone.getString(cursorOfPhone.getColumnIndex(Phone.NUMBER));
            m.put(Phone.TYPE, phoneType);
            m.put(Phone.NUMBER, phone);
            System.out.println("电话類型:" + phoneType);
            System.out.println("电话:" + phone);
            c.Number.add(m);
        }
        cursorOfPhone.close();
        // email
        Cursor cursorOfEmail = context.getContentResolver().query(Data.CONTENT_URI, new String[] {
                Email.ADDRESS,// data1
                Email.TYPE
        }, Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ? ", new String[] {
                rawContactId, Email.CONTENT_ITEM_TYPE,
        }, null);
        while (cursorOfEmail.moveToNext()) {
            Map<String, String> m = new HashMap<String, String>();
            String emailType = cursorOfEmail.getString(cursorOfEmail
                    .getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
            String email = cursorOfEmail.getString(cursorOfEmail.getColumnIndex(Email.DATA1));
            m.put(Email.TYPE, emailType);
            m.put(Email.ADDRESS, email);
            System.out.println("邮件:" + email);
            System.out.println("邮件類型:" + emailType);
            c.Email.add(m);
        }
        cursorOfEmail.close();
        // im
        Cursor cursorOfIm = context.getContentResolver().query(Data.CONTENT_URI, // 查询data表
                new String[] {
                        Im.PROTOCOL, // --->data5(type)
                        Data.DATA1
                // IM IM.type means home work and so on
                }, Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ? ", new String[] {
                        rawContactId, Im.CONTENT_ITEM_TYPE,
                }, null);
        while (cursorOfIm.moveToNext()) {
            Map<String, String> m = new HashMap<String, String>();
            String imType = cursorOfIm.getString(cursorOfIm.getColumnIndex(Im.PROTOCOL));
            String im = cursorOfIm.getString(cursorOfIm.getColumnIndex(Data.DATA1));
            m.put(Im.DATA, im);
            m.put(Im.TYPE, imType);
            System.out.print("即时消息类型:" + imType + "----->");
            System.out.println("即时消息:" + im);
            c.Exchange.add(m);
        }
        cursorOfIm.close();
        // address
        Cursor cursorOfAddress = context.getContentResolver().query(
                Data.CONTENT_URI,
                new String[] {
                        StructuredPostal.TYPE, StructuredPostal.CITY, StructuredPostal.STREET,
                        StructuredPostal.POSTCODE, StructuredPostal.REGION
                }, Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ? ", new String[] {
                        rawContactId, StructuredPostal.CONTENT_ITEM_TYPE,
                }, null);
        while (cursorOfAddress.moveToNext()) {
            Map<String, AddressInfo> m = new HashMap<String, AddressInfo>();
            AddressInfo ai = new AddressInfo();
            String addressType = cursorOfAddress.getString(cursorOfAddress
                    .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
            String region = cursorOfAddress.getString(cursorOfAddress.getColumnIndex(StructuredPostal.REGION));
            String street = cursorOfAddress.getString(cursorOfAddress.getColumnIndex(StructuredPostal.STREET));
            String city = cursorOfAddress.getString(cursorOfAddress.getColumnIndex(StructuredPostal.CITY));
            String postcode = cursorOfAddress.getString(cursorOfAddress.getColumnIndex(StructuredPostal.POSTCODE));
            if (region == null || street == null || city == null || postcode == null) {
                region = "";
                street = "";
                city = "";
                postcode = "";
            }
            ai.city = city;
            ai.province = region;
            ai.postCode = postcode;
            ai.street = street;
            ai.type = addressType;
            m.put(StructuredPostal.TYPE, ai);
            System.out.println("省:" + region);
            System.out.println("城市" + city);
            System.out.println("接到:" + street);
            System.out.println("邮编:" + postcode);
            c.Address.add(m);
        }
        cursorOfAddress.close();
        // organization
        Cursor cursorOfOrganization = context.getContentResolver().query(Data.CONTENT_URI, new String[] {
                Organization.COMPANY, Organization.TITLE, Organization.TYPE
        }, Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ? ", new String[] {
                rawContactId, Organization.CONTENT_ITEM_TYPE,
        }, null);
        while (cursorOfOrganization.moveToNext()) {
            Map<String, String> m = new HashMap<String, String>();
            String company = cursorOfOrganization.getString(cursorOfOrganization.getColumnIndex(Organization.COMPANY));
            String title = cursorOfOrganization.getString(cursorOfOrganization.getColumnIndex(Organization.TITLE));
            m.put(Organization.COMPANY, company);
            m.put(Organization.TITLE, title);
            System.out.print("组织:" + company + "------>");
            System.out.println("职位:" + title);
            c.Organization.add(m);
        }
        cursorOfOrganization.close();
        // comments
        Cursor cursorOfNote = context.getContentResolver().query(Data.CONTENT_URI, new String[] {
            Note.NOTE,
        }, Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ? ", new String[] {
                rawContactId, Note.CONTENT_ITEM_TYPE,
        }, null);
        while (cursorOfNote.moveToNext()) {
            String note = cursorOfNote.getString(cursorOfNote.getColumnIndex(Note.NOTE));
            if (null != note && !"".equals(note)) {
                System.out.println("备注:" + note);
                c.Comment = note;
            }
        }
        cursorOfNote.close();
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
        Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null,
                null);
        System.out.println("--------totlal count------------->" + cursor.getCount());
        while (cursor.moveToNext()) {
            long rawContactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
            l.add(getContactByRawId(context, rawContactId));
        }
        System.out.println("---------------l.size------------------------------------" + l.size());
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

    /**
     * judge whther it has SIMCard judge SIMcard 1 or SIMcard 2 or both return 1
     * SIMcard1 has return 2 SIMcard2 has return 3 both has return 0 none
     * 
     * @param context
     * @return
     */
    public static int getSimCardState(Context context) {
        int count = 0;
        String where = ContactsContract.RawContacts.ACCOUNT_NAME + "=?" + " and "
                + ContactsContract.RawContacts.ACCOUNT_TYPE + "=?";
        Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, where,
                new String[] {
                        SIM1_ACCOUNT_NAME, SIM1_ACCOUNT_TYPE
                }, null);
        if (cursor.getCount() > 0)
            count++;
        cursor.close();
        cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, where,
                new String[] {
                        SIM2_ACCOUNT_NAME, SIM2_ACCOUNT_TYPE
                }, null);
        if (cursor.getCount() > 0)
            count += 2;
        cursor.close();
        return count;
    }

    /**
     * print all contacts only for test
     * 
     * @param List<contact>
     */

    public static void print(List<Contact> l) {
        for (Iterator<Contact> iter = l.iterator(); iter.hasNext();) {
            Contact c = iter.next();
            System.out.print("联系人姓名" + c.Name);
            for (Iterator<Map<String, String>> numberIter = c.Number.iterator(); numberIter.hasNext();) {
                Map<String, String> m = numberIter.next();
                if (Integer.parseInt(m.get(Phone.TYPE)) == Phone.TYPE_HOME) {
                    System.out.print("   电话类型" + "家庭电话   ");
                }
                if (Integer.parseInt(m.get(Phone.TYPE)) == Phone.TYPE_OTHER) {
                    System.out.print("   电话类型" + "其他电话  ");
                }
                if (Integer.parseInt(m.get(Phone.TYPE)) == Phone.TYPE_MOBILE) {
                    System.out.println("  电话类型" + "个人电话  ");
                }
                if (Integer.parseInt(m.get(Phone.TYPE)) == Phone.TYPE_OTHER_FAX) {
                    System.out.print("   电话类型" + " 传真电话  ");
                }
                if (Integer.parseInt(m.get(Phone.TYPE)) == Phone.TYPE_WORK) {
                    System.out.print("  电话类型" + "  工作电话  ");
                }
                System.out.println("电话号码" + m.get(Phone.NUMBER));
            }
            for (Iterator<Map<String, String>> emailIter = c.Email.iterator(); emailIter.hasNext();) {
                Map<String, String> m = emailIter.next();
                if (Integer.parseInt(m.get(Email.TYPE)) == Email.TYPE_HOME) {
                    System.out.print("邮件类型" + "家庭邮件");
                }
                if (Integer.parseInt(m.get(Email.TYPE)) == Email.TYPE_OTHER) {
                    System.out.print("邮件类型" + "其他邮件");
                }
                if (Integer.parseInt(m.get(Email.TYPE)) == Email.TYPE_WORK) {
                    System.out.print("邮件类型" + "工作邮箱");
                }
                System.out.println("邮箱地址" + m.get(Email.ADDRESS));
            }
            for (Iterator<Map<String, AddressInfo>> addressIter = c.Address.iterator(); addressIter.hasNext();) {
                Map<String, AddressInfo> m = addressIter.next();
                if (Integer.parseInt(m.get(StructuredPostal.TYPE).type) == StructuredPostal.TYPE_HOME) {
                    System.out.print("地址类型" + "家庭地址");
                }
                if (Integer.parseInt(m.get(StructuredPostal.TYPE).type) == StructuredPostal.TYPE_OTHER) {
                    System.out.print("地址类型" + "其他地址");
                }
                if (Integer.parseInt(m.get(StructuredPostal.TYPE).type) == StructuredPostal.TYPE_WORK) {
                    System.out.print("地址类型" + "工作地址");
                }
                System.out.print("省--直辖市" + m.get(StructuredPostal.TYPE).province);
                System.out.print("城市" + m.get(StructuredPostal.TYPE).city);
                System.out.print("街道" + m.get(StructuredPostal.TYPE).street);
                System.out.print("编码" + m.get(StructuredPostal.TYPE).postCode);
                System.out.println();
            }
            for (Iterator<Map<String, String>> imIter = c.Exchange.iterator(); imIter.hasNext();) {
                Map<String, String> m = imIter.next();
                if (Integer.parseInt(m.get(Im.TYPE)) == Im.PROTOCOL_AIM) {
                    System.out.print("通讯工具类型" + "AIM");
                }
                if (Integer.parseInt(m.get(Im.TYPE)) == Im.PROTOCOL_GOOGLE_TALK) {
                    System.out.print("通讯工具类型" + "PROTOCOL_GOOGLE_TALK");
                }
                if (Integer.parseInt(m.get(Im.TYPE)) == Im.PROTOCOL_JABBER) {
                    System.out.print("通讯工具类型" + "PROTOCOL_JABBER");
                }
                if (Integer.parseInt(m.get(Im.TYPE)) == Im.PROTOCOL_QQ) {
                    System.out.print("通讯工具类型" + "QQ");
                }
                if (Integer.parseInt(m.get(Im.TYPE)) == Im.PROTOCOL_MSN) {
                    System.out.print("通讯工具类型" + "MSN");
                }
                if (Integer.parseInt(m.get(Im.TYPE)) == Im.PROTOCOL_YAHOO) {
                    System.out.print("通讯工具类型" + "PROTOCOL_YAHOO");
                }
                if (Integer.parseInt(m.get(Im.TYPE)) == Im.PROTOCOL_ICQ) {
                    System.out.print("通讯工具类型" + "PROTOCOL_ICQ");
                }
                if (Integer.parseInt(m.get(Im.TYPE)) == Im.PROTOCOL_SKYPE) {
                    System.out.print("通讯工具类型" + "PROTOCOL_SKYPE");
                }
                System.out.println("通讯工具号码" + m.get(Im.DATA));
            }
            for (Iterator<Map<String, String>> organIter = c.Email.iterator(); organIter.hasNext();) {
                Map<String, String> m = organIter.next();
                System.out.print("公司" + m.get(Organization.COMPANY));
                System.out.print("职位" + m.get(Organization.TITLE));
                System.out.println();
            }
            if (c.Comment != null)
                System.out.print("备注" + c.Comment);
            System.out.println("");
        }
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
     * update contact
     * 
     * @param context
     * @param c
     * @return
     */
    public static boolean updateContact(Context context, Contact c) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        Map<String, String> m = new HashMap<String, String>();
        Map<String, AddressInfo> m1 = new HashMap<String, AddressInfo>();

        // update Phone
        if (c.Number.size() > 0) {
            for (Iterator<Map<String, String>> iter = c.Number.iterator(); iter.hasNext();) {
                m = iter.next();
                ops.add(ContentProviderOperation
                        .newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(
                                Data.RAW_CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?"
                                        + " AND " + Phone.TYPE + "=?", new String[] {
                                        String.valueOf(c._ID), Phone.CONTENT_ITEM_TYPE, m.get(Phone.TYPE)
                                }).withValue(Phone.NUMBER, m.get(Phone.NUMBER)).build());
            }
        }
        // update Email
        if (c.Email.size() > 0) {
            for (Iterator<Map<String, String>> iter = c.Number.iterator(); iter.hasNext();) {
                m = iter.next();
                ops.add(ContentProviderOperation
                        .newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(
                                Data.RAW_CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?"
                                        + " AND " + Email.TYPE + "=?",
                                new String[] {
                                        String.valueOf(c._ID), Email.CONTENT_ITEM_TYPE,
                                        String.valueOf(m.get(Email.TYPE))
                                }).withValue(Email.DATA, m.get(Email.ADDRESS)).build());
            }
        }
        // update Name
        ops.add(ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(Data.RAW_CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?",
                        new String[] {
                                String.valueOf(c._ID), StructuredName.CONTENT_ITEM_TYPE
                        }).withValue(StructuredName.DISPLAY_NAME, c.Name).build());

        // update Organization
        if (c.Organization.size() > 0) {
            for (Iterator<Map<String, String>> iter = c.Organization.iterator(); iter.hasNext();) {
                m = iter.next();
                ops.add(ContentProviderOperation
                        .newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(Data.RAW_CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?",
                                new String[] {
                                        String.valueOf(c._ID), Organization.CONTENT_ITEM_TYPE
                                }).withValue(Organization.TITLE, m.get(Organization.TITLE))
                        .withValue(Organization.COMPANY, m.get(Organization.COMPANY)).build());
            }
        }
        // update Comments
        if (null != c.Comment && !"".equals(c.Comment)) {
            ops.add(ContentProviderOperation
                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(Data.RAW_CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?",
                            new String[] {
                                    String.valueOf(c._ID), Note.CONTENT_ITEM_TYPE
                            }).withValue(Note.NOTE, c.Comment).build());
        }

        // update Address
        for (Iterator<Map<String, AddressInfo>> iter = c.Address.iterator(); iter.hasNext();) {
            m1 = iter.next();
            ops.add(ContentProviderOperation
                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(Data.RAW_CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?",
                            new String[] {
                                    String.valueOf(c._ID), StructuredPostal.CONTENT_ITEM_TYPE
                            }).withValue(StructuredPostal.REGION, m1.get(StructuredPostal.TYPE).province)
                    .withValue(StructuredPostal.CITY, m1.get(StructuredPostal.TYPE).city)
                    .withValue(StructuredPostal.STREET, m1.get(StructuredPostal.TYPE).street)
                    .withValue(StructuredPostal.POSTCODE, m1.get(StructuredPostal.TYPE).postCode).build());
        }

        // uodate Im
        if (c.Exchange.size() > 0) {
            for (Iterator<Map<String, String>> iter = c.Exchange.iterator(); iter.hasNext();) {
                m = iter.next();
                ops.add(ContentProviderOperation
                        .newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(Data.RAW_CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?",
                                new String[] {
                                        String.valueOf(c._ID), Im.CONTENT_ITEM_TYPE
                                }).withValue(Im.TYPE, m.get(Im.TYPE)).withValue(Im.DATA, m.get(Im.DATA)).build());
            }
        }
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            return false;
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
    public static boolean addContact(Context context, Contact c, AccountInfo ac) {

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        Map<String, String> m = new HashMap<String, String>();
        Map<String, AddressInfo> m1 = new HashMap<String, AddressInfo>();
        // rawcontacts'account
        // don't give c._Id value because it is automaticly increased
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts._ID, c._ID)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, ac.accountType)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, ac.accountName)
                .withValue(ContactsContract.RawContacts.AGGREGATION_MODE,
                        ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED).build());
        // name
        if (!c.Name.equals("")) {
            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, c._ID)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, c.Name).build());
        }
        // Organnization
        if (c.Organization.size() > 0) {
            for (Iterator<Map<String, String>> iter = c.Organization.iterator(); iter.hasNext();) {
                m = iter.next();
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, c._ID)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, m.get(Organization.COMPANY))
                        .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, m.get(Organization.TITLE))
                        .withValue(ContactsContract.CommonDataKinds.Organization.TYPE,
                                ContactsContract.CommonDataKinds.Organization.TYPE_WORK).build());
            }
        }
        // phone number
        if (c.Number.size() > 0) {
            for (Iterator<Map<String, String>> iter = c.Number.iterator(); iter.hasNext();) {
                m = iter.next();
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, c._ID)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, m.get(Phone.NUMBER))
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, m.get(Phone.TYPE)).build());
            }
        }
        // email
        if (c.Email.size() > 0) {
            for (Iterator<Map<String, String>> iter = c.Email.iterator(); iter.hasNext();) {
                m = iter.next();
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, c._ID)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, m.get(Email.ADDRESS))
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, m.get(Email.TYPE)).build());
            }
        }
        // address
        if (c.Address.size() > 0) {
            for (Iterator<Map<String, AddressInfo>> iter = c.Address.iterator(); iter.hasNext();) {
                m1 = iter.next();
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, c._ID)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET,
                                m1.get(StructuredPostal.STREET))
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY,
                                m1.get(StructuredPostal.CITY))
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION,
                                m1.get(StructuredPostal.REGION))
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
                                m1.get(StructuredPostal.POSTCODE))
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                                m1.get(StructuredPostal.TYPE)).build());
            }
        }
        // IM
        if (c.Exchange.size() > 0) {
            for (Iterator<Map<String, String>> iter = c.Exchange.iterator(); iter.hasNext();) {
                m = iter.next();
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, c._ID)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Im.DATA1, m.get(Im.DATA))
                        .withValue(ContactsContract.CommonDataKinds.Im.PROTOCOL, m.get(Im.TYPE)).build());
            }
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
        String where = ContactsContract.Groups.ACCOUNT_NAME + "=?" + " and " + ContactsContract.Groups.ACCOUNT_TYPE
                + "=?";
        Cursor mr = context.getContentResolver().query(uri, null, where, new String[] {
                ai.accountName, ai.accountType
        }, null);
        List<GroupInfo> ls = new ArrayList<GroupInfo>();
        while (mr.moveToNext()) {
            GroupInfo gi = new GroupInfo();
            gi.grId = mr.getString(mr.getColumnIndex(ContactsContract.Groups._ID));
            gi.name = mr.getString(mr.getColumnIndex(ContactsContract.Groups.TITLE));
            gi.accountType = mr.getString(mr.getColumnIndex(ContactsContract.Groups.ACCOUNT_TYPE));
            gi.accountName = mr.getString(mr.getColumnIndex(ContactsContract.Groups.ACCOUNT_NAME));
            System.out.println("--------------->" + mr.getString(mr.getColumnIndex(ContactsContract.Groups._ID)));
            System.out.println("--------------->" + mr.getString(mr.getColumnIndex(ContactsContract.Groups.TITLE)));
            System.out.println("--------------->"
                    + mr.getString(mr.getColumnIndex(ContactsContract.Groups.ACCOUNT_NAME)));
            System.out.println("--------------->"
                    + mr.getString(mr.getColumnIndex(ContactsContract.Groups.ACCOUNT_TYPE)));
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
        String[] RAW_PROJECTION = new String[] {
            Data.RAW_CONTACT_ID,
        };
        String RAW_CONTACTS_WHERE = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=?" + " and "
                + ContactsContract.Data.MIMETYPE + "=" + "'"
                + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
        Cursor mr = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, RAW_PROJECTION,
                RAW_CONTACTS_WHERE, new String[] {
                    gi.grId
                }, null);
        while (mr.moveToNext()) {
            long rawId = mr.getLong(mr.getColumnIndex(Data.RAW_CONTACT_ID));
            System.out.println("--------------->" + rawId);
            ls.add(getContactByRawId(context, rawId));
        }
        return ls;
    }

    /**
     * get all collect contacts
     * @param context
     * @return
     */
    public static List<Contact> getCollectContacts(Context context) {
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

}
