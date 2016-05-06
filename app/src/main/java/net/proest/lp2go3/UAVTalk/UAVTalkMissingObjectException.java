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

public class UAVTalkMissingObjectException extends Exception {
    private int mInstance;
    private boolean mIsSettings;
    private String mObjectName;

    public int getInstance() {
        return mInstance;
    }

    public void setInstance(int instance) {
        this.mInstance = instance;
    }

    public String getObjectname() {
        return mObjectName;
    }

    public void setObjectname(String objectname) {
        this.mObjectName = objectname;
    }

    public boolean isSettings() {
        return mIsSettings;
    }

    public void setIsSettings(boolean isSettings) {
        this.mIsSettings = isSettings;
    }
}
