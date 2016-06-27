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
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.helper.SettingsHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkMissingObjectException;
import org.librepilot.lp2go.ui.SingleToast;
import org.librepilot.lp2go.ui.alertdialog.EnumInputAlertDialog;

import java.io.File;

public class ViewControllerLogs extends ViewController implements View.OnClickListener {

    private final ImageView imgLogShare;
    private final ImageView imgLogStart;
    private final ImageView imgLogStop;
    private TextView txtLogDuration;
    private TextView txtLogFilename;
    private TextView txtLogObjects;
    private TextView txtLogSize;

    public ViewControllerLogs(MainActivity activity, int title, int icon, int localSettingsVisible,
                              int flightSettingsVisible) {
        super(activity, title, icon, localSettingsVisible, flightSettingsVisible);
        activity.mViews
                .put(VIEW_LOGS, activity.getLayoutInflater().inflate(R.layout.activity_logs, null));
        activity.setContentView(activity.mViews.get(VIEW_LOGS)); //Logs

        txtLogFilename = (TextView) activity.findViewById(R.id.txtLogFilename);
        txtLogSize = (TextView) activity.findViewById(R.id.txtLogSize);
        txtLogObjects = (TextView) activity.findViewById(R.id.txtLogObjects);
        txtLogDuration = (TextView) activity.findViewById(R.id.txtLogDuration);

        imgLogStart = (ImageView) findViewById(R.id.imgLogStart);
        imgLogStop = (ImageView) findViewById(R.id.imgLogStop);
        imgLogShare = (ImageView) findViewById(R.id.imgLogShare);

        imgLogStart.setOnClickListener(this);
        imgLogStop.setOnClickListener(this);
        imgLogShare.setOnClickListener(this);
    }

    @Override
    public int getID() {
        return ViewController.VIEW_LOGS;
    }

    @Override
    public void update() {
        super.update();
        MainActivity ma = getMainActivity();
        if (ma.mFcDevice.isLogging()) {
            try {
                txtLogFilename.setText(ma.mFcDevice.getLogFileName());
                double lUAV = Math.round(ma.mFcDevice.getLogBytesLoggedUAV()
                        / 102.4) / 10.;
                double lOPL = Math.round(ma.mFcDevice.getLogBytesLoggedOPL()
                        / 102.4) / 10.;
                txtLogSize.setText(String.valueOf(lUAV)
                        + getString(R.string.TAB) + "("
                        + String.valueOf(lOPL) + ") KB");
                txtLogObjects.setText(
                        String.valueOf(ma.mFcDevice.getLogObjectsLogged()));
                txtLogDuration.setText(
                        String.valueOf((System.currentTimeMillis()
                                - ma.mFcDevice.getLogStartTimeStamp())
                                / 1000) + " s");
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onToolbarFlightSettingsClick(View v) {
        onTelemetryTimestampsClick();
    }

    @Override
    public void onToolbarLocalSettingsClick(View v) {
        {
            final String[] items =
                    {getString(R.string.LOG_RAW), getString(R.string.FC_TIMESTATMPS)};
            final boolean[] checkedItems = {false, false};

            checkedItems[0] = SettingsHelper.mLogAsRawUavTalk;
            checkedItems[1] = SettingsHelper.mUseTimestampsFromFc;


            AlertDialog dialog = new AlertDialog.Builder(getMainActivity())
                    .setTitle(R.string.SETTINGS)
                    .setMultiChoiceItems(items, checkedItems,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int indexSelected,
                                                    boolean isChecked) {
                                    switch (indexSelected) {
                                        case 0: {
                                            SettingsHelper.mLogAsRawUavTalk = isChecked;
                                            break;
                                        }
                                        case 1: {
                                            SettingsHelper.mUseTimestampsFromFc = isChecked;
                                            break;
                                        }
                                    }
                                    SettingsHelper.saveSettings(getMainActivity());
                                }
                            }).setPositiveButton(R.string.CLOSE_BUTTON,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            }).create();
            dialog.show();
        }
    }

    private void onLogStart(View v) {
        try {
            getMainActivity().mFcDevice.setLogging(true);
        } catch (NullPointerException e) {
            VisualLog.i("INFO", "Device is null");
        }
    }

    private void onLogStop(View v) {
        try {
            getMainActivity().mFcDevice.setLogging(false);
        } catch (NullPointerException e) {
            VisualLog.i("INFO", "Device is null");
        }
    }

    private void onLogShare(View v) {
        try {
            getMainActivity().mFcDevice.setLogging(false);
        } catch (NullPointerException e) {
            return;
        }
        Intent share = new Intent(Intent.ACTION_SEND);

        share.setType(getString(R.string.MIME_APPLICATION_OCTETSTREAM));

        File logPath = new File(getMainActivity().getFilesDir(), "");
        File logFile = new File(logPath, getMainActivity().mFcDevice.getLogFileName());
        Uri contentUri =
                FileProvider.getUriForFile(getMainActivity(),
                        getString(R.string.APP_ID) + ".logfileprovider", logFile);

        share.putExtra(Intent.EXTRA_STREAM, contentUri);
        getMainActivity()
                .startActivity(Intent.createChooser(share, getString(R.string.SHARE_LOG_TITLE)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgLogStart: {
                onLogStart(v);
                break;
            }
            case R.id.imgLogStop: {
                onLogStop(v);
                break;
            }
            case R.id.imgLogShare: {
                onLogShare(v);
                break;
            }
        }
    }

    private void onTelemetryTimestampsClick() {
        final MainActivity ma = getMainActivity();
        if (ma.mFcDevice != null) {
            String armingState;
            try {
                armingState =
                        ma.mFcDevice.getObjectTree().getData("FlightStatus", "Armed").toString();
            } catch (UAVTalkMissingObjectException e) {
                armingState = "";
                ma.mFcDevice.requestObject("HwSettings");
            }
            if (armingState.equals("Disarmed")) {
                new EnumInputAlertDialog(ma)
                        .withTitle("Telemetry Timestamps (reboot required)")
                        .withUavTalkDevice(ma.mFcDevice)
                        .withObject("HwSettings")
                        .withField("TelemetryTimestamps")
                        .show();
            } else {
                SingleToast.show(ma,
                        getString(R.string.CHANGE_SETTINGS_DISARMED) + " " + armingState,
                        Toast.LENGTH_LONG);
            }
        } else {
            SingleToast.show(ma, R.string.SEND_FAILED, Toast.LENGTH_SHORT);
        }
    }
}
