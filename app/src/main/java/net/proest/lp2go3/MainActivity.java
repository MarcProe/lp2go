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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
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

import net.proest.lp2go3.UAVTalk.UAVTalkDeviceHelper;
import net.proest.lp2go3.UAVTalk.UAVTalkMissingObjectException;
import net.proest.lp2go3.UAVTalk.UAVTalkObjectTree;
import net.proest.lp2go3.UAVTalk.UAVTalkXMLObject;
import net.proest.lp2go3.UAVTalk.device.UAVTalkBluetoothDevice;
import net.proest.lp2go3.UAVTalk.device.UAVTalkDevice;
import net.proest.lp2go3.UAVTalk.device.UAVTalkUsbDevice;
import net.proest.lp2go3.UI.PidSeekBar;
import net.proest.lp2go3.UI.SingleToast;
import net.proest.lp2go3.c.PID;
import net.proest.lp2go3.slider.AboutFragment;
import net.proest.lp2go3.slider.LogsFragment;
import net.proest.lp2go3.slider.MainFragment;
import net.proest.lp2go3.slider.MapFragment;
import net.proest.lp2go3.slider.ObjectsFragment;
import net.proest.lp2go3.slider.PidFragment;
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
import java.util.HashMap;
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

    private static final int POLL_WAIT_TIME = 500;
    private static final int POLL_SECOND_FACTOR = 1000 / POLL_WAIT_TIME;

    private static final boolean LOCAL_LOGD = true;
    private final static int HISTORY_MARKER_NUM = 5;
    static boolean sHasPThread = false;
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
    protected TextView txtFusionAlgorithm;
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
    protected TextView txtModeAssistedControl;
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
    protected TextView txtLogFilename;
    protected TextView txtLogSize;
    protected TextView txtLogObjects;
    protected TextView txtLogDuration;
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

    protected ImageView imgPidBank;
    protected TextView txtDeviceText;
    int mCurrentPosMarker = 0;
    private String mCurrentStabilizationBank;
    private String mLoadedUavo = null;
    private BluetoothAdapter mBluetoothAdapter;
    private long mTxObjects;
    private long mRxObjectsGood;
    private long mRxObjectsBad;
    private int mSerialModeUsed = -1;
    private String mBluetoothDeviceUsed = null;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mNavMenuTitles;
    private HashMap<String, Object> mOffset;
    private PollThread mPollThread = null;
    private ConnectionThread mConnectionThread = null;
    private UsbManager mUsbManager = null;
    private UsbDevice mDevice;
    private UsbDeviceConnection mDeviceConnection;
    private PendingIntent mPermissionIntent = null;
    private UsbInterface mInterface;
    private UAVTalkDevice mUAVTalkDevice;
    private HashMap<String, UAVTalkXMLObject> mXmlObjects = null;
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
                                setUsbInterface(device, intf);
                            }
                        }
                    } else {
                        Log.d("DBG", "permission denied for device " + mDevice);
                    }
                }
            }
        }
    };
    private Marker[] mPosHistory = new Marker[HISTORY_MARKER_NUM];
    private View mView0, mView1, mView2, mView3, mView4, mView5, mView6;
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
        if (files != null) {
            for (String filename : files) {
                try {
                    Log.d("COPY", "Copy " + filename);
                    InputStream in = assetManager.open(UAVO_INTERNAL_PATH + File.separator + filename);
                    FileOutputStream out = openFileOutput(UAVO_INTERNAL_PATH + "-" + filename, Context.MODE_PRIVATE);
                    //File outFile = new File(out, Filename);


                    //out = new FileOutputStream(outFile);
                    copyFile(in, out);
                    in.close();
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    Log.e("tag", "Failed to copy asset file: " + filename, e);
                }
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
        TypedArray navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<NavDrawerItem>();

        navDrawerItems.add(new NavDrawerItem(mNavMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(mNavMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(mNavMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(mNavMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        navDrawerItems.add(new NavDrawerItem(mNavMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        navDrawerItems.add(new NavDrawerItem(mNavMenuTitles[5], navMenuIcons.getResourceId(5, -1)));
        navDrawerItems.add(new NavDrawerItem(mNavMenuTitles[6], navMenuIcons.getResourceId(6, -1)));

        navMenuIcons.recycle();
        NavDrawerListAdapter drawListAdapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(drawListAdapter);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
            ab.setIcon(R.mipmap.ic_launcher);
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
        //menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
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

        mOffset = new HashMap<String, Object>();
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
        txtFusionAlgorithm = (TextView) findViewById(R.id.txtFusionAlgorithm);

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

        //mSerialModeUsed = sharedPref.getInt(getString(R.string.SETTINGS_SERIAL_MODE), 0);
        //mBluetoothDeviceUsed = sharedPref.getString(getString(R.string.SETTINGS_BT_NAME), null);

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
                mMap.addMarker(new MarkerOptions()
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
                SingleToast.makeText(this, "To use Bluetooth, turn it on in your device.", Toast.LENGTH_LONG).show();

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
            int i = 0;
            for (File file : subFiles) {
                Pattern p = Pattern.compile(".*uavo-(.*)\\.zip");
                Matcher m = p.matcher(file.toString());
                boolean b = m.matches();
                if (b) {
                    Log.d("FILELIST", file.toString());
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

        imgPidBank = (ImageView) findViewById(R.id.imgPidBank);

            sbrPidRateRollProportional = (PidSeekBar) findViewById(R.id.sbrPidRateRollProportional);
            sbrPidRateRollProportional.init((TextView) findViewById(R.id.txtPidRateRollProportional),
                    (ImageView) findViewById(R.id.imgRateRollProportionalLock),
                    PID.PID_RATE_ROLL_PROP_DENOM, PID.PID_RATE_ROLL_PROP_MAX,
                    PID.PID_RATE_ROLL_PROP_STEP, PID.PID_RATE_ROLL_PROP_DFS);

            sbrPidRatePitchProportional = (PidSeekBar) findViewById(R.id.sbrPidRatePitchProportional);
            sbrPidRatePitchProportional.init((TextView) findViewById(R.id.txtPidRatePitchProportional),
                    (ImageView) findViewById(R.id.imgRatePitchProportionalLock),
                    PID.PID_RATE_PITCH_PROP_DENOM, PID.PID_RATE_PITCH_PROP_MAX,
                    PID.PID_RATE_PITCH_PROP_STEP, PID.PID_RATE_PITCH_PROP_DFS);

            sbrPidRateRollIntegral = (PidSeekBar) findViewById(R.id.sbrPidRateRollIntegral);
            sbrPidRateRollIntegral.init((TextView) findViewById(R.id.txtPidRateRollIntegral),
                    (ImageView) findViewById(R.id.imgRateRollIntegralLock),
                    PID.PID_RATE_ROLL_INTE_DENOM, PID.PID_RATE_ROLL_INTE_MAX,
                    PID.PID_RATE_ROLL_INTE_STEP, PID.PID_RATE_ROLL_INTE_DFS);

            sbrPidRatePitchIntegral = (PidSeekBar) findViewById(R.id.sbrPidRatePitchIntegral);
            sbrPidRatePitchIntegral.init((TextView) findViewById(R.id.txtPidRatePitchIntegral),
                    (ImageView) findViewById(R.id.imgRatePitchIntegralLock),
                    PID.PID_RATE_PITCH_INTE_DENOM, PID.PID_RATE_PITCH_INTE_MAX,
                    PID.PID_RATE_PITCH_INTE_STEP, PID.PID_RATE_PITCH_INTE_DFS);

            sbrPidRollProportional = (PidSeekBar) findViewById(R.id.sbrPidRollProportional);
            sbrPidRollProportional.init((TextView) findViewById(R.id.txtPidRollProportional),
                    (ImageView) findViewById(R.id.imgRollProportionalLock),
                    PID.PID_ROLL_PROP_DENOM, PID.PID_ROLL_PROP_MAX,
                    PID.PID_ROLL_PROP_STEP, PID.PID_ROLL_PROP_DFS);

            sbrPidPitchProportional = (PidSeekBar) findViewById(R.id.sbrPidPitchProportional);
            sbrPidPitchProportional.init((TextView) findViewById(R.id.txtPidPitchProportional),
                    (ImageView) findViewById(R.id.imgPitchProportionalLock),
                    PID.PID_PITCH_PROP_DENOM, PID.PID_PITCH_PROP_MAX,
                    PID.PID_PITCH_PROP_STEP, PID.PID_PITCH_PROP_DFS);

            sbrPidRateRollDerivative = (PidSeekBar) findViewById(R.id.sbrPidRateRollDerivative);
            sbrPidRateRollDerivative.init((TextView) findViewById(R.id.txtPidRateRollDerivative),
                    (ImageView) findViewById(R.id.imgRateRollDerivativeLock),
                    PID.PID_RATE_ROLL_DERI_DENOM, PID.PID_RATE_ROLL_DERI_MAX,
                    PID.PID_RATE_ROLL_DERI_STEP, PID.PID_RATE_ROLL_DERI_DFS);

            sbrPidRatePitchDerivative = (PidSeekBar) findViewById(R.id.sbrPidRatePitchDerivative);
            sbrPidRatePitchDerivative.init((TextView) findViewById(R.id.txtPidRatePitchDerivative),
                    (ImageView) findViewById(R.id.imgRatePitchDerivativeLock),
                    PID.PID_RATE_PITCH_DERI_DENOM, PID.PID_RATE_PITCH_DERI_MAX,
                    PID.PID_RATE_PITCH_DERI_STEP, PID.PID_RATE_PITCH_DERI_DFS);


    }

    @Override
    public void onRestart() {
        super.onRestart();

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        mSerialModeUsed = sharedPref.getInt(getString(R.string.SETTINGS_SERIAL_MODE), 0);
        mBluetoothDeviceUsed = sharedPref.getString(getString(R.string.SETTINGS_BT_NAME), null);
        mLoadedUavo = sharedPref.getString(getString(R.string.SETTINGS_UAVO_SOURCE), "uav-15.09");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("INIT", "" + initDone);

        copyAssets();

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        mSerialModeUsed = sharedPref.getInt(getString(R.string.SETTINGS_SERIAL_MODE), 0);
        mBluetoothDeviceUsed = sharedPref.getString(getString(R.string.SETTINGS_BT_NAME), null);
        mLoadedUavo = sharedPref.getString(getString(R.string.SETTINGS_UAVO_SOURCE), "uav-15.09");

        initViewPid();
        initViewAbout();
        initViewLogs();
        initViewSettings();
        initViewObjects();
        initViewMap(savedInstanceState);
        initViewMain(savedInstanceState);

        initDone = true;

        isReady = true;

        if (mBluetoothAdapter != null || !mBluetoothAdapter.isEnabled()) {
            try {
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SingleToast.makeText(getParent(), "To use Bluetooth, turn it on in your device.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (RuntimeException e) {
                Log.d("RTE", "Toast not successful");
            }
        }
    }

    private boolean loadXmlObjects(boolean overwrite) {

        if (mXmlObjects == null || (overwrite && mLoadedUavo != null)) {
            mXmlObjects = new HashMap<String, UAVTalkXMLObject>();

            AssetManager assets = getAssets();

            String file = this.mLoadedUavo + ".zip"; //TODO: Get files from internal storage
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
                    String xml = baos.toString();
                    if (xml.length() > 0) {
                        UAVTalkXMLObject obj = new UAVTalkXMLObject(xml);
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
            mDoReconnect = true;
            return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();

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

        displayView(mCurrentView);

        initWarnDialog().show();

        Log.d("onStart", "onStart");
    }

    @Override
    protected void onStop() {

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
                if (device.getDeviceClass() == UsbConstants.USB_CLASS_MISC) {
                    mUsbManager.requestPermission(device, mPermissionIntent);
                }
            }
        }
    }

    private void connectBluetooth() {
        if (mSerialModeUsed == SERIAL_BLUETOOTH) {
            setBluetoothInterface();
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

                String btname = getString(R.string.EMPTY_STRING);
                if (spnBluetoothPairedDevice.getSelectedItem() != null) {
                    btname = spnBluetoothPairedDevice.getSelectedItem().toString();
                }

                String btmac = getString(R.string.EMPTY_STRING);

                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                // If there are paired devices
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
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

                if (spnUavoSource.getSelectedItem() != null && !spnUavoSource.getSelectedItem().toString().equals(mLoadedUavo)) {
                    mLoadedUavo = spnUavoSource.getSelectedItem().toString();
                    Log.d("UAVSource", mLoadedUavo + "  " + mLoadedUavo);

                    loadXmlObjects(true);
                    SingleToast.makeText(this, "UAVO load completed", Toast.LENGTH_SHORT).show();
                }

                editor.putString(getString(R.string.SETTINGS_BT_MAC), btmac);
                editor.putString(getString(R.string.SETTINGS_BT_NAME), btname);
                editor.putString(getString(R.string.SETTINGS_UAVO_SOURCE), mLoadedUavo);
                editor.putInt(getString(R.string.SETTINGS_SERIAL_MODE), mSerialModeUsed);

                editor.commit();
                mDoReconnect = true;

                break;
            case VIEW_LOGS:

                break;
            case VIEW_ABOUT:

                break;

            case VIEW_PID:
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
                mMapView.onResume();  //(re)activate the Map

                break;
            case VIEW_OBJECTS:
                fragment = new ObjectsFragment();
                setContentView(mView2, position);

                break;
            case VIEW_SETTINGS:
                fragment = new SettingsFragment();
                setContentView(mView3, position);

                if (mSerialModeUsed == SERIAL_NONE) {
                    SingleToast.makeText(this, getString(R.string.PLEASE_SET_A)
                            + getString(R.string.CON_TYPE), Toast.LENGTH_LONG).show();
                } else if (mSerialModeUsed == SERIAL_BLUETOOTH && mBluetoothDeviceUsed == null) {
                    SingleToast.makeText(this, getString(R.string.PLEASE_SET_A)
                            + getString(R.string.BT_DEVICE), Toast.LENGTH_LONG).show();
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

            case VIEW_PID:
                fragment = new PidFragment();
                setContentView(mView6, position);
                //allowPidSliderUpdate();
                SingleToast.makeText(this, R.string.CHECK_PID_WARNING, Toast.LENGTH_SHORT).show();
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

    public void onBatteryCapacityClick(View v) {
        initBatteryCapacityDialog().show();
    }

    public void onAltitudeClick(View V) {
        try {
            mOffset.put(OFFSET_BAROSENSOR_ALTITUDE, mUAVTalkDevice.getObjectTree()
                    .getData("BaroSensor", "Altitude"));
            txtAltitude.setText(R.string.EMPTY_STRING);
        } catch (UAVTalkMissingObjectException | NullPointerException e) {
            Log.i("INFO", "UAVO is missing");
        }
    }

    public void onAltitudeAccelClick(View V) {
        try {
            mOffset.put(OFFSET_VELOCITY_DOWN, mUAVTalkDevice.getObjectTree()
                    .getData("VelocityState", "Down"));
            txtAltitudeAccel.setText(R.string.EMPTY_STRING);
        } catch (UAVTalkMissingObjectException | NullPointerException e) {
            Log.i("INFO", "UAVO is missing");
        }
    }

    public void onBatteryCellsClick(View v) {
        initBatteryCellsDialog().show();
    }

    public void onFusionAlgoClick(View v) {
        String armingState;
        try {
            armingState = mUAVTalkDevice.getObjectTree()
                    .getData("FlightStatus", "Armed").toString();
        } catch (UAVTalkMissingObjectException e) {
            armingState = "";
        }
        if (armingState.equals("Disarmed")) {
            initFusionAlgoDialog().show();
        } else {
            SingleToast.makeText(this, getString(R.string.CHANGE_FUSION_ALGO_DISARMED),
                    Toast.LENGTH_LONG).show();
        }


    }

    public void onLogStartClick(View v) {
        try {
            mUAVTalkDevice.setLogging(true);
        } catch (NullPointerException e) {
            Log.i("INFO", "Device is null");
        }
    }

    public void onLogStopClick(View v) {
        try {
            mUAVTalkDevice.setLogging(false);
        } catch (NullPointerException e) {
            Log.i("INFO", "Device is null");
        }
    }

    public void onPidSaveClick(View v) {
        if (mUAVTalkDevice != null && mUAVTalkDevice.isConnected()) {
            mUAVTalkDevice.savePersistent(mCurrentStabilizationBank);
            SingleToast.makeText(this, getString(R.string.SAVED_PERSISTENT)
                    + getString(R.string.CHECK_PID_WARNING), Toast.LENGTH_SHORT).show();
        } else {
            SingleToast.makeText(this, R.string.SEND_FAILED, Toast.LENGTH_SHORT).show();
        }
    }

    public void onPidUploadClick(View v) {
        if (mUAVTalkDevice != null && mUAVTalkDevice.isConnected()) {
            UAVTalkObjectTree oTree = mUAVTalkDevice.getObjectTree();
            if (oTree != null) {
                oTree.getObjectFromName(mCurrentStabilizationBank).setWriteBlocked(true);

                byte[] buffer0 = H.reverse4bytes(H.floatToByteArray((float) sbrPidRateRollProportional.getProgress() / PID.PID_RATE_ROLL_PROP_DENOM));
                UAVTalkDeviceHelper.updateSettingsObject(oTree, mCurrentStabilizationBank, 0, "RollRatePID", "Kp", buffer0);

                byte[] buffer1 = H.reverse4bytes(H.floatToByteArray((float) sbrPidRatePitchProportional.getProgress() / PID.PID_RATE_PITCH_PROP_DENOM));
                UAVTalkDeviceHelper.updateSettingsObject(oTree, mCurrentStabilizationBank, 0, "PitchRatePID", "Kp", buffer1);

                byte[] buffer2 = H.reverse4bytes(H.floatToByteArray((float) sbrPidRateRollIntegral.getProgress() / PID.PID_RATE_ROLL_INTE_DENOM));
                UAVTalkDeviceHelper.updateSettingsObject(oTree, mCurrentStabilizationBank, 0, "RollRatePID", "Ki", buffer2);

                byte[] buffer3 = H.reverse4bytes(H.floatToByteArray((float) sbrPidRatePitchIntegral.getProgress() / PID.PID_RATE_PITCH_INTE_DENOM));
                UAVTalkDeviceHelper.updateSettingsObject(oTree, mCurrentStabilizationBank, 0, "PitchRatePID", "Ki", buffer3);

                byte[] buffer4 = H.reverse4bytes(H.floatToByteArray((float) sbrPidRollProportional.getProgress() / PID.PID_ROLL_PROP_DENOM));
                UAVTalkDeviceHelper.updateSettingsObject(oTree, mCurrentStabilizationBank, 0, "RollPI", "Kp", buffer4);

                byte[] buffer5 = H.reverse4bytes(H.floatToByteArray((float) sbrPidPitchProportional.getProgress() / PID.PID_PITCH_PROP_DENOM));
                UAVTalkDeviceHelper.updateSettingsObject(oTree, mCurrentStabilizationBank, 0, "PitchPI", "Kp", buffer5);

                byte[] buffer6 = H.reverse4bytes(H.floatToByteArray((float) sbrPidRateRollDerivative.getProgress() / PID.PID_RATE_ROLL_DERI_DENOM));
                UAVTalkDeviceHelper.updateSettingsObject(oTree, mCurrentStabilizationBank, 0, "RollRatePID", "Kd", buffer6);

                byte[] buffer7 = H.reverse4bytes(H.floatToByteArray((float) sbrPidRatePitchDerivative.getProgress() / PID.PID_RATE_PITCH_DERI_DENOM));
                UAVTalkDeviceHelper.updateSettingsObject(oTree, mCurrentStabilizationBank, 0, "PitchRatePID", "Kd", buffer7);

                mUAVTalkDevice.sendSettingsObject(mCurrentStabilizationBank, 0);

                SingleToast.makeText(this, getString(R.string.PID_SENT)
                        + getString(R.string.CHECK_PID_WARNING), Toast.LENGTH_SHORT).show();

                oTree.getObjectFromName(mCurrentStabilizationBank).setWriteBlocked(false);
            }
        } else {
            SingleToast.makeText(this, R.string.SEND_FAILED, Toast.LENGTH_SHORT).show();
        }
    }

    public void onPidDownloadClick(View v) {
        if (mUAVTalkDevice != null && mUAVTalkDevice.isConnected()) {
            allowPidSliderUpdate();
            SingleToast.makeText(this, getString(R.string.PID_LOADING)
                    + getString(R.string.CHECK_PID_WARNING), Toast.LENGTH_SHORT).show();
        } else {
            SingleToast.makeText(this, R.string.SEND_FAILED, Toast.LENGTH_SHORT).show();
        }
    }

    private void allowPidSliderUpdate() {
        sbrPidRateRollProportional.setAllowUpdateFromFC(true);
        sbrPidRatePitchProportional.setAllowUpdateFromFC(true);
        sbrPidRateRollIntegral.setAllowUpdateFromFC(true);
        sbrPidRatePitchIntegral.setAllowUpdateFromFC(true);
        sbrPidRollProportional.setAllowUpdateFromFC(true);
        sbrPidPitchProportional.setAllowUpdateFromFC(true);
        sbrPidRateRollDerivative.setAllowUpdateFromFC(true);
        sbrPidRatePitchDerivative.setAllowUpdateFromFC(true);
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
        Uri contentUri =
                FileProvider.getUriForFile(this, "net.proest.lp2go3.logfileprovider", logFile);

        share.putExtra(Intent.EXTRA_STREAM, contentUri);
        startActivity(Intent.createChooser(share, getString(R.string.SHARE_LOG_TITLE)));
    }

    public void setPollThreadObjectTree(UAVTalkObjectTree oTree) {
        mPollThread.setObjectTree(oTree);
    }

    private boolean setBluetoothInterface() {
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
                    mUAVTalkDevice = new UAVTalkUsbDevice(this, mDeviceConnection, intf,
                            mXmlObjects);
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

    private AlertDialog.Builder initBatteryCapacityDialog() {
        AlertDialog.Builder batteryCapacityDialogBuilder = new AlertDialog.Builder(this);
        batteryCapacityDialogBuilder.setTitle(R.string.CAPACITY_DIALOG_TITLE);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        batteryCapacityDialogBuilder.setView(input);
        input.setText(txtCapacity.getText());

        batteryCapacityDialogBuilder.setPositiveButton(R.string.OK_BUTTON,
                new DialogInterface.OnClickListener() {
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
                    mUAVTalkDevice.sendSettingsObject("FlightBatterySettings", 0, "Capacity", 0,
                            bcdata);
                    //mUAVTalkDevice.savePersistent("FlightBatterySettings");
                }
                dialog.dismiss();
                dialog.cancel();
            }
        });

        batteryCapacityDialogBuilder.setNegativeButton(R.string.CANCEL_BUTTON,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                dialog.dismiss();
            }
        });
        return batteryCapacityDialogBuilder;
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

    private AlertDialog.Builder initBatteryCellsDialog() {
        AlertDialog.Builder batteryCellsDialogBuilder = new AlertDialog.Builder(this);
        batteryCellsDialogBuilder.setTitle(R.string.CELLS_DIALOG_TITLE);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        batteryCellsDialogBuilder.setView(input);
        input.setText(txtCells.getText());

        batteryCellsDialogBuilder.setPositiveButton(R.string.OK_BUTTON, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                byte[] bcdata = new byte[1];
                try {
                    bcdata[0] = H.toBytes(Integer.parseInt(input.getText().toString()))[3]; //want the lsb
                } catch (NumberFormatException e) {
                    bcdata[0] = 0x00;
                }
                if (mUAVTalkDevice != null && bcdata.length == 1) {
                    mUAVTalkDevice.sendSettingsObject("FlightBatterySettings", 0, "NbCells", 0,
                            bcdata);
                    //mUAVTalkDevice.savePersistent("FlightBatterySettings");
                }
                dialog.dismiss();
                dialog.cancel();
            }
        });

        batteryCellsDialogBuilder.setNegativeButton(R.string.CANCEL_BUTTON,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                dialog.dismiss();
            }
        });
        return batteryCellsDialogBuilder;
    }

    public AlertDialog.Builder initFusionAlgoDialog() {
        AlertDialog.Builder fusionAlgoDialogBuilder = new AlertDialog.Builder(this);
        fusionAlgoDialogBuilder.setTitle("Select Fusion Algorithm");

        String[] types;
        try {
            types = mXmlObjects.get("RevoSettings").getFields().get("FusionAlgorithm").getOptions();
        } catch (NullPointerException e) {
            types = null;
        }
        fusionAlgoDialogBuilder.setItems(types, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                byte[] send = new byte[1];
                send[0] = (byte) which;
                if (mUAVTalkDevice != null) {
                    mUAVTalkDevice.sendSettingsObject("RevoSettings", 0, "FusionAlgorithm", 0, send);
                    //mUAVTalkDevice.savePersistent("RevoSettings");
                }
            }
        });
        return fusionAlgoDialogBuilder;
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

    private void resetMainView() {
        txtDeviceText.setText(R.string.EMPTY_STRING);
        txtAtti.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));
        txtStab.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));
        txtPath.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));
        txtPlan.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));

        txtGPSSatsInView.setText(R.string.EMPTY_STRING);
        txtGPS.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));
        txtSensor.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));
        txtAirspd.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));
        txtMag.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));

        txtInput.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));
        txtOutput.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));
        txtI2C.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));
        txtTelemetry.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));

        txtFlightTelemetry.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));
        txtGCSTelemetry.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));

        txtBatt.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));
        txtTime.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));
        txtConfig.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));

        txtBoot.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));
        txtMem.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));
        txtStack.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));
        txtEvent.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));
        txtCPU.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));

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
            this.setName("LP2GoPollThread");
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
            if (color == null || color.equals(getString(R.string.EMPTY_STRING))) {
                return;
            }
            switch (color) {
                case "OK":
                case "None":
                case "Connected":
                    t.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_ok));
                    break;
                case "Warning":
                case "HandshakeReq":
                case "HandshakeAck":
                    t.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_warning));
                    break;
                case "Error":
                    t.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_error));
                    break;
                case "Critical":
                case "RebootRequired":
                case "Disconnected":
                    t.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_critical));
                    break;
                case "Uninitialised":
                    t.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_uninitialised));
                    break;
                case "InProgress":
                    t.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_inprogress));
                    break;
                case "Completed":
                    t.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corner_completed));
                    break;
            }
        }

        public void setInvalid() {
            mIsValid = false;
        }

        public void run() {
            while (mIsValid) {
                blink = !blink;
                try {
                    Thread.sleep(POLL_WAIT_TIME);
                } catch (InterruptedException ignored) {
                }

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mSerialModeUsed == SERIAL_BLUETOOTH) {
                            imgUSB.setColorFilter(Color.argb(0xff, 0x00, 0x00, 0x00));
                            if (mUAVTalkDevice != null && mUAVTalkDevice.isConnected()) {
                                imgBluetooth.setColorFilter(Color.argb(0xff, 0x00, 0x80, 0x00));
                                imgBluetooth.setImageDrawable(
                                        ContextCompat.getDrawable(getApplicationContext(),
                                                R.drawable.ic_bluetooth_connected_128dp));

                            } else if (mUAVTalkDevice != null && mUAVTalkDevice.isConnecting()) {
                                if (blink) {
                                    imgBluetooth.setColorFilter(Color.argb(0xff, 0xff, 0x66, 0x00));
                                    imgBluetooth.setImageDrawable(
                                            ContextCompat.getDrawable(getApplicationContext(),
                                                    R.drawable.ic_bluetooth_128dp));
                                } else {
                                    imgBluetooth.setColorFilter(Color.argb(0xff, 0xff, 0x66, 0x00));
                                    imgBluetooth.setImageDrawable(
                                            ContextCompat.getDrawable(getApplicationContext(),
                                                    R.drawable.ic_bluetooth_connected_128dp));
                                }
                            } else {
                                imgBluetooth.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
                                imgBluetooth.setImageDrawable(
                                        ContextCompat.getDrawable(getApplicationContext(),
                                                R.drawable.ic_bluetooth_disabled_128dp));
                            }
                        } else if (mSerialModeUsed == SERIAL_USB) {
                            imgBluetooth.setColorFilter(Color.argb(0xff, 0x00, 0x00, 0x00));
                            if (mUAVTalkDevice != null && mUAVTalkDevice.isConnected()) {
                                imgUSB.setColorFilter(Color.argb(0xff, 0x00, 0x80, 0x00));

                            } else if (mUAVTalkDevice != null && mUAVTalkDevice.isConnecting()) {
                                if (blink) {
                                    imgUSB.setColorFilter(Color.argb(0xff, 0xff, 0x66, 0x00));
                                } else {
                                    imgUSB.setColorFilter(Color.argb(0xff, 0xff, 0x88, 0x00));
                                }
                            } else {
                                imgUSB.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
                            }
                        }
                    }
                });

                if (this.mObjectTree == null || mUAVTalkDevice == null
                        || !mUAVTalkDevice.isConnected()) {
                    continue;  //nothing yet to show, or not connected
                }

                requestObjects();

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            switch (mCurrentView) {
                                case VIEW_MAIN:

                                    txtObjectLogTx.setText(
                                            H.k(String.valueOf(mTxObjects * POLL_SECOND_FACTOR)));
                                    txtObjectLogRxGood.setText(
                                            H.k(String.valueOf(mRxObjectsGood * POLL_SECOND_FACTOR)));
                                    txtObjectLogRxBad.setText(
                                            H.k(String.valueOf(mRxObjectsBad * POLL_SECOND_FACTOR)));

                                    if (blink) {
                                        if (mRxObjectsGood > 0)
                                            imgPacketsGood.setColorFilter(
                                                    Color.argb(0xff, 0x00, 0x88, 0x00));
                                        if (mRxObjectsBad > 0)
                                            imgPacketsBad.setColorFilter(
                                                    Color.argb(0xff, 0x88, 0x00, 0x00));
                                        if (mTxObjects > 0)
                                            imgPacketsUp.setColorFilter(
                                                    Color.argb(0xff, 0x00, 0x00, 0x88));
                                    } else {
                                        if (mRxObjectsGood > 0)
                                            imgPacketsGood.setColorFilter(
                                                    Color.argb(0xff, 0x00, 0x00, 0x00));
                                        if (mRxObjectsBad > 0)
                                            imgPacketsBad.setColorFilter(
                                                    Color.argb(0xff, 0x00, 0x00, 0x00));
                                        if (mTxObjects > 0)
                                            imgPacketsUp.setColorFilter(
                                                    Color.argb(0xff, 0x00, 0x00, 0x00));
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

                                    setTextBGColor(mActivity.txtFlightTelemetry, getData("FlightTelemetryStats", "Status").toString());
                                    setTextBGColor(mActivity.txtGCSTelemetry, getData("GCSTelemetryStats", "Status").toString());
                                    setText(mActivity.txtFusionAlgorithm, getData("RevoSettings", "FusionAlgorithm").toString());

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
                                    } catch (Exception ignored) {
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
                                        );

                                        mCurrentPosMarker++;
                                        if (mCurrentPosMarker >= HISTORY_MARKER_NUM) {
                                            mCurrentPosMarker = 0;
                                        }
                                        if (mPosHistory[mCurrentPosMarker] != null) {
                                            mPosHistory[mCurrentPosMarker].remove();
                                        }
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
                                case VIEW_PID:

                                    String fmode = getData("ManualControlCommand", "FlightModeSwitchPosition").toString();
                                    String bank = getData("StabilizationSettings", "FlightModeMap", fmode).toString();

                                    mCurrentStabilizationBank = "StabilizationSettings" + bank;


                                    switch (mCurrentStabilizationBank) {
                                        case "StabilizationSettingsBank1":
                                            imgPidBank.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_filter_1_128dp));
                                            break;
                                        case "StabilizationSettingsBank2":
                                            imgPidBank.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_filter_2_128dp));
                                            break;
                                        case "StabilizationSettingsBank3":
                                            imgPidBank.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_filter_3_128dp));
                                            break;
                                        default:
                                            imgPidBank.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_filter_none_128dp));
                                            break;
                                    }

                                    sbrPidRateRollProportional.setProgress(toFloat(getData(mCurrentStabilizationBank, "RollRatePID", "Kp")));
                                    sbrPidRatePitchProportional.setProgress(toFloat(getData(mCurrentStabilizationBank, "PitchRatePID", "Kp")));

                                    sbrPidRateRollIntegral.setProgress(toFloat(getData(mCurrentStabilizationBank, "RollRatePID", "Ki")));
                                    sbrPidRatePitchIntegral.setProgress(toFloat(getData(mCurrentStabilizationBank, "PitchRatePID", "Ki")));

                                    sbrPidRollProportional.setProgress(toFloat(getData(mCurrentStabilizationBank, "RollPI", "Kp")));
                                    sbrPidPitchProportional.setProgress(toFloat(getData(mCurrentStabilizationBank, "PitchPI", "Kp")));

                                    sbrPidRateRollDerivative.setProgress(toFloat(getData(mCurrentStabilizationBank, "RollRatePID", "Kd")));
                                    sbrPidRatePitchDerivative.setProgress(toFloat(getData(mCurrentStabilizationBank, "PitchRatePID", "Kd")));

                                    break;
                                case VIEW_ABOUT:

                                    break;

                            }
                        } catch (NullPointerException e) {
                            Log.d("NPE", "Nullpointer Exception in Pollthread, most likely switched Connections");

                        }
                    }
                });
            }
        }

        private Float toFloat(Object o) {
            try {
                return (Float) o;
            } catch (ClassCastException e) {
                return .0f;
            }
        }

        private void requestObjects() {
            try {
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
                    mUAVTalkDevice.requestObject("StabilizationSettings");
                    mUAVTalkDevice.requestObject("StabilizationSettingsBank1");
                    mUAVTalkDevice.requestObject("StabilizationSettingsBank2");
                    mUAVTalkDevice.requestObject("StabilizationSettingsBank3");

                }
            } catch (NullPointerException e) {
                Log.e("ERR", "UAVTalkdevice is null. Reconnecting?");
            }
        }

        private String getVehicleNameData() {
            char[] b = new char[20];
            try {
                for (int i = 0; i < 20; i++) {
                    String str = mObjectTree.getData("SystemSettings", 0, "VehicleName", i)
                            .toString();
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
                Log.e("ERR", "Object Tree not loaded yet.");
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
            this.setName("LP2GoConnectionThread");
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

            boolean loaded = false;
            if (mLoadedUavo != null) {
                loaded = loadXmlObjects(false);
            }
            if (loaded) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final long submil = (System.currentTimeMillis() - millis);
                        SingleToast.makeText(mActivity,
                                "UAVO load completed in " + submil + " milliseconds",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            }

            while (mIsValid) {

                if (mUAVTalkDevice != null && mUAVTalkDevice.isConnected()) {
                    try {
                        String status = (String) mUAVTalkDevice.getObjectTree()
                                .getData("FlightTelemetryStats", "Status");
                        String[] options = mUAVTalkDevice.getObjectTree().getXmlObjects()
                                .get("FlightTelemetryStats").getFields().get("Status").getOptions();
                        byte b = 0;
                        for (String o : options) {
                            if (o.equals(status)) {
                                break;
                            }
                            b++;
                        }
                        mUAVTalkDevice.handleHandshake(b);
                    } catch (UAVTalkMissingObjectException e) {
                        e.printStackTrace();
                    }

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

                if (mUAVTalkDevice == null
                        || (!mUAVTalkDevice.isConnected() && !mUAVTalkDevice.isConnecting())) {
                    switch (mSerialModeUsed) {
                        case SERIAL_BLUETOOTH:
                            connectBluetooth();
                            break;
                        case SERIAL_USB:
                            connectUSB();
                            break;
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
