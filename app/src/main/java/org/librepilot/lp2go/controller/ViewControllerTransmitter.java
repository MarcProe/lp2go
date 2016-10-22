/*
 * @file   ViewControllerManualControl.java
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

import android.widget.LinearLayout;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.helper.H;
import org.librepilot.lp2go.ui.manualcontrol.MultiTouchStickPainterView;

public class ViewControllerTransmitter extends ViewController implements MultiTouchStickPainterView.StickListener {

    private MultiTouchStickPainterView mStickView;
    private float mOlx, mOrx, mOly, mOry = .0f;

    public ViewControllerTransmitter(MainActivity activity, int title, int icon, int localSettingsVisible, int flightSettingsVisible) {
        super(activity, title, icon, localSettingsVisible, flightSettingsVisible);
        final MainActivity ma = getMainActivity();
        ma.mViews.put(ViewController.VIEW_TRANSMITTER, ma.getLayoutInflater().inflate(R.layout.activity_transmitter, null));
        ma.setContentView(ma.mViews.get(ViewController.VIEW_TRANSMITTER));
    }

    @Override
    public int getID() {
        return ViewController.VIEW_TRANSMITTER;
    }

    @Override
    public void leave() {
        super.leave();
        ((LinearLayout) findViewById(R.id.view_transmitter_parent)).removeView(mStickView);
    }

    @Override
    public void update() {

        super.update();
        onLeftChange(mOlx, mOly);
        onRightChange(mOrx, mOry);
    }

    @Override
    public void enter(int view) {
        super.enter(view);
        mStickView = new MultiTouchStickPainterView(getMainActivity().getApplicationContext(), null, this);
        ((LinearLayout) findViewById(R.id.view_transmitter_parent)).addView(mStickView);
    }

    @Override
    public void onLeftChange(float x, float y) {
        //VisualLog.d("Left", ""+x+" " +y);

        int yaw = Math.round(x + 100) * 5 + 1000;
        int thrust = Math.round(y + 100) * 5 + 1000;

        setData("GCSReceiver", "Channel", 0, H.toUint16(yaw));
        setData("GCSReceiver", "Channel", 1, H.toUint16(thrust));
        sendData("GCSReceiver");

        mOlx = x;
        mOly = y;
    }

    @Override
    public void onRightChange(float x, float y) {
        //VisualLog.d("Right", ""+x+" " +y);

        int roll = Math.round(x + 100) * 5 + 1000;
        int pitch = Math.round(y + 100) * 5 + 1000;

        setData("GCSReceiver", "Channel", 2, H.toUint16(roll));
        setData("GCSReceiver", "Channel", 3, H.toUint16(pitch));
        sendData("GCSReceiver");

        mOrx = x;
        mOry = y;
    }
}
