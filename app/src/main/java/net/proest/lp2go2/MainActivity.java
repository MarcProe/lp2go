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

/* This file incorporates work covered by the following copyright and
 * permission notice:
 */

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.proest.lp2go2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.proest.lp2go2.UAVTalk.UAVTalkDevice;
import net.proest.lp2go2.UAVTalk.UAVTalkMissingObjectException;
import net.proest.lp2go2.UAVTalk.UAVTalkObjectTree;
import net.proest.lp2go2.UAVTalk.UAVTalkUsbDevice;
import net.proest.lp2go2.UAVTalk.UAVTalkXMLObject;

import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends Activity {


    private static final String ACTION_USB_PERMISSION = "net.proest.lp2go.USB_PERMISSION";
    private static final String OFFSET_VELOCITY_DOWN = "VelocityState-Down";
    private static final String OFFSET_BAROSENSOR_ALTITUDE = "BaroSensor-Altitude";
    private final String EMPTY_STRING = "";
    public boolean isReady = false;
    protected TextView txtAtti;
    protected TextView txtPlan;
    protected TextView txtStab;
    protected TextView txtPath;
    protected TextView txtGPS;
    protected TextView txtGPSSatsInView;
    protected TextView txtSensor;
    protected TextView txtAirspd;
    protected TextView txtMag;
    protected TextView txtInput;
    protected TextView txtOutput;
    protected TextView txtI2C;
    protected TextView txtTelemetry;
    protected TextView txtFlightTelemetry;
    protected TextView txtGCSTelemetry;
    protected TextView txtBatt;
    protected TextView txtTime;
    protected TextView txtConfig;
    protected TextView txtBoot;
    protected TextView txtStack;
    protected TextView txtMem;
    protected TextView txtEvent;
    protected TextView txtCPU;
    protected TextView txtArmed;
    protected TextView txtVolt;
    protected TextView txtAmpere;
    protected TextView txtmAh;
    protected TextView txtTimeLeft;
    protected TextView txtCapacity;
    protected TextView txtCells;
    protected TextView txtAltitude;
    protected TextView txtAltitudeAccel;
    protected TextView txtModeNum;
    protected TextView txtModeFlightMode;
    protected TextView txtModeSettingsBank;
    protected TextView txtModeAssistedControl;
    protected TextView txtModeRoll;
    protected TextView txtModePitch;
    protected TextView txtModeYaw;
    protected TextView txtModeThrust;
    protected Button btnStart;
    private Hashtable<String, Object> offset;
    private PollThread pThread;
    private TextView txtDeviceText;
    private UsbManager mManager;
    private UsbDevice mDevice;
    private UsbDeviceConnection mDeviceConnection;
    private PendingIntent mPermissionIntent;
    private UsbInterface mInterface;
    private UAVTalkDevice mUAVTalkDevice;
    private Hashtable<String, UAVTalkXMLObject> xmlObjects;
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("USB", action);

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                Log.d("USB", device.getVendorId() + "-" + device.getProductId() + "-" + device.getDeviceClass()
                        + " " + device.getDeviceSubclass() + " " + device.getDeviceProtocol());

                if (device.getDeviceClass() == UsbConstants.USB_CLASS_MISC) {
                    mManager.requestPermission(device, mPermissionIntent);
                }

                txtDeviceText.setText(device.getDeviceName());


            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null /*&& device.equals(deviceName)*/) {
                    setUsbInterface(null, null);
                    if (mUAVTalkDevice != null) {
                        mUAVTalkDevice.stop();
                    }
                }
                txtDeviceText.setText(R.string.DEVICE_NAME_NONE);
                stopPollThread();
            } else if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                            UsbInterface intf = findAdbInterface(device);
                            if (intf != null) {
                                setUsbInterface(device, intf);
                            }
                        }
                        startPollThread();
                    } else {
                        Log.d("DBG", "permission denied for device " + mDevice);
                    }
                }
            }
        }
    };
    private AlertDialog.Builder batteryCapacityDialogBuilder;
    private AlertDialog.Builder batteryCellsDialogBuilder;

    static private UsbInterface findAdbInterface(UsbDevice device) {

        int count = device.getInterfaceCount();
        for (int i = 0; i < count; i++) {
            UsbInterface intf = device.getInterface(i);
            if (intf.getInterfaceClass() == 3
                    && intf.getInterfaceSubclass() == 0
                    && intf.getInterfaceProtocol() == 0) {
                return intf;
            }
        }
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        AssetManager assets = getAssets();

        offset = new Hashtable<String, Object>();
        offset.put(OFFSET_BAROSENSOR_ALTITUDE, .0f);
        offset.put(OFFSET_VELOCITY_DOWN, .0f);

        // setContentView(R.layout.adb);
        //mLog = (TextView) findViewById(R.id.txt_out);
        txtDeviceText = (TextView) findViewById(R.id.txtDeviceName);

        txtPlan = (TextView) findViewById(R.id.txtPlan);
        txtAtti = (TextView) findViewById(R.id.txtAtti);
        txtStab = (TextView) findViewById(R.id.txtStab);
        txtPath = (TextView) findViewById(R.id.txtPath);

        txtGPS = (TextView) findViewById(R.id.txtGPS);
        txtGPSSatsInView = (TextView) findViewById(R.id.txtGPSSatsInView);

        txtAirspd = (TextView) findViewById(R.id.txtAirspd);
        txtSensor = (TextView) findViewById(R.id.txtSensor);
        txtMag = (TextView) findViewById(R.id.txtMag);

        txtInput = (TextView) findViewById(R.id.txtInput);
        txtOutput = (TextView) findViewById(R.id.txtOutput);
        txtI2C = (TextView) findViewById(R.id.txtI2C);
        txtTelemetry = (TextView) findViewById(R.id.txtTelemetry);
        txtFlightTelemetry = (TextView) findViewById(R.id.txtFlightTelemetry);
        txtGCSTelemetry = (TextView) findViewById(R.id.txtGCSTelemetry);

        txtBatt = (TextView) findViewById(R.id.txtBatt);
        txtTime = (TextView) findViewById(R.id.txtTime);
        txtConfig = (TextView) findViewById(R.id.txtConfig);

        txtBoot = (TextView) findViewById(R.id.txtBoot);
        txtStack = (TextView) findViewById(R.id.txtStack);
        txtMem = (TextView) findViewById(R.id.txtMem);
        txtEvent = (TextView) findViewById(R.id.txtEvent);
        txtCPU = (TextView) findViewById(R.id.txtCPU);

        txtArmed = (TextView) findViewById(R.id.txtArmed);

        txtVolt = (TextView) findViewById(R.id.txtVolt);
        txtAmpere = (TextView) findViewById(R.id.txtAmpere);
        txtmAh = (TextView) findViewById(R.id.txtmAh);
        txtTimeLeft = (TextView) findViewById(R.id.txtTimeLeft);

        txtCapacity = (TextView) findViewById(R.id.txtCapacity);
        txtCells = (TextView) findViewById(R.id.txtCells);

        txtAltitude = (TextView) findViewById(R.id.txtAltitude);
        txtAltitudeAccel = (TextView) findViewById(R.id.txtAltitudeAccel);

        txtModeNum = (TextView) findViewById(R.id.txtModeNum);
        txtModeFlightMode = (TextView) findViewById(R.id.txtModeFlightMode);

        txtModeAssistedControl = (TextView) findViewById(R.id.txtModeAssistedControl);


        btnStart = (Button) findViewById(R.id.btnStart);

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        xmlObjects = new Hashtable<String, UAVTalkXMLObject>();

        try {
            String path = "uav-15.09";
            String files[] = assets.list(path);
            for (String file : files) {
                InputStream ius = assets.open(path + File.separator + file);
                UAVTalkXMLObject obj = new UAVTalkXMLObject(readFully(ius));
                //Integer id = Integer.valueOf(obj.getId());
                xmlObjects.put(obj.getName(), obj);
                //Log.d("DGB", readFully(ius));
                ius.close();
                ius = null;
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        Log.d("MainActivity.onCreate", "XML Loading Complete");

        // check for existing devices
        for (UsbDevice device : mManager.getDeviceList().values()) {
            UsbInterface intf = findAdbInterface(device);
            if (setUsbInterface(device, intf)) {
                break;
            }
        }

        // listen for new devices
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);

        registerReceiver(mUsbReceiver, filter);
        // pThread = new PollThread(this);
        // pThread.start();
        isReady = true;
        OnStartClick(null);
        Log.d("MainActivity.onCreate", "onCreate done");
    }

    private String readFully(InputStream inputStream)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toString();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mUsbReceiver);
        setUsbInterface(null, null);
        super.onDestroy();
    }

    public void OnBatteryCapacityClick(View v) {
        initBatteryCapacityDialog();
        batteryCapacityDialogBuilder.show();
    }

    public void OnAltitudeClick(View V) {
        try {
            offset.put(OFFSET_BAROSENSOR_ALTITUDE, mUAVTalkDevice.getoTree().getData("BaroSensor", "Altitude"));
            txtAltitude.setText(EMPTY_STRING);
        } catch (UAVTalkMissingObjectException e) {
            e.printStackTrace();
        }
    }

    public void OnAltitudeAccelClick(View V) {
        try {
            offset.put(OFFSET_VELOCITY_DOWN, mUAVTalkDevice.getoTree().getData("VelocityState", "Down"));
            txtAltitudeAccel.setText(EMPTY_STRING);
        } catch (UAVTalkMissingObjectException e) {
            e.printStackTrace();
        }
    }

    public void OnBatteryCellsClick(View v) {
        initBatteryCellsDialog();
        batteryCellsDialogBuilder.show();
    }

    public void OnStartClick(View v) {
        if (pThread == null) {
            startPollThread();
        } else {
            stopPollThread();
        }
    }

    public void stopPollThread() {
        if (pThread != null) {
            pThread.setInvalid();
            pThread = null;
            btnStart.setText(R.string.Start);
        }
    }

    public void startPollThread() {
        if (pThread == null) {
            pThread = new PollThread(this);
            if (mUAVTalkDevice == null) {
                return;
            }
            pThread.setoTree(mUAVTalkDevice.getoTree());
            pThread.start();
            btnStart.setText(R.string.Stop);
        }
    }

    private boolean setUsbInterface(UsbDevice device, UsbInterface intf) {
        if (mDeviceConnection != null) {
            if (mInterface != null) {
                mDeviceConnection.releaseInterface(mInterface);
                mInterface = null;
            }
            mDeviceConnection.close();
            mDevice = null;
            mDeviceConnection = null;
        }

        if (device != null && intf != null) {
            UsbDeviceConnection connection = mManager.openDevice(device);
            if (connection != null) {
                if (connection.claimInterface(intf, true)) {
                    mDevice = device;
                    mDeviceConnection = connection;
                    mInterface = intf;
                    mUAVTalkDevice = new UAVTalkUsbDevice(this, mDeviceConnection, intf, xmlObjects);
                    mUAVTalkDevice.getoTree().setXmlObjects(xmlObjects);

                    mUAVTalkDevice.start();
                    txtDeviceText.setText(device.getDeviceName());
                    return true;
                } else {
                    connection.close();
                }
            }
        }

        if (mDeviceConnection == null && mUAVTalkDevice != null) {
            mUAVTalkDevice.stop();
            mUAVTalkDevice = null;
        }
        return false;
    }

    private void initBatteryCapacityDialog() {
        batteryCapacityDialogBuilder = new AlertDialog.Builder(this);
        batteryCapacityDialogBuilder.setTitle(R.string.CAPACITY_DIALOG_TITLE);

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        batteryCapacityDialogBuilder.setView(input);
        input.setText(txtCapacity.getText());

        // Set up the buttons
        batteryCapacityDialogBuilder.setPositiveButton(R.string.OK_BUTTON, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                byte[] bcdata = H.toBytes(Integer.parseInt(input.getText().toString()));
                if (bcdata.length == 4) {
                    bcdata = H.reverse4bytes(bcdata);
                    mUAVTalkDevice.sendSettingsObject("FlightBatterySettings", 0, "Capacity", 0, bcdata);
                }
                dialog.dismiss();
                dialog.cancel();
            }
        });

        batteryCapacityDialogBuilder.setNegativeButton(R.string.CANCEL_BUTTON, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                dialog.dismiss();
            }
        });
    }

    private void initBatteryCellsDialog() {
        batteryCellsDialogBuilder = new AlertDialog.Builder(this);
        batteryCellsDialogBuilder.setTitle(R.string.CELLS_DIALOG_TITLE);

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        batteryCellsDialogBuilder.setView(input);
        input.setText(txtCells.getText());

        // Set up the buttons
        batteryCellsDialogBuilder.setPositiveButton(R.string.OK_BUTTON, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                byte[] bcdata = new byte[1];
                bcdata[0] = H.toBytes(Integer.parseInt(input.getText().toString()))[3]; //want the lsb
                if (bcdata.length == 1) {
                    mUAVTalkDevice.sendSettingsObject("FlightBatterySettings", 0, "NbCells", 0, bcdata);
                }
                dialog.dismiss();
                dialog.cancel();
            }
        });

        batteryCellsDialogBuilder.setNegativeButton(R.string.CANCEL_BUTTON, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                dialog.dismiss();
            }
        });
    }

    private class PollThread extends Thread {

        MainActivity mActivity;
        UAVTalkObjectTree oTree;
        private boolean isValid = false;

        public PollThread(MainActivity mActivity) {
            this.mActivity = mActivity;
            this.isValid = true;
        }

        public void setoTree(UAVTalkObjectTree oTree) {
            this.oTree = oTree;
        }

        public void setInvalid() {
            this.isValid = false;
        }

        private void setText(TextView t, String text) {
            if (text != null) {
                t.setText(text);
            }
        }

        private void setTextBGColor(TextView t, String color) {
            if (color == null) {
                return;
            }
            switch (color) {
                case "OK":
                case "None":
                    t.setBackground(mActivity.getResources().getDrawable(R.drawable.rounded_corner_ok));
                    break;
                case "Warning":
                    t.setBackground(mActivity.getResources().getDrawable(R.drawable.rounded_corner_warning));
                    break;
                case "Error":
                    t.setBackground(mActivity.getResources().getDrawable(R.drawable.rounded_corner_error));
                    break;
                case "Critical":
                case "RebootRequired":
                    t.setBackground(mActivity.getResources().getDrawable(R.drawable.rounded_corner_critical));
                    break;
                case "Uninitialised":
                    t.setBackground(mActivity.getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
                    break;
                case "InProgress":
                    t.setBackground(mActivity.getResources().getDrawable(R.drawable.rounded_corner_inprogress));
                    break;
                case "Completed":
                    t.setBackground(mActivity.getResources().getDrawable(R.drawable.rounded_corner_completed));
                    break;
            }
        }

        public void run() {

            while (isValid) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

                if (this.oTree == null) {
                    continue;
                }
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setTextBGColor(mActivity.txtAtti, getData("SystemAlarms", "Alarm", "Attitude").toString());
                        setTextBGColor(mActivity.txtStab, getData("SystemAlarms", "Alarm", "Stabilization").toString());
                        setTextBGColor(mActivity.txtPath, getData("PathStatus", "Status").toString());
                        setTextBGColor(mActivity.txtPlan, getData("SystemAlarms", "Alarm", "PathPlan").toString());

                        setText(mActivity.txtGPSSatsInView, getData("GPSSatellites", "SatsInView").toString());
                        setTextBGColor(mActivity.txtGPS, getData("SystemAlarms", "Alarm", "GPS").toString());
                        setTextBGColor(mActivity.txtSensor, getData("SystemAlarms", "Alarm", "Sensors").toString());
                        setTextBGColor(mActivity.txtAirspd, getData("SystemAlarms", "Alarm", "Airspeed").toString());
                        setTextBGColor(mActivity.txtMag, getData("SystemAlarms", "Alarm", "Magnetometer").toString());

                        setTextBGColor(mActivity.txtInput, getData("SystemAlarms", "Alarm", "Receiver").toString());
                        setTextBGColor(mActivity.txtOutput, getData("SystemAlarms", "Alarm", "Actuator").toString());
                        setTextBGColor(mActivity.txtI2C, getData("SystemAlarms", "Alarm", "I2C").toString());
                        setTextBGColor(mActivity.txtTelemetry, getData("SystemAlarms", "Alarm", "Telemetry").toString());

                        setText(mActivity.txtFlightTelemetry, getData("FlightTelemetryStats", "Status").toString());
                        setText(mActivity.txtGCSTelemetry, getData("GCSTelemetryStats", "Status").toString());

                        setTextBGColor(mActivity.txtBatt, getData("SystemAlarms", "Alarm", "Battery").toString());
                        setTextBGColor(mActivity.txtTime, getData("SystemAlarms", "Alarm", "FlightTime").toString());
                        setTextBGColor(mActivity.txtConfig, getData("SystemAlarms", "ExtendedAlarmStatus", "SystemConfiguration").toString());

                        setTextBGColor(mActivity.txtBoot, getData("SystemAlarms", "Alarm", "BootFault").toString());
                        setTextBGColor(mActivity.txtMem, getData("SystemAlarms", "Alarm", "OutOfMemory").toString());
                        setTextBGColor(mActivity.txtStack, getData("SystemAlarms", "Alarm", "StackOverflow").toString());
                        setTextBGColor(mActivity.txtEvent, getData("SystemAlarms", "Alarm", "EventSystem").toString());
                        setTextBGColor(mActivity.txtCPU, getData("SystemAlarms", "Alarm", "CPUOverload").toString());

                        setText(mActivity.txtArmed, getData("FlightStatus", "Armed").toString());

                        setText(mActivity.txtVolt, getData("FlightBatteryState", "Voltage").toString());
                        setText(mActivity.txtAmpere, getData("FlightBatteryState", "Current").toString());
                        setText(mActivity.txtmAh, getData("FlightBatteryState", "ConsumedEnergy").toString());
                        setText(mActivity.txtTimeLeft, H.getDateFromSeconds(getData("FlightBatteryState", "EstimatedFlightTime").toString()));

                        setText(mActivity.txtCapacity, getData("FlightBatterySettings", "Capacity").toString());
                        setText(mActivity.txtCells, getData("FlightBatterySettings", "NbCells").toString());

                        setText(mActivity.txtAltitude, getFloatOffsetData("BaroSensor", "Altitude", OFFSET_BAROSENSOR_ALTITUDE));
                        setText(mActivity.txtAltitudeAccel, getFloatOffsetData("VelocityState", "Down", OFFSET_VELOCITY_DOWN));


                        String flightModeSwitchPosition = getData("ManualControlCommand", "FlightModeSwitchPosition", true).toString();
                        //Log.d("FMSP", flightModeSwitchPosition);

                        setText(mActivity.txtModeNum, flightModeSwitchPosition);
                        setText(mActivity.txtModeFlightMode, getData("FlightStatus", "FlightMode", true).toString());
                        setText(mActivity.txtModeAssistedControl, getData("FlightStatus", "FlightModeAssist", true).toString());


                        //setText(mActivity.txtAltitudeAccel, getData("AccelState", "z").toString());

                    }
                });
            }
        }

        private String getFloatOffsetData(String obj, String field, String soffset) {
            try {
                Float f1 = Float.parseFloat(getData(obj, field).toString());
                Float f2 = (Float) offset.get(soffset);
                return String.valueOf(H.round(f1 - f2, 2));
            } catch (NumberFormatException e) {
                return "";
            }

        }

        private Object getData(String objectname, String fieldname, boolean request) {
            try {
                if (request) {
                    mUAVTalkDevice.requestObject(objectname);
                }
                return getData(objectname, fieldname);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return EMPTY_STRING;
        }

        private Object getData(String objectname, String fieldname) {
            try {
                Object o = oTree.getData(objectname, fieldname);
                if (o != null) return o;
            } catch (UAVTalkMissingObjectException e1) {
                try {
                    mUAVTalkDevice.requestObject(e1.getObjectname(), e1.getInstance());
                } catch (NullPointerException e2) {
                    e2.printStackTrace();
                }
            } catch (NullPointerException e3) {
                e3.printStackTrace();
            }
            return EMPTY_STRING;
        }

        private Object getData(String objectname, String fieldname, String elementname) {
            try {
                return oTree.getData(objectname, fieldname, elementname);
            } catch (UAVTalkMissingObjectException e1) {
                try {
                    mUAVTalkDevice.requestObject(e1.getObjectname(), e1.getInstance());
                } catch (NullPointerException e2) {
                    e2.printStackTrace();
                }
            } catch (NullPointerException e3) {
                e3.printStackTrace();
            }
            return EMPTY_STRING;
        }
    }
}
