package net.proest.lp2go3.UI.objectbrowser.scope;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

/**
 * Created by Marcus on 25.04.2016.
 */
public class ObjectLineDataSet extends LineDataSet {
    private String mObjectName;
    private int mInstance;
    private String mFieldname;
    private String mElement;

    public ObjectLineDataSet(List<Entry> yVals, String label) {
        super(yVals, label);
    }
}
