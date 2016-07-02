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
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.librepilot.lp2go.H;
import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.c.PID;
import org.librepilot.lp2go.helper.CompatHelper;
import org.librepilot.lp2go.helper.SettingsHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkDeviceHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectTree;
import org.librepilot.lp2go.uavtalk.UAVTalkXMLObject;
import org.librepilot.lp2go.ui.PidTextView;
import org.librepilot.lp2go.ui.SingleToast;
import org.librepilot.lp2go.ui.alertdialog.PidInputAlertDialog;

import java.util.HashSet;

public class ViewControllerPid extends ViewController implements View.OnClickListener {
    private final HashSet<PidTextView> mPidTexts;
    private ImageView imgPidBank;
    private ImageView imgPidDownload;
    private ImageView imgPidSave;
    private ImageView imgPidUpload;
    private String mCurrentStabilizationBank;

    public ViewControllerPid(MainActivity activity, int title, int icon, int localSettingsVisible,
                             int flightSettingsVisible) {
        super(activity, title, icon, localSettingsVisible, flightSettingsVisible);

        final MainActivity ma = getMainActivity();
        SingleToast.show(ma, R.string.CHECK_PID_WARNING, Toast.LENGTH_SHORT);

        ma.mViews.put(VIEW_PID, ma.getLayoutInflater().inflate(R.layout.activity_pid, null));
        ma.setContentView(ma.mViews.get(VIEW_PID));

        imgPidBank = (ImageView) ma.findViewById(R.id.imgPidBank);

        imgPidUpload = (ImageView) ma.findViewById(R.id.imgPidUpload);
        imgPidDownload = (ImageView) ma.findViewById(R.id.imgPidDownload);
        imgPidSave = (ImageView) ma.findViewById(R.id.imgPidSave);

        imgPidUpload.setOnClickListener(this);
        imgPidDownload.setOnClickListener(this);
        imgPidSave.setOnClickListener(this);

        mPidTexts = new HashSet<>();

        PidTextView txtPidRateRollProportional =
                (PidTextView) ma.findViewById(R.id.txtRateRollProportional);
        if (txtPidRateRollProportional != null) {
            txtPidRateRollProportional.init(
                    PID.PID_RATE_ROLL_PROP_DENOM,
                    PID.PID_RATE_ROLL_PROP_MAX,
                    PID.PID_RATE_ROLL_PROP_STEP,
                    PID.PID_RATE_ROLL_PROP_DFS,
                    ma.getString(R.string.PID_NAME_RRP),
                    "RollRatePID", "Kp");
        }
        mPidTexts.add(txtPidRateRollProportional);

        PidTextView txtPidRatePitchProportional =
                (PidTextView) ma.findViewById(R.id.txtRatePitchProportional);
        if (txtPidRatePitchProportional != null) {
            txtPidRatePitchProportional.init(
                    PID.PID_RATE_PITCH_PROP_DENOM,
                    PID.PID_RATE_PITCH_PROP_MAX,
                    PID.PID_RATE_PITCH_PROP_STEP,
                    PID.PID_RATE_PITCH_PROP_DFS,
                    ma.getString(R.string.PID_NAME_RPP),
                    "PitchRatePID", "Kp");
        }
        mPidTexts.add(txtPidRatePitchProportional);

        PidTextView txtPidRateYawProportional =
                (PidTextView) ma.findViewById(R.id.txtRateYawProportional);
        if (txtPidRateYawProportional != null) {
            txtPidRateYawProportional.init(
                    PID.PID_RATE_YAW_PROP_DENOM,
                    PID.PID_RATE_YAW_PROP_MAX,
                    PID.PID_RATE_YAW_PROP_STEP,
                    PID.PID_RATE_YAW_PROP_DFS,
                    ma.getString(R.string.PID_NAME_RYP),
                    "YawRatePID", "Kp");
        }
        mPidTexts.add(txtPidRateYawProportional);

        PidTextView txtPidRateRollIntegral =
                (PidTextView) ma.findViewById(R.id.txtRateRollIntegral);
        if (txtPidRateRollIntegral != null) {
            txtPidRateRollIntegral.init(
                    PID.PID_RATE_ROLL_INTE_DENOM,
                    PID.PID_RATE_ROLL_INTE_MAX,
                    PID.PID_RATE_ROLL_INTE_STEP,
                    PID.PID_RATE_ROLL_INTE_DFS,
                    ma.getString(R.string.PID_NAME_RRI),
                    "RollRatePID", "Ki");
        }
        mPidTexts.add(txtPidRateRollIntegral);

        PidTextView txtPidRatePitchIntegral =
                (PidTextView) ma.findViewById(R.id.txtRatePitchIntegral);
        if (txtPidRatePitchIntegral != null) {
            txtPidRatePitchIntegral.init(
                    PID.PID_RATE_PITCH_INTE_DENOM,
                    PID.PID_RATE_PITCH_INTE_MAX,
                    PID.PID_RATE_PITCH_INTE_STEP,
                    PID.PID_RATE_PITCH_INTE_DFS,
                    ma.getString(R.string.PID_NAME_RPI),
                    "PitchRatePID", "Ki");
        }
        mPidTexts.add(txtPidRatePitchIntegral);

        PidTextView txtPidRateYawIntegral =
                (PidTextView) ma.findViewById(R.id.txtRateYawIntegral);
        if (txtPidRateYawIntegral != null) {
            txtPidRateYawIntegral.init(
                    PID.PID_RATE_YAW_INTE_DENOM,
                    PID.PID_RATE_YAW_INTE_MAX,
                    PID.PID_RATE_YAW_INTE_STEP,
                    PID.PID_RATE_YAW_INTE_DFS,
                    ma.getString(R.string.PID_NAME_RYI),
                    "YawRatePID", "Ki");
        }
        mPidTexts.add(txtPidRateYawIntegral);

        PidTextView txtPidRateRollDerivative =
                (PidTextView) ma.findViewById(R.id.txtRateRollDerivative);
        if (txtPidRateRollDerivative != null) {
            txtPidRateRollDerivative.init(
                    PID.PID_RATE_ROLL_DERI_DENOM,
                    PID.PID_RATE_ROLL_DERI_MAX,
                    PID.PID_RATE_ROLL_DERI_STEP,
                    PID.PID_RATE_ROLL_DERI_DFS,
                    ma.getString(R.string.PID_NAME_RRD),
                    "RollRatePID", "Kd");
        }
        mPidTexts.add(txtPidRateRollDerivative);

        PidTextView txtPidRatePitchDerivative =
                (PidTextView) ma.findViewById(R.id.txtRatePitchDerivative);
        if (txtPidRatePitchDerivative != null) {
            txtPidRatePitchDerivative.init(
                    PID.PID_RATE_PITCH_DERI_DENOM,
                    PID.PID_RATE_PITCH_DERI_MAX,
                    PID.PID_RATE_PITCH_DERI_STEP,
                    PID.PID_RATE_PITCH_DERI_DFS,
                    ma.getString(R.string.PID_NAME_RPD),
                    "PitchRatePID", "Kd");
        }
        mPidTexts.add(txtPidRatePitchDerivative);

        PidTextView txtPidRateYawDerivative =
                (PidTextView) ma.findViewById(R.id.txtRateYawDerivative);
        if (txtPidRateYawDerivative != null) {
            txtPidRateYawDerivative.init(
                    PID.PID_RATE_YAW_DERI_DENOM,
                    PID.PID_RATE_YAW_DERI_MAX,
                    PID.PID_RATE_YAW_DERI_STEP,
                    PID.PID_RATE_YAW_DERI_DFS,
                    ma.getString(R.string.PID_NAME_RPD),
                    "YawRatePID", "Kd");
        }
        mPidTexts.add(txtPidRateYawDerivative);

        PidTextView txtPidRollProportional =
                (PidTextView) ma.findViewById(R.id.txtAttitudeRollProportional);
        if (txtPidRollProportional != null) {
            txtPidRollProportional.init(
                    PID.PID_ROLL_PROP_DENOM,
                    PID.PID_ROLL_PROP_MAX,
                    PID.PID_ROLL_PROP_STEP,
                    PID.PID_ROLL_PROP_DFS,
                    ma.getString(R.string.PID_NAME_ARP),
                    "RollPI", "Kp");
        }
        mPidTexts.add(txtPidRollProportional);

        PidTextView txtPidPitchProportional =
                (PidTextView) ma.findViewById(R.id.txtAttitudePitchProportional);
        if (txtPidPitchProportional != null) {
            txtPidPitchProportional.init(
                    PID.PID_PITCH_PROP_DENOM,
                    PID.PID_PITCH_PROP_MAX,
                    PID.PID_PITCH_PROP_STEP,
                    PID.PID_PITCH_PROP_DFS,
                    ma.getString(R.string.PID_NAME_APP),
                    "PitchPI", "Kp");
        }
        mPidTexts.add(txtPidPitchProportional);

        PidTextView txtPidYawProportional =
                (PidTextView) ma.findViewById(R.id.txtAttitudeYawProportional);
        if (txtPidYawProportional != null) {
            txtPidYawProportional.init(
                    PID.PID_YAW_PROP_DENOM,
                    PID.PID_YAW_PROP_MAX,
                    PID.PID_YAW_PROP_STEP,
                    PID.PID_YAW_PROP_DFS,
                    ma.getString(R.string.PID_NAME_AYP),
                    "YawPI", "Kp");
        }
        mPidTexts.add(txtPidYawProportional);

        PidTextView txtPidRollIntegral =
                (PidTextView) ma.findViewById(R.id.txtAttitudeRollIntegral);
        if (txtPidRollIntegral != null) {
            txtPidRollIntegral.init(
                    PID.PID_ROLL_INTE_DENOM,
                    PID.PID_ROLL_INTE_MAX,
                    PID.PID_ROLL_INTE_STEP,
                    PID.PID_ROLL_INTE_DFS,
                    ma.getString(R.string.PID_NAME_ARI),
                    "RollPI", "Ki");
        }
        mPidTexts.add(txtPidRollIntegral);

        PidTextView txtPidPitchIntegral =
                (PidTextView) ma.findViewById(R.id.txtAttitudePitchIntegral);
        if (txtPidPitchIntegral != null) {
            txtPidPitchIntegral.init(
                    PID.PID_PITCH_INTE_DENOM,
                    PID.PID_PITCH_INTE_MAX,
                    PID.PID_PITCH_INTE_STEP,
                    PID.PID_PITCH_INTE_DFS,
                    ma.getString(R.string.PID_NAME_API),
                    "PitchPI", "Ki");
        }
        mPidTexts.add(txtPidPitchIntegral);

        PidTextView txtPidYawIntegral =
                (PidTextView) ma.findViewById(R.id.txtAttitudeYawIntegral);
        if (txtPidYawIntegral != null) {
            txtPidYawIntegral.init(
                    PID.PID_YAW_INTE_DENOM,
                    PID.PID_YAW_INTE_MAX,
                    PID.PID_YAW_INTE_STEP,
                    PID.PID_YAW_INTE_DFS,
                    ma.getString(R.string.PID_NAME_AYI),
                    "YawPI", "Ki");
        }
        mPidTexts.add(txtPidYawIntegral);

        for (PidTextView ptv : mPidTexts) {
            ptv.setOnClickListener(this);
        }
    }

    @Override
    public int getID() {
        return ViewController.VIEW_PID;
    }

    @Override
    public void enter(int view) {
        super.enter(view);

        try {
            final MainActivity ma = getMainActivity();
            final View lloOuterPid = findViewById(R.id.lloOuterPid);
            final View lloInnerPid = findViewById(R.id.lloInnerPid);

            if (lloInnerPid != null && lloOuterPid != null) {
                if (SettingsHelper.mColorfulPid) {
                    CompatHelper.setBackground(lloOuterPid, ma.getApplicationContext(),
                            R.drawable.border_top_yellow);
                    CompatHelper.setBackground(lloInnerPid, ma.getApplicationContext(),
                            R.drawable.border_top_blue);
                } else {
                    CompatHelper.setBackground(lloOuterPid, ma.getApplicationContext(),
                            R.drawable.border_top);
                    CompatHelper.setBackground(lloInnerPid, ma.getApplicationContext(),
                            R.drawable.border_top);
                }
            }
        } catch (NullPointerException e1) {
            VisualLog.d("MainActivity", "VIEW_PID", e1);
        }
    }

    @Override
    public void update() {
        super.update();
        MainActivity ma = getMainActivity();
        String fmode =
                getData("ManualControlCommand", "FlightModeSwitchPosition")
                        .toString();
        String bank =
                getData("StabilizationSettings", "FlightModeMap", fmode)
                        .toString();

        mCurrentStabilizationBank = "StabilizationSettings" + bank;

        switch (mCurrentStabilizationBank) {
            case "StabilizationSettingsBank1":
                imgPidBank.setImageDrawable(ContextCompat.getDrawable(
                        ma.getApplicationContext(),
                        R.drawable.ic_filter_1_128dp));
                ma.mFcDevice.requestObject("StabilizationSettingsBank1");
                break;
            case "StabilizationSettingsBank2":
                imgPidBank.setImageDrawable(ContextCompat.getDrawable(
                        ma.getApplicationContext(),
                        R.drawable.ic_filter_2_128dp));
                ma.mFcDevice.requestObject("StabilizationSettingsBank2");
                break;
            case "StabilizationSettingsBank3":
                imgPidBank.setImageDrawable(ContextCompat.getDrawable(
                        ma.getApplicationContext(),
                        R.drawable.ic_filter_3_128dp));
                ma.mFcDevice.requestObject("StabilizationSettingsBank3");
                break;
            default:
                imgPidBank.setImageDrawable(ContextCompat.getDrawable(
                        ma.getApplicationContext(),
                        R.drawable.ic_filter_none_128dp));
                break;
        }

        for (PidTextView ptv : mPidTexts) {
            String data = ptv.getDecimalString(
                    toFloat(getData(mCurrentStabilizationBank,
                            ptv.getField(), ptv.getElement())));
            ptv.setText(data);
        }
    }

    @Override
    public void onToolbarLocalSettingsClick(View v) {
        {
            final String[] items = {getString(R.string.COLORFUL_VIEW)};
            final boolean[] checkedItems = {true};

            checkedItems[0] = SettingsHelper.mColorfulPid;


            AlertDialog dialog = new AlertDialog.Builder(getMainActivity())
                    .setTitle(R.string.SETTINGS)
                    .setMultiChoiceItems(items, checkedItems,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int indexSelected,
                                                    boolean isChecked) {

                                    SettingsHelper.mColorfulPid = isChecked;

                                }
                            }).setPositiveButton(R.string.CLOSE_BUTTON,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    enter(VIEW_PID);
                                }
                            }).create();
            dialog.show();
        }
    }

    private void onPidSaveClick() {
        MainActivity ma = getMainActivity();
        if (ma.mFcDevice != null && ma.mFcDevice.isConnected()) {
            ma.mFcDevice.savePersistent(mCurrentStabilizationBank);
            SingleToast.show(ma, getString(R.string.SAVED_PERSISTENT)
                    + getString(R.string.CHECK_PID_WARNING), Toast.LENGTH_SHORT);
        } else {
            SingleToast.show(ma, R.string.SEND_FAILED, Toast.LENGTH_SHORT);
        }
    }

    private void onPidUploadClick() {
        VisualLog.d("DBG", "" + mCurrentStabilizationBank);
        MainActivity ma = getMainActivity();
        if (ma.mFcDevice != null && ma.mFcDevice.isConnected()) {
            UAVTalkObjectTree oTree = ma.mFcDevice.getObjectTree();
            if (oTree != null) {
                oTree.getObjectFromName(mCurrentStabilizationBank).setWriteBlocked(true);

                for (PidTextView ptv : mPidTexts) {
                    try {
                        float f = H.stringToFloat(ptv.getText().toString());

                        byte[] buffer = H.reverse4bytes(H.floatToByteArray(f));

                        UAVTalkDeviceHelper.updateSettingsObject(
                                oTree, mCurrentStabilizationBank, 0, ptv.getField(),
                                ptv.getElement(), buffer);
                    } catch (NumberFormatException e) {
                        VisualLog.e("ViewControllerPid",
                                "Error parsing float: " + ptv.getField() + " " + ptv.getElement() +
                                        " " + ptv.getText().toString());
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

    private void onPidDownloadClick() {
        MainActivity ma = getMainActivity();
        if (ma.mFcDevice != null && ma.mFcDevice.isConnected()) {

            for (PidTextView ptv : mPidTexts) {
                ptv.allowUpdate();
            }

            SingleToast.show(ma, getString(R.string.PID_LOADING)
                    + getString(R.string.CHECK_PID_WARNING), Toast.LENGTH_SHORT);
        } else {
            SingleToast.show(ma, R.string.SEND_FAILED, Toast.LENGTH_SHORT);
        }
    }

    private void onPidGridNumberClick(View v) {
        PidTextView p = (PidTextView) v;
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
                .withFieldType(UAVTalkXMLObject.FIELDTYPE_FLOAT32)
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgPidDownload: {
                onPidDownloadClick();
            }
            break;
            case R.id.imgPidUpload: {
                onPidUploadClick();
            }
            break;
            case R.id.imgPidSave: {
                onPidSaveClick();
            }
            break;
        }

        if (v.getClass().equals(PidTextView.class)) {
            onPidGridNumberClick(v);
        }

    }
}
