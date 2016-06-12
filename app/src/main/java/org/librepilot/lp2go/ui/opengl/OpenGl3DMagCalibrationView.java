package org.librepilot.lp2go.ui.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.AttributeSet;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import org.librepilot.lp2go.uavtalk.device.FcDevice;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGl3DMagCalibrationView extends GLSurfaceView {

    private final OpenGLRenderer mRenderer;
    private FcDevice mFcDevice;


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

    public void setFcDevice(FcDevice fcDevice) {
        this.mFcDevice = fcDevice;
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

}

class OpenGLRenderer implements GLSurfaceView.Renderer {

    protected float mPitch = 0;
    protected float mRoll = 0;
    protected float mYaw = 0;

    private Rhombicuboctahedron mRhombicuboctahedron = new Rhombicuboctahedron();

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
            0.75f, 0.5f, 0.75f, alpha,       //TNE
            0.9f, 0.9f, 0.9f, alpha,         //T
            0.5f, 0.75f, 0.75f, alpha,       //TNW
            1.0f, 0.75f, 0.75f, alpha,       //TS
            0.75f, 0.75f, 1.0f, alpha,       //TN
            .5f, .0f, .7f, alpha,            //BNE
            0.5f, 0.5f, 1.0f, alpha,         //N
            0.1f, .5f, .7f, alpha,           //BNW
            0.9f, 0.5f, 0.9f, alpha,         //TE
            0.5f, 0.0f, 0.5f, alpha,         //E
            1.0f, 0.5f, 0.75f, alpha,        //TSE
            0.25f, 0.0f, 0.25f, alpha,       //BE
            .25f, .25f, 1.0f, alpha,         //BN
            0.7f, 0.0f, 0.2f, alpha,         //BSE
            0.1f, 0.1f, 0.1f, alpha,         //B
            0.5f, 0.5f, 0.2f, alpha,         //BSW
            0.9f, 0.0f, 0.4f, alpha,         //SE
            1.0f, 0.25f, 0.25f, alpha,       //BS
            1.0f, 0.5f, 0.5f, alpha,         //S
            0.75f, 0.75f, 0.5f, alpha,       //SW
            0.5f, 0.9f, 0.5f, alpha,         //TW
            0.5f, 0.75f, 0.5f, alpha,        //W
            1.0f, 1.00f, 0.75f, alpha,       //TSW
            0.2f, 0.5f, 0.2f, alpha,         //BW
            0.5f, 0.0f, 0.8f, alpha,         //NE
            0.5f, 0.75f, 1.0f, alpha         //NW
    };
    private byte indices[] = {
            //                              PITCH   ROLL   YAW
            8, 4, 0,            //TNE       45      -45

            0, 2, 1,            // T1       0       0
            3, 2, 1,            // T2

            6, 20, 2,           ///TNW      45      45

            1, 16, 3,           //TS1       -45     0
            18, 16, 3,          //TS2

            0, 2, 4,            // TN1      45      0
            6, 2, 4,            // TN2

            12, 9, 5,           //BNE       45      -135

            4, 5, 6,            // N1       90      X
            7, 5, 6,            // N2

            14, 21, 7,          //BNW       45      135

            0, 1, 8,            //TE1       0       -45     X
            10, 1, 8,           //TE2

            8, 10, 9,           // E1       0       -90      X
            11, 10, 9,          // E2

            1, 16, 10,          //TSE       -45     -45
            //                              PITCH    ROLL   YAW
            13, 12, 11,         //BE1       0       -135
            9, 12, 11,          //BE2

            5, 7, 12,           //BN1       45      +-180
            14, 7, 12,          //BN2

            11, 17, 13,           //BSE     -45     -135

            12, 13, 14,         // B1       0       +-180     X
            15, 13, 14,         // B2

            19, 23, 15,          //BSW       -45     135

            11, 17, 16,         //SE1       -45     -90
            11, 10, 16,         //SE2

            13, 15, 17,         //BS1       -45     +-180
            19, 15, 17,         //BS2

            16, 17, 18,         //S1        -90     X       X
            19, 17, 18,         //S2

            18, 22, 19,         //SW        -45     90
            22, 23, 19,         //SW
            //                              PITCH    ROLL   YAW
            2, 3, 20,           //TW1       0       45      X
            22, 3, 20,          //TW2

            20, 22, 21,         //W1        0       90      X
            23, 22, 21,         //W2

            3, 18, 22,          //TSW       -45     45

            14, 15, 23,         //BW1       0       135
            14, 21, 23,         //BW2

            //last vertex is equal
            // to vertex 9 to map a
            // unique colour

            4, 5, 24,           //NE        45      -90
            4, 8, 24,           //NE

            //last vertex is equal
            // to vertex 7 to map a
            // unique colour
            6, 20, 25,          //NW        45      90
            21, 20, 25          //NW

    };
    private FloatBuffer mColorBuffer;
    private ByteBuffer mIndexBuffer;
    private FloatBuffer mVertexBuffer;
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
}
