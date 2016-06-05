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

import org.librepilot.lp2go.uavtalk.device.FcWaiterThread;

public class UAVTalkMessage {

    private byte[] mData;
    private int mInstanceId;
    private int mLength;
    private int mObjectId;
    private byte[] mRaw;
    private int mTimestamp;
    private byte mType;

    public UAVTalkMessage(byte[] bytes, int offset) {
        this.mRaw = bytes;
        int tsoffset = 0;
        if (bytes.length >= 10 + offset) {

            this.mType = bytes[1 + offset];

            if (((byte) 0x80 & this.mType) == 0x80) {
                tsoffset = 2;
            }

            int lb1 = bytes[3 + offset] & 0x000000ff;
            int lb2 = bytes[2 + offset] & 0x000000ff;

            this.mLength = lb1 << 8 | lb2;

            int ob1 = bytes[7 + offset] & 0x000000ff;
            int ob2 = bytes[6 + offset] & 0x000000ff;
            int ob3 = bytes[5 + offset] & 0x000000ff;
            int ob4 = bytes[4 + offset] & 0x000000ff;

            this.mObjectId = ob1 << 24 | ob2 << 16 | ob3 << 8 | ob4;

            int ib1 = bytes[9 + offset] & 0x000000ff;
            int ib2 = bytes[8 + offset] & 0x000000ff;

            this.mInstanceId = ib1 << 8 | ib2;

            if ((FcWaiterThread.MASK_TIMESTAMP & this.mType) == FcWaiterThread.MASK_TIMESTAMP) {
                int ts1 = bytes[9 + offset] & 0x000000ff;
                int ts2 = bytes[8 + offset] & 0x000000ff;

                this.mTimestamp = ts1 << 8 | ts2;
            } else {
                mTimestamp = -1;
            }

        } else {
            throw new UnsupportedOperationException("Bad Message, < 12 bytes");
        }

        if (this.mLength > 10 + offset && bytes.length - offset >= this.mLength) {
            this.mData = new byte[this.mLength - (10 + tsoffset)];
            System.arraycopy(bytes, 10 + tsoffset + offset, this.mData, 0,
                    this.mLength - (10 + tsoffset));
        }
    }

    public byte[] getData() {
        return mData;
    }

    public int getInstanceId() {
        return mInstanceId;
    }

    public int getLength() {
        return mLength;
    }

    public int getObjectId() {
        return mObjectId;
    }

    public byte[] getRaw() {
        return mRaw;
    }

    public int getTimestamp() {
        return mTimestamp;
    }

    public byte getType() {
        return mType;
    }
}
