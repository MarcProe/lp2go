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

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import net.proest.lp2go3.UAVTalk.UAVTalkMissingObjectException;
import net.proest.lp2go3.UAVTalk.UAVTalkObjectTree;
import net.proest.lp2go3.UAVTalk.UAVTalkXMLObject;
import net.proest.lp2go3.UI.PidTextView;

import java.util.Iterator;

class PollThread extends Thread {

    private boolean blink = true;
    private int request = 0;
    private MainActivity mA;
    private UAVTalkObjectTree mObjectTree;
    private boolean mIsValid = true;

    public PollThread(MainActivity activity) {
        this.setName("LP2GoPollThread");
        if (MainActivity.hasPThread()) throw new IllegalStateException("double mPollThread");
        MainActivity.hasPThread(true);
        this.mA = activity;
    }

    public void setObjectTree(UAVTalkObjectTree mObjectTree) {
        this.mObjectTree = mObjectTree;
    }

    private void setText(TextView t, String text) {
        if (text != null && t != null) {
            t.setText(text);
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
                        R.drawable.rounded_corner_uninitialised));
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
            blink = !blink;
            try {
                Thread.sleep(MainActivity.POLL_WAIT_TIME);
            } catch (InterruptedException ignored) {
            }

            mA.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mA.mSerialModeUsed == MainActivity.SERIAL_BLUETOOTH) {
                        mA.imgUSB.setColorFilter(Color.argb(0xff, 0x00, 0x00, 0x00));
                        if (mA.mFcDevice != null && mA.mFcDevice.isConnected()) {
                            mA.imgBluetooth.setColorFilter(Color.argb(0xff, 0x00, 0x80, 0x00));
                            mA.imgBluetooth.setImageDrawable(
                                    ContextCompat.getDrawable(mA.getApplicationContext(),
                                            R.drawable.ic_bluetooth_connected_128dp));

                        } else if (mA.mFcDevice != null && mA.mFcDevice.isConnecting()) {
                            if (blink) {
                                mA.imgBluetooth.setColorFilter(Color.argb(0xff, 0xff, 0x66, 0x00));
                                mA.imgBluetooth.setImageDrawable(
                                        ContextCompat.getDrawable(mA.getApplicationContext(),
                                                R.drawable.ic_bluetooth_128dp));
                            } else {
                                mA.imgBluetooth.setColorFilter(Color.argb(0xff, 0xff, 0x66, 0x00));
                                mA.imgBluetooth.setImageDrawable(
                                        ContextCompat.getDrawable(mA.getApplicationContext(),
                                                R.drawable.ic_bluetooth_connected_128dp));
                            }
                        } else {
                            mA.imgBluetooth.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
                            mA.imgBluetooth.setImageDrawable(
                                    ContextCompat.getDrawable(mA.getApplicationContext(),
                                            R.drawable.ic_bluetooth_disabled_128dp));
                        }
                    } else if (mA.mSerialModeUsed == MainActivity.SERIAL_USB) {
                        mA.imgBluetooth.setColorFilter(Color.argb(0xff, 0x00, 0x00, 0x00));
                        if (mA.mFcDevice != null && mA.mFcDevice.isConnected()) {
                            mA.imgUSB.setColorFilter(Color.argb(0xff, 0x00, 0x80, 0x00));

                        } else if (mA.mFcDevice != null && mA.mFcDevice.isConnecting()) {
                            if (blink) {
                                mA.imgUSB.setColorFilter(Color.argb(0xff, 0xff, 0x66, 0x00));
                            } else {
                                mA.imgUSB.setColorFilter(Color.argb(0xff, 0xff, 0x88, 0x00));
                            }
                        } else {
                            mA.imgUSB.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
                        }
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
                        switch (MainActivity.mCurrentView) {
                            case MainActivity.VIEW_MAIN:

                                mA.txtObjectLogTx.setText(
                                        H.k(String.valueOf(
                                                mA.mTxObjects * MainActivity.POLL_SECOND_FACTOR)));
                                mA.txtObjectLogRxGood.setText(
                                        H.k(String.valueOf(
                                                mA.mRxObjectsGood * MainActivity.POLL_SECOND_FACTOR)));
                                mA.txtObjectLogRxBad.setText(
                                        H.k(String.valueOf(
                                                mA.mRxObjectsBad * MainActivity.POLL_SECOND_FACTOR)));

                                if (blink) {
                                    if (mA.mRxObjectsGood > 0)
                                        mA.imgPacketsGood.setColorFilter(
                                                Color.argb(0xff, 0x00, 0x88, 0x00));
                                    if (mA.mRxObjectsBad > 0)
                                        mA.imgPacketsBad.setColorFilter(
                                                Color.argb(0xff, 0x88, 0x00, 0x00));
                                    if (mA.mTxObjects > 0)
                                        mA.imgPacketsUp.setColorFilter(
                                                Color.argb(0xff, 0x00, 0x00, 0x88));
                                } else {
                                    if (mA.mRxObjectsGood > 0)
                                        mA.imgPacketsGood.setColorFilter(
                                                Color.argb(0xff, 0x00, 0x00, 0x00));
                                    if (mA.mRxObjectsBad > 0)
                                        mA.imgPacketsBad.setColorFilter(
                                                Color.argb(0xff, 0x00, 0x00, 0x00));
                                    if (mA.mTxObjects > 0)
                                        mA.imgPacketsUp.setColorFilter(
                                                Color.argb(0xff, 0x00, 0x00, 0x00));
                                }

                                mA.setTxObjects(0);
                                mA.setRxObjectsBad(0);
                                mA.setRxObjectsGood(0);

                                setText(mA.txtVehicleName, getStringData("SystemSettings", "VehicleName", 20));

                                setTextBGColor(mA.txtAtti, getData("SystemAlarms", "Alarm", "Attitude").toString());
                                setTextBGColor(mA.txtStab, getData("SystemAlarms", "Alarm", "Stabilization").toString());
                                setTextBGColor(mA.txtPath, getData("PathStatus", "Status").toString());
                                setTextBGColor(mA.txtPlan, getData("SystemAlarms", "Alarm", "PathPlan").toString());

                                setText(mA.txtGPSSatsInView, getData("GPSSatellites", "SatsInView").toString());
                                setTextBGColor(mA.txtGPS, getData("SystemAlarms", "Alarm", "GPS").toString());
                                setTextBGColor(mA.txtSensor, getData("SystemAlarms", "Alarm", "Sensors").toString());
                                setTextBGColor(mA.txtAirspd, getData("SystemAlarms", "Alarm", "Airspeed").toString());
                                setTextBGColor(mA.txtMag, getData("SystemAlarms", "Alarm", "Magnetometer").toString());

                                setTextBGColor(mA.txtInput, getData("SystemAlarms", "Alarm", "Receiver").toString());
                                setTextBGColor(mA.txtOutput, getData("SystemAlarms", "Alarm", "Actuator").toString());
                                setTextBGColor(mA.txtI2C, getData("SystemAlarms", "Alarm", "I2C").toString());
                                setTextBGColor(mA.txtTelemetry, getData("SystemAlarms", "Alarm", "Telemetry").toString());
                                //setText(mA.txtFusionAlgorithm, getData("SystemAlarms", "Alarm", "Telemetry").toString());

                                //setTextBGColor(mA.txtFlightTelemetry, getData("FlightTelemetryStats", "Status").toString());
                                //setText(mA.txtFlightTelemetry, getData("FlightTelemetryStats", "Status").toString());
                                //setTextBGColor(mA.txtGCSTelemetry, getData("GCSTelemetryStats", "Status").toString());
                                //setText(mA.txtGCSTelemetry, getData("GCSTelemetryStats", "Status").toString());

                                setText(mA.txtHealthAlertDialogFusionAlgorithm, getData("RevoSettings", "FusionAlgorithm").toString());

                                setTextBGColor(mA.txtBatt, getData("SystemAlarms", "Alarm", "Battery").toString());
                                setTextBGColor(mA.txtTime, getData("SystemAlarms", "Alarm", "FlightTime").toString());
                                setTextBGColor(mA.txtConfig, getData("SystemAlarms", "ExtendedAlarmStatus", "SystemConfiguration").toString());

                                setTextBGColor(mA.txtBoot, getData("SystemAlarms", "Alarm", "BootFault").toString());
                                setTextBGColor(mA.txtMem, getData("SystemAlarms", "Alarm", "OutOfMemory").toString());
                                setTextBGColor(mA.txtStack, getData("SystemAlarms", "Alarm", "StackOverflow").toString());
                                setTextBGColor(mA.txtEvent, getData("SystemAlarms", "Alarm", "EventSystem").toString());
                                setTextBGColor(mA.txtCPU, getData("SystemAlarms", "Alarm", "CPUOverload").toString());

                                setText(mA.txtArmed, getData("FlightStatus", "Armed").toString());

                                setText(mA.txtFlightTime, H.getDateFromMilliSeconds(getData("SystemStats", "FlightTime").toString()));

                                setText(mA.txtVolt, getData("FlightBatteryState", "Voltage").toString());
                                setText(mA.txtAmpere, getData("FlightBatteryState", "Current").toString());
                                setText(mA.txtmAh, getData("FlightBatteryState", "ConsumedEnergy").toString());
                                setText(mA.txtTimeLeft, H.getDateFromSeconds(getData("FlightBatteryState", "EstimatedFlightTime").toString()));

                                setText(mA.txtHealthAlertDialogBatteryCapacity, getData("FlightBatterySettings", "Capacity").toString());
                                setText(mA.txtHealthAlertDialogBatteryCells, getData("FlightBatterySettings", "NbCells").toString());

                                setText(mA.txtAltitude, getFloatOffsetData("BaroSensor", "Altitude", MainActivity.OFFSET_BAROSENSOR_ALTITUDE));
                                setText(mA.txtAltitudeAccel, getFloatOffsetData("VelocityState", "Down", MainActivity.OFFSET_VELOCITY_DOWN));

                                String flightModeSwitchPosition = getData("ManualControlCommand", "FlightModeSwitchPosition", true).toString();

                                try {   //FlightMode in GCS is 1...n, so add "1" to be user friendly
                                    setText(mA.txtModeNum, String.valueOf(Integer.parseInt(flightModeSwitchPosition) + 1));
                                } catch (NumberFormatException e) {
                                    VisualLog.e("MainActivity", "Could not parse numeric Flightmode: " + flightModeSwitchPosition);
                                }

                                setText(mA.txtModeFlightMode, getData("FlightStatus", "FlightMode", true).toString());
                                setText(mA.txtModeAssistedControl, getData("FlightStatus", "FlightModeAssist", true).toString());

                                mA.mFcDevice.requestObject("FlightBatteryState");
                                mA.mFcDevice.requestObject("SystemStats");

                                break;
                            case MainActivity.VIEW_MAP:

                                if (mA.mSerialModeUsed == MainActivity.SERIAL_BLUETOOTH) {
                                    mA.mFcDevice.requestObject("GPSSatellites");
                                    mA.mFcDevice.requestObject("SystemAlarms");
                                    mA.mFcDevice.requestObject("GPSPositionSensor");
                                }

                                setText(mA.txtMapGPSSatsInView, getData("GPSSatellites", "SatsInView").toString());
                                setTextBGColor(mA.txtMapGPS, getData("SystemAlarms", "Alarm", "GPS").toString());
                                float deg = 0;
                                try {
                                    deg = (Float) getData("GPSPositionSensor", "Heading");
                                } catch (Exception ignored) {
                                }

                                Float lat = getGPSCoordinates("GPSPositionSensor", "Latitude");
                                Float lng = getGPSCoordinates("GPSPositionSensor", "Longitude");

                                setText(mA.txtLatitude, lat.toString());
                                setText(mA.txtLongitude, lng.toString());

                                LatLng src = mA.mMap.getCameraPosition().target;
                                LatLng dst = new LatLng(lat, lng);

                                double distance = H.calculationByDistance(src, dst);
                                if (distance > 0.001) {
                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
                                    MapsInitializer.initialize(mA);
                                    if (distance < 200) {
                                        mA.mMap.animateCamera(cameraUpdate);
                                    } else {
                                        mA.mMap.moveCamera(cameraUpdate);
                                    }

                                    mA.mPosHistory[mA.mCurrentPosMarker]
                                            = mA.mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(lat, lng))
                                            .title("Librepilot")
                                            .snippet("LP rules")
                                            .flat(true)
                                            .anchor(0.5f, 0.5f)
                                            .rotation(deg)
                                    );

                                    mA.mCurrentPosMarker++;
                                    if (mA.mCurrentPosMarker >= MainActivity.HISTORY_MARKER_NUM) {
                                        mA.mCurrentPosMarker = 0;
                                    }
                                    if (mA.mPosHistory[mA.mCurrentPosMarker] != null) {
                                        mA.mPosHistory[mA.mCurrentPosMarker].remove();
                                    }
                                }
                                break;
                            case MainActivity.VIEW_OBJECTS:
                                try {
                                    mA.txtObjects.setText(mA.mFcDevice.getObjectTree().toString());
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

                                String fmode = getData("ManualControlCommand", "FlightModeSwitchPosition").toString();
                                String bank = getData("StabilizationSettings", "FlightModeMap", fmode).toString();

                                mA.mCurrentStabilizationBank = "StabilizationSettings" + bank;

                                switch (mA.mCurrentStabilizationBank) {
                                    case "StabilizationSettingsBank1":
                                        mA.imgPidBank.setImageDrawable(ContextCompat.getDrawable(
                                                mA.getApplicationContext(), R.drawable.ic_filter_1_128dp));
                                        mA.mFcDevice.requestObject("StabilizationSettingsBank1");
                                        break;
                                    case "StabilizationSettingsBank2":
                                        mA.imgPidBank.setImageDrawable(ContextCompat.getDrawable(
                                                mA.getApplicationContext(), R.drawable.ic_filter_2_128dp));
                                        mA.mFcDevice.requestObject("StabilizationSettingsBank2");
                                        break;
                                    case "StabilizationSettingsBank3":
                                        mA.imgPidBank.setImageDrawable(ContextCompat.getDrawable(
                                                mA.getApplicationContext(), R.drawable.ic_filter_3_128dp));
                                        mA.mFcDevice.requestObject("StabilizationSettingsBank3");
                                        break;
                                    default:
                                        mA.imgPidBank.setImageDrawable(ContextCompat.getDrawable(
                                                mA.getApplicationContext(), R.drawable.ic_filter_none_128dp));
                                        break;
                                }

                                Iterator<PidTextView> i = mA.mPidTexts.iterator();

                                while (i.hasNext()) {
                                    PidTextView ptv = i.next();
                                    String data = ptv.getDecimalString(
                                            toFloat(getData(mA.mCurrentStabilizationBank,
                                                    ptv.getField(), ptv.getElement())));
                                    ptv.setText(data);
                                }

                                break;

                            case MainActivity.VIEW_VPID:

                                Iterator<PidTextView> vi = mA.mVerticalPidTexts.iterator();

                                while (vi.hasNext()) {
                                    PidTextView ptv = vi.next();
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
                                            data = null;
                                            break;

                                    }
                                }
                                mA.mFcDevice.requestObject("AltitudeHoldSettings");

                                break;

                            case MainActivity.VIEW_ABOUT:

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
            }
        } catch (NullPointerException e) {
            VisualLog.e("ERR", "UAVTalkdevice is null. Reconnecting?");
        }
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
            if (o != null) return o;
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