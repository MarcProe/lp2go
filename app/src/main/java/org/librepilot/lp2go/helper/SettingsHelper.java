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

package org.librepilot.lp2go.helper;

import android.content.Context;
import android.content.SharedPreferences;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;

public class SettingsHelper {
    public static String mBluetoothDeviceAddress = null;
    public static String mBluetoothDeviceUsed = null;
    public static boolean mColorfulPid = false;
    public static boolean mColorfulVPid = false;
    public static String mLoadedUavo = null;
    public static int mSerialModeUsed = -1;
    public static boolean mText2SpeechEnabled = false;

    public static void loadSettings(MainActivity mainActivity) {
        SharedPreferences sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
        mSerialModeUsed =
                sharedPref.getInt(mainActivity
                        .getString(R.string.SETTINGS_SERIAL_MODE, R.string.APP_ID), 0);
        mBluetoothDeviceUsed =
                sharedPref.getString(
                        mainActivity.getString(R.string.SETTINGS_BT_NAME, R.string.APP_ID), null);
        mBluetoothDeviceAddress =
                sharedPref.getString(
                        mainActivity.getString(R.string.SETTINGS_BT_MAC, R.string.APP_ID), null);
        mLoadedUavo = sharedPref
                .getString(mainActivity.getString(R.string.SETTINGS_UAVO_SOURCE, R.string.APP_ID),
                        null);
        mColorfulPid = sharedPref
                .getBoolean(mainActivity.getString(R.string.SETTINGS_COLORFUL_PID, R.string.APP_ID),
                        false);
        mColorfulVPid = sharedPref
                .getBoolean(
                        mainActivity.getString(R.string.SETTINGS_COLORFUL_VPID, R.string.APP_ID),
                        false);
        mText2SpeechEnabled = sharedPref
                .getBoolean(mainActivity
                                .getString(R.string.SETTINGS_TEXT2SPEECH_ENABLED, R.string.APP_ID),
                        false);
    }

    public static void saveSettings(MainActivity mainActivity) {
        SharedPreferences sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(mainActivity.getString(R.string.SETTINGS_BT_MAC, R.string.APP_ID),
                mBluetoothDeviceAddress);
        editor.putString(mainActivity.getString(R.string.SETTINGS_BT_NAME, R.string.APP_ID),
                mBluetoothDeviceUsed);
        editor.putString(mainActivity.getString(R.string.SETTINGS_UAVO_SOURCE, R.string.APP_ID),
                mLoadedUavo);
        editor.putInt(mainActivity.getString(R.string.SETTINGS_SERIAL_MODE, R.string.APP_ID),
                mSerialModeUsed);
        editor.putBoolean(mainActivity.getString(R.string.SETTINGS_COLORFUL_PID, R.string.APP_ID),
                SettingsHelper.mColorfulPid);
        editor.putBoolean(mainActivity.getString(R.string.SETTINGS_COLORFUL_VPID, R.string.APP_ID),
                SettingsHelper.mColorfulVPid);

        editor.commit();
    }
}
