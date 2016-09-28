/*
 * @file   ObjectsExpandableListViewAdapter.java
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

package org.librepilot.lp2go.ui.objectbrowser;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.librepilot.lp2go.R;
import org.librepilot.lp2go.helper.H;
import org.librepilot.lp2go.helper.SettingsHelper;
import org.librepilot.lp2go.uavtalk.UAVTalkXMLObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

public class ObjectsExpandableListViewAdapter extends BaseExpandableListAdapter {

    private final Context mContext;
    // child data in format of header title, child title
    private final HashMap<String, List<ChildString>> mListDataChild;
    private final List<String> mListDataHeader; // header titles

    public ObjectsExpandableListViewAdapter(Context context, List<String> listDataHeader,
                                            HashMap<String, List<ChildString>> listChildData) {
        this.mContext = context;
        this.mListDataHeader = listDataHeader;
        this.mListDataChild = listChildData;
    }

    @Override
    public int getGroupCount() {
        return this.mListDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        try {
            return this.mListDataChild.get(this.mListDataHeader.get(groupPosition)).size();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mListDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.mListDataChild.get(this.mListDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        View retView = convertView;
        String headerTitle = (String) getGroup(groupPosition);
        if (retView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            retView = infalInflater.inflate(R.layout.activity_objects_list_group, null);
        }

        TextView lblListHeader = (TextView) retView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        ImageView imgListHeaderFavIcon = (ImageView) retView.findViewById(R.id.imgListHeaderFavIcon);
        if (SettingsHelper.mObjectFavorites.contains(headerTitle)) {
            imgListHeaderFavIcon.setImageDrawable(ContextCompat.getDrawable(mContext,
                    R.drawable.ic_star_black_128dp));
        } else {
            imgListHeaderFavIcon.setImageDrawable(ContextCompat.getDrawable(mContext,
                    android.R.color.transparent));
        }

        return retView;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertViewParam, ViewGroup parent) {

        View convertView = convertViewParam;

        final ChildString childText = (ChildString) getChild(groupPosition, childPosition);
        //boolean isInstanceHeader = childText.isInstanceHeader;

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.activity_objects_list_item, null);
        }

        TextView txtListChildLabel = (TextView) convertView.findViewById(R.id.txtListItemLabel);
        TextView txtListChildValue = (TextView) convertView.findViewById(R.id.txtListItemValue);
        ImageView imgListChildIcon = (ImageView) convertView.findViewById(R.id.imgListChildIcon);

        if (childText.isInstanceHeader || childText.fieldname == null ||
                childText.fieldname.equals("")) {
            txtListChildLabel.setBackgroundColor(Color.argb(0xff, 0x80, 0x80, 0xff));
            txtListChildValue.setBackgroundColor(Color.argb(0xff, 0x80, 0x80, 0xff));
            imgListChildIcon.setImageResource(android.R.color.transparent);
        } else if (childText.isSettings) {
            txtListChildLabel.setBackgroundColor(Color.TRANSPARENT);
            txtListChildValue.setBackgroundColor(Color.TRANSPARENT);
            imgListChildIcon.setImageDrawable(ContextCompat.getDrawable(mContext,
                    R.drawable.ic_create_black_48dp));
        } else {
            txtListChildLabel.setBackgroundColor(Color.TRANSPARENT);
            txtListChildValue.setBackgroundColor(Color.TRANSPARENT);
            imgListChildIcon.setImageDrawable(ContextCompat.getDrawable(mContext,
                    R.drawable.ic_timeline_black_48dp));
        }

        String fText;
        if (childText.type == UAVTalkXMLObject.FIELDTYPE_FLOAT32) {
            fText = (new DecimalFormat("##########.#######"))
                    .format(H.stringToFloat(childText.getValue()));
        } else {
            fText = childText.getValue();
        }

        txtListChildLabel.setText(childText.getLabel());
        txtListChildValue.setText(fText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}

