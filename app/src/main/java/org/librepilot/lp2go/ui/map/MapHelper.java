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

package org.librepilot.lp2go.ui.map;

import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.librepilot.lp2go.H;

import java.util.ArrayDeque;
import java.util.Deque;

public class MapHelper {

    private final int MAX_LINES = 20;
    private final GoogleMap mMap;
    private Deque<Polyline> mMapLines;
    private Marker mPosMarker;

    public MapHelper(GoogleMap map) {
        mMapLines = new ArrayDeque<>();
        mMap = map;
    }

    public void updatePosition(LatLng s, LatLng e, float deg) {
        PolylineOptions line = new PolylineOptions()
                .add(s)
                .add(e)
                .color(Color.RED);
        Polyline pl = mMap.addPolyline(line);
        mMapLines.addFirst(pl);
        if (mMapLines.size() > MAX_LINES) {
            Polyline del = mMapLines.removeLast();
            del.remove();
        }

        if (mPosMarker != null) {
            mPosMarker.remove();
        }
        mPosMarker = mMap.addMarker(new MarkerOptions()
                .position(e)
                .flat(true)
                .anchor(0.5f, 0.5f)
                .rotation(deg)
        );

        double distance = H.calculationByDistance(s, e);
        if (distance > 0.001) {
            CameraUpdate cameraUpdate =
                    CameraUpdateFactory.newLatLng(e);

            if (distance < 200) {
                mMap.animateCamera(cameraUpdate);
            } else {
                mMap.moveCamera(cameraUpdate);
            }
        }
    }
}
