package org.librepilot.lp2go.controller;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.librepilot.lp2go.H;
import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.uavtalk.UAVTalkMissingObjectException;

import java.util.HashMap;

public abstract class ViewController {

    public static final int VIEW_3DMAG = 10;
    public static final int VIEW_ABOUT = 7;
    public static final int VIEW_DEBUG = 8;
    public static final int VIEW_LOGS = 5;
    public static final int VIEW_MAIN = 0;
    public static final int VIEW_MAP = 1;
    public static final int VIEW_OBJECTS = 2;
    public static final int VIEW_PID = 3;
    public static final int VIEW_SCOPE = 9;
    public static final int VIEW_SETTINGS = 6;
    public static final int VIEW_VPID = 4;
    protected final HashMap<String, Object> mOffset;
    protected boolean mBlink;
    protected int mFlightSettingsVisible;
    protected int mLocalSettingsVisible;
    protected int mTitle;
    private MainActivity mActivity;

    ViewController(MainActivity activity, int title, int localSettingsVisible,
                   int flightSettingsVisible) {
        this.mActivity = activity;
        mOffset = new HashMap<String, Object>();
        mBlink = false;
        this.mLocalSettingsVisible = localSettingsVisible;
        this.mFlightSettingsVisible = flightSettingsVisible;
        this.mTitle = title;
    }

    protected MainActivity getMainActivity() {
        return this.mActivity;
    }

    public void enter(int view) {
        enter(view, false);
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

    public void update() {
        mBlink = !mBlink;
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
        } catch (NumberFormatException e) {
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
        switch (color) {
            case "OK":
            case "None":
            case "Connected":
                t.setBackground(ContextCompat.getDrawable(mActivity,
                        R.drawable.rounded_corner_ok));
                break;
            case "Warning":
            case "HandshakeReq":
            case "HandshakeAck":
                t.setBackground(ContextCompat.getDrawable(mActivity,
                        R.drawable.rounded_corner_warning));
                break;
            case "Error":
                t.setBackground(ContextCompat.getDrawable(mActivity,
                        R.drawable.rounded_corner_error));
                break;
            case "Critical":
            case "RebootRequired":
            case "Disconnected":
                t.setBackground(ContextCompat.getDrawable(mActivity,
                        R.drawable.rounded_corner_critical));
                break;
            case "Uninitialised":
                t.setBackground(ContextCompat.getDrawable(mActivity,
                        R.drawable.rounded_corner_unini));
                break;
            case "InProgress":
                t.setBackground(ContextCompat.getDrawable(mActivity,
                        R.drawable.rounded_corner_inprogress));
                break;
            case "Completed":
                t.setBackground(ContextCompat.getDrawable(mActivity,
                        R.drawable.rounded_corner_completed));
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

    Object getData(String objectname, String fieldname, boolean request) {
        try {
            if (request) {
                mActivity.mFcDevice.requestObject(objectname);
            }
            return getData(objectname, fieldname);
        } catch (NullPointerException e) {
            //e.printStackTrace();
        }
        return "";
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
}
