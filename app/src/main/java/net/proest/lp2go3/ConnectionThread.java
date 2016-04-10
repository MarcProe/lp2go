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
package net.proest.lp2go3;

import android.widget.Toast;

import net.proest.lp2go3.UAVTalk.UAVTalkMissingObjectException;
import net.proest.lp2go3.UI.SingleToast;

public class ConnectionThread extends Thread {
    private final MainActivity mA;
    private boolean mIsValid = true;

    public ConnectionThread(MainActivity activity) {
        this.setName("LP2GoConnectionThread");
        this.mA = activity;
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
        if (mA.mLoadedUavo != null) {
            loaded = mA.loadXmlObjects(false);
        }
        if (loaded) {
            mA.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final long submil = (System.currentTimeMillis() - millis);
                    SingleToast.makeText(mA,
                            "UAVO load completed in " + submil + " milliseconds",
                            Toast.LENGTH_SHORT).show();

                }
            });
        }

        while (mIsValid) {

            if (mA.mFcDevice != null && mA.mFcDevice.isConnected()) {
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
                    e.printStackTrace();
                }

            }

            if (mA.mDoReconnect) {
                if (mA.mFcDevice != null) mA.mFcDevice.stop();
                mA.mFcDevice = null;
                mA.mDoReconnect = false;
                mA.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mA.resetMainView();
                    }
                });
            }

            if (mA.mFcDevice == null
                    || (!mA.mFcDevice.isConnected() && !mA.mFcDevice.isConnecting())) {
                switch (mA.mSerialModeUsed) {
                    case MainActivity.SERIAL_BLUETOOTH:
                        mA.connectBluetooth();
                        break;
                    case MainActivity.SERIAL_USB:
                        mA.connectUSB();
                        break;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }
}