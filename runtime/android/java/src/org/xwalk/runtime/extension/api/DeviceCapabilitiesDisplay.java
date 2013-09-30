// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.runtime.extension.api;

import org.xwalk.runtime.extension.XWalkExtensionContext;

import android.content.Context;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DeviceCapabilitiesDisplay {
    private static final String TAG = "DeviceCapabilitiesDisplay";

    private DeviceCapabilities mDeviceCapabilities;

    private Context mContext;
    private DisplayManager mDisplayManager;
    private XWalkExtensionContext mExtensionContext;

    private boolean mIsRegisteredOnConnect = false;
    private boolean mIsRegisteredOnDisconnect = false;

    private boolean mIsInternal;
    private boolean mIsPrimary;
    private int mDpiX;
    private int mDpiY;
    private long mAvailableHeight;
    private long mAvailableWidth;
    private long mHeight;
    private long mWidth;
    private String mDisplayId;
    private String mDisplayName;

    private final Handler mHandler = new Handler();

    private final DisplayListener mOnConnectListener = new DisplayListener() {
        @Override
        public void onDisplayAdded(int displayId) {
            JSONObject displayObject = new JSONObject();
            try {
                displayObject.put("cmd", "connectDisplay");
                displayObject.put("eventName", "onconnect");
                displayObject.put("content", displayId);
            } catch (JSONException e) {
                mDeviceCapabilities.postMessage("display listener error");
            }
            mDeviceCapabilities.postMessage(displayObject.toString());
        }
        @Override
        public void onDisplayChanged(int arg0) {
        }
        @Override
        public void onDisplayRemoved(int arg0) {
        }
    };

    private final DisplayListener mOnDisconnectListener = new DisplayListener() {
        @Override
        public void onDisplayAdded(int displayId) {
        }
        @Override
        public void onDisplayChanged(int arg0) {
        }
        @Override
        public void onDisplayRemoved(int displayId) {
            JSONObject displayObject = new JSONObject();
            try {
                displayObject.put("cmd", "disconnectDisplay");
                displayObject.put("eventName", "ondisconnect");
                displayObject.put("content", displayId);
            } catch (JSONException e) {
                mDeviceCapabilities.postMessage("display listener error");
            }
            mDeviceCapabilities.postMessage(displayObject.toString());
        }
    };

    public DeviceCapabilitiesDisplay(DeviceCapabilities instance) {
        mDeviceCapabilities = instance;
    }

    public void setExtensionContext(XWalkExtensionContext context) {
        mExtensionContext = context;
    }

    public JSONArray getInfo() {
        JSONArray outputArray = new JSONArray();

        mContext = mExtensionContext.getContext();
        mDisplayManager =
                (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        Display[] dispArr = mDisplayManager.getDisplays();

        for (Display disp : dispArr) {
            JSONObject displayObject = new JSONObject();
            if (getDisplayInfo(disp)) {
                try {
                    displayObject.put("id", mDisplayId);
                    displayObject.put("mDisplayName", mDisplayName);
                    displayObject.put("mIsPrimary", mIsPrimary);
                    displayObject.put("isInternal", mIsInternal);
                    displayObject.put("mDpiX", mDpiX);
                    displayObject.put("mDpiY", mDpiY);
                    displayObject.put("mWidth", mWidth);
                    displayObject.put("mHeight", mHeight);
                    displayObject.put("mAvailableWidth", mAvailableWidth);
                    displayObject.put("mAvailableHeight", mAvailableHeight);
                } catch (JSONException e) {
                    return outputArray;
                }
                outputArray.put(displayObject);
            }
        }
        return outputArray;
    }

    public void registerOnConnectListener() {
        mContext = mExtensionContext.getContext();
        mDisplayManager =
                (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        if (!mIsRegisteredOnConnect) {
            mDisplayManager.registerDisplayListener(mOnConnectListener, mHandler);
            mIsRegisteredOnConnect = true;
        }
    }

    public void registerOnDisonnectListener() {
        mContext = mExtensionContext.getContext();
        mDisplayManager =
                (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        if (!mIsRegisteredOnDisconnect) {
            mDisplayManager.registerDisplayListener(mOnDisconnectListener, mHandler);
            mIsRegisteredOnDisconnect = true;
        }
    }

    public void unregisterListener() {
        if (mIsRegisteredOnConnect) {
            mDisplayManager.unregisterDisplayListener(mOnConnectListener);
        }
        if (mIsRegisteredOnDisconnect) {
            mDisplayManager.unregisterDisplayListener(mOnDisconnectListener);
        }
        mIsRegisteredOnConnect = false;
        mIsRegisteredOnDisconnect = false;
    }

    private boolean getDisplayInfo(Display display) {
        mDisplayId = String.valueOf(display.getDisplayId());
        mDisplayName = display.getName();
        if (mDisplayId.equals(String.valueOf(display.DEFAULT_DISPLAY))) {
            mIsPrimary = true;
            mIsInternal = true;
        } else {
            mIsPrimary = false;
            mIsInternal = false;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        mDpiX = (int) displayMetrics.xdpi;
        mDpiY = (int) displayMetrics.ydpi;
        Point outSize = new Point();
        display.getRealSize(outSize);
        mWidth = outSize.x;
        mHeight = outSize.y;
        display.getSize(outSize);
        mAvailableWidth = outSize.x;
        mAvailableHeight = outSize.y;
        return true;
    }
}
