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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import net.proest.lp2go3.H;
import net.proest.lp2go3.MainActivity;
import net.proest.lp2go3.R;
import net.proest.lp2go3.UAVTalk.UAVTalkDeviceHelper;
import net.proest.lp2go3.UAVTalk.UAVTalkMessage;
import net.proest.lp2go3.UAVTalk.UAVTalkObject;
import net.proest.lp2go3.UAVTalk.UAVTalkObjectInstance;
import net.proest.lp2go3.UAVTalk.UAVTalkObjectTree;
import net.proest.lp2go3.UAVTalk.UAVTalkXMLObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class UAVTalkBluetoothDevice extends UAVTalkDevice {
    private static final int STATE_NONE = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    //private final MainActivity mActivity;
    private WaiterThread mWaiterThread;

    private BluetoothAdapter mBluetoothAdapter;
    private ConnectThread mConnectThread;
    private BluetoothDevice mDevice;
    private int mState;

    public UAVTalkBluetoothDevice(MainActivity activity, HashMap<String, UAVTalkXMLObject> xmlObjects) {
        super(activity);

        //this.mActivity = activity;
        this.mObjectTree = new UAVTalkObjectTree();
        mObjectTree.setXmlObjects(xmlObjects);

        mActivity.setPollThreadObjectTree(mObjectTree);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            //Toast.makeText(this.mActivity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            //finish();
            return;
        }
        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);


        String mDeviceAddress = sharedPref.getString(mActivity.getString(R.string.SETTINGS_BT_MAC), "").toUpperCase().replace('-', ':');
        //String reg1 = "^([0-9A-F]{2}[:]){5}([0-9A-F]{2})$";
        if (mDeviceAddress.matches("^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$")) {
            mDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
        }
        connect(mDevice);
    }

    private synchronized void connect(BluetoothDevice device) {
        if (mState == STATE_NONE) {
            if (mWaiterThread != null) {
                mWaiterThread.cancel();
                mWaiterThread = null;
            }

            mConnectThread = new ConnectThread(device);
            mConnectThread.start();
            setState(STATE_CONNECTING);
        }
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
    public boolean sendAck(String objectId, int instance) {
        byte[] send = mObjectTree.getObjectFromID(objectId).toMessage((byte) 0x23, instance, true);
        Log.d("SEND", "" + H.bytesToHex(send));
        if (send != null) {
            writeByteArray(Arrays.copyOfRange(send, 0, send.length));
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean sendSettingsObject(String objectName, int instance) {
        byte[] send = mObjectTree.getObjectFromName(objectName).toMessage((byte) 0x22, instance, false);
        if (send != null) {
            writeByteArray(Arrays.copyOfRange(send, 0, send.length));
            requestObject(objectName, instance);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean sendSettingsObject(String objectName, int instance, String fieldName, int element, byte[] newFieldData, final boolean block) {
        boolean retval;
        if (block) mObjectTree.getObjectFromName(objectName).setWriteBlocked(true);
        UAVTalkDeviceHelper.updateSettingsObject(mObjectTree, objectName, instance, fieldName, element, newFieldData);

        retval = sendSettingsObject(objectName, instance);

        if (block) mObjectTree.getObjectFromName(objectName).setWriteBlocked(false);
        return retval;
    }

    @Override
    public boolean requestObject(String objectName) {
        return requestObject(objectName, 0);
    }

    @Override
    public boolean requestObject(String objectName, int instance) {
        UAVTalkXMLObject xmlObj = mObjectTree.getXmlObjects().get(objectName);
        if (xmlObj == null) return false;

        byte[] send = UAVTalkObject.getReqMsg((byte) 0x21, xmlObj.getId(), instance);

        writeByteArray(send);

        return true;
    }

    @Override
    public boolean isConnected() {
        return mState == STATE_CONNECTED;
    }


    @Override
    public boolean isConnecting() {
        return mState != STATE_NONE;
    }


    private synchronized void connected(BluetoothSocket socket) {  //this is only called when we weren't connected before
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

    @Override
    protected boolean writeByteArray(byte[] out) {
        WaiterThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) {
                return false;
            }
            r = mWaiterThread;
        }
        r.write(out);
        return true;
    }

    private void connectionLost() {
        mActivity.reconnect();

        setState(STATE_NONE);
    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
        }

        public void run() {
            setName("LP2GoDeviceBluetoothConnectThread");

            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                mmSocket.connect();
            } catch (IOException e) {
                Log.e("BT", "Default BT  Connection failed, trying fallback.");
                try {
                    // This is a workaround that reportedly helps on some older devices like HTC Desire, where using
                    // the standard createRfcommSocketToServiceRecord() method always causes connect() to fail.
                    //Method method = mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(mmDevice, 1);
                    mmSocket.connect();
                } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e1) {
                    Log.e("BT", "Fallback BT  Connection failed, trying again.");
                    connectionFailed();
                    try {
                        mmSocket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    } catch (NullPointerException e3) {
                        return;
                    }
                    return;
                } catch (NullPointerException e4) {
                    connectionFailed();
                    return;
                }
            } catch (NullPointerException e5) {
                connectionFailed();
                return;
            }

            synchronized (this) {
                mConnectThread = null;
            }

            connected(mmSocket);
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
            this.setName("LP2GoDeviceBluetoothWaiterThread");
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

        public void run() {

            byte[] seekbuffer = new byte[1];
            byte[] syncbuffer = new byte[3];
            byte[] msgtypebuffer = new byte[1];
            byte[] lenbuffer = new byte[2];
            byte[] oidbuffer = new byte[4];
            byte[] iidbuffer = new byte[2];
            byte[] databuffer;
            byte[] crcbuffer = new byte[1];

            mActivity.setRxObjectsGood(0);
            mActivity.setRxObjectsBad(0);
            mActivity.setTxObjects(0);


            while (true) {
                try {

                    while (seekbuffer[0] != 0x3c) {
                        int read = mmInStream.read(seekbuffer);
                    }
                    seekbuffer[0] = 0x00;
                    syncbuffer[2] = 0x3c;

                    msgtypebuffer = bufferRead(msgtypebuffer.length);

                    switch (msgtypebuffer[0]) {
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
                            mActivity.incRxObjectsBad();
                            Log.w("UAVTalk", "Received NACK Object");
                            break;
                        default:
                            mActivity.incRxObjectsBad();
                            Log.w("UAVTalk", "Received bad Object Type " + H.bytesToHex(msgtypebuffer));
                            continue;
                    }

                    lenbuffer = bufferRead(lenbuffer.length);

                    int lb1 = lenbuffer[1] & 0x000000ff;
                    int lb2 = lenbuffer[0] & 0x000000ff;
                    int len = lb1 << 8 | lb2;

                    if (len > 266 || len < 10) {
                        mActivity.incRxObjectsBad();
                        continue; // maximum possible packet size
                    }

                    oidbuffer = bufferRead(oidbuffer.length);
                    iidbuffer = bufferRead(iidbuffer.length);
                    databuffer = bufferRead(len - 10);
                    crcbuffer = bufferRead(crcbuffer.length);

                    if (lenbuffer.length != 2 || oidbuffer.length != 4 || iidbuffer.length != 2
                            || databuffer.length == 0 || crcbuffer.length != 1) {
                        mActivity.incRxObjectsBad();
                        continue;
                    }

                    byte[] bmsg = H.concatArray(syncbuffer, msgtypebuffer);
                    bmsg = H.concatArray(bmsg, lenbuffer);
                    bmsg = H.concatArray(bmsg, oidbuffer);
                    bmsg = H.concatArray(bmsg, iidbuffer);
                    bmsg = H.concatArray(bmsg, databuffer);
                    int crc = H.crc8(bmsg, 0, bmsg.length);
                    bmsg = H.concatArray(bmsg, crcbuffer);


                    if ((((int) crcbuffer[0] & 0xff) == (crc & 0xff))) {
                        mActivity.incRxObjectsGood();
                    } else {
                        mActivity.incRxObjectsBad();
                        continue;
                    }

                    try {
                        UAVTalkMessage msg = new UAVTalkMessage(bmsg);
                        UAVTalkObject myObj = mObjectTree.getObjectFromID(H.intToHex(msg.getObjectId()));
                        UAVTalkObjectInstance myIns;

                        try {
                            myIns = myObj.getInstance(msg.getInstanceId());
                            myIns.setData(msg.getData());
                            myObj.setInstance(myIns);
                        } catch (Exception e) {
                            myIns = new UAVTalkObjectInstance(msg.getInstanceId(), msg.getData());
                            myObj.setInstance(myIns);
                        }

                        mObjectTree.updateObject(myObj);
                        if (isLogging()) {
                            log(bmsg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    if (mmInStream != null) {
                        try {
                            mmInStream.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    connectionLost();
                    break;
                }
            }
        }

        private byte[] bufferRead(int dlen) throws IOException {
            int read = 0;
            byte[] buffer = new byte[dlen];
            read = mmInStream.read(buffer);
            int pos = read;
            while (read < dlen) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                byte[] readmore = new byte[dlen - read];
                pos = mmInStream.read(readmore);
                read += pos;
                try {
                    System.arraycopy(readmore, 0, buffer, dlen - pos, readmore.length);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    Log.e("BLUETOOTH", "Bad Packet, should not happen.");
                    return new byte[0];
                }

            }
            return buffer;
        }

        public void write(byte[] buffer) {
            try {
                mActivity.incTxObjects();
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
