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

import android.content.Context;
import android.util.Log;

import net.proest.lp2go3.H;
import net.proest.lp2go3.MainActivity;

import java.io.FileOutputStream;
import java.util.Arrays;

public class UAVTalkDevice implements UAVTalkDeviceInterface {
    private static boolean isInstanciated;
    MainActivity mActivity;
    private FileOutputStream logOutputStream;
    private String logFileName = "OP-YYYY-MM-DD_HH-MM-SS";
    private long logStartTimeStamp;
    private boolean isLogging = false;
    private long logBytesLoggedUAV = 0;
    private long logBytesLoggedOPL = 0;
    private long logObjectsLogged = 0;

    public UAVTalkDevice(MainActivity mActivity) throws IllegalStateException {
        this.mActivity = mActivity;
    }

    public long getLogBytesLoggedUAV() {
        return logBytesLoggedUAV;
    }

    public long getLogBytesLoggedOPL() {
        return logBytesLoggedOPL;
    }

    public long getLogObjectsLogged() {
        return this.logObjectsLogged;
    }

    public long getLogStartTimeStamp() {
        return this.logStartTimeStamp;
    }

    public boolean isLogging() {
        return this.isLogging;
    }

    public void setLogging(boolean logNow) {
        if (isLogging == logNow) {   //if we are already logging, and we should start, just return
            return;                 //if we are not logging and should stop, nothing to do as well
        }

        isLogging = logNow;

        try {       //anyway, close the current stream
            logOutputStream.close();
        } catch (Exception e) {
            // e.printStackTrace();
        }

        if (isLogging) {  //if logging should start, create new stream
            mActivity.deleteFile(logFileName); //delete old log
            logFileName = H.getLogFilename();

            try {
                logOutputStream = mActivity.openFileOutput(logFileName, Context.MODE_PRIVATE);
                //outputStream.write(string.getBytes());
                //outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            logStartTimeStamp = System.currentTimeMillis();  //and set the time offset
            logBytesLoggedOPL = 0;
            logBytesLoggedUAV = 0;
            logObjectsLogged = 0;
        }
        Log.d("setLogging", "" + isLogging());
    }

    public void log(byte[] b) {
        if (b == null) return;
        try {
            long time = System.currentTimeMillis() - logStartTimeStamp;
            long len = b.length;

            byte[] btime = Arrays.copyOfRange(H.reverse8bytes(H.toBytes(time)), 0, 4);
            byte[] blen = H.reverse8bytes(H.toBytes(len));

            byte msg[] = H.concatArray(btime, blen);
            msg = H.concatArray(msg, b);

            logOutputStream.write(msg);
            logBytesLoggedUAV += b.length;
            logBytesLoggedOPL += msg.length;
            logObjectsLogged++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public UAVTalkObjectTree getoTree() {
        return null;
    }

    @Override
    public boolean sendSettingsObject(String objectName, int instance, String fieldName, int element, byte[] newFieldData) {
        return false;
    }

    @Override
    public boolean requestObject(String objectName) {
        return false;
    }

    @Override
    public boolean requestObject(String objectName, int instance) {
        return false;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean isConnecting() {
        return false;
    }


    public String getLogFileName() {
        return logFileName;
    }
}
