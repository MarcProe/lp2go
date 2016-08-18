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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.helper.SettingsHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkMissingObjectException;
import org.librepilot.lp2go.uavtalk.device.FcDevice;
import org.librepilot.lp2go.uavtalk.device.FcLogfileDevice;
import org.librepilot.lp2go.ui.SingleToast;
import org.librepilot.lp2go.ui.alertdialog.EnumInputAlertDialog;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewControllerLogs extends ViewController implements
        View.OnClickListener, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, FcDevice.GuiEventListener {

    private ImageView imgLogShare;
    private ImageView imgLogStart;
    private ImageView imgLogStop;
    private ImageButton imgLogRepForward;
    private ImageButton imgLogRepPlay;
    private ImageButton imgLogRepStop;
    private ImageButton imgLogRepPause;
    private List<String> mFileList;
    private TextView txtLogDuration;
    private TextView txtLogFilename;
    private TextView txtLogObjects;
    private TextView txtLogSize;
    private AtomicInteger mLogReplayState;

    private ListView mLogListView;
    private ArrayAdapter mLogListAdapter;
    private Integer mCurrentLogListPos = null;

    public ViewControllerLogs(MainActivity activity, int title, int icon, int localSettingsVisible,
                              int flightSettingsVisible) {
        super(activity, title, icon, localSettingsVisible, flightSettingsVisible);

        mFileList = new ArrayList<>();

        mLogReplayState = new AtomicInteger(FcDevice.GEL_STOPPED);

        init();

    }

    @Override
    public void init() {
        super.init();

        final MainActivity ma = getMainActivity();

        // this will set the layout according to device orientation
        ma.mViews.put(VIEW_LOGS, ma.getLayoutInflater().inflate(R.layout.activity_logs, null));
        ma.setContentView(ma.mViews.get(VIEW_LOGS));

        txtLogFilename = (TextView) ma.findViewById(R.id.txtLogFilename);
        txtLogSize = (TextView) ma.findViewById(R.id.txtLogSize);
        txtLogObjects = (TextView) ma.findViewById(R.id.txtLogObjects);
        txtLogDuration = (TextView) ma.findViewById(R.id.txtLogDuration);

        imgLogStart = (ImageButton) findViewById(R.id.imgLogStart);
        imgLogStop = (ImageButton) findViewById(R.id.imgLogStop);
        imgLogShare = (ImageButton) findViewById(R.id.imgLogShare);

        imgLogRepForward = (ImageButton) findViewById(R.id.imgLogRepForward);
        imgLogRepPlay = (ImageButton) findViewById(R.id.imgLogRepPlay);
        imgLogRepPause = (ImageButton) findViewById(R.id.imgLogRepPause);
        imgLogRepStop = (ImageButton) findViewById(R.id.imgLogRepStop);

        imgLogStart.setOnClickListener(this);
        imgLogStop.setOnClickListener(this);
        imgLogShare.setOnClickListener(this);

        imgLogRepForward.setOnClickListener(this);
        imgLogRepPlay.setOnClickListener(this);
        imgLogRepStop.setOnClickListener(this);
        imgLogRepPause.setOnClickListener(this);

        mLogListView = (ListView) findViewById(R.id.lsvLogList);
        mLogListAdapter = new ArrayAdapter(getMainActivity(), android.R.layout.simple_list_item_1, mFileList);
        mLogListView.setAdapter(mLogListAdapter);
        mLogListView.setClickable(true);
        mLogListView.setLongClickable(true);
        mLogListView.setOnItemClickListener(this);
        mLogListView.setOnItemLongClickListener(this);

        loadFileList(true);
    }

    private void loadFileList(boolean notify) {
        mFileList.clear();
        File f = getMainActivity().getFilesDir();
        File file[] = f.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".opl");
            }
        });

        for (File aFile : file) {
            mFileList.add(aFile.getName() + " (" + String.format("%.1f", (float) aFile.length() / 1024) + " KB) ");

            VisualLog.d("FILE", aFile.getName());
        }
        if (mFileList.size() == 0) {
            mFileList.add("<no logs found>");
        }

        Collections.sort(mFileList);
        Collections.reverse(mFileList);

        if (notify) {
            mLogListAdapter.notifyDataSetChanged();
        }

        //clear current choice...
        mLogListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
        mLogListView.setAdapter(mLogListAdapter);
        mLogListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

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

        switch (mLogReplayState.get()) {
            case FcDevice.GEL_STOPPED: {
                getMainActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        SingleToast.show(getMainActivity(), "Log Replay done!", Toast.LENGTH_LONG);
                    }
                });
                break;
            }
            case FcDevice.GEL_PAUSED: {
                break;
            }
            case FcDevice.GEL_RUNNING: {
                break;
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
            loadFileList(true);
        } catch (NullPointerException e) {
            VisualLog.i("INFO", "Device is null");
        }
    }

    private String getFilename(String string) {
        return string == null ? null : string.substring(0, string.indexOf(" "));
    }

    private void onLogShare(View v) {
        try {
            getMainActivity().mFcDevice.setLogging(false);
        } catch (NullPointerException e) {
            return;
        }
        Intent share = new Intent(Intent.ACTION_SEND);

        share.setType(getString(R.string.MIME_APPLICATION_OCTETSTREAM));

        String filename;
        if (mCurrentLogListPos != null) {
            filename = getFilename((String) mLogListView.getItemAtPosition(mCurrentLogListPos));
            VisualLog.d("LOOOOG", "" + mCurrentLogListPos);
            if (filename == null) {
                SingleToast.show(getMainActivity(), "Please select a log from the list", Toast.LENGTH_LONG);
                return;
            }
            VisualLog.d("LOOuOG", filename);
        } else {
            SingleToast.show(getMainActivity(), "Please select a log from the list", Toast.LENGTH_LONG);
            return;
        }

        File logPath = new File(getMainActivity().getFilesDir(), "");
        File logFile = new File(logPath, filename);
        Uri contentUri = FileProvider.getUriForFile(getMainActivity(),
                getString(R.string.APP_ID) + ".logfileprovider", logFile);

        share.putExtra(Intent.EXTRA_STREAM, contentUri);
        getMainActivity().startActivity(Intent.createChooser(share, getString(R.string.SHARE_LOG_TITLE)));
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
            case R.id.imgLogRepPlay: {
                onReplayStart(v);
                break;
            }
            case R.id.imgLogRepForward: {
                onReplayForward(v);
                break;
            }
            case R.id.imgLogRepPause: {
                onReplayPause(v);
                break;
            }
            case R.id.imgLogRepStop: {
                onReplayStop(v);
                break;
            }
        }
    }

    private void onReplayStop(View v) {
        if (getMainActivity().getFcDevice() != null &&
                SettingsHelper.mSerialModeUsed == MainActivity.SERIAL_LOG_FILE) {
            (getMainActivity().getFcDevice()).stop();
        } else {
            SingleToast.show(getMainActivity(), "Replay not running", Toast.LENGTH_SHORT);
        }
    }

    private boolean isPaused() {
        return SettingsHelper.mSerialModeUsed == MainActivity.SERIAL_LOG_FILE &&
                getMainActivity().getFcDevice() != null &&
                ((FcLogfileDevice) getMainActivity().getFcDevice()).isPaused();
    }

    private void togglePaused() {
        if (SettingsHelper.mSerialModeUsed == MainActivity.SERIAL_LOG_FILE &&
                getMainActivity().getFcDevice() != null) {
            ((FcLogfileDevice) getMainActivity().getFcDevice()).setPaused(!isPaused());
        } else {
            SingleToast.show(getMainActivity(), "Replay not running", Toast.LENGTH_SHORT);
        }
    }

    private void onReplayPause(View v) {
        togglePaused();
        //TODO: make this visible on the button
    }

    private void onReplayForward(View v) {
        if (getMainActivity().getFcDevice() != null &&
                SettingsHelper.mSerialModeUsed == MainActivity.SERIAL_LOG_FILE) {
            SingleToast.show(getMainActivity(),
                    String.format(getMainActivity().getString(R.string.SKIPPING_OBJECTS),
                            SettingsHelper.mLogReplaySkipObjects), Toast.LENGTH_SHORT);
            ((FcLogfileDevice) getMainActivity().getFcDevice()).setSkip(SettingsHelper.mLogReplaySkipObjects);
        } else {
            SingleToast.show(getMainActivity(), "Replay not running", Toast.LENGTH_SHORT);
        }
    }

    private void onReplayStart(View v) {
        //if replay is paused, resume
        if (isPaused()) {
            togglePaused();
        } else {
            //else, start replay
            if (mCurrentLogListPos != null) {
                String filename = getFilename((String) mLogListView.getItemAtPosition(mCurrentLogListPos));
                getMainActivity().getConnectionThread().setReplayLogFile(filename);
                getMainActivity().getConnectionThread().setGuiEventListener(this);
                SettingsHelper.mSerialModeUsed = MainActivity.SERIAL_LOG_FILE;
                getMainActivity().reconnect();
                //TODO: make this visible on the button
            } else {
                SingleToast.show(getMainActivity(), "Please select a logfile", Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void leave() {
        super.leave();
        mFileList.clear();
        mLogListAdapter.notifyDataSetChanged();
    }

    @Override
    public void enter(int view) {
        super.enter(view);
        loadFileList(true);
    }

    private void onTelemetryTimestampsClick() {
        final MainActivity ma = getMainActivity();
        if (ma.mFcDevice != null) {
            String armingState;
            try {
                armingState =
                        ma.mFcDevice.getObjectTree().getData("FlightStatus", "Armed").toString();
            } catch (UAVTalkMissingObjectException | NullPointerException e) {
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
        VisualLog.d("CLICK", mFileList.get(pos));
        view.setSelected(true);
        mCurrentLogListPos = pos;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
        VisualLog.d("LONGCLICK", mFileList.get(pos));
        final int j = pos;

        AlertDialog dialog = new AlertDialog.Builder(getMainActivity())
                .setTitle("Delete File?")
                .setMessage("Are you sure to delete the log " + mFileList.get(pos) + "?")
                .setPositiveButton(R.string.OK_BUTTON, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final File logFile = new File(getMainActivity().getFilesDir(),
                                getFilename(mFileList.get(j)));
                        final boolean del = logFile.delete();
                        SingleToast.show(getMainActivity(), del ? "File deleted" : "Error deleting File",
                                Toast.LENGTH_LONG);
                        mCurrentLogListPos = null;
                        loadFileList(true);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.CANCEL_BUTTON, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setNeutralButton(R.string.DELETE_ALL_BUTTON, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        boolean del = true;
                        for (int i = 0; i < mFileList.size(); i++) {
                            final File logFile = new File(getMainActivity().getFilesDir(),
                                    getFilename(mFileList.get(i)));
                            del = del & logFile.delete();
                        }
                        SingleToast.show(getMainActivity(),
                                del ? "Files deleted" : "Error deleting Files", Toast.LENGTH_LONG);
                        loadFileList(true);
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();

        return true;
    }

    @Override
    public void reportState(int i) {
        mLogReplayState.set(i);
    }
}
