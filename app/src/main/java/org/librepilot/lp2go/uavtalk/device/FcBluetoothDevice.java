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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;

import org.librepilot.lp2go.H;
import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.uavtalk.UAVTalkDeviceHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkObject;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectTree;
import org.librepilot.lp2go.uavtalk.UAVTalkXMLObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class FcBluetoothDevice extends FcDevice {
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_NONE = 0;
    private final BluetoothAdapter mBluetoothAdapter;
    private ConnectThread mConnectThread;
    private android.bluetooth.BluetoothDevice mDevice;
    private int mState;
    private FcWaiterThread mWaiterThread;

    public FcBluetoothDevice(MainActivity activity, Map<String, UAVTalkXMLObject> xmlObjects) {
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


        String mDeviceAddress = mActivity.mBluetoothDeviceAddress.toUpperCase().replace('-', ':');
        //sharedPref.getString(mActivity.getString(R.string.SETTINGS_BT_MAC), "")
        //        .toUpperCase().replace('-', ':');
        //String reg1 = "^([0-9A-F]{2}[:]){5}([0-9A-F]{2})$";
        if (mDeviceAddress.matches("^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$")) {
            mDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
        }
        connect(mDevice);
    }

    @Override
    public boolean isConnected() {
        return mState == STATE_CONNECTED;
    }

    @Override
    public boolean isConnecting() {
        return mState != STATE_NONE;
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
            mWaiterThread.stopThread();
            mWaiterThread = null;
        }
        setState(STATE_NONE);
    }

    @Override
    protected boolean writeByteArray(byte[] out) {
        FcBluetoothWaiterThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) {
                return false;
            }
            r = (FcBluetoothWaiterThread) mWaiterThread;
        }
        r.write(out);
        return true;
    }

    @Override
    public boolean sendAck(String objectId, int instance) {
        byte[] send = mObjectTree.getObjectFromID(objectId).toMessage((byte) 0x23, instance, true);
        VisualLog.d("SEND", "" + H.bytesToHex(send));
        if (send != null) {
            writeByteArray(Arrays.copyOfRange(send, 0, send.length));
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean sendSettingsObject(String objectName, int instance) {
        byte[] send =
                mObjectTree.getObjectFromName(objectName).toMessage((byte) 0x22, instance, false);
        if (send != null) {
            writeByteArray(Arrays.copyOfRange(send, 0, send.length));
            requestObject(objectName, instance);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean sendSettingsObject(String objectName, int instance, String fieldName,
                                      int element, byte[] newFieldData, final boolean block) {
        boolean retval;
        if (block) {
            mObjectTree.getObjectFromName(objectName).setWriteBlocked(true);
        }
        UAVTalkDeviceHelper
                .updateSettingsObject(mObjectTree, objectName, instance, fieldName, element,
                        newFieldData);

        retval = sendSettingsObject(objectName, instance);

        if (block) {
            mObjectTree.getObjectFromName(objectName).setWriteBlocked(false);
        }
        return retval;
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
            VisualLog.d("NACKED", xmlObj.getId());
            return false;  //if it was already nacked, don't try to get it again
        }


        byte[] send = UAVTalkObject.getReqMsg((byte) 0x21, xmlObj.getId(), instance);

        writeByteArray(send);

        return true;
    }

    private synchronized void setState(int state) {
        mState = state;
    }

    void connectionLost() {
        mActivity.reconnect();

        setState(STATE_NONE);
    }

    private synchronized void connect(android.bluetooth.BluetoothDevice device) {
        if (mState == STATE_NONE) {
            if (mWaiterThread != null) {
                mWaiterThread.stopThread();
                mWaiterThread = null;
            }

            mConnectThread = new ConnectThread(device);
            mConnectThread.start();
            setState(STATE_CONNECTING);
        }
    }

    private void connectionFailed(Exception e) {
        //VisualLog.d("BT","ConnectionFailed", e);
        setState(STATE_NONE);
    }

    private synchronized void connected(
            BluetoothSocket socket) {  //this is only called when we weren't connected before
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mWaiterThread != null) {
            mWaiterThread.stopThread();
            mWaiterThread = null;
        }

        mWaiterThread = new FcBluetoothWaiterThread(socket, this);
        mWaiterThread.start();

        //toast("Connected to " + device.getName());

        setState(STATE_CONNECTED);
    }

    private class ConnectThread extends Thread {
        private final android.bluetooth.BluetoothDevice mmDevice;
        private BluetoothSocket mmSocket;

        public ConnectThread(android.bluetooth.BluetoothDevice device) {
            mmDevice = device;
        }

        public void run() {
            setName("LP2GoDeviceBluetoothConnectThread");

            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket = mmDevice.createRfcommSocketToServiceRecord(
                        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                mmSocket.connect();
            } catch (IOException e) {
                VisualLog.e("BT", "Default BT  Connection failed, trying fallback.");
                try {
                    // This is a workaround that reportedly helps on some older devices like HTC Desire, where using
                    // the standard createRfcommSocketToServiceRecord() method always causes connect() to fail.
                    //Method method = mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    mmSocket = (BluetoothSocket) mmDevice.getClass()
                            .getMethod("createRfcommSocket", new Class[]{int.class})
                            .invoke(mmDevice, 1);
                    mmSocket.connect();
                } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e1) {
                    VisualLog.e("BT", "Fallback BT  Connection failed, trying again.", e);
                    connectionFailed(e1);
                    try {
                        mmSocket.close();
                    } catch (IOException e2) {
                        VisualLog.e(e2);
                    } catch (NullPointerException e3) {
                        return;
                    }
                    return;
                } catch (NullPointerException e4) {
                    connectionFailed(e4);
                    return;
                }
            } catch (NullPointerException e5) {
                connectionFailed(e5);
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
                VisualLog.e(e);
            }
        }
    }
}
