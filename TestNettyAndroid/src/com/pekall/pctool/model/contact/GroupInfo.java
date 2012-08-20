
package com.pekall.pctool.model.contact;

import com.pekall.pctool.model.account.AccountInfo;

public class GroupInfo {

    public String name;
    public String note;
    public AccountInfo accountInfo = new AccountInfo();
    public long grId;
    public int modifyFlag;
    public long dataId;
    
    public void setAccountInfo(String name, String type) {
        accountInfo.accountName = name;
        accountInfo.accountType = type;
    }

}
