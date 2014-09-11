// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.core.internal.extension.api.ardrone;

import org.xwalk.core.internal.extension.XWalkExtensionWithActivityStateListener;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramSocket;

import org.chromium.base.ActivityState;
import org.json.JSONException;
import org.json.JSONObject;

public class ARDrone extends XWalkExtensionWithActivityStateListener {
    public static final String JS_API_PATH = "jsapi/ardrone_api.js";

    private static final String NAME = "xwalk.experimental.system";
    private static final String TAG = "ARDrone";

    private ATCommandManager mATCommandManager;
    private ATCommandQueue mQueue;
    private DatagramSocket mDataSocket;
    private String mLocalAddress;
    private String mRemoteAddress;
    private Thread mCommandThread;

    public ARDrone(String jsApiContent, Activity activity) {
        super(NAME, jsApiContent, activity);
        this.mQueue = new ATCommandQueue(10);
        this.mLocalAddress = "192.168.1.2";
        this.mRemoteAddress = "192.168.1.1";
    }

    private void handleMessage(int instanceID, String message) {
        try {
            JSONObject jsonInput = new JSONObject(message);
            String cmd = jsonInput.getString("cmd");
            String asyncCallId = jsonInput.getString("asyncCallId");
            handle(instanceID, asyncCallId, cmd);
        } catch (JSONException e) {
            printErrorMessage(e);
        }
    }

    private void handle(int instanceID, String asyncCallId, String cmd) {
        try {
            JSONObject jsonOutput = new JSONObject();

            if (cmd.equals("connect")) {
                jsonOutput.put("data", connect());
            } else if (cmd.equals("takeoff")) {
                jsonOutput.put("data", takeoff());
            } else if (cmd.equals("landing")) {
                jsonOutput.put("data", landing());
            }

            this.postMessage(instanceID, jsonOutput.toString());
        } catch (JSONException e) {
            printErrorMessage(e);
        }
    }

    private JSONObject connect() {
        JSONObject out = new JSONObject();

        try {
            this.mDataSocket = new DatagramSocket();
            this.mATCommandManager = new ATCommandManager(this.mQueue, this.mDataSocket, this.mRemoteAddress);
            this.mCommandThread = new Thread(this.mATCommandManager);
            this.mCommandThread.start();
        } catch (IOException e) {
            return setErrorMessage(e.toString());
        }

        try {
            out.put("results", "connect: true");
        } catch (JSONException e) {
            return setErrorMessage(e.toString());
        }

        return out;
    }

    private JSONObject takeoff() {
        mQueue.add(new ATCommand(new TakeoffCommand()));

        JSONObject out = new JSONObject();
        try {
            out.put("results", "takeoff: true");
        } catch (JSONException e) {
            return setErrorMessage(e.toString());
        }

        return out;
    }

    private JSONObject landing() {
        mQueue.add(new ATCommand(new LandingCommand()));
        
        JSONObject out = new JSONObject();
        try {
            out.put("results", "landing: true");
        } catch (JSONException e) {
            return setErrorMessage(e.toString());
        }

        return out;
    }

    protected void printErrorMessage(JSONException e) {
        Log.e(TAG, e.toString());
    }

    protected JSONObject setErrorMessage(String error) {
        JSONObject out = new JSONObject();
        JSONObject errorMessage = new JSONObject();
        try {
            errorMessage.put("message", error);
            out.put("error", errorMessage);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        return out;
    }

    @Override
    public void onMessage(int instanceID, String message) {
        if (!message.isEmpty()) {
            handleMessage(instanceID, message);
        }
    }

    @Override
    public void onActivityStateChange(Activity activity, int newState) {
        switch (newState) {
            case ActivityState.RESUMED:
                break;
            case ActivityState.PAUSED:
                break;
            case ActivityState.DESTROYED:
                this.mQueue.add(new ATCommand(new QuitCommand()));
                this.mDataSocket.close();
                break;
            default:
                break;
        }
    }

    @Override
    public String onSyncMessage(int instanceID, String message) {
        return null;
    }
}
