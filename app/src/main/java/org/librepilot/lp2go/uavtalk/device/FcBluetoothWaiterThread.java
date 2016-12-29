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

package org.librepilot.lp2go.uavtalk.device;

import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

import org.librepilot.lp2go.H;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.uavtalk.UAVTalkMessage;
import org.librepilot.lp2go.uavtalk.UAVTalkObject;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectInstance;
import org.librepilot.lp2go.ui.SingleToast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class FcBluetoothWaiterThread extends FcWaiterThread {
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final BluetoothSocket mmSocket;
    private FcBluetoothDevice mBluetoothDevice;

    public FcBluetoothWaiterThread(BluetoothSocket socket, FcDevice device) {
        super(device);
        mBluetoothDevice = (FcBluetoothDevice) device;
        this.setName("LP2GoDeviceBluetoothWaiterThread");
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            //VisualLog.e(e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    protected void stopThread() {
        this.cancel();
    }


    public void run() {

        byte[] seekbuffer = new byte[1];
        byte[] syncbuffer = new byte[3];
        byte[] msgtypebuffer = new byte[1];
        byte[] lenbuffer = new byte[2];
        byte[] oidbuffer = new byte[4];
        byte[] iidbuffer = new byte[2];
        byte[] timestampbuffer = new byte[2];
        byte[] databuffer;
        byte[] crcbuffer = new byte[1];

        mDevice.mActivity.setRxObjectsGood(0);
        mDevice.mActivity.setRxObjectsBad(0);
        mDevice.mActivity.setTxObjects(0);



        while (true) {
            try {

                while (seekbuffer[0] != 0x3c) {
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        mmInStream.read(seekbuffer);
                    } catch (NullPointerException e) {
                        VisualLog.e("BluetTooth Waiter", "InStream is Null");
                        SingleToast.show(mDevice.mActivity, "Bluetooth is not working. Please check if it is enabled and paired.", Toast.LENGTH_LONG);
                        return;
                    }
                }
                seekbuffer[0] = 0x00;
                syncbuffer[2] = 0x3c;

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

                int tsoffset = 0;
                if ((MASK_TIMESTAMP & msgtypebuffer[0]) == MASK_TIMESTAMP) {
                    timestampbuffer = bufferRead(timestampbuffer.length);
                    tsoffset = 2;
                }

                databuffer = bufferRead(len - (10 + tsoffset));
                crcbuffer = bufferRead(crcbuffer.length);

                if (lenbuffer.length != 2 || oidbuffer.length != 4 || iidbuffer.length != 2
                        || databuffer.length == 0 || crcbuffer.length != 1) {
                    mDevice.mActivity.incRxObjectsBad();
                    continue;
                }

                byte[] bmsg = H.concatArray(syncbuffer, msgtypebuffer);
                bmsg = H.concatArray(bmsg, lenbuffer);
                bmsg = H.concatArray(bmsg, oidbuffer);
                bmsg = H.concatArray(bmsg, iidbuffer);
                if ((MASK_TIMESTAMP & msgtypebuffer[0]) == MASK_TIMESTAMP) {
                    bmsg = H.concatArray(bmsg, timestampbuffer);
                }
                bmsg = H.concatArray(bmsg, databuffer);
                int crc = H.crc8(bmsg, 0, bmsg.length);
                bmsg = H.concatArray(bmsg, crcbuffer);


                if ((((int) crcbuffer[0] & 0xff) == (crc & 0xff))) {
                    mDevice.mActivity.incRxObjectsGood();
                } else {
                    mDevice.mActivity.incRxObjectsBad();
                    continue;
                }

                try {
                    UAVTalkMessage msg = new UAVTalkMessage(bmsg, 2);
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
                            mDevice.log(msg);
                        }
                    }
                } catch (Exception e) {
                    VisualLog.e(e);
                }

            } catch (IOException e) {
                VisualLog.e(e);
                if (mmInStream != null) {
                    try {
                        mmInStream.close();
                    } catch (IOException e1) {
                        VisualLog.e(e1);
                    }
                }
                if (mBluetoothDevice != null) {
                    mBluetoothDevice.connectionLost();
                }
                break;
            }
        }
    }

    private byte[] bufferRead(int dlen) throws IOException {
        if (dlen <= 0) {
            return new byte[0];
        }

        byte[] buffer = new byte[dlen];
        int read = mmInStream.read(buffer);
        int pos;

        while (read < dlen) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                VisualLog.e(e);
            }

            byte[] readmore = new byte[dlen - read];
            pos = mmInStream.read(readmore);
            read += pos;
            try {
                System.arraycopy(readmore, 0, buffer, dlen - pos, readmore.length);
            } catch (ArrayIndexOutOfBoundsException e) {
                VisualLog.e(e);
                VisualLog.e("BLUETOOTH", "Bad Packet, should not happen.");
                return new byte[0];
            }
        }
        return buffer;
    }

    public void write(byte[] buffer) {
        try {
            mDevice.mActivity.incTxObjects();
            mmOutStream.write(buffer);
        } catch (IOException e) {
            VisualLog.e("ERR", "Error while writing to BT Stack");
        }
    }

    private void cancel() {
        try {
            mmSocket.close();
            mBluetoothDevice = null;
        } catch (IOException e) {
            VisualLog.e(e);
        }
    }
}