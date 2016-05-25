package org.librepilot.lp2go.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import org.librepilot.lp2go.H;
import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.helper.SettingsHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkMissingObjectException;
import org.librepilot.lp2go.uavtalk.UAVTalkXMLObject;
import org.librepilot.lp2go.ui.SingleToast;
import org.librepilot.lp2go.ui.alertdialog.EnumInputAlertDialog;
import org.librepilot.lp2go.ui.alertdialog.NumberInputAlertDialog;

public class ViewControllerMain extends ViewController implements View.OnClickListener,
        ViewControllerMainAnimatorViewSetter {
    private final View mFlightSettingsView;
    private Drawable imgPacketsBad;
    private Drawable imgPacketsGood;
    private Drawable imgPacketsUp;
    private ViewAnimator mBottomAnimator;
    private int mBottomLayout;
    private ViewAnimator mTopAnimator;
    private int mTopLayout;
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

    public ViewControllerMain(MainActivity activity, int title, int localSettingsVisible,
                              int flightSettingsVisible) {
        super(activity, title, localSettingsVisible, flightSettingsVisible);

        final MainActivity ma = getMainActivity();

        mTopLayout = R.layout.activity_main_inc_empty;
        mBottomLayout = R.layout.activity_main_inc_empty;

        ma.mViews.put(VIEW_MAIN, ma.getLayoutInflater().inflate(R.layout.activity_main, null));
        ma.setContentView(ma.mViews.get(VIEW_MAIN));  //Main

        mTopAnimator = (ViewAnimator) findViewById(R.id.main_top);
        mBottomAnimator = (ViewAnimator) findViewById(R.id.main_bottom);

        setTop(mTopLayout);
        setBottom(mBottomLayout);

        mFlightSettingsView = View.inflate(ma, R.layout.alert_health_settings_flight, null);

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
    }

    @Override
    public void setTop(int topLayoutId) {
        mTopLayout = topLayoutId;

        final View top = LayoutInflater.from(getMainActivity()).inflate(mTopLayout, null);
        final View old = mTopAnimator.getCurrentView();

        mTopAnimator.addView(top);
        mTopAnimator.showNext();
        mTopAnimator.removeView(old);
        init();
    }

    @Override
    public void setBottom(int bottomLayoutId) {
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

        init();
    }

    private void init() {
        if (mTopAnimator.getCurrentView().getId() == R.id.root_main_inc_health
                || (mBottomAnimator.getCurrentView() != null
                && mBottomAnimator.getCurrentView().getId() == R.id.root_main_inc_health)) {
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
        if (mTopAnimator.getCurrentView().getId() == R.id.root_main_inc_info
                || (mBottomAnimator.getCurrentView() != null
                && mBottomAnimator.getCurrentView().getId() == R.id.root_main_inc_info)) {
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

    }

    @Override
    public void update() {
        super.update();

        MainActivity ma = getMainActivity();

        if (mTopAnimator.getCurrentView().getId() == R.id.root_main_inc_health
                || mBottomAnimator.getCurrentView().getId() == R.id.root_main_inc_health) {
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


        if (mTopAnimator.getCurrentView().getId() == R.id.root_main_inc_info
                || mBottomAnimator.getCurrentView().getId() == R.id.root_main_inc_info) {

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
        getMainActivity().mFcDevice.requestObject("FlightBatteryState");
        getMainActivity().mFcDevice.requestObject("SystemStats");
    }

    @Override
    public void reset() {
        Context c = getMainActivity().getApplicationContext();

        if (mTopAnimator.getCurrentView().getId() == R.id.root_main_inc_health
                || mBottomAnimator.getCurrentView().getId() == R.id.root_main_inc_health) {

            txtAtti.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
            txtStab.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
            txtPath.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
            txtPlan.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));

            txtGPSSatsInView.setText(R.string.EMPTY_STRING);
            txtGPS.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
            txtSensor.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
            txtAirspd.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
            txtMag.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));

            txtInput.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
            txtOutput.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
            txtI2C.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
            txtTelemetry
                    .setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));

            txtBatt.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
            txtTime.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
            txtConfig.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));

            txtBoot.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
            txtMem.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
            txtStack.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
            txtEvent.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
            txtCPU.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));

        }
        if (mTopAnimator.getCurrentView().getId() == R.id.root_main_inc_info
                || mBottomAnimator.getCurrentView().getId() == R.id.root_main_inc_info) {
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
    }

    @Override
    public void onToolbarFlightSettingsClick(View v) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getMainActivity());
        dialogBuilder.setTitle(R.string.HEALTH_SETTINGS);
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
        final MainActivity activity = getMainActivity();

        final CharSequence[] items = {getString(R.string.ENABLE_TEXT2SPEECH)};
        final boolean[] checked = {SettingsHelper.mText2SpeechEnabled};
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.SETTINGS)
                .setMultiChoiceItems(items, checked,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int indexSelected,
                                                boolean isChecked) {
                                if (indexSelected == 0) { //first element of string array
                                    SettingsHelper.mText2SpeechEnabled = isChecked;
                                    activity.getTtsHelper()
                                            .setEnabled(SettingsHelper.mText2SpeechEnabled);
                                    activity.getPreferences(Context.MODE_PRIVATE).edit()
                                            .putBoolean(getString(
                                                    R.string.SETTINGS_TEXT2SPEECH_ENABLED,
                                                    R.string.APP_ID),
                                                    SettingsHelper.mText2SpeechEnabled)
                                            .apply();
                                }
                            }
                        })
                .setPositiveButton(R.string.CLOSE_BUTTON,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        })
                .create();
        dialog.show();
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
            } catch (UAVTalkMissingObjectException e) {
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
}
