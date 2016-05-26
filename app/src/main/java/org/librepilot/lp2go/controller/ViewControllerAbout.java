package org.librepilot.lp2go.controller;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.menu.MenuItem;

public class ViewControllerAbout extends ViewController implements View.OnClickListener {

    public ViewControllerAbout(MainActivity activity, int title, int localSettingsVisible,
                               int flightSettingsVisible) {
        super(activity, title, localSettingsVisible, flightSettingsVisible);
        final MainActivity ma = getMainActivity();
        ma.mViews.put(VIEW_ABOUT, ma.getLayoutInflater().inflate(R.layout.activity_about, null));
        ma.setContentView(ma.mViews.get(VIEW_ABOUT));  //About

        final Resources res = ma.getResources();
        PackageInfo pInfo = null;

        try {
            pInfo = ma.getPackageManager().getPackageInfo(ma.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        final TextView txtAndroidVersionRelease =
                (TextView) ma.findViewById(R.id.txtAndroidVersionRelease);
        final TextView txtLP2GoVersionRelease =
                (TextView) ma.findViewById(R.id.txtLP2GoVersionRelease);
        final TextView txtLP2GoPackage =
                (TextView) ma.findViewById(R.id.txtLP2GoPackage);

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
        findViewById(R.id.imgDebugLogo).setOnClickListener(this);

    }

    public void onDebugLogoClick() {
        if (getMainActivity().menDebug == null) {
            getMainActivity().menDebug =
                    new MenuItem(getString(R.string.menu_debug), R.drawable.ic_cancel_128dp);
            getMainActivity().initSlider();
        } else {
            getMainActivity().menDebug = null;
            getMainActivity().initSlider();
        }
    }

    @Override
    public void onClick(View v) {
        onDebugLogoClick();
    }
}
