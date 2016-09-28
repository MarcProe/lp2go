/*
 * @file   ViewControllerObjects.java
 * @author The LibrePilot Project, http://www.librepilot.org Copyright (C) 2016.
 * @see    The GNU Public License (GPL) Version 3
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.librepilot.lp2go.controller;

import android.widget.EditText;
import android.widget.TextView;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.helper.SettingsHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkXMLObject;
import org.librepilot.lp2go.ui.objectbrowser.ChildString;
import org.librepilot.lp2go.ui.objectbrowser.ObjectsExpandableListView;
import org.librepilot.lp2go.ui.objectbrowser.ObjectsExpandableListViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViewControllerObjects extends ViewController {
    private ObjectsExpandableListView mExpListView;
    private ObjectsExpandableListViewAdapter mListAdapter;
    private TextView txtObjects;

    public ViewControllerObjects(MainActivity activity, int title, int icon, int localSettingsVisible,
                                 int flightSettingsVisible) {
        super(activity, title, icon, localSettingsVisible, flightSettingsVisible);
        activity.mViews.put(VIEW_OBJECTS,
                activity.getLayoutInflater().inflate(R.layout.activity_objects, null));
        activity.setContentView(activity.mViews.get(VIEW_OBJECTS)); //Objects

        txtObjects = new EditText(activity);
        mExpListView = (ObjectsExpandableListView) findViewById(R.id.elvObjects);
        if (mExpListView != null) {
            mExpListView.setOnGroupExpandListener(mExpListView);
            mExpListView.setOnChildClickListener(mExpListView);
            mExpListView.setOnItemLongClickListener(mExpListView);
        }
    }

    @Override
    public void enter(int view) {
        super.enter(view);
        initObjectListData();
    }

    @Override
    public void leave() {
        super.leave();
        SettingsHelper.saveSettings(getMainActivity());
    }

    @Override
    public int getID() {
        return ViewController.VIEW_OBJECTS;
    }

    @Override
    public void update() {
        super.update();
        MainActivity ma = getMainActivity();
        try {
            txtObjects.setText(ma.mFcDevice.getObjectTree().toString());
            if (mExpListView.getExpandedObjectName() != null) {
                UAVTalkXMLObject xmlobj =
                        ma.mFcDevice.getObjectTree().getXmlObjects()
                                .get(mExpListView
                                        .getExpandedObjectName());
                mExpListView.updateExpandedGroup(xmlobj);
                ma.mFcDevice.requestObject(xmlobj.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initObjectListData() {
        final MainActivity ma = getMainActivity();
        if (ma.mXmlObjects != null) {
            ma.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mExpListView.init(new ArrayList<String>(),
                            new HashMap<String, List<ChildString>>());

                    int favs = 0;
                    for (UAVTalkXMLObject xmlobj : ma.mXmlObjects.values()) {
                        String name = xmlobj.getName();
                        if (SettingsHelper.mObjectFavorites.contains(name)) {
                            mExpListView.getListDataHeader().add(favs++, name);
                        } else {
                            mExpListView.getListDataHeader().add(name);
                        }
                    }

                    mListAdapter = new ObjectsExpandableListViewAdapter(ma,
                                    mExpListView.getListDataHeader(),
                                    mExpListView.getListDataChild());

                    mExpListView.setCustomAdapter(mListAdapter);

                }
            });
        }
    }
}
