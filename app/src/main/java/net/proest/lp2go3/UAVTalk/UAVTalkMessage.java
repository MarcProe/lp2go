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

    private int head;
    private byte sync;
    private byte type;
    private int length;
    private int oID;
    private int iID;
    private int timestamp;
    private byte[] data;
    private byte crc;

    public UAVTalkMessage(byte[] bytes) {
        if (bytes.length >= 12) {
            //head = bytes[0] + bytes[1]*8;
            this.sync = bytes[2];
            this.type = bytes[3];

            int lb1 = bytes[5] & 0x000000ff;
            int lb2 = bytes[4] & 0x000000ff;

            this.length = lb1 << 8 | lb2;

            int ob1 = bytes[9] & 0x000000ff;
            int ob2 = bytes[8] & 0x000000ff;
            int ob3 = bytes[7] & 0x000000ff;
            int ob4 = bytes[6] & 0x000000ff;

            this.oID = ob1 << 24 | ob2 << 16 | ob3 << 8 | ob4;

            int ib1 = bytes[11] & 0x000000ff;
            int ib2 = bytes[10] & 0x000000ff;

            this.iID = ib1 << 8 | ib2;

        } else {
            throw new UnsupportedOperationException("Bad Message, < 12 bytes");
        }

        if (this.length > 12 && bytes.length >= this.length) {
            this.data = new byte[this.length - 10];
            System.arraycopy(bytes, 12, this.data, 0, this.length - 10);
        }
    }

    public byte getCrc() {
        return crc;
    }

    public byte getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public int getoID() {
        return oID;
    }

    public int getiID() {
        return iID;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public byte[] getData() {
        return data;
    }
}
