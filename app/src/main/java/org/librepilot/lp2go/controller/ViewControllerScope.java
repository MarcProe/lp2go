package org.librepilot.lp2go.controller;

import com.github.mikephil.charting.charts.LineChart;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;

/**
 * Created by Marcus on 19.05.2016.
 */
public class ViewControllerScope extends ViewController {
    LineChart lchScope;

    public ViewControllerScope(MainActivity activity, int title, int localSettingsVisible,
                               int flightSettingsVisible) {
        super(activity, title, localSettingsVisible, flightSettingsVisible);
        activity.mViews.put(VIEW_SCOPE,
                activity.getLayoutInflater().inflate(R.layout.activity_scope, null));
        activity.setContentView(activity.mViews.get(VIEW_SCOPE));
        {
            lchScope = (LineChart) findViewById(R.id.scope_chart);
            lchScope.setDrawGridBackground(false);
        }
    }
}
