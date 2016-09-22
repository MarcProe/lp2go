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

package org.librepilot.lp2go.ui.alertdialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import org.librepilot.lp2go.R;
import org.librepilot.lp2go.uavtalk.UAVTalkMissingObjectException;
import org.librepilot.lp2go.ui.SingleToast;

public class EnumInputAlertDialog extends InputAlertDialog {

    private int mChoice = -1;

    public EnumInputAlertDialog(Context parent) {
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

        String[] types;
        try {
            types = mFcDevice.getObjectTree().getXmlObjects().get(mObject).getFields()
                    .get(mField).getOptions();
        } catch (NullPointerException e) {
            types = null;
        }


        int current = 0;
        try {
            String type =
                    mFcDevice.getObjectTree().getData(mObject, 0, mField, mElement).toString();
            if (type != null && types != null) {
                for (String t : types) {
                    if (t.equals(type)) {
                        break;
                    }
                    current++;
                }
            }
        } catch (UAVTalkMissingObjectException | NumberFormatException | NullPointerException ignored) {
            ignored.printStackTrace();
        }

        dialogBuilder.setSingleChoiceItems(types, current, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mChoice = which;
            }
        });

        dialogBuilder.setPositiveButton(R.string.SAVE_BUTTON,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mChoice >= 0) {
                            process(mChoice);
                            mFcDevice.savePersistent(mObject);
                        }

                        dialog.dismiss();
                    }
                });
        dialogBuilder.setNeutralButton(R.string.UPLOAD_BUTTON,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mChoice >= 0) {
                            process(mChoice);
                        }
                        dialog.dismiss();
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

    private void process(int which) {

        byte[] send = new byte[1];
        send[0] = (byte) which;
        if (mFcDevice != null) {
            mFcDevice.sendSettingsObject(mObject, 0, mField, mElement, send);
            //mUAVTalkDevice.savePersistent("RevoSettings");
        }
    }
}
