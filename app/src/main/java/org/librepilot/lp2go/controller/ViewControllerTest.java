/*
 * @file   ViewControllerTest.java
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

import android.view.View;
import android.widget.TextView;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.uavtalk.UAVTalkObject;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectListener;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectTree;
import org.librepilot.lp2go.uavtalk.UAVTalkXMLObject;
import org.librepilot.lp2go.ui.ObjectTextView;
import org.librepilot.lp2go.ui.alertdialog.PidInputAlertDialog;


public class ViewControllerTest extends ViewController implements UAVTalkObjectListener, View.OnClickListener {

    private TextView txtTestRoll;
    private TextView txtTestPitch;
    private ObjectTextView txtBank1RollMax;

    public ViewControllerTest(MainActivity activity, int title, int icon, int localSettingsVisible, int flightSettingsVisible) {
        super(activity, title, icon, localSettingsVisible, flightSettingsVisible);
        final MainActivity ma = getMainActivity();
        ma.mViews.put(VIEW_TEST, ma.getLayoutInflater().inflate(R.layout.activity_test, null));
        ma.setContentView(ma.mViews.get(VIEW_TEST));

        txtTestRoll = (TextView) findViewById(R.id.txtTestRoll);
        txtTestPitch = (TextView) findViewById(R.id.txtTestPitch);
        txtBank1RollMax = (ObjectTextView) findViewById(R.id.txtTestBank1RollMax);
        findViewById(R.id.btnTestUpdateRollMax).setOnClickListener(this);
    }

    @Override
    public void leave() {
        super.leave();
        getMainActivity().getFcDevice().getObjectTree().removeListener("AttitudeState");
    }

    @Override
    public int getID() {
        return ViewController.VIEW_TEST;
    }

    @Override
    public void update() {
        super.update();
        final MainActivity ma = getMainActivity();
        final UAVTalkObjectTree t = ma.getFcDevice().getObjectTree();

        if (t != null && t.getListener("AttitudeState") == null) {
            ma.getFcDevice().getObjectTree().setListener("AttitudeState", this);
        }

        ma.mFcDevice.requestObject("StabilizationSettingsBank1");  // requesting a settings object from the FC
        if ("".equals(txtBank1RollMax.getText())) {
            txtBank1RollMax.allowUpdate();
            txtBank1RollMax.setText(getData("StabilizationSettingsBank1", "RollMax").toString()); //getting the data
        }


        txtTestPitch.setText(getData("AttitudeState", "Pitch").toString());  //getting the data of a state object
    }

    @Override
    public void onObjectUpdate(UAVTalkObject o) {
        //this is called from another thread, so we have to switch to the ui thread
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtTestRoll.setText(getData("AttitudeState", "Roll").toString());  //getting the data of a state object
            }
        });
    }

    @Override
    public void onClick(View view) {
        new PidInputAlertDialog(getMainActivity())
                .withStep(1)
                .withDenominator(1)
                .withDecimalFormat("0")
                .withPidTextView(txtBank1RollMax)
                .withValueMax(180)
                .withPresetText(getData("StabilizationSettingsBank1", "RollMax").toString())
                .withTitle("RollMax")
                .withLayout(R.layout.alert_dialog_pid_grid)
                .withUavTalkDevice(getMainActivity().mFcDevice)
                .withObject("StabilizationSettingsBank1")
                .withField("RollMax")
                .withElement("0")
                .withFieldType(UAVTalkXMLObject.FIELDTYPE_INT8)
                .show();
    }
}
