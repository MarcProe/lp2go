/*
 * @file   ViewControllerTuningResp.java
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

package org.librepilot.lp2go.controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.c.RESP;
import org.librepilot.lp2go.helper.CompatHelper;
import org.librepilot.lp2go.helper.H;
import org.librepilot.lp2go.helper.SettingsHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkDeviceHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectTree;
import org.librepilot.lp2go.uavtalk.UAVTalkXMLObject;
import org.librepilot.lp2go.ui.ObjectTextView;
import org.librepilot.lp2go.ui.SingleToast;
import org.librepilot.lp2go.ui.alertdialog.PidInputAlertDialog;

import java.text.MessageFormat;
import java.util.HashSet;

public class ViewControllerTuningResp extends ViewController implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private final HashSet<ObjectTextView> mRespTexts;
    private final ObjectTextView txtRespRollAcroFactor;
    private final ObjectTextView txtRespPitchAcroFactor;
    private final CheckBox cbxRespLinkRollAndPitch;
    private ImageView imgRespBank;
    private ImageView imgRespDownload;
    private ImageView imgRespSave;
    private ImageView imgRespUpload;
    private String mCurrentStabilizationBank;

    public ViewControllerTuningResp(MainActivity activity, int title, int icon, int localSettingsVisible,
                                    int flightSettingsVisible) {
        super(activity, title, icon, localSettingsVisible, flightSettingsVisible);

        final MainActivity a = getMainActivity();

        a.mViews.put(VIEW_RESP, a.getLayoutInflater().inflate(R.layout.activity_responsiveness, null));
        a.setContentView(a.mViews.get(VIEW_RESP));

        a.findViewById(R.id.imgRespAtti).setOnClickListener(this);
        a.findViewById(R.id.imgRespRate).setOnClickListener(this);
        a.findViewById(R.id.imgRespMax).setOnClickListener(this);
        a.findViewById(R.id.imgRespAcro).setOnClickListener(this);

        cbxRespLinkRollAndPitch = ((CheckBox) a.findViewById(R.id.cbxRespLinkRollAndPitch));
        cbxRespLinkRollAndPitch.setOnCheckedChangeListener(this);

        imgRespBank = (ImageView) a.findViewById(R.id.imgRespBank);

        imgRespUpload = (ImageView) a.findViewById(R.id.imgRespUpload);
        imgRespDownload = (ImageView) a.findViewById(R.id.imgRespDownload);
        imgRespSave = (ImageView) a.findViewById(R.id.imgRespSave);

        imgRespUpload.setOnClickListener(this);
        imgRespDownload.setOnClickListener(this);
        imgRespSave.setOnClickListener(this);

        mRespTexts = new HashSet<>();

        ObjectTextView txtRespRollAttitude =
                (ObjectTextView) a.findViewById(R.id.txtRespRollAttitude);
        if (txtRespRollAttitude != null) {
            txtRespRollAttitude.init(
                    RESP.RESP_ROLL_ATTI_DENOM,
                    RESP.RESP_ROLL_ATTI_MAX,
                    RESP.RESP_ROLL_ATTI_STEP,
                    RESP.RESP_ROLL_ATTI_DFS,
                    "Roll Attitude Mode Response (deg)",
                    "RollMax", "",
                    UAVTalkXMLObject.FIELDTYPE_UINT8);
        }
        mRespTexts.add(txtRespRollAttitude);

        ObjectTextView txtRespPitchAttitude =
                (ObjectTextView) a.findViewById(R.id.txtRespPitchAttitude);
        if (txtRespPitchAttitude != null) {
            txtRespPitchAttitude.init(
                    RESP.RESP_PITCH_ATTI_DENOM,
                    RESP.RESP_PITCH_ATTI_MAX,
                    RESP.RESP_PITCH_ATTI_STEP,
                    RESP.RESP_PITCH_ATTI_DFS,
                    "Pitch Attitude Mode Response (deg)",
                    "PitchMax", "",
                    UAVTalkXMLObject.FIELDTYPE_UINT8);
        }
        mRespTexts.add(txtRespPitchAttitude);

        ObjectTextView txtRespRollRate =
                (ObjectTextView) a.findViewById(R.id.txtRespRollRate);
        if (txtRespRollRate != null) {
            txtRespRollRate.init(
                    RESP.RESP_ROLL_RATE_DENOM,
                    RESP.RESP_ROLL_RATE_MAX,
                    RESP.RESP_ROLL_RATE_STEP,
                    RESP.RESP_ROLL_RATE_DFS,
                    "Roll Rate Mode Response (deg/s)",
                    "ManualRate", "Roll",
                    UAVTalkXMLObject.FIELDTYPE_UINT16);
        }
        mRespTexts.add(txtRespRollRate);

        ObjectTextView txtRespPitchRate =
                (ObjectTextView) a.findViewById(R.id.txtRespPitchRate);
        if (txtRespPitchRate != null) {
            txtRespPitchRate.init(
                    RESP.RESP_PITCH_RATE_DENOM,
                    RESP.RESP_PITCH_RATE_MAX,
                    RESP.RESP_PITCH_RATE_STEP,
                    RESP.RESP_PITCH_RATE_DFS,
                    "Pitch Rate Mode Response (deg/s)",
                    "ManualRate", "Pitch",
                    UAVTalkXMLObject.FIELDTYPE_UINT16);
        }
        mRespTexts.add(txtRespPitchRate);

        ObjectTextView txtRespYawRate =
                (ObjectTextView) a.findViewById(R.id.txtRespYawRate);
        if (txtRespYawRate != null) {
            txtRespYawRate.init(
                    RESP.RESP_YAW_RATE_DENOM,
                    RESP.RESP_YAW_RATE_MAX,
                    RESP.RESP_YAW_RATE_STEP,
                    RESP.RESP_YAW_RATE_DFS,
                    "Yaw Rate Mode Response (deg/s)",
                    "ManualRate", "Yaw",
                    UAVTalkXMLObject.FIELDTYPE_UINT16);
        }
        mRespTexts.add(txtRespYawRate);

        ObjectTextView txtRespRollLimit =
                (ObjectTextView) a.findViewById(R.id.txtRespRollLimit);
        if (txtRespRollLimit != null) {
            txtRespRollLimit.init(
                    RESP.RESP_ROLL_LIMIT_DENOM,
                    RESP.RESP_ROLL_LIMIT_MAX,
                    RESP.RESP_ROLL_LIMIT_STEP,
                    RESP.RESP_ROLL_LIMIT_DFS,
                    "Max Roll Rate Limit (all modes) (deg/s)",
                    "MaximumRate", "Roll",
                    UAVTalkXMLObject.FIELDTYPE_UINT16);
        }
        mRespTexts.add(txtRespRollLimit);

        ObjectTextView txtRespPitchLimit =
                (ObjectTextView) a.findViewById(R.id.txtRespPitchLimit);
        if (txtRespPitchLimit != null) {
            txtRespPitchLimit.init(
                    RESP.RESP_PITCH_LIMIT_DENOM,
                    RESP.RESP_PITCH_LIMIT_MAX,
                    RESP.RESP_PITCH_LIMIT_STEP,
                    RESP.RESP_PITCH_LIMIT_DFS,
                    "Max Pitch Rate Limit (all modes) (deg/s)",
                    "MaximumRate", "Pitch",
                    UAVTalkXMLObject.FIELDTYPE_UINT16);
        }
        mRespTexts.add(txtRespPitchLimit);

        ObjectTextView txtRespYawLimit =
                (ObjectTextView) a.findViewById(R.id.txtRespYawLimit);
        if (txtRespYawLimit != null) {
            txtRespYawLimit.init(
                    RESP.RESP_YAW_LIMIT_DENOM,
                    RESP.RESP_YAW_LIMIT_MAX,
                    RESP.RESP_YAW_LIMIT_STEP,
                    RESP.RESP_YAW_LIMIT_DFS,
                    "Max Yaw Rate Limit (all modes) (deg/s)",
                    "MaximumRate", "Yaw",
                    UAVTalkXMLObject.FIELDTYPE_UINT16);
        }
        mRespTexts.add(txtRespYawLimit);

        txtRespRollAcroFactor =
                (ObjectTextView) a.findViewById(R.id.txtRespRollAcroFactor);
        if (txtRespRollAcroFactor != null) {
            txtRespRollAcroFactor.init(
                    RESP.RESP_ROLL_ACRO_DENOM,
                    RESP.RESP_ROLL_ACRO_MAX,
                    RESP.RESP_ROLL_ACRO_STEP,
                    RESP.RESP_ROLL_ACRO_DFS,
                    "Roll Factor",
                    "AcroInsanityFactor", "Roll",
                    UAVTalkXMLObject.FIELDTYPE_UINT8);
        }
        mRespTexts.add(txtRespRollAcroFactor);

        txtRespPitchAcroFactor =
                (ObjectTextView) a.findViewById(R.id.txtRespPitchAcroFactor);
        if (txtRespPitchAcroFactor != null) {
            txtRespPitchAcroFactor.init(
                    RESP.RESP_PITCH_ACRO_DENOM,
                    RESP.RESP_PITCH_ACRO_MAX,
                    RESP.RESP_PITCH_ACRO_STEP,
                    RESP.RESP_PITCH_ACRO_DFS,
                    "Pitch Factor",
                    "AcroInsanityFactor", "Pitch",
                    UAVTalkXMLObject.FIELDTYPE_UINT8);
        }
        mRespTexts.add(txtRespPitchAcroFactor);

        ObjectTextView txtRespYawAcroFactor =
                (ObjectTextView) a.findViewById(R.id.txtRespYawAcroFactor);
        if (txtRespYawAcroFactor != null) {
            txtRespYawAcroFactor.init(
                    RESP.RESP_YAW_ACRO_DENOM,
                    RESP.RESP_YAW_ACRO_MAX,
                    RESP.RESP_YAW_ACRO_STEP,
                    RESP.RESP_YAW_ACRO_DFS,
                    "Yaw Factor",
                    "AcroInsanityFactor", "Yaw",
                    UAVTalkXMLObject.FIELDTYPE_UINT8);
        }
        mRespTexts.add(txtRespYawAcroFactor);

        for (ObjectTextView ptv : mRespTexts) {
            ptv.setOnClickListener(this);
        }
    }

    @Override
    public int getID() {
        return ViewController.VIEW_RESP;
    }

    @Override
    public void enter(int view) {
        super.enter(view);

        try {
            final MainActivity a = getMainActivity();
            final View lloResponsiveness = findViewById(R.id.lloResponsiveness);
            final View lloAcroPlus = findViewById(R.id.lloAcroPlus);

            if (lloAcroPlus != null && lloResponsiveness != null) {
                if (SettingsHelper.mColorfulResp) {
                    CompatHelper.setBackground(lloResponsiveness, a.getApplicationContext(),
                            R.drawable.border_top_yellow);
                    CompatHelper.setBackground(lloAcroPlus, a.getApplicationContext(),
                            R.drawable.border_top_blue);
                } else {
                    CompatHelper.setBackground(lloResponsiveness, a.getApplicationContext(),
                            R.drawable.border_top);
                    CompatHelper.setBackground(lloAcroPlus, a.getApplicationContext(),
                            R.drawable.border_top);
                }
            }
        } catch (NullPointerException e1) {
            VisualLog.d(e1.getMessage());
        }
    }

    @Override
    public void update() {
        super.update();
        MainActivity a = getMainActivity();
        String fmode =
                getData("ManualControlCommand", "FlightModeSwitchPosition")
                        .toString();
        String bank =
                getData("StabilizationSettings", "FlightModeMap", fmode)
                        .toString();

        mCurrentStabilizationBank = "StabilizationSettings" + bank;

        switch (mCurrentStabilizationBank) {
            case "StabilizationSettingsBank1":
                imgRespBank.setImageDrawable(ContextCompat.getDrawable(
                        a.getApplicationContext(),
                        R.drawable.ic_filter_1_128dp));
                a.mFcDevice.requestObject("StabilizationSettingsBank1");
                break;
            case "StabilizationSettingsBank2":
                imgRespBank.setImageDrawable(ContextCompat.getDrawable(
                        a.getApplicationContext(),
                        R.drawable.ic_filter_2_128dp));
                a.mFcDevice.requestObject("StabilizationSettingsBank2");
                break;
            case "StabilizationSettingsBank3":
                imgRespBank.setImageDrawable(ContextCompat.getDrawable(
                        a.getApplicationContext(),
                        R.drawable.ic_filter_3_128dp));
                a.mFcDevice.requestObject("StabilizationSettingsBank3");
                break;
            default:
                imgRespBank.setImageDrawable(ContextCompat.getDrawable(
                        a.getApplicationContext(),
                        R.drawable.ic_filter_none_128dp));
                break;
        }

        for (ObjectTextView ptv : mRespTexts) {
            String data = null;
            switch (ptv.getFieldType()) {
                case (UAVTalkXMLObject.FIELDTYPE_FLOAT32):
                    data = ptv.getDecimalString(
                            toFloat(getData(mCurrentStabilizationBank,
                                    ptv.getField(), ptv.getElement())));
                    break;
                case (UAVTalkXMLObject.FIELDTYPE_UINT8):
                case (UAVTalkXMLObject.FIELDTYPE_UINT16):
                    data = getData(mCurrentStabilizationBank,
                            ptv.getField(), ptv.getElement()).toString();
                    break;
                default: //do nothing
                    break;
            }
            if (data == null || data.equals("")) {
                data = "0";
            }
            ptv.setText(data);
        }

        if (cbxRespLinkRollAndPitch.isChecked()) {
            txtRespPitchAcroFactor.setText(txtRespRollAcroFactor.getText());
        }
    }

    @Override
    public void onToolbarLocalSettingsClick(View v) {
        {
            final String[] items = {getString(R.string.COLORFUL_VIEW)};
            final boolean[] checkedItems = {true};

            checkedItems[0] = SettingsHelper.mColorfulResp;


            AlertDialog dialog = new AlertDialog.Builder(getMainActivity())
                    .setTitle(R.string.SETTINGS)
                    .setMultiChoiceItems(items, checkedItems,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int indexSelected,
                                                    boolean isChecked) {

                                    SettingsHelper.mColorfulResp = isChecked;

                                }
                            }).setPositiveButton(R.string.CLOSE_BUTTON,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    enter(VIEW_RESP);
                                }
                            }).create();
            dialog.show();
        }
    }

    private void onRespSaveClick() {
        MainActivity ma = getMainActivity();
        if (ma.mFcDevice != null && ma.mFcDevice.isConnected()) {
            ma.mFcDevice.savePersistent(mCurrentStabilizationBank);
            SingleToast.show(ma, getString(R.string.SAVED_PERSISTENT)
                    + getString(R.string.CHECK_PID_WARNING), Toast.LENGTH_SHORT);
        } else {
            SingleToast.show(ma, R.string.SEND_FAILED, Toast.LENGTH_SHORT);
        }
    }

    private void onRespUploadClick() {
        VisualLog.d("DBG", "" + mCurrentStabilizationBank);
        MainActivity ma = getMainActivity();
        if (ma.mFcDevice != null && ma.mFcDevice.isConnected()) {
            UAVTalkObjectTree oTree = ma.mFcDevice.getObjectTree();
            if (oTree != null) {
                oTree.getObjectFromName(mCurrentStabilizationBank).setWriteBlocked(true);

                for (ObjectTextView ptv : mRespTexts) {
                    switch (ptv.getFieldType()) {
                        case (UAVTalkXMLObject.FIELDTYPE_FLOAT32):
                            try {
                                float f = H.stringToFloat(ptv.getText().toString());

                                byte[] buffer = H.reverse4bytes(H.floatToByteArray(f));

                                UAVTalkDeviceHelper.updateSettingsObject(
                                        oTree, mCurrentStabilizationBank, 0,
                                        ptv.getField(), ptv.getElement(), buffer);
                            } catch (NumberFormatException e) {
                                VisualLog.e("MainActivity",
                                        "Error parsing float (vertical): " + ptv.getField() + " " +
                                                ptv.getElement() + " " + ptv.getText().toString());
                            }
                            break;
                        case (UAVTalkXMLObject.FIELDTYPE_UINT8): {
                            try {
                                byte[] buffer = new byte[1];
                                buffer[0] =
                                        (byte) (Integer.parseInt(ptv.getText().toString()) & 0xff);
                                UAVTalkDeviceHelper.updateSettingsObject(
                                        oTree, mCurrentStabilizationBank, 0, ptv.getField(),
                                        ptv.getElement(), buffer);
                            } catch (NumberFormatException e) {
                                VisualLog.e(e.getMessage());
                            }
                            break;
                        }
                        case (UAVTalkXMLObject.FIELDTYPE_UINT16): {
                            try {
                                byte[] buffer = new byte[2];
                                int i = Integer.parseInt(ptv.getText().toString());
                                byte[] temp = H.toBytes(i);
                                buffer[0] = temp[3];
                                buffer[1] = temp[2];
                                UAVTalkDeviceHelper.updateSettingsObject(
                                        oTree, mCurrentStabilizationBank, 0, ptv.getField(),
                                        ptv.getElement(), buffer);
                            } catch (NumberFormatException e) {
                                VisualLog.e(e.getMessage());
                            }
                            break;
                        }
                        default:
                            VisualLog.e("Internal Error: Type not implemented");
                            break;
                    }
                }

                ma.mFcDevice.sendSettingsObject(mCurrentStabilizationBank, 0);

                SingleToast.show(ma, getString(R.string.PID_SENT)
                        + getString(R.string.CHECK_PID_WARNING), Toast.LENGTH_SHORT);

                oTree.getObjectFromName(mCurrentStabilizationBank).setWriteBlocked(false);
            }
        } else {
            SingleToast.show(ma, R.string.SEND_FAILED, Toast.LENGTH_SHORT);
        }
    }

    private void onRespDownloadClick() {
        MainActivity ma = getMainActivity();
        if (ma.mFcDevice != null && ma.mFcDevice.isConnected()) {

            for (ObjectTextView ptv : mRespTexts) {
                ptv.allowUpdate();
            }

            SingleToast.show(ma, MessageFormat.format("{0}{1}",
                    getString(R.string.PID_LOADING), getString(R.string.CHECK_PID_WARNING)),
                    Toast.LENGTH_SHORT);
        } else {
            SingleToast.show(ma, R.string.SEND_FAILED, Toast.LENGTH_SHORT);
        }
    }

    private void onRespGridNumberClick(View v) {
        ObjectTextView p = (ObjectTextView) v;
        new PidInputAlertDialog(getMainActivity())
                .withStep(p.getStep())
                .withDenominator(p.getDenom())
                .withDecimalFormat(p.getDfs())
                .withPidTextView(p)
                .withValueMax(p.getMax())
                .withPresetText(p.getText().toString())
                .withTitle(p.getDialogTitle())
                .withLayout(R.layout.alert_dialog_pid_grid)
                .withUavTalkDevice(getMainActivity().mFcDevice)
                .withObject(mCurrentStabilizationBank)
                .withField(p.getField())
                .withElement(p.getElement())
                .withFieldType(p.getFieldType())
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgRespDownload: {
                onRespDownloadClick();
            }
            break;
            case R.id.imgRespUpload: {
                onRespUploadClick();
            }
            break;
            case R.id.imgRespSave: {
                onRespSaveClick();
            }
            break;
            case R.id.imgRespAtti:
            case R.id.imgRespRate:
            case R.id.imgRespMax:
            case R.id.imgRespAcro: {
                SingleToast.show(getMainActivity(), v.getContentDescription().toString());
            }
            break;
            default: //do nothing
                break;
        }

        if (v.getClass().equals(ObjectTextView.class)) {
            onRespGridNumberClick(v);
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.cbxRespLinkRollAndPitch: {
                if (b) {
                    txtRespPitchAcroFactor.setVisibility(View.GONE);
                    LinearLayout.LayoutParams params =
                            (LinearLayout.LayoutParams) txtRespRollAcroFactor.getLayoutParams();
                    params.weight = 2.0f;
                    txtRespRollAcroFactor.setLayoutParams(params);
                } else {
                    txtRespPitchAcroFactor.setVisibility(View.VISIBLE);
                    LinearLayout.LayoutParams params =
                            (LinearLayout.LayoutParams) txtRespRollAcroFactor.getLayoutParams();
                    params.weight = 1.0f;
                    txtRespRollAcroFactor.setLayoutParams(params);
                }
                break;
            }
            default: //do nothing
                break;
        }
    }

    @Override
    public boolean isChild() {
        return true;
    }
}
