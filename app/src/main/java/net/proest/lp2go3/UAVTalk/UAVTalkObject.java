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

import net.proest.lp2go3.H;

import java.util.HashMap;

public class UAVTalkObject {

    private final String mId;
    private final HashMap<Integer, UAVTalkObjectInstance> mInstances;
    private boolean mWriteBlocked = false;

    public UAVTalkObject(String id) {
        this.mId = id;

        mInstances = new HashMap<Integer, UAVTalkObjectInstance>();
    }

    public static byte[] getReqMsg(byte type, String objectId, int instance) {
        byte[] retval = new byte[11];

        retval[0] = 0x3c;
        retval[1] = type;

        retval[2] = 0x0a;
        retval[3] = 0x00;

        byte[] objId = H.hexStringToByteArray(objectId);

        retval[4] = objId[3];
        retval[5] = objId[2];
        retval[6] = objId[1];
        retval[7] = objId[0];

        byte[] iid = H.toBytes(instance);

        retval[8] = iid[3];
        retval[9] = iid[2];

        retval[10] = (byte) (H.crc8(retval, 0, 10) & 0xff);

        return retval;
    }

    public String getId() {
        return mId;
    }

    public HashMap<Integer, UAVTalkObjectInstance> getInstances() {
        return mInstances;
    }

    public boolean isWriteBlocked() {
        return mWriteBlocked;
    }

    public void setWriteBlocked(boolean mWriteBlocked) {
        this.mWriteBlocked = mWriteBlocked;
    }

    public void setInstance(UAVTalkObjectInstance instance) {
        mInstances.put(instance.getId(), instance);
    }

    public String toString() {
        return mId;
    }

    public UAVTalkObjectInstance getInstance(int id) {
        return mInstances.get(id);
    }

    public byte[] toMessage(byte type, int instance, boolean asAck) {
        UAVTalkObjectInstance inst = mInstances.get(instance);

        byte[] instData = null;

        if (inst != null) {
            instData = inst.getData();
        }

        if (instData == null) {
            return new byte[0];
        }

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

        byte[] objId = H.hexStringToByteArray(this.mId);

        retval[4] = objId[3];
        retval[5] = objId[2];
        retval[6] = objId[1];
        retval[7] = objId[0];

        byte[] iid = H.toBytes(instance);

        retval[8] = iid[3];
        retval[9] = iid[2];

        retval[retval.length - 1] = (byte) (H.crc8(retval, 0, retval.length - 1) & 0xff);

        return retval;
    }
}
