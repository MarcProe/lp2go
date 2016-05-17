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
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.librepilot.lp2go.helper.SettingsHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkMissingObjectException;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectTree;
import org.librepilot.lp2go.uavtalk.UAVTalkXMLObject;
import org.librepilot.lp2go.ui.PidTextView;
import org.librepilot.lp2go.ui.map.MapHelper;

class PollThread extends Thread {

    private MainActivity mA;
    private boolean mBlink = true;
    private Float mFcCurrentLat = null;
    private Float mFcCurrentLng = null;
    private boolean mIsValid = true;
    private MapHelper mMapHelper;
    private UAVTalkObjectTree mObjectTree;
    private int mRotObjIcon = 0;
    private int request = 0;

    public PollThread(MainActivity activity) {
        this.setName("LP2GoPollThread");
        if (MainActivity.hasPThread()) {
            throw new IllegalStateException("double mPollThread");
        }
        MainActivity.hasPThread(true);
        this.mA = activity;
        mMapHelper = new MapHelper(mA.mMap);
    }

    public void setObjectTree(UAVTalkObjectTree mObjectTree) {
        this.mObjectTree = mObjectTree;
    }

    private void setText(TextView t, String text) {
        if (text != null && t != null) {
            t.setText(text);
        }
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

    private void setTextBGColor(TextView t, String color) {
        if (color == null || color.equals(mA.getString(R.string.EMPTY_STRING))) {
            return;
        }
        switch (color) {
            case "OK":
            case "None":
            case "Connected":
                t.setBackground(ContextCompat.getDrawable(mA.getApplicationContext(),
                        R.drawable.rounded_corner_ok));
                break;
            case "Warning":
            case "HandshakeReq":
            case "HandshakeAck":
                t.setBackground(ContextCompat.getDrawable(mA.getApplicationContext(),
                        R.drawable.rounded_corner_warning));
                break;
            case "Error":
                t.setBackground(ContextCompat.getDrawable(mA.getApplicationContext(),
                        R.drawable.rounded_corner_error));
                break;
            case "Critical":
            case "RebootRequired":
            case "Disconnected":
                t.setBackground(ContextCompat.getDrawable(mA.getApplicationContext(),
                        R.drawable.rounded_corner_critical));
                break;
            case "Uninitialised":
                t.setBackground(ContextCompat.getDrawable(mA.getApplicationContext(),
                        R.drawable.rounded_corner_unini));
                break;
            case "InProgress":
                t.setBackground(ContextCompat.getDrawable(mA.getApplicationContext(),
                        R.drawable.rounded_corner_inprogress));
                break;
            case "Completed":
                t.setBackground(ContextCompat.getDrawable(mA.getApplicationContext(),
                        R.drawable.rounded_corner_completed));
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
                            mA.imgUavoSanity.setColorFilter(Color.argb(0xff, 0, 0x80, 0));
                            mA.imgUavoSanity.setRotation(0f);
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


                        switch (MainActivity.mCurrentView) {
                            case MainActivity.VIEW_MAIN:

                                mA.txtObjectLogTx.setText(
                                        H.k(String.valueOf(mA.mTxObjects *
                                                MainActivity.POLL_SECOND_FACTOR)));
                                mA.txtObjectLogRxGood.setText(
                                        H.k(String.valueOf(mA.mRxObjectsGood *
                                                MainActivity.POLL_SECOND_FACTOR)));
                                mA.txtObjectLogRxBad.setText(
                                        H.k(String.valueOf(mA.mRxObjectsBad *
                                                MainActivity.POLL_SECOND_FACTOR)));

                                if (mBlink) {
                                    if (mA.mRxObjectsGood > 0) {
                                        mA.imgPacketsGood.setColorFilter(
                                                Color.argb(0xff, 0x00, 0x88, 0x00),
                                                PorterDuff.Mode.SRC_ATOP);
                                    }
                                    if (mA.mRxObjectsBad > 0) {
                                        mA.imgPacketsBad.setColorFilter(
                                                Color.argb(0xff, 0x88, 0x00, 0x00),
                                                PorterDuff.Mode.SRC_ATOP);
                                    }
                                    if (mA.mTxObjects > 0) {
                                        mA.imgPacketsUp.setColorFilter(
                                                Color.argb(0xff, 0x00, 0x00, 0xdd),
                                                PorterDuff.Mode.SRC_ATOP);
                                    }
                                } else {
                                    if (mA.mRxObjectsGood > 0) {
                                        mA.imgPacketsGood.setColorFilter(
                                                Color.argb(0xff, 0x00, 0x00, 0x00),
                                                PorterDuff.Mode.SRC_ATOP);
                                    }
                                    if (mA.mRxObjectsBad > 0) {
                                        mA.imgPacketsBad.setColorFilter(
                                                Color.argb(0xff, 0x00, 0x00, 0x00),
                                                PorterDuff.Mode.SRC_ATOP);
                                    }
                                    if (mA.mTxObjects > 0) {
                                        mA.imgPacketsUp.setColorFilter(
                                                Color.argb(0xff, 0x00, 0x00, 0x00),
                                                PorterDuff.Mode.SRC_ATOP);
                                    }
                                }

                                mA.setTxObjects(0);
                                mA.setRxObjectsBad(0);
                                mA.setRxObjectsGood(0);


                                setTextBGColor(mA.txtAtti,
                                        getData("SystemAlarms", "Alarm", "Attitude").toString());
                                setTextBGColor(mA.txtStab,
                                        getData("SystemAlarms", "Alarm", "Stabilization")
                                                .toString());
                                setTextBGColor(mA.txtPath,
                                        getData("PathStatus", "Status").toString());
                                setTextBGColor(mA.txtPlan,
                                        getData("SystemAlarms", "Alarm", "PathPlan").toString());

                                setText(mA.txtGPSSatsInView,
                                        getData("GPSSatellites", "SatsInView").toString());
                                setTextBGColor(mA.txtGPS,
                                        getData("SystemAlarms", "Alarm", "GPS").toString());
                                setTextBGColor(mA.txtSensor,
                                        getData("SystemAlarms", "Alarm", "Sensors").toString());
                                setTextBGColor(mA.txtAirspd,
                                        getData("SystemAlarms", "Alarm", "Airspeed").toString());
                                setTextBGColor(mA.txtMag,
                                        getData("SystemAlarms", "Alarm", "Magnetometer")
                                                .toString());

                                setTextBGColor(mA.txtInput,
                                        getData("SystemAlarms", "Alarm", "Receiver").toString());
                                setTextBGColor(mA.txtOutput,
                                        getData("SystemAlarms", "Alarm", "Actuator").toString());
                                setTextBGColor(mA.txtI2C,
                                        getData("SystemAlarms", "Alarm", "I2C").toString());
                                setTextBGColor(mA.txtTelemetry,
                                        getData("SystemAlarms", "Alarm", "Telemetry").toString());

                                setText(mA.txtHealthAlertDialogFusionAlgorithm,
                                        getData("RevoSettings", "FusionAlgorithm").toString());

                                setTextBGColor(mA.txtBatt,
                                        getData("SystemAlarms", "Alarm", "Battery").toString());
                                setTextBGColor(mA.txtTime,
                                        getData("SystemAlarms", "Alarm", "FlightTime").toString());
                                setTextBGColor(mA.txtConfig,
                                        getData("SystemAlarms", "ExtendedAlarmStatus",
                                                "SystemConfiguration").toString());

                                setTextBGColor(mA.txtBoot,
                                        getData("SystemAlarms", "Alarm", "BootFault").toString());
                                setTextBGColor(mA.txtMem,
                                        getData("SystemAlarms", "Alarm", "OutOfMemory").toString());
                                setTextBGColor(mA.txtStack,
                                        getData("SystemAlarms", "Alarm", "StackOverflow")
                                                .toString());
                                setTextBGColor(mA.txtEvent,
                                        getData("SystemAlarms", "Alarm", "EventSystem").toString());
                                setTextBGColor(mA.txtCPU,
                                        getData("SystemAlarms", "Alarm", "CPUOverload").toString());

                                String statusArmed = getData("FlightStatus", "Armed").toString();
                                if (!mA.txtArmed.getText().toString().equals(statusArmed)) {
                                    mA.getTtsHelper().speakFlush(statusArmed);
                                    setText(mA.txtArmed, statusArmed);
                                }

                                setText(mA.txtFlightTime, H.getDateFromMilliSeconds(
                                        getData("SystemStats", "FlightTime").toString()));

                                setText(mA.txtVolt,
                                        getFloatData("FlightBatteryState", "Voltage", 4));
                                setText(mA.txtAmpere,
                                        getFloatData("FlightBatteryState", "Current", 4));
                                setText(mA.txtmAh,
                                        getFloatData("FlightBatteryState", "ConsumedEnergy", 3));
                                setText(mA.txtTimeLeft, H.getDateFromSeconds(
                                        getData("FlightBatteryState", "EstimatedFlightTime")
                                                .toString()));

                                setText(mA.txtHealthAlertDialogBatteryCapacity,
                                        getData("FlightBatterySettings", "Capacity").toString());
                                setText(mA.txtHealthAlertDialogBatteryCells,
                                        getData("FlightBatterySettings", "NbCells").toString());

                                setText(mA.txtAltitude, getFloatOffsetData("BaroSensor", "Altitude",
                                        mA.getString(R.string.OFFSET_BAROSENSOR_ALTITUDE,
                                                R.string.APP_ID)));

                                setText(mA.txtAltitudeAccel,
                                        getFloatData("VelocityState", "Down", 2));

                                String flightModeSwitchPosition =
                                        getData("ManualControlCommand", "FlightModeSwitchPosition",
                                                true).toString();

                                try {   //FlightMode in GCS is 1...n, so add "1" to be user friendly
                                    setText(mA.txtModeNum, String.valueOf(
                                            Integer.parseInt(flightModeSwitchPosition) + 1));
                                } catch (NumberFormatException e) {
                                    VisualLog.e("MainActivity",
                                            "Could not parse numeric Flightmode: " +
                                                    flightModeSwitchPosition);
                                }

                                setText(mA.txtModeFlightMode,
                                        getData("FlightStatus", "FlightMode", true).toString());
                                setText(mA.txtModeAssistedControl,
                                        getData("FlightStatus", "FlightModeAssist", true)
                                                .toString());

                                mA.mFcDevice.requestObject("FlightBatteryState");
                                mA.mFcDevice.requestObject("SystemStats");

                                break;
                            case MainActivity.VIEW_MAP:

                                if (SettingsHelper.mSerialModeUsed ==
                                        MainActivity.SERIAL_BLUETOOTH) {
                                    mA.mFcDevice.requestObject("GPSSatellites");
                                    mA.mFcDevice.requestObject("SystemAlarms");
                                    mA.mFcDevice.requestObject("GPSPositionSensor");
                                }

                                setText(mA.txtMapGPSSatsInView,
                                        getData("GPSSatellites", "SatsInView").toString());
                                setTextBGColor(mA.txtMapGPS,
                                        getData("SystemAlarms", "Alarm", "GPS").toString());
                                float deg = 0;
                                try {
                                    deg = (Float) getData("GPSPositionSensor", "Heading");
                                } catch (Exception ignored) {
                                }

                                Float lat = getGPSCoordinates("GPSPositionSensor", "Latitude");
                                Float lng = getGPSCoordinates("GPSPositionSensor", "Longitude");

                                if (mFcCurrentLat != null && mFcCurrentLng != null) {
                                    mMapHelper.updatePosition(
                                            new LatLng(mFcCurrentLat, mFcCurrentLng),
                                            new LatLng(lat, lng), deg);
                                }

                                mFcCurrentLat = lat;
                                mFcCurrentLng = lng;

                                setText(mA.txtLatitude, lat.toString());
                                setText(mA.txtLongitude, lng.toString());

                                break;
                            case MainActivity.VIEW_OBJECTS:
                                try {
                                    mA.txtObjects.setText(mA.mFcDevice.getObjectTree().toString());
                                    if (mA.mExpListView.getExpandedObjectName() != null) {
                                        UAVTalkXMLObject xmlobj =
                                                mA.mFcDevice.getObjectTree().getXmlObjects()
                                                        .get(mA.mExpListView
                                                                .getExpandedObjectName());
                                        mA.mExpListView.updateExpandedGroup(xmlobj);
                                        mA.mFcDevice.requestObject(xmlobj.getName());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case MainActivity.VIEW_LOGS:
                                if (mA.mFcDevice.isLogging()) {
                                    try {
                                        mA.txtLogFilename.setText(mA.mFcDevice.getLogFileName());
                                        double lUAV = Math.round(mA.mFcDevice.getLogBytesLoggedUAV()
                                                / 102.4) / 10.;
                                        double lOPL = Math.round(mA.mFcDevice.getLogBytesLoggedOPL()
                                                / 102.4) / 10.;
                                        mA.txtLogSize.setText(String.valueOf(lUAV)
                                                + mA.getString(R.string.TAB) + "("
                                                + String.valueOf(lOPL) + ") KB");
                                        mA.txtLogObjects.setText(
                                                String.valueOf(mA.mFcDevice.getLogObjectsLogged()));
                                        mA.txtLogDuration.setText(
                                                String.valueOf((System.currentTimeMillis()
                                                        - mA.mFcDevice.getLogStartTimeStamp())
                                                        / 1000) + " s");
                                    } catch (Exception ignored) {
                                    }
                                }
                                break;
                            case MainActivity.VIEW_PID:

                                String fmode =
                                        getData("ManualControlCommand", "FlightModeSwitchPosition")
                                                .toString();
                                String bank =
                                        getData("StabilizationSettings", "FlightModeMap", fmode)
                                                .toString();

                                mA.mCurrentStabilizationBank = "StabilizationSettings" + bank;

                                switch (mA.mCurrentStabilizationBank) {
                                    case "StabilizationSettingsBank1":
                                        mA.imgPidBank.setImageDrawable(ContextCompat.getDrawable(
                                                mA.getApplicationContext(),
                                                R.drawable.ic_filter_1_128dp));
                                        mA.mFcDevice.requestObject("StabilizationSettingsBank1");
                                        break;
                                    case "StabilizationSettingsBank2":
                                        mA.imgPidBank.setImageDrawable(ContextCompat.getDrawable(
                                                mA.getApplicationContext(),
                                                R.drawable.ic_filter_2_128dp));
                                        mA.mFcDevice.requestObject("StabilizationSettingsBank2");
                                        break;
                                    case "StabilizationSettingsBank3":
                                        mA.imgPidBank.setImageDrawable(ContextCompat.getDrawable(
                                                mA.getApplicationContext(),
                                                R.drawable.ic_filter_3_128dp));
                                        mA.mFcDevice.requestObject("StabilizationSettingsBank3");
                                        break;
                                    default:
                                        mA.imgPidBank.setImageDrawable(ContextCompat.getDrawable(
                                                mA.getApplicationContext(),
                                                R.drawable.ic_filter_none_128dp));
                                        break;
                                }

                                for (PidTextView ptv : mA.mPidTexts) {
                                    String data = ptv.getDecimalString(
                                            toFloat(getData(mA.mCurrentStabilizationBank,
                                                    ptv.getField(), ptv.getElement())));
                                    ptv.setText(data);
                                }

                                break;

                            case MainActivity.VIEW_VPID:

                                for (PidTextView ptv : mA.mVerticalPidTexts) {
                                    //VisualLog.d("VPID", ptv.getDialogTitle() + " "  + ptv.getField() + " " + ptv.getElement() + " " + ptv.getText());
                                    String data;
                                    switch (ptv.getFieldType()) {
                                        case (UAVTalkXMLObject.FIELDTYPE_FLOAT32):
                                            data = ptv.getDecimalString(
                                                    toFloat(getData("AltitudeHoldSettings",
                                                            ptv.getField(), ptv.getElement())));
                                            ptv.setText(data);
                                            break;
                                        case (UAVTalkXMLObject.FIELDTYPE_UINT8):
                                            data = getData("AltitudeHoldSettings",
                                                    ptv.getField(), ptv.getElement()).toString();
                                            ptv.setText(data);
                                            break;
                                        default:
                                            break;

                                    }
                                }
                                mA.mFcDevice.requestObject("AltitudeHoldSettings");

                                break;

                            case MainActivity.VIEW_ABOUT:

                                break;

                            case MainActivity.VIEW_SCOPE:

                                break;

                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        VisualLog.d("NPE", "Nullpointer Exception in Pollthread, "
                                + "most likely switched Connections");

                    }
                }
            });
        }
    }

    private Float toFloat(Object o) {
        try {
            return (Float) o;
        } catch (ClassCastException e) {
            return .0f;
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

    private String getStringData(String object, String field, int len) {
        char[] b = new char[len];
        try {
            for (int i = 0; i < len; i++) {
                String str = mObjectTree.getData(object, 0, field, i)
                        .toString();
                b[i] = (char) Byte.parseByte(str);

            }
        } catch (UAVTalkMissingObjectException | NumberFormatException e) {
            try {
                mA.mFcDevice.requestObject(object);
            } catch (NullPointerException e2) {
                e2.printStackTrace();
            }
        }
        return new String(b);
    }

    private Float getGPSCoordinates(String object, String field) {
        try {
            int i = (Integer) mObjectTree.getData(object, field);
            return ((float) i / 10000000);
        } catch (UAVTalkMissingObjectException | NullPointerException | ClassCastException e1) {
            VisualLog.d("GPS", "getCoord", e1);
            return 0.0f;
        }
    }

    private String getFloatData(String obj, String field, int b) {
        try {
            Float f1 = H.stringToFloat(getData(obj, field).toString());
            return String.valueOf(H.round(f1, b));
        } catch (NumberFormatException e) {
            return "";
        }
    }

    private String getFloatOffsetData(String obj, String field, String soffset) {
        try {
            Float f1 = H.stringToFloat(getData(obj, field).toString());
            Float f2 = (Float) mA.mOffset.get(soffset);
            return String.valueOf(H.round(f1 - f2, 2));
        } catch (NumberFormatException e) {
            return "";
        }
    }

    Object getData(String objectname, String fieldname, String elementName, boolean request) {
        try {
            if (request) {
                mA.mFcDevice.requestObject(objectname);
            }
            return getData(objectname, fieldname, elementName);
        } catch (NullPointerException e) {
            //e.printStackTrace();
        }
        return "";
    }

    private Object getData(String objectname, String fieldname, boolean request) {
        try {
            if (request) {
                mA.mFcDevice.requestObject(objectname);
            }
            return getData(objectname, fieldname);
        } catch (NullPointerException e) {
            //e.printStackTrace();
        }
        return "";
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

    private Object getData(String objectname, String fieldname, String elementname) {
        Object o = null;
        try {
            o = mObjectTree.getData(objectname, fieldname, elementname);
        } catch (UAVTalkMissingObjectException e1) {
            try {
                mA.mFcDevice.requestObject(e1.getObjectname(), e1.getInstance());
            } catch (NullPointerException e2) {
                e2.printStackTrace();
            }
        } catch (NullPointerException e3) {
            VisualLog.e("ERR", "Object Tree not loaded yet.");
        }
        if (o != null) {
            return o;
        } else {
            return "";
        }
    }
}