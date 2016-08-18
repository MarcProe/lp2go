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

package org.librepilot.lp2go.uavtalk.device;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectTree;
import org.librepilot.lp2go.uavtalk.UAVTalkXMLObject;

import java.util.Map;

public class FcLogfileDevice extends FcDevice {


    private final FcWaiterThread mWaiterThread;

    public FcLogfileDevice(MainActivity mActivity, String filename, Map<String, UAVTalkXMLObject> xmlObjects) throws IllegalStateException {
        super(mActivity);

        mObjectTree = new UAVTalkObjectTree();
        mObjectTree.setXmlObjects(xmlObjects);
        mActivity.setPollThreadObjectTree(mObjectTree);

        mWaiterThread = new FcLogfileWaiterThread(this, filename);
    }

    public void setSkip(int skip) {
        if (mWaiterThread != null) {
            ((FcLogfileWaiterThread) mWaiterThread).mSkipForward = skip;
        }
    }

    @Override
    public boolean isConnected() {
        return mWaiterThread != null;
    }

    @Override
    public boolean isConnecting() {
        return false;
    }

    @Override
    public void start() {
        mWaiterThread.start();
    }

    @Override
    public void stop() {
        synchronized (mWaiterThread) {
            mWaiterThread.stopThread();
        }
    }

    @Override
    public boolean sendAck(String objectId, int instance) {
        return true;
    }

    @Override
    public boolean sendSettingsObject(String objectName, int instance) {
        return true;
    }

    @Override
    public boolean sendSettingsObject(String objectName, int instance, String fieldName, int element, byte[] newFieldData, boolean block) {
        return true;
    }

    @Override
    public boolean requestObject(String objectName) {
        return true;
    }

    @Override
    public boolean requestObject(String objectName, int instance) {
        return true;
    }

    @Override
    public void drawConnectionLogo(boolean blink) {
        int state = ((FcLogfileWaiterThread) mWaiterThread).mState;
        final MainActivity mA = mActivity;

        if (blink) {
            mA.imgSerial.setColorFilter(Color.argb(0xff, 0x00, 0x00, 0x00));
        }

        switch (state) {
            case FcDevice.GEL_PAUSED:
                setIcon(mA, R.drawable.ic_pause_black_128dp);
                break;
            case FcDevice.GEL_STOPPED:
                setIcon(mA, R.drawable.ic_stop_black_128dp);
                break;
            case FcDevice.GEL_RUNNING:
                setIcon(mA, R.drawable.ic_play_arrow_128dp);
                if (!blink) {
                    mA.imgSerial.setColorFilter(Color.argb(0xff, 0xff, 0xff, 0xff));
                }
                break;
            default:
                setIcon(mA, R.drawable.ic_rate_review_24dp);
                break;
        }
    }

    private void setIcon(MainActivity mA, int res) {
        mA.imgSerial.setImageDrawable(ContextCompat.getDrawable(mA.getApplicationContext(), res));
    }

    @Override
    protected boolean writeByteArray(byte[] bytes) {
        return true;
    }

    @Override
    public void setGuiEventListener(GuiEventListener gel) {
        mWaiterThread.setGuiEventListener(gel);
    }

    public boolean isPaused() {
        return ((FcLogfileWaiterThread) mWaiterThread).isPaused();
    }

    public void setPaused(boolean p) {
        ((FcLogfileWaiterThread) mWaiterThread).setPaused(p);
    }
}
