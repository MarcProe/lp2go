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
import android.bluetooth.BluetoothDevice;
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
import android.content.res.TypedArray;
import android.graphics.Color;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import net.proest.lp2go3.UAVTalk.UAVTalkBluetoothDevice;
import net.proest.lp2go3.UAVTalk.UAVTalkDevice;
import net.proest.lp2go3.UAVTalk.UAVTalkMissingObjectException;
import net.proest.lp2go3.UAVTalk.UAVTalkObjectTree;
import net.proest.lp2go3.UAVTalk.UAVTalkUsbDevice;
import net.proest.lp2go3.UAVTalk.UAVTalkXMLObject;
import net.proest.lp2go3.UI.PidSeekBar;
import net.proest.lp2go3.slider.AboutFragment;
import net.proest.lp2go3.slider.LogsFragment;
import net.proest.lp2go3.slider.MainFragment;
import net.proest.lp2go3.slider.MapFragment;
import net.proest.lp2go3.slider.ObjectsFragment;
import net.proest.lp2go3.slider.SettingsFragment;
import net.proest.lp2go3.slider.adapter.NavDrawerListAdapter;
import net.proest.lp2go3.slider.model.NavDrawerItem;

import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String UAVO_INTERNAL_PATH = "uavo";
    private static final int ICON_OPAQUE = 255;
    private static final int ICON_TRANSPARENT = 64;
    private static final int SERIAL_NONE = 0;
    private static final int SERIAL_USB = 1;
    private static final int SERIAL_BLUETOOTH = 2;
    private static final int SERIAL_CONNECTED = 2;
    private static final int SERIAL_CONNECTING = 1;
    private static final int SERIAL_DISCONNECTED = 0;
    private static final int REQUEST_ENABLE_BT = 1000;
    private static final String ACTION_USB_PERMISSION = "net.proest.lp2go3.USB_PERMISSION";
    private static final String OFFSET_VELOCITY_DOWN = "net.proest.lp2go3.VelocityState-Down";
    private static final String OFFSET_BAROSENSOR_ALTITUDE = "net.proest.lp2go3.BaroSensor-Altitude";
    private static final int VIEW_MAIN = 0;
    private static final int VIEW_MAP = 1;
    private static final int VIEW_OBJECTS = 2;
    private static final int VIEW_SETTINGS = 3;
    private static final int VIEW_LOGS = 4;
    private static final int VIEW_ABOUT = 5;
    private static final int VIEW_PID = 6;
    private static final int PID_RATE_ROLL_PROP_DENOM = 100000;
    private static final int PID_RATE_ROLL_PROP_STEP = 10;
    private static final int PID_RATE_ROLL_PROP_MAX = 1000;
    private static final String PID_RATE_ROLL_PROP_DFS = "0.00000";
    private static final int PID_RATE_PITCH_PROP_DENOM = 100000;
    private static final int PID_RATE_PITCH_PROP_STEP = 10;
    private static final int PID_RATE_PITCH_PROP_MAX = 1000;
    private static final String PID_RATE_PITCH_PROP_DFS = "0.00000";
    private static final int PID_RATE_ROLL_INTE_DENOM = 100000;
    private static final int PID_RATE_ROLL_INTE_STEP = 10;
    private static final int PID_RATE_ROLL_INTE_MAX = 1000;
    private static final String PID_RATE_ROLL_INTE_DFS = "0.00000";
    private static final int PID_RATE_PITCH_INTE_DENOM = 100000;
    private static final int PID_RATE_PITCH_INTE_STEP = 10;
    private static final int PID_RATE_PITCH_INTE_MAX = 1000;
    private static final String PID_RATE_PITCH_INTE_DFS = "0.00000";
    private static final int PID_RATE_ROLL_DERI_DENOM = 1000000;
    private static final int PID_RATE_ROLL_DERI_STEP = 1;
    private static final int PID_RATE_ROLL_DERI_MAX = 1000;
    private static final String PID_RATE_ROLL_DERI_DFS = "0.000000";
    private static final int PID_RATE_PITCH_DERI_DENOM = 1000000;
    private static final int PID_RATE_PITCH_DERI_STEP = 1;
    private static final int PID_RATE_PITCH_DERI_MAX = 100000;
    private static final String PID_RATE_PITCH_DERI_DFS = "0.000000";
    private static final int PID_ROLL_PROP_DENOM = 100000;
    private static final int PID_ROLL_PROP_STEP = 10000;
    private static final int PID_ROLL_PROP_MAX = 500000;
    private static final String PID_ROLL_PROP_DFS = "0.000";
    private static final int PID_PITCH_PROP_DENOM = 100000;
    private static final int PID_PITCH_PROP_STEP = 10000;
    private static final int PID_PITCH_PROP_MAX = 500000;
    private static final String PID_PITCH_PROP_DFS = "0.000";
    private static final boolean LOCAL_LOGD = true;
    private final static int HISTORY_MARKER_NUM = 5;
    static boolean sHasPThread = false;
    private static boolean DEV = false;
    private static boolean initDone = false;
    private static int mCurrentView = 0;
    public boolean isReady = false;
    protected TextView txtObjectLogTx;
    protected TextView txtObjectLogRxGood;
    protected TextView txtObjectLogRxBad;
    protected TextView txtAtti;
    protected TextView txtPlan;
    protected TextView txtStab;
    protected TextView txtPath;
    protected TextView txtGPS;
    protected TextView txtGPSSatsInView;
    protected TextView txtSensor;
    protected TextView txtAirspd;
    protected TextView txtMag;
    protected TextView txtInput;
    protected TextView txtOutput;
    protected TextView txtI2C;
    protected TextView txtTelemetry;
    protected TextView txtFlightTelemetry;
    protected TextView txtGCSTelemetry;
    protected TextView txtBatt;
    protected TextView txtTime;
    protected TextView txtConfig;
    protected TextView txtBoot;
    protected TextView txtStack;
    protected TextView txtMem;
    protected TextView txtEvent;
    protected TextView txtCPU;
    protected TextView txtArmed;
    protected TextView txtVolt;
    protected TextView txtAmpere;
    protected TextView txtmAh;
    protected TextView txtTimeLeft;
    protected TextView txtCapacity;
    protected TextView txtCells;
    protected TextView txtAltitude;
    protected TextView txtAltitudeAccel;
    protected TextView txtModeNum;
    protected TextView txtModeFlightMode;
    protected TextView txtFlightTime;
    protected TextView txtModeSettingsBank;
    protected TextView txtModeAssistedControl;
    protected TextView txtModeRoll;
    protected TextView txtModePitch;
    protected TextView txtModeYaw;
    protected TextView txtModeThrust;
    protected TextView txtLatitude;
    protected TextView txtLongitude;
    protected TextView txtMapGPS;
    protected TextView txtMapGPSSatsInView;
    protected TextView txtVehicleName;
    protected ImageView imgBluetooth;
    protected ImageView imgUSB;
    protected ImageView imgPacketsUp;
    protected ImageView imgPacketsGood;
    protected ImageView imgPacketsBad;
    protected TextView txtObjects;
    protected TextView txtLogFilenameLabel;
    protected TextView txtLogFilename;
    protected TextView txtLogSize;
    protected TextView txtLogSizeLabel;
    protected TextView txtLogObjects;
    protected TextView txtLogObjectsLabel;
    protected TextView txtLogDuration;
    protected TextView txtLogDurationLabel;
    protected Spinner spnUavoSource;
    protected Spinner spnConnectionTypeSpinner;
    protected Spinner spnBluetoothPairedDevice;
    protected PidSeekBar sbrPidRateRollProportional;
    protected PidSeekBar sbrPidRatePitchProportional;
    protected PidSeekBar sbrPidRateRollIntegral;
    protected PidSeekBar sbrPidRatePitchIntegral;
    protected PidSeekBar sbrPidRateRollDerivative;
    protected PidSeekBar sbrPidRatePitchDerivative;
    protected PidSeekBar sbrPidRollProportional;
    protected PidSeekBar sbrPidPitchProportional;
    protected TextView txtDeviceText;
    int mCurrentPosMarker = 0;
    private String loadedUavo = null;
    private BluetoothAdapter mBluetoothAdapter;
    private long mTxObjects;
    private long mRxObjectsGood;
    private long mRxObjectsBad;
    private int serialConnectionState;
    private int mSerialModeUsed = -1;
    private String mBluetoothDeviceUsed = null;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mNavMenuTitles;
    private TypedArray mNavMenuIcons;
    private ArrayList<NavDrawerItem> mNavDrawerItems;
    private NavDrawerListAdapter mDrawListAdapter;
    private Hashtable<String, Object> mOffset;
    private PollThread mPollThread = null;
    private ConnectionThread mConnectionThread = null;
    private UsbManager mUsbManager = null;
    private UsbDevice mDevice;
    private UsbDeviceConnection mDeviceConnection;
    private PendingIntent mPermissionIntent = null;
    private UsbInterface mInterface;
    private UAVTalkDevice mUAVTalkDevice;
    private Hashtable<String, UAVTalkXMLObject> mXmlObjects = null;
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("USB", action);

            if (mSerialModeUsed == SERIAL_USB && UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                Log.d("USB", device.getVendorId() + "-" + device.getProductId() + "-" + device.getDeviceClass()
                        + " " + device.getDeviceSubclass() + " " + device.getDeviceProtocol());

                if (device.getDeviceClass() == UsbConstants.USB_CLASS_MISC) {
                    mUsbManager.requestPermission(device, mPermissionIntent);
                }

                txtDeviceText.setText(device.getDeviceName());


            } else if (mSerialModeUsed == SERIAL_USB && UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null /*&& device.equals(deviceName)*/) {
                    setUsbInterface(null, null);
                    if (mUAVTalkDevice != null) {
                        mUAVTalkDevice.stop();
                    }
                }
                txtDeviceText.setText(R.string.DEVICE_NAME_NONE);
            } else if (mSerialModeUsed == SERIAL_USB && ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                            UsbInterface intf = findAdbInterface(device);
                            if (intf != null) {
                                boolean ok = setUsbInterface(device, intf);
                            }
                        }
                    } else {
                        Log.d("DBG", "permission denied for device " + mDevice);
                    }
                }
            }
        }
    };
    private boolean mSettingsOK = false;
    private Marker[] mPosHistory = new Marker[HISTORY_MARKER_NUM];
    private AlertDialog.Builder mBatteryCapacityDialogBuilder;
    private AlertDialog.Builder mBatteryCellsDialogBuilder;
    private View mView0, mView0l, mView1, mView2, mView3, mView4, mView5, mView6;
    private GoogleMap mMap;
    private MapView mMapView;
    private boolean mDoReconnect = false;

    static private UsbInterface findAdbInterface(UsbDevice device) {

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

        Log.d("STARTING", "CopyAssets");
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list(UAVO_INTERNAL_PATH);
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for (String filename : files) {
            InputStream in = null;
            FileOutputStream out = null;
            try {
                Log.d("COPY", "Copy " + filename);
                in = assetManager.open(UAVO_INTERNAL_PATH + File.separator + filename);
                out = openFileOutput(UAVO_INTERNAL_PATH + "-" + filename, Context.MODE_PRIVATE);
                //File outFile = new File(out, Filename);


                //out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
        }
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
        mNavMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        mNavMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        mNavDrawerItems = new ArrayList<NavDrawerItem>();

        mNavDrawerItems.add(new NavDrawerItem(mNavMenuTitles[0], mNavMenuIcons.getResourceId(0, -1)));
        mNavDrawerItems.add(new NavDrawerItem(mNavMenuTitles[1], mNavMenuIcons.getResourceId(1, -1)));
        mNavDrawerItems.add(new NavDrawerItem(mNavMenuTitles[2], mNavMenuIcons.getResourceId(2, -1)));
        mNavDrawerItems.add(new NavDrawerItem(mNavMenuTitles[3], mNavMenuIcons.getResourceId(3, -1)));
        mNavDrawerItems.add(new NavDrawerItem(mNavMenuTitles[4], mNavMenuIcons.getResourceId(4, -1)));
        mNavDrawerItems.add(new NavDrawerItem(mNavMenuTitles[5], mNavMenuIcons.getResourceId(5, -1)));

        mNavMenuIcons.recycle();
        mDrawListAdapter = new NavDrawerListAdapter(getApplicationContext(),
                mNavDrawerItems);
        mDrawerList.setAdapter(mDrawListAdapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);


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
        //menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);

        displayView(mCurrentView);
    }


    public void reconnect() {
        mDoReconnect = true;
    }

    private void initViewMain(Bundle savedInstanceState) {
        mView0 = getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(mView0);  //Main

        initSlider(savedInstanceState);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        mOffset = new Hashtable<String, Object>();
        mOffset.put(OFFSET_BAROSENSOR_ALTITUDE, .0f);
        mOffset.put(OFFSET_VELOCITY_DOWN, .0f);

        imgPacketsUp = (ImageView) findViewById(R.id.imgPacketsUp);
        imgPacketsGood = (ImageView) findViewById(R.id.imgPacketsGood);
        imgPacketsBad = (ImageView) findViewById(R.id.imgPacketsBad);

        txtObjectLogTx = (TextView) findViewById(R.id.txtObjectLogTx);
        txtObjectLogRxGood = (TextView) findViewById(R.id.txtObjectLogRxGood);
        txtObjectLogRxBad = (TextView) findViewById(R.id.txtObjectLogRxBad);

        txtDeviceText = (TextView) findViewById(R.id.txtDeviceName);

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
        txtFlightTelemetry = (TextView) findViewById(R.id.txtFlightTelemetry);
        txtGCSTelemetry = (TextView) findViewById(R.id.txtGCSTelemetry);

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

        txtCapacity = (TextView) findViewById(R.id.txtCapacity);
        txtCells = (TextView) findViewById(R.id.txtCells);

        txtAltitude = (TextView) findViewById(R.id.txtAltitude);
        txtAltitudeAccel = (TextView) findViewById(R.id.txtAltitudeAccel);

        txtModeNum = (TextView) findViewById(R.id.txtModeNum);
        txtModeFlightMode = (TextView) findViewById(R.id.txtModeFlightMode);

        txtModeAssistedControl = (TextView) findViewById(R.id.txtModeAssistedControl);
        txtVehicleName = (TextView) findViewById(R.id.txtVehicleName);

        mSerialModeUsed = sharedPref.getInt(getString(R.string.SETTINGS_SERIAL_MODE), 0);
        mBluetoothDeviceUsed = sharedPref.getString(getString(R.string.SETTINGS_BT_NAME), null);

        imgBluetooth = (ImageView) findViewById(R.id.imgBluetooth);
        if (mSerialModeUsed != SERIAL_BLUETOOTH || mSerialModeUsed == SERIAL_NONE) {
            //imgBluetooth.setVisibility(View.INVISIBLE);
            imgBluetooth.setAlpha(ICON_TRANSPARENT);
        } else {

            imgBluetooth.setColorFilter(Color.argb(255, 255, 0, 0));
        }

        imgUSB = (ImageView) findViewById(R.id.imgUSB);
        if (mSerialModeUsed != SERIAL_USB || mSerialModeUsed == SERIAL_NONE) {
            imgUSB.setAlpha(ICON_TRANSPARENT);
        } else {
            imgUSB.setColorFilter(Color.argb(255, 255, 0, 0));
        }

    }

    private void initViewMap(Bundle savedInstanceState) {
        mView1 = getLayoutInflater().inflate(R.layout.activity_map, null);
        setContentView(mView1); //Map
        {
            mMapView = (MapView) findViewById(R.id.map);
            mMapView.onCreate(savedInstanceState);

            mMap = mMapView.getMap();
            if (mMap != null) {  //Map can be null if services are not available, e.g. on an amazon fire tab
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.setMyLocationEnabled(true);
                MapsInitializer.initialize(this);

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(32.154599, -110.827369), 18);
                mMap.animateCamera(cameraUpdate);
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                Marker center = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(32.154599, -110.827369))
                        .title("Librepilot")
                        .snippet("LP rules")
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
            }
            txtLatitude = (TextView) findViewById(R.id.txtLatitude);
            txtLongitude = (TextView) findViewById(R.id.txtLongitude);
            txtMapGPS = (TextView) findViewById(R.id.txtMapGPS);
            txtMapGPSSatsInView = (TextView) findViewById(R.id.txtMapGPSSatsInView);
        }
    }

    private void initViewObjects() {
        mView2 = getLayoutInflater().inflate(R.layout.activity_objects, null);
        setContentView(mView2); //Objects
        {
            txtObjects = (EditText) findViewById(R.id.etxObjects);
        }
    }

    private void initViewSettings() {
        mView3 = getLayoutInflater().inflate(R.layout.activity_settings, null);
        setContentView(mView3); //Settings

            spnConnectionTypeSpinner = (Spinner) findViewById(R.id.spnConnectionTypeSpinner);
            ArrayAdapter<CharSequence> serialConnectionTypeAdapter = ArrayAdapter.createFromResource(this,
                    R.array.connections_settings, android.R.layout.simple_spinner_item);

            serialConnectionTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spnConnectionTypeSpinner.setAdapter(serialConnectionTypeAdapter);
            spnConnectionTypeSpinner.setOnItemSelectedListener(this);
            spnConnectionTypeSpinner.setSelection(mSerialModeUsed);

            spnBluetoothPairedDevice = (Spinner) findViewById(R.id.spnBluetoothPairedDevice);

        ArrayAdapter<CharSequence> btPairedDeviceAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        btPairedDeviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnBluetoothPairedDevice.setAdapter(btPairedDeviceAdapter);
            spnBluetoothPairedDevice.setOnItemSelectedListener(this);

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null) {
                // Device does support Bluetooth

                if (!mBluetoothAdapter.isEnabled()) {
                    //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    Toast.makeText(this, "To use Bluetooth, turn it on in your device.", Toast.LENGTH_LONG);

                } else {

                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                    // If there are paired devices
                    if (pairedDevices.size() > 0) {
                        // Loop through paired devices
                        int btpd = 0;
                        for (BluetoothDevice device : pairedDevices) {
                            // Add the name and address to an array adapter to show in a ListView
                            btPairedDeviceAdapter.add(device.getName());
                            if (device.getName().equals(mBluetoothDeviceUsed)) {
                                spnBluetoothPairedDevice.setSelection(btpd);
                            }
                            btpd++;
                            if (LOCAL_LOGD)
                                Log.d("BTE", device.getName() + " " + device.getAddress());
                        }
                    }
                }
            }

        spnUavoSource = (Spinner) findViewById(R.id.spnUavoSource);
        ArrayAdapter<CharSequence> uavoSourceAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        uavoSourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnUavoSource.setAdapter(uavoSourceAdapter);
        spnUavoSource.setOnItemSelectedListener(this);

        File dir = getFilesDir();
        File[] subFiles = dir.listFiles();

        if (subFiles != null) {
            for (File file : subFiles) {
                Pattern p = Pattern.compile(".*uavo-(.*)\\.zip");
                Matcher m = p.matcher(file.toString());
                boolean b = m.matches();
                if (b) {
                    Log.d("FILELIST", file.toString());
                    uavoSourceAdapter.add(m.group(1));
                }
            }
        }


    }

    private void initViewLogs() {
        mView4 = getLayoutInflater().inflate(R.layout.activity_logs, null);
        setContentView(mView4); //Logs
        {
            txtLogFilename = (TextView) findViewById(R.id.txtLogFilename);
            txtLogSize = (TextView) findViewById(R.id.txtLogSize);
            txtLogObjects = (TextView) findViewById(R.id.txtLogObjects);
            txtLogDuration = (TextView) findViewById(R.id.txtLogDuration);
        }
    }

    private void initViewAbout() {
        mView5 = getLayoutInflater().inflate(R.layout.activity_about, null);
        setContentView(mView5);  //About
        {
            PackageInfo pInfo = null;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            ((TextView) findViewById(R.id.txtAndroidVersionRelease))
                    .setText(getString(R.string.RUNNING_ON_ANDROID_VERSION) + Build.VERSION.RELEASE);
            ((TextView) findViewById(R.id.txtLP2GoVersionRelease))
                    .setText(getString(R.string.LP2GO_RELEASE) + pInfo.versionName + " " + getString(R.string.OPEN_ROUND_BRACKET_WITH_SPACE) + pInfo.versionName + getString(R.string.CLOSE_ROUND_BRACKET));
            ((TextView) findViewById(R.id.txtLP2GoPackage)).setText(pInfo.packageName);
        }

    }

    private void initViewPid() {
        mView6 = getLayoutInflater().inflate(R.layout.activity_pid, null);
        setContentView(mView6);
        {
            sbrPidRateRollProportional = (PidSeekBar) findViewById(R.id.sbrPidRateRollProportional);
            sbrPidRateRollProportional.init((TextView) findViewById(R.id.txtPidRateRollProportional),
                    (ImageView) findViewById(R.id.imgRateRollProportionalLock),
                    (ImageView) findViewById(R.id.imgRateRollProportionalPlus),
                    (ImageView) findViewById(R.id.imgRateRollProportionalMinus),
                    PID_RATE_ROLL_PROP_DENOM, PID_RATE_ROLL_PROP_MAX,
                    PID_RATE_ROLL_PROP_STEP, PID_RATE_ROLL_PROP_DFS);

            sbrPidRatePitchProportional = (PidSeekBar) findViewById(R.id.sbrPidRatePitchProportional);
            sbrPidRatePitchProportional.init((TextView) findViewById(R.id.txtPidRatePitchProportional),
                    (ImageView) findViewById(R.id.imgRatePitchProportionalLock),
                    (ImageView) findViewById(R.id.imgRatePitchProportionalPlus),
                    (ImageView) findViewById(R.id.imgRatePitchProportionalMinus),
                    PID_RATE_PITCH_PROP_DENOM, PID_RATE_PITCH_PROP_MAX,
                    PID_RATE_PITCH_PROP_STEP, PID_RATE_PITCH_PROP_DFS);

            sbrPidRateRollIntegral = (PidSeekBar) findViewById(R.id.sbrPidRateRollIntegral);
            sbrPidRateRollIntegral.init((TextView) findViewById(R.id.txtPidRateRollIntegral),
                    (ImageView) findViewById(R.id.imgRateRollIntegralLock),
                    (ImageView) findViewById(R.id.imgRateRollIntegralPlus),
                    (ImageView) findViewById(R.id.imgRateRollIntegralMinus),
                    PID_RATE_ROLL_INTE_DENOM, PID_RATE_ROLL_INTE_MAX,
                    PID_RATE_ROLL_INTE_STEP, PID_RATE_ROLL_INTE_DFS);

            sbrPidRatePitchIntegral = (PidSeekBar) findViewById(R.id.sbrPidRatePitchIntegral);
            sbrPidRatePitchIntegral.init((TextView) findViewById(R.id.txtPidRatePitchIntegral),
                    (ImageView) findViewById(R.id.imgRatePitchIntegralLock),
                    (ImageView) findViewById(R.id.imgRatePitchIntegralPlus),
                    (ImageView) findViewById(R.id.imgRatePitchIntegralMinus),
                    PID_RATE_PITCH_INTE_DENOM, PID_RATE_PITCH_INTE_MAX,
                    PID_RATE_PITCH_INTE_STEP, PID_RATE_PITCH_INTE_DFS);

            sbrPidRollProportional = (PidSeekBar) findViewById(R.id.sbrPidRollProportional);
            sbrPidRollProportional.init((TextView) findViewById(R.id.txtPidRollProportional),
                    (ImageView) findViewById(R.id.imgRollProportionalLock),
                    (ImageView) findViewById(R.id.imgRollProportionalPlus),
                    (ImageView) findViewById(R.id.imgRollProportionalMinus),
                    PID_ROLL_PROP_DENOM, PID_ROLL_PROP_MAX,
                    PID_ROLL_PROP_STEP, PID_ROLL_PROP_DFS);

            sbrPidPitchProportional = (PidSeekBar) findViewById(R.id.sbrPidPitchProportional);
            sbrPidPitchProportional.init((TextView) findViewById(R.id.txtPidPitchProportional),
                    (ImageView) findViewById(R.id.imgPitchProportionalLock),
                    (ImageView) findViewById(R.id.imgPitchProportionalPlus),
                    (ImageView) findViewById(R.id.imgPitchProportionalMinus),
                    PID_PITCH_PROP_DENOM, PID_PITCH_PROP_MAX,
                    PID_PITCH_PROP_STEP, PID_PITCH_PROP_DFS);

            sbrPidRateRollDerivative = (PidSeekBar) findViewById(R.id.sbrPidRateRollDerivative);
            sbrPidRateRollDerivative.init((TextView) findViewById(R.id.txtPidRateRollDerivative),
                    (ImageView) findViewById(R.id.imgRateRollDerivativeLock),
                    (ImageView) findViewById(R.id.imgRateRollDerivativePlus),
                    (ImageView) findViewById(R.id.imgRateRollDerivativeMinus),
                    PID_RATE_ROLL_DERI_DENOM, PID_RATE_ROLL_DERI_MAX,
                    PID_RATE_ROLL_DERI_STEP, PID_RATE_ROLL_DERI_DFS);

            sbrPidRatePitchDerivative = (PidSeekBar) findViewById(R.id.sbrPidRatePitchDerivative);
            sbrPidRatePitchDerivative.init((TextView) findViewById(R.id.txtPidRatePitchDerivative),
                    (ImageView) findViewById(R.id.imgRatePitchDerivativeLock),
                    (ImageView) findViewById(R.id.imgRatePitchDerivativePlus),
                    (ImageView) findViewById(R.id.imgRatePitchDerivativeMinus),
                    PID_RATE_PITCH_DERI_DENOM, PID_RATE_PITCH_DERI_MAX,
                    PID_RATE_PITCH_DERI_STEP, PID_RATE_PITCH_DERI_DFS);

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("INIT", "" + initDone);
        serialConnectionState = SERIAL_DISCONNECTED;

        copyAssets();

        initViewPid();
        initViewAbout();
        initViewLogs();
        initViewSettings();
        initViewObjects();
        initViewMap(savedInstanceState);
        initViewMain(savedInstanceState);


        initDone = true;

        isReady = true;
    }

    private boolean loadXmlObjects() {
        String fileid = "uav-15.09";
        return loadXmlObjects(fileid, false);
    }

    private boolean loadXmlObjects(String fileid, boolean overwrite) {

        if (mXmlObjects == null || overwrite) {
            mXmlObjects = new Hashtable<String, UAVTalkXMLObject>();

            AssetManager assets = getAssets();

            this.loadedUavo = fileid;
            String file = fileid + ".zip"; //TODO: Get files from internal storage
            ZipInputStream zis = null;
            try {
                InputStream is = assets.open(UAVO_INTERNAL_PATH + File.separator + file);
                zis = new ZipInputStream(new BufferedInputStream(is));
                ZipEntry ze;
                while ((ze = zis.getNextEntry()) != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int count;
                    while ((count = zis.read(buffer)) != -1) {
                        baos.write(buffer, 0, count);
                    }
                    String filename = ze.getName();
                    String xml = baos.toString();
                    //byte[] bytes = baos.toByteArray();
                    // do something with 'filename' and 'bytes'...
                    //Log.d(filename, "" + xml.length());
                    if (xml.length() > 0) {
                        UAVTalkXMLObject obj = new UAVTalkXMLObject(baos.toString());
                        mXmlObjects.put(obj.getName(), obj);
                    }

                }
            } catch (IOException | SAXException | ParserConfigurationException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (zis != null) zis.close();
                } catch (IOException e) {

                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("START", "" + isChangingConfigurations() + " - " + initDone);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        mSerialModeUsed = sharedPref.getInt(getString(R.string.SETTINGS_SERIAL_MODE), 0);
        mBluetoothDeviceUsed = sharedPref.getString(getString(R.string.SETTINGS_BT_NAME), null);

        if (mPermissionIntent == null)
            mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

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

        /*
        if (mSerialModeUsed == SERIAL_USB ||
                (mSerialModeUsed == SERIAL_BLUETOOTH && mBluetoothDeviceUsed != null)) {
            setContentView(mView0);
            displayView(VIEW_MAIN);  //reset to start view
        } else {
            setContentView(mView3);
            displayView(VIEW_SETTINGS); //reset to settings view
        }
        */

        displayView(mCurrentView);

//        setContentView(mView0);
//        displayView(VIEW_MAIN);  //reset to start view

        Log.d("onStart", "onStart");
        if (DEV) {
            setContentView(mView6);
            mCurrentView = VIEW_PID;
        }
    }

    @Override
    protected void onStop() {

        Log.d("STOP", "" + isChangingConfigurations() + " - " + initDone);

        if (mUAVTalkDevice != null) mUAVTalkDevice.setLogging(false);

        if (mPollThread != null) {
            mPollThread.setInvalid();
            mPollThread = null;
        }

        if (mConnectionThread != null) {
            mConnectionThread.setInvalid();
            mConnectionThread = null;
            sHasPThread = false;
        }

        mSerialModeUsed = SERIAL_NONE;
        mDoReconnect = true;

        if (mUAVTalkDevice != null) mUAVTalkDevice.stop();
        mUAVTalkDevice = null;

        unregisterReceiver(mUsbReceiver);
        setUsbInterface(null, null);
        mPermissionIntent = null;

        Log.d("onStop", "onStop");

        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void connectUSB() {
        if (mSerialModeUsed == SERIAL_USB) {
            for (UsbDevice device : mUsbManager.getDeviceList().values()) {
                UsbInterface intf = findAdbInterface(device);
                if (device.getDeviceClass() == UsbConstants.USB_CLASS_MISC) {
                    mUsbManager.requestPermission(device, mPermissionIntent);
                }
            }
        }
    }

    private void connectBluetooth(MainActivity activity) {
        if (mSerialModeUsed == SERIAL_BLUETOOTH) {
            setBluetoothInterface(activity);
        }
    }

    public void setContentView(View v, int p) {
        if (mCurrentView != p) {
            mCurrentView = p;
            super.setContentView(v);
            initSlider(null);
        }
    }

    private void displayView(int position) {
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

                String btname = "";
                if (spnBluetoothPairedDevice.getSelectedItem() != null) {
                    btname = spnBluetoothPairedDevice.getSelectedItem().toString();
                }

                String btmac = "";

                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                // If there are paired devices
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    int btpd = 0;
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        if (device.getName().equals(btname)) {
                            btmac = device.getAddress();
                        }
                        btpd++;
                        //Log.d("BTE",device.getName() + " " + device.getAddress());
                    }
                }

                //etxSettingsBTMac.setText(btmac);
                editor.putString(getString(R.string.SETTINGS_BT_MAC), btmac);
                editor.putString(getString(R.string.SETTINGS_BT_NAME), btname);
                editor.commit();
                mDoReconnect = true;

                break;
            case VIEW_LOGS:

                break;
            case VIEW_ABOUT:

                break;

            default:
                break;
        }


        //init new view
        switch (position) {
            case VIEW_MAIN:
                initViewMain(null);
                fragment = new MainFragment();
                setContentView(mView0, position);

                break;
            case VIEW_MAP:
                fragment = new MapFragment();
                setContentView(mView1, position);
                mMapView.onResume();  //(re)activate the mMap

                break;
            case VIEW_OBJECTS:
                fragment = new ObjectsFragment();
                setContentView(mView2, position);

                break;
            case VIEW_SETTINGS:
                fragment = new SettingsFragment();
                setContentView(mView3, position);

                if (mSerialModeUsed == SERIAL_NONE) {
                    Toast.makeText(this, getString(R.string.PLEASE_SET_A) + "Connection Type", Toast.LENGTH_LONG).show();
                } else if (mSerialModeUsed == SERIAL_BLUETOOTH && mBluetoothDeviceUsed == null) {
                    Toast.makeText(this, getString(R.string.PLEASE_SET_A) + "Bluetooth Device", Toast.LENGTH_LONG).show();
                }


                break;
            case VIEW_LOGS:
                fragment = new LogsFragment();
                setContentView(mView4, position);

                break;
            case VIEW_ABOUT:
                fragment = new AboutFragment();
                setContentView(mView5, position);

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
                Log.e("FIXME", "After wakeup in different orientation, this happens.");
            }

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(mNavMenuTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    private String readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toString();
    }

    public void onBatteryCapacityClick(View v) {
        initBatteryCapacityDialog();
        mBatteryCapacityDialogBuilder.show();
    }

    public void onAltitudeClick(View V) {
        try {
            mOffset.put(OFFSET_BAROSENSOR_ALTITUDE, mUAVTalkDevice.getObjectTree().getData("BaroSensor", "Altitude"));
            txtAltitude.setText(R.string.EMPTY_STRING);
        } catch (UAVTalkMissingObjectException | NullPointerException e) {
            //e.printStackTrace();
        }
    }

    public void onAltitudeAccelClick(View V) {
        try {
            mOffset.put(OFFSET_VELOCITY_DOWN, mUAVTalkDevice.getObjectTree().getData("VelocityState", "Down"));
            txtAltitudeAccel.setText(R.string.EMPTY_STRING);
        } catch (UAVTalkMissingObjectException e) {
            //e.printStackTrace();
        }
    }

    public void onBatteryCellsClick(View v) {
        initBatteryCellsDialog();
        mBatteryCellsDialogBuilder.show();
    }

    public void onLogStartClick(View v) {
        try {
            mUAVTalkDevice.setLogging(true);
        } catch (NullPointerException e) {

        }
    }

    public void onLogStopClick(View v) {
        try {
            mUAVTalkDevice.setLogging(false);
        } catch (NullPointerException e) {

        }
    }

    public void onLogShare(View v) {
        try {
            mUAVTalkDevice.setLogging(false);
        } catch (NullPointerException e) {
            return;
        }
        Intent share = new Intent(Intent.ACTION_SEND);

        share.setType(getString(R.string.MIME_APPLICATION_OCTETSTREAM));

        File logPath = new File(this.getFilesDir(), "");
        File logFile = new File(logPath, mUAVTalkDevice.getLogFileName());
        Uri contentUri = FileProvider.getUriForFile(this, "net.proest.lp2go3.logfileprovider", logFile);

        share.putExtra(Intent.EXTRA_STREAM, contentUri);
        startActivity(Intent.createChooser(share, getString(R.string.SHARE_LOG_TITLE)));
    }

    public void setPollThreadObjectTree(UAVTalkObjectTree oTree) {
        mPollThread.setObjectTree(oTree);
    }

    private boolean setBluetoothInterface(MainActivity activity) {
        if (mUAVTalkDevice != null) {
            mUAVTalkDevice.stop();
        }
        mUAVTalkDevice = null;
        mUAVTalkDevice = new UAVTalkBluetoothDevice(this, mXmlObjects);
        mUAVTalkDevice.start();

        return mUAVTalkDevice != null;
    }

    private boolean setUsbInterface(UsbDevice device, UsbInterface intf) {
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
                    mUAVTalkDevice = new UAVTalkUsbDevice(this, mDeviceConnection, intf, mXmlObjects);
                    mUAVTalkDevice.getObjectTree().setXmlObjects(mXmlObjects);

                    mUAVTalkDevice.start();
                    txtDeviceText.setText(device.getDeviceName());
                    return true;
                } else {
                    connection.close();
                }
            }
        }

        if (mDeviceConnection == null && mUAVTalkDevice != null) {
            mUAVTalkDevice.stop();
            mUAVTalkDevice = null;
        }
        return false;
    }

    private void initBatteryCapacityDialog() {
        mBatteryCapacityDialogBuilder = new AlertDialog.Builder(this);
        mBatteryCapacityDialogBuilder.setTitle(R.string.CAPACITY_DIALOG_TITLE);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mBatteryCapacityDialogBuilder.setView(input);
        input.setText(txtCapacity.getText());

        mBatteryCapacityDialogBuilder.setPositiveButton(R.string.OK_BUTTON, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                byte[] bcdata;
                try {
                    bcdata = H.toBytes(Integer.parseInt(input.getText().toString()));
                } catch (NumberFormatException e) {
                    bcdata = H.toBytes(0);
                }
                if (mUAVTalkDevice != null && bcdata.length == 4) {
                    bcdata = H.reverse4bytes(bcdata);
                    mUAVTalkDevice.sendSettingsObject("FlightBatterySettings", 0, "Capacity", 0, bcdata);
                }
                dialog.dismiss();
                dialog.cancel();
            }
        });

        mBatteryCapacityDialogBuilder.setNegativeButton(R.string.CANCEL_BUTTON, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                dialog.dismiss();
            }
        });
    }

    private void initBatteryCellsDialog() {
        mBatteryCellsDialogBuilder = new AlertDialog.Builder(this);
        mBatteryCellsDialogBuilder.setTitle(R.string.CELLS_DIALOG_TITLE);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mBatteryCellsDialogBuilder.setView(input);
        input.setText(txtCells.getText());

        mBatteryCellsDialogBuilder.setPositiveButton(R.string.OK_BUTTON, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                byte[] bcdata = new byte[1];
                try {
                    bcdata[0] = H.toBytes(Integer.parseInt(input.getText().toString()))[3]; //want the lsb
                } catch (NumberFormatException e) {
                    bcdata[0] = 0x00;
                }
                if (mUAVTalkDevice != null && bcdata.length == 1) {
                    mUAVTalkDevice.sendSettingsObject("FlightBatterySettings", 0, "NbCells", 0, bcdata);
                }
                dialog.dismiss();
                dialog.cancel();
            }
        });

        mBatteryCellsDialogBuilder.setNegativeButton(R.string.CANCEL_BUTTON, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        switch (parent.getId()) {
            case R.id.spnConnectionTypeSpinner: {
                mSerialModeUsed = pos;
                imgBluetooth.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
                imgUSB.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
                switch (mSerialModeUsed) {
                    case SERIAL_NONE:

                        imgBluetooth.setAlpha(ICON_TRANSPARENT);
                        imgUSB.setAlpha(ICON_TRANSPARENT);
                        break;
                    case SERIAL_USB:
                        imgBluetooth.setAlpha(ICON_TRANSPARENT);
                        imgUSB.setAlpha(ICON_OPAQUE);
                        break;
                    case SERIAL_BLUETOOTH:
                        imgBluetooth.setAlpha(ICON_OPAQUE);
                        imgUSB.setAlpha(ICON_TRANSPARENT);
                        break;
                }
                SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(getString(R.string.SETTINGS_SERIAL_MODE), mSerialModeUsed);
                editor.commit();
                break;
            }
            case R.id.spnUavoSource: {
                String fileid = spnUavoSource.getItemAtPosition(pos).toString();
                Log.d("UAVSource", fileid + "  " + loadedUavo);
                if (!loadedUavo.equals(fileid)) {
                    loadXmlObjects(fileid, true);
                    Toast.makeText(this, "UAVO load completed", Toast.LENGTH_SHORT).show();
                }

                break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void resetMainView() {
        txtDeviceText.setText(R.string.EMPTY_STRING);
        txtAtti.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtStab.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtPath.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtPlan.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));

        txtGPSSatsInView.setText(R.string.EMPTY_STRING);
        txtGPS.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtSensor.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtAirspd.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtMag.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));

        txtInput.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtOutput.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtI2C.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtTelemetry.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));

        txtFlightTelemetry.setText(R.string.EMPTY_STRING);
        txtGCSTelemetry.setText(R.string.EMPTY_STRING);

        txtBatt.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtTime.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtConfig.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));

        txtBoot.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtMem.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtStack.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtEvent.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtCPU.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));

        txtArmed.setText(R.string.EMPTY_STRING);

        txtVolt.setText(R.string.EMPTY_STRING);
        txtAmpere.setText(R.string.EMPTY_STRING);
        txtmAh.setText(R.string.EMPTY_STRING);
        txtTimeLeft.setText(R.string.EMPTY_STRING);

        txtCapacity.setText(R.string.EMPTY_STRING);
        txtCells.setText(R.string.EMPTY_STRING);

        txtAltitude.setText(R.string.EMPTY_STRING);
        txtAltitudeAccel.setText(R.string.EMPTY_STRING);

        txtModeNum.setText(R.string.EMPTY_STRING);
        txtModeFlightMode.setText(R.string.EMPTY_STRING);
        txtModeAssistedControl.setText(R.string.EMPTY_STRING);
    }

    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            displayView(position);
        }
    }

    private class PollThread extends Thread {

        boolean blink = true;
        private MainActivity mActivity;
        private UAVTalkObjectTree mObjectTree;
        private boolean mIsValid = true;

        public PollThread(MainActivity mActivity) {
            if (sHasPThread) throw new IllegalStateException("double mPollThread");
            sHasPThread = true;
            this.mActivity = mActivity;
        }

        public void setObjectTree(UAVTalkObjectTree mObjectTree) {
            this.mObjectTree = mObjectTree;
        }

        private void setText(TextView t, String text) {
            if (text != null) {
                t.setText(text);
            }
        }

        private void setTextBGColor(TextView t, String color) {
            if (color == null || color == "") {
                return;
            }
            switch (color) {
                case "OK":
                case "None":
                    t.setBackground(mActivity.getResources().getDrawable(R.drawable.rounded_corner_ok));
                    break;
                case "Warning":
                    t.setBackground(mActivity.getResources().getDrawable(R.drawable.rounded_corner_warning));
                    break;
                case "Error":
                    t.setBackground(mActivity.getResources().getDrawable(R.drawable.rounded_corner_error));
                    break;
                case "Critical":
                case "RebootRequired":
                    t.setBackground(mActivity.getResources().getDrawable(R.drawable.rounded_corner_critical));
                    break;
                case "Uninitialised":
                    t.setBackground(mActivity.getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
                    break;
                case "InProgress":
                    t.setBackground(mActivity.getResources().getDrawable(R.drawable.rounded_corner_inprogress));
                    break;
                case "Completed":
                    t.setBackground(mActivity.getResources().getDrawable(R.drawable.rounded_corner_completed));
                    break;
            }
        }

        public void setInvalid() {
            mIsValid = false;
        }

        public void run() {

            //while (isValid) {
            while (mIsValid) {
                blink = !blink;
                //Log.d("PING","PONG");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (mSerialModeUsed == SERIAL_BLUETOOTH) {
                            imgUSB.setColorFilter(Color.argb(0xff, 0x00, 0x00, 0x00));
                            if (mUAVTalkDevice != null && mUAVTalkDevice.isConnected()) {
                                imgBluetooth.setColorFilter(Color.argb(0xff, 0x00, 0x80, 0x00));
                                imgBluetooth.setImageDrawable(getResources().getDrawable(R.drawable.ic_bluetooth_connected_128dp));
                                serialConnectionState = SERIAL_CONNECTED;

                            } else if (mUAVTalkDevice != null && mUAVTalkDevice.isConnecting()) {
                                serialConnectionState = SERIAL_CONNECTING;
                                int alpha;
                                if (blink) {
                                    imgBluetooth.setColorFilter(Color.argb(0xff, 0xff, 0x66, 0x00));
                                    imgBluetooth.setImageDrawable(getResources().getDrawable(R.drawable.ic_bluetooth_128dp));
                                } else {
                                    imgBluetooth.setColorFilter(Color.argb(0xff, 0xff, 0x66, 0x00));
                                    imgBluetooth.setImageDrawable(getResources().getDrawable(R.drawable.ic_bluetooth_connected_128dp));
                                }
                            } else {
                                serialConnectionState = SERIAL_DISCONNECTED;
                                imgBluetooth.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
                                imgBluetooth.setImageDrawable(getResources().getDrawable(R.drawable.ic_bluetooth_disabled_128dp));
                            }
                        } else if (mSerialModeUsed == SERIAL_USB) {
                            imgBluetooth.setColorFilter(Color.argb(0xff, 0x00, 0x00, 0x00));
                            if (mUAVTalkDevice != null && mUAVTalkDevice.isConnected()) {
                                imgUSB.setColorFilter(Color.argb(0xff, 0x00, 0x80, 0x00));
                                //imgUSB.setImageDrawable(getResources().getDrawable(R.drawable.ic_usb_128dp));
                                serialConnectionState = SERIAL_CONNECTED;

                            } else if (mUAVTalkDevice != null && mUAVTalkDevice.isConnecting()) {
                                serialConnectionState = SERIAL_CONNECTING;
                                int alpha;
                                if (blink) {
                                    imgUSB.setColorFilter(Color.argb(0xff, 0xff, 0x66, 0x00));
                                } else {
                                    imgUSB.setColorFilter(Color.argb(0xff, 0xff, 0x88, 0x00));
                                }
                            } else {
                                serialConnectionState = SERIAL_DISCONNECTED;
                                imgUSB.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
                            }
                        }
                    }
                });

                if (this.mObjectTree == null || mUAVTalkDevice == null || !mUAVTalkDevice.isConnected()) {
                    continue;  //nothing yet to show, or not connected
                }

                requestObjects();  //BT is flightmode, so the flow is not flowing..missing handshake?

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            switch (mCurrentView) {
                                case VIEW_MAIN:

                                    txtObjectLogTx.setText(H.k(String.valueOf(mTxObjects)));
                                    txtObjectLogRxGood.setText(H.k(String.valueOf(mRxObjectsGood)));
                                    txtObjectLogRxBad.setText(H.k(String.valueOf(mRxObjectsBad)));

                                    if (blink) {
                                        if (mRxObjectsGood > 0)
                                            imgPacketsGood.setColorFilter(Color.argb(0xff, 0x00, 0x88, 0x00));
                                        if (mRxObjectsBad > 0)
                                            imgPacketsBad.setColorFilter(Color.argb(0xff, 0x88, 0x00, 0x00));
                                        if (mTxObjects > 0)
                                            imgPacketsUp.setColorFilter(Color.argb(0xff, 0x00, 0x00, 0x88));
                                    } else {
                                        if (mRxObjectsGood > 0)
                                            imgPacketsGood.setColorFilter(Color.argb(0xff, 0x00, 0x00, 0x00));
                                        if (mRxObjectsBad > 0)
                                            imgPacketsBad.setColorFilter(Color.argb(0xff, 0x00, 0x00, 0x00));
                                        if (mTxObjects > 0)
                                            imgPacketsUp.setColorFilter(Color.argb(0xff, 0x00, 0x00, 0x00));
                                    }
                                    setTxObjects(0);
                                    setRxObjectsBad(0);
                                    setRxObjectsGood(0);

                                    setText(mActivity.txtVehicleName, getVehicleNameData());

                                    setTextBGColor(mActivity.txtAtti, getData("SystemAlarms", "Alarm", "Attitude").toString());
                                    setTextBGColor(mActivity.txtStab, getData("SystemAlarms", "Alarm", "Stabilization").toString());
                                    setTextBGColor(mActivity.txtPath, getData("PathStatus", "Status").toString());
                                    setTextBGColor(mActivity.txtPlan, getData("SystemAlarms", "Alarm", "PathPlan").toString());

                                    setText(mActivity.txtGPSSatsInView, getData("GPSSatellites", "SatsInView").toString());
                                    setTextBGColor(mActivity.txtGPS, getData("SystemAlarms", "Alarm", "GPS").toString());
                                    setTextBGColor(mActivity.txtSensor, getData("SystemAlarms", "Alarm", "Sensors").toString());
                                    setTextBGColor(mActivity.txtAirspd, getData("SystemAlarms", "Alarm", "Airspeed").toString());
                                    setTextBGColor(mActivity.txtMag, getData("SystemAlarms", "Alarm", "Magnetometer").toString());

                                    setTextBGColor(mActivity.txtInput, getData("SystemAlarms", "Alarm", "Receiver").toString());
                                    setTextBGColor(mActivity.txtOutput, getData("SystemAlarms", "Alarm", "Actuator").toString());
                                    setTextBGColor(mActivity.txtI2C, getData("SystemAlarms", "Alarm", "I2C").toString());
                                    setTextBGColor(mActivity.txtTelemetry, getData("SystemAlarms", "Alarm", "Telemetry").toString());

                                    setText(mActivity.txtFlightTelemetry, getData("FlightTelemetryStats", "Status").toString());
                                    setText(mActivity.txtGCSTelemetry, getData("GCSTelemetryStats", "Status").toString());

                                    setTextBGColor(mActivity.txtBatt, getData("SystemAlarms", "Alarm", "Battery").toString());
                                    setTextBGColor(mActivity.txtTime, getData("SystemAlarms", "Alarm", "FlightTime").toString());
                                    setTextBGColor(mActivity.txtConfig, getData("SystemAlarms", "ExtendedAlarmStatus", "SystemConfiguration").toString());

                                    setTextBGColor(mActivity.txtBoot, getData("SystemAlarms", "Alarm", "BootFault").toString());
                                    setTextBGColor(mActivity.txtMem, getData("SystemAlarms", "Alarm", "OutOfMemory").toString());
                                    setTextBGColor(mActivity.txtStack, getData("SystemAlarms", "Alarm", "StackOverflow").toString());
                                    setTextBGColor(mActivity.txtEvent, getData("SystemAlarms", "Alarm", "EventSystem").toString());
                                    setTextBGColor(mActivity.txtCPU, getData("SystemAlarms", "Alarm", "CPUOverload").toString());

                                    setText(mActivity.txtArmed, getData("FlightStatus", "Armed").toString());

                                    setText(mActivity.txtFlightTime, H.getDateFromMilliSeconds(getData("SystemStats", "FlightTime").toString()));

                                    setText(mActivity.txtVolt, getData("FlightBatteryState", "Voltage").toString());
                                    setText(mActivity.txtAmpere, getData("FlightBatteryState", "Current").toString());
                                    setText(mActivity.txtmAh, getData("FlightBatteryState", "ConsumedEnergy").toString());
                                    setText(mActivity.txtTimeLeft, H.getDateFromSeconds(getData("FlightBatteryState", "EstimatedFlightTime").toString()));

                                    setText(mActivity.txtCapacity, getData("FlightBatterySettings", "Capacity").toString());
                                    setText(mActivity.txtCells, getData("FlightBatterySettings", "NbCells").toString());

                                    setText(mActivity.txtAltitude, getFloatOffsetData("BaroSensor", "Altitude", OFFSET_BAROSENSOR_ALTITUDE));
                                    setText(mActivity.txtAltitudeAccel, getFloatOffsetData("VelocityState", "Down", OFFSET_VELOCITY_DOWN));

                                    String flightModeSwitchPosition = getData("ManualControlCommand", "FlightModeSwitchPosition", true).toString();

                                    setText(mActivity.txtModeNum, flightModeSwitchPosition);
                                    setText(mActivity.txtModeFlightMode, getData("FlightStatus", "FlightMode", true).toString());
                                    setText(mActivity.txtModeAssistedControl, getData("FlightStatus", "FlightModeAssist", true).toString());
                                    break;
                                case VIEW_MAP:

                                    if (mSerialModeUsed == SERIAL_BLUETOOTH) {
                                        mUAVTalkDevice.requestObject("GPSSatellites");
                                        mUAVTalkDevice.requestObject("SystemAlarms");
                                        mUAVTalkDevice.requestObject("GPSPositionSensor");
                                    }

                                    setText(mActivity.txtMapGPSSatsInView, getData("GPSSatellites", "SatsInView").toString());
                                    setTextBGColor(mActivity.txtMapGPS, getData("SystemAlarms", "Alarm", "GPS").toString());
                                    float deg = 0;
                                    try {
                                        deg = (Float) getData("GPSPositionSensor", "Heading");
                                    } catch (Exception e) {

                                    }

                                    Float lat = getGPSCoordinates("GPSPositionSensor", "Latitude");
                                    Float lng = getGPSCoordinates("GPSPositionSensor", "Longitude");

                                    setText(mActivity.txtLatitude, lat.toString());
                                    setText(mActivity.txtLongitude, lng.toString());

                                    LatLng src = mMap.getCameraPosition().target;
                                    LatLng dst = new LatLng(lat, lng);

                                    double distance = H.calculationByDistance(src, dst);
                                    if (distance > 0.001) {
                                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 19);
                                        MapsInitializer.initialize(mActivity);
                                        if (distance < 200) {
                                            mMap.animateCamera(cameraUpdate);
                                        } else {
                                            mMap.moveCamera(cameraUpdate);
                                        }

                                        mPosHistory[mCurrentPosMarker] = mMap.addMarker(new MarkerOptions()
                                                        .position(new LatLng(lat, lng))
                                                        .title("Librepilot")
                                                        .snippet("LP rules")
                                                        .flat(true)
                                                        .anchor(0.5f, 0.5f)
                                                        .rotation(deg)
                                                //.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                                        );

                                        mCurrentPosMarker++;
                                        if (mCurrentPosMarker >= HISTORY_MARKER_NUM) {
                                            mCurrentPosMarker = 0;
                                        }
                                        if (mPosHistory[mCurrentPosMarker] != null) {
                                            mPosHistory[mCurrentPosMarker].remove();
                                        }
                                    } else {
                                    }
                                    break;
                                case VIEW_OBJECTS:
                                    try {
                                        txtObjects.setText(mUAVTalkDevice.getObjectTree().toString());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case VIEW_LOGS:
                                    if (mUAVTalkDevice.isLogging()) {
                                        try {
                                            txtLogFilename.setText(mUAVTalkDevice.getLogFileName());
                                            double lUAV = Math.round(mUAVTalkDevice.getLogBytesLoggedUAV() / 102.4) / 10.;
                                            double lOPL = Math.round(mUAVTalkDevice.getLogBytesLoggedOPL() / 102.4) / 10.;
                                            txtLogSize.setText(String.valueOf(lUAV) + getString(R.string.TAB) + "(" + String.valueOf(lOPL) + ") KB");
                                            txtLogObjects.setText(String.valueOf(mUAVTalkDevice.getLogObjectsLogged()));
                                            txtLogDuration.setText(String.valueOf((System.currentTimeMillis() - mUAVTalkDevice.getLogStartTimeStamp()) / 1000) + " s");
                                        } catch (Exception e) {

                                        }
                                    }
                                    break;
                                //case VIEW_PID:
                                case VIEW_ABOUT:
                                    String fmode = getData("ManualControlCommand", "FlightModeSwitchPosition").toString();
                                    String fm1position = getData("StabilizationSettings", "FlightModeMap", fmode).toString();

                                    //String arming = getData("FlightModeSettings", "Arming").toString();

                                    Log.d("PID", fmode + " " + fm1position + " " + " " + " " + " " + " ");
                                    break;

                            }
                        } catch (NullPointerException e) {
                            Log.d("NPE", "Nullpointer Exception in Pollthread, most likely switched Connections");

                        }
                    }
                });
            }
        }

        private void requestObjects() {
            if (mUAVTalkDevice != null && mSerialModeUsed == SERIAL_BLUETOOTH) {
                mUAVTalkDevice.requestObject("SystemAlarms");
                mUAVTalkDevice.requestObject("PathStatus");
                mUAVTalkDevice.requestObject("GPSSatellites");
                mUAVTalkDevice.requestObject("FlightTelemetryStats");
                mUAVTalkDevice.requestObject("GCSTelemetryStats");
                mUAVTalkDevice.requestObject("FlightStatus");
                mUAVTalkDevice.requestObject("FlightBatteryState");
                mUAVTalkDevice.requestObject("FlightBatterySettings");
                mUAVTalkDevice.requestObject("BaroSensor");
                mUAVTalkDevice.requestObject("VelocityState");
                mUAVTalkDevice.requestObject("ManualControlCommand");
            }
        }

        private String getVehicleNameData() {
            char[] b = new char[20];
            try {
                for (int i = 0; i < 20; i++) {
                    String str = mObjectTree.getData("SystemSettings", 0, "VehicleName", i).toString();
                    b[i] = (char) Byte.parseByte(str);

                }
            } catch (UAVTalkMissingObjectException e) {
                try {
                    mUAVTalkDevice.requestObject("SystemSettings");
                } catch (NullPointerException e2) {
                    e2.printStackTrace();
                }
            }
            return new String(b);
        }

        private Float getGPSCoordinates(String object, String field) {
            try {
                Long l = (Long) mObjectTree.getData(object, field);
                return ((float) l / 10000000);
            } catch (UAVTalkMissingObjectException e1) {
                return 0.0f;
            } catch (NullPointerException e1) {
                return 0.0f;
            }
        }

        private String getFloatOffsetData(String obj, String field, String soffset) {
            try {
                Float f1 = Float.parseFloat(getData(obj, field).toString());
                Float f2 = (Float) mOffset.get(soffset);
                return String.valueOf(H.round(f1 - f2, 2));
            } catch (NumberFormatException e) {
                return "";
            }
        }

        private Object getData(String objectname, String fieldname, boolean request) {
            try {
                if (request) {
                    mUAVTalkDevice.requestObject(objectname);
                }
                return getData(objectname, fieldname);
            } catch (NullPointerException e) {
                //e.printStackTrace();
            }
            return "";
        }

        private Object getData(String objectname, String fieldname) {
            try {
                Object o = mObjectTree.getData(objectname, fieldname);
                if (o != null) return o;
            } catch (UAVTalkMissingObjectException e1) {
                try {
                    mUAVTalkDevice.requestObject(e1.getObjectname(), e1.getInstance());
                } catch (NullPointerException e2) {
                    //e2.printStackTrace();
                }
            } catch (NullPointerException e3) {
                //e3.printStackTrace();
            }
            return "";
        }

        private Object getData(String objectname, String fieldname, String elementname) {
            Object o = null;
            try {
                o = mObjectTree.getData(objectname, fieldname, elementname);
            } catch (UAVTalkMissingObjectException e1) {
                try {
                    mUAVTalkDevice.requestObject(e1.getObjectname(), e1.getInstance());
                } catch (NullPointerException e2) {
                    e2.printStackTrace();
                }
            } catch (NullPointerException e3) {
                e3.printStackTrace();
            }
            if (o != null) {
                return o;
            } else {
                return "";
            }
        }
    }

    private class ConnectionThread extends Thread {
        private MainActivity mActivity;
        private boolean mIsValid = true;

        public ConnectionThread(MainActivity mActivity) {
            this.mActivity = mActivity;
        }

        public void setInvalid() {
            mIsValid = false;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //wakeup little prince
            }


            final long millis = System.currentTimeMillis();

            boolean loaded = loadXmlObjects();
            if (loaded) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity, "UAVO load completed in " + (System.currentTimeMillis() - millis) + " milliseconds", Toast.LENGTH_SHORT).show();

                    }
                });
            }

            while (mIsValid) {

                if (mBluetoothAdapter != null || !mBluetoothAdapter.isEnabled()) {
                    try {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mActivity, "To use Bluetooth, turn it on in your device.", Toast.LENGTH_SHORT);
                            }
                        });
                    } catch (RuntimeException e) {
                        Log.d("RTE", "Toast not successfull");
                    }
                } else {
                    Log.d("RTED", "" + mBluetoothAdapter.isEnabled());
                }

                if (mDoReconnect) {
                    if (mUAVTalkDevice != null) mUAVTalkDevice.stop();
                    mUAVTalkDevice = null;
                    mDoReconnect = false;
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mActivity.resetMainView();
                        }
                    });
                }

                if (mUAVTalkDevice == null || (!mUAVTalkDevice.isConnected() && !mUAVTalkDevice.isConnecting())) {
                    switch (mSerialModeUsed) {
                        case SERIAL_BLUETOOTH:
                            connectBluetooth(mActivity);
                            break;
                        case SERIAL_USB:
                            connectUSB();
                            break;
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //thread wakes up
                }
            }
        }
    }
}
