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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

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
import net.proest.lp2go3.slider.AboutFragment;
import net.proest.lp2go3.slider.LogsFragment;
import net.proest.lp2go3.slider.MainFragment;
import net.proest.lp2go3.slider.MapFragment;
import net.proest.lp2go3.slider.ObjectsFragment;
import net.proest.lp2go3.slider.SettingsFragment;
import net.proest.lp2go3.slider.adapter.NavDrawerListAdapter;
import net.proest.lp2go3.slider.model.NavDrawerItem;

import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final int ICON_OPAQUE = 255;
    private static final int ICON_TRANSPARENT = 64;

    private final static int SERIAL_NONE = 0;
    private final static int SERIAL_USB = 1;
    private final static int SERIAL_BLUETOOTH = 2;

    private final static int REQUEST_ENABLE_BT = 1000;

    private final static int SERIAL_CONNECTED = 2;
    private final static int SERIAL_CONNECTING = 1;
    private final static int SERIAL_DISCONNECTED = 0;
    private static final String ACTION_USB_PERMISSION = "net.proest.lp2go.USB_PERMISSION";
    private static final String OFFSET_VELOCITY_DOWN = "VelocityState-Down";
    private static final String OFFSET_BAROSENSOR_ALTITUDE = "BaroSensor-Altitude";
    private static final int VIEW_MAIN = 0;
    private static final int VIEW_MAP = 1;
    private static final int VIEW_OBJECTS = 2;
    private static final int VIEW_SETTINGS = 3;
    private static final int VIEW_LOGS = 4;
    private static final int VIEW_ABOUT = 5;
    private static final String EMPTY_STRING = "";
    static boolean hasPThread = false;
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
    protected TextView etxObjects;
    protected TextView txtLogFilenameLabel;
    protected TextView txtLogFilename;
    protected TextView txtLogSize;
    protected TextView txtLogSizeLabel;
    protected TextView txtLogObjects;
    protected TextView txtLogObjectsLabel;
    protected TextView txtLogDuration;
    protected TextView txtLogDurationLabel;
    //protected EditText etxSettingsBTMac;
    protected Spinner spnConnectionTypeSpinner;
    protected Spinner spnBluetoothPairedDevice;
    protected Button btnStart;
    int currentView = 0;
    int HISTORY_MARKER_NUM = 5;
    int currentPosMarker = 0;
    private BluetoothAdapter mBluetoothAdapter;
    private long txObjects;
    private long rxObjectsGood;
    private long rxObjectsBad;

    private int serialConnectionState;
    private int serialModeUsed;
    private String bluetoothDeviceUsed;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    // nav drawer title
    private CharSequence mDrawerTitle;
    // used to store app title
    private CharSequence mTitle;
    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;
    private Hashtable<String, Object> offset;
    private PollThread pThread;
    private ConnectionThread cThread;
    private TextView txtDeviceText;
    private UsbManager mManager;
    private UsbDevice mDevice;
    private UsbDeviceConnection mDeviceConnection;
    private PendingIntent mPermissionIntent;
    private UsbInterface mInterface;
    private UAVTalkDevice mUAVTalkDevice;
    private Hashtable<String, UAVTalkXMLObject> xmlObjects;
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("USB", action);

            if (serialModeUsed == SERIAL_USB && UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                Log.d("USB", device.getVendorId() + "-" + device.getProductId() + "-" + device.getDeviceClass()
                        + " " + device.getDeviceSubclass() + " " + device.getDeviceProtocol());

                if (device.getDeviceClass() == UsbConstants.USB_CLASS_MISC) {
                    mManager.requestPermission(device, mPermissionIntent);
                }

                txtDeviceText.setText(device.getDeviceName());


            } else if (serialModeUsed == SERIAL_USB && UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null /*&& device.equals(deviceName)*/) {
                    setUsbInterface(null, null);
                    if (mUAVTalkDevice != null) {
                        mUAVTalkDevice.stop();
                    }
                }
                txtDeviceText.setText(R.string.DEVICE_NAME_NONE);
            } else if (serialModeUsed == SERIAL_USB && ACTION_USB_PERMISSION.equals(action)) {
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
    private Marker[] posHistory = new Marker[HISTORY_MARKER_NUM];
    private AlertDialog.Builder batteryCapacityDialogBuilder;
    private AlertDialog.Builder batteryCellsDialogBuilder;
    private View view0, view1, view2, view3, view4, view5;
    private GoogleMap map;
    private MapView mapView;
    private boolean doReconnect = false;

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

    public void setRxObjectsGood(long o) {
        this.rxObjectsGood = o;
    }

    public void incRxObjectsGood() {
        this.rxObjectsGood++;
    }

    public void setRxObjectsBad(long o) {
        this.rxObjectsBad = o;
    }

    public void incRxObjectsBad() {
        this.rxObjectsBad++;
    }

    public void setTxObjects(long o) {
        this.txObjects = o;
    }

    public void incTxObjects() {
        this.txObjects++;
    }

    private void initSlider(Bundle savedInstanceState) {
        mTitle = mDrawerTitle = getTitle();
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        navDrawerItems = new ArrayList<NavDrawerItem>();

        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));

        navMenuIcons.recycle();
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);

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
    }

    /*
        public void setObjectLogTx(String o) {
            txtObjectLogTx.setText(o);
        }
        public void setObjectLogRxGood(String o) {
            txtObjectLogRxGood.setText(o);
        }
        public void setObjectLogRxBad(String o) {
            txtObjectLogRxBad.setText(o);
        }
    */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        serialConnectionState = SERIAL_DISCONNECTED;

        view0 = getLayoutInflater().inflate(R.layout.activity_main, null);
        view1 = getLayoutInflater().inflate(R.layout.activity_map, null);
        view2 = getLayoutInflater().inflate(R.layout.activity_objects, null);
        view3 = getLayoutInflater().inflate(R.layout.activity_settings, null);
        view4 = getLayoutInflater().inflate(R.layout.activity_logs, null);
        view5 = getLayoutInflater().inflate(R.layout.activity_about, null);

        setContentView(view1);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        map = mapView.getMap();
        if (map != null) {  //Map can be null if services are not available, e.g. on an amazon fire tab
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.setMyLocationEnabled(true);
            MapsInitializer.initialize(this);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(32.154599, -110.827369), 18);
            map.animateCamera(cameraUpdate);
            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            Marker center = map.addMarker(new MarkerOptions()
                    .position(new LatLng(32.154599, -110.827369))
                    .title("Librepilot")
                    .snippet("LP rules")
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
        }
        setContentView(view0);  //Main

        AssetManager assets = getAssets();

        initSlider(savedInstanceState);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        Log.d("SMU", "" + serialModeUsed);
        offset = new Hashtable<String, Object>();
        offset.put(OFFSET_BAROSENSOR_ALTITUDE, .0f);
        offset.put(OFFSET_VELOCITY_DOWN, .0f);

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

        serialModeUsed = sharedPref.getInt(getString(R.string.SETTINGS_SERIAL_MODE), 0);
        bluetoothDeviceUsed = sharedPref.getString(getString(R.string.SETTINGS_BT_NAME), "");

        imgBluetooth = (ImageView) findViewById(R.id.imgBluetooth);
        if (serialModeUsed != SERIAL_BLUETOOTH || serialModeUsed == SERIAL_NONE) {
            //imgBluetooth.setVisibility(View.INVISIBLE);
            imgBluetooth.setAlpha(ICON_TRANSPARENT);
        } else {

            imgBluetooth.setColorFilter(Color.argb(255, 255, 0, 0));
        }

        imgUSB = (ImageView) findViewById(R.id.imgUSB);
        if (serialModeUsed != SERIAL_USB || serialModeUsed == SERIAL_NONE) {
            imgUSB.setAlpha(ICON_TRANSPARENT);
        } else {
            imgUSB.setColorFilter(Color.argb(255, 255, 0, 0));
        }

        setContentView(view1); //Map
        txtLatitude = (TextView) findViewById(R.id.txtLatitude);
        txtLongitude = (TextView) findViewById(R.id.txtLongitude);
        txtMapGPS = (TextView) findViewById(R.id.txtMapGPS);
        txtMapGPSSatsInView = (TextView) findViewById(R.id.txtMapGPSSatsInView);

        setContentView(view2); //Objects
        etxObjects = (EditText) findViewById(R.id.etxObjects);

        setContentView(view3); //Settings
        spnConnectionTypeSpinner = (Spinner) findViewById(R.id.spnConnectionTypeSpinner);
        ArrayAdapter<CharSequence> serialConnectionTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.connections_settings, android.R.layout.simple_spinner_item);

        serialConnectionTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnConnectionTypeSpinner.setAdapter(serialConnectionTypeAdapter);
        spnConnectionTypeSpinner.setOnItemSelectedListener(this);
        spnConnectionTypeSpinner.setSelection(serialModeUsed);


        spnBluetoothPairedDevice = (Spinner) findViewById(R.id.spnBluetoothPairedDevice);
        //ArrayAdapter<CharSequence> bntPairedDeviceAdapter = ArrayAdapter.createFromResource(this,
        //        R.array.connections_settings, android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> bntPairedDeviceAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);

        bntPairedDeviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnBluetoothPairedDevice.setAdapter(bntPairedDeviceAdapter);
        spnBluetoothPairedDevice.setOnItemSelectedListener(this);
        //spnBluetoothPairedDevice.setSelection(bluetoothDeviceUsed);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            int btpd = 0;
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                bntPairedDeviceAdapter.add(device.getName());
                if (device.getName().equals(bluetoothDeviceUsed)) {
                    spnBluetoothPairedDevice.setSelection(btpd);
                }
                btpd++;
                Log.d("BTE", device.getName() + " " + device.getAddress());
            }
        }

        setContentView(view4); //Logs

        txtLogFilename = (TextView) findViewById(R.id.txtLogFilename);
        txtLogSize = (TextView) findViewById(R.id.txtLogSize);
        txtLogObjects = (TextView) findViewById(R.id.txtLogObjects);
        txtLogDuration = (TextView) findViewById(R.id.txtLogDuration);


        setContentView(view5);  //About
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        {

            ((TextView) findViewById(R.id.txtAndroidVersionRelease))
                    .setText(getString(R.string.RUNNING_ON_ANDROID_VERSION) + Build.VERSION.RELEASE);
            ((TextView) findViewById(R.id.txtLP2GoVersionRelease))
                    .setText(getString(R.string.LP2GO_RELEASE) + pInfo.versionName + getString(R.string.OPEN_ROUND_BRACKET_WITH_SPACE) + pInfo.versionName + getString(R.string.CLOSE_ROUND_BRACKET));
            ((TextView) findViewById(R.id.txtLP2GoPackage)).setText(pInfo.packageName);
        }
        setContentView(view0);


        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        xmlObjects = new Hashtable<String, UAVTalkXMLObject>();

        try {
            String path = "uav-15.09";
            String files[] = assets.list(path);
            for (String file : files) {
                InputStream ius = assets.open(path + File.separator + file);
                UAVTalkXMLObject obj = new UAVTalkXMLObject(readFully(ius));
                xmlObjects.put(obj.getName(), obj);
                ius.close();
                ius = null;
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        Log.d("MainActivity.onCreate", "XML Loading Complete");

        // check for existing devices


        // listen for new devices
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);

        registerReceiver(mUsbReceiver, filter);

        if (Looper.myLooper() == null) Looper.prepare();

        pThread = new PollThread(this);
        cThread = new ConnectionThread(this);
        pThread.start();
        cThread.start();


        isReady = true;
    }

    private void connectUSB() {
        if (serialModeUsed == SERIAL_USB) {
            for (UsbDevice device : mManager.getDeviceList().values()) {
                UsbInterface intf = findAdbInterface(device);
                if (device.getDeviceClass() == UsbConstants.USB_CLASS_MISC) {
                    mManager.requestPermission(device, mPermissionIntent);
                }
            }
        }
    }

    private void connectBluettooth() {
        if (serialModeUsed == SERIAL_BLUETOOTH) {
            setBluetoothInterface();
        }
    }

    public void setContentView(View v, int p) {
        if (currentView != p) {
            currentView = p;
            super.setContentView(v);
            initSlider(null);
        }
    }

    private void displayView(int position) {
        Fragment fragment = null;


        //clean up current view
        switch (currentView) {
            case VIEW_MAIN:

                break;
            case VIEW_MAP:
                mapView.onPause();

                break;
            case VIEW_OBJECTS:

                break;
            case VIEW_SETTINGS:
                SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                String btname = spnBluetoothPairedDevice.getSelectedItem().toString();
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
                doReconnect = true;

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
                fragment = new MainFragment();
                setContentView(view0, position);

                break;
            case VIEW_MAP:
                fragment = new MapFragment();
                setContentView(view1, position);
                mapView.onResume();  //(re)activate the map

                break;
            case VIEW_OBJECTS:
                fragment = new ObjectsFragment();
                setContentView(view2, position);

                break;
            case VIEW_SETTINGS:
                fragment = new SettingsFragment();
                setContentView(view3, position);

                break;
            case VIEW_LOGS:
                fragment = new LogsFragment();
                setContentView(view4, position);

                break;
            case VIEW_ABOUT:
                fragment = new AboutFragment();
                setContentView(view5, position);

                break;

            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(navMenuTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
        }
    }

    private String readFully(InputStream inputStream)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toString();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mUsbReceiver);
        setUsbInterface(null, null);
        super.onDestroy();
    }

    public void onBatteryCapacityClick(View v) {
        initBatteryCapacityDialog();
        batteryCapacityDialogBuilder.show();
    }

    public void onAltitudeClick(View V) {
        try {
            offset.put(OFFSET_BAROSENSOR_ALTITUDE, mUAVTalkDevice.getoTree().getData("BaroSensor", "Altitude"));
            txtAltitude.setText(EMPTY_STRING);
        } catch (UAVTalkMissingObjectException | NullPointerException e) {
            //e.printStackTrace();
        }
    }

    public void onAltitudeAccelClick(View V) {
        try {
            offset.put(OFFSET_VELOCITY_DOWN, mUAVTalkDevice.getoTree().getData("VelocityState", "Down"));
            txtAltitudeAccel.setText(EMPTY_STRING);
        } catch (UAVTalkMissingObjectException e) {
            e.printStackTrace();
        }
    }

    public void onBatteryCellsClick(View v) {
        initBatteryCellsDialog();
        batteryCellsDialogBuilder.show();
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
        //share.setType("txt/plain");
        share.setType("application/octet-stream");

        File logPath = new File(this.getFilesDir(), "");
        File logFile = new File(logPath, mUAVTalkDevice.getLogFileName());
        Uri contentUri = FileProvider.getUriForFile(this, "net.proest.lp2go.logfileprovider", logFile);

        share.putExtra(Intent.EXTRA_STREAM, contentUri);
        Log.d("contentur", contentUri.toString());

        startActivity(Intent.createChooser(share, "Share Log"));
    }

    public void setPThreadOTree(UAVTalkObjectTree oTree) {
        pThread.setoTree(oTree);
    }

    private boolean setBluetoothInterface() {
        /*try {
            mUAVTalkDevice.stop();
        } catch (Exception e) {
            Log.d("DBG", "Could'nt stop device");
        }
        mUAVTalkDevice = null;*/
        if (mUAVTalkDevice != null) {
            mUAVTalkDevice.stop();
        }
        mUAVTalkDevice = new UAVTalkBluetoothDevice(this, xmlObjects);
        mUAVTalkDevice.start();
        //}
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
            UsbDeviceConnection connection = mManager.openDevice(device);
            if (connection != null) {
                if (connection.claimInterface(intf, true)) {
                    mDevice = device;
                    mDeviceConnection = connection;
                    mInterface = intf;
                    mUAVTalkDevice = new UAVTalkUsbDevice(this, mDeviceConnection, intf, xmlObjects);
                    mUAVTalkDevice.getoTree().setXmlObjects(xmlObjects);

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
        batteryCapacityDialogBuilder = new AlertDialog.Builder(this);
        batteryCapacityDialogBuilder.setTitle(R.string.CAPACITY_DIALOG_TITLE);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        batteryCapacityDialogBuilder.setView(input);
        input.setText(txtCapacity.getText());

        batteryCapacityDialogBuilder.setPositiveButton(R.string.OK_BUTTON, new DialogInterface.OnClickListener() {
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

        batteryCapacityDialogBuilder.setNegativeButton(R.string.CANCEL_BUTTON, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                dialog.dismiss();
            }
        });
    }

    private void initBatteryCellsDialog() {
        batteryCellsDialogBuilder = new AlertDialog.Builder(this);
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
                    mUAVTalkDevice.sendSettingsObject("FlightBatterySettings", 0, "NbCells", 0, bcdata);
                }
                dialog.dismiss();
                dialog.cancel();
            }
        });

        batteryCellsDialogBuilder.setNegativeButton(R.string.CANCEL_BUTTON, new DialogInterface.OnClickListener() {
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
                serialModeUsed = pos;
                switch (serialModeUsed) {
                    case SERIAL_NONE:
                        //imgBluetooth.setVisibility(View.INVISIBLE);
                        //imgUSB.setVisibility(View.INVISIBLE);
                        imgBluetooth.setAlpha(ICON_TRANSPARENT);  //FIXME: reset color as well
                        imgUSB.setAlpha(ICON_TRANSPARENT);//FIXME: preset color as well
                        break;
                    case SERIAL_USB:
                        //imgBluetooth.setVisibility(View.INVISIBLE);
                        //imgUSB.setVisibility(View.VISIBLE);
                        imgBluetooth.setAlpha(ICON_TRANSPARENT);//FIXME: reset color as well
                        imgUSB.setAlpha(ICON_OPAQUE);//FIXME: preset color as well
                        break;
                    case SERIAL_BLUETOOTH:
                        //imgBluetooth.setVisibility(View.VISIBLE);
                        //imgUSB.setVisibility(View.INVISIBLE);
                        imgBluetooth.setAlpha(ICON_OPAQUE);//FIXME: preset color as well
                        imgUSB.setAlpha(ICON_TRANSPARENT);//FIXME: reset color as well
                        break;
                }
                SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(getString(R.string.SETTINGS_SERIAL_MODE), serialModeUsed);
                editor.commit();
                //Log.d("SET",parent.getItemAtPosition(pos).toString());
                break;
            }
        }
        //Log.d("SET", "" + (parent.getId() == R.id.spnConnectionTypeSpinner) + " " + pos + " " + id);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void resetMainView() {

        txtDeviceText.setText("");
        txtAtti.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtStab.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtPath.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtPlan.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));

        txtGPSSatsInView.setText("");
        txtGPS.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtSensor.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtAirspd.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtMag.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));

        txtInput.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtOutput.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtI2C.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtTelemetry.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));

        txtFlightTelemetry.setText("");
        txtGCSTelemetry.setText("");

        txtBatt.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtTime.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtConfig.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));

        txtBoot.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtMem.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtStack.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtEvent.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));
        txtCPU.setBackground(getResources().getDrawable(R.drawable.rounded_corner_uninitialised));

        txtArmed.setText("");

        txtVolt.setText("");
        txtAmpere.setText("");
        txtmAh.setText("");
        txtTimeLeft.setText("");

        txtCapacity.setText("");
        txtCells.setText("");

        txtAltitude.setText("");
        txtAltitudeAccel.setText("");

        txtModeNum.setText("");
        txtModeFlightMode.setText("");
        txtModeAssistedControl.setText("");
    }

    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            displayView(position);
        }
    }

    private class PollThread extends Thread {

        MainActivity mActivity;
        UAVTalkObjectTree oTree;
        boolean blink = true;

        public PollThread(MainActivity mActivity) {
            if (hasPThread) throw new IllegalStateException("double pThread");
            hasPThread = true;
            this.mActivity = mActivity;
        }

        public void setoTree(UAVTalkObjectTree oTree) {
            this.oTree = oTree;
        }

        private void setText(TextView t, String text) {
            if (text != null) {
                t.setText(text);
            }
        }

        private void setTextBGColor(TextView t, String color) {
            if (color == null || color == EMPTY_STRING) {
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

        public void run() {

            //while (isValid) {
            while (true) {
                blink = !blink;
                //Log.d("PING","PONG");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (serialModeUsed == SERIAL_BLUETOOTH) {
                            imgUSB.setColorFilter(Color.argb(0xff, 0x00, 0x00, 0x00));
                            if (mUAVTalkDevice != null && mUAVTalkDevice.isConnected()) {
                                imgBluetooth.setColorFilter(Color.argb(0xff, 0x00, 0x80, 0x00));
                                imgBluetooth.setImageDrawable(getResources().getDrawable(R.drawable.ic_bluetooth_connected_24dp));
                                serialConnectionState = SERIAL_CONNECTED;

                            } else if (mUAVTalkDevice != null && mUAVTalkDevice.isConnecting()) {
                                serialConnectionState = SERIAL_CONNECTING;
                                int alpha;
                                if (blink) {
                                    imgBluetooth.setColorFilter(Color.argb(0xff, 0xff, 0x66, 0x00));
                                    imgBluetooth.setImageDrawable(getResources().getDrawable(R.drawable.ic_bluetooth_24dp));
                                } else {
                                    imgBluetooth.setColorFilter(Color.argb(0xff, 0xff, 0x66, 0x00));
                                    imgBluetooth.setImageDrawable(getResources().getDrawable(R.drawable.ic_bluetooth_connected_24dp));
                                }
                            } else {
                                serialConnectionState = SERIAL_DISCONNECTED;
                                imgBluetooth.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
                                imgBluetooth.setImageDrawable(getResources().getDrawable(R.drawable.ic_bluetooth_disabled_24dp));
                            }
                        } else if (serialModeUsed == SERIAL_USB) {
                            imgBluetooth.setColorFilter(Color.argb(0xff, 0x00, 0x00, 0x00));
                            if (mUAVTalkDevice != null && mUAVTalkDevice.isConnected()) {
                                imgUSB.setColorFilter(Color.argb(0xff, 0x00, 0x80, 0x00));
                                //imgUSB.setImageDrawable(getResources().getDrawable(R.drawable.ic_usb_24dp));
                                serialConnectionState = SERIAL_CONNECTED;

                            } else if (mUAVTalkDevice != null && mUAVTalkDevice.isConnecting()) {
                                serialConnectionState = SERIAL_CONNECTING;
                                int alpha;
                                if (blink) {
                                    imgUSB.setColorFilter(Color.argb(0xff, 0xff, 0x66, 0x00));
                                    //imgUSB.setImageDrawable(getResources().getDrawable(R.drawable.ic_usb_24dp));
                                } else {
                                    imgUSB.setColorFilter(Color.argb(0xff, 0xff, 0x88, 0x00));
                                    //imgUSB.setImageDrawable(getResources().getDrawable(R.drawable.ic_usb_24dp));
                                }
                            } else {
                                serialConnectionState = SERIAL_DISCONNECTED;
                                imgUSB.setColorFilter(Color.argb(0xff, 0xd4, 0x00, 0x00));
                                //imgBluetooth.setImageDrawable(getResources().getDrawable(R.drawable.ic_usb_24dp));
                            }
                        }
                    }
                });

                if (this.oTree == null || mUAVTalkDevice == null || !mUAVTalkDevice.isConnected()) {
                    continue;  //nothing yet to show, or not connected
                }

                requestObjects();  //BT is flightmode, so the flow is not flowing..missing handshake?

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            switch (currentView) {
                                case VIEW_MAIN:

                                    txtObjectLogTx.setText(H.k(String.valueOf(txObjects)));
                                    txtObjectLogRxGood.setText(H.k(String.valueOf(rxObjectsGood)));
                                    txtObjectLogRxBad.setText(H.k(String.valueOf(rxObjectsBad)));

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

                                    if (serialModeUsed == SERIAL_BLUETOOTH) {
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

                                    LatLng src = map.getCameraPosition().target;
                                    LatLng dst = new LatLng(lat, lng);

                                    double distance = H.calculationByDistance(src, dst);
                                    if (distance > 0.001) {
                                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 19);
                                        MapsInitializer.initialize(mActivity);
                                        if (distance < 200) {
                                            map.animateCamera(cameraUpdate);
                                        } else {
                                            map.moveCamera(cameraUpdate);
                                        }

                                        posHistory[currentPosMarker] = map.addMarker(new MarkerOptions()
                                                        .position(new LatLng(lat, lng))
                                                        .title("Librepilot")
                                                        .snippet("LP rules")
                                                        .flat(true)
                                                        .anchor(0.5f, 0.5f)
                                                        .rotation(deg)
                                                //.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                                        );

                                        currentPosMarker++;
                                        if (currentPosMarker >= HISTORY_MARKER_NUM) {
                                            currentPosMarker = 0;
                                        }
                                        if (posHistory[currentPosMarker] != null) {
                                            posHistory[currentPosMarker].remove();
                                        }
                                    } else {
                                    }
                                    break;
                                case VIEW_OBJECTS:
                                    try {
                                        etxObjects.setText(mUAVTalkDevice.getoTree().toString());
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
                            }
                        } catch (NullPointerException e) {
                            Log.d("NPE", "Nullpointer Exception in Pollthread, most likely switched Connections");

                        }
                    }
                });
            }
        }

        private void requestObjects() {
            if (serialModeUsed == SERIAL_BLUETOOTH) {
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
                    String str = oTree.getData("SystemSettings", 0, "VehicleName", i).toString();
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
                Long l = (Long) oTree.getData(object, field);
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
                Float f2 = (Float) offset.get(soffset);
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
                e.printStackTrace();
            }
            return EMPTY_STRING;
        }

        private Object getData(String objectname, String fieldname) {
            try {
                Object o = oTree.getData(objectname, fieldname);
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
            return EMPTY_STRING;
        }

        private Object getData(String objectname, String fieldname, String elementname) {
            Object o = null;
            try {
                o = oTree.getData(objectname, fieldname, elementname);
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
                return EMPTY_STRING;
            }
        }
    }

    private class ConnectionThread extends Thread {
        MainActivity mActivity;

        public ConnectionThread(MainActivity mActivity) {
            this.mActivity = mActivity;
        }


        public void run() {
            while (true) {
                try {
                    //Log.d("CT", "Alive " + doReconnect);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
                if (mUAVTalkDevice != null) {
                    //Log.d("CT", "MUAVTD " + mUAVTalkDevice.isConnecting() + " " +mUAVTalkDevice.isConnected());
                } else {
                    //Log.d("CT", "MUAVTD NULL");
                }

                if (doReconnect) {
                    if (mUAVTalkDevice != null) mUAVTalkDevice.stop();
                    mUAVTalkDevice = null;
                    doReconnect = false;
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mActivity.resetMainView();
                        }
                    });
                }

                if (mUAVTalkDevice == null || (!mUAVTalkDevice.isConnected() && !mUAVTalkDevice.isConnecting())) {
                    switch (serialModeUsed) {
                        case SERIAL_BLUETOOTH:
                            connectBluettooth();
                            break;
                        case SERIAL_USB:
                            connectUSB();
                            break;
                    }
                } else {
                    //mUAVTalkDevice.stop();
                    //mUAVTalkDevice = null;
                }

            }
        }
    }
}
