package org.librepilot.lp2go.helper;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;

public class CompatHelper {
    public static void setBackground(View v, Context mActivity, int bg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            v.setBackground(ContextCompat.getDrawable(mActivity, bg));
        } else {
            //noinspection deprecation
            v.setBackgroundDrawable(ContextCompat.getDrawable(mActivity, bg));
        }
    }
}
