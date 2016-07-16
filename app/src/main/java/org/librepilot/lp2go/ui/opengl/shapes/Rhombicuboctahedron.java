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

package org.librepilot.lp2go.ui.opengl.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

public class Rhombicuboctahedron {

    private final float no = -1.0f;
    private final float po = 1.0f;
    private final float ps = 1.0f + (float) Math.sqrt(1.0f);
    private final float ns = ps * (-1);
    private float alpha = 0.4f;
    private float colors[] = {
            //Top           *
            //Bottom        o
            //Front         ^
            //Stern         v
            //Left          <
            //Right         >

            0.7f, 0.5f, 0.7f, alpha,       //TFR
            0.9f, 0.9f, 0.9f, alpha,       //T
            0.5f, 0.7f, 0.7f, alpha,       //TFL
            1.0f, 0.7f, 0.7f, alpha,       //TS
            0.7f, 0.7f, 1.0f, alpha,       //TF
            0.5f, 0.0f, 0.7f, alpha,       //BFR
            0.5f, 0.5f, 1.0f, alpha,       //F
            0.1f, 0.5f, 0.7f, alpha,       //BFL
            0.9f, 0.5f, 0.9f, alpha,       //TR
            0.5f, 0.0f, 0.5f, alpha,       //R
            1.0f, 0.5f, 0.7f, alpha,       //TSR
            0.2f, 0.0f, 0.2f, alpha,       //BR
            0.2f, 0.2f, 1.0f, alpha,       //BF
            0.7f, 0.0f, 0.2f, alpha,       //BSR
            0.1f, 0.1f, 0.1f, alpha,       //B
            0.5f, 0.5f, 0.2f, alpha,       //BSL
            0.9f, 0.0f, 0.4f, alpha,       //SR
            1.0f, 0.2f, 0.2f, alpha,       //BS
            1.0f, 0.5f, 0.5f, alpha,       //S
            0.7f, 0.7f, 0.5f, alpha,       //SL
            0.5f, 0.9f, 0.5f, alpha,       //TL
            0.5f, 0.7f, 0.5f, alpha,       //L
            1.0f, 1.0f, 0.7f, alpha,       //TSL
            0.2f, 0.5f, 0.2f, alpha,       //BL
            0.5f, 0.0f, 0.8f, alpha,       //FR
            0.5f, 0.7f, 1.0f, alpha        //FL
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

            11, 17, 13,         //BSR       -45     -135

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

    public String PitchRollToString(float pitch, float roll) {
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
