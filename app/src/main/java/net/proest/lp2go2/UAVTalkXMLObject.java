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
package net.proest.lp2go2;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class UAVTalkXMLObject {

    public static final int FIELDTYPE_INT8 = 0;
    public static final int FIELDTYPE_INT16 = 1;
    public static final int FIELDTYPE_INT32 = 2;
    public static final int FIELDTYPE_UINT8 = 3;
    public static final int FIELDTYPE_UINT16 = 4;
    public static final int FIELDTYPE_UINT32 = 5;
    public static final int FIELDTYPE_FLOAT32 = 6;
    public static final int FIELDTYPE_ENUM = 7;
    public static final String FIELDNAME_INT8 = "int8";
    public static final String FIELDNAME_INT16 = "int16";
    public static final String FIELDNAME_INT32 = "int32";
    public static final String FIELDNAME_UINT8 = "uint8";
    public static final String FIELDNAME_UINT16 = "uint16";
    public static final String FIELDNAME_UINT32 = "uint32";
    public static final String FIELDNAME_FLOAT32 = "float";
    public static final String FIELDNAME_ENUM = "enum";
    private static final String XML_TAG_OBJECT = "object";
    private static final String XML_TAG_FIELD = "field";
    private static final String XML_TAG_OPTIONS = "options";
    private static final String XML_TAG_ELEMENTNAMES = "elementnames";
    private static final String XML_ATT_NAME = "name";
    private static final String XML_ATT_CATEGORY = "category";
    private static final String XML_ATT_SINGLEINSTANCE = "singleinstance";
    private static final String XML_ATT_SETTINGS = "settings";
    private static final String XML_ATT_CLONEOF = "cloneof";
    private static final String XML_ATT_TYPE = "type";
    private static final String XML_ATT_ELEMENTS = "elements";
    private static final String XML_ATT_ELEMENTNAMES = "elementnames";
    private static final String XML_ATT_OPTIONS = "options";
    private static final String XML_TRUE = "true";
    final private static String REPLACE_OPTION_NODES = "\\r\\n|\\r|\\n| |\\t";
    final private static String REPLACE_ELEMENT_NODES = "\\r\\n|\\r|\\n| |\\t";
    final private static String XML_ATTRIBUTE_SPLITTER = "\\s*,\\s*";

    private final boolean DBG = false;
    private String xml;
    private String name;
    private String category;
    private Boolean isSettings;
    private Boolean isSingleInst;
    private int id;
    private Hashtable<String, Integer> fieldnames;
    private int[] fieldlengths;
    private Hashtable<String, UAVTalkXMLObjectField> fields;
    private UAVTalkXMLObjectField[] fieldArr;

    public UAVTalkXMLObject(String xml) throws IOException, SAXException, ParserConfigurationException {
        this.xml = xml;

        //TODO: Make this final
        fieldnames = new Hashtable<String, Integer>();
        fieldnames.put(FIELDNAME_INT8, Integer.valueOf(FIELDTYPE_INT8));
        fieldnames.put(FIELDNAME_INT16, Integer.valueOf(FIELDTYPE_INT16));
        fieldnames.put(FIELDNAME_INT32, Integer.valueOf(FIELDTYPE_INT32));
        fieldnames.put(FIELDNAME_UINT8, Integer.valueOf(FIELDTYPE_UINT8));
        fieldnames.put(FIELDNAME_UINT16, Integer.valueOf(FIELDTYPE_UINT16));
        fieldnames.put(FIELDNAME_UINT32, Integer.valueOf(FIELDTYPE_UINT32));
        fieldnames.put(FIELDNAME_FLOAT32, Integer.valueOf(FIELDTYPE_FLOAT32));
        fieldnames.put(FIELDNAME_ENUM, Integer.valueOf(FIELDTYPE_ENUM));

        Document doc = loadXMLFromString(this.xml);

        NodeList objectNodeList = doc.getElementsByTagName(XML_TAG_OBJECT);
        Node objectNode = objectNodeList.item(0);
        Element e = (Element) objectNode;

        name = e.getAttribute(XML_ATT_NAME);
        category = e.getAttribute(XML_ATT_CATEGORY);
        isSingleInst = e.getAttribute(XML_ATT_SINGLEINSTANCE).equals(XML_TRUE);
        isSettings = e.getAttribute(XML_ATT_SETTINGS).equals(XML_TRUE);

        NodeList fieldNodeList = doc.getElementsByTagName(XML_TAG_FIELD);
        fields = new Hashtable<String, UAVTalkXMLObjectField>();
        fieldArr = new UAVTalkXMLObjectField[fieldNodeList.getLength()];
        fieldlengths = new int[fieldNodeList.getLength()];
        int x = 0;

        for (int i = 0; i < fieldNodeList.getLength(); i++) {
            Node fieldNode = fieldNodeList.item(i);
            UAVTalkXMLObjectField uavField = new UAVTalkXMLObjectField();
            Element f = (Element) fieldNode;

            String sclone = f.getAttribute(XML_ATT_CLONEOF);
            uavField.name = f.getAttribute(XML_ATT_NAME);

            if (sclone != null && sclone != "") {
                String tn = uavField.name;
                uavField = fields.get(sclone);
                uavField.name = tn;
            } else {

                uavField.type = fieldnames.get(f.getAttribute(XML_ATT_TYPE)).intValue();

                String elementString = f.getAttribute(XML_ATT_ELEMENTNAMES);

                uavField.elements =
                        new ArrayList<String>(Arrays.asList(elementString.split(XML_ATTRIBUTE_SPLITTER)));

                String elementCountString = f.getAttribute(XML_ATT_ELEMENTS);

                if (elementCountString != "" && elementCountString != null) {
                    uavField.elementCount = Integer.parseInt(elementCountString);
                    if (uavField.type == FIELDTYPE_ENUM
                            && f.getElementsByTagName(XML_TAG_ELEMENTNAMES).getLength() == 0) {
                        uavField.elements.clear();
                        for (int j = 0; j < uavField.elementCount; j++) {
                            uavField.elements.add(String.valueOf(j));
                        }
                    }
                }


                //if ((uavField.elements.size() == 1) && (uavField.elements.get(0) == "")) {
                //uavField.elements = new String[0];
                //}
                if (uavField.type == FIELDTYPE_ENUM) {

                    String optionsString = f.getAttribute(XML_ATT_OPTIONS);
                    try {
                        uavField.options =
                                (String[]) Arrays.asList(optionsString.split(XML_ATTRIBUTE_SPLITTER)).toArray();
                    } catch (Exception ex) {
                    }

                    if (uavField.options == null || uavField.options.length == 0
                            || ((uavField.options.length == 1) && (uavField.options[0] == ""))) {
                        if (f.getElementsByTagName(XML_TAG_OPTIONS).getLength() > 0) {
                            NodeList optionnodes =
                                    f.getElementsByTagName(XML_TAG_OPTIONS).item(0).getChildNodes();

                            ArrayList<String> options = new ArrayList<String>();
                            for (int j = 0; j < optionnodes.getLength(); j++) {
                                String content =
                                        optionnodes.item(j).getTextContent().replaceAll(REPLACE_OPTION_NODES, "");
                                if (content != null && content != "") {
                                    options.add(content);
                                }
                            }
                            uavField.options = options.toArray(new String[options.size()]);
                        }
                    }
                }

                if (f.getElementsByTagName(XML_TAG_ELEMENTNAMES).getLength() > 0) {
                    NodeList elementnodes = f.getElementsByTagName(XML_TAG_ELEMENTNAMES).item(0).getChildNodes();

                    uavField.elements = new ArrayList<String>();
                    for (int j = 0; j < elementnodes.getLength(); j++) {
                        String content = elementnodes.item(j).getTextContent().replaceAll(REPLACE_ELEMENT_NODES, "");
                        if (content != null && !content.equals("")) {
                            uavField.elements.add(content);
                        }
                    }
                }

                if (uavField.elementCount == 0) {
                    uavField.elementCount = uavField.elements.size();
                }
            }

            if (uavField.type == FIELDTYPE_INT8 || uavField.type == FIELDTYPE_UINT8) {
                fieldlengths[x] = /*1 * */uavField.elementCount;
                uavField.size = /*1 * */uavField.elementCount;
                uavField.typelength = 1;
            } else if (uavField.type == FIELDTYPE_INT16 || uavField.type == FIELDTYPE_UINT16) {
                fieldlengths[x] = 2 * uavField.elementCount;
                uavField.size = 2 * uavField.elementCount;
                uavField.typelength = 2;
            } else if (uavField.type == FIELDTYPE_INT32 || uavField.type == FIELDTYPE_UINT32 || uavField.type == FIELDTYPE_FLOAT32) {
                fieldlengths[x] = 4 * uavField.elementCount;
                uavField.size = 4 * uavField.elementCount;
                uavField.typelength = 4;
            } else if (uavField.type == FIELDTYPE_ENUM) {
                fieldlengths[x] = uavField.elementCount;
                uavField.size = uavField.elementCount;
                uavField.typelength = 1;
            }
            x++;

            fields.put(uavField.name, uavField);
            fieldArr[i] = uavField;
        }

        Arrays.sort(fieldArr);
        fields.clear(); //TODO:This is crappy code!
        int j = 0;
        for (UAVTalkXMLObjectField xuav : fieldArr) {
            xuav.pos = j;     //set new position
            j += xuav.typelength * xuav.elementCount;
            fields.put(xuav.name, xuav);
        }

        if (DBG) {
            Log.d(name, Arrays.toString(fieldArr));
        }

        this.id = calculateID();
        if (DBG) Log.d("TTT", H.intToHex(this.id));
    }

    public static Document loadXMLFromString(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    public int[] getFieldlengths() {
        return fieldlengths;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public Boolean getIsSettings() {
        return isSettings;
    }

    public Boolean getIsSingleInst() {
        return isSingleInst;
    }

    public String getId() {
        return H.bytesToHex(H.toBytes(id));
    }

    public int getIntId() {
        return this.id;
    }

    public Hashtable<String, UAVTalkXMLObjectField> getFields() {
        return fields;
    }

    private int calculateID() {
        // Hash object name
        int hash = updateHash(this.name, 0);
        //long y = hash & 0x00000000ffffffffL;
        // Hash object attributes
        hash = updateHash(this.isSettings.booleanValue() ? 1 : 0, hash);
        hash = updateHash(this.isSingleInst.booleanValue() ? 1 : 0, hash);
        // Hash field information
        for (int n = 0; n < this.fieldArr.length; n++) {
            if (DBG) {
                Log.d("FLD", this.fieldArr[n].name);
            }
            hash = updateHash(this.fieldArr[n].name, hash);
            hash = updateHash((this.fieldArr[n].elementCount), hash);
            hash = updateHash(this.fieldArr[n].type, hash);
            if (this.fieldArr[n].type == FIELDTYPE_ENUM) {
                String[] options = this.fieldArr[n].options;
                for (int m = 0; m < options.length; m++) {
                    hash = updateHash(options[m], hash);
                }
            }
        }
        return hash & 0xFFFFFFFE;
    }

    private int updateHash(int value, int hash) {
        int ret = hash ^ ((hash << 5) + (hash >>> 2) + value);
        if (DBG) {
            long y = ret & 0x00000000ffffffffL;
            long in = value & 0x00000000ffffffffL;
            Log.d("HASH", "" + in + "=>" + y);
        }
        return ret;
    }

    /**
     * Update the hash given a string
     */
    private int updateHash(String value, int hash) {
        byte[] bytes = value.getBytes();
        int hashout = hash;
        for (int n = 0; n < bytes.length; ++n) {
            hashout = updateHash(bytes[n], hashout);
        }

        return hashout;
    }

    //TODO: Getter and Setter
    protected class UAVTalkXMLObjectField implements Comparable<UAVTalkXMLObjectField> {
        protected String name;
        protected ArrayList<String> elements;
        protected int elementCount;
        protected int type;
        protected int typelength;
        protected String[] options;
        protected int pos;
        protected int size;

        @Override
        public int compareTo(UAVTalkXMLObjectField another) {
            return another.typelength - typelength;
        }

        public String toString() {
            return name + " Pos: " + pos + " ElementCount: " + elementCount + " Size:" + size + " Type:" + type;
        }
    }
}
