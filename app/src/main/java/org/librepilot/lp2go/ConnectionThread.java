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

package org.librepilot.lp2go;

import android.widget.Toast;

import org.librepilot.lp2go.controller.ViewController;
import org.librepilot.lp2go.helper.SettingsHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkMissingObjectException;
import org.librepilot.lp2go.uavtalk.device.FcDevice;
import org.librepilot.lp2go.ui.SingleToast;

public class ConnectionThread extends Thread {
    private final MainActivity mA;
    private boolean mIsValid = true;
    private String mReplayLogFile = null;
    private FcDevice.GuiEventListener mGuiEventListener = null;

    public ConnectionThread(MainActivity activity) {
        this.setName("LP2GoConnectionThread");
        this.mA = activity;
    }

    public void setReplayLogFile(String mReplayLogFile) {
        this.mReplayLogFile = mReplayLogFile;
    }

    public void setInvalid() {
        mIsValid = false;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //wakeup little prince
        }

        final long millis = System.currentTimeMillis();

        boolean loaded = false;
        if (SettingsHelper.mLoadedUavo != null) {
            loaded = mA.loadXmlObjects(false);
            mA.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (ViewController vc : mA.mVcList.values()) {
                        vc.reset();
                    }
                }
            });
        }
        if (loaded) {
            mA.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final long submil = (System.currentTimeMillis() - millis);
                    SingleToast.show(mA,
                            "UAVO load completed in " + submil + " milliseconds",
                            Toast.LENGTH_SHORT);
                }
            });
        }

        while (mIsValid) {

            if (mA.mFcDevice != null
                    && mA.mFcDevice.isConnected()
                    && SettingsHelper.mSerialModeUsed != MainActivity.SERIAL_LOG_FILE) {
                try {
                    String status = (String) mA.mFcDevice.getObjectTree()
                            .getData("FlightTelemetryStats", "Status");
                    String[] options = mA.mFcDevice.getObjectTree().getXmlObjects()
                            .get("FlightTelemetryStats").getFields().get("Status").getOptions();
                    byte b = 0;
                    for (String o : options) {
                        if (o.equals(status)) {
                            break;
                        }
                        b++;
                    }
                    mA.mFcDevice.handleHandshake(b);
                } catch (UAVTalkMissingObjectException | NullPointerException e) {
                    VisualLog.d("ConnectionThread", "Error handshaking");
                }
            }

            if (mA.mDoReconnect) {
                if (mA.mFcDevice != null) {
                    mA.mFcDevice.stop();
                }
                mA.mFcDevice = null;
                mA.mDoReconnect = false;
                mA.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (ViewController vc : mA.mVcList.values()) {
                            vc.reset();
                        }
                    }
                });
            }

            if (mA.mFcDevice == null
                    || (!mA.mFcDevice.isConnected() && !mA.mFcDevice.isConnecting())) {
                switch (SettingsHelper.mSerialModeUsed) {
                    case MainActivity.SERIAL_BLUETOOTH:
                        mA.connectBluetooth();
                        break;
                    case MainActivity.SERIAL_USB:
                        mA.connectUSB();
                        break;
                    case MainActivity.SERIAL_LOG_FILE:
                        if (mReplayLogFile != null) {
                            mA.connectLogFile(mReplayLogFile, mGuiEventListener);
                        }
                        break;
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void setGuiEventListener(FcDevice.GuiEventListener gel) {
        mGuiEventListener = gel;
    }
}