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

import android.graphics.Color;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.helper.ScopeHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkObject;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectListener;

public class ViewControllerScope extends ViewController implements
        UAVTalkObjectListener {
    LineChart lchScope;
    TextView txtScopeLastValue;
    TextView txtScopeObjectName;
    String mObject;
    String mField;
    String mElement;
    private LineData mData;
    private long relTime;
    private long curTime = 0;

    public ViewControllerScope(MainActivity activity, int title, int icon, int localSettingsVisible,
                               int flightSettingsVisible) {
        super(activity, title, icon, localSettingsVisible, flightSettingsVisible);
        activity.mViews.put(VIEW_SCOPE,
                activity.getLayoutInflater().inflate(R.layout.activity_scope, null));
        activity.setContentView(activity.mViews.get(VIEW_SCOPE));
    }

    @Override
    public void enter(int view) {
        super.enter(view);
        lchScope = (LineChart) findViewById(R.id.scope_chart);
        lchScope.setDrawGridBackground(false);
        lchScope.setNoDataTextDescription("Select a Value in the ObjectBrowser to start the Scope");

        mData = new LineData();
        mData.setValueTextColor(Color.GREEN);

        // add empty data
        if (mObject != null) lchScope.setData(mData);

        txtScopeLastValue = (TextView) findViewById(R.id.txtScopeLastValue);
        txtScopeObjectName = (TextView) findViewById(R.id.txtScopeObjectName);

        mObject = ScopeHelper.object;
        mField = ScopeHelper.field;
        mElement = ScopeHelper.element;

        if (mObject != null) {
            setScopeMetric(mObject, mField, mElement);
            lchScope.setDescription(mObject + "/" + mField + "/" + mElement);
        }
        txtScopeObjectName.setText(mObject);
        relTime = System.currentTimeMillis();



    }

    @Override
    public void leave() {
        super.leave();
        try {
            getMainActivity().mFcDevice.getObjectTree().removeListener(mObject);
        } catch (NullPointerException ignored) {

        }
        lchScope = null;
        txtScopeObjectName = null;
        txtScopeLastValue = null;
    }

    public void setScopeMetric(String object, String field, String element) {
        this.mObject = object;
        this.mField = field;
        this.mElement = element;
        try {
            getMainActivity().mFcDevice.getObjectTree().removeListener(mObject);
            try {
                getMainActivity().mFcDevice.getObjectTree().setListener(object, this);
            } catch (IllegalStateException e) {
                getMainActivity().mFcDevice.getObjectTree().removeListener(object);
                getMainActivity().mFcDevice.getObjectTree().setListener(object, this);
            }
        } catch (NullPointerException ignored) {

        }
    }

    @Override
    public void onObjectUpdate(UAVTalkObject o) {
        if (txtScopeLastValue != null) {
            String data = getData(mObject, mField, mElement).toString();
            float entry = .0f;
            try {
                entry = Float.parseFloat(data);
            } catch (NumberFormatException e) {

            }
            txtScopeLastValue.setText(data);

            addEntry(entry);

            lchScope.postInvalidate();
        }
    }

    @Override
    public int getID() {
        return ViewController.VIEW_SCOPE;
    }

    private void addEntry(float entry) {

        LineData data = lchScope.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            // add a new x-value first
            Long tsLong = (System.currentTimeMillis() - relTime) / 1000;
            // if(tsLong != curTime) {
            data.addXValue(tsLong.toString());
            curTime = tsLong;
            //}

            data.addEntry(new Entry(entry, set.getEntryCount()), 0);


            // let the chart know it's data has changed
            lchScope.notifyDataSetChanged();

            // limit the number of visible entries
            lchScope.setVisibleXRangeMaximum(60);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            lchScope.moveViewToX(data.getXValCount() - 61);

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

}
