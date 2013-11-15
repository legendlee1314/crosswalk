// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.runtime.extension.api.contacts;

import org.xwalk.runtime.extension.api.Contacts.ContactBuilder;
import org.xwalk.runtime.extension.api.Contacts.ContactFinder;
import org.xwalk.runtime.extension.XWalkExtension;
import org.xwalk.runtime.extension.XWalkExtensionContext;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class Contacts extends XWalkExtension {
    public static final String NAME = "navigator.contacts";
    public static final String JS_API_PATH = "jsapi/contacts_api.js";

    private static final String TAG = "XWalkExtensionAPIContacts";

    private final ContactBuilder mBuilder;
    private final ContactsEventListener mObserver;
    private final ContentResolver mResolver;

    public Contacts(String jsApiContent, XWalkExtensionContext context) {
        super(NAME, jsApiContent, context);
        mResolver = context.getContext().getContentResolver();
        mBuilder = new ContactBuilder(mResolver, TAG);
        mObserver = new ContactsEventListener(new Handler(), this, mResolver);
        mResolver.registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI, true, mObserver);
    }

    @Override
    public void onMessage(int instanceID, String message) {
        if (message.isEmpty()) {
            return;
        }
        try {
            JSONObject jsonInput = new JSONObject(message);
            String cmd = jsonInput.getString("cmd");
            if (cmd.equals("addEventListener")) {
                String eventName = jsonInput.getString("eventName");
                if (eventName.equals("oncontactschange")) {
                    mObserver.startListening();
                }
                return;
            }
            JSONObject jsonOutput = new JSONObject();
            jsonOutput.put("_promise_id", jsonInput.getString("_promise_id"));
            if (cmd.equals("save")) {
                jsonOutput.put("data",
                        mBuilder.init(jsonInput.getString("contact")).build());
            } else if (cmd.equals("find")) {
                ContactFinder f = new ContactFinder(mResolver,
                        jsonInput.getString("options"), TAG);
                jsonOutput.put("data", f.find());
            } else if (cmd.equals("remove")) {
                ArrayList<ContentProviderOperation> ops =
                        new ArrayList<ContentProviderOperation>();
                String[] args = new String[] { jsonInput.getString("contactId") };
                ops.add(ContentProviderOperation.newDelete(RawContacts.CONTENT_URI)
                        .withSelection(RawContacts.CONTACT_ID + "=?", args).build());
                try {
                    mResolver.applyBatch(ContactsContract.AUTHORITY, ops);
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to apply batch to delete contacts: " + e);
                } catch (OperationApplicationException e) {
                    Log.e(TAG, "Failed to apply batch to delete contacts: " + e);
                }
            }
            this.postMessage(instanceID, jsonOutput.toString());
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onDestroy() {
        mResolver.unregisterContentObserver(mObserver);
    }
}
