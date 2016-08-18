package org.librepilot.lp2go.uavtalk.device;

import org.librepilot.lp2go.H;
import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.VisualLog;
import org.librepilot.lp2go.uavtalk.UAVTalkMessage;
import org.librepilot.lp2go.uavtalk.UAVTalkObject;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectInstance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

public class FcLogfileWaiterThread extends FcWaiterThread {
    protected int mSkipForward = 0;
    private String mFilename = null;
    private boolean mStop;
    private boolean mPaused = false;
    private Queue<Byte> queue;

    public FcLogfileWaiterThread(FcDevice device, String filename) {
        super(device);
        this.mFilename = filename;
    }

    public boolean isPaused() {
        return mPaused;
    }

    public void setPaused(boolean mPaused) {
        this.mPaused = mPaused;
    }

    private byte[] bufferRead(int len) {
        byte[] retval = new byte[len];
        for (int i = 0; i < len; i++) {
            if (!queue.isEmpty()) {
                retval[i] = queue.remove();
            }
        }
        return retval;
    }

    private void reportState(int i) {
        if (mGuiEventListener != null) {
            mGuiEventListener.reportState(i);
        }
    }

    public void run() {

        reportState(FcDevice.GEL_RUNNING);
        queue = new ArrayDeque<>();

        byte[] logtimestampbuffer = new byte[4];
        byte[] logdatasizebuffer = new byte[8];

        byte[] syncbuffer = new byte[1];
        byte[] msgtypebuffer = new byte[1];
        byte[] lenbuffer = new byte[2];
        byte[] oidbuffer = new byte[4];
        byte[] iidbuffer = new byte[2];
        byte[] timestampbuffer = new byte[2];
        byte[] databuffer;
        byte[] crcbuffer = new byte[1];

        mDevice.mActivity.setRxObjectsGood(0);
        mDevice.mActivity.setRxObjectsBad(0);
        mDevice.mActivity.setTxObjects(0);

        final MainActivity ma = mDevice.mActivity;

        final File logFile = new File(ma.getFilesDir(), mFilename);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(logFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //wait a second before starting
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //Thread wakes up
        }

        int previousTimestamp = 0;

        while (true) {

            if (mPaused) {
                mGuiEventListener.reportState(FcDevice.GEL_PAUSED);
                //wait a second before starting
                try {
                    Thread.sleep(500);

                } catch (InterruptedException e) {

                }
                continue;
            } else {
                mGuiEventListener.reportState(FcDevice.GEL_RUNNING);
            }

            byte[] buffer = new byte[64];
            int read = 0;
            try {
                while (queue.size() < 350 && fis != null) {

                    read = fis.read(buffer);

                    if (read == 0) {
                        break;
                    }

                    try {
                        for (int i = 0; i < read; i++) {
                            queue.add(buffer[i]);
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        VisualLog.e("FcLogFileWaiterThread", "AIOOBE in LogFile Queue filler");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    fis.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                fis = null;
            }

            if (mStop || queue.size() == 0) {
                VisualLog.i("LOGFILE", "Done.");
                reportState(FcDevice.GEL_STOPPED);
                this.stopThread();
                return;
            }

            logtimestampbuffer = bufferRead(4);
            int ts = H.toInt(H.reverse4bytes(logtimestampbuffer));
            VisualLog.d("SYNC", "logtimestampbuffer! " + H.bytesToHex(logtimestampbuffer) + " " + ts);

            logdatasizebuffer = bufferRead(8); //since this is a redundant information, ignore.
            //we will need this, however, once we implement skipping of frames.

            //wait for the timestamp
            final int wait;

            if (mSkipForward > 0) {      //just don't wait if skipforward is set
                wait = 0;               //however, we will read the data and save it, to
                mSkipForward--;         //maintain the ObjectTree state
            } else {
                wait = ts - previousTimestamp;
            }

            previousTimestamp = ts;

            if (wait > 0) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    //Thread wakes up
                }
            }

            syncbuffer[0] = 0x00;
            while (syncbuffer[0] != 0x3c) {
                syncbuffer = bufferRead(1);
            }

            msgtypebuffer = bufferRead(msgtypebuffer.length);

            lenbuffer = bufferRead(lenbuffer.length);

            int lb1 = lenbuffer[1] & 0x000000ff;
            int lb2 = lenbuffer[0] & 0x000000ff;
            int len = lb1 << 8 | lb2;

            if (len > 266 || len < 10) {
                mDevice.mActivity.incRxObjectsBad();
                continue; // maximum possible packet size
            }

            oidbuffer = bufferRead(oidbuffer.length);
            iidbuffer = bufferRead(iidbuffer.length);

            int tsoffset = 0;
            if ((MASK_TIMESTAMP & msgtypebuffer[0]) == MASK_TIMESTAMP) {
                timestampbuffer = bufferRead(timestampbuffer.length);
                tsoffset = 2;
            }

            databuffer = bufferRead(len - (10 + tsoffset));
            crcbuffer = bufferRead(crcbuffer.length);

            byte[] bmsg = H.concatArray(syncbuffer, msgtypebuffer);
            bmsg = H.concatArray(bmsg, lenbuffer);
            bmsg = H.concatArray(bmsg, oidbuffer);
            bmsg = H.concatArray(bmsg, iidbuffer);
            if ((MASK_TIMESTAMP & msgtypebuffer[0]) == MASK_TIMESTAMP) {
                bmsg = H.concatArray(bmsg, timestampbuffer);
            }
            bmsg = H.concatArray(bmsg, databuffer);
            int crc = H.crc8(bmsg, 0, bmsg.length);
            bmsg = H.concatArray(bmsg, crcbuffer);

            if ((((int) crcbuffer[0] & 0xff) == (crc & 0xff))) {
                mDevice.mActivity.incRxObjectsGood();
            } else {
                mDevice.mActivity.incRxObjectsBad();
                VisualLog.d("USB", "Bad CRC");
                continue;
            }

            try {
                UAVTalkMessage msg = new UAVTalkMessage(bmsg, 0);
                UAVTalkObject myObj =
                        mDevice.mObjectTree.getObjectFromID(H.intToHex(msg.getObjectId()));
                UAVTalkObjectInstance myIns;

                try {
                    myIns = myObj.getInstance(msg.getInstanceId());
                    myIns.setData(msg.getData());
                    myObj.setInstance(myIns);
                } catch (Exception e) {
                    myIns = new UAVTalkObjectInstance(msg.getInstanceId(), msg.getData());
                    myObj.setInstance(myIns);
                }

                if (handleMessageType(msgtypebuffer[0], myObj)) {
                    mDevice.mObjectTree.updateObject(myObj);
                    if (mDevice.isLogging()) {
                        mDevice.log(msg);
                    }
                }
            } catch (Exception e) {
                VisualLog.e(e);
            }
        }
    }

    @Override
    protected void stopThread() {
        this.mStop = true;
    }
}
