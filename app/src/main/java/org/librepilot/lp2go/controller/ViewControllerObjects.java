package org.librepilot.lp2go.controller;

import android.widget.EditText;
import android.widget.TextView;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.R;
import org.librepilot.lp2go.uavtalk.UAVTalkXMLObject;
import org.librepilot.lp2go.ui.objectbrowser.ChildString;
import org.librepilot.lp2go.ui.objectbrowser.ObjectsExpandableListView;
import org.librepilot.lp2go.ui.objectbrowser.ObjectsExpandableListViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Marcus on 19.05.2016.
 */
public class ViewControllerObjects extends ViewController {
    private ObjectsExpandableListView mExpListView;
    private ObjectsExpandableListViewAdapter mListAdapter;
    private TextView txtObjects;

    public ViewControllerObjects(MainActivity activity, int title, int localSettingsVisible,
                                 int flightSettingsVisible) {
        super(activity, title, localSettingsVisible, flightSettingsVisible);
        activity.mViews.put(VIEW_OBJECTS,
                activity.getLayoutInflater().inflate(R.layout.activity_objects, null));
        activity.setContentView(activity.mViews.get(VIEW_OBJECTS)); //Objects

        txtObjects = new EditText(activity);
        mExpListView = (ObjectsExpandableListView) findViewById(R.id.elvObjects);
        if (mExpListView != null) {
            mExpListView.setOnGroupExpandListener(mExpListView);
            mExpListView.setOnChildClickListener(mExpListView);
        }
    }

    @Override
    public void enter(int view) {
        super.enter(view);
        initObjectListData();
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

                    mExpListView
                            .init(new ArrayList<String>(),
                                    new HashMap<String, List<ChildString>>());


                    for (UAVTalkXMLObject xmlobj : ma.mXmlObjects.values()) {
                        mExpListView.getListDataHeader()
                                //.add(xmlobj.getName() + " (" + xmlobj.getId() + ")");
                                .add(xmlobj.getName());
                        //VisualLog.d("OBJ", xmlobj.getName());
                    }

                    mListAdapter =
                            new ObjectsExpandableListViewAdapter(ma,
                                    mExpListView.getListDataHeader(),
                                    mExpListView.getListDataChild());

                    // setting list adapter
                    mExpListView.setmAdapter(mListAdapter);

                }
            });
        }
    }
}
