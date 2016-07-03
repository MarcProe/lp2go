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

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.helper.SettingsHelper;
import org.librepilot.lp2go.ui.SingleToast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewControllerSettings extends ViewController
        implements AdapterView.OnItemSelectedListener,
        View.OnClickListener {

    private Button btnClearUavo;
    private Button btnLoadUavo;
    private Spinner spnBluetoothPairedDevice;
    private Spinner spnConnectionTypeSpinner;
    private Spinner spnUavoSource;

    public ViewControllerSettings(MainActivity activity, int title, int icon, int localSettingsVisible,
                                  int flightSettingsVisible) {
        super(activity, title, icon, localSettingsVisible, flightSettingsVisible);
        activity.mViews.put(VIEW_SETTINGS,
                activity.getLayoutInflater().inflate(R.layout.activity_settings, null));

        activity.setContentView(activity.mViews.get(VIEW_SETTINGS)); //Settings

        spnConnectionTypeSpinner = (Spinner) findViewById(R.id.spnConnectionTypeSpinner);
        ArrayAdapter<CharSequence> serialConnectionTypeAdapter
                = ArrayAdapter.createFromResource(activity,
                R.array.connections_settings, android.R.layout.simple_spinner_item);

        serialConnectionTypeAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnConnectionTypeSpinner.setAdapter(serialConnectionTypeAdapter);
        spnConnectionTypeSpinner.setOnItemSelectedListener(this);
        spnConnectionTypeSpinner.setSelection(SettingsHelper.mSerialModeUsed);

        spnBluetoothPairedDevice = (Spinner) findViewById(R.id.spnBluetoothPairedDevice);

        ArrayAdapter<CharSequence> btPairedDeviceAdapter =
                new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);
        btPairedDeviceAdapter.
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnBluetoothPairedDevice.setAdapter(btPairedDeviceAdapter);
        spnBluetoothPairedDevice.setOnItemSelectedListener(this);

        activity.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (activity.mBluetoothAdapter != null) {
            // Device does support Bluetooth
            if (!activity.mBluetoothAdapter.isEnabled()) {
                SingleToast.show(activity,
                        getString(R.string.BLUETOOTH_WARNING), Toast.LENGTH_LONG);

            } else {

                Set<BluetoothDevice> pairedDevices =
                        activity.mBluetoothAdapter.getBondedDevices();
                // If there are paired devices
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    int btpd = 0;
                    for (android.bluetooth.BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        btPairedDeviceAdapter.add(device.getName());
                        if (device.getName().equals(SettingsHelper.mBluetoothDeviceUsed)) {
                            spnBluetoothPairedDevice.setSelection(btpd);
                        }
                        btpd++;

                        VisualLog.d("BTE", device.getName() + " " + device.getAddress());

                    }
                }
            }
        }
        initUavoSpinner();

        btnClearUavo = (Button) activity.findViewById(R.id.btnClearUavo);
        if (btnClearUavo != null) {
            btnClearUavo.setOnClickListener(this);
        }
        btnLoadUavo = (Button) activity.findViewById(R.id.btnLoadUavo);
        if (btnLoadUavo != null) {
            btnLoadUavo.setOnClickListener(this);
        }

    }

    @Override
    public int getID() {
        return ViewController.VIEW_SETTINGS;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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

    @Override
    public void enter(int view) {
        super.enter(view);

        if (SettingsHelper.mSerialModeUsed == MainActivity.SERIAL_NONE) {
            SingleToast.show(getMainActivity(), getString(R.string.PLEASE_SET_A)
                    + getString(R.string.CON_TYPE), Toast.LENGTH_LONG);
        } else if (SettingsHelper.mSerialModeUsed == MainActivity.SERIAL_BLUETOOTH &&
                SettingsHelper.mBluetoothDeviceUsed == null) {
            SingleToast.show(getMainActivity(), getString(R.string.PLEASE_SET_A)
                    + getString(R.string.BT_DEVICE), Toast.LENGTH_LONG);
        }

    }

    @Override
    public void leave() {
        super.leave();
        String btname = getString(R.string.EMPTY_STRING);
        if (spnBluetoothPairedDevice.getSelectedItem() != null) {
            btname = spnBluetoothPairedDevice.getSelectedItem().toString();
        }
        SettingsHelper.mBluetoothDeviceUsed = btname;

        String btmac = getString(R.string.EMPTY_STRING);

        try {
            Set<android.bluetooth.BluetoothDevice> pairedDevices =
                    getMainActivity().mBluetoothAdapter.getBondedDevices();
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
        } catch (NullPointerException e) {
            VisualLog.e("ERR", "No BT Device found.");
        }
        SettingsHelper.mBluetoothDeviceAddress = btmac;

        SettingsHelper.mSerialModeUsed = spnConnectionTypeSpinner.getSelectedItemPosition();

        if (spnUavoSource.getSelectedItem() != null
                && !spnUavoSource.getSelectedItem().toString()
                .equals(SettingsHelper.mLoadedUavo)) {
            SettingsHelper.mLoadedUavo = spnUavoSource.getSelectedItem().toString();
            VisualLog.d("UAVSource",
                    SettingsHelper.mLoadedUavo + "  " + SettingsHelper.mLoadedUavo);

            getMainActivity().loadXmlObjects(true);
            SingleToast.show(getMainActivity(), "UAVO load completed", Toast.LENGTH_SHORT);
        }

        SettingsHelper.saveSettings(getMainActivity());

        getMainActivity().mDoReconnect = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        MainActivity ma = getMainActivity();

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.CALLBACK_FILEPICKER_UAVO && resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            SingleToast.show(ma, filePath, Toast.LENGTH_LONG);

            try {
                FileInputStream in = new FileInputStream(new File(filePath));
                Uri uri = Uri.parse(filePath);
                String strFileName = uri.getLastPathSegment();
                ma.copyFile(in, strFileName);
                VisualLog.d("OK", "OK");
            } catch (FileNotFoundException e) {
                VisualLog.d("FNF", "FNF");
                SingleToast.show(ma, filePath + " not found", Toast.LENGTH_LONG);
            } catch (IOException e) {
                VisualLog.d("IOE", "IOE");
                SingleToast.show(ma, "Cannot open " + filePath, Toast.LENGTH_LONG);
            }

            initUavoSpinner();

        }
    }

    private void initUavoSpinner() {
        spnUavoSource = (Spinner) findViewById(R.id.spnUavoSource);
        ArrayAdapter<CharSequence> uavoSourceAdapter =
                new ArrayAdapter<>(getMainActivity(), android.R.layout.simple_spinner_item);
        uavoSourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnUavoSource.setAdapter(uavoSourceAdapter);
        spnUavoSource.setOnItemSelectedListener(this);

        File dir = getMainActivity().getFilesDir();
        File[] subFiles = dir.listFiles();

        if (subFiles != null) {
            int i = 0;
            for (File file : subFiles) {
                Pattern p = Pattern.compile(".*uavo-(.*)\\.zip$");
                Matcher m = p.matcher(file.toString());
                boolean b = m.matches();
                if (b) {
                    uavoSourceAdapter.add(m.group(1));
                    if (m.group(1).equals(SettingsHelper.mLoadedUavo)) {
                        spnUavoSource.setSelection(i);
                    }
                    i++;
                }
            }
        }
    }

    private void onClearUavObjectFilesClick() {
        File dir = getMainActivity().getFilesDir();
        File[] subFiles = dir.listFiles();

        if (subFiles != null) {
            for (File file : subFiles) {
                Pattern p = Pattern.compile(".*uavo-(.*)\\.zip$");
                Matcher m = p.matcher(file.toString());
                boolean b = m.matches();
                if (b) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        }

        getMainActivity().copyAssets();
        initUavoSpinner();

        SingleToast.show(getMainActivity(), "Files deleted", Toast.LENGTH_LONG);
    }

    private void onLoadUavObjectFile() {
        new MaterialFilePicker()
                .withActivity(getMainActivity())
                .withRequestCode(MainActivity.CALLBACK_FILEPICKER_UAVO)
                .withFilter(Pattern.compile(".*\\.zip$"))
                .withFilterDirectories(false)
                .withHiddenFiles(false)
                .start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClearUavo: {
                onClearUavObjectFilesClick();
                break;
            }
            case R.id.btnLoadUavo: {
                onLoadUavObjectFile();
                break;
            }
        }
    }
}
