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

import com.github.mikephil.charting.charts.LineChart;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;

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
