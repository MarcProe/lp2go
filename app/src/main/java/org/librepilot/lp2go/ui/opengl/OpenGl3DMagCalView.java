/*
 * @file   OpenGl3DMagCalView.java
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

package org.librepilot.lp2go.ui.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.widget.Toast;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.controller.ViewController3DMagCal;
import org.librepilot.lp2go.helper.ellipsoidfit.FitPoints;
import org.librepilot.lp2go.ui.SingleToast;
import org.librepilot.lp2go.ui.opengl.shapes.Cube;
import org.librepilot.lp2go.ui.opengl.shapes.Rhombicuboctahedron;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGl3DMagCalView extends GLSurfaceView {

    private final OpenGLRenderer mRenderer;
    private Map<String, Integer> mSamplesPerFace;
    private Map<String, Integer> mAuxSamplesPerFace;
    private String mPreferedFace;
    private String mCurrentFace;
    private Cube mPrefFaceCube;

    public OpenGl3DMagCalView(Context context) {
        super(context);
        setEGLContextClientVersion(1);
        mRenderer = new OpenGLRenderer();
        setRenderer(mRenderer);
        initPreferedFaces();
    }

    public OpenGl3DMagCalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(1);
        mRenderer = new OpenGLRenderer();
        setRenderer(mRenderer);
        initPreferedFaces();
    }

    public void setPrefFaceCube(String face) {

        final int SIZE = 750;
        float x = 0, y = 0, z = 0;

        if (face.contains("T")) {
            z += SIZE;
        } else if (face.contains("B")) {
            z -= SIZE;
        }

        if (face.contains("L")) {
            x -= SIZE;
        } else if (face.contains("R")) {
            x += SIZE;
        }

        if (face.contains("F")) {
            y -= SIZE;
        } else if (face.contains("S")) {
            y += SIZE;
        }

        mPrefFaceCube = new Cube(x, y, z, -0.2f, 0.2f);
        mPrefFaceCube.setRGB(.0f, 1.f, .0f);
        mPrefFaceCube.setAlpha(.5f);
        //VisualLog.i(face + " " + x + " " + y + " " + z);
    }

    public Map<String, Integer> getSamplesPerFace() {
        return this.mSamplesPerFace;
    }

    public Map<String, Integer> getAuxSamplesPerFace() {
        return this.mAuxSamplesPerFace;
    }

    private void initPreferedFaces() {
        //T/B|F/S|R/L
        mPreferedFace = "T";
        mSamplesPerFace = new LinkedHashMap<>();
        mSamplesPerFace.put("T", 0);
        mSamplesPerFace.put("TS", 0);
        mSamplesPerFace.put("TSL", 0);
        mSamplesPerFace.put("TL", 0);
        mSamplesPerFace.put("TFL", 0);
        mSamplesPerFace.put("TF", 0);
        mSamplesPerFace.put("TFR", 0);
        mSamplesPerFace.put("TR", 0);
        mSamplesPerFace.put("TSR", 0);
        mSamplesPerFace.put("SL", 0);
        mSamplesPerFace.put("L", 0);
        mSamplesPerFace.put("FL", 0);
        mSamplesPerFace.put("F", 0);
        mSamplesPerFace.put("FR", 0);
        mSamplesPerFace.put("R", 0);
        mSamplesPerFace.put("SR", 0);
        mSamplesPerFace.put("S", 0);
        mSamplesPerFace.put("BS", 0);
        mSamplesPerFace.put("BSR", 0);
        mSamplesPerFace.put("BR", 0);
        mSamplesPerFace.put("BFR", 0);
        mSamplesPerFace.put("BF", 0);
        mSamplesPerFace.put("BFL", 0);
        mSamplesPerFace.put("BL", 0);
        mSamplesPerFace.put("BSL", 0);
        mSamplesPerFace.put("B", 0);

        mAuxSamplesPerFace = new LinkedHashMap<>();
        mAuxSamplesPerFace.put("T", 0);
        mAuxSamplesPerFace.put("TS", 0);
        mAuxSamplesPerFace.put("TSL", 0);
        mAuxSamplesPerFace.put("TL", 0);
        mAuxSamplesPerFace.put("TFL", 0);
        mAuxSamplesPerFace.put("TF", 0);
        mAuxSamplesPerFace.put("TFR", 0);
        mAuxSamplesPerFace.put("TR", 0);
        mAuxSamplesPerFace.put("TSR", 0);
        mAuxSamplesPerFace.put("SL", 0);
        mAuxSamplesPerFace.put("L", 0);
        mAuxSamplesPerFace.put("FL", 0);
        mAuxSamplesPerFace.put("F", 0);
        mAuxSamplesPerFace.put("FR", 0);
        mAuxSamplesPerFace.put("R", 0);
        mAuxSamplesPerFace.put("SR", 0);
        mAuxSamplesPerFace.put("S", 0);
        mAuxSamplesPerFace.put("BS", 0);
        mAuxSamplesPerFace.put("BSR", 0);
        mAuxSamplesPerFace.put("BR", 0);
        mAuxSamplesPerFace.put("BFR", 0);
        mAuxSamplesPerFace.put("BF", 0);
        mAuxSamplesPerFace.put("BFL", 0);
        mAuxSamplesPerFace.put("BL", 0);
        mAuxSamplesPerFace.put("BSL", 0);
        mAuxSamplesPerFace.put("B", 0);

        mPrefFaceCube = new Cube(500, 0, 0);

    }

    public void resetSamples() {
        if (this.mRenderer.mCubes != null) {
            this.mRenderer.mCubes.clear();
            initPreferedFaces();
        }
        if (this.mRenderer.mAuxCubes != null) {
            this.mRenderer.mAuxCubes.clear();
            initPreferedFaces();
        }
    }

    public FitPoints fit(boolean isAuxMag) {
        FitPoints fp = new FitPoints();
        try {
            boolean ok;
            if (isAuxMag) {
                ok = fp.fitEllipsoid(mRenderer.mAuxCubes);
            } else {
                ok = fp.fitEllipsoid(mRenderer.mCubes);
            }
            if (ok) {
                return fp;
            } else {
                return null;
            }
        } catch (Exception e) {
            SingleToast.show(getContext(), e.getMessage(), Toast.LENGTH_LONG);
            return null;
        }
    }

    public void setPitch(float pitch) {
        this.mRenderer.mPitch = pitch;
    }

    public void setRoll(float roll) {
        this.mRenderer.mRoll = roll;
    }

    public void setYaw(float yaw) {
        this.mRenderer.mYaw = yaw;
    }

    public String getPreferedFace(boolean isAuxMag) {
        int curcount = isAuxMag ? mAuxSamplesPerFace.get(mPreferedFace) : mSamplesPerFace.get(mPreferedFace);
        if (curcount < 24) {
            return mPreferedFace;
        } else {
            if (isAuxMag) {
                for (String f : mAuxSamplesPerFace.keySet()) {
                    if (mAuxSamplesPerFace.get(f) < 24) {
                        mPreferedFace = f;
                        return f;
                    }
                }
            } else {
                for (String f : mSamplesPerFace.keySet()) {
                    if (mSamplesPerFace.get(f) < 24) {
                        mPreferedFace = f;
                        return f;
                    }
                }
            }
        }
        return "";
    }

    public String getCurrentFace() {
        return mCurrentFace != null ? mCurrentFace : "";
    }

    public int addSample(float x, float y, float z, boolean isAuxMag) {

        Cube c = new Cube(x, y, z);

        int alpha = (int) (c.alpha / OpenGLRenderer.ANGLE_DEG);
        int beta = (int) (c.beta / OpenGLRenderer.ANGLE_DEG);

        int num;
        if (isAuxMag) {
            num = (int) this.mRenderer.mAuxSampleCubes[alpha][beta];
            if (this.mRenderer.mAuxCubes != null && num < ViewController3DMagCal.SAMPLES) {
                VisualLog.i("" + ((float) num / ViewController3DMagCal.SAMPLES) + " " + num);
                c.setRGB(0.9f * ((float) num / ViewController3DMagCal.SAMPLES), 0.9f * ((float) num / ViewController3DMagCal.SAMPLES), 1.0f);
                this.mRenderer.mAuxCubes.add(c);
                //increase counter for current sector
                this.mRenderer.mAuxSampleCubes[alpha][beta] = num + 1;
                mCurrentFace = pitchRollToString(this.mRenderer.mPitch, this.mRenderer.mRoll);
                final Integer t = mAuxSamplesPerFace.get(mCurrentFace);
                mAuxSamplesPerFace.put(mCurrentFace, t == null ? 1 : t + 1);
            }
        } else {
            num = (int) this.mRenderer.mSampleCubes[alpha][beta];
            if (this.mRenderer.mCubes != null && num < ViewController3DMagCal.SAMPLES) {
                c.setRGB(1.0f, 0.9f * ((float) num / ViewController3DMagCal.SAMPLES), 0.9f * ((float) num / ViewController3DMagCal.SAMPLES));
                this.mRenderer.mCubes.add(c);
                //increase counter for current sector
                this.mRenderer.mSampleCubes[alpha][beta] = num + 1;
                mCurrentFace = pitchRollToString(this.mRenderer.mPitch, this.mRenderer.mRoll);
                final Integer t = mSamplesPerFace.get(mCurrentFace);
                mSamplesPerFace.put(mCurrentFace, t == null ? 1 : t + 1);
            }
        }

        if (isAuxMag) {
            return this.mRenderer.mAuxCubes != null ? this.mRenderer.mAuxCubes.size() : 0;
        } else {
            return this.mRenderer.mCubes != null ? this.mRenderer.mCubes.size() : 0;
        }
    }

    public String pitchRollToString(float pitch, float roll) {
        return mRenderer.pitchRollToString(pitch, roll);
    }

    private class OpenGLRenderer implements GLSurfaceView.Renderer {

        final static int ANGLE_DEG = 15;
        final static int ANGLES = (int) (360 / ANGLE_DEG);
        public ArrayList<Cube> mCubes;
        public ArrayList<Cube> mAuxCubes;
        protected float mPitch = 0;
        protected float mRoll = 0;
        protected float[][] mSampleCubes = new float[ANGLES][ANGLES];
        protected float[][] mAuxSampleCubes = new float[ANGLES][ANGLES];
        protected float mYaw = 0;
        private CartesianCoordinateSystem mCcs = new CartesianCoordinateSystem();
        private Rhombicuboctahedron mRhombicuboctahedron = new Rhombicuboctahedron();

        public String pitchRollToString(float pitch, float roll) {
            return mRhombicuboctahedron.pitchRollToString(pitch, roll);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);

            gl.glClearDepthf(1.0f);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glDepthFunc(GL10.GL_LEQUAL);

            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                    GL10.GL_NICEST);

            for (int i = 0; i < ANGLES; i++) {
                for (int j = 0; j < ANGLES; j++) {
                    mSampleCubes[i][j] = 0;
                    mAuxSampleCubes[i][j] = 0;
                }
            }

            mCubes = new ArrayList<>();
            mAuxCubes = new ArrayList<>();

        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);
            gl.glViewport(0, 0, width, height);

            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glEnable(GL10.GL_LIGHTING);
            gl.glEnable(GL10.GL_LIGHT0);

            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity();

            gl.glTranslatef(0.0f, 0.0f, -10.0f);

            Quaternion p = new Quaternion(new Vector3(1, 0, 0), mPitch);
            Quaternion r = new Quaternion(new Vector3(0, 1, 0), mRoll);
            Quaternion y = new Quaternion(new Vector3(0, 0, 1), mYaw);

            Quaternion temp;
            temp = y.mul(p).mul(r);

            float[] rotate = new float[16];

            temp.toMatrix(rotate);

            float transMinus[] = new float[16];
            float transPlus[] = new float[16];
            Matrix.setIdentityM(transMinus, 0);
            Matrix.setIdentityM(transPlus, 0);
            Matrix.translateM(transMinus, 0, 0, 0, 0);
            Matrix.translateM(transPlus, 0, 0, 0, 0);

            Matrix.multiplyMM(rotate, 0, transMinus, 0, rotate, 0);
            Matrix.multiplyMM(rotate, 0, rotate, 0, transPlus, 0);


            gl.glMultMatrixf(rotate, 0);

            float lightpos1[] = {.5f, 1.f, 1.f, 0.f};
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightpos1, 0);


            // gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, ambient, 0 );
            //gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, position, 0);
            //gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPOT_DIRECTION, direction, 0);
            //gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_CUTOFF, 30.0f);

            //mRhombicuboctahedron.draw(gl);

            mCcs.draw(gl);

            ArrayList<Cube> tc = new ArrayList<>(mCubes);

            for (Cube mCube : tc) {
                mCube.draw(gl);
            }

            ArrayList<Cube> atc = new ArrayList<>(mAuxCubes);

            for (Cube maCube : atc) {
                maCube.draw(gl);
            }

            mPrefFaceCube.draw(gl);

            gl.glLoadIdentity();


        }
    }
}

