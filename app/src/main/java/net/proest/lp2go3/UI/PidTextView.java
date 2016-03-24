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

package net.proest.lp2go3.UI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.proest.lp2go3.R;

public class PidTextView extends TextView {

    GestureDetector mGestureDetector;
    PidSeekBar mPidSeekBar;

    public PidTextView(Context context) {
        super(context);
        mGestureDetector = new GestureDetector(context, new GestureListener());
    }

    public PidTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, new GestureListener());
    }

    public PidTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGestureDetector = new GestureDetector(context, new GestureListener());
    }

    protected PidSeekBar getPidSeekBar() {
        return mPidSeekBar;
    }

    public void setPidSeekBar(PidSeekBar psb) {
        this.mPidSeekBar = psb;
    }

    public void showDialog() {

        AlertDialog.Builder pidTextViewAlertDialog = new AlertDialog.Builder(this.getContext());
        pidTextViewAlertDialog.setTitle(this.mPidSeekBar.getName());

        final EditText input = new EditText(this.getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        pidTextViewAlertDialog.setView(input);
        input.setText(this.getText());
        final PidTextView me = this;

        pidTextViewAlertDialog.setPositiveButton(R.string.OK_BUTTON,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try {
                            float parse = Float.parseFloat(input.getText().toString());
                            String s = mPidSeekBar.getDecimalString(parse);
                            me.setText(s);
                            me.getPidSeekBar().setProgressOverride(parse);
                        } catch (Exception e) {
                            SingleToast.makeText(me.getContext(), getContext().getString(R.string.PID_COULD_NOT_PARSE), Toast.LENGTH_LONG).show();
                        }

                        dialog.dismiss();
                        dialog.cancel();
                    }
                });

        pidTextViewAlertDialog.setNegativeButton(R.string.CANCEL_BUTTON,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        dialog.dismiss();
                    }
                });
        pidTextViewAlertDialog.show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return mGestureDetector.onTouchEvent(e);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            showDialog();
            return true;
        }
    }
}

