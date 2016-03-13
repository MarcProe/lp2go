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

package net.proest.lp2go3.UAVTalk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import net.proest.lp2go3.H;
import net.proest.lp2go3.MainActivity;
import net.proest.lp2go3.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.UUID;

public class UAVTalkBluetoothDevice extends UAVTalkDevice {
    public static final int STATE_NONE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    //private final MainActivity mActivity;
    private WaiterThread mWaiterThread;
    private UAVTalkObjectTree mObjectTree;
    private BluetoothAdapter mBluetoothAdapter;
    private ConnectThread mConnectThread;
    private BluetoothDevice mDevice;
    private int mState;

    public UAVTalkBluetoothDevice(MainActivity activity, Hashtable<String, UAVTalkXMLObject> xmlObjects) {
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
        } else {
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
    public UAVTalkObjectTree getObjectTree() {
        return mObjectTree;
    }

    @Override
    public boolean sendSettingsObject(String objectName, int instance, String fieldName, int element, byte[] newFieldData) {
        byte[] send = UAVTalkDeviceHelper.createSettingsObjectByte(mObjectTree, objectName, instance, fieldName, element, newFieldData);
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
        UAVTalkXMLObject xmlObj = mObjectTree.getXmlObjects().get(objectName);
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
    public boolean isConnecting() {
        return mState != STATE_NONE;
    }


    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {  //this is only called when we weren't connected before
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
            setName("ConnectThread");

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
                    e1.printStackTrace();
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

        public void run() {

            byte[] seekbuffer = new byte[1];
            byte[] syncbuffer = new byte[3];
            byte[] msgtypebuffer = new byte[1];
            byte[] lenbuffer = new byte[2];
            byte[] oidbuffer = new byte[4];
            byte[] iidbuffer = new byte[2];
            byte[] databuffer = new byte[0];
            byte[] crcbuffer = new byte[1];
            int read;

            mActivity.setRxObjectsGood(0);
            mActivity.setRxObjectsBad(0);
            mActivity.setTxObjects(0);


            while (true) {
                try {
                    while (seekbuffer[0] != 0x3c) {
                        read = mmInStream.read(seekbuffer);
                    }
                    seekbuffer[0] = 0x00;
                    syncbuffer[2] = 0x3c;

                    //read = mmInStream.read(msgtypebuffer);
                    //if (read != 1) Log.d("TYP", "UNDERRUN " + read);
                    msgtypebuffer = bufferRead(msgtypebuffer.length);
                    if (msgtypebuffer[0] != 0x20) {
                        mActivity.incRxObjectsBad();
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
