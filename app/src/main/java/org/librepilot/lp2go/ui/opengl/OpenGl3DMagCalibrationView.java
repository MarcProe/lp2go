package org.librepilot.lp2go.ui.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.AttributeSet;

import org.librepilot.lp2go.helper.libgdx.math.Quaternion;
import org.librepilot.lp2go.helper.libgdx.math.Vector3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGl3DMagCalibrationView extends GLSurfaceView {

    private final OpenGLRenderer mRenderer;

    public OpenGl3DMagCalibrationView(Context context) {
        super(context);
        setEGLContextClientVersion(1);
        mRenderer = new OpenGLRenderer();
        setRenderer(mRenderer);
    }

    public OpenGl3DMagCalibrationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(1);
        mRenderer = new OpenGLRenderer();
        setRenderer(mRenderer);
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

    public String PitchRollToString(float pitch, float roll) {
        return mRenderer.PitchRollToString(pitch, roll);
    }

}

class OpenGLRenderer implements GLSurfaceView.Renderer {

    protected float mPitch = 0;
    protected float mRoll = 0;
    protected float mYaw = 0;

    private Rhombicuboctahedron mRhombicuboctahedron = new Rhombicuboctahedron();

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
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        gl.glTranslatef(0.0f, 0.0f, -10.0f);

        //gl.glRotatef(mRoll, 0.0f, 1.0f, 0.0f);      //Roll
        //gl.glRotatef(mPitch, 1.0f, 0.0f, 0.0f);     //Pitch
        //gl.glRotatef(mYaw, 0.0f, 0.0f, 1.0f);       //Yaw

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

        gl.glMultMatrixf(rotate,0);

        mRhombicuboctahedron.draw(gl);

        gl.glLoadIdentity();
    }
}

class Rhombicuboctahedron {

    private final float no = -1.f;
    private final float po = 1.0f;
    private final float ps = 1.0f + (float) Math.sqrt(1.0f);
    private final float ns = ps * (-1);
    private float alpha = 0.65f;
    private float colors[] = {
            //Top           *
            //Bottom        o
            //Front         ^
            //Stern         v
            //Left          <
            //Right         >

            0.75f, 0.5f, 0.75f, alpha,       //TFR
            0.9f, 0.9f, 0.9f, alpha,         //T
            0.5f, 0.75f, 0.75f, alpha,       //TFL
            1.0f, 0.75f, 0.75f, alpha,       //TS
            0.75f, 0.75f, 1.0f, alpha,       //TF
            .5f, .0f, .7f, alpha,            //BFR
            0.5f, 0.5f, 1.0f, alpha,         //F
            0.1f, .5f, .7f, alpha,           //BFL
            0.9f, 0.5f, 0.9f, alpha,         //TR
            0.5f, 0.0f, 0.5f, alpha,         //R
            1.0f, 0.5f, 0.75f, alpha,        //TSR
            0.25f, 0.0f, 0.25f, alpha,       //BR
            .25f, .25f, 1.0f, alpha,         //BF
            0.7f, 0.0f, 0.2f, alpha,         //BSR
            0.1f, 0.1f, 0.1f, alpha,         //B
            0.5f, 0.5f, 0.2f, alpha,         //BSL
            0.9f, 0.0f, 0.4f, alpha,         //SR
            1.0f, 0.25f, 0.25f, alpha,       //BS
            1.0f, 0.5f, 0.5f, alpha,         //S
            0.75f, 0.75f, 0.5f, alpha,       //SL
            0.5f, 0.9f, 0.5f, alpha,         //TL
            0.5f, 0.75f, 0.5f, alpha,        //L
            1.0f, 1.00f, 0.75f, alpha,       //TSL
            0.2f, 0.5f, 0.2f, alpha,         //BL
            0.5f, 0.0f, 0.8f, alpha,         //FR
            0.5f, 0.75f, 1.0f, alpha         //FL
    };
    private byte indices[] = {
            //                              PITCH   ROLL   YAW
            8, 4, 0,            //TFR       45      -45

            0, 2, 1,            // T1       0       0
            3, 2, 1,            // T2

            6, 20, 2,           ///TFL      45      45

            1, 16, 3,           //TS1       -45     0
            18, 16, 3,          //TS2

            0, 2, 4,            // TF1      45      0
            6, 2, 4,            // TF2

            12, 9, 5,           //BFR       45      -135

            4, 5, 6,            // F1       90      X
            7, 5, 6,            // F2

            14, 21, 7,          //BFL       45      135

            0, 1, 8,            //TR1       0       -45
            10, 1, 8,           //TR2

            8, 10, 9,           // R1       0       -90
            11, 10, 9,          // R2

            1, 16, 10,          //TSR       -45     -45
            //                              PITCH    ROLL   YAW
            13, 12, 11,         //BR1       0       -135
            9, 12, 11,          //BR2

            5, 7, 12,           //BF1       45      +-180
            14, 7, 12,          //BF2

            11, 17, 13,           //BSR     -45     -135

            12, 13, 14,         // B1       0       +-180
            15, 13, 14,         // B2

            19, 23, 15,          //BSL       -45     135

            11, 17, 16,         //SR1       -45     -90
            11, 10, 16,         //SR2

            13, 15, 17,         //BS1       -45     +-180
            19, 15, 17,         //BS2

            16, 17, 18,         //S1        -90     X
            19, 17, 18,         //S2

            18, 22, 19,         //SL        -45     90
            22, 23, 19,         //SL
            //                              PITCH    ROLL   YAW
            2, 3, 20,           //TL1       0       45
            22, 3, 20,          //TL2

            20, 22, 21,         //L1        0       90
            23, 22, 21,         //L2

            3, 18, 22,          //TSL       -45     45

            14, 15, 23,         //BL1       0       135
            14, 21, 23,         //BL2

            //last vertex is equal
            // to vertex 9 to map a
            // unique colour

            4, 5, 24,           //FR        45      -90
            4, 8, 24,           //FR

            //last vertex is equal
            // to vertex 7 to map a
            // unique colour
            6, 20, 25,          //FL        45      90
            21, 20, 25          //FL

    };
    private FloatBuffer mColorBuffer;
    private ByteBuffer mIndexBuffer;
    private FloatBuffer mVertexBuffer;
    private HashMap<String, Integer> verticeMap;
    private float vertices[] = {
            po, po, ps,
            po, no, ps,
            no, po, ps,
            no, no, ps,

            po, ps, po,
            po, ps, no,
            no, ps, po,
            no, ps, no,

            ps, po, po,
            ps, po, no,
            ps, no, po,
            ps, no, no,

            po, po, ns,
            po, no, ns,
            no, po, ns,
            no, no, ns,

            po, ns, po,
            po, ns, no,
            no, ns, po,
            no, ns, no,

            ns, po, po,
            ns, po, no,
            ns, no, po,
            ns, no, no,

            //bonus vertices to map unique colors
            //we have only 24 regular vertices, but 26 faces
            ps, po, no, //copy from vertex 9
            no, ps, no  //copy from vertex 7
    };

    public Rhombicuboctahedron() {
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuf.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(colors.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mColorBuffer = byteBuf.asFloatBuffer();
        mColorBuffer.put(colors);
        mColorBuffer.position(0);

        mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);

        verticeMap = new HashMap<>();
        int a = 0;
        verticeMap.put("TFR", a++);
        verticeMap.put("T", a++);
        verticeMap.put("TFL", a++);
        verticeMap.put("TS", a++);
        verticeMap.put("TF", a++);
        verticeMap.put("BFR", a++);
        verticeMap.put("F", a++);
        verticeMap.put("BFL", a++);
        verticeMap.put("TR", a++);
        verticeMap.put("R", a++);
        verticeMap.put("TSR", a++);
        verticeMap.put("BR", a++);
        verticeMap.put("BF", a++);
        verticeMap.put("BSR", a++);
        verticeMap.put("B", a++);
        verticeMap.put("BSL", a++);
        verticeMap.put("SR", a++);
        verticeMap.put("BS", a++);
        verticeMap.put("S", a++);
        verticeMap.put("SL", a++);
        verticeMap.put("TL", a++);
        verticeMap.put("L", a++);
        verticeMap.put("TSL", a++);
        verticeMap.put("BL", a++);
        verticeMap.put("FR", a++);
        verticeMap.put("FL", a);

        for (int i = 0; i < verticeMap.size(); i++) {
            mColorBuffer.put(i * 4, 1.f);
            mColorBuffer.put(i * 4 + 1, .5f);
            mColorBuffer.put(i * 4 + 2, .5f);
            mColorBuffer.put(i * 4 + 3, .5f);
        }

    }

    public void draw(GL10 gl) {
        gl.glFrontFace(GL10.GL_CW);
        gl.glShadeModel(GL10.GL_FLAT);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE,
                mIndexBuffer);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    }

    protected String PitchRollToString(float pitch, float roll) {
        String retval = "";

        if (Math.abs(pitch) <= 67.5f && Math.abs(roll) <= 67.5f) {
            retval += "T";
        } else if (Math.abs(pitch) <= 67.5f && Math.abs(roll) >= 112.5) {
            retval += "B";
        }

        if (pitch > 22.5f) {
            retval += "F";
        } else if (pitch < -22.5f) {
            retval += "S";
        }

        if (roll < -22.5f && roll > -157.5f && Math.abs(pitch) < 67.5f) {
            retval += "R";
        } else if (roll > 22.5f && roll < 157.5f && Math.abs(pitch) < 67.5f) {
            retval += "L";
        }

        if (!retval.equals("")) {
            int index = verticeMap.get(retval);

            mColorBuffer.put(index * 4, .5f);
            mColorBuffer.put(index * 4 + 1, 1.0f);
            mColorBuffer.put(index * 4 + 2, .5f);
            mColorBuffer.put(index * 4 + 3, .5f);
        }


        return retval;
    }
}
