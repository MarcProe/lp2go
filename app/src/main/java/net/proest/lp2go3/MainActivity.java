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

/* This file incorporates work covered by the following copyright and
 * permission notice:
 */

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.proest.lp2go3;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import net.proest.lp2go3.UAVTalk.UAVTalkDeviceHelper;
import net.proest.lp2go3.UAVTalk.UAVTalkMissingObjectException;
import net.proest.lp2go3.UAVTalk.UAVTalkObjectTree;
import net.proest.lp2go3.UAVTalk.UAVTalkXMLObject;
import net.proest.lp2go3.UAVTalk.device.FcBluetoothDevice;
import net.proest.lp2go3.UAVTalk.device.FcDevice;
import net.proest.lp2go3.UAVTalk.device.FcUsbDevice;
import net.proest.lp2go3.UI.PidTextView;
import net.proest.lp2go3.UI.SingleToast;
import net.proest.lp2go3.UI.alertdialog.EnumInputAlertDialog;
import net.proest.lp2go3.UI.alertdialog.NumberInputAlertDialog;
import net.proest.lp2go3.UI.alertdialog.PidInputAlertDialog;
import net.proest.lp2go3.UI.objectbrowser.list.ChildString;
import net.proest.lp2go3.UI.objectbrowser.list.ObjectsExpandableListView;
import net.proest.lp2go3.UI.objectbrowser.list.ObjectsExpandableListViewAdapter;
import net.proest.lp2go3.c.PID;
import net.proest.lp2go3.slider.AboutFragment;
import net.proest.lp2go3.slider.DebugFragment;
import net.proest.lp2go3.slider.LogsFragment;
import net.proest.lp2go3.slider.MainFragment;
import net.proest.lp2go3.slider.MapFragment;
import net.proest.lp2go3.slider.ObjectsFragment;
import net.proest.lp2go3.slider.PidFragment;
import net.proest.lp2go3.slider.ScopeFragment;
import net.proest.lp2go3.slider.SettingsFragment;
import net.proest.lp2go3.slider.VerticalPidFragment;
import net.proest.lp2go3.slider.adapter.NavDrawerListAdapter;
import net.proest.lp2go3.slider.model.NavDrawerItem;

import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static final int VIEW_SCOPE = 9;
    protected static final int SERIAL_USB = 1;
    protected static final int SERIAL_BLUETOOTH = 2;
    protected static final String OFFSET_VELOCITY_DOWN = "net.proest.lp2go3.VelocityState-Down";
    protected static final String OFFSET_BAROSENSOR_ALTITUDE = "net.proest.lp2go3.BaroSensor-Altitude";
    protected static final int VIEW_MAIN = 0;
    protected static final int VIEW_MAP = 1;
    protected static final int VIEW_OBJECTS = 2;
    protected static final int VIEW_PID = 3;
    protected static final int VIEW_VPID = 4;
    protected static final int VIEW_LOGS = 5;
    protected static final int VIEW_SETTINGS = 6;
    protected static final int VIEW_ABOUT = 7;
    protected static final int VIEW_DEBUG = 8;
    protected static final int POLL_WAIT_TIME = 500;
    protected static final int POLL_SECOND_FACTOR = 1000 / POLL_WAIT_TIME;
    protected final static int HISTORY_MARKER_NUM = 5;
    private static final int NUM_OF_VIEWS = 10;
    private static final String UAVO_INTERNAL_PATH = "uavo";
    private static final int ICON_OPAQUE = 255;
    private static final int ICON_TRANSPARENT = 64;
    private static final int SERIAL_NONE = 0;
    private static final String ACTION_USB_PERMISSION = "net.proest.lp2go3.USB_PERMISSION";
    private static final int CALLBACK_FILEPICKER = 3456;
    private static final boolean LOCAL_LOGD = true;
    static int mCurrentView = 0;
    private static boolean mHasPThread = false;
    private static boolean mColorfulPid;
    final Marker[] mPosHistory = new Marker[HISTORY_MARKER_NUM];
    public ObjectsExpandableListView mExpListView;
    protected HashSet<PidTextView> mPidTexts;
    protected HashSet<PidTextView> mVerticalPidTexts;
    protected ImageView imgPidBank;
    protected int mCurrentPosMarker = 0;
    protected String mCurrentStabilizationBank;
    protected String mLoadedUavo = null;
    protected long mTxObjects;
    protected long mRxObjectsGood;
    protected long mRxObjectsBad;
    protected int mSerialModeUsed = -1;
    protected HashMap<String, Object> mOffset;
    protected FcDevice mFcDevice;
    protected GoogleMap mMap;
    protected boolean mDoReconnect = false;
    TextView txtObjectLogTx;
    TextView txtObjectLogRxGood;
    TextView txtObjectLogRxBad;
    TextView txtAtti;
    TextView txtPlan;
    TextView txtStab;
    TextView txtPath;
    TextView txtGPS;
    TextView txtGPSSatsInView;
    TextView txtSensor;
    TextView txtAirspd;
    TextView txtMag;
    TextView txtInput;
    TextView txtOutput;
    TextView txtI2C;
    TextView txtTelemetry;
    TextView txtBatt;
    TextView txtTime;
    TextView txtConfig;
    TextView txtBoot;
    TextView txtStack;
    TextView txtMem;
    TextView txtEvent;
    TextView txtCPU;
    TextView txtArmed;
    TextView txtVolt;
    TextView txtAmpere;
    TextView txtmAh;
    TextView txtTimeLeft;
    TextView txtAltitude;
    TextView txtAltitudeAccel;
    TextView txtModeNum;
    TextView txtModeFlightMode;
    TextView txtFlightTime;
    TextView txtModeAssistedControl;
    TextView txtLatitude;
    TextView txtLongitude;
    TextView txtMapGPS;
    TextView txtMapGPSSatsInView;
    TextView txtVehicleName;
    TextView txtHealthAlertDialogBatteryCapacity;
    TextView txtHealthAlertDialogBatteryCells;
    TextView txtHealthAlertDialogFusionAlgorithm;
    ImageView imgFlightTelemetry;
    ImageView imgGroundTelemetry;
    ImageView imgBluetooth;
    ImageView imgUSB;
    ImageView imgPacketsUp;
    ImageView imgPacketsGood;
    ImageView imgPacketsBad;
    TextView txtObjects;
    TextView txtLogFilename;
    TextView txtLogSize;
    TextView txtLogObjects;
    TextView txtLogDuration;
    ImageView imgUavoSanity;
    LineChart lchScope;
    private Map<Integer, View> mViews;
    private Spinner spnUavoSource;
    private Spinner spnConnectionTypeSpinner;
    private Spinner spnBluetoothPairedDevice;
    private CheckBox cbxColorfulPid;
    private ObjectsExpandableListViewAdapter mListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceUsed = null;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private PollThread mPollThread = null;
    private ConnectionThread mConnectionThread = null;
    private UsbManager mUsbManager = null;
    private android.hardware.usb.UsbDevice mDevice;
    private UsbDeviceConnection mDeviceConnection;
    private PendingIntent mPermissionIntent = null;
    private UsbInterface mInterface;
    private Map<String, UAVTalkXMLObject> mXmlObjects = null;
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            VisualLog.d(getString(R.string.USB), action);

            if (mSerialModeUsed == SERIAL_USB && UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                android.hardware.usb.UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                VisualLog.d(getString(R.string.USB), device.getVendorId() +
                        getString(R.string.DASH) +
                        device.getProductId() +
                        getString(R.string.DASH) +
                        device.getDeviceClass() +
                        getString(R.string.SPACE) +
                        device.getDeviceSubclass() +
                        getString(R.string.SPACE) +
                        device.getDeviceProtocol());

                if (device.getDeviceClass() == UsbConstants.USB_CLASS_MISC) {
                    mUsbManager.requestPermission(device, mPermissionIntent);
                }

            } else if (mSerialModeUsed == SERIAL_USB
                    && UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                android.hardware.usb.UsbDevice device =
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    setUsbInterface(null, null);
                    if (mFcDevice != null) {
                        mFcDevice.stop();
                    }
                }
            } else if (mSerialModeUsed == SERIAL_USB && ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    android.hardware.usb.UsbDevice device =
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            UsbInterface intf = findAdbInterface(device);
                            if (intf != null) {
                                setUsbInterface(device, intf);
                            }
                        }
                    } else {
                        VisualLog.d("DBG", "permission denied for device " + mDevice);
                    }
                }
            }
        }
    };
    private MapView mMapView;
    private TextView txtDebugLog;
    private String mUavoLongHash;
    private String mUavoLongHashFc;

    public static boolean hasPThread() {
        return mHasPThread;
    }

    public static void hasPThread(boolean mHasPThread) {
        MainActivity.mHasPThread = mHasPThread;
    }

    static private UsbInterface findAdbInterface(android.hardware.usb.UsbDevice device) {

        int count = device.getInterfaceCount();
        for (int i = 0; i < count; i++) {
            UsbInterface intf = device.getInterface(i);
            if (intf.getInterfaceClass() == 3
                    && intf.getInterfaceSubclass() == 0
                    && intf.getInterfaceProtocol() == 0) {
                return intf;
            }
        }
        return null;
    }

    public void setUavoLongHashFC(String uavolonghashfc) {
        mUavoLongHashFc = uavolonghashfc;
    }

    public String getUavoLongHash() {
        return mUavoLongHash;
    }

    public FcDevice getFcDevice() {
        return mFcDevice;
    }

    public synchronized void setRxObjectsGood(long o) {
        this.mRxObjectsGood = o;
    }

    public synchronized void incRxObjectsGood() {
        this.mRxObjectsGood++;
    }

    public synchronized void setRxObjectsBad(long o) {
        this.mRxObjectsBad = o;
    }

    public synchronized void incRxObjectsBad() {
        this.mRxObjectsBad++;
    }

    public synchronized void setTxObjects(long o) {
        this.mTxObjects = o;
    }

    public synchronized void incTxObjects() {
        this.mTxObjects++;
    }

    private void copyAssets() {

        VisualLog.d("STARTING", "CopyAssets");
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list(UAVO_INTERNAL_PATH);
        } catch (IOException e) {
            VisualLog.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) {
            for (String filename : files) {
                try {
                    copyFile(assetManager.open(UAVO_INTERNAL_PATH +
                            File.separator + filename), filename);
                } catch (IOException e) {
                    VisualLog.e("tag", "Failed to copy asset file: " + filename, e);
                }
            }
        }
    }

    private void copyFile(InputStream source, String relativeFilename) throws IOException {

        VisualLog.d("COPY", "Copy " + relativeFilename);

        FileOutputStream out = openFileOutput(UAVO_INTERNAL_PATH +
                getString(R.string.DASH) + relativeFilename, Context.MODE_PRIVATE);

        copyFile(source, out);
        source.close();
        out.flush();
        out.close();
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void initSlider(Bundle savedInstanceState) {
        mTitle = mDrawerTitle = getTitle();
        TypedArray navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<NavDrawerItem>();

        navDrawerItems.add(new NavDrawerItem(getString(R.string.menu_main),
                R.drawable.ic_notifications_on_24dp));
        navDrawerItems.add(new NavDrawerItem(getString(R.string.menu_map),
                R.drawable.ic_public_24dp));
        navDrawerItems.add(new NavDrawerItem(getString(R.string.menu_objects),
                R.drawable.ic_now_widgets_24dp));
        navDrawerItems.add(new NavDrawerItem(getString(R.string.menu_pid),
                R.drawable.ic_tune_128dp));
        navDrawerItems.add(new NavDrawerItem(getString(R.string.menu_vpid),
                R.drawable.ic_vertical_align_center_black_128dp));
        navDrawerItems.add(new NavDrawerItem(getString(R.string.menu_logs),
                R.drawable.ic_rate_review_24dp));
        navDrawerItems.add(new NavDrawerItem(getString(R.string.menu_settings),
                R.drawable.ic_settings_24dp));
        navDrawerItems.add(new NavDrawerItem(getString(R.string.menu_about),
                R.drawable.ic_info_outline_24dp));
        navDrawerItems.add(new NavDrawerItem(getString(R.string.menu_debug),
                R.drawable.ic_cancel_128dp));


        navMenuIcons.recycle();
        NavDrawerListAdapter drawListAdapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(drawListAdapter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setLogo(R.drawable.ic_toolbar_36x47dp);
            toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
        }
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeButtonEnabled(true);
            actionbar.setDisplayShowHomeEnabled(true);
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {

            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            //case R.id.action_settings:
            //   return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        ActionBar ab = getSupportActionBar();
        if (ab != null) getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);

        displayView(mCurrentView);
    }


    public void reconnect() {
        mDoReconnect = true;
    }

    private void initViewMain(Bundle savedInstanceState) {
        mViews.put(VIEW_MAIN, getLayoutInflater().inflate(R.layout.activity_main, null));
        setContentView(mViews.get(VIEW_MAIN));  //Main

        initSlider(savedInstanceState);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        mOffset = new HashMap<String, Object>();
        mOffset.put(OFFSET_BAROSENSOR_ALTITUDE, .0f);
        mOffset.put(OFFSET_VELOCITY_DOWN, .0f);

        imgUavoSanity = (ImageView) findViewById(R.id.imgUavoSanity);

        imgGroundTelemetry = (ImageView) findViewById(R.id.imgGroundTelemetry);
        imgFlightTelemetry = (ImageView) findViewById(R.id.imgFlightTelemetry);

        imgPacketsUp = (ImageView) findViewById(R.id.imgPacketsUp);
        imgPacketsGood = (ImageView) findViewById(R.id.imgPacketsGood);
        imgPacketsBad = (ImageView) findViewById(R.id.imgPacketsBad);

        txtObjectLogTx = (TextView) findViewById(R.id.txtObjectLogTx);
        txtObjectLogRxGood = (TextView) findViewById(R.id.txtObjectLogRxGood);
        txtObjectLogRxBad = (TextView) findViewById(R.id.txtObjectLogRxBad);

        txtPlan = (TextView) findViewById(R.id.txtPlan);
        txtAtti = (TextView) findViewById(R.id.txtAtti);
        txtStab = (TextView) findViewById(R.id.txtStab);
        txtPath = (TextView) findViewById(R.id.txtPath);

        txtGPS = (TextView) findViewById(R.id.txtGPS);
        txtGPSSatsInView = (TextView) findViewById(R.id.txtGPSSatsInView);

        txtAirspd = (TextView) findViewById(R.id.txtAirspd);
        txtSensor = (TextView) findViewById(R.id.txtSensor);
        txtMag = (TextView) findViewById(R.id.txtMag);

        txtInput = (TextView) findViewById(R.id.txtInput);
        txtOutput = (TextView) findViewById(R.id.txtOutput);
        txtI2C = (TextView) findViewById(R.id.txtI2C);
        txtTelemetry = (TextView) findViewById(R.id.txtTelemetry);

        txtBatt = (TextView) findViewById(R.id.txtBatt);
        txtTime = (TextView) findViewById(R.id.txtTime);
        txtConfig = (TextView) findViewById(R.id.txtConfig);

        txtBoot = (TextView) findViewById(R.id.txtBoot);
        txtStack = (TextView) findViewById(R.id.txtStack);
        txtMem = (TextView) findViewById(R.id.txtMem);
        txtEvent = (TextView) findViewById(R.id.txtEvent);
        txtCPU = (TextView) findViewById(R.id.txtCPU);

        txtArmed = (TextView) findViewById(R.id.txtArmed);
        txtFlightTime = (TextView) findViewById(R.id.txtFlightTime);

        txtVolt = (TextView) findViewById(R.id.txtVolt);
        txtAmpere = (TextView) findViewById(R.id.txtAmpere);
        txtmAh = (TextView) findViewById(R.id.txtmAh);
        txtTimeLeft = (TextView) findViewById(R.id.txtTimeLeft);

        txtAltitude = (TextView) findViewById(R.id.txtAltitude);
        txtAltitudeAccel = (TextView) findViewById(R.id.txtAltitudeAccel);

        txtModeNum = (TextView) findViewById(R.id.txtModeNum);
        txtModeFlightMode = (TextView) findViewById(R.id.txtModeFlightMode);

        txtModeAssistedControl = (TextView) findViewById(R.id.txtModeAssistedControl);
        txtVehicleName = (TextView) findViewById(R.id.txtVehicleName);

        imgBluetooth = (ImageView) findViewById(R.id.imgBluetooth);
        imgUSB = (ImageView) findViewById(R.id.imgUSB);

        if (mSerialModeUsed != SERIAL_BLUETOOTH) {
            imgBluetooth.setImageAlpha(ICON_TRANSPARENT);
        } else {
            imgBluetooth.setColorFilter(Color.argb(0xff, 0xff, 0x0, 0x0));
        }

        if (mSerialModeUsed != SERIAL_USB) {
            imgUSB.setImageAlpha(ICON_TRANSPARENT);
        } else {
            imgUSB.setColorFilter(Color.argb(0xff, 0xff, 0x0, 0x0));
        }
    }

    private void initViewMap(Bundle savedInstanceState) {
        mViews.put(VIEW_MAP, getLayoutInflater().inflate(R.layout.activity_map, null));
        setContentView(mViews.get(VIEW_MAP)); //Map
        {
            mMapView = (MapView) findViewById(R.id.map);

            if (mMapView != null) {
                mMapView.onCreate(savedInstanceState);
                mMap = mMapView.getMap();
            }

            //Map can be null if services are not available, e.g. on an amazon fire tab
            if (mMap != null) {
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.setMyLocationEnabled(true);
                MapsInitializer.initialize(this);

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(32.154599, -110.827369), 18);
                mMap.animateCamera(cameraUpdate);
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
            txtLatitude = (TextView) findViewById(R.id.txtLatitude);
            txtLongitude = (TextView) findViewById(R.id.txtLongitude);
            txtMapGPS = (TextView) findViewById(R.id.txtMapGPS);
            txtMapGPSSatsInView = (TextView) findViewById(R.id.txtMapGPSSatsInView);
        }
    }

    private void initViewObjects() {
        mViews.put(VIEW_OBJECTS, getLayoutInflater().inflate(R.layout.activity_objects, null));
        setContentView(mViews.get(VIEW_OBJECTS)); //Objects

        txtObjects = new EditText(this);
        mExpListView = (ObjectsExpandableListView) findViewById(R.id.elvObjects);
        // get the listview
        mExpListView.setOnGroupExpandListener(mExpListView);
        mExpListView.setOnChildClickListener(mExpListView);
    }

    /*
     * Preparing the list data
     */
    private void initObjectListData() {
        final MainActivity me = this;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mExpListView.init(new ArrayList<String>(), new HashMap<String, List<ChildString>>());

                // Adding child data
                int i = 0;
                for (UAVTalkXMLObject xmlobj : mXmlObjects.values()) {
                    mExpListView.getListDataHeader().add(xmlobj.getName());
                    //VisualLog.d("OBJ", xmlobj.getName());
                }

                mListAdapter =
                        new ObjectsExpandableListViewAdapter(me, mExpListView.getListDataHeader(), mExpListView.getListDataChild());

                // setting list adapter
                mExpListView.setmAdapter(mListAdapter);

            }
        });
    }

    private void initViewSettings() {
        mViews.put(VIEW_SETTINGS, getLayoutInflater().inflate(R.layout.activity_settings, null));
        setContentView(mViews.get(VIEW_SETTINGS)); //Settings

        cbxColorfulPid = (CheckBox) findViewById(R.id.cbxColorfulPid);
        cbxColorfulPid.setChecked(mColorfulPid);

        spnConnectionTypeSpinner = (Spinner) findViewById(R.id.spnConnectionTypeSpinner);
        ArrayAdapter<CharSequence> serialConnectionTypeAdapter
                = ArrayAdapter.createFromResource(this,
                R.array.connections_settings, android.R.layout.simple_spinner_item);

        serialConnectionTypeAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnConnectionTypeSpinner.setAdapter(serialConnectionTypeAdapter);
        spnConnectionTypeSpinner.setOnItemSelectedListener(this);
        spnConnectionTypeSpinner.setSelection(mSerialModeUsed);

        spnBluetoothPairedDevice = (Spinner) findViewById(R.id.spnBluetoothPairedDevice);

        ArrayAdapter<CharSequence> btPairedDeviceAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        btPairedDeviceAdapter.
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnBluetoothPairedDevice.setAdapter(btPairedDeviceAdapter);
        spnBluetoothPairedDevice.setOnItemSelectedListener(this);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            // Device does support Bluetooth
            if (!mBluetoothAdapter.isEnabled()) {
                SingleToast.show(this,
                        getString(R.string.BLUETOOTH_WARNING), Toast.LENGTH_LONG);

            } else {

                Set<android.bluetooth.BluetoothDevice> pairedDevices =
                        mBluetoothAdapter.getBondedDevices();
                // If there are paired devices
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    int btpd = 0;
                    for (android.bluetooth.BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        btPairedDeviceAdapter.add(device.getName());
                        if (device.getName().equals(mBluetoothDeviceUsed)) {
                            spnBluetoothPairedDevice.setSelection(btpd);
                        }
                        btpd++;
                        if (LOCAL_LOGD)
                            VisualLog.d("BTE", device.getName() + " " + device.getAddress());
                    }
                }
            }
        }
        initUavoSpinner();
    }

    private void initUavoSpinner() {
        spnUavoSource = (Spinner) findViewById(R.id.spnUavoSource);
        ArrayAdapter<CharSequence> uavoSourceAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        uavoSourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnUavoSource.setAdapter(uavoSourceAdapter);
        spnUavoSource.setOnItemSelectedListener(this);

        File dir = getFilesDir();
        File[] subFiles = dir.listFiles();

        if (subFiles != null) {
            int i = 0;
            for (File file : subFiles) {
                Pattern p = Pattern.compile(".*uavo-(.*)\\.zip$");
                Matcher m = p.matcher(file.toString());
                boolean b = m.matches();
                if (b) {
                    uavoSourceAdapter.add(m.group(1));
                    if (m.group(1).equals(mLoadedUavo)) {
                        spnUavoSource.setSelection(i);
                    }
                    i++;
                }
            }
        }
    }

    private void initViewLogs() {
        mViews.put(VIEW_LOGS, getLayoutInflater().inflate(R.layout.activity_logs, null));
        setContentView(mViews.get(VIEW_LOGS)); //Logs
        {
            txtLogFilename = (TextView) findViewById(R.id.txtLogFilename);
            txtLogSize = (TextView) findViewById(R.id.txtLogSize);
            txtLogObjects = (TextView) findViewById(R.id.txtLogObjects);
            txtLogDuration = (TextView) findViewById(R.id.txtLogDuration);
        }
    }

    private void initViewDebug() {
        mViews.put(VIEW_DEBUG, getLayoutInflater().inflate(R.layout.activity_debug, null));
        setContentView(mViews.get(VIEW_DEBUG)); //Logs
        {
            txtDebugLog = (TextView) findViewById(R.id.txtDebugLog);
            VisualLog.setDebugLogTextView(txtDebugLog);
        }
    }

    private void initViewAbout() {
        mViews.put(VIEW_ABOUT, getLayoutInflater().inflate(R.layout.activity_about, null));
        setContentView(mViews.get(VIEW_ABOUT));  //About
        {

            final Resources res = getResources();
            PackageInfo pInfo = null;

            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            final TextView txtAndroidVersionRelease =
                    (TextView) findViewById(R.id.txtAndroidVersionRelease);
            final TextView txtLP2GoVersionRelease =
                    (TextView) findViewById(R.id.txtLP2GoVersionRelease);
            final TextView txtLP2GoPackage =
                    (TextView) findViewById(R.id.txtLP2GoPackage);

            if (txtAndroidVersionRelease != null) {
                txtAndroidVersionRelease.setText(
                        String.format(res.getString(R.string.RUNNING_ON_ANDROID_VERSION),
                                Build.VERSION.RELEASE));
            }
            if (txtLP2GoVersionRelease != null && pInfo != null) {
                txtLP2GoVersionRelease.setText(
                        String.format(res.getString(R.string.LP2GO_RELEASE),
                                pInfo.versionName, pInfo.versionCode));
            }
            if (txtLP2GoPackage != null && pInfo != null) {
                txtLP2GoPackage.setText(pInfo.packageName);
            }
        }

    }

    private void initViewScope() {
        mViews.put(VIEW_SCOPE, getLayoutInflater().inflate(R.layout.activity_scope, null));
        setContentView(mViews.get(VIEW_SCOPE)); //Logs
        {
            lchScope = (LineChart) findViewById(R.id.scope_chart);
            lchScope.setDrawGridBackground(false);
        }
    }

    private void initViewPid() {
        mViews.put(VIEW_PID, getLayoutInflater().inflate(R.layout.activity_pid, null));
        setContentView(mViews.get(VIEW_PID));

        imgPidBank = (ImageView) findViewById(R.id.imgPidBank);

        mPidTexts = new HashSet<PidTextView>();

        PidTextView txtPidRateRollProportional =
                (PidTextView) findViewById(R.id.txtRateRollProportional);
        if (txtPidRateRollProportional != null) {
            txtPidRateRollProportional.init(
                    PID.PID_RATE_ROLL_PROP_DENOM,
                    PID.PID_RATE_ROLL_PROP_MAX,
                    PID.PID_RATE_ROLL_PROP_STEP,
                    PID.PID_RATE_ROLL_PROP_DFS,
                    getString(R.string.PID_NAME_RRP),
                    "RollRatePID", "Kp");
        }
        mPidTexts.add(txtPidRateRollProportional);

        PidTextView txtPidRatePitchProportional =
                (PidTextView) findViewById(R.id.txtRatePitchProportional);
        if (txtPidRatePitchProportional != null) {
            txtPidRatePitchProportional.init(
                    PID.PID_RATE_PITCH_PROP_DENOM,
                    PID.PID_RATE_PITCH_PROP_MAX,
                    PID.PID_RATE_PITCH_PROP_STEP,
                    PID.PID_RATE_PITCH_PROP_DFS,
                    getString(R.string.PID_NAME_RPP),
                    "PitchRatePID", "Kp");
        }
        mPidTexts.add(txtPidRatePitchProportional);

        PidTextView txtPidRateYawProportional =
                (PidTextView) findViewById(R.id.txtRateYawProportional);
        if (txtPidRateYawProportional != null) {
            txtPidRateYawProportional.init(
                    PID.PID_RATE_YAW_PROP_DENOM,
                    PID.PID_RATE_YAW_PROP_MAX,
                    PID.PID_RATE_YAW_PROP_STEP,
                    PID.PID_RATE_YAW_PROP_DFS,
                    getString(R.string.PID_NAME_RYP),
                    "YawRatePID", "Kp");
        }
        mPidTexts.add(txtPidRateYawProportional);

        PidTextView txtPidRateRollIntegral =
                (PidTextView) findViewById(R.id.txtRateRollIntegral);
        if (txtPidRateRollIntegral != null) {
            txtPidRateRollIntegral.init(
                    PID.PID_RATE_ROLL_INTE_DENOM,
                    PID.PID_RATE_ROLL_INTE_MAX,
                    PID.PID_RATE_ROLL_INTE_STEP,
                    PID.PID_RATE_ROLL_INTE_DFS,
                    getString(R.string.PID_NAME_RRI),
                    "RollRatePID", "Ki");
        }
        mPidTexts.add(txtPidRateRollIntegral);

        PidTextView txtPidRatePitchIntegral =
                (PidTextView) findViewById(R.id.txtRatePitchIntegral);
        if (txtPidRatePitchIntegral != null) {
            txtPidRatePitchIntegral.init(
                    PID.PID_RATE_PITCH_INTE_DENOM,
                    PID.PID_RATE_PITCH_INTE_MAX,
                    PID.PID_RATE_PITCH_INTE_STEP,
                    PID.PID_RATE_PITCH_INTE_DFS,
                    getString(R.string.PID_NAME_RPI),
                    "PitchRatePID", "Ki");
        }
        mPidTexts.add(txtPidRatePitchIntegral);

        PidTextView txtPidRateYawIntegral =
                (PidTextView) findViewById(R.id.txtRateYawIntegral);
        if (txtPidRateYawIntegral != null) {
            txtPidRateYawIntegral.init(
                    PID.PID_RATE_YAW_INTE_DENOM,
                    PID.PID_RATE_YAW_INTE_MAX,
                    PID.PID_RATE_YAW_INTE_STEP,
                    PID.PID_RATE_YAW_INTE_DFS,
                    getString(R.string.PID_NAME_RYI),
                    "YawRatePID", "Ki");
        }
        mPidTexts.add(txtPidRateYawIntegral);

        PidTextView txtPidRateRollDerivative =
                (PidTextView) findViewById(R.id.txtRateRollDerivative);
        if (txtPidRateRollDerivative != null) {
            txtPidRateRollDerivative.init(
                    PID.PID_RATE_ROLL_DERI_DENOM,
                    PID.PID_RATE_ROLL_DERI_MAX,
                    PID.PID_RATE_ROLL_DERI_STEP,
                    PID.PID_RATE_ROLL_DERI_DFS,
                    getString(R.string.PID_NAME_RRD),
                    "RollRatePID", "Kd");
        }
        mPidTexts.add(txtPidRateRollDerivative);

        PidTextView txtPidRatePitchDerivative =
                (PidTextView) findViewById(R.id.txtRatePitchDerivative);
        if (txtPidRatePitchDerivative != null) {
            txtPidRatePitchDerivative.init(
                    PID.PID_RATE_PITCH_DERI_DENOM,
                    PID.PID_RATE_PITCH_DERI_MAX,
                    PID.PID_RATE_PITCH_DERI_STEP,
                    PID.PID_RATE_PITCH_DERI_DFS,
                    getString(R.string.PID_NAME_RPD),
                    "PitchRatePID", "Kd");
        }
        mPidTexts.add(txtPidRatePitchDerivative);

        PidTextView txtPidRateYawDerivative =
                (PidTextView) findViewById(R.id.txtRateYawDerivative);
        if (txtPidRateYawDerivative != null) {
            txtPidRateYawDerivative.init(
                    PID.PID_RATE_YAW_DERI_DENOM,
                    PID.PID_RATE_YAW_DERI_MAX,
                    PID.PID_RATE_YAW_DERI_STEP,
                    PID.PID_RATE_YAW_DERI_DFS,
                    getString(R.string.PID_NAME_RPD),
                    "YawRatePID", "Kd");
        }
        mPidTexts.add(txtPidRateYawDerivative);

        PidTextView txtPidRollProportional =
                (PidTextView) findViewById(R.id.txtAttitudeRollProportional);
        if (txtPidRollProportional != null) {
            txtPidRollProportional.init(
                    PID.PID_ROLL_PROP_DENOM,
                    PID.PID_ROLL_PROP_MAX,
                    PID.PID_ROLL_PROP_STEP,
                    PID.PID_ROLL_PROP_DFS,
                    getString(R.string.PID_NAME_ARP),
                    "RollPI", "Kp");
        }
        mPidTexts.add(txtPidRollProportional);

        PidTextView txtPidPitchProportional =
                (PidTextView) findViewById(R.id.txtAttitudePitchProportional);
        if (txtPidPitchProportional != null) {
            txtPidPitchProportional.init(
                    PID.PID_PITCH_PROP_DENOM,
                    PID.PID_PITCH_PROP_MAX,
                    PID.PID_PITCH_PROP_STEP,
                    PID.PID_PITCH_PROP_DFS,
                    getString(R.string.PID_NAME_APP),
                    "PitchPI", "Kp");
        }
        mPidTexts.add(txtPidPitchProportional);

        PidTextView txtPidYawProportional =
                (PidTextView) findViewById(R.id.txtAttitudeYawProportional);
        if (txtPidYawProportional != null) {
            txtPidYawProportional.init(
                    PID.PID_YAW_PROP_DENOM,
                    PID.PID_YAW_PROP_MAX,
                    PID.PID_YAW_PROP_STEP,
                    PID.PID_YAW_PROP_DFS,
                    getString(R.string.PID_NAME_AYP),
                    "YawPI", "Kp");
        }
        mPidTexts.add(txtPidYawProportional);

        PidTextView txtPidRollIntegral =
                (PidTextView) findViewById(R.id.txtAttitudeRollIntegral);
        if (txtPidRollIntegral != null) {
            txtPidRollIntegral.init(
                    PID.PID_ROLL_INTE_DENOM,
                    PID.PID_ROLL_INTE_MAX,
                    PID.PID_ROLL_INTE_STEP,
                    PID.PID_ROLL_INTE_DFS,
                    getString(R.string.PID_NAME_ARI),
                    "RollPI", "Ki");
        }
        mPidTexts.add(txtPidRollIntegral);

        PidTextView txtPidPitchIntegral =
                (PidTextView) findViewById(R.id.txtAttitudePitchIntegral);
        if (txtPidPitchIntegral != null) {
            txtPidPitchIntegral.init(
                    PID.PID_PITCH_INTE_DENOM,
                    PID.PID_PITCH_INTE_MAX,
                    PID.PID_PITCH_INTE_STEP,
                    PID.PID_PITCH_INTE_DFS,
                    getString(R.string.PID_NAME_API),
                    "PitchPI", "Ki");
        }
        mPidTexts.add(txtPidPitchIntegral);

        PidTextView txtPidYawIntegral =
                (PidTextView) findViewById(R.id.txtAttitudeYawIntegral);
        if (txtPidYawIntegral != null) {
            txtPidYawIntegral.init(
                    PID.PID_YAW_INTE_DENOM,
                    PID.PID_YAW_INTE_MAX,
                    PID.PID_YAW_INTE_STEP,
                    PID.PID_YAW_INTE_DFS,
                    getString(R.string.PID_NAME_AYI),
                    "YawPI", "Ki");
        }
        mPidTexts.add(txtPidYawIntegral);

    }

    private void initViewVerticalPid() {
        mViews.put(VIEW_VPID, getLayoutInflater().inflate(R.layout.activity_vpid, null));
        setContentView(mViews.get(VIEW_VPID));

        mVerticalPidTexts = new HashSet<PidTextView>();

        PidTextView txtVerticalAltitudeProportional =
                (PidTextView) findViewById(R.id.txtVerticalAltitudeProportional);
        if (txtVerticalAltitudeProportional != null) {
            txtVerticalAltitudeProportional.init(
                    PID.PID_VERTICAL_ALTI_PROP_DENOM,
                    PID.PID_VERTICAL_ALTI_PROP_MAX,
                    PID.PID_VERTICAL_ALTI_PROP_STEP,
                    PID.PID_VERTICAL_ALTI_PROP_DFS,
                    getString(R.string.VPID_NAME_ALP),
                    "VerticalPosP", "");
        }
        mVerticalPidTexts.add(txtVerticalAltitudeProportional);

        PidTextView txtVerticalExponential =
                (PidTextView) findViewById(R.id.txtVerticalExponential);
        if (txtVerticalExponential != null) {
            txtVerticalExponential.init(
                    PID.PID_VERTICAL_EXPO_DENOM,
                    PID.PID_VERTICAL_EXPO_MAX,
                    PID.PID_VERTICAL_EXPO_STEP,
                    PID.PID_VERTICAL_EXPO_DFS,
                    getString(R.string.VPID_NAME_EXP),
                    "ThrustExp", "",
                    UAVTalkXMLObject.FIELDTYPE_UINT8);
        }
        mVerticalPidTexts.add(txtVerticalExponential);

        PidTextView txtVerticalThrustRate =
                (PidTextView) findViewById(R.id.txtVerticalThrustRate);
        if (txtVerticalThrustRate != null) {
            txtVerticalThrustRate.init(
                    PID.PID_VERTICAL_THRUST_R_DENOM,
                    PID.PID_VERTICAL_THRUST_R_MAX,
                    PID.PID_VERTICAL_THRUST_R_STEP,
                    PID.PID_VERTICAL_THRUST_R_DFS,
                    getString(R.string.VPID_NAME_THR),
                    "ThrustRate", "");
        }
        mVerticalPidTexts.add(txtVerticalThrustRate);

        PidTextView txtVerticalVelocityBeta =
                (PidTextView) findViewById(R.id.txtVerticalVelocityBeta);
        if (txtVerticalVelocityBeta != null) {
            txtVerticalVelocityBeta.init(
                    PID.PID_VERTICAL_VELO_BETA_DENOM,
                    PID.PID_VERTICAL_VELO_BETA_MAX,
                    PID.PID_VERTICAL_VELO_BETA_STEP,
                    PID.PID_VERTICAL_VELO_BETA_DFS,
                    getString(R.string.VPID_NAME_VEB),
                    "VerticalVelPID", "Beta");
        }
        mVerticalPidTexts.add(txtVerticalVelocityBeta);

        PidTextView txtVerticalVelocityDerivative =
                (PidTextView) findViewById(R.id.txtVerticalVelocityDerivative);
        if (txtVerticalVelocityDerivative != null) {
            txtVerticalVelocityDerivative.init(
                    PID.PID_VERTICAL_VELO_DERI_DENOM,
                    PID.PID_VERTICAL_VELO_DERI_MAX,
                    PID.PID_VERTICAL_VELO_DERI_STEP,
                    PID.PID_VERTICAL_VELO_DERI_DFS,
                    getString(R.string.VPID_NAME_VED),
                    "VerticalVelPID", "Kd");
        }
        mVerticalPidTexts.add(txtVerticalVelocityDerivative);

        PidTextView txtVerticalVelocityIntegral =
                (PidTextView) findViewById(R.id.txtVerticalVelocityIntegral);
        if (txtVerticalVelocityIntegral != null) {
            txtVerticalVelocityIntegral.init(
                    PID.PID_VERTICAL_VELO_INTE_DENOM,
                    PID.PID_VERTICAL_VELO_INTE_MAX,
                    PID.PID_VERTICAL_VELO_INTE_STEP,
                    PID.PID_VERTICAL_VELO_INTE_DFS,
                    getString(R.string.VPID_NAME_VEI),
                    "VerticalVelPID", "Ki");
        }
        mVerticalPidTexts.add(txtVerticalVelocityIntegral);

        PidTextView txtVerticalVelocityProportional =
                (PidTextView) findViewById(R.id.txtVerticalVelocityProportional);
        if (txtVerticalVelocityProportional != null) {
            txtVerticalVelocityProportional.init(
                    PID.PID_VERTICAL_VELO_PROP_DENOM,
                    PID.PID_VERTICAL_VELO_PROP_MAX,
                    PID.PID_VERTICAL_VELO_PROP_STEP,
                    PID.PID_VERTICAL_VELO_PROP_DFS,
                    getString(R.string.VPID_NAME_VEP),
                    "VerticalVelPID", "Kp");
        }
        mVerticalPidTexts.add(txtVerticalVelocityProportional);
    }

    @Override
    public void onRestart() {
        super.onRestart();

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        mSerialModeUsed = sharedPref.getInt(getString(R.string.SETTINGS_SERIAL_MODE), 0);
        mBluetoothDeviceUsed = sharedPref.getString(getString(R.string.SETTINGS_BT_NAME), null);
        mLoadedUavo = sharedPref.getString(getString(R.string.SETTINGS_UAVO_SOURCE), null);
        mColorfulPid = sharedPref.getBoolean(getString(R.string.SETTINGS_COLORFUL_PID), false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VisualLog.setActivity(this);

        mViews = new HashMap<>(NUM_OF_VIEWS);
        initViewDebug();

        copyAssets();

        final SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        mSerialModeUsed = sharedPref.getInt(getString(R.string.SETTINGS_SERIAL_MODE), 0);
        mBluetoothDeviceUsed = sharedPref.getString(getString(R.string.SETTINGS_BT_NAME), null);
        mLoadedUavo = sharedPref.getString(getString(R.string.SETTINGS_UAVO_SOURCE), null);
        mColorfulPid = sharedPref.getBoolean(getString(R.string.SETTINGS_COLORFUL_PID), false);

        //debug view is initialized above
        initViewPid();
        initViewVerticalPid();
        initViewScope();
        initViewAbout();
        initViewLogs();
        initViewSettings();
        initViewObjects();
        initViewMap(savedInstanceState);
        initViewMain(savedInstanceState);

        initWarnDialog().show();

    }

    protected boolean loadXmlObjects(boolean overwrite) {

        if (mXmlObjects == null || (overwrite && mLoadedUavo != null)) {
            mXmlObjects = new TreeMap<String, UAVTalkXMLObject>();

            AssetManager assets = getAssets();

            String file = this.mLoadedUavo + getString(R.string.UAVO_FILE_EXTENSION);
            ZipInputStream zis = null;
            MessageDigest crypt = null;
            MessageDigest cumucrypt = null;
            try {
                InputStream is =
                        openFileInput(UAVO_INTERNAL_PATH + getString(R.string.DASH) + file);
                zis = new ZipInputStream(new BufferedInputStream(is));
                ZipEntry ze;

                //we need to sort the files to generate the correct hash
                SortedMap<String, String> files = new TreeMap<String, String>();

                while ((ze = zis.getNextEntry()) != null) {
                    if (ze.getName().endsWith("xml")) {

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int count;
                        while ((count = zis.read(buffer)) != -1) {
                            baos.write(buffer, 0, count);
                        }

                        String xml = baos.toString();
                        files.put(ze.getName(), xml);

                        if (xml.length() > 0) {
                            UAVTalkXMLObject obj = new UAVTalkXMLObject(xml);
                            mXmlObjects.put(obj.getName(), obj);
                        }
                    }
                }

                crypt = MessageDigest.getInstance("SHA-1");     //single files hash
                cumucrypt = MessageDigest.getInstance("SHA-1"); //cumulative hash
                cumucrypt.reset();
                for (String xmle : files.values()) {            //cycle over the sorted files
                    crypt.reset();
                    crypt.update(xmle.getBytes());              //hash the file
                    //update a hash over the file hash string representations (yes.)
                    cumucrypt.update(H.bytesToHex(crypt.digest()).toLowerCase().getBytes()); //sic!
                }

                mUavoLongHash = H.bytesToHex(cumucrypt.digest()).toLowerCase();
                VisualLog.d("SHA1", H.bytesToHex(cumucrypt.digest()).toLowerCase());

            } catch (IOException | SAXException
                    | ParserConfigurationException | NoSuchAlgorithmException e) {
                VisualLog.e("UAVO", "UAVO Load Error", e);
            } finally {
                try {
                    if (zis != null) zis.close();
                } catch (IOException e) {
                    VisualLog.e("LoadXML", "Exception on Close");
                }
            }
            mDoReconnect = true;

            initObjectListData();

            return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mPermissionIntent == null)
            mPermissionIntent =
                    PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        // listen for new usb devices
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);

        registerReceiver(mUsbReceiver, filter);

        if (Looper.myLooper() == null) Looper.prepare();

        if (mPollThread == null) {
            mPollThread = new PollThread(this);
            mPollThread.start();
        }
        if (mConnectionThread == null) {
            mConnectionThread = new ConnectionThread(this);
            mConnectionThread.start();
        }

        mDoReconnect = true;

        displayView(mCurrentView);

        VisualLog.d("onStart", "onStart");
    }

    @Override
    protected void onStop() {

        if (mFcDevice != null) mFcDevice.setLogging(false);

        if (mPollThread != null) {
            mPollThread.setInvalid();
            mPollThread = null;
        }

        if (mConnectionThread != null) {
            mConnectionThread.setInvalid();
            mConnectionThread = null;
            MainActivity.mHasPThread = false;
        }

        mSerialModeUsed = SERIAL_NONE;
        mDoReconnect = true;

        if (mFcDevice != null) mFcDevice.stop();
        mFcDevice = null;

        unregisterReceiver(mUsbReceiver);
        setUsbInterface(null, null);
        mPermissionIntent = null;

        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected void connectUSB() {
        if (mSerialModeUsed == SERIAL_USB) {
            for (android.hardware.usb.UsbDevice device : mUsbManager.getDeviceList().values()) {
                if (device.getDeviceClass() == UsbConstants.USB_CLASS_MISC) {
                    mUsbManager.requestPermission(device, mPermissionIntent);
                }
            }
        }
    }

    protected void connectBluetooth() {
        if (mSerialModeUsed == SERIAL_BLUETOOTH) {
            setBluetoothInterface();
        }
    }

    private void setContentView(View v, int p) {
        if (mCurrentView != p) {
            mCurrentView = p;
            super.setContentView(v);
            initSlider(null);
        }
    }

    public void displayView(int position) {
        Fragment fragment = null;

        //clean up current view
        switch (mCurrentView) {
            case VIEW_MAIN:

                break;
            case VIEW_MAP:
                mMapView.onPause();

                break;
            case VIEW_OBJECTS:

                break;
            case VIEW_SETTINGS:
                SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                String btname = getString(R.string.EMPTY_STRING);
                if (spnBluetoothPairedDevice.getSelectedItem() != null) {
                    btname = spnBluetoothPairedDevice.getSelectedItem().toString();
                }

                String btmac = getString(R.string.EMPTY_STRING);

                Set<android.bluetooth.BluetoothDevice> pairedDevices =
                        mBluetoothAdapter.getBondedDevices();
                // If there are paired devices
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    for (android.bluetooth.BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        if (device.getName().equals(btname)) {
                            btmac = device.getAddress();
                        }
                    }
                }

                mSerialModeUsed = spnConnectionTypeSpinner.getSelectedItemPosition();

                imgBluetooth.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
                imgUSB.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
                switch (mSerialModeUsed) {
                    case SERIAL_NONE:
                        imgBluetooth.setImageAlpha(ICON_TRANSPARENT);
                        imgUSB.setImageAlpha(ICON_TRANSPARENT);
                        break;
                    case SERIAL_USB:
                        imgBluetooth.setImageAlpha(ICON_TRANSPARENT);
                        imgUSB.setImageAlpha(ICON_OPAQUE);
                        break;
                    case SERIAL_BLUETOOTH:
                        imgBluetooth.setImageAlpha(ICON_OPAQUE);
                        imgUSB.setImageAlpha(ICON_TRANSPARENT);
                        break;
                }

                if (spnUavoSource.getSelectedItem() != null
                        && !spnUavoSource.getSelectedItem().toString().equals(mLoadedUavo)) {
                    mLoadedUavo = spnUavoSource.getSelectedItem().toString();
                    VisualLog.d("UAVSource", mLoadedUavo + "  " + mLoadedUavo);

                    loadXmlObjects(true);
                    SingleToast.show(this, "UAVO load completed", Toast.LENGTH_SHORT);
                }

                mColorfulPid = cbxColorfulPid.isChecked();

                editor.putString(getString(R.string.SETTINGS_BT_MAC), btmac);
                editor.putString(getString(R.string.SETTINGS_BT_NAME), btname);
                editor.putString(getString(R.string.SETTINGS_UAVO_SOURCE), mLoadedUavo);
                editor.putInt(getString(R.string.SETTINGS_SERIAL_MODE), mSerialModeUsed);
                editor.putBoolean(getString(R.string.SETTINGS_COLORFUL_PID), mColorfulPid);

                editor.commit();
                mDoReconnect = true;

                break;
            case VIEW_LOGS:

                break;
            case VIEW_ABOUT:

                break;

            case VIEW_PID:
                break;

            case VIEW_VPID:
                break;

            case VIEW_DEBUG:
                break;

            case VIEW_SCOPE:
                break;

            default:
                break;
        }

        String menuTitle = "";

        //init new view
        switch (position) {
            case VIEW_MAIN:
                initViewMain(null);
                fragment = new MainFragment();
                setContentView(mViews.get(VIEW_MAIN), position);

                imgBluetooth = (ImageView) findViewById(R.id.imgBluetooth);
                imgUSB = (ImageView) findViewById(R.id.imgUSB);

                menuTitle = getString(R.string.menu_main);

                break;
            case VIEW_MAP:
                fragment = new MapFragment();
                setContentView(mViews.get(VIEW_MAP), position);
                mMapView.onResume();  //(re)activate the Map

                menuTitle = getString(R.string.menu_map);

                break;
            case VIEW_OBJECTS:
                fragment = new ObjectsFragment();
                setContentView(mViews.get(VIEW_OBJECTS), position);

                menuTitle = getString(R.string.menu_objects);

                break;
            case VIEW_SETTINGS:
                fragment = new SettingsFragment();
                setContentView(mViews.get(VIEW_SETTINGS), position);

                if (mSerialModeUsed == SERIAL_NONE) {
                    SingleToast.show(this, getString(R.string.PLEASE_SET_A)
                            + getString(R.string.CON_TYPE), Toast.LENGTH_LONG);
                } else if (mSerialModeUsed == SERIAL_BLUETOOTH && mBluetoothDeviceUsed == null) {
                    SingleToast.show(this, getString(R.string.PLEASE_SET_A)
                            + getString(R.string.BT_DEVICE), Toast.LENGTH_LONG);
                }

                menuTitle = getString(R.string.menu_settings);

                break;
            case VIEW_LOGS:
                fragment = new LogsFragment();
                setContentView(mViews.get(VIEW_LOGS), position);

                menuTitle = getString(R.string.menu_logs);

                break;
            case VIEW_ABOUT:
                fragment = new AboutFragment();
                setContentView(mViews.get(VIEW_ABOUT), position);

                menuTitle = getString(R.string.menu_about);

                break;

            case VIEW_PID:
                fragment = new PidFragment();
                setContentView(mViews.get(VIEW_PID), position);
                SingleToast.show(this, R.string.CHECK_PID_WARNING, Toast.LENGTH_SHORT);

                try {
                    imgBluetooth = (ImageView) findViewById(R.id.imgBluetooth);
                    imgUSB = (ImageView) findViewById(R.id.imgUSB);

                    final View lloOuterPid = findViewById(R.id.lloOuterPid);
                    final View lloInnerPid = findViewById(R.id.lloInnerPid);

                    if (lloInnerPid != null && lloOuterPid != null) {
                        if (mColorfulPid) {
                            lloOuterPid.setBackground(
                                    ContextCompat.getDrawable(getApplicationContext(),
                                            R.drawable.border_top_yellow));
                            lloInnerPid.setBackground(
                                    ContextCompat.getDrawable(getApplicationContext(),
                                            R.drawable.border_top_blue));
                        } else {
                            lloOuterPid.setBackground(
                                    ContextCompat.getDrawable(getApplicationContext(),
                                            R.drawable.border_top));
                            lloInnerPid.setBackground(
                                    ContextCompat.getDrawable(getApplicationContext(),
                                            R.drawable.border_top));
                        }
                    }
                } catch (NullPointerException e1) {
                    VisualLog.d("MainActivity", "VIEW_PID", e1);
                }

                menuTitle = getString(R.string.menu_pid);

                break;

            case VIEW_VPID:
                fragment = new VerticalPidFragment();
                setContentView(mViews.get(VIEW_VPID), position);
                SingleToast.show(this, R.string.CHECK_PID_WARNING, Toast.LENGTH_SHORT);

                try {
                    imgBluetooth = (ImageView) findViewById(R.id.imgBluetooth);
                    imgUSB = (ImageView) findViewById(R.id.imgUSB);

                    final View lloStickResponse = findViewById(R.id.lloStickResponse);
                    final View lloControllCoeff = findViewById(R.id.lloControlCoeff);

                    if (lloStickResponse != null && lloControllCoeff != null) {
                        if (mColorfulPid) {
                            lloStickResponse.setBackground(
                                    ContextCompat.getDrawable(getApplicationContext(),
                                            R.drawable.border_top_yellow));
                            lloControllCoeff.setBackground(
                                    ContextCompat.getDrawable(getApplicationContext(),
                                            R.drawable.border_top_blue));
                        } else {
                            lloStickResponse.setBackground(
                                    ContextCompat.getDrawable(getApplicationContext(),
                                            R.drawable.border_top));
                            lloControllCoeff.setBackground(
                                    ContextCompat.getDrawable(getApplicationContext(),
                                            R.drawable.border_top));
                        }
                    }
                } catch (NullPointerException e2) {
                    VisualLog.d("MainActivity", "VIEW_VPID", e2);
                }

                menuTitle = getString(R.string.menu_vpid);

                break;

            case VIEW_DEBUG:
                fragment = new DebugFragment();
                setContentView(mViews.get(VIEW_DEBUG), position);
                break;

            case VIEW_SCOPE:
                fragment = new ScopeFragment();
                setContentView(mViews.get(VIEW_SCOPE), position);
                break;

            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            try {
                fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
            } catch (IllegalStateException e) {
                //Maybe there's no need to fix, because we don't really replace the fragment here
                VisualLog.e("FIXME", "After wakeup in different orientation, this happens.");
            }

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(menuTitle);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (mCurrentView == VIEW_MAIN) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .setMessage(R.string.ARE_YOU_SURE_YOU_WANT_TO_CLOSE)
                    .setTitle(R.string.EXIT)
                    .setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.NO, null)
                    .show();
        } else {
            displayView(VIEW_MAIN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CALLBACK_FILEPICKER && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            SingleToast.show(this, filePath, Toast.LENGTH_LONG);

            try {
                FileInputStream in = new FileInputStream(new File(filePath));
                Uri uri = Uri.parse(filePath);
                String strFileName = uri.getLastPathSegment();
                copyFile(in, strFileName);
                VisualLog.d("FNF", "FNF");
            } catch (FileNotFoundException e) {
                VisualLog.d("FNF", "FNF");
                SingleToast.show(this, filePath + " not found", Toast.LENGTH_LONG);
            } catch (IOException e) {
                VisualLog.d("IOE", "IOE");
                SingleToast.show(this, "Cannot open " + filePath, Toast.LENGTH_LONG);
            }

            initUavoSpinner();


        }
    }

    public void onClearUavObjectFilesClick(View v) {

        File dir = getFilesDir();
        File[] subFiles = dir.listFiles();

        if (subFiles != null) {
            for (File file : subFiles) {
                Pattern p = Pattern.compile(".*uavo-(.*)\\.zip$");
                Matcher m = p.matcher(file.toString());
                boolean b = m.matches();
                if (b) {
                    file.delete();
                }
            }
        }

        copyAssets();
        initUavoSpinner();

        SingleToast.show(this, "Files deleted", Toast.LENGTH_LONG);
    }

    public void onSelectUavObjectsSourceFileClick(View v) {
        new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(CALLBACK_FILEPICKER)
                // Filtering files and directories by file name using regexp
                .withFilter(Pattern.compile(".*\\.zip$"))
                .withFilterDirectories(false) // Set directories filterable (false by default)
                .withHiddenFiles(false) // Show hidden files and folders
                .start();
    }

    public void onHealthSettingsClick(View v) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Health Settings");

        final View alertView = View.inflate(this, R.layout.alert_dialog_health, null);
        dialogBuilder.setView(alertView);

        try {
            txtHealthAlertDialogBatteryCapacity =
                    (TextView) alertView.findViewById(R.id.txtHealthAlertDialogBatteryCapacity);
            txtHealthAlertDialogBatteryCells =
                    (TextView) alertView.findViewById(R.id.txtHealthAlertDialogBatteryCells);
            txtHealthAlertDialogFusionAlgorithm =
                    (TextView) alertView.findViewById(R.id.txtHealthAlertDialogFusionAlgorithm);

            txtHealthAlertDialogBatteryCapacity.setText(mFcDevice.getObjectTree()
                    .getData("FlightBatterySettings", "Capacity").toString());
            txtHealthAlertDialogBatteryCells.setText(mFcDevice.getObjectTree()
                    .getData("FlightBatterySettings", "NbCells").toString());
            txtHealthAlertDialogFusionAlgorithm.setText(mFcDevice.getObjectTree()
                    .getData("RevoSettings", "FusionAlgorithm").toString());
        } catch (UAVTalkMissingObjectException | NullPointerException e) {
            e.printStackTrace();
        }

        dialogBuilder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.show();
    }


    public void onBatteryCapacityClick(View v) {
        String moduleEnabled =
                mPollThread.getData("HwSettings", "OptionalModules", "Battery", true).toString();
        if (moduleEnabled.equals("Enabled")) {
            new NumberInputAlertDialog(this)
                    .withPresetText(txtHealthAlertDialogBatteryCapacity.getText().toString())
                    .withTitle(getString(R.string.CAPACITY_DIALOG_TITLE))
                    .withLayout(R.layout.alert_dialog_integer_input)
                    .withUavTalkDevice(mFcDevice)
                    .withObject("FlightBatterySettings")
                    .withField("Capacity")
                    .withFieldType(UAVTalkXMLObject.FIELDTYPE_UINT32)
                    .show();
        } else {
            SingleToast.show(this, "Battery Module not enabled", Toast.LENGTH_SHORT);
        }
    }

    public void onAltitudeClick(View V) {
        try {
            mOffset.put(OFFSET_BAROSENSOR_ALTITUDE, mFcDevice.getObjectTree()
                    .getData("BaroSensor", "Altitude"));
            txtAltitude.setText(R.string.EMPTY_STRING);
        } catch (UAVTalkMissingObjectException | NullPointerException e) {
            VisualLog.i("INFO", "UAVO is missing");
        }
    }

    public void onAltitudeAccelClick(View V) {
        try {
            mOffset.put(OFFSET_VELOCITY_DOWN, mFcDevice.getObjectTree()
                    .getData("VelocityState", "Down"));
            txtAltitudeAccel.setText(R.string.EMPTY_STRING);
        } catch (UAVTalkMissingObjectException | NullPointerException e) {
            VisualLog.i("INFO", "UAVO is missing");
        }
    }

    public void onBatteryCellsClick(View v) {
        String moduleEnabled =
                mPollThread.getData("HwSettings", "OptionalModules", "Battery", true).toString();
        if (moduleEnabled.equals("Enabled")) {
            new NumberInputAlertDialog(this)
                    .withPresetText(txtHealthAlertDialogBatteryCells.getText().toString())
                    .withTitle(getString(R.string.CELLS_DIALOG_TITLE))
                    .withLayout(R.layout.alert_dialog_integer_input)
                    .withUavTalkDevice(mFcDevice)
                    .withObject("FlightBatterySettings")
                    .withField("NbCells")
                    .withFieldType(UAVTalkXMLObject.FIELDTYPE_UINT8)
                    .withMinMax(1, 254)
                    .show();
        } else {
            SingleToast.show(this, "Battery Module not enabled", Toast.LENGTH_SHORT);
        }
    }

    public void onFusionAlgoClick(View v) {
        if (mFcDevice != null) {
            String armingState;
            try {
                armingState = mFcDevice.getObjectTree()
                        .getData("FlightStatus", "Armed").toString();
            } catch (UAVTalkMissingObjectException e) {
                armingState = "";
                mFcDevice.requestObject("FlightStatus");
            }
            if (armingState.equals("Disarmed")) {
                //initFusionAlgoDialog().show();
                new EnumInputAlertDialog(this)
                        .withTitle("Select Fusion Algorithm")
                        .withUavTalkDevice(mFcDevice)
                        .withObject("RevoSettings")
                        .withField("FusionAlgorithm")
                        .show();
            } else {
                SingleToast.show(this,
                        getString(R.string.CHANGE_FUSION_ALGO_DISARMED) + " " + armingState,
                        Toast.LENGTH_LONG);
            }
        } else {
            SingleToast.show(this, R.string.SEND_FAILED, Toast.LENGTH_SHORT);
        }

    }

    public void onLogStartClick(View v) {
        try {
            mFcDevice.setLogging(true);
        } catch (NullPointerException e) {
            VisualLog.i("INFO", "Device is null");
        }
    }

    public void onLogStopClick(View v) {
        try {
            mFcDevice.setLogging(false);
        } catch (NullPointerException e) {
            VisualLog.i("INFO", "Device is null");
        }
    }

    public void onVerticalPidSaveClick(View v) {
        if (mFcDevice != null && mFcDevice.isConnected()) {
            mFcDevice.savePersistent("AltitudeHoldSettings");
            SingleToast.show(this, getString(R.string.SAVED_PERSISTENT)
                    + getString(R.string.CHECK_PID_WARNING), Toast.LENGTH_SHORT);
        } else {
            SingleToast.show(this, R.string.SEND_FAILED, Toast.LENGTH_SHORT);
        }
    }

    public void onVerticalPidUploadClick(View v) {
        if (mFcDevice != null && mFcDevice.isConnected()) {
            UAVTalkObjectTree oTree = mFcDevice.getObjectTree();
            if (oTree != null) {
                oTree.getObjectFromName("AltitudeHoldSettings").setWriteBlocked(true);

                Iterator<PidTextView> i = mVerticalPidTexts.iterator();

                while (i.hasNext()) {
                    PidTextView ptv = i.next();

                    switch (ptv.getFieldType()) {
                        case (UAVTalkXMLObject.FIELDTYPE_FLOAT32):
                            try {
                                float f = H.stringToFloat(ptv.getText().toString());

                                byte[] buffer = H.reverse4bytes(H.floatToByteArray(f));

                                UAVTalkDeviceHelper.updateSettingsObject(
                                        oTree, "AltitudeHoldSettings", 0,
                                        ptv.getField(), ptv.getElement(), buffer);
                            } catch (NumberFormatException e) {
                                VisualLog.e("MainActivity",
                                        "Error parsing float (vertical): " + ptv.getField() + " " +
                                                ptv.getElement() + " " + ptv.getText().toString());
                            }
                            break;
                        case (UAVTalkXMLObject.FIELDTYPE_UINT8):
                            try {
                                byte[] buffer = new byte[1];
                                VisualLog.d("SDFG", ptv.getText().toString());
                                buffer[0] =
                                        (byte) (Integer.parseInt(ptv.getText().toString()) & 0xff);
                                UAVTalkDeviceHelper.updateSettingsObject(
                                        oTree, "AltitudeHoldSettings", 0, ptv.getField(),
                                        ptv.getElement(), buffer);
                            } catch (NumberFormatException e) {
                                VisualLog.e("MainActivity",
                                        "Error parsing uint8 (vertical): " + ptv.getField() + " " +
                                                ptv.getElement() + " " + ptv.getText().toString());
                            }
                            break;
                        default:

                            break;
                    }
                }

                mFcDevice.sendSettingsObject("AltitudeHoldSettings", 0);

                SingleToast.show(this, getString(R.string.PID_SENT)
                        + getString(R.string.CHECK_PID_WARNING), Toast.LENGTH_SHORT);

                oTree.getObjectFromName("AltitudeHoldSettings").setWriteBlocked(false);
            }
        } else {
            SingleToast.show(this, R.string.SEND_FAILED, Toast.LENGTH_SHORT);
        }
    }

    public void onVerticalPidDownloadClick(View v) {
        if (mFcDevice != null && mFcDevice.isConnected()) {
            allowVerticalPidUpdate();
            SingleToast.show(this, getString(R.string.PID_LOADING)
                    + getString(R.string.CHECK_PID_WARNING), Toast.LENGTH_SHORT);
        } else {
            SingleToast.show(this, R.string.SEND_FAILED, Toast.LENGTH_SHORT);
        }
    }

    public void onPidSaveClick(View v) {
        if (mFcDevice != null && mFcDevice.isConnected()) {
            mFcDevice.savePersistent(mCurrentStabilizationBank);
            SingleToast.show(this, getString(R.string.SAVED_PERSISTENT)
                    + getString(R.string.CHECK_PID_WARNING), Toast.LENGTH_SHORT);
        } else {
            SingleToast.show(this, R.string.SEND_FAILED, Toast.LENGTH_SHORT);
        }
    }

    public void onPidUploadClick(View v) {
        if (mFcDevice != null && mFcDevice.isConnected()) {
            UAVTalkObjectTree oTree = mFcDevice.getObjectTree();
            if (oTree != null) {
                oTree.getObjectFromName(mCurrentStabilizationBank).setWriteBlocked(true);

                Iterator<PidTextView> i = mPidTexts.iterator();

                while (i.hasNext()) {
                    PidTextView ptv = i.next();
                    try {
                        float f = H.stringToFloat(ptv.getText().toString());

                        byte[] buffer = H.reverse4bytes(H.floatToByteArray(f));

                        UAVTalkDeviceHelper.updateSettingsObject(
                                oTree, mCurrentStabilizationBank, 0, ptv.getField(),
                                ptv.getElement(), buffer);
                    } catch (NumberFormatException e) {
                        VisualLog.e("MainActivity",
                                "Error parsing float: " + ptv.getField() + " " + ptv.getElement() +
                                        " " + ptv.getText().toString());
                    }
                }

                mFcDevice.sendSettingsObject(mCurrentStabilizationBank, 0);

                SingleToast.show(this, getString(R.string.PID_SENT)
                        + getString(R.string.CHECK_PID_WARNING), Toast.LENGTH_SHORT);

                oTree.getObjectFromName(mCurrentStabilizationBank).setWriteBlocked(false);
            }
        } else {
            SingleToast.show(this, R.string.SEND_FAILED, Toast.LENGTH_SHORT);
        }
    }

    public void onPidDownloadClick(View v) {
        if (mFcDevice != null && mFcDevice.isConnected()) {
            allowPidUpdate();
            SingleToast.show(this, getString(R.string.PID_LOADING)
                    + getString(R.string.CHECK_PID_WARNING), Toast.LENGTH_SHORT);
        } else {
            SingleToast.show(this, R.string.SEND_FAILED, Toast.LENGTH_SHORT);
        }
    }

    private void allowPidUpdate() {
        Iterator<PidTextView> i = mPidTexts.iterator();

        while (i.hasNext()) {
            PidTextView ptv = i.next();
            ptv.allowUpdate();
        }
    }

    private void allowVerticalPidUpdate() {
        Iterator<PidTextView> i = mVerticalPidTexts.iterator();

        while (i.hasNext()) {
            PidTextView ptv = i.next();
            ptv.allowUpdate();
        }
    }

    public void onLogShare(View v) {
        try {
            mFcDevice.setLogging(false);
        } catch (NullPointerException e) {
            return;
        }
        Intent share = new Intent(Intent.ACTION_SEND);

        share.setType(getString(R.string.MIME_APPLICATION_OCTETSTREAM));

        File logPath = new File(this.getFilesDir(), "");
        File logFile = new File(logPath, mFcDevice.getLogFileName());
        Uri contentUri =
                FileProvider.getUriForFile(this, "net.proest.lp2go3.logfileprovider", logFile);

        share.putExtra(Intent.EXTRA_STREAM, contentUri);
        startActivity(Intent.createChooser(share, getString(R.string.SHARE_LOG_TITLE)));
    }

    public void onDebugLogShare(View v) {
        Intent share = new Intent(Intent.ACTION_SEND);

        share.setType(getString(R.string.MIME_APPLICATION_TEXT));
        share.putExtra(Intent.EXTRA_TEXT, txtDebugLog.getText().toString());
        startActivity(Intent.createChooser(share, getString(R.string.SHARE_LOG_TITLE)));
    }

    public void setPollThreadObjectTree(UAVTalkObjectTree oTree) {
        mPollThread.setObjectTree(oTree);
    }

    private boolean setBluetoothInterface() {
        if (mFcDevice != null) {
            mFcDevice.stop();
        }
        mFcDevice = null;
        mFcDevice = new FcBluetoothDevice(this, mXmlObjects);
        mFcDevice.start();

        return mFcDevice != null;
    }

    private boolean setUsbInterface(android.hardware.usb.UsbDevice device, UsbInterface intf) {
        if (mDeviceConnection != null) {
            if (mInterface != null) {
                mDeviceConnection.releaseInterface(mInterface);
                mInterface = null;
            }
            mDeviceConnection.close();
            mDevice = null;
            mDeviceConnection = null;
        }

        if (device != null && intf != null) {
            UsbDeviceConnection connection = mUsbManager.openDevice(device);
            if (connection != null) {
                if (connection.claimInterface(intf, true)) {
                    mDevice = device;
                    mDeviceConnection = connection;
                    mInterface = intf;
                    mFcDevice = new FcUsbDevice(this, mDeviceConnection, intf,
                            mXmlObjects);
                    mFcDevice.getObjectTree().setXmlObjects(mXmlObjects);

                    mFcDevice.start();
                    return true;
                } else {
                    connection.close();
                }
            }
        }

        if (mDeviceConnection == null && mFcDevice != null) {
            mFcDevice.stop();
            mFcDevice = null;
        }
        return false;
    }

    private AlertDialog.Builder initWarnDialog() {
        AlertDialog.Builder warnDialogBuilder = new AlertDialog.Builder(this);
        warnDialogBuilder.setTitle(R.string.WARNING);

        warnDialogBuilder.setCancelable(false);

        final TextView info = new TextView(this);
        warnDialogBuilder.setView(info);
        info.setText(R.string.GNU_WARNING);
        info.setPadding(5, 5, 5, 5);

        warnDialogBuilder.setPositiveButton(R.string.I_UNDERSTAND, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                dialog.cancel();
            }
        });
        return warnDialogBuilder;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        switch (parent.getId()) {
            case R.id.spnConnectionTypeSpinner: {

                break;
            }
            case R.id.spnUavoSource: {

                break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    protected void resetMainView() {
        Context c = getApplicationContext();
        txtAtti.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
        txtStab.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
        txtPath.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
        txtPlan.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));

        txtGPSSatsInView.setText(R.string.EMPTY_STRING);
        txtGPS.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
        txtSensor.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
        txtAirspd.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
        txtMag.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));

        txtInput.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
        txtOutput.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
        txtI2C.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
        txtTelemetry.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));

        txtBatt.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
        txtTime.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
        txtConfig.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));

        txtBoot.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
        txtMem.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
        txtStack.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
        txtEvent.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));
        txtCPU.setBackground(ContextCompat.getDrawable(c, R.drawable.rounded_corner_unini));

        txtArmed.setText(R.string.EMPTY_STRING);

        txtVolt.setText(R.string.EMPTY_STRING);
        txtAmpere.setText(R.string.EMPTY_STRING);
        txtmAh.setText(R.string.EMPTY_STRING);
        txtTimeLeft.setText(R.string.EMPTY_STRING);

        txtAltitude.setText(R.string.EMPTY_STRING);
        txtAltitudeAccel.setText(R.string.EMPTY_STRING);

        txtModeNum.setText(R.string.EMPTY_STRING);
        txtModeFlightMode.setText(R.string.EMPTY_STRING);
        txtModeAssistedControl.setText(R.string.EMPTY_STRING);
    }

    public void onPidGridNumberClick(View v) {
        PidTextView p = (PidTextView) v;
        new PidInputAlertDialog(this)
                .withStep(p.getStep())
                .withDenominator(p.getDenom())
                .withDecimalFormat(p.getDfs())
                .withPidTextView(p)
                .withValueMax(p.getMax())
                .withPresetText(p.getText().toString())
                .withTitle(p.getDialogTitle())
                .withLayout(R.layout.alert_dialog_pid_grid)
                .withUavTalkDevice(mFcDevice)
                .withObject(mCurrentStabilizationBank)
                .withField(p.getField())
                .withElement(p.getElement())
                .withFieldType(UAVTalkXMLObject.FIELDTYPE_FLOAT32)
                .show();
    }

    public void onVerticalPidGridNumberClick(View v) {
        PidTextView p = (PidTextView) v;
        new PidInputAlertDialog(this)
                .withStep(p.getStep())
                .withDenominator(p.getDenom())
                .withDecimalFormat(p.getDfs())
                .withPidTextView(p)
                .withValueMax(p.getMax())
                .withPresetText(p.getText().toString())
                .withTitle(p.getDialogTitle())
                .withLayout(R.layout.alert_dialog_pid_grid)
                .withUavTalkDevice(mFcDevice)
                .withObject("AltitudeHoldSettings")
                .withField(p.getField())
                .withElement(p.getElement())
                .withFieldType(p.getFieldType())
                .show();
    }

    public void onObjectsSanityIndicatorClick(View v) {
        showObjectSanityWarningMessage();
    }

    void showObjectSanityWarningMessage() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        String uavoShortHash = mUavoLongHash != null && mUavoLongHash.length() > 8
                ? mUavoLongHash.substring(0, 8) : getString(R.string.NULL);
        String uavoShortHashFc = mUavoLongHashFc != null && mUavoLongHashFc.length() > 8
                ? mUavoLongHashFc.substring(0, 8) : getString(R.string.NULL);

        if (uavoShortHashFc.equals(getString(R.string.NULL))) {
            dialogBuilder.setTitle(R.string.INFO);
            dialogBuilder.setMessage(
                    getString(R.string.NOT_CONNECTED) + getString(R.string.LF) +
                            getString(R.string.LF) +
                            getString(R.string.UAVO_VERSION_FC) + getString(R.string.TAB) +
                            getString(R.string.TAB) + getString(R.string.TAB) +
                            getString(R.string.TAB) + uavoShortHashFc + getString(R.string.LF) +
                            getString(R.string.UAVO_VERSION) + getString(R.string.TAB) +
                            uavoShortHash
            );
        } else if (uavoShortHashFc.equals(uavoShortHash)) {
            dialogBuilder.setTitle(R.string.INFO);
            dialogBuilder.setMessage(
                    getString(R.string.UAVO_VERSION_OK) + getString(R.string.LF) +
                            getString(R.string.LF) +
                            getString(R.string.UAVO_VERSION_FC) + getString(R.string.TAB) +
                            getString(R.string.TAB) + getString(R.string.TAB) +
                            getString(R.string.TAB) + uavoShortHashFc + getString(R.string.LF) +
                            getString(R.string.UAVO_VERSION) + getString(R.string.TAB) +
                            uavoShortHash
            );
        } else {
            dialogBuilder.setTitle(R.string.WARNING);
            dialogBuilder.setMessage(
                    getString(R.string.UAVO_WARNING_A) + getString(R.string.LF) +
                            getString(R.string.UAVO_WARNING_B) + getString(R.string.LF) +
                            getString(R.string.LF) +
                            getString(R.string.UAVO_VERSION_FC) + getString(R.string.TAB) +
                            getString(R.string.TAB) + getString(R.string.TAB) +
                            getString(R.string.TAB) + uavoShortHashFc + getString(R.string.LF) +
                            getString(R.string.UAVO_VERSION) + getString(R.string.TAB) +
                            uavoShortHash
            );
        }

        dialogBuilder.setPositiveButton(R.string.OK_BUTTON,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialogBuilder.show();
    }

    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            displayView(position);
        }
    }
}
