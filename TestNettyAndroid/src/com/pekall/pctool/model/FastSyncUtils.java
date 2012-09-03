package com.pekall.pctool.model;

import android.content.Context;
import android.content.Intent;

import com.pekall.pctool.Slog;
import com.pekall.pctool.UpdateContactVersionDBService;
import com.pekall.pctool.model.contact.Contact;
import com.pekall.pctool.model.contact.Contact.ContactVersion;
import com.pekall.pctool.model.contact.Contact.ModifyTag;
import com.pekall.pctool.model.contact.ContactUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class FastSyncUtils {
    
    public static void notifyUpdateContactVersionDB(Context context) {
        Slog.d("notifyUpdateContactVersionDB E");
        Intent intent = new Intent(context, UpdateContactVersionDBService.class);
        context.startService(intent);
        Slog.d("notifyUpdateContactVersionDB X");
    }
    
    public static List<Contact> findChangedContacts(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        
        databaseHelper.open();
        Map<Long, Integer> lastSyncVersions = databaseHelper.getLastSyncContactVersions();
        databaseHelper.close();

        List<ContactVersion> currentVersions = ContactUtil.getAllContactVersions(context);
        
        List<ContactVersion> changedVersions = calculateContactChanges(currentVersions, lastSyncVersions);
        
        //
        // Get the changed(add, update, delete) contacts
        //
        Contact contact = null;
        List<Contact> changedContacts = new ArrayList<Contact>();
        for (ContactVersion contactVersion : changedVersions) {
            switch (contactVersion.modifyTag) {
                case ModifyTag.add:
                case ModifyTag.edit:
                    contact = ContactUtil.getContactById(context, contactVersion.id);
                    break;
                case ModifyTag.del:
                    contact = new Contact();
                    contact.id = contactVersion.id;
                    break;
                default:
                    final String msg = "Error invalid modifyTag = " + contactVersion.modifyTag;
                    Slog.e(msg);
                    throw new IllegalStateException(msg);
            }
            contact.modifyTag = contactVersion.modifyTag;
            
            changedContacts.add(contact);
        }
        return changedContacts;
    }
    
    public static List<ContactVersion> calculateContactChanges(List<ContactVersion> currentVersions, Map<Long, Integer> lastSyncVersions) {
        
        List<ContactVersion> changes = new ArrayList<ContactVersion>();
        
        for (ContactVersion contactVersion : currentVersions) {
            final Integer lastSyncVersion = lastSyncVersions.get(contactVersion.id);
            
            if (lastSyncVersion == null) {
                // cannot find record in last sync table, treat as add
                contactVersion.modifyTag = ModifyTag.add;
                
                changes.add(contactVersion);
            } else {
                if (lastSyncVersion != contactVersion.version) {
                    // version changed, treat as update
                    contactVersion.modifyTag = ModifyTag.edit;
                    
                    changes.add(contactVersion);
                }
                lastSyncVersions.remove(contactVersion.id);
            }
        }
        
        for (Entry<Long, Integer> entry : lastSyncVersions.entrySet()) {
            changes.add(new ContactVersion(entry.getKey(), entry.getValue(), ModifyTag.del));
        }
        
        return changes;
    }
}
