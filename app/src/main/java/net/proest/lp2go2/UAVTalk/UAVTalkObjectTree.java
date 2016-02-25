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

package net.proest.lp2go2.UAVTalk;

import android.util.Log;

import net.proest.lp2go2.H;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

public class UAVTalkObjectTree {

    private Hashtable<String, UAVTalkObject> objects;
    private Hashtable<String, UAVTalkXMLObject> xmlObjects;

    public UAVTalkObjectTree() {
        objects = new Hashtable<String, UAVTalkObject>();
    }

    public String toString() {
        String ret = "";
        Enumeration<String> e = xmlObjects.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            ret += xmlObjects.get(key).getId() + " " + key + "\r\n";
        }
        return ret;
    }

    public Hashtable<String, UAVTalkXMLObject> getXmlObjects() {
        return xmlObjects;
    }

    public void setXmlObjects(Hashtable<String, UAVTalkXMLObject> xmlObjects) {
        this.xmlObjects = xmlObjects;
    }

    public UAVTalkObject getObjectNoCreate(String name) {
        return objects.get(xmlObjects.get(name).getId());
    }

    public UAVTalkObject getObjectFromID(String oID) {
        UAVTalkObject obj = objects.get(oID);
        if (obj == null) {
            obj = new UAVTalkObject(oID);
        }
        return obj;
    }

    public UAVTalkObject getObjectFromName(String name) {
        String oID = xmlObjects.get(name).getId();
        UAVTalkObject obj = objects.get(oID);
        if (obj == null) {
            Log.d("OFN", "CREATED NEW OBJECT! " + name + " " + oID);
            obj = new UAVTalkObject(oID);
        }
        return obj;
    }

    public void updateObject(UAVTalkObject obj) {
        objects.put(obj.getId(), obj);
    }

    public int getSize() {
        return objects.size();
    }

    public byte[] getInstanceData(String objectname, int instance) {
        UAVTalkObject obj = getObjectFromName(objectname);
        UAVTalkObjectInstance ins = obj.getInstance(instance);
        if (ins == null) {
            return null;
        }
        return ins.getData();
    }

    private int getElementIndex(String objectname, String fieldname, String element) {
        int retval;
        UAVTalkXMLObject xmlobj = xmlObjects.get(objectname);
        UAVTalkXMLObject.UAVTalkXMLObjectField xmlfield = xmlobj.getFields().get(fieldname);

        retval = xmlfield.elements.indexOf(element);
        Arrays.toString(xmlfield.elements.toArray());
        //Log.d(xmlobj.getId(), Arrays.toString(xmlfield.elements.toArray()) + "~" +element+"#" +retval);
        return retval;
    }

    public Object getData(String objectname, String fieldname) throws UAVTalkMissingObjectException {
        return getData(objectname, 0, fieldname, 0);
    }

    public Object getData(String objectname, String fieldname, String element) throws UAVTalkMissingObjectException {
        return getData(objectname, 0, fieldname, element);
    }

    public Object getData(String objectname, int instance, String fieldname) throws UAVTalkMissingObjectException {
        return getData(objectname, instance, fieldname, 0);
    }

    public Object getData(String objectname, int instance, String fieldname, String elementname) throws UAVTalkMissingObjectException {
        return getData(objectname, instance, fieldname, getElementIndex(objectname, fieldname, elementname));
    }


    public Object getData(String objectname, int instance, String fieldname, int element) throws UAVTalkMissingObjectException {
        UAVTalkXMLObject xmlobj = xmlObjects.get(objectname);
        if (xmlobj == null) return "";
        UAVTalkXMLObject.UAVTalkXMLObjectField xmlfield = xmlobj.getFields().get(fieldname);

        UAVTalkObject obj = getObjectNoCreate(objectname);
        if (obj == null) {
            UAVTalkMissingObjectException e = new UAVTalkMissingObjectException();
            e.setInstance(instance);
            e.setObjectname(objectname);
            e.setIsSettings(xmlobj.getIsSettings());
            throw e;
        }

        UAVTalkObjectInstance ins = obj.getInstance(instance);
        if (ins == null) {
            UAVTalkMissingObjectException e = new UAVTalkMissingObjectException();
            e.setInstance(instance);
            e.setObjectname(objectname);
            e.setIsSettings(xmlobj.getIsSettings());
            throw e;
        }

        byte[] data = ins.getData();

        int[] fl = xmlobj.getFieldlengths();

        int pos = xmlfield.pos;

        Object retval = null;
        if (data != null) {
            if (xmlfield.type == UAVTalkXMLObject.FIELDTYPE_FLOAT32) {
                byte[] fielddata = new byte[4];
                float f = 0;
                System.arraycopy(data, pos, fielddata, 0, 4);
                f = ByteBuffer.wrap(fielddata).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                f = (float) Math.round(f * 100) / 100;
                //retval = String.valueOf(f);
                retval = f;
            } else if (xmlfield.type == UAVTalkXMLObject.FIELDTYPE_ENUM) {
                //byte[] fielddata = new byte[1];
                byte b = 0;
                b = data[pos + element];
                retval = xmlfield.options[H.toInt(b)];
                //retval = b;
            } else if (xmlfield.type == UAVTalkXMLObject.FIELDTYPE_UINT32) {
                byte[] fielddata = new byte[4];
                System.arraycopy(data, pos, fielddata, 0, 4);
                int i = ByteBuffer.wrap(fielddata).order(ByteOrder.LITTLE_ENDIAN).getInt();
                Long l = (long) i;
                //retval = String.valueOf(l);
                retval = l;
            } else if (xmlfield.type == UAVTalkXMLObject.FIELDTYPE_INT32) {
                byte[] fielddata = new byte[4];
                System.arraycopy(data, pos, fielddata, 0, 4);
                int i = ByteBuffer.wrap(fielddata).order(ByteOrder.LITTLE_ENDIAN).getInt();
                Long l = (long) i & 0xFFFFFFFFL;
                retval = l;
            } else if (xmlfield.type == UAVTalkXMLObject.FIELDTYPE_UINT8) {
                byte[] fielddata = new byte[1];
                System.arraycopy(data, pos + element, fielddata, 0, 1);
                int i = fielddata[0] & 0xff;
                //retval = String.valueOf(i);
                retval = i;
            } else if (xmlfield.type == UAVTalkXMLObject.FIELDTYPE_INT8) {
                byte[] fielddata = new byte[1];
                System.arraycopy(data, pos, fielddata, 0, 1);
                int i = fielddata[0];
                //retval = String.valueOf(i);
                retval = i;
            } else {
                retval = "Type not implemented";
            }
        }
        return retval;
    }
}
