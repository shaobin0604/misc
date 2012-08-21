
package com.pekall.pctool.model.contact;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.account.AccountInfo;
import com.pekall.pctool.model.contact.Contact.AddressInfo;
import com.pekall.pctool.model.contact.Contact.EmailInfo;
import com.pekall.pctool.model.contact.Contact.ImInfo;
import com.pekall.pctool.model.contact.Contact.ModifyTag;
import com.pekall.pctool.model.contact.Contact.OrgInfo;
import com.pekall.pctool.model.contact.Contact.PhoneInfo;

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
    public static final int USER_DEFINED = 99;
    public static final int FAVORITE_CONTACT_FLAG = 1;

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
        Slog.d(">>>>>>>>>> getContactByRawId E");

        Contact c = new Contact();
        String rawContactId = String.valueOf(rawId);
        c.id = Integer.parseInt(String.valueOf(rawId));
        Uri rawContactUri = RawContacts.CONTENT_URI;
        ContentResolver resolver = context.getContentResolver();
        
        
        // photo
        {
            Cursor cursorOfPhoto = resolver.query(rawContactUri,
                    new String[] {
                        RawContacts.CONTACT_ID
                    }, RawContacts._ID
                            + "=?", new String[] {
                        rawContactId
                    }, null);
            if (cursorOfPhoto != null) {
                if (cursorOfPhoto.moveToNext()) {
                    long contactId = cursorOfPhoto.getLong(cursorOfPhoto
                            .getColumnIndex(RawContacts.CONTACT_ID));
                    c.photo = getContactPhoto(context, contactId);
                }
                cursorOfPhoto.close();
            }
        }

        // account
        {
            Cursor cursorOfAccount = resolver.query(rawContactUri,
                    new String[] {
                            RawContacts.ACCOUNT_NAME,
                            RawContacts.ACCOUNT_TYPE
                    }, RawContacts._ID + "=?",
                    new String[] {
                        rawContactId
                    }, null);
            while (cursorOfAccount.moveToNext()) {
                c.accountInfo.accountName = cursorOfAccount
                        .getString(cursorOfAccount
                                .getColumnIndex(RawContacts.ACCOUNT_NAME));
                c.accountInfo.accountType = cursorOfAccount
                        .getString(cursorOfAccount
                                .getColumnIndex(RawContacts.ACCOUNT_TYPE));
            }
            cursorOfAccount.close();
        }

        // get group
        {
            Cursor cursorOfGroup = resolver.query(
                    Data.CONTENT_URI,
                    new String[] {
                            Data._ID, Data.DATA1
                    },
                    Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + "=?",
                    new String[] {
                            rawContactId,
                            GroupMembership.CONTENT_ITEM_TYPE
                    }, null);
            while (cursorOfGroup.moveToNext()) {
                GroupInfo gr = new GroupInfo();
                gr.grId = cursorOfGroup.getLong(cursorOfGroup
                        .getColumnIndex(Data.DATA1));
                gr.dataId = cursorOfGroup.getLong(cursorOfGroup
                        .getColumnIndex(Data._ID));

                // Cursor cursorOfGroupInfo = resolver.query(
                // Groups.CONTENT_URI,
                // new String[] {
                // Groups.TITLE, Groups.NOTES
                // },
                // Groups._ID + " = ?  ",
                // new String[] {
                // String.valueOf(gr.grId)
                // }, null);
                // if (cursorOfGroupInfo.moveToNext()) {
                // gr.name = cursorOfGroupInfo.getString(cursorOfGroupInfo
                // .getColumnIndex(Groups.TITLE));
                // gr.note = convertToString(cursorOfGroupInfo
                // .getString(cursorOfGroupInfo
                // .getColumnIndex(Groups.NOTES)));
                // }
                // cursorOfGroupInfo.close();

                c.groupInfos.add(gr);
            }
            cursorOfGroup.close();
        }

        // get the contact name
        {
            Cursor cursorOfName = resolver.query(
                    Data.CONTENT_URI,
                    new String[] {
                        Data.DISPLAY_NAME
                    // --------->data1
                    }, Data.RAW_CONTACT_ID + " = ? ",
                    new String[] {
                        rawContactId
                    }, null);
            if (cursorOfName.moveToNext()) {
                c.name = cursorOfName.getString(cursorOfName
                        .getColumnIndex(Data.DISPLAY_NAME));
                // we only need one name for a contact
            }
            cursorOfName.close();
        }

        // nickname
        {
            Cursor cursorOfNickname = resolver
                    .query(Data.CONTENT_URI,
                            new String[] {
                                Data.DATA1
                            // --------->data1
                            },
                            Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE
                                    + " = ? ",
                            new String[] {
                                    rawContactId, Nickname.TYPE
                            }, null);
            if (cursorOfNickname.moveToNext()) {
                c.nickname = cursorOfNickname.getString(cursorOfNickname
                        .getColumnIndex(Data.DATA1));
                // we only need one name for a contact
            }
            cursorOfNickname.close();
        }

        // phone Number
        {
            Cursor cursorOfPhone = resolver
                    .query(Data.CONTENT_URI,
                            new String[] {
                                    Data._ID, Phone.NUMBER,// ----data1
                                    Phone.TYPE, Phone.DATA3
                            // -----custom data name
                            },
                            Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE
                                    + " = ? ",
                            new String[] {
                                    rawContactId,
                                    Phone.CONTENT_ITEM_TYPE,
                            }, null);
            while (cursorOfPhone.moveToNext()) {
                PhoneInfo pr = new PhoneInfo();

                pr.id = cursorOfPhone.getLong(cursorOfPhone
                        .getColumnIndex(Data._ID));
                pr.type = cursorOfPhone.getInt(cursorOfPhone
                        .getColumnIndex(Phone.TYPE));
                pr.number = cursorOfPhone.getString(cursorOfPhone
                        .getColumnIndex(Phone.NUMBER));
                if (pr.type == USER_DEFINED) {
                    pr.customName = cursorOfPhone.getString(cursorOfPhone
                            .getColumnIndex(Phone.DATA3));
                }

                c.phoneInfos.add(pr);
            }
            cursorOfPhone.close();
        }

        // email
        {
            Cursor cursorOfEmail = resolver
                    .query(Data.CONTENT_URI,
                            new String[] {
                                    Data._ID, Email.ADDRESS,// data1
                                    Email.TYPE, Email.DATA3
                            },
                            Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE
                                    + " = ? ",
                            new String[] {
                                    rawContactId,
                                    Email.CONTENT_ITEM_TYPE,
                            }, null);
            while (cursorOfEmail.moveToNext()) {
                EmailInfo er = new EmailInfo();

                er.id = cursorOfEmail.getLong(cursorOfEmail
                        .getColumnIndex(Data._ID));
                er.type = cursorOfEmail.getInt(cursorOfEmail
                        .getColumnIndex(Email.TYPE));
                er.email = cursorOfEmail.getString(cursorOfEmail
                        .getColumnIndex(Email.ADDRESS));
                if (er.type == USER_DEFINED) {
                    er.customName = cursorOfEmail.getString(cursorOfEmail
                            .getColumnIndex(Email.DATA3));
                }

                c.emailInfos.add(er);
            }
            cursorOfEmail.close();
        }

        // im
        {
            Cursor cursorOfIm = resolver
                    .query(Data.CONTENT_URI, // 查询data表
                            new String[] {
                                    Data._ID, Im.PROTOCOL, // --->data5(type)
                                    Data.DATA1, Data.DATA3
                            // IM IM.type means home work and so on
                            },
                            Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE
                                    + " = ? ",
                            new String[] {
                                    rawContactId, Im.CONTENT_ITEM_TYPE,
                            },
                            null);
            while (cursorOfIm.moveToNext()) {
                ImInfo ir = new ImInfo();

                ir.id = cursorOfIm.getLong(cursorOfIm.getColumnIndex(Data._ID));
                ir.type = cursorOfIm.getInt(cursorOfIm
                        .getColumnIndex(Im.PROTOCOL));
                ir.account = cursorOfIm.getString(cursorOfIm
                        .getColumnIndex(Data.DATA1));
                if (ir.type == USER_DEFINED) {
                    ir.customName = cursorOfIm.getString(cursorOfIm
                            .getColumnIndex(Data.DATA3));
                }

                c.imInfos.add(ir);
            }
            cursorOfIm.close();
        }

        // address
        {
            Cursor cursorOfAddress = resolver
                    .query(Data.CONTENT_URI,
                            new String[] {
                                    Data._ID,
                                    StructuredPostal.FORMATTED_ADDRESS,
                                    StructuredPostal.CITY,
                                    StructuredPostal.DATA3,
                                    StructuredPostal.POSTCODE,
                                    StructuredPostal.COUNTRY,
                                    StructuredPostal.STREET,
                                    StructuredPostal.TYPE,
                                    StructuredPostal.REGION, Data.DATA3
                            },
                            Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE
                                    + " = ? ",
                            new String[] {
                                    rawContactId,
                                    StructuredPostal.CONTENT_ITEM_TYPE,
                            }, null);
            while (cursorOfAddress.moveToNext()) {
                AddressInfo ar = new AddressInfo();

                ar.id = cursorOfAddress.getLong(cursorOfAddress
                        .getColumnIndex(Data._ID));
                ar.type = cursorOfAddress
                        .getInt(cursorOfAddress
                                .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
                ar.address = convertToString(cursorOfAddress
                        .getString(cursorOfAddress
                                .getColumnIndex(StructuredPostal.FORMATTED_ADDRESS)));
                ar.country = convertToString(cursorOfAddress
                        .getString(cursorOfAddress
                                .getColumnIndex(StructuredPostal.COUNTRY)));
                ar.city = convertToString(cursorOfAddress
                        .getString(cursorOfAddress
                                .getColumnIndex(StructuredPostal.CITY)));
                ar.street = convertToString(cursorOfAddress
                        .getString(cursorOfAddress
                                .getColumnIndex(StructuredPostal.STREET)));
                ar.postcode = convertToString(cursorOfAddress
                        .getString(cursorOfAddress
                                .getColumnIndex(StructuredPostal.POSTCODE)));
                ar.province = convertToString(cursorOfAddress
                        .getString(cursorOfAddress
                                .getColumnIndex(StructuredPostal.REGION)));
                if (ar.type == USER_DEFINED) {
                    ar.customName = convertToString(cursorOfAddress
                            .getString(cursorOfAddress
                                    .getColumnIndex(StructuredPostal.DATA3)));
                }

                c.addressInfos.add(ar);
            }
            cursorOfAddress.close();
        }

        // organization
        {
            Cursor cursorOfOrganization = resolver
                    .query(Data.CONTENT_URI,
                            new String[] {
                                    Data._ID, Organization.COMPANY,
                                    Organization.TYPE, Data.DATA3
                            },
                            Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE
                                    + " = ? ",
                            new String[] {
                                    rawContactId,
                                    Organization.CONTENT_ITEM_TYPE,
                            }, null);
            while (cursorOfOrganization.moveToNext()) {
                OrgInfo or = new OrgInfo();

                or.id = cursorOfOrganization.getLong(cursorOfOrganization
                        .getColumnIndex(Data._ID));
                or.type = cursorOfOrganization.getInt(cursorOfOrganization
                        .getColumnIndex(Organization.TYPE));
                or.org = convertToString(cursorOfOrganization
                        .getString(cursorOfOrganization
                                .getColumnIndex(Organization.COMPANY)));
                if (or.type == USER_DEFINED) {
                    or.customName = cursorOfOrganization
                            .getString(cursorOfOrganization
                                    .getColumnIndex(Data.DATA3));
                }

                c.orgInfos.add(or);
            }
            cursorOfOrganization.close();
        }

        Slog.d("<<<<<<<<<< getContactByRawId X");

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
        List<Contact> contacts = new ArrayList<Contact>();

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[] {
                    RawContacts._ID
                }, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long rawContactId = cursor.getLong(cursor
                        .getColumnIndex(ContactsContract.RawContacts._ID));
                contacts.add(getContactByRawId(context, rawContactId));
            }

            cursor.close();
        }

        return contacts;
    }

    /**
     * get some contacts of one account
     * 
     * @param context
     * @param accountName
     * @param accountType
     * @return
     */
    public static List<Contact> getAccountContacts(Context context,
            AccountInfo account) {
        List<Contact> contacts = new ArrayList<Contact>();
        if (account == null)
            return contacts;
        String where = ContactsContract.RawContacts.ACCOUNT_NAME + "=?"
                + " and " + ContactsContract.RawContacts.ACCOUNT_TYPE + "=?";
        Cursor cursor = context
                .getContentResolver()
                .query(ContactsContract.RawContacts.CONTENT_URI,
                        null,
                        where,
                        new String[] {
                                account.accountName, account.accountType
                        },
                        null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long rawContactId = cursor.getLong(cursor
                        .getColumnIndex(ContactsContract.RawContacts._ID));
                contacts.add(getContactByRawId(context, rawContactId));
            }
            cursor.close();
        }

        return contacts;
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
        List<AccountInfo> accountInfos = new ArrayList<AccountInfo>();
        for (int i = 0; i < accounts.length; i++) {
            AccountInfo ai = new AccountInfo();
            ai.accountName = accounts[i].name;
            ai.accountType = accounts[i].type;
            accountInfos.add(ai);
        }
        AccountInfo ai = new AccountInfo();
        ai.accountName = DEFAULT_ACCOUNT_NAME;
        ai.accountType = DEFAULT_ACCOUNT_TYPE;
        accountInfos.add(ai);
        if (getSimCardState(context) == 1) {
            ai = new AccountInfo();
            ai.accountName = SIM1_ACCOUNT_NAME;
            ai.accountType = SIM1_ACCOUNT_TYPE;
            accountInfos.add(ai);
        } else if (getSimCardState(context) == 2) {
            ai = new AccountInfo();
            ai.accountName = SIM2_ACCOUNT_NAME;
            ai.accountType = SIM2_ACCOUNT_TYPE;
            accountInfos.add(ai);
        } else if (getSimCardState(context) == 3) {
            ai = new AccountInfo();
            ai.accountName = SIM1_ACCOUNT_NAME;
            ai.accountType = SIM1_ACCOUNT_TYPE;
            accountInfos.add(ai);
            ai = new AccountInfo();
            ai.accountName = SIM2_ACCOUNT_NAME;
            ai.accountType = SIM2_ACCOUNT_TYPE;
            accountInfos.add(ai);
        }
        return accountInfos;
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
        String where = ContactsContract.RawContacts.ACCOUNT_NAME + "=?"
                + " and " + ContactsContract.RawContacts.ACCOUNT_TYPE + "=?";
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI, null, where,
                new String[] {
                        SIM1_ACCOUNT_NAME, SIM1_ACCOUNT_TYPE
                }, null);
        if (cursor.getCount() > 0)
            count++;
        cursor.close();
        cursor = context.getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI, null, where,
                new String[] {
                        SIM2_ACCOUNT_NAME, SIM2_ACCOUNT_TYPE
                }, null);
        if (cursor.getCount() > 0)
            count += 2;
        cursor.close();
        return count;
    }

    /**
     * delete a contact if the contact has the same name
     * 
     * @param name context
     */

    public static boolean deleteContact(Context context, String name) {
        Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI,
                new String[] {
                    Data.RAW_CONTACT_ID
                },
                ContactsContract.Contacts.DISPLAY_NAME + "=?",
                new String[] {
                    name
                }, null);
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    long Id = cursor.getLong(cursor
                            .getColumnIndex(Data.RAW_CONTACT_ID));
                    ops.add(ContentProviderOperation
                            .newDelete(
                                    ContentUris.withAppendedId(
                                            RawContacts.CONTENT_URI, Id)).build());
                    try {
                        context.getContentResolver().applyBatch(
                                ContactsContract.AUTHORITY, ops);
                    } catch (Exception e) {
                        return false;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return true;
    }

    /**
     * delete a contact by rawId
     * 
     * @param context ,id
     */
    public static boolean deleteContact(Context context, long rawId) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newDelete(
                ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawId))
                .build());
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY,
                    ops);
        } catch (Exception e) {
            return false;
        }
        return true;

    }

    /**
     * delete some contact by rawIds
     * 
     * @param context
     * @param rowIds
     */
    public static boolean deleteContact(Context context, List<Long> rowIds) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        for (long rowId : rowIds) {
            ops.add(ContentProviderOperation.newDelete(
                    ContentUris.withAppendedId(RawContacts.CONTENT_URI, rowId))
                    .build());
        }
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY,
                    ops);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * update contact
     * 
     * @param context
     * @param contact
     * @return
     */
    public static boolean updateContact(Context context, Contact contact) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        // update photo
        if (contact.shouldUpdatePhoto) {
            if (contact.photo != null) {
                if (hasField("photo", context, contact.id)) {
                    // update photo
                    ops.add(ContentProviderOperation
                            .newUpdate(ContactsContract.Data.CONTENT_URI)
                            .withSelection(
                                    Data.RAW_CONTACT_ID + "=?" + " AND "
                                            + ContactsContract.Data.MIMETYPE + " = ?",
                                    new String[] {
                                            String.valueOf(contact.id),
                                            Photo.CONTENT_ITEM_TYPE
                                    })
                            .withValue(Data.DATA15, contact.photo)
                            .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                            .build());
                } else {
                    // insert photo
                    ops.add(ContentProviderOperation
                            .newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValue(ContactsContract.Data.RAW_CONTACT_ID, contact.id)
                            .withValue(ContactsContract.Data.MIMETYPE,
                                    Photo.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO,
                                    contact.photo).build());
                }
            } else {
                // delete photo
                long dataPhotoId = -1;
                Uri uri = Data.CONTENT_URI;
                Cursor cursor = context.getContentResolver().query(
                        uri,
                        new String[] {
                            Data._ID
                        },
                        Data.CONTENT_TYPE + "=? and" + Data.RAW_CONTACT_ID + "=?",
                        new String[] {
                                Photo.CONTENT_ITEM_TYPE,
                                String.valueOf(contact.id)
                        }, null);
                if (cursor.moveToNext())
                    dataPhotoId = cursor.getLong(cursor.getColumnIndex(Data._ID));
                if (-1 != dataPhotoId) {
                    ops.add(ContentProviderOperation.newDelete(
                            ContentUris.withAppendedId(Data.CONTENT_URI,
                                    dataPhotoId)).build());
                }
            }
        }

        // update nickname
        if (hasField("nickname", context, contact.id)) {
            ops.add(ContentProviderOperation
                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                            Data.RAW_CONTACT_ID + "=?" + " AND "
                                    + ContactsContract.Data.MIMETYPE + " = ?",
                            new String[] {
                                    String.valueOf(contact.id),
                                    Nickname.CONTENT_ITEM_TYPE
                            })
                    .withValue(Nickname.NAME, contact.nickname).build());
        } else {
            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, contact.id)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            Nickname.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Nickname.NAME,
                            contact.nickname).build());
        }

        // update Name
        if (hasField("name", context, contact.id)) {
            ops.add(ContentProviderOperation
                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                            Data.RAW_CONTACT_ID + "=?" + " AND "
                                    + ContactsContract.Data.MIMETYPE + " = ?",
                            new String[] {
                                    String.valueOf(contact.id),
                                    StructuredName.CONTENT_ITEM_TYPE
                            })
                    .withValue(StructuredName.DISPLAY_NAME, contact.name).build());
        } else {
            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, contact.id)
                    .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            contact.name).build());
        }

        // has group
        for (GroupInfo gi : contact.groupInfos) {
            if (gi.modifyFlag == ModifyTag.add) {
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID,
                                contact.id)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                GroupMembership.CONTENT_ITEM_TYPE)
                        .withValue(Data.DATA1, gi.grId).build());
            } else if (gi.modifyFlag == ModifyTag.del) {
                ops.add(ContentProviderOperation
                        .newDelete(
                                ContentUris.withAppendedId(Data.CONTENT_URI,
                                        gi.dataId)).build());
            } else if (gi.modifyFlag == ModifyTag.edit) {
                ops.add(ContentProviderOperation
                        .newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(Data._ID + "=?",
                                new String[] {
                                    String.valueOf(gi.dataId)
                                })
                        .withValue(Data.DATA1, gi.grId).build());
            }
        }

        // update Phone
        for (PhoneInfo pr : contact.phoneInfos) {
            if (pr.modifyFlag == ModifyTag.add) {
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID,
                                contact.id)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                Phone.CONTENT_ITEM_TYPE)
                        .withValue(Phone.NUMBER, pr.number)
                        .withValue(Phone.TYPE, pr.type)
                        .withValue(Phone.DATA3, pr.customName).build());
            } else if (pr.modifyFlag == ModifyTag.del) {
                ops.add(ContentProviderOperation.newDelete(
                        ContentUris.withAppendedId(Data.CONTENT_URI, pr.id))
                        .build());
            } else if (pr.modifyFlag == ModifyTag.edit) {
                ops.add(ContentProviderOperation
                        .newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(Data._ID + "=?",
                                new String[] {
                                    String.valueOf(pr.id)
                                })
                        .withValue(Phone.NUMBER, pr.number)
                        .withValue(Phone.DATA3, pr.customName)
                        .withValue(Phone.TYPE, pr.type).build());
            }
        }

        // update Email
        for (EmailInfo er : contact.emailInfos) {
            if (er.modifyFlag == ModifyTag.add) {
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID,
                                contact.id)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                Email.CONTENT_ITEM_TYPE)
                        .withValue(Email.ADDRESS, er.email)
                        .withValue(Email.TYPE, er.type)
                        .withValue(Email.DATA3, er.customName).build());
            } else if (er.modifyFlag == ModifyTag.del) {
                ops.add(ContentProviderOperation.newDelete(
                        ContentUris.withAppendedId(Data.CONTENT_URI, er.id))
                        .build());
            } else if (er.modifyFlag == ModifyTag.edit) {
                ops.add(ContentProviderOperation
                        .newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(Data._ID + "=?",
                                new String[] {
                                    String.valueOf(er.id)
                                })
                        .withValue(Email.ADDRESS, er.email)
                        .withValue(Email.DATA3, er.customName)
                        .withValue(Email.TYPE, er.type).build());
            }
        }

        // update Organization
        for (OrgInfo or : contact.orgInfos) {
            if (or.modifyFlag == ModifyTag.add) {
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID,
                                contact.id)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                Organization.CONTENT_ITEM_TYPE)
                        .withValue(Organization.COMPANY, or.org)
                        .withValue(Organization.TYPE, or.type)
                        .withValue(Organization.DATA3, or.customName).build());
            } else if (or.modifyFlag == ModifyTag.del) {
                ops.add(ContentProviderOperation.newDelete(
                        ContentUris.withAppendedId(Data.CONTENT_URI, or.id))
                        .build());
            } else if (or.modifyFlag == ModifyTag.edit) {
                ops.add(ContentProviderOperation
                        .newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(Data._ID + "=?",
                                new String[] {
                                    String.valueOf(or.id)
                                })
                        .withValue(Organization.COMPANY, or.org)
                        .withValue(Organization.TYPE, or.type)
                        .withValue(Organization.DATA3, or.customName).build());
            }
        }

        // update Address
        for (AddressInfo ar : contact.addressInfos) {
            if (ar.modifyFlag == ModifyTag.add) {
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID,
                                contact.id)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(StructuredPostal.COUNTRY, ar.country)
                        .withValue(StructuredPostal.CITY, ar.city)
                        .withValue(StructuredPostal.STREET, ar.street)
                        .withValue(StructuredPostal.POSTCODE, ar.postcode)
                        .withValue(StructuredPostal.REGION, ar.province)
                        .withValue(StructuredPostal.FORMATTED_ADDRESS,
                                ar.address)
                        .withValue(Data.DATA3, ar.customName)
                        .withValue(StructuredPostal.TYPE, ar.type).build());

            } else if (ar.modifyFlag == ModifyTag.del) {
                ops.add(ContentProviderOperation.newDelete(
                        ContentUris.withAppendedId(Data.CONTENT_URI, ar.id))
                        .build());
            } else if (ar.modifyFlag == ModifyTag.edit) {
                ops.add(ContentProviderOperation
                        .newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(Data._ID + "=?",
                                new String[] {
                                    String.valueOf(ar.id)
                                })
                        .withValue(StructuredPostal.FORMATTED_ADDRESS,
                                ar.address)
                        .withValue(StructuredPostal.COUNTRY, ar.country)
                        .withValue(StructuredPostal.CITY, ar.city)
                        .withValue(StructuredPostal.STREET, ar.street)
                        .withValue(StructuredPostal.POSTCODE, ar.postcode)
                        .withValue(StructuredPostal.DATA3, ar.customName)
                        .withValue(StructuredPostal.REGION, ar.province)
                        .withValue(StructuredPostal.TYPE, ar.type).build());
            }
        }

        // update Im
        for (ImInfo ir : contact.imInfos) {
            if (ir.modifyFlag == ModifyTag.add) {
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID,
                                contact.id)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                Im.CONTENT_ITEM_TYPE)
                        .withValue(Im.DATA1, ir.account)
                        .withValue(Im.TYPE, ir.type)
                        .withValue(Data.DATA3, ir.customName).build());
            } else if (ir.modifyFlag == ModifyTag.del) {
                ops.add(ContentProviderOperation.newDelete(
                        ContentUris.withAppendedId(Data.CONTENT_URI, ir.id))
                        .build());
            } else if (ir.modifyFlag == ModifyTag.edit) {
                ops.add(ContentProviderOperation
                        .newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(Data._ID + "=?",
                                new String[] {
                                    String.valueOf(ir.id)
                                })
                        .withValue(Im.DATA1, ir.account)
                        .withValue(Im.TYPE, ir.type)
                        .withValue(Im.DATA3, ir.customName).build());
            }
        }
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY,
                    ops);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * add a contact
     * 
     * @param context
     * @param c
     * @param AccountInfo
     * @return
     */
    public static boolean addContact(Context context, Contact c) {

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        if (c.accountInfo.accountName == null
                || "".equals(c.accountInfo.accountName)) {
            c.accountInfo.accountName = DEFAULT_ACCOUNT_NAME;
            c.accountInfo.accountType = DEFAULT_ACCOUNT_TYPE;
        }

        // rawcontacts'account
        // don't give c.id value because it is automatically increased
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,
                        c.accountInfo.accountType)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME,
                        c.accountInfo.accountName)
                .withValue(ContactsContract.RawContacts.AGGREGATION_MODE,
                        ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED)
                .build());
        // add group
        if (c.groupInfos != null) {
            for (int i = 0; i < c.groupInfos.size(); i++) {
                GroupInfo gr = c.groupInfos.get(i);
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(
                                ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                GroupMembership.CONTENT_ITEM_TYPE)
                        .withValue(Data.DATA1, gr.grId).build());
            }
        }

        if (c.photo != null) {
            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                            ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            Photo.CONTENT_ITEM_TYPE)
                    .withValue(Photo.PHOTO, c.photo).build());
        }
        // name
        if (!c.name.equals("")) {
            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                            ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            c.name).build());
        }
        // Organization
        if (c.orgInfos.size() > 0) {
            for (Iterator<OrgInfo> iter = c.orgInfos.iterator(); iter.hasNext();) {
                OrgInfo or = iter.next();
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(
                                ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValue(
                                ContactsContract.CommonDataKinds.Organization.COMPANY,
                                or.org)
                        .withValue(
                                ContactsContract.CommonDataKinds.Organization.TYPE,
                                or.type)
                        .withValue(
                                ContactsContract.CommonDataKinds.Organization.DATA3,
                                or.customName).build());
            }
        }
        // phone number
        if (c.phoneInfos.size() > 0) {
            for (Iterator<PhoneInfo> iter = c.phoneInfos.iterator(); iter
                    .hasNext();) {
                PhoneInfo pr = iter.next();
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(
                                ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(
                                ContactsContract.CommonDataKinds.Phone.NUMBER,
                                pr.number)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                pr.type)
                        .withValue(
                                ContactsContract.CommonDataKinds.Phone.DATA3,
                                pr.customName).build());
            }
        }
        // email
        if (c.emailInfos.size() > 0) {
            for (Iterator<EmailInfo> iter = c.emailInfos.iterator(); iter
                    .hasNext();) {
                EmailInfo er = iter.next();
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(
                                ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA,
                                er.email)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE,
                                er.type)
                        .withValue(
                                ContactsContract.CommonDataKinds.Email.DATA3,
                                er.customName).build());
            }
        }
        // address
        if (c.addressInfos.size() > 0) {
            for (Iterator<AddressInfo> iter = c.addressInfos.iterator(); iter
                    .hasNext();) {
                AddressInfo ar = iter.next();
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(
                                ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                                ar.type)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
                                ar.country)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredPostal.CITY,
                                ar.city)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredPostal.STREET,
                                ar.street)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredPostal.REGION,
                                ar.province)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
                                ar.postcode)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                                ar.address)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredPostal.DATA3,
                                ar.customName).build());
            }
        }
        // IM
        if (c.imInfos.size() > 0) {
            for (Iterator<ImInfo> iter = c.imInfos.iterator(); iter.hasNext();) {
                ImInfo ir = iter.next();
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(
                                ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Im.DATA1,
                                ir.account)
                        .withValue(
                                ContactsContract.CommonDataKinds.Im.PROTOCOL,
                                ir.type)
                        .withValue(ContactsContract.CommonDataKinds.Im.DATA3,
                                ir.customName).build());
            }
        }

        // nick name
        if (c.nickname != null && !"".equals(c.nickname)) {
            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                            ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            Nickname.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Nickname.NAME,
                            c.nickname).build());
        }
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY,
                    ops);
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
        String where = ContactsContract.Groups.ACCOUNT_NAME + "=?" + " and "
                + ContactsContract.Groups.ACCOUNT_TYPE + "=?";
        Cursor cursor = context.getContentResolver().query(uri, null, where,
                new String[] {
                        ai.accountName, ai.accountType
                }, null);
        List<GroupInfo> groups = new ArrayList<GroupInfo>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                GroupInfo gi = new GroupInfo();

                gi.grId = cursor
                        .getLong(cursor.getColumnIndex(ContactsContract.Groups._ID));
                gi.name = cursor.getString(cursor
                        .getColumnIndex(ContactsContract.Groups.TITLE));
                gi.accountInfo.accountType = cursor.getString(cursor
                        .getColumnIndex(ContactsContract.Groups.ACCOUNT_TYPE));
                gi.accountInfo.accountName = cursor.getString(cursor
                        .getColumnIndex(ContactsContract.Groups.ACCOUNT_NAME));

                groups.add(gi);
            }
            cursor.close();
        }
        return groups;
    }

    /**
     * add a group
     */
    public static boolean addGroup(Context context, GroupInfo gi) {
        Uri groupUri = ContactsContract.Groups.CONTENT_URI;
        if (null == gi.accountInfo.accountName
                || "".equals(gi.accountInfo.accountName)) {
            gi.accountInfo.accountName = DEFAULT_ACCOUNT_NAME;
            gi.accountInfo.accountType = DEFAULT_ACCOUNT_TYPE;
        }
        ContentValues cv = new ContentValues();
        cv.put(Groups.ACCOUNT_NAME, gi.accountInfo.accountName);
        cv.put(Groups.ACCOUNT_TYPE, gi.accountInfo.accountType);
        cv.put(Groups.TITLE, gi.name);
        cv.put(Groups.NOTES, gi.note);
        Uri newUri = context.getContentResolver().insert(groupUri, cv);
        return (Long.parseLong(newUri.getLastPathSegment()) > 0);
    }

    /**
     * delete a group
     */
    public static boolean deleteGroup(Context context, long groupId) {
        Uri groupUri = Uri.parse(ContactsContract.Groups.CONTENT_URI.toString()
                + "?" + ContactsContract.CALLER_IS_SYNCADAPTER + "=true");
        System.out.println(groupUri.toString());
        // remeber if we let callerIsSyncAdapter=true we delete the group and
        // contact's data
        // if we don't attach importance to explain callerIsSyncAdapter
        // we only make group's data dirty not really deleted and unless sync
        // contact
        int rows = context.getContentResolver().delete(groupUri,
                Groups._ID + "=" + groupId, null);
        return rows > 0;
    }

    /**
     * edit a group
     */
    public static boolean updateGroup(Context context, GroupInfo gi) {
        Uri groupUri = ContactsContract.Groups.CONTENT_URI;
        // if (null == gi.accountInfo.accountName ||
        // "".equals(gi.accountInfo.accountName)) {
        // gi.accountInfo.accountName = DEFAULT_ACCOUNT_NAME;
        // gi.accountInfo.accountType = DEFAULT_ACCOUNT_TYPE;
        // }
        ContentValues cv = new ContentValues();
        // cv.put(Groups.ACCOUNT_NAME, gi.accountInfo.accountName);
        // cv.put(Groups.ACCOUNT_TYPE, gi.accountInfo.accountType);
        cv.put(Groups.TITLE, gi.name);
        cv.put(Groups.NOTES, gi.note);
        int rows = context.getContentResolver().update(groupUri, cv,
                Groups._ID + "=?", new String[] {
                    String.valueOf(gi.grId)
                });
        return rows > 0;
    }

    /**
     * get all contacts from one account's one group
     * 
     * @param context
     * @param gi
     * @return
     */
    public static List<Contact> getContactsByGroup(Context context, GroupInfo gi) {
        List<Contact> contacts = new ArrayList<Contact>();
        if (gi == null)
            gi = new GroupInfo();
        String[] RAW_PROJECTION = new String[] {
                Data.RAW_CONTACT_ID,
        };
        String RAW_CONTACTS_WHERE = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
                + "=?"
                + " and "
                + ContactsContract.Data.MIMETYPE
                + "="
                + "'"
                + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
                + "'";
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI, RAW_PROJECTION,
                RAW_CONTACTS_WHERE, new String[] {
                    String.valueOf(gi.grId)
                },
                null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long rawId = cursor.getLong(cursor.getColumnIndex(Data.RAW_CONTACT_ID));
                contacts.add(getContactByRawId(context, rawId));
            }
            cursor.close();
        }
        return contacts;
    }

    /**
     * get all Favorite contacts
     * 
     * @param context
     * @return
     */
    public static List<Contact> getFavoriteContacts(Context context) {
        List<Contact> contacts = new ArrayList<Contact>();
        String where = RawContacts.STARRED + "=?";
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[] {
                    RawContacts._ID
                }, where,
                new String[] {
                    String.valueOf(FAVORITE_CONTACT_FLAG)
                }, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long rawContactId = cursor.getLong(cursor
                        .getColumnIndex(RawContacts._ID));
                contacts.add(getContactByRawId(context, rawContactId));
            }
            cursor.close();
        }
        return contacts;

    }

    /**
     * Check whether the specified contact belongs to any group
     * 
     * @param context
     * @return
     */
    public static boolean isContactBelongsToAnyGroup(Context context,
            long contactId) {
        Cursor cursor = context.getContentResolver().query(
                Data.CONTENT_URI,
                new String[] {
                    Data.DATA1
                },
                Data.RAW_CONTACT_ID + "=?" + " AND "
                        + ContactsContract.Data.MIMETYPE + " = ?",
                new String[] {
                        String.valueOf(contactId),
                        GroupMembership.CONTENT_ITEM_TYPE
                }, null);
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        return count > 0;
    }

    /**
     * @param obj
     * @return
     */
    public static String convertToString(String obj) {
        return obj == null ? "" : obj;
    }

    /**
     * get photo of a contact
     * 
     * @param context
     * @param contactId
     * @return
     */

    public static byte[] getContactPhoto(Context context, long contactId) {
        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
                contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri,
                Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] {
                    Contacts.Photo.PHOTO
                }, null, null, null);

        byte[] data = null;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                data = cursor.getBlob(0);
            }
            cursor.close();
        }

        return data;
    }

    /**
     * has the name or nickname
     * 
     * @param filed
     * @param context
     * @param rawId
     * @return
     */
    public static boolean hasField(String filed, Context context, long rawId) {
        Uri dataUri = Data.CONTENT_URI;
        String where = Data.CONTENT_TYPE + "=? and " + Data.RAW_CONTACT_ID
                + "=?";
        String whereargs[] = null;
        if (filed.equals("name")) {
            whereargs = new String[] {
                    StructuredName.CONTENT_ITEM_TYPE,
                    String.valueOf(rawId)
            };
        } else if (filed.equals("nickname")) {
            whereargs = new String[] {
                    Nickname.CONTENT_ITEM_TYPE,
                    String.valueOf(rawId)
            };
        } else if (filed.equals("photo")) {
            whereargs = new String[] {
                    Photo.CONTENT_ITEM_TYPE,
                    String.valueOf(rawId)
            };
        }
        Cursor cursor = context.getContentResolver().query(dataUri,
                new String[] {
                    Data._ID
                }, where, whereargs, null);
        boolean ret = false;
        if (cursor != null) {
            ret = cursor.getCount() > 0;
            cursor.close();
        }
        return ret;
    }
}
