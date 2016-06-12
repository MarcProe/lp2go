package org.librepilot.lp2go.controller;

import android.widget.TextView;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.uavtalk.UAVTalkObject;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectListener;
import org.librepilot.lp2go.ui.opengl.OpenGl3DMagCalibrationView;

public class ViewController3DMagCalibration extends ViewController implements
        UAVTalkObjectListener {

    private OpenGl3DMagCalibrationView glv3DMagCalibration;

    public ViewController3DMagCalibration(MainActivity activity, int title,
                                          int localSettingsVisible, int flightSettingsVisible) {
        super(activity, title, localSettingsVisible, flightSettingsVisible);
        activity.mViews.put(VIEW_3DMAG,
                activity.getLayoutInflater().inflate(R.layout.activity_3dmagcalibration, null));
        activity.setContentView(activity.mViews.get(VIEW_3DMAG));

        this.glv3DMagCalibration =
                (OpenGl3DMagCalibrationView) activity.findViewById(R.id.glv_3d_mag_calibration);

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
        } catch (NullPointerException e1) {

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
            } catch (NullPointerException e1) {

            }
        }
    }

    @Override
    public void onObjectUpdate(UAVTalkObject o) {
        final float pitch = Float.parseFloat(getData("AttitudeState", "Pitch").toString());
        final float roll = Float.parseFloat(getData("AttitudeState", "Roll").toString());
        final float yaw = -Float.parseFloat(getData("AttitudeState", "Yaw").toString());

        final float q1 = Float.parseFloat(getData("AttitudeState", "q1").toString());
        final float q2 = Float.parseFloat(getData("AttitudeState", "q1").toString());
        final float q3 = Float.parseFloat(getData("AttitudeState", "q1").toString());
        final float q4 = Float.parseFloat(getData("AttitudeState", "q1").toString());

        try {
            glv3DMagCalibration
                    .setRoll(roll);
            glv3DMagCalibration
                    .setPitch(pitch);
            glv3DMagCalibration
                    .setYaw(yaw);
            final MainActivity m = getMainActivity();
            m.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    try {
                        ((TextView) m.findViewById(R.id.q1)).setText("" + Math.floor(q1));
                        ((TextView) m.findViewById(R.id.q2)).setText("" + Math.floor(q2));
                        ((TextView) m.findViewById(R.id.q3)).setText("" + Math.floor(q3));
                        ((TextView) m.findViewById(R.id.q4)).setText("" + Math.floor(q4));

                        ((TextView) m.findViewById(R.id.txt3dpitch))
                                .setText("" + Math.floor(pitch));
                        ((TextView) m.findViewById(R.id.txt3droll)).setText("" + Math.floor(roll));
                        ((TextView) m.findViewById(R.id.txt3dyaw)).setText("" + Math.floor(yaw));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
