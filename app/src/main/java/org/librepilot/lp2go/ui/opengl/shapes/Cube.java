package org.librepilot.lp2go.ui.opengl.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Cube {

    final public double alpha, beta;
    final public float x, y, z;
    private final float colors[] = {
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f
    };
    private final byte indices[] = {
            0, 4, 5, 0, 5, 1,
            1, 5, 6, 1, 6, 2,
            2, 6, 7, 2, 7, 3,
            3, 7, 4, 3, 4, 0,
            4, 7, 6, 4, 6, 5,
            3, 0, 1, 3, 1, 2
    };
    private final float n = -0.1f;
    private final float p = 0.1f;
    private final float vertices[] = {
            n, n, n,
            p, n, n,
            p, p, n,
            n, p, n,
            n, n, p,
            p, n, p,
            p, p, p,
            n, p, p
    };
    private FloatBuffer mColorBuffer;
    private ByteBuffer mIndexBuffer;
    private FloatBuffer mVertexBuffer;

    public Cube(float x, float y, float z) {

        this.x = x / 300;
        this.y = y / 300;
        this.z = z / 300;


        this.alpha = Math.atan2(x, y) * 180 / Math.PI + 180;
        this.beta = Math.atan2(x, z) * 180 / Math.PI + 180;

        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuf.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        //float[] lightcolor = {.0f, .0f, .0f, 1.0f};

        byteBuf = ByteBuffer.allocateDirect(colors.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mColorBuffer = byteBuf.asFloatBuffer();
        mColorBuffer.put(colors);
        mColorBuffer.position(0);

        mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);
    }

    public void setRGB(float r, float g, float b) {
        mColorBuffer.put(0, r);
        mColorBuffer.put(1, g);
        mColorBuffer.put(2, b);
    }

    public void draw(GL10 gl) {
        gl.glPushMatrix();
        gl.glFrontFace(GL10.GL_CW);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_BLEND);


        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        //gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        //gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, mColorBuffer);
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, mColorBuffer);

        gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, 40.0f);

        gl.glTranslatef(x, y, z);
        gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE,
                mIndexBuffer);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glPopMatrix();
    }
}