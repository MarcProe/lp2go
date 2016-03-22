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

import android.content.Context;
import android.util.Log;

import net.proest.lp2go3.H;
import net.proest.lp2go3.MainActivity;
import net.proest.lp2go3.UAVTalk.UAVTalkDeviceHelper;
import net.proest.lp2go3.UAVTalk.UAVTalkObjectTree;

import java.io.FileOutputStream;
import java.util.Arrays;

public abstract class UAVTalkDevice {
    public static final byte UAVTALK_DISCONNECTED = 0x00;
    public static final byte UAVTALK_HANDSHAKE_REQUESTED = 0x01;
    public static final byte UAVTALK_HANDSHAKE_ACKNOWLEDGED = 0x02;
    public static final byte UAVTALK_CONNECTED = 0x03;
    protected volatile UAVTalkObjectTree mObjectTree;
    MainActivity mActivity;
    private FileOutputStream mLogOutputStream;
    private String mLogFileName = "OP-YYYY-MM-DD_HH-MM-SS";
    private long mLogStartTimeStamp;
    private boolean mIsLogging = false;
    private long mLogBytesLoggedUAV = 0;
    private long mLogBytesLoggedOPL = 0;
    private long mLogObjectsLogged = 0;
    private int mUavTalkConnectionState = 0x00;
    public UAVTalkDevice(MainActivity mActivity) throws IllegalStateException {
        this.mActivity = mActivity;
    }

    public UAVTalkObjectTree getObjectTree() {
        return mObjectTree;
    }

    public long getLogBytesLoggedUAV() {
        return mLogBytesLoggedUAV;
    }

    public long getLogBytesLoggedOPL() {
        return mLogBytesLoggedOPL;
    }

    public long getLogObjectsLogged() {
        return this.mLogObjectsLogged;
    }

    public long getLogStartTimeStamp() {
        return this.mLogStartTimeStamp;
    }

    public boolean isLogging() {
        return this.mIsLogging;
    }

    public void setLogging(boolean logNow) {
        if (mIsLogging == logNow) {   //if we are already logging, and we should start, just return
            return;                 //if we are not logging and should stop, nothing to do as well
        }

        mIsLogging = logNow;

        try {       //anyway, close the current stream
            mLogOutputStream.close();
        } catch (Exception e) {
            // e.printStackTrace();
        }

        if (mIsLogging) {  //if logging should start, create new stream
            mActivity.deleteFile(mLogFileName); //delete old log
            mLogFileName = H.getLogFilename();

            try {
                mLogOutputStream = mActivity.openFileOutput(mLogFileName, Context.MODE_PRIVATE);
                //outputStream.write(string.getBytes());
                //outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mLogStartTimeStamp = System.currentTimeMillis();  //and set the time offset
            mLogBytesLoggedOPL = 0;
            mLogBytesLoggedUAV = 0;
            mLogObjectsLogged = 0;
        }
    }

    public void log(byte[] b) {
        if (b == null) return;
        try {
            long time = System.currentTimeMillis() - mLogStartTimeStamp;
            long len = b.length;

            @SuppressWarnings("ConstantConditions") //time is long, so reverse8bytes is just fine.
            byte[] btime = Arrays.copyOfRange(H.reverse8bytes(H.toBytes(time)), 0, 4);
            byte[] blen = H.reverse8bytes(H.toBytes(len));

            byte msg[] = H.concatArray(btime, blen);
            msg = H.concatArray(msg, b);

            mLogOutputStream.write(msg);
            mLogBytesLoggedUAV += b.length;
            mLogBytesLoggedOPL += msg.length;
            mLogObjectsLogged++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void start();

    public abstract void stop();

    public boolean sendSettingsObject(String objectName, int instance, String fieldName, int element, byte[] newFieldData) {
        return sendSettingsObject(
                objectName,
                instance,
                fieldName,
                element,
                newFieldData,
                false
        );
    }

    protected abstract boolean writeByteArray(byte[] bytes);

    public abstract boolean sendAck(String objectId, int instance);

    public abstract boolean sendSettingsObject(String objectName, int instance);

    public abstract boolean sendSettingsObject(String objectName, int instance, String fieldName, int element, byte[] newFieldData, final boolean block);

    public boolean sendSettingsObject(String objectName, int instance, String fieldName, String elementName, byte[] newFieldData, final boolean block) {
        return sendSettingsObject(
                objectName,
                instance,
                fieldName,
                mObjectTree.getElementIndex(objectName, fieldName, elementName),
                newFieldData,
                block
        );
    }

    public boolean sendSettingsObject(String objectName, int instance, String fieldName, String elementName, byte[] newFieldData) {
        return sendSettingsObject(
                objectName,
                instance,
                fieldName,
                mObjectTree.getElementIndex(objectName, fieldName, elementName),
                newFieldData,
                false
        );
    }
    public abstract boolean requestObject(String objectName);

    public abstract boolean requestObject(String objectName, int instance);

    public boolean savePersistent(String saveObjectName) {
        boolean retval = true;

        byte[] send;

        mObjectTree.getObjectFromName("ObjectPersistence").setWriteBlocked(true);

        byte[] op = {0x02};
        UAVTalkDeviceHelper.updateSettingsObject(mObjectTree, "ObjectPersistence", 0, "Operation", 0, op);

        byte[] sel = {0x00};
        UAVTalkDeviceHelper.updateSettingsObject(mObjectTree, "ObjectPersistence", 0, "Selection", 0, sel);
        String sid = mObjectTree.getXmlObjects().get(saveObjectName).getId();

        byte[] oid = H.reverse4bytes(H.hexStringToByteArray(sid));

        UAVTalkDeviceHelper.updateSettingsObject(mObjectTree, "ObjectPersistence", 0, "ObjectID", 0, oid);

        //for the last things we set, we can just use the sendsettingsobject. It will call updateSettingsObjectDeprecated for the last field.
        byte[] ins = {0x00};
        UAVTalkDeviceHelper.updateSettingsObject(mObjectTree, "ObjectPersistence", 0, "InstanceID", 0, ins);

        sendSettingsObject("ObjectPersistence", 0);

        Log.d("OBJ", "" + sid + " " + H.bytesToHex(oid));

        mObjectTree.getObjectFromName("ObjectPersistence").setWriteBlocked(false);

        return retval;
    }

    public abstract boolean isConnected();

    public abstract boolean isConnecting();

    public String getLogFileName() {
        return mLogFileName;
    }

    public void handleHandshake(byte flightTelemtryStatusField) {

        if (mUavTalkConnectionState == UAVTALK_DISCONNECTED) {
            //Send Handshake initiator packet (HANDSHAKE_REQUEST)
            byte[] msg = new byte[1];
            msg[0] = UAVTALK_HANDSHAKE_REQUESTED;
            sendSettingsObject("GCSTelemetryStats", 0, "Status", 0, msg);
            mUavTalkConnectionState = UAVTALK_HANDSHAKE_REQUESTED;
        } else if (flightTelemtryStatusField == UAVTALK_HANDSHAKE_ACKNOWLEDGED && mUavTalkConnectionState == UAVTALK_HANDSHAKE_REQUESTED) {
            byte[] msg = new byte[1];
            msg[0] = UAVTALK_CONNECTED;
            sendSettingsObject("GCSTelemetryStats", 0, "Status", 0, msg);
            mUavTalkConnectionState = UAVTALK_CONNECTED;
        } else if (flightTelemtryStatusField == UAVTALK_CONNECTED && mUavTalkConnectionState == UAVTALK_CONNECTED) {
            //We are connected, that is good.
        }
    }
}
