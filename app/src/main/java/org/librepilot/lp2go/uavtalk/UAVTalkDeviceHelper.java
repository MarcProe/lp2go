/*
 * @file   UAVTalkDeviceHelper.java
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

package org.librepilot.lp2go.uavtalk;

import android.support.annotation.Nullable;

import org.librepilot.lp2go.VisualLog;

public class UAVTalkDeviceHelper {

    private static final String TAG = UAVTalkDeviceHelper.class.getSimpleName();

    public static byte[] updateSettingsObject(
            UAVTalkObjectTree oTree, String objectName, int instance, String fieldName,
            String elementName, byte[] newFieldData) {
        try {
            return updateSettingsObject(
                    oTree,
                    objectName,
                    instance,
                    fieldName,
                    oTree.getElementIndex(objectName, fieldName, elementName),
                    newFieldData);
        } catch (NullPointerException e) {
            VisualLog.e(TAG, e.getMessage());
            return new byte[0];
        }
    }

    @Nullable
    public static byte[] updateSettingsObject(
            UAVTalkObjectTree oTree, String objectName, int instance,
            String fieldName, int element, byte[] newFieldData) {

        UAVTalkXMLObject xmlObj = oTree.getXmlObjects().get(objectName);
        if (xmlObj == null) {
            return null;
        }

        UAVTalkObject obj = oTree.getObjectNoCreate(objectName);
        if (obj == null) {
            obj = new UAVTalkObject(xmlObj.getId());
            //return null;
        }

        UAVTalkObjectInstance ins = obj.getInstance(instance);
        if (ins == null) {
            byte[] emptydata = new byte[xmlObj.getLength()];
            ins = new UAVTalkObjectInstance(instance, emptydata);
            obj.setInstance(ins);
            //return null;
        }

        byte[] data = ins.getData();
        if (data == null) {
            return null;
        }

        UAVTalkXMLObject.UAVTalkXMLObjectField xmlField = xmlObj.getFields().get(fieldName);
        if (xmlField == null) {
            return null;
        }

        int fpos = xmlField.mPos;
        int elen = xmlField.mTypelength;

        int savepos = fpos + elen * element;

        try {
            System.arraycopy(newFieldData, 0, data, savepos, newFieldData.length);
        } catch (ArrayIndexOutOfBoundsException e) {
            VisualLog.e(e);
            return null;
        }

        ins.setData(data);
        obj.setInstance(ins);
        oTree.updateObject(obj);

        return obj.toMessage((byte) 0x22, ins.getId(), false);
    }
}
