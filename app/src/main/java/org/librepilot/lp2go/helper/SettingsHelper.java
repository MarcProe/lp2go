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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;

import java.util.HashSet;
import java.util.Set;

public class SettingsHelper {
    public static String mBluetoothDeviceAddress = null;
    public static String mBluetoothDeviceUsed = null;
    public static String mBottomRightLayout;
    public static boolean mColorfulPid = false;
    public static boolean mColorfulVPid = false;
    public static String mLoadedUavo = null;
    public static boolean mLogAsRawUavTalk;
    public static int mSerialModeUsed = -1;
    public static boolean mText2SpeechEnabled = false;
    public static String mTopLeftLayout;
    public static boolean mUseTimestampsFromFc;
    public static Set<String> mObjectFavorites;
    public static int mLogReplaySkipObjects;

    public static void loadSettings(MainActivity mainActivity) {
        SharedPreferences sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
        mSerialModeUsed = sharedPref.getInt(mainActivity
                .getString(R.string.SETTINGS_SERIAL_MODE, R.string.APP_ID), 0);
        mBluetoothDeviceUsed = sharedPref.getString(
                mainActivity.getString(R.string.SETTINGS_BT_NAME, R.string.APP_ID), null);
        mBluetoothDeviceAddress = sharedPref.getString(
                mainActivity.getString(R.string.SETTINGS_BT_MAC, R.string.APP_ID), null);
        mLoadedUavo = sharedPref
                .getString(mainActivity.getString(R.string.SETTINGS_UAVO_SOURCE, R.string.APP_ID),
                        null);
        mColorfulPid = sharedPref.getBoolean(
                mainActivity.getString(R.string.SETTINGS_COLORFUL_PID, R.string.APP_ID), false);
        mColorfulVPid = sharedPref.getBoolean(
                mainActivity.getString(R.string.SETTINGS_COLORFUL_VPID, R.string.APP_ID),
                false);
        mText2SpeechEnabled = sharedPref.getBoolean(
                mainActivity.getString(R.string.SETTINGS_TEXT2SPEECH_ENABLED, R.string.APP_ID),
                false);
        mTopLeftLayout = sharedPref.getString(
                mainActivity.getString(R.string.SETTINGS_TOP_LEFT_LAYOUT_RES, R.string.APP_ID),
                mainActivity.getString(R.string.main_element_health));
        mBottomRightLayout = sharedPref.getString(
                mainActivity.getString(R.string.SETTINGS_BOTTOM_RIGHT_LAYOUT_RES, R.string.APP_ID),
                mainActivity.getString(R.string.main_element_info));
        mLogAsRawUavTalk = sharedPref.getBoolean(
                mainActivity.getString(R.string.SETTINGS_LOG_RAW, R.string.APP_ID),
                false);
        mUseTimestampsFromFc = sharedPref.getBoolean(
                mainActivity.getString(R.string.SETTINGS_TIMESTAMPS_FROM_FC, R.string.APP_ID),
                false);
        mLogReplaySkipObjects = sharedPref.getInt(
                mainActivity.getString(R.string.SETTINGS_LOG_REPLAY_SKIP_OBJECTS, R.string.APP_ID),
                100);
        mObjectFavorites = sharedPref.getStringSet(
                mainActivity.getString(R.string.SETTINGS_OBJECT_FAVORITES, R.string.APP_ID),
                new HashSet<String>());

        String listString = "";

        for (String s : mObjectFavorites) {
            listString += s + "\t";
        }
        VisualLog.d("DSFG L", listString);
    }

    @SuppressLint("CommitPrefEdits")
    public static void saveSettings(MainActivity mainActivity, boolean async) {
        SharedPreferences sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();

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
        editor.putString(mainActivity.getString(R.string.SETTINGS_TOP_LEFT_LAYOUT_RES,
                R.string.APP_ID), mTopLeftLayout);
        editor.putString(mainActivity.getString(R.string.SETTINGS_BOTTOM_RIGHT_LAYOUT_RES,
                R.string.APP_ID), mBottomRightLayout);
        editor.putBoolean(mainActivity.getString(R.string.SETTINGS_LOG_RAW, R.string.APP_ID),
                SettingsHelper.mLogAsRawUavTalk);
        editor.putBoolean(
                mainActivity.getString(R.string.SETTINGS_TIMESTAMPS_FROM_FC, R.string.APP_ID),
                SettingsHelper.mUseTimestampsFromFc);
        editor.putBoolean(
                mainActivity.getString(R.string.SETTINGS_TEXT2SPEECH_ENABLED, R.string.APP_ID),
                SettingsHelper.mText2SpeechEnabled);
        editor.putInt(
                mainActivity.getString(R.string.SETTINGS_LOG_REPLAY_SKIP_OBJECTS, R.string.APP_ID),
                SettingsHelper.mLogReplaySkipObjects);
        editor.putStringSet(
                mainActivity.getString(R.string.SETTINGS_OBJECT_FAVORITES, R.string.APP_ID),
                SettingsHelper.mObjectFavorites);

        String listString = "";

        for (String s : mObjectFavorites) {
            listString += s + "\t";
        }
        VisualLog.d("DSFG S", listString);

        if (async) {
            editor.apply();
        } else {
            editor.commit();
        }
        VisualLog.d("SETTINGS", "Saving.... (" + async + ")");
    }

    public static void saveSettings(MainActivity mainActivity) {
        saveSettings(mainActivity, false);
    }

    public static Spinner initSpinner(int spinnerRes, View parent,
                                      AdapterView.OnItemSelectedListener l, int elementRes,
                                      String selection) {
        Spinner spinner = (Spinner) parent.findViewById(spinnerRes);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parent.getContext(),
                elementRes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (l != null) {
            spinner.setOnItemSelectedListener(l);
        }
        spinner.setSelection(adapter.getPosition(selection));
        return spinner;
    }
}
