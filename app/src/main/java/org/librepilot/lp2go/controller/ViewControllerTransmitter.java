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
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.helper.H;
import org.librepilot.lp2go.ui.manualcontrol.MultiTouchStickPainterView;

public class ViewControllerTransmitter extends ViewController implements MultiTouchStickPainterView.StickListener {

    private MultiTouchStickPainterView mStickView;
    private float mOlx, mOrx, mOly, mOry = .0f;
    private short mThrust, mYaw, mRoll, mPitch = 1500;
    private SendThread mST;

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
        mST.running = false;
        mST = null;
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void enter(int view) {
        super.enter(view);
        mStickView = new MultiTouchStickPainterView(getMainActivity().getApplicationContext(), null, this);
        ((LinearLayout) findViewById(R.id.view_transmitter_parent)).addView(mStickView);
        mST = new SendThread();
        mST.start();
    }

    @Override
    public void onLeftChange(float x, float y) {

        int yaw = Math.round(x + 100) * 5 + 1000;
        int thrust = Math.round((y * (-1)) + 100) * 5 + 1000;

        VisualLog.d("Left", "" + yaw + " " + thrust);

        //setData("GCSReceiver", "Channel", 0, H.toUint16(yaw));
        //setData("GCSReceiver", "Channel", 1, H.toUint16(thrust));

        mYaw = H.toUint16(yaw);
        mThrust = H.toUint16(thrust);

        //sendData("GCSReceiver");

        mOlx = x;
        mOly = y;
    }

    @Override
    public void onRightChange(float x, float y) {

        int roll = Math.round(x + 100) * 5 + 1000;
        int pitch = Math.round(y + 100) * 5 + 1000;

        VisualLog.d("Right", "" + roll + " " + pitch);

        //setData("GCSReceiver", "Channel", 2, H.toUint16(roll));
        //setData("GCSReceiver", "Channel", 3, H.toUint16(pitch));

        mRoll = H.toUint16(roll);
        mPitch = H.toUint16(pitch);

        //sendData("GCSReceiver");

        mOrx = x;
        mOry = y;
    }

    @Override
    public void onChange(float lx, float ly, float rx, float ry) {
        onLeftChange(lx, ly);
        onRightChange(rx, ry);
    }

    private class SendThread extends Thread {
        public boolean running = true;

        @Override
        public void run() {
            while (running) {
                long before = System.currentTimeMillis();

                setData("GCSReceiver", "Channel", 0, mYaw);
                setData("GCSReceiver", "Channel", 1, mThrust);
                setData("GCSReceiver", "Channel", 2, mRoll);
                setData("GCSReceiver", "Channel", 3, mPitch);

                sendData("GCSReceiver");
                long diff = 20 - (System.currentTimeMillis() - before);
                long sl = diff < 0 ? 0 : diff;

                //VisualLog.d(""+sl);

                try {
                    Thread.sleep(sl);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }
    }
}
