// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.runtime.extension.api.contacts;

import android.util.Log;

import org.xwalk.runtime.extension.api.Contacts.ContactBuilder;
import org.xwalk.runtime.extension.XWalkExtension;
import org.xwalk.runtime.extension.XWalkExtensionContext;

import org.json.JSONException;
import org.json.JSONObject;

public class Contacts extends XWalkExtension {
    public static final String NAME = "navigator.contacts";
    public static final String JS_API_PATH = "jsapi/contacts_api.js";

    private static final String TAG = "ContactsManager";

    private ContactBuilder mBuilder;

    public Contacts(String jsApiContent, XWalkExtensionContext context) {
        super(NAME, jsApiContent, context);
        mBuilder = new ContactBuilder(context.getContext().getContentResolver(), TAG);
    }

    @Override
    public void onMessage(int instanceID, String message) {
        if (!message.isEmpty()) {
            try {
                JSONObject jsonInput = new JSONObject(message);
                String cmd = jsonInput.getString("cmd");

                JSONObject jsonOutput = new JSONObject();
                jsonOutput.put("_promise_id", jsonInput.getString("_promise_id"));
                if (cmd.equals("save")) {
                    jsonOutput.put("data",
                            mBuilder.init(jsonInput.getString("contact"))
                                    .build());
                }
                this.postMessage(instanceID, jsonOutput.toString());
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}
