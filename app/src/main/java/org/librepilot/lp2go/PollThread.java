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

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import org.librepilot.lp2go.helper.SettingsHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkMissingObjectException;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectTree;

public class PollThread extends Thread {

    public UAVTalkObjectTree mObjectTree;
    private MainActivity mA;
    private boolean mBlink = true;
    private boolean mIsValid = true;
    private int mRotObjIcon = 0;
    private int request = 0;

    public PollThread(MainActivity activity) {
        this.setName("LP2GoPollThread");
        if (!MainActivity.hasPThread()) {
            MainActivity.setPThread(true);
            this.mA = activity;
        }
    }

    public void setObjectTree(UAVTalkObjectTree mObjectTree) {
        this.mObjectTree = mObjectTree;
    }

    private void setImageColor(ImageView i, String color) {
        if (color == null || color.equals("")) {
            return;
        }
        switch (color) {
            case "Connected":
                i.setColorFilter(Color.argb(0xff, 0x00, 0x80, 0x00));
                break;
            case "HandshakeReq":
                i.setColorFilter(Color.argb(0xff, 0xff, 0x80, 0x00));
                break;
            case "HandshakeAck":
                i.setColorFilter(Color.argb(0xff, 0xff, 0x00, 0x80));
                break;
            case "Disconnected":
                i.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
                break;
        }
    }

    public void setInvalid() {
        mIsValid = false;
    }

    public void run() {
        while (mIsValid) {
            mBlink = !mBlink;
            try {
                Thread.sleep(MainActivity.POLL_WAIT_TIME);
            } catch (InterruptedException ignored) {
            }

            if (mA == null) {
                return;
            }

            mA.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (SettingsHelper.mSerialModeUsed == MainActivity.SERIAL_BLUETOOTH) {
                        if (mA.mFcDevice != null && mA.mFcDevice.isConnected()) {
                            mA.imgSerial.setColorFilter(Color.argb(0xff, 0x00, 0x80, 0x00));
                            mA.imgSerial.setImageDrawable(
                                    ContextCompat.getDrawable(mA.getApplicationContext(),
                                            R.drawable.ic_bluetooth_connected_128dp));

                        } else if (mA.mFcDevice != null && mA.mFcDevice.isConnecting()) {
                            if (mBlink) {
                                mA.imgSerial.setColorFilter(Color.argb(0xff, 0xff, 0x66, 0x00));
                                mA.imgSerial.setImageDrawable(
                                        ContextCompat.getDrawable(mA.getApplicationContext(),
                                                R.drawable.ic_bluetooth_128dp));
                            } else {
                                mA.imgSerial.setColorFilter(Color.argb(0xff, 0xff, 0x66, 0x00));
                                mA.imgSerial.setImageDrawable(
                                        ContextCompat.getDrawable(mA.getApplicationContext(),
                                                R.drawable.ic_bluetooth_connected_128dp));
                            }
                        } else {
                            mA.imgSerial.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
                            mA.imgSerial.setImageDrawable(
                                    ContextCompat.getDrawable(mA.getApplicationContext(),
                                            R.drawable.ic_bluetooth_disabled_128dp));
                        }
                    } else if (SettingsHelper.mSerialModeUsed == MainActivity.SERIAL_USB) {
                        mA.imgSerial.setImageDrawable(
                                ContextCompat.getDrawable(mA.getApplicationContext(),
                                        R.drawable.ic_usb_128dp));
                        if (mA.mFcDevice != null && mA.mFcDevice.isConnected()) {
                            mA.imgSerial.setColorFilter(Color.argb(0xff, 0x00, 0x80, 0x00));


                        } else if (mA.mFcDevice != null && mA.mFcDevice.isConnecting()) {
                            if (mBlink) {
                                mA.imgSerial.setColorFilter(Color.argb(0xff, 0xff, 0x66, 0x00));
                            } else {
                                mA.imgSerial.setColorFilter(Color.argb(0xff, 0xff, 0x88, 0x00));
                            }
                        } else {
                            mA.imgSerial.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
                        }
                    } else {
                        mA.imgSerial.setImageDrawable(
                                ContextCompat.getDrawable(mA.getApplicationContext(),
                                        R.drawable.ic_warning_black_24dp));
                    }
                }
            });

            if (this.mObjectTree == null || mA.mFcDevice == null
                    || !mA.mFcDevice.isConnected()) {
                continue;  //nothing yet to show, or not connected
            }

            if (++request % 10 == 0) { //FIXME: is it needed to get the settings every 10 seconds?
                requestObjects();
                request = 0;
            }
            mA.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {

                        /**
                         * We have 100 bytes for the whole description.
                         *
                         * Structure is:
                         *   4 bytes: header: "OpFw".
                         *   4 bytes: GIT commit tag (short version of SHA1).
                         *   4 bytes: Unix timestamp of compile time.
                         *   2 bytes: target platform. Should follow same rule as BOARD_TYPE and BOARD_REVISION in board define files.
                         *  26 bytes: commit tag if it is there, otherwise branch name. '-dirty' may be added if needed. Zero-padded.
                         *  20 bytes: SHA1 sum of the firmware.
                         *  20 bytes: SHA1 sum of the uavo definitions.
                         *  20 bytes: free for now.
                         *
                         */
                        String fcUavoHash =
                                H.bytesToHex(getByteData("FirmwareIAPObj", "Description", 60, 20));
                        mA.setUavoLongHashFC(fcUavoHash.toLowerCase());
                        if (fcUavoHash.toLowerCase().equals(mA.getUavoLongHash().toLowerCase())) {
                            if (mA.getFcDevice() != null) {
                                if (mA.getFcDevice().isLogging()) {
                                    if (mBlink) {
                                        mA.imgUavoSanity.setColorFilter(Color.argb(0xff, 0, 0x00, 0x80));
                                        mA.imgUavoSanity.setRotation(90f);
                                    } else {
                                        mA.imgUavoSanity.setColorFilter(Color.argb(0xff, 0, 0x00, 0x80));
                                        mA.imgUavoSanity.setRotation(180f);
                                    }
                                } else {
                                    mA.imgUavoSanity.setColorFilter(Color.argb(0xff, 0, 0x80, 0));
                                    mA.imgUavoSanity.setRotation(0f);
                                }
                            }

                        } else {
                            if (mBlink) {
                                mA.imgUavoSanity.setColorFilter(Color.argb(0xff, 0xd4, 0, 0));
                                mA.imgUavoSanity.setRotation(mRotObjIcon++ * 90.f);
                            } else {
                                mA.imgUavoSanity.setColorFilter(Color.argb(0xff, 0xd4, 0x80, 0x80));
                                mA.imgUavoSanity.setRotation(mRotObjIcon++ * 90.f);
                                if (mRotObjIcon == 4) {
                                    mRotObjIcon = 0;
                                }
                            }
                        }

                        setImageColor(mA.imgFlightTelemetry,
                                getData("FlightTelemetryStats", "Status").toString());
                        setImageColor(mA.imgGroundTelemetry,
                                getData("GCSTelemetryStats", "Status").toString());

                        //UPDATING THE VIEW!
                        mA.mVcList.get(MainActivity.mCurrentView).update();

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        VisualLog.d("NPE", "Nullpointer Exception in Pollthread, "
                                + "most likely switched Connections");

                    }
                }
            });
        }
    }

    private void requestObjects() {
        try {
            if (mA.mFcDevice != null) {
                mA.mFcDevice.requestObject("SystemAlarms");
                mA.mFcDevice.requestObject("PathStatus");
                mA.mFcDevice.requestObject("GPSSatellites");
                mA.mFcDevice.requestObject("FlightTelemetryStats");
                mA.mFcDevice.requestObject("GCSTelemetryStats");
                mA.mFcDevice.requestObject("FlightStatus");
                mA.mFcDevice.requestObject("FlightBatteryState");
                mA.mFcDevice.requestObject("FlightBatterySettings");
                mA.mFcDevice.requestObject("BaroSensor");
                mA.mFcDevice.requestObject("HwSettings");
                mA.mFcDevice.requestObject("VelocityState");
                mA.mFcDevice.requestObject("ManualControlCommand");
                mA.mFcDevice.requestObject("StabilizationSettings");
                mA.mFcDevice.requestObject("FirmwareIAPObj");
            }
        } catch (NullPointerException e) {
            VisualLog.e("ERR", "UAVTalkdevice is null. Reconnecting?");
        }
    }

    private byte[] getByteData(String object, String field, int offset, int len) {
        byte[] b = new byte[len];
        try {
            for (int i = 0; i < len; i++) {
                int j = (int) mObjectTree.getData(object, 0, field, i + offset);
                b[i] = (byte) (j & 0xff);

            }
        } catch (UAVTalkMissingObjectException | NumberFormatException | ClassCastException e) {
            VisualLog.e("Pollthread", "Requesting " + object);

            try {
                mA.mFcDevice.requestObject(object);
            } catch (NullPointerException e2) {
                e2.printStackTrace();
            }
        }
        return b;
    }

    private Object getData(String objectname, String fieldname) {
        try {
            Object o = mObjectTree.getData(objectname, fieldname);
            if (o != null) {
                return o;
            }
        } catch (UAVTalkMissingObjectException e1) {
            try {
                mA.mFcDevice.requestObject(e1.getObjectname(), e1.getInstance());
            } catch (NullPointerException e2) {
                //e2.printStackTrace();
            }
        } catch (NullPointerException e3) {
            //e3.printStackTrace();
        }
        return "";
    }
}