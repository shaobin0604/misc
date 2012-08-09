
package com.pekall.pctool.model;

import android.content.Context;

import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.Person;
import com.pekall.pctool.model.app.AppUtil;
import com.pekall.pctool.protos.AppInfoProtos.AppInfoPList;

public class FakeBusinessLogicFacade {

    private Context mContext;

    public FakeBusinessLogicFacade(Context context) {
        this.mContext = context;
    }

    public AddressBook getAddressBook() {
        AddressBook.Builder builder = AddressBook.newBuilder();
        builder.addPerson(getPerson("李雷", 1));
        builder.addPerson(getPerson("韩梅梅", 2));
        return builder.build();
    }

    private Person getPerson(String name, int id) {
        return Person.newBuilder().setName(name).setId(id).build();
    }
    
    public AppInfoPList getAppInfoPList() {
        return AppUtil.getAppInfoPList(mContext);
    }

}
