package org.librepilot.lp2go.controller;

import android.widget.TextView;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.helper.MapHelper;
import org.librepilot.lp2go.helper.SettingsHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkMissingObjectException;

/**
 * Created by Marcus on 19.05.2016.
 */
public class ViewControllerMap extends ViewController {
    private final MapHelper mMapHelper;
    private final MapView mMapView;
    private Float mFcCurrentLat = null;
    private Float mFcCurrentLng = null;
    private TextView txtLatitude;
    private TextView txtLongitude;
    private TextView txtMapGPS;
    private TextView txtMapGPSSatsInView;

    public ViewControllerMap(MainActivity activity, int title, int localSettingsVisible,
                             int flightSettingsVisible) {
        super(activity, title, localSettingsVisible, flightSettingsVisible);
        activity.mViews
                .put(VIEW_MAP, activity.getLayoutInflater().inflate(R.layout.activity_map, null));
        activity.setContentView(activity.mViews.get(VIEW_MAP)); //Map
        {
            mMapView = (MapView) findViewById(R.id.map);
            mMapHelper = new MapHelper(activity);

            if (mMapView != null) {
                mMapView.onCreate(null);

                mMapView.getMapAsync(mMapHelper);
            }

            txtLatitude = (TextView) findViewById(R.id.txtLatitude);
            txtLongitude = (TextView) findViewById(R.id.txtLongitude);
            txtMapGPS = (TextView) findViewById(R.id.txtMapGPS);
            txtMapGPSSatsInView = (TextView) findViewById(R.id.txtMapGPSSatsInView);
        }
    }

    @Override
    public void enter(int view) {
        super.enter(view);
        mMapView.onResume();    //(re)activate the Map
    }

    @Override
    public void leave() {
        super.leave();
        mMapView.onPause();
    }

    @Override
    public void update() {
        super.update();
        MainActivity ma = getMainActivity();
        if (SettingsHelper.mSerialModeUsed ==
                MainActivity.SERIAL_BLUETOOTH) {
            ma.mFcDevice.requestObject("GPSSatellites");
            ma.mFcDevice.requestObject("SystemAlarms");
            ma.mFcDevice.requestObject("GPSPositionSensor");
        }

        setText(txtMapGPSSatsInView,
                getData("GPSSatellites", "SatsInView").toString());
        setTextBGColor(txtMapGPS,
                getData("SystemAlarms", "Alarm", "GPS").toString());
        float deg = 0;
        try {
            deg = (Float) getData("GPSPositionSensor", "Heading");
        } catch (Exception ignored) {
        }

        Float lat = getGPSCoordinates("GPSPositionSensor", "Latitude");
        Float lng = getGPSCoordinates("GPSPositionSensor", "Longitude");

        if (mFcCurrentLat != null && mFcCurrentLng != null) {
            mMapHelper.updatePosition(
                    new LatLng(mFcCurrentLat, mFcCurrentLng),
                    new LatLng(lat, lng), deg);
        }

        mFcCurrentLat = lat;
        mFcCurrentLng = lng;

        setText(txtLatitude, lat.toString());
        setText(txtLongitude, lng.toString());
    }

    @Override
    public void reset() {
        super.reset();
        mMapHelper.clear();     //clear old markers
    }

    private Float getGPSCoordinates(String object, String field) {
        try {
            int i = (Integer) getMainActivity().mPollThread.mObjectTree.getData(object, field);
            return ((float) i / 10000000);
        } catch (UAVTalkMissingObjectException | NullPointerException | ClassCastException e1) {
            VisualLog.d("GPS", "getCoord", e1);
            return 0.0f;
        }
    }
}
