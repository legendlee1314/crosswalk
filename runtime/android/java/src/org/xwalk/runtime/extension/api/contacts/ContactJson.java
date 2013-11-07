package org.xwalk.runtime.extension.api.Contacts;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class ContactJson {

    private static final String TAG = "ContactJson";

    private JSONObject mObject;
    public ContactJson(JSONObject o) {
        this.mObject = o;
    }

    public ContactJson(String init) {
        try {
            mObject = new JSONObject(init);
        } catch (JSONException e) {
            Log.e(TAG, "Init JSON by " + init + " failed: "+e);
        }
    }

    public List<String> getStringArray(String name) {
        List<String> list = new ArrayList<String>();
        if (mObject != null && mObject.has(name)) {
            try {
                JSONArray jsonArray = mObject.getJSONArray(name);
                for (int i=0; i<jsonArray.length(); i++) {
                    list.add( jsonArray.getString(i) );
                }
            } catch (JSONException e) {
                Log.e(TAG, "getArrayFirstValue(" + name + "): Failed to parse json data: "+e);
            }
        }
        return list;
    }

    public String getArrayFirstValue(String name) {
        String value = null;
        if (mObject != null && mObject.has(name)) {
            try {
                value = mObject.getJSONArray(name).getString(0);
            } catch (JSONException e) {
                Log.e(TAG, "getArrayFirstValue(" + name + "): Failed to parse json data: "+e);
            }
        }
        return value;
    }

    public String getString(String name) {
        String value = null;
        if (mObject != null && mObject.has(name)) {
            try {
                value = mObject.getString(name);
            } catch (JSONException e) {
                Log.e(TAG, "getString(" + name + "): Failed to parse json data: "+e);
            }
        }
        return value;
    }

    public boolean getBoolean(String name) {
        boolean value = false;
        if (mObject != null && mObject.has(name)) {
            try {
                value = mObject.getBoolean(name);
            } catch (JSONException e) {
                Log.e(TAG, "getBoolean(" + name + "): Failed to parse json data: "+e);
            }
        }
        return value;
    }

    public JSONObject getObject(String name) {
        JSONObject o = null;
        if (mObject != null && mObject.has(name)) {
            try {
                o = mObject.getJSONObject(name);
            } catch (JSONException e) {
                Log.e(TAG, "getObject(" + name + "): Failed to parse json data: "+e);
            }
        }
        return o;
    }
}
