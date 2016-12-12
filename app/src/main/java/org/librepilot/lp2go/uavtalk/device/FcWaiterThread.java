/*
 * @file   FcWaiterThread.java
 * @author The LibrePilot Project, http://www.librepilot.org Copyright (C) 2016.
 * @see    The GNU Public License (GPL) Version 3
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.librepilot.lp2go.uavtalk.device;

import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.helper.H;
import org.librepilot.lp2go.uavtalk.UAVTalkObject;

import java.text.MessageFormat;

public abstract class FcWaiterThread extends Thread {
    public final static byte MASK_TIMESTAMP = (byte) 0x80;
    final FcDevice mDevice;
    FcDevice.GuiEventListener mGuiEventListener;

    FcWaiterThread(FcDevice device) {
        this.mDevice = device;
    }

    public void setGuiEventListener(FcDevice.GuiEventListener gel) {
        mGuiEventListener = gel;
    }

    protected abstract void stopThread();

    final boolean handleMessageType(byte msgType, UAVTalkObject obj) {
        switch (msgType) {
            case (byte) 0x20:
            case (byte) 0xa0:
                //handle default package, nothing to do
                break;
            case (byte) 0x21:
            case (byte) 0xa1:
                //handle request message, nobody'd request from LP2Go (so we don't implement this)
                VisualLog.e("UAVTalk",
                        MessageFormat.format("Received Object Request, but won't send any {0}",
                                obj.getId()));
                break;
            case (byte) 0x22:
            case (byte) 0xa2:
                //handle object with ACK REQ, means send ACK
                mDevice.sendAck(obj.getId(), 0);
                VisualLog.d("UAVTalk",
                        MessageFormat.format("Received Object with ACK Request {0}", obj.getId()));
                break;
            case (byte) 0x23:
            case (byte) 0xa3:
                //handle received ACK, e.g. save in Object that it has been acknowledged
                VisualLog.d("UAVTalk", MessageFormat.format("Received ACK Object {0}",
                        obj.getId()));
                break;
            case (byte) 0x24:
            case (byte) 0xa4:
                //handle NACK, show warning and add to request blacklist
                mDevice.nackedObjects.add(obj.getId());
                mDevice.mActivity.incRxObjectsBad();
                VisualLog.w("UAVTalk", MessageFormat.format("Received NACK Object {0}",
                        obj.getId()));
                break;
            default:
                mDevice.mActivity.incRxObjectsBad();
                byte[] b = new byte[1];
                b[0] = msgType;
                VisualLog.w("UAVTalk", MessageFormat.format("Received bad Object Type {0}",
                        H.bytesToHex(b)));
                return false;
        }
        return true;
    }
}
