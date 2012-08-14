
package com.pekall.pctool.model.contact;

import java.util.ArrayList;
import java.util.List;

public class Contact {
    public int _ID;// rawId
    public String name;
    public String nickname;
    public List<PhoneRecord> phoneRecord = new ArrayList<PhoneRecord>();
    public List<EmailRecord> emailRecord = new ArrayList<EmailRecord>();
    public List<IMRecord> imRecord = new ArrayList<IMRecord>();
    public List<AddressRecord> addressRecord = new ArrayList<AddressRecord>();
    public List<OrgRecord> orgRecord = new ArrayList<OrgRecord>();
    public AccountInfo accountInfo = new AccountInfo();
    public GroupInfo groupInfo = new GroupInfo();

    public static class PhoneRecord {
        public long type;
        public String number;
        public String customName;
    }

    public static class EmailRecord {
        public long type;
        public String email;
        public String customName;
    }

    public static class IMRecord {
        public long type;
        public String im;
        public String customName;
    }

    public static class AddressRecord {
        public long type;
        public String address;
        public String customName;
    }

    public static class OrgRecord {
        public long type;
        public String org;
        public String customName;
    }

}
