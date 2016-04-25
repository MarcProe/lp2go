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

package net.proest.lp2go3.UAVTalk;

import net.proest.lp2go3.H;
import net.proest.lp2go3.VisualLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UAVTalkObjectTree {

    private final ConcurrentHashMap<String, UAVTalkObject> objects;
    private Map<String, UAVTalkXMLObject> xmlObjects;

    public UAVTalkObjectTree() {
        objects = new ConcurrentHashMap<String, UAVTalkObject>();
    }

    public String toString() {
        String ret = "";
        Iterator<String> i = xmlObjects.keySet().iterator();
        while (i.hasNext()) {
            String key = i.next();
            ret += xmlObjects.get(key).getId() + " " + key + "\r\n";
        }
        return ret;
    }

    public Map<String, UAVTalkXMLObject> getXmlObjects() {
        return xmlObjects;
    }

    public void setXmlObjects(Map<String, UAVTalkXMLObject> xmlObjects) {
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
            VisualLog.d("OFN", "CREATED NEW OBJECT! " + name + " " + oID);
            obj = new UAVTalkObject(oID);
        }
        return obj;
    }

    public void updateObject(UAVTalkObject obj) {
        try {
            if (objects.get(obj.getId()) == null || !objects.get(obj.getId()).isWriteBlocked()) { //FIXME: This is maybe expensive
                objects.put(obj.getId(), obj);
            }
        } catch (NullPointerException e) {
            VisualLog.w("WAR", "objects not initialized");
        }
    }

    public byte[] getInstanceData(String objectname, int instance) {
        UAVTalkObject obj = getObjectFromName(objectname);
        UAVTalkObjectInstance ins = obj.getInstance(instance);
        if (ins == null) {
            return null;
        }
        return ins.getData();
    }

    public int getElementIndex(String objectname, String fieldname, String element) {
        int retval;
        UAVTalkXMLObject xmlobj = xmlObjects.get(objectname);
        UAVTalkXMLObject.UAVTalkXMLObjectField xmlfield = xmlobj.getFields().get(fieldname);


        retval = xmlfield.mElements.indexOf(element);

        //VisualLog.d(xmlobj.getId(), Arrays.toString(xmlfield.getElements().toArray()) + "~" +element+"#" +retval);

        // -1 is the return value if indexOf does not return anything
        // that means there is only the default element
        if (retval < 0) retval = 0;

        return retval;
    }

    public Object getData(String objectname, String fieldname) throws UAVTalkMissingObjectException {
        return getData(objectname, 0, fieldname, 0);
    }

    public Object getData(String objectname, String fieldname, String element) throws UAVTalkMissingObjectException {
        return getData(objectname, 0, fieldname, element);
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
            e.setIsSettings(xmlobj.isSettings());
            throw e;
        }

        UAVTalkObjectInstance ins = obj.getInstance(instance);
        if (ins == null) {
            UAVTalkMissingObjectException e = new UAVTalkMissingObjectException();
            e.setInstance(instance);
            e.setObjectname(objectname);
            e.setIsSettings(xmlobj.isSettings());
            throw e;
        }

        byte[] data = ins.getData();

        int pos = xmlfield.mPos;

        Object retval = null;
        if (data != null) {
            switch (xmlfield.mType) {
                case (UAVTalkXMLObject.FIELDTYPE_ENUM): {
                    byte[] fielddata = new byte[1];
                    byte b = data[pos + element];
                    fielddata[0] = b;
                    try {
                        retval = xmlfield.mOptions[H.toInt(b)];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        VisualLog.d("AIOOBE", "" + H.toInt(b) + " " + data.length + " " + b + " " +
                                H.bytesToHex(fielddata) + " " + H.bytesToHex(data) + " " + pos +
                                " " + element);
                    }
                    break;
                }
                case (UAVTalkXMLObject.FIELDTYPE_FLOAT32): {
                    byte[] fielddata = new byte[4];
                    float f = 0;
                    System.arraycopy(data, pos + element * 4, fielddata, 0, 4);
                    f = ByteBuffer.wrap(fielddata).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    retval = f;
                    break;
                }
                case (UAVTalkXMLObject.FIELDTYPE_UINT32): {
                    byte[] fielddata = new byte[8]; //we need 8 bytes to get a long value
                    System.arraycopy(data, pos + element * 4, fielddata, 0, 4);
                    long l = ByteBuffer.wrap(fielddata).order(
                            //set 4 higer bytes to zero (truncates sign)
                            ByteOrder.LITTLE_ENDIAN).getLong() & 0xffffff;
                    retval = l;
                    break;
                }
                case (UAVTalkXMLObject.FIELDTYPE_INT32): {
                    byte[] fielddata = new byte[4];
                    System.arraycopy(data, pos, fielddata, 0, 4);
                    int i = ByteBuffer.wrap(fielddata).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    retval = i;
                    break;
                }
                case (UAVTalkXMLObject.FIELDTYPE_UINT16): {
                    byte[] fielddata = new byte[4]; //we need four bytes to get an integer value
                    System.arraycopy(data, pos + element * 2, fielddata, 0, 2);
                    int i = ByteBuffer.wrap(fielddata).order(
                            //set 2 higher bytes to zero (truncates sign)
                            ByteOrder.LITTLE_ENDIAN).getInt() & 0xffff;
                    retval = i;
                    break;
                }
                case (UAVTalkXMLObject.FIELDTYPE_INT16): {
                    byte[] fielddata = new byte[2];
                    System.arraycopy(data, pos + element * 2, fielddata, 0, 2);
                    int i = ByteBuffer.wrap(fielddata).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    retval = i;
                    break;
                }
                case (UAVTalkXMLObject.FIELDTYPE_UINT8): {
                    byte[] fielddata = new byte[1];
                    System.arraycopy(data, pos + element, fielddata, 0, 1);
                    int i = fielddata[0] & 0xff;
                    retval = i;
                    break;
                }
                case (UAVTalkXMLObject.FIELDTYPE_INT8): {
                    byte[] fielddata = new byte[1];
                    System.arraycopy(data, pos + element, fielddata, 0, 1);
                    int i = fielddata[0];
                    retval = i;
                    break;
                }
                default: {
                    retval = "Type not implemented";
                    break;
                }
            }
        }
        return retval;
    }
}
