
package com.pekall.pctool.model.contact;

import android.provider.BaseColumns;

import com.pekall.pctool.model.account.AccountInfo;

import java.util.ArrayList;
import java.util.List;

public class Contact {
    public long id;                         // rawId
    public String name;
    public String sortKey;                  // sort key for name
    public String nickname;
    public byte[] photo;
    public boolean shouldUpdatePhoto;       // whether update photo attribute
    public int modifyTag;                   // contact change flag(no change, add, update, delete) since last sync
    public int version;                     // used for detect contact change
    public AccountInfo accountInfo;
    public List<GroupInfo> groupInfos;
    public List<PhoneInfo> phoneInfos;
    public List<EmailInfo> emailInfos;
    public List<ImInfo> imInfos;
    public List<AddressInfo> addressInfos;
    public List<OrgInfo> orgInfos;
    
    public Contact() {
        modifyTag = ModifyTag.same;
        accountInfo = new AccountInfo();
        groupInfos = new ArrayList<GroupInfo>();
        phoneInfos = new ArrayList<PhoneInfo>();
        emailInfos = new ArrayList<EmailInfo>();
        imInfos = new ArrayList<ImInfo>();
        addressInfos = new ArrayList<AddressInfo>();
        orgInfos = new ArrayList<OrgInfo>();
    }
    
    @Override
    public String toString() {
        return "Contact [id=" + id + ", name=" + name + ", sortKey=" + sortKey + ", nickname=" + nickname + ", accountInfo=" + accountInfo
                + ", groupInfos=" + groupInfos + ", phoneInfos=" + phoneInfos + ", emailInfos=" + emailInfos
                + ", imInfos=" + imInfos + ", addressInfos=" + addressInfos + ", orgInfos=" + orgInfos + "]";
    }

    public void setAccountInfo(String name, String type) {
        accountInfo.accountName = name;
        accountInfo.accountType = type;
    }
    
    public void addGroupInfo(GroupInfo groupInfo) {
        groupInfos.add(groupInfo);
    }
    
    public void addPhoneInfo(PhoneInfo phoneInfo) {
        phoneInfos.add(phoneInfo);
    }
    
    public void addEmailInfo(EmailInfo emailInfo) {
        emailInfos.add(emailInfo);
    }
    
    public void addImInfo(ImInfo imInfo) {
        imInfos.add(imInfo);
    }
    
    public void addAddressInfo(AddressInfo addressInfo) {
        addressInfos.add(addressInfo);
    }
    
    public void addOrgInfo(OrgInfo orgInfo) {
        orgInfos.add(orgInfo);
    }
    
    public static class PhoneInfo {
        public long id;
        public int type;    // http://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Phone.html
        public String number;
        public String customName;
        public int    modifyFlag;   // default same
        @Override
        public String toString() {
            return "PhoneInfo [id=" + id + ", type=" + type + ", number=" + number + ", customName=" + customName
                    + ", modifyFlag=" + modifyFlag + "]";
        }
        
        
    }

    public static class EmailInfo {
        public long id;
        public int type;    // http://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Email.html
        public String address;
        public String customName;
        public int    modifyFlag;   // default same
        @Override
        public String toString() {
            return "EmailInfo [id=" + id + ", type=" + type + ", address=" + address + ", customName=" + customName
                    + ", modifyFlag=" + modifyFlag + "]";
        }
        
        
    }

    public static class ImInfo {
        public long id;
        public int protocol;    // http://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Im.html
        public String account;
        public String customProtocol;
        public int    modifyFlag;   // default same
        @Override
        public String toString() {
            return "ImInfo [id=" + id + ", protocol=" + protocol + ", account=" + account + ", customName=" + customProtocol
                    + ", modifyFlag=" + modifyFlag + "]";
        }
        
    }

    public static class AddressInfo {
        public long id;
        public int type;    // http://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.StructuredPostal.html
        public String country;
        public String region;
        public String city;
        public String street;
        public String postcode;
        public String address;
        public String customName;
        public int    modifyFlag;   // default same
        @Override
        public String toString() {
            return "AddressInfo [id=" + id + ", country=" + country + ", region=" + region + ", city=" + city
                    + ", street=" + street + ", postcode=" + postcode + ", type=" + type + ", address=" + address
                    + ", customName=" + customName + ", modifyFlag=" + modifyFlag + "]";
        }
        
    }

    public static class OrgInfo {
        public long id;
        public int type;    // http://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Organization.html
        public String company;
        public String customName;
        public int    modifyFlag;   // default same
        @Override
        public String toString() {
            return "OrgInfo [id=" + id + ", type=" + type + ", company=" + company + ", customName=" + customName
                    + ", modifyFlag=" + modifyFlag + "]";
        }
        
    }
    
    public static class ModifyTag {
        public static final int same = 0;   //未修改
        public static final int add  = 1;   //增加
        public static final int del  = 2;   //删除
        public static final int edit = 3;   //修改
        
        public static String toString(int modifyTag) {
            switch (modifyTag) {
                case same:
                    return "same";
                case add:
                    return "add";
                case del:
                    return "delete";
                case edit:
                    return "edit";
                default:
                    throw new IllegalArgumentException("unknown modifyTag: " + modifyTag);
            }
        }
    }

    public static class RawContact {
        public long rawId;
        public int version;
        public AccountInfo accountInfo = new AccountInfo();
    }

    public static class DataModel {
        public long dataId;
        public String mimeType;
        public long rawId;
        public String data1;
        public int data2; // usually for the type
        public String data3; // usually for the user custom name
        public String data4;
        public int data5;
        public String data6;
        public String data7;
        public String data8;
        public String data9;
        public String data10;
        public byte[] data15;
        public GroupInfo gi = new GroupInfo();

    }

    public static class MimeType {
        public long id;
        public String mimeType;
    }

    public static class ContactVersion implements BaseColumns {
        public static final String VERSION = "version";

        public long id;
        public int version;
        public int modifyTag;   // default same

        public ContactVersion() {
            super();
        }

        public ContactVersion(long id, int version, int modifyTag) {
            super();
            this.id = id;
            this.version = version;
            this.modifyTag = modifyTag;
        }

        @Override
        public String toString() {
            return "ContactVersion [id=" + id + ", version=" + version + ", modifyTag=" + modifyTag + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (id ^ (id >>> 32));
            result = prime * result + modifyTag;
            result = prime * result + version;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ContactVersion other = (ContactVersion) obj;
            if (id != other.id)
                return false;
            if (modifyTag != other.modifyTag)
                return false;
            if (version != other.version)
                return false;
            return true;
        }

        
    }
}
