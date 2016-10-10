/*
 * @file   ViewController3DMagCal.java
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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.helper.H;
import org.librepilot.lp2go.helper.SettingsHelper;
import org.librepilot.lp2go.helper.ellipsoidfit.FitPoints;
import org.librepilot.lp2go.uavtalk.UAVTalkMetaData;
import org.librepilot.lp2go.uavtalk.UAVTalkObject;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectListener;
import org.librepilot.lp2go.uavtalk.device.FcDevice;
import org.librepilot.lp2go.ui.SingleToast;
import org.librepilot.lp2go.ui.opengl.OpenGl3DMagCalView;

import java.text.MessageFormat;
import java.util.Locale;

public class ViewController3DMagCal extends ViewController implements
        UAVTalkObjectListener, View.OnClickListener {

    public final static int SAMPLES = 10;
    private final TextView txtBe0;
    private final TextView txtBe1;
    private final TextView txtBe2;
    private final TextView txtMagBiasX;
    private final TextView txtMagBiasY;
    private final TextView txtMagBiasZ;
    private final TextView txtR0c0;
    private final TextView txtR1c1;
    private final TextView txtR2c2;
    private final TextView txtCurrentFace;
    private final TextView txtMagX;
    private final TextView txtMagY;
    private final TextView txtMagZ;
    private final TextView txt3dPitch;
    private final TextView txt3dRoll;
    private final TextView txt3dYaw;
    private final TextView txtCollectedSamples;
    private final TextView txtSamplesPercentage;
    private final TextView txtPreferedFace;
    ImageView imgCompass;
    private OpenGl3DMagCalView glv3DMagCalibration;
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
    private int mSamples;
    private View mToolbarAlertView;

    public ViewController3DMagCal(MainActivity activity, int title, int icon,
                                  int localSettingsVisible, int flightSettingsVisible) {
        super(activity, title, icon, localSettingsVisible, flightSettingsVisible);
        activity.mViews.put(VIEW_3DMAG,
                activity.getLayoutInflater().inflate(R.layout.activity_3dmagcalibration, null));
        activity.setContentView(activity.mViews.get(VIEW_3DMAG));

        glv3DMagCalibration = (OpenGl3DMagCalView) findViewById(R.id.glv_3d_mag_calibration);

        imgCompass = (ImageView) findViewById(R.id.imgStartStopCalibration);

        txtBe0 = ((TextView) findViewById(R.id.txtBe0));
        txtBe1 = ((TextView) findViewById(R.id.txtBe1));
        txtBe2 = ((TextView) findViewById(R.id.txtBe2));

        txtMagBiasX = ((TextView) findViewById(R.id.txtMagBiasX));
        txtMagBiasY = ((TextView) findViewById(R.id.txtMagBiasY));
        txtMagBiasZ = ((TextView) findViewById(R.id.txtMagBiasZ));

        txtR0c0 = ((TextView) findViewById(R.id.txtR0c0));
        txtR1c1 = ((TextView) findViewById(R.id.txtR1c1));
        txtR2c2 = ((TextView) findViewById(R.id.txtR2c2));

        txtCurrentFace = ((TextView) findViewById(R.id.txtCurrentFace));

        txtMagX = ((TextView) findViewById(R.id.txtMagX));
        txtMagY = ((TextView) findViewById(R.id.txtMagY));
        txtMagZ = ((TextView) findViewById(R.id.txtMagZ));

        txt3dPitch = ((TextView) findViewById(R.id.txt3dPitch));
        txt3dRoll = ((TextView) findViewById(R.id.txt3dRoll));
        txt3dYaw = ((TextView) findViewById(R.id.txt3dYaw));

        txtCollectedSamples = ((TextView) findViewById(R.id.txtCollectedSamples));
        txtSamplesPercentage = ((TextView) findViewById(R.id.txtSamplesPercentage));
        txtPreferedFace = ((TextView) findViewById(R.id.txtPreferedFace));

        findViewById(R.id.imgStartStopCalibration).setOnClickListener(this);
    }

    private boolean isCalibrationRunning() {
        return mCalibrationRunning;
    }

    private boolean resetCalibrationOnFc() {
        final String O = "RevoCalibration";
        final String B = "mag_bias";
        final String X = "X";
        final String Y = "Y";
        final String Z = "Z";
        final String T = "mag_transform";
        final String R0 = "r0c0";
        final String R1 = "r1c1";
        final String R2 = "r2c2";

        try {
            final FcDevice dev = getMainActivity().getFcDevice();

            dev.sendSettingsObject(O, 0, B, X, H.floatToByteArrayRev(.0f));
            dev.sendSettingsObject(O, 0, B, Y, H.floatToByteArrayRev(.0f));
            dev.sendSettingsObject(O, 0, B, Z, H.floatToByteArrayRev(.0f));

            dev.sendSettingsObject(O, 0, T, R0, H.floatToByteArrayRev(1f));
            dev.sendSettingsObject(O, 0, T, R1, H.floatToByteArrayRev(1f));
            dev.sendSettingsObject(O, 0, T, R2, H.floatToByteArrayRev(1f));
        } catch (NullPointerException e) {
            return false;
        }
        return true;
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

        boolean success;

        //check if home position is set
        success = getData("HomeLocation", "Set").toString().toLowerCase().equals("true");
        if (!success) {
            VisualLog.e("HomeLocation not set!");
            SingleToast.show(getMainActivity(), "HomeLocation not set!", Toast.LENGTH_LONG);
            return false;
        }

        //check if mag is activated
        success = !getData("MagState", "Source").equals("Invalid");
        if (!success) {
            VisualLog.e("Mag not activated");
            SingleToast.show(getMainActivity(), "Magnetometer is not activated!", Toast.LENGTH_LONG);
            return false;
        }

        //reset calibration on FC
        success = resetCalibrationOnFc();
        if (!success) {
            VisualLog.e("Calibration reset failed");
            return false;
        }

        //increase magstate update rate
        success = setMetaUpdateRate("MagState", 300);
        if (!success) {
            VisualLog.e("MagState Meta Update failed");
            SingleToast.show(getMainActivity(), "MagState Meta Update failed", Toast.LENGTH_LONG);
            return false;
        }

        //hook on to object listener and start collecting samples
        try {
            final FcDevice dev = getMainActivity().mFcDevice;
            try {
                dev.getObjectTree().setListener("AttitudeState", this);
            } catch (IllegalStateException e) {
                dev.getObjectTree().removeListener("AttitudeState");
                dev.getObjectTree().setListener("AttitudeState", this);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            VisualLog.e(e.getMessage());
            return false;
        }

        mCalibrationRunning = true;
        return true;
    }

    private void stopCalibration() {
        imgCompass.setRotation(0);
        mCalibrationRunning = false;

        //remove listener
        try {
            getMainActivity().mFcDevice.getObjectTree().removeListener("MagState");
        } catch (NullPointerException e) {
            VisualLog.e(e.getMessage());
        }

        //reset magstate update rate
        final boolean setUR = setMetaUpdateRate("MagState", 1000);

        //make fit and upload calibration to FC
        final FitPoints fp = fit();
        upload(fp);
    }

    private void toggleCalibration() {
        if (isCalibrationRunning()) {
            stopCalibration();
        } else {
            final boolean s = startCalibration();
            VisualLog.d("Start successful " + s);
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
        requestObject("RevoCalibration");
        requestObject("HomeLocation");
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

    private String longFace(String s) {
        String retval = "";

        VisualLog.d(s);

        retval += s.contains("T") ? "Top " : "";
        retval += s.contains("B") ? "Bottom " : "";
        retval += s.contains("F") ? "Front " : "";
        retval += s.contains("S") ? "Stern " : "";
        retval += s.contains("L") ? "Left " : "";
        retval += s.contains("R") ? "Right " : "";

        VisualLog.d(retval);

        return retval.trim();
    }

    @Override
    public void update() {
        super.update();

        if (getMainActivity().mFcDevice.isConnected()) {
            imgCompass.setEnabled(true);
        } else {
            imgCompass.setEnabled(false);
        }

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

        }

        txtBe0.setText(String.valueOf(be_0));
        txtBe1.setText(String.valueOf(be_1));
        txtBe2.setText(String.valueOf(be_2));

        txtMagBiasX.setText(String.valueOf(mag_bias_x));
        txtMagBiasY.setText(String.valueOf(mag_bias_y));
        txtMagBiasZ.setText(String.valueOf(mag_bias_z));

        txtR0c0.setText(String.valueOf(mag_transform_r0c0));
        txtR1c1.setText(String.valueOf(mag_transform_r1c1));
        txtR2c2.setText(String.valueOf(mag_transform_r2c2));
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
        final float GOOD_SAMPLE_SIZE = 260.f;
        final float pitch = Float.parseFloat(getData("AttitudeState", "Pitch").toString());
        final float roll = Float.parseFloat(getData("AttitudeState", "Roll").toString());
        final float yaw = -Float.parseFloat(getData("AttitudeState", "Yaw").toString());

        final float magx = Float.parseFloat(getData("MagState", "x").toString());
        final float magy = Float.parseFloat(getData("MagState", "y").toString());
        final float magz = Float.parseFloat(getData("MagState", "z").toString());

        final MainActivity m = getMainActivity();
        final int cSamples = mSamples;
        mSamples = addSample();        //saving the current sample

        try {
            glv3DMagCalibration.setRoll(roll);
            glv3DMagCalibration.setPitch(pitch);
            glv3DMagCalibration.setYaw(yaw);

            m.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    try {
                        txtCurrentFace.setText(longFace(glv3DMagCalibration.pitchRollToString(pitch, roll)));

                        txtMagX.setText(String.valueOf(Math.floor(magx)));
                        txtMagY.setText(String.valueOf(Math.floor(magy)));
                        txtMagZ.setText(String.valueOf(Math.floor(magz)));

                        txt3dPitch.setText(String.valueOf(Math.floor(pitch)));
                        txt3dRoll.setText(String.valueOf(Math.floor(roll)));
                        txt3dYaw.setText(String.valueOf(Math.floor(yaw)));

                        txtCollectedSamples.setText(String.valueOf(mSamples));

                        int sum = 0;
                        for (Integer i : glv3DMagCalibration.getSamplesPerFace().values()) {
                            sum += i > SAMPLES ? SAMPLES : i;
                        }
                        txtSamplesPercentage.setText(String.valueOf(
                                H.round((sum / GOOD_SAMPLE_SIZE) * 100, 2)));

                        txtPreferedFace.setText(longFace(glv3DMagCalibration.getPreferedFace()));

                        final String pFace = glv3DMagCalibration.getPreferedFace();
                        final String cFace = glv3DMagCalibration.getCurrentFace();
                        if (pFace.equals(cFace)) {
                            txtPreferedFace.setTextColor(Color.GREEN);
                        } else {
                            txtPreferedFace.setTextColor(Color.RED);
                        }

                        if (cSamples != mSamples) {
                            getMainActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    imgCompass.setRotation(imgCompass.getRotation() % 360 + 1f);
                                }
                            });
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private FitPoints fit() {
        FitPoints fp = glv3DMagCalibration.fit();

        String result;
        try {
            result = fp.toString();
        } catch (NullPointerException e) {
            SingleToast.show(getMainActivity(), "Error fitting points.", Toast.LENGTH_LONG);
            return null;
        }

        VisualLog.d("FIT2", result);

        return fp;
    }

    private void upload(FitPoints fp) {

        //get fit results
        float biasx = (float) fp.center.getEntry(0);
        float biasy = (float) fp.center.getEntry(1);
        float biasz = (float) fp.center.getEntry(2);

        final FcDevice dev = getMainActivity().getFcDevice();

        if (dev != null) {

            //send fix results to fx
            dev.sendSettingsObject("RevoCalibration", 0, "mag_bias", "X", H.floatToByteArrayRev(biasx));
            dev.sendSettingsObject("RevoCalibration", 0, "mag_bias", "Y", H.floatToByteArrayRev(biasy));
            dev.sendSettingsObject("RevoCalibration", 0, "mag_bias", "Z", H.floatToByteArrayRev(biasz));

            float beVecLen = (float) Math.sqrt(Math.pow(be_0, 2) + Math.pow(be_1, 2) + Math.pow(be_2, 2));

            float r0c0 = beVecLen / (float) fp.radii.getEntry(0);
            float r1c1 = beVecLen / (float) fp.radii.getEntry(1);
            float r2c2 = beVecLen / (float) fp.radii.getEntry(2);

            dev.sendSettingsObject("RevoCalibration", 0, "mag_transform", "r0c0", H.floatToByteArrayRev(r0c0));
            dev.sendSettingsObject("RevoCalibration", 0, "mag_transform", "r1c1", H.floatToByteArrayRev(r1c1));
            dev.sendSettingsObject("RevoCalibration", 0, "mag_transform", "r2c2", H.floatToByteArrayRev(r2c2));

            dev.savePersistent("RevoCalibration");

            VisualLog.d("FIT1", MessageFormat.format("{0} {1} {2} {3} {4} {5}", r0c0, r1c1, r2c2, biasx, biasy, biasz));
        }

        be_0 = 0; //resetting one value will re-receive all values
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgStartStopCalibration: {
                toggleCalibration();
                break;
            }
            default: //do nothing
                break;
        }
    }

    @Override
    public void onToolbarFlightSettingsClick(View v) {

        final MainActivity ma = getMainActivity();

        if (isConnected()) {

            mToolbarAlertView = View.inflate(ma, R.layout.alert_dialog_magcal, null);

            final float biasX = H.stringToFloat(getData("RevoCalibration", "mag_bias", "X").toString());
            final float biasY = H.stringToFloat(getData("RevoCalibration", "mag_bias", "Y").toString());
            final float biasZ = H.stringToFloat(getData("RevoCalibration", "mag_bias", "Z").toString());

            final float trans0 = H.stringToFloat(getData("RevoCalibration", "mag_transform", "r0c0").toString());
            final float trans1 = H.stringToFloat(getData("RevoCalibration", "mag_transform", "r1c1").toString());
            final float trans2 = H.stringToFloat(getData("RevoCalibration", "mag_transform", "r2c2").toString());

            //set final vars to be used in the dialog onClick Listeners
            final float lmBiasX = this.mag_bias_x;
            final float lmBiasY = this.mag_bias_y;
            final float lmBiasZ = this.mag_bias_z;

            final float lmTrans0 = this.mag_transform_r0c0;
            final float lmTrans1 = this.mag_transform_r1c1;
            final float lmTrans2 = this.mag_transform_r2c2;

            fillToolbarAlertViewLeft("Current", biasX, biasY, biasZ, trans0, trans1, trans2);
            fillToolbarAlertViewRight("Calculated", this.mag_bias_x, this.mag_bias_y, this.mag_bias_z,
                    this.mag_transform_r0c0, this.mag_transform_r1c1, this.mag_transform_r2c2);

            setEnabledRightEditTextFields(false);

            mToolbarAlertView.findViewById(R.id.etxMagCalBiasRightX).requestFocus();

            AlertDialog.Builder builder = new AlertDialog.Builder(ma);
            builder.setView(mToolbarAlertView);
            builder.setTitle("Mag Calibration Backup");
            builder.setCancelable(true);

            builder.setNeutralButton("Close",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //just dismiss dialog
                        }
                    });
            builder.setPositiveButton("Upload",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //Copy calc to current (volatile)
                            sendSettingsObjectRevFloat("RevoCalibration", "mag_bias", "X", lmBiasX);
                            sendSettingsObjectRevFloat("RevoCalibration", "mag_bias", "Y", lmBiasY);
                            sendSettingsObjectRevFloat("RevoCalibration", "mag_bias", "Z", lmBiasZ);

                            sendSettingsObjectRevFloat("RevoCalibration", "mag_transform", "r0c0", lmTrans0);
                            sendSettingsObjectRevFloat("RevoCalibration", "mag_transform", "r1c1", lmTrans1);
                            sendSettingsObjectRevFloat("RevoCalibration", "mag_transform", "r2c2", lmTrans2);

                            SingleToast.show(ma, "Calculated values uploaded volatile to FlightController");
                        }
                    });

            builder.setNegativeButton("Save",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //Copy calc to current (persistent)
                            sendSettingsObjectRevFloat("RevoCalibration", "mag_bias", "X", lmBiasX);
                            sendSettingsObjectRevFloat("RevoCalibration", "mag_bias", "Y", lmBiasY);
                            sendSettingsObjectRevFloat("RevoCalibration", "mag_bias", "Z", lmBiasZ);

                            sendSettingsObjectRevFloat("RevoCalibration", "mag_transform", "r0c0", lmTrans0);
                            sendSettingsObjectRevFloat("RevoCalibration", "mag_transform", "r1c1", lmTrans1);
                            sendSettingsObjectRevFloat("RevoCalibration", "mag_transform", "r2c2", lmTrans2);

                            savePersistent("RevoCalibration");

                            SingleToast.show(ma, "Calculated values saved persistent to FlightController");

                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();

        } else {
            SingleToast.show(ma, "Not connected");
        }
    }

    @Override
    public void onToolbarLocalSettingsClick(View v) {

        final MainActivity ma = getMainActivity();

        mToolbarAlertView = View.inflate(ma, R.layout.alert_dialog_magcal, null);

        final float biasX = H.stringToFloat(getData("RevoCalibration", "mag_bias", "X").toString());
        final float biasY = H.stringToFloat(getData("RevoCalibration", "mag_bias", "Y").toString());
        final float biasZ = H.stringToFloat(getData("RevoCalibration", "mag_bias", "Z").toString());

        final float trans0 = H.stringToFloat(getData("RevoCalibration", "mag_transform", "r0c0").toString());
        final float trans1 = H.stringToFloat(getData("RevoCalibration", "mag_transform", "r1c1").toString());
        final float trans2 = H.stringToFloat(getData("RevoCalibration", "mag_transform", "r2c2").toString());

        fillDefaultBackupIfEmpty(biasX, biasY, biasZ, trans0, trans1, trans2);
        fillToolbarAlertViewLeft("Backup", SettingsHelper.mMagCalBiasX,
                SettingsHelper.mMagCalBiasY, SettingsHelper.mMagCalBiasZ,
                SettingsHelper.mMagCalTransformR0C0, SettingsHelper.mMagCalTransformR1C1,
                SettingsHelper.mMagCalTransformR2C2);

        fillToolbarAlertViewRight("Current", biasX, biasY, biasZ, trans0, trans1, trans2);
        setEnabledRightEditTextFields(true);

        mToolbarAlertView.findViewById(R.id.etxMagCalBiasRightX).requestFocus();

        AlertDialog.Builder builder = new AlertDialog.Builder(ma);
        builder.setView(mToolbarAlertView);
        builder.setTitle("Mag Calibration Backup");
        builder.setCancelable(true);

        builder.setNeutralButton("Save",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        SettingsHelper.mMagCalBiasX = H.stringToFloat(((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalBiasLeftX)).getText().toString());
                        SettingsHelper.mMagCalBiasY = H.stringToFloat(((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalBiasLeftY)).getText().toString());
                        SettingsHelper.mMagCalBiasZ = H.stringToFloat(((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalBiasLeftZ)).getText().toString());

                        SettingsHelper.mMagCalTransformR0C0 = H.stringToFloat(((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalTransLeftR0C0)).getText().toString());
                        SettingsHelper.mMagCalTransformR1C1 = H.stringToFloat(((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalTransLeftR1C1)).getText().toString());
                        SettingsHelper.mMagCalTransformR2C2 = H.stringToFloat(((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalTransLeftR2C2)).getText().toString());

                        SingleToast.show(ma, "Manual changes in Backup values saved");
                    }
                });
        builder.setPositiveButton("Backup",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        if (isConnected()) {
                            //Copy live to backup
                            SettingsHelper.mMagCalBiasX = biasX;
                            SettingsHelper.mMagCalBiasY = biasY;
                            SettingsHelper.mMagCalBiasZ = biasZ;

                            SettingsHelper.mMagCalTransformR0C0 = trans0;
                            SettingsHelper.mMagCalTransformR1C1 = trans1;
                            SettingsHelper.mMagCalTransformR2C2 = trans2;

                            SettingsHelper.saveSettings(ma);

                            SingleToast.show(ma, "Current values copied to Backup");
                        } else {
                            SingleToast.show(ma, "Not connected");
                        }
                    }
                });

        builder.setNegativeButton("Restore",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        if (!isConnected()) {
                            //Copy backup to live
                            sendSettingsObjectRevFloat("RevoCalibration", "mag_bias", "X", SettingsHelper.mMagCalBiasX);
                            sendSettingsObjectRevFloat("RevoCalibration", "mag_bias", "Y", SettingsHelper.mMagCalBiasY);
                            sendSettingsObjectRevFloat("RevoCalibration", "mag_bias", "Z", SettingsHelper.mMagCalBiasZ);

                            sendSettingsObjectRevFloat("RevoCalibration", "mag_transform", "r0c0", SettingsHelper.mMagCalTransformR0C0);
                            sendSettingsObjectRevFloat("RevoCalibration", "mag_transform", "r1c1", SettingsHelper.mMagCalTransformR1C1);
                            sendSettingsObjectRevFloat("RevoCalibration", "mag_transform", "r2c2", SettingsHelper.mMagCalTransformR2C2);

                            savePersistent("RevoCalibration");

                            SingleToast.show(ma, "Backup values copied to FlightController and saved persistent");
                        } else {
                            SingleToast.show(ma, "Not connected");
                        }
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void fillDefaultBackupIfEmpty(float biasX, float biasY, float biasZ, float trans0, float trans1, float trans2) {
        //fill settings with current values, if saved settings are "0"
        SettingsHelper.mMagCalBiasX = SettingsHelper.mMagCalBiasX == 0 ? biasX : SettingsHelper.mMagCalBiasX;
        SettingsHelper.mMagCalBiasY = SettingsHelper.mMagCalBiasY == 0 ? biasY : SettingsHelper.mMagCalBiasY;
        SettingsHelper.mMagCalBiasZ = SettingsHelper.mMagCalBiasZ == 0 ? biasZ : SettingsHelper.mMagCalBiasZ;

        SettingsHelper.mMagCalTransformR0C0 = SettingsHelper.mMagCalTransformR0C0 == 0 ? trans0 : SettingsHelper.mMagCalTransformR0C0;
        SettingsHelper.mMagCalTransformR1C1 = SettingsHelper.mMagCalTransformR1C1 == 0 ? trans1 : SettingsHelper.mMagCalTransformR1C1;
        SettingsHelper.mMagCalTransformR2C2 = SettingsHelper.mMagCalTransformR2C2 == 0 ? trans2 : SettingsHelper.mMagCalTransformR2C2;
    }

    private void fillToolbarAlertViewLeft(String leftLabel, float biasX, float biasY, float biasZ, float trans0, float trans1, float trans2) {
        ((TextView) mToolbarAlertView.findViewById(R.id.txtMagCalBiasLeftLabel)).setText(leftLabel);
        ((TextView) mToolbarAlertView.findViewById(R.id.txtMagCalTransformLeftLabel)).setText(leftLabel);

        ((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalBiasLeftX))
                .setText(String.format(Locale.US, "%f", biasX));
        ((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalBiasLeftY))
                .setText(String.format(Locale.US, "%f", biasY));
        ((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalBiasLeftZ))
                .setText(String.format(Locale.US, "%f", biasZ));

        ((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalTransLeftR0C0))
                .setText(String.format(Locale.US, "%f", trans0));
        ((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalTransLeftR1C1))
                .setText(String.format(Locale.US, "%f", trans1));
        ((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalTransLeftR2C2))
                .setText(String.format(Locale.US, "%f", trans2));
    }

    private void fillToolbarAlertViewRight(String rightLabel, float biasX, float biasY, float biasZ, float trans0, float trans1, float trans2) {
        ((TextView) mToolbarAlertView.findViewById(R.id.txtMagCalBiasRightLabel)).setText(rightLabel);
        ((TextView) mToolbarAlertView.findViewById(R.id.txtMagCalTransformRightLabel)).setText(rightLabel);

        ((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalBiasRightX))
                .setText(String.format(Locale.US, "%f", biasX));
        ((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalBiasRightY))
                .setText(String.format(Locale.US, "%f", biasY));
        ((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalBiasRightZ))
                .setText(String.format(Locale.US, "%f", biasZ));

        ((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalTransRightR0C0))
                .setText(String.format(Locale.US, "%f", trans0));
        ((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalTransRightR1C1))
                .setText(String.format(Locale.US, "%f", trans1));
        ((EditText) mToolbarAlertView.findViewById(R.id.etxMagCalTransRightR2C2))
                .setText(String.format(Locale.US, "%f", trans2));
    }

    private void setEnabledRightEditTextFields(boolean enabled) {
        mToolbarAlertView.findViewById(R.id.etxMagCalBiasLeftX).setEnabled(enabled);
        mToolbarAlertView.findViewById(R.id.etxMagCalBiasLeftY).setEnabled(enabled);
        mToolbarAlertView.findViewById(R.id.etxMagCalBiasLeftZ).setEnabled(enabled);

        mToolbarAlertView.findViewById(R.id.etxMagCalTransLeftR0C0).setEnabled(enabled);
        mToolbarAlertView.findViewById(R.id.etxMagCalTransLeftR1C1).setEnabled(enabled);
        mToolbarAlertView.findViewById(R.id.etxMagCalTransLeftR2C2).setEnabled(enabled);
    }
}
