package net.proest.lp2go3.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

@SuppressLint("ShowToast")
public class SingleToast {

    private static Toast mToast;

    public static Toast makeText(Context context, String text, int duration) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(context, text, duration);
        return mToast;
    }

    public static Toast makeText(Context context, int text, int duration) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(context, text, duration);
        return mToast;
    }
}