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

package net.proest.lp2go3;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

public class VisualLog {

    private static TextView txtDebugLog;
    private static Activity activity;

    public static void setDebugLogTextView(TextView txtDebugLog) {
        VisualLog.txtDebugLog = txtDebugLog;
    }

    public static void setActivity(Activity activity) {
        VisualLog.activity = activity;
    }

    static public final void d(String tag, String msg) {
        Log.d(tag, msg);
        printToDebug(tag, msg);
    }

    static public final void d(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
        printToDebug(tag, msg);
        printToDebug(tr.getClass().getSimpleName(), Log.getStackTraceString(tr));
    }

    static public final void w(String tag, String msg) {
        Log.w(tag, msg);
        printToDebug(tag, msg);
    }

    static public final void e(String tag, String msg) {
        Log.e(tag, msg);
        printToDebug(tag, msg);
    }

    static public final void e(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
        printToDebug(tag, msg);
        printToDebug(tr.getClass().getSimpleName(), Log.getStackTraceString(tr));
    }

    static public final void e(Throwable tr) {
        VisualLog.e(tr.getClass().getSimpleName(), tr.getMessage(), tr);
    }

    static public final void i(String tag, String msg) {
        Log.i(tag, msg);
        printToDebug(tag, msg);
    }

    static private void printToDebug(final String t, final String s) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (txtDebugLog != null) {
                    txtDebugLog.append(t + " " + s + "\n");
                }
            }
        });
    }
}
