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

package net.proest.lp2go2.UAVTalk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import net.proest.lp2go2.H;
import net.proest.lp2go2.MainActivity;
import net.proest.lp2go2.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.UUID;

public class UAVTalkBluetoothDevice extends UAVTalkDevice implements UAVTalkDeviceInterface {
    public static final int STATE_NONE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    private final MainActivity mActivity;
    private WaiterThread mWaiterThread;
    private UAVTalkObjectTree oTree;
    private BluetoothAdapter mBluetoothAdapter;
    private ConnectThread mConnectThread;
    private BluetoothDevice mDevice;
    private int mState;

    public UAVTalkBluetoothDevice(MainActivity activity, Hashtable<String, UAVTalkXMLObject> xmlObjects) {
        super(activity);
        this.mActivity = activity;
        this.oTree = new UAVTalkObjectTree();
        oTree.setXmlObjects(xmlObjects);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            //Toast.makeText(this.mActivity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            //finish();
            return;
        }
        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);

        //String mDeviceAddress = "98:D3:31:FC:18:FC"; //TODO:got to come from settings.
        String mDeviceAddress = sharedPref.getString(mActivity.getString(R.string.SETTINGS_BT_MAC), "");
        mDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
        //mNXTTalker.connect(mDevice);

    }

    private synchronized void connect(BluetoothDevice device) {
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mWaiterThread != null) {
            mWaiterThread.cancel();
            mWaiterThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    private synchronized void setState(int state) {
        mState = state;
    }

    @Override
    public void start() {
        connect(mDevice);
    }

    @Override
    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mWaiterThread != null) {
            mWaiterThread.cancel();
            mWaiterThread = null;
        }
        setState(STATE_NONE);
    }

    private void connectionFailed() {
        setState(STATE_NONE);
    }

    @Override
    public UAVTalkObjectTree getoTree() {
        return oTree;
    }

    @Override
    public boolean sendSettingsObject(String objectName, int instance, String fieldName, int element, byte[] newFieldData) {
        byte[] send = UAVTalkDeviceHelper.createSettingsObjectByte(oTree, objectName, instance, fieldName, element, newFieldData);
        if (send == null) return false;

        write(Arrays.copyOfRange(send, 2, send.length)); //TODO: This removes the first two byte, added only for USB compatibility

        requestObject(objectName, instance);
        return true;
    }

    @Override
    public boolean requestObject(String objectName) {
        return requestObject(objectName, 0);
    }

    public boolean requestObject(String objectName, int instance) {
        UAVTalkXMLObject xmlObj = oTree.getXmlObjects().get(objectName);
        if (xmlObj == null) return false;

        byte[] send = UAVTalkObject.getReqMsg((byte) 0x21, xmlObj.getId(), instance);

        write(Arrays.copyOfRange(send, 2, send.length));

        return true;
    }

    @Override
    public boolean isConnected() {
        return mState == STATE_CONNECTED;
    }

    @Override
    public boolean setConnected(boolean connected) {
        return false;  //bluetooth maintains connection state by itself
    }


    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mWaiterThread != null) {
            mWaiterThread.cancel();
            mWaiterThread = null;
        }

        mWaiterThread = new WaiterThread(socket);
        mWaiterThread.start();

        //toast("Connected to " + device.getName());

        setState(STATE_CONNECTED);
    }

    private void write(byte[] out) {
        WaiterThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) {
                return;
            }
            r = mWaiterThread;
        }

        r.write(out);
    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
        }

        public void run() {
            setName("ConnectThread");

            mBluetoothAdapter.cancelDiscovery();

            try {
                //mmSocket = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                mmSocket = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString("00000000-0000-1000-8000-00805F9B34FB"));
                mmSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    // This is a workaround that reportedly helps on some older devices like HTC Desire, where using
                    // the standard createRfcommSocketToServiceRecord() method always causes connect() to fail.
                    //Method method = mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(mmDevice, 1);
                    //mmSocket = (BluetoothSocket) method.invoke(mmDevice, Integer.valueOf(1));
                    mmSocket.connect();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    connectionFailed();
                    try {
                        mmSocket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    return;
                }
            }

            synchronized (this) {
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                if (mmSocket != null) {
                    mmSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class WaiterThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public boolean mStop;

        public WaiterThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                //e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        private byte[] addTwoBytes(byte[] b) {
            byte[] r = new byte[b.length + 2];
            r[0] = 0x00;
            r[1] = 0x00;
            System.arraycopy(b, 0, r, 2, b.length);
            return r;
        }

        private byte[] findUAVTalkMessage(byte[] b) {
            byte[] retval = new byte[0];
            int len = 0;
            int startMsg = 0;

            for (int i = 0; i < b.length - 8; i++) {
                if (b[i] == 0x3c && b[i + 1] == 0x20) {

                    int lb1 = b[i + 3] & 0x000000ff;
                    int lb2 = b[i + 2] & 0x000000ff;
                    len = lb1 << 8 | lb2;

                    if (len > 64) return new byte[0];

                    byte[] bmsg = Arrays.copyOfRange(b, i, i + len + 1); //+1 cause we want crc as well
                    int crc = H.crc8(bmsg, 2, bmsg.length - 3);

                    //Log.d("INPUT" + len, H.bytesToPrintHex(b));
                    //Log.d("FOUND" + len, H.bytesToPrintHex(bmsg));
                    //Log.d("CRC", "" + crc + " " + (bmsg[bmsg.length-1] & 0xff));

                    bmsg = addTwoBytes(bmsg);

                    //Log.d("FOUND" + len, H.bytesToPrintHex(bmsg));

                    try {
                        retval = Arrays.copyOfRange(b, i + bmsg.length - 1, b.length);
                    } catch (IllegalArgumentException e) {
                    }
                    if (retval.length == 0) return retval;
                    //if retval is 0, then we exactly stopped at the end of the bluetooth buffer
                    //in this case, we should not create a new objhect, because there may be more data for the current object.
                    //we could check this with length or crc (better length)
                    //for now, just return to be save.

                    try {
                        UAVTalkMessage msg = new UAVTalkMessage(bmsg);
                        //Log.d("MSG", ""+H.intToHex(msg.getoID()));
                        UAVTalkObject myObj = oTree.getObjectFromID(H.intToHex(msg.getoID()));
                        UAVTalkObjectInstance myIns;

                        try {
                            myIns = myObj.getInstance(msg.getiID());
                            myIns.setData(msg.getData());
                            myObj.setInstance(myIns);
                        } catch (Exception e) {
                            myIns = new UAVTalkObjectInstance(msg.getiID(), msg.getData());
                            myObj.setInstance(myIns);
                        }
                        if (myObj.getId().equals("6B7639EC")) {
                            Log.d("6B7639EC", myObj.getId());

                            Log.d("INPUT" + len, H.bytesToPrintHex(b));
                            Log.d("FOUND" + len, H.bytesToPrintHex(bmsg));
                            Log.d("CRC", "" + crc + " " + (bmsg[bmsg.length - 1] & 0xff));
                        }
                        oTree.updateObject(myObj);
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                    break;
                }
            }

            return retval;
        }

        public void run() {

            byte[] syncbuffer = new byte[3];
            byte[] buffer = new byte[64];
            byte[] seekbuffer = new byte[1];
            byte[] headerbuffer = new byte[9];
            byte[] crcbuffer = new byte[1];
            int read;
            ////int lbytes;
            ////lbytes = 0;

            while (true) {
                //Util.sleep(50);
                try {
                    ////lbytes = mmInStream.read(buffer);
                    while (seekbuffer[0] != 0x3c) {
                        read = mmInStream.read(seekbuffer);
                    }
                    syncbuffer[2] = 0x3c;
                    read = mmInStream.read(headerbuffer);
                    int lb1 = headerbuffer[2] & 0x000000ff;
                    int lb2 = headerbuffer[1] & 0x000000ff;
                    int len = lb1 << 8 | lb2;

                    buffer = new byte[len];
                    read = mmInStream.read(buffer);
                    read = mmInStream.read(crcbuffer);

                    byte[] bmsg = H.concatArray(syncbuffer, headerbuffer);
                    bmsg = H.concatArray(bmsg, buffer);
                    int crc = H.crc8(bmsg, 0, bmsg.length);
                    bmsg = H.concatArray(bmsg, crcbuffer);

                    try {
                        UAVTalkMessage msg = new UAVTalkMessage(bmsg);
                        //Log.d("MSG", ""+H.intToHex(msg.getoID()));
                        UAVTalkObject myObj = oTree.getObjectFromID(H.intToHex(msg.getoID()));
                        UAVTalkObjectInstance myIns;

                        try {
                            myIns = myObj.getInstance(msg.getiID());
                            myIns.setData(msg.getData());
                            myObj.setInstance(myIns);
                        } catch (Exception e) {
                            myIns = new UAVTalkObjectInstance(msg.getiID(), msg.getData());
                            myObj.setInstance(myIns);
                        }
                        //if(myObj.getId().equals("6B7639EC")) {
                        //Log.d("6B7639EC", myObj.getId());

                        //Log.d("HBUFF" + bmsg.length, H.bytesToPrintHex(headerbuffer));
                        if (myObj.getId().equals("6B7639EC")) {

                            Log.d("BMSG" + len, H.bytesToPrintHex(bmsg));
                            Log.d("CRC (MSG/CALC)", "" + crcbuffer[0] + " " + (byte) (crc & 0xff));

                        }
                        oTree.updateObject(myObj);

                        if (isLogging()) {
                            log(bmsg);
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }

                    ////toast(Integer.toString(bytes) + " bytes read from device");
                    ////mReadBuffer.addLast(buffer);
                    //sendRead(buffer);
                    //Log.d("BLUETOOTH", H.bytesToPrintHex(buffer));
                   /* byte[] retbuffer = buffer;
                    do {            //get all packets from the byte (does not take into account possible fragmentation;
                                    // I don't know if this could happen
                        retbuffer = findUAVTalkMessage(retbuffer);
                        //Log.d("retbuffer",""+retbuffer.length);
                        //buffer = retbuffer;
                    } while (retbuffer.length > 0);
                    */
                    //Log.d("EXT","exited do while");

                    //mLastMessage = buffer;

                } catch (IOException e) {
                    e.printStackTrace();
                    //connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                //e.printStackTrace();
                // XXX
            }
        }

        public void cancel() {
            mStop = true;
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
