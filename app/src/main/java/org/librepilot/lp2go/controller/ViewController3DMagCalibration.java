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

import android.view.View;
import android.widget.TextView;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.uavtalk.UAVTalkObject;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectListener;
import org.librepilot.lp2go.ui.opengl.OpenGl3DMagCalibrationView;

public class ViewController3DMagCalibration extends ViewController implements
        UAVTalkObjectListener, View.OnClickListener {

    private OpenGl3DMagCalibrationView glv3DMagCalibration;

    public ViewController3DMagCalibration(MainActivity activity, int title, int icon,
                                          int localSettingsVisible, int flightSettingsVisible) {
        super(activity, title, icon, localSettingsVisible, flightSettingsVisible);
        activity.mViews.put(VIEW_3DMAG,
                activity.getLayoutInflater().inflate(R.layout.activity_3dmagcalibration, null));
        activity.setContentView(activity.mViews.get(VIEW_3DMAG));

        this.glv3DMagCalibration =
                (OpenGl3DMagCalibrationView) activity.findViewById(R.id.glv_3d_mag_calibration);

        activity.findViewById(R.id.startFit).setOnClickListener(this);

    }

    @Override
    public int getID() {
        return ViewController.VIEW_3DMAG;
    }

    @Override
    public void enter(int view) {
        super.enter(view);

    }

    @Override
    public void leave() {
        super.leave();
        try {
            getMainActivity().mFcDevice.getObjectTree().removeListener("AttitudeState");
        } catch (NullPointerException ignored) {

        }
    }

    @Override
    public void init() {
        super.init();
        //glv3DMagCalibration.setRenderer(new OpenGLRenderer());

    }

    @Override
    public void update() {
        super.update();
        if (getMainActivity().mFcDevice.getObjectTree().getListener("AttitudeState") == null) {
            try {
                try {
                    getMainActivity().mFcDevice.getObjectTree().setListener("AttitudeState", this);
                } catch (IllegalStateException e) {
                    getMainActivity().mFcDevice.getObjectTree().removeListener("AttitudeState");
                    getMainActivity().mFcDevice.getObjectTree().setListener("AttitudeState", this);
                }
            } catch (NullPointerException ignored) {

            }
        }

    }

    private int addSample() {
        try {
            final float magx = Float.parseFloat(getData("MagState", "x").toString());
            final float magy = Float.parseFloat(getData("MagState", "y").toString());
            final float magz = Float.parseFloat(getData("MagState", "z").toString());
            return glv3DMagCalibration.addSample(magx, magy, magz);

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onObjectUpdate(UAVTalkObject o) {
        final float pitch = Float.parseFloat(getData("AttitudeState", "Pitch").toString());
        final float roll = Float.parseFloat(getData("AttitudeState", "Roll").toString());
        final float yaw = -Float.parseFloat(getData("AttitudeState", "Yaw").toString());

        final float magx = Float.parseFloat(getData("MagState", "x").toString());
        final float magy = Float.parseFloat(getData("MagState", "y").toString());
        final float magz = Float.parseFloat(getData("MagState", "z").toString());
        //final float q4 = Float.parseFloat(getData("AttitudeState", "q1").toString());

        final MainActivity m = getMainActivity();

        final int samples = addSample();        //saving the current sample

        try {
            glv3DMagCalibration.setRoll(roll);
            glv3DMagCalibration.setPitch(pitch);
            glv3DMagCalibration.setYaw(yaw);

            m.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    try {
                        ((TextView) m.findViewById(R.id.faceName))
                                .setText("" + glv3DMagCalibration.PitchRollToString(pitch, roll));

                        ((TextView) m.findViewById(R.id.magx)).setText("" + Math.floor(magx));
                        ((TextView) m.findViewById(R.id.magy)).setText("" + Math.floor(magy));
                        ((TextView) m.findViewById(R.id.magz)).setText("" + Math.floor(magz));

                        ((TextView) m.findViewById(R.id.txt3dpitch))
                                .setText("" + Math.floor(pitch));
                        ((TextView) m.findViewById(R.id.txt3droll)).setText("" + Math.floor(roll));
                        ((TextView) m.findViewById(R.id.txt3dyaw)).setText("" + Math.floor(yaw));

                        ((TextView) m.findViewById(R.id.collectedSamples)).setText("" + samples);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        String result = this.glv3DMagCalibration.fit();
        VisualLog.d("FIT", result);
    }
}
