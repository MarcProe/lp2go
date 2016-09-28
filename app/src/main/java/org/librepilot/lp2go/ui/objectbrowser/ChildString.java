/*
 * @file   ChildString.java
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

import java.text.MessageFormat;

public class ChildString {
    protected String data;
    protected String element;
    protected String fieldname;
    protected int instance;
    protected boolean isInstanceHeader;
    protected boolean isSettings;
    protected String objectname;
    protected int type;

    private String message;

    public ChildString(String objectname, int instance, String fieldname, String element,
                       String data, int type, boolean isSettings) {
        this.objectname = objectname;
        this.fieldname = fieldname;
        this.instance = instance;
        this.element = element;
        this.data = data;
        this.type = type;
        this.isSettings = isSettings;
        isInstanceHeader = false;

        if (this.element == null || "".equals(this.element)) {
            this.element = "";
        } else {
            this.element = MessageFormat.format("-{0}", this.element);
        }
        message = MessageFormat.format("{0} {1}", fieldname, this.element);
    }

    public ChildString(String message) {
        isInstanceHeader = false;
        isSettings = false;
        this.message = message;
    }

    public ChildString(int instance) {
        isInstanceHeader = true;
        isSettings = false;
        this.instance = instance;
        this.message = MessageFormat.format("Instance: {0}", instance);
    }

    public String getLabel() {
        return message;
    }

    public String getValue() {
        return data;
    }

    @Override
    public String toString() {
        return message;
    }

}
