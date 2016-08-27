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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class CartesianCoordinateSystem {
    final float n = 0.0f;
    final float o = 1.0f;
    CartesianCoordinateSystemAxis x, y, z;

    public CartesianCoordinateSystem() {
        float d = 250.0f;
        float m = -2.5f;
        float p = 2.5f;
        float[] verticesX = {
                m, m / d, m / d,
                p, m / d, m / d,
                p, p / d, m / d,
                m, p / d, m / d,
                m, m / d, p / d,
                p, m / d, p / d,
                p, p / d, p / d,
                m, p / d, p / d
        };
        float[] colorsX = {
                o, n, n, o,
                o, n, n, o,
                o, n, n, o,
                o, n, n, o,
                o, n, n, o,
                o, n, n, o,
                o, n, n, o,
                o, n, n, o
        };
        x = new CartesianCoordinateSystemAxis(verticesX, colorsX);
        float[] verticesY = {
                m / d, m, m / d,
                p / d, m, m / d,
                p / d, p, m / d,
                m / d, p, m / d,
                m / d, m, p / d,
                p / d, m, p / d,
                p / d, p, p / d,
                m / d, p, p / d
        };
        float[] colorsY = {
                n, o, n, o,
                n, o, n, o,
                n, o, n, o,
                n, o, n, o,
                n, o, n, o,
                n, o, n, o,
                n, o, n, o,
                n, o, n, o
        };
        y = new CartesianCoordinateSystemAxis(verticesY, colorsY);
        float[] colorsZ = {
                n, n, o, o,
                n, n, o, o,
                n, n, o, o,
                n, n, o, o,
                n, n, o, o,
                n, n, o, o,
                n, n, o, o,
                n, n, o, o
        };
        float[] verticesZ = {
                m / d, m / d, m,
                p / d, m / d, m,
                p / d, p / d, m,
                m / d, p / d, m,
                m / d, m / d, p,
                p / d, m / d, p,
                p / d, p / d, p,
                m / d, p / d, p
        };
        z = new CartesianCoordinateSystemAxis(verticesZ, colorsZ);
    }

    public void draw(GL10 gl) {
        x.draw(gl);
        y.draw(gl);
        z.draw(gl);
    }

    private class CartesianCoordinateSystemAxis {

        private final byte indices[] = {
                0, 4, 5, 0, 5, 1,
                1, 5, 6, 1, 6, 2,
                2, 6, 7, 2, 7, 3,
                3, 7, 4, 3, 4, 0,
                4, 7, 6, 4, 6, 5,
                3, 0, 1, 3, 1, 2
        };

        private final FloatBuffer mVertexBuffer;
        private FloatBuffer mColorBuffer;
        private ByteBuffer mIndexBuffer;

        public CartesianCoordinateSystemAxis(float[] vertices, float[] colors) {

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

            gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, 10.0f);

            gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE,
                    mIndexBuffer);

            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            //gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
            gl.glPopMatrix();
        }
    }
}

