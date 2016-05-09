package org.librepilot.lp2go.ui.objectbrowser.scope;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

/**
 * Created by Marcus on 25.04.2016.
 */
public class ObjectLineDataSet extends LineDataSet {
    private String mElement;
    private String mFieldname;
    private int mInstance;
    private String mObjectName;

    public ObjectLineDataSet(List<Entry> yVals, String label) {
        super(yVals, label);
    }
}
