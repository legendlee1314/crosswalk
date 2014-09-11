// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.core.internal.extension.api.ardrone;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ATCommandManager implements Runnable {
    public static final String TAG = "ATCommandManager";

    public static final int CMD_PORT = 5556;

    private ATCommandQueue mQueue;
    private DatagramSocket mDataSocket;
    private InetAddress mInetAddress;
    private int sequence;

    public ATCommandManager(ATCommandQueue queue, DatagramSocket socket, String remoteAddress) {
        this.mQueue = queue;
        this.mDataSocket = socket;
        this.sequence = 1;
        try {
            this.mInetAddress = InetAddress.getByName(remoteAddress);
        } catch (UnknownHostException e) {
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                ATCommand atCommand = mQueue.take();
                if (atCommand.getCommandType().equals("Quit")) {
                    break;
                }

                byte[] packetData = atCommand.buildPacketBytes(this.sequence);
                this.sequence += 1;
                DatagramPacket datagramPacket = new DatagramPacket(packetData, packetData.length,
                        this.mInetAddress, this.CMD_PORT);
                this.mDataSocket.send(datagramPacket);
            } catch (SocketException e) {
                break;
            } catch (IOException e) {
                break;
            }
        }
    }
}
