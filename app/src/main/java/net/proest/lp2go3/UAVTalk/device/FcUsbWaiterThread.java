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
package net.proest.lp2go3.UAVTalk.device;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;

import net.proest.lp2go3.H;
import net.proest.lp2go3.UAVTalk.UAVTalkMessage;
import net.proest.lp2go3.UAVTalk.UAVTalkObject;
import net.proest.lp2go3.UAVTalk.UAVTalkObjectInstance;
import net.proest.lp2go3.VisualLog;

import java.util.ArrayDeque;
import java.util.Queue;

class FcUsbWaiterThread extends FcWaiterThread {

    private final UsbEndpoint mEndpointIn;
    private final UsbDeviceConnection mUsbDeviceConnection;
    private boolean mStop;
    private Queue<Byte> queue;

    public FcUsbWaiterThread(FcDevice device, UsbDeviceConnection usbDeviceConnection,
                             UsbEndpoint endpointIn) {
        super(device);
        this.mUsbDeviceConnection = usbDeviceConnection;
        this.mEndpointIn = endpointIn;
        this.setName("LP2GoDeviceUsbWaiterThread");
    }

    protected void stopThread() {
        this.mStop = true;
    }

    private byte[] bufferRead(int len) {
        byte[] retval = new byte[len];
        for (int i = 0; i < len; i++) {
            if (!queue.isEmpty()) {
                retval[i] = queue.remove();
            }
        }
        return retval;
    }

    public void run() {

        queue = new ArrayDeque<Byte>();

        byte[] syncbuffer = new byte[1];
        byte[] msgtypebuffer = new byte[1];
        byte[] lenbuffer = new byte[2];
        byte[] oidbuffer = new byte[4];
        byte[] iidbuffer = new byte[2];
        byte[] databuffer;
        byte[] crcbuffer = new byte[1];

        mDevice.mActivity.setRxObjectsGood(0);
        mDevice.mActivity.setRxObjectsBad(0);
        mDevice.mActivity.setTxObjects(0);

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
            while (queue.size() < 350) {
                mUsbDeviceConnection.bulkTransfer(mEndpointIn, buffer, buffer.length, 1000);
                try {
                    for (int i = 2; i < (buffer[1] & 0xff) + 2; i++) {
                        queue.add(buffer[i]);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    VisualLog.e("FcUsbWaiterThread", "AIOOBE in Usb Queue filler");
                }
            }

            syncbuffer[0] = 0x00;
            while (syncbuffer[0] != 0x3c) {
                syncbuffer = bufferRead(1);
            }

            msgtypebuffer = bufferRead(msgtypebuffer.length);

            lenbuffer = bufferRead(lenbuffer.length);

            int lb1 = lenbuffer[1] & 0x000000ff;
            int lb2 = lenbuffer[0] & 0x000000ff;
            int len = lb1 << 8 | lb2;

            if (len > 266 || len < 10) {
                mDevice.mActivity.incRxObjectsBad();
                continue; // maximum possible packet size
            }

            oidbuffer = bufferRead(oidbuffer.length);
            iidbuffer = bufferRead(iidbuffer.length);
            databuffer = bufferRead(len - 10);
            crcbuffer = bufferRead(crcbuffer.length);

            byte[] bmsg = H.concatArray(syncbuffer, msgtypebuffer);
            bmsg = H.concatArray(bmsg, lenbuffer);
            bmsg = H.concatArray(bmsg, oidbuffer);
            bmsg = H.concatArray(bmsg, iidbuffer);
            bmsg = H.concatArray(bmsg, databuffer);
            int crc = H.crc8(bmsg, 0, bmsg.length);
            bmsg = H.concatArray(bmsg, crcbuffer);

            if ((((int) crcbuffer[0] & 0xff) == (crc & 0xff))) {
                mDevice.mActivity.incRxObjectsGood();
            } else {
                mDevice.mActivity.incRxObjectsBad();
                VisualLog.d("USB", "Bad CRC");
                continue;
            }

            try {
                UAVTalkMessage msg = new UAVTalkMessage(bmsg, 0);
                UAVTalkObject myObj =
                        mDevice.mObjectTree.getObjectFromID(H.intToHex(msg.getObjectId()));
                UAVTalkObjectInstance myIns;

                try {
                    myIns = myObj.getInstance(msg.getInstanceId());
                    myIns.setData(msg.getData());
                    myObj.setInstance(myIns);
                } catch (Exception e) {
                    myIns = new UAVTalkObjectInstance(msg.getInstanceId(), msg.getData());
                    myObj.setInstance(myIns);
                }

                if (handleMessageType(msgtypebuffer[0], myObj)) {
                    mDevice.mObjectTree.updateObject(myObj);
                    if (mDevice.isLogging()) {
                        mDevice.log(bmsg);
                    }
                }
            } catch (Exception e) {
                VisualLog.e(e);
            }
        }
    }
}