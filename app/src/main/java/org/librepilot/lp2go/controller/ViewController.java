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

package org.librepilot.lp2go.controller;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.librepilot.lp2go.H;
import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.helper.CompatHelper;
import org.librepilot.lp2go.helper.SettingsHelper;
import org.librepilot.lp2go.menu.MenuItem;
import org.librepilot.lp2go.uavtalk.UAVTalkMetaData;
import org.librepilot.lp2go.uavtalk.UAVTalkMissingObjectException;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public abstract class ViewController {

    public static final int VIEW_3DMAG = 45;
    public static final int VIEW_ABOUT = 70;
    public static final int VIEW_DEBUG = 80;
    public static final int VIEW_LOGS = 50;
    public static final int VIEW_MAIN = 0;
    public static final int VIEW_MAP = 10;
    public static final int VIEW_OBJECTS = 20;
    public static final int VIEW_PID = 30;
    public static final int VIEW_P_TUNING = 35;
    public static final int VIEW_SCOPE = 25;
    public static final int VIEW_SETTINGS = 60;
    public static final int VIEW_VPID = 40;
    protected final HashMap<String, Object> mOffset;
    protected boolean mBlink;
    protected int mFlightSettingsVisible;
    protected int mLocalSettingsVisible;
    protected int mTitle;
    protected Map<Integer, ViewController> mRightMenuItems;
    ViewController mCurrentRightView;
    private MainActivity mActivity;
    private MenuItem mMenuItem;
    private String mPreviousArmedStatus = "";
    private ViewController mFavorite;

    ViewController(MainActivity activity, int title, int icon, int localSettingsVisible,
                   int flightSettingsVisible) {
        this.mActivity = activity;
        mOffset = new HashMap<>();
        mBlink = false;
        this.mLocalSettingsVisible = localSettingsVisible;
        this.mFlightSettingsVisible = flightSettingsVisible;
        this.mTitle = title;
        this.mMenuItem = new MenuItem(getString(mTitle), icon);
        this.mRightMenuItems = new TreeMap<>();
    }

    public String getTitle() {
        return getString(mTitle);
    }

    public abstract int getID();

    protected MainActivity getMainActivity() {
        return this.mActivity;
    }

    public void enter(int view) {
        enter(view, false);
    }

    public ViewController getCurrentRightView() {
        return mCurrentRightView;
    }

    public void setCurrentRightView(ViewController crv) {
        this.mCurrentRightView = crv;
    }

    public void enter(int view, boolean isSubwindow) {

        if (!isSubwindow) {

            mActivity.setContentView(mActivity.mViews.get(view), view);
            //mActivity.setTitle(mActivity.getString(title));
            ActionBar ab = mActivity.getSupportActionBar();
            if (ab != null) {
                ab.setTitle(mTitle);
            }
            mActivity.imgToolbarFlightSettings =
                    (ImageView) findViewById(R.id.imgToolbarFlightSettings);
            if (mActivity.imgToolbarFlightSettings != null) {
                mActivity.imgToolbarFlightSettings.setVisibility(mFlightSettingsVisible);
            }
            mActivity.imgToolbarLocalSettings =
                    (ImageView) findViewById(R.id.imgToolbarLocalSettings);
            if (mActivity.imgToolbarLocalSettings != null) {
                mActivity.imgToolbarLocalSettings.setVisibility(mLocalSettingsVisible);
            }
            mActivity.imgSerial = (ImageView) findViewById(R.id.imgSerial);
            mActivity.imgUavoSanity = (ImageView) findViewById(R.id.imgUavoSanity);

            mActivity.logViewEvent(this);
        }
    }

    protected View findViewById(int res) {
        return mActivity.findViewById(res);
    }

    protected String getString(int res) {
        return mActivity.getString(res);
    }

    protected String getString(int res, int arg) {
        return mActivity.getString(res, arg);
    }

    public void leave() {
    }

    public void init() {

    }

    public void initRightMenu() {

    }

    public Map<Integer, ViewController> getMenuRightItems() {
        return mRightMenuItems;
    }

    public MenuItem getMenuItem() {
        return mMenuItem;
    }

    public void update() {
        mBlink = !mBlink;

        //TTS
        if (SettingsHelper.mText2SpeechEnabled) {
            String statusArmed = getData("FlightStatus", "Armed").toString();
            if (!mPreviousArmedStatus.equals(statusArmed)) {
                mPreviousArmedStatus = statusArmed;
                getMainActivity().getTtsHelper().speakFlush(statusArmed);
            }
        }

    }

    public void reset() {

    }

    public void onToolbarFlightSettingsClick(View v) {
    }

    public void onToolbarLocalSettingsClick(View v) {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    protected String getFloatData(String obj, String field, int b) {
        try {
            Float f1 = H.stringToFloat(getData(obj, field).toString());
            return String.valueOf(H.round(f1, b));
        } catch (NumberFormatException e) {
            return "";
        }
    }

    protected String getFloatOffsetData(String obj, String field, String soffset) {
        try {
            Float f1 = H.stringToFloat(getData(obj, field).toString());
            Float f2 = (Float) mOffset.get(soffset);
            return String.valueOf(H.round(f1 - f2, 2));
        } catch (NumberFormatException | ClassCastException e) {
            return "";
        }
    }

    protected void setText(TextView t, String text) {
        if (text != null && t != null) {
            t.setText(text);
        }
    }

    protected void setTextBGColor(TextView t, String color) {
        if (color == null || color.equals(getString(R.string.EMPTY_STRING))) {
            return;
        }

        final Context c = mActivity.getApplicationContext();

        switch (color) {
            case "OK":
            case "None":
            case "Connected":
                CompatHelper.setBackground(t, c, R.drawable.rounded_corner_ok);
                break;
            case "Warning":
            case "HandshakeReq":
            case "HandshakeAck":
                CompatHelper.setBackground(t, c, R.drawable.rounded_corner_warning);
                break;
            case "Error":
                CompatHelper.setBackground(t, c, R.drawable.rounded_corner_error);
                break;
            case "Critical":
            case "RebootRequired":
            case "Disconnected":
                CompatHelper.setBackground(t, c, R.drawable.rounded_corner_critical);
                break;
            case "Uninitialised":
                CompatHelper.setBackground(t, c, R.drawable.rounded_corner_unini);
                break;
            case "InProgress":
                CompatHelper.setBackground(t, c, R.drawable.rounded_corner_inprogress);
                break;
            case "Completed":
                CompatHelper.setBackground(t, c, R.drawable.rounded_corner_completed);
                break;
        }
    }

    Object getData(String objectname, String fieldname, String elementName, boolean request) {
        try {
            if (request) {
                mActivity.mFcDevice.requestObject(objectname);
            }
            return getData(objectname, fieldname, elementName);
        } catch (NullPointerException e) {
            //e.printStackTrace();
        }
        return "";
    }

    boolean requestMetaData(String objectName) {
        try {
            return mActivity.mFcDevice.requestMetaObject(objectName);
        } catch (NullPointerException e) {
            return false;
        }
    }

    boolean sendMetaObject(UAVTalkMetaData o) {
        return mActivity.mFcDevice.sendMetaObject(o.toMessage((byte) 0x22, false));
    }

    UAVTalkMetaData getMetaData(String objectName) throws NullPointerException {
        try {
            String oId = mActivity.mFcDevice.getObjectTree().getXmlObjects().get(objectName).getId();
            String metaId = H.intToHex((int) (Long.decode("0x" + oId) + 1));  //oID + 1
            return new UAVTalkMetaData(metaId, mActivity.mPollThread.
                    mObjectTree.getObjectFromID(metaId).getInstance(0).getData());
        } catch (NullPointerException e) {
            return null;
        }
    }

    Object getData(String objectname, String fieldname, boolean request) {
        try {
            if (request) {
                mActivity.mFcDevice.requestObject(objectname);
            }
            return getData(objectname, fieldname);
        } catch (NullPointerException e) {
            //e.printStackTrace();
        }
        return null;
    }

    Object getData(String objectname, String fieldname) {
        try {
            Object o = mActivity.mPollThread.mObjectTree.getData(objectname, fieldname);
            if (o != null) {
                return o;
            }
        } catch (UAVTalkMissingObjectException e1) {
            try {
                mActivity.mFcDevice.requestObject(e1.getObjectname(), e1.getInstance());
            } catch (NullPointerException e2) {
                //e2.printStackTrace();
            }
        } catch (NullPointerException e3) {
            //e3.printStackTrace();
        }
        return "";
    }

    Object getData(String objectname, String fieldname, int elementindex) {
        Object o = null;
        try {
            o = mActivity.mPollThread.mObjectTree.getData(objectname, 0, fieldname, elementindex);
        } catch (UAVTalkMissingObjectException e1) {
            try {
                mActivity.mFcDevice.requestObject(e1.getObjectname(), e1.getInstance());
            } catch (NullPointerException e2) {
                e2.printStackTrace();
            }
        } catch (NullPointerException e3) {
            VisualLog.e("ERR", "Object Tree not loaded yet.");
        }
        if (o != null) {
            return o;
        } else {
            return "";
        }
    }

    Object getData(String objectname, String fieldname, String elementname) {
        Object o = null;
        try {
            o = mActivity.mPollThread.mObjectTree.getData(objectname, fieldname, elementname);
        } catch (UAVTalkMissingObjectException e1) {
            try {
                mActivity.mFcDevice.requestObject(e1.getObjectname(), e1.getInstance());
            } catch (NullPointerException e2) {
                e2.printStackTrace();
            }
        } catch (NullPointerException e3) {
            VisualLog.e("ERR", "Object Tree not loaded yet.");
        }
        if (o != null) {
            return o;
        } else {
            return "";
        }
    }

    protected Float toFloat(Object o) {
        try {
            return (Float) o;
        } catch (ClassCastException e) {
            return .0f;
        }
    }

    public ViewController getFavorite() {
        return mFavorite;
    }

    public void setFavorite(int id) {
        if (this.mFavorite != null) {
            SettingsHelper.mRightMenuFavorites.remove(String.valueOf(this.mFavorite.getID()));
        }
        this.mFavorite = mRightMenuItems.get(id);
        SettingsHelper.mRightMenuFavorites.add(String.valueOf(this.mFavorite.getID()));
        SettingsHelper.saveSettings(getMainActivity(), true);
    }
}
