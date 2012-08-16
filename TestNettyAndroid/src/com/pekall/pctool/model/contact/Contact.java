
package com.pekall.pctool.model.contact;

import com.pekall.pctool.model.account.AccountInfo;

import java.util.ArrayList;
import java.util.List;

public class Contact {
    public long id;// rawId
    public String name;
    public String nickname;
    public byte[] photo;
    public AccountInfo accountInfo = new AccountInfo();
    public List<PhoneInfo> phoneInfos = new ArrayList<PhoneInfo>();
    public List<EmailInfo> emailInfos = new ArrayList<EmailInfo>();
    public List<ImInfo> imInfos = new ArrayList<ImInfo>();
    public List<AddressInfo> addressInfos = new ArrayList<AddressInfo>();
    public List<OrgInfo> orgInfos = new ArrayList<OrgInfo>();
    public List<GroupInfo> groupInfos = new ArrayList<GroupInfo>();

    public static class PhoneInfo {
        public long id;
        public int type;
        public String number;
        public String customName;
        public int    modifyFlag;
    }

    public static class EmailInfo {
        public long id;
        public int type;
        public String email;
        public String customName;
        public int    modifyFlag;
    }

    public static class ImInfo {
        public long id;
        public int type;
        public String im;
        public String customName;
        public int    modifyFlag;
    }

    public static class AddressInfo {
        public long id;
        public String country;
        public String province;
        public String city;
        public String street;
        public String postcode;
        public int type;
        public String address;
        public String customName;
        public int    modifyFlag;
    }

    public static class OrgInfo {
        public long id;
        public int type;
        public String org;
        public String customName;
        public int    modifyFlag;
    }
    
    public static class ModifyTag {
        public static int  same = 0;   //未修改
        public static int  add = 1;    //增加
        public static int  del = 2;    //删除
        public static int  edit = 3;   //修改
    }

}
