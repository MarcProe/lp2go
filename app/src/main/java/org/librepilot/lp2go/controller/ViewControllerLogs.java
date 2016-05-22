package org.librepilot.lp2go.controller;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;

import java.io.File;

public class ViewControllerLogs extends ViewController implements View.OnClickListener {

    private final ImageView imgLogShare;
    private final ImageView imgLogStart;
    private final ImageView imgLogStop;
    private TextView txtLogDuration;
    private TextView txtLogFilename;
    private TextView txtLogObjects;
    private TextView txtLogSize;

    public ViewControllerLogs(MainActivity activity, int title, int localSettingsVisible,
                              int flightSettingsVisible) {
        super(activity, title, localSettingsVisible, flightSettingsVisible);
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
}
