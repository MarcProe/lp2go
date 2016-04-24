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

package net.proest.lp2go3.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

import net.proest.lp2go3.MainActivity;
import net.proest.lp2go3.UAVTalk.UAVTalkMissingObjectException;
import net.proest.lp2go3.UAVTalk.UAVTalkObject;
import net.proest.lp2go3.UAVTalk.UAVTalkObjectInstance;
import net.proest.lp2go3.UAVTalk.UAVTalkXMLObject;
import net.proest.lp2go3.UAVTalk.device.FcDevice;
import net.proest.lp2go3.VisualLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ObjectsExpandableListView extends ExpandableListView implements ExpandableListView.OnGroupExpandListener {
    private List<String> mListDataHeader;
    private HashMap<String, List<String>> mListDataChild;
    private int mGroupPosition;
    private String mExpandedObjectName;
    private ObjectsExpandableListViewAdapter mAdapter;

    public ObjectsExpandableListView(Context context) {
        super(context);
    }

    public ObjectsExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObjectsExpandableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setmAdapter(ObjectsExpandableListViewAdapter mAdapter) {
        super.setAdapter(mAdapter);
        this.mAdapter = mAdapter;
    }

    public String getExpandedObjectName() {
        return mExpandedObjectName;
    }

    public List<String> getListDataHeader() {
        return mListDataHeader;
    }

    public HashMap<String, List<String>> getListDataChild() {
        return mListDataChild;
    }

    public void init(List<String> listDataHeader, HashMap<String, List<String>> listDataChild) {
        mListDataHeader = listDataHeader;
        mListDataChild = listDataChild;
    }

    @Override
    public void onGroupExpand(int groupPosition) {
        if (mGroupPosition != groupPosition && isGroupExpanded(mGroupPosition)) {
            //collapse previously expanded group
            collapseGroup(mGroupPosition);
        }
        mGroupPosition = groupPosition;
        mExpandedObjectName = mListDataHeader.get(groupPosition);
    }

    public void updateExpandedGroup(UAVTalkXMLObject xmlobj) {
        if (xmlobj == null) return;
        List<String> fields = new ArrayList<String>();
        try {

            FcDevice fcdevice = ((MainActivity) getContext()).getFcDevice();
            UAVTalkObject obj = fcdevice.getObjectTree().getObjectNoCreate(xmlobj.getName());

            for (UAVTalkObjectInstance inst : obj.getInstances().values()) {
                if (inst != null) {
                    fields.add("Instance " + inst.getId());
                    for (UAVTalkXMLObject.UAVTalkXMLObjectField xmlfield : xmlobj.getFields().values()) {
                        VisualLog.d("FLD", xmlfield.toString());
                        for (String element : xmlfield.getElements()) {
                            try {
                                String data = fcdevice.getObjectTree().getData(xmlobj.getName(), inst.getId(), xmlfield.getName(), element).toString();
                                String tele = "";
                                if (element != null && element.length() > 0) {
                                    tele = " - " + element;
                                }
                                fields.add(xmlfield.getName() + tele + " = " + data);
                            } catch (UAVTalkMissingObjectException e) {
                                fields.add(e.getMessage());
                            }
                        }
                    }
                }
            }

        } catch (NullPointerException e) {
            VisualLog.d("OBJ", "NPE", e);
            fields.add(xmlobj.getName() + " not found.");
        }

        this.getListDataChild().put(this.getListDataHeader().get(mGroupPosition), fields);
        mAdapter.notifyDataSetChanged();
    }
}
