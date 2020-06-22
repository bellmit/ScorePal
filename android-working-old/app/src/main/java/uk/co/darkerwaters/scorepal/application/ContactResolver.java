package uk.co.darkerwaters.scorepal.application;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContactResolver {

    private class Contact {
        final String name;
        String[] emails;
        final String imageUrl;
        Contact(String name, String email, String imageUrl) {
            this.name = name;
            this.emails = new String[] {email};
            this.imageUrl = imageUrl;
        }
        void addEmail(String email) {
            String[] oldEmails = this.emails;
            // expand the array
            this.emails = new String[oldEmails.length + 1];
            // fill with the old data
            for (int i = 0; i < oldEmails.length; ++i) {
                this.emails[i] = oldEmails[i];
            }
            // and the new one
            this.emails[this.emails.length - 1] = email;
        }
    }

    private final List<Contact> contactList;

    public ContactResolver(Context context) {
        // create the list
        this.contactList = new ArrayList<>();
        // get all the contacts from this phone so we can get details from it
        //TODO filtering on the search might be nicer code
        if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            Cursor cursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, null,
                    null, null);
            // while there are emails, add to the list of names we have (unique names)
            while (cursor.moveToNext()) {
                //Long id = cursor.getLong(cursor.getColumnIndex(Contacts.People._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String image = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

                //Uri imageUri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, id);
                // have we seen this one already?
                Contact contact = getContact(name);
                if (null == contact) {
                    // add this to the sorted list new
                    this.contactList.add(new Contact(name, email, image == null ? "" : image));
                } else {
                    // include the email on this one
                    contact.addEmail(email);
                }
            }
        }
        //else we cannot resolve contacts without permission
    }

    public List<String> getAllContacts() {
        List<String> names = new ArrayList<>();
        for (Contact contact : this.contactList) {
            names.add(contact.name);
        }
        return names;
    }

    private Contact getContact(String contactName) {
        for (Contact contact : this.contactList) {
            if (contact.name.equals(contactName)) {
                return contact;
            }
        }
        return null;
    }

    public boolean contactExists(String contactName) {
        return null != getContact(contactName);
    }

    public String getContactImage(String contactName) {
        Contact contact = getContact(contactName);
        if (null == contact) {
            return "";
        }
        else {
            return contact.imageUrl;
        }
    }

    public String[] getContactEmails(String contactName) {
        Contact contact = getContact(contactName);
        if (null == contact) {
            return new String[0];
        }
        else {
            return Arrays.copyOf(contact.emails, contact.emails.length);
        }
    }
}
