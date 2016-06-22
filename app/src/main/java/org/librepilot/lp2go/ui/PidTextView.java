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
package org.librepilot.lp2go.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

import org.librepilot.lp2go.H;
import org.librepilot.lp2go.uavtalk.UAVTalkXMLObject;

import java.text.DecimalFormat;

public class PidTextView extends AutoResizeTextView {
    private String mDecimalFormatString;
    private int mDenom;
    private String mDialogTitle;
    private String mElement;
    private String mField;
    private int mFieldType;
    private int mMax;
    private int mStep;
    private boolean mUpdateAllowed = true;


    public PidTextView(Context context) {
        super(context);
    }

    public PidTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PidTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getDenom() {
        return mDenom;
    }

    public String getDfs() {
        return mDecimalFormatString;
    }

    public String getDialogTitle() {
        return mDialogTitle;
    }

    public String getElement() {
        return mElement;
    }

    public String getField() {
        return mField;
    }

    public int getFieldType() {
        return mFieldType;
    }

    public int getMax() {
        return mMax;
    }

    public int getStep() {
        return mStep;
    }

    public void setText(String s) {
        if (mUpdateAllowed) {
            super.setText(s);
            mUpdateAllowed = false;
        }

        if (getText().toString().equals(s)) {
            setTextColor(Color.argb(0xff, 0x33, 0x33, 0x33));
        } else {
            setTextColor(Color.argb(0xff, 0xff, 0x00, 0x00));
        }
    }

    public synchronized void setTextOverride(String s) {
        allowUpdate();
        setText(s);
    }

    public void init(int denominator, int max, int step, String dfs, String title, String field,
                     String element, int fieldType) {
        this.mDenom = denominator;
        this.mStep = step;
        this.mMax = max;
        this.mDecimalFormatString = dfs;
        this.mDialogTitle = title;
        this.mElement = element;
        this.mField = field;
        this.mFieldType = fieldType;
    }

    public void init(int denominator, int max, int step, String dfs, String title, String field,
                     String element) {
        init(denominator, max, step, dfs, title, field, element,
                UAVTalkXMLObject.FIELDTYPE_FLOAT32);
    }

    public void allowUpdate() {
        mUpdateAllowed = true;
    }

    public String getDecimalString(float v) {
        DecimalFormat df = new DecimalFormat(mDecimalFormatString);
        return df.format(v).replace(H.NS, H.S);
    }
}
