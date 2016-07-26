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

import java.util.Arrays;

public class UAVTalkMetaData {

    public static int READONLY = 0;
    public static int READWRITE = 1;

    public static int ACKED_ACK = 1;
    public static int ACKED_NACK = 0;

    public static int UPDATEMODE_MANUAL = 0;
    /**
     * Manually update object, by calling the updated() function
     */
    public static int UPDATEMODE_PERIODIC = 1;
    /**
     * Automatically update object at periodic intervals
     */
    public static int UPDATEMODE_ONCHANGE = 2;
    /**
     * Only update object when its data changes
     */
    public static int UPDATEMODE_THROTTLED = 3;
    /**
     * Object is updated on change, but not more often than the interval time
     */

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

    public UAVTalkMetaData(byte[] data) {
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
        return "" + getAccess() + " " + getGcsAccess() + " " + getTelemetryAcked() + " " +
                getGcsTelementryAcked() + " " + getTelemetryUpdateMode() + " " +
                getGcsTelemetryUpdateMode() + " " + getLoggingUpdateMode() + " " +
                getLoggingUpdatePeriod() + " " + getGcsTelemetryUpdatePeriod() + " " +
                getFlightTelemetryUpdatePeriod();
    }

    public byte[] getData() {
        /*
        final byte[] bFlags                           = Arrays.copyOfRange(data, 0, 2);
        final byte[] bFlightTelemetryUpdatePeriod     = Arrays.copyOfRange(data, 2, 4);
        final byte[] bGcsTelemetryUpdatePeriod        = Arrays.copyOfRange(data, 4, 6);
        final byte[] bLoggingUpdatePeriod             = Arrays.copyOfRange(data, 6, 8);
         */
        byte[] retval = new byte[8];

        return retval;

    }

    /**
     * Object metadata, each object has a meta object that holds its metadata. The metadata define
     * properties for each object and can be used by multiple modules (e.g. telemetry and logger)
     *
     * The object metadata flags are packed into a single 16 bit integer.
     * The bits in the flag field are defined as:
     *
     *   Bit(s)  Name                       Meaning
     *   ------  ----                       -------
     *      0    access                     Defines the access level for the local transactions (readonly=0 and readwrite=1)
     *      1    gcsAccess                  Defines the access level for the local GCS transactions (readonly=0 and readwrite=1), not used in the flight s/w
     *      2    telemetryAcked             Defines if an ack is required for the transactions of this object (1:acked, 0:not acked)
     *      3    gcsTelemetryAcked          Defines if an ack is required for the transactions of this object (1:acked, 0:not acked)
     *    4-5    telemetryUpdateMode        Update mode used by the telemetry module (UAVObjUpdateMode)
     *    6-7    gcsTelemetryUpdateMode     Update mode used by the GCS (UAVObjUpdateMode)
     *    8-9    mLoggingUpdateMode          Update mode used by the logging module (UAVObjUpdateMode)
     */
    //typedef struct {
    //   quint16 flags; /** Defines flags for update and logging modes and whether an update should be ACK'd (bits defined above) */
    //   quint16 flightTelemetryUpdatePeriod; /** Update period used by the telemetry module (only if telemetry mode is PERIODIC) */
    //   quint16 gcsTelemetryUpdatePeriod; /** Update period used by the GCS (only if telemetry mode is PERIODIC) */
    //   quint16 loggingUpdatePeriod; /** Update period used by the logging module (only if logging mode is PERIODIC) */
    //} __attribute__((packed)) Metadata;


}