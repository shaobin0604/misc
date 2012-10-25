
package com.pekall.pctool.model.calendar;

import com.pekall.pctool.model.account.AccountInfo;

public class CalendarInfo {
    public long caId;
    public String name;
    public AccountInfo accountInfo = new AccountInfo();
    
    @Override
    public String toString() {
        return "CalendarInfo [caId=" + caId + ", name=" + name + ", accountInfo=" + accountInfo + "]";
    }
    
}
