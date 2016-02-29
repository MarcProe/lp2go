package net.proest.lp2go2.UAVTalk;

import android.app.Activity;
import android.content.Context;

import net.proest.lp2go2.H;

import java.io.FileOutputStream;
import java.util.Arrays;

/**
 * Created by Marcus on 29.02.2016.
 */
public class UAVTalkDevice implements UAVTalkDeviceInterface {
    Activity mActivity;
    private FileOutputStream logOutputStream;
    private long logStartTimeStamp;
    private boolean isLogging = false;

    public UAVTalkDevice(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public boolean isLogging() {
        return isLogging;
    }

    public void setLogging(boolean logNow) {
        if (isLogging == logNow) {   //if we are already logging, and we should start, just return
            return;                 //if we are not logging and should stop, nothing to do as well
        }

        isLogging = logNow;

        String filename = "oplog";
        String string = "Hello world!";

        try {       //anyway, close the current stream
            logOutputStream.close();
        } catch (Exception e) {
            // e.printStackTrace();
        }

        if (isLogging) {  //if logging should start, create new stream
            try {
                logOutputStream = mActivity.openFileOutput(filename, Context.MODE_PRIVATE);
                //outputStream.write(string.getBytes());
                //outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            logStartTimeStamp = System.currentTimeMillis();  //and set the time offset
        }
    }

    public void log(byte[] b) {
        if (b == null) return;
        try {
            long time = System.currentTimeMillis() - logStartTimeStamp;
            long len = b.length;

            byte[] btime = Arrays.copyOfRange(H.reverse8bytes(H.toBytes(time)), 0, 3);
            byte[] blen = H.reverse8bytes(H.toBytes(time));

            byte msg[] = H.concatArray(btime, blen);
            msg = H.concatArray(msg, b);

            logOutputStream.write(msg);
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
    public boolean setConnected(boolean connected) {
        return false;
    }
}
