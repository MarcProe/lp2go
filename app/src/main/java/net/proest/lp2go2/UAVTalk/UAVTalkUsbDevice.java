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

package net.proest.lp2go2.UAVTalk;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;

import net.proest.lp2go2.H;
import net.proest.lp2go2.MainActivity;

import java.util.Hashtable;
import java.util.LinkedList;

public class UAVTalkUsbDevice implements UAVTalkDevice {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private final MainActivity mActivity;
    private final UsbDeviceConnection mDeviceConnection;
    private final UsbEndpoint mEndpointOut;
    private final UsbEndpoint mEndpointIn;

    private final LinkedList<UsbRequest> mOutRequestPool = new LinkedList<UsbRequest>();

    private final LinkedList<UsbRequest> mInRequestPool = new LinkedList<UsbRequest>();

    private final WaiterThread mWaiterThread = new WaiterThread();
    private UAVTalkObjectTree oTree;
    private String mSerial;

    public UAVTalkUsbDevice(MainActivity activity, UsbDeviceConnection connection,
                            UsbInterface intf, Hashtable<String, UAVTalkXMLObject> xmlObjects) {
        mActivity = activity;
        mDeviceConnection = connection;
        mSerial = connection.getSerial();
        this.oTree = new UAVTalkObjectTree();
        oTree.setXmlObjects(xmlObjects);


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

    /*
        public static String bytesToHex(byte[] bytes) {
            if (bytes == null) {
                return "null";
            }
            char[] hexChars = new char[bytes.length * 3];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 3] = hexArray[v >>> 4];
                hexChars[j * 3 + 1] = hexArray[v & 0x0F];
                hexChars[j * 3 + 2] = ' ';
            }
            return new String(hexChars);
        }

        public static byte[] toBytes(int i) {
            byte[] result = new byte[4];

            result[0] = (byte) (i >> 24);
            result[1] = (byte) (i >> 16);
            result[2] = (byte) (i >> 8);
            result[3] = (byte) (i);

            return result;
        }
    */
    public UAVTalkObjectTree getoTree() {
        return oTree;
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
        mWaiterThread.start();
    }

    public void stop() {
        synchronized (mWaiterThread) {
            mWaiterThread.mStop = true;
        }
    }

    public boolean requestObject(String objectName) {
        return requestObject(objectName, 0);
    }

    public boolean requestObject(String objectName, int instance) {
        UAVTalkXMLObject xmlObj = oTree.getXmlObjects().get(objectName);

        if (xmlObj == null) {
            return false;
        }

        byte[] send = UAVTalkObject.getReqMsg((byte) 0x21, xmlObj.getId(), instance);
        //Log.d("REQ", objectName);
        mDeviceConnection.bulkTransfer(mEndpointOut, send, send.length, 1000);

        return true;
    }

    public boolean sendSettingsObject(String objectName, int instance, String fieldName, int element, byte[] newFieldData) {

        UAVTalkObject obj = oTree.getObjectNoCreate(objectName);
        if (obj == null) {
            return false;
        }
        UAVTalkObjectInstance ins = obj.getInstance(instance);
        if (ins == null) {
            return false;
        }

        UAVTalkXMLObject xmlObj = oTree.getXmlObjects().get(objectName);
        if (xmlObj == null) {
            return false;
        }
        UAVTalkXMLObject.UAVTalkXMLObjectField xmlField = xmlObj.getFields().get(fieldName);
        if (xmlField == null) {
            return false;
        }

        byte[] data = ins.getData();
        int fpos = xmlField.pos;
        int elen = xmlField.typelength;

        int savepos = fpos + elen * element;

        //if(newFieldData.length != elen) { return false; }  //ACTIVATE ME!

        System.arraycopy(newFieldData, 0, data, savepos, newFieldData.length);

        ins.setData(data);
        obj.setInstance(ins);
        oTree.updateObject(obj);

        byte[] send = obj.toMsg((byte) 0x20, ins.getId());

        mDeviceConnection.bulkTransfer(mEndpointOut, send, send.length, 1000);

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
                    }
                    continue;
                }
                byte[] buffer = new byte[mEndpointIn.getMaxPacketSize()];

                int result = mDeviceConnection.bulkTransfer(mEndpointIn, buffer, buffer.length, 1000);

                if (result > 0 && mActivity.isReady && buffer.length > 3 && buffer[2] == 0x3C) {

                    bytes = buffer;

                    UAVTalkMessage cM = new UAVTalkMessage(bytes);

                    //TODO: crappy code...
                    if (cM.getLength() > mEndpointIn.getMaxPacketSize()) {
                        byte[] bigbuffer = new byte[mEndpointIn.getMaxPacketSize() * 2];
                        System.arraycopy(bytes, 0, bigbuffer, 0, mEndpointIn.getMaxPacketSize());
                        System.arraycopy(buffer, 0, bigbuffer, mEndpointIn.getMaxPacketSize(), mEndpointIn.getMaxPacketSize());
                        bytes = bigbuffer;
                        cM = new UAVTalkMessage(bytes);
                    }

                    if (cM.getLength() > mEndpointIn.getMaxPacketSize() * 2) {
                        byte[] bigbuffer = new byte[mEndpointIn.getMaxPacketSize() * 3];
                        System.arraycopy(bytes, 0, bigbuffer, 0, mEndpointIn.getMaxPacketSize());
                        System.arraycopy(buffer, 0, bigbuffer, mEndpointIn.getMaxPacketSize(), mEndpointIn.getMaxPacketSize());
                        bytes = bigbuffer;
                        cM = new UAVTalkMessage(bytes);
                    }

                    if (cM.getLength() > mEndpointIn.getMaxPacketSize() * 3) {
                        byte[] bigbuffer = new byte[mEndpointIn.getMaxPacketSize() * 4];
                        System.arraycopy(bytes, 0, bigbuffer, 0, mEndpointIn.getMaxPacketSize());
                        System.arraycopy(buffer, 0, bigbuffer, mEndpointIn.getMaxPacketSize(), mEndpointIn.getMaxPacketSize());
                        bytes = bigbuffer;
                        cM = new UAVTalkMessage(bytes);
                    }

                    UAVTalkObject myObj = oTree.getObjectFromID(H.intToHex(cM.getoID()));
                    UAVTalkObjectInstance myIns;

                    try {
                        myIns = myObj.getInstance(cM.getiID());
                        myIns.setData(cM.getData());
                        myObj.setInstance(myIns);
                    } catch (Exception e) {
                        myIns = new UAVTalkObjectInstance(cM.getiID(), cM.getData());
                        myObj.setInstance(myIns);
                    }
                    oTree.updateObject(myObj);
                    //
                    try {
                        if (H.intToHex(cM.getoID()).equals("C243686C")) {
                            // Log.d("RAW ", H.bytesToHex(bytes));
                            //Log.d("HANDSHAKE IN<--", H.bytesToHex(bytes));
                            //processHandshake();
                        }
                    } catch (Exception e) {
                    }
                    try {
                        if (H.intToHex(cM.getoID()).equals(oTree.getObjectNoCreate("GPSSatellites").getId())) {
                            //Log.d("GPSsats "+oTree.getObjectNoCreate("GPSSatellites").getId(), H.bytesToHex(bytes));
                            //Log.d("HANDSHAKE IN<--", H.bytesToHex(bytes));
                            //processHandshake();
                        }
                    } catch (Exception e) {
                    }

                    try {
                        if (H.intToHex(cM.getoID()).equals(oTree.getObjectNoCreate("FlightTelemetryStats").getId())) {
                            //Log.d("HANDSHAKE", "Got a FlightTelemetryStats Packet");
                            //Log.d("HANDSHAKE IN<--", H.bytesToHex(bytes));
                            //processHandshake();
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }

       /* private void processHandshake() { // no way this is going to work
            byte[] bytes = new byte[mEndpointOut.getMaxPacketSize()];
            Arrays.fill( bytes, (byte) 0 );

            switch(oTree.getData("FlightTelemetryStats", "Status")) {
                case "Disconnected":
                    Log.d("HANDSHAKE", "Disconnected");
                    //send gcs handshakereq
                    bytes[0] = 0x02;    //stuff I don't
                    bytes[1] = 0x30;    //                know what it means
                    bytes[2] = 0x3c;    //Sync
                    bytes[3] = 0x20;    //Type
                    bytes[4] = 0x2f;    //Len
                    bytes[5] = 0x00;    //   gth

                    byte[] objId = H.hexStringToByteArray(oTree.getObjectFromName("GCSTelemetryStats").getId());
                    bytes[6] = objId[3];
                    bytes[7] = objId[2];
                    bytes[8] = objId[1];
                    bytes[9] = objId[0];

                    bytes[10] = 0x00; //Instance
                    bytes[11] = 0x00;     //ID

                    //0000   02 30 3c 20 2f 00 0a dc d1 ca 00 00 00 00 c0 41
                    bytes[12] = 0x01;  //Status
                    bytes[13] = 0x00;
                    bytes[14] = (byte)0xc0;
                    bytes[15] = 0x41;
                    //0010   63 39 00 00 -- 00 00 00 00 -- 00 00 00 00 -- 00 00 c0 41
                    bytes[16] = 0x63;
                    bytes[17] = 0x39;
                    bytes[18] = 0x00;
                    bytes[19] = 0x0;

                    bytes[20] = 0x00;
                    bytes[21] = 0x00;
                    bytes[22] = 0x00;
                    bytes[23] = 0x00;

                    bytes[24] = 0x00;
                    bytes[25] = 0x00;
                    bytes[26] = 0x00;
                    bytes[27] = 0x00;

                    bytes[28] = 0x00;
                    bytes[29] = 0x00;
                    bytes[30] = (byte)0xc0;
                    bytes[31] = 0x41;

                    //0020   67 b9 0a 00 -- 00 00 00 00 -- 00 00 00 00 -- 00 00 00 00
                    bytes[32] = 0x67;
                    bytes[33] = (byte)0xb9;
                    bytes[34] = 0x0a;
                    bytes[35] = 0x0;

                    bytes[36] = 0x00;
                    bytes[37] = 0x00;
                    bytes[38] = 0x00;
                    bytes[39] = 0x00;

                    bytes[40] = 0x00;
                    bytes[41] = 0x00;
                    bytes[42] = 0x00;
                    bytes[43] = 0x00;

                    bytes[44] = 0x00;
                    bytes[45] = 0x00;
                    bytes[46] = 0x00;
                    bytes[47] = 0x00;

                    //0030   03 8f 00 00 00 00 00 00 00 00 00 00 00 00 00 00

                    bytes[48] = 0x03;


                    byte[] b = new byte[47];
                    System.arraycopy(bytes, 2, b, 0, 47);

                    bytes[49] = (byte)H.crc8(b);
                    Log.d("HANDSHAKE OUT->", H.bytesToHex(bytes));
                    Log.d("CHECKSUMSRC->", H.bytesToHex(b) + " " +(byte)H.crc8(b));

                    mDeviceConnection.bulkTransfer(mEndpointOut, bytes, bytes.length, 1000);
                    // set local state to handshakereq
                    break;
                case "HandshakeAck":
                    //send gcs connected packet
                    //set local state connected
                    break;
            }
        }*/
    }
}

