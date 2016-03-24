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

public class UAVTalkMessage {

    private byte mType;
    private int mLength;
    private int mObjectId;
    private int mInstanceId;
    private byte[] mData;

    public UAVTalkMessage(byte[] bytes, int offset) {
        if (bytes.length >= 10 + offset) {

            //this.mSync = bytes[offset];
            this.mType = bytes[1 + offset];

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

           /* if(mType == 0x23) {
                Log.d("MSG", "NACK");
            }*/

        } else {
            throw new UnsupportedOperationException("Bad Message, < 12 bytes");
        }

        if (this.mLength > 10 + offset && bytes.length - offset >= this.mLength) {
            this.mData = new byte[this.mLength - 10];
            System.arraycopy(bytes, 10 + offset, this.mData, 0, this.mLength - 10);
        }
    }

    @Deprecated
    public UAVTalkMessage(byte[] bytes) {
        if (bytes.length >= 12) {
            //mHead = bytes[0] + bytes[1]*8;
            //this.mSync = bytes[2];
            this.mType = bytes[3];

            int lb1 = bytes[5] & 0x000000ff;
            int lb2 = bytes[4] & 0x000000ff;

            this.mLength = lb1 << 8 | lb2;

            int ob1 = bytes[9] & 0x000000ff;
            int ob2 = bytes[8] & 0x000000ff;
            int ob3 = bytes[7] & 0x000000ff;
            int ob4 = bytes[6] & 0x000000ff;

            this.mObjectId = ob1 << 24 | ob2 << 16 | ob3 << 8 | ob4;

            int ib1 = bytes[11] & 0x000000ff;
            int ib2 = bytes[10] & 0x000000ff;

            this.mInstanceId = ib1 << 8 | ib2;

        } else {
            throw new UnsupportedOperationException("Bad Message, < 12 bytes");
        }

        if (this.mLength > 12 && bytes.length >= this.mLength) {
            this.mData = new byte[this.mLength - 10];
            System.arraycopy(bytes, 12, this.mData, 0, this.mLength - 10);
        }
    }

    public byte getType() {
        return mType;
    }

    public int getLength() {
        return mLength;
    }

    public int getObjectId() {
        return mObjectId;
    }

    public int getInstanceId() {
        return mInstanceId;
    }

    public byte[] getData() {
        return mData;
    }
}
