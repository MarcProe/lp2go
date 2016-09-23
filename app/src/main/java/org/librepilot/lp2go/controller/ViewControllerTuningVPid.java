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
import android.content.DialogInterface;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.c.PID;
import org.librepilot.lp2go.helper.CompatHelper;
import org.librepilot.lp2go.helper.H;
import org.librepilot.lp2go.helper.SettingsHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkDeviceHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectTree;
import org.librepilot.lp2go.uavtalk.UAVTalkXMLObject;
import org.librepilot.lp2go.ui.ObjectTextView;
import org.librepilot.lp2go.ui.SingleToast;
import org.librepilot.lp2go.ui.alertdialog.PidInputAlertDialog;

import java.util.HashSet;

public class ViewControllerTuningVPid extends ViewController implements View.OnClickListener {
    private final ImageView imgVPidDownload;
    private final ImageView imgVPidSave;
    private final ImageView imgVPidUpload;
    private final HashSet<ObjectTextView> mVerticalPidTexts;

    public ViewControllerTuningVPid(MainActivity activity, int title, int icon, int localSettingsVisible,
                                    int flightSettingsVisible) {
        super(activity, title, icon, localSettingsVisible, flightSettingsVisible);

        final MainActivity ma = getMainActivity();

        ma.mViews.put(VIEW_VPID, ma.getLayoutInflater().inflate(R.layout.activity_vpid, null));
        ma.setContentView(ma.mViews.get(VIEW_VPID));

        imgVPidUpload = (ImageView) ma.findViewById(R.id.imgVPidUpload);
        imgVPidDownload = (ImageView) ma.findViewById(R.id.imgVPidDownload);
        imgVPidSave = (ImageView) ma.findViewById(R.id.imgVPidSave);

        if (imgVPidUpload != null) {
            imgVPidUpload.setOnClickListener(this);
        }
        if (imgVPidDownload != null) {
            imgVPidDownload.setOnClickListener(this);
        }
        if (imgVPidSave != null) {
            imgVPidSave.setOnClickListener(this);
        }

        mVerticalPidTexts = new HashSet<>();

        ObjectTextView txtVerticalAltitudeProportional =
                (ObjectTextView) findViewById(R.id.txtVerticalAltitudeProportional);
        if (txtVerticalAltitudeProportional != null) {
            txtVerticalAltitudeProportional.init(
                    PID.PID_VERTICAL_ALTI_PROP_DENOM,
                    PID.PID_VERTICAL_ALTI_PROP_MAX,
                    PID.PID_VERTICAL_ALTI_PROP_STEP,
                    PID.PID_VERTICAL_ALTI_PROP_DFS,
                    getString(R.string.VPID_NAME_ALP),
                    "VerticalPosP", "");
        }
        mVerticalPidTexts.add(txtVerticalAltitudeProportional);

        ObjectTextView txtVerticalExponential =
                (ObjectTextView) findViewById(R.id.txtVerticalExponential);
        if (txtVerticalExponential != null) {
            txtVerticalExponential.init(
                    PID.PID_VERTICAL_EXPO_DENOM,
                    PID.PID_VERTICAL_EXPO_MAX,
                    PID.PID_VERTICAL_EXPO_STEP,
                    PID.PID_VERTICAL_EXPO_DFS,
                    getString(R.string.VPID_NAME_EXP),
                    "ThrustExp", "",
                    UAVTalkXMLObject.FIELDTYPE_UINT8);
        }
        mVerticalPidTexts.add(txtVerticalExponential);

        ObjectTextView txtVerticalThrustRate =
                (ObjectTextView) findViewById(R.id.txtVerticalThrustRate);
        if (txtVerticalThrustRate != null) {
            txtVerticalThrustRate.init(
                    PID.PID_VERTICAL_THRUST_R_DENOM,
                    PID.PID_VERTICAL_THRUST_R_MAX,
                    PID.PID_VERTICAL_THRUST_R_STEP,
                    PID.PID_VERTICAL_THRUST_R_DFS,
                    getString(R.string.VPID_NAME_THR),
                    "ThrustRate", "");
        }
        mVerticalPidTexts.add(txtVerticalThrustRate);

        ObjectTextView txtVerticalVelocityBeta =
                (ObjectTextView) findViewById(R.id.txtVerticalVelocityBeta);
        if (txtVerticalVelocityBeta != null) {
            txtVerticalVelocityBeta.init(
                    PID.PID_VERTICAL_VELO_BETA_DENOM,
                    PID.PID_VERTICAL_VELO_BETA_MAX,
                    PID.PID_VERTICAL_VELO_BETA_STEP,
                    PID.PID_VERTICAL_VELO_BETA_DFS,
                    getString(R.string.VPID_NAME_VEB),
                    "VerticalVelPID", "Beta");
        }
        mVerticalPidTexts.add(txtVerticalVelocityBeta);

        ObjectTextView txtVerticalVelocityDerivative =
                (ObjectTextView) findViewById(R.id.txtVerticalVelocityDerivative);
        if (txtVerticalVelocityDerivative != null) {
            txtVerticalVelocityDerivative.init(
                    PID.PID_VERTICAL_VELO_DERI_DENOM,
                    PID.PID_VERTICAL_VELO_DERI_MAX,
                    PID.PID_VERTICAL_VELO_DERI_STEP,
                    PID.PID_VERTICAL_VELO_DERI_DFS,
                    getString(R.string.VPID_NAME_VED),
                    "VerticalVelPID", "Kd");
        }
        mVerticalPidTexts.add(txtVerticalVelocityDerivative);

        ObjectTextView txtVerticalVelocityIntegral =
                (ObjectTextView) findViewById(R.id.txtVerticalVelocityIntegral);
        if (txtVerticalVelocityIntegral != null) {
            txtVerticalVelocityIntegral.init(
                    PID.PID_VERTICAL_VELO_INTE_DENOM,
                    PID.PID_VERTICAL_VELO_INTE_MAX,
                    PID.PID_VERTICAL_VELO_INTE_STEP,
                    PID.PID_VERTICAL_VELO_INTE_DFS,
                    getString(R.string.VPID_NAME_VEI),
                    "VerticalVelPID", "Ki");
        }
        mVerticalPidTexts.add(txtVerticalVelocityIntegral);

        ObjectTextView txtVerticalVelocityProportional =
                (ObjectTextView) findViewById(R.id.txtVerticalVelocityProportional);
        if (txtVerticalVelocityProportional != null) {
            txtVerticalVelocityProportional.init(
                    PID.PID_VERTICAL_VELO_PROP_DENOM,
                    PID.PID_VERTICAL_VELO_PROP_MAX,
                    PID.PID_VERTICAL_VELO_PROP_STEP,
                    PID.PID_VERTICAL_VELO_PROP_DFS,
                    getString(R.string.VPID_NAME_VEP),
                    "VerticalVelPID", "Kp");
        }
        mVerticalPidTexts.add(txtVerticalVelocityProportional);

        for (ObjectTextView ptv : mVerticalPidTexts) {
            ptv.setOnClickListener(this);
        }
    }

    @Override
    public void enter(int view) {
        super.enter(view);

        final MainActivity ma = getMainActivity();

        SingleToast.show(ma, R.string.CHECK_PID_WARNING, Toast.LENGTH_SHORT);

        try {

            final View lloStickResponse = findViewById(R.id.lloStickResponse);
            final View lloControllCoeff = findViewById(R.id.lloControlCoeff);

            if (lloStickResponse != null && lloControllCoeff != null) {
                if (SettingsHelper.mColorfulVPid) {
                    CompatHelper.setBackground(lloStickResponse, ma.getApplicationContext(),
                            R.drawable.border_top_yellow);
                    CompatHelper.setBackground(lloControllCoeff, ma.getApplicationContext(),
                            R.drawable.border_top_blue);
                } else {
                    CompatHelper.setBackground(lloStickResponse, ma.getApplicationContext(),
                            R.drawable.border_top);
                    CompatHelper.setBackground(lloControllCoeff, ma.getApplicationContext(),
                            R.drawable.border_top);
                }
            }
        } catch (NullPointerException e2) {
            VisualLog.d("MainActivity", "VIEW_VPID", e2);
        }

    }

    @Override
    public void update() {
        super.update();
        for (ObjectTextView ptv : mVerticalPidTexts) {
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
                    if (data == null || data.equals("")) {
                        data = "0";
                    }
                    ptv.setText(data);
                    break;
                default: //do nothing
                    break;

            }
        }
        getMainActivity().mFcDevice.requestObject("AltitudeHoldSettings");

    }

    @Override
    public void onToolbarLocalSettingsClick(View v) {
        final String[] items = {getString(R.string.COLORFUL_VIEW)};
        final boolean[] checkedItems = {true};

        checkedItems[0] = SettingsHelper.mColorfulVPid;

        AlertDialog dialog = new AlertDialog.Builder(getMainActivity())
                .setTitle(R.string.SETTINGS)
                .setMultiChoiceItems(items, checkedItems,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int indexSelected,
                                                boolean isChecked) {

                                SettingsHelper.mColorfulVPid = isChecked;

                            }
                        }).setPositiveButton(R.string.CLOSE_BUTTON,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                enter(VIEW_VPID);
                            }
                        }).create();
        dialog.show();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgVPidDownload: {
                onVerticalPidDownloadClick(v);
            }
            break;
            case R.id.imgVPidUpload: {
                onVerticalPidUploadClick(v);
            }
            break;
            case R.id.imgVPidSave: {
                onVerticalPidSaveClick(v);
            }
            break;
            default: //do nothing
                break;
        }

        if (v.getClass().equals(ObjectTextView.class)) {
            onVerticalPidGridNumberClick(v);
        }
    }

    private void onVerticalPidSaveClick(View v) {
        final MainActivity ma = getMainActivity();
        if (ma.mFcDevice != null && ma.mFcDevice.isConnected()) {
            ma.mFcDevice.savePersistent("AltitudeHoldSettings");
            SingleToast.show(ma, getString(R.string.SAVED_PERSISTENT)
                    + getString(R.string.CHECK_PID_WARNING), Toast.LENGTH_SHORT);
        } else {
            SingleToast.show(ma, R.string.SEND_FAILED, Toast.LENGTH_SHORT);
        }
    }

    private void onVerticalPidUploadClick(View v) {
        final MainActivity ma = getMainActivity();
        if (ma.mFcDevice != null && ma.mFcDevice.isConnected()) {
            UAVTalkObjectTree oTree = ma.mFcDevice.getObjectTree();
            if (oTree != null) {
                oTree.getObjectFromName("AltitudeHoldSettings").setWriteBlocked(true);

                for (ObjectTextView ptv : mVerticalPidTexts) {
                    switch (ptv.getFieldType()) {
                        case (UAVTalkXMLObject.FIELDTYPE_FLOAT32):
                            try {
                                float f = H.stringToFloat(ptv.getText().toString());

                                byte[] buffer = H.reverse4bytes(H.floatToByteArray(f));

                                UAVTalkDeviceHelper.updateSettingsObject(
                                        oTree, "AltitudeHoldSettings", 0,
                                        ptv.getField(), ptv.getElement(), buffer);
                            } catch (NumberFormatException e) {
                                VisualLog.e("MainActivity",
                                        "Error parsing float (vertical): " + ptv.getField() + " " +
                                                ptv.getElement() + " " + ptv.getText().toString());
                            }
                            break;
                        case (UAVTalkXMLObject.FIELDTYPE_UINT8):
                            try {
                                byte[] buffer = new byte[1];
                                VisualLog.d("SDFG", ptv.getText().toString());
                                buffer[0] =
                                        (byte) (Integer.parseInt(ptv.getText().toString()) & 0xff);
                                UAVTalkDeviceHelper.updateSettingsObject(
                                        oTree, "AltitudeHoldSettings", 0, ptv.getField(),
                                        ptv.getElement(), buffer);
                            } catch (NumberFormatException e) {
                                VisualLog.e("MainActivity",
                                        "Error parsing uint8 (vertical): " + ptv.getField() + " " +
                                                ptv.getElement() + " " + ptv.getText().toString());
                            }
                            break;
                        default: //do nothing
                            break;
                    }
                }

                ma.mFcDevice.sendSettingsObject("AltitudeHoldSettings", 0);

                SingleToast.show(ma, getString(R.string.PID_SENT)
                        + getString(R.string.CHECK_PID_WARNING), Toast.LENGTH_SHORT);

                oTree.getObjectFromName("AltitudeHoldSettings").setWriteBlocked(false);
            }
        } else {
            SingleToast.show(ma, R.string.SEND_FAILED, Toast.LENGTH_SHORT);
        }
    }

    private void onVerticalPidDownloadClick(View v) {
        final MainActivity ma = getMainActivity();
        if (ma.mFcDevice != null && ma.mFcDevice.isConnected()) {

            for (ObjectTextView ptv : mVerticalPidTexts) {
                ptv.allowUpdate();
            }

            SingleToast.show(ma, getString(R.string.PID_LOADING)
                    + getString(R.string.CHECK_PID_WARNING), Toast.LENGTH_SHORT);
        } else {
            SingleToast.show(ma, R.string.SEND_FAILED, Toast.LENGTH_SHORT);
        }
    }

    private void onVerticalPidGridNumberClick(View v) {
        final MainActivity ma = getMainActivity();
        ObjectTextView p = (ObjectTextView) v;
        new PidInputAlertDialog(ma)
                .withStep(p.getStep())
                .withDenominator(p.getDenom())
                .withDecimalFormat(p.getDfs())
                .withPidTextView(p)
                .withValueMax(p.getMax())
                .withPresetText(p.getText().toString())
                .withTitle(p.getDialogTitle())
                .withLayout(R.layout.alert_dialog_pid_grid)
                .withUavTalkDevice(ma.mFcDevice)
                .withObject("AltitudeHoldSettings")
                .withField(p.getField())
                .withElement(p.getElement())
                .withFieldType(p.getFieldType())
                .show();
    }

    @Override
    public int getID() {
        return ViewController.VIEW_VPID;
    }
}
