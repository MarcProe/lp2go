/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

/* This file incorporates work covered by the following copyright and
 * permission notice:
 */

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.proest.lp2go3.UAVTalk;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.util.Log;

import net.proest.lp2go3.H;
import net.proest.lp2go3.MainActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class UAVTalkUsbDevice extends UAVTalkDevice {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    //private final MainActivity mActivity;
    private final UsbDeviceConnection mDeviceConnection;
    private final UsbEndpoint mEndpointOut;
    private final UsbEndpoint mEndpointIn;

    private final LinkedList<UsbRequest> mOutRequestPool = new LinkedList<UsbRequest>();
    private final LinkedList<UsbRequest> mInRequestPool = new LinkedList<UsbRequest>();

    private final WaiterThread mWaiterThread = new WaiterThread();
    private volatile UAVTalkObjectTree mObjectTree;
    private String mSerial;

    private boolean connected = false;

    public UAVTalkUsbDevice(MainActivity activity, UsbDeviceConnection connection,
                            UsbInterface intf, HashMap<String, UAVTalkXMLObject> xmlObjects) {
        super(activity);

        //mActivity = activity;
        mDeviceConnection = connection;
        mSerial = connection.getSerial();
        this.mObjectTree = new UAVTalkObjectTree();
        mObjectTree.setXmlObjects(xmlObjects);
        mActivity.setPollThreadObjectTree(mObjectTree);

        UsbEndpoint epOut = null;
        UsbEndpoint epIn = null;
        // look for our bulk endpoints
        for (int i = 0; i < intf.getEndpointCount(); i++) {
            UsbEndpoint ep = intf.getEndpoint(i);
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    epOut = ep;
                } else {
                    epIn = ep;
                }
            }
        }
        if (epOut == null || epIn == null) {
            throw new IllegalArgumentException("not all endpoints found");
        }
        mEndpointOut = epOut;
        mEndpointIn = epIn;
    }

    public UAVTalkObjectTree getObjectTree() {
        return mObjectTree;
    }

    // return device serial number
    public String getSerial() {
        return mSerial;
    }

    // get an OUT request from our pool
    public UsbRequest getOutRequest() {
        synchronized (mOutRequestPool) {
            if (mOutRequestPool.isEmpty()) {
                UsbRequest request = new UsbRequest();
                request.initialize(mDeviceConnection, mEndpointOut);
                return request;
            } else {
                return mOutRequestPool.removeFirst();
            }
        }
    }

    public void releaseOutRequest(UsbRequest request) {
        synchronized (mOutRequestPool) {
            mOutRequestPool.add(request);
        }
    }

    public UsbRequest getInRequest() {
        synchronized (mInRequestPool) {
            if (mInRequestPool.isEmpty()) {
                UsbRequest request = new UsbRequest();
                request.initialize(mDeviceConnection, mEndpointIn);
                return request;
            } else {
                return mInRequestPool.removeFirst();
            }
        }
    }

    public void start() {
        this.connected = true;
        mWaiterThread.start();
    }

    public void stop() {
        synchronized (mWaiterThread) {
            mWaiterThread.mStop = true;
        }
        this.connected = false;
    }

    public boolean requestObject(String objectName) {
        return requestObject(objectName, 0);
    }

    public boolean requestObject(String objectName, int instance) {
        UAVTalkXMLObject xmlObj = mObjectTree.getXmlObjects().get(objectName);

        if (xmlObj == null) {
            return false;
        }

        byte[] send = UAVTalkObject.getReqMsg((byte) 0x21, xmlObj.getId(), instance);
        mActivity.incTxObjects();
        mDeviceConnection.bulkTransfer(mEndpointOut, send, send.length, 1000);

        return true;
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public boolean isConnecting() {
        return this.connected;
    }

    @Override
    public boolean sendSettingsObject(String objectName, int instance, String fieldName, String elementName, byte[] newFieldData) {
        return sendSettingsObject(
                objectName,
                instance,
                fieldName,
                mObjectTree.getElementIndex(objectName, fieldName, elementName),
                newFieldData
        );
    }

    public boolean sendSettingsObject(String objectName, int instance, String fieldName, int element, byte[] newFieldData) {
        byte[] send = UAVTalkDeviceHelper.createSettingsObjectByte(mObjectTree, objectName, instance, fieldName, element, newFieldData);
        if (send == null) return false;
        mActivity.incTxObjects();

        //This whole code needs rewrite. Bugger that two bytes preceding USB.
        int written = 2;
        int towrite = send.length;
        int packetsize = mEndpointOut.getMaxPacketSize() - 2;

        while (towrite > 0) {
            int nextpacketsize = (towrite > packetsize ? packetsize + 2 : towrite);
            byte[] packet = new byte[nextpacketsize];
            System.arraycopy(send, written, packet, 2, nextpacketsize - 2);
            packet[0] = send[0];
            packet[1] = send[1];
            int i = mDeviceConnection.bulkTransfer(mEndpointOut, packet, packet.length, 1000);
            towrite -= nextpacketsize;
            written += nextpacketsize;
        }

        requestObject(objectName, instance);
        return true;
    }

    private class WaiterThread extends Thread {
        public boolean mStop;

        public void run() {
            byte[] bytes;
            while (true) {
                if (mStop) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        //Thread wakes up
                    }
                    continue;
                }

                byte[] buffer = new byte[mEndpointIn.getMaxPacketSize()];

                int result = mDeviceConnection.bulkTransfer(mEndpointIn, buffer, buffer.length, 1000);
                //synbuffer[0] = buffer[2];
                //Log.d("SYNC "+result, H.bytesToHex(buffer));

                if (result > 0 && mActivity.isReady && buffer.length > 3 && buffer[2] == 0x3C) {

                    bytes = buffer;

                    UAVTalkMessage cM = new UAVTalkMessage(bytes, 2);
                    if (cM.getLength() > 266) {
                        mActivity.incRxObjectsBad();
                        Log.d("TOO", "LONG");
                        continue;
                    }

                    //TODO: crappy code...
                    if (cM.getLength() > mEndpointIn.getMaxPacketSize()) {
                        byte[] bigbuffer = new byte[mEndpointIn.getMaxPacketSize() * 2];
                        System.arraycopy(bytes, 0, bigbuffer, 0, mEndpointIn.getMaxPacketSize());
                        result = mDeviceConnection.bulkTransfer(mEndpointIn, buffer, buffer.length, 1000);
                        System.arraycopy(buffer, 2, bigbuffer, mEndpointIn.getMaxPacketSize(), mEndpointIn.getMaxPacketSize() - 2);
                        bytes = bigbuffer;
                        cM = new UAVTalkMessage(bytes, 2);
                    }

                    if (cM.getLength() > mEndpointIn.getMaxPacketSize() * 2) {
                        byte[] bigbuffer = new byte[mEndpointIn.getMaxPacketSize() * 3];
                        System.arraycopy(bytes, 0, bigbuffer, 0, mEndpointIn.getMaxPacketSize());
                        result = mDeviceConnection.bulkTransfer(mEndpointIn, buffer, buffer.length, 1000);
                        System.arraycopy(buffer, 2, bigbuffer, mEndpointIn.getMaxPacketSize(), mEndpointIn.getMaxPacketSize() - 2);
                        bytes = bigbuffer;
                        cM = new UAVTalkMessage(bytes, 2);
                    }

                    if (cM.getLength() > mEndpointIn.getMaxPacketSize() * 3) {
                        byte[] bigbuffer = new byte[mEndpointIn.getMaxPacketSize() * 4];
                        System.arraycopy(bytes, 0, bigbuffer, 0, mEndpointIn.getMaxPacketSize());
                        result = mDeviceConnection.bulkTransfer(mEndpointIn, buffer, buffer.length, 1000);
                        System.arraycopy(buffer, 2, bigbuffer, mEndpointIn.getMaxPacketSize(), mEndpointIn.getMaxPacketSize() - 2);
                        bytes = bigbuffer;
                        cM = new UAVTalkMessage(bytes, 2);
                    }

                    mActivity.incRxObjectsGood();


                    UAVTalkObject myObj = mObjectTree.getObjectFromID(H.intToHex(cM.getObjectId()));
                    UAVTalkObjectInstance myIns;

                    switch (cM.getType()) {
                        case 0x20:
                            //handle default package, nothing to do
                            break;
                        case 0x21:
                            //handle request message, nobody should request from LP2Go (so we don't implement this)
                            Log.e("UAVTalk", "Received Object Request, but won't send any");
                            break;
                        case 0x22:
                            //handle object with ACK REQ, means send ACK
                            Log.d("UAVTalk", "Received Object with ACK Request");
                            break;
                        case 0x23:
                            //handle received ACK, e.g. save in Object that it has been acknowledged
                            break;
                        case 0x24:
                            //handle NACK, e.g. show warning
                            Log.w("UAVTalk", "Received NACK Object");
                            break;
                        default:
                            mActivity.incRxObjectsBad();
                            Log.w("UAVTalk", "Received bad Object Type " + (cM.getType() & 0xff));
                            continue;
                    }
                    try {
                        myIns = myObj.getInstance(cM.getInstanceId());
                        myIns.setData(cM.getData());
                        myObj.setInstance(myIns);
                    } catch (Exception e) {
                        myIns = new UAVTalkObjectInstance(cM.getInstanceId(), cM.getData());
                        myObj.setInstance(myIns);
                    }
                    mObjectTree.updateObject(myObj);
                    if (isLogging()) {
                        //TODO: CHECK IF CRC IS IN bytes!
                        log(Arrays.copyOfRange(bytes, 2, bytes.length)); //TODO: This removes the first two byte, added only for USB compatibility
                    }
                }
            }
        }
    }
}

