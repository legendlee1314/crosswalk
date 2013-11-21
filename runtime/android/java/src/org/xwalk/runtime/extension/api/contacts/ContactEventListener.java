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

public class ContactEventListener extends ContentObserver {
    private static final String TAG = "ContactsEventListener";

    private final Contacts mContacts;
    private final ContentResolver mResolver;

    private boolean mIsListening = false;
    private HashMap<String, String> mRawID2ContactIDMaps;
    private HashMap<String, String> mRawID2VersionMaps;
    private HashSet<String> mContactIDs;

    public ContactEventListener(Handler handler,
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
        checkChangedOnChange();
    }

    protected void startListening() {
        if (mIsListening) {
            return;
        }
        mIsListening = true;
        mContactIDs = getAllContactIDs();
        readAllRawContactInfo();
    }

    protected void onResume() {
        if (!mIsListening) {
            return;
        }
        checkChangedOnResume();
    }

    /** FIXME(guanxian): to check the changes will meet some problems.
     *  For one case -- add A, add B, modify B, remove A, remove C.
     *  Consider these circumstances:
     *  1. changed by `Contacts API`.
     *  -- API's operations can be united as one batch by database or async msg.
     *  -- a. Result: add A, add B, modify B and remove A, remove C. (batch)
     *  -- b. Result: add A, add B, modify B, remove A and remove C. (batch)
     *  -- c. Result: add A, add B, modify B, remove A, remove C. (individual)
     *  -- d. Result: Others.
     *  2. changed by User after `onPause()` and `onResume()`.
     *  -- These operations must be united as one batch.
     *  -- There're more than one operations can't be estimated.
     *  -- Result: add B and remove C. (united)
     *  So case 1 and 2 both needs batch solvement. If using same batch solvement
     *  `checkChangedOnResume()`, some test cases get unexpected randomly. Like
     *  when remove A and B, results are `remove A and modify B`, remove B through
     *  `Contact API`. This implementation considers each operation as individual
     *  at case 1. And consider them as batch while `onResume()` at case 2. The 
     *  previous results don't occur but others. Like when save A and update it too
     *  many times, unexpected result is `add A and modify A and remove A`.
     *  So maybe the `Contact API` leads to this problem, or because of other async
     *  problems. 
     */

    private void checkChangedOnChange() {
        // aim to solve changes by database.
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
                HashSet<String> modifiedIDs = compareAllRawContactInfo(contactIDs);
                if (modifiedIDs.size() != 0) {
                    jsonOutput.put("modified", convertSet2JSONArray(modifiedIDs));
                }
            }
            notifyContactChanged(jsonOutput);
            mContactIDs = contactIDs;
            readAllRawContactInfo();
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }

    private void checkChangedOnResume() {
        // aim to solve multiple changes after app resuming.
        try {
            JSONObject jsonOutput = new JSONObject();
            HashSet<String> contactIDs = getAllContactIDs();
            // calculate the added ids.
            HashSet<String> addedIDs = getDiffSet(contactIDs, mContactIDs);
            if (addedIDs.size() > 0) {
                jsonOutput.put("added", convertSet2JSONArray(addedIDs));
            }
            // calculate the removed ids.
            HashSet<String> removedIDs = getDiffSet(mContactIDs, contactIDs);
            if (removedIDs.size() > 0) {
                jsonOutput.put("removed", convertSet2JSONArray(removedIDs));
            }
            HashSet<String> commonIDs = getInterSet(mContactIDs, contactIDs);
            // calculate the modified ids.
            HashSet<String> modifiedIDs = compareAllRawContactInfo(commonIDs);
            if (modifiedIDs.size() != 0) {
                jsonOutput.put("modified", convertSet2JSONArray(modifiedIDs));
            }
            notifyContactChanged(jsonOutput);
            mContactIDs = contactIDs;
            readAllRawContactInfo();
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }

    private void notifyContactChanged(JSONObject outObject) {
        if (outObject == null || outObject.length() == 0) {
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

    private HashSet<String> getInterSet(HashSet<String> setA, HashSet<String> setB) {
        HashSet<String> resultSet = new HashSet<String>();
        // resultSet = setA  n  setB.
        resultSet.addAll(setA);
        resultSet.retainAll(setB);
        return resultSet;
    }

    private HashSet<String> getDiffSet(HashSet<String> setA, HashSet<String> setB) {
        HashSet<String> resultSet = new HashSet<String>();
        // resultSet = setA  -  setB.
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

    private HashSet<String> compareAllRawContactInfo(HashSet<String> commonSet) {
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
                if (contactID != null && commonSet.contains(contactID)) {
                    contactIDs.add(contactID);
                }
            }
        }

        return contactIDs;
    }
}
