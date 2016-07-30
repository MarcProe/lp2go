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


package org.librepilot.lp2go.uavtalk;

import android.util.Log;

import org.librepilot.lp2go.H;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class UAVTalkMetaData {

    public static int READONLY = 0;
    public static int READWRITE = 1;
    public static int ACKED_ACK = 1;
    public static int ACKED_NACK = 0;
    public static int UPDATEMODE_MANUAL = 0;
    public static int UPDATEMODE_PERIODIC = 1;
    public static int UPDATEMODE_ONCHANGE = 2;
    public static int UPDATEMODE_THROTTLED = 3;

    private final String mMetaId;

    private int mAccess;
    private int mGcsAccess;
    private int mTelemetryAcked;
    private int mGcsTelementryAcked;
    private int mTelemetryUpdateMode;
    private int mGcsTelemetryUpdateMode;
    private int mLoggingUpdateMode;

    private int mFlightTelemetryUpdatePeriod;
    private int mGcsTelemetryUpdatePeriod;
    private int mLoggingUpdatePeriod;

    public UAVTalkMetaData(String metaId, byte[] data) {

        mMetaId = metaId;

        Log.d("TTT", "" + data.length);
        Log.d("TTT", H.bytesToHex(data));
        final byte[] bFlags = Arrays.copyOfRange(data, 0, 2);
        final byte[] bFlightTelemetryUpdatePeriod = Arrays.copyOfRange(data, 2, 4);
        final byte[] bGcsTelemetryUpdatePeriod = Arrays.copyOfRange(data, 4, 6);
        final byte[] bLoggingUpdatePeriod = Arrays.copyOfRange(data, 6, 8);

        int flags = (((bFlags[1] & 0xff) << 8) | (bFlags[0] & 0xff));
        mFlightTelemetryUpdatePeriod =
                (((bFlightTelemetryUpdatePeriod[1] & 0xff) << 8) |
                        (bFlightTelemetryUpdatePeriod[0] & 0xff));
        mGcsTelemetryUpdatePeriod =
                (((bGcsTelemetryUpdatePeriod[1] & 0xff) << 8) |
                        (bGcsTelemetryUpdatePeriod[0] & 0xff));
        mLoggingUpdatePeriod = (((bLoggingUpdatePeriod[1] & 0xff) << 8) |
                (bLoggingUpdatePeriod[0] & 0xff));

        mAccess = flags & 0b0000000000000001;
        mGcsAccess = (flags & 0b0000000000000010) >>> 1;
        mTelemetryAcked = (flags & 0b0000000000000100) >>> 2;
        mGcsTelementryAcked = (flags & 0b0000000000001000) >>> 3;
        mTelemetryUpdateMode = (flags & 0b0000000000110000) >>> 4;
        mGcsTelemetryUpdateMode = (flags & 0b0000000011000000) >>> 6;
        mLoggingUpdateMode = (flags & 0b0000001100000000) >>> 8;
    }

    public int getAccess() {
        return mAccess;
    }

    public void setAccess(int mAccess) {
        this.mAccess = mAccess;
    }

    public int getGcsAccess() {
        return mGcsAccess;
    }

    public void setGcsAccess(int mGcsAccess) {
        this.mGcsAccess = mGcsAccess;
    }

    public int getTelemetryAcked() {
        return mTelemetryAcked;
    }

    public void setTelemetryAcked(int mTelemetryAcked) {
        this.mTelemetryAcked = mTelemetryAcked;
    }

    public int getGcsTelementryAcked() {
        return mGcsTelementryAcked;
    }

    public void setGcsTelementryAcked(int mGcsTelementryAcked) {
        this.mGcsTelementryAcked = mGcsTelementryAcked;
    }

    public int getTelemetryUpdateMode() {
        return mTelemetryUpdateMode;
    }

    public void setTelemetryUpdateMode(int mTelemetryUpdateMode) {
        this.mTelemetryUpdateMode = mTelemetryUpdateMode;
    }

    public int getGcsTelemetryUpdateMode() {
        return mGcsTelemetryUpdateMode;
    }

    public void setGcsTelemetryUpdateMode(int mGcsTelemetryUpdateMode) {
        this.mGcsTelemetryUpdateMode = mGcsTelemetryUpdateMode;
    }

    public int getLoggingUpdateMode() {
        return mLoggingUpdateMode;
    }

    public void setLoggingUpdateMode(int mLoggingUpdateMode) {
        this.mLoggingUpdateMode = mLoggingUpdateMode;
    }

    public int getLoggingUpdatePeriod() {
        return mLoggingUpdatePeriod;
    }

    public void setLoggingUpdatePeriod(int mLoggingUpdatePeriod) {
        this.mLoggingUpdatePeriod = mLoggingUpdatePeriod;
    }

    public int getGcsTelemetryUpdatePeriod() {
        return mGcsTelemetryUpdatePeriod;
    }

    public void setGcsTelemetryUpdatePeriod(int mGcsTelemetryUpdatePeriod) {
        this.mGcsTelemetryUpdatePeriod = mGcsTelemetryUpdatePeriod;
    }

    public int getFlightTelemetryUpdatePeriod() {
        return mFlightTelemetryUpdatePeriod;
    }

    public void setFlightTelemetryUpdatePeriod(int mFlightTelemetryUpdatePeriod) {
        this.mFlightTelemetryUpdatePeriod = mFlightTelemetryUpdatePeriod;
    }

    public String toString() {
        return H.bytesToHex(getData()) + " # " + getAccess() + " " + getGcsAccess() + " " + getTelemetryAcked() + " " +
                getGcsTelementryAcked() + " " + getTelemetryUpdateMode() + " " +
                getGcsTelemetryUpdateMode() + " " + getLoggingUpdateMode() + " " +
                getLoggingUpdatePeriod() + " " + getGcsTelemetryUpdatePeriod() + " " +
                getFlightTelemetryUpdatePeriod();
    }

    private byte[] getData() {
        final byte[] retval = new byte[8];

        final int iFlags = mAccess |
                (mGcsAccess << 1) |
                (mTelemetryAcked << 2) |
                (mGcsTelementryAcked << 3) |
                (mTelemetryUpdateMode << 4) |
                (mGcsTelemetryUpdateMode << 6) |
                (mLoggingUpdateMode << 8);

        final byte[] bFlags = ByteBuffer.allocate(4).
                order(ByteOrder.LITTLE_ENDIAN).putInt(iFlags).array();
        final byte[] bFlightTelemetryUpdatePeriod = ByteBuffer.allocate(4).
                order(ByteOrder.LITTLE_ENDIAN).putInt(mFlightTelemetryUpdatePeriod).array();
        final byte[] bGcsTelemetryUpdatePeriod = ByteBuffer.allocate(4).
                order(ByteOrder.LITTLE_ENDIAN).putInt(mGcsTelemetryUpdatePeriod).array();
        final byte[] bLoggingUpdatePeriod = ByteBuffer.allocate(4).
                order(ByteOrder.LITTLE_ENDIAN).putInt(mLoggingUpdatePeriod).array();


        retval[0] = bFlags[0];
        retval[1] = bFlags[1];
        retval[2] = bFlightTelemetryUpdatePeriod[0];
        retval[3] = bFlightTelemetryUpdatePeriod[1];
        retval[4] = bGcsTelemetryUpdatePeriod[0];
        retval[5] = bGcsTelemetryUpdatePeriod[1];
        retval[6] = bLoggingUpdatePeriod[0];
        retval[7] = bLoggingUpdatePeriod[1];

        return retval;
    }

    public byte[] toMessage(byte type, boolean asAck) {

        byte[] instData = getData();
        byte[] retval;

        if (asAck) {
            retval = new byte[11]; //only the header and CRC
        } else {
            retval = new byte[instData.length + 11]; // data as well
            System.arraycopy(instData, 0, retval, 10, instData.length); // copy the data
        }

        retval[0] = 0x3c;
        retval[1] = type;

        byte[] len = H.toBytes(instData.length + 10);
        retval[2] = len[3];
        retval[3] = len[2];

        byte[] objId = H.hexStringToByteArray(this.mMetaId);

        retval[4] = objId[3];
        retval[5] = objId[2];
        retval[6] = objId[1];
        retval[7] = objId[0];

        byte[] iid = H.toBytes(0);

        retval[8] = iid[3];
        retval[9] = iid[2];

        retval[retval.length - 1] = (byte) (H.crc8(retval, 0, retval.length - 1) & 0xff);

        return retval;
    }
}