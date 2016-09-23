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

package org.librepilot.lp2go.uavtalk.device;

import android.graphics.Color;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.support.v4.content.ContextCompat;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.helper.H;
import org.librepilot.lp2go.uavtalk.UAVTalkDeviceHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkObject;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectTree;
import org.librepilot.lp2go.uavtalk.UAVTalkXMLObject;

import java.nio.ByteBuffer;
import java.util.Map;

public class FcUsbDevice extends FcDevice {

    private final UsbDeviceConnection mDeviceConnection;
    private final UsbEndpoint mEndpointIn;
    private final UsbEndpoint mEndpointOut;
    private final FcWaiterThread mWaiterThread;
    private boolean connected = false;
    private UsbRequest mOutRequest = null;

    public FcUsbDevice(MainActivity activity, UsbDeviceConnection connection,
                       UsbInterface intf, Map<String, UAVTalkXMLObject> xmlObjects) {
        super(activity);

        //mActivity = activity;
        mDeviceConnection = connection;
        mObjectTree = new UAVTalkObjectTree();
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

        mWaiterThread = new FcUsbWaiterThread(this, mDeviceConnection, mEndpointIn);
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
    public void start() {
        this.connected = true;
        mWaiterThread.start();
    }

    @Override
    public void stop() {
        synchronized (mWaiterThread) {
            mWaiterThread.stopThread();
        }
        this.connected = false;
    }

    @Override
    protected boolean writeByteArray(byte[] bytes) {
        boolean retval = false;
        int psize = mEndpointOut.getMaxPacketSize() - 2;
        int toWrite = bytes.length;

        while (toWrite > 0) {
            int sendlen = toWrite - psize > 0 ? psize : toWrite;
            byte[] buffer = new byte[sendlen + 2];

            System.arraycopy(bytes, bytes.length - toWrite, buffer, 2, sendlen);
            buffer[0] = (byte) 0x02;//report id, is always 2. Period.
            buffer[1] = (byte) ((sendlen) & 0xff);//bytes to send, which is packet.size()-2

            if (mOutRequest == null) {
                mOutRequest = new UsbRequest();
                mOutRequest.initialize(mDeviceConnection, mEndpointOut);
            }

            retval = mOutRequest.queue(ByteBuffer.wrap(buffer), buffer.length);

            toWrite -= sendlen;
        }

        return retval;
    }

    @Override
    public boolean sendAck(String objectId, int instance) {
        byte[] send = mObjectTree.getObjectFromID(objectId).toMessage((byte) 0x23, instance, true);
        VisualLog.d("SEND_ACK_USB", "" + H.bytesToHex(send));
        if (send != null) {
            mActivity.incTxObjects();

            writeByteArray(send);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean sendSettingsObject(String objectName, int instance) {
        byte[] send;
        send = mObjectTree.getObjectFromName(objectName).toMessage((byte) 0x22, instance, false);

        if (send != null) {
            mActivity.incTxObjects();

            writeByteArray(send);

            requestObject(objectName, instance);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean sendSettingsObject(String objectName, int instance, String fieldName,
                                      int element, byte[] newFieldData, final boolean block) {
        if (block) {
            mObjectTree.getObjectFromName(objectName).setWriteBlocked(true);
        }
        UAVTalkDeviceHelper
                .updateSettingsObject(mObjectTree, objectName, instance, fieldName, element,
                        newFieldData);

        sendSettingsObject(objectName, instance);

        if (block) {
            mObjectTree.getObjectFromName(objectName).setWriteBlocked(false);
        }
        return true;
    }

    @Override
    public boolean requestObject(String objectName) {
        return requestObject(objectName, 0);
    }

    @Override
    public boolean requestObject(String objectName, int instance) {
        UAVTalkXMLObject xmlObj = mObjectTree.getXmlObjects().get(objectName);

        if (xmlObj == null) {
            return false;
        }

        if (nackedObjects.contains(xmlObj.getId())) {
            //VisualLog.d("NACKED", xmlObj.getId());
            return false;  //if it was already nacked, don't try to get it again
        }

        byte[] send = UAVTalkObject.getReqMsg((byte) 0x21, xmlObj.getId(), instance);
        mActivity.incTxObjects();
        writeByteArray(send);

        return true;
    }

    @Override
    public void drawConnectionLogo(boolean blink) {
        MainActivity mA = mActivity;
        mA.imgSerial.setImageDrawable(
                ContextCompat.getDrawable(mA.getApplicationContext(),
                        R.drawable.ic_usb_128dp));
        if (mA.mFcDevice != null && mA.mFcDevice.isConnected()) {
            mA.imgSerial.setColorFilter(Color.argb(0xff, 0x00, 0x80, 0x00));


        } else if (mA.mFcDevice != null && mA.mFcDevice.isConnecting()) {
            if (blink) {
                mA.imgSerial.setColorFilter(Color.argb(0xff, 0xff, 0x66, 0x00));
            } else {
                mA.imgSerial.setColorFilter(Color.argb(0xff, 0xff, 0x88, 0x00));
            }
        } else {
            mA.imgSerial.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
        }
    }

}

