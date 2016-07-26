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
        byte[] bFlags = Arrays.copyOfRange(data, 0, 2);
        byte[] bFlightTelemetryUpdatePeriod = Arrays.copyOfRange(data, 2, 2);
        byte[] bGcsTelemetryUpdatePeriod = Arrays.copyOfRange(data, 4, 2);
        byte[] bLoggingUpdatePeriod = Arrays.copyOfRange(data, 6, 2);

        int flags = ((bFlags[0] << 8) | bFlags[1]);
        mFlightTelemetryUpdatePeriod =
                ((bFlightTelemetryUpdatePeriod[0] << 8) | bFlightTelemetryUpdatePeriod[1]);
        mGcsTelemetryUpdatePeriod =
                ((bGcsTelemetryUpdatePeriod[0] << 8) | bGcsTelemetryUpdatePeriod[1]);
        mLoggingUpdatePeriod = ((bLoggingUpdatePeriod[0] << 8) | bLoggingUpdatePeriod[1]);

        mAccess = flags & 0b0000000000000001;
        mGcsAccess = flags & 0b0000000000000010;
        mTelemetryAcked = flags & 0b0000000000000100;
        mGcsTelementryAcked = flags & 0b0000000000001000;
        mTelemetryUpdateMode = flags & 0b0000000000110000;
        mGcsTelemetryUpdateMode = flags & 0b0000000011000000;
        mLoggingUpdateMode = flags & 0b0000001100000000;
    }

    public int getAccess() {
        return mAccess;
    }

    public int getGcsAccess() {
        return mGcsAccess;
    }

    public int getTelemetryAcked() {
        return mTelemetryAcked;
    }

    public int getGcsTelementryAcked() {
        return mGcsTelementryAcked;
    }

    public int getTelemetryUpdateMode() {
        return mTelemetryUpdateMode;
    }

    public int getGcsTelemetryUpdateMode() {
        return mGcsTelemetryUpdateMode;
    }

    public int getLoggingUpdateMode() {
        return mLoggingUpdateMode;
    }

    public int getmLoggingUpdatePeriod() {
        return mLoggingUpdatePeriod;
    }

    public int getmGcsTelemetryUpdatePeriod() {
        return mGcsTelemetryUpdatePeriod;
    }

    public int getmFlightTelemetryUpdatePeriod() {
        return mFlightTelemetryUpdatePeriod;
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