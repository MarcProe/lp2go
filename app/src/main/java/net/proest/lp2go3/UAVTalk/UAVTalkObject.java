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

import java.util.Hashtable;

public class UAVTalkObject {

    private String id;

    private Hashtable<Integer, UAVTalkObjectInstance> instances;

    public UAVTalkObject(String id) {
        this.id = id;
        instances = new Hashtable<Integer, UAVTalkObjectInstance>();
    }

    public static byte[] getReqMsg(byte type, String objectID, int instance) {
        byte[] retval = new byte[13];

        retval[0] = 0x02;
        retval[1] = 0x30;
        retval[2] = 0x3c;
        retval[3] = type;

        retval[4] = 0x0a;
        retval[5] = 0x00;

        byte[] objId = H.hexStringToByteArray(objectID);

        retval[6] = objId[3];
        retval[7] = objId[2];
        retval[8] = objId[1];
        retval[9] = objId[0];

        byte[] iid = H.toBytes(instance);

        retval[10] = iid[3];
        retval[11] = iid[2];

        retval[12] = (byte) (H.crc8(retval, 2, 10) & 0xff);

        return retval;
    }

    public String toString() {
        return id;
    }

    public String getId() {
        return id;
    }

    public UAVTalkObjectInstance getInstance(int id) {
        return instances.get(Integer.valueOf(id));
    }

    public void setInstance(UAVTalkObjectInstance instance) {
        instances.put(Integer.valueOf(instance.getId()), instance);
    }

    public int size() {
        return instances.size();
    }

    public byte[] toMsg(byte type, int instance) {
        int length = 0;

        UAVTalkObjectInstance inst = instances.get(instance);

        byte[] instData = inst.getData();
        byte[] retval = new byte[instData.length + 13];

        System.arraycopy(instData, 0, retval, 12, instData.length);

        retval[0] = 0x02;
        retval[1] = 0x30;
        retval[2] = 0x3c;
        retval[3] = type;

        byte[] len = H.toBytes(instData.length + 10);
        retval[4] = len[3];
        retval[5] = len[2];

        byte[] objId = H.hexStringToByteArray(this.id);

        retval[6] = objId[3];
        retval[7] = objId[2];
        retval[8] = objId[1];
        retval[9] = objId[0];

        byte[] iid = H.toBytes(instance);

        retval[10] = iid[3];
        retval[11] = iid[2];

        retval[retval.length - 1] = (byte) (H.crc8(retval, 2, retval.length - 3) & 0xff);

        return retval;
    }
}
