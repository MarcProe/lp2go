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

package org.librepilot.lp2go.ui.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.AttributeSet;

import org.librepilot.lp2go.helper.ellipsoidFit.FitPoints;
import org.librepilot.lp2go.helper.libgdx.math.Quaternion;
import org.librepilot.lp2go.helper.libgdx.math.Vector3;
import org.librepilot.lp2go.ui.opengl.shapes.Cube;
import org.librepilot.lp2go.ui.opengl.shapes.Rhombicuboctahedron;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGl3DMagCalView extends GLSurfaceView {

    private final OpenGLRenderer mRenderer;

    public OpenGl3DMagCalView(Context context) {
        super(context);
        setEGLContextClientVersion(1);
        mRenderer = new OpenGLRenderer();
        setRenderer(mRenderer);
    }

    public OpenGl3DMagCalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(1);
        mRenderer = new OpenGLRenderer();
        setRenderer(mRenderer);
    }

    public void resetSamples() {
        this.mRenderer.mCubes.clear();

    }

    public FitPoints fit() {
        FitPoints fp = new FitPoints();
        boolean ok = fp.fitEllipsoid(mRenderer.mCubes);
        if (ok) {
            return fp;
        } else {
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


    public int addSample(float x, float y, float z) {

        Cube c = new Cube(x, y, z);

        int alpha = (int) (c.alpha / OpenGLRenderer.ANGLE_DEG);
        int beta = (int) (c.beta / OpenGLRenderer.ANGLE_DEG);

        int num = (int) this.mRenderer.mSampleCubes[alpha][beta];

        if (num < OpenGLRenderer.SAMPLES) {
            if (this.mRenderer.mCubes != null) {

                c.setRGB(1.0f, 0.3f * num, 0.3f * num);
                this.mRenderer.mCubes.add(c);

                //increase counter for current sector
                this.mRenderer.mSampleCubes[alpha][beta] = num + 1;
                //VisualLog.d("DBG", "Adding Cube");
            }

        } else {
            //VisualLog.d("DBG", "Won't add because " + num + " " + alpha +" " + beta + " ");
        }
        return this.mRenderer.mCubes.size();
    }

    public String PitchRollToString(float pitch, float roll) {
        return mRenderer.PitchRollToString(pitch, roll);
    }

    private class OpenGLRenderer implements GLSurfaceView.Renderer {

        final static int ANGLE_DEG = 15;
        final static int ANGLES = (int) (360 / ANGLE_DEG);
        final static int SAMPLES = 6;
        public ArrayList<Cube> mCubes;
        protected float mPitch = 0;
        protected float mRoll = 0;
        protected float[][] mSampleCubes = new float[ANGLES][ANGLES];
        protected float mYaw = 0;
        private CartesianCoordinateSystem mCcs = new CartesianCoordinateSystem();
        private Rhombicuboctahedron mRhombicuboctahedron = new Rhombicuboctahedron();


        public OpenGLRenderer() {

        }

        public String PitchRollToString(float pitch, float roll) {
            return mRhombicuboctahedron.PitchRollToString(pitch, roll);
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
                }
            }

            mCubes = new ArrayList<>();

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

            gl.glLoadIdentity();
        }
    }
}

