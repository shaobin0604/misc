
package com.pekall.pctool.model.contact;

import com.pekall.pctool.model.account.AccountInfo;

import java.util.ArrayList;
import java.util.List;

public class Contact {
    public long id;// rawId
    public String name;
    public String nickname;
    public byte[] photo;
    public boolean shouldUpdatePhoto;
    public AccountInfo accountInfo = new AccountInfo();
    public List<GroupInfo> groupInfos = new ArrayList<GroupInfo>();
    public List<PhoneInfo> phoneInfos = new ArrayList<PhoneInfo>();
    public List<EmailInfo> emailInfos = new ArrayList<EmailInfo>();
    public List<ImInfo> imInfos = new ArrayList<ImInfo>();
    public List<AddressInfo> addressInfos = new ArrayList<AddressInfo>();
    public List<OrgInfo> orgInfos = new ArrayList<OrgInfo>();
    
    @Override
    public String toString() {
        return "Contact [id=" + id + ", name=" + name + ", nickname=" + nickname + ", accountInfo=" + accountInfo
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
        public int    modifyFlag;
        @Override
        public String toString() {
            return "PhoneInfo [id=" + id + ", type=" + type + ", number=" + number + ", customName=" + customName
                    + ", modifyFlag=" + modifyFlag + "]";
        }
        
        
    }

    public static class EmailInfo {
        public long id;
        public int type;    // http://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Email.html
        public String email;
        public String customName;
        public int    modifyFlag;
        @Override
        public String toString() {
            return "EmailInfo [id=" + id + ", type=" + type + ", email=" + email + ", customName=" + customName
                    + ", modifyFlag=" + modifyFlag + "]";
        }
        
        
    }

    public static class ImInfo {
        public long id;
        public int type;    // http://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Im.html
        public String account;
        public String customName;
        public int    modifyFlag;
        @Override
        public String toString() {
            return "ImInfo [id=" + id + ", type=" + type + ", account=" + account + ", customName=" + customName
                    + ", modifyFlag=" + modifyFlag + "]";
        }
        
    }

    public static class AddressInfo {
        public long id;
        public String country;
        public String province;
        public String city;
        public String street;
        public String postcode;
        public int type;    // http://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.StructuredPostal.html
        public String address;
        public String customName;
        public int    modifyFlag;
        @Override
        public String toString() {
            return "AddressInfo [id=" + id + ", country=" + country + ", province=" + province + ", city=" + city
                    + ", street=" + street + ", postcode=" + postcode + ", type=" + type + ", address=" + address
                    + ", customName=" + customName + ", modifyFlag=" + modifyFlag + "]";
        }
        
    }

    public static class OrgInfo {
        public long id;
        public int type;    // http://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Organization.html
        public String org;
        public String customName;
        public int    modifyFlag;
        @Override
        public String toString() {
            return "OrgInfo [id=" + id + ", type=" + type + ", org=" + org + ", customName=" + customName
                    + ", modifyFlag=" + modifyFlag + "]";
        }
        
    }
    
    public static class ModifyTag {
        public static int  same = 0;   //未修改
        public static int  add = 1;    //增加
        public static int  del = 2;    //删除
        public static int  edit = 3;   //修改
    }

}
