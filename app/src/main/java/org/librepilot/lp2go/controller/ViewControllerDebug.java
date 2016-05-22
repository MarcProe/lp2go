package org.librepilot.lp2go.controller;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;

public class ViewControllerDebug extends ViewController implements View.OnClickListener {

    private ImageView imgDebugLogShare;
    private TextView txtDebugLog;

    public ViewControllerDebug(MainActivity activity, int title, int localSettingsVisible,
                               int flightSettingsVisible) {
        super(activity, title, localSettingsVisible, flightSettingsVisible);
        activity.mViews.put(VIEW_DEBUG,
                activity.getLayoutInflater().inflate(R.layout.activity_debug, null));
        activity.setContentView(activity.mViews.get(VIEW_DEBUG)); //Logs

        txtDebugLog = (TextView) findViewById(R.id.txtDebugLog);
        VisualLog.setDebugLogTextView(txtDebugLog);

        imgDebugLogShare = (ImageView) findViewById(R.id.imgDebugLogShare);
        if (imgDebugLogShare != null) {
            imgDebugLogShare.setOnClickListener(this);
        }
    }

    private void onDebugLogShare() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType(getString(R.string.MIME_APPLICATION_TEXT));
        share.putExtra(Intent.EXTRA_TEXT, txtDebugLog.getText().toString());
        getMainActivity()
                .startActivity(Intent.createChooser(share, getString(R.string.SHARE_LOG_TITLE)));
    }

    @Override
    public void onClick(View v) {
        onDebugLogShare();
    }
}
