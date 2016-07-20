package org.librepilot.lp2go.uavtalk.device;

import org.librepilot.lp2go.MainActivity;
import org.librepilot.lp2go.uavtalk.UAVTalkObjectTree;
import org.librepilot.lp2go.uavtalk.UAVTalkXMLObject;

import java.util.Map;

/**
 * Created by marc on 03.07.2016.
 */
public class FcLogfileDevice extends FcDevice {


    private final FcWaiterThread mWaiterThread;

    public FcLogfileDevice(MainActivity mActivity, String filename, Map<String, UAVTalkXMLObject> xmlObjects) throws IllegalStateException {
        super(mActivity);

        mObjectTree = new UAVTalkObjectTree();
        mObjectTree.setXmlObjects(xmlObjects);
        mActivity.setPollThreadObjectTree(mObjectTree);

        mWaiterThread = new FcLogfileWaiterThread(this, filename);
    }

    @Override
    public boolean isConnected() {
        return mWaiterThread != null;
    }

    @Override
    public boolean isConnecting() {
        return false;
    }

    @Override
    public void start() {
        mWaiterThread.start();
    }

    @Override
    public void stop() {
        synchronized (mWaiterThread) {
            mWaiterThread.stopThread();
        }
    }

    @Override
    public boolean sendAck(String objectId, int instance) {
        return true;
    }

    @Override
    public boolean sendSettingsObject(String objectName, int instance) {
        return true;
    }

    @Override
    public boolean sendSettingsObject(String objectName, int instance, String fieldName, int element, byte[] newFieldData, boolean block) {
        return true;
    }

    @Override
    public boolean requestObject(String objectName) {
        return true;
    }

    @Override
    public boolean requestObject(String objectName, int instance) {
        return true;
    }

    @Override
    protected boolean writeByteArray(byte[] bytes) {
        return true;
    }
}
