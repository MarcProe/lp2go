/*
 * @file   SettingsHelper.java
 * @author The LibrePilot Project, http://www.librepilot.org Copyright (C) 2016.
 * @see    The GNU Public License (GPL) Version 3
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
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

import static org.librepilot.lp2go.R.string.APP_ID;

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
    public static boolean mCollectUsageStatistics;
    public static Set<String> mRightMenuFavorites;
    public static boolean mColorfulResp;
    public static float mMagCalBiasX;
    public static float mMagCalBiasY;
    public static float mMagCalBiasZ;
    public static float mMagCalTransformR0C0;
    public static float mMagCalTransformR1C1;
    public static float mMagCalTransformR2C2;
    public static float mAuxMagCalBiasX;
    public static float mAuxMagCalBiasY;
    public static float mAuxMagCalBiasZ;
    public static float mAuxMagCalTransformR0C0;
    public static float mAuxMagCalTransformR1C1;
    public static float mAuxMagCalTransformR2C2;

    @SuppressLint({"CommitPrefEdits", "StringFormatMatches"})
    public static void loadSettings(MainActivity ma) {
        SharedPreferences sharedPref = ma.getPreferences(Context.MODE_PRIVATE);
        mSerialModeUsed = sharedPref.getInt(ma
                .getString(R.string.SETTINGS_SERIAL_MODE, APP_ID), 0);
        mBluetoothDeviceUsed = sharedPref.getString(
                ma.getString(R.string.SETTINGS_BT_NAME, APP_ID), null);
        mBluetoothDeviceAddress = sharedPref.getString(
                ma.getString(R.string.SETTINGS_BT_MAC, APP_ID), null);
        mLoadedUavo = sharedPref
                .getString(ma.getString(R.string.SETTINGS_UAVO_SOURCE, APP_ID),
                        null);
        mColorfulPid = sharedPref.getBoolean(
                ma.getString(R.string.SETTINGS_COLORFUL_PID, APP_ID), false);
        mColorfulVPid = sharedPref.getBoolean(
                ma.getString(R.string.SETTINGS_COLORFUL_VPID, APP_ID),
                false);
        mColorfulResp = sharedPref.getBoolean(
                ma.getString(R.string.SETTINGS_COLORFUL_RESP, APP_ID), false);
        mText2SpeechEnabled = sharedPref.getBoolean(
                ma.getString(R.string.SETTINGS_TEXT2SPEECH_ENABLED, APP_ID),
                false);
        mTopLeftLayout = sharedPref.getString(
                ma.getString(R.string.SETTINGS_TOP_LEFT_LAYOUT_RES, APP_ID),
                ma.getString(R.string.main_element_health));
        mBottomRightLayout = sharedPref.getString(
                ma.getString(R.string.SETTINGS_BOTTOM_RIGHT_LAYOUT_RES, APP_ID),
                ma.getString(R.string.main_element_info));
        mLogAsRawUavTalk = sharedPref.getBoolean(
                ma.getString(R.string.SETTINGS_LOG_RAW, APP_ID),
                false);
        mUseTimestampsFromFc = sharedPref.getBoolean(
                ma.getString(R.string.SETTINGS_TIMESTAMPS_FROM_FC, APP_ID),
                false);
        mLogReplaySkipObjects = sharedPref.getInt(
                ma.getString(R.string.SETTINGS_LOG_REPLAY_SKIP_OBJECTS, APP_ID),
                100);
        mObjectFavorites = sharedPref.getStringSet(
                ma.getString(R.string.SETTINGS_OBJECT_FAVORITES, APP_ID),
                new HashSet<String>());
        mCollectUsageStatistics = sharedPref.getBoolean(
                ma.getString(R.string.SETTINGS_COLLECT_USAGE_STATS, APP_ID),
                false);  //Default for statistics is false, this is opt-in
        mRightMenuFavorites = sharedPref.getStringSet(
                ma.getString(R.string.SETTINGS_RIGHT_MENU_FAVORITES, APP_ID),
                new HashSet<String>());
        mMagCalBiasX = sharedPref.getFloat(
                ma.getString(R.string.SETTINGS_MAGCAL_BIAS_X, APP_ID), .0f);
        mMagCalBiasY = sharedPref.getFloat(
                ma.getString(R.string.SETTINGS_MAGCAL_BIAS_Y, APP_ID), .0f);
        mMagCalBiasZ = sharedPref.getFloat(
                ma.getString(R.string.SETTINGS_MAGCAL_BIAS_Z, APP_ID), .0f);
        mMagCalTransformR0C0 = sharedPref.getFloat(
                ma.getString(R.string.SETTINGS_MAGCAL_TRANSFORM_R0C0, APP_ID), .0f);
        mMagCalTransformR1C1 = sharedPref.getFloat(
                ma.getString(R.string.SETTINGS_MAGCAL_TRANSFORM_R1C1, APP_ID), .0f);
        mMagCalTransformR2C2 = sharedPref.getFloat(
                ma.getString(R.string.SETTINGS_MAGCAL_TRANSFORM_R1C1, APP_ID), .0f);

        mAuxMagCalBiasX = sharedPref.getFloat(
                ma.getString(R.string.SETTINGS_AUX_MAGCAL_BIAS_X, APP_ID), .0f);
        mAuxMagCalBiasY = sharedPref.getFloat(
                ma.getString(R.string.SETTINGS_AUX_MAGCAL_BIAS_Y, APP_ID), .0f);
        mAuxMagCalBiasZ = sharedPref.getFloat(
                ma.getString(R.string.SETTINGS_AUX_MAGCAL_BIAS_Z, APP_ID), .0f);
        mAuxMagCalTransformR0C0 = sharedPref.getFloat(
                ma.getString(R.string.SETTINGS_AUX_MAGCAL_TRANSFORM_R0C0, APP_ID), .0f);
        mAuxMagCalTransformR1C1 = sharedPref.getFloat(
                ma.getString(R.string.SETTINGS_AUX_MAGCAL_TRANSFORM_R1C1, APP_ID), .0f);
        mAuxMagCalTransformR2C2 = sharedPref.getFloat(
                ma.getString(R.string.SETTINGS_AUX_MAGCAL_TRANSFORM_R1C1, APP_ID), .0f);
    }

    @SuppressLint({"CommitPrefEdits", "StringFormatMatches"})
    public static void saveSettings(MainActivity ma, boolean async) {
        SharedPreferences sharedPref = ma.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();

        editor.putString(ma.getString(R.string.SETTINGS_BT_MAC, APP_ID),
                mBluetoothDeviceAddress);
        editor.putString(ma.getString(R.string.SETTINGS_BT_NAME, APP_ID),
                mBluetoothDeviceUsed);
        editor.putString(ma.getString(R.string.SETTINGS_UAVO_SOURCE, APP_ID),
                mLoadedUavo);
        editor.putInt(ma.getString(R.string.SETTINGS_SERIAL_MODE, APP_ID),
                mSerialModeUsed);
        editor.putBoolean(ma.getString(R.string.SETTINGS_COLORFUL_PID, APP_ID),
                SettingsHelper.mColorfulPid);
        editor.putBoolean(ma.getString(R.string.SETTINGS_COLORFUL_VPID, APP_ID),
                SettingsHelper.mColorfulVPid);
        editor.putBoolean(ma.getString(R.string.SETTINGS_COLORFUL_RESP, APP_ID),
                SettingsHelper.mColorfulResp);
        editor.putString(ma.getString(R.string.SETTINGS_TOP_LEFT_LAYOUT_RES,
                APP_ID), mTopLeftLayout);
        editor.putString(ma.getString(R.string.SETTINGS_BOTTOM_RIGHT_LAYOUT_RES,
                APP_ID), mBottomRightLayout);
        editor.putBoolean(ma.getString(R.string.SETTINGS_LOG_RAW, APP_ID),
                SettingsHelper.mLogAsRawUavTalk);
        editor.putBoolean(
                ma.getString(R.string.SETTINGS_TIMESTAMPS_FROM_FC, APP_ID),
                SettingsHelper.mUseTimestampsFromFc);
        editor.putBoolean(
                ma.getString(R.string.SETTINGS_TEXT2SPEECH_ENABLED, APP_ID),
                SettingsHelper.mText2SpeechEnabled);
        editor.putInt(
                ma.getString(R.string.SETTINGS_LOG_REPLAY_SKIP_OBJECTS, APP_ID),
                SettingsHelper.mLogReplaySkipObjects);
        editor.putStringSet(
                ma.getString(R.string.SETTINGS_OBJECT_FAVORITES, APP_ID),
                SettingsHelper.mObjectFavorites);
        editor.putBoolean(
                ma.getString(R.string.SETTINGS_COLLECT_USAGE_STATS, APP_ID),
                SettingsHelper.mCollectUsageStatistics);
        editor.putStringSet(
                ma.getString(R.string.SETTINGS_RIGHT_MENU_FAVORITES, APP_ID),
                SettingsHelper.mRightMenuFavorites);
        editor.putFloat(
                ma.getString(R.string.SETTINGS_MAGCAL_BIAS_X, APP_ID), SettingsHelper.mMagCalBiasX);
        editor.putFloat(
                ma.getString(R.string.SETTINGS_MAGCAL_BIAS_Y, APP_ID), SettingsHelper.mMagCalBiasY);
        editor.putFloat(
                ma.getString(R.string.SETTINGS_MAGCAL_BIAS_Z, APP_ID), SettingsHelper.mMagCalBiasZ);
        editor.putFloat(
                ma.getString(R.string.SETTINGS_MAGCAL_TRANSFORM_R0C0, APP_ID),
                SettingsHelper.mMagCalTransformR0C0);
        editor.putFloat(
                ma.getString(R.string.SETTINGS_MAGCAL_TRANSFORM_R1C1, APP_ID),
                SettingsHelper.mMagCalTransformR1C1);
        editor.putFloat(
                ma.getString(R.string.SETTINGS_MAGCAL_TRANSFORM_R2C2, APP_ID),
                SettingsHelper.mMagCalTransformR2C2);

        editor.putFloat(
                ma.getString(R.string.SETTINGS_AUX_MAGCAL_BIAS_X, APP_ID), SettingsHelper.mAuxMagCalBiasX);
        editor.putFloat(
                ma.getString(R.string.SETTINGS_AUX_MAGCAL_BIAS_Y, APP_ID), SettingsHelper.mAuxMagCalBiasY);
        editor.putFloat(
                ma.getString(R.string.SETTINGS_AUX_MAGCAL_BIAS_Z, APP_ID), SettingsHelper.mAuxMagCalBiasZ);
        editor.putFloat(
                ma.getString(R.string.SETTINGS_AUX_MAGCAL_TRANSFORM_R0C0, APP_ID),
                SettingsHelper.mAuxMagCalTransformR0C0);
        editor.putFloat(
                ma.getString(R.string.SETTINGS_AUX_MAGCAL_TRANSFORM_R1C1, APP_ID),
                SettingsHelper.mAuxMagCalTransformR1C1);
        editor.putFloat(
                ma.getString(R.string.SETTINGS_AUX_MAGCAL_TRANSFORM_R2C2, APP_ID),
                SettingsHelper.mAuxMagCalTransformR2C2);

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
