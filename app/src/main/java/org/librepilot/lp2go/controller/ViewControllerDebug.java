/*
 * @file   ViewControllerDebug.java
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

    public ViewControllerDebug(MainActivity activity, int title, int icon, int localSettingsVisible,
                               int flightSettingsVisible) {
        super(activity, title, icon, localSettingsVisible, flightSettingsVisible);
        activity.mViews.put(VIEW_DEBUG,
                activity.getLayoutInflater().inflate(R.layout.activity_debug, null));
        activity.setContentView(activity.mViews.get(VIEW_DEBUG)); //DebugLogs

        txtDebugLog = (TextView) findViewById(R.id.txtDebugLog);
        VisualLog.setDebugLogTextView(txtDebugLog);

        imgDebugLogShare = (ImageView) findViewById(R.id.imgDebugLogShare);
        if (imgDebugLogShare != null) {
            imgDebugLogShare.setOnClickListener(this);
        }
    }

    @Override
    public int getID() {
        return ViewController.VIEW_DEBUG;
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
