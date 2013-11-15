// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.runtime.extension.api.contacts;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ContactsEventListener extends ContentObserver {
    private static final String TAG = "ContactsEventListener";

    private boolean mIsListening = false;
    private final Contacts mContacts;
    private final ContentResolver mResolver;

    private HashMap<String, String> mRawID2ContactIDMaps;
    private HashMap<String, String> mRawID2VersionMaps;
    private HashSet<String> mContactIDs;

    public ContactsEventListener(Handler handler,
            Contacts instance, ContentResolver resolver) {
        super(handler);
        this.mContacts = instance;
        this.mResolver = resolver;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        if (!mIsListening) {
            return;
        }
        try {
            JSONObject jsonOutput = new JSONObject();
            HashSet<String> contactIDs = getAllContactIDs();
            if (contactIDs.size() > mContactIDs.size()) {
                // calculate the added ids.
                HashSet<String> addedIDs = getDiffSet(contactIDs, mContactIDs);
                jsonOutput.put("added", convertSet2JSONArray(addedIDs));
            } else if (contactIDs.size() < mContactIDs.size()) {
                // calculate the removed ids.
                HashSet<String> removedIDs = getDiffSet(mContactIDs, contactIDs);
                jsonOutput.put("removed", convertSet2JSONArray(removedIDs));
            } else {
                // calculate the modified ids.
                HashSet<String> modifiedIDs = compareAllRawContactInfo();
                if (modifiedIDs.size() == 0) {
                    return;
                }
                jsonOutput.put("modified", convertSet2JSONArray(modifiedIDs));
            }
            notifyContactChanged(jsonOutput);
            mContactIDs = contactIDs;
            readAllRawContactInfo();
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }

    protected void startListening() {
        if (mIsListening) {
            return;
        }
        mIsListening = true;
        mContactIDs = getAllContactIDs();
        readAllRawContactInfo();
        Log.d(TAG, "start listening");
    }

    private void notifyContactChanged(JSONObject outObject) {
        if (outObject == null) {
            return;
        }
        try {
            JSONObject jsonOutput = new JSONObject();
            jsonOutput.put("reply", "oncontactschange");
            jsonOutput.put("eventName", "oncontactschange");
            jsonOutput.put("data", outObject);
            mContacts.broadcastMessage(jsonOutput.toString());
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }

    private JSONArray convertSet2JSONArray(HashSet<String> set) {
        JSONArray jsonArray = new JSONArray();
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            jsonArray.put(iterator.next());
        }
        return jsonArray;
    }

    private HashSet<String> getAllContactIDs() {
        HashSet<String> contactIDs = new HashSet<String>();

        Cursor cursor = mResolver.query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String contactID = cursor.getString(
                      cursor.getColumnIndex(ContactsContract.Contacts._ID));
            contactIDs.add(contactID);
        }
        cursor.close();

        return contactIDs;
    }

    private HashSet<String> getDiffSet(HashSet<String> setA, HashSet<String> setB) {
        HashSet<String> resultSet = new HashSet<String>();
        // resultSet = setA - setB.
        resultSet.addAll(setA);
        resultSet.removeAll(setB);
        return resultSet;
    }

    private void readAllRawContactInfo() {
        mRawID2ContactIDMaps = new HashMap<String, String>();
        mRawID2VersionMaps = new HashMap<String, String>();

        Cursor cursor = mResolver.query(
                RawContacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String contactID =
                    cursor.getString(cursor.getColumnIndex(RawContacts.CONTACT_ID));
            String rawContactID =
                    cursor.getString(cursor.getColumnIndex(RawContacts._ID));
            String version =
                    cursor.getString(cursor.getColumnIndex(RawContacts.VERSION));
            mRawID2ContactIDMaps.put(rawContactID, contactID);
            mRawID2VersionMaps.put(rawContactID, version);
        }
        cursor.close();
    }

    private HashSet<String> compareAllRawContactInfo() {
        HashSet<String> contactIDs = new HashSet<String>();
        HashMap<String, String> compareMaps = new HashMap<String, String>();

        Cursor cursor = mResolver.query(
                RawContacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String rawContactID =
                    cursor.getString(cursor.getColumnIndex(RawContacts._ID));
            String version =
                    cursor.getString(cursor.getColumnIndex(RawContacts.VERSION));
            compareMaps.put(rawContactID, version);
        }
        cursor.close();

        Iterator<String> iterator = compareMaps.keySet().iterator();
        while (iterator.hasNext()) {
            String rawContactID = iterator.next();
            String newVersion = compareMaps.get(rawContactID);
            String oldVersion = mRawID2VersionMaps.get(rawContactID);
            if (oldVersion == null || !newVersion.equals(oldVersion)) {
                String contactID = mRawID2ContactIDMaps.get(rawContactID);
                if (contactID != null && mContactIDs.contains(contactID)) {
                    contactIDs.add(contactID);
                }
            }
        }

        return contactIDs;
    }
}
