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

import net.proest.lp2go3.H;
import net.proest.lp2go3.UAVTalk.UAVTalkObject;
import net.proest.lp2go3.VisualLog;

abstract class FcWaiterThread extends Thread {
    final FcDevice mDevice;

    FcWaiterThread(FcDevice device) {
        this.mDevice = device;
    }

    protected abstract void stopThread();

    boolean handleMessageType(byte msgType, UAVTalkObject obj) {
        switch (msgType) {
            case 0x20:
                //handle default package, nothing to do
                break;
            case 0x21:
                //handle request message, nobody should request from LP2Go (so we don't implement this)
                VisualLog
                        .e("UAVTalk", "Received Object Request, but won't send any " + obj.getId());
                break;
            case 0x22:
                //handle object with ACK REQ, means send ACK
                mDevice.sendAck(obj.getId(), 0);
                VisualLog.d("UAVTalk", "Received Object with ACK Request " + obj.getId());
                break;
            case 0x23:
                //handle received ACK, e.g. save in Object that it has been acknowledged
                VisualLog.d("UAVTalk", "Received ACK Object " + obj.getId());
                break;
            case 0x24:
                //handle NACK, show warning and add to request blacklist
                mDevice.nackedObjects.add(obj.getId());
                mDevice.mActivity.incRxObjectsBad();
                VisualLog.w("UAVTalk", "Received NACK Object " + obj.getId());
                break;
            default:
                mDevice.mActivity.incRxObjectsBad();
                byte[] b = new byte[1];
                b[0] = msgType;
                VisualLog.w("UAVTalk", "Received bad Object Type " + H.bytesToHex(b));
                return false;
        }
        return true;
    }
}
