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

package net.proest.lp2go3.ui.objectbrowser.list;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import net.proest.lp2go3.MainActivity;
import net.proest.lp2go3.R;
import net.proest.lp2go3.VisualLog;
import net.proest.lp2go3.uavtalk.UAVTalkMissingObjectException;
import net.proest.lp2go3.uavtalk.UAVTalkObject;
import net.proest.lp2go3.uavtalk.UAVTalkObjectInstance;
import net.proest.lp2go3.uavtalk.UAVTalkXMLObject;
import net.proest.lp2go3.uavtalk.device.FcDevice;
import net.proest.lp2go3.ui.SingleToast;
import net.proest.lp2go3.ui.alertdialog.EnumInputAlertDialog;
import net.proest.lp2go3.ui.alertdialog.NumberInputAlertDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ObjectsExpandableListView extends ExpandableListView
        implements ExpandableListView.OnGroupExpandListener,
        ExpandableListView.OnChildClickListener {
    private ObjectsExpandableListViewAdapter mAdapter;
    private String mExpandedObjectName;
    private int mGroupPosition;
    private HashMap<String, List<ChildString>> mListDataChild;
    private List<String> mListDataHeader;

    public ObjectsExpandableListView(Context context) {
        super(context);
    }

    public ObjectsExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObjectsExpandableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public String getExpandedObjectName() {
        return mExpandedObjectName;
    }

    public HashMap<String, List<ChildString>> getListDataChild() {
        return mListDataChild;
    }

    public List<String> getListDataHeader() {
        return mListDataHeader;
    }

    public void setmAdapter(ObjectsExpandableListViewAdapter mAdapter) {
        super.setAdapter(mAdapter);
        this.mAdapter = mAdapter;
    }

    public void init(List<String> listDataHeader,
                     HashMap<String, List<ChildString>> listDataChild) {
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
        if (xmlobj == null) {
            return;
        }
        List<ChildString> fields = new ArrayList<>();
        try {

            FcDevice fcdevice = ((MainActivity) getContext()).getFcDevice();
            UAVTalkObject obj = fcdevice.getObjectTree().getObjectNoCreate(xmlobj.getName());

            for (UAVTalkObjectInstance inst : obj.getInstances().values()) {
                if (inst != null) {
                    fields.add(new ChildString(inst.getId()));
                    for (UAVTalkXMLObject.UAVTalkXMLObjectField xmlfield : xmlobj.getFields()
                            .values()) {
                        for (String element : xmlfield.getElements()) {
                            try {
                                String data = fcdevice.getObjectTree()
                                        .getData(xmlobj.getName(), inst.getId(), xmlfield.getName(),
                                                element).toString();
                                fields.add(new ChildString(xmlobj.getName(), inst.getId(),
                                        xmlfield.getName(), element, data, xmlfield.getType(),
                                        xmlobj.isSettings()));
                            } catch (UAVTalkMissingObjectException e) {
                                fields.add(new ChildString(e.getMessage()));
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            fields.add(new ChildString(xmlobj.getName() + " not found."));
        }

        this.getListDataChild().put(this.getListDataHeader().get(mGroupPosition), fields);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                int childPosition, long id) {
        ObjectsExpandableListView extParent = (ObjectsExpandableListView) parent;

        String objectname = extParent.getListDataHeader().get(groupPosition);

        ChildString c = extParent.getListDataChild().get(objectname).get(childPosition);
        VisualLog.d("CHILD", c.toString());

        if (c.isSettings) {
            switch (c.type) {
                case UAVTalkXMLObject.FIELDTYPE_ENUM:
                    new EnumInputAlertDialog(getContext())
                            .withTitle(c.objectname + " " + c.fieldname + " " + c.element)
                            .withUavTalkDevice(((MainActivity) getContext()).getFcDevice())
                            .withObject(c.objectname)
                            .withField(c.fieldname)
                            .withElement(c.element)
                            .show();
                    break;
                case UAVTalkXMLObject.FIELDTYPE_FLOAT32:
                case UAVTalkXMLObject.FIELDTYPE_UINT8:
                case UAVTalkXMLObject.FIELDTYPE_INT8:
                case UAVTalkXMLObject.FIELDTYPE_UINT16:
                case UAVTalkXMLObject.FIELDTYPE_INT16:
                case UAVTalkXMLObject.FIELDTYPE_UINT32:
                case UAVTalkXMLObject.FIELDTYPE_INT32:
                    new NumberInputAlertDialog(getContext())
                            .withTitle(c.objectname + " " + c.fieldname + " " + c.element)
                            .withUavTalkDevice(((MainActivity) getContext()).getFcDevice())
                            .withLayout(R.layout.alert_dialog_integer_input)
                            .withPresetText(c.data)
                            .withFieldType(c.type)
                            .withObject(c.objectname)
                            .withField(c.fieldname)
                            .withElement(c.element)
                            .show();
                    break;
                default:
                    SingleToast.show(getContext(), "Type not implemented", Toast.LENGTH_SHORT);
                    break;
            }
        } else {  //start graphing

        }

        return false;
    }
}
