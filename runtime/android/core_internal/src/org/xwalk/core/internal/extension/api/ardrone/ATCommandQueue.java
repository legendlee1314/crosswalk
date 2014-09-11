// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.core.internal.extension.api.ardrone;

import android.util.Log;

import java.util.concurrent.PriorityBlockingQueue;

public class ATCommandQueue {
    private static final String TAG = "ATCommandQueue"; 

    private int mMaxSize;
    private PriorityBlockingQueue<ATCommand> mQueue;

    public ATCommandQueue(int maxSize) {
        this.mMaxSize = maxSize;
        this.mQueue = new PriorityBlockingQueue<ATCommand>(maxSize);
    }

    public void add(ATCommand atCommand) {
        // FIXME(guanxian): ignore the more commands.
        if (mQueue.size() < this.mMaxSize) {
            mQueue.add(atCommand);
        }
    }

    public ATCommand take() {
        ATCommand atCommand = null;
        try {
            atCommand = mQueue.take();
        } catch (InterruptedException e) {
            return null;
        }

        return atCommand;
    }
}
