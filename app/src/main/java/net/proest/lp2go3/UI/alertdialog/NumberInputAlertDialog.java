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

package net.proest.lp2go3.UI.alertdialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import net.proest.lp2go3.H;
import net.proest.lp2go3.R;
import net.proest.lp2go3.UAVTalk.UAVTalkXMLObject;
import net.proest.lp2go3.UI.InputFilterMinMax;
import net.proest.lp2go3.UI.SingleToast;
import net.proest.lp2go3.VisualLog;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class NumberInputAlertDialog extends InputAlertDialog implements TextWatcher {

    private static String str = "";

    public NumberInputAlertDialog(Context parent) {
        super(parent);
    }

    public void show() {
        if (mFcDevice == null) {
            SingleToast.show(getContext(), getContext().getString(R.string.NOT_CONNECTED),
                    Toast.LENGTH_SHORT);
            return;
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(mTitle);

        final View alertView = View.inflate(getContext(), mLayout, null);
        dialogBuilder.setView(alertView);

        final EditText input = (EditText) alertView.findViewById(R.id.etxInput);

        long min = -1;
        long max = -1;

        //max/min values are fallback default if there are none set with .withMinMax
        //.setInputType will block "-" and "."/"," if not appripriate
        switch (mFieldType) {
            case UAVTalkXMLObject.FIELDTYPE_ENUM:
                throw new UnsupportedOperationException();
                //break;
            case UAVTalkXMLObject.FIELDTYPE_UINT8:
                min = 0;
                max = 255;
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case UAVTalkXMLObject.FIELDTYPE_UINT16:
                min = 0;
                max = 65535;
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case UAVTalkXMLObject.FIELDTYPE_UINT32:
                min = 0;
                max = 4294967295L;
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case UAVTalkXMLObject.FIELDTYPE_INT8:
                min = -128;
                max = 127;
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                break;
            case UAVTalkXMLObject.FIELDTYPE_INT16:
                min = -32768;
                max = 32767;
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                break;
            case UAVTalkXMLObject.FIELDTYPE_INT32:
                min = -2147483648;
                max = 2147483647;
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                break;
            case UAVTalkXMLObject.FIELDTYPE_FLOAT32:
                min = -(int) Float.MAX_VALUE; //don't get academic on this.
                max = (long) Float.MAX_VALUE;

                input.setInputType(
                        InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                input.setKeyListener(DigitsKeyListener.getInstance("0123456789" + H.S));
                break;
            default:
                SingleToast.show(getContext(), "Type not implemented", Toast.LENGTH_SHORT);
                break;
        }

        if (this.mMin == -1 && this.mMax == -1) {
            mMin = min;
            mMax = max;
        }

        String fText;
        if (mFieldType == UAVTalkXMLObject.FIELDTYPE_FLOAT32) {
            fText = (new DecimalFormat("##########.#######")).format(H.stringToFloat(mText));
            input.addTextChangedListener(this);
            fText = fText.replace(H.NS, H.S);
        } else {
            fText = mText;
        }


        input.setText(fText);
        input.setSelection(fText.length());
        input.setSelection(0);
        input.requestFocus();
        if (mMin >= 0 && mMax >= 0) {
            input.setFilters(new InputFilter[]{new InputFilterMinMax(getContext(), mMin, mMax)});
        }

        dialogBuilder.setPositiveButton(R.string.SAVE_BUTTON,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        process(input.getText().toString());

                        mFcDevice.savePersistent(mObject);

                        dialog.dismiss();
                    }
                });
        dialogBuilder.setNeutralButton(R.string.UPLOAD_BUTTON,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        process(input.getText().toString());
                    }
                });

        dialogBuilder.setNegativeButton(R.string.CANCEL_BUTTON,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        dialog.dismiss();
                    }
                });
        dialogBuilder.show();
    }

    private void process(String input) {

        byte[] data;
        try {
            switch (mFieldType) {
                case UAVTalkXMLObject.FIELDTYPE_UINT8: {
                    data = new byte[1];
                    int i = Integer.parseInt(input);
                    data[0] = H.toBytes(i)[3]; //want the lsb
                    break;
                }
                case UAVTalkXMLObject.FIELDTYPE_INT8: {
                    data = new byte[1];
                    int i = Integer.parseInt(input);
                    byte b = (byte) i;
                    data[0] = b;
                    break;
                }
                case UAVTalkXMLObject.FIELDTYPE_UINT16: {
                    data = new byte[2];
                    int i = Integer.parseInt(input);
                    byte[] temp = new byte[4];
                    temp = H.toBytes(i);
                    data[0] = temp[3];
                    data[1] = temp[2];
                    break;
                }
                case UAVTalkXMLObject.FIELDTYPE_INT16: {
                    int i = Integer.parseInt(input);
                    short s = (short) i;
                    data = H.toReversedBytes(s);
                    break;
                }
                case UAVTalkXMLObject.FIELDTYPE_UINT32: {
                    data = new byte[4];
                    long l = Long.parseLong(input) & 0xffffff;  //remove higher 4 bytes
                    byte[] temp = H.toBytes(l); //8 bytes
                    data[3] = temp[4];  //TODO:check if that's correct
                    data[2] = temp[5];
                    data[1] = temp[6];
                    data[0] = temp[7];
                    break;
                }
                case UAVTalkXMLObject.FIELDTYPE_INT32: {
                    data = H.toReversedBytes(Integer.parseInt(input));
                    break;
                }
                case UAVTalkXMLObject.FIELDTYPE_FLOAT32: {
                    data = H.reverse4bytes(H.floatToByteArray(Float.parseFloat(input)));
                    break;
                }
                default:
                    data = new byte[0];
                    SingleToast.show(getContext(), "Type not yet implemented!", Toast.LENGTH_SHORT);
                    VisualLog.e("NumberInputAlertDialog",
                            "Type not implemented! " + mFieldType + " " + input + " " +
                                    H.toBytes(Integer.parseInt(input)));
                    break;
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
            data = H.toBytes(0);
        }


        if (mFcDevice != null && data.length > 0) {
            VisualLog.d("IntegerInputDialog",
                    "" + mFieldType + " " + input + " " + H.bytesToHex(data));
            mFcDevice.sendSettingsObject(mObject, 0, mField, mElement, data);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        str = s.toString();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String ls = s.toString();
        if (ls.length() > 0 && !Pattern.matches("^\\d*" + H.RS + "?\\d*$", ls)) {
            s.replace(0, s.length(), str);
        }
    }
}
