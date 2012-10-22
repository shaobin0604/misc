package com.pekall.pctool.model;

import android.content.Context;
import android.content.Intent;

import com.pekall.pctool.Slog;
import com.pekall.pctool.UpdateVersionDBService;
import com.pekall.pctool.model.calendar.CalendarUtil;
import com.pekall.pctool.model.calendar.EventInfo;
import com.pekall.pctool.model.calendar.EventInfo.EventVersion;
import com.pekall.pctool.model.contact.Contact;
import com.pekall.pctool.model.contact.Contact.ContactVersion;
import com.pekall.pctool.model.contact.Contact.ModifyTag;
import com.pekall.pctool.model.contact.ContactUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class FastSyncUtils {
    public static final String ACTION_UPDATE_CONTACT_VERSION = "action.UPDATE_CONTACT_VERSION";
    public static final String ACTION_UPDATE_EVENT_VERSION = "action.UPDATE_EVENT_VERSION";
    
    //
    // Contact fast sync
    //
    
    public static void notifyUpdateContactVersionDB(Context context) {
        Slog.d("notifyUpdateContactVersionDB E");
        Intent intent = new Intent(context, UpdateVersionDBService.class);
        intent.setAction(ACTION_UPDATE_CONTACT_VERSION);
        context.startService(intent);
        Slog.d("notifyUpdateContactVersionDB X");
    }
    
    public static List<Contact> findChangedPhoneContacts(Context context) {
        Slog.d("findChangedContacts E");
        
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        
        databaseHelper.open();
        Map<Long, Integer> lastSyncVersions = databaseHelper.getLastSyncPhoneContactVersions();
        databaseHelper.close();

        List<ContactVersion> currentVersions = ContactUtil.getPhoneContactVersions(context);
        
        List<ContactVersion> changedVersions = calculatePhoneContactChanges(currentVersions, lastSyncVersions);
        
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
            
            Slog.d(contact.toString());
            
            changedContacts.add(contact);
        }
        
        
        Slog.d("findChangedContacts X");
        return changedContacts;
    }
    
    public static List<ContactVersion> calculatePhoneContactChanges(List<ContactVersion> currentVersions, Map<Long, Integer> lastSyncVersions) {
        
        List<ContactVersion> changes = new ArrayList<ContactVersion>();
        
        for (ContactVersion contactVersion : currentVersions) {
            final Integer lastSyncVersion = lastSyncVersions.get(contactVersion.id);
            
            if (lastSyncVersion == null) {
                // cannot find record in last sync table, treat as add
                contactVersion.modifyTag = ModifyTag.add;
                
                changes.add(contactVersion);
            } else {
                if (lastSyncVersion.intValue() != contactVersion.version) {
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
    
    //
    // Calendar Event fast sync
    //
    
    public static void notifyUpdateEventVersionDB(Context context) {
        Slog.d("notifyUpdateEventVersionDB E");
        Intent intent = new Intent(context, UpdateVersionDBService.class);
        intent.setAction(ACTION_UPDATE_EVENT_VERSION);
        context.startService(intent);
        Slog.d("notifyUpdateEventVersionDB X");
    }
    
    public static List<EventInfo> findChangedEvents(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        
        databaseHelper.open();
        Map<Long, Long> lastSyncVersions = databaseHelper.getLastSyncEventVersions();
        databaseHelper.close();

        List<EventVersion> currentVersions = CalendarUtil.getEventVersions(context);
        
        List<EventVersion> changedVersions = calculateEventChanges(currentVersions, lastSyncVersions);
        
        //
        // Get the changed(add, update, delete) contacts
        //
        EventInfo eventInfo = null;
        List<EventInfo> changedEvents = new ArrayList<EventInfo>();
        for (EventVersion eventVersion : changedVersions) {
            switch (eventVersion.modifyTag) {
                case ModifyTag.add:
                case ModifyTag.edit:
                    eventInfo = CalendarUtil.queryEventById(context, eventVersion.id);
                    break;
                case ModifyTag.del:
                    eventInfo = new EventInfo();
                    eventInfo.id = eventVersion.id;
                    break;
                default:
                    final String msg = "Error invalid modifyTag = " + eventVersion.modifyTag;
                    Slog.e(msg);
                    throw new IllegalStateException(msg);
            }
            eventInfo.modifyTag = eventVersion.modifyTag;
            
            changedEvents.add(eventInfo);
        }
        return changedEvents;
    }
    
    public static List<EventVersion> calculateEventChanges(List<EventVersion> currentVersions, Map<Long, Long> lastSyncVersions) {
        
        List<EventVersion> changes = new ArrayList<EventVersion>();
        
        for (EventVersion eventVersion : currentVersions) {
            final Long lastSyncVersion = lastSyncVersions.get(eventVersion.id);
            
            if (lastSyncVersion == null) {
                // cannot find record in last sync table, treat as add
                eventVersion.modifyTag = ModifyTag.add;
                
                changes.add(eventVersion);
            } else {
                if (lastSyncVersion != eventVersion.version) {
                    // version changed, treat as update
                    eventVersion.modifyTag = ModifyTag.edit;
                    
                    changes.add(eventVersion);
                }
                lastSyncVersions.remove(eventVersion.id);
            }
        }
        
        for (Entry<Long, Long> entry : lastSyncVersions.entrySet()) {
            changes.add(new EventVersion(entry.getKey(), entry.getValue(), ModifyTag.del));
        }
        
        return changes;
    }
}
