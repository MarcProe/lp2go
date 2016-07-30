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
import android.widget.ImageView;
import android.widget.TextView;

import org.librepilot.lp2go.H;
import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.helper.ellipsoidFit.FitPoints;
import org.librepilot.lp2go.uavtalk.UAVTalkMetaData;
import org.librepilot.lp2go.uavtalk.UAVTalkObject;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectListener;
import org.librepilot.lp2go.uavtalk.device.FcDevice;
import org.librepilot.lp2go.ui.SingleToast;
import org.librepilot.lp2go.ui.opengl.OpenGl3DMagCalibrationView;

public class ViewController3DMagCalibration extends ViewController implements
        UAVTalkObjectListener, View.OnClickListener {

    ImageView imgCompass;
    private OpenGl3DMagCalibrationView glv3DMagCalibration;
    private float be_0;
    private float be_1;
    private float be_2;
    private float mag_bias_x;
    private float mag_bias_y;
    private float mag_bias_z;
    private float mag_transform_r0c0;
    private float mag_transform_r1c1;
    private float mag_transform_r2c2;
    private boolean mCalibrationRunning = false;

    public ViewController3DMagCalibration(MainActivity activity, int title, int icon,
                                          int localSettingsVisible, int flightSettingsVisible) {
        super(activity, title, icon, localSettingsVisible, flightSettingsVisible);
        activity.mViews.put(VIEW_3DMAG,
                activity.getLayoutInflater().inflate(R.layout.activity_3dmagcalibration, null));
        activity.setContentView(activity.mViews.get(VIEW_3DMAG));

        this.glv3DMagCalibration =
                (OpenGl3DMagCalibrationView) activity.findViewById(R.id.glv_3d_mag_calibration);

        imgCompass = (ImageView) findViewById(R.id.imgStartStopCalibration);

        activity.findViewById(R.id.imgStartStopCalibration).setOnClickListener(this);

    }

    private boolean isCalibrationRunning() {
        return mCalibrationRunning;
    }

    private void resetCalibrationOnFc() {
        final String O = "RevoCalibration";
        final String B = "mag_bias";
        final String X = "X";
        final String Y = "Y";
        final String Z = "Z";
        final String T = "mag_transform";
        final String R0 = "r0c0";
        final String R1 = "r1c1";
        final String R2 = "r2c2";

        getMainActivity().getFcDevice().sendSettingsObject(O, 0, B, X, H.floatToByteArrayRev(.0f));
        getMainActivity().getFcDevice().sendSettingsObject(O, 0, B, Y, H.floatToByteArrayRev(.0f));
        getMainActivity().getFcDevice().sendSettingsObject(O, 0, B, Z, H.floatToByteArrayRev(.0f));

        getMainActivity().getFcDevice().sendSettingsObject(O, 0, T, R0, H.floatToByteArrayRev(1f));
        getMainActivity().getFcDevice().sendSettingsObject(O, 0, T, R1, H.floatToByteArrayRev(1f));
        getMainActivity().getFcDevice().sendSettingsObject(O, 0, T, R2, H.floatToByteArrayRev(1f));
    }

    private boolean setMetaUpdateRate(String objectname, int updaterate) {
        try {
            UAVTalkMetaData o = getMetaData(objectname);
            if (o == null) {
                requestMetaData(objectname);
                return false;
            } else if (o.getFlightTelemetryUpdatePeriod() != updaterate) {
                o.setFlightTelemetryUpdatePeriod(updaterate);
                sendMetaObject(o);
            }

        } catch (Exception e) {
            VisualLog.e("TTT", e.getMessage(), e);
        }
        return true;
    }

    private boolean startCalibration() {
        imgCompass.setRotation(90);

        //reset calibration on FC
        resetCalibrationOnFc();

        //increase magstate update rate
        final boolean setUR = setMetaUpdateRate("MagState", 300);
        if (!setUR) {
            return false;
        }

        //hook on to object listener and start collecting samples
        try {
            try {
                getMainActivity().mFcDevice.getObjectTree().setListener("AttitudeState", this);
            } catch (IllegalStateException e) {
                getMainActivity().mFcDevice.getObjectTree().removeListener("AttitudeState");
                getMainActivity().mFcDevice.getObjectTree().setListener("AttitudeState", this);
            }
        } catch (NullPointerException ignored) {

        }

        mCalibrationRunning = true;
        return true;
    }

    private void stopCalibration() {
        imgCompass.setRotation(0);
        mCalibrationRunning = false;

        //remove listener
        getMainActivity().mFcDevice.getObjectTree().removeListener("MagState");

        //reset magstate update rate
        final boolean setUR = setMetaUpdateRate("MagState", 1000);

        //make fit and upload calibration to FC
        fitAndUpload();

    }

    private void toggleCalibration() {
        if (isCalibrationRunning()) {
            stopCalibration();
        } else {
            final boolean s = startCalibration();
            SingleToast.show(getMainActivity(), "Start: " + s);
        }
    }

    @Override
    public int getID() {
        return ViewController.VIEW_3DMAG;
    }

    @Override
    public void enter(int view) {
        super.enter(view);
        requestMetaData("MagState");
    }

    @Override
    public void leave() {
        super.leave();
        try {
            getMainActivity().mFcDevice.getObjectTree().removeListener("AttitudeState");
        } catch (NullPointerException ignored) {

        }

        try {
            UAVTalkMetaData o = getMetaData("MagState");
            o.setFlightTelemetryUpdatePeriod(1000);
            sendMetaObject(o);

        } catch (Exception e) {
            VisualLog.e("TTT", e.getMessage(), e);
        }
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void update() {
        super.update();

        //get needed objects
        if (be_0 == 0
                && getMainActivity().getFcDevice() != null
                && getMainActivity().getFcDevice().getObjectTree() != null) {

            be_0 = toFloat(getData("HomeLocation", "Be", 0));
            be_1 = toFloat(getData("HomeLocation", "Be", 1));
            be_2 = toFloat(getData("HomeLocation", "Be", 2));

            mag_bias_x = toFloat(getData("RevoCalibration", "mag_bias", "X"));
            mag_bias_y = toFloat(getData("RevoCalibration", "mag_bias", "Y"));
            mag_bias_z = toFloat(getData("RevoCalibration", "mag_bias", "Z"));

            mag_transform_r0c0 = toFloat(getData("RevoCalibration", "mag_transform", "r0c0"));
            mag_transform_r1c1 = toFloat(getData("RevoCalibration", "mag_transform", "r1c1"));
            mag_transform_r2c2 = toFloat(getData("RevoCalibration", "mag_transform", "r2c2"));

            VisualLog.d("TEST", "TEST");
        }

        final MainActivity m = getMainActivity();

        ((TextView) m.findViewById(R.id.txtBe0)).setText("" + be_0);
        ((TextView) m.findViewById(R.id.txtBe1)).setText("" + be_1);
        ((TextView) m.findViewById(R.id.txtBe2)).setText("" + be_2);

        ((TextView) m.findViewById(R.id.txtMagBiasX)).setText("" + mag_bias_x);
        ((TextView) m.findViewById(R.id.txtMagBiasY)).setText("" + mag_bias_y);
        ((TextView) m.findViewById(R.id.txtMagBiasZ)).setText("" + mag_bias_z);

        ((TextView) m.findViewById(R.id.txtR0c0)).setText("" + mag_transform_r0c0);
        ((TextView) m.findViewById(R.id.txtR1c1)).setText("" + mag_transform_r1c1);
        ((TextView) m.findViewById(R.id.txtR2c2)).setText("" + mag_transform_r2c2);

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

        getData("", "", true);

        final float magx = Float.parseFloat(getData("MagState", "x").toString());
        final float magy = Float.parseFloat(getData("MagState", "y").toString());
        final float magz = Float.parseFloat(getData("MagState", "z").toString());

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

    private void fitAndUpload() {
        FitPoints fp = glv3DMagCalibration.fit();
        String result = fp.toString();

        //get fit results
        float biasx = (float) fp.center.getEntry(0);
        float biasy = (float) fp.center.getEntry(1);
        float biasz = (float) fp.center.getEntry(2);

        final FcDevice fcDev = getMainActivity().getFcDevice();

        if (fcDev != null) {

            //send fix results to fx
            fcDev.sendSettingsObject("RevoCalibration", 0, "mag_bias", "X", H.floatToByteArrayRev(biasx));
            fcDev.sendSettingsObject("RevoCalibration", 0, "mag_bias", "Y", H.floatToByteArrayRev(biasy));
            fcDev.sendSettingsObject("RevoCalibration", 0, "mag_bias", "Z", H.floatToByteArrayRev(biasz));

            float beVecLen = (float) Math.sqrt(Math.pow(be_0, 2) + Math.pow(be_1, 2) + Math.pow(be_2, 2));

            float r0c0 = beVecLen / (float) fp.radii.getEntry(0);
            float r1c1 = beVecLen / (float) fp.radii.getEntry(1);
            float r2c2 = beVecLen / (float) fp.radii.getEntry(2);

            fcDev.sendSettingsObject("RevoCalibration", 0, "mag_transform", "r0c0", H.floatToByteArrayRev(r0c0));
            fcDev.sendSettingsObject("RevoCalibration", 0, "mag_transform", "r1c1", H.floatToByteArrayRev(r1c1));
            fcDev.sendSettingsObject("RevoCalibration", 0, "mag_transform", "r2c2", H.floatToByteArrayRev(r2c2));

            fcDev.savePersistent("RevoCalibration");

            VisualLog.d("FIT", "" + r0c0 + " " + r1c1 + " " + r2c2 + " " + biasx + " " + biasy + " " + biasz);
        }

        be_0 = 0; //resetting one value will re-receive all values

        VisualLog.d("FIT", result);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgStartStopCalibration: {
                toggleCalibration();
            }
        }
    }
}
