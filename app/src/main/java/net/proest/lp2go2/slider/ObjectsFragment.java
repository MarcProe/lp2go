package net.proest.lp2go2.slider;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.proest.lp2go2.R;

public class ObjectsFragment extends Fragment {

    public ObjectsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_objects, container, false);

        return rootView;
    }
}