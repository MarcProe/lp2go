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
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import net.proest.lp2go3.H;
import net.proest.lp2go3.R;
import net.proest.lp2go3.UAVTalk.UAVTalkXMLObject;
import net.proest.lp2go3.UI.InputFilterMinMax;
import net.proest.lp2go3.UI.SingleToast;
import net.proest.lp2go3.VisualLog;

public class IntegerInputAlertDialog extends InputAlertDialog {

    public IntegerInputAlertDialog(Context parent) {
        super(parent);
    }

    public void show() {
        if (mUavTalkDevice == null) {
            SingleToast.makeText(getContext(), getContext().getString(R.string.NOT_CONNECTED),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(mTitle);

        final View alertView = View.inflate(getContext(), mLayout, null);
        dialogBuilder.setView(alertView);

        final EditText input = (EditText) alertView.findViewById(R.id.etxInput);
        input.setText(mText);
        input.setSelection(mText.length());
        input.requestFocus();
        if (mMin >= 0 && mMax >= 0) {
            input.setFilters(new InputFilter[]{new InputFilterMinMax(getContext(), mMin, mMax)});
        }

        dialogBuilder.setPositiveButton(R.string.SAVE_BUTTON,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        process(input.getText().toString());

                        mUavTalkDevice.savePersistent(mObject);

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
                case UAVTalkXMLObject.FIELDTYPE_UINT8:
                    data = new byte[1];
                    data[0] = H.toBytes(Integer.parseInt(input))[3]; //want the lsb
                    break;
                case UAVTalkXMLObject.FIELDTYPE_UINT32:
                    data = H.toBytes(Integer.parseInt(input));
                    if (data.length == 4) {
                        data = H.reverse4bytes(data);
                    } else {
                        data = H.toBytes(0);
                    }
                    break;
                default:
                    VisualLog.e("IntegerUnputAlertDialog", "Type not implemented!");
                    data = H.toBytes(0);
                    break;
            }

        } catch (NumberFormatException e) {
            data = H.toBytes(0);
        }

        if (mUavTalkDevice != null) {
            mUavTalkDevice.sendSettingsObject(mObject, 0, mField, 0, data);
        }
    }
}
