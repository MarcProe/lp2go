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

package net.proest.lp2go3.ui.alertdialog;

import android.app.AlertDialog;
import android.content.Context;

import net.proest.lp2go3.uavtalk.device.FcDevice;

public abstract class InputAlertDialog extends AlertDialog {
    String mElement = "0";
    FcDevice mFcDevice;
    String mField;
    int mFieldType;
    int mLayout;
    long mMax = -1;
    long mMin = -1;
    String mObject;
    String mText;
    String mTitle;

    InputAlertDialog(Context parent) {
        super(parent);
    }

    public InputAlertDialog withMinMax(int min, int max) {
        this.mMax = max;
        this.mMin = min;
        return this;
    }

    public InputAlertDialog withFieldType(int fieldtype) {
        this.mFieldType = fieldtype;
        return this;
    }

    public InputAlertDialog withElement(String element) {
        this.mElement = element;
        return this;
    }

    public InputAlertDialog withField(String field) {
        this.mField = field;
        return this;
    }

    public InputAlertDialog withObject(String object) {
        this.mObject = object;
        return this;
    }

    public InputAlertDialog withUavTalkDevice(FcDevice fcDevice) {
        this.mFcDevice = fcDevice;
        return this;
    }

    public InputAlertDialog withPresetText(String text) {
        this.mText = text;
        return this;
    }

    public InputAlertDialog withLayout(int layout) {
        this.mLayout = layout;
        return this;
    }

    public InputAlertDialog withTitle(String title) {
        this.mTitle = title;
        return this;
    }

    public abstract void show();
}
