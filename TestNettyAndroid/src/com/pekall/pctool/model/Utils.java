package com.pekall.pctool.model;

import com.pekall.pctool.model.contact.Contact.ContactVersion;
import com.pekall.pctool.model.contact.Contact.ModifyTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Utils {
    public static List<ContactVersion> findContactChanges(List<ContactVersion> current, Map<Long, Integer> lastSync) {
        
        List<ContactVersion> changes = new ArrayList<ContactVersion>();
        
        for (ContactVersion contactVersion : current) {
            final Integer lastSyncVersion = lastSync.get(contactVersion.id);
            
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
                lastSync.remove(contactVersion.id);
            }
        }
        
        for (Entry<Long, Integer> entry : lastSync.entrySet()) {
            changes.add(new ContactVersion(entry.getKey(), entry.getValue(), ModifyTag.del));
        }
        
        return changes;
    }
}
