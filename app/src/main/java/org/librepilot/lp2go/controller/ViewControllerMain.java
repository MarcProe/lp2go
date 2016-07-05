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

package org.librepilot.lp2go.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import org.librepilot.lp2go.H;
import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.helper.CompatHelper;
import org.librepilot.lp2go.helper.SettingsHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkMissingObjectException;
import org.librepilot.lp2go.uavtalk.UAVTalkObject;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectListener;
import org.librepilot.lp2go.uavtalk.UAVTalkXMLObject;
import org.librepilot.lp2go.ui.SingleToast;
import org.librepilot.lp2go.ui.alertdialog.EnumInputAlertDialog;
import org.librepilot.lp2go.ui.alertdialog.NumberInputAlertDialog;
import org.librepilot.lp2go.ui.pfd.PfdRollPitchView;

public class ViewControllerMain extends ViewController implements View.OnClickListener,
        ViewControllerMainAnimatorViewSetter, CompoundButton.OnCheckedChangeListener,
        UAVTalkObjectListener {
    private final View mFlightSettingsView;
    private final View mLocalSettingsView;
    private Drawable imgPacketsBad;
    private Drawable imgPacketsGood;
    private Drawable imgPacketsUp;
    private PfdRollPitchView imgPfdRollPitch;
    private ViewAnimator mBottomAnimator;
    private int mBottomLayout;
    private Float mPreviousPitch = .0f;
    private Float mPreviousRoll = .0f;
    private ViewAnimator mTopAnimator;
    private int mTopLayout;
    private Spinner spiBottomRight;
    private Spinner spiTopLeft;
    private Switch swiEnableText2Speech;
    private TextView txtAirspd;
    private TextView txtAltitude;
    private TextView txtAltitudeAccel;
    private TextView txtAmpere;
    private TextView txtArmed;
    private TextView txtAtti;
    private TextView txtBatt;
    private TextView txtBoot;
    private TextView txtCPU;
    private TextView txtConfig;
    private TextView txtEvent;
    private TextView txtFlightTime;
    private TextView txtGPS;
    private TextView txtGPSSatsInView;
    private TextView txtHealthAlertDialogBatteryCapacity;
    private TextView txtHealthAlertDialogBatteryCells;
    private TextView txtHealthAlertDialogFusionAlgorithm;
    private TextView txtI2C;
    private TextView txtInput;
    private TextView txtMag;
    private TextView txtMem;
    private TextView txtModeAssistedControl;
    private TextView txtModeFlightMode;
    private TextView txtModeNum;
    private TextView txtObjectLogRxBad;
    private TextView txtObjectLogRxGood;
    private TextView txtObjectLogTx;
    private TextView txtOutput;
    private TextView txtPath;
    private TextView txtPlan;
    private TextView txtSensor;
    private TextView txtStab;
    private TextView txtStack;
    private TextView txtTelemetry;
    private TextView txtTime;
    private TextView txtTimeLeft;
    private TextView txtVolt;
    private TextView txtmAh;

    public ViewControllerMain(MainActivity activity, int title, int icon, int localSettingsVisible,
                              int flightSettingsVisible) {
        super(activity, title, icon, localSettingsVisible, flightSettingsVisible);

        final MainActivity ma = getMainActivity();

        mTopLayout = R.layout.activity_main_inc_empty;
        mBottomLayout = R.layout.activity_main_inc_empty;

        init();

        mFlightSettingsView = View.inflate(ma, R.layout.alert_main_settings_flight, null);
        mLocalSettingsView = View.inflate(ma, R.layout.alert_main_settings_local, null);

        txtHealthAlertDialogBatteryCapacity =
                (TextView) mFlightSettingsView
                        .findViewById(R.id.txtHealthAlertDialogBatteryCapacity);
        txtHealthAlertDialogBatteryCapacity.setOnClickListener(this);

        txtHealthAlertDialogBatteryCells =
                (TextView) mFlightSettingsView.findViewById(R.id.txtHealthAlertDialogBatteryCells);
        txtHealthAlertDialogBatteryCells.setOnClickListener(this);

        txtHealthAlertDialogFusionAlgorithm =
                (TextView) mFlightSettingsView
                        .findViewById(R.id.txtHealthAlertDialogFusionAlgorithm);
        txtHealthAlertDialogFusionAlgorithm.setOnClickListener(this);

        spiTopLeft = SettingsHelper.initSpinner(R.id.spiTopLeftView, mLocalSettingsView,
                null, R.array.main_elements, SettingsHelper.mTopLeftLayout);
        spiBottomRight = SettingsHelper.initSpinner(R.id.spiBottomRightView, mLocalSettingsView,
                null, R.array.main_elements, SettingsHelper.mBottomRightLayout);
        swiEnableText2Speech = (Switch) mLocalSettingsView.findViewById(R.id.swiEnableText2Speech);
        swiEnableText2Speech.setOnCheckedChangeListener(this);
    }

    private boolean isView(int layoutId) {
        return mTopAnimator.getCurrentView().getId() == layoutId
                || (mBottomAnimator.getCurrentView() != null
                && mBottomAnimator.getCurrentView().getId() == layoutId);
    }

    @Override
    public int getID() {
        return ViewController.VIEW_MAIN;
    }

    @Override
    public void setBottom(int bottomLayoutId) {
        setBottomNoInit(bottomLayoutId);
        init();
    }

    @Override
    public void setTop(int topLayoutId) {
        setTopNoInit(topLayoutId);
        init();
    }

    @Override
    public void setLayout() {
        MainActivity ma = getMainActivity();

        if (SettingsHelper.mTopLeftLayout.equals(ma.getString(R.string.main_element_health))) {
            setTopNoInit(R.layout.activity_main_inc_health);
        } else if (SettingsHelper.mTopLeftLayout.equals(ma.getString(R.string.main_element_info))) {
            setTopNoInit(R.layout.activity_main_inc_info);
        } else if (SettingsHelper.mTopLeftLayout.equals(ma.getString(R.string.main_element_map))) {
            setTopNoInit(R.layout.activity_main_inc_map);
        } else if (SettingsHelper.mTopLeftLayout.equals(ma.getString(R.string.main_element_pfd))) {
            setTopNoInit(R.layout.activity_main_inc_pfd);
        }

        if (SettingsHelper.mBottomRightLayout.equals(ma.getString(R.string.main_element_health))) {
            setBottomNoInit(R.layout.activity_main_inc_health);
        } else if (SettingsHelper.mBottomRightLayout
                .equals(ma.getString(R.string.main_element_info))) {
            setBottomNoInit(R.layout.activity_main_inc_info);
        } else if (SettingsHelper.mBottomRightLayout
                .equals(ma.getString(R.string.main_element_map))) {
            setBottomNoInit(R.layout.activity_main_inc_map);
        } else if (SettingsHelper.mBottomRightLayout
                .equals(ma.getString(R.string.main_element_pfd))) {
            setBottomNoInit(R.layout.activity_main_inc_pfd);
        }

        init();
        enter(ViewController.VIEW_MAIN);
        getMainActivity().initSlider();
    }

    private void setBottomNoInit(int bottomLayoutId) {
        mBottomLayout = bottomLayoutId;

        final View old = mBottomAnimator.getCurrentView();
        final View bottom;

        if (mTopLayout != mBottomLayout) { //only show one layout if both are equal {
            bottom = LayoutInflater.from(getMainActivity()).inflate(mBottomLayout, null);
            mBottomAnimator.setVisibility(View.VISIBLE);
        } else {
            bottom = LayoutInflater.from(getMainActivity())
                    .inflate(R.layout.activity_main_inc_empty, null);
            mBottomAnimator.setVisibility(View.GONE);
        }
        mBottomAnimator.addView(bottom);
        mBottomAnimator.showNext();
        mBottomAnimator.removeView(old);
    }

    private void setTopNoInit(int topLayoutId) {
        mTopLayout = topLayoutId;

        final View top = LayoutInflater.from(getMainActivity()).inflate(mTopLayout, null);
        final View old = mTopAnimator.getCurrentView();

        mTopAnimator.addView(top);
        mTopAnimator.showNext();
        mTopAnimator.removeView(old);
    }

    @Override
    public void enter(int view) {
        init();
        super.enter(view);

        if (isView(R.id.root_main_inc_map)) {
            getMainActivity().mVcList.get(ViewController.VIEW_MAP).enter(view, true);
        }
        getMainActivity().initSlider();
    }

    @Override
    public void leave() {
        super.leave();
        if (isView(R.id.root_main_inc_map)) {
            getMainActivity().mVcList.get(ViewController.VIEW_MAP).leave();

        }

        if (isView(R.id.root_main_inc_pfd)) {
            try {
                getMainActivity().mFcDevice.getObjectTree().removeListener("AttitudeState");
            } catch (NullPointerException | IllegalStateException ignored) {

            }
        }
    }

    @Override
    public void init() {
        super.init();

        MainActivity ma = getMainActivity();

        ma.mViews.put(VIEW_MAIN, ma.getLayoutInflater().inflate(R.layout.activity_main, null));
        ma.setContentView(ma.mViews.get(VIEW_MAIN));  //Main

        mTopAnimator = (ViewAnimator) findViewById(R.id.main_top);
        mBottomAnimator = (ViewAnimator) findViewById(R.id.main_bottom);

        setTopNoInit(mTopLayout);
        setBottomNoInit(mBottomLayout);

        if (isView(R.id.root_main_inc_health)) {
            mOffset.put(getString(R.string.OFFSET_BAROSENSOR_ALTITUDE, R.string.APP_ID), .0f);

            txtObjectLogTx = (TextView) findViewById(R.id.txtPacketsUp);
            txtObjectLogRxGood = (TextView) findViewById(R.id.txtPacketsGood);
            txtObjectLogRxBad = (TextView) findViewById(R.id.txtPacketsBad);

            imgPacketsUp = txtObjectLogTx.getCompoundDrawables()[0];
            imgPacketsGood = txtObjectLogRxGood.getCompoundDrawables()[0];
            imgPacketsBad = txtObjectLogRxBad.getCompoundDrawables()[0];

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

            txtBatt = (TextView) findViewById(R.id.txtBatt);
            txtTime = (TextView) findViewById(R.id.txtTime);
            txtConfig = (TextView) findViewById(R.id.txtConfig);

            txtBoot = (TextView) findViewById(R.id.txtBoot);
            txtStack = (TextView) findViewById(R.id.txtStack);
            txtMem = (TextView) findViewById(R.id.txtMem);
            txtEvent = (TextView) findViewById(R.id.txtEvent);
            txtCPU = (TextView) findViewById(R.id.txtCPU);

        }
        if (isView(R.id.root_main_inc_info)) {
            txtArmed = (TextView) findViewById(R.id.txtArmed);
            txtFlightTime = (TextView) findViewById(R.id.txtFlightTime);

            txtVolt = (TextView) findViewById(R.id.txtVolt);
            txtAmpere = (TextView) findViewById(R.id.txtAmpere);
            txtmAh = (TextView) findViewById(R.id.txtmAh);
            txtTimeLeft = (TextView) findViewById(R.id.txtTimeLeft);

            txtAltitude = (TextView) findViewById(R.id.txtAltitude);
            txtAltitude.setOnClickListener(this);

            txtAltitudeAccel = (TextView) findViewById(R.id.txtAltitudeAccel);

            txtModeNum = (TextView) findViewById(R.id.txtModeNum);
            txtModeFlightMode = (TextView) findViewById(R.id.txtModeFlightMode);

            txtModeAssistedControl = (TextView) findViewById(R.id.txtModeAssistedControl);
        }
        if (isView(R.id.root_main_inc_map)) {
            getMainActivity().mVcList.get(ViewController.VIEW_MAP).init();

        }

        if (isView(R.id.root_main_inc_pfd)) {

            imgPfdRollPitch = (PfdRollPitchView) findViewById(R.id.pfd_roll_pitch);
            //imgPfdRoll = (ImageView) findViewById(R.id.pfd_roll);
        }
    }

    @Override
    public void update() {
        super.update();

        MainActivity ma = getMainActivity();

        if (isView(R.id.root_main_inc_health)) {
            txtObjectLogTx.setText(
                    H.k(String.valueOf(ma.getTxObjects() *
                            MainActivity.POLL_SECOND_FACTOR)));
            txtObjectLogRxGood.setText(
                    H.k(String.valueOf(ma.getRxObjectsGood() *
                            MainActivity.POLL_SECOND_FACTOR)));
            txtObjectLogRxBad.setText(
                    H.k(String.valueOf(ma.getRxObjectsBad() *
                            MainActivity.POLL_SECOND_FACTOR)));

            if (mBlink) {
                if (ma.getRxObjectsGood() > 0) {
                    imgPacketsGood.setColorFilter(
                            Color.argb(0xff, 0x00, 0x88, 0x00),
                            PorterDuff.Mode.SRC_ATOP);
                }
                if (ma.getRxObjectsBad() > 0) {
                    imgPacketsBad.setColorFilter(
                            Color.argb(0xff, 0x88, 0x00, 0x00),
                            PorterDuff.Mode.SRC_ATOP);
                }
                if (ma.getTxObjects() > 0) {
                    imgPacketsUp.setColorFilter(
                            Color.argb(0xff, 0x00, 0x00, 0xdd),
                            PorterDuff.Mode.SRC_ATOP);
                }
            } else {
                if (ma.getRxObjectsGood() > 0) {
                    imgPacketsGood.setColorFilter(
                            Color.argb(0xff, 0x00, 0x00, 0x00),
                            PorterDuff.Mode.SRC_ATOP);
                }
                if (ma.getRxObjectsBad() > 0) {
                    imgPacketsBad.setColorFilter(
                            Color.argb(0xff, 0x00, 0x00, 0x00),
                            PorterDuff.Mode.SRC_ATOP);
                }
                if (ma.getTxObjects() > 0) {
                    imgPacketsUp.setColorFilter(
                            Color.argb(0xff, 0x00, 0x00, 0x00),
                            PorterDuff.Mode.SRC_ATOP);
                }
            }

            ma.setTxObjects(0);
            ma.setRxObjectsBad(0);
            ma.setRxObjectsGood(0);

            setTextBGColor(txtAtti,
                    getData("SystemAlarms", "Alarm", "Attitude").toString());
            setTextBGColor(txtStab,
                    getData("SystemAlarms", "Alarm", "Stabilization")
                            .toString());
            setTextBGColor(txtPath,
                    getData("PathStatus", "Status").toString());
            setTextBGColor(txtPlan,
                    getData("SystemAlarms", "Alarm", "PathPlan").toString());

            setText(txtGPSSatsInView,
                    getData("GPSSatellites", "SatsInView").toString());
            setTextBGColor(txtGPS,
                    getData("SystemAlarms", "Alarm", "GPS").toString());
            setTextBGColor(txtSensor,
                    getData("SystemAlarms", "Alarm", "Sensors").toString());
            setTextBGColor(txtAirspd,
                    getData("SystemAlarms", "Alarm", "Airspeed").toString());
            setTextBGColor(txtMag,
                    getData("SystemAlarms", "Alarm", "Magnetometer")
                            .toString());

            setTextBGColor(txtInput,
                    getData("SystemAlarms", "Alarm", "Receiver").toString());
            setTextBGColor(txtOutput,
                    getData("SystemAlarms", "Alarm", "Actuator").toString());
            setTextBGColor(txtI2C,
                    getData("SystemAlarms", "Alarm", "I2C").toString());
            setTextBGColor(txtTelemetry,
                    getData("SystemAlarms", "Alarm", "Telemetry").toString());

            setText(txtHealthAlertDialogFusionAlgorithm,
                    getData("RevoSettings", "FusionAlgorithm").toString());

            setTextBGColor(txtBatt,
                    getData("SystemAlarms", "Alarm", "Battery").toString());
            setTextBGColor(txtTime,
                    getData("SystemAlarms", "Alarm", "FlightTime").toString());
            setTextBGColor(txtConfig,
                    getData("SystemAlarms", "ExtendedAlarmStatus",
                            "SystemConfiguration").toString());

            setTextBGColor(txtBoot,
                    getData("SystemAlarms", "Alarm", "BootFault").toString());
            setTextBGColor(txtMem,
                    getData("SystemAlarms", "Alarm", "OutOfMemory").toString());
            setTextBGColor(txtStack,
                    getData("SystemAlarms", "Alarm", "StackOverflow")
                            .toString());
            setTextBGColor(txtEvent,
                    getData("SystemAlarms", "Alarm", "EventSystem").toString());
            setTextBGColor(txtCPU,
                    getData("SystemAlarms", "Alarm", "CPUOverload").toString());

        }


        if (isView(R.id.root_main_inc_info)) {

            String statusArmed = getData("FlightStatus", "Armed").toString();
            if (!txtArmed.getText().toString().equals(statusArmed)) {
                getMainActivity().getTtsHelper().speakFlush(statusArmed);
                setText(txtArmed, statusArmed);
            }

            setText(txtFlightTime, H.getDateFromMilliSeconds(
                    getData("SystemStats", "FlightTime").toString()));

            setText(txtVolt,
                    getFloatData("FlightBatteryState", "Voltage", 4));
            setText(txtAmpere,
                    getFloatData("FlightBatteryState", "Current", 4));
            setText(txtmAh,
                    getFloatData("FlightBatteryState", "ConsumedEnergy", 3));
            setText(txtTimeLeft, H.getDateFromSeconds(
                    getData("FlightBatteryState", "EstimatedFlightTime")
                            .toString()));

            setText(txtHealthAlertDialogBatteryCapacity,
                    getData("FlightBatterySettings", "Capacity").toString());
            setText(txtHealthAlertDialogBatteryCells,
                    getData("FlightBatterySettings", "NbCells").toString());

            setText(txtAltitude, getFloatOffsetData("BaroSensor", "Altitude",
                    getString(R.string.OFFSET_BAROSENSOR_ALTITUDE,
                            R.string.APP_ID)));

            setText(txtAltitudeAccel,
                    getFloatData("VelocityState", "Down", 2));

            String flightModeSwitchPosition =
                    getData("ManualControlCommand", "FlightModeSwitchPosition",
                            true).toString();

            try {   //FlightMode in GCS is 1...n, so add "1" to be user friendly
                setText(txtModeNum, String.valueOf(
                        Integer.parseInt(flightModeSwitchPosition) + 1));
            } catch (NumberFormatException e) {
                VisualLog.e("MainActivity",
                        "Could not parse numeric Flightmode: " +
                                flightModeSwitchPosition);
            }

            setText(txtModeFlightMode,
                    getData("FlightStatus", "FlightMode", true).toString());
            setText(txtModeAssistedControl,
                    getData("FlightStatus", "FlightModeAssist", true)
                            .toString());
        }

        if (isView(R.id.root_main_inc_map)) {

            getMainActivity().mVcList.get(ViewController.VIEW_MAP).update();

        }

        if (isView(R.id.root_main_inc_pfd)) {
            if (getMainActivity().mFcDevice.getObjectTree().getListener("AttitudeState") == null) {
                try {
                    try {
                        getMainActivity().mFcDevice.getObjectTree()
                                .setListener("AttitudeState", this);
                    } catch (IllegalStateException e1) {
                        getMainActivity().mFcDevice.getObjectTree().removeListener("AttitudeState");
                        getMainActivity().mFcDevice.getObjectTree()
                                .setListener("AttitudeState", this);
                    }
                } catch (NullPointerException ignored) {

                }
            }
        }

        getMainActivity().mFcDevice.requestObject("FlightBatteryState");
        getMainActivity().mFcDevice.requestObject("SystemStats");
    }

    @Override
    public void reset() {
        Context c = getMainActivity().getApplicationContext();

        if (isView(R.id.root_main_inc_health)) {

            CompatHelper.setBackground(txtAtti, c, R.drawable.rounded_corner_unini);

            CompatHelper.setBackground(txtStab, c, R.drawable.rounded_corner_unini);
            CompatHelper.setBackground(txtPath, c, R.drawable.rounded_corner_unini);
            CompatHelper.setBackground(txtPlan, c, R.drawable.rounded_corner_unini);

            txtGPSSatsInView.setText(R.string.EMPTY_STRING);
            CompatHelper.setBackground(txtGPS, c, R.drawable.rounded_corner_unini);
            CompatHelper.setBackground(txtSensor, c, R.drawable.rounded_corner_unini);
            CompatHelper.setBackground(txtAirspd, c, R.drawable.rounded_corner_unini);
            CompatHelper.setBackground(txtMag, c, R.drawable.rounded_corner_unini);

            CompatHelper.setBackground(txtInput, c, R.drawable.rounded_corner_unini);
            CompatHelper.setBackground(txtOutput, c, R.drawable.rounded_corner_unini);
            CompatHelper.setBackground(txtI2C, c, R.drawable.rounded_corner_unini);
            CompatHelper.setBackground(txtTelemetry, c, R.drawable.rounded_corner_unini);

            CompatHelper.setBackground(txtBatt, c, R.drawable.rounded_corner_unini);
            CompatHelper.setBackground(txtTime, c, R.drawable.rounded_corner_unini);
            CompatHelper.setBackground(txtConfig, c, R.drawable.rounded_corner_unini);

            CompatHelper.setBackground(txtBoot, c, R.drawable.rounded_corner_unini);
            CompatHelper.setBackground(txtMem, c, R.drawable.rounded_corner_unini);
            CompatHelper.setBackground(txtStack, c, R.drawable.rounded_corner_unini);
            CompatHelper.setBackground(txtEvent, c, R.drawable.rounded_corner_unini);
            CompatHelper.setBackground(txtCPU, c, R.drawable.rounded_corner_unini);

        }
        if (isView(R.id.root_main_inc_info)) {
            txtArmed.setText(R.string.EMPTY_STRING);

            txtVolt.setText(R.string.EMPTY_STRING);
            txtAmpere.setText(R.string.EMPTY_STRING);
            txtmAh.setText(R.string.EMPTY_STRING);
            txtTimeLeft.setText(R.string.EMPTY_STRING);

            txtAltitude.setText(R.string.EMPTY_STRING);
            txtAltitudeAccel.setText(R.string.EMPTY_STRING);

            txtModeNum.setText(R.string.EMPTY_STRING);
            txtModeFlightMode.setText(R.string.EMPTY_STRING);
            txtModeAssistedControl.setText(R.string.EMPTY_STRING);
        }

        if (isView(R.id.root_main_inc_map)) {
            getMainActivity().mVcList.get(ViewController.VIEW_MAP).reset();
        }
    }

    @Override
    public void onToolbarFlightSettingsClick(View v) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getMainActivity());
        dialogBuilder.setTitle(R.string.FLIGHT_HEALTH_SETTINGS);
        dialogBuilder.setView(mFlightSettingsView);

        try {
            txtHealthAlertDialogBatteryCapacity.setText(getMainActivity().mFcDevice.getObjectTree()
                    .getData("FlightBatterySettings", "Capacity").toString());
            txtHealthAlertDialogBatteryCells.setText(getMainActivity().mFcDevice.getObjectTree()
                    .getData("FlightBatterySettings", "NbCells").toString());
            txtHealthAlertDialogFusionAlgorithm.setText(getMainActivity().mFcDevice.getObjectTree()
                    .getData("RevoSettings", "FusionAlgorithm").toString());
        } catch (UAVTalkMissingObjectException | NullPointerException e) {
            e.printStackTrace();
        }

        dialogBuilder.setPositiveButton(R.string.CLOSE_BUTTON,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        try {
            ((ViewGroup) mFlightSettingsView.getParent()).removeView(mFlightSettingsView);
        } catch (NullPointerException ignored) {
        }
        dialogBuilder.show();
    }

    @Override
    public void onToolbarLocalSettingsClick(View v) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getMainActivity());
        dialogBuilder.setTitle(R.string.LOCAL_HEALTH_SETTINGS);
        dialogBuilder.setView(mLocalSettingsView);


        dialogBuilder.setPositiveButton(R.string.CLOSE_BUTTON,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SettingsHelper.mTopLeftLayout = spiTopLeft.getSelectedItem().toString();
                        SettingsHelper.mBottomRightLayout =
                                spiBottomRight.getSelectedItem().toString();
                        SettingsHelper.saveSettings(getMainActivity());
                        setLayout();
                        dialog.dismiss();
                    }
                });

        try {
            ((ViewGroup) mLocalSettingsView.getParent()).removeView(mLocalSettingsView);
        } catch (NullPointerException ignored) {
        }
        dialogBuilder.show();

    }

    private void onAltitudeClick(View v) {
        try {
            mOffset.put(getString(R.string.OFFSET_BAROSENSOR_ALTITUDE, R.string.APP_ID),
                    getMainActivity().mFcDevice.getObjectTree()
                            .getData("BaroSensor", "Altitude"));
            txtAltitude.setText(R.string.EMPTY_STRING);
        } catch (UAVTalkMissingObjectException | NullPointerException e) {
            VisualLog.i("INFO", "UAVO is missing");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtAltitude: {
                onAltitudeClick(v);
                break;
            }
            case R.id.txtHealthAlertDialogBatteryCapacity: {
                onBatteryCapacityClick(v);
                break;
            }
            case R.id.txtHealthAlertDialogBatteryCells: {
                onBatteryCellsClick(v);
                break;
            }
            case R.id.txtHealthAlertDialogFusionAlgorithm: {
                onFusionAlgoClick(v);
                break;
            }
        }
    }

    private void onBatteryCapacityClick(View v) {
        String moduleEnabled =
                getData("HwSettings", "OptionalModules", "Battery", true).toString();
        if (moduleEnabled.equals("Enabled")) {
            new NumberInputAlertDialog(getMainActivity())
                    .withPresetText(
                            txtHealthAlertDialogBatteryCapacity.getText().toString())
                    .withTitle(getString(R.string.CAPACITY_DIALOG_TITLE))
                    .withLayout(R.layout.alert_dialog_integer_input)
                    .withUavTalkDevice(getMainActivity().mFcDevice)
                    .withObject("FlightBatterySettings")
                    .withField("Capacity")
                    .withFieldType(UAVTalkXMLObject.FIELDTYPE_UINT32)
                    .show();
        } else {
            SingleToast.show(getMainActivity(), "Battery Module not enabled", Toast.LENGTH_SHORT);
        }
    }

    private void onBatteryCellsClick(View v) {
        String moduleEnabled =
                getData("HwSettings", "OptionalModules", "Battery", true).toString();
        if (moduleEnabled.equals("Enabled")) {
            new NumberInputAlertDialog(getMainActivity())
                    .withPresetText(txtHealthAlertDialogBatteryCells.getText().toString())
                    .withTitle(getString(R.string.CELLS_DIALOG_TITLE))
                    .withLayout(R.layout.alert_dialog_integer_input)
                    .withUavTalkDevice(getMainActivity().mFcDevice)
                    .withObject("FlightBatterySettings")
                    .withField("NbCells")
                    .withFieldType(UAVTalkXMLObject.FIELDTYPE_UINT8)
                    .withMinMax(1, 254)
                    .show();
        } else {
            SingleToast.show(getMainActivity(), "Battery Module not enabled", Toast.LENGTH_SHORT);
        }
    }

    private void onFusionAlgoClick(View v) {
        final MainActivity ma = getMainActivity();
        if (ma.mFcDevice != null) {
            String armingState;
            try {
                armingState =
                        ma.mFcDevice.getObjectTree().getData("FlightStatus", "Armed").toString();
            } catch (UAVTalkMissingObjectException | NullPointerException e) {
                armingState = "";
                ma.mFcDevice.requestObject("FlightStatus");
            }
            if (armingState.equals("Disarmed")) {
                new EnumInputAlertDialog(ma)
                        .withTitle("Select Fusion Algorithm")
                        .withUavTalkDevice(ma.mFcDevice)
                        .withObject("RevoSettings")
                        .withField("FusionAlgorithm")
                        .show();
            } else {
                SingleToast.show(ma,
                        getString(R.string.CHANGE_FUSION_ALGO_DISARMED) + " " + armingState,
                        Toast.LENGTH_LONG);
            }
        } else {
            SingleToast.show(ma, R.string.SEND_FAILED, Toast.LENGTH_SHORT);
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.swiEnableText2Speech) {
            SettingsHelper.mText2SpeechEnabled = isChecked;
            getMainActivity().getTtsHelper().setEnabled(SettingsHelper.mText2SpeechEnabled);
            getMainActivity().getPreferences(Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(getString(R.string.SETTINGS_TEXT2SPEECH_ENABLED, R.string.APP_ID),
                            SettingsHelper.mText2SpeechEnabled)
                    .apply();
        }
    }

    @Override
    public void onObjectUpdate(UAVTalkObject o) {
        if (isView(R.id.root_main_inc_pfd)) {

            Float roll;
            Float pitch;
            boolean inval = false;

            try {
                roll = (Float) getData("AttitudeState", "Roll");
                pitch = (Float) getData("AttitudeState", "Pitch");
            } catch (java.lang.ClassCastException e) {
                roll = null;
                pitch = null;
            }

            if (roll != null && (roll - mPreviousRoll > 1 || roll - mPreviousRoll < -1)) {
                imgPfdRollPitch.setRoll(roll);
                mPreviousRoll = roll;
                inval = true;
            }

            if (pitch != null && (pitch - mPreviousPitch > 1 || pitch - mPreviousPitch < -1)) {
                imgPfdRollPitch.setPitch(pitch);
                mPreviousPitch = pitch;
                inval = true;
            }

            //if(pitch!=null && roll!=null) {
            //    VisualLog.d("DBG", ""+roll+" " +pitch);
            //}

            if (inval) {
                imgPfdRollPitch.postInvalidate();
            }
        }
    }
}
