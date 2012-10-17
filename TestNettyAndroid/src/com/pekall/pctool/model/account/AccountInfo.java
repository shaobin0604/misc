package com.pekall.pctool.model.account;

public class AccountInfo {

    public String accountName;
    public String accountType;
    
    @Override
    public String toString() {
        return "AccountInfo [accountName=" + accountName + ", accountType=" + accountType + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accountName == null) ? 0 : accountName.hashCode());
        result = prime * result + ((accountType == null) ? 0 : accountType.hashCode());
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
        AccountInfo other = (AccountInfo) obj;
        if (accountName == null) {
            if (other.accountName != null)
                return false;
        } else if (!accountName.equals(other.accountName))
            return false;
        if (accountType == null) {
            if (other.accountType != null)
                return false;
        } else if (!accountType.equals(other.accountType))
            return false;
        return true;
    }
}
